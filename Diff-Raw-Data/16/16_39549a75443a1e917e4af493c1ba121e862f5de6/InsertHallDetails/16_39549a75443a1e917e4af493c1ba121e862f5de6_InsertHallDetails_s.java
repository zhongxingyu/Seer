 package com.globalmesh.action.hall;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.util.Streams;
 import org.apache.commons.io.IOUtils;
 
 import com.globalmesh.dao.HallDAO;
 import com.globalmesh.dto.Hall;
 import com.globalmesh.util.Constants;
 import com.globalmesh.util.Utility;
 import com.google.appengine.api.datastore.Blob;
 
 /**
  * This serlet insert new hall to database or update existing hall from the database.
  *
  */
 
 public class InsertHallDetails extends HttpServlet {
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		ServletFileUpload upload = new ServletFileUpload();
 		Blob image = null;
 		Map<String, String> formFields = new HashMap<String, String>();
 		try {
 			FileItemIterator iterator = upload.getItemIterator(req);
 			
 			while(iterator.hasNext()){
 				FileItemStream item = iterator.next();
 				InputStream stream = item.openStream();
 				
 				if(item.isFormField()){
 					formFields.put(item.getFieldName(), Streams.asString(stream));
 				} else {
 					byte[] b = IOUtils.toByteArray(stream);
 					if(b.length > 0) {
 						image = new Blob(b);
 					}
 				}
 			}	
 			
 			
 		} catch (FileUploadException e) {
 			e.printStackTrace();
 		}
 		
 		String hallName = formFields.get("hallName");
 		int numOfSeats = Integer.parseInt(formFields.get("noOfSeats"));
 		boolean is3D = formFields.get("is3D")==null?false:true;
 		double odcFull = Double.parseDouble(formFields.get("odcTicketPrice"));
 		double odcHalf = Double.parseDouble(formFields.get("odcHalfTicketPrice"));
 		int seatLimit = Integer.parseInt(formFields.get("maxSeat"));
 		String hallUri = formFields.get("hallUri");
 		double price3D = Double.parseDouble(formFields.get("priceAddition3D"));
 		
 		Hall h = HallDAO.INSTANCE.getHallById(hallName);
 		
 		if(h != null ) {
 			h.setNumOfSeats(numOfSeats);
 			h.setThreeD(is3D);
 			h.setOdcFull(odcFull);
 			h.setOdcHalf(odcHalf);
 			h.setSeatLimit(seatLimit);
 			h.setSeatPlanUrl(hallUri);
 			h.setPrice3D(price3D);
 			if(image != null) {
 				h.setMovieBanner(image);
 			}
 			
 			if(HallDAO.INSTANCE.updateHall(h)){
 				req.setAttribute("msgClass", Constants.MSG_CSS_SUCCESS);
 				String message = MessageFormat.format(Utility.getCONFG().getProperty(Constants.HALL_UPDATE_SUCCESS_FULL), h.getHallId());
 				req.setAttribute("message", message);
 				req.getRequestDispatcher("/messages.jsp").forward(req, resp);
 			} else {
 				req.setAttribute("msgClass", Constants.MSG_CSS_ERROR);
 				req.setAttribute("message",Utility.getCONFG().getProperty(Constants.HALL_TRAN_FAIl));
 				req.getRequestDispatcher("/messages.jsp").forward(req, resp);
 			}
 			
 		} else {
 			h = new Hall();
 			h.setHallId(hallName);
 			h.setNumOfSeats(numOfSeats);
 			h.setThreeD(is3D);
 			h.setOdcFull(odcFull);
 			h.setOdcHalf(odcHalf);
 			h.setSeatLimit(seatLimit);
 			h.setSeatPlanUrl(hallUri);
 			h.setMovieBanner(image);
 			h.setPrice3D(price3D);
 			
 			if(HallDAO.INSTANCE.addHall(h)){
 				req.setAttribute("msgClass", Constants.MSG_CSS_SUCCESS);
 				req.setAttribute("message", Utility.getCONFG().getProperty(Constants.HALL_ENTER_SUCCESSFULL));
 				req.getRequestDispatcher("/messages.jsp").forward(req, resp);
 			} else {
 				req.setAttribute("msgClass", Constants.MSG_CSS_ERROR);
 				req.setAttribute("message", Utility.getCONFG().getProperty(Constants.HALL_TRAN_FAIl));
 				req.getRequestDispatcher("/messages.jsp").forward(req, resp);
 			}
 		}
 		
 		
 	}
 	
 }
