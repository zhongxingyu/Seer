 package contollers;
 
 import java.util.LinkedList;
 
 import beans.AuctionListBean;
 import beans.BidListBean;
 import beans.WinningAuctionListBean;
 import ass2.Emailer;
 import ass2.JDBCConnector;
 import ass2.ParameterManager;
 
 public class GetAuctionController extends MasterFormBasedController {
 	
 	private final static String NO_BIDS = "There were no bids to your auction!";
 	private final static String BEAT_RESERVE = "You have beat the reserve!";
 	private final static String PAY_RESERVE = "You must pay the reserve price to win the auction!";
 	
 	public GetAuctionController(ParameterManager params) {
 		super(params);
 	}
 
 	protected void createForm() {
 		if (paramManager.hasParameter("id")) formManager.addForm("id", paramManager.getIndividualParam("id"));
 		if (paramManager.hasParameter("author")) formManager.addForm("id", paramManager.getIndividualParam("author"));
 		if (paramManager.hasParameter("title")) formManager.addForm("id", paramManager.getIndividualParam("title"));
 	}
 
 	//TODO set messages, also Integer.parseInt blows up if fails
 	public AuctionListBean getAuction() {
 		int id = (paramManager.hasParameter("id")) ? Integer.parseInt(paramManager.getIndividualParam("id")) : 0;
 		String author = (paramManager.hasParameter("author")) ? paramManager.getIndividualParam("author") : null;
 		String title = (paramManager.hasParameter("title")) ? paramManager.getIndividualParam("title") : null;
 		boolean finished = (paramManager.hasParameter("only_not_finished")) ? true : false;
 		return JDBCConnector.getAuction(id, author, title, finished);
 	}
 	
 	public WinningAuctionListBean getWinningAuctions(String author) {
 		return JDBCConnector.getWinningAuctions(author);
 	}
 	
 	public static void popAuction(int id, boolean reserve) {
 		BidListBean biddings = JDBCConnector.getBiddings(id, true);
 		//TODO make transaction i suppose?
 		JDBCConnector.finishAuction(id);
		System.out.println("asd");
 		AuctionListBean alb = JDBCConnector.getAuction(id, null, null, false);
 		if (!biddings.getBids().isEmpty()) {
 			if (!reserve) {
 				JDBCConnector.addWinningAuction(id, biddings.getBids().get(0).getID());
 				JDBCConnector.addAlert(alb.getAuctions().get(0).getAuthor(), id, PAY_RESERVE);
 				String title = "You have won (reserve).";
 				String msg = "You have completed an auction but must pay the reserve price to win the auction. Login to the site to review the action";
 				Emailer e = new Emailer(JDBCConnector.getUserBean(biddings.getBids().get(0).getAuthor(), false).getEmail(), title, msg);
 				e.email();
 			} else
 				JDBCConnector.addAlert(alb.getAuctions().get(0).getAuthor(), id, BEAT_RESERVE);
 		} else {
 			if (!alb.isEmpty())
 				JDBCConnector.addAlert(alb.getAuctions().get(0).getAuthor(), id, NO_BIDS);
 		}
 	}
 	
 	//TODO This shoul dbe a transaction 
 	public void winAuction(String bidder) {
 		//TODO get the emails and email them i suppose
 		
 		int id = Integer.parseInt(paramManager.getIndividualParam("id"));
 		if (JDBCConnector.isOwnerWinningAuction(id, bidder)) {
 			String[] emails = JDBCConnector.getUserEmailsFromWA(id);
 			String tmp = emails[1];
 			String title = "You have won.";
 			String msg = "You have completed an auction. Your partner in this auction can be contacted on the email: " + tmp;
 			Emailer e = new Emailer(emails[0], title, msg);
 			e.email();
 			tmp = emails[0];
 			e = new Emailer(emails[1], title, msg);
 			e.email();
 			
 			JDBCConnector.deleteWinningAuction(id);
 		}
 	}
 	
 	public void haltAuction(String url) {
 		JDBCConnector.haltAuction(Integer.parseInt(paramManager.getIndividualParam("id")));
 		String email = JDBCConnector.getOwnerEmailFromAuction(Integer.parseInt(paramManager.getIndividualParam("id")));
 		String title = "Your auction has been halted!.";
 		String msg = "Your auction has been halted!. You can still view the auction here: " + url + "?action=auction&id=" + paramManager.getIndividualParam("id");
 		Emailer e = new Emailer(email, title, msg);
 		e.email();
 	}
 	
 	public LinkedList<Integer> haltAllAuctions() {
 		if (paramManager.hasParameter("username") && "".equals(paramManager.hasParameter("username")))
 			return JDBCConnector.haltAllAuctions(paramManager.getIndividualParam("username"));
 		return new LinkedList<Integer>();
 	}
 	
 	public void deleteAuction() {
 		JDBCConnector.deleteAuction(Integer.parseInt(paramManager.getIndividualParam("id")));
 	}
 	
 }
