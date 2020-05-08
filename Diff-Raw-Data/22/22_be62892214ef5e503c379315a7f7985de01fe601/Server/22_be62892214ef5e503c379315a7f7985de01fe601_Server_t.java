 package com.marakana.clientserver;
 
 import java.io.BufferedReader;
import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import com.marakana.concurrency.Logger;
 
 public class Server {
 
	private static class Task implements Runnable, Closeable {
 		private final Socket client;
 		private final Logger logger;
 
 		public Task(Socket client, Logger logger) {
 			this.client = client;
 			this.logger = logger;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				try {
 					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 					try {
 						for (String line = in.readLine(); line != null; line = in.readLine()) {
 							logger.put(line);
 						}
 					} finally {
 						in.close();
 					}
 				} finally {
 					client.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}

		@Override
		public void close() throws IOException {
			client.close();
		}

 	}
 
 	public static void main(String[] args) {
 		ExecutorService executor = Executors.newFixedThreadPool(10);
 		Logger logger = new Logger();
 		executor.submit(logger);
 
 		try {
 			ServerSocket server = new ServerSocket(31337);
 			try {
				// main loop: accept client connections and handle them asynchronously
 				while (true) {
 					Socket client = server.accept();
 					executor.submit(new Task(client, logger));
 				}
 			} finally {
				// clean up as best as reasonably possible
 				server.close();
				for (Runnable runnable : executor.shutdownNow()) {
					if (runnable instanceof Task) {
						Task task = (Task) runnable;
						task.close();
					}
				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
