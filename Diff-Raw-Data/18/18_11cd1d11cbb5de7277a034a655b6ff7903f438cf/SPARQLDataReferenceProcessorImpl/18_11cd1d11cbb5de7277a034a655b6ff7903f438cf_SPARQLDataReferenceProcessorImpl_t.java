 package com.github.podd.impl.file;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.semanticweb.owlapi.model.IRI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.file.SPARQLDataReference;
 import com.github.podd.api.file.SPARQLDataReferenceProcessor;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * Processor for File References of type <i>http://purl.org/podd/ns/poddBase#SPARQLDataReference</i>
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public class SPARQLDataReferenceProcessorImpl implements SPARQLDataReferenceProcessor
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     @Override
     public boolean canHandle(final Model rdfStatements)
     {
         if(rdfStatements == null || rdfStatements.isEmpty())
         {
             return false;
         }
         
         for(final URI fileType : this.getTypes())
         {
             final Model matchingModels = rdfStatements.filter((Resource)null, null, fileType);
             if(!matchingModels.isEmpty())
             {
                 return true;
             }
         }
         
         return false;
     }
     
     @Override
     public Collection<SPARQLDataReference> createReferences(final Model rdfStatements)
     {
         if(rdfStatements == null || rdfStatements.isEmpty())
         {
             return null;
         }
         
         final Set<SPARQLDataReference> results = new HashSet<SPARQLDataReference>();
         
         for(final URI fileType : this.getTypes())
         {
             final Set<Resource> fileRefUris = rdfStatements.filter(null, RDF.TYPE, fileType).subjects();
             
             for(final Resource fileRef : fileRefUris)
             {
                 final Model model = rdfStatements.filter(fileRef, null, null);
                 
                 if(this.log.isDebugEnabled())
                 {
                     DebugUtils.printContents(model);
                 }
                 
                 final SPARQLDataReference fileReference = new SPARQLDataReferenceImpl();
                 
                 // note: artifact ID is not available to us in here and must be added externally
                 
                 if(fileRef instanceof URI)
                 {
                     fileReference.setObjectIri(IRI.create((URI)fileRef));
                 }
                 
                 final Set<Value> label = model.filter(fileRef, RDFS.LABEL, null).objects();
                 if(!label.isEmpty())
                 {
                     fileReference.setLabel(label.iterator().next().stringValue());
                 }
                 
                 final Set<Value> graph =
                         model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_SPARQL_GRAPH, null).objects();
                 if(!graph.isEmpty())
                 {
                     fileReference.setGraph(graph.iterator().next().stringValue());
                 }
                 
                 final Set<Value> endpoint =
                         model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_SPARQL_ENDPOINT, null).objects();
                 if(!endpoint.isEmpty())
                 {
                     fileReference.setEndpointURL(endpoint.iterator().next().stringValue());
                 }
                 
                 final Set<Value> alias = model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_ALIAS, null).objects();
                 if(!alias.isEmpty())
                 {
                     fileReference.setRepositoryAlias(alias.iterator().next().stringValue());
                 }
                 
                 final Model linksToFileReference = rdfStatements.filter(null, null, fileRef);
                 
                 // TODO: Need to use a SPARQL query to verify that the property is a sub-property of
                 // PODD Contains
                 if(!linksToFileReference.isEmpty())
                 {
                     for(final Resource nextResource : linksToFileReference.subjects())
                     {
                         if(nextResource instanceof URI)
                         {
                             fileReference.setParentIri(IRI.create((URI)nextResource));
                             break;
                         }
                     }
                     fileReference
                             .setParentPredicateIRI(IRI.create(linksToFileReference.predicates().iterator().next()));
                 }
                 
                 results.add(fileReference);
             }
         }
         return results;
     }
     
     @Override
     public Set<URI> getTypes()
     {
        return Collections.singleton(PoddRdfConstants.PODD_BASE_DATA_REFERENCE_TYPE_SPARQL);
     }
     
 }
