 package hudson.plugins.URLSCM;
 
 import hudson.Plugin;
 import hudson.scm.SCMS;
 
 /**
 * Entry point of a plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @author Kohsuke Kawaguchi
  * @Plugin
  */
 public class PluginImpl extends Plugin {
     public void start() throws Exception {
        // plugins normally extend Hudson by providing custom implementations
        // of 'extension points'. In this example, we'll add one builder.
        //BuildStep.BUILDERS.add(HelloWorldBuilder.DESCRIPTOR);
         SCMS.SCMS.add(URLSCM.DescriptorImpl.DESCRIPTOR);
     }
 }
