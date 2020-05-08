 package haw.ai.rn;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.Date;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 public class Connection implements Runnable {
 	private Socket socket = null;
 	private String delimiter;
 	private int requestCounter = 0;
 	private long connectionTime = System.currentTimeMillis();
 	
 	public Connection(Socket socket, String delimiter) {
 		this.setSocket(socket);
 		this.delimiter = delimiter;
 	}
 
 	public void setSocket(Socket socket) {
 		this.socket = socket;
 	}
 	
 	@Override
 	public void run() {
 		boolean connected = true;
 		while (connected) {
 			try {
 				String msg = new Scanner(getSocket().getInputStream()).useDelimiter(delimiter).next();
 				if (msg.trim().equals("end")) {
 					getSocket().getOutputStream().write(("abort"+delimiter).getBytes());
 					System.out.println(String.format("%s requests end of connection", getSocket().getInetAddress()));
 				}
 				else {
 					getSocket().getOutputStream().write((msg+delimiter).toUpperCase().getBytes());
 				}
 				log();
 			}
 			catch (NoSuchElementException e) {
 				connected = false;
 			}
 			catch (Exception e) {
 				connected = false;
 				e.printStackTrace();
 			}
 		}
 		try {
 			socket.close();
 		} catch (IOException e) {
 			System.out.println(String.format("Can't close connection to %s", socket.getInetAddress()));
 			e.printStackTrace();
 		}
 	}
 
 	private void log() {
     	String now = (new Date()).toString();
     	InetAddress ip = getSocket().getInetAddress();
     	
     	System.out.println(String.format("[%s] %s ClientRequest#: %d ActiveSince: %s", now, ip, incRequestCounter(), getConnectionTime()));
     }
 	
 	private int incRequestCounter() {
 		return ++requestCounter ;
 	}
 
 	private Long getConnectionTime() {
 		
 		return System.currentTimeMillis() - connectionTime ;
 	}
 
 	public Socket getSocket() {
 		return socket;
 	}
 }
