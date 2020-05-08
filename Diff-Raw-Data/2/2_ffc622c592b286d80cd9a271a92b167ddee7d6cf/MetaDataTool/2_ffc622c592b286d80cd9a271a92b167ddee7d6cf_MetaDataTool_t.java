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
 package org.paxle.gui.impl.tools;
 
 import java.util.Map;
 
 import org.apache.velocity.tools.Scope;
 import org.apache.velocity.tools.config.DefaultKey;
 import org.apache.velocity.tools.config.ValidScope;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.paxle.core.metadata.IMetaData;
 import org.paxle.core.metadata.IMetaDataProvider;
 import org.paxle.core.metadata.IMetaDataService;
 
 @DefaultKey("metaData")
 @ValidScope(Scope.REQUEST)
 public class MetaDataTool extends PaxleLocaleConfig {
 	private IMetaDataService metaService = null;
 
 	public void configure(@SuppressWarnings("unchecked") Map props) {
 		super.configure(props);
 		
 		// getting the meta-data service
 		if (this.context != null) {
 			ServiceReference ref = this.context.getServiceReference(IMetaDataService.class.getName());
 			if (ref != null) {
 				this.metaService = (IMetaDataService) this.context.getService(ref);
 			}
 		}
 	}
 	
 	/**
 	 * Getting the {@link IMetaData} of a service registered to the OSGi framework with the given {@link Constants#SERVICE_PID service-PID}.
 	 * @param servicePID the {@link Constants#SERVICE_PID service-PID}
 	 * @return the {@link MetaData} object or <code>null</code>
 	 */
 	public IMetaData getMetaData(String servicePID) {
 		try {
 			// getting a reference to the service with the given ID
 			ServiceReference[] refs = context.getServiceReferences(
 					null, 
 					String.format("(%s=%s)",
 							Constants.SERVICE_PID,
 							servicePID
 					)
 			);
 			if (refs == null || refs.length == 0) return null;
 			
 			// getting the service
 			Object service = null;
 			try {
 				service = context.getService(refs[0]);
 				if (IMetaDataProvider.class.isAssignableFrom(service.getClass())) {
 					// getting the metadata
 					return ((IMetaDataProvider)service).getMetadata(servicePID, this.getLocale().toString());
 				}
 			} finally {
 				context.ungetService(refs[0]);
 			}
 			
 			if (this.metaService != null) {
 				return this.metaService.getMetadata(servicePID, this.getLocale().toString());
 			}
 			
 		} catch (Exception e) {
 			this.logger.error(e);
 		}
 		return null;
 	}
 }
