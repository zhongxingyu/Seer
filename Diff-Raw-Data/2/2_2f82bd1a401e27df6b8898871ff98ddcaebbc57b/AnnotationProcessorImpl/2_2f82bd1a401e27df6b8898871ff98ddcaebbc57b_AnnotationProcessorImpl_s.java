 package org.kohsuke.metainf_services;
 
 import org.kohsuke.MetaInfServices;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.RoundEnvironment;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.MirroredTypeException;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import javax.tools.Diagnostic.Kind;
 import javax.tools.FileObject;
 import javax.tools.StandardLocation;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 @SuppressWarnings({"Since15"})
 public class AnnotationProcessorImpl extends AbstractProcessor {
     @Override
     public Set<String> getSupportedAnnotationTypes() {
         return Collections.singleton(MetaInfServices.class.getName());
     }
 
     @Override
     public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
         if (roundEnv.processingOver())      return false;
 
         Map<String,Set<String>> services = new HashMap<String, Set<String>>();
 
         // discover services from the current compilation sources
         for (TypeElement type : (Collection<TypeElement>)roundEnv.getElementsAnnotatedWith(MetaInfServices.class)) {
             TypeElement contract = getContract(type);
             if(contract==null)  continue; // error should have already been reported
 
             String cn = contract.getQualifiedName().toString();
             Set<String> v = services.get(cn);
             if(v==null)
                 services.put(cn,v=new TreeSet<String>());
             v.add(type.getQualifiedName().toString());
         }
 
         // also load up any existing values, since this compilation may be partial
         Filer filer = processingEnv.getFiler();
         for (Map.Entry<String,Set<String>> e : services.entrySet()) {
             try {
                 String contract = e.getKey();
                 FileObject f = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" +contract);
                 BufferedReader r = new BufferedReader(new InputStreamReader(f.openInputStream(), "UTF-8"));
                 String line;
                 while((line=r.readLine())!=null)
                     e.getValue().add(line);
                 r.close();
             } catch (FileNotFoundException x) {
                 // doesn't exist
             } catch (IOException x) {
                 processingEnv.getMessager().printMessage(Kind.ERROR,"Failed to load existing service definition files: "+x);
             }
         }
 
         // now write them back out
         for (Map.Entry<String,Set<String>> e : services.entrySet()) {
             try {
                 String contract = e.getKey();
                 processingEnv.getMessager().printMessage(Kind.NOTE,"Writing META-INF/services/"+contract);
                 FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" +contract);
                 PrintWriter pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), "UTF-8"));
                 for (String value : e.getValue())
                     pw.println(value);
                 pw.close();
             } catch (IOException x) {
                 processingEnv.getMessager().printMessage(Kind.ERROR,"Failed to write service definition files: "+x);
             }
         }
 
         return false;
     }
 
     private TypeElement getContract(TypeElement type) {
         // explicitly specified?
         try {
             MetaInfServices a = type.getAnnotation(MetaInfServices.class);
             a.value();
             throw new AssertionError();
         } catch (MirroredTypeException e) {
             TypeMirror m = e.getTypeMirror();
             if (m.getKind()== TypeKind.VOID) {
                 // contract inferred from the signature
                boolean hasBaseClass = type.getSuperclass().getKind() == TypeKind.NONE;
                 boolean hasInterfaces = !type.getInterfaces().isEmpty();
                 if(hasBaseClass^hasInterfaces) {
                     if(hasBaseClass)
                         return (TypeElement)((DeclaredType)type.getSuperclass()).asElement();
                     return (TypeElement)((DeclaredType)type.getInterfaces().get(0)).asElement();
                 }
 
                 error(type, "Contract type was not specified, but it couldn't be inferred.");
                 return null;
             }
 
             if (m instanceof DeclaredType) {
                 DeclaredType dt = (DeclaredType) m;
                 return (TypeElement)dt.asElement();
             } else {
                 error(type, "Invalid type specified as the contract");
                 return null;
             }
         }
 
 
     }
 
     private void error(Element source, String msg) {
         processingEnv.getMessager().printMessage(Kind.ERROR,msg,source);
     }
 }
