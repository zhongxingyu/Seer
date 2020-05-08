 /**
  * 
  */
 package com.github.podd.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
 
 import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.rio.RDFFormat;
 import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
 import org.semanticweb.owlapi.io.StreamDocumentSource;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.UnmanagedSchemaException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.exception.UnmanagedSchemaOntologyIDException;
 import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PoddSchemaManagerImpl implements PoddSchemaManager
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     private PoddRepositoryManager repositoryManager;
     private PoddSesameManager sesameManager;
     private PoddOWLManager owlManager;
     
     /**
      * 
      */
     public PoddSchemaManagerImpl()
     {
         // TODO Auto-generated constructor stub
     }
     
     @Override
     public void downloadSchemaOntology(final OWLOntologyID schemaOntologyID, final OutputStream outputStream,
             final RDFFormat format, final boolean includeInferences) throws UnmanagedSchemaException
     {
         throw new RuntimeException("TODO: Implement downloadSchemaOntology");
     }
     
     @Override
     public InferredOWLOntologyID getCurrentSchemaOntologyVersion(final IRI schemaOntologyIRI)
         throws UnmanagedSchemaIRIException
     {
         if(schemaOntologyIRI == null)
         {
             throw new UnmanagedSchemaIRIException(schemaOntologyIRI, "NULL is not a managed schema ontology");
         }
         throw new RuntimeException("TODO: Implement getCurrentSchemaOntologyVersion(IRI)");
     }
     
     @Override
     public OWLOntology getSchemaOntology(final IRI schemaOntologyIRI) throws UnmanagedSchemaIRIException
     {
         throw new RuntimeException("TODO: Implement getSchemaOntology(IRI)");
     }
     
     @Override
     public OWLOntology getSchemaOntology(final OWLOntologyID schemaOntologyID)
         throws UnmanagedSchemaOntologyIDException
     {
         throw new RuntimeException("TODO: Implement getSchemaOntology(OWLOntologyID)");
     }
     
     @Override
     public void setCurrentSchemaOntologyVersion(final OWLOntologyID schemaOntologyID)
         throws UnmanagedSchemaOntologyIDException, IllegalArgumentException
     {
         throw new RuntimeException("TODO: Implement setCurrentSchemaOntologyVersion");
     }
     
     @Override
     public void setOwlManager(final PoddOWLManager owlManager)
     {
         this.owlManager = owlManager;
     }
     
     @Override
     public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
     {
         this.repositoryManager = repositoryManager;
     }
     
     @Override
     public void setSesameManager(final PoddSesameManager sesameManager)
     {
         this.sesameManager = sesameManager;
     }
     
     @Override
     public InferredOWLOntologyID uploadSchemaOntology(final InputStream inputStream, final RDFFormat fileFormat)
         throws OpenRDFException, IOException, OWLException, PoddException
     {
         return this.uploadSchemaOntology(null, inputStream, fileFormat);
     }
     
     @Override
     public InferredOWLOntologyID uploadSchemaOntology(final OWLOntologyID schemaOntologyID,
             final InputStream inputStream, final RDFFormat fileFormat) throws OpenRDFException, IOException,
         OWLException, PoddException
     {
         if(inputStream == null)
         {
             throw new NullPointerException("Schema Ontology input stream was null");
         }
         
         OWLOntologyDocumentSource owlSource = new StreamDocumentSource(inputStream, fileFormat.getDefaultMIMEType());
         OWLOntology ontology = this.owlManager.loadOntology(owlSource);
         
         if(ontology.isEmpty())
         {
             throw new EmptyOntologyException(ontology, "Schema Ontology contained no axioms");
         }
         
         if(schemaOntologyID != null)
         {
             // FIXME: Change OWLOntologyID to schemaOntologyID in this case
         }
         
         RepositoryConnection conn = null;
         
         try
         {
             conn = this.repositoryManager.getRepository().getConnection();
             conn.begin();
             
             this.owlManager.dumpOntologyToRepository(ontology, conn);
             
             final InferredOWLOntologyID nextInferredOntology = this.owlManager.inferStatements(ontology, conn);
             
             conn.commit();
             
             // update the link in the schema ontology management graph
             this.sesameManager.updateCurrentManagedSchemaOntologyVersion(nextInferredOntology, true, conn,
                     this.repositoryManager.getSchemaManagementGraph());
             
             // update the link in the schema ontology management graph
             // TODO: This is probably not the right method for this purpose
             this.sesameManager.updateCurrentManagedSchemaOntologyVersion(nextInferredOntology, true, conn,
                     this.repositoryManager.getSchemaManagementGraph());
             
             return new InferredOWLOntologyID(ontology.getOntologyID().getOntologyIRI(), ontology.getOntologyID()
                     .getVersionIRI(), nextInferredOntology.getOntologyIRI());
         }
         catch(OpenRDFException | IOException e)
         {
             if(conn != null && conn.isActive())
             {
                 conn.rollback();
             }
             
             throw e;
         }
         finally
         {
             if(conn != null && conn.isOpen())
             {
                 conn.close();
             }
         }
         
     }
     
      
 }
