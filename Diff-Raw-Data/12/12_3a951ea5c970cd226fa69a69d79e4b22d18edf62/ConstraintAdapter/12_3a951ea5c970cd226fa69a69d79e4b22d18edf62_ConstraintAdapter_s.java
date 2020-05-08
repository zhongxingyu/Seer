 package cz.muni.fi.ngmon.pubsub.benchmark.adapter.matchingtree;
 
 import siena.AttributeConstraint;
 import cz.muni.fi.ngmon.pubsub.benchmark.adapter.Operator;
 import cz.muni.fi.ngmon.pubsub.benchmark.adapter.interfaces.AttributeValue;
 import cz.muni.fi.ngmon.pubsub.benchmark.adapter.interfaces.Constraint;
 import cz.muni.fi.publishsubscribe.matchingtree.ComparisonResult;
 import cz.muni.fi.publishsubscribe.matchingtree.Predicate;
 import cz.muni.fi.publishsubscribe.matchingtree.PredicateTest;
 import cz.muni.fi.publishsubscribe.matchingtree.TestOperation;
 import cz.muni.fi.publishsubscribe.matchingtree.TestResult;
 
 public class ConstraintAdapter<T1 extends Comparable<T1>> implements
 		Constraint<T1> {
 
	private Predicate<? extends Comparable<?>, ? extends Comparable<?>> predicate = null;
 
 	private ComparisonResult operatorToComparisonResult(Operator operator) {
 		switch (operator) {
 		case GREATER_THAN:
 			return ComparisonResult.GREATER;
 		case GREATER_THAN_OR_EQUAL_TO:
 			return ComparisonResult.GREATER_OR_EQUAL;
 		case LESS_THAN:
 			return ComparisonResult.SMALLER;
 		case LESS_THAN_OR_EQUAL_TO:
 			return ComparisonResult.SMALLER_OR_EQUAL;
 		default:
 			throw new IllegalArgumentException(
 					"illegal operator in operatorToComparisonResult");
 		}
 	}
 
 	public ConstraintAdapter(String attributeName,
 			AttributeValue<T1> attributeValue, Operator operator) {
 		PredicateTest<?> predicateTest = null;
 
 		cz.muni.fi.publishsubscribe.matchingtree.AttributeValue<T1> matchingTreeAttributeValue = attributeValue
 				.getMatchingTreeAttributeValue();
 
 		switch (operator) {
 
 		case GREATER_THAN:
 		case GREATER_THAN_OR_EQUAL_TO:
 		case LESS_THAN:
 		case LESS_THAN_OR_EQUAL_TO:
 			predicateTest = new PredicateTest<>(attributeName,
 					matchingTreeAttributeValue, TestOperation.COMPARE);
 			this.predicate = new Predicate<>(predicateTest,
 					new TestResult<ComparisonResult>(
 							operatorToComparisonResult(operator),
 							ComparisonResult.class));
 			break;
 
 		case EQUALS:
 			predicateTest = new PredicateTest<>(attributeName, null,
 					TestOperation.EXAMINE);
 			if (attributeValue.getType() == Long.class) {
 				this.predicate = new Predicate<>(predicateTest,
 						new TestResult<Long>(
 								(Long) matchingTreeAttributeValue.getValue(),
 								Long.class));
 			} else {
 				this.predicate = new Predicate<>(predicateTest,
 						new TestResult<String>(
 								(String) attributeValue.getValue(),
 								String.class));
 			}
 			break;
 		}
 	}
 
 	@Override
	public Predicate<? extends Comparable<?>, ? extends Comparable<?>> getMatchingTreePredicate() {
 		return predicate;
 	}
 
 	@Override
 	public cz.muni.fi.publishsubscribe.countingtree.Constraint<T1> getCountingTreeConstraint() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public AttributeConstraint getSienaAttributeConstraint() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public String getAttributeName() {
 		throw new UnsupportedOperationException();
 	}
 
 }
