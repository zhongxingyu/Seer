 package cd.semantic.ti.constraintSolving;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.TypeSymbol;
 import cd.semantic.TypeSymbolTable;
 
 /**
  * Constructs and caches constant type sets.
  * 
  * It avoids instantiating the same constant type sets for a given type symbol
  * table over and over again, especially the ones that only contain one type.
  * Besides, it also provides commonly-used constant type sets, for instance for
  * all numerical, array and reference types.
  */
 public class ConstantTypeSetFactory {
 
 	private static final ConstantTypeSet EMPTY_TYPE_SET = new ConstantTypeSet();
 
 	private final TypeSymbolTable typeSymbols;
 	private final Map<TypeSymbol, ConstantTypeSet> singletonTypeSets;
 	private final Map<TypeSymbol, ConstantTypeSet> declarableSubtypeSets;
 
 	private ConstantTypeSet numericalTypeSet;
 	private ConstantTypeSet referenceTypeSet;
 	private ConstantTypeSet arrayTypeSet;
 
 	/**
 	 * Constructs a new factory for constant type symbols.
 	 * 
 	 * @param typeSymbols
 	 *            The table with all type symbols to construct the constant type
 	 *            sets from. It must be final at this point in time.
 	 */
 	public ConstantTypeSetFactory(TypeSymbolTable typeSymbols) {
 		super();
 		this.typeSymbols = checkNotNull(typeSymbols);
 		this.singletonTypeSets = new HashMap<>();
 		this.declarableSubtypeSets = new HashMap<>();
 	}
 
 	/**
 	 * Returns an empty constant type set.s
 	 */
 	public ConstantTypeSet makeEmpty() {
 		return EMPTY_TYPE_SET;
 	}
 
 	/**
 	 * Returns a constant type set for a single type.
 	 */
 	public ConstantTypeSet make(TypeSymbol typeSymbol) {
 		checkNotNull(typeSymbol);
 
 		ConstantTypeSet result = singletonTypeSets.get(typeSymbol);
 		if (result == null) {
 			result = new ConstantTypeSet(typeSymbol);
 			singletonTypeSets.put(typeSymbol, result);
 		}
 		return result;
 	}
 
 	public ConstantTypeSet makeDeclarableSubtypes(TypeSymbol typeSymbol) {
 		checkNotNull(typeSymbol);
 
 		if (!typeSymbol.isDeclarableType()) {
 			return makeEmpty();
 		}
 
 		ConstantTypeSet result = declarableSubtypeSets.get(typeSymbol);
 		if (result == null) {
 			if (typeSymbol instanceof ClassSymbol) {
 				ClassSymbol classSym = (ClassSymbol) typeSymbol;
 				Set<ClassSymbol> classSymbolSubtypes = typeSymbols
 						.getClassSymbolSubtypes(classSym);
 				result = new ConstantTypeSet(classSymbolSubtypes);
 			} else {
 				// All other types do not have any strict subtypes (that may be
 				// used in a program)
 				result = make(typeSymbol);
 			}
 			declarableSubtypeSets.put(typeSymbol, result);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns a constant type set containing only the boolean type.
 	 */
 	public ConstantTypeSet makeBoolean() {
 		return make(typeSymbols.getBooleanType());
 	}
 
 	/**
 	 * Returns a constant type set containing only the integer type.
 	 */
 	public ConstantTypeSet makeInt() {
 		return make(typeSymbols.getIntType());
 	}
 
 	/**
 	 * Returns a constant type set containing only the float type.
 	 */
 	public ConstantTypeSet makeFloat() {
 		return make(typeSymbols.getFloatType());
 	}
 
 	/**
 	 * Returns a constant type set containing all numerical types.
 	 */
 	public ConstantTypeSet makeNumericalTypeSet() {
 		if (numericalTypeSet == null) {
 			numericalTypeSet = new ConstantTypeSet(
 					typeSymbols.getNumericalTypeSymbols());
 		}
 		return numericalTypeSet;
 	}
 
 	/**
 	 * Returns a constant type set containing all declarable reference types.
 	 */
 	public ConstantTypeSet makeReferenceTypeSet() {
 		if (referenceTypeSet == null) {
 			Set<TypeSymbol> referenceTypes = new HashSet<>();
 			referenceTypes.addAll(typeSymbols.getClassSymbols());
 			referenceTypes.addAll(typeSymbols.getArrayTypeSymbols());
 			referenceTypeSet = new ConstantTypeSet(referenceTypes);
 		}
 		return referenceTypeSet;
 	}
 
 	/**
 	 * Returns a constant type set containing all declarable array types.
 	 */
 	public ConstantTypeSet makeArrayTypeSet() {
 		if (arrayTypeSet == null) {
 			arrayTypeSet = new ConstantTypeSet(
 					typeSymbols.getArrayTypeSymbols());
 		}
 		return arrayTypeSet;
 	}
 
 }
