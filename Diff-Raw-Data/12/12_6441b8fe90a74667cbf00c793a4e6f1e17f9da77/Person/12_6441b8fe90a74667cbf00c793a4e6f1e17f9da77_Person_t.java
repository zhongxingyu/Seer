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
 
 package lostsheep.creatures;
 
 import lostsheep.creatures.view.*;
 
 import chlorophytum.story.Story;
 import chlorophytum.mapobject.MapObject;
 import chlorophytum.mapobject.MapObjectView;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.maps.tiled.TiledMapTile;
 
 import java.lang.Math;
 
 public class Person extends MapObject {
     protected float speed = 1;
     
     protected final String name;
     
     protected MapObject followingTarget;
     
     public Person (String nm) {
         super();
         viewData = new PersonViewData(this);
         name = nm;
     }
     
     @Override
     public MapObjectView newView () {
         return new PersonView (viewData);
     }
     
     @Override
     public void update (float dt) {
         super.update(dt);
         
         processTargetFollowing(dt);
         
         if (wantMove.x != 0 || wantMove.y != 0) {
             int angle = ((int) wantMove.angle()/45) * 45;
             float dx = (float) Math.cos(angle*Math.PI/180) * dt * speed;
             float dy = (float) Math.sin(angle*Math.PI/180) * dt * speed;
             
             // check if can move
            if (getTile("walls", dx, dy) == null) {
                 move(dx, dy);
            } else {
                if (getTile("walls", dx, 0) == null) {
                    move(dx*(float)Math.sqrt(2), 0);
                } else if (getTile("walls", 0, dy) == null) {
                    move(0, dy*(float)Math.sqrt(2));
                }
             }
         }
     }
     
     @Override
     public void clicked () {
         Story.instance().trigger(name, null);
     }
     
     /**
      * Follow object target.
      * Person will try to get as close to the object as it can on each update().
      * TODO: should be moved to more generic place
      * TODO: limit this by forbidding object overlaps
      * @param target object to follow; if null, stop following anything
      */
     public void followTarget (MapObject target) {
         followingTarget = target;
     }
     
     protected void processTargetFollowing (float dt) {
         if (followingTarget != null) {
             wantMove.x = followingTarget.position.x - position.x;
             wantMove.y = followingTarget.position.y - position.y;
             
             if (Math.abs(wantMove.x) > 1/256f) {
                 wantMove.x /= Math.abs(wantMove.x);
             } else {
                 wantMove.x = 0;
             }
             if (Math.abs(wantMove.y) > 1/256f) {
                 wantMove.y /= Math.abs(wantMove.y);
             } else {
                 wantMove.y = 0;
             }
         }
     }
 }
