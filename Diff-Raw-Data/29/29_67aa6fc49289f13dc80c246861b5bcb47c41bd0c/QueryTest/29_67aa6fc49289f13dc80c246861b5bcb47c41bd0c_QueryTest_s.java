 package org.linkedgov.questions.model;
 
 import junit.framework.TestCase;
 
 public class QueryTest extends TestCase {
 
     public QueryTest(String name) {
         super(name);
     }
 
     /** Test the Query to String thing **/
     public void testThatFeature() throws Exception {
         
         QueryFilter first = new QueryFilter();
         first.setPredicate("http://xmlns.com/foaf/0.1/interest");
         first.setObject("http://dbpedia.org/page/Beer");
         
        Query q = new Query();
       
        q.setQuestionType(QuestionType.SELECT);
        q.setSubject("http://xmlns.com/foaf/0.1/Person");
        q.setFirstFilter(first);
                
        assertEquals(q.toSparqlString(), "SELECT DISTINCT ?sub ?pred ?obj WHERE { ?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . ?sub ?pred ?obj . ?sub <http://xmlns.com/foaf/0.1/interest> <http://dbpedia.org/page/Beer> . } ");
     }
 
 }
