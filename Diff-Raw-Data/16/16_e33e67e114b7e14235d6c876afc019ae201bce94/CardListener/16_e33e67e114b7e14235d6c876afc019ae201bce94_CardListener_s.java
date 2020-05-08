 package se.chalmers.dat255.risk.controller;
 
 import se.chalmers.dat255.risk.model.ICard;
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.view.CardView;
 
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 //import com.badlogic.gdx.scenes.scene2d.InputListener;
 
 public class CardListener extends ClickListener {
 	IGame theGame;
 
 	public CardListener(IGame theGame) {
 		this.theGame = theGame;
 	}
 
 	@Override
 	public void clicked(InputEvent event, float x, float y) {
 		if (event.getTarget() instanceof CardView) {
 			CardView newClick = ((CardView) event.getTarget());
 			if (newClick.hasCard()) {
 				ICard newClickedCard = newClick.getCard();
 				newClick.check();
				theGame.handleCardClick(newClickedCard);
 			}
 		}
 	}
 }
