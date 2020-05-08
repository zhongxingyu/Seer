 package se.chalmers.dat255.risk.controller;
 
 import se.chalmers.dat255.risk.model.IProvince;
import se.chalmers.dat255.risk.model.ICard;
import se.chalmers.dat255.risk.model.IGame;
import se.chalmers.dat255.risk.view.CardView;
 
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 //import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 

 public class CardListener extends ClickListener {
	IGame theGame;
 	@Override
 	public void clicked(InputEvent event, float x, float y){
		if(event.getTarget() instanceof CardView){
			CardView newClick =  ((CardView)event.getTarget());
			ICard newClickedCard = newClick.getCard();
 			newClick.check();
			theGame.handleCardClick(newClickedCard);
 		}
 	//	Gdx.app.log("trololol", "province clicked");
 	}
 }
