 package controllers;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import models.Act;
 import models.Competence;
 import models.Revision;
 import play.db.jpa.JPA;
 import play.mvc.Controller;
 
 public class SearchController extends Controller {
 
 	public static void advancedSearch(String act, String revision,
 			String action, String actorAct, String actorComp,
 			String passiveActor, String location, String dateFrom, String dateTo) {
 
 		List<Integer> revisionList = new LinkedList();
 		for (int i = 0; i <= Revision.maxRevisionCount(); i++) {
 			revisionList.add(i);
 		}
 
 		String query = createAdvancedSearchQuery(act, revision, action,
				actorAct, actorComp, passiveActor, location, dateFrom, dateTo);
 		Query q = JPA.em().createQuery(query);
 
 		List<Competence> results = new LinkedList();
 		results.addAll(q.getResultList());
 
 		// Get all Act names
 		List<Act> actList = Act.findAll();
 
 		render(results, revisionList, act, revision, revision, actorAct,
 				actorComp, action, passiveActor, location, dateFrom, dateTo,
 				actList);
 	}
 
 	/**
 	 * Helper method for {@link controllers.SearchController.advancedSearch}
 	 * that creates the query text.
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
 	private static String createAdvancedSearchQuery(String act,
 			String revision, String action, String actorAct, String actorComp,
			String passiveActor, String location, String dateFrom, String dateTo) {
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
 
 		String queryText = "SELECT DISTINCT c FROM Competence c "
 				+ "LEFT JOIN c.actor actor "
 				+ "LEFT JOIN c.action action "
 				+ "LEFT JOIN c.term term "
 				+ "LEFT JOIN c.actor actor "
 				+ "LEFT JOIN c.passiveActor actor"
 				+ "LEFT JOIN term.revision revision "
 				+ "LEFT JOIN revision.act act "
 				+ "LEFT JOIN act.legislator legislator "
 				+ "LEFT JOIN act.location location "
 				+ "WHERE (UPPER(act.name) LIKE '%"
 				+ act
 				+ "%' OR act.name IS NULL) "
 				+ "AND (UPPER(legislator.name) LIKE '%"
 				+ actorAct
 				+ "%' OR legislator.name IS NULL) "
 				+ "AND (UPPER(action.name) LIKE '%"
 				+ action // TODO: In ganzem titel suchen
 				+ "%' OR action.name IS NULL) "
 				+ "AND (UPPER(actor.name) LIKE '%" + actorComp
 				+ "%' OR actor.name IS NULL) "
 				+ "AND (UPPER(c.passiveActor.name) LIKE '%" + passiveActor
 				+ "%' OR c.passiveActor.name IS NULL) "
 				+ "AND (UPPER(location.name) LIKE '%" + location
 				+ "%' OR location.name IS NULL) ";
 		if (revision != null && !revision.equals("noInput")) {
 			queryText = queryText + "AND revision.revisionCount='" + revision
 					+ "'";
 		}
 		if (dateFrom != null && dateTo != null) {
			// convert date fields
			dateFrom = dateFrom + " 00:00:00.000";
			dateTo = dateTo + " 00:00:00.000";
 
 			queryText = queryText + " AND ((revision.commencementDate > '"
 					+ dateFrom + "' AND revision.commencementDate < '" + dateTo
 					+ "') OR (revision.orderDate > '" + dateFrom
 					+ "' AND revision.orderDate < '" + dateTo + "')";
 		}
 
 		return queryText;
 
 	}
 
 	public static void basicSearch(String input) {
 		try {
 			input = input.toUpperCase();
 		} catch (NullPointerException e) {
 		}
 		Query qAct = JPA
 				.em()
 				.createQuery(
 						"SELECT DISTINCT a FROM Act a LEFT JOIN a.location location "
 								+ "LEFT JOIN a.legislator actor "
 								+ "WHERE UPPER(a.name) LIKE '%" + "" + input
 								+ "%' " + "OR UPPER(location.name) LIKE '%"
 								+ "" + input + "%' "
 								+ "OR UPPER(actor.name) LIKE '%" + input + "%'");
 
 		Query qAction = JPA.em().createQuery(
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
 
 		List<Act> actResults = qAct.getResultList();
 		List<Competence> compResults = qAction.getResultList();
 
 		render(actResults, compResults);
 
 	}
 
 	public static void searchPage() {
 		render();
 	}
 
 }
