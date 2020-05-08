 package models;
 
  
 import java.io.File;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import play.test.UnitTest;
 import util.TestHelper;
 
 public class RepoTest extends UnitTest {
 	
 	@BeforeClass
 	public static void init() {
 		AppProps.DATA_FOLDER.mkdirs();
 	}
 
 	@Test 
 	public void testRepoDefault() {
 		String RID = "test-" + TestHelper.randomHashString();
 		Repo repo = new Repo(RID);
 		
 		assertEquals( RID, repo.rid );
 		assertEquals( RID, repo.getFile().getName() );
 		
 	}
 	
 	@Test 
 	public void testRepoCreate() {
 		String RID = "test-" + TestHelper.randomHashString();
 		Repo repo = new Repo(RID,true);
 		
 		assertEquals( RID, repo.rid );
 		assertEquals( RID, repo.getFile().getName() );
 		assertTrue( repo.fFolder.exists() );
 		assertTrue( repo.fMarker.exists() );
 		assertFalse( repo.fLock.exists() );
 		assertEquals( Status.READY, repo.getStatus() );
 		
 	}
 	
 	@Test
 	public void testCopy() {
 		Repo repo = new Repo( TestHelper.randomHashString() );
 		Repo copy = new Repo(repo);
 		
 		assertEquals( copy, repo );
 		assertEquals( copy.hashCode(), repo.hashCode() );
 	}
 
 	@Test
 	public void testFindAll() {
 		/* clean all folder */
 		File[] files = AppProps.DATA_FOLDER.listFiles();
 		for( File file : files ) if( file.isDirectory() ) {
 			FileUtils.deleteQuietly(file);
 		}
 
 		/* now tests */
 		Repo repo1 = new Repo("test-" + TestHelper.randomHashString(), true);
 		Repo repo2 = new Repo("test-" + TestHelper.randomHashString(), true);
 		Repo repo3 = new Repo("test-" + TestHelper.randomHashString(), false); // <-- does not crete it on fail system 
 		
 		List<Repo> repos = Repo.findByStatus(Status.READY);
 		assertTrue( repos.contains(repo1) );
 		assertTrue( repos.contains(repo2) );
 		assertFalse( repos.contains(repo3) );
 	}
 	
 	@Test 
 	public void testDrop() throws Exception {
 
 		Repo repo = new Repo("test-drop",true);
 		assertTrue( repo.fFolder.exists() );
 		
 		/*
 		 * TODO improve this test locking a file a forcing the deletion
 		 */
 		repo.drop(true);
 		assertFalse( repo.fFolder.exists() );
 	}
 	
 	public void testExpiredStatus() {
 		Repo repo = new Repo( "test-expired", true );
 		assertFalse( repo.isExpired() );
 
 		repo.touch( System.currentTimeMillis() - (AppProps.instance().getRequestTimeToLive()+10)*1000 );
 		assertTrue( repo.isExpired() );
 		
 	} 
 	
 	
 	static void assertBetween( long min, long max, long value ) {
 		assertTrue( value>=min && value<=max );
 	}
 	
 	@Test 
 	public void testCreationDate() {
 		long before = System.currentTimeMillis() /1000;
 		Repo repo = new Repo("test-creation-date", true);
 		assertBetween ( before, System.currentTimeMillis() /1000, repo.getCreationTime() /1000 );
 	}
 
 	@Test
 	public void testLastAccessedDate() {
 
 		long before = System.currentTimeMillis();
 		Repo repo = new Repo("test-accessed-date", true);
 		long ctime = repo.getCreationTime();
 		long atime = repo.getLastAccessedTime();
 		long now = System.currentTimeMillis();
 		
 		assertBetween ( before /1000, now /1000, atime /1000);
 
 
 		/* wait a second */
 		try {
 			Thread.currentThread().sleep(1100);
 		} catch (InterruptedException e) { }
 		
 		/* test again */
 		Repo test = new Repo("test-accessed-date", false);
 		assertEquals( ctime, test.getCreationTime() );
 		assertTrue( atime < test.getLastAccessedTime() );
 		assertTrue( test.getLastAccessedTime() < System.currentTimeMillis() );
 		
 	}
 	
 	@Test 
 	public void testExpirationTime() {
 		Repo repo = new Repo("test-expireation-time", true);
 		try {
 			repo.getExpirationTime();
 			fail(); // must fail because #getExpirationTime() is not supported in this state
 		} catch( Exception e ) { }
 		
 		/* in any case the following test is false */
 		assertFalse( repo.isExpired() );
 		
 		
 		/* create a valid result */
 		OutResult result = new OutResult();
 		result.status = Status.DONE;
 		repo.saveResult(result);
 		
 		assertFalse( repo.isExpired() );
 		long atime = repo.getLastAccessedTime() ;
 		long etime = repo.getExpirationTime();
 		long ttl = AppProps.instance().getRequestTimeToLive() *1000;
 		assertEquals( atime + ttl, etime );
 		assertFalse( repo.isExpired() );		
 		
 		/* force expiration */
 		repo.touch( atime - ttl -1000 );
 		assertTrue( repo.isExpired() );
 		
		/* set as FAILED */
		repo.touch(atime);
		result.status = Status.FAILED;
		repo.saveResult(result);
 
		long ctime = repo.getCreationTime();
		ttl = 30 * 1000;
		etime = repo.getExpirationTime();
		
		assertEquals( ctime +ttl, etime );
		assertFalse(repo.isExpired());
		
		/* force expiration */
		repo.fFolder.setLastModified(ctime - ttl -1000 );
		assertTrue( repo.isExpired() );
		
		
 		
 	}
 	
 }
