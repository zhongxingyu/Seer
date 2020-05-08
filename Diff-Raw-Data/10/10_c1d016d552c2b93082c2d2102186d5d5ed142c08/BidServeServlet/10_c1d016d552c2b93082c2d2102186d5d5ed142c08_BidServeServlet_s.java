 package com.epam.lab.buyit.controller.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.epam.lab.buyit.controller.email.EmailMessageBuilder;
 import com.epam.lab.buyit.controller.exception.AuctionAllreadyClosedException;
 import com.epam.lab.buyit.controller.exception.BidAmountException;
 import com.epam.lab.buyit.controller.service.auction.AuctionServiceImp;
 import com.epam.lab.buyit.controller.service.bid.BidService;
 import com.epam.lab.buyit.controller.service.bid.BidServiceImp;
 import com.epam.lab.buyit.controller.service.product.ProductService;
 import com.epam.lab.buyit.controller.service.product.ProductServiceImpl;
 import com.epam.lab.buyit.controller.service.user.UserServiceImpl;
 import com.epam.lab.buyit.model.Auction;
 import com.epam.lab.buyit.model.Product;
 import com.epam.lab.buyit.model.User;
 
 public class BidServeServlet extends HttpServlet {
 	private static final Logger LOGGER = Logger
 			.getLogger(BidServeServlet.class);
 	private static final long serialVersionUID = 1L;
 	private BidService bidService;
 	private ProductService productService;
 	private AuctionServiceImp auctionService;
 	private UserServiceImpl userService;
 
 	public void init() {
 		bidService = new BidServiceImp();
 		productService = new ProductServiceImpl();
 		auctionService = new AuctionServiceImp();
 		userService = new UserServiceImpl();
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		serve(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		serve(request, response);
 	}
 
 	private void serve(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		EmailMessageBuilder emailMessageBuilder = new EmailMessageBuilder();
 		double bidAmount = Double.parseDouble(request.getParameter("bid"));
 		int productId = Integer.parseInt(request.getParameter("id_product"));
 		User user = (User) request.getSession(false).getAttribute("user");
 		Auction auction = auctionService.getByProductId(productId);
 		int oldBuyerId = bidService.getWinUserIdByAuctionId(auction
 				.getIdAuction());
 		User oldBuyer = userService.getItemById(oldBuyerId);
 		Product product = productService.getItemById(productId);
 
 		try {
 			if (bidService.serveBid(user.getIdUser(), productId, bidAmount)) {
 				LOGGER.info("Bid was placed");
 				if (oldBuyer != null) {
 					emailMessageBuilder.sendYourBidKilldForm(oldBuyer, product);
 				}
 				emailMessageBuilder.sendPlaceABidForm(user, product);
 				request.setAttribute("product", product);
 				request.setAttribute("bidAmount", bidAmount);
 				request.setAttribute("actionMessage", "You place a bid on");
 			} else {
 				LOGGER.error("Place a bid query was failed");
 				request.setAttribute("queryFail",
 						"Sorry, someone have just exceeded your bid");
 			}
 		} catch (BidAmountException e) {
 			LOGGER.error(e);
 			request.setAttribute("bidAmountException", true);
 		} catch (AuctionAllreadyClosedException e) {
 			request.setAttribute("auctionCloseException", true);
 			LOGGER.error(e);
 		} finally {
 			request.getRequestDispatcher("bid_information").forward(request,
 					response);
 		}
 	}
 }
