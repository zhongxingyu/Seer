 package com.github.syqnew.services;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.github.syqnew.dao.ClientDao;
 import com.github.syqnew.dao.MarketOrderDao;
 import com.github.syqnew.dao.MetadataDao;
 import com.github.syqnew.dao.SaleDao;
 import com.github.syqnew.dao.impl.ClientDaoImpl;
 import com.github.syqnew.dao.impl.MarketOrderDaoImpl;
 import com.github.syqnew.dao.impl.MetadataDaoImpl;
 import com.github.syqnew.dao.impl.SaleDaoImpl;
 import com.github.syqnew.domain.Client;
 import com.github.syqnew.domain.MarketOrder;
 import com.github.syqnew.domain.Metadata;
 import com.github.syqnew.domain.Sale;
 
 public class OrderServices {
 
 	MarketOrderDao orderDao;
 	ClientDao clientDao;
 	SaleDao saleDao;
 	MetadataDao metadataDao;
 
 	public OrderServices() {
 		orderDao = new MarketOrderDaoImpl();
 		clientDao = new ClientDaoImpl();
 		saleDao = new SaleDaoImpl();
 		metadataDao = new MetadataDaoImpl();
 	}
 
 	public void getMyOrders(String clientId, HttpServletRequest request,
 			HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException {
 		ObjectMapper mapper = new ObjectMapper();
 		List<String> list = new ArrayList<String>();
 		int clientID = Integer.parseInt(clientId);
 		List<MarketOrder> marketOrders = orderDao.getByClient(clientID);
 		for (MarketOrder order : marketOrders) {
 			list.add(mapper.writeValueAsString(order));
 		}
 		response.getWriter().println(list);
 	}
 
 	public void submitOrder(HttpServletRequest request)
 			throws JsonParseException, JsonMappingException, IOException {
 		ObjectMapper mapper = new ObjectMapper();
 		MarketOrder order = mapper.readValue(request.getReader(), MarketOrder.class);
 		orderDao.persist(order);
 
 		// handle most recent order
 		int orderType = order.getOrderType();
 		switch (orderType) {
 		
 		// Market Buy
 		case 1:
 			List<MarketOrder> askOrders = orderDao.getLimitSells();
 			List<MarketOrder> buyOrders = orderDao.getMarketBuys();
 			matchMarketOrders(askOrders, buyOrders, false);
 			break;
 			
 		// Market Sell
 		case 2:
 			List<MarketOrder> bidOrders = orderDao.getLimitBuys();
 			List<MarketOrder> sellOrders = orderDao.getMarketSells();
 			matchMarketOrders(bidOrders, sellOrders, true);
 			break;
 			
 		// Limit Buy
 		case 3:
 			List<MarketOrder> bidOrders2 = orderDao.getLimitBuys();
 			if (bidOrders2.size() > 0) {
 				MarketOrder bestBid = bidOrders2.get(0);
 				Metadata update = metadataDao.findAll().get(0);
 				update.setBid(bestBid.getPrice());
 				update.setBidSize(bestBid.getUnfulfilled());
 				metadataDao.merge(update);
 				
 				List<MarketOrder> sellOrders2 = orderDao.getMarketSells();
 				matchMarketOrders(bidOrders2, sellOrders2, true);
 				
 				List<MarketOrder> askOrders2 = orderDao.getLimitSells();
 				matchLimitOrders(bidOrders2, askOrders2, false);
 			}
 			break;
 			
 		// Limit Sell
 		case 4:
 			List<MarketOrder> askOrders3 = orderDao.getLimitSells();
 			if (askOrders3.size() > 0) {
 				MarketOrder bestAsk = askOrders3.get(0);
 				Metadata update = metadataDao.findAll().get(0);
 				update.setAsk(bestAsk.getPrice());
 				update.setAskSize(bestAsk.getUnfulfilled());
 				metadataDao.merge(update);
 				
 				List<MarketOrder> buyOrders2 = orderDao.getMarketBuys();
 				matchMarketOrders(askOrders3, buyOrders2, false);
 				
 				List<MarketOrder> bidOrders3 = orderDao.getLimitBuys();
 				matchLimitOrders(bidOrders3, askOrders3, true);
 			}
 			break;
 		}
 	}
 
 	protected void matchMarketOrders(List<MarketOrder> priceOrders, List<MarketOrder> marketOrders,
 			boolean sellAtMarketPrice) {
 		// Cannot handle order if one of the lists is empty
 		if (priceOrders.size() == 0 || marketOrders.size() == 0)
 			return;
 
 		// First, get the orders that would be matched.
 		MarketOrder bestAtPrice = priceOrders.get(0);
 		MarketOrder firstAtMarket = marketOrders.get(0);
 
 		// Update the metadata
 		Metadata metadata = metadataDao.findAll().get(0);
 		
 		int price = bestAtPrice.getPrice();
 		metadata.setLast(price);
 
 		int amount = firstAtMarket.getUnfulfilled();
 		int amountAtPrice = bestAtPrice.getUnfulfilled();
		if (amount >= amountAtPrice) {
 			amount = amountAtPrice;
 			if (priceOrders.size()>1) {
 				MarketOrder nextOrder = priceOrders.get(1);
 				if (sellAtMarketPrice) {
 					metadata.setBid(nextOrder.getPrice());
 					metadata.setBidSize(nextOrder.getUnfulfilled());
 				} else {
 					metadata.setAsk(nextOrder.getPrice());
 					metadata.setAskSize(nextOrder.getUnfulfilled());
 				}
 			} else {
 				if (sellAtMarketPrice) {
 					metadata.setBid(-1);
 					metadata.setBidSize(0);
 				} else {
 					metadata.setAsk(-1);
 					metadata.setAskSize(0);
 				}
 			}
 		}
 
 		long currentTime = new Date().getTime();
 
 		// fulfill the orders
 		bestAtPrice.fulfillOrder(amount);
 		firstAtMarket.fulfillOrder(amount);
 		orderDao.merge(bestAtPrice);
 		orderDao.merge(firstAtMarket);
 
 		// update client assets
 		int limitClientId = bestAtPrice.getClient();
 		int marketClientId = firstAtMarket.getClient();
 		Client limitClient = clientDao.findById(limitClientId);
 		Client marketClient = clientDao.findById(marketClientId);
 		if (sellAtMarketPrice) {
 			limitClient.buyShares(amount, price * amount);
 			marketClient.sellShares(amount, price * amount);
 		} else {
 			limitClient.sellShares(amount, price * amount);
 			marketClient.buyShares(amount, price * amount);
 		}
 		clientDao.merge(limitClient);
 		clientDao.merge(marketClient);
 
 		// record the sale
 		Sale sale;
 		if (sellAtMarketPrice) {
 			sale = new Sale(limitClientId, marketClientId, amount, price, currentTime, bestAtPrice.getId(),
 					firstAtMarket.getId());
 		} else {
 			sale = new Sale(marketClientId, limitClientId, amount, price, currentTime, firstAtMarket.getId(),
 					bestAtPrice.getId());
 		}
 		saleDao.persist(sale);
 
 		// update the metadata
 		metadata.addToVolume(amount);
 		if (metadata.getHigh() < price) {
 			metadata.setHigh(price);
 		}
 		if (metadata.getLow() > price) {
 			metadata.setLow(price);
 		}
 		metadataDao.merge(metadata);
 
 		// TODO create a quote
 	}
 
 	protected void matchLimitOrders(List<MarketOrder> bidOrders, List<MarketOrder> askOrders, boolean sellInitiated) {
 
 		// make sure that there are both ask and bid orders 
 		if (bidOrders.size() == 0 || askOrders.size() == 0) {
 			return;
 		}
 
 		MarketOrder bestBid = bidOrders.get(0);
 		MarketOrder bestAsk = askOrders.get(0);
 
 		int bidPrice = bestBid.getPrice();
 		int askPrice = bestAsk.getPrice();
 
 		if (bidPrice != askPrice)
 			return;
 
 		int price;
 		if (sellInitiated) {
 			price = bidPrice;
 		} else {
 			price = askPrice;
 		}
 
 		int amount = bestBid.getUnfulfilled();
 		int amountAsk = bestAsk.getUnfulfilled();
 		if (amount > amountAsk)
 			amount = amountAsk;
 
 		long currentTime = new Date().getTime();
 
 		// fulfill the orders
 		bestBid.fulfillOrder(amount);
 		bestAsk.fulfillOrder(amount);
 		orderDao.merge(bestBid);
 		orderDao.merge(bestAsk);
 
 		// update client assets
 		int bidClientId = bestBid.getClient();
 		int askClientId = bestAsk.getClient();
 		Client bidClient = clientDao.findById(bidClientId);
 		Client askClient = clientDao.findById(askClientId);
 		bidClient.buyShares(amount, price * amount);
 		askClient.sellShares(amount, price * amount);
 		clientDao.merge(bidClient);
 		clientDao.merge(askClient);
 
 		// record sale
 		Sale sale = new Sale(bidClientId, askClientId, amount, price, currentTime, bestBid.getId(), bestAsk.getId());
 		saleDao.persist(sale);
 
 		// update metadata
 		Metadata metadata = metadataDao.findAll().get(0);
 		metadata.addToVolume(amount);
 		if (metadata.getHigh() < price) {
 			metadata.setHigh(price);
 		}
 		if (metadata.getLow() > price) {
 			metadata.setLow(price);
 		}
 		metadata.setLast(price);
 		metadataDao.merge(metadata);
 
 		// TODO create quote
 	}
 }
