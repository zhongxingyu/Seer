 package org.spoofax.modelware.emf.trans;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.modelware.emf.utils.Utils;
 
 public class Tree2modelConverter {
 
 	private final EPackage pack;
 	private final List<Reference> references = new LinkedList<Reference>();
 	private final HashMap<IStrategoTerm, EObject> uriMap = new HashMap<IStrategoTerm, EObject>();
 
 	public Tree2modelConverter(EPackage pack) {
 		this.pack = pack;
 	}
 
 	public EObject convert(IStrategoTerm term) {
 		EObject result = convert((IStrategoAppl) term.getSubterm(0));
 		setReferences();
 		return result;
 	}
 
 	private EObject convert(IStrategoAppl term) {
 		IStrategoList URIs = (IStrategoList) term.getSubterm(0);
 		IStrategoTerm QID = term.getSubterm(1);
 		IStrategoList slots = (IStrategoList) term.getSubterm(2);
 
 		EClass c = getClass(QID);
 		EObject obj = createObject(QID);
 
 		for (IStrategoTerm uri : URIs.getAllSubterms()) {
 			if (!uriMap.containsKey(uri)) {
 				uriMap.put(uri, obj);
 			}
 		}
 
 		for (int i = 0; i < slots.getAllSubterms().length; i++) {
 			EStructuralFeature f = Utils.getFeature(c, i);
 			setFeature(slots.getAllSubterms()[i], obj, f);
 		}
 		
 		return obj;
 	}
 
 	private void setFeature(IStrategoTerm t, EObject obj, EStructuralFeature f) {
 		if (Utils.isNone(t) || Utils.isEmptyList(t)) {
 			return;
 		}
 		if (Utils.isSome(t)) {
 			t = t.getSubterm(0); // normalization
 		}
 		final boolean isList = t.getSubterm(0).isList();
 		if (!isList) {
 			IStrategoTerm list = Utils.termFactory.makeList(t.getSubterm(0));
 			t = Utils.termFactory.makeAppl(((IStrategoAppl) t).getConstructor(), list); // normalization
 		}
 
 		String featureType = ((IStrategoAppl) t).getConstructor().getName();
 
 		if (featureType.equals("Link")) {
 			references.add(new Reference(obj, f, t.getSubterm(0)));
 		} else {
 			List<Object> values = new LinkedList<Object>();
 			for (IStrategoTerm subTerm : t.getSubterm(0).getAllSubterms()) {
 				if (featureType.equals("Data")) {
					EDataType type = ((EAttribute) f).getEAttributeType(); // TODO: error if f is not instance of EAttribute (possible error made by user)
 					values.add(EcoreUtil.createFromString(type, ((IStrategoString) subTerm).stringValue()));
 				} else if (featureType.equals("Contain")) {
 					values.add(convert((IStrategoAppl) subTerm));
 				}
 			}
 
 			obj.eSet(f, isList ? values : values.get(0));
 		}
 	}
 
 	
 	private EObject createObject(IStrategoTerm QID) {
 		return getPackage(QID).getEFactoryInstance().create(getClass(QID));
 	}
 	
 	private EClass getClass(IStrategoTerm QID) {
 		EPackage pack = getPackage(QID);
 		String className = ((IStrategoString) QID.getSubterm(1)).stringValue();
 		return (EClass) pack.getEClassifier(className);
 	}
 	
 	private EPackage getPackage(IStrategoTerm QID) {
 		String subpackName = ((IStrategoString) QID.getSubterm(0)).stringValue();
 		for (EPackage subpack : pack.getESubpackages()) {
 			if (subpack.getName().equals(subpackName)) {
 				return subpack;
 			}
 		}
 		return pack;
 	}
 
 	private void setReferences() {
 		for (Reference ref : references) {
 			EList<EObject> results = new BasicEList<EObject>();
 			for (IStrategoTerm uri : ref.uris.getAllSubterms()) {
 				if (!Utils.isUnresolved(uri)) {
 					if (uriMap.get(uri) != null) { // error recovery: user forgets/has not yet added definition to model
 						results.add(uriMap.get(uri));
 					}
 				}
 			}
 
 			if (results.size() > 0) {
 				ref.object.eSet(ref.feature, ref.feature.isMany() ? results : results.get(0));
 			}
 		}
 	}
 
 	private class Reference {
 
 		public final EObject object;
 		public final EStructuralFeature feature;
 		public final IStrategoTerm uris;
 
 		public Reference(EObject object, EStructuralFeature feature, IStrategoTerm uris) {
 			this.object = object;
 			this.feature = feature;
 			this.uris = uris;
 		}
 	}
 }
