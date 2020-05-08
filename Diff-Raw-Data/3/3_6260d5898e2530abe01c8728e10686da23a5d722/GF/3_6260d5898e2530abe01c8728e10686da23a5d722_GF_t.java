 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.bootstrap;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.Field;
 import java.net.BindException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import net.wazari.bootstrap.GF.Config.User;
 import org.glassfish.api.admin.ParameterMap;
 import org.glassfish.embeddable.CommandResult;
 import org.glassfish.embeddable.Deployer;
 import org.glassfish.embeddable.GlassFish;
 import org.glassfish.embeddable.GlassFishException;
 import org.glassfish.embeddable.GlassFishProperties;
 import org.glassfish.embeddable.GlassFishRuntime;
 import org.glassfish.internal.embedded.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author pk033
  */
 public class GF {
     private static final Logger log = LoggerFactory.getLogger(GF.class.getName());
     
     public static final String DEFAULT_CONFIG_PATH = "conf/config.xml";
     private static final String SHUTDOWN_PORT_PPT = "SHUTDOWN_PORT";
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         try {
             GF glassfish = new GF();
             glassfish.start();
             GF.waitForPortStop(cfg.port + 1);
             glassfish.terminate();
         } catch (Throwable e) {
             e.printStackTrace();
         }
     }
     public static Config cfg;
 
     static {
         try {
             log.debug("Load configuration from '"+DEFAULT_CONFIG_PATH+"'.");
             cfg = Config.load(DEFAULT_CONFIG_PATH);
         } catch (Exception ex) {
             throw new RuntimeException("Couldn't load the configuration file: " + ex.getMessage());
         }
     }
     public GlassFish server = null;
     public Deployer deployer = null;
     public String appName = null;
     public File keyfile = new File("keyfile");
 
     public void start() throws Throwable {
         long timeStart = System.currentTimeMillis();
         log.warn("Starting WebAlbums GF bootstrap");
 
         log.info(cfg.print());
 
         if (keyfile.exists()) {
             log.warn("delete ./keyfile ");
             keyfile.delete();
         }
 
         File earfile = new File(cfg.webAlbumsEAR);
         log.warn("Using EAR: {}", earfile);
         if (!earfile.exists()) {
             log.warn("The earFile {} doesn't exist ...", earfile.getAbsolutePath());
             return;
         }
 
         try {
             new ServerSocket(cfg.port).close();
         } catch (BindException e) {
             log.warn("Port {} already in use", new Object[]{cfg.port});
             return;
         }
 
         File installDirGF = new File("./glassfish").getCanonicalFile();
         EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
         efsb.autoDelete(false);
         efsb.installRoot(installDirGF, true);
 
         /* EAR not recognized with domain ...*/
         //File instanceGF = new File(cfg.glassfishDIR+"/domains/domain1");
         //efsb.instanceRoot(instanceGF) ;
         EmbeddedFileSystem efs = efsb.build();
 
         server = startServer(cfg.port, "./glassfish");
 
         for (User usr : cfg.user) {
             createUsers(server, usr);
         }
 
         createJDBC_add_Resources(server, cfg.sunResourcesXML);
 
         deployer = server.getDeployer();
 
        if (!cfg.root_path.endsWith("/")) {
            cfg.root_path += "/" ;
        }
         log.info("Setting root path: {}", cfg.root_path);
         System.setProperty("root.path", cfg.root_path);
         
         log.info("Setting java library path: {}", cfg.libJnetFs);
         addToJavaLibraryPath(new File(cfg.libJnetFs));
         
         log.info("Deploying EAR: {}", cfg.webAlbumsEAR);
         appName = deployer.deploy(new File(cfg.webAlbumsEAR));
         if (appName == null) {
             log.info("Couldn't deploy ...");
             throw new GlassFishException("Couldn't deploy ...");
         }
         log.info("Deployed {}", appName);
 
         long loadingTime = System.currentTimeMillis();
         float time = ((float) (loadingTime - timeStart) / 1000);
 
         log.info("Ready to server at http://localhost:{}/WebAlbums3.5-dev after {}s", new Object[]{Integer.toString(cfg.port), time});
     }
 
     public void terminate() throws GlassFishException {
         if (deployer != null && appName != null) {
             deployer.undeploy(appName);
         }
         if (server != null) {
             server.stop();
         }
 
         if (keyfile.exists()) {
             log.warn("delete ./keyfile ");
             keyfile.delete();
         }
     }
 
     public static void waitForPortStop(int stopPort) throws IOException {
         System.setProperty(SHUTDOWN_PORT_PPT, Integer.toString(stopPort));
         log.info("Connect to http://localhost:{} to shutdown the server", Integer.toString(stopPort));
 
         ServerSocket servSocker = new ServerSocket(stopPort);
         servSocker.accept().close();
         servSocker.close();
     }
 
     private static GlassFish startServer(int port, String glassfishDIR) throws LifecycleException, IOException, GlassFishException {
         /**
          * Create and start GlassFish which listens at 8080 http port
          */
         GlassFishProperties gfProps = new GlassFishProperties();
         gfProps.setPort("http-listener", port); // refer JavaDocs for the details of this API.
         
         System.setProperty("java.security.auth.login.config",
                 glassfishDIR + "/config/login.conf");
         GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish(gfProps);
 
         glassfish.start();
 
         return glassfish;
     }
 
     private static void asAdmin(GlassFish server, String command, ParameterMap params) throws Throwable {
         org.glassfish.embeddable.CommandRunner runner = server.getCommandRunner();
 
         log.info("Invoke {} {}", new Object[]{command, params});
 
         log.info("command \"{}\" invoked", command);
         ArrayList<String> paramLst = new ArrayList<String>();
         if (params != null) {
             for (String key : params.keySet()) {
                 for (String value : params.get(key)) {
                     if (key.length() != 0) {
                         paramLst.add("--" + key);
                     }
                     paramLst.add(value);
                 }
             }
         }
         String[] paramArray = paramLst.toArray(new String[0]);
         CommandResult result = runner.run(command, paramArray);
         log.info("command finished with {}", result.getExitStatus());
 
         if (result.getFailureCause() != null) {
             throw result.getFailureCause();
         }
 
         //log.info("--> {}", result.getOutput());
     }
 
     private static void createUsers(GlassFish server, User usr) throws Throwable {
         ParameterMap params = new ParameterMap();
         File tmp = File.createTempFile("embGF-", "-Webalbums");
         {
             tmp.createNewFile();
             Writer out = new OutputStreamWriter(new FileOutputStream(tmp));
             out.append("AS_ADMIN_USERPASSWORD=" + usr.password);
             out.close();
         }
         params.add("passwordfile", tmp.getAbsolutePath());
         params.add("groups", usr.groups);
         params.add("", usr.name);
 
         asAdmin(server, "create-file-user", params);
         tmp.delete();
     }
 
     private static void createJDBC_add_Resources(GlassFish server, String path) throws Throwable {
         if (!new File(path).exists()) {
             throw new IllegalArgumentException(path + " doesn't exists");
         }
 
         ParameterMap params = new ParameterMap();
         params.add("", path);
         asAdmin(server, "add-resources", params);
     }
 
     @XmlRootElement
     static class Config {
 
         public static Config loadDefault(String cfgFilePath) throws Exception {
             Config cfg = new Config();
             cfg.sunResourcesXML = "./conf/sun-resources.xml";
             cfg.libJnetFs = "./lib/libJnetFS.so";
             cfg.webAlbumsEAR = "./bin/WebAlbums3-ea.ear";
             cfg.root_path = "./";
             cfg.port = 8080;
             cfg.user = new LinkedList<User>();
 
             cfg.webAlbumsFS = "./WebAlbums3-FS";
             
             User usr = new User();
             usr.name = "kevin";
             usr.password = "";
             usr.groups = "Admin:Manager";
             cfg.user.add(usr);
 
             cfg.save(cfgFilePath);
 
             return cfg;
         }
 
         public static Config load(String path) throws Exception {
             File file = new File(path);
             if (!file.isFile()) {
                 log.info("Path '"+file.getCanonicalPath()+"' is not a file ...");
                 return loadDefault(path);
             }
 
             //Create JAXB Context
             JAXBContext jc = JAXBContext.newInstance(Config.class);
             Unmarshaller um = jc.createUnmarshaller();
             Config cfg = (Config) um.unmarshal(file);
 
             return cfg;
         }
 
         public String print() throws JAXBException {
             //Create JAXB Context
             JAXBContext jc = JAXBContext.newInstance(Config.class);
 
             //Create marshaller
             Marshaller marshaller = jc.createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 
             StringWriter writer = new StringWriter();
             marshaller.marshal(this, writer);
 
             return writer.toString();
         }
 
         public void save(String path) throws Exception {
             //Create JAXB Context
             JAXBContext jc = JAXBContext.newInstance(Config.class);
             //Create marshaller
             Marshaller marshaller = jc.createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             
             File file = new File(path);
             file.getParentFile().mkdirs();
             marshaller.marshal(this, file);
             log.info("Configuration saved into '"+file.getCanonicalPath()+"'.");
         }
         
         @XmlAttribute
         int port;
         @XmlElement
         String sunResourcesXML;
         @XmlElement
         String webAlbumsEAR;
         @XmlElement
         String webAlbumsFS;
         @XmlElement
         String libJnetFs;
         @XmlElement
         String root_path;
         @XmlElementWrapper(name = "users")
         List<User> user;
 
         static class User {
 
             @XmlAttribute
             String name;
             @XmlAttribute
             String password;
             @XmlAttribute
             String groups;
         }
     }
     
     /**
     * Ajoute un nouveau répertoire dans le java.library.path.
     * @param dir Le nouveau répertoire à ajouter.
     */
     public static void addToJavaLibraryPath(File dir) {
             final String LIBRARY_PATH = "java.library.path";
             if (!dir.isDirectory()) {
                     throw new IllegalArgumentException(dir + " is not a directory.");
             }
             String javaLibraryPath = System.getProperty(LIBRARY_PATH);
             System.setProperty(LIBRARY_PATH, javaLibraryPath + File.pathSeparatorChar + dir.getAbsolutePath());
 
             resetJavaLibraryPath();
     }
     
     
 /**
  * Supprime le cache du "java.library.path".
  * Cela forcera le classloader à revérifier sa valeur lors du prochaine chargement de librairie.
  * 
  * Attention : ceci est spécifique à la JVM de Sun et pourrait ne pas fonctionner
  * sur une autre JVM...
  */
     public static void resetJavaLibraryPath() {
 	synchronized(Runtime.getRuntime()) {
 		try {
 			Field field = ClassLoader.class.getDeclaredField("usr_paths");
 			field.setAccessible(true);
 			field.set(null, null);
 			
 			field = ClassLoader.class.getDeclaredField("sys_paths");
 			field.setAccessible(true);
 			field.set(null, null);
 		} catch (NoSuchFieldException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 	}
     }   
 }
