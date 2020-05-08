 package org.linkedgov.questions.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Pojo that represents a query, or question, built up by the user. 
  * 
  * @author Luke Wilson-Mawer <a href="http://viscri.co.uk/">Viscri</a> and 
  * @author <a href="http://mmt.me.uk/foaf.rdf#mischa">Mischa Tuffield</a> for LinkedGov
  * 
  */
 public class Query {
 
     private QuestionType questionType = QuestionType.SELECT;
 
     private String subject;
 
     private QueryFilter firstFilter = new QueryFilter();
 
     private QueryFilter secondFilter = new QueryFilter();
 
     public final static List<String> URI_PREFIXES;
     static {
         final ArrayList<String> lst = new ArrayList<String>();
         lst.add("http:");
         lst.add("ftp");
         lst.add("tag");
         lst.add("urn");
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
 
     public String toSparqlString() {
         StringBuilder query = new StringBuilder();
 
         if (questionType.equals(QuestionType.COUNT)) {
            query.append("SELECT (COUNT(?sub) AS ?cnt) ");
         } else {
             query.append("SELECT DISTINCT ?sub ?pred ?obj ");
         }
 
         query.append("WHERE { ");
 
         query.append("?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <");
         query.append(subject);
         query.append("> . ");
 
         query.append("?sub ?pred ?obj . ");
 
         if (!firstFilter.isNull()) {
             query.append(filterToSparqlBGP(firstFilter));
         }
         if (!secondFilter.isNull()) {
             query.append(filterToSparqlBGP(secondFilter));
         }
 
         query.append("} ");
 
         return query.toString();
     }
 
     public String filterToSparqlBGP(QueryFilter filter) {
         StringBuilder bgp = new StringBuilder();
         bgp.append("?sub <");
         bgp.append(filter.getPredicate());
         bgp.append("> ");
 
         boolean isURI = false;
         String object = filter.getObject();
         for (String prefix : URI_PREFIXES) {
             if (object.startsWith(prefix)) {
                 isURI = true;
                 break;
             }
         }
         if (isURI) {
             object = "<"+object+">";
         } else {
             object = "\""+object+"\"";
         }
         
         bgp.append(object);
 
         bgp.append(" . ");
 
         return bgp.toString();
     }
 }
