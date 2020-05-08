 package org.spoofax.modelware.emf.tree2model;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
  
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.strategoxt.imp.runtime.Environment;
 
 /**
  * @author Oskar van Rest
  */
 public class Term2Model extends AbstractTerm2Model {
 
 	private final EPackage MM;
 	private final List<Reference> references;
 	private final HashMap<String, EObject> uriMap;
 	
 	private class Reference {
 	
 		public Reference (IStrategoTerm term, EObject object, EStructuralFeature feature) {
 			this.term = term;
 			this.object = object;
 			this.feature = feature;
 		}
 		
 		public IStrategoTerm term;
 		public EObject object;
 		public EStructuralFeature feature;
 	}
 
 	public Term2Model(EPackage MM) {
 		this.MM = MM;
 		references = new LinkedList<Reference>();
 		uriMap = new HashMap<String, EObject>();
 	}
 
 	@Override
 	protected Object convert(IStrategoAppl term, EStructuralFeature feature) {
 		String constructor = term.getConstructor().getName();
 
 		if (constructor.equals("Some")) {
 			return convert(term.getSubterm(0), feature);
 		}
 		if (constructor.equals("None")) {
 			return null;
 		}
 
 		EClass c = (EClass) MM.getEClassifier(constructor);
 		EMap<String, String> term2slot = c.getEAnnotation(term2featureAnno).getDetails();
 		EObject object = MM.getEFactoryInstance().create(c);
 		
 		for (int i = 0; i < term.getSubtermCount(); i++) {
 			EStructuralFeature f = c.getEStructuralFeature(term2slot
 					.get(Integer.toString(i)));
 
 			if (f instanceof EAttribute) {
 				if (f.getEAnnotation(defAnno) != null) {
					IStrategoList spoofaxURI = fetchURI(term.getSubterm(i));
 					if (spoofaxURI != null && !uriMap.containsKey(spoofaxURI.toString())) {
 						uriMap.put(spoofaxURI.toString(), object);
 					}
 				}			
 			}
 			
 			if (f instanceof EReference && !((EReference) f).isContainment()) {
 				Reference reference = new Reference(term.getSubterm(i), object, f);
 				references.add(reference);
 			}
 			else {
 				Object value = convert(term.getSubterm(i), f);
 				object.eSet(f, value);
 			}
 		}
 
 		return object;
 	}
 
 	@Override
 	protected Object convert(IStrategoString term, EStructuralFeature feature) {
 		
 		if (feature instanceof EAttribute) {
 			
 			EAttribute eAttribute = (EAttribute) feature;
 			EDataType eDataType = eAttribute.getEAttributeType();
 			return EcoreUtil.createFromString(eDataType, term.stringValue());
 		}
 
 		return null;
 	}
 
 	@Override
 	protected List<Object> convert(IStrategoList term, EStructuralFeature feature) {
 
 		if (term.isEmpty())
 			return new LinkedList<Object>();
 		else {
 			List<Object> result = convert(term.tail(), feature);
 			result.add(0, convert(term.head(), feature));
 			return result;
 		}
 	}
 
 
 	@Override
 	protected void setReferences() {
 		for (Reference ref : references) {
			IStrategoList spoofaxURI = fetchURI(ref.term);
 			if (spoofaxURI != null) {
 				EObject target = uriMap.get(spoofaxURI.toString());
 				ref.object.eSet(ref.feature, target);
 			}
 		}
 	}
 
	private IStrategoList fetchURI(IStrategoTerm term) {
 		try {
			return (IStrategoList) term.getAnnotations().head();
 		} catch (Exception e) {
 			Environment.logException("URI expected for " + term.toString() + ", but not found or in wrong format.");
 		}
 		return null;
 	}
 
 
 }
