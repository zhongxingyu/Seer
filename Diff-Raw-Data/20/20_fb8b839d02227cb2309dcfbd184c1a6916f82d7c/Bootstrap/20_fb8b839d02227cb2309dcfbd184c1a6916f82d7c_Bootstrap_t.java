 package jobs;
 
 import models.Author;
 import play.db.jpa.JPABase;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.libs.Crypto;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	public void doJob() {
 		// Set loadTestData to true if you want to load test data. CAUTION: All
 		// existing data will be deleted!
		Boolean loadTestData = true;
 
 		if (loadTestData) {
 			Fixtures.deleteAll();
 			Fixtures.load("initial-data.yml");
 
 			for (JPABase a : Author.findAll()) {
 				Author aut = (Author) a;
 				aut.password = Crypto.passwordHash(aut.password);
 				aut.save();
 			}
 
 			Author a = Author.find("byName", "Alexander Striffeler").first();
 			a.password = Crypto.passwordHash("1234");
 			a.save();
 		}
 		Author admin = new Author("admin");
 		admin.password = Crypto.passwordHash("admin");
 		admin.save();
 
 	}
 }
