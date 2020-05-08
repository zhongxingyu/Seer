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
 
 import java.util.Collections;
 import java.util.Set;
 
import org.kohsuke.MetaInfServices;
 import org.openrdf.model.vocabulary.RDF;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddProcessorStage;
 import com.github.podd.api.file.DataReferenceProcessor;
 import com.github.podd.api.file.DataReferenceProcessorFactory;
 import com.github.podd.api.file.SSHFileReferenceProcessorFactory;
 import com.github.podd.utils.PODD;
 
 /**
  * An SSH File Reference Processor Factory that creates <code>SSHFileReferenceProcessorImpl</code>
  * instances.
  * 
  * @author kutila
  */
@MetaInfServices(DataReferenceProcessorFactory.class)
 public class SSHFileReferenceProcessorFactoryImpl implements SSHFileReferenceProcessorFactory
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
         return SSHFileReferenceProcessorFactoryImpl.STAGES.contains(stage);
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
                 .append(" <" + RDF.TYPE.stringValue() + "> <" + PODD.PODD_BASE_FILE_REFERENCE_TYPE_SSH.stringValue()
                         + "> . ");
         
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
         return SSHFileReferenceProcessorFactoryImpl.STAGES;
     }
     
 }
