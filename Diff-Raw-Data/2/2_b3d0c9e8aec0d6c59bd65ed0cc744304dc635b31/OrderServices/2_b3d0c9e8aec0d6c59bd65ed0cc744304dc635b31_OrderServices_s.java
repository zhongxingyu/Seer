 package com.github.syqnew.services;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.github.syqnew.dao.ClientDao;
 import com.github.syqnew.dao.MarketOrderDao;
 import com.github.syqnew.dao.impl.ClientDaoImpl;
 import com.github.syqnew.dao.impl.MarketOrderDaoImpl;
 import com.github.syqnew.domain.MarketOrder;
 
 public class OrderServices {
 	
 	MarketOrderDao dao;
 	ClientDao clientDao;
 
 	public OrderServices(){
 		dao = new MarketOrderDaoImpl();
 		clientDao = new ClientDaoImpl();
 	}
 
 	public void getMyOrders(String clientId, HttpServletRequest request,
 			HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException {
 		ObjectMapper mapper = new ObjectMapper();
 		List<String> list = new ArrayList<String>();
 		int clientID = Integer.parseInt(clientId);
 		List<MarketOrder> marketOrders = dao.getByClient(clientID);
 		for (MarketOrder order : marketOrders ) {
 			list.add(mapper.writeValueAsString(order));
 		}
 		response.getWriter().println(list);
 	}
 	
 	public void submitOrder(HttpServletRequest request)
 			throws JsonParseException, JsonMappingException, IOException {
 		ObjectMapper mapper = new ObjectMapper();
 		MarketOrder order = mapper.readValue(request.getReader(), MarketOrder.class);
		dao.merge(order);
 	}
 
 }
