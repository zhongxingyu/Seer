 package nl.vaya.mobilegame.layer;
 
 import java.util.ArrayList;
 
 import org.cocos2d.layers.CCLayer;
 
 import android.util.Log;
 
 public class TileLayer extends CCLayer {
 
 	protected ArrayList<String> _tiles;
 	
 	public TileLayer(){
 		_tiles = new ArrayList<String>();
		for(int i = 0; i < (100*100); i++){
 			_tiles.add("a");
 			i++;
 		}
 	}
 	
 	public void update(float dt){
 		for(String tile:_tiles){
 			Log.i("datathroug", tile);
 		}
 	}
 }
