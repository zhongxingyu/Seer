 package hudson.plugins.network_monitor;
 
 import hudson.Plugin;
 import hudson.node_monitors.NodeMonitor;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class PluginImpl extends Plugin {
     @Override
     public void start() throws Exception {
        NodeMonitor.LIST.load(NameResolutionMonitor.class);
     }
 }
