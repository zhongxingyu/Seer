 /**
  *
  * Copyright (c) 2012, PetalsLink
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package org.ow2.play.service.client;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.ow2.play.governance.api.EventGovernance;
 import org.ow2.play.governance.api.SimplePatternService;
 import org.ow2.play.governance.api.SubscriptionService;
 import org.ow2.play.governance.api.TopicAware;
 import org.ow2.play.governance.api.TopicRegistry;
 import org.ow2.play.metadata.api.service.MetadataService;
 import org.ow2.play.service.registry.api.Constants;
 import org.ow2.play.service.registry.api.Registry;
 import org.ow2.play.service.registry.api.RegistryException;
 import org.petalslink.dsb.cxf.CXFHelper;
 
 /**
  * Getting CXF-generated client classes for the PLAY platform access...
  * 
  * @author chamerling
  * 
  */
 public class PlayClient {
 
 	protected String registryEndpoint;
 
 	private Registry client;
 
 	public PlayClient(String registryEndpoint) {
 		this.registryEndpoint = registryEndpoint;
 	}
 
 	protected synchronized Registry getRegistryClient() {
 		if (client == null) {
 			client = CXFHelper.getClientFromFinalURL(registryEndpoint,
 					Registry.class);
 		}
 		return client;
 	}
 
 	protected <T> T getWSClient(String key, Class<T> clazz)
 			throws ClientException {
 		try {
 			return CXFHelper.getClientFromFinalURL(
 					getRegistryClient().get(key), clazz);
 		} catch (RegistryException e) {
 			throw new ClientException(e);
 		}
 	}
 
 	public Map<String, String> list() throws ClientException {
 		Map<String, String> result = new HashMap<String, String>();
 		try {
 			for (String key : getRegistryClient().keys()) {
 				result.put(key, getRegistryClient().get(key));
 			}
 		} catch (RegistryException e) {
 			throw new ClientException(e);
 		}
 		return result;
 	}
 
 	/**
 	 * Where to manage DSB business topics...
 	 * 
 	 * @return
 	 * @throws ClientException
 	 */
 	public TopicAware getDSBTopicAware() throws ClientException {
 		return getWSClient(Constants.DSB_BUSINESS_TOPIC_MANAGEMENT,
 				TopicAware.class);
 	}
 
 	/**
 	 * Get metadata client
 	 * 
 	 * @return
 	 * @throws ClientException
 	 */
 	public MetadataService getMetadataService() throws ClientException {
 		return getWSClient(Constants.METADATA, MetadataService.class);
 	}
 	
 	/**
 	 * Get the governance client
 	 * 
 	 * @return
 	 * @throws ClientException
 	 */
 	public EventGovernance getEventGovernance() throws ClientException {
 		return getWSClient(Constants.GOVERNANCE, EventGovernance.class);
 	}
 	
 	/**
 	 * Get the subscription client
 	 * 
 	 * @return
 	 * @throws ClientException
 	 */
 	public SubscriptionService getSubscriptionService() throws ClientException {
 		return getWSClient(Constants.GOVERNANCE_SUBSCRIPTION_SERVICE, SubscriptionService.class);
 	}
 	
 	/**
 	 * Get the topic registry client
 	 * 
 	 * @return
 	 * @throws ClientException
 	 */
 	public TopicRegistry getTopicRegistryService() throws ClientException {
 		return getWSClient(Constants.GOVERNANCE_TOPICREGISTRY_SERVICE, TopicRegistry.class);
 	}
 	
 	/**
 	 * Get the pattern deployer client
 	 * 
 	 * @return
 	 * @throws ClientException
 	 */
 	public SimplePatternService getPatternService() throws ClientException {
		return getWSClient(Constants.GOVERNANCE_PATTERN_SERVICE, SimplePatternService.class);
 	}
 }
