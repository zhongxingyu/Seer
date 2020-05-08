 package org.spoofax.modelware.emf;
 
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
 
 /**
  * @author Oskar van Rest
  */
 public class Subterm2Object {
 
 	public EObject subterm2object(IStrategoTerm term, EObject root) {
 		List<Integer> path = StrategoTermPath.createPathList(term);
 		return path2object(path, root);
 	}
 	
 	private EObject path2object(List<Integer> path, EObject root) {
 		EObject current = root;
 
 		for (int i = 0; i < path.size(); i++) {
 			EClass eClass = current.eClass();
			EMap<String, String> index2name = eClass.getEAnnotation("StrategoTerm.index").getDetails();
 			EStructuralFeature feature = eClass.getEStructuralFeature(index2name.get(path.get(i).toString()));
 			if (feature.getLowerBound() == 0 && feature.getUpperBound() == 1) { // ignore Some(...)
 				i++;
 			}
 			if (feature.getUpperBound() == -1) { // list
 				if (i + 1 < path.size()) {
 					current = (EObject) ((EList<?>) current.eGet(feature)).get(path.get(i + 1));
 					i++;
 				}
 			} else {
 				current = (EObject) current.eGet(feature);
 			}
 		}
 
 		return current;
 	}
 }
