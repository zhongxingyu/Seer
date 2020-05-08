 package se.chalmers.dat255.risk.controller;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.model.IProvince;
 import se.chalmers.dat255.risk.view.ProvinceView;
 
import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 public class ProvinceListener extends ClickListener {
 
 	private IGame theGame;
 
 	public ProvinceListener(IGame theGame) {
 		this.theGame = theGame;
 	}
 
 	/**
 	 * If we want to highlight the province that logic can/should be in this
 	 * method.
 	 * 
 	 */
 	@Override
 	public void clicked(InputEvent event, float x, float y) {
 		if (event.getTarget() instanceof ProvinceView) {
			
 			ProvinceView newClick = ((ProvinceView) event.getTarget());
 			IProvince newClickedProvince = newClick.getProvince();
			Gdx.app.log("werf", "provinceListener" + newClickedProvince.getId());
 			if (theGame.getActivePlayer().getId() == theGame
 					.getOwner(newClickedProvince.getId())) {
 				newClick.check();
 				if (theGame.getCurrentPhase() == IGame.Phase.F1
 						|| theGame.getCurrentPhase() == IGame.Phase.FBuild) {
 					newClick.check();
 
 				}
 			}
 			theGame.handleProvinceClick(newClickedProvince);
 		}
 	}
 }
