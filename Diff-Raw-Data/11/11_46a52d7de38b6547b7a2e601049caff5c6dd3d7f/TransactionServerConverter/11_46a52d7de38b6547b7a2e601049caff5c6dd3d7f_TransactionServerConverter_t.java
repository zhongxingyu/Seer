 package com.amee.engine.restlet;
 
 import com.amee.base.transaction.TransactionController;
 import com.amee.base.utils.ThreadBeanHolder;
 import com.amee.domain.IDataItemService;
 import com.amee.domain.ILocaleService;
 import com.amee.domain.IMetadataService;
import com.amee.domain.IProfileItemService;
 import com.amee.domain.profile.CO2CalculationService;
 import com.noelios.restlet.http.HttpRequest;
 import com.noelios.restlet.http.HttpResponse;
 import com.noelios.restlet.http.HttpServerCall;
 import com.noelios.restlet.http.HttpServerConverter;
 import org.restlet.Context;
 import org.springframework.context.ApplicationContext;
 
 public class TransactionServerConverter extends HttpServerConverter {
 
     private ApplicationContext springContext;
     private TransactionController transactionController;
 
     public TransactionServerConverter(Context context) {
         super(context);
         springContext = (ApplicationContext) context.getAttributes().get("springContext");
         transactionController = (TransactionController) context.getAttributes().get("transactionController");
     }
 
     public HttpRequest toRequest(HttpServerCall httpCall) {
         // Clear the ThreadBeanHolder at the start of each request.
         ThreadBeanHolder.clear();
         // Store commonly used services.
         ThreadBeanHolder.set(IDataItemService.class, (IDataItemService) springContext.getBean("dataItemService"));
        ThreadBeanHolder.set(IProfileItemService.class, (IProfileItemService) springContext.getBean("profileItemService"));
         ThreadBeanHolder.set(ILocaleService.class, (ILocaleService) springContext.getBean("localeService"));
         ThreadBeanHolder.set(IMetadataService.class, (IMetadataService) springContext.getBean("metadataService"));
         ThreadBeanHolder.set(CO2CalculationService.class, (CO2CalculationService) springContext.getBean("calculationService"));
         // Pass request through.
         return super.toRequest(httpCall);
     }
 
     public void commit(HttpResponse response) {
         // Commit the response.
         super.commit(response);
         // End transaction / entity manager.
         transactionController.afterCommit();
         // Clear the ThreadBeanHolder at the end of each request.
         ThreadBeanHolder.clear();
     }
 }
