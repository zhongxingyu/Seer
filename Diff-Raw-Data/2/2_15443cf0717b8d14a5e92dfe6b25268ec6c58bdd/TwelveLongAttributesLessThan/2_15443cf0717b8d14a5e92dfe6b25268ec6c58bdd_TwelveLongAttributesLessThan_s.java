 package cz.muni.fi.publishsubscribe.countingtree.benchmark;
 
 import com.google.caliper.SimpleBenchmark;
 
 import cz.muni.fi.publishsubscribe.countingtree.Attribute;
 import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
 import cz.muni.fi.publishsubscribe.countingtree.Constraint;
 import cz.muni.fi.publishsubscribe.countingtree.CountingTree;
 import cz.muni.fi.publishsubscribe.countingtree.Event;
 import cz.muni.fi.publishsubscribe.countingtree.Filter;
 import cz.muni.fi.publishsubscribe.countingtree.Operator;
 import cz.muni.fi.publishsubscribe.countingtree.Predicate;
 
 /**
  * 12 Long attributes, operator <
  * every event has all of the 12 attributes, "notMatchingEvent1" matches
  * all attributes (Constraints) except one, "notMatchingEvent2" matches
  * no attribute (Constraint)
  */
 public class TwelveLongAttributesLessThan extends SimpleBenchmark {
 
 	private static final String LONG_ATTRIBUTE_NAME_PREFIX = "longAttribute";
 	private static final int ATTRIBUTE_COUNT = 12;
 	private static final long MIN_VALUE = 1000L;
 	private static final long MAX_VALUE = 5000L;
 	private static final long ALWAYS_MATCHING_VALUE = 0L;
 	private static final long NEVER_MATCHING_VALUE = 100000000L;
 
 	private static final int PREDICATE_COUNT = 50;
 	private static final int EVENT_COUNT = 1000;
 
 	private CountingTree tree;
 	private Event matchingEvent;
 	private Event notMatchingEvent1;
 	private Event notMatchingEvent2;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		this.tree = new CountingTree();
 
 		long val = MIN_VALUE;
 		for (int i = 0; i < PREDICATE_COUNT; i++) {
 
 			Filter filter = new Filter();
 			for (int j = 0; j < ATTRIBUTE_COUNT; j++) {
 				Constraint<Long> constraint = new Constraint<>(
 						LONG_ATTRIBUTE_NAME_PREFIX + j, new AttributeValue<>(j
 								* 10000 + val, Long.class), Operator.LESS_THAN);
 				filter.addConstraint(constraint);
 			}
 
 			Predicate predicate = new Predicate();
 			predicate.addFilter(filter);
 
 			tree.subscribe(predicate);
 
 			val = val >= MAX_VALUE ? MIN_VALUE : val + 1;
 
 		}
 
 		matchingEvent = new Event();
 		for (int j = 0; j < ATTRIBUTE_COUNT; j++) {
 			matchingEvent.addAttribute(new Attribute<>(
 					LONG_ATTRIBUTE_NAME_PREFIX + j, new AttributeValue<Long>(
 							ALWAYS_MATCHING_VALUE, Long.class)));
 		}
 
 		notMatchingEvent1 = new Event();
 		for (int j = 0; j < ATTRIBUTE_COUNT - 1; j++) {
 			notMatchingEvent1.addAttribute(new Attribute<>(
 					LONG_ATTRIBUTE_NAME_PREFIX + j, new AttributeValue<Long>(
 							ALWAYS_MATCHING_VALUE, Long.class)));
 		}
 		notMatchingEvent1.addAttribute(new Attribute<>(
 				LONG_ATTRIBUTE_NAME_PREFIX + (ATTRIBUTE_COUNT - 1),
 				new AttributeValue<Long>(NEVER_MATCHING_VALUE, Long.class)));
 
 		notMatchingEvent2 = new Event();
 		for (int j = 0; j < ATTRIBUTE_COUNT; j++) {
			matchingEvent.addAttribute(new Attribute<>(
 					LONG_ATTRIBUTE_NAME_PREFIX + j, new AttributeValue<Long>(
 							NEVER_MATCHING_VALUE, Long.class)));
 		}
 	}
 
 	public void timeMatch_25(int reps) {
 		for (int i = 0; i < reps; i++) {
 			for (int j = 0; j < EVENT_COUNT / 4; j++) {
 				tree.match(matchingEvent);
 				tree.match(notMatchingEvent1);
 				tree.match(notMatchingEvent1);
 				tree.match(notMatchingEvent1);
 			}
 		}
 	}
 
 	public void timeMatch2_25(int reps) {
 		for (int i = 0; i < reps; i++) {
 			for (int j = 0; j < EVENT_COUNT / 4; j++) {
 				tree.match(matchingEvent);
 				tree.match(notMatchingEvent2);
 				tree.match(notMatchingEvent2);
 				tree.match(notMatchingEvent2);
 			}
 		}
 	}
 
 	public void timeMatch_50(int reps) {
 		for (int i = 0; i < reps; i++) {
 			for (int j = 0; j < EVENT_COUNT / 4; j++) {
 				tree.match(matchingEvent);
 				tree.match(matchingEvent);
 				tree.match(notMatchingEvent1);
 				tree.match(notMatchingEvent1);
 			}
 		}
 	}
 
 	public void timeMatch2_50(int reps) {
 		for (int i = 0; i < reps; i++) {
 			for (int j = 0; j < EVENT_COUNT / 4; j++) {
 				tree.match(matchingEvent);
 				tree.match(matchingEvent);
 				tree.match(notMatchingEvent2);
 				tree.match(notMatchingEvent2);
 			}
 		}
 	}
 
 	public void timeMatch_75(int reps) {
 		for (int i = 0; i < reps; i++) {
 			for (int j = 0; j < EVENT_COUNT / 4; j++) {
 				tree.match(matchingEvent);
 				tree.match(matchingEvent);
 				tree.match(matchingEvent);
 				tree.match(notMatchingEvent1);
 			}
 		}
 	}
 
 	public void timeMatch2_75(int reps) {
 		for (int i = 0; i < reps; i++) {
 			for (int j = 0; j < EVENT_COUNT / 4; j++) {
 				tree.match(matchingEvent);
 				tree.match(matchingEvent);
 				tree.match(matchingEvent);
 				tree.match(notMatchingEvent2);
 			}
 		}
 	}
 
 	public void timeMatch_100(int reps) {
 		for (int i = 0; i < reps; i++) {
 			for (int j = 0; j < EVENT_COUNT; j++) {
 				tree.match(matchingEvent);
 			}
 		}
 	}
 
 }
