 package project.server;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
 public class CalculatorServlet extends HttpServlet{
 	
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response){
 		String panel = request.getParameter("solarPanel");
 		int numPanels = Integer.parseInt(request.getParameter("numPanels"));
 		String inverter = request.getParameter("inverter");
 		double daytimeUsage = Double.parseDouble(request.getParameter("daytimeUsage"));
 		double efficiencyLoss = Double.parseDouble(request.getParameter("efficiencyLoss"));
 		String postcode = request.getParameter("postcode");
 		String suburb = request.getParameter("suburb");
 		String energyProvider = request.getParameter("energyProvider");
 		try {
 			response.sendRedirect("contact us.jsp");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
     public void doGet(HttpServletRequest request, HttpServletResponse response) {
         doPost(request, response);
     }
 }
