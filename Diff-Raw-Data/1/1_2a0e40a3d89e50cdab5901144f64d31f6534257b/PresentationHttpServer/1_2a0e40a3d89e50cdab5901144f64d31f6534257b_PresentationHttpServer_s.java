 package com.nitorcreations.presentation;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
import java.rmi.server.ServerCloneException;
 import java.util.Properties;
 import java.util.concurrent.Executors;
 
 import com.sun.net.httpserver.HttpContext;
 import com.sun.net.httpserver.HttpServer;
 
 
 @SuppressWarnings("restriction")
 public class PresentationHttpServer {
 	private HttpServer server;
 
 	public PresentationHttpServer(int port, PresentationController controller) throws IOException {
 		InetSocketAddress addr = new InetSocketAddress(port);
 		server = HttpServer.create(addr, 0);
 
 		HttpContext cc = server.createContext("/run/", new RequestHandler("run", controller));
 		if (System.getProperty("httprunpasswords") != null) {
 			Properties passwd = new Properties();
 			passwd.load(new FileInputStream(System.getProperty("httprunpasswords")));
 			cc.setAuthenticator(new DigestAuthenticator(passwd, "run-presentation"));
 		}
 		cc = server.createContext("/follow/", new RequestHandler("follow", controller));
 		if (System.getProperty("httpfollowpasswords") != null) {
 			Properties passwd = new Properties();
 			passwd.load(new FileInputStream(System.getProperty("httpfollowpasswords")));
 			cc.setAuthenticator(new DigestAuthenticator(passwd, "follow-presentation"));
 		}
 		cc = server.createContext("/", new RequestHandler(""));
 		if (System.getProperty("httpdefaultpasswords") != null) {
 			Properties passwd = new Properties();
 			passwd.load(new FileInputStream(System.getProperty("httpdefaultpasswords")));
 			cc.setAuthenticator(new DigestAuthenticator(passwd, "default-presentation"));
 		}
 		cc = server.createContext("/download/", new DownloadHandler());
 		if (System.getProperty("httpdownloadpasswords") != null) {
 			Properties passwd = new Properties();
 			passwd.load(new FileInputStream(System.getProperty("httpdownloadpasswords")));
 			cc.setAuthenticator(new DigestAuthenticator(passwd, "download-presentation"));
 		}
 		server.setExecutor(Executors.newCachedThreadPool());
 		server.start();
 		System.out.println("Server is listening on port " + port );
 	}
 
 	protected static class Range {
 
 		public int start;
 		public int end;
 		public int rangeLen;
 		public int length;
 
 		public boolean validate() {
 			if (end >= length)
 				end = length - 1;
 			rangeLen=end-start+1;
 			return (start >= 0) && (end >= 0) && (start <= end) && (length > 0);
 		}
 	}
 
 	public void quit() {
 		server.stop(0);
 	}
 }
