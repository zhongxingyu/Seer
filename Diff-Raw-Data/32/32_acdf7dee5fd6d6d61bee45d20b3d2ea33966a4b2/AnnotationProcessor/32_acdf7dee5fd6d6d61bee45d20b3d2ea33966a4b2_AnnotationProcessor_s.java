 package be.ac.ua.processor;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Messager;
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.util.Elements;
 import javax.lang.model.util.Types;
 import javax.tools.Diagnostic.Kind;
 
 import be.ac.ua.visitors.ClassProcessorEV;
 
@SupportedAnnotationTypes({"org.ua.annotations.*"})
 @SupportedSourceVersion(SourceVersion.RELEASE_7)
 public class AnnotationProcessor extends AbstractProcessor {
 
 	private static Messager messager;
 	private static Types typeUtils;
 	private static Elements elementUtils;
 	private Set<Element> checked;
 //	private SMTSolver SMT;
 
 	public static Messager getMessager() {
 		return messager;
 	}
 
 	public static Types getTypeUtils() {
 		return typeUtils;
 	}
 
 	public static Elements getElementUtils() {
 		return elementUtils;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public synchronized void init(ProcessingEnvironment processingEnv){
 		super.init(processingEnv);
 		messager = processingEnv.getMessager();
 		typeUtils = processingEnv.getTypeUtils();
 		elementUtils = processingEnv.getElementUtils();
 //		SMT = SMTSolver.getInstance();
 		checked = new HashSet<Element>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean process(Set<? extends TypeElement> annotations,
 			RoundEnvironment roundEnv) {

 		if(!roundEnv.processingOver()){
 			// Type of annotations present in the analyzed code
 			for (TypeElement annotation : annotations) {
 				// Elements annotated with them
 				for(Element el: roundEnv.getElementsAnnotatedWith(annotation)){
 
 					if (!checked.contains(el.getEnclosingElement())) {
 						checked.add(el.getEnclosingElement());
 						el.getEnclosingElement()
 						.accept(new ClassProcessorEV(), null);
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Join the annotations in the first element with the same type of annotations
 	 * present in the second one. If there is one of the annotations not present in
 	 * the second element, the inheritance from the superclass is warned. If there
 	 * are annotations of the second element not present in the first one, throws
 	 * an error.
 	 * 
 	 * @param superE Superclass element
 	 * @param subE Subclass element
 	 * @return A list of contract pairs
 	 */
 	private List<ContractPair> joinContracts(Element superE, Element subE){
 
 		List<ContractPair> contracts = new ArrayList<ContractPair>();
 		Set<DeclaredType> typesSuper = new HashSet<DeclaredType>();
 		boolean found = false;
 
 		for ( AnnotationMirror annotation1 : superE.getAnnotationMirrors()) {
 			typesSuper.add(annotation1.getAnnotationType());
 			for ( AnnotationMirror annotation2 : subE.getAnnotationMirrors()) {
 
 				if(typeUtils.isSameType(annotation1.getAnnotationType(),
 						annotation2.getAnnotationType())){
 					contracts.add(new ContractPair(annotation1, annotation2));
 					found = true;
 					break;
 				}
 			}
 			if(!found){
 				messager.printMessage(Kind.NOTE, "Inheriting superclass contract",
 						subE, annotation1);
 			}
 		}
 		//Get the annotations from the second element that
 		//were not in the first one and throw error
 		for ( AnnotationMirror annotation2 : subE.getAnnotationMirrors()) {
 			if (!typesSuper.contains(annotation2.getAnnotationType())) {
 				messager.printMessage(Kind.ERROR, "Subclass element contract more "
 						+"restrictive than the corresponding parent",
 						subE, annotation2);
 			}
 		}
 		return contracts;
 	}
 }
