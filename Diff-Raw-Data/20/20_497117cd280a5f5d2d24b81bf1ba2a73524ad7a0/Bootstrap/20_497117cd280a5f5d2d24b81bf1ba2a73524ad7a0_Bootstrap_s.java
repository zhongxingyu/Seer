 package jobs;
 
 import javax.persistence.Query;
 
 import models.Author;
 import play.db.jpa.JPA;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	public void doJob() {
		Query q = JPA.em().createQuery(
				"SELECT x FROM Author x WHERE x.name='admin'");
		if (q.getResultList().isEmpty()) {
 			Author admin = new Author("admin");
 			admin.password = "admin".hashCode();
 			admin.save();
 		}
 	}
 
 }
