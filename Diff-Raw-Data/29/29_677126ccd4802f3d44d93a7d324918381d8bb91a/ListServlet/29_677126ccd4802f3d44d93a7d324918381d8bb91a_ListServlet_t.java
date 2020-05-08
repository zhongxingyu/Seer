 package br.com.cafeteriaweb.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.HttpRequestHandler;
 
 import br.com.cafeteriaweb.dao.CafeDao;
 
public class ListServlet implements HttpRequestHandler {
 	
 	@Autowired
 	private CafeDao cafeDao;
 
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		request.setAttribute("cafes", cafeDao.findAll() );
 		RequestDispatcher requestDispatcher = request.getRequestDispatcher("list.jsp");
 		requestDispatcher.forward(request, response);
 	}
 }
