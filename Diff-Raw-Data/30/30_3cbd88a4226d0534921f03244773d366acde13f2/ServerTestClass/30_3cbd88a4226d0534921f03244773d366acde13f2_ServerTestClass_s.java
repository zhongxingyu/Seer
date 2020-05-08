 package Test.TestClient;
 
 import com.mycompany.reservationsystem.peer.deamon.PeerStateDeamon;
 import com.mycompany.reservationsystem.peer.server.PeerServer;
 import com.mycompany.reservationsystem.peer.server.booking.BookingServer;
 
 public class ServerTestClass {
 	@SuppressWarnings("deprecation")
 	public static void main(String[] args) {
 		PeerStateDeamon peerDeamon = new PeerStateDeamon();
 		PeerServer peerServer = new PeerServer();
 		BookingServer bookingServer = new BookingServer();
 		
 		peerDeamon.start();
 		peerServer.start();
 		bookingServer.start();
 		
 		try {
 			System.out.println("Hello");
 			Thread.currentThread();
			Thread.sleep(100000);
 		} 
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		peerDeamon.stop();
 		peerServer.stop();
 		bookingServer.stop();
 	}
 }
