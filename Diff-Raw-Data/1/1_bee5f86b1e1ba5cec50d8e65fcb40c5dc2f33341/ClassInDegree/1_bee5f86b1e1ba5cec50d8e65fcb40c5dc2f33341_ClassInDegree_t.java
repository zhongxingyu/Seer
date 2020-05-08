 package com.karlhammar.ontometrics.plugins.axiomatic;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.Selector;
 import com.hp.hpl.jena.rdf.model.SimpleSelector;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.karlhammar.ontometrics.plugins.api.OntoMetricsPlugin;
 
 public class ClassInDegree implements OntoMetricsPlugin {
 
 	private Logger logger = Logger.getLogger(getClass().getName());
 	private StructuralSingleton ss;
 	
 	public String getName() {
 		return "Class in-degree plugin";
 	}
 
 	public void init(File ontologyFile) {
 		ss = StructuralSingleton.getSingletonObject(ontologyFile);
 
 	}
 
 	public String getMetricAbbreviation() {
 		return "CInDegree";
 	}
 
 	public String getMetricValue(File ontologyFile) {
 		if (null == ss) {
 			logger.info("getMetricValue called before init()!");
 			init(ontologyFile);
 		}
 		OntModel ontology = ss.getOntology();
 		ExtendedIterator<OntClass> allClasses = ontology.listClasses();
 		int nrNamedClasses = 0;
 		int inEdges = 0;
 		while (allClasses.hasNext()) {
 			OntClass c = allClasses.next();
 			if (!c.isAnon()) {
 				Selector selector = new SimpleSelector(null, null, c);
 				StmtIterator iter = ontology.listStatements(selector);
 				while (iter.hasNext()) {
					iter.next();
 					inEdges++;
 				}
 				nrNamedClasses++;
 			}
 		}
 		Float inDegree = (float)inEdges / nrNamedClasses;
 		return inDegree.toString();
 	}
 }
