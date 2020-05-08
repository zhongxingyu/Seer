 package com.admin.owlmanager;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.mindswap.pellet.jena.PelletReasonerFactory;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntProperty;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.InfModel;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.reasoner.Reasoner;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 
 
 
 /**
  * This class, generates a complete reasoned OWL file
  * from a base model
  * 
  * @author israelord
  */
 public class OWLActor {
 
 
 	/**
 	 * Writes an OWL model to a file on the given path
 	 * 
 	 * @param model Model to be written
 	 * @param path Path to the file where the model will be written
 	 * @throws FileNotFoundException if the route to the file is not valid
 	 */
 	private void writeModel(InfModel model, String path) throws FileNotFoundException {
 		FileOutputStream out = new FileOutputStream(path);
 		model.write(out);
 	}
 	
 	/**
 	 * Applies forward chaining reasoning through pellet to a 
 	 * given model
 	 * @param path route to the model
 	 * @return a complete reasoned model
 	 */
 	private InfModel reasonOverModel(String path) {
 		Model emptyModel = ModelFactory.createDefaultModel();
 		
 		//Pellet Instance
 		Reasoner reasoner = PelletReasonerFactory.theInstance().create();
 		
 		InfModel model = ModelFactory.createInfModel(reasoner, emptyModel);
 		InputStream in = FileManager.get().open(path);
 		model.read(in, "");
 		
 		return model;
 	}
 	
 	/**
 	 * TODO: read locations from properties files
 	 * 
 	 * @param sourcePath Original model to be reasoned
 	 * @param targetPath Target file to store the reasoned model
 	 * @return true if everything went OK
 	 */
 	public boolean generateReasonedModel(String sourcePath, String targetPath) {
 		InfModel model = this.reasonOverModel(sourcePath);
 		boolean res = false;
 		try {
 			this.writeModel(model, targetPath);
 			res = true;
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 			// TODO log4j
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			// TODO log4j
 		}
 		return res;
 	}
 
 	/**
 	 * Gets all classes available on the model
 	 * @param modelUrl the location of the owl model
 	 * @return a list of classes
 	 */
 	public ExtendedIterator<OntClass> getClasses(String modelUrl) {
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read(modelUrl);
 
 		ExtendedIterator<OntClass> classes = model.listClasses();
 
 		return classes;
 	}
 	
 	/**
 	 * Gets all available instances of a given class
 	 * 
 	 * @param modelUrl the location of the owl model
 	 * @param className the name of the class
 	 * @return the list of instances for the given class
 	 */
 	public ExtendedIterator<?> getInstances(String modelUrl, String className) {
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read(modelUrl);
 		OntClass c = model.getOntClass(className);
 		ExtendedIterator<?> iter = c.listInstances();
 
 		return iter;
 	}
 	
 	/**
 	 * Add an individual or instance on a given class
 	 * 
 	 * @param modelPath write model address
 	 * @param modelUrl model uel
 	 * @param uri resource to be added
 	 * @param className class where we'll add the individual
 	 * @return true if everything went ok
 	 */
 	public boolean addIndividual(String modelPath, String modelUrl, String uri, String className) {
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read(modelUrl);
 		OntClass c = model.getOntClass(className);
 	
 		model.createIndividual(uri, c);	
 		
 		try {
 			this.writeModel(model, modelPath);
 		} catch (FileNotFoundException e) {
 			// TODO: log4j
 			e.printStackTrace();
 			return false;
 		} finally {
 			model.close();
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Gets all properties on a given model
 	 * @param modelURL model URL
 	 * @return an iterator with the properties
 	 */
 	public ExtendedIterator<OntProperty> getProperties(String modelURL) {
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read(modelURL);
 		
 		ExtendedIterator<OntProperty> result = model.listAllOntProperties();
 		
 		return result;
 	}
 	
 	/**
 	 * Adds a Statement to a given model
 	 * @param modelPath write model path
 	 * @param modelUrl model url
 	 * @param subject Subject element of the triple
 	 * @param predicate Predicate element of the triple
 	 * @param object Object element of the triple
 	 * @return True if everything went ok
 	 */
 	public boolean addTripleStore(String modelPath, String modelUrl, String subject, String predicate, String object) {
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read(modelUrl);
 		
 		Resource resource = model.getResource(subject);
 		Property property = model.getOntProperty(predicate);
 		RDFNode node = model.getIndividual(object);
 		
 		Statement statement = ResourceFactory.createStatement(resource, property, node);
 		
 		model.add(statement);
 		
 		try {
 			this.writeModel(model, modelPath);
 		} catch (FileNotFoundException e) {
 			// TODO log4j
 			e.printStackTrace();
 			return false;
 		} finally {
 			model.close();
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Eliminates a given instance and all its relations and properties
 	 * @param modelPath model physical path to write
 	 * @param modelUrl model uel
 	 * @param individual target individual
 	 * @return true if everything went ok
 	 */
 	public boolean deleteIndividual(String modelPath, String modelUrl, String individual) {
 		boolean result = false;
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read(modelUrl);
 		
 		Individual resource = model.getIndividual(individual);
 		StmtIterator iter = resource.listProperties();
 		
 		model.remove(iter);
 		resource.remove();
 		
 		try {
 			this.writeModel(model, modelPath);
 			result = true;
 		} catch (FileNotFoundException e) {
 			//TODO: log4j
 			e.printStackTrace();
 		} finally {
 			model.close();
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Gets all the properties of a given individual
 	 * @param modelURL model's url
 	 * @param resourceURI target resource's URI
 	 * @param property preperty we're looking for
 	 * @return an ArrayList with the requested property values for the given individual
 	 */
 	public ArrayList<RDFNode> getIndividualPropertyValues(String modelURL, String resourceURI, String property) {
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read(modelURL);
 		
 		Individual resource = model.getIndividual(resourceURI);
 		StmtIterator iter = resource.listProperties(ResourceFactory.createProperty(property));
 		
 		ArrayList<RDFNode> values = new ArrayList<RDFNode>();
 		
 		while (iter.hasNext()) {
 			Statement current = iter.next();
 			values.add(current.getObject());
 		}
 		
 		return values;
 	}
 	
 	
 	/**
 	 * Deletes relations from a given individual
 	 * @param modelURL model's read location
 	 * @param modelPath model's write location
 	 * @param resourceURI target resource
 	 * @param objects objects to be deleted
 	 * @param prefix prefix of the relation (namespace)
 	 * @param relation name of the relation
	 * @return true if everything went ok
 	 */
 	public boolean deleteIndividualObjects(String modelURL, String modelPath, String resourceURI, 
 			ArrayList<String> objects, String prefix, String relation) {
 		boolean result = false;
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read(modelURL);
 		String completeRelation = prefix + relation;
 		
 		Individual individual = model.getIndividual(resourceURI);
 		Property property = ResourceFactory.createProperty(completeRelation);
 		Iterator<String> iter = objects.iterator();
 		
 		while (iter.hasNext()) {
 			String currentObject = iter.next();
 			Resource object = ResourceFactory.createResource(currentObject);
 			individual.removeProperty(property, object);
 		}
 		
 		try {
 			this.writeModel(model, modelPath);
 			result = true;
 		} catch (FileNotFoundException e) {
 			// TODO log4j
 			e.printStackTrace();
 		} finally {
 			model.close();
 		}
 		
 		return result;
 	}
 	
 }
