 package com.ctp.javaone.archiver.plugin;
 
 import javax.enterprise.inject.Any;
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.ctp.javaone.archiver.archive.ArchivingResult;
 import com.ctp.javaone.archiver.archive.ArchivingTask;
 import com.ctp.javaone.archiver.log.LoggerFactory;
 import com.ctp.javaone.archiver.shell.Shell;
 import com.ctp.javaone.test.ThreadContextRule;
 
 @RunWith(Arquillian.class)
 public class ArchiveTest {
 
     @Rule
     public ThreadContextRule threadContext = new ThreadContextRule();
     
     @Inject @Any
     private Archive archive;
 
     @Deployment
     public static JavaArchive createTestArchive() {
         return ShrinkWrap
                 .create(JavaArchive.class, "test.jar")
                 .addClasses(Archive.class, ArchivingTask.class, ArchivingResult.class)
                 .addClasses(Shell.class, LoggerFactory.class)
                 .addAsManifestResource(
                         EmptyAsset.INSTANCE,
                         ArchivePaths.create("beans.xml"));
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void shouldFailWithNullArgument() {
         archive.executeCommand((String[]) null);
     }
     
     @Test
     public void shouldProduceArchiveResult() {
         Result result = archive.executeCommand("pom.xml");
         Assert.assertNotNull(result);
         Assert.assertEquals(Status.SUCCESS, result.getStatus());
     }
 
 }
