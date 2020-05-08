 package net.mms_projects.copy_it;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketAddress;
 
 public class ApplicationLock {
 
 	private File lockFile;
 	private ServerSocket server;
 	private ServerThread thread;
 	private boolean locked = false;
 
 	public ApplicationLock(File path) {
 		this.lockFile = new File(path, ".lock");
 	}
 
 	public void lock() throws LockException {
 		if (this.isLocked()) {
 			throw new LockException("The app is already locked");
 		}
 		BufferedWriter writer = null;
 		try {
 			this.server = new ServerSocket(0);
 
 			this.thread = new ServerThread();
 			this.thread.start();
 
 			writer = new BufferedWriter(new FileWriter(this.lockFile));
 			writer.write(Integer.toString(this.server.getLocalPort()));
 		} catch (IOException e) {
 			throw new LockException(e);
 		} finally {
 			try {
 				if (writer != null) {
 					writer.close();
 				}
 			} catch (IOException e) {
 			}
 		}
 		this.locked = true;
 	}
 
 	public void unlock() {
 		if (!this.isLocked()) {
 			return;
 		}
 		this.lockFile.delete();
 		this.thread.interrupt();
 		try {
 			this.server.close();
 		} catch (IOException e) {
 		}
 		this.locked = false;
 	}
 
 	public boolean isLocked() {
 		return this.locked;
 	}
 
 	public boolean isRunning() {
 		BufferedReader reader = null;
 		Socket socket = null;
 		try {
 			if (!this.lockFile.exists()) {
 				return false;
 			}
 			reader = new BufferedReader(new FileReader(this.lockFile));
 			String content = reader.readLine();
 			if (content == null) {
 				return false;
 			}
 			SocketAddress sockaddr = new InetSocketAddress("127.0.0.1",
 					Integer.parseInt(content));
 			socket = new Socket();
 			socket.connect(sockaddr, 100);
 			if (socket.isConnected()) {
 				return true;
 			}
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {
 			return false;
 		} finally {
 			try {
 				if (reader != null) {
 					reader.close();
 				}
 				if (socket != null) {
 					socket.close();
 				}
 			} catch (IOException e) {
 			}
 		}
 		return false;
 	}
 
 	public class LockException extends Exception {
 		private static final long serialVersionUID = -1567053088980704966L;
 
 		public LockException(String message) {
 			super(message);
 		}
 
 		public LockException(Throwable clause) {
 			super(clause);
 		}
 	}
 
 	private class ServerThread extends Thread {
 		@Override
 		public void run() {
 			while (true) {
 				if (this.isInterrupted()) {
 					break;
 				}
 				try {
 					if (!server.isClosed()) {
 						server.accept();
 					}
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 }
