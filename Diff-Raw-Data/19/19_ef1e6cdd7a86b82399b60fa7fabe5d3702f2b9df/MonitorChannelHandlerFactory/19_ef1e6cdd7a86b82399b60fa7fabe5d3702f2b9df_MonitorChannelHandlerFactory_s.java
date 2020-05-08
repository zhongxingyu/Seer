 package edu.uchicago.monitor;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import org.dcache.xrootd.plugins.ChannelHandlerFactory;
 import org.jboss.netty.channel.ChannelHandler;
 
 public class MonitorChannelHandlerFactory implements ChannelHandlerFactory
 {
     final static String NAME = "edu.uchicago.monitor";    
     final static Set<String> ALTERNATIVE_NAMES = new HashSet<String>(Arrays.asList(NAME));
     private Collector collector;
     
     static boolean hasName(String name)
     {
         return ALTERNATIVE_NAMES.contains(name);
     }
 
     public MonitorChannelHandlerFactory(Properties properties)
     {	String site=properties.getProperty("sitename");
 		String host=properties.getProperty("hostname");
 		String sport=properties.getProperty("port");
 		String sdelay=properties.getProperty("delay");
		site="TEST.uc";
		host="atl-prod05.slac.stanford.edu";
		sport="9931";
		sdelay="15";
    		System.out.println("site: "+site);
    		System.out.println("host: "+host);
    		System.out.println("port: "+sport);
    		System.out.println("delay: "+sdelay);
     	if(site==null||host==null||sport==null){
     		System.err.println("Monitor not intialized properly. Exiting.");
     		System.exit(1);
     	}
     	if (sdelay==null) sdelay="60";
         collector = new Collector(site, host, Integer.parseInt(sport), Integer.parseInt(sdelay));
     }
 
     @Override
     public String getName()
     {
         return NAME;
     }
 
     @Override
     public String getDescription()
     {
         return "Monitor xrootd plugin";
     }
 
     @Override
     public ChannelHandler createHandler()
     {
         return new MonitorChannelHandler(collector);
     }
 }
