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
 
 import chlorophytum.story.Story;
 
 import com.badlogic.gdx.Gdx;
 
 public class Player extends Person {
     @Override
     public void moved () {
        for (com.badlogic.gdx.maps.MapObject object : onMap.checkObjectLayer("events", position)) {
             if (Boolean.parseBoolean(object.getProperties().get("auto", "", String.class))) {
                 processStory(object);
             }
         }
     }
     
     protected void processStory (com.badlogic.gdx.maps.MapObject object) {
         String tname = object.getName();
         if (!tname.isEmpty()) {
             Story.instance().trigger(tname);
         } else {
             Gdx.app.log("story", "no story name here");
         }
     }
 }
