 /**
  * 
  */
 package hu.e.compiler.tasks.internal;
 
 import hu.e.compiler.TaskUtils;
 import hu.modembed.model.core.CorePackage;
 import hu.modembed.model.core.MODembedElement;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 /**
  * @author balazs.grill
  *
  */
 public abstract class AbstractConverter {
 
 	private class Reference{
 		public final EObject source;
 		public final EReference reference;
 		public final EObject originalTarget;
 		
 		private Reference(EObject source, EReference reference,
 				EObject originalTarget) {
 			this.source = source;
 			this.reference = reference;
 			this.originalTarget = originalTarget;
 		}
 	
 		@SuppressWarnings("unchecked")
 		private void resolve(AbstractConverter converter){
 			EObject target = converter.getConverted(originalTarget);
 			if (reference.isMany()){
 				((List<EObject>)source.eGet(reference)).add(target);
 			}else{
 				source.eSet(reference, target);
 			}
 		}
 	}
 	
 	private final Map<EObject, EObject> converted = new HashMap<EObject, EObject>();
 	private final List<Reference> references = new LinkedList<Reference>();
 	
 	public EObject getConverted(EObject original) {
 		EObject target = converted.get(original);
 		if (target == null) target = original;
 		return target;
 	}
 	
 	public void addReference(EObject source, EReference reference, EObject originalTarget){
 		references.add(new Reference(source, reference, originalTarget));
 	}
 	
 	public void resolveCrossReferences(){
 		for(Reference ref : references){
 			ref.resolve(this);
 		}
 	}
 
 	protected EObject internalCopy(EObject element){
 		EClass eclass = element.eClass();
 		EObject result = eclass.getEPackage().getEFactoryInstance().create(eclass);
 		converted.put(element, result);
 		
 		if (element instanceof MODembedElement){
			TaskUtils.addOrigin((MODembedElement) result, (MODembedElement)element);
 		}
 		
 		for(EStructuralFeature feature: eclass.getEAllStructuralFeatures()){
 			if (!CorePackage.eINSTANCE.getMODembedElement_Origins().equals(feature)){
 				if (feature instanceof EAttribute){
 					result.eSet(feature, element.eGet(feature));
 				}
 				if (feature instanceof EReference){
 					if (((EReference) feature).isContainment()){
 						if (feature.isMany()){
 							@SuppressWarnings("unchecked")
 							List<EObject> resultList = (List<EObject>) result.eGet(feature);
 							for(Object o : (List<?>)element.eGet(feature)){
 								EObject copied = copy((EObject)o);
 								if (copied != null)
 									resultList.add(copied);
 							}
 						}else{
 							Object o = element.eGet(feature);
 							if (o instanceof EObject){
 								result.eSet(feature, copy((EObject)o));
 							}
 						}
 					}else{
 						if (feature.isMany()){
 							for(Object o : (List<?>)element.eGet(feature)){
 								addReference(result, (EReference)feature, (EObject)o);
 							}
 						}else{
 							Object o = element.eGet(feature);
 							if (o instanceof EObject){
 								addReference(result, (EReference)feature, (EObject)o);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T extends EObject> T copy(T element){
 		return (T)internalCopy(element);
 	}
 	
 }
