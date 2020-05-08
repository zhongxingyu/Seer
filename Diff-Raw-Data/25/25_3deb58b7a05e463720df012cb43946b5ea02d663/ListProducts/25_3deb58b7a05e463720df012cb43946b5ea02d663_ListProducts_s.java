 package domain;
 
 import display.FrontCommand;
 import data.ProductRowGateway;
 import data.ProductFinder;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 
 public class ListProducts extends FrontCommand {
 
   public void process() throws ServletException, IOException {
	/*ProductFinder prods = (ProductFinder) context.getBean("productFinder");
    List<ProductRowGateway> data = prods.findAll();
     List param = new ArrayList();
    for (int i=0;i<data.size();i++) {
       ProductRowGateway prod = data.get(i);
       Map item = new HashMap();
       //item.put("id",prod.getX());
 	  //etc
       param.add(item);
     }*/
     request.setAttribute("products",param);
     forward("/listProducts.jsp");
   }
 }
