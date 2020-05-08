 package controller;
 
 import exceptions.MailSenderException;
 import exceptions.ServiceLocatorException;
 import group.Bid;
 import group.Item;
 import group.UserBean;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jdbc.DBConnectionFactory;
 import jdbc.MailSenderService;
 
 public class CommandConfirmBid implements Command {
 	
 	private static final String page = "/BidSuccess.jsp";
 	private static final String error = "/WEB-INF/error.jsp";
 	private static final String login = "/controller?action=loginPage";
 
 	public CommandConfirmBid() {
 		super();
 	}
 
 	@Override
 	public String execute(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException, SQLException {
 
 		if (!Controller.isLoggedIn(request, response))
 			return login;
 
 		String itemIdString = request.getParameter("item");
 		String bidAmountString = request.getParameter("bidAmount");
 		int itemId = 0;
 		int bidAmount= 0;
 		try {
 		    itemId = Integer.parseInt(itemIdString);
 		} catch ( Exception e ) {
 		    itemId = 0;
 		}
 		try {
 		    bidAmount = Integer.parseInt(bidAmountString);
 		} catch ( Exception e ) {
 //			e.printStackTrace();
 			request.setAttribute("errorMsg", "Invalid bid $"+bidAmountString);
 			return error;
 		}
 
 		String username = Controller.getUsername(request, response);
 		UserBean user = new UserBean();
 		UserBean prevBidder = new UserBean();
 		int oldPrice = 0;
 		Bid bid = new Bid();
 		Item item = new Item();
 		Connection conn = null;
 		try {
 			conn = DBConnectionFactory.getConnection();
 			conn.setAutoCommit(false);
 
 			user = UserBean.initializeFromUsername(conn, username);
 			System.out.println("amount: " + bidAmount);
 			bid.setBid(bidAmount);
 			bid.setBidder(user.getUserid());
 			bid.setBidTimeAsCurrentTime();
 			bid.setItem(itemId);
 
 			item = Item.initializeFromId(conn, itemId);
 			if (item.isClosed()) {
 				request.setAttribute("errorMsg", "Auction is closed");
 				return error;
 			}
 			oldPrice = item.getCurrentBiddingPrice();
 			if (bid.getBid() < item.getMinimumBid()) {
 				request.setAttribute("errorMsg", "Bid is lower than minimum bid");
 				return error;
 			}
			prevBidder = UserBean.initializeFromId(conn, item.getCurrentBidder());
 			bid.insert(conn);
 
 			conn.commit();
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (conn != null)
 				conn.rollback();
 			return error;
 		} finally {
 			if (conn != null)
 				conn.close();
 		}
 		
 		// send email
		if (user.getUserid() != prevBidder.getUserid()) {
 			try {
 				MailSenderService mail = MailSenderService.getMailSender();
 				StringBuffer text = new StringBuffer();
 				text.append("You have been outbidden on item:\n");
 				text.append(item.getTitle());
 				text.append("\nYour bid:\n");
 				text.append("$"+oldPrice);
 				text.append("\nNew bid:\n");
 				text.append("$"+bid.getBid());
 				text.append("\nThe auction is scheduled to end on:\n");
 				text.append(item.getCtime());
 				text.append("\nLink to auction:\n");
 				String link = request.getScheme() + "://"
 						+ request.getServerName() +":" + request.getServerPort()
 						+ request.getContextPath() + "/"
 						+ "controller?action=itemPage&item="
 						+ item.getId();
 				text.append(link);
 				mail.sendMessage(prevBidder.getUseremail(), "Outbidden on '"+item.getTitle()+"'", text);
 			} catch (ServiceLocatorException e) {
 				e.printStackTrace();
 			} catch (MailSenderException e) {
 				e.printStackTrace();
 			} catch (AddressException e) {
 				e.printStackTrace();
 			} catch (MessagingException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			MailSenderService mail = MailSenderService.getMailSender();
 			StringBuffer text = new StringBuffer();
 			text.append("You have successfully made a bid on the item:\n");
 			text.append(item.getTitle());
 			text.append("\nYour bid:\n");
 			text.append("$"+bid.getBid());
 			text.append("\nThe auction is scheduled to end on:\n");
 			text.append(item.getCtime());
 			text.append("\nLink to auction:\n");
 			String link = request.getScheme() + "://"
 					+ request.getServerName() +":" + request.getServerPort()
 					+ request.getContextPath() + "/"
 					+ "controller?action=itemPage&item="
 					+ item.getId();
 			text.append(link);
 			mail.sendMessage(user.getUseremail(), "Successful bid on '"+item.getTitle()+"'", text);
 		} catch (ServiceLocatorException e) {
 			e.printStackTrace();
 		} catch (MailSenderException e) {
 			e.printStackTrace();
 		} catch (AddressException e) {
 			e.printStackTrace();
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		}
 
 		request.setAttribute("item", item);
 		request.setAttribute("bid", bid);
 		return page;
 	}
 
 
 }
