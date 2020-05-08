 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
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
 import com.github.podd.utils.PODD;
 
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
            final Model matchingModels = rdfStatements.filter((Resource)null, RDF.TYPE, fileType);
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
                 
                 final Set<Value> graph = model.filter(fileRef, PODD.PODD_BASE_HAS_SPARQL_GRAPH, null).objects();
                 if(!graph.isEmpty())
                 {
                     fileReference.setGraph(graph.iterator().next().stringValue());
                 }
                 
                 final Set<Value> alias = model.filter(fileRef, PODD.PODD_BASE_HAS_ALIAS, null).objects();
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
         return Collections.singleton(PODD.PODD_BASE_DATA_REFERENCE_TYPE_SPARQL);
     }
     
 }
