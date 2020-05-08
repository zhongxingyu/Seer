 package se.chalmers.dat255.risk.controller;
 
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.view.ChangePhase;
 
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 public class ChangePhaseListener extends ClickListener{
 
 	IGame theGame;
 	
 	public ChangePhaseListener(IGame theGame){
 		this.theGame = theGame;
 	}
 	
 	@Override
 	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 		if(event.getTarget() instanceof ChangePhase){
 			((ChangePhase) event.getTarget()).check();
 		}
 		return super.touchDown(event, x, y, pointer, button);
 	}
 	
 	@Override
 	public void clicked(InputEvent event, float x, float y){
 		if(event.getTarget() instanceof ChangePhase){
 			ChangePhase newClick =  ((ChangePhase) event.getTarget());
 			newClick.check();
			theGame.handlePhaseClick();
 		} 
 	}
 }
