 /**
  * 
  */
 package app;
 
 import java.util.ArrayList;
 
 import javax.swing.SwingWorker;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 import framework.Clue;
 
 /**
  * @author Ben Griffiths
  *
  */
 public class EntityRecogniserTask extends SwingWorker<ArrayList<RecognisedResource>, Void> {
 	private final String LANG = "@en";
 	private final String ENDPOINT_URI = "http://dbpedia.org/sparql"; // DUPLICATED IN QUERYIMPL
 	private final String RDFS_PREFIX_DECLARATION = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"; // DUPLICATED IN QUERYIMPL
 	private final String DBPPROP_PREFIX_DECLARATION = "PREFIX dbpprop: <http://dbpedia.org/property/>";
 	private final String DB_OWL_PREFIX_DECLARATION = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>";
 	private final String RDF_PREFIX_DECLARATION = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private Clue clue;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<String> clueFragments;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private StmtIterator statementsIterator; // used to iterate over the statements in my local ontology
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ResIterator propertiesIterator;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private OntModel ontModel; // to hold the ontology in memory
 	private final String[] WORDS_TO_CONSIDER_AS_PREDICATES_ONLY = {"artist", "singer", "band", "album", "member", 
 			"writer", "song", "group"};
 	private final String[] VOCABULARIES_TO_EXCLUDE = {"http://dbpedia.org/class/yago/", 
 			"http://dbpedia.org/resource/Category:"}; // a list of namespaces whose terms should be excluded from consideration
 
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) ArrayList<RecognisedResource> recognisedResources;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) ArrayList<String> recognisedResourceUris;
 
 	/* This constructor will be used to instantiate a task that recognises entities in a clue */
 	public EntityRecogniserTask(Clue clue) {
 		super(); // call SwingWorker Default constructor
 		this.setClue(clue);
 		this.setClueFragments(clue.getClueFragments());
 		this.setRecognisedResources(new ArrayList<RecognisedResource>());
 		this.setRecognisedResourceUris(new ArrayList<String>());
 	}
     
 	/*
      * Main task. Executed in background thread. 
      */
     @Override
     public ArrayList<RecognisedResource> doInBackground() {
         int progress = 0;
         this.setProgress(progress); // Initialise progress property of SwingWorker
         
         
         int combinedLengthOfQueries = this.getClueFragments().size();
         int taskLength = (100 / combinedLengthOfQueries);
         
         for(String clueFragment : this.getClueFragments()) {
         	try {
         		this.extractEntities(clueFragment); // extract entities for next clue fragment
         	}
         	catch (QueryExceptionHTTP e) {
         		System.err.println("DBpedia connection dropped. Entity recognition for clue fragment " + clueFragment + " failed");
         	}
         	progress += taskLength;
             this.setProgress(progress); // one query has been completed
         }
         return this.getRecognisedResources();
     }
     
     /*
      * Executed on EDT
      */
     @Override
     public void done() {
     	this.setProgress(100);
     }
     
     private void extractEntities(String clueFragment) throws QueryExceptionHTTP {
                  if(considerAsPredicateOnly(clueFragment))
                           return;
 
                  String wrappedClueFragment = "\"" + clueFragment + "\"" + LANG; // wrap with escaped quotes and append a language tag
 
                  String SPARQLquery = RDFS_PREFIX_DECLARATION + " " +
                                                               RDF_PREFIX_DECLARATION + " " +
                                                               DBPPROP_PREFIX_DECLARATION + " " +
                                                               DB_OWL_PREFIX_DECLARATION +
                                             " select distinct ?resource ?typeLabel {" +
                                            " {" +
                                                     "{ select distinct ?resource ?typeLabel" +
                                                     " where {?resource rdfs:label " + wrappedClueFragment + "." +
                                                              " ?resource rdf:type ?type." +
                                                              " ?type rdfs:label ?typeLabel.}" +
                                                     " }" +
                                                     " UNION" +
                                                     " { select distinct ?resource ?typeLabel" +
                                                     "  where {?resource dbpprop:name " + wrappedClueFragment + "." +
                                                              " ?resource rdf:type ?type." +
                                                              " ?type rdfs:label ?typeLabel.}" +
                                                     " }" +
                                                     " UNION" +
                                                     " { select distinct ?resource ?typeLabel" +
                                                     "  where {?redirectingResource rdfs:label " + wrappedClueFragment + "." +
                                                     "         ?redirectingResource dbpedia-owl:wikiPageRedirects ?resource." +                                                             
                                                              " ?resource rdf:type ?type." +
                                                              " ?type rdfs:label ?typeLabel.}" +
 
                                                     " }" +
                                                     " UNION" +
                                                     " { select distinct ?resource ?typeLabel" +
                                                     "  where {?redirectingResource dbpprop:name " + wrappedClueFragment + "." +
                                                     "         ?redirectingResource dbpedia-owl:wikiPageRedirects ?resource." +
                                                              " ?resource rdf:type ?type." +
                                                              " ?type rdfs:label ?typeLabel.}" +
 
                                                     " }" +
                                            " }" + 
                                  " }";
 
                  Query query = QueryFactory.create(SPARQLquery);
                  QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URI, query);
                  try {
 
                           ResultSet resultSet = queryExecution.execSelect();
                           while(resultSet.hasNext()) {
                                    QuerySolution querySolution = resultSet.nextSolution();
                                    Resource thisResource = querySolution.getResource("?resource");
 
                                    String nameSpace = thisResource.getNameSpace();
                                    
                                    /* Trialling dbpedia.org/resource only ... */
                                   if(!nameSpace.contains("http://dbpedia.org/resource/")) {
                                	   System.out.println("Rejected resource <" + thisResource.getURI() + "> with NS " +
                                			   				nameSpace);
                                 	   continue;
                                   }
                                    
                                    /*
                                    if(excludedNameSpace(nameSpace))
                                             continue;
                                    */
                                    
                                    Literal thisTypeLabel = querySolution.getLiteral("?typeLabel");
                                    String typeLabel = thisTypeLabel.toString();
 
                                    String resourceUri = thisResource.getURI();
                                    
                                    boolean resourceAlreadyRecognised = false;
                                    int indexOfResource = 0;
                                    
                                    /* Check if a RecognisedResource has already been created for this resource */
                                    for(int i = 0; i < this.getRecognisedResources().size(); i++) {
                                 	   if(this.getRecognisedResources().get(i).getUri().equals(resourceUri)) {
                                 		   resourceAlreadyRecognised = true;
                                 		   indexOfResource = i;
                                 	   	   break;
                                 	   }
                                    }
                                 
                                    if(resourceAlreadyRecognised) {
                                 	   this.getRecognisedResources().get(indexOfResource).addTypeLabel(typeLabel);
                                    }
                                    else {
 		                                   RecognisedResource recognisedResource = new RecognisedResource(resourceUri, clueFragment);
 		                                   recognisedResource.addTypeLabel(typeLabel);
 		                                   this.getRecognisedResources().add(recognisedResource);
                                    }
                           }
                           queryExecution.close();   
                  }
                  catch(QueryExceptionHTTP e) {
                           throw e;
                  }
 	}
 
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
 
 
 	private boolean considerAsPredicateOnly(String wordToCheck) {
 		for(int i = 0; i < this.WORDS_TO_CONSIDER_AS_PREDICATES_ONLY.length; i++)
 			if(toProperCase(WORDS_TO_CONSIDER_AS_PREDICATES_ONLY[i]).equals(wordToCheck))
 				return true;
 		return false;
 	}
 
 	/**
 	 * excludedNameSpace - checks if any of the namespaces of the vocabularies that we want to exclude from consideration are
 	 * substrings of the namespace under consideration. It is necessary to do it this way rather than simply checking if the NS 
 	 * under consideration is in our list of namespaces to be excluded primarily because of the high number of resources on DBpedia
 	 * that have been created under a badly-formed variant of the NS http://dbpedia.org/class/yago/, where a resource with local
 	 * name local_name has been put into the DBpedia graph with the NS "http://dbpedia.org/class/yago/local_name"
 	 * @param namespaceToCheck
 	 * @return
 	 */
 	private boolean excludedNameSpace(String nameSpaceToCheck) {
 		for(int i = 0; i < this.VOCABULARIES_TO_EXCLUDE.length; i++)
 			if(nameSpaceToCheck.contains(VOCABULARIES_TO_EXCLUDE[i]))
 				return true;
 		return false;
 	}
 }
