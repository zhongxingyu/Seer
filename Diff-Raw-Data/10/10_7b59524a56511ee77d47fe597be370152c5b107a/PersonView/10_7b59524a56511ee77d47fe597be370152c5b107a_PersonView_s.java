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
 
 package shabby.person;
 
 import chlorophytum.mapobject.*;
 
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g2d.*;
 import com.badlogic.gdx.math.*;
 
 /**
  * View class for person.
  * Should be replaced with scripting..
  */
 public class PersonView implements MapObjectView {
    Texture texture;
    Animation defaultSprite;
    Animation[] spriteRun = new Animation[8];
    Animation[] spriteStand = new Animation[8];
     
     @Override
     public void init () {
        texture = new Texture("data/maps/char-1.png");
         TextureRegion[][] regions = TextureRegion.split(texture, 32, 32);
         
         defaultSprite = new Animation(0, regions[0][0]);
         
         for (int i = 0; i < 8; ++i) {
             spriteStand[i] = new Animation(0, regions[i+1][0]);
             
             TextureRegion[] tmp = new TextureRegion[8];
             System.arraycopy(regions[i+1], 1, tmp, 0, 8);
             spriteRun[i] = new Animation(0.11f, tmp);
             spriteRun[i].setPlayMode(Animation.LOOP);
         }
     }
     
     @Override
     public void render (SpriteBatch batch, MapObjectViewData data) {
         PersonViewData pdata = (PersonViewData) data;
         
         final Animation[] spriteT;
         if (pdata.origin.state == Person.State.Run) {
             spriteT = spriteRun;
         } else {
             spriteT = spriteStand;
         }
         
         int direction = pdata.origin.direction;
         // fix this when all animations are done
         if (direction % 4 == 3) {
             direction = (direction+1)%8;
         }
         direction = direction/2*2;
         final Animation sprite = spriteT[direction];
         
         final Vector2 position = pdata.origin.position;
         
         batch.begin();
         batch.draw(sprite.getKeyFrame(pdata.tc), position.x, position.y, 2, 2);
         batch.end();
     }
 }
