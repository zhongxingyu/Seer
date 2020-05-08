 package ucbang.launchers;
 
 import ucbang.network.*;
 
 public class Host {
 	public static void main(String args[]) {
 		new Server(12345);
 		new Client("localhost", true, "Host");
		//new Client("localhost", true);
 		// new Client("localhost", true, "Test client 0");
 	}
 }
