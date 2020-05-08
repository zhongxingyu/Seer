 package com.ssc;
 
 import java.io.IOException;
 import java.util.Enumeration;
 

import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 @SuppressWarnings("serial")
 public class SimpleSolarCalculatorServlet extends HttpServlet {
 	@SuppressWarnings("rawtypes")
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		
 		SolarPowerSystem sps = new SolarPowerSystem();
 		try {
		
 		sps.setAverageDailyHoursOfSunlight(Double.parseDouble(request.getParameter("averageDailyHoursOfSunlight")));
 		sps.setEfficiencyLossNorthRoof(Double.parseDouble(request.getParameter("efficiencyLossNorthRoof")));
 		sps.setEfficiencyLossWestRoof(Double.parseDouble(request.getParameter("efficiencyLossWestRoof")));		
 		sps.setPercentagesOnOrientations(Double.parseDouble(request.getParameter("percentageOnWestRoof")),
 										 Double.parseDouble(request.getParameter("percentageOnNorthRoof")));
 		sps.setPanelEfficiency(Double.parseDouble(request.getParameter("panelEfficiency")));
 		sps.setSystemSize(Double.parseDouble(request.getParameter("systemSize")));
 		sps.setInverterEfficiency(Double.parseDouble(request.getParameter("inverterEfficiency")));
 		sps.setDayTimeHourlyUsage(Double.parseDouble(request.getParameter("dayTimeHourlyUsage")));
        sps.setElectricityRate(Double.parseDouble(request.getParameter("electricityRate")));
         sps.setFeedInFee(Double.parseDouble(request.getParameter("feedInFee")));
         sps.setSystemCost(Double.parseDouble(request.getParameter("systemCost")));
 			
 			
 			//sps = new SolarPowerSystem();
 			//sps.setSystemSize(Double.parseDouble(request.getParameter("systemSize")));
 	        //sps.setPercentagesOnOrientations(0.381, 0.619);
 	        //sps.setEfficiencyLossNorthRoof(0.05);
 	        //sps.setEfficiencyLossWestRoof(0.15);
 	       //sps.setPanelEfficiency(1.0);			
 	        //sps.setInverterEfficiency(0.96);		
 	        //sps.setAverageDailyHoursOfSunlight(4.5);
 	        //sps.setDayTimeHourlyUsage(1.0);
 	        //sps.setElectricityRate(0.1941);
 	        //sps.setFeedInFee(0.50);
 	        //sps.setSystemCost(18000.0);
 	        
 		} catch (SolarPowerSystemException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		Enumeration test2 = request.getParameterNames();
 		
 		response.setContentType("text/plain");
 		response.getWriter().println("paramaters");
 		
 		
 		do {
 			
 			String temp = test2.nextElement().toString();					
 			response.getWriter().println(temp + " ==> " + request.getParameter(temp));			
 			
 		} while (test2.hasMoreElements());
 		
 		response.getWriter().println("Result: " + sps.toString());
 		/*
		String destination  ="/result.jsp?result=" + sps.toString();        
 		response.sendRedirect(response.encodeRedirectURL(destination));
		*/
		//~~~~~~~
		String show = sps.toString();
		request.setAttribute("show", show);
	    String url="/result.jsp"; //relative url for display jsp page
	    
	    

	    
	    try {
			getServletConfig().getServletContext().getRequestDispatcher(url).forward(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
 
 	}
 	
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		this.doGet(request, response);
 	}
 	
 	
 }
