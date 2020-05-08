 package se.chalmers.dat255.risk.controller;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.view.PopUp;
 
import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 public class PopUpListener extends ClickListener {
 
 	IGame model;
 
 	public PopUpListener(IGame model) {
 		this.model = model;
 	}
 
 	@Override
 	public void clicked(InputEvent event, float x, float y) {
 		// I can't for the life of me figure out why I can't reach the buttons
 		// at once
 		if (event.getTarget() instanceof Label) {

			PopUp pop = (PopUp) event.getListenerActor();
			String name = event.getTarget().getParent().getName();
			String title = pop.getTitle();
 			if (title.equalsIgnoreCase("Attack")) {
 				if (name.equals("confirm")) {
 					model.battle((int) pop.getValue());
 				} else if (name.equals("cancel")) {
 					model.flushProvinces();
 				}
 			} else if (title.equalsIgnoreCase("Movement")
 					|| pop.getTitle().equalsIgnoreCase("Occupy")) {
 				if (name.equals("confirm")) {
 					model.moveToProvince((int) pop.getValue());
 				} else if (name.equals("cancel")) {
 					model.flushProvinces();
 				}
 			} else if (title.equalsIgnoreCase("Again?")) {
 				if (name.equals("confirm")) {
 					model.battle((int) pop.getValue());
 				} else if (name.equals("cancel")) {
 					model.flushProvinces();
 				}
			} else if(title.equalsIgnoreCase("Congratz")){
 				ScreenManager.getInstance().gameOver();
 			}
 		}
 	}
 }
