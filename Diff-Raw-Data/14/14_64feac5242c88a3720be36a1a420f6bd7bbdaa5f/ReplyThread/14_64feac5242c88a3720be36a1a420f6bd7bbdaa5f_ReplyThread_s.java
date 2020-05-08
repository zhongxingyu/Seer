 package at.ac.tuwien.dslab2.service.biddingClient;
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.security.GeneralSecurityException;
 import java.util.regex.Pattern;
 
 import at.ac.tuwien.dslab2.service.KeyService;
 import at.ac.tuwien.dslab2.service.net.TCPClientNetworkService;
 
 import javax.crypto.SecretKey;
 
 class ReplyThread extends Thread {
     private final BiddingClientService bcs;
     private final KeyService ks;
     private volatile boolean stop;
 	private final TCPClientNetworkService ns;
 	private final ReplyListener listener;
 
     public ReplyThread(TCPClientNetworkService ns, ReplyListener listener, BiddingClientService bcs, KeyService ks)
 			throws IOException {
 		if (ns == null)
 			throw new IllegalArgumentException(
 					"The TCPClientNetworkService is null");
 
 		this.ns = ns;
         this.bcs = bcs;
         this.ks = ks;
         this.listener = listener;
 	}
 
 	public boolean isConnected() {
 		return ns != null;
 	}
 
 	@Override
 	public void run() {
 		String reply = null;
 		stop = false;
 
 		if (!isConnected())
 			throw new IllegalStateException("Service not connected!");
 
 		try {
 			try {
 				while (!stop) {
 					reply = ns.receive().trim();
 
 					if (listener != null){
                         String userName = bcs.getUserName();
                         //if it's a list command
                         if (userName != null && isListResponse(reply)) {
                             if (!hasCorrectMAC(reply, userName)) {
                                 listener.displayReply(
                                         "The provided Message Authentication Code is incorrect!\n"
                                       + "The command will be retransmitted!");
                                 bcs.submitCommand("!list");
                                 reply = ns.receive();
                                 if (!hasCorrectMAC(reply, userName)) {
                                     listener.displayReply(
                                         "The provided Message Authentication Code is incorrect again!\n"
                                       + "No further retransmission");
                                     continue;
                                 }
                             }
                         }
                         listener.displayReply(reply);
                     }
 				}
 			} finally {
 				if (ns != null)
 					ns.close();
 			}
 		} catch (IOException e) {
 			// The if-clause down here is because of what is described in
 			// http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
 			// under "What if a thread doesn't respond to Thread.interrupt?"
 
 			// So the socket is closed when the ClientNetworkService is
 			// closed and in that special case no error should be
 			// propagated.
 			if (!(stop && e.getClass() == SocketException.class)) {
 				UncaughtExceptionHandler eh = this
 						.getUncaughtExceptionHandler();
 				if (eh != null)
 					eh.uncaughtException(this, e);
 			}
 		}
 	}
 
     private boolean isListResponse(String reply) {
         return Pattern.compile("\\d+\\..*", Pattern.DOTALL).matcher(reply).matches()
         || reply.startsWith("There are currently no auctions running!");
     }
 
     private boolean hasCorrectMAC(String message, String userName) {
         try {
             String[] chunks = message.split("\u001f");
             if (chunks.length != 2) {
                 return false;
             }
             byte[] data = chunks[0].getBytes();
            byte[] actualMAC = chunks[1].getBytes();
             SecretKey secretKey = ks.createKeyFor(userName);
             byte[] expectedMAC = ks.createHashMAC(secretKey, data);
 
             return ks.verifyHashMAC(expectedMAC, actualMAC);
         } catch (Exception e) {
             if (listener != null)
                 listener.displayReply(e.getMessage());
         }
          return false;
     }
 
 	public void close() throws IOException {
 		stop = true;
 		this.interrupt();
 		if (ns != null)
 			ns.close();
 	}
 
 }
