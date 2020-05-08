 package com.hsun324.ftplite;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.hsun324.ftplite.commands.FTPCommandActive;
 import com.hsun324.ftplite.commands.FTPCommandChained;
 import com.hsun324.ftplite.commands.FTPCommandConnect;
 import com.hsun324.ftplite.commands.FTPCommandNameList;
 import com.hsun324.ftplite.commands.FTPCommandPassive;
 import com.hsun324.ftplite.commands.FTPCommandPassword;
 import com.hsun324.ftplite.commands.FTPCommandPut;
 import com.hsun324.ftplite.commands.FTPCommandQuit;
 import com.hsun324.ftplite.commands.FTPCommandReinitialize;
 import com.hsun324.ftplite.commands.FTPCommandRetreive;
 import com.hsun324.ftplite.commands.FTPCommandUser;
 
 public class FTPClient {
 	public static final int DEFAULT_FTP_SERVER_PORT = 21;
 	
 	public FTPClient(String host) {
 		this(host, DEFAULT_FTP_SERVER_PORT);
 	}
 	public FTPClient(String host, int port) {
 		super();
 		this.state = new FTPState(this, host, port);
 	}
 	protected final FTPState state;
 	
 	protected final List<FTPFuture> commandQueue = new ArrayList<FTPFuture>();
 	
 	private Socket connection = null;
 	private FTPStreamThread responseThread = null;
 	private OutputStream commandStream = null;
 	
 	private FTPQueueThread queueThread = null;
 
 	public FTPFuture queueCommand(FTPCommand command) throws IOException {
 		synchronized (commandQueue) {
 			if (command.isExecuted()) throw new IllegalStateException();
 			FTPFuture future = new FTPFuture(this, command);
 			commandQueue.add(future);
 			commandQueue.notify();
 			return future;
 		}
 	}
 	protected OutputStream getCommandStream() {
 		return commandStream;
 	}
 	public synchronized FTPFuture connect(String user, String password) throws UnknownHostException, IOException {
 		FTPCommand firstCommand = null;
 		
 		if (!state.connected) {
 			connection = new Socket(state.host, state.port);
 			
 			queueThread = new FTPQueueThread();
 			queueThread.start();
 			
 			responseThread = new FTPStreamThread(this, connection);
 			responseThread.start();
 			
 			commandStream = connection.getOutputStream();
 			
 			firstCommand = new FTPCommandConnect();
 		} else firstCommand = new FTPCommandReinitialize();
 		
 		return queueCommand(new FTPCommandChained(new FTPCommand[]{
 			firstCommand,
 			new FTPCommandUser(user),
 			new FTPCommandPassword(password)
 		}));
 	}
 	
 	public void quit() throws IOException {
 		queueCommand(new FTPCommandQuit());
 	}
 	public synchronized void close() {
 		try {
 			responseThread.requestStop();
 			queueThread.requestStop();
 			commandStream.close();
 			
 			state.reset();
 			state.connected = false;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setActiveMode() throws IOException {
 		state.modeActive = true;
 		state.modeCommand = new FTPCommandActive();
 	}
 	public void setPassiveMode() throws IOException {
 		state.modeActive = false;
 		state.modeCommand = new FTPCommandPassive();
 	}
 
 	public FTPFutureData<String> getFile(String directory) throws IOException {
 		return sendDataCommand(new FTPTransformation<String>() {
 			@Override
 			public String transform(byte[] data) throws Exception {
 				return new String(data).replace("\r\n", "");
 			}
 		}, new FTPCommandRetreive(directory));
 	}
 	
 	public FTPFuture putFile(String directory, String data) throws IOException {
 		return queueCommand(new FTPCommandChained(state.modeCommand, new FTPCommandPut(directory, data)));
 	}
 	
 	public FTPFutureData<List<String>> getFiles(String directory) throws IOException {
 		return sendDataCommand(new FTPTransformation<List<String>>() {
 			@Override
 			public List<String> transform(byte[] data) throws Exception {
 				String[] lines = new String(data).trim().split("\n\r");
 				List<String> list = new ArrayList<String>();
 				int length = lines.length;
 				for (int i = 0; i < length; i++)
 					list.add(lines[i]);
 				
 				return list;
 			}
 		}, new FTPCommandNameList(directory));
 	}
 	
 	public <T> FTPFutureData<T> sendDataCommand(final FTPTransformation<T> function, FTPCommand command) throws IOException {
 		return new FTPFutureData<T>(queueCommand(new FTPCommandChained(new FTPCommand[] { state.modeCommand, command }))) {
 			@Override
 			protected T formData(byte[] result) throws Exception {
 				return function.transform(result);
 			}
 		};
 	}
 	
 	class FTPQueueThread extends Thread {
 		private boolean stopRequested = false;
 		
		protected final Object futureSync = new Object();
		
 		protected FTPFuture pullFuture() throws IOException {
 			synchronized (commandQueue) {
 				try {
 					while (commandQueue.size() == 0)
 						commandQueue.wait();
 					return commandQueue.remove(0);
 				} catch (InterruptedException e) {
 					throw new IOException(e);
 				}
 			}
 		}
 		@Override
 		public void run() {
 			while (!stopRequested) {
 				try {
					state.currentFuture = pullFuture();
					synchronized (futureSync) {
						state.currentFuture.execute();
						state.currentFuture.waitUntilResult();
					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 		}
 		public void requestStop() {
 			stopRequested = true;
 		}
 	}

	public void clearFuture() {
		synchronized (queueThread.futureSync) {
			state.currentFuture = null;
		}
	}
 }
