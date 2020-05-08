 package org.opencredo.batch.modules.processor;
 
 
 import org.opencredo.batch.modules.domain.Order;
 import org.opencredo.batch.modules.domain.PricingService;
 import org.slf4j.Logger;
 import org.springframework.batch.item.ItemProcessor;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class PricingProcessor implements ItemProcessor<Order, Order> {
 
     private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
 
     @Autowired
     private PricingService pricingService;
 
 	private String jobName;
 	private String stepName;
 
 	
 	public PricingProcessor() {
 	}
 	
     public Order process(Order unpriced) {
         logger.info("Job: {}, Step:{}, Pricing " + unpriced.toString(), jobName, stepName);
         if (pricingService == null) {
         	logger.error("Pricing service not autowired");
         	return unpriced; // TODO remove (should use service)
         }
         return pricingService.priceOrder(unpriced);
     }
 
     
     public void setJobName(String jobName) {
 		this.jobName = jobName;
 	}
     
     public void setStepName(String stepName) {
 		this.stepName = stepName;
 	}
    
    public void setPricingService(PricingService pricingService) {
		this.pricingService = pricingService;
	}
 }
