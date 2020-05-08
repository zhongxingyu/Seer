 package com.ebay.glass;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.ebay.sdk.ApiContext;
 import com.ebay.sdk.call.PlaceOfferCall;
 import com.ebay.soap.eBLBaseComponents.AmountType;
 import com.ebay.soap.eBLBaseComponents.BidActionCodeType;
 import com.ebay.soap.eBLBaseComponents.CurrencyCodeType;
 import com.ebay.soap.eBLBaseComponents.OfferType;
 import com.ebay.soap.eBLBaseComponents.SellingStatusType;
 
 /**
  * Servlet implementation class PlaceOfferNow
  */
 public class PlaceOffer extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public PlaceOffer() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		PrintWriter pw = response.getWriter();
 		try {
 			int status = 1;
 			String message = "U R highest bidder";
 			JSONObject jobj = new JSONObject();
 
 			try {
 				String itemId = request.getParameter("itemId");
 				Double finalPrice = 0.0;
 				if (request.getParameter("amount") != null) {
 					finalPrice = Double.parseDouble((String) request
 							.getParameter("amount"));
 				} else {
 					Double price = getCurrentPrice(itemId);
 					if (price == 0) {
 						pw.println("error");
 						return;
 					}
 					finalPrice = getBidIncrements(price);
 				}
 				System.out.println(request.getParameter("amount"));
 				ApiContext apiContext = GetApiContext.getApiContext();
 				PlaceOfferCall apiCall = new PlaceOfferCall(apiContext);
 				apiCall.setItemID(itemId);
 
 				OfferType offer = new OfferType();
 				offer.setAction(BidActionCodeType.BID);
 
 				AmountType amount = new AmountType();
 				amount.setCurrencyID(CurrencyCodeType.USD);
 				amount.setValue(finalPrice);
 
 				offer.setMaxBid(amount);
 				offer.setQuantity(1);
 				apiCall.setOffer(offer);
 
 				apiCall.setEndUserIP("195.34.23.32");
 
 				System.out.println("Begin to cal eBay API, please wait ... ");
 				SellingStatusType sellingStatus = apiCall.placeOffer();
 				Double currentPrice = sellingStatus.getCurrentPrice()
 						.getValue();
 				System.out.println("cr " +currentPrice);
 				System.out.println("fr " +finalPrice);
 
 				if (currentPrice <= finalPrice) {
 					status = 0;
 				} else {
 					status = 1;
 					message = "Outbid. Bid atleast $"
 							+ sellingStatus.getMinimumToBid().getValue();
 				}
 				System.out.println("End to cal eBay API, show call result ...");
 			} catch (Exception e) {
 				System.out.println("Fail to get eBay official time.");
 				status = 2;
				message = "Your Bid < current price";
 				e.printStackTrace();
 			}
 			jobj.put("ack", status);
 			jobj.put("message", message);
 			pw.println(jobj);
 		} catch (JSONException je) {
 			pw.print("error");
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	private Double getCurrentPrice(String itemId) {
 		try {
 			JSONObject itemDetails = GetItemDetails.getItemDetails(itemId);
 			Double price;
 
 			price = itemDetails.getJSONObject("Item")
 					.getJSONObject("ConvertedCurrentPrice").getDouble("Value");
 			return price;
 
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return 0.0;
 	}
 
 	private Double getBidIncrements(Double price) {
 		if (price <= .99) {
 			return price + .05;
 		} else if (price >= 1 && price <= 4.99) {
 			return price + .25;
 		} else if (price >= 5 && price <= 24.99) {
 			return price + .5;
 		} else if (price >= 25 && price <= 99.99) {
 			return price + 1;
 		} else if (price >= 100 && price <= 249.99) {
 			return price + 2.5;
 		} else {
 			return price + 5.0;
 		}
 	}
 }
