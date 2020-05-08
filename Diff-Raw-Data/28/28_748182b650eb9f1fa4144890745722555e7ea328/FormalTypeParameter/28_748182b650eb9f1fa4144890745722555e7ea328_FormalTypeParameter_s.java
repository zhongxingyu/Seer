 package chameleon.oo.type.generics;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.association.OrderedMultiAssociation;
 
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 import chameleon.exception.ChameleonProgrammerException;
 import chameleon.oo.language.ObjectOrientedLanguage;
 import chameleon.oo.type.ConstructedType;
 import chameleon.oo.type.Type;
 import chameleon.oo.type.TypeReference;
 import chameleon.util.CreationStackTrace;
 
 /**
  * This class represents generic parameters as used in Java and C#.
  * 
  * @author Marko van Dooren
  */
 public class FormalTypeParameter extends TypeParameter<FormalTypeParameter> {
 
 	public FormalTypeParameter(SimpleNameSignature signature) {
 		super(signature);
 	}
 	
   
 	@Override
 	public FormalTypeParameter clone() {
 		FormalTypeParameter result = new FormalTypeParameter(signature().clone());
 		for(TypeConstraint constraint: constraints()) {
 			result.addConstraint(constraint.clone());
 		}
 		return result;
 	}
 	
 	/**
 	 * Resolving a generic parameter results in a constructed type whose bound
 	 * is the upper bound of this generic parameter as defined by the upperBound method.
 	 */
 	public Type selectionDeclaration() throws LookupException {
 		ConstructedType constructedType = new ConstructedType(signature().clone(),upperBound(),this);
 		constructedType.setUniParent(parent());
 		return constructedType;
 	}
 	
 	public Type resolveForRoundTrip() throws LookupException {
   	Type result = new LazyTypeAlias(signature().clone(), this);
   	result.setUniParent(parent());
   	return result;
 	}
 	
 	public static class LazyTypeAlias extends ConstructedType {
 
 		public LazyTypeAlias(SimpleNameSignature sig, FormalTypeParameter param) {
 			super(sig,null,param);
 		}
 		
 		public Type aliasedType() {
 			try {
 				return parameter().upperBound();
 			} catch (LookupException e) {
 				throw new Error("LookupException while looking for aliasedType of a lazy alias",e);
 			}
 		}
 	}
 	
 
 
 	public List<Element> children() {
 		List<Element> result = new ArrayList<Element>();
 		result.add(signature());
 		result.addAll(constraints());
 		return result;
 	}
 	
 	private OrderedMultiAssociation<FormalTypeParameter,TypeConstraint> _typeConstraints = new OrderedMultiAssociation<FormalTypeParameter,TypeConstraint>(this);
 	
 	public List<TypeConstraint> constraints() {
 		return _typeConstraints.getOtherEnds();
 	}
 	
 	public void addConstraint(TypeConstraint constraint) {
 		if(constraint != null) {
 			_typeConstraints.add(constraint.parentLink());
 		}
 	}
 	
 	public TypeReference upperBoundReference() {
 		ObjectOrientedLanguage language = language(ObjectOrientedLanguage.class);
		TypeReference result = language.createTypeReference(language.getDefaultSuperClassFQN());
		for(TypeConstraint constraint: constraints()) {
			result = result.intersection(constraint.upperBoundReference());
 		}
		result.setUniParent(this);
 		return result;
 	}
 
 	public Type upperBound() throws LookupException {
 		Type result = language(ObjectOrientedLanguage.class).getDefaultSuperClass();
 		for(TypeConstraint constraint: constraints()) {
 			result = result.intersection(constraint.upperBound());
 		}
 		return result;
 	}
 
 	public FormalTypeParameter alias(SimpleNameSignature signature) {
 		throw new ChameleonProgrammerException();
 	}
 
 
 //	@Override
 //	public boolean compatibleWith(TypeParameter other) throws LookupException {
 //		return equals(other);
 //	}
 
 
 	@Override
 	public Type lowerBound() throws LookupException {
 		Type result = language(ObjectOrientedLanguage.class).getNullType();
 		for(TypeConstraint constraint: constraints()) {
 			result = result.intersection(constraint.lowerBound());
 		}
 		return result;
 	}
 
 
 	@Override
 	public boolean uniSameAs(Element other) throws LookupException {
 		if(origin() == this) {
 			return this == other;
 		} else {
 			return origin().sameAs(other);
 		}
 	}
 
   @Override
   public int hashCode() {
 		if(origin() == this) {
 			return super.hashCode();
 		} else {
 			return origin().hashCode();
 		}
 
   }
 }
