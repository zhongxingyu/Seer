 package ar.edu.itba.paw.grupo1.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ar.edu.itba.paw.grupo1.model.Property;
 
 @SuppressWarnings("serial")
 public class AbstractPropertyServlet extends BaseServlet {
 
 	public AbstractPropertyServlet() {
 		super();
 	}
 
 	protected Property buildProperty(HttpServletRequest req,
 			HttpServletResponse resp) {
 				
 		boolean error = false;
 
 		error |= !checkIntegerParameter(req, "propertyType", 0, 1);
 		error |= !checkIntegerParameter(req, "operationType", 0, 1);
 		error |= !checkParameter(req, "address", 0, 50);
 		error |= !checkParameter(req, "neighbourhood", 0, 50);
 		error |= !checkDoubleParameter(req, "price", 0, Double.MAX_VALUE);
 		error |= !checkIntegerParameter(req, "rooms", 1, Integer.MAX_VALUE);
 		error |= !checkDoubleParameter(req, "indoorSpace", 0, Double.MAX_VALUE);
 		error |= !checkDoubleParameter(req, "outdoorSpace", 0, Double.MAX_VALUE);
 		error |= !checkParameter(req, "description", 0, 1000, true); //This is an optional field.
 		error |= !checkIntegerParameter(req, "antiquity", 0, 2000);
 
 		if (error) {
 			return null;
 		}
 	
 		int propertyType = Integer.parseInt(req.getParameter("propertyType"));
 		int operationType = Integer.parseInt(req.getParameter("operationType"));
 		String address = req.getParameter("address");
 		String neighbourhood = req.getParameter("neighbourhood");
 		double price = Double.parseDouble(req.getParameter("price"));
 		int rooms = Integer.parseInt(req.getParameter("rooms"));
 		double indoorSpace = Double.parseDouble(req.getParameter("indoorSpace"));
 		double outdoorSpace = Double.parseDouble(req.getParameter("outdoorSpace"));
 		String description = req.getParameter("description");
 		int antiquity = Integer.parseInt(req.getParameter("antiquity"));
 		boolean cable = req.getParameter("cable") != null;
 		boolean phone = req.getParameter("phone") != null;
 		boolean pool = req.getParameter("pool") != null;
 		boolean lounge = req.getParameter("lounge") != null;			
 		boolean paddle = req.getParameter("paddle") != null;
 		boolean barbecue = req.getParameter("barbecue") != null;
		boolean published = true; // default value
 		
 		int userId = getLoggedInUser(req).getId();
 			
 		Integer id = null;
 		if (!req.getParameter("id").equals("")) {
 			id = Integer.parseInt(req.getParameter("id"));
 		}
 		
 		return new Property(id, propertyType, operationType, address, neighbourhood, price, rooms, 
 					indoorSpace, outdoorSpace, description, antiquity, cable, phone, pool, lounge, paddle, 
 					barbecue, published, userId);
 	}
 	
 	
 	protected void setPropertyAttributes(HttpServletRequest req, Property property) {
 		
 		req.setAttribute("id", property.getId());
 		req.setAttribute("propertyType", property.getPropertyType());
 		req.setAttribute("operationType", property.getOperationType());
 		req.setAttribute("address", property.getAddress());
 		req.setAttribute("neighbourhood", property.getNeighbourhood());
 		req.setAttribute("price", property.getPrice());
 		req.setAttribute("rooms", property.getRooms());
 		req.setAttribute("indoorSpace", property.getIndoorSpace());
 		req.setAttribute("outdoorSpace", property.getOutdoorSpace());
 		req.setAttribute("description", property.getDescription());
 		req.setAttribute("antiquity", property.getAntiquity());
 		req.setAttribute("cable", property.isCable());
 		req.setAttribute("phone", property.isPhone());
 		req.setAttribute("pool", property.isPool());
 		req.setAttribute("lounge", property.isLounge());
 		req.setAttribute("paddle", property.isPaddle());
 		req.setAttribute("barbecue", property.isBarbecue());
 	}
 	
 	
 	protected void setPropertyAttributes(HttpServletRequest req) {
 		
 		req.setAttribute("id", req.getParameter("id"));
 		req.setAttribute("propertyType", req.getParameter("propertyType"));
 		req.setAttribute("operationType", req.getParameter("operationType"));
 		req.setAttribute("address", req.getParameter("address"));
 		req.setAttribute("neighbourhood", req.getParameter("neighbourhood"));
 		req.setAttribute("price", req.getParameter("price"));
 		req.setAttribute("rooms", req.getParameter("rooms"));
 		req.setAttribute("indoorSpace", req.getParameter("indoorSpace"));
 		req.setAttribute("outdoorSpace", req.getParameter("outdoorSpace"));
 		req.setAttribute("description", req.getParameter("description"));
 		req.setAttribute("antiquity", req.getParameter("antiquity"));
 		req.setAttribute("cable", req.getParameter("cable"));
 		req.setAttribute("phone", req.getParameter("phone"));
 		req.setAttribute("pool", req.getParameter("pool"));
 		req.setAttribute("lounge", req.getParameter("lounge"));
 		req.setAttribute("paddle", req.getParameter("paddle"));
 		req.setAttribute("barbecue", req.getParameter("barbecue"));
 	}
 }
