 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.indexer.impl;
 
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.component.ComponentContext;
 import org.paxle.core.IMWComponent;
 import org.paxle.core.IMWComponentFactory;
 import org.paxle.core.doc.ICommand;
 import org.paxle.core.doc.IDocumentFactory;
 import org.paxle.core.threading.IMaster;
 import org.paxle.core.threading.IWorker;
 import org.paxle.core.threading.IWorkerFactory;
 
 /**
  * @scr.component metatype="false" immediate="true" 
  * @scr.service interface="org.paxle.core.threading.IWorkerFactory"
  */
@Component(metatype=false, immediate=true)
@Service(IWorkerFactory.class)
 public class WorkerFactory implements IWorkerFactory<IndexerWorker> {
 	
	@Reference(target="(docType=org.paxle.core.doc.IIndexerDocument)")
 	protected IDocumentFactory idocFactory;	
 
	@Reference
 	protected IMWComponentFactory componentFactory;
 
 	/**
 	 * A reference to the {@link IMWComponent master-worker-component} used
 	 * by this bundle.
 	 */
 	private IMWComponent<ICommand> mwComponent;	
 	
 	/**
 	 * for logging
 	 */
 	private final Log logger = LogFactory.getLog(this.getClass());	
 	
 	protected void activate(ComponentContext context) throws IOException {
 		final BundleContext bc = context.getBundleContext();
 		this.logger.info("Initializing mwcomponent for bundle: " + bc.getBundle().getSymbolicName());
 		
 		this.mwComponent = componentFactory.createCommandComponent(this, 5, ICommand.class);
 		this.componentFactory.registerComponentServices(this.mwComponent, bc);
 	}
 	
 	protected void deactivate(ComponentContext context ){
 		if (this.mwComponent != null) {
 			// shutdown threads
 			IMaster<?> master = this.mwComponent.getMaster();
 			master.terminate();
 			
 			// unregister mw-components
 			// TODO: this.componentFactory.unregisterComponentServices(componentID, component, bc);		
 		}		
 	}
 	
 	public IndexerWorker createWorker() throws Exception {
 		return new IndexerWorker(this.idocFactory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see IWorkerFactory#initWorker(IWorker)
 	 */		
 	public void initWorker(IndexerWorker worker) {
 		// TODO Auto-generated method stub
 		
 	}
 }
