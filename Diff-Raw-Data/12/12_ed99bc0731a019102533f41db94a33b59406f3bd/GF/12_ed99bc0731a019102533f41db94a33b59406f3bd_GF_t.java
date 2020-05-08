 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.bootstrap;
 /*
 
  * To change this template, choose Tools | Templates
 
  * and open the template in the editor.
 
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.BindException;
 import java.net.ServerSocket;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import net.wazari.bootstrap.GF.Config.User;
 import org.glassfish.api.ActionReport;
 import org.glassfish.api.ActionReport.MessagePart;
 import org.glassfish.api.admin.CommandRunner;
 import org.glassfish.api.admin.ParameterMap;
 import org.glassfish.api.deployment.DeployCommandParameters;
 import org.glassfish.api.embedded.ContainerBuilder;
 import org.glassfish.api.embedded.EmbeddedDeployer;
 import org.glassfish.api.embedded.EmbeddedFileSystem;
 import org.glassfish.api.embedded.LifecycleException;
 import org.glassfish.api.embedded.Server;
 
 /**
  *
  * @author pk033
  */
 public class GF {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws LifecycleException, IOException, InterruptedException, Throwable {
         long timeStart = System.currentTimeMillis() ;
         log.warning("Starting WebAlbums GF bootstrap");
 
         Config cfg = Config.load();
         
         log.info(Config.print(cfg)) ;
         File keyfile = new File("keyfile");
         if (keyfile.exists()) {
             log.warning("delete ./keyfile ");
             keyfile.delete();
         }
 
         File earfile = new File(cfg.webAlbumsEAR);
         if (!earfile.exists()) {
             log.log(Level.WARNING, "The earFile {0} doesn't exist ...", cfg.webAlbumsEAR);
             return;
         }
         log.log(Level.WARNING, "Using EAR: {0}", earfile);
 
         try {
             new ServerSocket(cfg.port).close();
             new ServerSocket(cfg.port + 1).close();
         } catch (BindException e) {
             log.log(Level.WARNING, "Port {0} or {1} already in use", new Object[]{cfg.port, cfg.port + 1});
             return;
         }
 
         Server server = null;
         EmbeddedDeployer deployer = null;
         try {
             File installDirGF = new File(cfg.glassfishDIR);
             installDirGF = installDirGF.getCanonicalFile();
             log.log(Level.WARNING, "Using Glassfish FS: {0}", installDirGF);
             
 
             EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
             efsb.autoDelete(false);
             efsb.installRoot(installDirGF, true);
 
             /*EAR not recognized with domain ...*/
             //File instanceGF = new File(cfg.glassfishDIR+"/domains/domain1");
             //efsb.instanceRoot(instanceGF) ;
             EmbeddedFileSystem efs = efsb.build();
 
             server = startServer(cfg.port, efs);
 
             for (User usr : cfg.user) {
                 createUsers(server, usr);
             }
             
             createJDBC_add_Resources(server, cfg.sunResourcesXML);
 
             deployer = server.getDeployer();
             log.info("Deploying ");
             String appName = null;
             DeployCommandParameters params = new DeployCommandParameters();
             appName = deployer.deploy(new File(cfg.webAlbumsEAR), params);
             log.log(Level.INFO, "Deployed {0}", appName);
 
             long loadingTime = System.currentTimeMillis();
             float time = ((float) (loadingTime - timeStart) / 1000);
 
             log.log(Level.INFO, "Ready to server at http://localhost:{0}/WebAlbums3-Servlet after {1}s", new Object[] {Integer.toString(cfg.port), time});
             log.log(Level.INFO, "Connect to http://localhost:{0} to shutdown the server", Integer.toString(cfg.port + 1));
 
             ServerSocket servSocker = new ServerSocket(cfg.port + 1);
             servSocker.accept().close();
             servSocker.close();
 
         } finally {
             if (deployer != null) {
                 deployer.undeployAll();
             }
             if (server != null) {
                 server.stop();
             }
 
             if (keyfile.exists()) {
                 log.warning("delete ./keyfile ");
                 keyfile.delete();
             }
         }
     }
 
     private static Server startServer(int port, EmbeddedFileSystem efs) throws LifecycleException, IOException {
         Server.Builder builder = new Server.Builder("test");
         if (efs != null) {
             builder.embeddedFileSystem(efs);
         }
         builder.logger(true);
         Server server = builder.build();
         server.addContainer(ContainerBuilder.Type.all);
         server.start();
 
         // Specify the port
         server.createPort(port);
 
         return server;
 
     }
 
     private static List<MessagePart> asAdmin(Server server, String command, ParameterMap params) throws Throwable {
         CommandRunner runner = server.getHabitat().getComponent(CommandRunner.class);
         ActionReport report = server.getHabitat().getComponent(ActionReport.class);
         log.log(Level.INFO, "Invoke {0} {1}", new Object[]{command, params});
 
         log.log(Level.INFO, "command \"{0}\" invoked", command);
         if (params == null) {
             runner.getCommandInvocation(command, report).execute();
         } else {
             runner.getCommandInvocation(command, report).parameters(params).execute();
         }
         log.log(Level.INFO, "command finished with {0}", report.getActionExitCode());
 
         if (report.hasFailures()) {
             if (report.getFailureCause() != null) {
                 throw report.getFailureCause();
             } else {
                 throw new Exception(report.getMessage());
             }
         }
 
         return report.getTopMessagePart().getChildren();
     }
 
     private static void createUsers(Server server, User usr) throws Throwable {
         ParameterMap params = new ParameterMap();
 
         params.add("username", usr.name);
         params.add("userpassword", usr.password);
        
        params.add("groups", usr.groups);
        
         
         asAdmin(server, "create-file-user", params);
     }
 
     private static void createJDBC_add_Resources(Server server, String path) throws Throwable {
         if (!new File(path).exists()) throw new IllegalArgumentException(path + " doesn't exists") ;
         ParameterMap params = new ParameterMap();
         params.add("", path );
         asAdmin(server, "add-resources", params);
     }
     private static final Logger log = Logger.getLogger(GF.class.getName());
 
     @XmlRootElement
 
     static class Config {
 
         public static Config loadDefault(File cfgFile) throws JAXBException {
             Config cfg = new Config();
             cfg.sunResourcesXML = "conf/sun-resources.xml" ;
             cfg.glassfishDIR = "./glassfish" ;
             cfg.webAlbumsEAR = "bin/WebAlbums3-ea.ear" ;
             cfg.port = 8080 ;
             cfg.user = new LinkedList<User>() ;
 
             User usr = new User() ;
             usr.name = "Kevin" ;
             usr.password = "" ;
            usr.groups = "Admin:Manager" ;
             cfg.user.add(usr) ;
 
             if (!cfgFile.exists()) {
                 save(cfgFile, cfg);
             }
             
             return cfg ;
         }
 
         public static Config load() throws JAXBException {
             File file = new File("conf/config.xml");
             if (!file.isFile()) {
                 return loadDefault(file) ;
             }
 
             //Create JAXB Context
             JAXBContext jc = JAXBContext.newInstance(Config.class);
             Unmarshaller um = jc.createUnmarshaller();
             Config cfg = (Config) um.unmarshal(file);
 
             return cfg;
         }
 
         public static <T> String print(T xml) throws JAXBException {
             //Create JAXB Context
             JAXBContext jc = JAXBContext.newInstance(Config.class);
 
             //Create marshaller
             Marshaller marshaller = jc.createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 
             StringWriter writer = new StringWriter();
             marshaller.marshal(xml, writer);
 
             return writer.toString();
         }
 
         public static <T> void save(File file, T obj) throws JAXBException {
             //Create JAXB Context
             JAXBContext jc = JAXBContext.newInstance(Config.class);
             //Create marshaller
             Marshaller marshaller = jc.createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 
             marshaller.marshal(obj, file);
         }
         @XmlAttribute
         int port ;
         @XmlElement
         String sunResourcesXML ;
         @XmlElement
         String webAlbumsEAR ;
         @XmlElement
         String glassfishDIR ;
         @XmlElementWrapper(name="users")
         List<User> user ;
 
         static class User {
             @XmlAttribute String name ;
             @XmlAttribute String password ;
            @XmlAttribute String groups ;
         }
     }
 }
