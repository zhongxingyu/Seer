 package de.kile.zapfmaster2000.rest.api.achievements;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.api.achievements.AchievementResonse.UserThatGained;
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 
 @Path("/achievements")
 public class AchievementsResource {
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveAchievements(@QueryParam("token") String pToken) {
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 		if (account != null) {
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			@SuppressWarnings("unchecked")
 			List<Object[]> result = session
 					.createQuery(
 							"SELECT a.id, a.name, a.description, a.imagePath, "
 									+ "u.id, u.name, u.imagePath "
 									+ "FROM Achievement a "
 									+ "LEFT JOIN a.gained g LEFT JOIN g.user u "
 									+ "WITH u.account.id = :accountId "
 									+ "ORDER BY a.name ASC, g.date DESC")
 					.setLong("accountId", account.getId()).list();
 
 			tx.commit();
 
 			Map<Long, AchievementResonse> mapIdToResp = new LinkedHashMap<>();
 
 			for (Object[] resultRow : result) {
 				long id = (Long) resultRow[0];
 				AchievementResonse resp;
 				if (mapIdToResp.containsKey(id)) {
 					resp = mapIdToResp.get(id);
 				} else {
 					resp = new AchievementResonse();
					resp.setAchivementId((Long) resultRow[0]);
 					resp.setAchievementName((String) resultRow[1]);
 					resp.setAchievementDescription((String) resultRow[2]);
 					resp.setAchievementImage((String) resultRow[3]);
 					mapIdToResp.put(id, resp);
 				}
 
 				if (resultRow[4] != null) {
 					// a user gained that achievement
 					UserThatGained user = new UserThatGained();
 					user.setUserId((Long) resultRow[4]);
 					user.setUserName((String) resultRow[5]);
 					user.setUserImage((String) resultRow[6]);
 					resp.getUsers().add(user);
 				}
 			}
 
 			return Response.ok(new ArrayList<>(mapIdToResp.values())).build();
 		}
 
 		return Response.status(Status.FORBIDDEN).build();
 
 	}
 
 }
