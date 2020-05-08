 package isnork.g3;
 
 import isnork.sim.SeaLifePrototype;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 
 import com.google.common.collect.ImmutableSortedMap;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.google.common.primitives.Doubles;
 
 public class WaterProofPokedex extends AbstractPokedex {
 	ImmutableSortedMap<Integer, String> speciesRanking;
 
 	public WaterProofPokedex(Set<SeaLifePrototype> species) {
 		super(species);
 
 		Ordering<SeaLifePrototype> happiness = new Ordering<SeaLifePrototype>() {
 			public int compare(SeaLifePrototype left, SeaLifePrototype right) {
				double left_happiness = left.getHappinessD();
				double right_happiness = right.getHappinessD();
				if (left_happiness != right_happiness) {
					return Doubles.compare(left_happiness, right_happiness);
				}
				return -1;
 			}
 		};
 		ImmutableSortedSet<SeaLifePrototype> sortedSpecies = ImmutableSortedSet
 				.orderedBy(happiness).addAll(species).build();
 
 		Map<Integer, String> tempSpeciesRanking = Maps.newHashMap();
 		int i = 0;
 		for (SeaLifePrototype aSpecies : sortedSpecies) {
 			tempSpeciesRanking.put(i++, aSpecies.getName());
 		}
 		speciesRanking = ImmutableSortedMap.copyOf(tempSpeciesRanking);
 	}
 
 	public SortedMap<Integer, String> getSpeciesRanking() {
 		return speciesRanking;
 	}
 }
