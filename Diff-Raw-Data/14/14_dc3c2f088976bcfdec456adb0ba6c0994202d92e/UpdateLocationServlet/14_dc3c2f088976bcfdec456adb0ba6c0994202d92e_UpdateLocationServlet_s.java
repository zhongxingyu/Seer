 package com.incidences.servlets;
 
 import java.io.IOException;
import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import com.google.appengine.api.datastore.KeyFactory;
 import com.incidences.entities.Technician;
 import com.incidences.entities.TechnicianDao;
 import com.incidences.entities.TechnicianDaoImpJdo;
 
 /**
  * 
  * @author toni
  *
  */
 public class UpdateLocationServlet extends BaseServlet {
 
 	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(UpdateLocationServlet.class.getName());
 
 	/* update technician location. */
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		String idTechnician = getParameter(req, "id_technician");
 		TechnicianDao technicianDao = new TechnicianDaoImpJdo();
		Technician technician = technicianDao.getTechnician(KeyFactory.stringToKey(idTechnician));
		
 		String newLatitude = getParameter(req, "new_latitude");
 		String newLongitude = getParameter(req, "new_longitude");
 		technician.setLongitude(newLongitude);
 		technician.setLatitude(newLatitude);
		technicianDao.updateTechnician(KeyFactory.stringToKey(idTechnician), technician);
		
 		resp.setStatus(HttpServletResponse.SC_OK);
 	}
 }
