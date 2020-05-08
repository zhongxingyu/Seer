 package org.docear.syncdaemon.client;
 
 import org.docear.syncdaemon.Daemon;
 import org.docear.syncdaemon.TestUtils;
 import org.docear.syncdaemon.fileindex.FileMetaData;
 import org.docear.syncdaemon.projects.Project;
 import org.docear.syncdaemon.users.User;
 import org.fest.assertions.Assertions;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Julius
  * Date: 07.06.13
  * Time: 15:06
  */
 public class ListenForUpdatesTest{
     private static final User user = new User("Julius", "Julius-token");
     private Daemon daemon;
     private ClientService clientService;
     private Project project;
     private FileMetaData fileMetaData;
 
     @Before
     public void setUp() {
         daemon = TestUtils.daemonWithService(ClientService.class, ClientServiceImpl.class);
         clientService = daemon.service(ClientService.class);
 
         String pathOfClass = DownloadFileTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
         final String rootPath = pathOfClass + File.separator + "Testprojects" + File.separator + "Project_0";
         final String projectId = "507f191e810c19729de860ea";
         project = new Project(projectId, rootPath, 8);
 
         final String filename = "/rootFile.pptx";
         fileMetaData = new FileMetaData(filename, "", projectId, false, false, 0);
     }
 
     @Test
     @Ignore
     public void testNotUpToDate() {
         final Map<String,Long> projectIdRevisionMap = new HashMap<String, Long>();
         projectIdRevisionMap.put("507f191e810c19729de860ea",0L);
 
        final ListenForUpdatesResponse listenForUpdatesResponse = clientService.listenForUpdates(user,projectIdRevisionMap, null);
         Assertions.assertThat(listenForUpdatesResponse.getNewProjects().size()).isGreaterThan(0);
     }
 }
