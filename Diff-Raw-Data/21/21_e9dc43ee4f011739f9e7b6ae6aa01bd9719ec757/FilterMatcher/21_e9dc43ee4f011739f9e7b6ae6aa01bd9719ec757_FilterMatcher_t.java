 package cz.muni.fi.publishsubscribe.countingtree;
 
 import cz.muni.fi.publishsubscribe.countingtree.index.AttributeIndex;
 
 import java.util.*;
 
 /**
  * Stores data for the Counting Tree (stores Constraints inside Predicates using
  * AttributeIndex), returns all Filters matching a specific Event (by examining
  * the individual Constraints inside the Filter)
  */
 public class FilterMatcher {
 
 	private AttributeIndex attributeIndex = new AttributeIndex();
 
 	// private Map<Constraint<? extends Comparable<?>>, Set<Filter>>
 	// reverseLookup = new HashMap<>();
 	// private Map<Filter, Set<Predicate>> filterPredicateLookup = new
 	// HashMap<>();
 
 	private Map<Filter, Filter> filters = new HashMap<>();
 	private Map<Constraint<?>, Constraint<?>> constraints = new HashMap<>();
 
 	private Long filterId = 1L;
 
 	public FilterMatcher() {
 	}
 
 	public void addPredicate(Predicate predicate) {
 		List<Filter> predicateFilters = predicate.getFilters();
 
 		for (Filter filter : predicateFilters) {
 
 			// Set<Predicate> predicateSet = filterPredicateLookup.get(filter);
 			Filter fullFilter = filters.get(filter);
 			// the filter has already been inserted
 			if (fullFilter != null) {
 				fullFilter.addPredicate(predicate);
 				// new filter
 			} else {
 				filter.setId(filterId++);
 				filters.put(filter, filter);
 				filter.addPredicate(predicate);
 
 				List<Constraint<? extends Comparable<?>>> filterConstraints = filter
 						.getConstraints();
 				for (Constraint<? extends Comparable<?>> constraint : filterConstraints) {
 
 					this.attributeIndex.addConstraint(constraint);
 
 					Constraint<?> fullConstraint = constraints.get(constraint);
 					if (fullConstraint != null) {
 						fullConstraint.addFilter(filter);
 					} else {
 						constraints.put(constraint, constraint);
 						constraint.addFilter(filter);
 					}
 				}
 			}
 		}
 	}
 
 	public void removePredicate(Predicate predicate) {
 		// remove the relevant items from reverse lookup maps
 		/*-List<Filter> filters = predicate.getFilters();
 		for (Filter filter : filters) {
 			Set<Predicate> predicateSet = filterPredicateLookup.get(filter);
 			predicateSet.remove(predicate);
 			if (predicateSet.isEmpty()) {
 				filterPredicateLookup.remove(filter);
 			}
 
 			List<Constraint<? extends Comparable<?>>> constraints = filter
 					.getConstraints();
 			for (Constraint<? extends Comparable<?>> constraint : constraints) {
 				Set<Filter> associatedFilters = this.reverseLookup
 						.get(constraint);
 				if (associatedFilters != null) {
 					associatedFilters.remove(filter);
 					if (associatedFilters.isEmpty()) {
 						this.reverseLookup.remove(constraint);
 					}
 				}
 				this.attributeIndex.removeConstraint(constraint);
 			}
 		}*/

 		for (Filter filter : predicate.getFilters()) {
 			filter.removePredicate(predicate);
 			if (filter.noPredicates())
 				filters.remove(filter);

 			for (Constraint<?> constraint : filter.getConstraints()) {
 				constraint.removeFilter(filter);
 				if (constraint.noFilters())
 					constraints.remove(constraint);

 				attributeIndex.removeConstraint(constraint);
 			}
 		}
 	}
 
 	/*-public <T1 extends Comparable<T1>, T2 extends Constraint<T1>> List<Constraint<? extends Comparable<?>>> getMatchingConstraints(Event event) {
 		List<Constraint<? extends Comparable<?>>> constraints = new ArrayList<>();
 
 		List<Attribute<? extends Comparable<?>>> attributes = event.getAttributes();
 
 		for (Attribute<? extends Comparable<?>> uncastAttribute :  attributes) {
 			Attribute<T1> attribute = (Attribute<T1>) uncastAttribute;
 
 			List<T2> foundConstraints = this.attributeIndex.getConstraints(attribute.getName(), attribute.getValue());
 
 			constraints.addAll(foundConstraints);
 		}
 
 		return constraints;
 	}*/
 
 	public <T1 extends Comparable<T1>, T2 extends Constraint<T1>> void iterateThroughMatchedConstraints(
 			Event event, List<Subscription> subscriptions,
 			List<Filter> filtersToReset, List<Predicate> predicatesToReset) {
 		List<Attribute<? extends Comparable<?>>> attributes = event
 				.getAttributes();
 
 		for (Attribute<? extends Comparable<?>> uncastAttribute : attributes) {
 			Attribute<T1> attribute = (Attribute<T1>) uncastAttribute;
 
			List<Collection<Constraint<T1>>> foundConstraintLists = this.attributeIndex
					.getConstraints(attribute.getName(), attribute.getValue());
			for (Collection<Constraint<T1>> foundConstraints : foundConstraintLists) {
				for (Constraint<T1> constraint : foundConstraints) {
					constraint.incrementFiltersCounters(subscriptions,
							filtersToReset, predicatesToReset);
				}
 			}
 		}
 	}
 
 	/*-public List<Filter> getMatchingFilters(Event event) {
 		List<Filter> filters = new ArrayList<>();
 
 		for (Constraint c : this.getMatchingConstraints(event)) {
 
 			Set<Filter> foundFilters = this.reverseLookup.get(c);
 
 			if (foundFilters != null) {
 				filters.addAll(foundFilters);
 			}
 
 		}
 
 		return filters;
 	}*/
 
 	/*-public Set<Predicate> getPredicates(Filter filter) {
 		return this.filterPredicateLookup.get(filter);
 	}*/
 }
