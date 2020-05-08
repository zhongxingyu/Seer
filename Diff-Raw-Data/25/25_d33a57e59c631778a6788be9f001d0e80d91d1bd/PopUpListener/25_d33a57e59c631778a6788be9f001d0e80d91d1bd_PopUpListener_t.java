 package se.chalmers.dat255.risk.controller;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.view.PopUp;
 
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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
		System.out.println(""+event.getTarget().getClass());
		PopUp pop = (PopUp) event.getListenerActor();
		String title = pop.getTitle();
		String name = null;
		
		// you can press both the button and the label...
 		if (event.getTarget() instanceof Label) {
			name = event.getTarget().getParent().getName();
		} else if (event.getTarget() instanceof Button) {
			name = event.getTarget().getName();
		}
		if (name != null) {
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
			} else if (title.equalsIgnoreCase("Congratz")) {
 				ScreenManager.getInstance().gameOver();
 			}
 		}

 	}
 }
