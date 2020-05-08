 package org.romaframework.core.schema;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.AnnotatedElement;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.core.annotation.AnnotationConstants;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.exception.ConfigurationException;
 import org.romaframework.core.schema.config.SchemaConfiguration;
 import org.romaframework.core.schema.reflection.SchemaActionReflection;
 import org.romaframework.core.schema.reflection.SchemaClassReflection;
 import org.romaframework.core.schema.reflection.SchemaEventReflection;
 import org.romaframework.core.schema.reflection.SchemaFieldReflection;
 import org.romaframework.core.schema.xmlannotations.XmlActionAnnotation;
 import org.romaframework.core.schema.xmlannotations.XmlAnnotation;
 import org.romaframework.core.schema.xmlannotations.XmlAspectAnnotation;
 import org.romaframework.core.schema.xmlannotations.XmlFieldAnnotation;
 import org.romaframework.core.schema.xmlannotations.XmlFormAreaAnnotation;
 
 @SuppressWarnings("rawtypes")
 public class FeatureLoader {
 
 	protected static Log									log								= LogFactory.getLog(FeatureLoader.class);
 
 	/**
 	 * Cache of annotation class.
 	 */
 	// TODO:Clear after loading complete possible memory leak.
 	private static Map<String, Class<?>>	annotationClasses	= new HashMap<String, Class<?>>();
 
 	/**
 	 * Load features for an SchemaFeature.
 	 * 
 	 * @param schemaFeatures
 	 *          to fill with features.
 	 * @param baseAnnotatedElement
 	 *          the annotated element that contains Annotation (example CLass, Method)
 	 * @param additionalAnnotatedElement
 	 *          an additional annotated element.
 	 * @param descriptor
 	 *          the Xml descriptor with configurations to load on features.
 	 */
 	@SuppressWarnings("unchecked")
	static public void loadFeatures(SchemaFeatures schemaFeatures, AnnotatedElement baseAnnotatedElement, AnnotatedElement additionalAnnotatedElement, XmlAnnotation descriptor) {
 		Map<String, Annotation> annotations = new HashMap<String, Annotation>();
 		Map<String, Annotation> additionalAnnotations = new HashMap<String, Annotation>();
 
 		List<Feature> features = FeatureRegistry.getFeatures(schemaFeatures.getFeatureType());
 		for (Feature feature : features) {
 
 			Object value = null;
 			if (descriptor != null) {
 				XmlAspectAnnotation aspectAnn = descriptor.aspect(feature.getAspectName());
 				if (aspectAnn != null) {
 					String xmlValue = aspectAnn.getAttribute(feature.getName());
 					value = readXmlValue(feature, xmlValue);
 				}
 			}
 			if (value == null && baseAnnotatedElement != null) {
 				value = getAnnotationValue(baseAnnotatedElement, annotations, feature);
 			}
 			if (value == null && additionalAnnotatedElement != null) {
 				value = getAnnotationValue(additionalAnnotatedElement, additionalAnnotations, feature);
 			}
 
 			if (value != null) {
 				schemaFeatures.setFeature(feature, value);
 			}
 		}
 	}
 
 	/**
 	 * Find the value of an Annotation correspondent to feature from an AnnotatedElement.
 	 * 
 	 * @param annotatedElement
 	 *          the annotated element where find.
 	 * @param alreadyFound
 	 *          the Cache Map of found Annotation instances values.
 	 * @param feature
 	 *          the feature to retrieve value.
 	 * @return the value of the feature in the annotation.
 	 */
 	@SuppressWarnings("unchecked")
 	private static Object getAnnotationValue(AnnotatedElement annotatedElement, Map<String, Annotation> alreadyFound, Feature feature) {
 
 		Annotation annInstance = alreadyFound.get(feature.getAspectName());
 		if (annInstance == null) {
 			Class ann = getAnnotationClass(feature.getType(), feature.getAspectName());
 			if (ann != null) {
 				annInstance = annotatedElement.getAnnotation(ann);
 				alreadyFound.put(feature.getAspectName(), annInstance);
 			}
 		}
 		if (annInstance != null) {
 			try {
 				Method mt = annInstance.getClass().getMethod(feature.getName());
 				return readAnnotationValue(feature, mt.invoke(annInstance));
 			} catch (Exception e) {
 				log.error("Problem on reading of declared feature:" + feature.getName() + " on Annotation:" + annInstance, e);
				throw new ConfigurationException("Problem on reading of declared feature:" + feature.getName() + " on Annotation:" + annInstance, e);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Convert a value of an xml annotation to an valid Feature value.
 	 * 
 	 * @param feature
 	 *          destination of value.
 	 * @param value
 	 *          the xml value.
 	 * @return the value converted.
 	 */
 	@SuppressWarnings("unchecked")
 	private static Object readXmlValue(Feature feature, String value) {
 		if (value == null)
 			return null;
 		Class valueType = feature.getValueType();
 		if (Boolean.class.equals(valueType)) {
 			return "true".equalsIgnoreCase((String) value) || "1".equals(value) || "t".equalsIgnoreCase((String) value);
 		}
 		if (SchemaClass.class.equals(valueType)) {
 			if (value.equals(FeatureNotSet.class.getSimpleName())) {
 				return null;
 			}
 			return Roma.schema().getSchemaClass(value);
 		}
 		if (Integer.class.equals(valueType)) {
 			return Integer.parseInt(value);
 		}
 		if (Long.class.equals(valueType)) {
 			return Long.parseLong(value);
 		}
 		if (Float.class.equals(valueType)) {
 			return Float.parseFloat(value);
 		}
 		if (Double.class.equals(valueType)) {
 			return Double.parseDouble(value);
 		}
 		if (Short.class.equals(valueType)) {
 			return Short.parseShort(value);
 		}
 		if (String[].class.equals(valueType)) {
 			return value.split(" |,");
 		}
 		if (Class[].class.equals(valueType)) {
 			String[] classNames = value.split(" |,");
 			if (classNames != null) {
 				Class<?>[] classes = new Class[classNames.length];
 				int i = 0;
 				for (String exception : classNames) {
 					try {
 						classes[i] = Class.forName(exception.trim());
 					} catch (Exception e) {
 						if (log.isErrorEnabled()) {
 							log.debug(" Error on loading feature " + feature.getAspectName() + " cannot initialize class " + exception, e);
 						}
 					}
 					i++;
 				}
 				return classes;
 			}
 		}
 		if (String.class.equals(valueType)) {
 			return value;
 		}
 		if (Enum.class.isAssignableFrom(valueType)) {
 			return Enum.valueOf(valueType, value);
 		}
 		return null;
 	}
 
 	/**
 	 * Convert the annotation value to feature expected value.
 	 * 
 	 * @param feature
 	 *          feature destination.
 	 * @param value
 	 *          the current value.
 	 * @return the converted value.
 	 */
 	private static Object readAnnotationValue(Feature feature, Object value) {
 		if (value == null)
 			return null;
 		Class<?> valueType = feature.getValueType();
 		if (Boolean.class.equals(valueType)) {
 			if (value instanceof Boolean)
 				return value;
 			if (value instanceof AnnotationConstants) {
 				return ((AnnotationConstants) value).getValue();
 			}
 			if (value instanceof Byte) {
 				if (1 == (Byte) value)
 					return true;
 				else if (0 == (Byte) value)
 					return false;
 				return null;
 			}
 			if (value instanceof String)
 				return "true".equalsIgnoreCase((String) value) || "1".equals(value) || "t".equalsIgnoreCase((String) value);
 		}
		if (Class.class.equals(valueType)) {
			if (value instanceof Class) {
				if (!value.equals(FeatureNotSet.class)) {
					return value;
				}
				return null;
			}
		}
 		if (SchemaClass.class.equals(valueType)) {
 			if (value instanceof String) {
 				if (value.equals(AnnotationConstants.DEF_VALUE)) {
 					return null;
 				}
 				return Roma.schema().getSchemaClass((String) value);
 			}
 			if (value != null && value.equals(FeatureNotSet.class)) {
 				return null;
 			}
 			return Roma.schema().getSchemaClass(value);
 		}
 
 		if (String.class.equals(valueType)) {
 			if (AnnotationConstants.DEF_VALUE.equals(value))
 				return null;
 			return value.toString();
 		}
 
 		if (String[].class.equals(valueType)) {
 			if (value instanceof String[]) {
 				String arrayValue[] = (String[]) value;
 				if (arrayValue.length == 1) {
 					if (!AnnotationConstants.DEF_VALUE.equals(arrayValue[0]))
 						return arrayValue[0].split(" |,");
 					return null;
 				}
 				return arrayValue;
 			}
 			if (value instanceof String && !AnnotationConstants.DEF_VALUE.equals(value))
 				return ((String) value).split(" |,");
 			return null;
 		}
 
 		if (Integer.class.equals(valueType)) {
 			if (value instanceof Number)
 				return ((Number) value).intValue();
 			if (value instanceof String)
 				return Integer.parseInt((String) value);
 			return null;
 		}
 		if (Long.class.equals(valueType)) {
 			if (value instanceof Number)
 				return ((Number) value).longValue();
 			if (value instanceof String)
 				return Long.parseLong((String) value);
 			return null;
 		}
 		if (Float.class.equals(valueType)) {
 			if (value instanceof Number)
 				return ((Number) value).floatValue();
 			if (value instanceof String)
 				return Float.parseFloat((String) value);
 			return null;
 		}
 		if (Double.class.equals(valueType)) {
 			if (value instanceof Number)
 				return ((Number) value).doubleValue();
 			if (value instanceof String)
 				return Double.parseDouble((String) value);
 			return null;
 		}
 		if (Short.class.equals(valueType)) {
 			if (value instanceof Number)
 				return ((Number) value).shortValue();
 			if (value instanceof String)
 				return Short.parseShort((String) value);
 			return null;
 		}
 
 		if (XmlFormAreaAnnotation.class.equals(valueType)) {
 			// TODO: Load Xml object From String annotation.
 			return null;
 		}
 
 		return value;
 	}
 
 	/**
 	 * Resolve the Class of annotation correspondent the the feature Type/aspect
 	 * 
 	 * @param featureType
 	 *          the t type of annotation to find
 	 * @param aspectName
 	 *          the aspect .
 	 * @return the Annotation class correspondent to feature Type/aspect (example "View"+"Field")
 	 */
 	static public Class<?> getAnnotationClass(FeatureType featureType, String aspectName) {
 		String name = Character.toUpperCase(aspectName.charAt(0)) + aspectName.substring(1) + featureType.getBaseName();
 		Class<?> cl = annotationClasses.get(name);
 		if (cl == null) {
 			try {
 				cl = Class.forName(Utility.ROMA_PACKAGE + ".aspect." + aspectName + ".annotation." + name);
 				annotationClasses.put(name, cl);
 			} catch (ClassNotFoundException e) {
 				log.debug("Not found expected annotation at:" + Utility.ROMA_PACKAGE + ".aspect." + aspectName + ".annotation." + name, e);
 			}
 		}
 		return cl;
 	}
 
 	/**
 	 * Load feature of field.
 	 * 
 	 * @param schemaField
 	 *          to fill with features.
 	 * @param parentDescriptor
 	 *          the xml descriptor for field.
 	 */
 	public static void loadFieldFeatures(SchemaField schemaField, XmlFieldAnnotation parentDescriptor) {
 		AnnotatedElement baseAnnotatedElement = null;
 		AnnotatedElement additionalAnnotatedElement = null;
 		if (schemaField instanceof SchemaFieldReflection) {
 			baseAnnotatedElement = ((SchemaFieldReflection) schemaField).getGetterMethod();
 			additionalAnnotatedElement = ((SchemaFieldReflection) schemaField).getField();
 		}
 		loadFeatures(schemaField, baseAnnotatedElement, additionalAnnotatedElement, parentDescriptor);
 	}
 
 	/**
 	 * Load feature for Action,
 	 * 
 	 * @param schemaAction
 	 *          the action to fill with features.
 	 * @param parentDescriptor
 	 *          the descriptor with xml fatures.
 	 */
 	public static void loadActionFeatures(SchemaAction schemaAction, XmlActionAnnotation parentDescriptor) {
 		AnnotatedElement baseAnnotatedElement = null;
 		if (schemaAction instanceof SchemaActionReflection) {
 			baseAnnotatedElement = ((SchemaActionReflection) schemaAction).getMethod();
 		}
 		loadFeatures(schemaAction, baseAnnotatedElement, null, parentDescriptor);
 	}
 
 	/**
 	 * Load Event features.
 	 * 
 	 * @param schemaEvent
 	 *          to fill with features.
 	 * @param parentDescriptor
 	 *          the xml descriptor with xml event features.
 	 */
 	public static void loadEventFeatures(SchemaEvent schemaEvent, XmlActionAnnotation parentDescriptor) {
 		AnnotatedElement baseAnnotatedElement = null;
 		if (schemaEvent instanceof SchemaEventReflection) {
 			baseAnnotatedElement = ((SchemaEventReflection) schemaEvent).getMethod();
 		}
 		loadFeatures(schemaEvent, baseAnnotatedElement, null, parentDescriptor);
 	}
 
 	/**
 	 * Load Class Features.
 	 * 
 	 * @param clazz
 	 *          the SchemaClass to fill with features.
 	 * @param descriptor
 	 *          the xml descriptor with additional feature.
 	 */
 	static public void loadClassFeatures(SchemaClass clazz, SchemaConfiguration descriptor) {
 		AnnotatedElement element = null;
 		if (clazz instanceof SchemaClassReflection) {
 			element = ((SchemaClassReflection) clazz).getLanguageType();
 		}
 
 		XmlAnnotation ann = null;
 		if (descriptor != null)
 			ann = descriptor.getType();
 		loadFeatures(clazz, element, null, ann);
 	}
 }
