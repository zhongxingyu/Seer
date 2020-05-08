 package de.nordakademie.nakjava.server.shared.serial.stateSpecificInfos;
 
 import java.util.Map;
 
import de.nordakademie.nakjava.gamelogic.shared.WinStrategyInformation;
 
 public class ConfigurationSpecificInformation implements
 		StateSpecificInformation {
 	// TODO to be implemented
 	private Map<String, WinStrategyInformation> winStrategyDescription;
 	private String currentlyChosenWinStrategy;
 	private String currentlyChosenCardDeck;
 
 	public ConfigurationSpecificInformation(
 			Map<String, WinStrategyInformation> winStrategyDescription,
 			String currentlyChosenWinStrategy, String currentlyChosenCardDeck) {
 		this.winStrategyDescription = winStrategyDescription;
 		this.currentlyChosenWinStrategy = currentlyChosenWinStrategy;
 		this.currentlyChosenCardDeck = currentlyChosenCardDeck;
 	}
 
 	public Map<String, WinStrategyInformation> getWinStrategyDescription() {
 		return winStrategyDescription;
 	}
 
 	public String getCurrentlyChosenWinStrategy() {
 		return currentlyChosenWinStrategy;
 	}
 
 	public String getCurrentlyChosenCardDeck() {
 		return currentlyChosenCardDeck;
 	}
 
 }
