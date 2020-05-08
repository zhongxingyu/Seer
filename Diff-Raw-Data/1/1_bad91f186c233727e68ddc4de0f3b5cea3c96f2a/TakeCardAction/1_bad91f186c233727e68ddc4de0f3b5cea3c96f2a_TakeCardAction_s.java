 package actions;
 
 import java.util.*;
 
 import roma.*;
 
 import cards.*;
 
 
 public class TakeCardAction implements PlayerAction {
 	
 	int diceRoll;
 	GameVisor game;
 	
 	public boolean isValid() {
 		
 		boolean isValid = true;
 		
 		if (diceRoll > 6 || diceRoll < 1) {
 			
 			isValid = false;
 			game.getController().showMessage("That Dice Roll is not possible");
 		}
 		
 		boolean found = false;
 		
 		for (int i : game.getDiceRolls()) {
 			
 			if (i == diceRoll) {
 				
 				found = true;
 				
 			}
 			
 		}
 		
 		if (!found) {
 			
 			isValid = false;
 			game.getController().showMessage("You dont have a Dice Roll of that value");
 			
 		}
 		
 		return isValid;
 	
 	}
 	
 	public void execute(GameVisor g) {
 		game = g;
 		List<Card> temp = new ArrayList<Card>();
 		int i = 0;
 		
 		query();
 		
 		if (isValid()) {
 			
 			g.useDice(diceRoll);
 			
 			for (i = 0; i < diceRoll; i++) {
 				
 				temp.add(g.drawCard());
 				
 			}
 			
 			Card selected = g.getController().getCard(temp, "Please select one Card that you want (the rest are discarded)");
 			
 			g.getCurrentPlayer().addCard(selected);
 			temp.remove(selected);
 			
 			for (Card c : temp) {
 				
				temp.remove(c);
 				g.discard(c);
 
 			}
 			
 		} else {
 			
 			g.getController().showMessage("You cannot take Cards");
 			
 		}
 		
 	}
 
 
 	@Override
 	public String getDescription() {
 		return "Take Card";
 	}
 
 	public void query() {
 		
 		game.getController().showDiceRolls();
 		
 		diceRoll = game.getController().getInt("Choose the Dice Roll you want to use");
 		
 	}
 
 	// only visible if we have dice
 	public boolean isVisible(GameVisor g) {
 		boolean hasDice = false;
 		
 		for (int i = 0; i < g.getDiceRolls().length; i++) {
 			if (g.getDiceRolls()[i] != 0) {
 				hasDice = true;
 			}
 		}
 		return hasDice;
 	}
 
 }
