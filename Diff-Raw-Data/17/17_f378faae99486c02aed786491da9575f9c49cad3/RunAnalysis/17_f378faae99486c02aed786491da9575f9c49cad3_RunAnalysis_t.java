 package org.chaoticbits.cardshuffling;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.List;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.chaoticbits.cardshuffling.entry.DataEntryException;
 import org.chaoticbits.cardshuffling.entry.EntryCorrection;
 import org.chaoticbits.cardshuffling.entry.ShuffleState;
 import org.chaoticbits.cardshuffling.entry.TrialReader;
 
 public class RunAnalysis {
 
 	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RunAnalysis.class);
 
 	public static void main(String[] args) throws FileNotFoundException, DataEntryException {
 		PropertyConfigurator.configure("log4j.properties");
 		log.info("Loading files...");
		List<ShuffleState> shuffles = new TrialReader().readDeck(new File("data/trial1.1.txt"));
 		shuffles.addAll(new TrialReader().readDeck(new File("data/trial1.2.txt")));
 		shuffles.addAll(new TrialReader().readDeck(new File("data/trial1.3.txt")));
 		shuffles.addAll(new TrialReader().readDeck(new File("data/trial1.4.txt")));
		shuffles.addAll(new TrialReader().readDeck(new File("data/trial1.5.txt")));
		shuffles.addAll(new TrialReader().readDeck(new File("data/trial1.6.txt")));
 		log.info("Checking data entry...");
 		for (ShuffleState shuffleState : shuffles) {
 			log.info("\tChecking..." + shuffleState.getDescription());
 			new EntryCorrection().checkDeckEntry(shuffleState.getDeck());
 		}
 		log.info("Done.");
 	}
 }
