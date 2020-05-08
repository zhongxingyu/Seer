 package org.glite.authz.pap.common.xacml.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.opensaml.xacml.policy.PolicyType;
 import org.opensaml.xacml.policy.RuleType;
 import org.opensaml.xacml.policy.TargetType;
 
 /**
  * Helper methods for {@link PolicyType}.
  */
 public class PolicyHelper extends XMLObjectHelper<PolicyType> {
 
     public static final String RULE_COMBALG_DENY_OVERRIDS = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides";
     public static final String RULE_COMBALG_FIRST_APPLICABLE = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
     public static final String RULE_COMBALG_PERMIT_OVERRIDS = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides";
 
     private static final javax.xml.namespace.QName ELEMENT_NAME = PolicyType.DEFAULT_ELEMENT_NAME;
 
     private static PolicyHelper instance = new PolicyHelper();
 
     private PolicyHelper() {}
 
     /**
      * Builds a PolicyType object with the given id and the given rule combining algorithm.
      * 
      * @param policyId PolicyId.
      * @param ruleCombinerAlgorithmId rule combining algorithm.
      * @return a PolicyType object with the given id and the given rule combining algorithm.
      */
     public static PolicyType build(String policyId, String ruleCombinerAlgorithmId) {
         return build(policyId, ruleCombinerAlgorithmId, null);
     }
 
     /**
      * Builds a PolicyType object with the given id, the given rule combining algorithm and the
      * given target.
      * 
      * @param policyId PolicyId.
      * @param ruleCombinerAlgorithmId rule combining algorithm.
      * @param target the policy target.
      * @return a PolicyType object with the given id, the given rule combining algorithm and the
      *         given target.
      */
     public static PolicyType build(String policyId, String ruleCombinerAlgorithmId, TargetType target) {
 
         PolicyType policy = (PolicyType) builderFactory.getBuilder(ELEMENT_NAME).buildObject(ELEMENT_NAME);
         policy.setPolicyId(policyId);
         policy.setRuleCombiningAlgoId(ruleCombinerAlgorithmId);
 
         if (target == null)
             policy.setTarget(TargetHelper.build());
         else
             policy.setTarget(target);
 
         return policy;
     }
 
     public static PolicyHelper getInstance() {
         return instance;
     }
 
     /**
      * Returns the rule identified by the given id.
      * 
      * @param policy the policy to serch the rule for.
      * @param ruleId the rule id.
      * @return the rule identified by the given id or <code>null</code> if no rule identified by the
      *         given id was found.
      */
     public static RuleType getRule(PolicyType policy, String ruleId) {
 
         if (ruleId == null) {
             return null;
         }
 
         for (RuleType rule : policy.getRules()) {
             if (ruleId.equals(rule.getRuleId())) {
                 return rule;
             }
         }
         return null;
     }
 
     /**
      * Checks if the policy contains a rule with the given id.
      * 
      * @param policy the policy to check for the rule id.
      * @param ruleId the rule id to check for.
      * @return <code>true</code> if a rule with the given id exists, <code>false</code> otherwise.
      */
     public static boolean hasRuleId(PolicyType policy, String ruleId) {
 
         if (ruleId == null) {
             return false;
         }
 
         List<RuleType> ruleList = policy.getRules();
 
         for (RuleType rule : ruleList) {
             if (ruleId.equals(rule.getRuleId())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns the index (in the list of rules) of the rule identified by the given id.
      * 
      * @param policy the policy to check for the rule id.
      * @param ruleId the rule id to search for.
      * @return the index (in the list of rules) of the rule identified by the given id,
      *         <code>-1</code> if no such rule was found.
      */
     public static int indexOfRule(PolicyType policy, String ruleId) {
 
         if (ruleId == null) {
             return -1;
         }
 
         List<RuleType> ruleList = policy.getRules();
 
         for (int i = 0; i < ruleList.size(); i++) {
             if (ruleId.equals(ruleList.get(i).getRuleId())) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Insert a rule at the given position.
      * 
      * @param policy the policy to insert the rule in.
      * @param index position of the rule in the rule list (-1 to append the rule at the end).
      * @param rule the rule to add.
      */
     public static void addRule(PolicyType policy, int index, RuleType rule) {
 
         if (index == -1) {
             policy.getRules().add(rule);
            return;
         } 
         // workaround for a bug in opensaml
         List<RuleType> ruleList = new ArrayList<RuleType>(policy.getRules());
         
         ruleList.add(index, rule);
         
         policy.getRules().clear();
         policy.getRules().addAll(ruleList);
     }
 
     /**
      * Remove the rule identified by the given id.
      * 
      * @param policy the policy to remove the rule from.
      * @param ruleId the id of the rule to remove.
      * @return the removed rule if it was removed, <code>null</code> if a rule with the given id was
      *         not found.
      */
     public static RuleType removeRule(PolicyType policy, String ruleId) {
 
         if (ruleId == null) {
             return null;
         }
 
         List<RuleType> ruleList = policy.getRules();
 
         RuleType targetRule = null;
 
         for (RuleType rule : ruleList) {
             if (ruleId.equals(rule.getRuleId())) {
                 targetRule = rule;
                 break;
             }
         }
 
         if (targetRule != null) {
             ruleList.remove(targetRule);
             return targetRule;
         }
         return null;
     }
 }
