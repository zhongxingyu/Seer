 package org.mattstep.platform.samples.contact;
 
 import com.google.inject.Injector;
 import com.proofpoint.bootstrap.Bootstrap;
 import com.proofpoint.discovery.client.Announcer;
 import com.proofpoint.discovery.client.DiscoveryModule;
 import com.proofpoint.jmx.JmxHttpModule;
 import com.proofpoint.http.server.HttpServerModule;
 import com.proofpoint.jaxrs.JaxrsModule;
 import com.proofpoint.jmx.JmxModule;
 import com.proofpoint.jmx.http.rpc.JmxHttpRpcModule;
 import com.proofpoint.json.JsonModule;
 import com.proofpoint.log.LogJmxModule;
 import com.proofpoint.log.Logger;
 import com.proofpoint.node.NodeModule;
 import com.proofpoint.tracetoken.TraceTokenModule;
 import org.weakref.jmx.guice.MBeanModule;
 
 public class Main
 {
     private final static Logger log = Logger.get(Main.class);
 
     public static void main(String[] args)
             throws Exception
     {
         Bootstrap app = new Bootstrap(
                 new NodeModule(),
                 new DiscoveryModule(),
                 new HttpServerModule(),
                 new JsonModule(),
                 new JaxrsModule(),
                 new MBeanModule(),
                 new JmxModule(),
                 new JmxHttpModule(),
                 new JmxHttpRpcModule(),
                 new LogJmxModule(),
                 new TraceTokenModule(),
                 new MainModule());
 
         try {
             Injector injector = app.strictConfig().initialize();
             injector.getInstance(Announcer.class).start();
         }
         catch (Throwable e) {
             log.error(e);
             System.exit(1);
         }
     }
 }
