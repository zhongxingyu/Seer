 /**
  * 
  */
 package prototype;
 
 import java.util.ArrayList;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.ontology.OntProperty;
 import com.hp.hpl.jena.ontology.OntResource;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 
 import framework.Clue;
 import framework.EntityRecogniser;
 
 /**
  * @author Ben Griffiths
  *
  */
 public class SimpleEntityRecogniser implements EntityRecogniser {
 	private final int LANGUAGE_TAG_LENGTH = 3; // DUPLICATED IN SIMPLESOLUTION CLASS - REFACTOR OUT AS A STATIC CONSTANT?
 	private final String LANGUAGE_TAG = "@"; // DUPLICATED IN SIMPLESOLUTION CLASS - REFACTOR OUT AS A STATIC CONSTANT?
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) Clue clue;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) Model model;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) OntModel ontModel;
 	@Setter(AccessLevel.PRIVATE) ArrayList<Resource> recognisedSubjects;
 	@Setter(AccessLevel.PRIVATE) ArrayList<Property> recognisedProperties;
 	@Setter(AccessLevel.PRIVATE) ArrayList<Resource> recognisedObjects;
 	
 	@Getter(AccessLevel.PUBLIC) ArrayList<String> clueFragments;
 
 	public SimpleEntityRecogniser(Clue clue, Model model) {
 		this.setClue(clue);
 		this.setModel(model);
 		this.setOntModel(ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM,this.getModel()));
 		this.setClueFragments();
 		this.setRecognisedSubjects(new ArrayList<Resource>());
 		this.setRecognisedProperties(new ArrayList<Property>());
 		this.setRecognisedObjects(new ArrayList<Resource>());
 	}
 	
 	private void setClueFragments() {
 		this.clueFragments = new ArrayList<String>();
 		String clueText = this.getClue().getSourceClue();
 		String[] wordsInClueText = clueText.split(" ");
 		for(int i = 0; i < wordsInClueText.length; i++) {
 			String thisWord = wordsInClueText[i];
 			this.getClueFragments().add(thisWord.toLowerCase());
 			for(int j = i + 1; j < wordsInClueText.length; j++) {
 				thisWord = thisWord + " " + wordsInClueText[j];
				this.getClueFragments().add(thisWord);
 			}
 		}
 	}
 	
 	/*
 	 * THIS CODE IS DUPLICATED IN THE SIMPLESOLUTION CLASS - REFACTOR IT OUT SOMEWHERE?
 	 */
 	private String stripLanguageTag(String text) {
 		int positionOfLanguageTag = text.length() - LANGUAGE_TAG_LENGTH;
 		if(text.length() > LANGUAGE_TAG_LENGTH) {
 			if(text.substring(positionOfLanguageTag, positionOfLanguageTag + 1).equals(LANGUAGE_TAG))
 				return text.substring(0, positionOfLanguageTag);
 		}
 		return text;
 	}
 
 	@Override
 	public ArrayList<Resource> getRecognisedSubjects() {
 		ResIterator subjectsInModel = this.getModel().listSubjects();
 		while(subjectsInModel.hasNext()) {
 			Resource thisSubject = subjectsInModel.nextResource();
 			if(this.recognisedSubjects.contains(thisSubject))
 				continue;
 			OntResource subject = this.getOntModel().getOntResource(thisSubject); // create an OntResource from the Resource object
 			ExtendedIterator<RDFNode> labels = subject.listLabels(null); // list all values of RDFS:label for this resource
 			while(labels.hasNext()) {
 				String thisLabel = stripLanguageTag(labels.next().toString());
 				if(this.getClueFragments().contains(thisLabel.toLowerCase()))
 					this.recognisedSubjects.add(thisSubject);
 			}
 		}
 		return this.recognisedSubjects;
 	}
 	
 	/*
 	 * CODE DUPLICATED FROM getRecognisedSubjects() method - TO BE REFACTORED SOMEHOW
 	 */
 	@Override
 	public ArrayList<Property> getRecognisedProperties() {
 		StmtIterator statementsInModel = this.getModel().listStatements();
 		
 		while(statementsInModel.hasNext()) {
 			Property thisProperty = statementsInModel.nextStatement().getPredicate();
 			if(this.recognisedProperties.contains(thisProperty))
 				continue;
 			String uri = thisProperty.getURI();
 			if(uri == null)
 				continue;
 			OntProperty property = this.getOntModel().getOntProperty(uri); // create an OntProperty from the Property object
 			if(property == null)
 				continue;
 			ExtendedIterator<RDFNode> labels = property.listLabels(null); // list all values of RDFS:label for this resource
 			if(labels == null)
 				continue;
 			while(labels.hasNext()) {
 				String thisLabel = stripLanguageTag(labels.next().toString());
 				if(this.getClueFragments().contains(thisLabel))
 					this.recognisedProperties.add(thisProperty);
 			}
 		}
 		return this.recognisedProperties;
 	}
 	
 	/*
 	 * CODE DUPLICATED FROM getRecognisedSubjects() method - TO BE REFACTORED SOMEHOW
 	 */
 	@Override
 	public ArrayList<Resource> getRecognisedObjects() {
 		NodeIterator objectsInModel = this.getModel().listObjects();
 		while(objectsInModel.hasNext()) {
 			RDFNode thisObject = objectsInModel.nextNode();
 			if(thisObject.isResource()) { // We are only interested in objects that are resources
 				if(this.recognisedObjects.contains((Resource)thisObject))
 					continue;
 				OntResource object = this.getOntModel().getOntResource((Resource)thisObject); // create an OntResource from the RDFNode object
 				ExtendedIterator<RDFNode> labels = object.listLabels(null); // list all values of RDFS:label for this resource
 				while(labels.hasNext()) {
 					String thisLabel = stripLanguageTag(labels.next().toString());
 					if(this.getClueFragments().contains(thisLabel))
 						this.recognisedObjects.add((Resource)thisObject);
 				}
 			}
 		}
 		return this.recognisedObjects;
 	}
 
 }
