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
 
 package lostsheep;
 
 import lostsheep.creatures.*;
 
 import chlorophytum.story.Story;
 import chlorophytum.World;
 import chlorophytum.map.*;
 
 import com.badlogic.gdx.*;
 
 public class LostSheepGame {
     private static LostSheepGame _instance;
     
     public static LostSheepGame instance () {
         if (_instance == null) {
             _instance = new LostSheepGame();
         }
         return _instance;
     }
     
     public Player player;
     public Person mechanic;
     public Person scientist;
     public Person writer;
     public Person policeman;
     
     public ChloroMap deck;
     public ChloroMap cabins;
     
     protected void spawn (Person person, ChloroMap map, String spawnName) {
         person.moveTo(map);
         com.badlogic.gdx.maps.MapObject spawn = map.getTiledObject("places", spawnName);
         if (spawn != null) {
             // fix this
             person.move(spawn.getProperties().get("x", Integer.class)/32, spawn.getProperties().get("y", Integer.class)/32);
         }
     }
     
     public void init () {
         Story.instance().init();
         
         deck = World.instance().loadMap("deck");
 //         cabins = World.instance().loadMap("cabins");
         
         player = new Player();
        Story.instance().addObject("self", player);
        
         mechanic = new Person("mechanic");
         scientist = new Person("scientist");
         writer = new Person("writer");
         policeman = new Person("policeman");
         spawn(player, deck, "spawn-player");
         spawn(mechanic, deck, "spawn-mechanic");
         spawn(scientist, deck, "spawn-scientist");
         spawn(writer, deck, "spawn-writer");
         spawn(policeman, deck, "spawn-policeman");
     }
 }
