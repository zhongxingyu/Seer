 package org.javakontor.ds.apt;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.TypeMirror;
 import javax.tools.Diagnostic;
 
 import org.javakontor.ds.apt.base.AnnotationInfos;
 import org.javakontor.ds.apt.base.BaseAnnotationScanner;
 import org.javakontor.ds.apt.base.ElementInfos;
 import org.osgi.service.component.annotations.Activate;
 import org.osgi.service.component.annotations.Component;
 import org.osgi.service.component.annotations.ConfigurationPolicy;
 import org.osgi.service.component.annotations.Deactivate;
 import org.osgi.service.component.annotations.Reference;
 
 public class OsgiAnnotationScanner extends BaseAnnotationScanner {
 	public OsgiAnnotationScanner(ProcessingEnvironment processingEnv) {
 		super(processingEnv);
 	}
 
 	@Override
 	public Void visitType(TypeElement e, Void p) {
 		dsXmlExporter.addImplementation(e.toString());
 
 		Component componentAnnotation = e.getAnnotation(Component.class);
 
 		if (componentAnnotation != null) {
 			ElementInfos elementInfos = new ElementInfos(e);
 			AnnotationInfos annotationInfos = elementInfos.getAnnotationInfos(Component.class);
			String[] provideValues = annotationInfos.getArrayAsStrings("service");
 
 			dsXmlExporter.setClassName(e.toString());
 			String name = componentAnnotation.name();
 			if (name.length() > 0) {
 				dsXmlExporter.setComponentAttribute("name", name);
 			} else {
 				dsXmlExporter.setComponentAttribute("name", e.toString());
 			}
 
 			List<String> interfaces = new ArrayList<String>();
 			if (provideValues.length == 0) {
				
				// If no service should be registered, the empty value {} must be specified.
				// If not specified, the service types for this Component are all the directly implemented interfaces of the class
				// being annotated.
				if (annotationInfos.getStringValue("service") == null) {
					for (TypeMirror mirror : e.getInterfaces()) {
						interfaces.add(mirror.toString());
					}
 				}
 			} else {
 				for (String provideInterface : provideValues) {
 					boolean foundInterface = false;
 					for (TypeMirror mirror : e.getInterfaces()) {
 						if (provideInterface.equals(mirror.toString())) {
 							interfaces.add(provideInterface);
 							foundInterface = true;
 						}
 					}
 					if (!foundInterface) {
 						processingEnv.getMessager().printMessage(
 							Diagnostic.Kind.ERROR,
 							"Class does not implement declared interface \"" + provideInterface
 							+ "\"",
 							e
 						);
 						errorsFound = true;
 					}
 				}
 			}
 			if (interfaces.size() > 0) {
 				dsXmlExporter.addServices(interfaces);
 			}
 			dsXmlExporter.setComponentAttribute("factory", componentAnnotation.factory());
 			dsXmlExporter.setComponentAttribute("activate", getActivateMethod(e, Activate.class));
 			dsXmlExporter.setComponentAttribute(
 				"deactivate",
 				getDeactivateMethod(e, Deactivate.class)
 			);
 
 			Boolean immediate = componentAnnotation.immediate();
 			if (immediate) {
 				dsXmlExporter.setComponentAttribute("immediate", "true");
 			}
 			ConfigurationPolicy configurationPolicy = componentAnnotation.configurationPolicy();
 			if (configurationPolicy != ConfigurationPolicy.OPTIONAL) {
 				dsXmlExporter.setComponentAttribute(
 					"configuration-policy",
 					configurationPolicy.value()
 				);
 			}
 
 			for (String property : componentAnnotation.properties()) {
 				dsXmlExporter.addProperty(property);
 			}
 
 		}
 		super.visitType(e, p);
 		return null;
 	}
 
 	@Override
 	public Void visitExecutable(ExecutableElement e, Void p) {
 		if (e.getKind() == ElementKind.METHOD) {
 			Reference referenceAnnotation = e.getAnnotation(Reference.class);
 
 			if (referenceAnnotation != null) {
 				String serviceInterface = e.getParameters().get(0).asType().toString();
 
 				ElementInfos elementInfos = new ElementInfos(e);
 				AnnotationInfos annotationInfos = elementInfos.getAnnotationInfos(Reference.class);
 				String serviceName = annotationInfos.getStringValue("service");
 				if (serviceName != null) {
 					System.out.println("Reference service: " + serviceName);
 					serviceInterface = serviceName;
 				}
 
 				String cardinality = referenceAnnotation.cardinality().value();
 				String policy = referenceAnnotation.policy().value();
 				Element unbindMethod = getUnbindMethod(e, referenceAnnotation.unbind());
 				dsXmlExporter.addReference(
 					serviceInterface,
 					policy,
 					cardinality,
 					referenceAnnotation.target(),
 					referenceAnnotation.name(),
 					e.getSimpleName().toString(),
 					unbindMethod != null ? unbindMethod.getSimpleName().toString() : null
 				);
 
 			}
 		}
 		return null;
 	}
 }
