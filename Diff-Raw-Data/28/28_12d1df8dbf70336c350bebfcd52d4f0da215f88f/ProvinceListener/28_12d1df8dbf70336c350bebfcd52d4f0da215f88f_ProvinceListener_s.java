 package se.chalmers.dat255.risk.controller;
 
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.model.IProvince;
import se.chalmers.dat255.risk.view.ProvinceView;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 public class ProvinceListener extends ClickListener {
 
 	IProvince oldClickedProvince;
 	IProvince movingFrom;//Used for F3 while moving troops
 	IGame	theGame;
	boolean movingTroops;//If in F3 and already choosed from where to where to move troops
 	@Override
 	public void clicked(InputEvent event, float x, float y){
 		if(event.getTarget() instanceof ProvinceView){
 			ProvinceView newClick =  ((ProvinceView)event.getTarget());
 			IProvince newClickedProvince = newClick.getProvince();
 			
 			newClick.check();
 	//		newClick.addUnits();
 			//if(F1)
 //			((ProvinceView)event.getTarget()).addUnits();
			if(movingTroops){
 				if(newClickedProvince==oldClickedProvince){
 					theGame.moveToProvince(1, movingFrom, oldClickedProvince); //I nulget flyttas endast en trupp
 				}
				else{
 					movingTroops=false;
 					movingFrom=null;
 					oldClickedProvince=null;
				}
			}
			else if(oldClickedProvince!=null){
 					if(oldClickedProvince!=newClickedProvince){
 						if(theGame.moveToProvince(1, oldClickedProvince, newClickedProvince)){
							movingTroops=true;
 						} //I nulget flyttas endast en trupp
 					}
 			}
 			oldClickedProvince=newClickedProvince;
 		}
		Gdx.app.log("trololol", "province clicked");
 		
 	}
 
 }
