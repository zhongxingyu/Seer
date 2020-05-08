 package org.javakontor.ds.apt;
 
 import aQute.bnd.annotation.component.Activate;
 import aQute.bnd.annotation.component.Component;
 import aQute.bnd.annotation.component.ConfigurationPolicy;
 import aQute.bnd.annotation.component.Deactivate;
 import aQute.bnd.annotation.component.Reference;
 
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
 
 public class BndAnnotationScanner extends BaseAnnotationScanner {
 	public BndAnnotationScanner(ProcessingEnvironment processingEnv) {
 		super(processingEnv);
 	}
 
 	@Override
 	public Void visitType(TypeElement e, Void p) {
 
 		Component componentAnnotation = e.getAnnotation(Component.class);
 
 		if (componentAnnotation != null) {
 			dsXmlExporter.addImplementation(e.toString());
 			ElementInfos elementInfos = new ElementInfos(e);
 			AnnotationInfos annotationInfos = elementInfos.getAnnotationInfos(Component.class);
 			String[] provideValues = annotationInfos.getArrayAsStrings("provide");
 
 			dsXmlExporter.setClassName(e.toString());
 			String componentPid;
 			if (componentAnnotation.name().length() > 0) {
 				componentPid = componentAnnotation.name();
 				dsXmlExporter.setComponentAttribute("name", componentAnnotation.name());
 			} else {
 				componentPid = e.toString();
 				dsXmlExporter.setComponentAttribute("name", e.toString());
 			}
 
 			String designateClassName = annotationInfos.getStringValue("designate");
 			if (designateClassName != null) {
 				new MetatypeExporter(e, componentPid, designateClassName, processingEnv);
 			}
 			List<String> interfaces = new ArrayList<String>();
 			if (provideValues.length == 0) {
 				for (TypeMirror mirror : e.getInterfaces()) {
 					interfaces.add(mirror.toString());
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
 			if (configurationPolicy != ConfigurationPolicy.optional) {
 				dsXmlExporter.setComponentAttribute(
 					"configuration-policy",
 					configurationPolicy.toString()
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
 				boolean dynamicReference = referenceAnnotation.dynamic();
 				boolean optionalReference = referenceAnnotation.optional();
 				boolean multipleReference = referenceAnnotation.multiple();
 				char type = referenceAnnotation.type();
 
 				ElementInfos elementInfos = new ElementInfos(e);
 				AnnotationInfos annotationInfos = elementInfos.getAnnotationInfos(Reference.class);
 				String serviceName = annotationInfos.getStringValue("service");
 				if (serviceName != null) {
 					System.out.println("Reference service: " + serviceName);
 					serviceInterface = serviceName;
 				}
 
 				String cardinality = "1..1";
 				String policy = "static";
 
 				switch (type) {
 
 					case '?' :
 						policy = "dynamic";
 						cardinality = "0..1";
 						break;
 
 					case '*' :
 						policy = "dynamic";
 						cardinality = "0..n";
 						break;
 
 					case '+' :
 						policy = "dynamic";
 						cardinality = "1..n";
 						break;
 
 					case '~' :
 						cardinality = "0..1";
 						break;
 
 					case ' ' :
 						cardinality = "1..1";
 						break;
 
 					default :
 						if (dynamicReference) {
 							policy = "dynamic";
 						}
 						if (!multipleReference && optionalReference) {
 							cardinality = "0..1";
 						} else if (multipleReference && optionalReference) {
 							cardinality = "0..n";
 						} else if (multipleReference && !optionalReference) {
 							cardinality = "1..n";
 						}
 				}
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
