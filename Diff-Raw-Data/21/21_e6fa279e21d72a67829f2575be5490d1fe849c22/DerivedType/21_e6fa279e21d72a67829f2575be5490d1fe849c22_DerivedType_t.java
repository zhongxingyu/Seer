 package chameleon.oo.type;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import chameleon.core.element.Element;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.member.Member;
 import chameleon.core.modifier.Modifier;
 import chameleon.exception.ChameleonProgrammerException;
 import chameleon.oo.type.generics.ActualTypeArgument;
 import chameleon.oo.type.generics.InstantiatedTypeParameter;
 import chameleon.oo.type.generics.TypeParameter;
 import chameleon.util.CreationStackTrace;
 import chameleon.util.Pair;
 
 
 /**
  * A derived type is created by filling in the type parameters of a parameterized
  * type.
  * 
  * @author Marko van Dooren
  */
 public class DerivedType extends TypeWithBody {
 	
 	public DerivedType(List<ParameterSubstitution> parameters, Type baseType) {
 		this(baseType);
 		substituteParameters(parameters);
 	}
 
 	public <P extends Parameter> DerivedType(Class<P> kind, List<P> parameters, Type baseType) {
 		this(baseType);
 		substituteParameters(kind, parameters);
 	}
 	
 	public DerivedType(ParameterSubstitution substitution, Type baseType) {
 		this(baseType);
 		substituteParameters(substitution);
 	}
 
 	/**
 	 * Create a new derived type for the given base type.
 	 * The contents of the type is copied into this type.
 	 * @param baseType
 	 */
 	private DerivedType(Type baseType) {
 		super(baseType.signature().clone());
 		_baseType = baseType;
 		setOrigin(baseType);
 		copyInheritanceRelations(baseType, true);
 		copyParameterBlocks(baseType, true);
 		setBody(new LazyClassBody(((TypeWithBody)baseType).body()));
 		copyImplicitMembers(baseType);
 	}
 	
 	/**
 	 * Create a derived type by filling in the type parameters with the given list of
 	 * actual type arguments.
 	 * 
 	 * Note that the arguments are reversed compared to the constructor which takes a list
 	 * of type parameters because otherwise, both constructors would have the same erasure.
 	 * 
 	 * @param baseType
 	 * @param typeArguments
 	 */
 	public DerivedType(Type baseType, List<ActualTypeArgument> typeArguments) {
 		this(baseType);
 		// substitute parameters
 		List<TypeParameter> myParameters = parameters(TypeParameter.class);
 		if(myParameters.size() != typeArguments.size()) {
 			throw new ChameleonProgrammerException();
 		}
 		Iterator<TypeParameter> parametersIterator = myParameters.iterator();
 		Iterator<ActualTypeArgument> argumentsIterator = typeArguments.iterator();
 		while (parametersIterator.hasNext()) {
 			TypeParameter parameter = parametersIterator.next();
 			ActualTypeArgument argument = argumentsIterator.next();
 			// The next call does not change the parent of 'argument'. It is stored in InstantiatedTypeParameter
 			// using a regular reference.
 			InstantiatedTypeParameter instantiated = new InstantiatedTypeParameter(parameter.signature().clone(), argument);
 			replaceParameter(TypeParameter.class,parameter, instantiated);
 		}
 	}
 	
 	private void copyImplicitMembers(Type original) {
 		_implicitMembers = new ArrayList<Member>();
 		List<Member> implicits = original.implicitMembers();
 		for(Member m: implicits) {
 			Member clone = m.clone();
 			clone.setUniParent(body());
 			_implicitMembers.add(clone);
 		}
 	}
 
 	private List<Member> _implicitMembers;
 	
 	@Override
 	public List<Member> implicitMembers() {
 		return new ArrayList<Member>(_implicitMembers);
 	}
 	
 	@Override
 	public <D extends Member> List<D> implicitMembers(DeclarationSelector<D> selector) throws LookupException {
 		return selector.selection(Collections.unmodifiableList(_implicitMembers));
 	}
 
 	/**
 	 * The modifiers are loaded lazily from the base type.
 	 */
 	@Override
 	public List<Modifier> modifiers() {
 		List<Modifier> result = super.modifiers();
 		if(result.isEmpty()) {
 			copyModifiers(baseType(), true);
 			result = super.modifiers();
 		}
 		return result;
 	}
 
 	@Override
 	public boolean uniSameAs(Element otherType) throws LookupException {
 		boolean result = false;
 		if(otherType instanceof DerivedType) {
 			DerivedType type = (DerivedType) otherType;
 			result = type.baseType().sameAs(baseType());
 			Iterator<TypeParameter> myParams = parameters(TypeParameter.class).iterator();
 			Iterator<TypeParameter> otherParams = type.parameters(TypeParameter.class).iterator();
 			while(myParams.hasNext() && result) {
 				TypeParameter mine = myParams.next();
 				TypeParameter otherParam = otherParams.next();
 				result = mine.sameValueAs(otherParam, new ArrayList());
 			}
 		}
 		return result;
 	}
 	
 	public boolean uniSameAs(Type otherType, List<Pair<TypeParameter, TypeParameter>> trace) throws LookupException {
 		boolean result = false;
 		if(otherType instanceof DerivedType) {
 			DerivedType type = (DerivedType) otherType;
 			result = type.baseType().sameAs(baseType());
 			Iterator<TypeParameter> myParams = parameters(TypeParameter.class).iterator();
 			Iterator<TypeParameter> otherParams = type.parameters(TypeParameter.class).iterator();
 			while(myParams.hasNext() && result) {
 				TypeParameter mine = myParams.next();
 				TypeParameter otherParam = otherParams.next();
 				result = mine.sameValueAs(otherParam,trace);
 			}
 		}
 		return result;
 	}
 
 	
 	@Override
 	public int hashCode() {
 		int result = baseType().hashCode();
 		return result;
 	}
 	
 	private Type _baseType;
 	
 	@Override
 	public Type baseType() {
 		return _baseType.baseType();
 	}
 
 	@Override
 	public DerivedType clone() {
 		return new DerivedType(clonedParameters(),baseType());
 	}
 
 	protected List<ParameterSubstitution> clonedParameters() {
 		List<ParameterSubstitution> args = new ArrayList<ParameterSubstitution>();
 		for(ParameterBlock<?,?> block: parameterBlocks()) {
 			List<Parameter> list = new ArrayList<Parameter>();
 			for(Parameter par: block.parameters()) {
 				list.add(par.clone());
 			}
 			args.add(new ParameterSubstitution(block.parameterType(), list)); 
 		}
 		return args;
 	}
 
 }
