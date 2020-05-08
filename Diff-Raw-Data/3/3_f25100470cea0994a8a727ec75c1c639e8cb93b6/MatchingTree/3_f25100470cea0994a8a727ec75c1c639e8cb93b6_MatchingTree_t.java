 package cz.muni.fi.publishsubscribe.matchingtree;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 public class MatchingTree {
 
 	private Node root = null;
 
 	public boolean preprocess(Subscription subscription) {
 		List<Predicate<Comparable<?>, Comparable<?>>> predicates = subscription
 				.getPredicates();
 		if (predicates.isEmpty())
 			return false;
 		int predicatesCount = predicates.size();
 
 		boolean found = true;
 		if (root == null) {
 			root = new Node();
 			root.setTest(predicates.get(0).getTest());
 			found = false;
 		}
 
 		Node currentNode = root;
 		int i = 0;
 
 		Node lastNode = null;
 		TestResult<Comparable<?>> lastResult = null;
 
 		while (found && i < predicatesCount) {
 			Predicate<Comparable<?>, Comparable<?>> currentPredicate = predicates
 					.get(i);
 			PredicateTest<Comparable<?>> currentTest = currentPredicate
 					.getTest();
 			TestResult<Comparable<?>> currentResult = currentPredicate
 					.getResult();
 
 			if (currentNode.isLeaf()) {
 				Node node = new Node();
 				node.setTest(currentTest);
 				node.setStarNode(currentNode);
 				lastNode.putResultNode(lastResult, node);
 				currentNode = node;
 				found = false;
 			} else if (currentNode.getTest().equals(currentTest)) {
 				Node resultNode = currentNode.getResultNode(currentResult);
 				if (resultNode == null)
 					found = false;
 				else {
 					lastNode = currentNode;
 					lastResult = currentResult;
 					currentNode = resultNode;
 					i++;
 				}
 			} else {
 				for (TestResult<Comparable<?>> result : currentNode
 						.getResultNodes().keySet()) {
 					if (currentPredicate.isCoveredBy(currentNode.getTest(),
 							result)) {
 						lastNode = currentNode;
 						lastResult = result;
 						currentNode = currentNode.getResultNode(result);
 					} else if (currentNode.getStarNode() != null) {
 						lastNode = currentNode;
 						lastResult = null; // meaning star node
 						currentNode = currentNode.getStarNode();
 					} else {
 						Node node = new Node();
 						node.setTest(currentTest);
 						currentNode.setStarNode(node);
 						lastNode = currentNode;
 						lastResult = null;
 						currentNode = node;
 						found = false;
 					}
 				}
 			}
 		}
 
 		if (!found) {
 			while (i < predicatesCount) {
 				Node node = new Node();
 				if (i == predicatesCount)
 					node.setSubscription(subscription);
 				else
 					node.setTest(predicates.get(i + 1).getTest());
 				currentNode.putResultNode(predicates.get(i).getResult(), node);
 				i++;
 			}
 		} else {
 			if (!currentNode.isLeaf()) {
 				while (currentNode.getStarNode() != null)
 					currentNode = currentNode.getStarNode();
 				if (!currentNode.isLeaf()) {
 					Node node = new Node();
 					node.setSubscription(subscription);
 					currentNode.setStarNode(node);
 				}
 			}
 		}
 
 		return true;
 	}
 
 	public List<Subscription> match(Event event) {
 		List<Subscription> matchedSubscriptions = new ArrayList<>();
 
 		Stack<Node> stack = new Stack<>();
		if (root != null)
			stack.push(root);
 
 		while (!stack.isEmpty()) {
 			Node currentNode = stack.pop();
 			if (currentNode.isLeaf()) {
 				matchedSubscriptions.add(currentNode.getSubscription());
 			} else {
 				PredicateTest<Comparable<?>> currentTest = currentNode
 						.getTest();
 				String attributeName = currentTest.getAttributeName();
 				AttributeValue<? extends Comparable> attributeValue = currentTest
 						.getValue();
 
 				Attribute<? extends Comparable<?>> eventAttribute = event
 						.getAttributeByName(attributeName);
 				AttributeValue<? extends Comparable<?>> eventAttributeValue = eventAttribute
 						.getValue();
 
 				boolean foundResult = false;
 
 				// switching based on the test in the current node
 				switch (currentTest.getOperation()) {
 
 				case EXAMINE: {
 					Node resultNode = currentNode
 							.getResultNode((TestResult<Comparable<?>>) eventAttributeValue
 									.getValue());
 					if (resultNode != null)
 						stack.push(resultNode);
 					break;
 				}
 
 				case COMPARE:
 					int compareResult = attributeValue.getValue().compareTo(
 							eventAttributeValue.getValue());
 					if (compareResult == 0) {
 						Node resultNode = currentNode
 								.getResultNode(new TestResult(
 										ComparisonResult.GREATER_OR_EQUAL,
 										ComparisonResult.class));
 						if (resultNode != null) {
 							stack.push(resultNode);
 							foundResult = true;
 						} else {
 							resultNode = currentNode
 									.getResultNode(new TestResult(
 											ComparisonResult.SMALLER_OR_EQUAL,
 											ComparisonResult.class));
 							if (resultNode != null) {
 								stack.push(resultNode);
 								foundResult = true;
 							}
 						}
 						// the node attribute value is less than event value ->
 						// event value is greater -> looking for greater /
 						// greater or equal results
 					} else if (compareResult < 0) {
 						Node resultNode = currentNode
 								.getResultNode(new TestResult(
 										ComparisonResult.GREATER,
 										ComparisonResult.class));
 						if (resultNode != null) {
 							stack.push(resultNode);
 							foundResult = true;
 						} else {
 							resultNode = currentNode
 									.getResultNode(new TestResult(
 											ComparisonResult.GREATER_OR_EQUAL,
 											ComparisonResult.class));
 							if (resultNode != null) {
 								stack.push(resultNode);
 								foundResult = true;
 							}
 						}
 					} else if (compareResult > 0) {
 						Node resultNode = currentNode
 								.getResultNode(new TestResult(
 										ComparisonResult.SMALLER,
 										ComparisonResult.class));
 						if (resultNode != null) {
 							stack.push(resultNode);
 							foundResult = true;
 						} else {
 							resultNode = currentNode
 									.getResultNode(new TestResult(
 											ComparisonResult.SMALLER_OR_EQUAL,
 											ComparisonResult.class));
 							if (resultNode != null) {
 								stack.push(resultNode);
 								foundResult = true;
 							}
 						}
 					}
 					break;
 				}
 
 				// star node
 				if (!foundResult) {
 					Node starNode = currentNode.getStarNode();
 					if (starNode != null) {
 						stack.push(starNode);
 					}
 				}
 			}
 		}
 
 		return matchedSubscriptions;
 	}
 }
