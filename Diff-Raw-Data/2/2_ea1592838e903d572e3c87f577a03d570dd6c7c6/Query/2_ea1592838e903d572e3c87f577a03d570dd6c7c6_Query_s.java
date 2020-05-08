 package org.linkedgov.questions.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
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
 
     /**
      * This enumerates a list of well known URI prefixes
      * 
      */
     public final static List<String> URI_PREFIXES;
     static {
         final ArrayList<String> lst = new ArrayList<String>();
         lst.add("http:");
         lst.add("ftp");
         lst.add("tag");
         lst.add("urn");
         lst.add("mailto");
         lst.add("tel");
         URI_PREFIXES = Collections.unmodifiableList(lst);
     };
     
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
         return buildSparqlString(overridenQuestionType);
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
         return buildSparqlString(questionType);
     }
 
     /**
      * 
      * @param thisQuestionType the question type of the query.
      * @return
      */
     private String buildSparqlString(QuestionType thisQuestionType) {
         StringBuilder query = new StringBuilder();        
         
         if (QuestionType.COUNT.equals(thisQuestionType)) {
             query.append("SELECT DISTINCT (COUNT(?sub) AS ?cnt) ");
         } else {
             query.append("SELECT DISTINCT ?sub ?pred ?obj ?slabel ?plabel ?olabel ");
         }
 
         query.append("WHERE { ");
 
         query.append("?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <");
         query.append(subject);
         query.append("> . ");
 
         query.append("?sub ?pred ?obj . ");
 
         if (!firstFilter.isComplete()) {
             query.append(filterToSparqlBGP(firstFilter));
         }
         if (!secondFilter.isComplete()) {
             query.append(filterToSparqlBGP(secondFilter));
         }
 
         if (questionType.equals(QuestionType.SELECT)) {
             query.append("OPTIONAL {?sub <http://www.w3.org/2000/01/rdf-schema#label> ?slabel } . ");
             query.append("OPTIONAL {?pred <http://www.w3.org/2000/01/rdf-schema#label> ?plabel } . ");
             query.append("OPTIONAL {?obj <http://www.w3.org/2000/01/rdf-schema#label> ?olabel } . ");
         }
          
         if (predicate != null) {
             query.append("FILTER (?pred = <");
             query.append(predicate);
             query.append(">) . ");
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
     public String filterToSparqlBGP(QueryFilter filter) {
         StringBuilder bgp = new StringBuilder();
         bgp.append("?sub <");
         bgp.append(filter.getPredicate());
         bgp.append("> ");
         
         String object = filter.getObject();
 
         if (isURI(object)) {
             object = "<"+object+">";
         /**
          * 4store specific hack, and the only one in the code
          * This is where we skolemise the 4store styled bnode identifier       
          */
         } else if (object.startsWith("b") && object.length() > 16 && !object.contains(" ")) {
             object = "<bnode:"+object+">";
         } else {
             if (isInteger(object)) {
                object = "?obj . FILTER (?obj = \""+object+"\"^^<http://www.w3.org/2001/XMLSchema#int> || ?obj = \""+object+"\")";
             } else if (isFloat(object)) {
                 object = "?obj . FILTER (?obj = \""+object+"\"^^<http://www.w3.org/2001/XMLSchema#float> || ?obj = \""+object+"\")";
             } else {
                 object = "?obj . FILTER (?obj = \""+object+"\" || ?obj = \""+object+"\"@EN || ?obj = \""+object+"\"@en)";        
             }
         }
         bgp.append(object);
         bgp.append(" . ");
 
         return bgp.toString();
     }
 
     /**
      * 
      * @param input a literal value returned by 4store
      * @return checks whether or not it is a URI
      */
     public boolean isURI (String input) {
         boolean isURI = false;
         for (String prefix : URI_PREFIXES) {
             if (input.startsWith(prefix)) {
                 isURI = true;
                 break;
             }
         }
         return isURI;
     }
     
     /**
      * 
      * @param input a literal 
      * @return whether or not it is an Integer
      */
     public boolean isInteger(String input) {  
        try {  
           Integer.parseInt(input);  
           return true;  
        } catch(Exception e) {  
           return false;  
        }  
     } 
     
     /**
      * 
      * @param input which is a literal value
      * @return a boolean stating with it is a floating point number or not
      */
     public boolean isFloat (String input) {
         try {
             Float.parseFloat(input);
             return true;
         } catch (Exception e) {
             return false;
         }
     }
     
 
     public void setPredicate(String predicate) {
         this.predicate = predicate;
     }
 
     public String getPredicate() {
         return predicate;
     }
 
 }
