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
 
 package chlorophytum.mapobject;
 
import chlorophytum.map.view.ChloroMapStage;

import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g2d.*;
 import com.badlogic.gdx.scenes.scene2d.*;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * Represents renderer for map objects
  */
 public class MapObjectView extends Actor {
     protected static boolean inited = false;
     
     /**
      * Load all required textures, etc
      */
     public static void init () {
         inited = true;
     }
     
     protected MapObjectViewData data;
     
     public MapObjectView (MapObjectViewData d) {
         data = d;
     }
 
     @Override
     public void act (float dt) {
         data.update(dt);
        Vector2 pos = data.position().cpy();
        Vector2 size = data.size().cpy();
        
        ChloroMapStage mapStage = (ChloroMapStage) getStage();
        if (mapStage != null) {
            pos.sub(mapStage.getCamPosition()).add(mapStage.tilesOnMap().scl(0.5f));
        } else {
            Gdx.app.error("MapObjectView.act", "not on ChloroMapStage, continuing with wrong coords");
        }
         setPosition(pos.x, pos.y);
         setSize(size.x, size.y);
     }
 }
