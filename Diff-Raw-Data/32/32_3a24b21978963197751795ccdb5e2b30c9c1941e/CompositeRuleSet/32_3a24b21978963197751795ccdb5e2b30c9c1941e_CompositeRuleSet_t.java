 package suite.lp.kb;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import suite.Suite;
 import suite.lp.doer.Cloner;
 import suite.node.Node;
import suite.util.InspectUtil;
 import suite.util.Util;
 
 public class CompositeRuleSet implements RuleSet {
 
 	private RuleSet first;
 	private RuleSet second;
 
 	public CompositeRuleSet(RuleSet first) {
 		this(first, Suite.createRuleSet());
 	}
 
 	public CompositeRuleSet(RuleSet first, RuleSet second) {
 		this.first = first;
 		this.second = second;
 	}
 
 	@Override
 	public void addRule(Rule rule) {
 		second.addRule(rule);
 	}
 
 	@Override
 	public void addRuleToFront(Rule rule) {
 		second.addRuleToFront(rule);
 	}
 
 	@Override
 	public void removeRule(Rule rule) {
 		second.removeRule(rule);
 	}
 
 	/**
 	 * If the override rule set contain rules for specified head, return them.
 	 * 
 	 * Otherwise return what we got from parent rule set.
 	 */
 	@Override
 	public List<Rule> searchRule(Node head) {
 		List<Rule> rules = second.searchRule(head);
 
 		if (rules.isEmpty()) {
 			rules = first.searchRule(head);
 
 			// Clone all the rules once to clear local variable references
 			List<Rule> newRules = new ArrayList<>(rules.size());
 			Cloner cloner = new Cloner();
 			for (Rule rule : rules)
 				newRules.add(cloner.clone(rule));
 			rules = newRules;
 		}
 
 		return rules;
 	}
 
 	/**
 	 * Returns everything we have anyway: override rule set and parent rule set.
 	 */
 	@Override
 	public List<Rule> getRules() {
 		return Util.add(first.getRules(), second.getRules());
 	}
 
	@Override
	public boolean equals(Object object) {
		return InspectUtil.equals(this, object);
	}

	@Override
	public int hashCode() {
		return InspectUtil.hashCode(this);
	}

 }
