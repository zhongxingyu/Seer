 package com.mtgi.analytics.aop.config.v10;
 
 import org.springframework.core.Conventions;
 
 public class BtNamespaceUtils {
 	public static final String TRACKING_MANAGER_ATTRIBUTE = "tracking-manager";
 
 	public static final String TRACKING_MANAGER_PROPERTY =
 			Conventions.attributeNameToPropertyName(TRACKING_MANAGER_ATTRIBUTE);
 
 	public static final String TRACKING_ATTRIBUTE_SOURCE = "trackingAttributeSource";
 
 	public static final String TRACKING_MANAGER_APPLICATION_ATTRIBUTE = "application";
 	
 	public static final String TRACKING_MANAGER_APPLICATION_PROPERTY =
 		Conventions.attributeNameToPropertyName(TRACKING_MANAGER_APPLICATION_ATTRIBUTE);
 
 //	private static final String ANNOTATION_TRACKING_ATTRIBUTE_SOURCE_CLASS_NAME =
 //			"org.springframework.transaction.annotation.AnnotationTransactionAttributeSource";
 //
 //
 //	public static Class getAnnotationTransactionAttributeSourceClass() {
 //		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_15) {
 //			throw new IllegalStateException(
 //					"AnnotationTransactionAttributeSource is only available on Java 1.5 and higher");
 //		}
 //		try {
 //			return ClassUtils.forName(
 //					ANNOTATION_TRACKING_ATTRIBUTE_SOURCE_CLASS_NAME, BtNameSpaceUtils.class.getClassLoader());
 //		}
 //		catch (Throwable ex) {
 //			throw new IllegalStateException("Unable to load Java 1.5 dependent class [" +
 //					ANNOTATION_TRACKING_ATTRIBUTE_SOURCE_CLASS_NAME + "]", ex);
 //		}
 //	}
 }
