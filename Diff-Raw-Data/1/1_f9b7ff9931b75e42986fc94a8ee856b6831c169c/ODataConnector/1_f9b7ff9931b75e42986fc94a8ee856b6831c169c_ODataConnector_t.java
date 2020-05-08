 /**
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.odata;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.apache.commons.beanutils.PropertyUtilsBean;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.mule.api.MuleMessage;
 import org.mule.api.NestedProcessor;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Connector;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.lifecycle.Start;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.api.transport.PropertyScope;
 import org.mule.modules.odata.factory.ODataConsumerFactory;
 import org.mule.modules.odata.factory.ODataConsumerFactoryImpl;
 import org.mule.modules.odata.odata4j.extensions.OBatchRequest;
 import org.odata4j.consumer.ODataClientRequest;
 import org.odata4j.consumer.ODataConsumer;
 import org.odata4j.core.Guid;
 import org.odata4j.core.OCollection;
 import org.odata4j.core.OCollections;
 import org.odata4j.core.OComplexObjects;
 import org.odata4j.core.OCreateRequest;
 import org.odata4j.core.ODataVersion;
 import org.odata4j.core.OEntity;
 import org.odata4j.core.OModifyRequest;
 import org.odata4j.core.OObject;
 import org.odata4j.core.OProperties;
 import org.odata4j.core.OProperty;
 import org.odata4j.core.OQueryRequest;
 import org.odata4j.core.OSimpleObjects;
 import org.odata4j.edm.EdmCollectionType;
 import org.odata4j.edm.EdmComplexType;
 import org.odata4j.edm.EdmProperty.CollectionKind;
 import org.odata4j.edm.EdmSimpleType;
 import org.odata4j.edm.EdmType;
 import org.odata4j.format.FormatType;
 import org.odata4j.format.FormatWriter;
 import org.odata4j.internal.InternalUtil;
 import org.odata4j.jersey.consumer.ConsumerDeleteEntityRequest;
 import org.odata4j.jersey.consumer.JerseyClientUtil;
 import org.odata4j.producer.resources.BatchBodyPart;
 import org.odata4j.producer.resources.BatchResult;
 import org.odata4j.producer.resources.ODataBatchProvider.HTTP_METHOD;
 
 /**
  * Connector for consuming OData feeds by performing read, create, update and delete operations.
  * Bath operations are also supported.
  * 
  * @author mariano.gonzalez@mulesoft.com
  *
  */
 @Connector(name = "odata", schemaVersion = "1.0", friendlyName = "OData Connector with Authentication", minMuleVersion = "3.3", configElementName="config")
 public class ODataConnector {
 
 	private static final Logger logger = Logger.getLogger(ODataConnector.class);
 	private static final PropertyUtilsBean propertyUtils = new PropertyUtilsBean();
 	private static final String BATCH_PARTS = "ODATA_CONNECTOR_BATCH_BODY_PARTS";
 	
 	/**
 	 * An instance of {@link org.mule.modules.odata.factory.ODataConsumerFactory}
 	 * to intanciate the {@link org.odata4j.consumer.ODataConsumer}. Normally you don't
 	 * need to set this unless you require some custom initialization of the consumer
 	 * or if you are doing test cases.
 	 * 
 	 * If this property is not specified, then an instance of
 	 * {@link org.mule.modules.odata.factory.ODataConsumerFactoryImpl.ODataConsumerFactoryImpl} is used 
 	 */
 	@Configurable
 	@Optional
 	private ODataConsumerFactory consumerFactory;
 	
 	/***
 	 * The OData service root uri 
 	 */
 	@Configurable
 	private String baseServiceUri;
 	
 	/**
 	 * If authentication is required, please provide username
 	 */
 	@Configurable
 	@Optional
 	private String username;
 	
 	/**
 	 * If authentication is required, please provide password
 	 */
 	@Configurable
 	@Optional
 	private String password;
 	
 	/**
 	 * The protocol version to be used when consuming external services
 	 */
 	@Configurable
 	@Optional
 	@Default("V2")
 	private ODataVersion consumerVersion = ODataVersion.V2 ;
 	
 	/**
 	 * The consumer to use
 	 */
 	private ODataConsumer consumer;
 	
 	/**
 	 * The namig policy to be used when mapping pojo's attributes to OData entities.
 	 * Depending on the OData service you're consuming, you might find that attributes usually follows a
 	 * lower camel case format (e.g.: theAttribute) or an upper camel case format (e.g.: TheAttribute).
 	 * 
 	 * The naming format assumes that your pojo's properties follow the lower camel case
 	 * format (just as the java coding standard dictates) and translates to the format that the OData service
 	 * is expecting.
 	 * 
 	 * Valid values are: LOWER_CAMEL_CASE and UPPER_CAMEL_CASE.
 	 */
 	@Configurable
 	@Optional
 	@Default("LOWER_CAMEL_CASE")
 	private PropertyNamingFormat namingFormat = PropertyNamingFormat.LOWER_CAMEL_CASE;
 	
 	/**
 	 * The format of the payload to be used during communication.
 	 * Valid values are JSON and ATOM
 	 */
 	@Configurable
 	@Optional
 	@Default("JSON")
 	private FormatType formatType = FormatType.JSON;
 	
 	/**
 	 * This method initializes the module by creating the consumer and the factory (if needed)
 	 */
 	@Start
 	public void init() {
 		if (this.consumer == null) {
 			this.consumer = this.getConsumerFactory().newConsumer(this.getBaseServiceUri(), this.getFormatType(), this.username, this.password, this.getConsumerVersion());
 		}
 	}
 	
     /**
      * Reads entities from an specified set and returns it as a list of pojos
      *
      * {@sample.xml ../../../doc/OData-connector.xml.sample odata:get-entities}
      *
      * @param returnClass the canonical class name for the pojo instances to be returned
      * @param entitySetName the name of the set to be read
      * @param filter an OData filtering expression. If not provided, no filtering occurs (see http://www.odata.org/developers/protocols/uri-conventions#FilterSystemQueryOption)
      * @param orderBy the ordering expression. If not provided, no ordering occurs (see http://www.odata.org/developers/protocols/uri-conventions#OrderBySystemQueryOption(
      * @param skip number of items to skip, usefull for pagination. If not provided, no records are skept (see http://www.odata.org/developers/protocols/uri-conventions#SkipSystemQueryOption)
      * @param expand Sets the expand expressions.
      * @param top number of items to return (see http://www.odata.org/developers/protocols/uri-conventions#TopSystemQueryOption)
      * @param select the selection clauses. If not specified, all fields are returned (see http://www.odata.org/developers/protocols/uri-conventions#SelectSystemQueryOption)
      * @return a list of objects of class "returnClass" representing the obtained entities
      */
     @Processor
     @SuppressWarnings("unchecked")
     public List<Object> getEntities(
     						String returnClass,
     						String entitySetName,
     						@Optional String filter,
     						@Optional String orderBy,
     						@Optional String expand,
     						@Optional Integer skip,
     						@Optional Integer top,
     						@Optional String select) {
     	
     	Class<?> clazz = null;
     	
     	try {
     		clazz = Class.forName(returnClass);
     	} catch (ClassNotFoundException e) {
     		throw new IllegalArgumentException(String.format("return class %s not found in classpath", returnClass), e);
     	}
     	
     	
     	OQueryRequest<?> request =  this.consumer.getEntities(clazz, entitySetName)
 										.filter(filter)
 										.orderBy(orderBy)
 										.expand(expand)
 										.select(select);
     	
     	if (skip != null) {
     		request.skip(skip);
     	}
     	
     	if (top != null) {
     		request.top(top);
     	}
     	
     	return (List<Object>) request.execute().toList();
     }
     
     /**
      * Inserts an entity from an input pojo
      * 
      * {@sample.xml ../../../doc/OData-connector.xml.sample odata:create-entity}
      * 
      * @param message the current mule message
      * @param entity an object representing the entity
      * @param entitySetName the name of the set. If not specified then it's inferred by adding the suffix 'Set' to the objects simple class name
      * @param serviceUri optional override of the service Uri for this particular call. Leave blank for defaulting to the config one
      */
     @Processor
     @Inject
     public void createEntity(MuleMessage message,
     						@Optional @Default("#[payload]") Object entity,
     						@Optional String entitySetName,
     						@Optional String serviceUri) {
     	
     	serviceUri = InternalUtil.chooseServiceUri(this.baseServiceUri, serviceUri);
     	
     	OCreateRequest<OEntity> request = this.consumer.createEntity(this.getEntitySetName(entity, entitySetName));
     	Collection<OProperty<?>> properties = this.populateODataProperties(entity);
     	
 		if (properties != null) {
 			request.properties(properties);
 		}
 		
 		if (!this.isBatchOperation(message, request.getRawRequest(serviceUri))) {
 			request.execute(serviceUri);
 		}
     }
     
     /**
      * Updates an entity represented by a pojo on the OData service
      * 
      * {@sample.xml ../../../doc/OData-connector.xml.sample odata:update-entity}
      * 
      * @param message the current mule message
      * @param entity an object representing the entity
      * @param entitySetName the name of the set. If not specified then it's inferred by adding the suffix 'Set' to the objects simple class name
      * @param keyAttribute the name of the pojo's attribute that holds the entity's key. The attribute cannot hold a null value
      * @param serviceUri optional override of the service Uri for this particular call. Leave blank for defaulting to the config one
      */
     @Processor
     @Inject
     public void updateEntity(MuleMessage message,
     						@Optional @Default("#[payload]") Object entity,
     						@Optional String entitySetName,
     						String keyAttribute,
     						@Optional String serviceUri) {
     	
     	serviceUri = InternalUtil.chooseServiceUri(this.baseServiceUri, serviceUri);
     	
     	OModifyRequest<OEntity> request = this.consumer.mergeEntity(this.getEntitySetName(entity, entitySetName), this.extractValue(entity, keyAttribute));
     	Collection<OProperty<?>> properties = this.populateODataProperties(entity);
 
 		if (properties != null) {
 			request.properties(properties);
 		}
 		
 		if (!this.isBatchOperation(message, request.getRawRequest(serviceUri))) {
 			request.execute(serviceUri);
 		}
     }
     
     
     /**
      * Deletes an entity represented by a pojo on the OData service
      * 
      * {@sample.xml ../../../doc/OData-connector.xml.sample odata:delete-entity}
      * 
      * @param message the current mule message
      * @param entity an object representing the entity
      * @param entitySetName the name of the set. If not specified then it's inferred by adding the suffix 'Set' to the objects simple class name
      * @param keyAttribute the name of the pojo's attribute that holds the entity's key. The attribute cannot hold a null value
      * @param serviceUri optional override of the service Uri for this particular call. Leave blank for defaulting to the config one
      */
     @Processor
     @Inject
     public void deleteEntity(
     						MuleMessage message,
     						@Optional @Default("#[payload]") Object entity,
     						@Optional String entitySetName,
     						String keyAttribute,
     						@Optional String serviceUri) {
     	
     	serviceUri = InternalUtil.chooseServiceUri(this.baseServiceUri, serviceUri);
     	
     	ConsumerDeleteEntityRequest request = this.consumer.deleteEntity(
     												this.getEntitySetName(entity, entitySetName),
     												this.extractValue(entity, keyAttribute));
     	
     	if (!this.isBatchOperation(message, request.getRawRequest(serviceUri))) {
 			request.execute(serviceUri);
 		}
     }
     
     /**
      * Executes a series of insert/update/deletes in a batch grouped in one changeset.
      * 
      * {@sample.xml ../../../doc/OData-connector.xml.sample odata:batch}
      * 
      * @param message the current mule message
      * @param processors nested processors where each individual operation is to be performed
      * @param serviceUri optional override of the service Uri for this particular call. Leave blank for defaulting to the config one
      * @return an instance of {@link org.odata4j.producer.resources.BatchResult}
      */
     @Processor
     @Inject
     public BatchResult batch(MuleMessage message, List<NestedProcessor> processors) {
     	List<BatchBodyPart> parts = new ArrayList<BatchBodyPart>();
     	message.setInvocationProperty(BATCH_PARTS, parts);
     	
     	try {
     		for (NestedProcessor processor : processors) {
     			processor.process();
     		}
     	} catch (Exception e) {
     		throw new RuntimeException("Error on batch nested processor", e);
     	} finally {
     		message.removeProperty(BATCH_PARTS, PropertyScope.INVOCATION);
     	}
     	
     	if (parts.isEmpty()) {
     		if (logger.isDebugEnabled()) {
     			logger.debug("No parts where added by nested processors, exiting without sending a batch request");
     		}
     	}
     	
     	OBatchRequest request = this.consumer.createBatch();
     	BatchResult result = request.execute(parts, this.formatType);
     	
     	return result;
     	
     }
     
     private boolean isBatchOperation(MuleMessage message, ODataClientRequest request) {
     	List<BatchBodyPart> batchParts = message.getInvocationProperty(BATCH_PARTS);
     	
     	if (batchParts != null) {
     		batchParts.add(this.toBatchBodyPart(request));
     		return true;
     	}
     	
     	return false;
     	
     }
     
     private BatchBodyPart toBatchBodyPart(ODataClientRequest request) {
     	BatchBodyPart part = new BatchBodyPart();
 		FormatWriter<Object> formatWriter = JerseyClientUtil.newFormatWriter(request, this.formatType, this.consumerVersion);
 		
 		part.setEntity(JerseyClientUtil.toString(request, formatWriter));
 		part.setHttpMethod(HTTP_METHOD.valueOf(request.getMethod()));
 		part.setUri(request.getUrl());
 		
 		return part;
     }
     
 	private Object extractValue(Object pojo, String keyAttribute) {
 		assert pojo != null : "pojo cannot be null";
 		assert !StringUtils.isBlank(keyAttribute) : "ket attribute cannot be null";
 		
 		Object keyValue = null;
     	
 		try {
     		
     		keyValue = propertyUtils.getProperty(pojo, this.namingFormat.toJava(keyAttribute));
     		
     		if (keyValue == null) {
     			throw new IllegalStateException(String.format("the key attribute %s on pojo of class %s cannot be null", keyAttribute, pojo.getClass().getCanonicalName()));
     		}
     		
     		return keyValue;
     		
     	} catch (IllegalAccessException e) {
     		this.handleReadPropertyException(pojo, keyAttribute, e);
     	} catch (NoSuchMethodException e) {
     		this.handleReadPropertyException(pojo, keyAttribute, e);
     	} catch (InvocationTargetException e) {
     		this.handleReadPropertyException(pojo, keyAttribute, e);
     	}
 		
 		return keyValue;
 	}
     
     private void handleReadPropertyException(Object pojo, String propertyName, Exception e) {
     	throw new RuntimeException(String.format("Could not read property %s on pojo of class %s", propertyName, pojo.getClass().getCanonicalName()), e);
     }
     
     private String getEntitySetName(Object pojo, String entitySetName) {
     	if (pojo == null) {
     		throw new IllegalArgumentException("cannot use a null pojo");
     	}
     	
     	return StringUtils.isBlank(entitySetName) ? pojo.getClass().getSimpleName() + "Set" : entitySetName; 
     }
      
     private <T> List<OProperty<?>> populateODataProperties(T object) {
 		Collection<PropertyDescriptor> properties = this.describe(object.getClass());
 		
 		if (properties.isEmpty()) {
 			return null;
 		}
 		
 		List<OProperty<?>> result = new ArrayList<OProperty<?>>(properties.size());
 		
 		try {
 			for (PropertyDescriptor prop : properties) {
 				Object value = prop.getReadMethod().invoke(object, (Object[]) null);
 				
 				if (value != null) {
 					String key = this.namingFormat.toOData(prop.getName());
 					OProperty<?> property = this.toOProperty(key, value);
 					
 					if (property != null) {
 						result.add(property);
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		
 		return result;
 	}
     
     private OProperty<?> toOProperty(String key, Object value) {
     	
     	if (value instanceof Guid) {
 			return OProperties.guid(key, (Guid) value);
 		} else if (this.isSimpleType(value)) {
 			return OProperties.simple(key, value);
 		} else if (value instanceof Date) {
 			return OProperties.datetime(key, (Date) value);
 		} else if (value instanceof Collection) {
 			Collection<?> collection = (Collection<?>) value;
 			return collection.isEmpty() ? null : this.toCollectionProperty(key, collection); 
 		} else {
 			return this.toObjectProperty(key, value);
 		}
     }
     
     private OProperty<List<OProperty<?>>> toObjectProperty(String key, Object value) {
     	return OProperties.complex(key, this.getEdmComplexType(key, value), this.populateODataProperties(value));
     }
     
     private <T> OProperty<OCollection<? extends OObject>> toCollectionProperty(String key, Collection<T> collection) {
 		EdmCollectionType type = this.getCollectionType(key, collection);
 		OCollection.Builder<OObject> builder = OCollections.newBuilder(type);
 		EdmType itemType = null;
 		
 		for (T item : collection) {
 			
 			if (itemType == null) {
 				itemType = this.getEdmType(key, item);
 			}
 			
 			if (this.isSimpleType(item)) {
 				builder.add(OSimpleObjects.create(EdmSimpleType.forJavaType(item.getClass()), item));
 			} else {
 				builder.add(OComplexObjects.create(this.getEdmComplexType(key, item), this.populateODataProperties(item)));
 			}
 					
 		}
 		
 		return OProperties.collection(key, type, builder.build());
     }
     
     private EdmType getEdmType(String key, Object value) {
     	EdmType type = EdmSimpleType.forJavaType(value.getClass());
     	return type != null ? type : this.getEdmComplexType(key, value);
     }
     
     private EdmComplexType getEdmComplexType(String key, Object value) {
     	return EdmComplexType.newBuilder().setName(key).build();
     }
     
     private <T> EdmCollectionType getCollectionType(String key, Collection<T> collection) {
     	T sample = null;
     	
     	for (T value : collection) {
     		
     		if (value != null) {
     			sample = value;
     			break;
     		}
     	}
     	
     	if (sample == null) {
     		throw new IllegalArgumentException("Collection only had null values");
     	}
     	
     	return new EdmCollectionType(CollectionKind.List, this.getEdmType(key, sample));
     }
     
     
 	private <T> Collection<PropertyDescriptor> describe(Class<T> clazz) {
 		BeanInfo info = null;
 		try {
 			info = Introspector.getBeanInfo(clazz);
 		} catch (IntrospectionException e) {
 			throw new RuntimeException();
 		}
 		
 		Collection<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>(info.getPropertyDescriptors().length);
 		for (PropertyDescriptor property : info.getPropertyDescriptors()) {
 			if (property.getReadMethod() != null && property.getWriteMethod() != null) {
 				descriptors.add(property);
 			}
 		}
 		
 		return descriptors;
 	}
 	
 	private boolean isSimpleType(Object value) {
 		return this.isSimpleType(value.getClass());
 	}
 	
 	private boolean isSimpleType(Class<?> clazz) {
 		return EdmSimpleType.forJavaType(clazz) != null;
 	}
 	
 	public ODataConsumerFactory getConsumerFactory() {
 		
 		if (this.consumerFactory == null) {
 			this.consumerFactory = new ODataConsumerFactoryImpl();
 		}
 		
 		return consumerFactory;
 	}
 
 	public void setConsumerFactory(ODataConsumerFactory consumerFactory) {
 		this.consumerFactory = consumerFactory;
 	}
 
 	public ODataConsumer getConsumer() {
 		return consumer;
 	}
 
 	public void setConsumer(ODataConsumer consumer) {
 		this.consumer = consumer;
 	}
 
 	public PropertyNamingFormat getNamingFormat() {
 		return namingFormat;
 	}
 
 	public void setNamingFormat(PropertyNamingFormat namingFormat) {
 		this.namingFormat = namingFormat;
 	}
 
 	public FormatType getFormatType() {
 		return formatType;
 	}
 
 	public void setFormatType(FormatType formatType) {
 		this.formatType = formatType;
 	}
 
 	public ODataVersion getConsumerVersion() {
 		return consumerVersion;
 	}
 
 	public void setConsumerVersion(ODataVersion consumerVersion) {
 		this.consumerVersion = consumerVersion;
 	}
 	
 	public String getBaseServiceUri() {
 		return baseServiceUri;
 	}
 
 	public void setBaseServiceUri(String baseServiceUri) {
 		this.baseServiceUri = baseServiceUri;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 }
