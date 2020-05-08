 package domain;
 
 import display.FrontCommand;
 import data.CategoryRowGateway;
 import data.CategoryFinder;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 
 public class ListCategories extends FrontCommand {
 	public void process() throws ServletException, IOException {
 		CategoryFinder categories = (CategoryFinder) context.getBean("categoryFinder");
 		List<CategoryRowGateway> data = categories.findAll();
 		List param = new ArrayList();
 		for (int i = 0; i < data.size(); i++) {
 			CategoryRowGateway cat = data.get(i);
 			Map item = new HashMap();
 			item.put("idCategory", cat.getidCategory()+" ");
 			item.put("name", cat.getName());
 			param.add(item);
     }
     request.setAttribute("categories",param);
    forward("/ListBrands.jsp");
   }
 }
