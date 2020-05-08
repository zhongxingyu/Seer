 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Responsible for keeping the universe active. Specifically,
  * 1. Manage patrols and traders - spawn replacements as needed - TODO
  * 2. Manage stations - spawn replacements as needed - TODO
  * 3. Add 'fun' disasters to the universe. - TODO
  */
 package engine;
 
 import java.util.ArrayList;
 import lib.Faction;
 import lib.Parser;
 import lib.Parser.Term;
 import lib.SuperFaction;
 import universe.Universe;
 
 /**
  *
  * @author nwiehoff
  */
 public class God implements EngineElement {
 
     private Universe universe;
     private ArrayList<SuperFaction> factions = new ArrayList<>();
 
     public God(Universe universe) {
         this.universe = universe;
         //generate lists
         initFactions();
     }
 
     private void initFactions() {
         //make a list of all factions
        ArrayList<Faction> tmpF = new ArrayList<>();
         Parser fParse = new Parser("FACTIONS.txt");
         ArrayList<Term> terms = fParse.getTermsOfType("Faction");
         for (int a = 0; a < terms.size(); a++) {
             factions.add(new SuperFaction(terms.get(a).getValue("name")));
         }
     }
 
     @Override
     public void periodicUpdate() {
         try {
             checkPatrols();
             checkStations();
         } catch (Exception e) {
             System.out.println("Error manipulating dynamic universe.");
             e.printStackTrace();
         }
     }
 
     private void checkPatrols() {
         //TODO
     }
     
     private void checkStations() {
         //TODO
     }
 }
