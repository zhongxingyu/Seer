 package auctionsniper.ui;
 
 import javax.swing.JFrame;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 
 public class MainWindow extends JFrame {
 	public static final String NAME = "Auction Sniper Main";
 	public static final String TITLE = "Auction Sniper";
 	
 	public static final String SNIPER_TABLE_NAME = "sniper_table";
 	public static final String STATUS_JOINING = "Joining";
 	public static final String STATUS_LOST = "Lost";
 	public static final String STATUS_BIDDING = "Bidding";
 	public static final String STATUS_WINNING = "Winning";
 	public static final String STATUS_WON = "Won";
 	
 	private final JTable sniperTable = createSniperTable();
 	
 	public MainWindow() {
 		super(TITLE);
 		setName(NAME);
 		add(sniperTable);
 		pack(); // fit to prefered size
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	
 	private JTable createSniperTable() {
		JTable table = new JTable(1,4);
 		table.setName(SNIPER_TABLE_NAME);
 		return table;
 	}
 
	public void joinAuction(String itemId) {
		int initialPrice = 0, initialBid = 0;
		sniperTable.setValueAt(itemId, 0, 0);
		sniperTable.setValueAt(initialPrice, 0, 1);
		sniperTable.setValueAt(initialBid, 0, 2);
		sniperTable.setValueAt(STATUS_JOINING, 0, 3);
	}

 	public void showStatusLost() {
 		showStatus(STATUS_LOST);
 	}
 
 	public void showStatusBidding() {
 		showStatus(STATUS_BIDDING);
 	}
 
 	public void showStatusWinning() {
 		showStatus(STATUS_WINNING);
 	}
 
 	public void showStatusWon() {
 		showStatus(STATUS_WON);
 	}
 
 	private void showStatus(final String status) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
				sniperTable.setValueAt(status, 0, 3);
 			}
 		});
 	}
 }
