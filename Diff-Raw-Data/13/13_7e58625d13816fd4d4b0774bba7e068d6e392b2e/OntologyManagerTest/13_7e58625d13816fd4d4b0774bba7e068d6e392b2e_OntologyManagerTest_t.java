 package com.admin.test;
 
 import static org.junit.Assert.*;
 
 import java.util.TreeSet;
 
 
 import org.junit.Test;
 
 import com.admin.domain.Statement;
 import com.admin.owlmanager.OntologyManager;
 
 public class OntologyManagerTest {
 
 	@Test
 	public final void getClassesTest() {
 		OntologyManager om = new OntologyManager();
 		TreeSet<Statement> classes = om.getClasses();
 		
 		assertTrue("ERROR:", classes.size() == 11);
 	}
 	
 	@Test
 	public final void oneResultQueryTest() {
 		OntologyManager om = new OntologyManager();
 		String conditions = "?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://localhost/ontologies/ThesisOntology.owl#KnowledgeArea> . " +
 							"?s <http://localhost/ontologies/ThesisOntology.owl#in-discipline> <http://localhost/ontologies/ThesisOntology.owl#ComputerScience>";
 		TreeSet<Statement> result = om.oneResultQuery("?s", conditions);
 		
 		assertTrue("Query Error", result.size() == 14);
 	}
 	
 	@Test
 	public final void addResourceTest() {
 		OntologyManager om = new OntologyManager();
 		String[] annotations = {"http://localhost/ontologies/ThesisOntology.owl#DataLinkLayerConcepts"};
 		boolean result = om.addResource("http://iferminmontilla.net", annotations);
 		String[] annotations2 = {"http://localhost/ontologies/ThesisOntology.owl#DatabaseModeling", 
 				"http://localhost/ontologies/ThesisOntology.owl#FundamentalDataStructures", 
 				"http://localhost/ontologies/ThesisOntology.owl#DatabaseSystems"};
		boolean result2 = new OntologyManager().addResource("http://www.w3c.org", annotations2);
 		assertTrue("Resource not added", result && result2);
 	}
	
	@Test
	public final void deleteResourceTest() {
		OntologyManager om = new OntologyManager();
		boolean result = om.deleteResource("http://iferminmontilla.net");
		boolean result2 = om.deleteResource("http://www.w3c.org");
		
		assertTrue("Errors deleting requested resource", result && result2);
	}
 }
