 package org.mule.galaxy.impl;
 
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.mule.galaxy.Dao;
 import org.mule.galaxy.script.CronParseException;
 import org.mule.galaxy.script.Script;
 import org.mule.galaxy.script.ScriptJob;
 import org.mule.galaxy.script.ScriptManager;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 
 public class ScriptManagerImplTest extends AbstractGalaxyTest {
     public static CountDownLatch latch = new CountDownLatch(1);
     
     protected ScriptManager scriptManager;
     protected Dao<ScriptJob> scriptJobDao;
 
     @Override
     protected String[] getConfigLocations() {
         return new String[] {
             "/META-INF/applicationContext-core.xml",
             "/META-INF/applicationContext-core-extensions.xml",
             "/META-INF/applicationContext-acegi-security.xml",
             "/META-INF/applicationContext-cache.xml",
             "classpath*:/META-INF/galaxy-applicationContext.xml",
             "/META-INF/applicationContext-test.xml"
         };
     }
     public void testScripts() throws Exception {
         Script script = new Script();
         script.setName("test");
         script.setRunOnStartup(true);
         script.setScript("println 'hello'; org.mule.galaxy.impl.ScriptManagerImplTest.latch.countDown(); 'hello'");
         
         scriptManager.save(script);
         
         assertEquals("hello", scriptManager.execute(script.getScript()));
         
         ScriptJob sj = new ScriptJob();
        sj.setName("test 123");
         sj.setDescription("test");
         sj.setExpression("bad expression");
         sj.setScript(script);
         
         try {
             scriptJobDao.save(sj);
             fail("Expected parse exception");
         } catch (CronParseException e) {
             System.out.println(e.getMessage());
         }
         
         sj.setExpression("* * * ? * *");
         scriptJobDao.save(sj);
        scriptJobDao.save(sj);
         latch.await(10, TimeUnit.SECONDS);
         
         assertEquals(0, latch.getCount());
         
         List<ScriptJob> jobs = scriptJobDao.listAll();
         assertEquals(1, jobs.size());
         
        sj.setName("Foo Bar");
         scriptJobDao.save(sj);
 
         latch = new CountDownLatch(1);
         latch.await(10, TimeUnit.SECONDS);
         assertEquals(0, latch.getCount());
         
         scriptManager.delete(script.getId());
         
         jobs = scriptJobDao.listAll();
         assertEquals(0, jobs.size());
     }
 }
