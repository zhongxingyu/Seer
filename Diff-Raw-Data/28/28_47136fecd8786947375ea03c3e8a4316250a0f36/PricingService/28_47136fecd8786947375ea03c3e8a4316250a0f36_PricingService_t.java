 package org.opencredo.batch.modules.domain;
 
 
 import java.math.BigDecimal;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.persistence.OptimisticLockException;
 
 import org.slf4j.Logger;
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedOperation;
 import org.springframework.jmx.export.annotation.ManagedResource;
 
 @ManagedResource
 public class PricingService {
 
     AtomicInteger pricedCount = new AtomicInteger(0);
 
     private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
 
     private Random rand = new Random();
 
 	private AtomicBoolean throwOnNext = new AtomicBoolean();
 
 	
     public Order priceOrder(Order unpriced) {
         unpriced.setPrice(new BigDecimal(rand.nextDouble()));
         unpriced.setStatus(OrderStatus.PRICED);
         logger.info(unpriced.toString() + " count is " + pricedCount.incrementAndGet());
         try {
         	if (throwOnNext.get()) {
         		throw new OptimisticLockException("Data was modified by someone else");
         	}
			Thread.sleep(200);
 		} catch (InterruptedException e) {
 			// do nothing
 		}
         return unpriced;
     }
 
     @ManagedAttribute
     public Integer getCount() {
     	return pricedCount.get();
     }
     
     @ManagedOperation
     public void throwCollisionExceptionOnNextProcess() {
     	throwOnNext.set(true);
     }
 }
