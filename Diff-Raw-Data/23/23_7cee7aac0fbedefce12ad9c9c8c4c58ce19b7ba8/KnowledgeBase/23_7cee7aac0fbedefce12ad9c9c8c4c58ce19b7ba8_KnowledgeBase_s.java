 package lois.lab1.datastructure;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import lois.lab1.inference.UnificationGraph;
 import lois.lab1.inference.Unificator;
 
 /**
  * Class that represent knowledge base.
  *
  * @author Svet
  */
 public class KnowledgeBase {
 
     /**
      * Set of facts in the knowledge base.
      */
     private List<Predicate> predicateList = new ArrayList<Predicate>();
 
     /**
      * Set of similarity relations in the knowledge base.
      */
     private List<SimilarityRelation> similarityRelationList = new ArrayList<SimilarityRelation>();
 
     /**
      * Set of rule in the knowledge base.
      */
     private List<Rule> ruleList = new ArrayList<Rule>();
 
     private static KnowledgeBase knowledgeBase;
 
     private KnowledgeBase() {
     }
 
     public static KnowledgeBase getInstance() {
         if (knowledgeBase == null) {
             knowledgeBase = new KnowledgeBase();
         }
 
         return knowledgeBase;
     }
 
     public void addPredicate(Predicate predicate) {
         predicateList.add(predicate);
     }
 
     public void addSimilarityRelation(SimilarityRelation similarityRelation) {
         similarityRelationList.add(similarityRelation);
     }
 
     public void addRule(Rule rule) {
         ruleList.add(rule);
     }
 
     /**
      * Finds similarity relation in the knowledge base by the given sign.
      *
      * @param sign sing of the similarity relation
      * @return list of the similarity relation with the given sign
      */
     public List<SimilarityRelation> getSimilarityRelationBySign(String sign) {
         List<SimilarityRelation> resultSimilarityRelationList = new ArrayList<SimilarityRelation>();
 
         for (SimilarityRelation similarityRelation : similarityRelationList) {
 
             if (similarityRelation.getSign().equals(sign)) {
                 resultSimilarityRelationList.add(similarityRelation);
             }
         }
 
         return resultSimilarityRelationList;
     }
 
     /**
      * Return all available names of the similarity relation from the knowledge base.
      *
      * @return list of the similarity relation names
      */
     public List<String> getAllNameOfSimilarityRelations() {
         List<String> similarityRelationNameList = new ArrayList<String>();
 
         for (SimilarityRelation similarityRelation : similarityRelationList) {
 
             if (!similarityRelationNameList.contains(similarityRelation.getSign())) {
                 similarityRelationNameList.add(similarityRelation.getSign());
             }
         }
 
         return similarityRelationNameList;
     }
 
     /**
      * Method that find all possible logically equivalent predicates in knowledge base.
      *
      * <p>
      * For the predicate unification used
      * {@link lois.lab1.inference.UnificationGraph} and {@link lois.lab1.inference.Unificator}
      * </p>
      *
      * @param predicate predicate to find logically the same predicates
      * @return logically equivalent predicates
      */
     public List<Predicate> findLogicallySameFacts(Predicate predicate) {
         List<Predicate> resultPredicateList = new ArrayList<Predicate>();
 
         for (Predicate factPredicate : getPredicateList()) {
             UnificationGraph unificationGraph = UnificationGraph.create(factPredicate, predicate);
             Unificator unificator = unificationGraph.buildUnificator();
 
             if (unificator != null && unificator.getUnificationFor(predicate).isLogicallyEquivalent(factPredicate)) {
                 resultPredicateList.add(factPredicate);
             }
         }
 
         return resultPredicateList;
     }
 
     /**
      * Finds atom sign that's similar to the given atom sign (find by the sign).
      *
      * @param atomSign atom sign to find similar
      * @param similarityRelationName name of the similarity relation
      * @return similar atom sign
      */
     public AtomSign findSimilarSign(AtomSign atomSign, String similarityRelationName) {
 
         for (SimilarityRelation similarityRelation : getSimilarityRelationBySign(similarityRelationName)) {
 
             if (similarityRelation.getArgumentList().get(0).getSign().equals(atomSign.getSign())) {
                 return similarityRelation.getArgumentList().get(1);
             }
             else if (similarityRelation.getArgumentList().get(1).getSign().equals(atomSign.getSign())) {
                 return similarityRelation.getArgumentList().get(0);
             }
         }
 
         return null;
     }
 
     /**
      * Find all similar predicates from all similarity relation.
      *
      * @param predicate predicate to find similar
      * @return list of the similar predicates with them name of the similar relations
      */
     public List<Pair<Predicate, String>> findAllSimilarPredicates(Predicate predicate) {
 
         Set<Pair<Predicate, String>> similarPredicates = new HashSet<Pair<Predicate, String>>();
 
         for (SimilarityRelation similarityRelation : getSimilarityRelationList()) {
 
             if (isSimilarExist(predicate, similarityRelation.getSign())) {
                 Predicate similarPredicate = createSimilarPredicate(predicate, similarityRelation.getSign());
                 similarPredicates.add(new Pair<Predicate, String>(similarPredicate, similarityRelation.getSign()));
             }
         }
 
         return new ArrayList<Pair<Predicate, String>>(similarPredicates);
     }
 
     /**
      * Create predicate that similar to the given predicate.
      *
      * <p> If similar predicate doesn't exist returns null. </p>
      *
      * @param predicate predicate to find similar
      * @param similarityRelationName name of similarity relation
      * @return similar predicate or null
      */
     public Predicate createSimilarPredicate(Predicate predicate, String similarityRelationName) {
 
         if (!isSimilarExist(predicate, similarityRelationName)) {
             return null;
         }
 
         List<AtomSign> newArgumentList = new ArrayList<AtomSign>();
 
         for (AtomSign argument : predicate.getArgumentList()) {
 
             if (argument.getType() == AtomSignType.CONST) {
                 newArgumentList.add(new Constant(findSimilarSign(argument, similarityRelationName).getSign()));
             }
             else if (argument.getType() == AtomSignType.VAR) {
                 newArgumentList.add(new Variable(argument.getSign()));
             }
         }
 
         return new Predicate(findSimilarSign(predicate, similarityRelationName).getSign(), newArgumentList);
     }
 
 
     /**
      * Determines if the similar predicate exist in knowledge base.
      *
      * @param predicate predicate to check similar
      * @param similarityRelationName name of similarity relation
      * @return true - if similar exist, false - in the other case.
      */
     public boolean isSimilarExist(Predicate predicate, String similarityRelationName) {
 
         boolean isSimilarExist = findSimilarSign(predicate, similarityRelationName) != null;
 
         for (AtomSign argument : predicate.getArgumentList()) {
             AtomSign similar = findSimilarSign(argument, similarityRelationName);
 
             if (argument.getType() == AtomSignType.CONST && similar == null) {
                 isSimilarExist = false;
                 break;
             }
         }
 
         return isSimilarExist;
     }
 
     /**
      * Determines if first sign similar to the second sign with similarity relation with the given name.
      *
      * @param first first atom sign
      * @param second second atom sign
      * @param similarityName name of the similarity relation
      * @return true - if first similar to the second sign, false in the other case
      */
     public boolean isSignSimilar(AtomSign first, AtomSign second, String similarityName) {
 
         for (SimilarityRelation similarityRelation : getSimilarityRelationBySign(similarityName)) {
             boolean isSimilar = (similarityRelation.getArgumentList().get(0).equals(first)
                 && similarityRelation.getArgumentList().get(1).equals(second))
                 || (similarityRelation.getArgumentList().get(0).equals(second)
                 && similarityRelation.getArgumentList().get(1).equals(first));
 
             if (isSimilar) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Creates similar rule for the given similarity relation.
      *
      * @param rule rule to create similar
      * @param similarityName name of the similarity relation
      * @return similar rule or null
      */
     public Rule createSimilarRule(Rule rule, String similarityName) {
         Predicate similarConsequent = createSimilarPredicate(rule.getConsequent(), similarityName);
 
         if (similarConsequent == null) {
             return null;
         }
 
         List<Predicate> similarReason = new ArrayList<Predicate>();
 
         for (Predicate predicate : rule.getReason()) {
             Predicate similarPredicate = createSimilarPredicate(predicate, similarityName);
 
             if (similarPredicate == null) {
                 return null;
             }
 
             similarReason.add(similarPredicate);
         }
 
         return new Rule(similarConsequent, similarReason);
     }
 
     /**
     * Creates similar rule for the every rule and every similarity relation.
     *
     * @return pair list of the similar rule.
     *         First element in the pair - similar rule, second - name of the similarity relation.
     */
    public List<Pair<Rule, String>> createAllSimilarRule() {
        List<Pair<Rule, String>> similarityRuleList = new ArrayList<Pair<Rule, String>>();

        for (String similarityRelationName : getAllNameOfSimilarityRelations()) {

            for (Rule rule : getRuleList()) {
                Rule similarRule = createSimilarRule(rule, similarityRelationName);

                if (similarRule != null) {
                    similarityRuleList.add(new Pair<Rule, String>(similarRule, similarityRelationName));
                }
            }
        }

        return similarityRuleList;
    }

    /**
      * Find all rule from the knowledge base where the given predicate it's consequent.
      *
      * @param predicate predicate to find rule
      * @return list of the found rule
      */
     public List<Rule> findRuleForPredicate(Predicate predicate) {
         List<Rule> resultList = new ArrayList<Rule>();
 
         for (Rule rule : getRuleList()) {
 
             if (rule.getConsequent().getSign().equals(predicate.getSign())) {
                 resultList.add(rule);
             }
         }
 
         return resultList;
     }
 
     /**
      * Verify knowledge base.
      *
      * @return list of the found errors (empty list if errors not found)
      */
     public List<String> verifyKnowledgeBase() {
         List<String> errorList = new ArrayList<String>();
         errorList.addAll(checkPredicateList(predicateList));
         errorList.addAll(checkForErrors(knowledgeBase));
 
         return errorList;
     }
 
     public List<Predicate> getPredicateList() {
         return predicateList;
     }
 
     public List<Rule> getRuleList() {
         return ruleList;
     }
 
     public List<SimilarityRelation> getSimilarityRelationList() {
         return similarityRelationList;
     }
 
     @Override
     public String toString() {
         return "KnowledgeBase{" +
             "predicateList=" + predicateList +
             ", similarityRelationList=" + similarityRelationList +
             ", ruleList=" + ruleList +
             '}';
     }
 
     /**
      * Check quantity of the arguments in the predicate with the same name.
      *
      * @return List list of the found errors (empty list if errors not found)
      */
     private List<String> checkPredicateList(List<Predicate> predicateList) {
         List<String> errorList = new ArrayList<String>();
 
         for (Predicate firstPredicate : predicateList) {
             for (Predicate secondPredicate : predicateList) {
 
                 if (firstPredicate.getSign().equals(secondPredicate.getSign())
                     && firstPredicate.getArgumentList().size() != secondPredicate.getArgumentList().size()) {
 
                     errorList.add("Incompatible predicates: " + firstPredicate + " and " + secondPredicate);
                 }
             }
         }
 
         return errorList;
     }
 
     private List<String> checkForErrors(KnowledgeBase knowledgeBase) {
         List<String> errorList = new ArrayList<String>();
 
         for (Predicate predicate : knowledgeBase.getPredicateList()) {
 
             if (predicate == null) {
                 errorList.add("Base verification error! Predicate must not be null");
                 continue;
             }
 
             for (AtomSign argument : predicate.getArgumentList()) {
                 if (argument == null) {
                     errorList.add("Base verification error! Predicate argument must not be null");
                 }
             }
         }
 
         if (!errorList.isEmpty()) {
             return errorList;
         }
 
         for (Rule rule : knowledgeBase.getRuleList()) {
 
             if (rule == null) {
                 errorList.add("Base verification error! Rule must not be null");
                 continue;
             }
 
             if (rule.getConsequent() == null) {
                 errorList.add("Base verification error! Rule consequent must not be null");
             }
 
             for (AtomSign reason : rule.getReason()) {
                 if (reason == null) {
                     errorList.add("Base verification error! Rule reason must not be null");
                 }
             }
         }
 
         if (!errorList.isEmpty()) {
             return errorList;
         }
 
         for (SimilarityRelation similarityRelation : knowledgeBase.getSimilarityRelationList()) {
 
             if (similarityRelation == null) {
                 errorList.add("Base verification error! Similarity relation must not be null");
                 continue;
             }
 
             for (AtomSign argument : similarityRelation.getArgumentList()) {
                 if (argument == null) {
                     errorList.add("Base verification error! Similarity relation argument must not be null");
                 }
             }
         }
 
         return errorList;
     }
 }
 
