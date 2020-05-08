 package controllers;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import models.Act;
 import models.Competence;
 import models.Revision;
 import play.db.jpa.JPA;
 import play.mvc.Controller;
 import search.Search;
 
 public class SearchController extends Controller {
 
 	public static void search(String act, String revision, String action,
 			String actorAct, String actorComp, String passiveActor,
 			String location, String orderDateFrom, String orderDateTo,
 			String commDateFrom, String commDateTo, String endDateFrom,
 			String endDateTo) {
 
 		/*
 		 * Find the highest revision number and add id to revisionList (used in
 		 * select-field in advanced-Search.html
 		 */
 		List<Integer> revisionList = new LinkedList();
 		for (int i = 0; i <= Revision.maxRevisionCount(); i++) {
 			revisionList.add(i);
 		}
 
 		/* Get all Act names (used in overlay to show all acts) */
 		Query q = JPA
 				.em()
 				.createQuery(
						"SELECT a FROM Revision r JOIN r.act a WHERE r.revisionCount=0 ORDER BY r.commencementDate");
 		List<Act> actList = q.getResultList();
 
 		/* Delegate search to search.Search.advancedSearch(). */
 		List<Competence> results = Search.search(act, revision, action,
 				actorAct, actorComp, passiveActor, location, orderDateFrom,
 				orderDateTo, commDateFrom, commDateTo, endDateFrom, endDateTo);
 
 		render(results, revisionList, act, revision, revision, actorAct,
 				actorComp, action, passiveActor, location, orderDateFrom,
 				orderDateTo, commDateFrom, commDateTo, endDateFrom, endDateTo,
 				actList);
 	}
 }
