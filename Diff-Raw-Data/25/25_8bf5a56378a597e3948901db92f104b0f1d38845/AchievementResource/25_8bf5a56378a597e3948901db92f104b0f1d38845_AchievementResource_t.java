 package de.kile.zapfmaster2000.rest.api.statistics;
 
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
 
@Path("statistics")
 public class AchievementResource {
 
 	// TODO add specific to user
 
 	@SuppressWarnings("unchecked")
 	@Path("achievementsStats")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveAchievementStats(@QueryParam("token") String pToken) {
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			// TODO restrict to box
 			List<Object> resultAchievementCount = session.createQuery(
 					"SELECT COUNT(g.id) FROM GainedAchievement g").list();
 
 			List<Object> resultMostActivity = session
 					.createQuery(
 							"SELECT HOUR(g.date) FROM GainedAchievement g"
 									+ " GROUP BY HOUR(g.date)"
 									+ " ORDER BY COUNT(g.id) DESC")
 					.setMaxResults(1).list();
 
 			tx.commit();
 
 			AchievementResponse response = new AchievementResponse();
 
 			response.setCount((Long) resultAchievementCount.get(0));
 			response.setMostAchievementHour((Integer) resultMostActivity.get(0));
 
 			return Response.ok(response).build();
 		}
 
 		return Response.status(Status.FORBIDDEN).build();
 	}
 }
