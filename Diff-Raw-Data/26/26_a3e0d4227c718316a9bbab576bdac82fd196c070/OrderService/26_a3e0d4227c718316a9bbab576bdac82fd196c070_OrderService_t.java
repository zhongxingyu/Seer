 package com.lenin.project.service;
 
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.stereotype.Service;
 
 import com.lenin.project.AuthComponent;
 import com.lenin.tradingplatform.client.BtceApi;
 import com.lenin.tradingplatform.client.RequestResponse;
 import com.lenin.tradingplatform.client.TradingClient;
 import com.lenin.tradingplatform.data.entities.Order;
 import com.lenin.tradingplatform.data.entities.Settings;
 import com.lenin.tradingplatform.data.entities.TradingSession;
 import com.lenin.tradingplatform.data.entities.User;
 import com.lenin.tradingplatform.data.repositories.OrderRepository;
 import com.lenin.tradingplatform.data.repositories.TradeRepository;
 import com.lenin.tradingplatform.data.repositories.TradingSessionRepository;
 import com.lenin.tradingplatform.data.repositories.UserRepository;
 
 @Service
 @Path("/order")
 public class OrderService {
 
 	@Autowired
 	private UserRepository userRepository;
 
 	@Autowired
 	private OrderRepository orderRepository;
 	
 	@Autowired
 	private TradingSessionRepository tradingSessionRepository;
 	
 	@Autowired
 	private TradeRepository tradeRepository;
 	
 	@Autowired
 	private MongoTemplate mongoTemplate;
 	
 	@Autowired
 	private AuthComponent authComponent;
 	
 	public OrderService() {
 	}
 
 
 	@GET
 	@Path("/")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public RequestResponse getOrders(@HeaderParam("Username") String username, @HeaderParam("Auth-Token") String authToken,
 			@HeaderParam("Trading-Session-Id") String tradingSessionId,
 			@QueryParam("type") String type) {
 
 		RequestResponse response = authComponent.getInitialResponse(username, authToken);
 		
 		if(response.getSuccess() < 0) {
 			return response;
 		}
 		
 		List<Order> orders = queryOrders(username, tradingSessionId, type);
 		response.setData(orders);
 		response.setSuccess(1);
 		
 		return response;
 		
 
 	}
 	
 	
 	private List<Order> queryOrders(String username, String tradingSessionId, String type) {
 
 		if(type != null) {
 			
 			User user = userRepository.findByUsername(username);
 			TradingSession tradingSession = tradingSessionRepository.findOne(tradingSessionId); // user.getCurrentTradingSession();
 			user.setCurrentTradingSession(tradingSession);
 			
 			List<Order> orders = orderRepository.findByTradingSessionAndType(tradingSession, type);
 			return orders;
 			
 		} else if(username != null) {
 			
 			User user = userRepository.findByUsername(username);
 			TradingSession tradingSession = tradingSessionRepository.findOne(tradingSessionId); // user.getCurrentTradingSession();
 			user.setCurrentTradingSession(tradingSession);
 			
 			List<Order> orders = orderRepository.findByTradingSession(tradingSession);
 			return orders;
 			
 		} else {
 			
 			List<Order> orders = orderRepository.findAll();
 			return orders;
 		}
 		
 
 	}
 	
 
 	@DELETE
 	@Path("/")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
 	public RequestResponse deleteOrder(@HeaderParam("Username") String username, @HeaderParam("Auth-Token") String authToken,
 			@HeaderParam("Trading-Session-Id") String tradingSessionId, Order order) {
 		
 		RequestResponse response = authComponent.getInitialResponse(username, authToken);
 		
 		if(response.getSuccess() < 0) {
 			return response;
 		}
 		
 		orderRepository.delete(order);
 		order = orderRepository.findOne(order.getId());
 
 		if(order == null) {
 			response.setSuccess(1);
 		}
 		
 		List<Order> orders = queryOrders(username, tradingSessionId, null);
 		response.setData(orders);
 		
 		return response;
 
 	}
 
 	
 	@POST
 	@Path("/")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
 	public RequestResponse postOrder(@HeaderParam("Username") String username, @HeaderParam("Auth-Token") String authToken,
 			@HeaderParam("Trading-Session-Id") String tradingSessionId,
 			Order order, @QueryParam("cancel") String cancel) {
 		
 		RequestResponse response = authComponent.getInitialResponse(username, authToken);
 		
 		if(response.getSuccess() < 0) {
 			return response;
 		}
 		
 		MongoOperations mongoOps = (MongoOperations)mongoTemplate;
 		List<Settings> settingsResult = mongoOps.findAll(Settings.class);
 		
 		Settings settings = settingsResult.get(0);
 		BtceApi btceApi = new BtceApi(settings.getBtceApiKey(), settings.getBtceApiSecret());
 		btceApi.setOrderFee(0.002);
 
 		User user = userRepository.findByUsername(username);
 		if (user == null) {
 			response.setMessage("Could not read user data.");
 			return response;
 		}
 
 		TradingSession tradingSession = tradingSessionRepository.findOne(tradingSessionId); // user.getCurrentTradingSession();
 		user.setCurrentTradingSession(tradingSession);
 
 		if (tradingSession.getRate().getLast() == 0.0) {
 			response.setMessage("Rate not set");
 			return response;
 		}
 
 		TradingClient tradingClient = new TradingClient(tradingSession,
 				tradingSessionRepository, orderRepository, tradeRepository);
 		
 		tradingClient.setBtceApi(btceApi);
 		
 		RequestResponse tradeResponse = null;
 		
 		if(cancel == null) {
 			tradeResponse = tradingClient.trade(order);
 		} else {
 			tradeResponse = tradingClient.cancelOrder(order);
 		}
 		
 		if(tradeResponse != null && tradeResponse.getSuccess() > 0) {
 			
 			List<Order> orders = queryOrders(username, tradingSessionId, null);
 			response.setData(orders);
 			response.setSuccess(1);
 			
		} else {
			
			response.setSuccess(tradeResponse.getSuccess());
			response.setMessage(tradeResponse.getMessage());
			
 		}
 
 		return response;
 		
 
 	}
 	
 
 }
