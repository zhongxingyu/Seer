 package com.pjcode.service;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.math.RandomUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import com.pjcode.domain.Order;
 import com.pjcode.domain.OrderStatus;
 import com.pjcode.domain.Product;
 import com.pjcode.repository.ProductRepository;
 
 @Service
 public class OrderGenerator {
 	
 	protected static Logger logger = Logger.getLogger("service");
 	
 	@Autowired
 	private ProductRepository productRepository;
 	
 	@Autowired 
 	private OrderService orderService;
 	
 	/* 
 	 * TODO: use annotation to configure the value from spring.properties
 	 *       and remove reference to the bean in applicationContext.xml
 	 */
 	private boolean generateOrders;
 	
 	public void setGenerateOrders(boolean generateOrders) {
 		this.generateOrders = generateOrders;
 	}
 
 	/**
 	 * Periodically generate order for existing product
 	 */
 	@Scheduled(fixedDelay=10000)
 	public void generateRandomOrders() {
 		logger.debug("generateOrders = " + generateOrders);
 		if (generateOrders) {
 			try {
 				
 				// wait randomly between 1 and 300 seconds
				int waitTime = RandomUtils.nextInt(300);
 				logger.debug("waiting for " + waitTime + " seconds");		
 				Thread.sleep(waitTime*1000);
 				
 				// ... retrieve all products
 				List<Product> allProducts = productRepository.findAll();
 				if (allProducts != null && allProducts.size() > 0) {
 					
 					// ... pick one product from the result
 					int rndOrdinal = RandomUtils.nextInt(allProducts.size());
 					Product product = allProducts.get(rndOrdinal);
 					
 					// ... generate random quantity
 					long rndQuantity = RandomUtils.nextInt(100);
 					
 					// ... and generate new order
 					Order newOrder = new Order();
 					newOrder.setPrice(product.getPrice());
 					newOrder.setProduct(product);
 					newOrder.setQuantity(rndQuantity);
 					newOrder.setRecievedDate(new Date());
 					newOrder.setStatus(OrderStatus.NEW.getCode());
 					newOrder.setComment("");
 					
 					orderService.create(newOrder);
 					logger.debug("Generated order: " + newOrder);
 					
 				} else {
 					logger.debug("Cannot generate order: There is no available product");
 				}
 				
 				
 			} catch (Exception ex) {
 				logger.error("Exception occured while generating order: ", ex);
 			}
 		}
 	}
 
 }
