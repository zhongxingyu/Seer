 package org.jabox.applicationcontext;
 
 import org.apache.wicket.persistence.provider.ConfigXstreamDao;
 import org.apache.wicket.persistence.provider.ContainerXstreamDao;
 import org.apache.wicket.persistence.provider.ServerXstreamDao;
 import org.apache.wicket.persistence.provider.UserXstreamDao;
 import org.jabox.cis.ejenkins.EJenkinsConnectorConfig;
 import org.jabox.its.eredmine.ERedmineRepositoryConfig;
 import org.jabox.model.Container;
 import org.jabox.model.DefaultConfiguration;
 import org.jabox.model.Server;
 import org.jabox.model.User;
 import org.jabox.mrm.nexus.NexusConnectorConfig;
 import org.jabox.sas.sonar.SonarConnectorConfig;
 import org.jabox.scm.esvn.ESVNConnectorConfig;
 import org.jabox.utils.LocalHostName;
 import org.mindrot.jbcrypt.BCrypt;
 
 public class InitializeDatabase {
 
     private final DefaultConfiguration dc = ConfigXstreamDao.getConfig();
 
     /**
      * check if database is already populated, if not, populate
      */
     public void init() {
         if (UserXstreamDao.getUsers().size() == 0) {
             createAdminUser();
             createSubversionServer();
             createJenkinsServer();
             createRedmineServer();
             createNexusServer();
             createSonarServer();
             createTomcatContainer();
             ConfigXstreamDao.persist(dc);
         }
     }
 
     private void createTomcatContainer() {
         Container container = new Container();
         container.setName("Default");
         container.setPort("9080");
         container.setRmiPort("9081");
         container.setAjpPort("9082");
         ContainerXstreamDao.persist(container);
     }
 
     private void createSonarServer() {
         SonarConnectorConfig config = new SonarConnectorConfig();
         config.setServer(new Server());
         config.getServer().setName("Sonar");
         config.getServer().setDeployerConfig(config);
         config.getServer().setUrl(
             "http://" + LocalHostName.getLocalHostname() + ":9080/sonar/");
         ServerXstreamDao.persist(config);
         dc.switchDefault(config);
     }
 
     private void createNexusServer() {
         NexusConnectorConfig config = new NexusConnectorConfig();
         config.setServer(new Server());
         config.getServer().setName("Nexus");
         config.setUsername("admin");
         config.setPassword("admin123");
         config.getServer().setDeployerConfig(config);
         config.getServer().setUrl(
             "http://" + LocalHostName.getLocalHostname() + ":9080/nexus/");
         ServerXstreamDao.persist(config);
         dc.switchDefault(config);
     }
 
     private void createJenkinsServer() {
         EJenkinsConnectorConfig config = new EJenkinsConnectorConfig();
         config.setServer(new Server());
         config.getServer().setName("Jenkins");
         config.getServer().setDeployerConfig(config);
         ServerXstreamDao.persist(config);
         dc.switchDefault(config);
     }
 
     private void createRedmineServer() {
         ERedmineRepositoryConfig config = new ERedmineRepositoryConfig();
         config.setUsername("admin");
         config.setPassword("admin");
         config.setServer(new Server());
         config.getServer().setName("Redmine");
         config.getServer().setDeployerConfig(config);
        config.setAddRepositoryConfiguration(true);
         ServerXstreamDao.persist(config);
         dc.switchDefault(config);
     }
 
     private void createSubversionServer() {
         ESVNConnectorConfig config = new ESVNConnectorConfig();
         config.setServer(new Server());
         config.getServer().setName("Subversion");
         config.getServer().setDeployerConfig(config);
         ServerXstreamDao.persist(config);
         dc.switchDefault(config);
     }
 
     private void createAdminUser() {
         User user = new User();
         user.setLogin("admin");
         String hashed = BCrypt.hashpw("admin", BCrypt.gensalt());
         user.setPasswordHash(hashed);
         UserXstreamDao.persist(user);
     }
 }
