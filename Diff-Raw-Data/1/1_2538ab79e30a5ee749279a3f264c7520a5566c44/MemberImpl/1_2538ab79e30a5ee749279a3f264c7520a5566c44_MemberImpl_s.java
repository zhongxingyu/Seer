 package chameleon.core.member;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.Signature;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.property.ChameleonProperty;
 import chameleon.core.scope.Scope;
 import chameleon.core.scope.ScopeProperty;
 import chameleon.exception.ChameleonProgrammerException;
 import chameleon.exception.ModelException;
 import chameleon.oo.language.ObjectOrientedLanguage;
 import chameleon.oo.type.Type;
 import chameleon.oo.type.TypeElementImpl;
 
 public abstract class MemberImpl<E extends Member<E,S,F>,S extends Signature, F extends Member> extends TypeElementImpl<E> implements Member<E,S,F>{
 
   /**
    * Return the signature of this member.
    */
   public abstract S signature();
   
   public final boolean overrides(Member other) throws LookupException {
 //  	return overridesSelector().selects(other);
   	return overridesRelation().contains(this,other);
 
   	//  	return overriddenMembers().contains(other);
   	
 //  	List<Member> todo = (List<Member>) directlyOverriddenMembers();
 //  	Set<Member> done = new HashSet<Member>();
 //  	while(! todo.isEmpty()) {
 //  		Member<?,?,?> m = todo.get(0);
 //  		todo.remove(0);
 //			if(m.sameAs(other)) {
 //				return true;
 //			}
 //  		if(done.add(m)) {
 //  			todo.addAll(m.directlyOverriddenMembers());
 //  		}
 //  	}
 //  	return false;
   }
   
   public final boolean hides(Member other) throws LookupException {
 	  return ((HidesRelation)hidesRelation()).contains(this,other);
   }
 
   public final boolean canImplement(Member other) throws LookupException {
   	return language(ObjectOrientedLanguage.class).implementsRelation().contains(this,other);
   }
 
   public List<? extends Member> directlyOverriddenMembers() throws LookupException {
     return nearestAncestor(Type.class).membersDirectlyOverriddenBy(overridesSelector());
   }
   
   public List<? extends Member> directlyAliasedMembers() throws LookupException {
     return nearestAncestor(Type.class).membersDirectlyAliasedBy(aliasSelector());
   }
   
   public List<? extends Member> directlyAliasingMembers() throws LookupException {
     return nearestAncestor(Type.class).membersDirectlyAliasing(aliasSelector());
   }
   
   public Set<? extends Member> overriddenMembers() throws LookupException {
   	List<Member> todo = (List<Member>) directlyOverriddenMembers();
   	Map<Type,List<Member>> visitedTypes = new HashMap<Type,List<Member>>();
   	while(! todo.isEmpty()) {
   		Member<?,?,?> m = todo.get(0);
   		todo.remove(0);
   		Type containingType = m.nearestAncestor(Type.class);
 		if(! visitedTypes.containsKey(containingType)) {
 			visitedTypes.put(containingType, new ArrayList<Member>());
 		}
 		List<Member> done = visitedTypes.get(containingType);
 		boolean contains = false;
 		for(Member member:done) {
 			if(member.signature().sameAs(m.signature())) {
 				contains = true;
 				break;
 			}
 		}
 		if(! contains) {
 			done.add(m);
 			todo.addAll(m.directlyOverriddenMembers());
 			todo.addAll(m.aliasedMembers());
 			todo.addAll(m.aliasingMembers());
 		}
   	}
   	Set<Member> result = new HashSet<Member>();
   	for(List<Member> members: visitedTypes.values()) {
   		result.addAll(members);
   	}
   	return result;
   }
   
   public Set<? extends Member> aliasedMembers() throws LookupException {
 	  List<Member> todo = (List<Member>) directlyAliasedMembers();
 	  Set<Member> result = new HashSet<Member>();
 	  while(! todo.isEmpty()) {
 		  Member<?,?,?> m = todo.get(0);
 		  todo.remove(0);
 		  if(result.add(m)) {
 			  todo.addAll(m.directlyAliasedMembers());
 		  }
 	  }
 	  return result;
   }
 
   public Set<? extends Member> aliasingMembers() throws LookupException {
 	  List<Member> todo = (List<Member>) directlyAliasingMembers();
 	  Set<Member> result = new HashSet<Member>();
 	  while(! todo.isEmpty()) {
 		  Member<?,?,?> m = todo.get(0);
 		  todo.remove(0);
 		  if(result.add(m)) {
 			  todo.addAll(m.directlyAliasingMembers());
 		  }
 	  }
 	  return result;
   }
 
   /**
    * Return the member that this member overrides. If there is more than
    * one candidate, an exception is thrown. If there is no candidate, null
    * is returned.
    */
  /*@
    @ public behavior
    @
    @ post directlyOverriddenMembers().size() == 0 ==> \result == null;
    @ post directlyOverriddenMembers().size() == 1 ==> directlyOverriddenMembers().contains(\result);
    @ post directlyOverriddenMembers().size() > 1 ==> false;
    @*/
   public Member overriddenMember() throws LookupException {
   	List<? extends Member> overridden = directlyOverriddenMembers();
   	int size = overridden.size();
   	if(size == 1) {
   		return overridden.iterator().next();
   	} else if (size > 1) {
   		throw new LookupException("There is than one overridden member. Use directlyOverriddenMembers() instead.");
   	} else {
   		return null;
   	}
   }
   
   public Declaration selectionDeclaration() throws LookupException {
   	return this;
   }
   
   public F actualDeclaration() throws LookupException {
   	return (F) this;
   }
   
   public Scope scope() throws ModelException {
   	Scope result = null;
   	ChameleonProperty scopeProperty = property(language().SCOPE_MUTEX);
   	if(scopeProperty instanceof ScopeProperty) {
   		result = ((ScopeProperty)scopeProperty).scope(this);
   	} else if(scopeProperty != null){
   		throw new ChameleonProgrammerException("Scope property is not a ScopeProperty");
   	}
   	return result;
   }
   
   public MemberRelationSelector<? extends Member> overridesSelector() {
 		return new MemberRelationSelector<Member>(Member.class,this,_overridesSelector);
   }
   
   public OverridesRelation<? extends Member> overridesRelation() {
   	return _overridesSelector;
   }
   
   private static OverridesRelation<Member> _overridesSelector = new OverridesRelation<Member>(Member.class) {
 		
 		public boolean containsBasedOnRest(Member first, Member second) throws LookupException {
 			return true;
 		}
 
 		@Override
 		public boolean containsBasedOnName(Signature first, Signature second) {
 			return first.name().equals(second.name());
 		}
 	};
 
   public HidesRelation<? extends Member> hidesRelation() {
 		return _hidesSelector;
   }
   
   private static HidesRelation<Member> _hidesSelector = new HidesRelation<Member>(Member.class) {
 		
 		public boolean containsBasedOnRest(Member first, Member second) throws LookupException {
 			return true;
 		}
 
 	};
 
   public MemberRelationSelector<? extends Member> aliasSelector() {
 		return new MemberRelationSelector<Member>(Member.class,this,_aliasSelector);
   }
 	
   private static DeclarationComparator<Member> _aliasSelector = new DeclarationComparator<Member>(Member.class) {
 		
 		public boolean containsBasedOnRest(Member first, Member second) throws LookupException {
 			return first.signature().sameAs(second.signature());
 		}
 
 		@Override
 		public boolean containsBasedOnName(Signature first, Signature second) {
 			return true;
 		}
 	};
 }
