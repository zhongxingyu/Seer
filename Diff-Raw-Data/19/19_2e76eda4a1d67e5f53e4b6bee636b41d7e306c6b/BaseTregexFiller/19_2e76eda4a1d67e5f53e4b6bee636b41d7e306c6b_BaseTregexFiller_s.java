 package sf.filler.tree;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.stanford.nlp.trees.Tree;
 import edu.stanford.nlp.trees.tregex.TregexMatcher;
 import edu.stanford.nlp.trees.tregex.TregexPattern;
 
 import sf.SFConstants;
 import sf.filler.Filler;
 import sf.retriever.CorefEntity;
 import sf.retriever.CorefMention;
 import sf.retriever.CorefProvider;
 import sf.retriever.NerType;
 
 public abstract class BaseTregexFiller extends Filler {
 	
 	protected List<String> getMatchNames(String pattern, Tree t, Map<String, String> annotations, CorefProvider coref) {
 		TregexPattern p = TregexPattern.compile(pattern);
 		TregexMatcher m = p.matcher(t);
 
 		List<String> tokens = Arrays.asList(annotations.get(SFConstants.TOKENS).split(" "));
 		Set<Integer> places = new HashSet<Integer>();
		while(m.find()) {
 			Tree match = m.getMatch();
 			for(Tree node : match.getLeaves()) {
 				String name = node.value();
 				places.add(tokens.indexOf(name));
 				
 				// For some reason, we are getting an infinite loop on
 				// sentence ID 8299442. For now, just limit the number of
 				// places to something reasonable.
 				if (places.size() > 100) break FIND;
 			}
 		}
 		
 		HashSet<String> placesSet = new HashSet<String>();
 		for(Integer placeIndex : places) {
 			if(placeIndex >= 0) {
 				String place = tokens.get(placeIndex);
 				if(annotations.get(SFConstants.STANFORDNER).split(" ")[placeIndex].equals("LOCATION"))
 					placesSet.add(place);
 				Collection<CorefMention> corefs = coref.inRange(placeIndex, placeIndex);
 				if(corefs.size() > 0) {
 					CorefEntity entity = corefs.iterator().next().entity;
 					if(entity.nerType == NerType.LOCATION)
 						placesSet.add(entity.repMention.entity.fullName);
 					else if(place.contains("-based")) {
 						place = place.replace("-based", "");
 						placesSet.add(place);
 					}
 				}
 			}
 		}
 		
 		ArrayList<String> retList = new ArrayList<String>();
 		retList.addAll(placesSet);
 		return retList;
 	}
 	
 }
