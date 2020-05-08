 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  * 
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under
  *  the Licence.
  */
 package eu.europeana.uim.europeanaspecific.workflows;
 
 import eu.europeana.uim.europeanaspecific.workflowstarts.httpzip.HttpZipWorkflowStart;
 import eu.europeana.uim.plugin.solr.service.SolrWorkflowPlugin;
 import eu.europeana.uim.util.BatchWorkflowStart;
 import eu.europeana.uim.workflow.AbstractWorkflow;
 
 /**
  *
  * @author Georgios Markakis <gwarkx@hotmail.com>
  * @since 23 May 2012
  */
 public class RepositoryIngestionWorkflow extends AbstractWorkflow{
 
	RepositoryIngestionWorkflow(){
 		super("Ingest into Repositotry",
 		        "Ingests everything into SOLR and MONGODB");
 
 		        setStart(new BatchWorkflowStart());
 
 		        addStep(new SolrWorkflowPlugin());
 	}
 
 	@Override
 	public boolean isSavepoint(String pluginIdentifier) {
 		return false;
 	}
 
 	@Override
 	public boolean isMandatory(String pluginIdentifier) {
 		return false;
 	}
 
 }
