 /*
  * Created on 09-03-2013 13:04:57 by Andrzej Ludwikowski
  */
 
 package info.ludwikowski.processor;
 
 import static info.ludwikowski.util.StringUtils.hasText;
 import static java.lang.Boolean.parseBoolean;
 import info.ludwikowski.common.Context;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.lang.model.util.Elements;
 import javax.lang.model.util.Types;
 import javax.tools.Diagnostic.Kind;
 
 
 public class ProcessorContext extends Context {
 
 	public static final String METHOD_PREFIX = "methodPrefix";
 	public static final String BUILD_METHOD_NAME = "buildMethodName";
 	public static final String BUILDER_CLASS_POSTFIX = "builderClassPostfix";
 	public static final String BUILDER_CLASS_PREFIX = "builderClassPrefix";
 	public static final String STATIC_CREATE_METHOD_NAME = "staticCreateMethodName";
 	public static final String GENERATE_STATIC_CREATE_METHOD = "generateStaticCreateMethod";
 	public static final String GENERATE_VARARGS_FOR_COLLECTIONS = "generateVarargsForCollections";
 	public static final String ACCEPT_JAVA_PERSISTENCE_ANNOTATIONS = "acceptJavaPersisentceAnnotations";
 	public static final String IGNORED_CLASS_PREFIX = "ignoredClassPrefix";
 	public static final String USE_INDEFINITE_ARTICLES = "useIndefiniteArticles";
 	public static final String DEBUG = "debug";
 
 	public static final String JAVAX_PERSISTENCE_ENTITY = "javax.persistence.Entity";
 	public static final String JAVAX_PERSISTENCE_MAPPEDSUPERCLASS = "javax.persistence.MappedSuperclass";
 	public static final String JAVAX_PERSISTENCE_EMBEDDABLE = "javax.persistence.Embeddable";
	public static final String FLUENT_BUILDER_ANNOTATATION = "info.ludwikowski.annotation.GenerateBuilder";
 
 	public static final List<String> JPA_ANNOTATIONS;
 
 	static {
 		List<String> jpaAnnotations = new ArrayList<String>(3);
 		jpaAnnotations.add(JAVAX_PERSISTENCE_ENTITY);
 		jpaAnnotations.add(JAVAX_PERSISTENCE_MAPPEDSUPERCLASS);
 		jpaAnnotations.add(JAVAX_PERSISTENCE_EMBEDDABLE);
 		JPA_ANNOTATIONS = Collections.unmodifiableList(jpaAnnotations);
 	}
 
 	private boolean debug = false;
 	private boolean acceptJavaPersisentceAnnotations = false;
 	private final ProcessingEnvironment processingEnv;
 
 
 	public ProcessorContext(ProcessingEnvironment processingEnv) {
 		Map<String, String> options = processingEnv.getOptions();
 		this.processingEnv = processingEnv;
 		this.debug = getBoolean(options, DEBUG, false);
 		this.acceptJavaPersisentceAnnotations = getBoolean(options, ACCEPT_JAVA_PERSISTENCE_ANNOTATIONS, false);
 		setAbstractBuilderClassPrefix(options.get(BUILDER_CLASS_PREFIX));
 		setBuilderClassPostfix(options.get(BUILDER_CLASS_POSTFIX));
 		setBuildMethodName(options.get(BUILD_METHOD_NAME));
 		setMethodPrefix(options.get(METHOD_PREFIX));
 		setStaticCreate(getBoolean(options, GENERATE_STATIC_CREATE_METHOD, true));
 		setStaticCreateMethodName(options.get(STATIC_CREATE_METHOD_NAME));
 		setVarargsForCollections(getBoolean(options, GENERATE_VARARGS_FOR_COLLECTIONS, true));
 		setIgnoredClassPrefix(options.get(IGNORED_CLASS_PREFIX));
 		setUseIndefiniteArticles(getBoolean(options, USE_INDEFINITE_ARTICLES, true));
 	}
 
 	private boolean getBoolean(Map<String, String> options, String key, boolean defaultValue) {
 
 		String option = options.get(key);
 
 		if (hasText(option)) {
 			return parseBoolean(option);
 		}
 
 		return defaultValue;
 	}
 
 	public Elements getElementUtils() {
 		return processingEnv.getElementUtils();
 	}
 
 	public Types getTypeUtils() {
 		return processingEnv.getTypeUtils();
 	}
 
 	public ProcessingEnvironment getProcessingEnvironment() {
 		return processingEnv;
 	}
 
 	public void logInfo(String message) {
 		log(Kind.NOTE, message);
 	}
 
 	private void log(Kind kind, String message) {
 		if (debug) {
 			processingEnv.getMessager().printMessage(kind, message);
 		}
 	}
 
 	public void logError(String message) {
 		log(Kind.ERROR, message);
 	}
 
 	public void logConfiguration() {
 		processingEnv.getMessager().printMessage(Kind.NOTE, toString());
 	}
 
 	public boolean isAcceptJavaPersisentceAnnotations() {
 		return acceptJavaPersisentceAnnotations;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("ProcessorContext [debug=");
 		builder.append(debug);
 		builder.append(", acceptJavaPersisentceAnnotations=");
 		builder.append(acceptJavaPersisentceAnnotations);
 		builder.append(", processingEnv=");
 		builder.append(processingEnv);
 		builder.append(", toString()=");
 		builder.append(super.toString());
 		builder.append("]");
 		return builder.toString();
 	}
 }
