 package nl.utwente.cs.pxml;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.basex.query.QueryModule;
 import org.basex.query.item.ANode;
 import org.basex.query.item.QNm;
 
 import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
 
 import nl.utwente.cs.pxml.util.CollectionUtils;
 
 /**
 * Importable query module containing functions for use with probabilistic XML.
  * 
  * @author Mattijs Ugen
  */
 public class PXML extends QueryModule {
 
	/**
	 * The namespace used for elements dealing with probability in the a document.
	 */
	public static final String NAMESPACE = "http://www.utwente.nl/~keulen/pxml/";

	/**
	 * Mapping used to cache probabilities for conditions encountered earlier.
	 */
 	protected Map<String, Double> probabilityCache;
 
 	/**
 	 * Creates a new PXML instance, with a newly created probability cache.
 	 */
 	public PXML() {
 		this.probabilityCache = new TreeMap<String, Double>();
 	}
 
 	/**
 	 * Combines all conditions into a single string keeping only the unique
 	 * conditions. The result need not be consistent.
 	 * 
 	 * @param conditions
 	 *            The conditions to be combined.
 	 * @return A single string containing all conditions in the parameters.
 	 */
 	@Requires(Permission.NONE)
 	@Deterministic
 	public String combine(String... conditions) { // TODO: make String... a BaseX Value?
 		// create a condition 'container'
 		Set<String> result = new HashSet<String>(conditions.length);
 		for (String condition : conditions) {
 			// rely on Set to enforce uniqueness
 			result.add(condition);
 		}
 
 		// join the resulting set on the separator
 		return CollectionUtils.join(result, " ");
 	}
 
 	/**
 	 * Checks whether the provided descriptor is consistent. Consistency means
 	 * no variables occurring twice with different values.
 	 * 
 	 * @param descriptor
 	 *            The condition descriptor string to be tested.
 	 * @return Whether the conditions in the provided condition string are
 	 *         consistent.
 	 */
 	@Requires(Permission.NONE)
 	@Deterministic
 	public boolean consistent(String descriptor) {
 		ConditionGenerator generator = new ConditionGenerator(descriptor);
 		Map<String, Integer> conditions = new HashMap<String, Integer>();
 
 		for (Condition condition : generator) {
 			// rely on Map to enforce uniqueness of name, use Integer as it can
 			// be null
 			Integer value = conditions.put(condition.name, condition.value);
 			if (value != null && value != condition.value) {
 				// immediately return false if the name was already encountered
 				// with a different value
 				return false;
 			}
 		}
 
 		// no inconsistencies found, return true
 		return true;
 	}
 
 	/**
 	 * Tests whether condition descriptor a is mutually exclusive with condition
 	 * descriptor b by checking for variables in a having different values in b.
 	 * 
 	 * @param a
 	 *            The one condition descriptor.
 	 * @param b
 	 *            The other condition descriptor.
 	 * @return Whether the two condition descriptors are mutually exclusive.
 	 */
 	@Requires(Permission.NONE)
 	@Deterministic
 	public boolean mutuallyExclusive(String a, String b) {
 		ConditionGenerator condA = new ConditionGenerator(a);
 		ConditionGenerator condB = new ConditionGenerator(b);
 		Map<String, Integer> conditions = new HashMap<String, Integer>();
 
 		// put all conditions a into the map ...
 		for (Condition condition : condA) {
 			conditions.put(condition.name, condition.value);
 		}
 
 		// ... check if there is a condition in b that has the same name but a
 		// different value
 		for (Condition condition : condB) {
 			Integer value = conditions.get(condition.name);
 			if (value != null && value != condition.value) {
 				// immediately return false if this such a condition is found
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Calculates the probability of all conditions by simply multiplying them
 	 * together. Does *not* check for consistency (which would make the
 	 * probability 0.0).
 	 * 
 	 * @param wsdList
 	 *            The node containing the probabilities.
 	 * @param conditions
 	 *            The conditions to look up.
 	 * @return The probability of all conditions being true.
 	 */
 	@Requires(Permission.NONE)
 	@ContextDependent
 	public double probability(ANode wsdList, String... conditions) { // TODO: make String... a BaseX Value?
 		double probability = 1.0;
 		// find probabilities for all conditions, multiply them
 		for (String condition : conditions) {
 			// use Double to allow null when key is not present (TODO: test this)
 			Double value = this.probabilityCache.get(condition);
 			if (value == null) {
 				// find it in the wsd-list ...
 				value = this.findProbability(wsdList, condition);
 				// ... and cache it for future use
 				this.probabilityCache.put(condition, value);
 			}
 
 			// do the probability multiplication
 			probability *= value;
 		}
 
 		// return the result
 		return probability;
 	}
 
 	/**
 	 * Finds a probability for the variable encoded by condition in the wsdList.
 	 * 
 	 * @param wsdList
 	 *            The node containing the probabilities.
 	 * @param condition
 	 *            The condition to match.
 	 * @return The probability of the condition being true.
 	 */
 	protected Double findProbability(ANode wsdList, String strCondition) {
 		Condition condition = new Condition(strCondition);
 		// find node matching variable name
 		for (ANode variable : wsdList.children()) {
 			if (condition.name.equals(new String(variable.qname().local()))) {
 				String valName = "val-" + condition.value;
 				// found the right node, now find the right attribute matching variable value
 				for (ANode attr : variable.attributes()) {
 					if (valName.equals(new String(attr.qname().local()))) {
 						// found the right attribute, parse the value as a double
 						// TODO: catch NumberFormatException?
 						return Double.parseDouble(new String(attr.string()));
 					}
 				}

 				// no attribute matching the value was found, return 0.0
 				return 0.0;
 			}
 		}

 		// no node matching the variable was found, return 0.0
 		return 0.0;
 	}
 
 }
