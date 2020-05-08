 package se.chalmers.dat255.risk.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.risk.controller.ProvinceListener;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 
 public class ProvinceStage extends Stage {
 	private Table table;
 	private List<ProvinceView> actor;
 
 	public ProvinceStage(int provinces) {
 		actor = new ArrayList<ProvinceView>();
 		
 		for (int i = 0 ; i<provinces ; i++){
 			ProvinceView provinceView = new ProvinceView(new Texture(Gdx.files.internal("Gfx/province.png")));
 			provinceView.addListener(new ProvinceListener());
 			actor.add(provinceView);
 		}
 		
		/*float y = actor.get(0).getHeight();
 		float x = actor.get(0).getWidth();
 		
 		//setPositionFromCetre(actor.get(0),0,0);
 		setPositionFromCetre(actor.get(1),-x,y);
 		setPositionFromCetre(actor.get(2),-x,-y);
 		setPositionFromCetre(actor.get(3),x,-y);
 		
 		table = new Table();
 		this.addActor(table);
		*/
		//correct placements 
		//also we dont need table. it just line things up.
		actor.get(0).setPosition(0, 0);
		actor.get(1).setPosition(getWidth()/2, getHeight()/2);
		actor.get(2).setPosition(0, getHeight()/2);
		actor.get(3).setPosition(getWidth()/2, 0);
 		
 		for (int i = 0 ; i<provinces ; i++){
 			addActor(actor.get(i));
 		}
 		Gdx.input.setInputProcessor(this);
 	}
 	
 	private void setPositionFromCetre(Actor actor,float f1,float f2){
 		actor.setPosition(getWidth()/2 + f1, getHeight()/2 + f1);
 	}
 }
