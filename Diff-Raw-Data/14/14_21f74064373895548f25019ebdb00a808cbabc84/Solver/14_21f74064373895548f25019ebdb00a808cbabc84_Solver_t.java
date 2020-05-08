 package lois.lab1.inference;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import lois.lab1.datastructure.AtomSign;
 import lois.lab1.datastructure.AtomSignType;
 import lois.lab1.datastructure.Goal;
 import lois.lab1.datastructure.KnowledgeBase;
 import lois.lab1.datastructure.Pair;
 import lois.lab1.datastructure.Predicate;
 import lois.lab1.datastructure.Rule;
 import lois.lab1.datastructure.TreeNode;
 
 /**
  * Class that execute logical inference.
  *
  * @author Q-YAA
  */
 public class Solver {
 
     private KnowledgeBase knowledgeBase;
 
     public Solver(KnowledgeBase knowledgeBase) {
         this.knowledgeBase = knowledgeBase;
     }
 
     /**
      * Creates tree of the logical inference.
      *
      * @param goal inference goal
      * @return list of the root node from inference tree for every goal term
      */
     public List<TreeNode> solve(Goal goal) {
         List<TreeNode> resultList = new ArrayList<TreeNode>();
 
         for (Predicate predicate : goal.getGoalTermList()) {
 
             TreeNode rootNode =
                solveRec(new TreeNode(TreeNode.OR_TYPE, null, predicate), predicate, 0, new ArrayList<String>());
 
             rootNode.calculateRelationTable();
             resultList.add(rootNode);
             System.out.println(rootNode.printInferenceTree());
         }
 
         return resultList;
     }
 
    public TreeNode solveRec(TreeNode currentNode, Predicate predicate, int level, List<String> usedPredicates) {
 
         // find contradictions
         List<Predicate> contradictionList = knowledgeBase.findLogicallySameFacts(predicate);
         for (Predicate contradiction : contradictionList) {
             currentNode.setType(TreeNode.AND_TYPE);
             Pair<List<AtomSign>, List<AtomSign>> row = createRowForContradiction(predicate, contradiction);
             currentNode.getRelationTable().addRow(row.getFirst(), row.getSecond());
         }
 
         // prevents recursion
         if (usedPredicates.contains(predicate.getSign())) {
             return currentNode;
         }
         usedPredicates.add(predicate.getSign());
 
         // apply rules
         List<Rule> ruleList = knowledgeBase.findRuleForPredicate(predicate);
         for (Rule rule : ruleList) {
             Rule unifiedRule = unifyRule(predicate, rule);
 
             if (unifiedRule == null) {
                 continue;
             }
 
             // append unified consequent of the used rule
             TreeNode unifiedNode = new TreeNode(TreeNode.AND_TYPE, currentNode, unifiedRule.getConsequent());
             currentNode.addChild(unifiedNode);
             currentNode.setType(TreeNode.OR_TYPE);
 
             // append all unified reason of the used rule
             for (Predicate reason : unifiedRule.getReason()) {
                 TreeNode reasonNode = new TreeNode(TreeNode.AND_TYPE, unifiedNode, reason);
                 unifiedNode.addChild(reasonNode);
                 usedPredicates.add(unifiedRule.getConsequent().getSign());
                solveRec(reasonNode, reason, level + 1, usedPredicates);
                 usedPredicates.remove(unifiedRule.getConsequent().getSign());
             }
         }
 
         // apply similarity relation
         List<Pair<Predicate, String>> similarPredicatePairList = knowledgeBase.findAllSimilarPredicates(predicate);
         for (Pair<Predicate, String> similarPredicatePair : similarPredicatePairList) {
             List<Rule> rulesForSimilarNode = knowledgeBase.findRuleForPredicate(similarPredicatePair.getFirst());
 
             for (Rule rule : rulesForSimilarNode) {
                 Rule similarRule = knowledgeBase.createSimilarRule(rule, similarPredicatePair.getSecond());
 
                 if (similarRule == null) {
                     continue;
                 }
 
                 Rule unifiedSimilarRule = unifyRule(predicate, similarRule);
 
                 if (unifiedSimilarRule == null) {
                     continue;
                 }
 
                 // append unified consequent of the used similar rule
                 TreeNode unifiedSimilarNode = new TreeNode(TreeNode.AND_TYPE, currentNode, unifiedSimilarRule.getConsequent());
                 currentNode.addChild(unifiedSimilarNode);
                 currentNode.setType(TreeNode.OR_TYPE);
 
                 // append all unified reason of the used similar rule
                 for (Predicate similarReason : unifiedSimilarRule.getReason()) {
                     TreeNode similarReasonNode = new TreeNode(TreeNode.AND_TYPE, unifiedSimilarNode, similarReason);
                     unifiedSimilarNode.addChild(similarReasonNode);
                     usedPredicates.add(unifiedSimilarRule.getConsequent().getSign());
                    solveRec(similarReasonNode, similarReason, level + 1, usedPredicates);
                     usedPredicates.remove(unifiedSimilarRule.getConsequent().getSign());
                 }
 
                 // append similar predicate
                 TreeNode similarPredicateNode =
                     new TreeNode(TreeNode.AND_TYPE, unifiedSimilarNode, similarPredicatePair.getFirst());
                 similarPredicateNode.setSimilarityName(similarPredicatePair.getSecond());
                 unifiedSimilarNode.addChild(similarPredicateNode);
                solveRec(similarPredicateNode, similarPredicatePair.getFirst(), level + 1, usedPredicates);
             }
         }
 
         usedPredicates.remove(predicate.getSign());
         return currentNode;
     }
 
     /**
      * Creates row for relation table from predicate and them contradiction.
      *
      * @param predicate predicate to build row of the relation table
      * @param contradiction contradiction of the given predicate
      * @return row for relation table (first element - titles of the row, second element - values of the row)
      */
     private Pair<List<AtomSign>, List<AtomSign>> createRowForContradiction(Predicate predicate, Predicate contradiction) {
         Pair<List<AtomSign>, List<AtomSign>> row =
             new Pair<List<AtomSign>, List<AtomSign>>(new ArrayList<AtomSign>(), new ArrayList<AtomSign>());
 
         for (int i = 0; i < predicate.getArgumentList().size(); i++) {
 
             if (predicate.getArgumentList().get(i).getType() == AtomSignType.VAR) {
                 row.getFirst().add(predicate.getArgumentList().get(i));
                 row.getSecond().add(contradiction.getArgumentList().get(i));
             }
         }
 
         return row;
     }
 
     /**
      * Method for the rule unification.
      *
      * @param predicate predicate for which it unified.
      * @param rule rule to unify
      * @return unified rule or null if unification impossible
      */
     private Rule unifyRule(Predicate predicate, Rule rule) {
         List<Predicate> resultList = new ArrayList<Predicate>();
         Predicate ruleConsequent = rule.getConsequent();
 
         if (!ruleConsequent.getSign().equals(predicate.getSign())) {
             return null;
         }
 
         UnificationGraph unificationGraph = UnificationGraph.create(predicate, ruleConsequent);
         Unificator unificator = unificationGraph.buildUnificator();
 
         if (unificator == null) {
             return null;
         }
 
         Predicate ruleConsequentUnification = unificator.getUnificationFor(ruleConsequent);
         Predicate predicateUnification = unificator.getUnificationFor(predicate);
 
         if (!ruleConsequentUnification.isLogicallyEquivalent(predicateUnification)) {
             return null;
         }
 
         for (Predicate reasonPredicate : rule.getReason()) {
             resultList.add(unificator.getUnificationFor(reasonPredicate));
         }
 
         return new Rule(ruleConsequentUnification, resultList);
     }
 }
