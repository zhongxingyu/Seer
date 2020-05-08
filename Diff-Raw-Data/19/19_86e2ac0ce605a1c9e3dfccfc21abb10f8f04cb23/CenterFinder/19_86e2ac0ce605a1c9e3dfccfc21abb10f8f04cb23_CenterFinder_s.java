 package tailor.geometry;
 
 import java.util.List;
 
 import tailor.Level;
 import tailor.datasource.Structure;
 import tailor.description.AtomDescription;
 import tailor.description.ChainDescription;
 import tailor.description.Description;
 import tailor.description.GroupDescription;
 import tailor.engine.Match;
 
 /**
  * Utility class that finds the center for a piece of structure, where the
  * center of an atom is just its coordinates, and the center of other 
  * hierarchical groups are ultimately averages of the atom centers. 
  * 
  * @author maclean
  *
  */
 public class CenterFinder {
 
     /**
      * Find the geometric center of the structure in match.
      * 
      * @param description
      * @param match
      * @return
      */
     public static Vector findCenter(Description description, Match match) {
         Structure structure = match.getStructure();
         if (description.getLevel() == Level.ATOM 
                 && match(description, structure)) {
             return structure.getAtomCenter(); 
         } else {
             Vector center = new Vector();
             int count = 0;
             List<? extends Description> subdescriptions = 
                 description.getSubDescriptions();
            for (int i = 0; i < match.size(); i++) {
                 Match submatch = match.getSubMatch(i);
                 Description subdescription = subdescriptions.get(i);
                 if (match(subdescription, submatch.getStructure())) {
                     center.add(findCenter(subdescription, submatch));
                     count++;
                 }
             }
             if (count == 0) {
                 return center;
             } else {
                 return center.divide(count);
             }
         }
     }
     
     public static boolean match(Description description, Structure structure) {
         if (description instanceof AtomDescription) {
             return ((AtomDescription)description).matches(structure);
         } else if (description instanceof GroupDescription) {
             return ((GroupDescription)description).nameMatches(structure);
         } else if (description instanceof ChainDescription) {
             return ((ChainDescription)description).nameMatches(structure);
         }
         return false;
     }
 }
