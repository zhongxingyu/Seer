 package org.ukiuni.lighthttpserver;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.security.KeyManagementException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLServerSocketFactory;
 
 import org.ukiuni.lighthttpserver.request.DefaultHandler;
 import org.ukiuni.lighthttpserver.request.Handler;
 
 public class HttpServer {
 	private boolean started;
 	private int port;
 	private int serverWaitQueue;
 	private boolean ssl;
 	private Thread portListenThread;
 
 	private char[] certificatePassword = "changeit".toCharArray();
 	private char[] keyStorePassword = "changeit".toCharArray();
 	private String keyStorePath = "default_keystore.jks";
 	private String keyStoreType = "JKS";
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public ExecutorService getExecutorService() {
 		return executorService;
 	}
 
 	public void setExecutorService(ExecutorService executorService) {
 		if (null != this.executorService) {
 			this.executorService.shutdown();
 			try {
 				this.executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
 			} catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		this.executorService = executorService;
 	}
 
 	public Handler getHandler() {
 		if (null == handler) {
 			handler = new DefaultHandler();
 		}
 		return handler;
 	}
 
 	public void setHandler(Handler handler) {
 		this.handler = handler;
 	}
 
 	public boolean isStarted() {
 		return started;
 	}
 
 	private ServerSocket serverSocket;
 	private ExecutorService executorService;
 	private Handler handler;
 
 	public HttpServer() {
 	}
 
 	public HttpServer(int port) {
 		this.port = port;
 	}
 
 	public HttpServer start() throws IOException {
 		if (started) {
 			throw new IllegalStateException("server aleady started");
 		}
 		started = true;
 		if (isSsl()) {
 			try {
 				serverSocket = initSSL(port);
 			} catch (Exception e) {
 				if (null != handler) {
 					handler.onException(e);
 				}
 			}
 		} else {
 			serverSocket = new ServerSocket(port);
 		}
 		if (null == executorService) {
 			executorService = Executors.newSingleThreadExecutor();
 		}
 		if (null == handler) {
 			handler = new DefaultHandler();
 		}
 		portListenThread = new Thread() {
 			public void run() {
 				while (started) {
 					try {
 						final Socket socket = serverSocket.accept();
 						Runnable runnable = new Runnable() {
 							@Override
 							public void run() {
 								try {
 									Client client = new Client(socket, handler);
 									client.init();
 									client.handleRequest();
 									if (!client.isAsyncMode()) {
 										client.close();
 									}
 								} catch (Exception e) {
 									if (started && null != handler) {
 										handler.onException(e);
 									}
 								}
 							}
 						};
 						executorService.execute(runnable);
 					} catch (IOException e) {
 						handler.onException(e);
 					}
 				}
 			}
 		};
 		portListenThread.start();
 
 		return this;
 	}
 
 	public void stop() throws IOException, InterruptedException {
 		started = false;
 		if (null != serverSocket) {
 			serverSocket.close();
 		}
 		if (null != portListenThread) {
 			portListenThread.interrupt();
 		}
 		if (null != executorService) {
 			executorService.shutdown();
 			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
 			executorService = null;
 		}
 	}
 
 	public DefaultHandler getDefaultHandler() {
 		if (null != this.handler && this.handler instanceof DefaultHandler) {
 			return (DefaultHandler) this.handler;
 		}
 		DefaultHandler handler = new DefaultHandler();
 		this.handler = handler;
 		return handler;
 	}
 
 	public void setKeyStorePassword(char[] keyStorePassword) {
 		this.keyStorePassword = keyStorePassword;
 	}
 
 	public void setKeyStorePath(String keyStorePath) {
 		this.keyStorePath = keyStorePath;
 	}
 
 	public void setKeyStoreType(String keyStoreType) {
 		this.keyStoreType = keyStoreType;
 	}
 
 	private ServerSocket initSSL(int port) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
 		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
 		keyStore.load(getClass().getClassLoader().getResourceAsStream(keyStorePath), keyStorePassword);
 		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
 		keyManagerFactory.init(keyStore, certificatePassword);
 		SSLContext sSLContext = SSLContext.getInstance("TLS");
 		sSLContext.init(keyManagerFactory.getKeyManagers(), null, null);
 		SSLServerSocketFactory serverSocketFactory = sSLContext.getServerSocketFactory();
 		ServerSocket serverSocket = serverSocketFactory.createServerSocket(port, serverWaitQueue);
 		return serverSocket;
 	}
 
 	public void setCertificatePassword(char[] certificatePassword) {
 		this.certificatePassword = certificatePassword;
 	}
 
 	public int getServerWaitQueue() {
 		return serverWaitQueue;
 	}
 
 	public void setServerWaitQueue(int serverWaitQueue) {
 		this.serverWaitQueue = serverWaitQueue;
 	}
 
 	public boolean isSsl() {
 		return ssl;
 	}
 
 	public void setSsl(boolean ssl) {
 		this.ssl = ssl;
 	}
 }
