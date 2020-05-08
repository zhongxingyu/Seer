 package jmxMultiQuery;
 
 import javax.management.MBeanServerConnection;
 import javax.management.remote.JMXConnector;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * 
  * JMXQuery is used for local or remote request of JMX attributes
  * It requires JRE 1.5 to be used for compilation and execution.
  * Look method main for description how it can be invoked.
  * @author unknown
  * @author Ryan Gravener (<a href="http://ryangravener.com/app/contact">rgravener</a>)
  * @author Per Huss mr.per.huss (a) gmail.com
  * 
  * This plugin was found on code.google.com and was updated to support 
  * multiple queries, objects with spaces, inverse threasholds and other minor enhancements 
  * 
  * @author Patrick.Presto@centurylink.com
  * 
  */
 public class JMXMultiQuery
 {
 	private JMXConnector connector;
 	private static final int RETURN_OK = 0; // 	 The plugin was able to check the service and it appeared to be functioning properly
 	private static final String OK_STRING = "JMX OK -"; 
 	private static final int RETURN_WARNING = 1; // The plugin was able to check the service, but it appeared to be above some "warning" threshold or did not appear to be working properly
 	private static final String WARNING_STRING = "JMX WARNING -"; 
 	private static final int RETURN_CRITICAL = 2; // The plugin detected that either the service was not running or it was above some "critical" threshold
 	private static final String CRITICAL_STRING = "JMX CRITICAL -"; 
 	private static final int RETURN_UNKNOWN = 3; // Invalid command line arguments were supplied to the plugin or low-level failures internal to the plugin (such as unable to fork, or open a tcp socket) that prevent it from performing the specified operation. Higher-level errors (such as name resolution errors, socket timeouts, etc) are outside of the control of plugins and should generally NOT be reported as UNKNOWN states.
 	private static final String UNKNOWN_STRING = "JMX UNKNOWN";
 
     public JMXMultiQuery() {
         //this.out = out;
     }
 
     public int runCommand(String... args) throws Exception
     {
     	Map<String,String> monitorData = new HashMap <String,String>();
         List<Map<String,String>> jmxAttrList = new ArrayList <Map<String,String>>();
         jmxMonitor monitor = new jmxMonitor();
         jmxMonitor mon = null;
         MBeanServerConnection connection = null;
        try {
 			monitor.parseArgs(args);
 			connection = monitor.connect(monitor.getUrl());
 			monitor.check(connection);
 			monitorData = monitor.validate();
 			jmxAttrList.add(monitorData);
        } catch (ParseError e) {
 			e.printStackTrace();
 		} catch(Exception ex) {
            return monitor.report(ex);
        }
        try {
 			List<String> additionalMonitors = monitor.searchForAdditionalMonitors();
 			if (!additionalMonitors.isEmpty()) {
 				for( String m : additionalMonitors) {
					if (mon != null){
						mon = new jmxMonitor(mon,jmxAttrList);
					} else {
						mon = new jmxMonitor(monitor,jmxAttrList);
					}
 					mon.parseArgs(m.split(" "));
 					mon.check(connection);
 					Map<String,String> monData = new HashMap <String,String>(mon.validate());
 					jmxAttrList.add(monData);	
 				}
 			}
 			
 			jmxMonitorGroup monitorGroup = new jmxMonitorGroup(jmxAttrList);
             if (monitorGroup.checkState(RETURN_CRITICAL)) {
             	disconnect();
             	return RETURN_CRITICAL;
             }
             if (monitorGroup.checkState(RETURN_WARNING)) {
             	disconnect();
             	return RETURN_WARNING;
             }
             if (monitorGroup.checkState(RETURN_OK)) {
             	disconnect();
             	return RETURN_OK;
             }
             
 		} catch (ParseError e) {
 			e.printStackTrace();
 		} catch(Exception ex) {
             return mon.report(ex);
         }
         finally {
                 disconnect();
             }
        return RETURN_UNKNOWN;
     }
 
 	private void disconnect() throws IOException
     {
         if(connector!=null)
         {
             connector.close();
         }
 	}
 	
 	public static void main(String[] args) throws IOException
     {
         try {
 			System.exit(new JMXMultiQuery().runCommand(args));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
 }
 
