 package org.ow2.play.test;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ontoware.rdf2go.model.Model;
 import org.openrdf.rdf2go.RepositoryModelSet;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.sail.memory.MemoryStore;
 
 import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_eventadapter.AbstractSenderRest;
 
 public class OverallSimulator {
 
 	private final Map<String, SailRepository> sesameRepository = new HashMap<String, SailRepository>();
 	public Map<String, RepositoryModelSet> sesame = new HashMap<String, RepositoryModelSet>();
 	/**
 	 * The company identifiers are used verbatim as directory names below {@code db/...} and as event types in the produced RDF.
 	 */
 	private final String[] companies = new String[] { "apple", "microsoft", "google", "yahoo" };
 	public final Map<String, TweetSimulator> simulator = new HashMap<String, TweetSimulator>();
 	private static final long DELAY =  Long.parseLong(Constants.getProperties("overall-simulator.properties").getProperty(
 			"delay.between.sending.events"));
 	private static final int MAX_ROUNDS =  Integer.parseInt(Constants.getProperties("overall-simulator.properties").getProperty(
 			"max.number.of.rounds"));
 	
 	public static void main(String[] args) {
 		final OverallSimulator sim = new OverallSimulator();
 		final AbstractSenderRest sender = new AbstractSenderRest(Stream.TwitterFeed.getTopicQName());
 		
 		
 		sim.init();
 		
 		Runtime.getRuntime().addShutdownHook(new Thread(){
 		    @Override
 			public void run() {
 		        System.out.println("Shutdown hook was invoked. Shutting down...");
 		        try {
 					sim.destroy();
 				} catch (Exception e) {
 					System.out.println(e.getMessage());
 				}
 		    }
 		});
 
 		
 		boolean proceed = true;
 		int i = 0;
 		
 		while (proceed) {
 			i++;
 			for (TweetSimulator t : sim.simulator.values()) {
 				if (t.hasNext()) {
 					Model m = t.next();
 					System.out.println("==============================================================================================");
 					System.out.println(m.getContextURI());
 					sender.notify(m);
 					m.close();
 					try {
 						Thread.sleep(DELAY);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				else {
					// There is no next loop if at least one simulator is out of stored events
 					proceed = false;
 				}
 			}
 			if (i >= MAX_ROUNDS) {
 				// There is no next loop if the count of rounds is exhausted
 				proceed = false;
 			}
 		}
 
 		sim.destroy();
 	}
 
 	public void init() {
 
 		for (String company : companies) {
 			
 			try {
 				File dataDir = new File(String.format("./db/%s/", company));
 
 				MemoryStore memStore = new MemoryStore(dataDir);
 				memStore.setSyncDelay(1000L);
 				
 				sesameRepository.put(company, new SailRepository(memStore));
 				sesameRepository.get(company).initialize();
 
 				sesame.put(company, new RepositoryModelSet(sesameRepository.get(company)));
 				sesame.get(company).open();
 				
 				simulator.put(company, new TweetSimulator(sesame.get(company), company));
 
 			} catch (RepositoryException e) {
 				Logger.getAnonymousLogger().log(Level.WARNING,
 						"Problem while initializing Sesame storage.", e);
 			}
 
 		}
 
 	}
 
 	public void destroy() {
 
 		for (RepositoryModelSet modelSet : sesame.values()) {
 			modelSet.close();
 		}
 		for (SailRepository repo : sesameRepository.values()) {
 			try {
 				repo.shutDown();
 			} catch (RepositoryException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
