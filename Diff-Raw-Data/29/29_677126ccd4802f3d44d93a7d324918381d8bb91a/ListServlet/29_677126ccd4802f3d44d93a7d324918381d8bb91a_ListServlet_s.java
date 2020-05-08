 package br.com.cafeteriaweb.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
 
 import br.com.cafeteriaweb.dao.CafeDao;
 
public class ListServlet extends HttpServlet {
 	
	private static final long serialVersionUID = 1L;

 	@Autowired
 	private CafeDao cafeDao;
	
    public ListServlet() {
        super();
        
    }
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		request.setAttribute("cafes", cafeDao.findAll() );
 		RequestDispatcher requestDispatcher = request.getRequestDispatcher("list.jsp");
 		requestDispatcher.forward(request, response);
 	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void setCafeDao(CafeDao cafeDao) {
		this.cafeDao = cafeDao;
	}
 }
