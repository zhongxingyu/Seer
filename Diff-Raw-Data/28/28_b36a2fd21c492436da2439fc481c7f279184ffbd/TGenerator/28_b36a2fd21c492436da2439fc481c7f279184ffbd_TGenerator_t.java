 package BreakOut_Level;
 
 import java.util.ArrayList;
 
 import BreakOut_GameObjects.TBrick;
 import BreakOut_GameObjects.TEdge;
 import BreakOut_GameObjects.TGameObject;
 
 import com.badlogic.gdx.Gdx;
 
 public class TGenerator {
 	public static ArrayList<TGameObject> Level1(){
 		ArrayList<TGameObject> obj = new ArrayList<TGameObject>();
 		
 		float EdgeWidth = 10;
		//obj.add(new TEdge(0, 0, EdgeWidth, Gdx.graphics.getHeight()));
		//obj.add(new TEdge(Gdx.graphics.getWidth()-EdgeWidth, 0, EdgeWidth, Gdx.graphics.getHeight()));
		//obj.add(new TEdge(0, Gdx.graphics.getHeight()-EdgeWidth, Gdx.graphics.getWidth(), EdgeWidth));
 		
 		float w = 39;
 		float h = 39;
 		
 		//Generate Blocks for level 1
 		for (int i=0; i<20; i++){
 			for (int j=1; j<6; j++){
 				obj.add(new TBrick(EdgeWidth+i*w, Gdx.graphics.getHeight()-EdgeWidth-j*h, w, h));
 			}
 		}
 		return obj;
 		
 	}
 
 }
