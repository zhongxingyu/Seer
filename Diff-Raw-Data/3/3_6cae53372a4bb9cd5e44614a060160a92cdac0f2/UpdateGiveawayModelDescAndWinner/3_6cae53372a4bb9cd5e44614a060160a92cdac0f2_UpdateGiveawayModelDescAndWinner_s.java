 package fr.cpcgifts.task;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.google.appengine.datanucleus.query.JDOCursorHelper;
 
 import fr.cpcgifts.model.Giveaway;
 import fr.cpcgifts.persistance.PMF;
 
 @SuppressWarnings("serial")
 public class UpdateGiveawayModelDescAndWinner extends HttpServlet {
 
 	private static final Logger log = Logger.getLogger(UpdateGiveawayModelDescAndWinner.class.getName());
 	
 	private static final int BATCH_SIZE = 100;
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		
 		Query q = pm.newQuery(Giveaway.class);
 		
 		Cursor cursor = null;
 		
 		try {
 			cursor = Cursor.fromWebSafeString(req.getParameter("cursor"));
 		} catch(Exception e) {}
 		
 		if(cursor != null) {
 			log.info("New task with cursor : " + cursor);
 			Map<String, Object> extensionMap = new HashMap<String, Object>();
 			extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
 			q.setExtensions(extensionMap);
 		}
 		
 		q.setRange(0, BATCH_SIZE);
 		
 		List<Giveaway> giveaways = (List<Giveaway>) q.execute();
 		
 		for(Giveaway ga : giveaways) {
 			if(ga.getWinners().size() == 0 && ga.winner != null) {
 				ga.addWinner(ga.winner);
 				ga.winner = null;
 				log.info("Updated giveaway winner : " + ga.getKey().getId());
 			}
 			
 			if(ga.getDescription().length() == 0 && ga.description != null) {
 				ga.setDescription(ga.description);
 				ga.description = null;
 				log.info("Updated giveaway description : " + ga.getKey().getId());
 			}
 			
 			if(ga.nbCopies == 0) {
 				ga.nbCopies = 1;
 			}
 			
 			if(ga.nbWinners == 0) {
 				ga.nbWinners = 0;
 			}
 			
 			pm.makePersistent(ga);
 		}
 		
 		cursor = JDOCursorHelper.getCursor(giveaways);
 		
 		String cursorString = cursor.toWebSafeString();
 
 		Queue queue = QueueFactory.getQueue("datastore-update");
 		if(giveaways.size() != 0) {
 			queue.add(TaskOptions.Builder.withUrl("/task/gaupdate1").param("cursor", cursorString));
 		}
 		
 		pm.close();
 	}
 	
 }
