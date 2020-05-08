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
 public class AmountResource {
 
 	@SuppressWarnings("unchecked")
 	@Path("amountStats")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveCurrentKegAmount(@QueryParam("token") String pToken) {
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			// TODO restrict to box
 
 			List<Object> resultAmountCurrentKeg = session
 					.createQuery(
 							"SELECT SUM(d.amount)" + " FROM Keg k, Drawing d"
 									+ " WHERE d.keg = k GROUP BY k.id "
 									+ " ORDER BY MAX(d.date) DESC")
 					.setMaxResults(1).list();
 
 			// TODO restrict to box
 			List<Object[]> resultAmounts = session.createQuery(
 					"SELECT SUM(d.amount), MAX(d.amount) FROM Drawing d")
 					.list();
 
 			List<Object> resultMostActivity = session
 					.createQuery(
 							"SELECT HOUR(d.date) FROM Drawing d"
 									+ " GROUP BY HOUR(d.date)"
 									+ " ORDER BY SUM(d.amount) DESC")
 					.setMaxResults(1).list();
 
 			tx.commit();
 
 			AmountResponse response = new AmountResponse();
 			response.setAmountCurrentKeg((Double) resultAmountCurrentKeg.get(0));
 			response.setAmountTotal((Double) resultAmounts.get(0)[0]);
 			response.setGreatestDrawing((Double) resultAmounts.get(0)[1]);
 			response.setMostActivityHour((Integer) resultMostActivity.get(0));
 
 			return Response.ok(response).build();
 		}
 
 		return Response.status(Status.FORBIDDEN).build();
 	}
 }
