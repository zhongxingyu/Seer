 package org.cvut.wa2.projectcontrol.DAO;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import org.cvut.wa2.projectcontrol.entities.CompositeTask;
 import org.cvut.wa2.projectcontrol.entities.DocumentEntity;
 import org.cvut.wa2.projectcontrol.entities.Status;
 import org.cvut.wa2.projectcontrol.entities.Task;
 import org.cvut.wa2.projectcontrol.entities.Team;
 import org.cvut.wa2.projectcontrol.entities.TeamMember;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 public class TaskDAO {
 
	
 	
 	
 	public static DocumentEntity addDoc(String taskName, String docName, String href) {
 		
 		DocumentEntity de = null;
 		
 		CompositeTask task = null;
 		
 		PersistenceManager pm = PMF.get();
 		
 		try {
 			task = pm.getObjectById(CompositeTask.class, taskName);
 		} catch (JDOObjectNotFoundException e) {
 			return null;
 		}
 		
 		try {
 			de = pm.getObjectById(DocumentEntity.class, docName);
 		} catch (JDOObjectNotFoundException e) {
 			de = new DocumentEntity();
 			Key key1 = KeyFactory.createKey(DocumentEntity.class.getSimpleName(),docName);
 			de.setDocKey(key1);
 			de.setDocName(docName);
 			de.setHref(href);
 		}
 		
 		
 		pm.makePersistent(de);
 		pm.makePersistent(task);
 		
 		return de;
 	}
 	
 	
 	public static List<CompositeTask> getTasks() {
 		
 		Query q = null;
 		List<CompositeTask> tasks = null;
 		
 		try {
 			PersistenceManager pm = PMF.get();
 			q  = pm.newQuery(CompositeTask.class);
 			tasks = (List<CompositeTask>) q.execute();
 		
 		} finally {
 			q.closeAll();
 		}
 		return tasks;
 	}
 	
 	
 	
 	public static CompositeTask createCompositeTask(String taskName, Date dateOfStartDelivery, String teamName,
 			String owner) {
 		
 		if (taskName == null) return null;
 		
 		taskName = taskName.trim();
 		
 		if (taskName.length() == 0) return null;
 		
 		Team team = TeamDAO.getTeam(teamName);
 		
 		if (team == null) return null;
 		
 		//Key key3 = KeyFactory.createKey(CompositeTask.class.getSimpleName(),taskName);
 		
 		CompositeTask ct = new CompositeTask();
 		ct.setOwner(team.getName());
 		ct.setSubtasks(new ArrayList<Task>());
 		ct.setTaskName(taskName);
 		ct.setDateOfStartDelivery(dateOfStartDelivery);
 		ct.setDocEntity(new ArrayList<DocumentEntity>());
 		
 		PersistenceManager manager = PMF.get();
 		manager.makePersistent(ct);
 		
 		return ct;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 }
