 /**
  * 
  */
 package com.github.podd.exception;
 
 import org.semanticweb.owlapi.model.IRI;
 
 /**
  * An exception indicating that the artifact denoted by the given IRI was not managed by PODD.
  * 
  * @author Kutila
  * @since 04/01/2013
  * 
  */
 public class UnmanagedArtifactIRIException extends UnmanagedSchemaException
 {
     private static final long serialVersionUID = 4395800605913179651L;
     
     private final IRI artifactOntologyIRI;
     
     /**
      * 
      * @param ontology
      *            The OWL Ontology IRI that was not managed.
      * @param msg
      *            The message for this exception.
      */
     public UnmanagedArtifactIRIException(final IRI artifactOntologyIRI, final String msg)
     {
         super(msg);
         this.artifactOntologyIRI = artifactOntologyIRI;
     }
     
     /**
      * @param ontology
      *            The OWL Ontology IRI that was not managed.
      * @param msg
      *            The message for this exception.
      * @param throwable
      *            The cause for this exception.
      */
     public UnmanagedArtifactIRIException(final IRI artifactOntologyIRI, final String msg, final Throwable throwable)
     {
         super(msg, throwable);
         this.artifactOntologyIRI = artifactOntologyIRI;
     }
     
     /**
      * @param ontology
      *            The OWL Ontology IRI that was not managed.
      * @param throwable
      *            The cause for this exception.
      */
     public UnmanagedArtifactIRIException(final IRI artifactOntologyIRI, final Throwable throwable)
     {
         super(throwable);
         this.artifactOntologyIRI = artifactOntologyIRI;
     }
     
     /**
      * @return The OWL Ontology IRI that was not managed.
      */
     public IRI getOntologyID()
     {
         return this.artifactOntologyIRI;
     }
     
 }
