 /*
  * Created on 09-03-2013 12:51:40 by Andrzej Ludwikowski
  */
 
 package info.ludwikowski.processor;
 
 import static info.ludwikowski.processor.ProcessorContext.FLUENT_BUILDER_ANNOTATATION;
 import static info.ludwikowski.processor.ProcessorContext.JAVAX_PERSISTENCE_EMBEDDABLE;
 import static info.ludwikowski.processor.ProcessorContext.JAVAX_PERSISTENCE_ENTITY;
 import static info.ludwikowski.processor.ProcessorContext.JAVAX_PERSISTENCE_MAPPEDSUPERCLASS;
 import info.ludwikowski.model.ClassMirror;
 
 import java.util.Collection;
 import java.util.Set;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 
 
 @SupportedAnnotationTypes({ JAVAX_PERSISTENCE_ENTITY,
 		JAVAX_PERSISTENCE_MAPPEDSUPERCLASS, JAVAX_PERSISTENCE_EMBEDDABLE,
 		FLUENT_BUILDER_ANNOTATATION })
 @SupportedSourceVersion(SourceVersion.RELEASE_6)
 public class FluentBuilderProcessor extends AbstractProcessor {
 
 	private ProcessorContext context;
 	private ClassMirrorProvider classMirrorProvider;
 	private ClassWriter classWriter;
 
 
 	@Override
 	public synchronized void init(ProcessingEnvironment processingEnv) {
 		super.init(processingEnv);
 		context = new ProcessorContext(processingEnv);
 		ClassVerifier classVerifier = new ClassVerifier(context);
 		classMirrorProvider = new ClassMirrorProvider(classVerifier, context);
 		classWriter = new ClassWriter(context);
 		context.logConfiguration();
 	}
 
 	@Override
 	public boolean process(Set<? extends TypeElement> annotations,
 			RoundEnvironment roundEnv) {
 
 		Set<? extends Element> elements = roundEnv.getRootElements();
 
 		Collection<ClassMirror> classMirrors = classMirrorProvider.prepareMirrors(elements);
 
 		classWriter.write(classMirrors);
 
 		return false;
 	}
 
 }
