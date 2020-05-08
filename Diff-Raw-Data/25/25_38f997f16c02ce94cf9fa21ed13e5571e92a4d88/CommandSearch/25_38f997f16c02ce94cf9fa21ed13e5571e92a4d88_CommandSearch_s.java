 package controller;
 
 import exceptions.ServiceLocatorException;
 import group.Item;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jdbc.DBConnectionFactory;
 
 public class CommandSearch implements Command {
 	
 	private static final String page = "/Search.jsp";
 
 	public CommandSearch() {
 		super();
 	}
 
 	@Override
 	public String execute(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException, SQLException {
 		String searchItem = request.getParameter("item");
 		String category = request.getParameter("category");
 		System.out.println("search: "+searchItem);
 		System.out.println("category: "+category);
 		List<Item> result = new ArrayList<Item>();
 		Connection conn = null;
 		try {
 			conn = DBConnectionFactory.getConnection();
			result = Item.search(conn, searchItem);
 			for (Item i : result) {
 				System.out.println(i.getTitle());
 			}
 			request.setAttribute("result", result);
 		} catch (ServiceLocatorException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (conn != null)
 				conn.close();
 		}
 		return page;
 	}
 
 
 }
