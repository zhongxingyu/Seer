 package ar.edu.itba.paw.grupo1.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ar.edu.itba.paw.grupo1.ApplicationContainer;
 import ar.edu.itba.paw.grupo1.controller.exception.InvalidParameterException;
 import ar.edu.itba.paw.grupo1.controller.exception.PermissionDeniedException;
 import ar.edu.itba.paw.grupo1.model.Picture;
 import ar.edu.itba.paw.grupo1.model.Property;
 import ar.edu.itba.paw.grupo1.service.PictureService;
 import ar.edu.itba.paw.grupo1.service.PropertyService;
 
 @SuppressWarnings("serial")
 public class PropertyDetailServlet extends BaseServlet {
 
 	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

 		if (checkIntegerParameter(req, "id")) {
			PropertyService propertyService = ApplicationContainer
					.get(PropertyService.class);
			PictureService pictureService = ApplicationContainer
					.get(PictureService.class);

 			int id = Integer.parseInt(req.getParameter("id"));
 			Property property = propertyService.getById(id);
 			List<Picture> pictures = pictureService.getByPropId(id);

 			if (property == null) {
 				throw new InvalidParameterException();
			} else if (!property.isPublished() && property.getUserId() != getLoggedInUser(req).getId()) {
				
				resp.sendRedirect("/query?unpublished=true");
				return;
 			}

 			req.setAttribute("property", property);
 			if (pictures.size() > 0) {
 				req.setAttribute("pictures", pictures);
 			}
 		} else {
 			throw new InvalidParameterException();
 		}
 		render(req, resp, "propertyDetail.jsp", "Property Detail");
	}
 }
