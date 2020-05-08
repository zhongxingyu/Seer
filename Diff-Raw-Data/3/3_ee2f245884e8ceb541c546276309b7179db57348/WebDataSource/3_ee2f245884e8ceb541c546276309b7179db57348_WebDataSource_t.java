 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.storage;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.wwscc.storage.SADateTime.SADate;
 import org.wwscc.util.ByteWriter;
 
 
 /**
  */
 public class WebDataSource extends SQLDataInterface
 {
 	private static Logger log = Logger.getLogger(WebDataSource.class.getCanonicalName());
 
 	public static final int INTTYPE = 1;
 	public static final int LONGTYPE = 2;
 	public static final int DOUBLETYPE = 3;
 	public static final int STRINGTYPE = 4;
 	public static final int BOOLEANTYPE = 5;
 	public static final int BYTEARRAYTYPE = 6;
 	public static final int DATETYPE = 7;
 	public static final int DATETIMETYPE = 8;
 
 	public static final int SELECT = 20;
 	public static final int UPDATE = 21;
 	public static final int FUNCTION = 22;
 
 	public static final int LASTID = 23;
 	public static final int ERROR = 24;
 	public static final int RESULTS = 25;
 
 	RemoteHTTPConnection server;
 	boolean buffering;
 	int lastId;
 	ByteWriter buffer;
 	DataOutputStream dataOutput;
 
 	public WebDataSource(String inHost, String inName) throws IOException
 	{
 		lastId = -1;
 		buffer = new ByteWriter();
 		dataOutput = new DataOutputStream(buffer);
 		buffering = false;
 
 		seriesName = inName;
 		host = inHost;
 		server = new RemoteHTTPConnection(host);
 	}
 
 	protected byte[] encodeValues(List<Object> args) throws IOException
 	{
 		if (args == null)
 			return new byte[0];
 		
 		ByteArrayOutputStream buf = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(buf);
 		for (Object v : args)
 		{
 			if (v instanceof Integer) {
 				out.write(INTTYPE);
 				out.writeInt((Integer)v);
 			} else if (v instanceof Long) {
 				out.write(LONGTYPE);
 				out.writeLong((Long)v);
 			} else if (v instanceof Double) {
 				out.write(DOUBLETYPE);
 				out.writeDouble((Double)v);
 			} else if (v instanceof String) {
 				String s = (String)v;
 				out.write(STRINGTYPE);
 				out.writeShort(s.length());
 				out.write(s.getBytes());
 			} else if (v instanceof Boolean) {
 				out.write(BOOLEANTYPE);
 				out.writeBoolean((Boolean)v);
 			} else if (v instanceof byte[]) {
 				byte[] b = (byte[])v;
 				out.write(BYTEARRAYTYPE);
 				out.writeShort(b.length);
 				out.write(b);
 			} else if (v instanceof SADate) {
 				SADate d = (SADate)v;
 				out.write(DATETYPE);
 				out.writeInt((int)d.getSeconds());
 			} else if (v instanceof SADateTime) {
 				SADateTime d = (SADateTime)v;
 				out.write(DATETIMETYPE);
 				out.writeInt((int)d.getSeconds());
 			} else if (v == null) {
 				out.write(STRINGTYPE);
 				out.writeShort(0);
 			} else {
 				throw new IOException("unexpected param type: " + v.getClass());
 			}
 		}
 
 		return buf.toByteArray();
 	} 
 
 	protected String decodeString(CountedDataInputStream in) throws IOException
 	{
 		return new String(in.readByteArray(in.readShort()));
 	}
 
 	/**
 	 * Decode object list which is a short size followed by encoded values
 	 * @param in the input stream to read from
 	 * @return the list of values read
 	 * @throws IOException
 	 */
 	protected List<Object> decodeValues(CountedDataInputStream in) throws IOException
 	{
 		List<Object> ret = new ArrayList<Object>();
 
 		// Set counter according to leading length
 		int size = in.readShort();
 		int start = in.getCounter();
 
 		// Keep expecting values until we've read to length
 		while (in.getCounter() < (start + size))
 		{
 			byte type = in.readByte();
 			switch (type)
 			{
 				case INTTYPE: ret.add(in.readInt()); break;
 				case LONGTYPE: ret.add(in.readLong());  break;
 				case DOUBLETYPE: ret.add(in.readDouble()); break;
 				case STRINGTYPE: ret.add(decodeString(in)); break;
 				case BOOLEANTYPE: ret.add(in.readBoolean()); break;
 				case BYTEARRAYTYPE: ret.add(in.readByteArray(in.readShort())); break;
 				case DATETYPE: ret.add(new SADate(in.readInt())); break;
 				case DATETIMETYPE: ret.add(new SADateTime(in.readInt())); break;
 				default:
 					throw new IOException("Invalid data type while decoding");
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Add a request to the data buffer being built for submission
 	 * @param type FUNCTION, UPDATE, SELECT
 	 * @param key the hash key for the sql statement or function
 	 * @param args the arguments for the command
 	 * @throws IOException
 	 * @todo need to escape commas in any data
 	 */
 	protected void addRequest(int type, String key, List<Object> args) throws IOException
 	{
 		dataOutput.writeByte(type);
 		dataOutput.writeShort(key.length());
 		dataOutput.write(key.getBytes());
 		byte[] values = encodeValues(args);
 		dataOutput.writeShort(values.length);
 		dataOutput.write(values);
 	}
 
 	/**
 	 * Send the buffered requests to the server and interpret the results.
 	 *		LASTID <int>
 	 *		RESULTS <int size> <header list> <object list> ...
 	 *		ERROR <string> <string> <string>
 	 * @return a ResultData structure with results or null
 	 * @throws java.io.IOException
 	 */
 	synchronized protected ResultData performRequests() throws IOException
 	{
 		if (buffer.size() <= 0)
 			return null;
 		
 		byte[] data = server.performSQL(seriesName, buffer.toByteArray());
 		buffer.clear();
 		CountedDataInputStream in = new CountedDataInputStream(new ByteArrayInputStream(data));
 
 		byte type = in.readByte();
 		switch (type)
 		{
 			case LASTID:
 				lastId = in.readInt();
 				return null;
 				
 			case RESULTS:
 				int size = in.readInt();
 				int start = in.getCounter();
 
 				ResultData ret = new ResultData();
 				if (size == 0)
 					return ret;
 				List<Object> head = decodeValues(in);
 				
 				while (in.getCounter() < (start + size))
 				{
 					ResultRow row = new ResultRow();
 					List<Object> values = decodeValues(in);
 					for (int ii = 0; ii < values.size(); ii++)
 						row.put((String)head.get(ii), values.get(ii));
 					ret.add(row);
 				}
 				return ret;
 
 			case ERROR:
 				StringBuilder b = new StringBuilder();
 				b.append("\n\nFile: " + decodeString(in));
 				b.append("\nLine: " + decodeString(in) + "\n");
 				b.append(decodeString(in));
 				throw new IOException(b.toString());
 				
 			default:
				throw new IOException("Invalid return value from server: " + type + "\n" +
								"String Value: (" + new String(data) + ")");
 		}
 	}
 
 	/**
 	 * Override the close method, nothing to do for us, just clear the buffers, etc.
 	 */
 	@Override
 	public void close()
 	{
 		rollback();
 	}
 
 	/**
 	 * Custom impl for each SQL source as attr is used in areas where SQL param are not allowed
 	 * @param attr the attribute the look for
 	 * @return a list of unique attributes for the car
 	 * @throws IOException 
 	 */
 	@Override
 	protected ResultData getCarAttributesImpl(String attr) throws IOException
 	{
 		addRequest(FUNCTION, "GetCarAttributes", newList(attr));
 		return performRequests();
 	}
 
 	/**
 	 * Override to speed up the process by not requiring the return of data to complete the function.
 	 * Also allows full transaction so lookup/delete/rewrite occurs atomically.  Also means update
 	 * timestamps are all using the same clock.
 	 * @param classcode class to update
 	 * @param carid carid that just finished
 	 */
 	@Override
 	protected void updateClassResults(String classcode, int carid)
 	{
 		try {
 			addRequest(FUNCTION, "UpdateClass", newList(currentEvent.id, classcode, carid));
 			performRequests();
 		} catch (IOException ioe) { 
 			log.log(Level.INFO, "updateClassResults failed", ioe);
 		}
 	}
 
 	/**
 	 * Implement executeGroupUpdate by adding an UPDATE request to the queue.
 	 * if we aren't in a 'transaction'.
 	 * @param key the sql key
 	 * @param args the list of arg lists
 	 * @throws java.io.IOException
 	 */
 	@Override
 	public void executeGroupUpdate(String key, List<List<Object>> args) throws IOException
 	{
 		for (List<Object> subargs : args)
 			addRequest(UPDATE, key, subargs);
 		if (buffering)
 			return;
 		performRequests();
 	}
 
 	/**
 	 * Implement executeUpdate by adding an UPDATE request to the queue.
 	 * @param key the sql key
 	 * @param args the arg list
 	 * @throws java.io.IOException
 	 */
 	@Override
 	public void executeUpdate(String key, List<Object> args) throws IOException
 	{
 		addRequest(UPDATE, key, args);
 		if (buffering)
 			return;
 		performRequests();
 	}
 
 
 	/**
 	 * Make a select request, this one always breaks the 'transaction' and forces
 	 * the request buffer to be sent as it expects data back.
 	 * @param key sql index to use
 	 * @param args list of args for the sql string
 	 * @return ResultData for the select request
 	 * @throws java.io.IOException
 	 */
 	@Override
 	public ResultData executeSelect(String key, List<Object> args) throws IOException
 	{
 		addRequest(SELECT, key, args);
 		return performRequests();
 	}
 
 	/**
 	 * Implement a 'transaction' start by beginning a queue of requests rather then
 	 * sending them immediatly.
 	 * @throws java.io.IOException
 	 */
 	@Override
 	public void start() throws IOException 
 	{
 		buffering = true;
 	}
 
 	/**
 	 * Implement a 'transaction' commit by sending the requests that have been queued
 	 * @throws java.io.IOException
 	 */
 	@Override
 	public void commit() throws IOException
 	{
 		buffering = false;
 		performRequests();
 	}
 
 	/**
 	 * Implement a 'transaction' rollback by clearing the current queue of requests.
 	 */
 	@Override
 	public void rollback()
 	{
 		buffering = false;
 		buffer.clear();
 	}
 
 	/**
 	 * Regardless of 'transaction', they want an ID so we need to send any buffer
 	 * requests to make sure that lastInsertID is from the last update request made.
 	 * @return the last insert id from the remote database
 	 * @throws java.io.IOException
 	 */
 	@Override
 	public int lastInsertId() throws IOException 
 	{
 		performRequests();
 		return lastId;
 	}
 
 	/**
 	 * Test the web data source.
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		try
 		{
 			WebDataSource d = new WebDataSource("127.0.0.1", "test");
 			d.currentEvent = new Event();
 			d.currentEvent.id = 7;
 			d.currentCourse = 1;
 			d.currentRunGroup = 1;
 			List<Entrant> l = d.getEntrantsByRunOrder();
 			System.out.println(l);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
 
