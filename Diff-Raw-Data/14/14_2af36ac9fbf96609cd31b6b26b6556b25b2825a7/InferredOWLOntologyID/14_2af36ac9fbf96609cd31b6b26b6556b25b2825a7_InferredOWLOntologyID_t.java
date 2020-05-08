 /**
  * 
  */
 package com.github.podd.utils;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 
 /**
  * Extends the OWLOntologyID class to provide for another variable to track the inferred ontology
  * IRI that matches this ontology ID.
  * 
  * Use getBaseOWLOntologyID to get a typical OWLOntologyID instance representing the base portion of
  * this object.
  * 
  * Use getInferredOWLOntologyID to get a typical OWLOntologyID instance representing the inferred
  * portion of this object.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class InferredOWLOntologyID extends OWLOntologyID
 {
     private static final long serialVersionUID = 30402L;
     
    private final IRI inferredOntologyIRI;
     
     /**
      * Creates an {@link InferredOWLOntologyID} using three OWLAPI {@link IRI}s.
      */
     public InferredOWLOntologyID(final IRI baseOntologyIRI, final IRI baseOntologyVersionIRI,
             final IRI inferredOntologyIRI)
     {
         super(baseOntologyIRI, baseOntologyVersionIRI);
         
         this.inferredOntologyIRI = inferredOntologyIRI;
         
         // Override hashcode if inferredOntologyIRI is not null, otherwise leave the hashcode to
         // match upstream
         if(inferredOntologyIRI != null)
         {
             // super uses 37, so to be distinct we need to use a different prime number here, ie, 41
             this.hashCode += 41 * inferredOntologyIRI.hashCode();
         }
     }
     
     /**
      * Creates an {@link InferredOWLOntologyID} using three OpenRDF {@link URI}s.
      */
     public InferredOWLOntologyID(final URI baseOntologyIRI, final URI baseOntologyVersionIRI,
             final URI inferredOntologyIRI)
     {
        super(IRI.create(baseOntologyIRI), IRI.create(baseOntologyVersionIRI));
        
        if(inferredOntologyIRI != null)
        {
            this.inferredOntologyIRI = IRI.create(inferredOntologyIRI);
        }
        else
        {
            this.inferredOntologyIRI = null;
        }
     }
     
     @Override
     public boolean equals(final Object obj)
     {
         // super must be equals or we cannot be equals, which also takes into account anonymous
         // ontologies
         if(!super.equals(obj))
         {
             return false;
         }
         
         // make sure that we can still be equal to OWLOntologyID instances if we did not get an
         // InferredOntologyIRI
         if(obj instanceof InferredOWLOntologyID)
         {
             final InferredOWLOntologyID other = (InferredOWLOntologyID)obj;
             if(this.inferredOntologyIRI == null)
             {
                 if(other.inferredOntologyIRI == null)
                 {
                     return true;
                 }
                 else
                 {
                     return false;
                 }
             }
             else if(other.inferredOntologyIRI == null)
             {
                 return false;
             }
             else
             {
                 return this.inferredOntologyIRI.equals(other.inferredOntologyIRI);
             }
         }
         else
         {
             // if the other object was not an InferredOWLOntogyID then we will only be equal if we
             // do not have an inferred ontologyIRI ourselves
             return this.inferredOntologyIRI == null;
         }
         
     }
     
     /**
      * Returns the OWLOntologyID representing the base ontology, ie, without the inferred ontology,
      * so that the hashcode will match that of the real OWLOntologyID for the base ontology.
      * 
      * @return
      */
     public OWLOntologyID getBaseOWLOntologyID()
     {
         return new OWLOntologyID(this.getOntologyIRI(), this.getVersionIRI());
     }
     
     /**
      * @return the inferredOntologyIRI
      */
     public IRI getInferredOntologyIRI()
     {
         return this.inferredOntologyIRI;
     }
     
     /**
      * Returns the OWLOntologyID representing the inferred ontology. ie, without the base ontology,
      * so that the hashcode will match that of the real OWLOntologyID for the inferred ontology. <br/>
      * NOTE: We make the assumption that the ontology IRI and version IRI for versioned ontologies
      * are the same.
      * 
      * @return
      */
     public OWLOntologyID getInferredOWLOntologyID()
     {
         return new OWLOntologyID(this.getInferredOntologyIRI(), this.getInferredOntologyIRI());
     }
     
     @Override
     public String toString()
     {
         if(this.inferredOntologyIRI == null)
         {
             return super.toString();
         }
         else
         {
             return super.toString() + this.inferredOntologyIRI.toQuotedString();
         }
     }
     
     public Model toRDF()
     {
         Model result = new LinkedHashModel();
         ValueFactory vf = ValueFactoryImpl.getInstance();
         result.add(vf.createStatement(this.getOntologyIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
         result.add(vf.createStatement(this.getVersionIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
         result.add(vf.createStatement(this.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI, this.getVersionIRI()
                 .toOpenRDFURI()));
         if(this.getInferredOntologyIRI() != null)
         {
             result.add(vf.createStatement(this.getInferredOntologyIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
             result.add(vf.createStatement(this.getVersionIRI().toOpenRDFURI(),
                     PoddRdfConstants.PODD_BASE_INFERRED_VERSION, this.getInferredOntologyIRI().toOpenRDFURI()));
         }
         
         return result;
     }
 }
