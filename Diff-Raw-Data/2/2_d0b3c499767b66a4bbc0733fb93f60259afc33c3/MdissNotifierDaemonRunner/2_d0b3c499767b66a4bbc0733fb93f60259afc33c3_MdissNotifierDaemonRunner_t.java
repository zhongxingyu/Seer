 package org.mdissjava.notifier.daemon;
 
 import javax.jms.JMSException;
 
 public class MdissNotifierDaemonRunner {
 	
 	//OPS
 	static final private String OPT_EMAIL_MIN= "-e";
 	static final private String OPT_EMAIL_MAX= "--email";
 	static final private String OPT_PERSISTENCE_MIN= "-p";
 	static final private String OPT_PERSISTENCE_MAX= "--persistence";
 	
 	//QUEUES & TOPICS
 	static final private boolean QUEUE_EMAIL = true;
 	static final private boolean QUEUE_PERSISTENCE = true;
 	
 	//DESTINATIONS OF QUEUES AND TOPICS
 	static final private String DESTINATION_EMAIL = "notifications_email";
 	static final private String DESTINATION_PERSISTENCE = "notifications_persistence";
 	
 	/**
 	 * @param args
 	 * @throws JMSException 
 	 */
 	public static void main(String[] args) throws JMSException {
 		if (args.length < 1)
 		{
 			usage();
 		}else{
 			
 			String opt = args[0];
 			Daemon daemon = null;
 			if (opt.equals(OPT_EMAIL_MAX) || opt.equals(OPT_EMAIL_MIN)){
 				
				daemon = new EmailDaemon(QUEUE_EMAIL, DESTINATION_EMAIL);
 				
 			}else if(opt.equals(OPT_PERSISTENCE_MAX) || opt.equals(OPT_PERSISTENCE_MIN)){
 				
 				if(args.length == 2){
 					String database = args[1];
 					daemon = new PersistenceDaemon(QUEUE_PERSISTENCE, DESTINATION_PERSISTENCE, database);
 				}
 			}
 			
 			//check if the option has been valid
 			if (daemon != null)
 				daemon.startDaemon();
 			else//no valid option
 				usage();				
 			
 		}
 	}
 
 	private static void usage() {
 		System.out.println("Usage: java -jar MdissNotifier.jar {DAEMON_TYPE} ");
 		System.out.println("Daemon types:");
 		System.out.println("   -e or --email: Notification email sender daemon");
 		System.out.println("   -p {DATABASE} or --persistence {DATABASE}:  Notification persistence daemon");
 		
 	}
 
 }
