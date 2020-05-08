 package be.ac.ua.visitors;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementVisitor;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.TypeParameterElement;
 import javax.lang.model.element.VariableElement;
 
 import be.ac.ua.visitors.IdentifierCheckerEV.CheckerInfo;
 
 /**
  * Class that implements an ElementVisitor that adds the functionality
  * of checking if the annotations are well formed and
  * if all the identifiers used on them correspond to the element.
  */
 public class AnnotationsCheckerEV implements ElementVisitor<Void, Void> {
 
 	@Override
 	public Void visit(Element e, Void p) {
 		ElementVisitor<Void, CheckerInfo> identifierChecker;
 		CheckerInfo ci;
 
 		for (AnnotationMirror annotation : e.getAnnotationMirrors()) {
 			identifierChecker = new IdentifierCheckerEV();
 			List<String> identifiers = annotation.getElementValues().values()
 					.iterator().next().accept(new IdentifiersGetterAVV(), null);
			System.out.println(Arrays.toString(identifiers.toArray()));
 			ci = new CheckerInfo(identifiers, annotation);
 			e.accept(identifierChecker, ci);
 		}
 		return null;
 	}
 
 	@Override
 	public Void visit(Element e) {
 		return visit(e,null);
 	}
 
 	@Override
 	public Void visitPackage(PackageElement e, Void p) {
 		return null;
 	}
 
 	@Override
 	public Void visitType(TypeElement e, Void p) {
 		return null;
 	}
 
 	@Override
 	public Void visitVariable(VariableElement e, Void p) {
 		return visit(e,null);
 	}
 
 	@Override
 	public Void visitExecutable(ExecutableElement e, Void p) {
 		return visit(e,null);
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
