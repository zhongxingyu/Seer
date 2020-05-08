 /**
  * SOEN 490
  * Capstone 2011
  * Team members: 	
  * 			Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 package application.commands;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import application.ServerParameters;
 import application.response.IOUtils;
 import application.strategy.MissingRetrievalStrategyException;
 import application.strategy.RetrievalStrategy;
 import application.strategy.RetrievalStrategyFactory;
 
 import exceptions.MapperException;
 import exceptions.ParameterException;
 import exceptions.UnrecognizedUserException;
 
 /**
  * Command for retrieving message ids.
  * Request parameters:
  *  - longitude Client's longitude
  *  - latitude Client's latitud
  *  - speed (optional) Client's speed
  *  - limit (optional) Limit of messages to return. If this value is over the application value, it is overwritten.
  *  - sort Field to sort by
  *  - advertiser (optional) If set to true, will only return advertiser messages. If set to anything else, will only return non-advertiser messages. If ommitted, returns all types.
  *
  */
 public class GetMessageIDsCommand extends FrontCommand{
 
 	@Override
 	public void execute(HttpServletRequest request, HttpServletResponse response) throws MapperException, ParameterException, UnrecognizedUserException, SQLException, IOException {
 		
 		String stringLongitude;
 		String stringLatitude;
 		String stringSpeed;
 		String sort = "";
 		String isAdvertiser = "";
 		String stringLimit;
 		
		int serverLimit = Integer.parseInt(ServerParameters.getUniqueInstance().get("maxMessages").getValue());
 		float speed = 0;
 		double longitude;
 		double latitude;
 		int limit;
 		
 		// Get longitude from request object
 		if ((stringLongitude = request.getParameter("longitude")) == null)
 			throw new ParameterException("Missing 'longitude' parameter.");
 		
 		// Get latitude from request object
 		if ((stringLatitude = request.getParameter("latitude")) == null)
 			throw new ParameterException("Missing 'latitude' parameter.");
 
 		if ((sort = request.getParameter("sort")) == null)
 			throw new ParameterException("Missing 'sort' parameter.");
 		
 		if ((isAdvertiser = request.getParameter("advertiser")) == null)
 			isAdvertiser = Boolean.toString(Boolean.parseBoolean(isAdvertiser));
 		
 		if ((stringLimit = request.getParameter("limit")) == null)
 			limit = serverLimit;
 		else 
 			try {
 				limit = Integer.parseInt(stringLimit);
 				// if the request limit is higher than the server limit, restrict it
 				if (limit > serverLimit) 
 					limit = serverLimit;
 			} catch (NumberFormatException e) {
 				throw new ParameterException("Invalid 'limit' parameter provided.", e);
 			}
 	
 		try {
 			// Get speed from request object
 			if ((stringSpeed = request.getParameter("speed")) != null)
 				speed = Float.parseFloat(stringSpeed);
 			
 			longitude = Double.parseDouble(stringLongitude);
 			latitude = Double.parseDouble(stringLatitude);
 		} catch (NumberFormatException e) {
 			throw new ParameterException("Longitude, latitude, and/or speed number format exception.", e);
 		}
 		
 		RetrievalStrategyFactory factory = RetrievalStrategyFactory.getUniqueInstance();
 		RetrievalStrategy strategy = null;
 		
 		try {
 			strategy = factory.createStrategy(sort, isAdvertiser);
 		} catch (MissingRetrievalStrategyException e) {
 			throw new ParameterException(e.getMessage());
 		}
 		
 		List<BigInteger> ids = strategy.retrieve(longitude, latitude, speed, limit);
 		
 		response.setContentType("application/octet-stream");
 		response.setStatus(HttpServletResponse.SC_OK);
 		
 		IOUtils.writeListMessageIDsToStream(ids, new DataOutputStream(response.getOutputStream()));
 	}
 
 	
 }
