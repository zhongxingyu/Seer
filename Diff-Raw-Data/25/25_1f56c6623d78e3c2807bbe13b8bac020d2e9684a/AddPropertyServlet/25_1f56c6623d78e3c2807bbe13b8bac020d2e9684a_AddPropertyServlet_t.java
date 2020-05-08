 package ar.edu.itba.paw.grupo1.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ar.edu.itba.paw.grupo1.ApplicationContainer;
 import ar.edu.itba.paw.grupo1.model.Property;
 import ar.edu.itba.paw.grupo1.service.PropertyService;
 
 @SuppressWarnings("serial")
 public class AddPropertyServlet extends AbstractPropertyServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
		req.setAttribute("property", new Property());
		render(req, resp, "editProperty.jsp", "Add Property");
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		PropertyService propertyService = ApplicationContainer.get(PropertyService.class);
 		Property property = getProperty(req, resp);
 
 		propertyService.save(property);
		req.getRequestDispatcher("listProperties").forward(req, resp);
 
 	}
 }
