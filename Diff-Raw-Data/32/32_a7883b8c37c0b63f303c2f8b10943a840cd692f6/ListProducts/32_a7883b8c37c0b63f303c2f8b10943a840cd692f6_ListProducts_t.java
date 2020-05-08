 package domain;
 
 import display.FrontCommand;
 import data.ProductRowGateway;
 import data.ProductFinder;
 import data.CategoryFinder;
 import data.CategoryRowGateway;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 
 public class ListProducts extends FrontCommand {
 
   public void process() throws ServletException, IOException {
	  String cat_filter="";
	if(request.getParameter("cat")!=null)
		cat_filter = request.getParameter("cat");  
 	ProductFinder prods = (ProductFinder) context.getBean("productFinder");
 	CategoryFinder categories = (CategoryFinder) context.getBean("categoryFinder");
 	List<ProductRowGateway> data =null;
 	if(cat_filter==null||cat_filter==""){
    		data = prods.findAll();
 	}else{
 		data = prods.findFilter(cat_filter);
 	}
 	List<CategoryRowGateway> data_cats = categories.findAll();
     List param = new ArrayList();
 	List param_cats = new ArrayList();
     for (int i = 0; i < data.size(); i++) {
 		ProductRowGateway prod = data.get(i);
 		Map item = new HashMap();
 		item.put("idProduct",prod.getidProduct()+"");
 		item.put("idCategory",prod.getidCategory());
 		item.put("idBrand",prod.getidBrand());
 		item.put("idGenre",prod.getidGenre());
 		item.put("price",prod.getPrice());
 		item.put("description",prod.getDescription());
 		item.put("name",prod.getName());
 		param.add(item);
     }
 	for (int i = 0; i < data_cats.size(); i++) {
 		CategoryRowGateway cat = data_cats.get(i);
 		Map item = new HashMap();
 		item.put("idCategory",cat.getidCategory());
 		item.put("name",cat.getName());
 		param_cats.add(item);
     }
     request.setAttribute("products",param);
 	request.setAttribute("categories",param_cats);
 	request.setAttribute("current_cat",cat_filter);
     forward("/listProducts.jsp");
   }
 }
