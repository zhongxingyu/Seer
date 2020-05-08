 package org.purl.rvl.interpreter.test;
 
 import static org.junit.Assert.*;
 
 import java.util.Random;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.ontoware.rdf2go.model.Model;
 import org.openrdf.rdf2go.StatementIterator;
 import org.purl.rvl.tooling.ModelBuilder;
 import org.purl.rvl.tooling.util.RVLUtils;
 
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntResource;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.rdf.model.impl.ModelCom;
 
 public class MappingModelTest {
 	
 	Model model;
 
 	@Before
 	public void setUp() throws Exception {
 		model = ModelBuilder.getModel();
 	}
 
 	//@Test
 	public void create10Mappings() {
 		
 		Random random = new Random();
 		
 		// create 10 new Mapping instances
 		for (int i = 0; i < 10; i++) {
 			
 			// create a new ID
 			int aID = random.nextInt(100000);
 			
 			org.purl.rvl.java.gen.rvl.Mapping mapping = 
 					new org.purl.rvl.java.gen.rvl.Mapping(
 							model,
 							"http://purl.org/rvl/example-mappings/Mapping" + aID,
 							true
 							);
 			mapping.setLabel("This is a new Mapping " + i);
 			mapping.setIncludeinlegend(true);
 			
 			System.out.println(mapping.getClass().getName() + ": " + mapping);
 			assertNotNull("Mapping " + mapping + " has no label" , mapping.getAllLabel_as().firstValue());
 		}
 		
 
 	}
 	
 	//@Test
 	public void testListAllMappings() {
 		RVLUtils.listAllMappings(model);
 	}
 	
 	@Test
 	public void testPrintMappingWithURI(){
 		RVLUtils.printMappingWithURI(model, "http://purl.org/rvl/example-mappings/PMwithNamedSubmappingToNamedMappingOnConnector");
		//RVLUtils.printMappingWithURI(model, "http://purl.org/rvl/example-mappings/PMwithAnonymousSubmappingToNamedMappingOnConnector");
 	}
 	
 
 /*
 		# sub-mapping - 
 		# PM with a sub-mapping to an existing, named mapping
 		# CHANGE IN LISTINGS: rdf:predicate, not rvl:predicate ; vg:targetAttribute vs vg:targetGraphicAttribute
 		rexm:PMwithNamedSubmappingOnConnector
 		      rdf:type rvl:PropertyMapping ;
 		      rdfs:label "PM with a sub-mapping to an existing, named mapping"^^xsd:string ;
 		      rvl:sourceProperty rexd:cites ;
 		      rvl:targetObjToObjRelation vg:Linking_Directed_Relation ; 
 		      rvl:subMapping [
 		        a rvl:SubMappingRelation ;
		        rvl:submapping_onRole vg:linking_connector;
		        rvl:submapping_onTriplePart rdf:predicate;
		        rvl:submapping_mapping rexm:PMfromID2ColorNamed;
 		      ]
 		      .
 */
 	
	//@Test
 	public void testJena(){
 		System.out.println("Access via Jena:");
 		System.out.println();
 		ModelCom jenaModelCom = (ModelCom) model.getUnderlyingModelImplementation();
 	
 		Property subMappingProperty =  jenaModelCom.getProperty("http://purl.org/rvl/subMapping");
		Property onTriplePartProperty =  jenaModelCom.getProperty("http://purl.org/rvl/submapping_onTriplePart");
		Property onRoleProperty =  jenaModelCom.getProperty("http://purl.org/rvl/submapping_onRole");
		Property mappingProperty =  jenaModelCom.getProperty("http://purl.org/rvl/submapping_mapping");
 		
 		Resource resource =  jenaModelCom.getResource("http://purl.org/rvl/example-mappings/PMwithNamedSubmappingToNamedMappingOnConnector");
 		Statement subMappingStatement =  jenaModelCom.getProperty(resource, subMappingProperty);
 		RDFNode subMappingNode = subMappingStatement.getObject();
 		
 		System.out.println("all statements with subject: " + subMappingNode);
 		StmtIterator it = jenaModelCom.listStatements(subMappingNode.asResource(), null, (RDFNode) null);
 		while (it.hasNext()) {
 			System.out.println("  "+it.next());
 		}
 		
 		System.out.println();
 		
 		Property p = onRoleProperty;
 		System.out.println("statements with subject: " + subMappingNode + " and property " + p);
 		StmtIterator it2 = jenaModelCom.listStatements(subMappingNode.asResource(), p, (RDFNode) null);
 		while (it2.hasNext()) {
 			System.out.println("  "+it2.next());
 		}
 		
 		System.out.println();
 		
 		//OntClass mappingClass = jenaModel.getOntClass("http://purl.org/rvl/example-mappings/PMwithNamedSubmappingToNamedMappingOnConnector");
 		//OntResource sourcePropertyValue = (OntResource) thisInstance.getPropertyValue(m.getProperty("http://purl.org/rvl/sourceProperty"));
 		System.out.println("Mapping: " + resource);
 		System.out.println("  SubMapping: " + subMappingNode.asResource());
 		System.out.println("    _onRole: " + jenaModelCom.getProperty(subMappingNode.asResource(), onRoleProperty).getObject());
 		System.out.println("    _mapping: " + jenaModelCom.getProperty(subMappingNode.asResource(), mappingProperty).getObject());
 		System.out.println("    _onTriplePart: " + jenaModelCom.getProperty(subMappingNode.asResource(), onTriplePartProperty).getObject());
 	}
 	
 	
 
 	
 }
