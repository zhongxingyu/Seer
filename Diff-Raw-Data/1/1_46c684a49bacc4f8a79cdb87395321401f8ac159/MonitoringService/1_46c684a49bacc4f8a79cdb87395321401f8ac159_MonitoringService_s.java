 /*
  * #%L
  * Bitrepository Monitoring Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.monitoringservice;
 
 import java.util.Map;
 
 import org.bitrepository.access.AccessComponentFactory;
 import org.bitrepository.access.getstatus.GetStatusClient;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.bitrepository.protocol.security.SecurityManager;
 import org.bitrepository.service.LifeCycledService;
 
 public class MonitoringService implements LifeCycledService {
 
 	/** The settings. */
     private final Settings settings;
 	/** The store of collected statuses */
 	private final ComponentStatusStore statusStore;
 	/** The client for getting statuses. */
 	private final GetStatusClient getStatusClient;
 	/** The alerter for sending alarms */
 	private final MonitoringServiceAlerter alerter;
 	/** The status collector */
 	private final StatusCollector collector;
     
 	public MonitoringService(Settings settings, SecurityManager securityManager) {
 		this.settings = settings;
 		statusStore = new ComponentStatusStore(settings.getCollectionSettings().getGetStatusSettings().getContributorIDs());
 		alerter = new MonitoringServiceAlerter(settings, 
 		        ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager), statusStore);
 		getStatusClient = AccessComponentFactory.getInstance().createGetStatusClient(settings, securityManager,
 		        settings.getReferenceSettings().getMonitoringServiceSettings().getID());
 		collector = new StatusCollector(getStatusClient, settings, statusStore, alerter);
 		collector.start();
 	}
 	
 	public Map<String, ComponentStatus> getStatus() {
 	    return statusStore.getStatusMap();
 	}
 
     public void start() {
         //Nothing to do
     }
 	
 	public int getMaxRetries() {
 	    return settings.getReferenceSettings().getMonitoringServiceSettings().getMaxRetries().intValue();
 	} 
 	
 	public long getCollectionInterval() {
 	    return settings.getReferenceSettings().getMonitoringServiceSettings().getCollectionInterval();
 	}
 	
 	public void shutdown() {
 	    collector.stop();
 	    getStatusClient.shutdown();
 	}
 }
