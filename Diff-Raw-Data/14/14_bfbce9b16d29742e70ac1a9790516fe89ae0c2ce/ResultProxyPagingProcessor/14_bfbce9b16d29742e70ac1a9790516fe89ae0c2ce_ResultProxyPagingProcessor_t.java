 package com.rhem.processors;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URL;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.tools.JavaFileObject;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 
 import com.google.web.bindery.requestfactory.shared.ProxyFor;
 
 @SupportedAnnotationTypes("com.google.web.bindery.requestfactory.shared.ProxyFor")
 @SupportedSourceVersion(SourceVersion.RELEASE_6)
 public class ResultProxyPagingProcessor extends AbstractProcessor {
 
 	@Override
 	public boolean process(Set<? extends TypeElement> annotations,
 			RoundEnvironment roundEnv) {
		
 		Filer filer = processingEnv.getFiler();
 		
 		String fqClassName = null;
         String className = null;
         String packageName = null;
         String packageResultProxy = null;
         String modelName = null;
         
 		for (Element e : roundEnv.getElementsAnnotatedWith(ProxyFor.class)) {
 			
 			if (e.getKind() == ElementKind.INTERFACE) {
                 TypeElement classElement = (TypeElement) e;
                 PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
                 packageName = packageElement.getQualifiedName().toString();
                 
                 if(!packageName.matches(".*shared.proxy")){
                	continue;
                 }
                 
                 fqClassName = classElement.getQualifiedName().toString();
                 className = classElement.getSimpleName().toString();
                 
                 modelName = className.replaceFirst("Proxy", "");
                 
                 packageResultProxy = packageName.replaceFirst("proxy", "resultproxy");
                 
                 
                 if (fqClassName != null) {
 
                     Properties props = new Properties();
                     URL url = this.getClass().getClassLoader().getResource("velocity.properties");
                     try {
         				props.load(url.openStream());
         			} catch (IOException e1) {
         				e1.printStackTrace();
         			}
 
                     VelocityEngine ve = new VelocityEngine(props);
                     ve.init();
 
                     VelocityContext vc = new VelocityContext();
 
                     vc.put("className", className);
                     vc.put("packageResultProxy", packageResultProxy);
                     vc.put("fqClassName", fqClassName);
                     vc.put("modelName", modelName);
 
                     Template vt = ve.getTemplate("resultproxypaging.vm");
                     
                     JavaFileObject jfo = null;
         			try {
         				jfo = filer.createSourceFile(packageResultProxy+"." + modelName+"PagingLoadResultProxy");
         			} catch (IOException e1) {
         				e1.printStackTrace();
         			}
 
                     Writer writer = null;
         			try {
         				writer = jfo.openWriter();
         			} catch (IOException e1) {
         				e1.printStackTrace();
         			}
 
                     vt.merge(vc, writer);
 
                     try {
         				writer.close();
         			} catch (IOException e1) {
         				e1.printStackTrace();
         			}
                     
                 }
 
             }
 		}
 		return false;
 	}
 
 }
