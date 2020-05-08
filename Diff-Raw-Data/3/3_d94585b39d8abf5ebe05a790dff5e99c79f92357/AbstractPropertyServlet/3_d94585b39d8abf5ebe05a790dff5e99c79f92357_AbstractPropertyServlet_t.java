 package ar.edu.itba.paw.grupo1.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ar.edu.itba.paw.grupo1.model.Property;
 
 @SuppressWarnings("serial")
 public class AbstractPropertyServlet extends BaseServlet {
 
 	public AbstractPropertyServlet() {
 		super();
 	}
 
 	protected Property getProperty(HttpServletRequest req,
 			HttpServletResponse resp) {
 				
 		boolean error = false;
 
 		error |= !checkIntegerParameter(req, "propertyType", 0, 1);
 		error |= !checkIntegerParameter(req, "operationType", 0, 1);
 		error |= !checkParameter(req, "neighbourhood", 0, 50);
 		error |= !checkDoubleParameter(req, "price", 0, Double.MAX_VALUE);
 		error |= !checkIntegerParameter(req, "rooms", 1, Integer.MAX_VALUE);
 		error |= !checkDoubleParameter(req, "indoorSpace", 0, Double.MAX_VALUE);
 		error |= !checkDoubleParameter(req, "outdoorSpace", 0, Double.MAX_VALUE);
 		error |= !checkParameter(req, "description", 0, 1000);
 
 		if (error) {
 			return null;
 		}
 	
 		int propertyType = Integer.parseInt(req.getParameter("propertyType"));
 		int operationType = Integer.parseInt(req.getParameter("operationType"));
 		String neighbourhood = req.getParameter("neighbourhood");
 		double price = Double.parseDouble(req.getParameter("price"));
 		int rooms = Integer.parseInt(req.getParameter("rooms"));
 		double indoorSpace = Double.parseDouble(req.getParameter("indoorSpace"));
 		double outdoorSpace = Double.parseDouble(req.getParameter("outdoorSpace"));
 		String description = req.getParameter("description");
 		boolean cable = req.getParameter("cable") != null;
 		boolean phone = req.getParameter("phone") != null;
 		boolean pool = req.getParameter("pool") != null;
 		boolean lounge = req.getParameter("lounge") != null;			
 		boolean paddle = req.getParameter("paddle") != null;
 		boolean barbecue = req.getParameter("barbecue") != null;
 		boolean sold = req.getParameter("sold") != null;
		
		int userId = getLoggedInUser(req).getId();
 			
 		Integer id = null;
 		if (!req.getParameter("id").equals("")) {
 			id = Integer.parseInt(req.getParameter("id"));
 		}
 		
 		return new Property(id, propertyType, operationType, neighbourhood, price, rooms, 
 					indoorSpace, outdoorSpace, description, cable, phone, pool, lounge, paddle, 
 					barbecue, sold, userId);
 	}
 }
