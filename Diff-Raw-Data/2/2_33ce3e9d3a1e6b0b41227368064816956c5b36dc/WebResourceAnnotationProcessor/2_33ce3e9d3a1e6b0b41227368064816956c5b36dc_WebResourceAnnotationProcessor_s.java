 package com.github.t1.webresource;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Set;
 
 import javax.annotation.processing.*;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.tools.Diagnostic.Kind;
 
 /**
  * The annotation processor that generates the REST bindings
  */
 @SupportedSourceVersion(SourceVersion.RELEASE_6)
 @SupportedAnnotationClasses(WebResource.class)
 public class WebResourceAnnotationProcessor extends AbstractProcessor2 {
 
     private WebResourceGenerator generator;
 
     @Override
     public synchronized void init(ProcessingEnvironment env) {
         super.init(env);
         Messager messager = getMessager();
        this.generator = new WebResourceGenerator(messager, env.getFiler(), env.getElementUtils());
     }
 
     @Override
     public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
         for (Element webResource : roundEnv.getElementsAnnotatedWith(WebResource.class)) {
             try {
                 generator.process(webResource);
             } catch (Error e) {
                 getMessager().printMessage(Kind.ERROR, "can't process WebResource: " + toString(e), webResource);
                 throw e;
             } catch (RuntimeException e) {
                 getMessager().printMessage(Kind.ERROR, "can't process WebResource: " + toString(e), webResource);
             }
         }
         return false;
     }
 
     private String toString(Throwable e) {
         StringWriter writer = new StringWriter();
         e.printStackTrace(new PrintWriter(writer));
         return writer.toString();
     }
 }
