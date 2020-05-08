 package org.ojim.logic.actions;
 
 import org.ojim.logic.ServerLogic;
 import org.ojim.logic.state.Card;
 import org.ojim.logic.state.CardStack;
 
 public class ActionFetchCard implements Action {
 
 	private final ServerLogic logic;
 	private final CardStack stack;	
 
 	public ActionFetchCard(ServerLogic logic, CardStack stack) {
 		this.logic = logic;
 		this.stack = stack;
 	}
 	
 	@Override
 	public void execute() {
		if (this.stack.isEmpty()) {
 			Card topCard = this.stack.getPointedCard();
 			this.stack.step();
 			topCard.fetch();
 		}
 	}
 
 }
