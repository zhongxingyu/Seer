 package se.chalmers.dat255.risk.controller;
 
 import se.chalmers.dat255.risk.model.IProvince;
import se.chalmers.dat255.risk.view.ProvinceView;
 
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 //import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 public class CardListener extends ClickListener {
 	@Override
 	public void clicked(InputEvent event, float x, float y){
		if(event.getTarget() instanceof ProvinceView){
			ProvinceView newClick =  ((ProvinceView)event.getTarget());
			IProvince newClickedProvince = newClick.getProvince();
 			newClick.check();
			theGame.handleProvinceClick(newClickedProvince);
 		}
 	//	Gdx.app.log("trololol", "province clicked");
 	}
 }
