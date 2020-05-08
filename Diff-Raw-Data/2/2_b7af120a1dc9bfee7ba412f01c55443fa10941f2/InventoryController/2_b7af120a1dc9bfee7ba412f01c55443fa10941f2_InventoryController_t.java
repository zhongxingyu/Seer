 package web.controllers;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import service.ProductManager;
 
 public class InventoryController implements Controller {
 
     protected final Log logger = LogFactory.getLog(getClass());
     private ProductManager productManager;
 
     public void setProductManager(ProductManager productManager) {
 		this.productManager = productManager;
 	}
     
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
  
     	Map<String, Object> myModel = new HashMap<String, Object>();
     	
         String now = (new Date()).toString();
         logger.info("Returning hello view with " + now);
 
         myModel.put("now", now);
         myModel.put("products", productManager.getProducts());
         
        return new ModelAndView("web/hello.jsp", "model", myModel);
     }
 
 }
