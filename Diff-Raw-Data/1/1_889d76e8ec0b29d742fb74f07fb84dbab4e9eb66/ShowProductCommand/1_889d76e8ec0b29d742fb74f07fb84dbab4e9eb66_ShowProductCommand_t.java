 package by.undead.web.command;
 
 import by.undead.dao.BaseDao;
 import by.undead.entity.Product;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Dzmitry
  * Date: 21.01.13
  * Time: 22:23
  * To change this template use File | Settings | File Templates.
  */
 public class ShowProductCommand implements Command {
 
     private Long id;
     private BaseDao productDao;
 
     public ShowProductCommand(Long id) {
         this.id = id;
         productDao = new BaseDao(Product.class);
     }
 
     @Override
     public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         String url = "WEB-INF/jsp/user/product.jsp";
         Product product = null;
         try {
             product = (Product) productDao.read(id);
         } catch (Exception ex) {
             request.setAttribute("MessageError", ex.getMessage());
         }
         RequestDispatcher dis = request.getRequestDispatcher(url);
         if (product == null) {
             request.setAttribute("MessageError", "product not found = " + id);
         } else {
             request.setAttribute("product", product);
         }
 
         dis.forward(request, response);
 
     }
 }
