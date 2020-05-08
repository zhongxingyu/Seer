 package chameleon.core.type.inheritance;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.rejuse.association.Reference;
 import org.rejuse.logic.ternary.Ternary;
 import org.rejuse.predicate.PrimitivePredicate;
 
 import chameleon.core.declaration.Signature;
 import chameleon.core.declaration.StubDeclarationContainer;
 import chameleon.core.element.ChameleonProgrammerException;
 import chameleon.core.element.Element;
 import chameleon.core.element.ElementImpl;
 import chameleon.core.lookup.DummyLookupStrategy;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.member.Member;
 import chameleon.core.type.Type;
 import chameleon.core.type.TypeReference;
 
 public abstract class InheritanceRelation<E extends InheritanceRelation> extends ElementImpl<E,Type> {
 	
 	private static Logger logger = Logger.getLogger("lookup.inheritance");
 	
 	public Logger lookupLogger() {
 		return logger;
 	}
 	
 	/**
 	 * Asks the context factory to create a context. The local lookup strategy by default is a
 	 * dummy lookup strategy because an inheritance relation typically does not contain declarations.
 	 * 
 	 * The lookup is intercepted at this point because for example Java has funny lookup rules with
 	 * respect to inheritance.
 	 */
 	public LookupStrategy lexicalContext(Element element) {
 		return language().lookupFactory().createLexicalContext(this, new DummyLookupStrategy());
 	}
 	
 	public abstract E clone();
 
 	public InheritanceRelation(TypeReference ref) {
 		setSuperClassReference(ref);
 	}
 	
 	public List<? extends Element> children() {
 		List<Element> result = new ArrayList<Element>();
 		result.add(superClassReference());
 		return result;
 	}
 	
 	/**
 	 * Return the inherited class.
 	 * @return
 	 * @throws LookupException
 	 */
  /*@
    @ public behavior
    @
    @ post \result == superClassReference().getType();
    @*/
 	public Type superClass() throws LookupException {
 			lookupLogger().debug("Inheritance relation of class "+parent().getFullyQualifiedName()+" is going to look up super class " + superClassReference().getFullyQualifiedName());
 			Type result = null;
 			try {
 		    result = superClassReference().getType();
 			} 
 			catch(NullPointerException exc) {
 				if(superClassReference() == null) {
 				  throw new ChameleonProgrammerException("trying to get the super class of an inheritance relation that points to null in class" + parent().getFullyQualifiedName(),exc);
 				} else {
 					throw exc;
 				}
 			}
 		  if(result != null) {
 		  	return result;
 		  } else {
 		  	throw new LookupException("Superclass is null",superClassReference());
 		  }
 	}
 	
 	/**
 	 * Return the inherited type, if this relation also introduces a subtype relation.
 	 * @return
 	 * @throws LookupException
 	 */
 	public abstract Type superType() throws LookupException;
 	
 	/**
 	 * Return a reference to the inherited class.
 	 * @return
 	 */
 	public TypeReference superClassReference() {
     return _superClass.getOtherEnd();
 	}
 	
  /*@
    @ public behavior
    @
    @ post superClassReference() == ref;
    @*/
 	public void setSuperClassReference(TypeReference ref) {
 		if(ref != null) {
 			_superClass.connectTo(ref.parentLink());
 		} else {
 			_superClass.connectTo(null);
 		}
 	}
 	
 	/**
 	 * Return the set of members inherited through this inheritance relation.
 	 * @param <M>
 	 * @param <S>
 	 * @param <F>
 	 * @param kind
 	 * @return
 	 * @throws LookupException
 	 */
 	public <M extends Member<M,? super Type,S,F>, S extends Signature<S,M>, F extends Member<? extends Member,? super Type,S,F>> 
   Set<M> inheritedMembers(final Class<M> kind) throws LookupException {
 		final Set<M> result = potentiallyInheritedMembers(kind);
 		result.addAll(parent().directlyDeclaredElements(kind));
 		try {
 			new PrimitivePredicate<M>() {
 				public boolean eval(final M superMember) throws Exception {
 					return !new PrimitivePredicate<M>() {
 						public boolean eval(M nc) throws LookupException {
 							return nc.overrides(superMember) || nc.hides(superMember) || ((superMember != nc)&&(superMember.equivalentTo(nc)));
 						}
 					}.exists(result);
 				}
 			}.filter(result);
 		} catch (RuntimeException e) {
 			throw e;
 		} catch (Error e) {
 			throw e;
 		} catch (LookupException e) {
 			throw e;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Error(e);
 		}
 		return result;
 	}
 	public <M extends Member<M,? super Type,S,F>, S extends Signature<S,M>, F extends Member<? extends Member,? super Type,S,F>> 
 	        Set<M> potentiallyInheritedMembers(final Class<M> kind) throws LookupException {
 		Set<M> superMembers = superClass().members(kind);
 		Set<M> result = new HashSet<M>();
 		// The stub container will contain all elements to enable binding to 
 		// members that are not inherited.
//		StubDeclarationContainer stub = new StubDeclarationContainer();
//		stub.setUniParent(parent());
 		for(M member:superMembers) {
 	    Ternary temp = member.is(language().INHERITABLE);
 	    if (temp == Ternary.UNKNOWN) {
 	    	temp = member.is(language().INHERITABLE);
 	      throw new LookupException(
 	          "For one of the members of super type "
 	              + superClass().getFullyQualifiedName()
 	              + " it is unknown whether it is inheritable or not. Member type: "+member.getClass().getName());
 	    } else {
 	    	// Even if the member is not inheritable, we must still add it
 	    	// to the stub declaration container because the implementation
 	    	// of inherited members may reference them.
 //	    	M transformed = transform(member);
 //	    	stub.add(transformed);
 	    	if (temp == Ternary.TRUE) {
 //		      result.add(transformed);
 	    		result.add(member);
 		    }
 	    }
 		}
     return result;
 	}
 	
 //	public <M extends Member<M,? super Type,S,F>, S extends Signature<S,M>, F extends Member<? extends Member,? super Type,S,F>> 
 //	        M transform(M member) throws MetamodelException {
 //		M result = member.clone();
 //		// 1) SUBSTITUTE GENERIC PARAMETERS, OR USE TRICK CONTAINER?
 //		//   1.a) WE NEED A TRICK CONTAINER (The superclass) IN WHICH THE PARAMETERS ARE SUBSTITUTED
 //	}
 	
 	private Reference<InheritanceRelation,TypeReference> _superClass = new Reference<InheritanceRelation, TypeReference>(this);
 
 }
