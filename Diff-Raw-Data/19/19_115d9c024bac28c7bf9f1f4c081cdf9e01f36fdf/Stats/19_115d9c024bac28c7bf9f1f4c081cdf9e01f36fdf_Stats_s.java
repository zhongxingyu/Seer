 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 import javax.management.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Map;
 import java.io.IOException;
 
 public class Stats {
 
     private ConcurrentHashMap<String, ConnectorHolder> connectors = new ConcurrentHashMap<String, ConnectorHolder>();
     private static final long TIMEOUT = 600000L;
 
     public Stats() {
         new Timer().scheduleAtFixedRate(new TimerTask() {
             public void run() {
                 for (Map.Entry<String, ConnectorHolder> entry : connectors.entrySet()) {
                     final ConnectorHolder holder = entry.getValue();
                     if (holder.lastRead + TIMEOUT < System.currentTimeMillis()) {
                         connectors.remove(entry.getKey());
                         try {
                             holder.conn.close();
                         } catch (IOException e) {
                             e.printStackTrace(System.err);
                         }
                     }
                 }
             }
         }, TIMEOUT, TIMEOUT);
     }
 
    public Object fetch(String server, int port, String mbean, String attr) throws IOException, MalformedObjectNameException, ReflectionException, InstanceNotFoundException, MBeanException, AttributeNotFoundException {
         String service = new StringBuilder("service:jmx:rmi:///jndi/rmi://").append(server).append(':').append(port).append("/jmxrmi").toString();
 
         ConnectorHolder holder = connectors.get(service);
         if (holder == null) {
             holder = new ConnectorHolder(JMXConnectorFactory.connect(new JMXServiceURL(service)));
             connectors.put(service, holder);
         }
 
        MBeanServerConnection connection = holder.conn.getMBeanServerConnection();
        ObjectName objectName = new ObjectName(mbean);
        return connection.getAttribute(objectName, attr);
     }
 
     public void destroy() {
         for (ConnectorHolder holder : connectors.values()) {
             try {
                 holder.conn.close();
             } catch (IOException e) {
                 e.printStackTrace(System.err);
             }
         }
     }
 
     private static final class ConnectorHolder {
         private final JMXConnector conn;
         private long lastRead;
 
         public ConnectorHolder(JMXConnector conn) {
             this.conn = conn;
            this.lastRead = System.currentTimeMillis();
         }
     }
 }
