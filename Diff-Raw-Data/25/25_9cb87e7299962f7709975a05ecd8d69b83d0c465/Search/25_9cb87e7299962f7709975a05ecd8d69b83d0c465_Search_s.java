 package search;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import models.Act;
 import models.Competence;
 import play.db.jpa.JPA;
 
 /**
  * The search class implements all search methods and executes the queries in
  * the database.
  */
 public class Search {
 
 	/**
 	 * Searches for acts containing the input text either in their name, in the
 	 * legislator's name or in the location's name.
 	 * 
 	 * @param input
 	 *            - the string to search after.
 	 * @return - a linked list containing all acts that match the input string.
 	 */
 	public static List<Act> basicActSearch(String input) {
 		try {
 			input = input.toUpperCase();
 		} catch (NullPointerException e) {
 		}
 		Query q = JPA
 				.em()
 				.createQuery(
 						"SELECT DISTINCT a FROM Act a LEFT JOIN a.location location "
 								+ "LEFT JOIN a.legislator actor "
 								+ "WHERE UPPER(a.name) LIKE '%" + "" + input
 								+ "%' " + "OR UPPER(location.name) LIKE '%"
 								+ "" + input + "%' "
 								+ "OR UPPER(actor.name) LIKE '%" + input + "%'");
 
 		List<Act> resultList = new LinkedList();
 		resultList = q.getResultList();
 
 		return resultList;
 	}
 
 	/**
 	 * Searches for competences containing the input text either in their
 	 * action's name, the active actor's name, the passive actor's name, the
 	 * term's name or the act's name.
 	 * 
 	 * @param input
 	 *            - the string to search after
 	 * @return - a linked list containing all competences that match the input
 	 *         string.
 	 */
 	public static List<Competence> basicCompetenceSearch(String input) {
 		try {
 			input = input.toUpperCase();
 		} catch (NullPointerException e) {
 		}
 
 		Query q = JPA.em().createQuery(
 				"SELECT DISTINCT c FROM Competence c "
 						+ "LEFT JOIN c.action action "
 						+ "LEFT JOIN c.term term "
 						+ "LEFT JOIN term.revision revision "
 						+ "LEFT JOIN revision.act act "
 						+ "WHERE UPPER(action.name) LIKE '%" + input + "%' "
 						+ "OR UPPER(c.actor.name) LIKE '%" + input
 						+ "%' OR UPPER(c.passiveActor.name) LIKE '%" + input
 						+ "%' OR UPPER(c.term.name) LIKE '%" + input + "%'"
 						+ "OR UPPER(c.term.revision.act.name) LIKE '%" + input
 						+ "%'");
 
 		List<Competence> resultList = new LinkedList();
 		resultList = q.getResultList();
 
 		return resultList;
 	}
 
 	/**
 	 * Gets all results for the advanced Search (invoked e.g. by
 	 * {@link controllers.SearchController.advancedSearch}).
 	 * 
 	 * @param act
 	 *            - the name of the act
 	 * @param revision
 	 *            - the revision number or "noInput" to neglect the revision.
 	 * @param action
 	 *            - the name of the action
 	 * @param actorAct
 	 *            - the legislator's name
 	 * @param actorComp
 	 *            - actor of the competence
 	 * @param passiveActor
 	 *            - the one who is affected by the competence
 	 * @param location
 	 *            - the location where the act is valid
 	 * @param dateFrom
 	 *            - lower date limit
 	 * @param dateTo
 	 *            - upper date limit
 	 * @return - the JPA query to search for all inputs.
 	 */
 
 	public static List<Competence> advancedSearch(String act, String revision,
 			String action, String actorAct, String actorComp,
 			String passiveActor, String location, String orderDateFrom,
 			String orderDateTo, String commDateFrom, String commDateTo,
 			String endDateFrom, String endDateTo) {
 
 		String query = createQuery(act, revision, action, actorAct, actorComp,
 				passiveActor, location, orderDateFrom, orderDateTo,
 				commDateFrom, commDateTo, endDateFrom, endDateTo);
 
 		Query q = JPA.em().createQuery(query);
 
 		List<Competence> resultList = new LinkedList();
 		resultList = q.getResultList();
 
 		return resultList;
 
 	}
 
 	/**
 	 * Helper method for {@link search.Search}
 	 * 
 	 * @param act
 	 *            - the name of the act
 	 * @param revision
 	 *            - the revision number or "noInput" to neglect the revision.
 	 * @param action
 	 *            - the name of the action
 	 * @param actorAct
 	 *            - the legislator's name
 	 * @param actorComp
 	 *            - actor of the competence
 	 * @param passiveActor
 	 *            - the one who is affected by the competence
 	 * @param location
 	 *            - the location where the act is valid
 	 * @param orderDateFrom
 	 *            - lower limit for order date
 	 * @param orderDateTo
 	 *            - upper limit for order date
 	 * @param commDateFrom
 	 *            - lower limit for commencement date
 	 * @param commDateTo
 	 *            - upper limit for commencement date
 	 * @param endDateFrom
 	 *            - lower limit for end date
 	 * @param endDateTo
 	 *            - upper limit for end date
 	 * @return - the JPA query to search for all inputs.
 	 */
 	private static String createQuery(String act, String revision,
 			String action, String actorAct, String actorComp,
 			String passiveActor, String location, String orderDateFrom,
 			String orderDateTo, String commDateFrom, String commDateTo,
 			String endDateFrom, String endDateTo) {
 		/*
 		 * In order to neglect case-sensitivity, all strings are converted to
 		 * upper case.
 		 */
 		try {
 			act = act.toUpperCase();
 			action = action.toUpperCase();
 			actorAct = actorAct.toUpperCase();
 			actorComp = actorComp.toUpperCase();
 			passiveActor = passiveActor.toUpperCase();
 			location = location.toUpperCase();
 		} catch (NullPointerException e) {
 		}
 		StringBuilder query = new StringBuilder();
 
 		query.append("SELECT DISTINCT c FROM Competence c "
 				+ "LEFT JOIN c.actor actor "
 				+ "LEFT JOIN c.action action "
 				+ "LEFT JOIN c.term term "
 				+ "LEFT JOIN c.actor actor "
 				+ "LEFT JOIN c.passiveActor passiveActor" // <---
 				+ "LEFT JOIN term.revision revision "
 				+ "LEFT JOIN revision.act act "
 				+ "LEFT JOIN act.legislator legislator "
 				+ "LEFT JOIN act.location location ");
 		if (act != null)
 			query.append("WHERE UPPER(act.name) LIKE '%" + act + "%' ");
 		if (actorAct != null)
 			query.append("AND UPPER(legislator.name) LIKE '%" + actorAct
 					+ "%' ");
 		if (action != null)
 			// TODO: In ganzem Titel suchen
 			query.append("AND UPPER(action.name) LIKE '%" + action + "%' ");
 		if (actorComp != null)
 			query.append("AND UPPER(actor.name) LIKE '%" + actorComp + "%' ");
 		if (passiveActor != null)
 			query.append("AND UPPER(c.passiveActor.name) LIKE '%"
 					+ passiveActor + "%' ");
 		if (location != null)
 			query.append("AND UPPER(location.name) LIKE '%" + location + "%' ");
 		if (revision != null && !revision.equals("noInput")) {
 			query.append("AND revision.revisionCount='" + revision + "'");
 		}
 
 		/* Check order dates */
 		if (orderDateFrom != null && orderDateTo != null
 				&& orderDateFrom.length() == 10 && orderDateTo.length() == 10) {
 
 			/* Convert the date format */
 			orderDateFrom = dateConverter(orderDateFrom);
 			orderDateTo = dateConverter(orderDateTo);
 			query
 					.append(" AND revision.orderDate IS NOT NULL AND revision.orderDate > '"
 							+ orderDateFrom
 							+ "' AND revision.orderDate < '"
 							+ orderDateTo + "'");
 		}
 
 		/* Check commencement dates */
 		if (commDateFrom != null && commDateTo != null
 				&& commDateFrom.length() == 10 && commDateTo.length() == 10) {
 
 			/* Convert the date format */
 			commDateFrom = dateConverter(commDateFrom);
 			commDateTo = dateConverter(commDateTo);
 			query
 					.append(" AND revision.orderDate IS NOT NULL AND revision.commencementDate > '"
 							+ commDateFrom
 							+ "' AND revision.commencementDate< '"
 							+ commDateTo
 							+ "'");
 		}
 
 		// TODO: Enddaten sauber implementieren im Model
 		/* Check end dates */
		// if (endDateFrom != null && endDateTo != null
		// && endDateFrom.length() == 10 && endDateTo.length() == 10) {
		//
		// /* Convert the date format */
		// endDateFrom = dateConverter(endDateFrom);
		// endDateTo = dateConverter(endDateTo);
		//
		// queryText = queryText
		// + " AND revision.orderDate IS NOT NULL AND revision.orderDate > '"
		// + endDateFrom + "' AND revision.orderDate < '" + endDateTo
		// + "'";
		// }
 
 		return query.toString();
 	}
 
 	private static String dateConverter(String inputDate) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(inputDate.substring(6, 10) + "-");
 		sb.append(inputDate.substring(3, 5) + "-");
 		sb.append(inputDate.substring(0, 2) + " 00:00:00");
 
 		return sb.toString();
 	}
 }
