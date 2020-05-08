 package main.java;
 
import java.net.MalformedURLException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Properties;
 
 import javax.jms.Connection;
 import javax.jms.DeliveryMode;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 
 
import org.apache.activemq.ActiveMQConnection;
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.hyperic.sigar.CpuPerc;
 import org.hyperic.sigar.Mem;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 import org.hyperic.sigar.jmx.SigarMem;
 
import com.sun.jndi.toolkit.url.Uri;

 public class MonitorDaemon implements Runnable {
 
 	private volatile boolean running = false;
 	private static Sigar sigar = null;
 	private LinkedList<InfoPacket> recordedData = new LinkedList<InfoPacket>();
 	// TODO thisneeds to be synchronized
 	private static Connection connection = null;
 
 	private String host;
 	private String port;
 	private String clusterName;
 	private String daemonNo;
 	private Properties configProps;
 	
 
 	public MonitorDaemon(String host, String port, String clusterName,
 			String daemonNo) {
 		super();
 		this.host = host;
 		this.port = port;
 		this.clusterName = clusterName;
 		this.daemonNo = daemonNo;
 	}
 	
 	public MonitorDaemon(Properties configProps){
 		  clusterName = configProps.getProperty(MonitorConstants.ConfigProperties.BROKER_CLUSTER);
 		  daemonNo = configProps.getProperty(MonitorConstants.ConfigProperties.BROKER_DAEMON_NUM);
 		  host = configProps.getProperty(MonitorConstants.ConfigProperties.BROKER_HOST);
 		  port = configProps.getProperty(MonitorConstants.ConfigProperties.BROKER_PORT);
 		  System.out.println("MonitorDaemon successfully started with configuration: " + host + ":" + port + " on cluster '" + clusterName + "' and daemon Number: " + daemonNo);
 		  this.configProps = configProps;
 		  this.start();
 	}
 
 	public void start() {
 		if("true".equals(this.configProps.getProperty(MonitorConstants.ConfigProperties.BROKER_ENABLE))){
 			
 			// TODO setup broker config
 			// TODO setup broker connection
 			
 			// Create a ConnectionFactory
			String connectUrl = "failover://tcp://"+host+":"+port;
	        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectUrl);
	        connectionFactory.setUseAsyncSend(true);
 	
 	        // Create a Connection
 	        try {
 				connection = connectionFactory.createConnection();
 				connection.start();
 			} catch (JMSException e) {
 				System.out.println("FATAL ERROR: ActiveMQ encountered a JMSException. Broker Publishing will fail. Stopping monitoring thread.");
 				e.printStackTrace();
 				this.setRunning(false);
 				this.shutdown();
 			}
 		}else{
 			System.out.println("WARNING: Monitor Thread started without setting up a broker connection. Publishing is NOT active.");
 		}
 
 	}
 
 	public void run() {
 		while (running) {
 			InfoPacket curInfo = new InfoPacket();
 			try {
 				Sigar sigar = getSigar();
 				curInfo.setCpuPerc(sigar.getCpuPerc());
 				curInfo.setMemInfo(sigar.getMem());
 				curInfo.setRecDate(new Date());
 				this.printReport(curInfo);
 				recordedData.add(curInfo);
 		        
 
 				if("true".equals(this.configProps.getProperty(MonitorConstants.ConfigProperties.BROKER_ENABLE))){
 					try {
 						//BEGIN SEND DATA 
 						// Create a Session
 		                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 		
 						// Create the destination (Topic or Queue)
 		                Destination destination = session.createQueue(clusterName+":"+daemonNo);//TODO use our topic ...
 		
 		                // Create a MessageProducer from the Session to the Topic or Queue
 		                MessageProducer producer = session.createProducer(destination);
 		                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 		
 		                // Create a messages
 		                String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
 		                Message message = session.createObjectMessage(curInfo);//TODO make sure this is serializable
 		
 		                // Tell the producer to send the message
 		                System.out.println("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
 		                producer.send(message);
 		
 		                // Clean up
 		                session.close();
 		
 		                //END SEND DATA
 			        } catch (JMSException e) {
 						System.out.println("ERROR: Sending encountered a JMSException. Broker Publishing will fail for this message.");
 						e.printStackTrace();
 					}
 				}
 				
 				Thread.sleep((int) (MonitorConstants.SYS_MONITOR_INTERVAL));
 			} catch (UnsatisfiedLinkError e0) {
 				System.out
 						.println("FATAL ERROR: Sigar encountered an unsatisfied link. Logging will fail from here forward. Stopping monitoring thread.");
 				System.out
 						.println("WARNING: Swallowing stack trace !!!");
 				// e0.printStackTrace();
 				// this.setRunning(false);
 			} catch (SigarException e1) {
 				System.out.println("ERROR: SigarException caught, data missing.\n");
 				// e1.printStackTrace();
 			} catch (InterruptedException e) {
 				System.out.println("WARNING: Interrupt caught, shutting down monitor...\n");
 				this.setRunning(false);
 				break;
 			}
 		}
 		this.shutdown();
 	}
 
 	private void printReport(InfoPacket curInfo) {
 		String report = "*** Report BEGIN ***\n";
 		report += "CPU Status: " + this.stringCpu("   ", curInfo.getCpuPerc()) + "\n";
 		report += "Memory Status: " + this.stringMem("   ", curInfo.getMemInfo()) + "\n";
 		report += "*** Report END ***";
 		System.out.println(report);		
 	}
 
 	public void shutdown() {
 		System.out.println("Shutdown in progress.");
 		this.setRunning(false);
 		sigar.close();
 		try {
 			connection.close();
 		} catch (JMSException e) {
 			System.out.println("WARNING: JMSException while closing the connection...\n");
 			e.printStackTrace();
 		}
 		System.out.println("NOTICE: Monitor shutdown complete.\n");
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 
 	public Sigar getSigar() throws SigarException {
 		if (sigar == null) {
 			sigar = new Sigar();
 			// if (false) {
 			// sigar.enableLogging(true);
 			// }
 		}
 		return sigar;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	public String getPort() {
 		return port;
 	}
 
 	public void setPort(String port) {
 		this.port = port;
 	}
 
 	public String getClusterName() {
 		return clusterName;
 	}
 
 	public void setClusterName(String clusterName) {
 		this.clusterName = clusterName;
 	}
 
 	public String getDaemonNo() {
 		return daemonNo;
 	}
 
 	public void setDaemonNo(String daemonNo) {
 		this.daemonNo = daemonNo;
 	}
 
 	private String stringCpu(String prefix, CpuPerc cpu) {
         return new String(prefix +
                            CpuPerc.format(cpu.getUser()) + "\t" +
                            CpuPerc.format(cpu.getSys()) + "\t" +
                            CpuPerc.format(cpu.getWait()) + "\t" +
                            CpuPerc.format(cpu.getNice()) + "\t" +
                            CpuPerc.format(cpu.getIdle()) + "\t" +
                            CpuPerc.format(cpu.getCombined()));
     }
 
 	private String stringMem(String prefix, Mem mem) {
         return new String(prefix +
         				   mem.getActualFree() + "\t" +
         				   mem.getActualUsed() + "\t" +
         				   mem.getFreePercent() + "\t" +
         				   mem.getTotal());
     }
 	
 }
