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
 	private String keyPath;
 	private int serverWaitQueue;
 	private boolean ssl;
 
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
 		if (null != executorService) {
 			executorService.shutdown();
 			try {
 				executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
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
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				while (started) {
 					try {
 						Socket socket = serverSocket.accept();
 						Client client = new Client(socket, handler);
 						client.init();
 						client.handleRequest();
						if (client.isAsyncMode()) {
 							client.close();
 						}
 					} catch (Exception e) {
 						if (started && null != handler) {
 							handler.onException(e);
 						}
 					}
 				}
 			}
 		};
 		executorService.execute(runnable);
 		return this;
 	}
 
 	public void stop() throws IOException, InterruptedException {
 		started = false;
 		if (null != serverSocket) {
 			serverSocket.close();
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
 
 	private ServerSocket initSSL(int port) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
 		KeyStore keyStore = KeyStore.getInstance("JKS");
 		char[] keyStorePassword = "changeit".toCharArray();
 		keyStore.load(getClass().getClassLoader().getResourceAsStream("default_keystore.jks"), keyStorePassword);
 		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
 		keyManagerFactory.init(keyStore, keyStorePassword);
 		SSLContext sSLContext = SSLContext.getInstance("TLS");
 		sSLContext.init(keyManagerFactory.getKeyManagers(), null, null);
 		SSLServerSocketFactory serverSocketFactory = sSLContext.getServerSocketFactory();
 		ServerSocket serverSocket = serverSocketFactory.createServerSocket(port, serverWaitQueue);
 		return serverSocket;
 	}
 
 	public String getKeyPath() {
 		return keyPath;
 	}
 
 	public void setKeyPath(String keyPath) {
 		this.keyPath = keyPath;
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
