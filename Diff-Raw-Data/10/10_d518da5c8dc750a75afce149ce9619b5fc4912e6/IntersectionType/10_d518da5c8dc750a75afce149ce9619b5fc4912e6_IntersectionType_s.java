 package be.kuleuven.cs.distrinet.chameleon.oo.type;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import be.kuleuven.cs.distrinet.chameleon.core.declaration.SimpleNameSignature;
 import be.kuleuven.cs.distrinet.chameleon.core.element.Element;
 import be.kuleuven.cs.distrinet.chameleon.core.lookup.DeclarationSelector;
 import be.kuleuven.cs.distrinet.chameleon.core.lookup.LookupException;
 import be.kuleuven.cs.distrinet.chameleon.core.lookup.SelectionResult;
 import be.kuleuven.cs.distrinet.chameleon.core.namespace.Namespace;
 import be.kuleuven.cs.distrinet.chameleon.exception.ChameleonProgrammerException;
 import be.kuleuven.cs.distrinet.chameleon.oo.language.ObjectOrientedLanguage;
 import be.kuleuven.cs.distrinet.chameleon.oo.member.Member;
 import be.kuleuven.cs.distrinet.chameleon.oo.type.generics.TypeParameter;
 import be.kuleuven.cs.distrinet.chameleon.oo.type.inheritance.InheritanceRelation;
 import be.kuleuven.cs.distrinet.chameleon.util.Pair;
 import be.kuleuven.cs.distrinet.rejuse.logic.ternary.Ternary;
 import be.kuleuven.cs.distrinet.rejuse.predicate.AbstractPredicate;
 
 public class IntersectionType extends MultiType {
 	
 	public static Type create(List<Type> types) throws LookupException {
 		if(types.size() == 1) {
 			return types.get(0);
 		} else {
 			Namespace def = types.get(0).view().namespace();
 			IntersectionType result = new IntersectionType(types);
 			result.setUniParent(def);
 			return result;
 		}
 	}
 
 	public IntersectionType(Type first, Type second) throws LookupException {
 		super(createSignature(Arrays.asList(new Type[]{first,second})), Arrays.asList(new Type[]{first,second}));
 	}
 	
 	public IntersectionType(List<Type> types) throws LookupException {
 		super(createSignature(types),types);
 	}
 	
 	@Override
 	public List<Type> getDirectSuperTypes() throws LookupException {
 		return types();
 	}
 
 	public Type intersectionDoubleDispatch(Type type) throws LookupException {
 		return type.intersectionDoubleDispatch(this);
 	}
 
 	public Type intersectionDoubleDispatch(IntersectionType type) throws LookupException {
 		IntersectionType result = clone(this);
 		result.addAll(type);
 		return type;
 	}
 
 	public void addAll(IntersectionType type) throws LookupException {
 		for(Type t: type.types()) {
 			addType(t);
 		}
 	}
 	
 	public void addType(Type type) throws LookupException {
 		for(Type alreadyPresent:types()) {
 			if(alreadyPresent.subTypeOf(type)) {
 				return;
 			}
 			if(type.subTypeOf(alreadyPresent)) {
 				removeType(alreadyPresent);
 			}
 		}
 		// If we reach this place, then no type in the intersection is a subtype of the new type.
 		_types.add(type);
 	}
 	
 	public static SimpleNameSignature createSignature(Collection<Type> types) {
 		StringBuffer name = new StringBuffer("intersection of ");
 		for(Type type:types) {
 			name.append(type.getFullyQualifiedName()+", ");
 		}
 		name.delete(name.length()-2, name.length()-1);
 		return new SimpleNameSignature(name.toString());
 	}
 	
 	private IntersectionType(List<Type> types, boolean useless) {
 		super(createSignature(types),types);
 	}
 	
 	@Override
 	protected IntersectionType cloneSelf() {
 		return new IntersectionType(types(), false);
 	}
 
 	@Override
 	public List<Member> localMembers() throws LookupException {
 		//FIXME: renaming and so on. Extend both types and perform automatic renaming?
 		//       what about conflicting member definitions?
 		List<Member> result = new ArrayList<Member>();
 		for(Type type: types()) {
 		  result.addAll(type.localMembers(Member.class));
 		}
 		removeConstructors(result);
 		return result;
 	}
 	
 	@Override
 	public <D extends Member> List<? extends SelectionResult> localMembers(DeclarationSelector<D> selector) throws LookupException {
 		//FIXME: renaming and so on. Extend both types and perform automatic renaming?
 		//       what about conflicting member definitions?
 		List<SelectionResult> result = new ArrayList<SelectionResult>();
 		for(Type type: types()) {
 		  result.addAll(type.localMembers(selector));
 		}
 		removeConstructors(result);
 		return result;
 	}
 	
 
 	
 	public void removeConstructors(List<?> members) {
 		Iterator<? extends Object> iter = members.iterator();
 		// Remove constructors. We really do need metaclasses so it seems.
 		while(iter.hasNext()) {
 			Object m = iter.next();
 			if(m instanceof Element) {
 				Element member = (Element) m;
 				if(member.is(language(ObjectOrientedLanguage.class).CONSTRUCTOR) == Ternary.TRUE) {
 					iter.remove();
 				}
 			}
 		}
 	}
 
 	@Override
 	public List<InheritanceRelation> inheritanceRelations() throws LookupException {
 		List<InheritanceRelation> result = new ArrayList<InheritanceRelation>();
 		for(Type type: types()) {
 		  result.addAll(type.inheritanceRelations());
 		}
 		return result;
 	}
 
 	public List<InheritanceRelation> nonMemberInheritanceRelations() {
 		List<InheritanceRelation> result = new ArrayList<InheritanceRelation>();
 		for(Type type: types()) {
 		  result.addAll(type.nonMemberInheritanceRelations());
 		}
 		return result;
 	}
 
 	@Override
 	public void removeNonMemberInheritanceRelation(InheritanceRelation type) {
 		throw new ChameleonProgrammerException("Trying to remove a super type from an intersection type.");
 	}
 
 	@Override
 	public List<? extends TypeElement> directlyDeclaredElements() {
 		List<TypeElement> result = new ArrayList<TypeElement>();
 		for(Type type: types()) {
 		  result.addAll(type.directlyDeclaredElements());
 		}
 		removeConstructors(result);
 		return result;
 	}
 	
 	
 
 	@Override
 	public boolean uniSameAs(final Element other) throws LookupException {
 		List<Type> types = types();
 		if (other instanceof IntersectionType) {
 			return new AbstractPredicate<Type, LookupException>() {
 				@Override
 				public boolean eval(final Type first) throws LookupException {
 					return new AbstractPredicate<Type, LookupException>() {
 						@Override
 						public boolean eval(Type second) throws LookupException {
 							return first.sameAs(second);
 						}
 						
 					}.exists(((IntersectionType)other).types());
 				}
 			}.forAll(types);
 		} else {
			return (other instanceof Type) && (types.size() == 1) && (types.iterator().next().sameAs(other));
 		}
 	}
 	
 	public boolean uniSameAs(final Type other, final List<Pair<TypeParameter, TypeParameter>> trace) throws LookupException {
 		List<Type> types = types();
 		if (other instanceof IntersectionType) {
 			return new AbstractPredicate<Type, LookupException>() {
 				@Override
 				public boolean eval(final Type first) throws LookupException {
 					return new AbstractPredicate<Type, LookupException>() {
 						@Override
 						public boolean eval(Type second) throws LookupException {
 							return first.sameAs(second,trace);
 						}
 						
 					}.exists(((IntersectionType)other).types());
 				}
 			}.forAll(types);
 		} else {
 			return (types.size() == 1) && (types.iterator().next().sameAs(other,trace));
 		}
 	}
 
 	@Override
 	public boolean auxSubTypeOf(Type other) throws LookupException {
 		List<Type> types = types();
 		int size = types.size();
 		boolean result = false;
 		for(int i=0; (!result) && i<size;i++) {
 			result = types.get(i).subTypeOf(other);
 		}
 		return result;
 	}
 	
 	@Override
 	public boolean auxSuperTypeOf(Type type) throws LookupException {
 		List<Type> types = types();
 		int size = types.size();
 		boolean result = size > 0;
 		for(int i=0; result && i<size;i++) {
 			result = type.subTypeOf(types.get(i));
 		}
 		return result;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		List<Type> types = types();
 		int size = types.size();
 		for(int i=0; i<size;i++) {
 			builder.append(types.get(i).toString());
 			if(i < size - 1) {
 				builder.append(" & ");
 			}
 		}
 		return builder.toString();
 	}
 }
