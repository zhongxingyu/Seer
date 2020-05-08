 package eu.fusepool.ecs.utils;
 
 import java.io.IOException;
 import java.security.AccessController;
 import java.security.AllPermission;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 
 import org.apache.clerezza.rdf.core.Literal;
 import org.apache.clerezza.rdf.core.LiteralFactory;
 import org.apache.clerezza.rdf.core.MGraph;
 import org.apache.clerezza.rdf.core.Resource;
 import org.apache.clerezza.rdf.core.Triple;
 import org.apache.clerezza.rdf.core.TripleCollection;
 import org.apache.clerezza.rdf.core.TypedLiteral;
 import org.apache.clerezza.rdf.core.UriRef;
 import org.apache.clerezza.rdf.core.access.LockableMGraph;
 import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
 import org.apache.clerezza.rdf.core.impl.TripleImpl;
 import org.apache.clerezza.rdf.ontologies.DC;
 import org.apache.clerezza.rdf.ontologies.DCTERMS;
 import org.apache.clerezza.rdf.ontologies.RDF;
 import org.apache.clerezza.rdf.ontologies.RDFS;
 import org.apache.clerezza.rdf.ontologies.SIOC;
 import org.apache.clerezza.rdf.utils.GraphNode;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.ConfigurationPolicy;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.stanbol.enhancer.servicesapi.ContentItem;
 import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
 import org.apache.stanbol.enhancer.servicesapi.ContentSource;
 import org.apache.stanbol.enhancer.servicesapi.EnhancementException;
 import org.apache.stanbol.enhancer.servicesapi.EnhancementJobManager;
 import org.apache.stanbol.enhancer.servicesapi.impl.ByteArraySource;
 import org.apache.stanbol.enhancer.servicesapi.rdf.TechnicalClasses;
 import org.apache.stanbol.entityhub.model.clerezza.RdfValueFactory;
 import org.apache.stanbol.entityhub.servicesapi.model.Entity;
 import org.apache.stanbol.entityhub.servicesapi.model.Representation;
 import org.apache.stanbol.entityhub.servicesapi.site.SiteManager;
 import org.osgi.framework.Constants;
 import org.osgi.service.component.ComponentContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.fusepool.datalifecycle.RdfDigester;
 import eu.fusepool.ecs.ontologies.ECS;
 
 /**
  * This component provides to functionalities:
  * 1) Extracts text from properties dcterms:title and dcterms:abstract of individuals 
  * of type pmo:PatentPublication. The extracted text is added as a value of the sioc.content 
  * property to the same individuals. The text can be used for indexing purposes.
  * 2) Adds dc:subject property to each patent pointing to entities 
  * extracted by NLP engines from the text extracted in the previous step.
  * 3) Adds dc:subject property to each applicant or inventor related to the patent 
  */
 @Component(immediate = true, metatype = true,  
 	configurationFactory = true, policy = ConfigurationPolicy.OPTIONAL)
 @Service(RdfDigester.class)
 @Properties(value = {
 	    @Property(name = Constants.SERVICE_RANKING, 
 	    		intValue = PatentTextExtractor.DEFAULT_SERVICE_RANKING),
 	    @Property(name = PatentTextExtractor.EXTRACTOR_TYPE_LABEL, 
 	    		value = PatentTextExtractor.EXTRACTOR_TYPE_VALUE)
 })
 public class PatentTextExtractor implements RdfDigester {
 	
 	public static final int DEFAULT_SERVICE_RANKING = 101;
 	
 	public static final String EXTRACTOR_TYPE_LABEL = "extractorType";
 	
 	public static final String EXTRACTOR_TYPE_VALUE = "patent";
 	
 	//Confidence threshold value to accept entities extracted by an NLP enhancement engine
     private static final double CONFIDENCE_THRESHOLD = 0.3;
 	
 	private static Logger log = LoggerFactory.getLogger(PatentTextExtractor.class);
 	
 	@Reference
     private ContentItemFactory contentItemFactory;
 	
 	@Reference
     private EnhancementJobManager enhancementJobManager;
 	
 	/**
      * This service allows to get entities from configures sites
      */
     @Reference
     private SiteManager siteManager;
 	
 	/**
 	 * Looks for sioc:content property in the input document graph in individual of type pmo:PatentPublication.
 	 * If there's no such property it adds it. 
 	 * The value of the property is taken from the following properties:
 	 * dcterms:title, dcterms:abstract 
 	 * @throws  
 	 * @throws IOException 
 	 */
 	public void extractText(MGraph graph) {
 		String text = "";
 		// select all the resources that are pmo:PatentPubblication and do not have a sioc:content property 
 		Set<UriRef> patentRefs = getPatents(graph);
 		for (UriRef patentRef : patentRefs) {
             
 			log.info("Adding sioc:content property to patent: " + patentRef.getUnicodeString());
             //text = addSiocContentToPatent(graph, patentRef);
             text = "Barack Obama is the president of the United States";
             
             //send the text to the default chain for enhancements if not empty
             if(! "".equals(text) && text != null ) {
 	            try {
 					enhance(text, patentRef, graph);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (EnhancementException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
             }
             
             //add a dc:subject statement for each individual that is the object of the predicate pmo:applicant
            aliasAsDcSubject(patentRef, PatentOntology.applicant, (LockableMGraph) graph);
             //add a dc:subject statement for each individual that is the object of the predicate pmo:inventor
            aliasAsDcSubject(patentRef, PatentOntology.inventor, (LockableMGraph) graph);
         }
 		
 	}
 	
 	/**
      * Select all resources of type pmo:patentPublication that do not have a sioc:content 
      * property and have at least one of dcterms:title, dcterms:abstract
      */
     private Set<UriRef> getPatents(MGraph graph) {
     	Set<UriRef> result = new HashSet<UriRef>();
     	
         Iterator<Triple> idocument = graph.filter(null, RDF.type, PatentOntology.PatentPublication);
         while (idocument.hasNext()) {
             Triple triple = idocument.next();
             UriRef patentRef = (UriRef) triple.getSubject();
             GraphNode node = new GraphNode(patentRef, graph);
             //uncomment the following 4 commented lines after debugging
             //if (!node.getObjects(SIOC.content).hasNext()) {
             	//if(node.getObjects(DCTERMS.abstract_).hasNext() || node.getObjects(DCTERMS.title).hasNext()){
             		result.add(patentRef);
             	//}
             //}
         }
         
         log.info(result.size() + " Document nodes found.");
         
         return result;
     }
     
     /**
      * Add a sioc:content property to a resource. 
      * The value is taken from dcterm:title and dcterms:abstract properties 
      */
 
     private String addSiocContentToPatent(MGraph graph, UriRef patentRef) {
     
     	AccessController.checkPermission(new AllPermission());
     	
     	String textContent = "";
     	
     	GraphNode node = new GraphNode(patentRef, graph);
     	
         Iterator<Literal> titles = node.getLiterals(DCTERMS.title);
         while (titles.hasNext()) {
         	String title = titles.next().getLexicalForm() + "\n";
             textContent += title;
         }
         
         Iterator<Literal> abstracts = node.getLiterals(DCTERMS.abstract_);
         while (abstracts.hasNext()) {
         	String _abstract = abstracts.next().getLexicalForm() + "\n";
             textContent += _abstract;
         }
         
         if(!"".equals(textContent)) {
         	
         	graph.add(new TripleImpl(patentRef, SIOC.content, new PlainLiteralImpl(textContent)));
         	// The following call to the node raise a org.apache.clerezza.rdf.core.NoConvertorException  
         	//node.addPropertyValue(SIOC.content, new PlainLiteralImpl("prova della disperazione"));
         	
         	// Resources with this type have sioc:content and rdfs:label indexed by the ECS 
         	// when added to the content graph
         	graph.add(new TripleImpl(patentRef, RDF.type, ECS.ContentItem));
         	// The following call to the node raise a org.apache.clerezza.rdf.core.NoConvertorException 
             //node.addProperty(RDF.type, ECS.ContentItem);
         	
         	log.info("Added sioc:content property to patent " + patentRef.getUnicodeString() + " value:  " + textContent);
         }
         else {
         	log.info("No text found in dcterms:title or dcterms:abstract to add to sioc:content");
         }
 
         return textContent;
 
     }
     
     /**
      * Add dc:subject properties to a node (patent) pointing to entities which are assumed to be related to
      * a content. This method uses the enhancementJobManager to extract related entities using NLP 
      * processor available in the default chain. The node uri is also the uri of the content item
      * so that the enhancements will be referred that node. Each enhancement found with a confidence 
      * value above a threshold is then added as a dc:subject to the node
      */
     private void enhance(String content, UriRef patentRef, MGraph graph) throws IOException,
             EnhancementException {
         final ContentSource contentSource = new ByteArraySource(
                 content.getBytes(), "text/plain");
         final ContentItem contentItem = contentItemFactory.createContentItem(
                 patentRef, contentSource);
         enhancementJobManager.enhanceContent(contentItem);
         // this contains the enhancement results
         final MGraph enhancementGraph = contentItem.getMetadata();
         addSubjects(patentRef, enhancementGraph, graph);
     }
     
     /** 
      * Add dc:subject property to patent (pmo:PatentPublication) pointing to entities 
      * extracted by NLP engines in the default chain. Given a node (patent) and a TripleCollection 
      * containing fise:Enhancements about that patent dc:subject properties are added to it pointing 
      * to entities referenced by those enhancements if the enhancement confidence value is above a 
      * threshold.
      * @param node
      * @param metadata
      */
     private void addSubjects(UriRef patentRef, TripleCollection metadata, MGraph graph) {
         final GraphNode enhancementType = new GraphNode(TechnicalClasses.ENHANCER_ENHANCEMENT, metadata);
         final Set<UriRef> entities = new HashSet<UriRef>();
         // get all the enhancements
         final Iterator<GraphNode> enhancements = enhancementType.getSubjectNodes(RDF.type);
         while (enhancements.hasNext()) {
             final GraphNode enhhancement = enhancements.next();
           //look the confidence value for each enhancement
             double enhancementConfidence = LiteralFactory.getInstance().createObject(Double.class,
             		(TypedLiteral) enhhancement.getLiterals(org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_CONFIDENCE).next());
             if( enhancementConfidence >= CONFIDENCE_THRESHOLD ) {            
             	// get entities referenced in the enhancement 
             	final Iterator<Resource> referencedEntities = enhhancement.getObjects(org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_REFERENCE);
             	while (referencedEntities.hasNext()) {
 	                final UriRef entity = (UriRef) referencedEntities.next();	                
 	                // Add dc:subject to the patent for each referenced entity
                 	graph.add(new TripleImpl(patentRef, DC.subject, entity));
                     entities.add(entity);
                 }
             }
 
 
         }
         for (UriRef uriRef : entities) {
             // We don't get the entity description directly from metadata
             // as the context there would include
             addResourceDescription(uriRef, graph);
         }
     }
     
     /**
      * Add a dc:subject statement to a patent for each entity that is linked 
      * to that resource through the predicate passed as argument..  
      */
    private void aliasAsDcSubject(UriRef patentRef, UriRef predicateRef, LockableMGraph graph) {
     	final GraphNode patentNode = new GraphNode(patentRef, graph);
    	Lock lock = graph.getLock().writeLock();
     	lock.lock();
     	try {
     		Iterator<Resource> iobjects = patentNode.getObjects(predicateRef);
             while (iobjects.hasNext()) {
                 Resource entity = iobjects.next();                             
                 // Add dc:subject to the patent publication for each entity related to it via the predicate
            	graph.add(new TripleImpl(patentRef, DC.subject, entity));
             }
         } 
     	finally {
            lock.unlock();
         }
         
     }
     
     /** 
      * Add a description of the entities extracted from the text by NLP engines in the default chain
      */
     private void addResourceDescription(UriRef iri, MGraph mGraph) {
         final Entity entity = siteManager.getEntity(iri.getUnicodeString());
         if (entity != null) {
             final RdfValueFactory valueFactory = new RdfValueFactory(mGraph);
             final Representation representation = entity.getRepresentation();
             if (representation != null) {
                 valueFactory.toRdfRepresentation(representation);
             }
         }
     }
 	
 	@Activate
     protected void activate(ComponentContext context) {
         log.info("The PatentTextExtractor service is being activated");
 
     }
 
     @Deactivate
     protected void deactivate(ComponentContext context) {
         log.info("The PatentTextExtractor service is being deactivated");
     }
 
 
 
 }
