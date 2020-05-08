 /**
  * 
  */
 package app;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 
 import com.hp.hpl.jena.rdf.model.InfModel;
 import com.hp.hpl.jena.rdf.model.LiteralRequiredException;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Selector;
 import com.hp.hpl.jena.rdf.model.SimpleSelector;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.reasoner.Reasoner;
 import com.hp.hpl.jena.reasoner.ReasonerRegistry;
 import com.hp.hpl.jena.util.FileManager;
 
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
 
 import experiments.NsPrefixLoader;
 import framework.Clue;
 import framework.ClueQuery;
 import framework.EntityRecogniser;
 import framework.Pop;
 
 /**
  * @author Ben Griffiths
  *
  */
 public class ClueQueryImpl implements ClueQuery {
 	private final String ENDPOINT_URI = "http://dbpedia.org/sparql";
 	private final String DBPEDIA_PREFIX_DECLARATION = "PREFIX dbpedia: <http://dbpedia.org/resource/>";
 	private final String DBPEDIA_OWL_PREFIX_DECLARATION = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>";
 	private final String RDFS_PREFIX_DECLARATION = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
 	private final int LANGUAGE_TAG_LENGTH = 3;
 	private final String LANGUAGE_TAG = "@";
 	private final String ENG_LANG = "en";
 	
 	private final String[] EXCLUDED_VOCABS = {};
 	
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ArrayList<Resource> extractedResources; // Resources whose labels have been extracted from DBpedia
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ArrayList<Selector> testedSelectors;
 	@Setter(AccessLevel.PRIVATE) private ArrayList<String> candidateSolutions;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private Clue clue;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private EntityRecogniser entityRecogniser;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private Query query;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private QueryExecution queryExecution;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PUBLIC) private String whereClause;
 	
 	public ClueQueryImpl(Clue clue, EntityRecogniser entityRecogniser) {
 		this.setClue(clue);
 		this.setEntityRecogniser(entityRecogniser);
 		this.setCandidateSolutions(new ArrayList<String>());
 		this.setExtractedResources(new ArrayList<Resource>());
 		this.setTestedSelectors(new ArrayList<Selector>());
 	}
 	
 	/**
 	 * @return ArrayList<String>
 	 */
 	@Override
 	public ArrayList<String> getCandidateSolutions() {
 		ArrayList<String> recognisedResourceURIs = this.getEntityRecogniser().getRecognisedResourceURIs();
 		Model schema = FileManager.get().loadModel("popv7.owl");
 		Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
 	    reasoner = reasoner.bindSchema(schema);
 		for(String resourceUri : recognisedResourceURIs) {
 			try {
 				Model data = this.constructModelFromRemoteStore(resourceUri); // Query DBpedia for triples that include this resource
 				InfModel infModel = ModelFactory.createInfModel(reasoner, data);
 			    this.extractCandidates(infModel);
 			}
 			catch(QueryExceptionHTTP e) {
 				System.err.println("Extraction of recognised resource <" + resourceUri + "> from DBpedia failed.");
 			}
 			
 		}
 		return this.candidateSolutions;
 	}
 	
 	private Model constructModelFromRemoteStore(String resourceUri) throws QueryExceptionHTTP {
 		
 		String sparqlQuery = RDFS_PREFIX_DECLARATION +
 				" construct {<" + resourceUri + "> ?predicate ?object." +
 				" 			?object rdfs:label ?label." +
 				" 			?subject ?anotherPredicate <" + resourceUri + ">." +
 				"			?subject rdfs:label ?anotherLabel." +
 				"}" +
 				" where {" +
 				" {<" + resourceUri + "> ?predicate ?object." +
 				" 			?object rdfs:label ?label.}" +
 				" UNION" +
 				" {?subject ?anotherPredicate <" + resourceUri + ">." +
 				" ?subject rdfs:label ?anotherLabel.} " +
 				"}";
 		
 		Query query = QueryFactory.create(sparqlQuery);
 		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URI, query);
 		
 		System.out.println("Constructing model around " + resourceUri); // DEBUGGING ******************************
 		
 		Model model = queryExecution.execConstruct();
 		
 		
 		// DEBUGGING ***************************************************************
 		/*
 		if(resourceUri.equals("http://dbpedia.org/resource/Houses_Of_The_Holy")) {
 			 // load standard prefixes into the model
 		    NsPrefixLoader prefixLoader = new NsPrefixLoader(model);
 			prefixLoader.loadStandardPrefixes();
 			 
 			// Now, write the model out to a file in RDF/XML-ABBREV format:
 			try {
 				Random rand = new Random();
 				int randToAppend = rand.nextInt(1000);
 				
 				String fileName = "data\\extractedModel" + randToAppend + ".xml";
 				FileOutputStream outFile = new FileOutputStream(fileName);
 				System.out.println("Writing retrieved data to file...");
 				model.write(outFile, "RDF/XML-ABBREV");
 				outFile.close();
 				System.out.println("Operation complete");
 			}
 			catch(FileNotFoundException e) {
 				e.printStackTrace();
 			} 
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		*/
 		
 		
 		
 		queryExecution.close();
 		return model;
 	}
 	
 	private void extractCandidates(InfModel infModel) {
 		ArrayList<String> clueFragments = this.getEntityRecogniser().getClueFragments();
 		System.out.println("Extracting labels..."); // DEBUGGING ****************************************************
 		
 		Selector propertiesOfInterestSelector = new SimpleSelector(null, Pop.relationalProperty, (RDFNode)null);
 		
 		//Model posit = ModelFactory.createDefaultModel();
 		//Resource desiredProperty = posit.createResource("http://www.griffithsben.com/ontologies/pop.owl#desiredProperty");
 		
 		
 		
 		// List statements in which the predicate is a pop:relationalProperty
 		StmtIterator statements = infModel.listStatements(propertiesOfInterestSelector);
 		
 		while(statements.hasNext()) {
 			Statement thisStatement = statements.nextStatement();
 			Resource subjectOfStatement = thisStatement.getSubject();
 			RDFNode objectOfStatement = thisStatement.getObject();
 			
 			Selector selector = new CandidateSelector(subjectOfStatement, null, objectOfStatement);
 			
 			
 			
 			
 			if(this.getTestedSelectors().contains(selector)) { // DEBUGGING **************************************
 				System.err.println("Already tested this selector"); // DEBUGGING **********************************
 			}
 			
 			StmtIterator statementsOfInterest = infModel.listStatements(selector);
 			
 			while(statementsOfInterest.hasNext()) {
 				Statement statementOfInterest = statementsOfInterest.nextStatement();
 				Property thisPredicate = statementOfInterest.getPredicate();
 				
 				Resource thisPredicateInModel = infModel.getResource(thisPredicate.getURI());
 				
 				StmtIterator labelProperties = thisPredicateInModel.listProperties(RDFS.label);
 				
 				if(labelProperties != null) {
 					while(labelProperties.hasNext()) {
 						RDFNode predicateLabelValue = labelProperties.nextStatement().getObject();
 						String rawPredicateLabel = predicateLabelValue.toString();
 						String predicateLabel = stripLanguageTag(rawPredicateLabel);
 						if(clueFragments.contains(toProperCase(predicateLabel))) {
 							RDFNode objectOfInterest = thisStatement.getObject();
 							if(objectOfInterest.isLiteral()) { // a string has been identified which may be a solution
 									this.addCandidateSolution(objectOfInterest.toString());
 							}
 								
 							else {  // a resource has been identified whose label may represent a solution
 								
 									// *********** SHOULD I ALSO TEST THE LABELS OF SUBJECTS????????? ********************
 									Resource object = objectOfStatement.asResource();
 									
 									if(!extractedResources.contains(object)) { // check if we have already tested this resource
 										extractedResources.add(object);
 										StmtIterator candidateLabels = object.listProperties(RDFS.label);
 										while(candidateLabels.hasNext()) {
 											Statement s = candidateLabels.nextStatement();
											String lang = null;
											try {
 												lang = s.getLanguage(); // we only want English-language labels
 											}
 											catch(LiteralRequiredException e) {
												/* It is not unknown within the DBpedia dataset for a property to have
												 * a resource as its value despite that property being specified as
												 * rdfs:subPropertyOf rdfs:label. For example, dbpprop:artist is sometimes
												 * given a resource as a value. In such cases, we ignore the resource
 												 */
 											}
 											if(lang == null || lang.equals(this.ENG_LANG)) {
												RDFNode candidateLabelValue = s.getObject();
 												String rawCandidateLabel = candidateLabelValue.toString();
 												String candidateLabel = stripLanguageTag(rawCandidateLabel);
 												this.addCandidateSolution(candidateLabel);
 											}
 										}
 										
 							
 									}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void addCandidateSolution(String candidateSolution) {
 		if(!(this.candidateSolutions.contains(candidateSolution)))
 			this.candidateSolutions.add(candidateSolution);
 		System.out.println("Found candidate solution: " + candidateSolution); // DEBUGGING ******************************
 	}
 
 	/*
 	 * THIS CODE IS DUPLICATED IN THE SIMPLEENTITYRECOGNISER CLASS - REFACTOR IT OUT SOMEWHERE?
 	 */
 	private String stripLanguageTag(String solutionText) {
 		int positionOfLanguageTag = solutionText.length() - LANGUAGE_TAG_LENGTH;
 		if(solutionText.length() > LANGUAGE_TAG_LENGTH) {
 			if(solutionText.substring(positionOfLanguageTag, positionOfLanguageTag + 1).equals(LANGUAGE_TAG))
 				return solutionText.substring(0, positionOfLanguageTag);
 		}
 		return solutionText;
 	}
 	
 	/*
 	 * DUPLICATED FROM ENTITYRECOGNISERIMPL CLASS
 	 */
 	private String toProperCase(String thisWord) {
 		String thisWordInProperCase = thisWord.substring(0, 1).toUpperCase();
 		if(thisWord.length() > 1) {
 			int index = 1; // start at the second letter of the word
 			while(index < thisWord.length()) {
 				String nextCharacter = thisWord.substring(index, index + 1);
 				thisWordInProperCase += nextCharacter;
 				if((nextCharacter.equals(" ")) && (index < (thisWord.length() - 1))) {
 					 index++; // the next character needs to be capitalised
 					 nextCharacter = thisWord.substring(index, index + 1);
 					 thisWordInProperCase += nextCharacter.toUpperCase();
 				}
 				index++;
 			}
 		}
 		return thisWordInProperCase;
 	}
 }
