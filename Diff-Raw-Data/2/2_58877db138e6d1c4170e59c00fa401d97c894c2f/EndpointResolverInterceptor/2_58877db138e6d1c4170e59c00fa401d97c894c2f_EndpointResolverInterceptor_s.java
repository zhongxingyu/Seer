 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.internal.core.interceptor;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.deployer.impl.Deployer;
 import org.apache.servicemix.jbi.runtime.ComponentRegistry;
 import org.apache.servicemix.jbi.runtime.ComponentWrapper;
 import org.apache.servicemix.jbi.runtime.impl.MessageExchangeImpl;
 import org.apache.servicemix.nmr.api.Channel;
 import org.apache.servicemix.nmr.api.Endpoint;
 import org.apache.servicemix.nmr.api.Exchange;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.api.Role;
 import org.apache.servicemix.nmr.api.internal.InternalEndpoint;
 import org.apache.servicemix.nmr.core.ChannelImpl;
 import org.apache.servicemix.nmr.core.EndpointRegistryImpl;
 import org.apache.servicemix.nmr.core.ExchangeImpl;
 import org.apache.servicemix.nmr.core.InternalEndpointWrapper;
 import org.apache.servicemix.nmr.core.PropertyMatchingReference;
 import org.apache.servicemix.nmr.core.StaticReferenceImpl;
 import org.eclipse.swordfish.core.Interceptor;
 import org.eclipse.swordfish.core.SwordfishException;
 import org.eclipse.swordfish.core.resolver.EndpointDescription;
 import org.eclipse.swordfish.core.resolver.EndpointMetadata;
 import org.eclipse.swordfish.core.resolver.ServiceResolver;
 import org.eclipse.swordfish.internal.core.resolver.ServiceResolverHolder;
 import org.eclipse.swordfish.internal.core.util.smx.ServiceMixSupport;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 import org.w3c.dom.DocumentFragment;
 
 public class EndpointResolverInterceptor<T> implements Interceptor, ServiceResolverHolder, InitializingBean {
 
 	private static final Log LOG = LogFactory.getLog(EndpointResolverInterceptor.class);
 	public static final String TARGETTED_TO_EXTERNAL_ENDPOINT_ME_PROPERTY = "TARGETTED_TO_EXTERNAL_ENDPOINT_ME_PROPERTY";
 	private NMR nmr;
     private ServiceResolver serviceResolver;
     private ComponentRegistry componentRegistry;
     private final Map<String, ?> properties = new HashMap<String, Object>();
 
     /**
      * Sets the mock endpoint as the target to the supplied messageExchange
      * @param exchange
      */
     private void setDummyExchangeDestination(Exchange exchange) {
     	try {
     		exchange.setTarget(null);
     		InternalEndpointWrapper internalEndpointWrapper = new InternalEndpointWrapper(new Endpoint() {
 				public void process(Exchange exchange) {
 					if (LOG.isDebugEnabled()){
 						LOG.debug("The stub has processed the exchange " + exchange);
 					}
 				}
 				public void setChannel(Channel arg0) {
 				}
 			}, new HashMap<String, String>());
 			internalEndpointWrapper.setChannel(new ChannelImpl(internalEndpointWrapper, ((EndpointRegistryImpl) nmr.getEndpointRegistry()).getExecutorFactory().createExecutor(EndpointResolverInterceptor.class.getSimpleName() + "MockExecutor"), nmr));
 			List<InternalEndpoint> internalEndpoints = new ArrayList<InternalEndpoint>();
 			internalEndpoints.add(internalEndpointWrapper);
 			exchange.setTarget(new StaticReferenceImpl(internalEndpoints));
 			if (exchange instanceof ExchangeImpl) {
 				ExchangeImpl exchangeImpl = (ExchangeImpl) exchange;
 				exchangeImpl.setDestination(internalEndpointWrapper);
 			}
 		} catch (Exception ex) {
 			LOG.info(ex.getMessage(), ex);
 		}
     }
 
 	public void process(MessageExchange messageExchange) throws SwordfishException {
 		Exchange exchange = ServiceMixSupport.toNMRExchange(messageExchange);
 		try {
 			if(messageExchange.getStatus() == ExchangeStatus.DONE){
 				LOG.warn("could't process terminated (status == DONE) message exchange!");
 				setDummyExchangeDestination(exchange);
 				return;
 			}
 
 			if (exchange.getRole() != Role.Consumer) {
 				return;
 			}
 
 			if (exchange.getTarget() != null && ServiceMixSupport.getEndpoint(getNmr(), exchange.getTarget()) != null) {
 			    return;
 			}
 			handleWrongInterfaceNameTargetProperty(exchange);
 			QName interfaceName = (QName) exchange.getProperty(Endpoint.INTERFACE_NAME);
 			if (interfaceName == null) {
 				interfaceName = (QName) exchange.getProperty(MessageExchangeImpl.INTERFACE_NAME_PROP);
 				if (interfaceName == null) {
 					return;
 				}
 			}
 
 			QName operation = exchange.getOperation();
 			if (operation == null) {
 				return;
 			}
 
 			EndpointDescription description =
 				getEndpointDescription(interfaceName, extractConsumerName(exchange));
 			if (description == null) {
 				throw new SwordfishException("Error resolving endpoint - no service "
 					+ "description has been found for the interface: " + interfaceName);
 			}
 
 			InternalEndpoint serviceEndpoint = getEndpointForExchange(messageExchange, description);
 			if (serviceEndpoint == null) {
 				throw new SwordfishException("Error resolving endpoint for the interface + ["
 					+ interfaceName + "] - no suitable endpoints have been found");
 			}
 			if (description.getMetadata() != null && isTargettedToExternalEndpoint(exchange, serviceEndpoint)) {
 				exchange.setProperty(EndpointMetadata.ENDPOINT_METADATA, description.getMetadata());
 				createSoapHeader(exchange);
 			}
 			exchange.setTarget(new StaticReferenceImpl(Arrays.asList(serviceEndpoint)));
 		} catch (Exception ex) {
 			LOG.error("The exception happened while trying to resolve service name via supplied wsdls ", ex);
 		}
 	}
 
 	private void handleWrongInterfaceNameTargetProperty(Exchange exchange) {
 		try {
 			if (exchange.getTarget() != null && exchange.getTarget() instanceof PropertyMatchingReference) {
 				PropertyMatchingReference propertyMatchingReference = (PropertyMatchingReference) exchange.getTarget();
 				Field propertiesField = PropertyMatchingReference.class.getDeclaredField("properties");
 				propertiesField.setAccessible(true);
 				Map props = (Map) propertiesField.get(propertyMatchingReference);
 				if (props.containsKey(Endpoint.SERVICE_NAME)) {
 					Map newProps = new HashMap();
 					newProps.put(Endpoint.SERVICE_NAME, props.get(Endpoint.SERVICE_NAME));
 					InternalEndpoint serviceEndpoint = ServiceMixSupport.getEndpoint(getNmr(), newProps);
 					if (serviceEndpoint != null) {
 						exchange.setTarget(new StaticReferenceImpl(Arrays.asList(serviceEndpoint)));
 					}
 				}
 			}
 		} catch (Exception ex) {
			LOG.error("The exception happened while trying to resolve service name via supplied wsdls ", ex);
 		}
 	}
 
 
 	private boolean isTargettedToExternalEndpoint(Exchange exchange, InternalEndpoint serviceEndpoint) {
 		if (exchange.getProperties().containsKey(TARGETTED_TO_EXTERNAL_ENDPOINT_ME_PROPERTY) &&exchange.getProperty(TARGETTED_TO_EXTERNAL_ENDPOINT_ME_PROPERTY).equals(Boolean.TRUE)) {
 			return true;
 		}
 		if (serviceEndpoint.getMetaData().containsKey(Deployer.TYPE) && serviceEndpoint.getMetaData().get(Deployer.TYPE).equals(Deployer.TYPE_BINDING_COMPONENT)) {
 			return true;
 		}
 		return false;
 	}
 
 	private void createSoapHeader(Exchange exchange) {
 		EndpointMetadata<?> metadata = (EndpointMetadata<?>) exchange.getProperty(EndpointMetadata.ENDPOINT_METADATA);
 		Map headers = new HashMap();
 		headers.put(new QName("http://eclipse.org/swordfish/headers", "Policy"), metadata.toXml());
 		exchange.getIn().setHeader("org.apache.servicemix.soap.headers", headers);
 	}
 
 
 	private EndpointDescription getEndpointDescription(QName interfaceName, QName consumerName) {
 		EndpointDescription description = null;
 		Collection<EndpointDescription> descriptions = getServiceResolver().getEndpointsFor(interfaceName, consumerName);
 		if (descriptions.size() > 0) {
 			// add policy matching logic to choose a suitable endpoint
 			// for now use a first one from the list
 			description = descriptions.iterator().next();
 		}
 		return description;
 	}
 
 	private InternalEndpoint getEndpointForExchange(MessageExchange messageExchange,
 			EndpointDescription description) {
 		QName serviceName = description.getServiceName();
 		Map<String,Object> props = new HashMap<String, Object>();
 		props.put(Endpoint.SERVICE_NAME, serviceName.toString());
 		InternalEndpoint serviceEndpoint = ServiceMixSupport.getEndpoint(getNmr(), props);
 
 		if (serviceEndpoint != null) {
 			LOG.info("The service endpoint for the servicename + [" + serviceName + "] has been found");
 		} else {
 			LOG.info("No service endpoints for the service + [" + serviceName + "] have been found");
 			LOG.info("Trying to establish a dynamic outbound endpoint for the service: " + serviceName);
 
 			ServiceEndpoint se = null;
 			ComponentWrapper wrapper = null;
 			DocumentFragment endpRef = ServiceMixSupport.getEndpointReference(description);
 
 	        for (ComponentWrapper component : getComponentRegistry().getServices()) {
 	            se = component.getComponent().resolveEndpointReference(endpRef);
 	            if (se != null) {
 	            	wrapper = component;
 	                break;
 	            }
 	        }
 
 	        if (wrapper != null && se != null) {
 	        	Map<String, ?> compProps = getComponentRegistry().getProperties(wrapper);
 	        	props = new HashMap<String, Object>();
 	        	if (compProps.containsKey(ComponentRegistry.NAME)) {
 	        		Object compName = compProps.get(ComponentRegistry.NAME);
 	        		props.put(ComponentRegistry.NAME, compName);
 	        	}
 
 	        	if (compProps.containsKey(ComponentRegistry.TYPE)) {
 	        		Object compType = compProps.get(ComponentRegistry.TYPE);
 	        		props.put(ComponentRegistry.TYPE, compType);
 	        	}
 
 	        	serviceEndpoint = ServiceMixSupport.getEndpoint(getNmr(), props);
 	        	if (serviceEndpoint != null) {
 	        		LOG.info("Succesfully established an outbound endpoint for the service: " + serviceName);
 	        		messageExchange.setProperty(TARGETTED_TO_EXTERNAL_ENDPOINT_ME_PROPERTY, true);
 	        		messageExchange.setEndpoint(se);
 	        	} else {
 	        		LOG.warn("Couldn't get an endpoint for the service: " + serviceName);
 	        	}
 	        } else {
         		LOG.warn("Couldn't get an endpoint for the service: " + serviceName);
 	        }
 		}
         return serviceEndpoint;
 	}
 
 	public NMR getNmr() {
 		return nmr;
 	}
 
 	public void setNmr(NMR nmr) {
 		this.nmr = nmr;
 	}
 
 	public ComponentRegistry getComponentRegistry() {
 		return componentRegistry;
 	}
 
 	public void setComponentRegistry(ComponentRegistry componentRegistry) {
 		this.componentRegistry = componentRegistry;
 	}
 
 	public org.eclipse.swordfish.core.resolver.ServiceResolver getServiceResolver() {
 		return serviceResolver;
 	}
 
 	public void setServiceResolver(ServiceResolver serviceResolver) {
 		this.serviceResolver = serviceResolver;
 	}
 
 	public Map<String, ?> getProperties() {
 	    return properties;
 	}
 
 	public String getId() {
 		return getClass().getName();
 	}
 
 	public void afterPropertiesSet() throws Exception {
 		Assert.notNull(nmr);
 		Assert.notNull(componentRegistry);
 		Assert.notNull(serviceResolver);
 	}
 
 	private QName extractConsumerName(final Exchange xch) {
 		final Object o = xch.getProperty(ServiceResolver.POLICY_CONSUMER_NAME);
 		return o instanceof QName ? (QName) o : null;
 	}
 }
