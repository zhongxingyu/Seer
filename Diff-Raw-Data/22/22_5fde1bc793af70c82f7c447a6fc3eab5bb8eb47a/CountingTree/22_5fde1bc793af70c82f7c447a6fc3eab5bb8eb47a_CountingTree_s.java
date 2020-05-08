 package cz.muni.fi.publishsubscribe.countingtree;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * The main (front-end) class for the algorithm; Usage: Add the required
  * Predicates (subscriptions) first, then get the Predicates matching the
  * Event(s)
  */
 public class CountingTree {
 
 	private Long subscriptionNextId = 1L;
 	// private Long subscriptionCount = 0L;
 	private Map<Predicate, Set<Subscription>> subscriptionLookup = new HashMap<>();
 	private FilterMatcher matcher = new FilterMatcher();
 
 	public Long subscribe(Predicate predicate, Subscription subscription) {
 		subscription.setId(subscriptionNextId);
 
 		Set<Subscription> subscriptions = subscriptionLookup.get(predicate);
 		// the same predicate has already been inserted
 		if (subscriptions != null) {
 			subscriptions.add(subscription);
 		} else {
 			Set<Subscription> subscriptionSet = new HashSet<>();
 			subscriptionSet.add(subscription);
 			subscriptionLookup.put(predicate, subscriptionSet);
 			matcher.addPredicate(predicate);
 		}
 
 		// subscriptionCount++;
 
 		return subscriptionNextId++;
 	}
 
 	/*-public void createIndexTable() {
 		if (predicates != null && !predicates.isEmpty()) {
 			matcher = new FilterMatcher(predicates);
 		} else {
 			matcher = null;
 		}
 	}*/
 
 	public boolean unsubscribe(Long subscriptionId) {
 		/*-Predicate predicate = this.predicates.get(subscriptionId);
 		if (predicate == null)
 			return false;
 		this.matcher.removePredicate(predicate);
 		return (this.predicates.remove(subscriptionId) != null);*/
 		throw new UnsupportedOperationException("not yet implemented");
 	}
 
 	public boolean unsubscribe(Predicate predicate) {
 		/*-Long predicateId = predicate.getId();
 		if (predicateId == null)
 			return false;
 		return unsubscribe(predicateId);*/
 		throw new UnsupportedOperationException("not yet implemented");
 	}
 
 	public List<Subscription> match(Event event) {
 
 		if (matcher == null) {
 			return new ArrayList<Subscription>();
 		}
 
 		List<Subscription> subscriptions = new ArrayList<>();
 		int predicateCount = subscriptionLookup.size();
 
 		Map<Filter, Integer> counters = new HashMap<Filter, Integer>();
 		Set<Predicate> matched = new HashSet<Predicate>();
 
 		// first get the filters associated to the matching constraints
 		List<Filter> matchingFilters = matcher.getMatchingFilters(event);
 
 		for (Filter filter : matchingFilters) {
			Set<Predicate> matchedPredicates = matcher.getPredicates(filter);
			for (Predicate matchedPredicate : matchedPredicates) {
				if (!matched.contains(matchedPredicate)) {
					Integer count = counters.get(filter);
					if (count == null)
						count = 0;
					counters.put(filter, ++count);
					if (count.equals(filter.getConstraints().size())) {
 						subscriptions.addAll(subscriptionLookup
 								.get(matchedPredicate));
 						matched.add(matchedPredicate);
 						if (matched.size() == predicateCount)
 							break;
 					}
 				}
 			}
 		}
 
 		return subscriptions;
 	}
 }
