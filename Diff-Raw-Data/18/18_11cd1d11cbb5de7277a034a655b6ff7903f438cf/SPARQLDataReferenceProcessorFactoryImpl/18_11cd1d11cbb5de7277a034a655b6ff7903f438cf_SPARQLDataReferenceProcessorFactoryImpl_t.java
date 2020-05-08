 package com.github.podd.impl.file;
 
 import java.util.Collections;
 import java.util.Set;
 
 import org.kohsuke.MetaInfServices;
 import org.openrdf.model.vocabulary.RDF;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddProcessorStage;
 import com.github.podd.api.file.DataReferenceProcessor;
 import com.github.podd.api.file.DataReferenceProcessorFactory;
 import com.github.podd.api.file.SPARQLDataReferenceProcessorFactory;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * An SPARQL Data Reference Processor Factory that creates {@link SPARQLDataReferenceProcessorImpl}
  * instances.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 @MetaInfServices(DataReferenceProcessorFactory.class)
 public class SPARQLDataReferenceProcessorFactoryImpl implements SPARQLDataReferenceProcessorFactory
 {
     
     protected final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /* The fixed set of stages supported by this Factory */
     private static final Set<PoddProcessorStage> STAGES = Collections.singleton(PoddProcessorStage.RDF_PARSING);
     
     @Override
     public boolean canHandleStage(final PoddProcessorStage stage)
     {
         if(stage == null)
         {
             throw new NullPointerException("Cannot handle NULL stage");
         }
         return SPARQLDataReferenceProcessorFactoryImpl.STAGES.contains(stage);
     }
     
     @Override
     public String getKey()
     {
         return this.getClass().getName();
     }
     
     @Override
     public String getParentSPARQLVariable()
     {
         // to find all file references directly under a given object, parent could be bound
         return "parent";
     }
     
     @Override
     public DataReferenceProcessor getProcessor()
     {
         return new SSHFileReferenceProcessorImpl();
     }
     
     @Override
     public String getSPARQLConstructBGP()
     {
         return " ?subject ?predicate ?object . ?parent ?containsPredicate ?subject . ";
     }
     
     @Override
     public String getSPARQLConstructWhere()
     {
         final StringBuilder builder = new StringBuilder();
         
         // match all triples about a subject whose TYPE is poddBase:SSHFileReference
         builder.append(" ?")
                 .append(this.getSPARQLVariable())
                 .append(" <" + RDF.TYPE.stringValue() + "> <"
                        + PoddRdfConstants.PODD_BASE_DATA_REFERENCE_TYPE_SPARQL.stringValue() + "> . ");
         
         builder.append(" ?").append(this.getSPARQLVariable()).append(" ?predicate ?object . ");
         
         builder.append(" ?").append(this.getParentSPARQLVariable()).append(" ?containsPredicate ").append(" ?")
                 .append(this.getSPARQLVariable());
         
         return builder.toString();
     }
     
     @Override
     public String getSPARQLGroupBy()
     {
         // an empty GROUP BY clause
         return "";
     }
     
     @Override
     public String getSPARQLVariable()
     {
         // to find ALL file references, subject should not be bound
         return "subject";
     }
     
     @Override
     public Set<PoddProcessorStage> getStages()
     {
         return SPARQLDataReferenceProcessorFactoryImpl.STAGES;
     }
     
 }
