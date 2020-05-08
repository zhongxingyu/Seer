 package com.github.podd.utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.rio.RDFHandler;
 import org.openrdf.rio.RDFHandlerException;
 
 /**
  * Utilities for working with {@link InferredOWLOntologyID}
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public class OntologyUtils
 {
     
     private OntologyUtils()
     {
     }
     
     /**
      * Serialises the given collection of {@link InferredOWLOntologyID} objects to RDF, adding the
      * {@link Statement}s to the given {@link Model}, or creating a new Model if the given model is
      * null.
      * <p>
      * This method wraps the serialisation from {@link InferredOWLOntologyID#toRDF()}.
      * 
      * @param input
      *            The collection of {@link InferredOWLOntologyID} objects to render to RDF.
      * @param result
      *            The Model to contain the resulting statements, or null to have one created
      *            internally
      * @return A model containing the RDF statements about the given ontologies.
      * @throws RDFHandlerException
      *             If there is an error while handling the statements.
      */
     public static Model ontologyIDsToModel(Collection<InferredOWLOntologyID> input, Model result)
     {
         Model results = result;
         
         if(results == null)
         {
             results = new LinkedHashModel();
         }
         
         for(InferredOWLOntologyID nextOntology : input)
         {
             results.addAll(nextOntology.toRDF());
         }
         
         return results;
     }
     
     /**
      * Serialises the given collection of {@link InferredOWLOntologyID} objects to RDF, adding the
      * {@link Statement}s to the given {@link RDFHandler}.
      * <p>
      * This method wraps the serialisation from {@link InferredOWLOntologyID#toRDF()}.
      * 
      * @param input
      *            The collection of {@link InferredOWLOntologyID} objects to render to RDF.
      * @param handler
      *            The handler for handling the RDF statements.
      * @throws RDFHandlerException
      *             If there is an error while handling the statements.
      */
     public static void ontologyIDsToHandler(Collection<InferredOWLOntologyID> input, RDFHandler handler)
         throws RDFHandlerException
     {
         for(InferredOWLOntologyID nextOntology : input)
         {
             for(Statement nextStatement : nextOntology.toRDF())
             {
                 handler.handleStatement(nextStatement);
             }
         }
     }
     
     /**
      * Extracts the {@link InferredOWLOntologyID} instances that are represented as RDF
      * {@link Statement}s in the given {@link Model}.
      * 
      * @param input
      *            The input model containing RDF statements.
      * @return A Collection of {@link InferredOWLOntologyID} instances derived from the statements
      *         in the model.
      */
     public static Collection<InferredOWLOntologyID> modelToOntologyIDs(Model input)
     {
         Collection<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
         
         Model typedOntologies = input.filter(null, RDF.TYPE, OWL.ONTOLOGY);
         
         for(Statement nextTypeStatement : typedOntologies)
         {
             if(nextTypeStatement.getSubject() instanceof URI)
             {
                 Model versions = input.filter((URI)nextTypeStatement.getSubject(), OWL.VERSIONIRI, null);
                 
                 for(Statement nextVersion : versions)
                 {
                     if(nextVersion.getObject() instanceof URI)
                     {
                         Model inferredOntologies =
                                 input.filter((URI)nextVersion.getObject(), PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                                         null);
                         
                         if(inferredOntologies.isEmpty())
                         {
                             results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                     (URI)nextTypeStatement.getObject(), null));
                         }
                         else
                         {
                             for(Statement nextInferredOntology : inferredOntologies)
                             {
                                if(nextInferredOntology.getObject() instanceof URI)
                                 {
                                     results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                             (URI)nextTypeStatement.getObject(), (URI)nextInferredOntology.getObject()));
                                 }
                             }
                         }
                     }
                 }
             }
         }
         
         return results;
     }
 }
