 /*
  *  Copyright (C) 2013 caryoscelus
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Additional permission under GNU GPL version 3 section 7:
  *  If you modify this Program, or any covered work, by linking or combining
  *  it with Clojure (or a modified version of that library), containing parts
  *  covered by the terms of EPL 1.0, the licensors of this Program grant you
  *  additional permission to convey the resulting work. {Corresponding Source
  *  for a non-source form of such a combination shall include the source code
  *  for the parts of Clojure used as well as that of the covered work.}
  */
 
 package chlorophytum.map.view;
 
 import chlorophytum.map.ChloroMap;
 import chlorophytum.mapobject.MapObject;
 import chlorophytum.mapobject.MapObjectView;
 
 import com.badlogic.gdx.*;
 import com.badlogic.gdx.maps.tiled.renderers.*;
 import com.badlogic.gdx.scenes.scene2d.*;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.math.*;
 import com.badlogic.gdx.utils.*;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.HashMap;
 
 /**
  * Stage for ChloroMap rendering and interaction
  */
 public class ChloroMapStage extends Stage {
     protected OrthogonalTiledMapRenderer renderer;
     protected OrthographicCamera camera;
     
     protected float tileSize;
    protected float tilesNX;
    protected float tilesNY;
     
     protected ChloroMap map;
     
     protected HashMap<MapObject,MapObjectView> objectViews = new HashMap();
     
     /**
      * init.
      * Note that tSize should eventually be replaced with width and height
      * @param tSize float tile size
      * @param tnX float number of tiles per x
      * @param tnY float number of tiles per y
      */
     public void init (float tSize, float tnX, float tnY) {
         tileSize = tSize;
         tilesNX = tnX;
         tilesNY = tnY;
         
         renderer = new OrthogonalTiledMapRenderer(null, 1 / tileSize);
         
         camera = new OrthographicCamera();
         camera.setToOrtho(false, tilesNX, tilesNY);
         camera.update();
     }
     
    public Vector2 tilesOnMap () {
        return new Vector2(tilesNX, tilesNY);
    }
    
     /**
      * 
      */
     public void mapChange () {
         Set<MapObject> myOnly = ((HashMap<MapObject,MapObjectView>) objectViews.clone()).keySet();
         myOnly.removeAll(map.objects);
         HashSet<MapObject> mapOnly = (HashSet<MapObject>) map.objects.clone();
         mapOnly.removeAll(objectViews.keySet());
         
         for (MapObject obj : myOnly) {
             objectViews.get(obj).remove();
             objectViews.remove(obj);
         }
         
         for (MapObject obj : mapOnly) {
             MapObjectView view = obj.newView();
             objectViews.put(obj, view);
             addActor(view);
         }
     }
     
     /**
      * Set new map to display
      * @param newMap new ChloroMap
      */
     public void setMap (ChloroMap newMap) {
         map = newMap;
         renderer.setMap(map.map);
     }
     
     /**
      * Set camera position
      * @param newPos new position (in tiles)
      */
     public void setPosition (Vector2 newPos) {
         camera.position.x = newPos.x;
         camera.position.y = newPos.y;
     }
     
     /**
      * Get position of camera.
      * (convert Vector3 to Vector2)
      * I don't know why it's required to substract 1 from it :/
      * @return Vector2 camera position
      */
     public Vector2 getCamPosition() {
         return new Vector2(camera.position.x-1, camera.position.y-1);
     }
     
     @Override
     public void draw () {
         camera.update();
         
         renderer.setView(camera);
         renderer.render();
         
         super.draw();
     }
     
     @Override
     public void act (float dt) {
         mapChange();
         super.act(dt);
     }
 }
