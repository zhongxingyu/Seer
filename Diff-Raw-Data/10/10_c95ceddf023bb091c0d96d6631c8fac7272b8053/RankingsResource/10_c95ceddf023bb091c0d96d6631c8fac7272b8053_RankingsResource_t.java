 package de.kile.zapfmaster2000.rest.api.statistics;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.constants.PlatformConstants;
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 
 /**
  * Ranks users by amount.
  * Gives out list of {@link UserAmountResponse}.
  * 
  * @author PB
  *
  */
 @Path("statistics/rankings")
 public class RankingsResource {
 
 	
 	private static final Logger LOG = Logger.getLogger(RankingsResource.class); 
 	
 	/**
 	 * Returns {@link UserAmountResponse} list ordered by the amount drawn by each {@link User}
 	 * in the given time span  
 	 * 
 	 * @param pFrom start of time span. Format: "yyyyMMdd-hhmmss". Empty String results in full list.
 	 * @param pTo end of time span. Format: "yyyyMMdd-hhmmss". Empty String results in list until now.
 	 * @param pRequest 
 	 * @return 
 	 */
 	@SuppressWarnings("unchecked")
 	public Response bestUserListTimeSpan(@QueryParam("from") String pFrom,
 			@QueryParam("to") String pTo, @Context HttpServletRequest pRequest) {
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pRequest);
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			session.update(account);
 
 			SimpleDateFormat df = new SimpleDateFormat(PlatformConstants.DATE_TIME_FORMAT); 
 			
 			
 			List<Object[]> list;
 			try {
 			if (pFrom.length() == 0) {//Full list
 				list = session
 						.createQuery(
 								"SELECT u.id, u.name, SUM(d.amount) AS amt, u.imagePath" +
 								" FROM User u, Drawing d " +
 								" WHERE d.user = u AND u.account = :account AND " +
 								" GROUP BY u.id ORDER BY amt DESC")
 						.setEntity("account", account)
 						.list();
 				
 			} else if (pTo.length() == 0){//List until now
 				Date dFrom = df.parse(pFrom);
 				list = session
 						.createQuery(
 								"SELECT u.id, u.name, SUM(d.amount) AS amt, u.imagePath" +
 								" FROM User u, Drawing d " +
 								" WHERE d.user = u AND u.account = :account AND " +
 								" d.date >= :from" +
 								" GROUP BY u.id ORDER BY amt DESC")
 						.setEntity("account", account).setDate("from", dFrom)
 						.list();				
 			} else { //general list
 				Date dFrom = df.parse(pFrom);
 				Date dTo = df.parse(pTo);
 				list = session
 						.createQuery(
 								"SELECT u.id, u.name, SUM(d.amount) AS amt, u.imagePath" +
 								" FROM User u, Drawing d " +
 								" WHERE d.user = u AND u.account = :account AND " +
 								" d.date >= :from AND d.date <= :to" +
 								" GROUP BY u.id ORDER BY amt DESC")
 						.setEntity("account", account).setDate("from", dFrom).setDate("to", dTo)
 						.list();
 				
 			}
 			} catch (ParseException e) {
 				LOG.error("Could not parse date", e);
 				return Response.status(Status.BAD_REQUEST).build();				
 			}
 			
 			tx.commit();
 
 			List<UserAmountResponse> resp = new ArrayList<>();
 			for (Object[] object : list) {
 				UserAmountResponse userAmountResponse = new UserAmountResponse();
 				userAmountResponse.setName((String) object[1]);
 				userAmountResponse.setId((Long) object[0]);
 				userAmountResponse.setAmount((Double) object[2]);
				userAmountResponse.setImage((String) object[3]);
 				resp.add(userAmountResponse);
 			}
 
 			return Response.ok(resp.toArray()).build();
 		}
 
 		return null;
 
 		
 	}
 	
 	/**
 	 * Returns {@link UserAmountResponse} list ordered by the amount drawn by each {@link User}
 	 * over the whole time span.
 	 * 
 	 * @param pRequest
 	 * @return
 	 */
 	public Response bestUserList(@Context HttpServletRequest pRequest){
 		return bestUserListTimeSpan("","",pRequest);
 		
 	}
 	
 }
