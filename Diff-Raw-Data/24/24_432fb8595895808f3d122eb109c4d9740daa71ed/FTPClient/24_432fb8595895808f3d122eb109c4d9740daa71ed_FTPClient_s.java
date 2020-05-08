 package com.hsun324.ftplite;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.net.Socket;
import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.hsun324.ftplite.commands.*;
 import com.hsun324.ftplite.commands.FTPCommandDirectory.DirectoryAction;
 import com.hsun324.ftplite.commands.FTPCommandFile.FileAction;
 import com.hsun324.ftplite.handlers.FTPHandlerRegistry;
 
 public class FTPClient {
 	public static final int DEFAULT_FTP_SERVER_PORT = 21;
 	
 	private static int THREAD_NAME_TICKER = 0;
 	
 	public FTPClient(String host) {
 		this(host, DEFAULT_FTP_SERVER_PORT);
 	}
 	public FTPClient(String host, int port) {
 		super();
 		this.state = new FTPState(this, host, port);
 	}
 	protected final FTPState state;
 	
 	protected final List<FTPFuture> commandQueue = new ArrayList<FTPFuture>();
 	
 	private final Object connectionSync = new Object();
 	private Socket connection = null;
 	private FTPStreamThread responseThread = null;
 	private OutputStream commandStream = null;
 	
 	private FTPQueueThread queueThread = null;
 
 	protected OutputStream getCommandStream() {
 		return commandStream;
 	}
 	
 	public FTPFuture queueCommand(FTPCommand command) throws IOException {
 		synchronized (commandQueue) {
 			if (!state.connected)  throw new IllegalStateException("conn closed");
 			return preQueueCommand(command);
 		}
 	}
 	private FTPFuture preQueueCommand(FTPCommand command) throws IOException {
 		synchronized (commandQueue) {
 			if (command.isExecuted()) throw new IllegalStateException("already executed command");
 			
 			FTPFuture future = new FTPFuture(this, command);
 			commandQueue.add(future);
 			commandQueue.notify();
 			return future;
 		}
 	}
 	
 	public synchronized FTPFuture connect(String user, String password) throws IOException {
 		try {
 			FTPCommand firstCommand = null;
 			
 			if (!state.connected) {
 				queueThread = new FTPQueueThread();
 				queueThread.start();
 				
 				responseThread = new FTPStreamThread();
 				responseThread.start();
 				
 				THREAD_NAME_TICKER++;
 				
 				firstCommand = new FTPCommandConnect();
 			} else firstCommand = new FTPCommandReinitialize();
 			
 			FTPFuture future = preQueueCommand(new FTPCommandChained(new FTPCommand[]{
 				firstCommand,
 				new FTPCommandUser(user),
 				new FTPCommandPassword(password),
 				new FTPCommandSystem(),
 				new FTPCommandFeatures(),
 				new FTPCommandDirectory(),
 				new FTPCommandUTF8()
 			}));
 			
 			if (!state.connected) {
 				putConnection(new Socket(state.host, state.port));
 			}
 			
 			return future;
 		} catch (Exception e) {
 			close();
 			throw new IOException("could not make connection", e);
 		}
 	}
 
 	public void putConnection(Socket connection) throws IOException {
 		synchronized (connectionSync) {
 			this.connection = connection;
 			this.commandStream = connection.getOutputStream();
 			connectionSync.notifyAll();
 		}
 	}
 	
 	public void quit() throws IOException {
 		queueCommand(new FTPCommandQuit());
 	}
 	public synchronized void close() {
 		try {
 			if (responseThread != null) {
 				responseThread.requestStop();
 				responseThread = null;
 			}
 
 			if (queueThread != null) {
 				queueThread.requestStop();
 				queueThread = null;
 			}
 			
 			if (commandStream != null) {
 				commandStream.close();
 				commandStream = null;
 			}
 
 			if (connection != null) {
 				connection.close();
 				connection = null;
 			}
 			
 			FTPFuture future = state.currentFuture;
 			if (future != null) {
 				future.command.quitExecution();
 				if (!future.isResultSet()) future.setResult(FTPResult.FAILED);
 			}
 			
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
 
 	public FTPFutureData<FTPFile> getFile(FTPFilename file) throws IOException {
 		return queueDataCommand(FTPTransformation.FILE_TRANSFORMATION, new FTPCommandFileRetrieve(file), file);
 	}
 	public FTPFuture writeFile(FTPFilename file, FTPFile data) throws IOException {
 		return queueFileCommand(new FTPCommandFile(FileAction.WRITE, file, data), file);
 	}
 	public FTPFuture appendFile(FTPFilename file, FTPFile data) throws IOException {
 		return queueFileCommand(new FTPCommandFile(FileAction.APPEND, file, data), file);
 	}
 	public FTPFuture deleteFile(FTPFilename file) throws IOException {
 		return queueCommand(new FTPCommandFileDelete(file));
 	}
 	
 	public FTPFutureData<FTPEntity[]> getFileList(FTPFilename directory) throws IOException {
 		return queueDataCommand(FTPTransformation.FILELIST_TRANSFORMATION, new FTPCommandList(directory));
 	}
 	
 	public FTPFutureData<String> getWorkingDirectory(FTPFilename directory) throws IOException {
 		return queueDataCommand(FTPTransformation.ASCII_TRANSFORMATION, new FTPCommandDirectory());
 	}
 	public FTPFuture changeWorkingDirectory(FTPFilename directory) throws IOException {
 		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.CHANGE, directory), new FTPCommandDirectory()));
 	}
 	public FTPFuture makeDirectory(FTPFilename directory) throws IOException {
 		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.MAKE, directory), new FTPCommandDirectory()));
 	}
 	public FTPFuture deleteDirectory(FTPFilename directory) throws IOException {
 		return queueCommand(new FTPCommandChained(false, new FTPCommandDirectory(DirectoryAction.REMOVE, directory), new FTPCommandDirectory()));
 	}
 	public FTPFuture completelyDeleteDirectory(FTPFilename directory) throws IOException {
 		throw new UnsupportedOperationException();
 	}
 	
 	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command) throws IOException {
 		return queueDataCommand(function, command, null);
 	}
 	public <T> FTPFutureData<T> queueDataCommand(final FTPTransformation<T> function, FTPCommand command, FTPFilename file) throws IOException {
 		return new FTPFutureData<T>(queueFileCommand(command, file)) {
 			@Override
 			protected T formData(byte[] result) throws Exception {
 				return function.transform(state, result);
 			}
 		};
 	}
 	
 	public FTPFuture queueFileCommand(FTPCommand command) throws IOException {
 		return queueFileCommand(command, null);
 	}
 	public FTPFuture queueFileCommand(FTPCommand command, FTPFilename file) throws IOException {
 		return queueCommand(new FTPCommandChained(new FTPCommand[] { new FTPCommandType(getFTPType(file)), state.modeCommand, command }));
 	}
 
 	public FTPFilename getAbsoluteFilename(String file) {
 		return new FTPFilename("/", file);
 	}
 	public FTPFilename getRelativeFilename(String file) {
 		return new FTPFilename(state.workingDirectory, file);
 	}
 	
 	private char getFTPType(FTPFilename file) {
 		if (file == null) return FTPTypeDecider.ASCII;
 		
 		String filename = file.getName();
 		int index = filename.lastIndexOf('.');
 		if (index > -1) return FTPTypeDecider.decideFTPType(filename.substring(0, index), index != 0);
 		return FTPTypeDecider.decideFTPType("", !filename.isEmpty());
 	}
 	
 	class FTPStreamThread extends Thread {
 		private final Pattern CONTROL_RESPONSE_PATTERN = Pattern.compile("([0-9]{3})([ -])(.*)");
 
 		public InputStream inputStream;
 		public Reader reader;
 		
 		private final char[] buffer;
 		private boolean stopRequested = false;
 		
 		private StringBuffer responseBuffer = new StringBuffer();
 		
 		public FTPStreamThread() throws IOException {
 			this(400);
 		}
 		public FTPStreamThread(int bufferSize) throws IOException {
 			this.buffer = new char[bufferSize];
 			this.setName("FTPLiteClientStream" + state.host + THREAD_NAME_TICKER);
 		}
 		
 		public Socket getConnection() {
 			synchronized (connectionSync) {
 				try {
 					while (connection == null && !stopRequested) connectionSync.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				return connection;
 			}
 		}
 		
 		@Override
 		public void run() {
 			try {
 				Socket socket = getConnection();
 				if (socket != null) {
 					this.inputStream = socket.getInputStream();
 					this.reader = new InputStreamReader(this.inputStream);
 					
 					while (!stopRequested) {
 						int read = reader.read(buffer);
 						int newline;
 						while ((newline = findNewlineIndex(buffer, read)) > -1) {
 							String line = new String(buffer, 0, newline);
 							
 							int shift = newline + 2;
 							int len = buffer.length;
 							int threshold = len - shift;
 							for(int i = 0; i < len; i++)
 								if (i < threshold) buffer[i] = buffer[i + shift];
 								else buffer[i] = 0;
 							
 							System.out.println("  " + line);
 							
 							Matcher matcher = CONTROL_RESPONSE_PATTERN.matcher(line);
 							if (matcher.find()) {
 								int code = Integer.parseInt(matcher.group(1));
 								String delim = matcher.group(2);
 								String content = matcher.group(3);
 								
 								if (delim.equals(" ")) {
 									FTPResponse response = new FTPResponse(code, responseBuffer.append(content).toString());
 									responseBuffer.setLength(0);
 									
 									if (!FTPHandlerRegistry.tryGlobalHandle(state, response) && state.currentFuture != null)
 										if (state.currentFuture.command.pushResponse(state, response))
 											state.currentFuture = null;
 								} else responseBuffer.append(content).append("\n");
 								continue;
 							}
 							responseBuffer.append(line).append("\n");
 						}
 					}
 					reader.close();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		public void requestStop() {
 			stopRequested = true;
 			synchronized (connectionSync) {
 				connectionSync.notifyAll();
 			}
 		}
 		private int findNewlineIndex(char[] buffer, int len) {
 			for (int i = 0, j = 1; j < len; i++, j++)
 				if (buffer[i] == '\r' && buffer[j] == '\n') return i;
 			return -1;
 		}
 	}
 	
 	class FTPQueueThread extends Thread {
 		protected FTPQueueThread() {
 			this.setName("FTPLiteClientQueue" + state.host + THREAD_NAME_TICKER);
 		}
 		private boolean stopRequested = false;
 		
 		protected FTPFuture pullFuture() throws IOException {
 			synchronized (commandQueue) {
 				try {
 					while (commandQueue.size() == 0 && !stopRequested)
 						commandQueue.wait();
 					return stopRequested ? null : commandQueue.remove(0);
 				} catch (InterruptedException e) {
 					throw new IOException("failed pull", e);
 				}
 			}
 		}
 		@Override
 		public void run() {
 			while (!stopRequested) {
 				try {
 					FTPFuture future = pullFuture();
 					
 					if (future != null) {
 						state.currentFuture = future;
 						future.execute();
 						future.waitUntilResult();
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
 }
