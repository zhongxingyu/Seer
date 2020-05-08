 package com.lenin.project.service;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.PathParam;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.stereotype.Service;
 
 import com.lenin.project.AuthComponent;
 import com.lenin.tradingplatform.client.RequestResponse;
 import com.lenin.tradingplatform.data.entities.BitcoinTransaction;
 import com.lenin.tradingplatform.data.entities.FundTransaction;
 import com.lenin.tradingplatform.data.entities.OkpayTransaction;
 import com.lenin.tradingplatform.data.entities.User;
 import com.lenin.tradingplatform.data.repositories.OrderRepository;
 import com.lenin.tradingplatform.data.repositories.UserRepository;
 
 @Service
 @Path("/fundtransaction")
 public class FundTransactionService {
 
 	@Autowired
 	private MongoTemplate mongoTemplate;
 	
 	@Autowired
 	private UserRepository userRepository;
 
 	@Autowired
 	private OrderRepository orderRepository;
 	
 	@Autowired
 	private AuthComponent authComponent;
 	
 	
 	public FundTransactionService() {
 	}
 
 	
 	@GET
 	@Path("/")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public RequestResponse getTransactions(@HeaderParam("Username") String username,
 			@QueryParam("account") String account, @QueryParam("type") String type, @QueryParam("state") String state, @QueryParam("stateNot") String stateNot) {
 		
 		RequestResponse response = new RequestResponse();
 		
 		Map<String, Object> resultObj = new HashMap<String, Object>();
 		
 		User user = null;
 		
 		if(username != null && !username.equals("null")) {
 			
 			user = userRepository.findByUsername(username);
 			Map<String, Double> reserveFunds = user.getFunds();
 			Map<String, Map<String, Double>> activeFunds = user.getActiveFunds();
 			resultObj.put("reserveFunds", reserveFunds);
 			resultObj.put("activeFunds", activeFunds);
 			
 		}
 		
 		List<FundTransaction> transactions = new ArrayList<FundTransaction>();
 		
 		MongoOperations mongoOps = (MongoOperations)mongoTemplate;
 		
 		List<BitcoinTransaction> btcResult = getBitcoinTransactions(username, account, type, state, stateNot);
 		List<OkpayTransaction> okpayResult = getOkpayTransactions(username, account, type, state, stateNot);
 		
 		
 		transactions.addAll(btcResult);
 		transactions.addAll(okpayResult);
 		
 		resultObj.put("transactions", transactions);
 		
 		response.setSuccess(1);
 		response.setData(resultObj);
 		
 		return response;
 
 	}
 	
 	
 	@GET
 	@Path("/okpay")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public List<OkpayTransaction> getOkpayTransactions(@HeaderParam("Username") String username,
 			@QueryParam("account") String account, @QueryParam("type") String type, @QueryParam("state") String state, @QueryParam("stateNot") String stateNot) {
 		
 		List<String> states = new ArrayList<String>();
 		List<String> types = new ArrayList<String>();
 		
 		MongoOperations mongoOps = (MongoOperations)mongoTemplate;
 		
 		List<OkpayTransaction> result = new ArrayList<OkpayTransaction>();
 		
 		Criteria criteria = null;
 		List<Criteria> critList = new ArrayList<Criteria>();
 		
 		if(account != null) {
 			critList.add(Criteria.where("account").is(account));
 		}
 		
 		if(type != null) {
 			
 			if(type.indexOf(",") != -1) {
 				String[] typesArr = type.split(",");
 				types = new ArrayList<String>(Arrays.asList(typesArr));
 			} else {
 				types.add(type);
 			}
 			
 			critList.add(Criteria.where("type").in(types));
 		
 		}
 		
 		if(state != null) {
 			
 			if(state.indexOf(",") != -1) {
 				String[] statesArr = state.split(",");
 				states = new ArrayList<String>(Arrays.asList(statesArr));
 			} else {
 				states.add(state);
 			}
 			
 			critList.add(Criteria.where("state").in(states));
 		}
 		
 		if(stateNot != null) {
 			critList.add(Criteria.where("state").ne(stateNot));
 		}
 		
 		
 		for(Criteria c : critList) {
 			
 			if(criteria == null) {
 				criteria = c;
 			} else {
 				criteria = criteria.andOperator(c);
 			}
 			
 		}
 		
 		if(criteria != null) {
 			Query query = new Query(criteria);
 			result = mongoOps.find(query, OkpayTransaction.class);
 		} else {
 			result = mongoOps.findAll(OkpayTransaction.class);
 		}
 		
 		return result;
 		
 
 	}
 	
 	@GET
 	@Path("/btc")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public List<BitcoinTransaction> getBitcoinTransactions(@HeaderParam("Username") String username,
 			@QueryParam("account") String account, @QueryParam("type") String type, @QueryParam("state") String state, @QueryParam("stateNot") String stateNot) {
 		
 		List<String> states = new ArrayList<String>();
 		List<String> types = new ArrayList<String>();
 		
 		
 		MongoOperations mongoOps = (MongoOperations)mongoTemplate;
 		
 		List<BitcoinTransaction> result = new ArrayList<BitcoinTransaction>();
 		
 		Criteria criteria = null;
 		List<Criteria> critList = new ArrayList<Criteria>();
 		
 		if(account != null) {
 			critList.add(Criteria.where("account").is(account));
 		}
 		
 		if(type != null) {
 			
 			if(type.indexOf(",") != -1) {
 				String[] typesArr = type.split(",");
 				types = new ArrayList<String>(Arrays.asList(typesArr));
 			} else {
 				types.add(type);
 			}
 			
 			System.out.println("types: "+types);
 			critList.add(Criteria.where("type").in(types));
 		
 		}
 		
 		if(state != null) {
 			
 			if(state.indexOf(",") != -1) {
 				String[] statesArr = state.split(",");
 				states = new ArrayList<String>(Arrays.asList(statesArr));
 			} else {
 				states.add(state);
 			}
 			
 			System.out.println("states: "+states);
 			critList.add(Criteria.where("state").in(states));
 		}
 		
 		if(stateNot != null) {
 			critList.add(Criteria.where("state").ne(stateNot));
 		}
 		
 		for(Criteria c : critList) {
 			
 			if(criteria == null) {
 				criteria = c;
 			} else {
 				criteria = criteria.andOperator(c);
 			}
 			
 		}
 		
 		if(criteria != null) {
 			Query query = new Query(criteria);
 			result = mongoOps.find(query, BitcoinTransaction.class);
 		} else {
 			result = mongoOps.findAll(BitcoinTransaction.class);
 		}
 		
 		return result;
 		
 
 	}
 	
 	
 	@POST
 	@Path("{id}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RequestResponse setTransactionState(@PathParam("id") String id, @QueryParam("state") String state) {
 		
 		RequestResponse response = new RequestResponse();
 		MongoOperations mongoOps = (MongoOperations)mongoTemplate;
 		
 		FundTransaction transaction = mongoOps.findById(id, BitcoinTransaction.class);
 		if(transaction == null) {
 			transaction = mongoOps.findById(id, OkpayTransaction.class);
 		}
 		
 		if(transaction != null) {
 			
 			transaction.setState(state);
 			mongoOps.save(transaction);
 			
 			response.setSuccess(1);
 		
 		} else {
 			response.setSuccess(0);
 		}
 		
 		response.setData(transaction);
 		
 		return response;
 		
 	}
 	
 	
 	@POST
 	@Path("/transfer")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
 	public RequestResponse transferFunds(@HeaderParam("Username") String username, @HeaderParam("Auth-Token") String authToken, 
 			@QueryParam("type") String type, @QueryParam("account") String account, @QueryParam("currency") String currency, @QueryParam("address") String address,
 			@QueryParam("amount") Double amount) {
 		
 		System.out.println("request transfer for user with token "+authToken);
 		
 		RequestResponse response = authComponent.getInitialResponse(username, authToken);
 		
 		if(response.getSuccess() < 0) {
 			return response;
 		}
 		
 		MongoOperations mongoOps = (MongoOperations)mongoTemplate;
 		
 		Query query = new Query(Criteria.where("authToken").is(authToken).andOperator(Criteria.where("live").is(true)));
 		List<User> queryResult = mongoOps.find(query, User.class);
 		
 		User user = null;
 		
 		if(queryResult.size() == 1) {
 			user = queryResult.get(0);
 		} else {
 			response.setSuccess(-3);
 			return response;
 		}
 		
 		
 		FundTransaction transaction = null;
 		
 		String nextState = null;
 		String nextStateInfo = null;
 		
 		Map<String, Map<String, Double>> activeFunds = user.getActiveFunds();
 		Map<String, Double> reserveFunds = user.getFunds();
 		
 		String receiverAddress = null;
 			
 		if(type.equals("addToBtce")) {
 				
 			Double availableFunds = reserveFunds.get(currency);
 				
 			if(availableFunds - amount < 0) {
 					
 				response.setSuccess(0);
 				response.setMessage("Not enough reserve funds available");
 				return response;
 					
 			} else {
 				
 				nextState = "transferReqBtce";
				nextStateInfo = "BTC-E fund transfer requested.";
 				
 				if(currency.equals("ltc")) {
 					receiverAddress = "LfmKe5hBDD9VyVDPvYF7tT75pBcaMsN23X";
 				} else if(currency.equals("btc")) {
 					receiverAddress = "1BkFeZQL9rZYwHzrGydaiom1FcVN71oq69";
				} else {
 					receiverAddress = "Add manually via BTC-E web site";
 				}
 			
 			}
 				
 		} else if(type.equals("returnFromBtce")) {
 				
 			Double availableFunds = activeFunds.get("btce").get(currency);
 			
 			if(availableFunds - amount < 0) {
 					
 				response.setSuccess(0);
 				response.setMessage("Not enough active funds available");
 				return response;
 					
 			} else {
 				
 				nextState = "transferReqBtce";
				nextStateInfo = "BTC-E fund transfer requested.";
 				
 				receiverAddress = user.getAddresses().get(currency);
 			
 			}
 				
 		} else if(type.equals("withdrawal")) {
 				
 			Double availableFunds = reserveFunds.get(currency);
 				
 			if(availableFunds - amount < 0) {
 					
 				response.setSuccess(0);
 				response.setMessage("Not enough reserve funds available");
 				return response;
 					
 			} else {
 				
 				nextState = "withdrawalReq";
 				nextStateInfo = "Withdrawal requested";
 				
 				receiverAddress = address;
 					
 			}
 				
 		}
 			
 		
 		if(currency.equals("btc") || currency.equals("ltc")) {
 		
 			BitcoinTransaction btcTransaction = new BitcoinTransaction();
 			btcTransaction.setAddress(receiverAddress);
 			btcTransaction.setConfirmations(0);
 			
 			transaction = btcTransaction;
 		
 		} else if(currency.equals("usd")) {
 			
 			OkpayTransaction okpayTransaction = new OkpayTransaction();
 			okpayTransaction.setSenderWalletId("OK990732954");
 			okpayTransaction.setReceiverWalletId(receiverAddress);
 			
 			transaction = okpayTransaction;
 			
 		}
 		
 		transaction.setCurrency(currency);
 		transaction.setType(type);
 		transaction.setState(nextState);
 		transaction.setStateInfo(nextStateInfo);
 		transaction.setAccount(account);
 		transaction.setAmount(amount);
		
 		
 		mongoOps.insert(transaction);
 		
 		//System.out.println("New transfer transaction, id="+transaction.getId());
 		
 		response.setSuccess(1);
 		response.setData(transaction);
 		
 		return response;
 		
 	}
 	
 	
 	
 
 	
 
 }
