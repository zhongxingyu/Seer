 package chameleon.core.type;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.rejuse.java.collections.TypeFilter;
 import org.rejuse.logic.ternary.Ternary;
 import org.rejuse.predicate.TypePredicate;
 
 import chameleon.core.Config;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.DeclarationContainer;
 import chameleon.core.declaration.Definition;
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.declaration.TargetDeclaration;
 import chameleon.core.element.ChameleonProgrammerException;
 import chameleon.core.element.Element;
 import chameleon.core.language.ObjectOrientedLanguage;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LocalLookupStrategy;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.lookup.LookupStrategySelector;
 import chameleon.core.member.FixedSignatureMember;
 import chameleon.core.member.Member;
 import chameleon.core.modifier.Modifier;
 import chameleon.core.namespace.NamespaceOrType;
 import chameleon.core.statement.CheckedExceptionList;
 import chameleon.core.statement.ExceptionSource;
 import chameleon.core.type.generics.TypeParameter;
 import chameleon.core.type.inheritance.InheritanceRelation;
 
 /**
  * <p>A class representing types in object-oriented programs.</p>
  *
  * <p>A class contains <a href="Member.html">members</a> as its content.</p>
  *
  * @author Marko van Dooren
  */
 public abstract class Type extends FixedSignatureMember<Type,DeclarationContainer,SimpleNameSignature,Type> 
                 implements TargetDeclaration<Type,DeclarationContainer,SimpleNameSignature,Type>, 
                            NamespaceOrType<Type,DeclarationContainer,SimpleNameSignature,Type>, 
                            VariableOrType<Type,DeclarationContainer,SimpleNameSignature,Type>, 
                            Definition<Type,DeclarationContainer,SimpleNameSignature,Type>,
                            Cloneable, 
                            ExceptionSource<Type,DeclarationContainer>, 
                            DeclarationContainer<Type,DeclarationContainer> {
  
 	
 	private List<? extends Declaration> _declarationCache = null;
 	
 	private List<? extends Declaration> declarationCache() {
 		if(_declarationCache != null && Config.CACHE_DECLARATIONS) {
 		  return new ArrayList<Declaration>(_declarationCache);
 		} else {
 			return null;
 		}
 	}
 	
 	private void setDeclarationCache(List<? extends Declaration> cache) {
 		if(Config.CACHE_DECLARATIONS) {
 		  _declarationCache = new ArrayList<Declaration>(cache);
 		}
 	}
 	
 	
     /**
      * Initialize a new Type.
      *
      * @param name    The short name of the new type.
      * @param access  The access modifier.
      * @param context The context of the new type. This will determine the lookup used by the new type.
      */
    /*@
      @ public behavior
      @
      @ pre name != null;
      @ pre access != null;
      @ pre context != null;
  	   @
      @ post getName() == name;
      @ post getAccessModifier() == access;
      @ post getMemberContext() == context;
      @ post getParent() == null;
      @*/
     public Type(SimpleNameSignature sig) {
         setSignature(sig);
     }
     
 
   	/********
   	 * NAME *
   	 ********/
 
   	/*@
   	 @ public behavior
   	 @
   	 @ post \result != null;
   	 @*/
   	public String getName() {
   		return signature().name();
   	}
 
     /**
      * Return the fully qualified name.
      */
    /*@
      @ public behavior
      @
      @ getPackage().getFullyQualifiedName().equals("") ==> \result == getName();
      @ ! getPackage().getFullyQualifiedName().equals("") == > \result.equals(getPackage().getFullyQualifiedName() + getName());
      @*/
     public String getFullyQualifiedName() {
         String prefix;
         Type nearest = nearestAncestor(Type.class);
         if(nearest != null) {
         	prefix = nearest.getFullyQualifiedName();
         } else {
           prefix = getNamespace().getFullyQualifiedName();
         }
         return (prefix.equals("") ? "" : prefix+".")+getName();
     }
 
     /*******************
      * LEXICAL CONTEXT *
      *******************/
     
     
     public LookupStrategy targetContext() {
     	return language().lookupFactory().createTargetLookupStrategy(this);
     }
     
     public LookupStrategy localStrategy() {
     	return targetContext();
     }
     
     /**
      * If the given element is an inheritance relation, the lookup must proceed to the parent. For other elements,
      * the context is a lexical context connected to the target context to perform a local search.
      * @throws LookupException 
      */
     public LookupStrategy lexicalLookupStrategy(Element element) throws LookupException {
     	if(inheritanceRelations().contains(element)) {
     		DeclarationContainer parent = parent();
     		if(parent != null) {
     			return lexicalParametersLookupStrategy();
 //    		  return parent().lexicalContext(this);
     		} else {
     			throw new LookupException("Parent of type is null when looking for the parent context.");
     		}
     	} else {
     	  return lexicalMembersLookupStrategy();
     	  
     	  //language().lookupFactory().createLexicalContext(this,targetContext());
     	}
     }
     
     private LookupStrategy lexicalMembersLookupStrategy() {
     	LookupStrategy result = _lexicalMembersLookupStrategy;
     	if(result == null) {
     		_lexicalMembersLookupStrategy = language().lookupFactory().createLexicalLookupStrategy(targetContext(), this, 
     			new LookupStrategySelector(){
 					
 						public LookupStrategy strategy() throws LookupException {
 	    	  		return lexicalParametersLookupStrategy();
 						}
 					}); 
     		result = _lexicalMembersLookupStrategy;
     	}
     	return result;
     }
     
     private LookupStrategy _lexicalMembersLookupStrategy;
     
     private LookupStrategy lexicalParametersLookupStrategy() {
     	LookupStrategy result = _lexicalParametersLookupStrategy;
     	if(result == null) {
     		_lexicalParametersLookupStrategy = language().lookupFactory().createLexicalLookupStrategy(_localInheritanceLookupStrategy, this);
     		result = _lexicalParametersLookupStrategy;
     	}
     	return result;
     }
     
     private LookupStrategy _lexicalParametersLookupStrategy;
     
     private LocalInheritanceLookupStrategy _localInheritanceLookupStrategy = new LocalInheritanceLookupStrategy(this);
     
   	protected class LocalInheritanceLookupStrategy extends LocalLookupStrategy<Type> {
   	  public LocalInheritanceLookupStrategy(Type element) {
   			super(element);
   		}
 
   	  @Override
   	  @SuppressWarnings("unchecked")
   	  public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
   	    return selector.selection(parameters());
   	  }
   	}
 
   	public abstract List<TypeParameter> parameters();
   	
   	public abstract void addParameter(TypeParameter parameter);
   	
   	public abstract void replaceParameter(TypeParameter oldParameter, TypeParameter newParameter);
 
     /************************
      * BEING A TYPE ELEMENT *
      ************************/
     
     public List<Member> getIntroducedMembers() {
       List<Member> result = new ArrayList<Member>();
       result.add(this);
       return result;
     }
     
     public boolean complete() {
     	List<Member> members = directlyDeclaredMembers(Member.class);
     	// Only check for actual definitions
     	new TypePredicate<Element,Definition>(Definition.class).filter(members);
     	Iterator<Member> iter = members.iterator();
     	boolean result = true;
     	while(result && iter.hasNext()) {
     		result = result && iter.next().is(language(ObjectOrientedLanguage.class).DEFINED) == Ternary.TRUE;
     	}
       return false;
     }
     
     /**********
      * ACCESS *
      **********/
 
     public Type getTopLevelType() {
         // FIXME: BAD design !!!
         if (parent() instanceof Type) {
             return ((Type)parent()).getTopLevelType();
         } else {
             return this;
         }
     }
 
 
     public Type getType() {
         return this;
     }
 
   	/***********
   	 * MEMBERS *
   	 ***********/
 
 //  	public OrderedReferenceSet<Type, TypeElement> getMembersLink() {
 //  		return _elements;
 //  	}
 
 
     /**
      * Add the given element to this type.
      * 
      * @throws ChameleonProgrammerException
      *         The given element could not be added. E.g when you try to add
      *         an element to a computed type.
      */
   	public abstract void add(TypeElement element) throws ChameleonProgrammerException;
   	
   	public void addAll(Collection<? extends TypeElement> elements) throws ChameleonProgrammerException {
   		for(TypeElement element: elements) {
   			add(element);
   		}
   	}
 
     /**************
      * SUPERTYPES *
      **************/
 
     public List<Type> getDirectSuperTypes() throws LookupException {
             final ArrayList<Type> result = new ArrayList<Type>();
             for(InheritanceRelation element:inheritanceRelations()) {
               Type type = element.superType();
               if (type!=null) {
                 result.add(type);
               }
             }
             return result;
     }
 
     public List<Type> getDirectSuperClasses() throws LookupException {
       final ArrayList<Type> result = new ArrayList<Type>();
       for(InheritanceRelation element:inheritanceRelations()) {
         result.add(element.superClass());
       }
       return result;
 }
 
     protected void accumulateAllSuperTypes(Set<Type> acc) throws LookupException {
     	List<Type> temp =getDirectSuperTypes();
     	acc.addAll(temp);
     	for(Type type:temp) {
     		type.accumulateAllSuperTypes(acc);
     	}
     }
     
     public Set<Type> getAllSuperTypes() throws LookupException {
     	Set result = new HashSet();
     	accumulateAllSuperTypes(result);
     	return result;
     }
 
     
     //TODO: rename to properSubTypeOf
     
     public boolean subTypeOf(Type other) throws LookupException {
     	return language(ObjectOrientedLanguage.class).subtypeRelation().contains(this, other);
 //    	  Collection superTypes = getAllSuperTypes(); 
 //        return superTypes.contains(other);
     }
     
     /**
      * Check if this type equals the given other type. This is
      * a unidirectional check to keep things extensible. It is fine
      * if equals(other) is false, but other.equals(this) is true.
      *  
      * @param other
      * @return
      */
     public boolean uniEqualTo(Type other) {
     	return other == this;
     }
     
    /*@
      @ public behavior
      @
      @ post ! other instanceof Type ==> \result == false
      @ post other instanceof Type ==> \result == equalTo(other) || other.equalTo(this); 
      @*/
     public boolean equals(Object other) {
     	boolean result = false;
     	if(other instanceof Type) {
     		result = uniEqualTo((Type)other) || ((Type)other).uniEqualTo(this);
     	}
     	return result;
     }
 
     /**
      * Check if this type is assignable to another type.
      * 
      * @param other
      * @return
      * @throws LookupException
      */
    /*@
      @ public behavior
      @
      @ post \result == equals(other) || subTypeOf(other);
      @*/
     public boolean assignableTo(Type other) throws LookupException {
     	boolean equal = equals(other);
     	boolean subtype = subTypeOf(other);
     	return (equal || subtype);
     }
 
     
   	/**
   	 * Return the inheritance relations of this type.
   	 */
    /*@
      @ public behavior
      @
      @ post \result != null;
      @*/
   	public abstract List<InheritanceRelation> inheritanceRelations();
 
   	/**
   	 * Add the give given inheritance relation to this type.
   	 * @param type
   	 * @throws ChameleonProgrammerException
   	 *         It is not possible to add the given type. E.g. you cannot
   	 *         add an inheritance relation to a computed type.
   	 */
    /*@
      @ public behavior
      @
      @ pre relation != null;
      @ post inheritanceRelations().contains(relation);
      @*/
   	public abstract void addInheritanceRelation(InheritanceRelation relation) throws ChameleonProgrammerException;
     
   	/**
   	 * Remove the give given inheritance relation from this type.
   	 * @param type
   	 * @throws ChameleonProgrammerException
   	 *         It is not possible to remove the given type. E.g. you cannot
   	 *         remove an inheritance relation to a computed type.
   	 */
    /*@
      @ public behavior
      @
      @ pre relation != null;
      @ post ! inheritanceRelations().contains(relation);
      @*/
   	public abstract void removeInheritanceRelation(InheritanceRelation relation) throws ChameleonProgrammerException;
   	
     /**
      * Return the members of the given kind directly declared by this type.
      * @return
      */
     public <T extends Member> List<T> directlyDeclaredMembers(final Class<T> kind) {
       return (List<T>) new TypeFilter(kind).retain(directlyDeclaredMembers());
     }
     
     /**
      * Return the members directly declared by this type. The order of the elements in the list is the order in which they
      * are written in the type.
      * @return
      */
     public abstract List<Member> directlyDeclaredMembers();
     
     public <D extends Member> List<D> members(DeclarationSelector<D> selector) throws LookupException {
 
   		// 1) All defined members of the requested kind are added.
   		final List<D> result = new ArrayList(directlyDeclaredMembers(selector));
 
   		// 2) Fetch all potentially inherited members from all inheritance relations
   		for (InheritanceRelation rel : inheritanceRelations()) {
   				rel.accumulateInheritedMembers(selector, result);
   		}
   		return selector.selection(result);
     }
     
     @SuppressWarnings("unchecked")
     public abstract <D extends Member> List<D> directlyDeclaredMembers(DeclarationSelector<D> selector) throws LookupException;
 
     public List<Member> members() throws LookupException {
       return members(Member.class);
     }
     
 //    public <M extends Member> Set<M> potentiallyInheritedMembers(final Class<M> kind) throws MetamodelException {
 //  		final Set<M> result = new HashSet<M>();
 //			for (InheritanceRelation rel : inheritanceRelations()) {
 //				result.addAll(rel.potentiallyInheritedMembers(kind));
 //			}
 //  		return result;
 //    }
 //
     /**
      * Return the members of this class.
      * @param <M>
      * @param kind
      * @return
      * @throws LookupException
      */
     public <M extends Member> List<M> members(final Class<M> kind) throws LookupException {
 
 		// 1) All defined members of the requested kind are added.
 		final List<M> result = new ArrayList(directlyDeclaredMembers(kind));
 
 		// 2) Fetch all potentially inherited members from all inheritance relations
 		for (InheritanceRelation rel : inheritanceRelations()) {
 				rel.accumulateInheritedMembers(kind, result);
 		}
 		return result;
 	}
 
 //    public RegularMethod getApplicableRegularMethod(final String name, final List paramTypes) throws MetamodelException {
 //        return (RegularMethod)getApplicableMethod(name, paramTypes, RegularMethod.class);
 //    }
 //
 //    public PrefixOperator getApplicablePrefixOperator(final String name, final List paramTypes) throws MetamodelException {
 //        return (PrefixOperator)getApplicableMethod(name, paramTypes, PrefixOperator.class);
 //    }
 //
 //    public PostfixOperator getApplicablePostfixOperator(final String name, final List paramTypes) throws MetamodelException {
 //        return (PostfixOperator)getApplicableMethod(name, paramTypes, PostfixOperator.class);
 //    }
 //
 //    public InfixOperator getApplicableInfixOperator(final String name, final List paramTypes) throws MetamodelException {
 //        return (InfixOperator)getApplicableMethod(name, paramTypes, InfixOperator.class);
 //    }
 
 //    /**
 //     * Return the method with the given name and type that is applicable to the given list of parameter types.
 //     * The type is for example a regular method, or a prefix, infix, or postfix method.
 //     *
 //     * @param name       The name of the requested method
 //     * @param paramTypes The list of types of the parameters supplied to the method.
 //     * @param type       The type of the requested method.
 //     */
 //    /*@
 //    @ public behavior
 //    @
 //    @ pre name != null;
 //    @ pre paramTypes != null;
 //    @ pre type != null;
 //    @*/
 //    public Method getApplicableMethod(final String name, final List paramTypes, Class type) throws MetamodelException {
 //        final Collection<Method> methods = getAllMethodsSimple(type);
 //        // filter on name
 //        // filter on number of parameters.
 //        final int nbParams = paramTypes.size();
 ////        new PrimitiveTotalPredicate() {
 ////            public boolean eval(Object o) {
 ////                return ((Method)o).getName().equals(name) && (((Method)o).getNbParameters()==nbParams);
 ////            }
 ////        }.filter(methods);
 //
 //        // filter out those to which the parameters do not apply
 //        Iterator<Method> methodListIterator = methods.iterator();
 //        while (methodListIterator.hasNext()) {
 //        	Method method = methodListIterator.next();
 //        	if((! method.getName().equals(name)) || (method.getNbParameters()!=nbParams)) {
 //        		methodListIterator.remove();
 //        		// go to the next method in the list
 //        		continue;
 //        	}
 //        	
 //        	List<FormalParameter> params = method.getParameters();
 //        	for (int i = 0; i < nbParams ; i++) {
 //        		Type param = (Type) paramTypes.get(i);
 //        		Type methodParamType = params.get(i).getType();
 //        		if (!param.assignableTo(methodParamType)) {
 //        			methodListIterator.remove();
 //        			// break from the loop when we encounter the first parameter that does not match.
 //        			break;
 //        		}
 //        	}
 //        }
 //
 //
 //        Set result = filter(methods);
 //
 //        if (result.size()==0) {
 //            return null;
 //        } else if (result.size()==1) {
 //            return (Method)result.iterator().next();
 //        } else {
 //            // It is possible that there isn't an implementation yet because this type is abstract, and
 //            // there are multiple abstract definitions of the given method in supertypes. If all remaining
 //            // methods have the same signature, we can pick any one of them.
 //            final Method nc = (Method)result.iterator().next();
 //            try {
 //                new PrimitivePredicate() {
 //                    public boolean eval(Object o) throws MetamodelException {
 //                        return (o==nc) || (! nc.sameSignatureAs((Method)o));
 //                    }
 //                }.filter(result);
 //            } catch (MetamodelException e1) {
 //                throw e1;
 //            } catch (Exception e1) {
 //                e1.printStackTrace();
 //                throw new Error();
 //            }
 //            if (result.size()==1) {
 //                return (Method)result.iterator().next();
 //            } else {
 //                throw new MetamodelException("Multiple matches");
 //            }
 //        }
 //    }
 
 //    /**
 //     * MODIFIES THE GIVEN COLLECTION !!!
 //     */
 //    protected Set filter(Collection methods) throws MetamodelException {
 //        filterOverriddenMethods(methods);
 //        return filterGeneralMethods(methods);
 //    }
 //
 //    /**
 //     * filter out methods that will not be selected
 //     * because there is a second method for which there
 //     * is at least 1 parameter that has a type which
 //     * is a subtype of the corresponding parameter of
 //     * the first method and the other arguments of the second
 //     * method are assignable to the parameters of the first
 //     * method.
 //     */
 //    protected Set filterGeneralMethods(Collection methods) throws MetamodelException {
 //        Set result = new HashSet();
 //        Iterator iter = methods.iterator();
 //        while (iter.hasNext()) {
 //            Method method = (Method)iter.next();
 //            Iterator inner = methods.iterator();
 //            boolean obsolete = false;
 //            while (inner.hasNext()) {
 //                Method other = (Method)inner.next();
 //                if (other!=method) {
 //                    boolean hasCompat = false;
 //                    boolean hasIncompat = false;
 //                    Iterator outerParams = method.getParameters().iterator();
 //                    Iterator innerParams = other.getParameters().iterator();
 //                    while (outerParams.hasNext()) {
 //                        Type outerType = ((FormalParameter)outerParams.next()).getType();
 //                        Type innerType = ((FormalParameter)innerParams.next()).getType();
 //                        if (innerType.assignableTo(outerType)) {
 //                            hasCompat = true;
 //                        } else {
 //                            hasIncompat = true;
 //                        }
 //                    }
 //                    // If 'other' can implement 'method', then
 //                    // 'method' is obsolete.
 //                    if ((hasCompat && (! hasIncompat)) || other.canImplement(method)) {
 //                        obsolete = true;
 //                    }
 //                }
 //            }
 //            if (! obsolete) {
 //                result.add(method);
 //            }
 //        }
 //        return result;
 //    }
 //
 //    /**
 //     *
 //     */
 //    protected void filterOverriddenMethods(final Collection methods) throws MetamodelException {
 //        try {
 ////    	 ... but remove the ones that are overridden, implemented or hidden.
 //            new PrimitivePredicate() {
 //                public boolean eval(Object o) throws Exception {
 //                    final Method superMethod = (Method)o;
 //                    return ! new PrimitivePredicate() {
 //                        public boolean eval(Object o2) throws MetamodelException {
 //                            Method nc = (Method)o2;
 //                            return (nc!=superMethod) && (nc.getName().equals(superMethod.getName())) && (nc.overrides(superMethod) || nc.canImplement(superMethod) || nc.hides(superMethod));
 //                        }
 //                    }.exists(methods);
 //                }
 //            }.filter(methods);
 //        } catch (MetamodelException e1) {
 //            throw e1;
 //        } catch (Exception e1) {
 //            throw new Error();
 //        }
 //    }
 
 //    public RegularMethod getApplicableSuperRegularMethod(final String name, final List paramTypes) throws MetamodelException {
 //        return (RegularMethod)getApplicableSuperMethod(name, paramTypes, RegularMethod.class);
 //    }
 //
 //    public PrefixOperator getApplicableSuperPrefixOperator(final String name, final List paramTypes) throws MetamodelException {
 //        return (PrefixOperator)getApplicableSuperMethod(name, paramTypes, PrefixOperator.class);
 //    }
 //
 //    public InfixOperator getApplicableSuperInfixOperator(final String name, final List paramTypes) throws MetamodelException {
 //        return (InfixOperator)getApplicableSuperMethod(name, paramTypes, InfixOperator.class);
 //    }
 //
 //    public PostfixOperator getApplicableSuperPostfixOperator(final String name, final List paramTypes) throws MetamodelException {
 //        return (PostfixOperator)getApplicableSuperMethod(name, paramTypes, PostfixOperator.class);
 //    }
 //
 //    protected Method getApplicableSuperMethod(final String name, final List paramTypes, final Class type) throws MetamodelException {
 //        List superTypes = getDirectSuperTypes();
 //        final List methods = new ArrayList();
 //        try {
 //            new RobustVisitor() {
 //                public Object visit(Object element) throws MetamodelException {
 //                    Method method = ((Type)element).getApplicableMethod(name, paramTypes, type);
 //                    if (method!=null) {
 //                        methods.add(method);
 //                    }
 //                    return null;
 //                }
 //
 //                public void unvisit(Object element, Object undo) {
 //                }
 //            }.applyTo(superTypes);
 //        } catch (MetamodelException e) {
 //            throw e;
 //        } catch (Exception e) {
 //            throw new Error();
 //        }
 //        //filter
 //
 //        Set filtered = filter(methods);
 //
 //        if (filtered.isEmpty()) {
 //            return null;
 //        } else if (filtered.size()==1) {
 //            return (Method)filtered.iterator().next();
 //        } else {
 //            throw new MetamodelException("Cannot find super method "+name);
 //        }
 //    }
 
     /****************
      * CONSTRUCTORS *
      ****************/
 //
 //    /**
 //     * Return the constructors of this type.
 //     */
 //    /*@
 //    @ public behavior
 //    @
 //    @ post \result != null;
 //    @ // The result only contains methods from this type.
 //    @ post (\forall Method m; \result.contains(m); getMethods().contains(m));
 //    @ // The result contains all methods of this type with the Constructor modifier.
 //    @ post (\forall Method m; getMethods().contains(m);
 //    @         m.is(Constructor.PROTOTYPE) ==> \result.contains(m));
 //    @*/
 //    public List getConstructors() {
 //        List result = getMembersLink().getOtherEnds();
 //        new PrimitiveTotalPredicate() {
 //            public boolean eval(Object o) {
 //                return (o instanceof RegularMethod) && (((RegularMethod)o).hasModifier(new Constructor()));
 //            }
 //        }.filter(result);
 //        return result;
 //    }
 
 //    public RegularMethod getConstructor(final List paramTypes) throws MetamodelException {
 //        List constructors = getConstructors();
 //        // filter on name
 //        // filter on number of parameters.
 //        final int nbParams = paramTypes.size();
 //        new PrimitiveTotalPredicate() {
 //            public boolean eval(Object o) {
 //                return (((RegularMethod)o).getNbParameters()==nbParams);
 //            }
 //        }.filter(constructors);
 //        // eliminate methods to which the arguments are not applicable
 //        try {
 //            new PrimitivePredicate() {
 //                public boolean eval(Object o) throws MetamodelException {
 //                    RegularMethod constructor = (RegularMethod)o;
 //                    List params = constructor.getParameters();
 //                    boolean ok = true;
 //                    for (int i = 0;i<nbParams;i++) {
 //                        Type param = (Type)paramTypes.get(i);
 //                        Type methodParamType = (Type)(((FormalParameter)params.get(i)).getType());
 //                        if (methodParamType==null) {
 //                            ((FormalParameter)params.get(i)).getType();
 //                        }
 //                        if (!param.assignableTo(methodParamType)) {
 //                            ok = false;
 //                        }
 //                    }
 //                    return ok;
 //                }
 //            }.filter(constructors);
 //        } catch (MetamodelException e) {
 //            throw e;
 //        } catch (Exception e) {
 //            e.printStackTrace();
 //            throw new Error();
 //        }
 //
 //        Set result = filterGeneralMethods(constructors);
 //
 //        if (result.size()==0) {
 //            return null;
 //        } else if (result.size()==1) {
 //            return (RegularMethod)result.iterator().next();
 //        } else {
 //            throw new MetamodelException();
 //        }
 //    }
 
 //    public RegularMethod getSuperConstructor(final List paramTypes) throws MetamodelException {
 //        List superTypes = getDirectSuperTypes();
 //        final List constructors = new ArrayList();
 //        try {
 //            new RobustVisitor() {
 //                public Object visit(Object element) throws MetamodelException {
 //                    RegularMethod constructor = ((Type)element).getConstructor(paramTypes);
 //                    if (constructor!=null) {
 //                        constructors.add(constructor);
 //                    }
 //                    return null;
 //                }
 //
 //                public void unvisit(Object element, Object undo) {
 //                }
 //            }.applyTo(superTypes);
 //        } catch (MetamodelException e) {
 //            throw e;
 //        } catch (Exception e) {
 //            throw new Error();
 //        }
 //        if (constructors.isEmpty()) {
 //            return null;
 //        } else if (constructors.size()==1) {
 //            return (RegularMethod)constructors.get(0);
 //        } else {
 //            throw new MetamodelException();
 //        }
 //    }
 
 
     /*@
     @ also public behavior
     @
     @ post \result == this;
     @*/
     public Type getNearestType() {
         return this;
     }
 
 
 //    public NamespacePart getNearestNamespacePart() {
 //        return parent().getNearestNamespacePart();
 //    }
 
 //    /**
 //     * @param string
 //     * @return
 //     */
 //    public VariableOrType getVariableOrType(String name) throws MetamodelException {
 //        VariableOrType result;
 //        result = getVariable(name);
 //        if (result!=null) {
 //            return result;
 //        } else {
 //            return getType(name);
 //        }
 //    }
 
     public abstract Type clone();
 
 //    protected abstract Type cloneThis();
 
    /*@
      @ also public behavior
      @
      @ post \result.containsAll(getSuperTypeReferences());
      @ post \result.containsAll(getMembers());
      @ post \result.containsAll(getModifiers());
      @*/
     public List<? extends Element> children() {
         List<Element> result = new ArrayList<Element>();
         result.addAll(inheritanceRelations());
         result.addAll(modifiers());
         result.addAll(directlyDeclaredElements());
         return result;
     }
 
     /**
      * DO NOT CONFUSE THIS METHOD WITH directlyDeclaredMembers. This method does not
      * transform type elements into members.
      * 
      * @return
      */
     public abstract List<? extends TypeElement> directlyDeclaredElements();
 
     /********************
      * EXCEPTION SOURCE *
      ********************/
 
     public CheckedExceptionList getCEL() throws LookupException {
         CheckedExceptionList cel = new CheckedExceptionList();
         for(TypeElement el : directlyDeclaredMembers()) {
         	cel.absorb(el.getCEL());
         }
         return cel;
     }
 
     public CheckedExceptionList getAbsCEL() throws LookupException {
       CheckedExceptionList cel = new CheckedExceptionList();
       for(TypeElement el : directlyDeclaredMembers()) {
       	cel.absorb(el.getAbsCEL());
       }
       return cel;
     }
 
     public List<? extends Declaration> declarations() throws LookupException {
     	List<? extends Declaration> result = declarationCache();
     	if(result == null) {
     		result = members();
     		setDeclarationCache(result);
     	}
     	return result;
     }
     public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
     	return (List<D>) members((DeclarationSelector<? extends Member>)selector);
     }
 
   	protected void copyContents(Type from) {
   		for(InheritanceRelation relation : from.inheritanceRelations()) {
         addInheritanceRelation(relation.clone());
   		}
       for(Modifier mod : from.modifiers()) {
       	addModifier(mod.clone());
       }
      for(TypeElement el : from.directlyDeclaredMembers()) {
         add(el.clone());
       }
       for(TypeParameter par : from.parameters()) {
       	addParameter(par.clone());
       }
   	}
   
   	public Type alias(SimpleNameSignature sig) {
       return new TypeAlias(sig,this);
   	}
 
   	public Type intersection(Type type) throws LookupException {
   		return type.intersectionDoubleDispatch(type);
   	}
   	
   	protected Type intersectionDoubleDispatch(Type type) throws LookupException {
   		Type result;
   		if(type.subTypeOf(this)) {
   			result = type;
   		} else if (subTypeOf(type)) {
   			result = this;
   		} else {
   		  result = new IntersectionType(this,type);
   		}
   		return result;
   	}
 
   	protected Type intersectionDoubleDispatch(IntersectionType type) {
   		IntersectionType result = type.clone();
   		result.addType(type);
   		return result;
   	}
 
 		public abstract void replace(TypeElement oldElement, TypeElement newElement);
 
 		public abstract Type baseType();
 
 }
 
 
