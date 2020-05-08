 package org.linkedgov.questions.model;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Pojo that represents a query, or question, built up by the user. 
  * This class has a function which turns the user input into a SPARQL query
  * 
  * @author Luke Wilson-Mawer <a href="http://viscri.co.uk/">Viscri</a> and 
  * @author <a href="http://mmt.me.uk/foaf.rdf#mischa">Mischa Tuffield</a> for LinkedGov
  * 
  */
 public class Query {
 
     /**
      * Currently one of SELECT or SELECT COUNT 
      */
     private QuestionType questionType = QuestionType.SELECT;
 
     /**
      * This is the predicate the user has chosen to filter on. 
      * 
      * If blank, the user wants everything.
      */
     private String predicate;
 
     /**
      * This is the rdf:type of the sub, 
      * i.e. http://xmlns.com/foaf/0.1/Person for example
      * 
      */
     private String subject;
 
     /**
      * These are the two filters which the user can 
      * currently add to narrow down their search results 
      * 
      */
     private QueryFilter firstFilter = new QueryFilter();
     private QueryFilter secondFilter = new QueryFilter();
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public void setQuestionType(QuestionType questionType) {
         this.questionType = questionType;
     }
 
     public QuestionType getQuestionType() {
         return questionType;
     }
 
     public void setSecondFilter(QueryFilter secondFilter) {
         this.secondFilter = secondFilter;
     }
 
     public QueryFilter getSecondFilter() {
         return secondFilter;
     }
 
     public void setFirstFilter(QueryFilter firstFilter) {
         this.firstFilter = firstFilter;
     }
 
     public QueryFilter getFirstFilter() {
         return firstFilter;
     }
 
     public boolean isNull() {
         if (subject == null) {
             return true;
         }
 
         return false;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("subject:[");
         sb.append(subject);
         sb.append("] predicate:[");
         sb.append(predicate);
         sb.append("] firstFilter:[");
         sb.append(firstFilter.toString());
         sb.append("] secondFilter:[");
         sb.append(secondFilter.toString());
         sb.append("]");
         return sb.toString();
     }
 
     /**
      * This function is used to turn a given instance of 
      * a Query class into a SPARQL query, allowing the caller to override the question type of the query.
      * 
      * @param overridenQuestionType - the questionType to override when building the query.
      * 
      * @return A Sparql Query String
      */
     public String toSparqlString(QuestionType overridenQuestionType) {
         if (StringUtils.isBlank(predicate)) {
             return buildSparqlStringWithoutPredicate(overridenQuestionType);
         } else {
             return buildSparqlWithPredicate(overridenQuestionType);
         }    
     }
 
     /**
      * This function is used to turn a given instance of 
      * a Query class into a SPARQL query. 
      * 
      * There are unit tests for this function in the repo
      * 
      * @return A Sparql Query String
      */
     public String toSparqlString() {
         if (StringUtils.isBlank(predicate)) {
             return buildSparqlStringWithoutPredicate(questionType);
         } else {
             return buildSparqlWithPredicate(questionType);
         }
     }
 
     private String buildSparqlWithPredicate(QuestionType thisQuestionType) {
         StringBuilder query = new StringBuilder();        
 
         if (QuestionType.COUNT.equals(thisQuestionType)) {
            query.append("SELECT DISTINCT (COUNT(*) AS ?cnt) ");
         } else {
             query.append("SELECT DISTINCT ?sub (<");
             query.append(predicate);
             query.append("> AS ?pred) ?obj ?slabel ?plabel ?olabel ");
 
         }
 
         query.append("WHERE { ");
 
         query.append("?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <");
         query.append(subject);
         query.append("> . ");
 
         query.append("?sub <"+predicate+"> ?obj . ");
 
         if (!firstFilter.isComplete()) {
             query.append(filterToSparqlBGP(firstFilter, "?obj2"));
         }
         if (!secondFilter.isComplete()) {
             query.append(filterToSparqlBGP(secondFilter, "?obj3"));
         }
 
         if (QuestionType.SELECT.equals(thisQuestionType)) {
             query.append("OPTIONAL {?sub <http://www.w3.org/2000/01/rdf-schema#label> ?slabel } . ");
             query.append("OPTIONAL {<");
             query.append(predicate);
             query.append("> <http://www.w3.org/2000/01/rdf-schema#label> ?plabel } . ");
             query.append("OPTIONAL {?obj <http://www.w3.org/2000/01/rdf-schema#label> ?olabel } . ");
         }
 
         query.append("} ");
         return query.toString();
     }
 
 
     /**
      * 
      * @param thisQuestionType the question type of the query.
      * @return
      */
     private String buildSparqlStringWithoutPredicate(QuestionType thisQuestionType) {
         StringBuilder query = new StringBuilder();        
 
         if (QuestionType.COUNT.equals(thisQuestionType)) {
            query.append("SELECT DISTINCT (COUNT(*) AS ?cnt) ");
         } else {
             query.append("SELECT DISTINCT ?sub ?pred ?obj ?slabel ?plabel ?olabel ");
         }
 
         query.append("WHERE { ");
 
         query.append("?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <");
         query.append(subject);
         query.append("> . ");
 
        query.append("?sub ?pred ?obj . ");
 
         if (!firstFilter.isComplete()) {
             query.append(filterToSparqlBGP(firstFilter, "?obj2"));
         }
         if (!secondFilter.isComplete()) {
             query.append(filterToSparqlBGP(secondFilter, "?obj3"));
         }
 
         if (QuestionType.SELECT.equals(thisQuestionType)) {
             query.append("OPTIONAL {?sub <http://www.w3.org/2000/01/rdf-schema#label> ?slabel } . ");
             query.append("OPTIONAL {?pred <http://www.w3.org/2000/01/rdf-schema#label> ?plabel } . ");
             query.append("OPTIONAL {?obj <http://www.w3.org/2000/01/rdf-schema#label> ?olabel } . ");
         }
         
         query.append("} ");
         return query.toString();
     }
 
     /**
      * This function takes a filter and turns it into a 
      * single SPARQL Basic Graph Pattern (BGP)
      * @param filter
      * @return a fragment of a SPARQL query
      */
     public String filterToSparqlBGP(QueryFilter filter, String variableName) {
         StringBuilder bgp = new StringBuilder();
 
         String object = filter.getObject();
         if (SparqlUtils.isBnode(object)) {
             bgp.append("?sub <");
             bgp.append(filter.getPredicate());
             bgp.append("> ");
             bgp.append("<bnode:"+object+"> . ");
         } else if (SparqlUtils.isURI(object)) {
             bgp.append("?sub <");
             bgp.append(filter.getPredicate());
             bgp.append("> ");
             bgp.append("<"+object+"> . ");
         } else  {
             if (SparqlUtils.isInteger(object)) {
                 bgp.append("?sub <");
                 bgp.append(filter.getPredicate());
                 bgp.append("> ");                
                 bgp.append(variableName);
                 bgp.append(" . ");
                 bgp.append("FILTER (");
                 bgp.append(variableName);
                 bgp.append(" = \""+object+"\"^^<http://www.w3.org/2001/XMLSchema#integer> || ");
                 bgp.append(variableName);
                 bgp.append(" = \""+object+"\") . ");
             } else if (SparqlUtils.isFloat(object)) {
                 bgp.append("?sub <");
                 bgp.append(filter.getPredicate());
                 bgp.append("> ");                
                 bgp.append(variableName);
                 bgp.append(" . ");
                 bgp.append("FILTER (");
                 bgp.append(variableName);
                 bgp.append(" = \""+object+"\"^^<http://www.w3.org/2001/XMLSchema#float> || ");
                 bgp.append(variableName);
                 bgp.append(" = \""+object+"\") . ");
             } else {
                 bgp.append("?sub <");
                 bgp.append(filter.getPredicate());
                 bgp.append("> ");                
                 bgp.append(variableName);
                 bgp.append(" . ");
                 bgp.append("FILTER (");
                 bgp.append(variableName);
                 bgp.append(" = \""+object+"\"@EN || ");
                 bgp.append(variableName);
                 bgp.append(" = \""+object+"\"@en || ");
                 bgp.append(variableName);
                 bgp.append(" = \""+object+"\") . ");
             }
         }
 
         return bgp.toString();
     }
 
     public void setPredicate(String predicate) {
         this.predicate = predicate;
     }
 
     public String getPredicate() {
         return predicate;
     }
 
 }
