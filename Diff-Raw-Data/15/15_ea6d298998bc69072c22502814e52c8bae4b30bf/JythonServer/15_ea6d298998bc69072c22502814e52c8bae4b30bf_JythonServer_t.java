 package com.hei.util.jython;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.python.util.InteractiveConsole;
 
 public class JythonServer {
 
 	public static final int JYTHON_PORT = 8991;
 
 	private static JythonServer SINGLETON = null;
 	private static final String NEXT_PROMPT = ">>> ";
 	private static final String CONT_PROMPT = "... ";
 	private static final String WELCOME = InteractiveConsole.getDefaultBanner()
 			+ "\n" + NEXT_PROMPT;
 
 	private final InteractiveConsole _jython;
 	private volatile Thread _serverThread;
 	private final Object _runLock;
 
 	public static JythonServer singleton() {
 		if (SINGLETON == null) {
 			SINGLETON = new JythonServer();
 		}
 
 		return SINGLETON;
 	}
 
 	private JythonServer() {
 		_serverThread = null;
 		_runLock = new Object();
 
 		System.setProperty("python.cachedir", "/home/hei/.jython-cache");
 		_jython = new InteractiveConsole();
 		_jython.exec("1");
 	}
 
 	public synchronized void startServer() {
 		final boolean started = isStarted();
 		if (started) {
 			return;
 		}
 
 		_serverThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				startServerImpl();
 			}
 		}, "JythonServer");
 		_serverThread.setDaemon(true);
 		_serverThread.start();
 	}
 
 	public boolean isStarted() {
 		return _serverThread != null;
 	}
 
 	public synchronized void stopServer() {
 		if (_serverThread != null) {
 			_serverThread.interrupt();
 		}
 	}
 
 	private void startServerImpl() {
 
 		try {
 			final ServerSocket server = new ServerSocket(JYTHON_PORT);
 			try {
 				while (true) {
 					final Socket connection = server.accept();
 					handleConnection(connection);
 				}
 			} catch (final IOException e) {
 				server.close();
 				e.printStackTrace();
 			}
 
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void handleConnection(final Socket connection) {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					final OutputStream outputStream = connection.getOutputStream();
 					final InputStream inputStream = connection.getInputStream();
 					outputStream.write(WELCOME.getBytes());
 
					final StringBuilder codeBuilder = new StringBuilder();
 					while (true) {
 						try {
 							final InputStreamReader inputStreamReader = new InputStreamReader(
 									inputStream);
 							final BufferedReader bufferedReader = new BufferedReader(
 									inputStreamReader);
 							final String line = bufferedReader.readLine();
 
 							final boolean more;
 							if (line != null) {
 								synchronized (_runLock) {
 									_jython.setOut(outputStream);
 									_jython.setErr(outputStream);

									if (codeBuilder.length() > 0) {
										codeBuilder.append("\n");
									}
									codeBuilder.append(line);

									final String code = codeBuilder.toString();
									more = _jython.runsource(code, InteractiveConsole.CONSOLE_FILENAME);

									if (!more) {
										codeBuilder.setLength(0);
									}
 								}
 							} else {
 								more = false;
 							}
 
 							final String prompt = more ? CONT_PROMPT
 									: NEXT_PROMPT;
 							final byte[] promptBytes = prompt.getBytes();
 							outputStream.write(promptBytes);
 							outputStream.flush();
 						} catch (final IOException e) {
 							break;
 						}
 					}
 
 					inputStream.close();
 					outputStream.close();
 					connection.close();
 				} catch (final IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}, "JythonConnection").start();
 	}
 
 	public static void main(final String[] args) {
 		JythonServer.singleton().startServer();
 		System.out.println(InteractiveConsole.getDefaultBanner());
 		System.out.println("Please any key to end...");
 		try {
 			System.in.read();
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
