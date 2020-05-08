 package org.linkedgov.questions.model;
 
 import junit.framework.TestCase;
 
 public class QueryTest extends TestCase {
 
    public QueryTest(String name) {
        super(name);
    }
 
    /** Test the Query to String thing **/
    public void testThatFeature() throws Exception {
        Query q = new Query();
        q.setQuestionType(QuestionType.SELECT);
        assertEquals(q.toSparqlString(), "SELECT DISTINCT ?s ?o WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o} LIMIT 150");
    }
 
 }
