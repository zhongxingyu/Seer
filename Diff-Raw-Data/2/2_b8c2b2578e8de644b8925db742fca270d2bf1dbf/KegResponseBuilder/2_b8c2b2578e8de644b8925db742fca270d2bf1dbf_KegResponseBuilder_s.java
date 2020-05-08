 package de.kile.zapfmaster2000.rest.impl.core.statistics;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.api.statistics.KegResponse;
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 
 public class KegResponseBuilder {
 
 	/**
 	 * Builds {@link KegResponse}.
 	 * 
 	 * @param account
 	 * @return {@link KegResponse}. <code>LastsUntil</code> may be
 	 *         <code>null</code> if there was no drawing in the last 3 hours in
 	 *         a current keg.
 	 */
 	@SuppressWarnings("unchecked")
 	public static KegResponse[] retrieveKegResponse(Account account) {
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 		
 		List<Object[]> resultCurrentKegs = session
 				.createQuery(
 						"SELECT k.id, k.brand, k.size, k.startDate, SUM(d.amount), k.box.id, k.box.location"
 								+ " FROM Keg k LEFT JOIN k.drawings AS d"
 								+ " WHERE k.box.account = :account "
 								+ " AND k.endDate IS NULL "
								+ " AND k.box.account.enabled = true "
 								+ " GROUP BY k.id, k.brand, k.size, k.startDate "
 								+ " ORDER BY k.id")
 				.setEntity("account", account).list();
 
 		Calendar calendar = Calendar.getInstance();
 		calendar.add(Calendar.HOUR, -3);
 
 		List<Object[]> resultLastThreeHours = session
 				.createQuery(
 						"SELECT k.id, SUM(d.amount)"
 								+ " FROM Keg k, Drawing d, User u "
 								+ " WHERE d.keg = k AND d.user = u "
 								+ " AND k.endDate IS NULL "
 								+ " AND d.date > :time"
 								+ " AND u.account = :account "
 								+ " GROUP BY (k.id) ORDER BY k.id")
 				.setTimestamp("time", calendar.getTime())
 				.setEntity("account", account).list();
 
 		List<Object> resultNumberKegs = session
 				.createQuery(
 						"SELECT COUNT (DISTINCT k.id) FROM Keg k, Drawing d, User u"
 								+ " WHERE d.keg = k AND d.user = u "
 								+ " AND u.account= :account")
 				.setEntity("account", account).list();
 
 		tx.commit();
 
 		KegResponse[] response = new KegResponse[resultCurrentKegs.size()];
 
 		int idxKeg = 0;
 		for (int i = 0; i < resultCurrentKegs.size(); i++) {
 			Object[] resultRow = (Object[]) resultCurrentKegs.get(i);
 			response[i] = new KegResponse();
 			response[i].setKegId((Long) resultRow[0]);
 			response[i].setBrand((String) resultRow[1]);
 			response[i].setSize((Integer) resultRow[2]);
 			response[i].setStartDate((Date) resultRow[3]);
 			Double amountDrank = (Double) resultRow[4];
 			if (amountDrank == null) {
 				// no one drawed something from keg
 				response[i].setCurrentAmount(0);
 			} else {
 				response[i].setCurrentAmount(amountDrank);
 			}
 			response[i].setKegNumber((Long) resultNumberKegs.get(0));
 			response[i].setBoxId((Long) resultRow[5]);
             response[i].setBoxLocation((String) resultRow[6]);
 			if (idxKeg < resultLastThreeHours.size()
 					&& response[i].getKegId() == (Long) resultLastThreeHours
 							.get(idxKeg)[0]) {
 
 				// lasts until
 				double amount = (Double) resultLastThreeHours.get(idxKeg)[1];
 				calendar = Calendar.getInstance();
 
 				calendar.add(
 						Calendar.SECOND,
 						(int) Math.ceil(response[i].getCurrentAmount() / amount
 								* (3 * 60 * 60)));
 
 				response[i].setLastsUntil(calendar.getTime());
 
 				idxKeg++;
 			}
 		}
 
 		return response;
 	}
 }
