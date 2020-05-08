 package de.kile.zapfmaster2000.rest.api.statistics;
 
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
 
 public class KegResource {
 
 	@Path("kegStats")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response retrieveKegStats(@QueryParam("token") String pToken) {
 		Account account = Zapfmaster2000Core.INSTANCE.getAuthService()
 				.retrieveAccount(pToken);
 		if (account != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			// TODO restrict to Box?
 
 			@SuppressWarnings("unchecked")
 			List<Object> resultCurrentKeg = session
 					.createQuery(
 							"SELECT k.id, k.brand, k.size, k.startDate "
 									+ " FROM Keg k, Drawing d "
 									+ " WHERE d.keg = k "
 									+ " ORDER BY d.date DESC").setMaxResults(1)
 					.list();
 
 			@SuppressWarnings("unchecked")
 			List<Object> resultNumberKegs = session.createQuery(
 					"SELECT COUNT (DISTINCT k.id) FROM Keg k, Drawing d"
 							+ " WHERE d.keg = k").list();
 
 			tx.commit();
 
 			KegResponse response = new KegResponse();
 
 			Object[] resultRow = (Object[]) resultCurrentKeg.get(0);
 			response.setKegId((Long) resultRow[0]);
 			response.setBrand((String) resultRow[1]);
 			response.setSize((Integer) resultRow[2]);
 			response.setStart_date((Date) resultRow[3]);
 			response.setKegNumbers((Long) resultNumberKegs.get(0));
 
 			return Response.ok(response).build();
 		}
 
 		return Response.status(Status.FORBIDDEN).build();
 	}
 }
