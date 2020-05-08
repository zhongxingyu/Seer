 package jnome.core.type;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.rejuse.association.OrderedMultiAssociation;
 
 import chameleon.core.Config;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.TargetDeclaration;
 import chameleon.core.element.ChameleonProgrammerException;
 import chameleon.core.element.Element;
 import chameleon.core.expression.NamedTarget;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.reference.CrossReference;
 import chameleon.core.type.DerivedType;
 import chameleon.core.type.Type;
 import chameleon.core.type.TypeReference;
 import chameleon.core.type.generics.ActualTypeArgument;
 import chameleon.core.type.generics.InstantiatedTypeParameter;
 import chameleon.core.type.generics.TypeParameter;
 
 /**
  * A class for Java type references. They add support for array types and generic parameters.
  * 
  * @author Marko van Dooren
  */
 public class JavaTypeReference extends TypeReference {
 
   public JavaTypeReference(String fqn) {
     this(fqn,0);
   }
   
   public JavaTypeReference(CrossReference<?,?,? extends TargetDeclaration> target, String name) {
   	super(target,name);
   }
   
   /**
    * THIS ONLY WORKS WHEN THE NAMED TARGET CONSISTS ENTIRELY OF NAMEDTARGETS.
    * @param target
    */
   public JavaTypeReference(NamedTarget target) {
  	super(target.getTarget() == null ? null : new JavaTypeReference((NamedTarget)target.getTarget()),target.getName());
   }
   
   public JavaTypeReference(String fqn, int arrayDimension) {
   	super(fqn);
   	if(Config.DEBUG) {
   		if((fqn != null) && (fqn.contains("["))) {
   			throw new ChameleonProgrammerException("Initializing a type reference with a [ in the name.");
   		}
   	}
   	setArrayDimension(arrayDimension);
   }
   
   public List<ActualTypeArgument> typeArguments() {
   	return _genericParameters.getOtherEnds();
   }
   
   public void addArgument(ActualTypeArgument arg) {
   	if(arg != null) {
   		_genericParameters.add(arg.parentLink());
   	}
   }
   
   public void addAllArguments(List<ActualTypeArgument> args) {
   	for(ActualTypeArgument argument : args) {
   		addArgument(argument);
   	}
   }
   
   public void removeArgument(ActualTypeArgument arg) {
   	if(arg != null) {
   		_genericParameters.remove(arg.parentLink());
   	}
   }
   
   private OrderedMultiAssociation<JavaTypeReference,ActualTypeArgument> _genericParameters = new OrderedMultiAssociation<JavaTypeReference, ActualTypeArgument>(this);
   
   public List<Element> children() {
   	List<Element> result = super.children();
   	result.addAll(_genericParameters.getOtherEnds());
   	return result;
   }
   
   public JavaTypeReference toArray(int arrayDimension) {
   	JavaTypeReference result = clone();
   	result.setArrayDimension(arrayDimension);
   	return result;
   }
 
   private int _arrayDimension;
   
   public int arrayDimension() {
   	return _arrayDimension;
   }
   
   public void setArrayDimension(int arrayDimension) {
   	_arrayDimension = arrayDimension;
   }
   
   protected <X extends Declaration> X getElement(DeclarationSelector<X> selector) throws LookupException {
     X result = null;
 
 	  boolean realSelector = selector.equals(selector());
 	  if(realSelector) {
 	    result = (X) getCache();
 	  }
 	  if(result != null) {
 	   	return result;
 	  }
 
     result = super.getElement(selector);
     
     if(realSelector) {
     	//First cast result to Type, then back to X.
     	//Because the selector is the connected selector of this Java type reference,
     	//we know that result is a Type.
       // FILL IN GENERIC PARAMETERS
       result = (X) fillInTypeArguments((Type)result);
       // ARRAY TYPE
       if ((arrayDimension() != 0) && (result != null)) {
         result = (X) new ArrayType(((Type)result),arrayDimension());
       }
     }
     
     if(result != null) {
     	if(realSelector) {
     		// This will flush the cache if the result is derived, for example, if it is an array type or generic instance
         setCache((Type)result);
     	}
       return result;
     } else {
       throw new LookupException("Result of type reference lookup is null: "+getName(),this);
     }
   }
 
   
   private Type fillInTypeArguments(Type type) throws LookupException {
   	Type result = type;
 		if (type != null) {
 			List<ActualTypeArgument> typeArguments = typeArguments();
 			if (typeArguments.size() > 0) {
 				result = new DerivedType(type);
 				// This is going to give trouble if there is a special lexical context
 				// selection for 'type' in its parent.
 				// set to the type itself? seems dangerous as well.
 				result.setUniParent(type.parent());
 				List<TypeParameter> parameters = result.parameters();
 				Iterator<TypeParameter> parametersIterator = parameters.iterator();
 				Iterator<ActualTypeArgument> argumentsIterator = typeArguments.iterator();
 				while (parametersIterator.hasNext()) {
 					TypeParameter parameter = parametersIterator.next();
 					ActualTypeArgument argument = argumentsIterator.next();
 					InstantiatedTypeParameter instantiated = new InstantiatedTypeParameter(parameter.signature().clone(), argument);
 					result.replaceParameter(parameter, instantiated);
 				}
 			}
 		}
 		return result;
 	}
 
 //  public Type getType() throws LookupException {
 //  	Type result = null;
 //  	
 //    result = getCache();
 //    if(result != null) {
 //    	return result;
 //    }
 //
 //    if (getArrayDimension() == 0) {
 //      if(getTarget() == null) {
 //        result = getParent().getContext(this).findType(getName());
 //      }
 //      else {
 //    	  NamespaceOrType target = getTarget().getNamespaceOrType();
 //        if(target != null) {
 //          result = target.getTargetContext().findType(getName());
 //        }
 //      }
 //    }
 //    else {
 //      if(getTarget() == null) {
 //        result = new ArrayType((Type)getParent().getContext(this).findType(getComponentName()), getArrayDimension());
 //      }
 //      else {
 //    	  NamespaceOrType target = getTarget().getNamespaceOrType();
 //        if(target != null) {
 //          result = new ArrayType((Type)target.getTargetContext().findType(getComponentName()), getArrayDimension());
 //        } 
 //      }
 //    }
 //    setCache(result);
 //    return result;
 //  }
 
 
   public JavaTypeReference clone() {
   	JavaTypeReference result =  new JavaTypeReference((getTarget() == null ? null : getTarget().clone()),getName());
   	result.setArrayDimension(arrayDimension());
   	for(ActualTypeArgument typeArgument: typeArguments()) {
   		result.addArgument(typeArgument.clone());
   	}
   	return result;
   }
 
 	public void addArrayDimension(int arrayDimension) {
 		_arrayDimension += arrayDimension;
 	}
   
 }
