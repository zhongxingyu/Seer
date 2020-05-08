 package chameleon.core.method;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.rejuse.association.OrderedMultiAssociation;
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.declaration.Declaration;
import chameleon.core.declaration.DeclarationWithParametersHeader;
 import chameleon.core.declaration.Signature;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.namespace.NamespaceElement;
 import chameleon.core.variable.FormalParameter;
 import chameleon.core.variable.VariableContainer;
 import chameleon.exception.ModelException;
 import chameleon.oo.type.Type;
 import chameleon.oo.type.generics.TypeParameter;
 import chameleon.oo.type.generics.TypeParameterBlock;
 import chameleon.util.Util;
 /**
  * A class of objects representing method headers. A method header contains for example the name and parameters of a method.
  * 
  * @author Marko van Dooren
  *
  * @param <E>
  * @param <P>
  * @param <S>
  */
public abstract class MethodHeader<E extends MethodHeader, P extends NamespaceElement, S extends MethodSignature> extends DeclarationWithParametersHeader <E,P,S> implements VariableContainer<E, P> { //extends Signature<E, P> 
   
   public E clone() {
     E result = cloneThis();
     for(FormalParameter param:formalParameters()) {
       result.addFormalParameter(param.clone());
     }
     for(TypeParameter<?> param:typeParameters()) {
     	result.addTypeParameter(param.clone());
     }
     return result;
   }
 
   /**
    * Return the signature of the method of this method header. The signature is generated based on
    * the information in the header.
    * @return
    */
   public abstract S signature();
   
   public abstract void setName(String name);
   
   public abstract E createFromSignature(Signature signature);
   
   protected abstract E cloneThis();
   
   /**
    * The children of a method header are its formal parameters and the type parameter block.
    */
  /*@
    @ public behavior
    @
    @ post \result != null;
    @ post \result.containsAll(formalParameters());
    @ post parameterBlock() != null ==> \result.contains(parameterBlock());
    @*/
   public List<Element> children() {
   	List<Element> result = new ArrayList<Element>();
   	result.addAll(formalParameters());
   	Util.addNonNull(parameterBlock(), result);
   	return result;
   }
   
   /**
    * The name of a method header is the name of its signature.
    */
  /*@
    @ public behavior
    @
    @ post \result == signature().name();
    @*/
   public String name() {
   	return signature().name();
   }
   
   /*********************
    * FORMAL PARAMETERS *
    *********************/
 
   public List<FormalParameter> formalParameters() {
     return _parameters.getOtherEnds();
   }
 
   public void addFormalParameter(FormalParameter arg) {
     _parameters.add(arg.parentLink());
   }
 
   public int nbFormalParameters() {
     return _parameters.size();
   }
   
   /**
    * The index starts from 1.
    * @param index
    * @return
    */
   public FormalParameter formalParameter(int index) {
   	return _parameters.elementAt(index);
   }
 
   private OrderedMultiAssociation<MethodHeader,FormalParameter> _parameters = new OrderedMultiAssociation<MethodHeader,FormalParameter>(this);
   
   /**
    * Return the type of the formal parameters of this signature.
    * 
    * @return
    * @throws ModelException
    */
   public List<Type> formalParameterTypes() throws LookupException {
     List<Type> result = new ArrayList<Type>();
     for(FormalParameter param:formalParameters()) {
       result.add(param.getType());
     }
     return result;
   }
 
 //  /**
 //   * Check whether or not this method contains a formal parameter with the given name.
 //   *
 //   * @param name
 //   *        The name that has to be checked.
 //   */
 //  /*@
 //   @ public behavior
 //   @
 //   @ post \result == (\exists FormalParameter fp; getFormalParameters.contains(fp);
 //   @                   fp.getName().equals(name);
 //   @*/
 //  public boolean containsParameterWithName(final String name) {
 //    return new SafePredicate() {
 //      public boolean eval(Object o) {
 //        return ((FormalParameter)o).getName().equals(name);
 //      }
 //    }.exists(getParameters());
 //  }
 
 //  public Type getNearestType() {
 //  	return parent().getNearestType();
 //  }
   
 	public List<? extends Declaration> locallyDeclaredDeclarations() throws LookupException {
 		return declarations();
 	}
 
   
   /**
    * The declarations of a method header are its formal parameters and its type parameters.
    */
  /*@
    @ public behavior
    @
    @ post \result != null;
    @ post \result.containsAll(formalParameters());
    @ post \result.containsAll(typeParameters());
    @*/
   public List<Declaration> declarations() {
     List<Declaration>  result = new ArrayList<Declaration>();
     result.addAll(formalParameters());
     result.addAll(typeParameters());
     return result;
   }
   
 	public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 		return selector.selection(declarations());
 	}
 
   @Override
   public LookupStrategy lexicalLookupStrategy(Element element) throws LookupException {
   	if(formalParameters().contains(element)) {
   		if(_lexical == null) {
   			_lexical = language().lookupFactory().createLexicalLookupStrategy(localLookupStrategy(), this);
   		}
   		return _lexical;
   	}
   	else {
   		return parent().lexicalLookupStrategy(this);
   	}
   }
   
   public LookupStrategy localLookupStrategy() {
   	if(_local == null) {
   		_local = language().lookupFactory().createTargetLookupStrategy(this);
   	}
   	return _local;
   }
   
   private LookupStrategy _local;
   
   private LookupStrategy _lexical;
 
 	public boolean sameParameterTypesAs(MethodHeader other) throws ModelException {
   	boolean result = false;
   	if (other != null) {
 			List<FormalParameter> mine = formalParameters();
 			List<FormalParameter> others = other.formalParameters();
 			result = mine.size() == others.size();
 			Iterator<FormalParameter> iter1 = mine.iterator();
 			Iterator<FormalParameter> iter2 = others.iterator();
 			while (result && iter1.hasNext()) {
         result = result && iter1.next().getType().equals(iter2.next().getType());
 			}
 		}
   	return result;
   }
 
   public NamespaceElement variableScopeElement() {
   	return parent();
   }
   
 //  public LookupStrategy lexicalLookupStrategy(Element element) {
 //  	return language().lookupFactory().createLexicalLookupStrategy(language().lookupFactory().createLocalLookupStrategy(this),this);
 //  }
   
 	private SingleAssociation<MethodHeader, TypeParameterBlock> _typeParameters = new SingleAssociation<MethodHeader, TypeParameterBlock>(this);
 	
 	public TypeParameterBlock parameterBlock() {
 		return _typeParameters.getOtherEnd();
 	}
 	
 	/**
 	 * Return the type parameters of this method header. If 
 	 * @return
 	 */
 	public List<TypeParameter> typeParameters() {
 		TypeParameterBlock parameterBlock = parameterBlock();
 		return (parameterBlock == null ? new ArrayList<TypeParameter>() :parameterBlock.parameters());
 	}
 	
 	/**
 	 * Return the index-th type parameter. Indices start at 1.
 	 */
 	public TypeParameter typeParameter(int index) {
 		return parameterBlock().parameter(index);
 	}
 	
 	public void addAllTypeParameters(Collection<TypeParameter> parameters) {
 		for(TypeParameter param:parameters) {
 			addTypeParameter(param);
 		}
 	}
 
 	public void addTypeParameter(TypeParameter parameter) {
 		TypeParameterBlock parameterBlock = parameterBlock();
 		if(parameterBlock == null) {
 			// Lazy init.
 			parameterBlock = new TypeParameterBlock();
 			_typeParameters.connectTo(parameterBlock.parentLink());
 		}
 		parameterBlock.add(parameter);
 	}
 
 	public void removeTypeParameter(TypeParameter parameter) {
 		parameterBlock().add(parameter);
 	}
 	
 	public void replaceTypeParameter(TypeParameter oldParameter, TypeParameter newParameter) {
 		parameterBlock().replace(oldParameter, newParameter);
 	}
 
 
 }
