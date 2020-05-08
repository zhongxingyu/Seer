 package ui;
 
 //Import phase constants
import static domain.Constants.*;
 import valueobjects.Territory;
 import valueobjects.Player;
 import domain.Game.Phases;
 
 
 public interface UserInterface {
 
 	Territory getTargetCountry(Player activePlayer, Phases placeunits);
 
 	int getAmountUnit(Player activePlayer, Phases placeunits);
 
 	boolean askForAttack(Player activePlayer);
 
 	Territory getOriginatingCountry(Player activePlayer, Phases attack);
 
 	boolean turnInCards();
 	
 }
