 /*-
  * Copyright © 2011 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector;
 
 import gda.device.DeviceBase;
 import gda.device.DeviceException;
 import gda.factory.Configurable;
 import gda.factory.FactoryException;
 import gda.factory.Findable;
 import gda.util.BusyFlag;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.ConnectException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provides Ethernet communications with OS9/Linux DAServer
  */
 public class DAServer extends DeviceBase implements Configurable, Findable {
 
 	public static final double CLOCKRATE = 12.5e-09;
 	private static final Logger logger = LoggerFactory.getLogger(DAServer.class);
 	private String host = "none";
 	private int port = -1;
 	private int dataPort = 20030;
 	protected ArrayList<String> startupCommands = new ArrayList<String>();
 	private int socketTimeOut = 2000;
 	private boolean connected = false;
 	private Socket socket = null;
 	private BufferedWriter out = null;
 	private BufferedReader in = null;
 	private int connectTimeOut = 2000;
 	private BusyFlag busyFlag = new BusyFlag();
 	private Vector<String> data = new Vector<String>();
 	private ServerSocketChannel serverSocket = null;
 	private long replyTimeOut = 30000;
 
 	/**
 	 * Set the host on which da.server is running
 	 * 
 	 * @param host
 	 */
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	/**
 	 * @return the host name on which da.server is running
 	 */
 	public String getHost() {
 		return host;
 	}
 
 	/**
 	 * Set the port on which da.server is listening
 	 * 
 	 * @param port
 	 */
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	/**
 	 * @return the port on which da.server is listening
 	 */
 	public int getPort() {
 		return port;
 	}
 
 	/**
 	 * Set the data port on which da.server can make socket connection back to this.
 	 * 
 	 * @param dataPort
 	 */
 	public void setDataPort(int dataPort) {
 		this.dataPort = dataPort;
 	}
 
 	/**
 	 * @return Get the data port on which da.server can make socket connection back to this.
 	 */
 	public int getDataPort() {
 		return dataPort;
 	}
 
 	/**
 	 * @param startupCommands
 	 */
 	public void setStartupCommands(ArrayList<String> startupCommands) {
 		this.startupCommands = startupCommands;
 	}
 
 	/**
 	 * @return an array list of startup commands to be processed by da.server on startup
 	 */
 	public ArrayList<String> getStartupCommands() {
 		return startupCommands;
 	}
 
 	/**
 	 * @return Returns the replyTimeOut.
 	 */
 	public long getReplyTimeOut() {
 		return replyTimeOut;
 	}
 
 	/**
 	 * @param replyTimeOut
 	 *            Set the replyTimeOut in msec. This is the time before we give up on da.Server
 	 */
 	public void setReplyTimeOut(long replyTimeOut) {
 		this.replyTimeOut = replyTimeOut;
 	}
 
 	@Override
 	public void configure() throws FactoryException {
 		lock();
 
 		try {
 			logger.debug("connecting.");
 			connect();
 			for (String command : getStartupCommands()) {
 				try {
 					if (null != out) {
 						logger.debug("sending startup command: " + command);
 						out.write(command + "\n");
 						out.flush();
 						getReply(false);
 					}
 				} catch (Exception e) {
 					throw new FactoryException("da.server config failed", e);
 				}
 			}
 
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void reconfigure() throws FactoryException {
 		configure();
 	}
 
 	public void connect() {
 		lock();
 		try {
 			InetSocketAddress inetAddr = new InetSocketAddress(getHost(), getPort());
 			socket = new Socket();
 			socket.connect(inetAddr, connectTimeOut);
 
 			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			socket.setSoTimeout(socketTimeOut);
 
 			connected = true;
 
 			// reset the timeout on successful connection as this is
 			// incremented when on an unsuccessful reconnect.
 			connectTimeOut = 2000;
 
 			// A getReply is needed to receive a prompt (">") from the
 			// server, otherwise startup messages get confused with
 			// replies to subsequent commands.
 			cleanPipe();
 		} catch (UnknownHostException ex) {
 			// should this be fatal as reconnect attempts are futile.
 			logger.error(getName() + ": connect: " + ex.getMessage(), ex);
 		} catch (ConnectException ex) {
 			logger.error(getName() + ": connect: " + ex.getMessage(), ex);
 		} catch (IOException ix) {
 			logger.error(getName() + ": connect: " + ix.getMessage(), ix);
 		} finally {
 			unlock();
 		}
 	}
 
 	private void cleanPipe() {
 		lock();
 		try {
 			if (isConnected()) {
 				logger.debug("cleaning pipe");
 				while (in.ready()) {
 					in.read();
 				}
 			}
 		} catch (IOException ex) {
 			logger.error(getName() + ": cleanPipe: " + ex.getMessage());
 			try {
 				close();
 			} catch (DeviceException e) {
 				// we know that already
 			}
 		} catch (Exception ex) {
 			logger.warn(getName() + ": cleanPipe: " + ex.getMessage());
 		} finally {
 			unlock();
 		}
 	}
 
 	private void ensureConnected() {
 		lock();
 
 		try {
 			cleanPipe();
 			if (!isConnected()) {
 				try {
 					configure();
 				} catch (FactoryException e) {
 					logger.error("error connecting", e);
 				}
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	/**
 	 * Tidy existing socket streams and try to connect them again within the thread. This method is synchronized as both
 	 * the main thread and run thread use this method.
 	 * 
 	 * @throws DeviceException
 	 */
 	public synchronized void reconnect() throws DeviceException {
 		try {
 			close();
 		} catch (DeviceException e) {
 			// do nothing for now
 		}
 		connect();
 		doStartupScript();
 
 		if (connectTimeOut < 30000) {
 			connectTimeOut += 1000;
 		}
 		logger.debug(getName() + ": will attempt to reconnect in " + connectTimeOut / 1000 + "s");
 	}
 
 	/**
 	 * {@inheritDoc} tidy up all streams before ending program
 	 * 
 	 * @see gda.device.DeviceBase#close()
 	 */
 	@Override
 	public void close() throws DeviceException {
 		connected = false;
 		if (serverSocket != null) {
 			try {
 				serverSocket.close();
 			} catch (IOException e) {
 				logger.warn("Error closing serverSocket");
 			}
 			serverSocket = null;
 		}
 		if (socket != null) {
 			try {
 				if (out != null) {
 					out.close();
 					out = null;
 				}
 				if (in != null) {
 					in.close();
 					in = null;
 				}
 				socket.close();
 
 				socket = null;
 			} catch (IOException ex) {
 				logger.error(getName() + ": disconnect: " + ex.getMessage());
 				throw new DeviceException(ex.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Returns the state of the socket connection
 	 * 
 	 * @return true if connected
 	 */
 	public boolean isConnected() {
 		if (socket == null || !socket.isConnected()) {
 			connected = false;
 		}
 		return connected;
 	}
 
	// TODO why myReadLine and not readLine. my??
 	private String myReadLine() throws IOException, InterruptedException {
 		char ch;
 		StringBuffer reply = new StringBuffer("");
 
 		lock();
 		try {
 			int waitIterations = 0;
 
 			while (in != null) {
 				while (!in.ready()) {
 					try {
 						Thread.sleep(25);
 						waitIterations++;
 						if (waitIterations > 4000) {
 							throw new IOException("waited too long");
 						}
 					} catch (InterruptedException e) {
 						throw new InterruptedException("interrupted waiting for reply");
 					}
 				}
 
 				ch = (char) in.read();
 
 				if (ch != '\n' && ch != '\r') {
 					reply.append(ch);
 				}
 
 				if (ch == '\r' || reply.toString().indexOf("> ") >= 0) {
 					break;
 				}
 
 			}
 			if (reply.length() > 0) {
 				return reply.toString();
 			}
 
 		} finally {
 			unlock();
 		}
 		return null;
 	}
 
 	private boolean isPrompt(String message) {
 		return message.charAt(0) == '>';
 	}
 
 	/**
 	 * Send command to the server and specify a timeout on this command
 	 * 
 	 * @param msg
 	 *            an unterminated command
 	 * @param timeout
 	 *            value in milliseconds
 	 * @return the reply which may be integer or string.
 	 * @throws DeviceException
 	 */
 	public Object sendCommand(String msg, int timeout) throws DeviceException {
 		try {
 			socket.setSoTimeout(timeout);
 		} catch (SocketException sx) {
 			logger.error(getName() + ": connect: " + sx.getMessage());
 		}
 		return sendCommand(msg);
 	}
 
 	public Object sendCommand(String msg) throws DeviceException {
 		return sendCommand(msg, false);
 	}
 
 	/**
 	 * Send command to the server.
 	 * 
 	 * @param msg
 	 *            an unterminated command
 	 * @return the reply which may be integer or string.
 	 * @throws DeviceException
 	 */
 	public Object sendCommand(String msg, Boolean multiline) throws DeviceException {
 		String command = msg + '\n';
 		Object reply = null;
 
 		lock();
 		try {
 			ensureConnected();
 			cleanPipe();
 			logger.debug(getName() + ": sending command: " + msg);
 			out.write(command);
 			out.flush();
 			reply = getReply(multiline);
 		} catch (Exception ex) {
 			// logger.error(getName() + ": sendCommand: " + ex.getMessage());
 			throw new DeviceException(getName() + ": sendCommand: " + ex.getMessage());
 		} finally {
 			unlock();
 		}
 		return reply;
 	}
 
 	/**
 	 * Get data from the server.
 	 * 
 	 * @param msg
 	 *            an unterminated command
 	 * @return returns data as an array of string objects.
 	 * @throws Exception
 	 */
 	public Object[] getData(String msg) throws Exception {
 		Object reply = null;
 		Object[] dataArray = null;
 
 		if (connected) {
 			// The busyflag lock mechanism ensures that only one thread
 			// will be sending a command and waiting for a reply.
 			lock();
 
 			String command = msg + '\n';
 
 			try {
 				logger.debug(getName() + ": getData command: " + msg);
 				out.write(command);
 				out.flush();
 				// clearing any previous data
 				data.clear();
 				reply = getReply(false);
 				if (((Integer) reply).intValue() != -1) {
 					dataArray = data.toArray();
 				}
 			} catch (IOException ex) {
 				logger.error(getName() + ": getData: " + ex.getMessage());
 			} finally {
 				unlock();
 			}
 		}
 		return dataArray;
 	}
 
 	private Object getReply(Boolean multiline) throws Exception {
 		Object reply = "";
 		Date stampIn = new Date();
 
 		lock();
 		try {
 			while (true) {
 
 				String message = myReadLine();
 				logger.debug(getName() + ": getReply message received : " + message);
 				if (isPrompt(message)) {
 					// we got a prompt, so the last message was the return value
 					return reply;
 				}
 				if (stampIn.getTime() + 300 * 1000 < new Date().getTime()) {
 					throw new IOException("no sensible reply in ages");
 				}
 				if (multiline) {
 					Object replyObj = parseReply(message);
 					if (replyObj != null) {
 						reply = reply.toString() + "\n" + message.toString();
 						reply = reply.toString().trim();
 					}
 				} else {
 					reply = parseReply(message);
 				}
 			}
 
 		} finally {
 			unlock();
 		}
 	}
 
 	private void doStartupScript() throws DeviceException {
 		if (isConnected() && startupCommands.size() != 0) {
 			for (String command : startupCommands) {
 				sendCommand(command);
 			}
 		}
 	}
 
 	private void lock() {
 		busyFlag.getBusyFlag();
 	}
 
 	private void unlock() {
 		busyFlag.freeBusyFlag();
 	}
 
 	private Object parseReply(String message) {
 
 		if (message.charAt(0) == '#') {
 			// XH hack: the read-status verbose command returns something useful in the # part of the returned message.
 			String restOfMessage = message.substring(2);
 			String[] parts = restOfMessage.split(":");
 			if (parts[0].equals("Idle") || parts[0].equals("Running") || parts[0].equals("Paused")) {
 				return restOfMessage;
 			}
 
 			// nothing interesting - diagnostics?
 			return null;
 		} else if (message.charAt(0) == '!') {
 			if (!message.contains("Image Display") && !message.contains("OS-9 error code: 29")) {
 				logger.error("Error:" + message.substring(1));
 			}
 			return null;
 		} else if (message.charAt(0) == '@') {
 			// progress bar equivalent to keep the line busy during long running operations
 			return null;
 		} else if (message.charAt(0) == '*') {
 			// logger.debug(getName() + ": replied with: [" + message.substring(0, len) + "]");
 			if (message.startsWith("* (null)")) {
 				return null;
 			} else if (message.charAt(2) == '"') {
 				if (message.charAt(3) == '#') {
 					return message.substring(4, message.length() - 1);
 				}
 				return message.substring(3, message.length() - 1);
 			} else {
 				try {
 					if (message.indexOf('.', 1) >= 0) {
 						return new Double(message.substring(2));
 					}
 
 					return new Integer(message.substring(2));
 				} catch (NumberFormatException ex) {
 					logger.error(getName() + ": NumberFormatException: " + ex.getMessage());
 				}
 			}
 		}
 
 		// It must be data.
 		StringTokenizer strtok = new StringTokenizer(message);
 		// data.clear();
 		while (strtok.hasMoreElements()) {
 			data.add((String) strtok.nextElement());
 		}
 
 		return null;
 	}
 
 	@SuppressWarnings("null")
 	protected ByteBuffer getBinaryDataBuffer(String command, int ndata) throws Exception {
 		ByteBuffer bb = ByteBuffer.allocate(ndata * (Float.SIZE / Byte.SIZE));
 
 //		if (dataport < 0) {
 //			dataport = getDataPort();
 //		}
 
 		lock();
 		try {
 			ensureConnected();
 			if (serverSocket == null) {
 				boolean bound = false;
 				while (!bound) {
 					if (dataPort < 0) {
 						throw new IOException("no bindable ports found");
 					}
 
 					try {
 						serverSocket = ServerSocketChannel.open();
 						serverSocket.socket().bind(new InetSocketAddress(dataPort));
 						serverSocket.configureBlocking(false);
 						bound = true;
 					} catch (IOException e) {
 						dataPort++;
 					}
 				}
 				// should this be re-sent every time ?
 				sendCommand("port " + dataPort);
 				logger.debug(getName() + " getBinaryDataBuffer(): serverSocket created on port " + dataPort);
 			}
 			out.flush();
 			// The reply to the command will not come until after the
 			// data is sent to the dataSocket so cannot use sendCommand
 			// but must split up its parts around the data reading.
 			logger.debug(getName() + ": sent command: " + command);
 			out.write(command + "\n");
 			out.flush();
 
 			SocketChannel dataSocket = null;
 			int tries = 0;
 			int maxNumberOfTries = 400; // originally 20000 i.e. 8m20s
 			int loopWaitTimeInMilliseconds = 25;
 			double timeoutInSeconds = (maxNumberOfTries*loopWaitTimeInMilliseconds)/1000;
 			while (true) {
 				dataSocket = serverSocket.accept();
 				if (dataSocket != null) {
 					break;
 				}
 				tries++;
 				if (tries > maxNumberOfTries) {
 					throw new IOException("No incoming data connection made by da.server after " + timeoutInSeconds + "s.");
 				}
 				try {
 					Thread.sleep(loopWaitTimeInMilliseconds);
 				} catch (InterruptedException e) {
 					throw new InterruptedException("Interrupted while waiting for da.server to make a data connection.");
 				}
 			}
 			logger.debug(getName() + " getBinaryDataBuffer(): socket connection established");
 
 			while (dataSocket.read(bb) > 0) {
 			}
 
 			dataSocket.close();
 			// get message from da.server over the comms socket about the outputted data. 
 			getReply(false);
 
 		} catch (IOException e) {
 			logger.error(getName() + " getBinaryDataBuffer(): " + e.getMessage());
 			try {
 				serverSocket.close();
 			} catch (IOException e1) {
 				// we have failed already
 			}
 			serverSocket = null;
 			return null;
 		} finally {
 			unlock();
 		}
 
 		bb.flip();
 		return bb;
 	}
 
 	/**
 	 * Get binary data from the server and transform to double
 	 * 
 	 * @param message
 	 *            an unterminated command
 	 * @param ndata
 	 *            number of data values to fetch.
 	 * @return returns data as an array.
 	 * @throws Exception
 	 */
 	public double[] getBinaryData(String message, int ndata) throws Exception {
 		if (!isConnected()) {
 			return null;
 		}
 
 		boolean retry = false;
 		int numRetries = 0;
 		double[] results = null;
 		do {
 			try {
 				results = tryToGetBinaryData(message, ndata);
				retry = false;
 			} catch (Exception e) {
 				if (numRetries >= 5) {
 					throw e;
 				}
 				logger.warn("Buffer returned from da.server was too small, retrying");
 				numRetries++;
 				retry = true;
 			}
 
 		} while (retry);
 		return results;
 	}
 
 	private double[] tryToGetBinaryData(String message, int ndata) throws Exception {
 		String command = message;
 		double[] binaryData = new double[ndata];
 		ByteBuffer bb = getBinaryDataBuffer(command, ndata);
 		if (bb == null)
 			return null;
 		int bufferSize = bb.array().length;
 		if (bufferSize != ndata * 4){
 			throw new Exception("Asked for " + ndata * 4 + "bytes but received " + bufferSize);
 		}
 		for (int i = 0; i < binaryData.length; i++) {
 			if (!bb.hasRemaining()) {
 				throw new Exception("Ran off end of buffer!");
 			}
 			binaryData[i] = bb.getFloat();
 		}
 		return binaryData;
 	}
 
 	/**
 	 * Get binary data from the server as float
 	 * 
 	 * @param message
 	 *            an unterminated command
 	 * @param ndata
 	 *            number of data values to fetch.
 	 * @return returns data as an array.
 	 * @throws Exception
 	 */
 	public float[] getFloatBinaryData(String message, int ndata) throws Exception {
 		if (!isConnected()) {
 			return null;
 		}
 
 		String command = message;
 		float[] binaryData = new float[ndata];
 		ByteBuffer bb = getBinaryDataBuffer(command, ndata);
 		if (bb == null)
 			return null;
 		bb.asFloatBuffer().get(binaryData);
 		return binaryData;
 	}
 
 	/**
 	 * Get binary data from the server and transform to long (64bit) *
 	 * 
 	 * @param message
 	 *            an unterminated command
 	 * @param ndata
 	 *            number of data values to fetch.
 	 * @return returns data as an array.
 	 * @throws Exception
 	 */
 	public long[] getLongBinaryData(String message, int ndata) throws Exception {
 		if (!isConnected()) {
 			return null;
 		}
 
 		String command = message;
 		long[] binaryData = new long[ndata];
 		ByteBuffer bb = getBinaryDataBuffer(command, ndata);
 		if (bb == null)
 			return null;
 		bb.asLongBuffer().get(binaryData);
 		return binaryData;
 	}
 
 	/**
 	 * Get binary data from the server and transform to int[] (32bit)
 	 * 
 	 * @param message
 	 *            an unterminated command
 	 * @param ndata
 	 *            number of data values to fetch.
 	 * @return returns data as an array.
 	 * @throws Exception
 	 */
 	public int[] getIntBinaryData(String message, int ndata) throws Exception {
 		if (!isConnected()) {
 			return null;
 		}
 
 		String command = message;
 		int[] binaryData = new int[ndata];
 		ByteBuffer bb = getBinaryDataBuffer(command, ndata);
 		if (bb == null)
 			return null;
 		bb.asIntBuffer().get(binaryData);
 		return binaryData;
 	}
 
 	/**
 	 * test method
 	 * 
 	 * @throws Exception
 	 */
 	public void test() throws Exception {
 		int[] testIntData;
 		double[] testDoubleData;
 		float[] testFloatData;
 		long[] testLongData;
 		Object obj;
 		int handle = -1;
 		int npoints = 512;
 		String command;
 
 		connect();
 		// Test Double data (x+2y+3t)
 		if ((obj = sendCommand("test open 512 1 100 double")) != null) {
 			handle = ((Integer) obj).intValue();
 		}
 		command = "read 0 0 0 512 1 1 motorola float from " + handle;
 		testDoubleData = getBinaryData(command, npoints);
 		for (int i = 0; i < npoints; i++) {
 			if (testDoubleData[i] != i) {
 				logger.info("DAServer: i= " + i + " data " + testDoubleData[i]);
 			}
 		}
 		// Test 32bit Integer data (x+2y+3t)
 		if ((obj = sendCommand("test open 512 1 100 long")) != null) {
 			handle = ((Integer) obj).intValue();
 		}
 		command = "read 0 0 0 512 1 1 motorola raw from " + handle;
 		testIntData = getIntBinaryData(command, npoints);
 		for (int i = 0; i < npoints; i++) {
 			if (testIntData[i] != i) {
 				logger.info("DAServer: i= " + i + " data " + testIntData[i]);
 			}
 		}
 
 		// Test float data (x+2y+3t)
 		if ((obj = sendCommand("test open 512 1 100 float")) != null) {
 			handle = ((Integer) obj).intValue();
 		}
 		command = "read 0 0 0 512 1 1 motorola float from " + handle;
 		testFloatData = getFloatBinaryData(command, npoints);
 		for (int i = 0; i < npoints; i++) {
 			if (testFloatData[i] != i) {
 				logger.info("DAServer: i= " + i + " data " + testFloatData[i]);
 			}
 		}
 		// Test 64bit Integer data (x*y*(t+1))
 		if ((obj = sendCommand("test open 512 2 100 int64")) != null) {
 			handle = ((Integer) obj).intValue();
 		}
 		command = "read 0 0 0 512 2 1 motorola raw from " + handle;
 		npoints = 1024;
 		testLongData = getLongBinaryData(command, npoints);
 		for (int i = 512; i < npoints; i++) {
 			if (testLongData[i] != (i - 512) * 2) {
 				logger.info("DAServer: i= " + i + " data " + testLongData[i]);
 			}
 		}
 	}
 }
