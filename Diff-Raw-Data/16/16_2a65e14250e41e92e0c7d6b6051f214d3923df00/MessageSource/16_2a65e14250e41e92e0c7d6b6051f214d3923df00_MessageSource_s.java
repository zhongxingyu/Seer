 package org.dejava.component.i18n.source.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import org.dejava.component.i18n.source.MessageSourceCreator;
 import org.dejava.component.i18n.source.processor.MessageSourceEntryProcessor;
 
 /**
  * Defines a source of messages (will be processed with the {@link MessageSourceCreator} annotation
  * processor).
  */
 @Retention(value = RetentionPolicy.RUNTIME)
 @Target(value = ElementType.ANNOTATION_TYPE)
 public @interface MessageSource {
 
 	/**
 	 * The source path where the source files should be created.
 	 * 
	 * "src" is used by default.
 	 */
 	String sourcePath() default "src/main/resources";
 
 	/**
 	 * Base name of the bundle that the class provides messages to.
 	 */
 	String bundleBaseName();
 
 	/**
 	 * Description for the message source file.
 	 */
 	String description() default "";
 
 	/**
 	 * Available locales that this source must provide messages to.
 	 * 
 	 * By default, "en_US" and "pt_BR" are used.
 	 */
 	String[] availableLocales() default { "en_US", "pt_BR" };
 
 	/**
 	 * Full qualified names of the processors classes (that must implement the
 	 * {@link MessageSourceEntryProcessor} interface).
 	 * 
 	 * The initial idea was to return an array of {@link MessageSourceEntryProcessor} classes. But when
 	 * resolving them at the annotation processor, a MirroredTypesException was raised. The work around was to
 	 * set the classes qualified names instead and resolve them via reflection later (at the annotation
 	 * processor).
 	 */
 	String[] processors();
 
 	/**
 	 * If the super classes of the current class should also be processed. Default is false.
 	 */
 	boolean processSuperclasses() default false;
 }
