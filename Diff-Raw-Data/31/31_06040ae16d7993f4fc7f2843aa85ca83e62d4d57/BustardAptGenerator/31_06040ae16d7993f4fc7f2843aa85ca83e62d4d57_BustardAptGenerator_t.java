 package ru.finam.bustard.codegen;
 
 import com.google.common.base.Supplier;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multimaps;
 
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.TypeMirror;
 import javax.tools.FileObject;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardLocation;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.*;
 
 public class BustardAptGenerator {
 
     private Multimap<TypeElement, ExecutableElement> events = Multimaps.newMultimap(
             new HashMap<TypeElement, Collection<ExecutableElement>>(),
             new Supplier<Collection<ExecutableElement>>() {
                 @Override
                 public Collection<ExecutableElement> get() {
                     return new HashSet<ExecutableElement>();
                 }
             });
 
     public static TypeElement mirrorToElement(TypeMirror typeMirror) {
         return (TypeElement) ((DeclaredType) typeMirror).asElement();
     }
 
     public void addListener(ExecutableElement listenerMethod) {
         List<? extends VariableElement> parameters = listenerMethod.getParameters();
 
         if (parameters.size() != 1) {
             throw new AssertionError("Listener method " + listenerMethod.toString() +
                     " must have only one argument.");
         }
 
         TypeElement eventType = mirrorToElement(parameters.get(0).asType());
         events.put(eventType, listenerMethod);
     }
 
     public void generate(ProcessingEnvironment environment) throws IOException {
         Set<Element> origin = new HashSet<Element>();
         BustardEmitter bustardEmitter = new BustardEmitter("BustardImpl");
         StringBuilder subscribersInfo = new StringBuilder();
 
         for (TypeElement eventType : events.keySet()) {
             for (ExecutableElement listenerMethod : events.get(eventType)) {
                 TypeElement subscriberType = (TypeElement) listenerMethod.getEnclosingElement();
                 origin.add(subscriberType);
 
                 String subscriberName = subscriberType.getQualifiedName().toString();
                 String eventName = eventType.getQualifiedName().toString();
                 String methodName = listenerMethod.getSimpleName().toString();
 
                 bustardEmitter.addSubscriber(eventName, subscriberName, methodName);
 
                 subscribersInfo.append(String.format("%s %s %s\n",
                         subscriberName, methodName, eventName));
             }
             origin.add(eventType);
         }
 
         FileObject subscribersFileObject = environment.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT,
                 "ru.finam.bustard", "subscribers.bustard",
                 origin.toArray(new Element[origin.size()]));
 
         Writer subscribersWriter = subscribersFileObject.openWriter();
 
         try {
             subscribersWriter.write(subscribersInfo.toString());
         } finally {
             subscribersWriter.close();
         }
 
         JavaFileObject bustardFileObject = environment.getFiler().createSourceFile(
                 "ru.finam.bustard.BustardImpl",
                 origin.toArray(new Element[origin.size()]));
 
         Writer bustardFileWriter = bustardFileObject.openWriter();
 
         try {
             bustardEmitter.emit(bustardFileWriter);
         } finally {
             bustardFileWriter.close();
         }
     }
 }
