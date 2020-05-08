 package jnome.core.expression;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jnome.core.type.JavaTypeReference;
 
 import org.rejuse.association.SingleAssociation;
 import org.rejuse.logic.ternary.Ternary;
 import org.rejuse.predicate.SafePredicate;
 
 import chameleon.core.MetamodelException;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.DeclarationContainer;
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.element.Element;
 import chameleon.core.expression.Expression;
 import chameleon.core.expression.Invocation;
 import chameleon.core.expression.InvocationTarget;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.relation.WeakPartialOrder;
 import chameleon.core.type.ClassBody;
 import chameleon.core.type.RegularType;
 import chameleon.core.type.Type;
 import chameleon.core.type.TypeElement;
 import chameleon.core.type.TypeReference;
 import chameleon.core.type.inheritance.SubtypeRelation;
 import chameleon.oo.language.ObjectOrientedLanguage;
 import chameleon.support.member.MoreSpecificTypesOrder;
 import chameleon.support.member.simplename.method.NormalMethod;
 import chameleon.util.Util;
 
 /**
  * @author Marko van Dooren
  *
  */
 public class ConstructorInvocation extends Invocation<ConstructorInvocation, NormalMethod> implements DeclarationContainer<ConstructorInvocation, Element> {
 
   /**
    * @param target
    */
   public ConstructorInvocation(JavaTypeReference type, InvocationTarget target) {
     super(target);
     setTypeReference(type);
   }
 
   public Expression getTargetExpression() {
     return (Expression)getTarget();
   }
 
 	/**
 	 * TYPE REFERENCE
 	 */
 	private SingleAssociation<ConstructorInvocation,JavaTypeReference> _typeReference = new SingleAssociation<ConstructorInvocation,JavaTypeReference>(this);
 
 
   public JavaTypeReference getTypeReference() {
     return (JavaTypeReference)_typeReference.getOtherEnd();
   }
 
     public void setTypeReference(JavaTypeReference type) {
         SingleAssociation<? extends TypeReference, ? super ConstructorInvocation> tref = type.parentLink();
         SingleAssociation<? extends JavaTypeReference, ? super ConstructorInvocation> ref = (SingleAssociation<? extends JavaTypeReference, ? super ConstructorInvocation>)tref;
         _typeReference.connectTo(ref);
     }
 
   /******************
    * ANONYMOUS TYPE *
    ******************/
 
 //    public void setAnonymousType(Type type) {
 //      if(type != null) {
 //        _anon.connectTo(type.parentLink());
 //      } else {
 //        _anon.connectTo(null);
 //      }
 //    }
 //
 //    public Type getAnonymousInnerType() {
 //    return _anon.getOtherEnd();
 //  }
 
 	private SingleAssociation<ConstructorInvocation,ClassBody> _body = new SingleAssociation<ConstructorInvocation,ClassBody>(this);
 	
 	public ClassBody body() {
 		return _body.getOtherEnd();
 	}
 	
 	public void setBody(ClassBody body) {
 		if(body == null) {
 			_body.connectTo(null);
 		} else {
 			_body.connectTo(body.parentLink());
 		}
 	}
 
   private Type createAnonymousType() throws LookupException {
   	final Type anon = new RegularType(new SimpleNameSignature("TODO"));
   	TypeReference tref = getTypeReference();
  	  Type writtenType = tref.getType();
 	  List<NormalMethod> superMembers = writtenType.directlyDeclaredMembers(NormalMethod.class);
 	  new SafePredicate<NormalMethod>() {
 		  @Override
 		  public boolean eval(NormalMethod object) {
 			  return object.is(language(ObjectOrientedLanguage.class).CONSTRUCTOR) == Ternary.TRUE;
 		  }
 	  }.filter(superMembers);
 	  for(NormalMethod method: superMembers) {
 	  	anon.add(method.clone());
 	  }
 	  for(TypeElement element : body().elements()) {
 	  	anon.add(element.clone());
 	  }
 	  anon.addInheritanceRelation(new SubtypeRelation(tref.clone()));
 	  // Attach the created type to this element.
 		anon.setUniParent(this);
 		return anon;
 	}
 
   protected Type actualType() throws LookupException {
     if (body() == null) {
       // Switching to target or not happens in getContext(Element) invoked by the type reference.
       return getTypeReference().getType();
     }
     else {
       return getAnonymousInnerType();
     }
   }
   
   public <X extends Declaration> X getElement(DeclarationSelector<X> selector) throws LookupException {
   	return actualType().targetContext().lookUp(selector);
 //  	InvocationTarget target = getTarget();
 //  	X result;
 //  	if(target == null) {
 //      result = lexicalLookupStrategy().lookUp(selector);
 //  	} else {
 //  		result = getTarget().targetContext().lookUp(selector);
 //  	}
 //		if (result == null) {
 //			//repeat lookup for debugging purposes.
 //	  	if(target == null) {
 //	      result = lexicalLookupStrategy().lookUp(selector);
 //	  	} else {
 //	  		result = getTarget().targetContext().lookUp(selector);
 //	  	}
 //			throw new LookupException("Method returned by invocation is null", this);
 //		}
 //    return result;
   }
 
   
 //	private Reference<ConstructorInvocation,Type> _anonymousType= new Reference<ConstructorInvocation,Type>(this);
 
   
   public Type getAnonymousInnerType() throws LookupException {
   	if(body() == null) {
   		return null;
   	} else {
   	  return createAnonymousType();
   	}
   }
 
   protected ConstructorInvocation cloneInvocation(InvocationTarget target) {
     ConstructorInvocation result = new ConstructorInvocation((JavaTypeReference)getTypeReference().clone(), (Expression)target);
     if(body() != null) {
       result.setBody(body().clone());
     }
     return result;
   }
 
   public void prefix(InvocationTarget target) throws LookupException {
     if(getTarget() != null) {
       getTarget().prefixRecursive(target);
     }
   }
 
  /*@
    @ also public behavior
    @
    @ post getAnonymousInnerType() != null ==> \result.contains(getAnonymousInnerType());
    @*/
   public List<Element> children() {
     List<Element> result = super.children();
     Util.addNonNull(body(), result);
    Util.addNonNull(getTypeReference(), result);
     return result;
   }
   
   @Override
   public LookupStrategy lexicalLookupStrategy(Element element) throws LookupException {
     if ((element == getTypeReference()) && (getTargetExpression() != null)) {
       return getTargetExpression().targetContext();
     } else {
       return super.lexicalLookupStrategy(element);
     }
   }
 
 //  public NormalMethod getMethod() throws LookupException {
 //  	InvocationTarget target = getTarget();
 //  	NormalMethod result;
 //  	if(getAnonymousInnerType() != null) {
 //  		LookupStrategy tctx = getAnonymousInnerType().targetContext();
 //  		result = tctx.lookUp(selector());
 //  	} else if(target == null) {
 //      result = lexicalLookupStrategy().lookUp(selector());
 //  	} else {
 //  		result = getTarget().targetContext().lookUp(selector());
 //  	}
 //		if (result == null) {
 //			throw new LookupException("Lookup in constructor invocation returned null",this);
 //		}
 //    return result;
 //  }
 
   public class ConstructorSelector extends DeclarationSelector<NormalMethod> {
     
     public NormalMethod filter(Declaration declaration) throws LookupException {
     	NormalMethod result = null;
 			NormalMethod<?,?,?> decl = (NormalMethod) declaration;
 			if(decl.nearestAncestor(Type.class).signature().sameAs(getTypeReference().signature())) {
 			if(decl.is(language(ObjectOrientedLanguage.class).CONSTRUCTOR)==Ternary.TRUE) {
 				List<Type> actuals = getActualParameterTypes();
 				List<Type> formals = decl.header().getParameterTypes();
 				if (new MoreSpecificTypesOrder().contains(actuals, formals)) {
 						result = decl;
 				}
 			}
 			}
       return result;
     }
 
     @Override
     public WeakPartialOrder<NormalMethod> order() {
       return new WeakPartialOrder<NormalMethod>() {
         @Override
         public boolean contains(NormalMethod first, NormalMethod second)
             throws LookupException {
           return new MoreSpecificTypesOrder().contains(first.header().getParameterTypes(), second.header().getParameterTypes());
         }
       };
     }
 
 		@Override
 		public Class<NormalMethod> selectedClass() {
 			return NormalMethod.class;
 		}
   }
   
 	@Override
 	public DeclarationSelector<NormalMethod> selector() {
 		return new ConstructorSelector();
 	}
 
 	public List<? extends Type> declarations() throws LookupException {
 		List<Type> result = new ArrayList<Type>();
 		if(getAnonymousInnerType() != null) {
 			result.add(getAnonymousInnerType());
 		}
 		return result;
 	}
 
 	public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 		return selector.selection(declarations());
 	}
 
 }
