 package Test.TestClient;
 
 import com.mycompany.reservationsystem.peer.client.PeerClient;
 import com.mycompany.reservationsystem.peer.client.booking.BookingClient;
 import com.mycompany.reservationsystem.peer.deamon.PeerStateDeamon;
 
 public class ClientTestClass {
 	@SuppressWarnings("deprecation")
 	public static void main(String[] args) {
 		PeerStateDeamon peerDeamon = new PeerStateDeamon();
 		PeerClient peerClient = new PeerClient();
 		BookingClient bookingClient = new BookingClient();
 		
 		peerDeamon.start();
 		peerClient.start();
 		bookingClient.start();
 		
 		try {
 			System.out.println("Hello");
 			Thread.currentThread();
			Thread.sleep(100000);
 		} 
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		peerDeamon.stop();
 		peerClient.stop();
 		bookingClient.stop();
 	}
 }
