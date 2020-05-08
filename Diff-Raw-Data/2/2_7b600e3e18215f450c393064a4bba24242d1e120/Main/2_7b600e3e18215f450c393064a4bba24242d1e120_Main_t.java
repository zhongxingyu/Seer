 /**
  * 
  */
 package org.ow2.play.test.pubsub.subscriber;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.event_processing.events.types.Event;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.Statement;
 import org.ontoware.rdf2go.model.node.Variable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
 
 import eu.play_project.play_commons.constants.Constants;
 
 /**
  * @author chamerling
  * @author stuehmer
  * 
  */
 public class Main {
 
     static PubSubClientServer pubSubClientServer;
 
     static boolean started = false;
 
     /** Seconds to wait for all events to come in */
 	private static int waitForResults = 2*60;
 
 	
 
 	/**
      * args[0] = local IP, args[1] = local (free) port,
      * args[2] = simulation name e.g. p1s1
      * 
      * @param args
      */
     public static void main(String[] args) throws IOException {
 
     	Logger logger = LoggerFactory.getLogger(Main.class);
     	
         if (args == null || args.length < 3) {
             System.err.println("!!! Bad number of arguments");
             usage();
             System.exit(-1);
         }
 
         String host = args[0];
         String port = args[1];
 
         String me = "http://" + host + ":" + port + "/pubsubcli/Service";
 
         String provider = Constants.getProperties().getProperty("dsb.subscribe.endpoint");
         String simulationName = args[2];
         
         if (simulationName.equals("t1p1s1")) {
             pubSubClientServer = new PubSubClientServer(System.out, T1P1S1Consumer.topic, provider, me);
            	Runnable notifier = new T1P1S1Notifier();
            	INotificationConsumer consumer = new T1P1S1Consumer(pubSubClientServer);
          	pubSubClientServer.start(consumer);
            	pubSubClientServer.simulate(notifier);
            	try {
 				Thread.sleep(waitForResults * 1000);
 			} catch (InterruptedException e) {
 			}
            	pubSubClientServer.stop();
            	if (Stats.get().nb == 200) {
            		logger.info("TEST {} true", simulationName);
            	}
            	else if (Stats.get().nb < 200){
            		logger.info("TEST {} false received_less_than_200_complex_events", simulationName);
            	}
            	else {
            		logger.info("TEST {} false received_more_than_200_complex_events", simulationName);
            	}
         }
         else if (simulationName.equals("t1p1s2")) {
         	pubSubClientServer = new PubSubClientServer(System.out, T1P1S2Consumer.topic, provider, me);
        		Runnable notifier = new T1P1S2Notifier();
        		INotificationConsumer consumer = new T1P1S2Consumer(pubSubClientServer);
      		pubSubClientServer.start(consumer);
        		pubSubClientServer.simulate(notifier);
            	try {
 				Thread.sleep(waitForResults * 1000);
 			} catch (InterruptedException e) {
 			}
            	pubSubClientServer.stop();
            	if (Stats.get().nb == 30) {
            		logger.info("TEST {} true", simulationName);
            	}
            	else if (Stats.get().nb < 30){
            		logger.info("TEST {} false received_less_than_30_complex_events", simulationName);
            	}
            	else {
            		logger.info("TEST {} false received_more_than_30_complex_events", simulationName);
            	}
         }
         else if (simulationName.equals("t1p2s1")) {
         	pubSubClientServer = new PubSubClientServer(System.out, T1P2S1Consumer.topic, provider, me);
        		Runnable notifier = new T1P2S1Notifier();
        		INotificationConsumer consumer = new T1P2S1Consumer(pubSubClientServer);
      		pubSubClientServer.start(consumer);
        		pubSubClientServer.simulate(notifier);
            	try {
 				Thread.sleep(waitForResults * 1000);
 			} catch (InterruptedException e) {
 			}
            	pubSubClientServer.stop();
            	if (Stats.get().nb == 30) {
            		logger.info("TEST {} true", simulationName);
            	}
            	else if (Stats.get().nb < 30){
            		logger.info("TEST {} false received_less_than_30_complex_events", simulationName);
            	}
            	else {
            		logger.info("TEST {} false received_more_than_30_complex_events", simulationName);
            	}
         }
         else if (simulationName.equals("overall-receiver")) { // receiver for M36 overall-tests
             pubSubClientServer = new PubSubClientServer(System.out, OverallConsumer.topic, provider, me);
            	Runnable notifier = new T1P1S1Notifier();
            	INotificationConsumer consumer = new OverallConsumer(pubSubClientServer);
          	pubSubClientServer.start(consumer);
            	//pubSubClientServer.simulate(notifier); // no simulations in this test we only want to receive
            	try {
 				Thread.sleep(waitForResults * 1000);
 			} catch (InterruptedException e) {
 			}
            	pubSubClientServer.stop();
 
            	logger.info("TEST {} received {} events", simulationName, Stats.get().nb);
         }
         else {
         	usage();
         }
         
         System.exit(0);
     }
 
     public static final void usage() {
         System.out
                .println("pubsubcli <host> <port> <simulation name e.g. t1p1s1>");
     }
 
 
     public static String getMembers(Model m) {
     	String members = "";
     	Iterator<Statement> i = m.findStatements(Variable.ANY, Event.MEMBERS, Variable.ANY);
     	while (i.hasNext()) {
     		String member = i.next().getObject().toString();
     		int endIndex = member.lastIndexOf(eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX);
     		if (endIndex > 0 ) {
     			member = member.substring(0, endIndex);
     		}
     		members += member + " ";
     	}
     	return members;
     }
 }
