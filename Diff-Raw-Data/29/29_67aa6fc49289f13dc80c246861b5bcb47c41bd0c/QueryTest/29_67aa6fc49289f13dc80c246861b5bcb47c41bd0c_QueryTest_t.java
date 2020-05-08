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
         
        Query qone = new Query();
        qone.setQuestionType(QuestionType.SELECT);
        qone.setSubject("http://xmlns.com/foaf/0.1/Person");
        qone.setFirstFilter(first);                
        assertEquals(qone.toSparqlString(), "SELECT DISTINCT ?sub ?pred ?obj WHERE { ?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . ?sub ?pred ?obj . ?sub <http://xmlns.com/foaf/0.1/interest> <http://dbpedia.org/page/Beer> . } ");

        Query qtwo = new Query();
        qtwo.setQuestionType(QuestionType.COUNT);
        qtwo.setSubject("http://xmlns.com/foaf/0.1/Person");
        qtwo.setFirstFilter(first);                

        
        System.err.println(qtwo.toSparqlString());
        assertEquals(qtwo.toSparqlString(), "SELECT DISTINCT (COUNT(?sub) AS ?cnt) WHERE { ?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . ?sub ?pred ?obj . ?sub <http://xmlns.com/foaf/0.1/interest> <http://dbpedia.org/page/Beer> . } ");

     }
 
 }
