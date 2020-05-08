 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.wwscc.storage;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpRetryException;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import javax.swing.ProgressMonitor;
 import org.wwscc.util.Prefs;
 
 /**
  *
  * @author bwilson
  */
 public class RemoteHTTPConnection
 {
 	private static final Logger log = Logger.getLogger(RemoteHTTPConnection.class.getCanonicalName());
 	String host;
 	String dbname;
 	byte[] buffer;
 	ProgressMonitor monitor;
 	int transfered;
 
 	class AuthException extends IOException {}
 	class CancelException extends IOException {}
 
 	public RemoteHTTPConnection() throws UnknownHostException
 	{
 		this(Prefs.getHomeServer());
 	}
 
 	public RemoteHTTPConnection(String remote) throws UnknownHostException
 	{
 		host = remote;
 		dbname = "";
 		buffer = new byte[4096];
 	}
 
 	protected void transferData(InputStream in, OutputStream out) throws IOException
 	{
 		int len;
 		while((len = in.read(buffer)) >= 0)
 		{
 			if (len == 0)
 				continue;
 
 			out.write(buffer, 0, len);
 
 			if (monitor.isCanceled())
 				throw new CancelException();
 
 			transfered += len;
 			monitor.setProgress(transfered);
 		}
 		
 		in.close();
 	}
 
 	protected byte[] basicRequest(URL url, String method, String content, Object... args) throws IOException
 	{
 		while (true)
 		{
 			monitor.setProgress(0);
 			transfered = 0;
 
 			try
 			{
 				byte[] data = doRequest(url, method, content, args);
 				monitor.close();
 				return data;
 			}
 			catch (AuthException e)
 			{
 				String s = JOptionPane.showInputDialog("Password not accepted, please enter the proper password: ");
 				if (s == null) return null;
 				Prefs.setPasswordFor(dbname, s);
 			}
 			catch (CancelException ce)
 			{
 				monitor.close();
 				return null;
 			}
 			catch (IOException ioe)
 			{
 				monitor.close();
 				throw ioe;
 			}
 		}
 	}
 
 	protected byte[] doRequest(URL url, String method, String content, Object... args) throws IOException
 	{
 		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 		conn.setConnectTimeout(3500);
 		conn.setRequestProperty("User-Agent", "Scorekeeper 1.1");
 		conn.setRequestProperty("X-Scorekeeper", Prefs.getPasswordFor(dbname));
 		//conn.setRequestProperty("Host", hostname);
 		conn.setUseCaches(false);
 		conn.setDoInput(true);
 
 		if (method != null)
 			conn.setRequestMethod(method);
 
 		if (content != null)
 			conn.setRequestProperty("Content-Type", content);
 
 		try
 		{
 			OutputStream out = null;
 			int sendlength = 0;
 
 			if (args.length > 0)
 			{
 				for (Object o : args)
 				{
 					if (o instanceof byte[])
 						sendlength += ((byte[])o).length;
 					else if (o instanceof File)
 						sendlength += ((File)o).length();
 					else
 						throw new IOException("Invalid data type for sending: " + o.getClass());
 				}
 			}
 
 			if (sendlength > 0)
 			{
 				conn.setFixedLengthStreamingMode(sendlength);
 				conn.setDoOutput(true);
 				
 				out = conn.getOutputStream();
 				transfered = 0;
 				monitor.setNote("sending data...");
 				monitor.setMaximum(sendlength+1);
 
 				// Send the request and data
 				for (Object o : args)
 				{
 					if (o instanceof byte[])
 						transferData(new ByteArrayInputStream((byte[])o), out);
 					else if (o instanceof File)
 						transferData(new FileInputStream((File)o), out);
 				}
 				
 				out.flush();
 			}
 
 			InputStream in = conn.getInputStream();
 			ByteArrayOutputStream buf = new ByteArrayOutputStream();
 			transfered = 0;
 			monitor.setNote("receiving data...");
 			monitor.setMaximum(conn.getContentLength() + 1);
 			transferData(in, buf);
 
 			if (out != null)
 				out.close();
 			in.close();
 			return buf.toByteArray();
 		}
 		catch (HttpRetryException rte) // only try and catch the need for auth retry
 		{
 			if (rte.responseCode() == 401)
 				throw new AuthException();
 			throw rte;
 		}
 		catch (IOException ioe)
 		{
 			// catch 401 in an IOexception, can't use getResponseCode as this may be a connection error
 			String msg = ioe.getMessage();
 			if (msg.contains("401") && msg.contains("response code"))
 				throw new AuthException();
 			
 			InputStream err = conn.getErrorStream();
			if (err == null)
				throw ioe;
 			byte[] errorbuf = new byte[err.available()];
 			err.read(errorbuf);
 			String serror = new String(errorbuf);
 			if (serror.contains("<body>"))
 				throw new IOException(serror.substring(serror.indexOf("<body>")+7), ioe);
 			else
 				throw new IOException(serror, ioe);
 		}
 	}
 
 	public byte[] performSQL(String name, byte[] data) throws IOException
 	{
 		monitor = new ProgressMonitor(null, "performSQL", "Connecting...", 0, Integer.MAX_VALUE);
 		monitor.setMillisToDecideToPopup(300);
 		monitor.setMillisToPopup(1000);
 		dbname = name;
 		return basicRequest(new URL(String.format("http://%s/dbserve/%s/sqlmap", host, dbname)), "POST", "text/plain", data);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String>[] getAvailableForCheckout() throws IOException
 	{
 		List<String>[] ret = new List[3];
 		ret[0] = new ArrayList<String>(); // unlocked
 		ret[1] = new ArrayList<String>(); // locked
 		ret[2] = new ArrayList<String>(); // all
 
 		monitor = new ProgressMonitor(null, "getAvailable", "Connecting...", 0, Integer.MAX_VALUE);
 		monitor.setMillisToDecideToPopup(0);
 		monitor.setMillisToPopup(0);
 		String data = new String(basicRequest(new URL(String.format("http://%s/dbserve/available", host)), "GET", null));
 		for (String db : data.split("\n"))
 		{
 			String[] parts = db.split("\\s+");
 			if (parts.length != 2)
 			{
 				log.info("Invalid data from available: " + db);
 				continue;
 			}
 
 			ret[2].add(parts[0]);
 			if (parts[1].trim().equals("0"))
 				ret[0].add(parts[0]);
 			else
 				ret[1].add(parts[0]);
 		}
 
 		return ret;
 	}
 
 
 	public void downloadDatabase(File dst, boolean lockServerSide) throws IOException
 	{
 		monitor = new ProgressMonitor(null, "Download", "Connecting...", 0, Integer.MAX_VALUE);
 		monitor.setMillisToDecideToPopup(0);
 		monitor.setMillisToPopup(0);
 		String name = dst.getName();
 		dbname = name.substring(0, name.indexOf('.'));
 		String action = (lockServerSide) ? "download" : "copy";
 
 		FileOutputStream output = new FileOutputStream(dst);
 		output.write(basicRequest(new URL(String.format("http://%s/dbserve/%s/%s", host, dbname, action)), "GET", null));
 		output.close();
 		output = null;
 	}
 
 	public void uploadDatabase(File f) throws IOException
 	{
 		monitor = new ProgressMonitor(null, "Upload", "Connecting...", 0, Integer.MAX_VALUE);
 		monitor.setMillisToDecideToPopup(0);
 		monitor.setMillisToPopup(0);
 		String name = f.getName();
 		dbname = name.substring(0, name.indexOf('.'));
 
 		String boundary = "--------------------" + Long.toString(System.currentTimeMillis(), 16);
 		byte[] header = mimeFileHeader(boundary, "db", dbname, "application/octet-stream").getBytes();
 		byte[] footer = mimeFooter(boundary).getBytes();
 
 		basicRequest(new URL(String.format("http://%s/dbserve/%s/upload", host, dbname)),
 						"POST", "multipart/form-data; boundary="+boundary, header, f, footer);
 	}
 
 	public String mimeFileHeader(String boundary, String name, String fileName, String mimeType)
 	{
 		String header = new String();
 		header += "--"+boundary+"\r\n";
 		header += "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n";
 		header += "Content-Type: " + mimeType + "\r\n";
 		header += "\r\n";
 		return header;
 	}
 
 
 	public String mimeFooter(String boundary)
 	{
 		String footer = new String();
 		footer += "\r\n";
 		footer += "--"+boundary+"--\r\n";
 		return footer;
 	}
 
 	public static void main(String[] args) throws Exception
 	{
 		RemoteHTTPConnection c = new RemoteHTTPConnection("localhost");
 		c.uploadDatabase(new File("d:/cygwin/home/bwilson/testing.db"));
 	}
 }
