 package net.ech.config;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.TypeVariable;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.List;
 import net.ech.doc.Document;
 import net.ech.doc.DocumentResolver;
 import net.ech.doc.DocPath;
 import net.ech.util.BeanPropertyMap;
 import net.ech.util.TypeCoercionSupport;
 import net.ech.util.TypeMismatchException;
 
 /**
  * Configurator implementation that interprets a {#link net.ech.doc.Document} hierarchy as
  * configuration and builds a parallel object tree.
  */
 public class DocumentBasedConfigurator
 	implements Configurator
 {
 	private static Map<Class<?>,List<SubtypeDescriptor>> subtypeDescriptorMap = new HashMap<Class<?>,List<SubtypeDescriptor>>();
 
 	private Document document;
 	private DocumentResolver documentResolver;
 
 	public DocumentBasedConfigurator(Document document)
 	{
 		this.document = document;
 	}
 
 	public DocumentBasedConfigurator(Document document, DocumentResolver documentResolver)
 	{
 		this(document);
 		this.documentResolver = documentResolver;
 	}
 
 	public DocumentBasedConfigurator(String key, DocumentResolver documentResolver)
 		throws IOException
 	{
 		this(documentResolver.resolve(key).produce(), documentResolver);
 	}
 
 	/**
 	 * Produce the object described by the document.
 	 */
 	@Override
 	public Object configure()
 		throws ConfigException
 	{
 		return new Builder().build();
 	}
 
 	/**
 	 * Produce the object described by the document.
 	 * @param requiredClass  fail if the object cannot be coerced to the given type
 	 */
 	@Override
 	public <T> T configure(Class<T> requiredClass)
 		throws ConfigException
 	{
 		return new Builder().build(requiredClass);
 	}
 
 	private class Builder
 	{
 		private Map<String,Object> context = new HashMap<String,Object>();
 		private List<Map<String,Object>> contextHistory = new ArrayList<Map<String,Object>>();
 
 		Builder()
 		{
 			context.put("$config.root", document);
 			context.put("$config.local", document);
 			if (documentResolver != null) {
 				context.put("$config.resolver", documentResolver);
 			}
 		}
 
 		public Object build()
 			throws ConfigException
 		{
 			return materialize(document, null);
 		}
 
 		public <T> T build(Class<T> requiredClass)
 			throws ConfigException
 		{
 			return requiredClass.cast(materialize(document, requiredClass));
 		}
 
 		private Object materialize(Document dq, Class<?> typeHint)
 			throws ConfigException
 		{
 			try {
 				dq = fillDocument(dq);
 
 				Object matter;
 				if (dq.get(Map.class) != null) {
 					matter =  materializeMap(dq, typeHint);
 				}
 				else if (dq.get(List.class) != null) {
 					matter =  materializeList(dq, typeHint);
 				}
 				else {
 					matter = materializeScalar(dq, typeHint);
 				}
 				return typeHint == null ? matter : TypeCoercionSupport.coerce(typeHint, matter);
 			}
 			catch (ConfigException e) {
 				throw e;
 			}
 			catch (TypeMismatchException e) {
 				throw dq.getPath().size() == 0 ? new ConfigException(e) : new ConfigException(dq.getPath().toString() + ": " + e.getMessage(), e);
 			}
 			catch (IOException e) {
 				throw dq.getPath().size() == 0 ? new ConfigException(e) : new ConfigException(dq.getPath().toString() + ": " + e.getMessage(), e);
 			}
 		}
 
 		private Document fillDocument(Document dq)
 			throws IOException
 		{
 			Document extendsDoc = dq.find("__extends");
 			if (!extendsDoc.isNull()) {
 				String superName = extendsDoc.get(String.class);
 				return extendDocument(superName != null ? Collections.singletonList(superName) : extendsDoc.require(List.class), dq);
 			}
 			return dq;
 		}
 
 		private Document extendDocument(List<String> superList, Document dq)
 			throws IOException
 		{
 			Document baseDoc = null;
 			for (String key : superList) {
 				if (documentResolver == null) {
 					throw new InternalConfigException("this configurator is not capable of resolving __extends");
 				}
 				Document superDoc = documentResolver.resolve(key).produce();
 				if (superDoc.isNull()) {
 					throw new InternalConfigException(key + ": (__extends) no such key");
 				}
 				superDoc = fillDocument(superDoc);
 				baseDoc = baseDoc == null ? superDoc : baseDoc.extend(superDoc);
 			}
 			return baseDoc == null ? dq : dq.extend(baseDoc);
 		}
 
 		private Object materializeMap(Document dq, Class<?> typeHint)
 			throws IOException
 		{
 			Object refObject = handleRef(dq);
 			if (refObject != null) {
 				return refObject;
 			}
 
 			Class<?> implClass = findMapImplementationClass(dq, typeHint);
 			updateContext(dq);
 
 			try {
 				if (implClass == null) {
 					Map<String,Object> map = new HashMap<String,Object>();
 					mapProperties(dq, null, map);
 					return Document.class.equals(typeHint) ? new Document(map).copy() : map;
 				}
 
 				Document argsDoc = dq.find("__args");
 				int nParams = 0;
 				if (!argsDoc.isNull()) {
 					nParams = argsDoc.get(List.class) == null ? 1 : argsDoc.get(List.class).size();
 				}
 
 				Constructor<?> cons = pickConstructor(implClass, nParams);
 				if (cons == null) {
 					throw new InternalConfigException(implClass + ": no compatible constructor");
 				}
 				Object[] consParams = new Object[nParams];
 
 				if (argsDoc.get(List.class) != null) {
 					List<Document> childDocs = argsDoc.children();
 					for (int i = 0; i < nParams; ++i) {
						consParams[i] = materialize(childDocs.get(i), cons.getParameterTypes()[i]);
 					}
 				}
 				else if (!argsDoc.isNull()) {
 					consParams[0] = materialize(argsDoc, cons.getParameterTypes()[0]);
 				}
 
 				Object obj = cons.newInstance(consParams);
 				BeanPropertyMap bpm = new BeanPropertyMap(obj);
 				mapProperties(dq, bpm, bpm);
 				return obj;
 			}
 			catch (InstantiationException e) {
 				throw new InternalConfigException(e);
 			}
 			catch (IllegalAccessException e) {
 				throw new InternalConfigException(e);
 			}
 			catch (java.lang.reflect.InvocationTargetException e) {
 				throw new InternalConfigException(e);
 			}
 			finally {
 				popContext();
 			}
 		}
 
 		private Constructor<?> pickConstructor(Class<?> targetClass, int nParams)
 		{
 			for (Constructor<?> cons : targetClass.getConstructors()) {
 				if (cons.getParameterTypes().length == nParams) {
 					return cons;
 				}
 			}
 			return null;
 		}
 
 		private Object materializeList(Document dq, Class<?> typeHint)
 			throws IOException
 		{
 			return Document.class.equals(typeHint) ? dq.copy() : listProperties(dq, typeHint != null && typeHint.isArray() ? typeHint.getComponentType() : Object.class);
 		}
 
 		private Object materializeScalar(Document dq, Class<?> typeHint)
 			throws IOException
 		{
 			return Document.class.equals(typeHint) ? dq : dq.get();
 		}
 
 		private Class<?> findMapImplementationClass(Document dq, Class<?> typeHint)
 			throws IOException
 		{
 			if (typeHint == null) {
 				typeHint = Object.class;
 			}
 			else if (typeHint.equals(Document.class)) {
 				typeHint = Map.class;
 			}
 
 			Class<?> implClass;
 			try {
 				implClass = dq.find("__type").isNull() ? typeHint : Class.forName(dq.find("__type").require(String.class));
 			}
 			catch (ClassNotFoundException e) {
 				throw new InternalConfigException(e);
 			}
 
 			// Catch type mismatches.
 			if (!typeHint.equals(implClass)) {
 				implClass.asSubclass(typeHint);  // throws ClassCastException
 			}
 
 			if (Map.class.equals(implClass) || Object.class.equals(implClass)) {
 				// If Object or Map is requested explicitly, return null to indicate that a Map should be instantiated.
 				implClass = null;
 			}
 			else if (implClass.isInterface()) {
 				// Handle request for interface by looking up a matching subtype configuration descriptor.
 				implClass = findMatchingSubtype(dq, implClass);
 			}
 
 			return implClass;
 		}
 
 		private Class<?> findMatchingSubtype(Document dq, Class<?> baseClass)
 			throws IOException
 		{
 			Class<?> implClass = null;
 
 			List<SubtypeDescriptor> subtypeDescriptors = getSubtypeDescriptors(baseClass);
 			if (subtypeDescriptors != null) {
 				for (SubtypeDescriptor subtypeDescriptor : subtypeDescriptors) {
 					if (subtypeDescriptor.getConfigPredicate().evaluate(dq)) {
 						if (implClass != null) {
 							throw new InternalConfigException("ambiguous subtype");
 						}
 						implClass = subtypeDescriptor.getSubtype();
 					}
 				}
 			}
 
 			if (implClass == null) {
 				throw new InternalConfigException("does not appear to configure a subtype of " + baseClass);
 			}
 			return implClass;
 		}
 
 		private void mapProperties(Document dq, BeanPropertyMap bpm, Map<String,Object> map)
 			throws IOException
 		{
 			for (Document cdq : dq.children()) {
 				String key = cdq.getPath().getLast().toString();
 				if (!key.startsWith("__")) {
 					Class<?> expectedPropertyType = Object.class;
 					if (bpm != null) {
 						bpm.assertProperty(key);
 						expectedPropertyType = bpm.getPropertyClass(key);
 					}
 					map.put(key, materialize(cdq, expectedPropertyType));
 				}
 			}
 		}
 
 		private List<Object> listProperties(Document dq, Class<?> typeHint)
 			throws IOException
 		{
 			List<Object> result = new ArrayList<Object>();
 			for (Document cdq : dq.children()) {
 				result.add(materialize(cdq, typeHint));
 			}
 			return result;
 		}
 
 		private Object handleRef(Document dq)
 			throws IOException
 		{
 			String ref = dq.find("__ref").cast(String.class, null);
 			if (ref != null) {
 				if (!context.containsKey(ref)) {
 					throw new IOException(ref + ": undefined");
 				}
 				return context.get(ref);
 			}
 			return null;
 		}
 
 		private void updateContext(Document dq)
 			throws IOException
 		{
 			Map<String,Object> newContext = new HashMap<String,Object>(context);
 			newContext.putAll(context);
 			newContext.put("$config.local", dq);
 			Document contextDoc = dq.find("__context");
 			if (contextDoc.cast(Map.class, null) != null) {
 				mapProperties(contextDoc, null, newContext);
 			}
 			contextHistory.add(context);
 			context = newContext;
 		}
 
 		private void popContext()
 		{
 			int size = contextHistory.size();
 			context = contextHistory.get(size - 1);
 			contextHistory.remove(size - 1);
 		}
 	}
 
 	private static List<SubtypeDescriptor> getSubtypeDescriptors(Class<?> iClass)
 	{
 		synchronized (subtypeDescriptorMap) {
 			if (!subtypeDescriptorMap.containsKey(iClass)) {
 
 				SubtypeDescriptor[] subtypeDescriptors = SubtypeDescriptor.discover(iClass);
 				if (subtypeDescriptors == null) {
 					return null;
 				}
 
 				subtypeDescriptorMap.put(iClass, Arrays.asList(subtypeDescriptors));
 			}
 			return subtypeDescriptorMap.get(iClass);
 		}
 	}
 }
