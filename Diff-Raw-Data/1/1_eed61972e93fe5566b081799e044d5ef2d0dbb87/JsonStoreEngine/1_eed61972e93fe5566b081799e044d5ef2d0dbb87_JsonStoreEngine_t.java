 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.web.dojo.json;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.reflect.Array;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.util.StringUtils;
 
 import com.evinceframework.core.factory.MapBackedClassLookupFactory;
 import com.evinceframework.web.dojo.json.JsonSerializationContext.DeferredSerialization;
 import com.evinceframework.web.dojo.json.conversion.PojoConverter;
 import com.evinceframework.web.dojo.json.conversion.MapConverter;
 import com.fasterxml.jackson.core.JsonEncoding;
 import com.fasterxml.jackson.core.JsonFactory;
 
 /**
  * The core entry point for serializing a graph of Java objects into a JSON format that can 
  * be consumed on the web client or any other application that supports javascript and JSON.  
  * 
  * The outputted format is a single array of javascript objects.  Any references to other 
  * objects are signified using the field name identified by referenceFieldName.
  * 
  * The JsonStoreEngine delegates the conversion of an object to JSON to {@link JsonConverter}s.
  * A custom {@link JsonConverter} can be registered using the setLookupMap(lookupMap) method.
  * The engine will determine the {@link JsonConverter} to use based on the class of the object
  * that needs to be serialized.
  * 
  * In the process of converting an graph of data to a single list of elements, the JsonStoreEngine 
  * handles the logic of determining when an object should be serialized or when the serialization
  * should be deferred and a reference object should be serialized.
  * 
  * The developer implementing a {@link JsonConverter} only needs to be concerned with how a Java
  * object should be converted to a JSON object. 
  * 
  * Example output:
  * <code>
  * [
  *    { id: 'customer::1', _type: 'customer', name: 'Anderson, Jose', address: { _reference: 'address::1' } },
  *    { id: 'address::1', _type: 'address', address1: '2272 Stratford Drive', address2: 'Honolulu, HI 96814' },
  *    { id: 'customer::2', _type: 'customer', name: 'Gomez, Marcus', address: { _reference: 'address::2' } },
  *    { id: 'address::2', _type: 'address', address1: '2718 College Avenue', address2: 'Dayton, OH 45459' },
  *    { id: 'customer::3', _type: 'customer', name: 'Kelley, Eleanor', address: { _reference: 'address::3' } },
  *    { id: 'address::3', _type: 'address', address1: '1611 Mandan Road', address2: 'Steelville, MO 65565' },
  *    { id: 'customer::4', _type: 'customer', name: 'Deen, Martha', address: { _reference: 'address::4' } },
  *    { id: 'address::4', _type: 'address', address1: '4104 Poe Lane', address2: 'Kansas City, KS 66215' },
  *    { id: 'customer::5', _type: 'customer', name: 'Marquez, Elaine', address: { _reference: 'address::5' } },
  *    { id: 'address::5', _type: 'address', address1: '3800 Deans Lane', address2: 'Bedford Village, NY 10506' },
  *    { id: 'product::1', _type: 'product', name: 'Ball Original Classic Pectin Small Batch', price: 1.07 },
  *    { id: 'product::2', _type: 'product', name: 'Ball 4-Pk 16 Oz. Wide Mouth Class Canning Jars with Lids', price: 4.48 },
  *    { id: 'product::3', _type: 'product', name: 'Compost Wizard Universal Composter Base', price: 61.00 },
  *    { id: 'product::4', _type: 'product', name: 'Exaco 187-Gallon Composter', price: 270.56 },
  *    { id: 'product::5', _type: 'product', name: 'Behlan Country 10\' x 6\' Outdoor Kennel', price: 299.00 },
  *    { id: 'product::6', _type: 'product', name: 'PetSafe Wireless Pet Containment System', price: 249.00 },
  *    { id: 'product::7', _type: 'product', name: 'Red Toolbox Toolbox House Woodworking Kit', price: 15.41 },
  *    { id: 'product::8', _type: 'product', name: 'Top Choice 2 x 6 x 8 #2 Prime Treated Lumber', price: 4.27 },
  *    { id: 'orderStatus::1', _type: 'orderStatus', name: 'Pending', displayOrder: 10 },
  *    { id: 'orderStatus::2', _type: 'orderStatus', name: 'Back Ordered', displayOrder: 30 },
  *    { id: 'orderStatus::3', _type: 'orderStatus', name: 'Shipped', displayOrder: 20 },    
  *    { id: 'order::1', _type: 'order', orderNumber: 1234, customer: { _reference: 'customer::1' }, orderStatus: { _reference: 'orderStatus::1' },
  *      lineItems:[ { _reference: 'orderLine::1' }, { _reference: 'orderLine::2' } ]},
  *    { id: 'orderLine::1', _type: 'orderLine', order: { _reference: 'order::1' }, price: 1.07, quantity: 3, product: { _reference: 'product::1' } },
  *    { id: 'orderLine::2', _type: 'orderLine', order: { _reference: 'order::1' }, price: 4.48, quantity: 1, product: { _reference: 'product::2' } },
  *    { id: 'order::2', _type: 'order', orderNumber: 1235, customer: { _reference: 'customer::2' }, orderStatus: { _reference: 'orderStatus::1' },
  *      lineItems:[ { _reference: 'orderLine::3' }, { _reference: 'orderLine::4' }]},
  *    { id: 'orderLine::3', _type: 'orderLine', order: { _reference: 'order::2' }, price: 4.48, quantity: 1, product: { _reference: 'product::2' } },
  *    { id: 'orderLine::4', _type: 'orderLine', order: { _reference: 'order::2' }, price: 61.00, quantity: 1, product: { _reference: 'product::3' } },
  *    { id: 'order::3', _type: 'order', orderNumber: 1236, customer: { _reference: 'customer::2' }, orderStatus: { _reference: 'orderStatus::3' },
  *      lineItems:[ { _reference: 'orderLine::5' }, { _reference: 'orderLine::6' } ]},
  *    { id: 'orderLine::5', _type: 'orderLine', order: { _reference: 'order::3' }, price: 270.56, quantity: 1, product: { _reference: 'product::4' } },
  *    { id: 'orderLine::6', _type: 'orderLine', order: { _reference: 'order::3' }, price: 299.00, quantity: 2, product: { _reference: 'product::5' } }  
  *  ]
  * </code>
  * 
  * @author Craig Swing
  */
 public class JsonStoreEngine extends MapBackedClassLookupFactory<JsonConverter> {
 
 	public static final String DEFAULT_IDENTIFIER_NAME = "id";
 	
 	public static final String DEFAULT_TYPE_NAME = "_type";
 	
 	private JsonFactory jsonFactory = new JsonFactory();
 	
 	private JsonEncoding encoding = JsonEncoding.UTF8;
 	
 	private String referenceFieldName = "_reference";
 	
 	private String identifierFieldName = DEFAULT_IDENTIFIER_NAME;
 	
 	private String typeFieldName = DEFAULT_TYPE_NAME;
 			
 	private Set<Class<?>> primitiveTypes = new HashSet<Class<?>>();
 	
 	public JsonStoreEngine() {
 		primitiveTypes.add(String.class);
 		primitiveTypes.add(Integer.class);
 		primitiveTypes.add(BigInteger.class);
 		primitiveTypes.add(Double.class);
 		primitiveTypes.add(Date.class);	
 		primitiveTypes.add(Boolean.class);
 		primitiveTypes.add(BigDecimal.class);
 		
 		primitiveTypes = Collections.unmodifiableSet(primitiveTypes);
 		
 		setDefaultImplementation(new PojoConverter());
 		getLookupMap().put(Map.class, new MapConverter());
 	}
 	
 	/**
 	 * A set of Java classes that translate into primitives in javascript. When the engine
 	 * encounters these types, the values will be written as the primitive value and not 
 	 * a javascript object.
 	 * 
 	 * @return
 	 */
 	public Set<Class<?>> getPrimitiveTypes() {
 		return primitiveTypes;
 	}
 
 	/**
 	 * Override the default Java classes that are considered primitives in javascript.
 	 * 
 	 * @param primitiveTypes the Java classes to consider primitives.
 	 */
 	public void setPrimitiveTypes(Set<Class<?>> primitiveTypes) {
 		this.primitiveTypes = Collections.unmodifiableSet(primitiveTypes);
 	}
 	
 	/**
 	 * When creating references to other objects, this field name will be used as the JSON field name.
 	 * The default is _reference.
 	 * 
 	 * In the following example, the address property is another object that exists in the top level array
 	 * with an id of address::1.
 	 * 
 	 * <code>
 	 * address: { _reference: 'address::1' }
 	 * </code>
 	 * 
 	 * @return the name of the field to use when building a reference.
 	 */
 	public String getReferenceFieldName() {
 		return referenceFieldName;
 	}
 
 	/**
 	 * Override the name of the field to use when building a reference.
 	 * 
 	 * @param referenceFieldName  the field name to use.
 	 */
 	public void setReferenceFieldName(String referenceFieldName) {
 		this.referenceFieldName = referenceFieldName;
 	}
 
 	/**
 	 * When serializing a Java object, the JSON object can be uniquely identified by the identifier field. 
 	 * The default is id.
 	 * 
 	 * @return the name of the field that contains a value that uniquely identifies the object. 
 	 */
 	public String getIdentifierFieldName() {
 		return identifierFieldName;
 	}
 
 	/**
 	 * Override the name if the field used to identify the JSON object.
 	 * 
 	 * @param identifierFieldName
 	 */
 	public void setIdentifierFieldName(String identifierFieldName) {
 		this.identifierFieldName = identifierFieldName;
 	}
 
 	/**
 	 * When serializing a Java object, the {@link JsonConverter} can optionally specify a "type".  Client side 
 	 * code can utilize this type when searching through the array of items looking for a particular group of items.
 	 * 
 	 * If the {@link JsonConverter} does specify a type, it will be serialized using the field name specified by this
 	 * property.  The default is _type.
 	 * 
 	 * @return the name of the field that contains the objects type.
 	 */
 	public String getTypeFieldName() {
 		return typeFieldName;
 	}
 
 	/**
 	 * Override the name used to specify an Objects type.
 	 * 
 	 * @param typeFieldName
 	 */
 	public void setTypeFieldName(String typeFieldName) {
 		this.typeFieldName = typeFieldName;
 	}
 
 	/**
 	 * Serializes a graph of Java objects into a single array of javascript objects using a
 	 * JSON format.
 	 * 
 	 * @param out the JSON is written to this stream.
 	 * @param model the object graph to serialize
 	 * @throws IOException
 	 */
 	public void serialize(OutputStream out, Object model) throws IOException {
 		
 		JsonSerializationContext context = new JsonSerializationContext(
 				this, jsonFactory.createJsonGenerator(out, encoding));
 		
 		context.getGenerator().writeStartArray();
 		
 		writeArrayItems(context, model, false);
 		
 		boolean wasWritten = true;
 		while(wasWritten) {
 			wasWritten = writeDeferredObject(context);
 		}
 		
 		context.getGenerator().writeEndArray();
 		context.getGenerator().close();
 	}
 	
 	/**
 	 * Serializes a graph of Java objects into a single array of javascript objects using a
 	 * JSON format.
 	 * 
 	 * @param model the object graph to serialize
 	 * @return the serialized objects in a JSON format.
 	 */
 	public String serialize(Object model) {
 		
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		
 		try {											
 			serialize(baos, model);
 			return baos.toString();
 			
 		} catch (IOException e) {
 			
 			return "{ error: \"An unknown error occurred.\" }";
 			
 		} finally {
 			try {
 				baos.close();
 			} catch (IOException e) { /* ignore */ }
 		}
 	}	
 
 	/* package */ void writeProperty(JsonSerializationContext context, String name, Object value) throws IOException {
 		
 		context.getGenerator().writeFieldName(name);
 		
 		if (isPrimitiveObject(value)) {
 			context.getGenerator().writeObject(value);
 			return;
 		}
 		
 		if (isJavascriptArrayType(value)) {
 			writeArray(context, value, true);
 			return;
 		}
 		
 		writeObject(context, value, true);
 	}
 	
 	private void writeArray(JsonSerializationContext context, Object arrOrObj, boolean useReferences) throws IOException {
 		
 		context.getGenerator().writeStartArray();
 		writeArrayItems(context, arrOrObj, useReferences);
 		context.getGenerator().writeEndArray();
 	}
 	
 	private void writeArrayItems(JsonSerializationContext context, Object arrOrObj, boolean useReferences) throws IOException {
 		
 		if (arrOrObj != null) {
 			
 			if (isJavascriptArrayType(arrOrObj)) {
 				
 				if (arrOrObj instanceof Iterable<?>) {						
 					for(Object obj : (Iterable<?>)arrOrObj) {								
 						writeObject(context, obj, useReferences);			
 					}			
 				
 				} else if (arrOrObj.getClass().isArray()) {
 					for (int i = 0; i < Array.getLength(arrOrObj); i++) {						
 						writeObject(context, Array.get(arrOrObj, i), useReferences);
 					}
 				
 			 	} else {
 			 		throw new JsonStoreException.UnknownArrayType();
 			 	}
 				
 			} else {
 				writeObject(context, arrOrObj, useReferences);
 			}
 			
 		}
 	}
 
 	private void writeObject(JsonSerializationContext context, Object obj, boolean asReference) throws IOException {
 		
 		if (obj == null) {
 			context.getGenerator().writeNull();
			return;
 		}	
 		
 		if (isPrimitiveObject(obj)) {
 			throw new JsonStoreException.ObjectIsPrimitive();
 		}
 		
 		if (isJavascriptArrayType(obj)) {
 			throw new JsonStoreException.ObjectIsArray();
 		}
 		
 		// lookup converter
 		JsonConverter converter = lookup(obj.getClass());
 		if (converter == null) {
 			throw new JsonStoreException.UnknownJsonConverter();
 		}
 		
 		if (asReference) {
 			// register deferred serialization object
 			context.registerDeferredSerialization(obj, converter);
 			
 			context.getGenerator().writeStartObject();
 			context.getGenerator().writeStringField(referenceFieldName, converter.determineIdentifier(obj));
 			context.getGenerator().writeEndObject();
 		
 		} else {
 			writeObject(context, obj, converter);
 		}
 	}
 
 	private void writeObject(JsonSerializationContext context, Object obj, JsonConverter converter) throws IOException {
 		
 		context.getGenerator().writeStartObject();
 		
 		String identifier = converter.determineIdentifier(obj);
 		context.registerIdentifier(identifier);
 		context.getGenerator().writeStringField(identifierFieldName, identifier);
 		
 		String type = converter.determineType(obj);
 		if (StringUtils.hasText(type)) {
 			context.getGenerator().writeStringField(typeFieldName, type);
 		}
 		
 		converter.writeObjectProperties(context, obj);
 		
 		context.getGenerator().writeEndObject();
 	}
 
 	private boolean writeDeferredObject(JsonSerializationContext context) throws IOException {
 		DeferredSerialization ds = context.popDeferred();
 		if (ds == null) {
 			return false;
 		}
 		
 		writeObject(context, ds.getValue(), ds.getConverter());
 		return true;
 	}
 	
 	private boolean isPrimitiveObject(Object value) {
 		return value != null && primitiveTypes.contains(value.getClass());
 	}
 	
 	private boolean isJavascriptArrayType(Object value) 
     {
 		if (value == null)
 			return false;
 		
         return isACollectionThatsNotAMap(value)
         	|| value.getClass().isArray();
     }	
 	
 	private static boolean isACollectionThatsNotAMap(Object value) {
 		return value instanceof Collection<?> && !(value instanceof Map<?, ?>);
 	}	
 }
