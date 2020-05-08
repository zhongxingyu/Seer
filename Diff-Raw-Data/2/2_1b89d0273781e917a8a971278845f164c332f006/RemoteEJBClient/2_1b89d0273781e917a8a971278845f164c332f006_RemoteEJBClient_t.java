 package com.adam.jboss.ticketsystem.client;
 
 import javax.naming.*;
 
 import com.adam.jboss.ticketsystem.ejb.TheatreBooker;
 import com.adam.jboss.ticketsystem.ejb.TheatreInfo;
 import com.adam.jboss.ticketsystem.exception.NotEnoughMoneyException;
 import com.adam.jboss.ticketsystem.exception.SeatBookedException;
 import com.adam.jboss.ticketsystem.utils.IOUtils;
 
 /*import org.jboss.ejb.client.ContextSelector;
 import org.jboss.ejb.client.EJBClient;
 import org.jboss.ejb.client.EJBClientConfiguration;
 import org.jboss.ejb.client.EJBClientContext;
 import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
 import org.jboss.ejb.client.StatelessEJBLocator;
 import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;*/
  
 import java.util.*;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class RemoteEJBClient {
	private final static Logger logger = Logger.getLogger(RemoteEJBClient.class.getName()); 
 	private final static Hashtable<String, String> jndiProperties = new Hashtable<String, String>();
 	
 	public static void main(String[] args) throws Exception {
 		Logger.getLogger("org.jboss").setLevel(Level.SEVERE);
 		Logger.getLogger("org.xnio").setLevel(Level.SEVERE);
 
 		testRemoteEJB();
 	}
 
 	private static void testRemoteEJB() throws NamingException {	
 		
 		jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
 		final TheatreInfo info = lookupTheatreInfoEJB();
 		final TheatreBooker book = lookupTheatreBookerEJB();
 		dumpWelcomeMessage();
 		
 		String command = ""; 
 		Future<String> futureResult = null;
 		 
 		while (true){
 			command = IOUtils.readLine("> ");
 			if (command.equals("book")) {
 
 				int seatId = 0;
 
 				try {
 					seatId = IOUtils.readInt("Enter SeatId");
 				} catch (NumberFormatException e1) {
 					logger.info("Wrong seatid format!");
 					continue;
 				}
 
 				try {
 					String retVal = book.bookSeat(seatId-1);
 					System.out.println(retVal);
 
 				} 
 				catch (SeatBookedException e) {
 					logger.info(e.getMessage());
 					continue;
 				} 
 				catch (NotEnoughMoneyException e) {
 					logger.info(e.getMessage());
 					continue;
 				}
 			}
 			else if (command.equals("bookasync")) {
 				String text = IOUtils.readLine("Enter SeatId  ");
 				int seatId = 0;
 					try {
 						seatId = Integer.parseInt(text);
 					} catch (NumberFormatException e1) {
 						// TODO Auto-generated catch block
 						logger.info("Wrong seatid format!");
 						continue;
 					}
 					futureResult = book.bookSeatAsync(seatId-1);
 					logger.info("Booking issued. Verify your mail!"); 
 			} 
 			else if (command.equals("mail")) {
 				if (futureResult == null || (!futureResult.isDone())) {
 					logger.info("No mail received!");
 					continue;
 				}
 				else {
 				try {
 					String result = futureResult.get();
 					logger.info("Last mail received: "+result);
 					 
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				continue;
 				}
 			}
 			else if (command.equals("list")) {
 				logger.info(info.printSeatList().toString());
 				continue;
 			}
 			else if (command.equals("quit")) {
 				logger.info("Bye");
 				break;
 			}
 			else {
 				logger.info("Unknown command "+command);
 
 			}
 		}
 	}
 
 	private static TheatreInfo lookupTheatreInfoEJB() throws NamingException {
 		
 		final Context context = new InitialContext(jndiProperties);
 		return (TheatreInfo) context.lookup("ejb:/ticket-agency-ejb/TheatreInfoBean!com.adam.jboss.ticketsystem.ejb.TheatreInfo");		   
 	}
 	
 	private static TheatreBooker lookupTheatreBookerEJB() throws NamingException {
 
 		final Context context = new InitialContext(jndiProperties);
 		return (TheatreBooker) context.lookup("ejb:/ticket-agency-ejb/TheatreBookerBean!com.adam.jboss.ticketsystem.ejb.TheatreBooker?stateful");		   
 	}
 	
 	public static void dumpWelcomeMessage() {
 		System.out.println("Theatre booking system");
 		System.out.println("=====================================");
 		System.out.println("Commands: book, bookasync, list, mail, quit");
 	}
 }
