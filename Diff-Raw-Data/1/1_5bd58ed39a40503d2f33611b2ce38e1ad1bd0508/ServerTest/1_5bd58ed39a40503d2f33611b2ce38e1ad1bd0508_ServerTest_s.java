 package model;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class ServerTest implements Runnable{
 
 	private ServerSocket server;
 	
 	@Test
 	public void test() {
 		try {
 			server = new ServerSocket(2345);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Server s = new Server();
		s.sendCommand("lol");
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		assertTrue(true);
 	}
 
 }
