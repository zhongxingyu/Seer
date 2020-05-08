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
 
 import chlorophytum.mapobject.MapObject;
 
 public class Person extends MapObject {
     protected float speed = 1;
     
     public Person () {
         super();
         viewData = new PersonViewData(this);
        view = new PersonView(viewData);
     }
     
     @Override
     public void update (float dt) {
         super.update(dt);
         
         if (move.x != 0 || move.y != 0) {
             int angle = ((int) move.angle()/45) * 45;
             float dx = (float) Math.cos(angle*Math.PI/180) * dt * speed;
             float dy = (float) Math.sin(angle*Math.PI/180) * dt * speed;
             
             move(dx, dy);
         }
     }
 }
