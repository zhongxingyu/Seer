 package com.axiomalaska.phenomena;
 
 import java.io.InputStream;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.util.FileManager;
 
 public class IoosParameterUtil {
	private static final String PARAMETER_RDF = "src/main/vocabs/parameter.rdf";
 	
 	private static IoosParameterUtil instance;
 	private OntModel model;
 	
 	protected IoosParameterUtil(){
 		model = loadModel();
 	}
 	
 	public static IoosParameterUtil getInstance(){
 		if( instance == null ){
 			instance = new IoosParameterUtil();
 		}
 		return instance;
 	}
 	
 	private static OntModel loadModel(){
         // create an empty model
         OntModel model = ModelFactory.createOntologyModel();
 
         //use the FileManager to find the input file
         InputStream in = FileManager.get().open( PARAMETER_RDF );
         if (in == null) {
         	throw new IllegalArgumentException("File: " + PARAMETER_RDF + " not found");
 		}
 
 		// read the RDF/XML file
 		model.read(in, null);
 		return model;
 	}
 	
 	public OntModel getModel(){
 		return model;
 	}
 	
 	public String getPropertyValue( Individual individual, Property property ){
 		Statement statement = model.getProperty( individual, property );
 		return statement == null ? null : statement.getString();
 	}
 	
 	public boolean vocabularyContainsParameter( String uri ){
 		return model.getIndividual( uri ) != null;
 	}
 }
