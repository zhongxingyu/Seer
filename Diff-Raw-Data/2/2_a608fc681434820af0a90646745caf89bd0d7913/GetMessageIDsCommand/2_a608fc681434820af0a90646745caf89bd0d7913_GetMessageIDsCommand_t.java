 package application.commands;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import application.IOUtils;
 
 import domain.message.mappers.MessageInputMapper;
 
 import exceptions.MapperException;
 import exceptions.ParameterException;
 import exceptions.UnrecognizedUserException;
 
 public class GetMessageIDsCommand extends FrontCommand{
 
 	@Override
 	public void execute(HttpServletRequest request, HttpServletResponse response) throws MapperException, ParameterException, UnrecognizedUserException, SQLException, IOException {
 		
 		String stringLongitude;
 		String stringLatitude;
 		String stringSpeed;
 		String sort = "";
 		
 		float speed = 0;
 		double longitude;
 		double latitude;
 		
 		// Get message longitude from request object
 		if ((stringLongitude = request.getParameter("longitude")) == null)
 			throw new ParameterException("Missing 'longitude' parameter.");
 		
 		// Get message latitude from request object
 		if ((stringLatitude = request.getParameter("latitude")) == null)
 			throw new ParameterException("Missing 'latitude' parameter.");
 		
 		// Get message latitude from request object
		if ((sort = request.getParameter("sorttype")) == null)
 				throw new ParameterException("Missing 'sorttype' parameter.");
 		try {
 			// Get speed from request object
 			if ((stringSpeed = request.getParameter("speed")) != null)
 				speed = Float.parseFloat(stringSpeed);
 			
 			longitude = Double.parseDouble(stringLongitude);
 			latitude = Double.parseDouble(stringLatitude);
 		} catch (NumberFormatException e) {
 			// TODO Log this shit
 			throw new ParameterException("Longitude, latitude, and/or speed number format exception.", e);
 		}
 
 		List<BigInteger> ids = MessageInputMapper.findIdsInProximity(longitude, latitude, speed, sort);	
 		
 		response.setContentType("text/plain");
 		response.setStatus(HttpServletResponse.SC_OK);
 		
 		IOUtils.writeListMessageIDsToStream(ids, new DataOutputStream(response.getOutputStream()));
 	}
 
 	
 }
