 package application.commands;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.GregorianCalendar;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.msgpack.MessagePack;
 import org.msgpack.packer.Packer;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.multipart.MultipartResolver;
 import org.springframework.web.multipart.commons.CommonsMultipartResolver;
 
 import application.MessageHelper;
 import application.ServerParameters;
 
 import ch.qos.logback.classic.Logger;
 
 import exceptions.MapperException;
 import exceptions.ParameterException;
 import exceptions.UnrecognizedUserException;
 import domain.message.Message;
 import domain.message.MessageFactory;
 import domain.message.MessageIdentityMap;
 import domain.message.mappers.MessageOutputMapper;
 import domain.user.User;
 import domain.user.mappers.UserInputMapper;
 
 public class CreateMessageCommand extends FrontCommand {
 
 	@Override
 	public void execute(HttpServletRequest request, HttpServletResponse response) throws MapperException, ParameterException, IOException, UnrecognizedUserException, SQLException {
 		MultipartResolver resolver = new CommonsMultipartResolver();
 		
 		// Make sure our request is multi-part; if it's not, then it's not properly formatted.
 		if (!resolver.isMultipart(request))
 			throw new ParameterException("Put requests must be multi-part, as in RFC1867.");
 		
 		MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request); 
 		
 		// If java is smart, it will allocate this on the stack.
 		MultipartFile multipartFile = multipartRequest.getFile("bin");
 		
 		if (multipartFile == null)
 			throw new ParameterException("Put requests must have 'bin' as a multipart file upload.");
 		
 		ServerParameters params = ServerParameters.getUniqueInstance();
 
 		// Check if file is to large
 		if (multipartFile.getSize() > Long.parseLong(params.get("maxMessageSizeBytes").getValue())) {
 			throw new ParameterException("Uploaded file's size is too large.");
 		}
 		
 		// Check if file is too small
 		if (multipartFile.getSize() < Long.parseLong(params.get("minMessageSizeBytes").getValue())) {
			throw new ParameterException("Uploaded file's size is too small.");
 		}
 				
 		byte[] messageBytes = null;
 		messageBytes = multipartFile.getBytes();
 		
 		// Get location information
 		String strLongitude = multipartRequest.getParameter("longitude");
 		String strSpeed = multipartRequest.getParameter("speed");
 		String strLatitude = multipartRequest.getParameter("latitude");
 		
 		double longitude;
 		double latitude;
 		float speed = 0;
 		
 		try {
 			// Get speed from request object
 			if ((strSpeed = request.getParameter("speed")) != null)
 				speed = Float.parseFloat(strSpeed);
 			
 			longitude = Double.parseDouble(strLongitude);
 			latitude = Double.parseDouble(strLatitude);
 		} catch (NumberFormatException e) {
 			throw new ParameterException("Longitude, latitude, and/or speed number format exception.", e);
 		}
 		
 		String email = multipartRequest.getParameter("email");
 		
 		if (email == null)
 			throw new ParameterException("Must pass in the email to validate user.");
 		
 		User user = UserInputMapper.findByEmail(email);
 		
 		if (user == null)
 			throw new UnrecognizedUserException("Unable to find user.");
 		
 		Message msg = null;
 		
 		try {
 			msg = MessageFactory.createNew(user.getUid(), messageBytes, speed, latitude, longitude, new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()), 0);
 		} catch (NoSuchAlgorithmException e) {
 			Logger logger = (Logger)LoggerFactory.getLogger("application");
 			logger.error("No such algorithm exception thrown when trying to create message: {}", e);
 		}
 		
 		// Put the new message in the identity map and insert into database
 		MessageIdentityMap.put(msg.getMid(), msg);		 
 		MessageOutputMapper.insert(msg);
 		
 		//TODO change this shit
 		// Write the id of the newly created message to the http response
 		MessageHelper.setMessageIDToResponse(msg,new DataOutputStream(response.getOutputStream()));
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 	
 }
