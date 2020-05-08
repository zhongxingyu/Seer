 package org.spoofax.modelware.emf;
 
 import java.util.ArrayList;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 
 /**
  * @author Oskar van Rest
  */
 public class Model2Term extends AbstractModel2Term {
 
 	public Model2Term(ITermFactory factory) {
 		super(factory);
 	}
 
 	@Override
 	public IStrategoTerm convert(EObject object) {
 		EClass c = object.eClass();
 		EMap<String, String> index2name = c.getEAnnotation("StrategoTerm.index").getDetails();
 
 		IStrategoConstructor constructor = factory.makeConstructor(c.getName(), c.getEStructuralFeatures().size());
 		ArrayList<IStrategoTerm> kids = new ArrayList<IStrategoTerm>();
 
 		for (int i = 0; i < index2name.size(); i++) {
 			EStructuralFeature eSructuralFeature = c.getEStructuralFeature(index2name.get(Integer.toString(i)));
 			kids.add(convert(object, eSructuralFeature));
 		}
 
 		return factory.makeAppl(constructor, (IStrategoTerm[]) kids.toArray(new IStrategoTerm[kids.size()]), factory.makeList());
 	}
 
 	@Override
 	protected IStrategoTerm convert(EObject object, EAttribute attribute) {
 		Object value = object.eGet(attribute);
 
 		if (value == null) {
 			return null;
 		} else {
 			return factory.makeString(value.toString());
 		}
 	}
 
 	@Override
 	protected IStrategoTerm convert(EObject object, EReference reference) {
 		Object value = object.eGet(reference);
 
 		if (value instanceof EObject) {
 			if (reference.isContainment()) {
 				return convert((EObject) value);
 			} else {
 				return factory.makeString(EcoreUtil.getID((EObject) value));
 			}
 		} else if (value instanceof EList) {
 			EList<?> elements = (EList<?>) value;
 			ArrayList<IStrategoTerm> results = new ArrayList<IStrategoTerm>();
 			for (Object element : elements) {
 				if (reference.isContainment())
 					results.add(convert((EObject) element));
 				else
 					results.add(factory.makeString(EcoreUtil.getID((EObject) element)));
 			}
 			return factory.makeList(results);
 		}
 
 		return null;
 	}
 
 	@Override
 	protected IStrategoTerm someOrNone(IStrategoTerm term) {
 		if (term == null)
 			return factory.makeAppl(factory.makeConstructor("None", 0));
 		else
 			return factory.makeAppl(factory.makeConstructor("Some", 1), term);
 	}
 
 	@Override
 	protected IStrategoTerm createDefaultValue(EAttribute attribute) {
		return factory.makeString(attribute.getDefaultValueLiteral());
 	}
 
 	@Override
 	protected IStrategoTerm createDefaultValue(EReference reference) {
		Object defaultValue = reference.getEReferenceType().getEIDAttribute().getEAttributeType().getDefaultValue();

 		if (defaultValue == null)
 			return factory.makeString("x");
 		else
 			return factory.makeString(defaultValue.toString());
 	}
 }
