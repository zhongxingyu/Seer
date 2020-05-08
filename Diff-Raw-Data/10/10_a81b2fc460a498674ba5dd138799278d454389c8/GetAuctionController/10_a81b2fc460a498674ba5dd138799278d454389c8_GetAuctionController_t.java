 package contollers;
 
 import beans.AuctionListBean;
 import beans.BidListBean;
 import beans.WinningAuctionListBean;
 import ass2.JDBCConnector;
 import ass2.ParameterManager;
 
 public class GetAuctionController extends MasterFormBasedController {
 	
 	private final static String NO_BIDS = "There were no bids to your auction!";
 	
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
 		return JDBCConnector.getAuction(id, author, title);
 	}
 	
 	public WinningAuctionListBean getWinningAuctions(String author) {
 		return JDBCConnector.getWinningAuctions(author);
 	}
 	
	public static void popAuction(int id) {
 		BidListBean biddings = JDBCConnector.getBiddings(id, true);
 		//TODO make transaction i suppose?
 		JDBCConnector.finishAuction(id);
 		if (!biddings.getBids().isEmpty()) {
 			 JDBCConnector.addWinningAuction(id, biddings.getBids().get(0).getID());
 		} else {
 			AuctionListBean alb = JDBCConnector.getAuction(id, null, null);
 			if (!alb.isEmpty())
 				JDBCConnector.addAlert(alb.getAuctions().get(0).getAuthor(), id, NO_BIDS);
 		}
 	}
 	
 	//TODO This shoul dbe a transaction 
 	public void winAuction(String bidder) {
 		//TODO get the emails and email them i suppose
 		
 		int id = Integer.parseInt(paramManager.getIndividualParam("id"));
 		if (JDBCConnector.isOwnerWinningAuction(id, bidder))
 			JDBCConnector.deleteAuction(id);
 	}
 	
 }
