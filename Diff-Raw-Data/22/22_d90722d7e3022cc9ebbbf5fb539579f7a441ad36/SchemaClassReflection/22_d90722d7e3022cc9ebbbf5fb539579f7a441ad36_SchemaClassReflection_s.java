 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.romaframework.core.schema.reflection;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.core.GlobalConstants;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.aspect.Aspect;
 import org.romaframework.core.exception.ConfigurationNotFoundException;
 import org.romaframework.core.schema.FeatureLoader;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaClassResolver;
 import org.romaframework.core.schema.SchemaConfigurationLoader;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaHelper;
 import org.romaframework.core.schema.SchemaManager;
 import org.romaframework.core.schema.SchemaParameter;
 import org.romaframework.core.schema.SchemaReloader;
 import org.romaframework.core.schema.config.SaxSchemaConfiguration;
 import org.romaframework.core.schema.config.SchemaConfiguration;
 
 /**
  * Represent a class. It's not necessary that a Java class exist in the Classpath since you can define a SchemaClassReflection that
  * inherits another Java Class and use XML descriptor to customize it. This feature avoid the writing of empty class that simply
  * inherit real domain class.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
 public class SchemaClassReflection extends SchemaClass {
 
 	private static final long			serialVersionUID		= 8389722670237445799L;
 
 	private Class<?>							javaClass;
 
 	private SchemaClass						baseClass;
 
 	public static final String		GET_METHOD					= "get";
 
 	public static final String		IS_METHOD						= "is";
 
 	public static final String		SET_METHOD					= "set";
 
 	public static final String[]	IGNORE_METHOD_NAMES	= { "equals", "toString", "hashCode", "validate", "getClass", "clone" };
 
 	private static Log						log									= LogFactory.getLog(SchemaClassReflection.class);
 
 	public SchemaClassReflection(Class<?> iClass) {
 		super(Utility.getClassName(iClass));
 		javaClass = iClass;
 		// USED CLASS EQUALS FOR CROSS CLASSLOADER COMPARE
 		baseClass = iClass.getSuperclass() != null && iClass.getSuperclass().equals(Object.class) ? Roma.schema().getSchemaClass(iClass.getSuperclass()) : null;
 		config();
 	}
 
 	public SchemaClassReflection(String iEntityName, Class<?> iClass, SchemaClass iBaseClass, SchemaConfiguration iDescriptor) throws ConfigurationNotFoundException {
 		super(iEntityName);
 
 		if (iDescriptor == null)
 			iDescriptor = Roma.component(SchemaConfigurationLoader.class).getSaxSchemaConfiguration(iEntityName);
 
 		if (iClass == null && iBaseClass == null)
 			// ERROR: CANNOT ASSOCIATE A JAVA CLASS
 			throw new ConfigurationNotFoundException("Class " + iEntityName);
 
 		javaClass = iClass;
 		baseClass = iBaseClass;
 
 		if (iDescriptor != null) {
 			descriptor = iDescriptor;
 			if (descriptor instanceof SaxSchemaConfiguration)
 				Roma.component(SchemaReloader.class).addResourceForReloading(((SaxSchemaConfiguration) descriptor).getFile(), iEntityName);
 		}
 
 		File classFile = Roma.component(SchemaClassResolver.class).getFileOwner(name + SchemaClassResolver.CLASS_SUFFIX);
 
 		if (classFile != null)
 			Roma.component(SchemaReloader.class).addResourceForReloading(classFile, iEntityName);
 	}
 
 	@Override
 	public Object newInstanceFinal(Object... iArgs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException,
 			NoSuchMethodException {
 		Class<?> currClass = (Class<?>) (javaClass != null ? javaClass : baseClass.getLanguageType());
 
 		if (iArgs == null || iArgs.length == 0)
 			return currClass.newInstance();
 
 		// TODO: SUPPORT SEARCH OF THE RIGHT CONSTRUCTOR
 		Class<?>[] parameterTypes = new Class[iArgs.length];
 		for (int i = 0; i < iArgs.length; ++i)
 			if (iArgs[i] != null)
 				parameterTypes[i] = iArgs[i].getClass();
 
 		// TRY TO ASSIGN THE ENTITY IN THE CONSTRUCTOR
 		try {
 			Constructor<?> constructor = currClass.getConstructor(parameterTypes);
 			return constructor.newInstance(iArgs);
 		} catch (NoSuchMethodException e) {
 			if (parameterTypes.length == 1) {
 				Class<?> pType = parameterTypes[0].getSuperclass();
 				while (!pType.equals(Object.class)) {
 					try {
 						Constructor<?> constructor = currClass.getConstructor(new Class[] { pType });
 						return constructor.newInstance(iArgs);
 					} catch (NoSuchMethodException xe) {
 						pType = pType.getSuperclass();
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public boolean isOfType(Class<?> iLanguageClass) {
 		return javaClass != null ? javaClass.equals(iLanguageClass) : false;
 	}
 
 	@Override
 	public boolean isArray() {
 		return javaClass != null ? javaClass.isArray() : false;
 	}
 
 	@Override
 	public void config() {
 		beginConfig();
 		inspectInheritance();
 		readAllAnnotations();
 
 		readClass();
 		endConfig();
 	}
 
 	private void readClass() {
 		Class<?> iClass = (Class<?>) (javaClass != null ? javaClass : baseClass.getLanguageType());
 
 		ParameterizedType type = SchemaHelper.resolveParameterizedType(iClass);
 
 		List<Method> methods = SchemaHelper.getMethods(iClass);
 		List<Method> eventsToAdd = new ArrayList<Method>();
 		for (Method method : methods) {
 			// JUMP STATIC FIELDS OR NOT PUBLIC FIELDS
 			if (isToIgnoreMethod(method))
 				continue;
 			if (isGetterForField(method, type) || isSetterForField(method, type))
 				continue;
 			else if (isEvent(method)) {
 				eventsToAdd.add(method);
 				continue;
 			} else
 				createAction(method, type);
 
 		}
 
 		Field[] javaFields = iClass.getDeclaredFields();
 		for (Field curField : javaFields) {
 			SchemaField sf = getField(curField.getName());
 			if (sf instanceof SchemaFieldReflection) {
 				Class<?> genericFieldClass = SchemaHelper.resolveClassFromType(curField.getGenericType(), type);
 				SchemaClass fieldSchemaClass = Roma.schema().getSchemaClassIfExist(genericFieldClass);
 				if (sf.getType() == null || fieldSchemaClass.isAssignableAs(sf.getType().getSchemaClass())) {
 					sf.setType(fieldSchemaClass);
 					((SchemaFieldReflection) sf).field = curField;
 					((SchemaFieldReflection) sf).languageType = genericFieldClass;
 				}
 			}
 		}
 
 		List<SchemaField> curFields = new ArrayList<SchemaField>(fields.values());
 		for (SchemaField field : curFields) {
 			if ((field instanceof SchemaFieldReflection && !(field instanceof SchemaFieldDelegate)) && ((SchemaFieldReflection) field).getGetterMethod() == null) {
 				fields.remove(field.getName());
 				orderedFields.remove(field);
 			} else {
 				if (field instanceof SchemaFieldReflection)
 					((SchemaFieldReflection) field).configure();
 				field.setOrder(getFieldOrder(field));
 			}
 		}
 		Collections.sort(orderedFields);
 
 		List<SchemaAction> curActions = new ArrayList<SchemaAction>(actions.values());
 
 		for (SchemaAction action : curActions) {
 			if (action instanceof SchemaActionReflection)
 				((SchemaActionReflection) action).configure();
 			action.setOrder(getActionOrder(action));
 		}
 		Collections.sort(orderedActions);
 
 		addEvents(eventsToAdd);
 
 		List<SchemaEvent> curEvents = new ArrayList<SchemaEvent>(events.values());
 		for (SchemaEvent event : curEvents) {
 			if (event instanceof SchemaEventReflection)
 				((SchemaEventReflection) event).configure();
 			event.setOrder(getActionOrder(event));
 		}
 		Collections.sort(orderedActions);
 	}
 
 	private void createAction(Method method, ParameterizedType params) {
 		String methodSignature = SchemaAction.getSignature(method.getName(), method.getParameterTypes());
 		log.debug("[SchemaClassReflection] Class " + getName() + " found method: " + methodSignature);
 
 		SchemaActionReflection actionInfo = (SchemaActionReflection) getAction(methodSignature);
 
 		if (actionInfo == null) {
 			List<SchemaParameter> orderedParameters = new ArrayList<SchemaParameter>();
 			for (int i = 0; i < method.getParameterTypes().length; ++i) {
 				orderedParameters.add(new SchemaParameter("param" + i, Roma.schema().getSchemaClassIfExist(method.getParameterTypes()[i])));
 			}
 			// ACTION NOT EXISTENT: CREATE IT AND INSERT IN THE COLLECTION
 			actionInfo = new SchemaActionReflection(this, methodSignature, orderedParameters);
 			actionInfo.method = method;
 			setAction(methodSignature, actionInfo);
 		}
 		actionInfo.method = method;
 		actionInfo.setReturnType(Roma.schema().getSchemaClassIfExist(SchemaHelper.resolveClassFromType(method.getGenericReturnType(), params)));
 	}
 
 	private SchemaFieldReflection createField(String fieldName, Class<?> javaFieldType) {
 		log.debug("[SchemaClassReflection] Class " + getName() + " found field: " + fieldName);
 		SchemaFieldReflection fieldInfo;
 		// FIELD NOT EXISTENT: CREATE IT AND INSERT IN THE COLLECTION
 		fieldInfo = new SchemaFieldReflection(this, fieldName);
 		fieldInfo.languageType = javaFieldType;
 		fieldInfo.setType(Roma.schema().getSchemaClassIfExist(javaFieldType));
 		setField(fieldName, fieldInfo);
 		return fieldInfo;
 	}
 
 	public Boolean isGetterForField(Method method, ParameterizedType owner) {
 		int prefixLength;
 		String fieldName = method.getName();
 		if (fieldName.startsWith(GET_METHOD) && checkIfFirstCharAfterPrefixIsUpperCase(fieldName, GET_METHOD))
 			prefixLength = GET_METHOD.length();
 		else if (fieldName.startsWith(IS_METHOD) && checkIfFirstCharAfterPrefixIsUpperCase(fieldName, IS_METHOD))
 			prefixLength = IS_METHOD.length();
 		else
 			return false;
 		if (method.getParameterTypes() != null && method.getParameterTypes().length > 0)
 			return false;
 		if (fieldName.length() <= prefixLength)
 			return false;
 
 		fieldName = firstToLower(fieldName.substring(prefixLength));
 
 		Class<?> javaFieldClass = SchemaHelper.resolveClassFromType(method.getGenericReturnType(), owner);
 
 		SchemaFieldReflection fieldInfo = (SchemaFieldReflection) getField(fieldName);
 		if (fieldInfo == null) {
 			fieldInfo = createField(fieldName, javaFieldClass);
 			fieldInfo.getterMethod = method;
 		} else if (fieldInfo instanceof SchemaFieldReflection) {
 			if (fieldInfo instanceof SchemaFieldDelegate && !javaFieldClass.isAssignableFrom(((SchemaFieldReflection) fieldInfo).getLanguageType())) {
 				fieldInfo = createField(fieldName, javaFieldClass);
 				fieldInfo.getterMethod = method;
 			} else {
 				if (javaFieldClass.isAssignableFrom(((SchemaFieldReflection) fieldInfo).getLanguageType()))
 					fieldInfo.getterMethod = method;
 			}
 		}
 
 		return true;
 	}
 
 	public boolean isSetterForField(Method method, ParameterizedType owner) {
 		String fieldName = method.getName();
 		if (!fieldName.startsWith(SET_METHOD) || !checkIfFirstCharAfterPrefixIsUpperCase(fieldName, SET_METHOD))
 			return false;
 		if (method.getParameterTypes() != null && method.getParameterTypes().length != 1)
 			return false;
 
 		fieldName = firstToLower(fieldName.substring(SET_METHOD.length()));
 		Class<?> javaFieldClass = SchemaHelper.resolveClassFromType(method.getGenericParameterTypes()[0], owner);
 		SchemaFieldReflection fieldInfo = (SchemaFieldReflection) getField(fieldName);
 		if (fieldInfo == null) {
 			fieldInfo = createField(fieldName, javaFieldClass);
 			fieldInfo.setterMethod = method;
 		} else if (fieldInfo instanceof SchemaFieldReflection) {
 			if (fieldInfo instanceof SchemaFieldDelegate && !javaFieldClass.isAssignableFrom(((SchemaFieldReflection) fieldInfo).getLanguageType())) {
 				fieldInfo = createField(fieldName, javaFieldClass);
 				fieldInfo.setterMethod = method;
 			} else {
 				if (((SchemaFieldReflection) fieldInfo).getLanguageType().isAssignableFrom(javaFieldClass)) {
 					fieldInfo.setterMethod = method;
 					fieldInfo.setType(Roma.schema().getSchemaClassIfExist(javaFieldClass));
 				}
 			}
 		}
 		return true;
 
 	}
 
 	/**
 	 * Checks if the class method is an event
 	 * 
 	 * @param method
 	 *          -: the class method to check
 	 * 
 	 * @return true if is a event, false otherwise.
 	 */
 	public boolean isEvent(Method method) {
 		String eventMethodName = method.getName();
 		if (!eventMethodName.startsWith(SchemaEvent.ON_METHOD) || !checkIfFirstCharAfterPrefixIsUpperCase(eventMethodName, SchemaEvent.ON_METHOD))
 			return false;
 		return true;
 	}
 
 	/**
 	 * Adds to the events list the methods argument list.
 	 * 
 	 * <p>
 	 * If the event is not linkable to a field will be added as class event. Example: onNameEvent will be added as field event only if
 	 * exists a field "Name" in the class.
 	 * 
 	 * @param methodsToAdd
 	 *          -: methods to
 	 */
 	protected void addEvents(List<Method> methodsToAdd) {
 		for (Method method : methodsToAdd) {
 
 			// GET THE EVENT REAL NAME
 			String eventMethodName = method.getName();
 			eventMethodName = firstToLower(eventMethodName.substring(SchemaEvent.ON_METHOD.length()));
 
 			// GET THE EVENT ASSOCIATED FIELD NAME IF ANY
 			String eventName = lastCapitalWords(eventMethodName);
 			String fieldName = eventMethodName.substring(0, eventMethodName.length() - eventName.length());
 			eventName = firstToLower(eventName);
 
 			// ADDS THE EVENT TO THE LIST, AS EVENT FIELD IF THE FIELD NAME IS FOUND, TO THE CLASS EVENT OTHERWISE
 			if (fieldName.length() > 0) {
 				fieldName = firstToLower(fieldName);
 				SchemaField field = getField(fieldName);
 				if (field == null) {
 					log.warn("[SchemaClassReflection] Cannot associate the event '" + getName() + "." + eventName + "' to the field '" + getName() + "." + fieldName
 							+ "'. The event will be set to the class.");
 				} else {
 					addEvent(eventName, field, method);
 					continue;
 				}
 			}
 			addEvent(eventName, null, method);
 		}
 	}
 
 	public Class<?> getLanguageType() {
 		return (Class<?>) (javaClass != null ? javaClass : baseClass.getLanguageType());
 	}
 
 	@Override
 	public String toString() {
 		return name + (javaClass != null ? " (class:" + javaClass.getName() + ")" : "");
 	}
 
 	public SchemaClass getBaseClass() {
 		return baseClass;
 	}
 
 	protected void inspectInheritance() {
 		// REGISTER THE SUPER CLASS
 		Class<?> javaSuperClass = null;
 
 		if (javaClass != null)
 			// GET SUPER CLASS IF ANY
 			javaSuperClass = javaClass.getSuperclass();
 		else
 			// COPY FROM BASE CLASS
 			javaSuperClass = (Class<?>) baseClass.getLanguageType();
 
 		superClass = baseClass;
 		if (superClass == null && javaSuperClass != null) {
 			if (Roma.existComponent(SchemaManager.class)) {
 				superClass = searchForInheritedClassOrInterface(javaSuperClass);
 			} else
 				superClass = new SchemaClassReflection(javaSuperClass);
 
 			if (javaSuperClass.equals(this))
 				// EXTENSION BY CONVENTION NAME: REMOVE PARENT TO AVOID RECURSION
 				javaSuperClass = null;
 		}
 
 		if (superClass == null && !name.equals(GlobalConstants.ROOT_CLASS))
 			// FORCE ALL THE CLASS TO EXTEND THE ROOT 'OBJECT'
 			superClass = Roma.schema().getSchemaClass(Object.class);
 
 		// REGISTER ALL IMPLEMENTED INTERFACES
 		if (javaClass != null) {
 			Class<?>[] ifcs = javaClass.getInterfaces();
 			if (ifcs != null) {
 				SchemaClass schemaIfc;
 				for (Class<?> ifc : ifcs) {
 					schemaIfc = searchForInheritedClassOrInterface(ifc);
 					addImplementsInterface(schemaIfc);
 				}
 			}
 		}
 
 		if (superClass != null && !superClass.getLanguageType().equals(Object.class))
 			// LAST REAL INHERITANCE WINS
 			makeDependency(superClass);
 	}
 
 	@Override
 	protected void makeDependency(SchemaClass iClass) {
 
 		super.makeDependency(iClass);
 		Class<?> clazz = (Class<?>) (javaClass != null ? javaClass : baseClass.getLanguageType());
 		ParameterizedType type = SchemaHelper.resolveParameterizedType(clazz);
 		for (SchemaField schemaField : fields.values()) {
 			if (schemaField instanceof SchemaField) {
 				SchemaFieldReflection schemaFieldReflection = (SchemaFieldReflection) schemaField;
 				Class<?> javaFieldClass = null;
 				if (schemaFieldReflection.getterMethod != null) {
 					javaFieldClass = SchemaHelper.resolveClassFromType(schemaFieldReflection.getterMethod.getGenericReturnType(), type);
 				} else if (schemaFieldReflection.field != null) {
 					javaFieldClass = SchemaHelper.resolveClassFromType(schemaFieldReflection.field.getGenericType(), type);
 				}
 				if (javaFieldClass != null)
 					schemaFieldReflection.setType(Roma.schema().getSchemaClass(javaFieldClass));
 			}
 
 		}
 	}
 
 	protected void readAllAnnotations() {
 		FeatureLoader.loadClassFeatures(this, descriptor);
 		for (Aspect aspect : Roma.aspects()) {
 			aspect.configClass(this);
 		}
 	}
 
 	private static boolean checkIfFirstCharAfterPrefixIsUpperCase(String methodName, String prefix) {
 		return methodName.length() > prefix.length() ? Character.isUpperCase(methodName.charAt(prefix.length())) : false;
 	}
 
 	private void addEvent(String eventName, SchemaField field, Method eventMethod) {
 
 		SchemaEvent eventInfoBase = null;
 		if (field == null)
 			eventInfoBase = getEvent(eventName);
 		else
 			eventInfoBase = field.getEvent(eventName);
 
 		SchemaEventReflection eventInfo = null;
 		if (eventInfoBase instanceof SchemaEventReflection)
 			eventInfo = (SchemaEventReflection) eventInfoBase;
 
 		if (eventInfo == null) {
 			// EVENT NOT EXISTENT: CREATE IT AND INSERT IN THE COLLECTION
 			List<SchemaParameter> orderedParameters = new ArrayList<SchemaParameter>();
 			for (int i = 0; i < eventMethod.getParameterTypes().length; ++i) {
 				orderedParameters.add(new SchemaParameter("param" + i, Roma.schema().getSchemaClassIfExist(eventMethod.getParameterTypes()[i])));
 			}
 			if (field == null) {
 				eventInfo = new SchemaEventReflection(this, eventName, orderedParameters);
 				eventInfo.setMethod(eventMethod);
 				setEvent(eventName, eventInfo);
 			} else {
 				eventInfo = new SchemaEventReflection(field, eventName, orderedParameters);
 				eventInfo.setMethod(eventMethod);
 				field.setEvent(eventName, eventInfo);
 			}
 		}
 	}
 
 	protected boolean isToIgnoreMethod(Method currentMethod) {
 		if (Modifier.isStatic(currentMethod.getModifiers()) || !Modifier.isPublic(currentMethod.getModifiers()))
 			return true;
 		String methodName = currentMethod.getName();
 		// CHECK FOR FIXED NAMES TO IGNORE
 		for (int ignoreId = 0; ignoreId < IGNORE_METHOD_NAMES.length; ++ignoreId) {
 			if (ignoreMethod(IGNORE_METHOD_NAMES[ignoreId], methodName))
 				return true;
 		}
 
 		if (Roma.existComponent(SchemaManager.class))
 			// CHECK FOR CUSTOM NAMES TO IGNORE, IF ANY
 			for (Iterator<String> it = Roma.schema().getIgnoreActions().iterator(); it.hasNext();) {
 				if (ignoreMethod(it.next(), methodName))
 					return true;
 			}
 
 		return false;
 	}
 
 	private static boolean ignoreMethod(String iItem, String iMethodName) {
 		if (iItem.endsWith("^*")) {
 			String trunk = iItem.substring(0, iItem.length() - 2);
 			if (iMethodName.startsWith(trunk) && Character.isUpperCase(iMethodName.charAt(trunk.length())))
 				return true;
 		} else if (iItem.endsWith("*")) {
 			if (iMethodName.startsWith(iItem.substring(0, iItem.length() - 1)))
 				return true;
 		} else if (iItem.startsWith("*")) {
 			if (iMethodName.endsWith(iItem.substring(1)))
 				return true;
 		} else {
 			if (iMethodName.equals(iItem))
 				return true;
 		}
 
 		return false;
 	}
 
 	protected void beginConfig() {
 		for (Aspect aspect : Roma.aspects()) {
 			aspect.beginConfigClass(this);
 		}
 	}
 
 	protected void endConfig() {
 		for (Aspect aspect : Roma.aspects()) {
 			aspect.endConfigClass(this);
 		}
 	}
 
 	public boolean isInterface() {
 		return javaClass != null ? javaClass.isInterface() : false;
 	}
 
 	@Override
 	public boolean isPrimitive() {
 		return javaClass != null ? javaClass.isPrimitive() : false;
 	}
 
 	@Override
 	public boolean isEnum() {
 		return javaClass != null ? javaClass.isEnum() : false;
 	}
 
 	@Override
 	public boolean isAbstract() {
 		return javaClass != null ? Modifier.isAbstract(javaClass.getModifiers()) : false;
 	}
 
 	@Override
 	public String getFullName() {
 		return javaClass.getName();
 	}
 }
