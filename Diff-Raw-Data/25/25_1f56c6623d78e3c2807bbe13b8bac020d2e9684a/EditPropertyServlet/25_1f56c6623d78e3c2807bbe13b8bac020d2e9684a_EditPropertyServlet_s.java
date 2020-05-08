 package ar.edu.itba.paw.grupo1.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ar.edu.itba.paw.grupo1.ApplicationContainer;
 import ar.edu.itba.paw.grupo1.model.Property;
 import ar.edu.itba.paw.grupo1.service.PropertyService;
 
 @SuppressWarnings("serial")
 public class EditPropertyServlet extends AbstractPropertyServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		Property property = null;
 		if (req.getParameter("id") != null) {
			ApplicationContainer.getInstance();
 			PropertyService propertyService = ApplicationContainer.get(PropertyService.class);
 
 			property = propertyService.getById(Integer.parseInt(req.getParameter("id")));
			req.setAttribute("id", req.getParameter("id"));
 			req.setAttribute("property", property);
 		}
 		render(req, resp, "editProperty.jsp", "Edit Property");
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
		ApplicationContainer.getInstance();
 		PropertyService propertyService = ApplicationContainer.get(PropertyService.class);
 		Property property = getProperty(req, resp);
 
 		propertyService.save(property);
		req.getRequestDispatcher("ListPropertiesServlet").forward(req, resp);
 
 	}
 
 }
