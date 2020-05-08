 package de.kile.zapfmaster2000.rest.api.register;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.core.registration.RegistrationException;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Sex;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Package;
 
 @Path("/register")
 public class RegisterResource {
 
 	/** logger */
 	private static final Logger LOG = Logger.getLogger(RegisterResource.class);
 
 	/**
 	 * Registers a new user.
 	 * 
 	 * <p>
 	 * If a user with the given name does already exist, no user will be
 	 * created.
 	 * </p>
 	 * 
 	 * @param pUserName
 	 * @param pWeight
 	 * @param pSex
 	 * @param pPassword
 	 * @param pToken
 	 */
 	@POST
 	@Path("/user")
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public Response registerUser(@FormParam("userName") String pUserName,
 			@FormParam("weight") int pWeight, @FormParam("sex") String pSex,
 			@FormParam("password") String pPassword,
 			@FormParam("token") String pToken) {
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 		if (account != null) {
 			Sex sex;
 			if ("m".equals(pSex)) {
 				sex = Sex.MALE;
 			} else if ("f".equals(pSex)) {
 				sex = Sex.FEMALE;
 			} else {
 				LOG.error("Invalid sex: " + pSex);
 				return Response.status(Status.BAD_REQUEST).build();
 			}
 
 			try {
 				Zapfmaster2000Core.INSTANCE.getRegistrationService()
						.registerUser(account, pUserName, pPassword, sex, pWeight);
 			} catch (RegistrationException e) {
 				LOG.warn("User registration failed", e);
 				return Response.status(Status.BAD_REQUEST).build();
 			}
 
 			// login to newly created user
 			String token = Zapfmaster2000Core.INSTANCE.getAuthService()
 					.loginUser(pUserName, pPassword);
 			if (token != null) {
 				return Response.ok(token).build();
 			} else {
 				LOG.error("Could not log in to previously created user "
 						+ pUserName);
 				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
 			}
 
 		} else {
 			return Response.status(Status.FORBIDDEN).build();
 		}
 
 	}
 
 	@POST
 	@Path("/rfid")
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public Response changeRfidTag(@FormParam("token") String pToken,
 			@FormParam("rfid") long pRfid) {
 
 		User user = Zapfmaster2000Core.INSTANCE.getAuthService().retrieveUser(
 				pToken);
 
 		if (user != null) {
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			user = (User) session.load(Zapfmaster2000Package.eINSTANCE
 					.getUser().getName(), user.getId());
 			user.setRfidTag(pRfid);
 			session.update(user);
 
 			tx.commit();
 			
 			return Response.ok().build();
 		} else {
 			return Response.status(Status.FORBIDDEN).build();
 		}
 	}
 }
