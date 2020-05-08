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
 public class DrawCountResource {
 
 	@SuppressWarnings("unchecked")
 	@Path("drawCount")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveDrawCount(@QueryParam("token") String pToken) {
 
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			session.update(account);
 
 			// TODO restrict to box/account?
 			List<Object> countByHourResult = session.createQuery(
 					"SELECT COUNT(d.id) FROM Drawing d"
 							+ " GROUP BY HOUR(d.date)").list();
 
 			tx.commit();
 
 			long sum = 0;
 			for (Object n : countByHourResult) {
 				sum += (Long) n;
 			}
 
 			DrawCountResponse drawCountResponse = new DrawCountResponse();
 			drawCountResponse.setCount(sum);
 			drawCountResponse.setAverageOperationsPerHour(Double.valueOf(sum)
 					/ countByHourResult.size());
 
 			return Response.ok(drawCountResponse).build();
 
 		}
 
 		return Response.status(Status.FORBIDDEN).build();
 	}
 }
