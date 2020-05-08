 package cz.muni.fi.publishsubscribe.matchingtree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Subscription {
 
 	private List<Predicate> predicates = new ArrayList<>();
 
 	public void addPredicate(
			Predicate<? extends Comparable, ? extends Comparable> predicate) {
 		this.predicates.add(predicate);
 	}
 
 	public List<Predicate> getPredicates() {
 		return predicates;
 	}
 
 }
