 package io.cloudsoft.marklogic;
 
 import brooklyn.entity.basic.AbstractApplication;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.basic.StartableApplication;
 import brooklyn.entity.proxying.BasicEntitySpec;
 import brooklyn.entity.proxying.EntitySpecs;
 import brooklyn.launcher.BrooklynLauncher;
 import brooklyn.util.CommandLineUtil;
 import com.google.common.collect.Lists;
 
 import java.util.List;
 
 /**
  * App to create a MarkLogic cluster (in a single availability zone).
  * <p/>
  * This can be launched by either:
  * <ul>
  * <li>Running the main method
  * <li>Running {@code export BROOKLYN_CLASSPATH=$(pwd)/target/classes; brooklyn launch --app io.cloudsoft.marklogic.MarkLogicApp}
  * </ul>
  */
 public class MarkLogicApp extends AbstractApplication {
 
     MarkLogicCluster cluster;
 
     @Override
     public void init() {
         cluster = (MarkLogicCluster) addChild(getEntityManager().createEntity(BasicEntitySpec.newInstance(MarkLogicCluster.class)
                .configure(MarkLogicCluster.INITIAL_SIZE, 1)));
     }
 
     /**
      * Launches the application, along with the brooklyn web-console.
      */
     public static void main(String[] argv) throws Exception {
         List<String> args = Lists.newArrayList(argv);
         String port = CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
         String location = CommandLineUtil.getCommandLineOption(args, "--location", "named:marklogic-us-east-1");
 
         BrooklynLauncher launcher = BrooklynLauncher.newInstance()
                 .application(EntitySpecs.appSpec(MarkLogicApp.class))
                 .webconsolePort(port)
                 .location(location)
                 .start();
 
         StartableApplication app = (StartableApplication) launcher.getApplications().get(0);
         Entities.dumpInfo(app);
 
         LOG.info("Press return to shut down the cluster");
         System.in.read(); //wait for the user to type a key
         app.stop();
     }
 }
