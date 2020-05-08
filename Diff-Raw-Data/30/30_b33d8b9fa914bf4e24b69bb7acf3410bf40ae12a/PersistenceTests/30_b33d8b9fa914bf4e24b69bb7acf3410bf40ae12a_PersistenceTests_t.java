 package eu.play_project.dcep.distributedetalis.test;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Random;
 import java.util.UUID;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import eu.play_project.dcep.distributedetalis.persistence.Persistence;
 import eu.play_project.dcep.distributedetalis.persistence.PersistenceException;
 import eu.play_project.dcep.distributedetalis.persistence.Sqlite;
 import eu.play_project.play_commons.constants.Stream;
 import fr.inria.eventcloud.utils.UniqueId;
 
 public class PersistenceTests {
 
 	/**
 	 * Test the persistence mechanism on a temporary file in order not to
 	 * interefere with any productive DCEP instance running on the test machine.
 	 */
 	@Test
 	public void testSqlite() throws PersistenceException, IOException {
		File dbFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "play-dcep-" + new Random().nextInt() + "-test");
 		dbFile.deleteOnExit();
 		Persistence db = new Sqlite(dbFile);
 		db.deleteAllSubscriptions();
 		Assert.assertTrue(db.getSubscriptions().size() == 0);
 		db.storeSubscription(Stream.TwitterFeed.getTopicUri(), UniqueId.encode(UUID.randomUUID()));
 		Assert.assertTrue(db.getSubscriptions().size() == 1);
 		db.deleteAllSubscriptions();
 		Assert.assertTrue(db.getSubscriptions().size() == 0);
 		dbFile.delete();
 	}
 }
