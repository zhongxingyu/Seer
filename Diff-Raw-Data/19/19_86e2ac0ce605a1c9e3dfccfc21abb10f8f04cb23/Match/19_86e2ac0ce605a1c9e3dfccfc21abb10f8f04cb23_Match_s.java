 package tailor.engine;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import tailor.datasource.Structure;
 import tailor.description.Description;
 
 /**
  * A search will produce a series of Match objects, that stores the association 
  * between the Description and the Structure it matches. 
  * 
  * @author maclean
  *
  */
 public class Match implements Iterable<Match> {
     
     private Description description;
     
     private Structure structure;
     
     private List<Match> subMatches;
 
     public Match(Description description, Structure structure) {
         this.description = description;
         this.structure = structure;
         this.subMatches = new ArrayList<Match>();
     }
     
     public Iterator<Match> iterator() {
         return subMatches.iterator();
     }
     
     public Match getSubMatch(int i) {
         return subMatches.get(i);
     }
     
     /**
      * Make a sub-match between a sub-description and a sub-structure.
      * Returns a reference to the newly created Match.
      *  
      * @param description
      * @param structure
      */
     public Match associate(Description description, Structure structure) {
         Match childMatch = new Match(description, structure);
         subMatches.add(childMatch);
         return childMatch;
     }
     
     public void addSubMatch(Match match) {
         this.subMatches.add(match);
     }
     
     public Description getDescription() {
         return this.description;
     }
     
     public Structure getStructure() {
         return this.structure;
     }
     
     public int size() {
         return this.subMatches.size();
     }
     
 }
