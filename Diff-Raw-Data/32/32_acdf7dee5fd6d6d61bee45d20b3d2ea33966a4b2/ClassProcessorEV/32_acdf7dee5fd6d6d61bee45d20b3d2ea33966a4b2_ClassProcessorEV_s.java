 package be.ac.ua.visitors;
 
 import java.lang.reflect.Modifier;
 
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ElementVisitor;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.TypeParameterElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.TypeMirror;
 
 import be.ac.ua.processor.AnnotationProcessor;
 /**
  * Processes a class, extracting its structure to an internal representation 
  * and stores the contracts within it as well as the relationship with the
  * other classes.
  */
 public class ClassProcessorEV implements ElementVisitor<Void, Void> {
 
 	@Override
 	public Void visit(Element e, Void p) {
 		TypeMirror dt = ((TypeElement)e).getSuperclass();
 		boolean same = AnnotationProcessor.getTypeUtils()
 				.isSameType(AnnotationProcessor.getElementUtils()
 						.getTypeElement("java.lang.Object").asType(), dt);
 		// If does not inherit from Object
 		if(!same){
			//e.accept(new ParentProcessorEV(),AnnotationProcessor.getTypeUtils().asElement(dt));
 			Element superElement = AnnotationProcessor.getTypeUtils().asElement(dt);
 
			for(Element subClassElement : superElement.getEnclosedElements()){
 				for (Element superClassElement : superElement.getEnclosedElements()) {

 					if(subClassElement.getModifiers().contains(Modifier.PRIVATE) ||
 							superClassElement.getModifiers().contains(Modifier.PRIVATE)){
 						continue;
 					} else if((superClassElement.getKind().equals(ElementKind.METHOD) 
 							&& subClassElement.getKind().equals(ElementKind.METHOD)
 							&& AnnotationProcessor.getTypeUtils().isSameType(subClassElement.asType(),
 									superClassElement.asType()))
 									|| (superClassElement.getKind().equals(ElementKind.FIELD) 
 											&& subClassElement.getKind().equals(ElementKind.FIELD) 
 											&& subClassElement.getSimpleName()
 											.equals(superClassElement.getSimpleName()))){
 
 						superClassElement.accept(new AnnotationsCheckerEV(), null);
 						subClassElement.accept(new AnnotationsCheckerEV(), null);
 						//TODO Join contracts
 					}else{
 						continue;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Void visit(Element e) {
 		return visit(e, null);
 	}
 
 	@Override
 	public Void visitPackage(PackageElement e, Void p) {
 		return null;
 	}
 
 	@Override
 	public Void visitType(TypeElement e, Void p) {
 		return visit(e, null);
 	}
 
 	@Override
 	public Void visitVariable(VariableElement e, Void p) {
 		return null;
 	}
 
 	@Override
 	public Void visitExecutable(ExecutableElement e, Void p) {
 		return null;
 	}
 
 	@Override
 	public Void visitTypeParameter(TypeParameterElement e, Void p) {
 		return null;
 	}
 
 	@Override
 	public Void visitUnknown(Element e, Void p) {
 		return null;
 	}
 
 }
