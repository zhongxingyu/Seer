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
 
 package lostsheep.creatures.view;
 
 import chlorophytum.mapobject.*;
 
 import com.badlogic.gdx.*;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g2d.*;
 import com.badlogic.gdx.math.*;
 
 public class PersonView extends MapObjectView {
     protected static boolean inited = false;
     protected static Animation defaultSprite;
     
     public static void init () {
         if (!inited) {
             Texture texture = new Texture("data/maps/ph-creature.png");
             TextureRegion[][] regions = TextureRegion.split(texture, 32, 32);
             defaultSprite = new Animation(0, regions[0][0]);
         }
         inited = true;
     }
     
     public PersonView (MapObjectViewData d) {
         super(d);
     }
     
     @Override
     public void draw (SpriteBatch batch, float parentAlpha) {
        // this doesn't work yet because mapView offset is not counted
        batch.draw(defaultSprite.getKeyFrame(((PersonViewData)data).tc), getX()*32, getY()*32, getWidth()*32, getHeight()*32);
     }
 }
