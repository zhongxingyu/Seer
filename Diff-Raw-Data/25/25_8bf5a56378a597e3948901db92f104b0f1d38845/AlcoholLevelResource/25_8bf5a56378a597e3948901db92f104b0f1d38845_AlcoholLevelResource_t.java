 package de.kile.zapfmaster2000.rest.api.statistics;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 
@Path("statistics")
 public class AlcoholLevelResource {
 
 	// TODO AlcoholLevelResponse necessary?
 
 	@SuppressWarnings("unchecked")
 	@Path("alcoholLevel")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveAlcoholLevel(@QueryParam("token") String pToken,
 			@QueryParam("user") User pUser) {
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTime(new Date());
 			calendar.add(Calendar.HOUR, -36);
 
 			// Widmark formula c = A / (m*r)
 			// c alcohol concentration in per mille
 			// A mass of alcohol in g
 			// => A = V * e* p
 			// V = volume in ml
 			// e = alcohol content = 0.048 TODO: brand specific
 			// p = density of alcohol = .8
 			// r reduction factor (M: .69, F: .57)
 			// m mass of user
 
 			List<Object[]> resultAmountByHour = session
 					.createQuery(
 							"SELECT (d.amount*1000*0.048*0.8), d.date"
 									+ " FROM Drawing d"
 									+ " WHERE d.user = :user"
 									+ " AND d.date > :timestamp "
 									+ " ORDER BY d.date ASC")
 					.setEntity("user", pUser)
 					.setDate("timestamp", calendar.getTime()).list();
 
 			tx.commit();
 
 			if (resultAmountByHour.size() > 0) {
 				// int sex = (Integer) resultAmountByHour.get(0)[2];
 				// int weight = (Integer) resultAmountByHour.get(0)[3];
 
 				int sex = pUser.getSex().getValue(); // TODO use enum instead
 				int weight = pUser.getWeight();
 
 				double reductionFactor;
 				double alcoholBreakDown = 0.15; // per mille per hour
 				if (sex == 0) {// male
 					reductionFactor = 1 / (weight * 0.69);
 				} else { // female
 					reductionFactor = 1 / (weight * 0.57);
 				}
 
 				Date oldDate = (Date) resultAmountByHour.get(0)[1];
 				double oldConcentration = (Double) resultAmountByHour.get(0)[0]
 						* reductionFactor;
 
 				Date curDate;
 				double curConcentration;
 
 				for (int i = 1; i < resultAmountByHour.size(); i++) {
 					curDate = (Date) resultAmountByHour.get(i)[1];
 					curConcentration = (Double) resultAmountByHour.get(i)[0]
 							* reductionFactor;
 
 					oldConcentration = curConcentration + oldConcentration
 							* (curDate.getTime() - oldDate.getTime()) / 1000
 							/ 60 / 60 * alcoholBreakDown;
 				}
 
 				AlcoholLevelResponse response = new AlcoholLevelResponse();
 
 				response.setAlcoholLevel(oldConcentration);
 
 				return Response.ok(response).build();
 			}
 
 		}
 		// TODO user empty? user not found?
 		return Response.status(Status.FORBIDDEN).build();
 	}
 }
