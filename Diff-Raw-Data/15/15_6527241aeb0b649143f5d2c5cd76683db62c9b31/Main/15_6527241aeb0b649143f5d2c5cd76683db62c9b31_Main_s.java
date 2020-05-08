 package auctionsniper;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.SwingUtilities;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 
 import auctionsniper.ui.MainWindow;
 import auctionsniper.xmpp.AuctionMessageTranslator;
 import auctionsniper.xmpp.XMPPAuction;
 
 public class Main {
 	private static final int ARG_HOSTNAME = 0;
 	private static final int ARG_USERNAME = 1;
 	private static final int ARG_PASSWORD = 2;
 	private static final int ARG_ITEM_ID = 3;
 	
 	public static final String AUCTION_RESOURCE = "Auction";
 	public static final String ITEM_ID_AS_LOGIN = "auction-%s";
 	public static final String AUCTION_ID_FORMAT =
 		ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
 	
 	public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
 	public static final String PRICE_FORMAT_MESSAGE = 
 		"SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;";
 	public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %s;";
 	public static final String CLOSE_FORMAT_MESSAGE = "SOLVersion: 1.1; Event: CLOSE;";
 	
 	public static void main(String... args) throws Exception {
 		String host = args[ARG_HOSTNAME], 
 			user = args[ARG_USERNAME], 
 			password = args[ARG_PASSWORD],
 			itemId = args[ARG_ITEM_ID];
 
 		XMPPConnection connection = connectTo(host, user, password);
 		
 		Main main = new Main();
 		main.joinAuction(connection, itemId);
 	}
 	
 	private MainWindow ui;
 	
 	public Main() throws Exception {
 		startUserInterface();
 	}
 
 	private void startUserInterface() throws Exception {
 		SwingUtilities.invokeAndWait(new Runnable() {
 			public void run() {
 				ui = new MainWindow();
 			}
 		});
 	}
 
 	private void joinAuction(XMPPConnection connection, String itemId) throws XMPPException {
 		ui.joinAuction(itemId);
 		disconnectWhenUiCloses(connection);
 		String auctionId = String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
 		final Chat chat = connection.getChatManager().createChat(auctionId, null);
 		
 		XMPPAuction auction = new XMPPAuction(chat);
 		AuctionSniper sniper = new AuctionSniper(itemId, auction, new SniperStateDisplayer());
 		chat.addMessageListener(new AuctionMessageTranslator(connection.getUser(), sniper));
 		auction.join();
 	}
 	
 	private void disconnectWhenUiCloses(final XMPPConnection connection) {
 		ui.addWindowListener(new WindowAdapter() {
 		   @Override 
 		   public void windowClosed(WindowEvent e) {
 		      connection.disconnect();
 		   }
 		});
 	}
 	
 	private static XMPPConnection connectTo(String hostname, String username, String password)
 	throws XMPPException {
 		XMPPConnection connection = new XMPPConnection(hostname);
 		connection.connect();
 		connection.login(username, password, AUCTION_RESOURCE);
 		return connection;
 	}
 	
 	class SniperStateDisplayer implements SniperListener {
 		public void sniperBidding(SniperState state) {
			ui.showStatusBidding();
 		}
 
 		public void sniperWinning() {
 			ui.showStatusWinning();
 		}
 		
 		public void sniperLost() {
 			ui.showStatusLost();
 		}
 
 		public void sniperWon() {
 			ui.showStatusWon();
 		}
 	}
 }
