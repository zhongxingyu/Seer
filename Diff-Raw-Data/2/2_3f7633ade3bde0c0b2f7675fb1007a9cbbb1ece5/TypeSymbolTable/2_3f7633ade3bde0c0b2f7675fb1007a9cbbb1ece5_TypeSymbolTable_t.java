 package cd.semantic;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import cd.exceptions.SemanticFailure;
 import cd.exceptions.SemanticFailure.Cause;
 import cd.ir.symbols.ArrayTypeSymbol;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.NullTypeSymbol;
 import cd.ir.symbols.PrimitiveTypeSymbol;
 import cd.ir.symbols.TypeSymbol;
 
 /**
  * Table that holds all type symbols of the program.
  * 
  * All type symbols in this table can be used from anywhere in the program.
  * 
  * The class automatically creates a corresponding array type for every type
  * added to the symbol table using {@link #add(TypeSymbol)}.
  */
 public class TypeSymbolTable extends SymbolTable<TypeSymbol> {
 
 	/**
 	 * Allow static access to the special type names from the parser.
 	 * 
 	 * The underscore prefix ensures that there will be no name clash with any
 	 * type in user programs. Also, underscores are safe to use in assembly
 	 * mnemonics, in contrast to "<" and ">".
 	 */
 	public static final String TOP_TYPE_NAME = "_top";
 	public static final String NULL_TYPE_NAME = "_null";
 	public static final String BOTTOM_TYPE_NAME = "_bottom";
 
 	/**
 	 * Symbols for the built-in primitive types.
 	 */
 	private final PrimitiveTypeSymbol intType;
 	private final PrimitiveTypeSymbol floatType;
 	private final PrimitiveTypeSymbol voidType;
 	private final PrimitiveTypeSymbol booleanType;
 
 	/**
 	 * Symbols for the built-in Object and null types.
 	 */
 	private final ClassSymbol objectType;
 	private final NullTypeSymbol nullType;
 
 	/**
 	 * Symbol for the built-in top and bottom type.
 	 */
 	private final TypeSymbol topType;
 	private final TypeSymbol bottomType;
 
 	public TypeSymbolTable() {
 		super();
 
 		intType = new PrimitiveTypeSymbol("int");
 		floatType = new PrimitiveTypeSymbol("float");
 		booleanType = new PrimitiveTypeSymbol("boolean");
 		voidType = new PrimitiveTypeSymbol("void");
 		objectType = new ClassSymbol("Object");
 		nullType = new NullTypeSymbol(NULL_TYPE_NAME);
 		topType = new TypeSymbol(TOP_TYPE_NAME);
 		bottomType = new TypeSymbol(BOTTOM_TYPE_NAME);
 
 		add(intType);
 		add(booleanType);
 		add(floatType);
 		add(voidType);
 		add(objectType);
 		add(nullType);
 		add(topType);
 		add(bottomType);
 	}
 
 	public PrimitiveTypeSymbol getIntType() {
 		return intType;
 	}
 
 	public PrimitiveTypeSymbol getFloatType() {
 		return floatType;
 	}
 
 	public PrimitiveTypeSymbol getVoidType() {
 		return voidType;
 	}
 
 	public PrimitiveTypeSymbol getBooleanType() {
 		return booleanType;
 	}
 
 	public ClassSymbol getObjectType() {
 		return objectType;
 	}
 
 	public TypeSymbol getNullType() {
 		return nullType;
 	}
 
 	public TypeSymbol getTopType() {
 		return topType;
 	}
 
 	public TypeSymbol getBottomType() {
 		return bottomType;
 	}
 
 	/**
 	 * Returns the array type symbol corresponding to a given element type.
 	 * 
 	 * @param elementType
 	 *            the type of elements in the array
 	 * @return the array type symbol or <code>null</code> if it cannot be found
 	 */
 	public ArrayTypeSymbol getArrayTypeSymbol(TypeSymbol elementType) {
 		String name = ArrayTypeSymbol.makeNameFromElementType(elementType);
 		return (ArrayTypeSymbol) get(name);
 	}
 
 	@Override
 	public void add(TypeSymbol typeSymbol) {
 		super.add(typeSymbol);
 
 		// Automatically create a the corresponding array type for each type
 		// Overriding the method is a bit fragile, because it assumes that the
 		// 'add' method is the only way of adding a symbol to the symbol table.
 
 		// TODO: This also creates array type symbols for the bottom, top and
 		// null element types. This does not seem necessary.
 		super.add(new ArrayTypeSymbol(typeSymbol));
 	}
 
 	/**
 	 * Returns the list of all class symbols in this symbol table.
 	 * 
 	 * It also includes built-in class symbols like <code>Object</code> and
 	 * <code>null</code>.
 	 * 
 	 * @return the class symbols
 	 */
 	public List<ClassSymbol> getClassSymbols() {
 		List<ClassSymbol> classSymbols = new ArrayList<>();
 		for (TypeSymbol typeSymbol : localSymbols()) {
 			if (typeSymbol instanceof ClassSymbol) {
 				classSymbols.add((ClassSymbol) typeSymbol);
 			}
 		}
 		return classSymbols;
 	}
 
 	/**
 	 * Returns a list of all type symbols that represent primitive numerical
 	 * types.
 	 * 
 	 * @return the list of numerical type symbols
 	 */
 	public List<PrimitiveTypeSymbol> getNumericalTypeSymbols() {
 		return Arrays.asList(getIntType(), getFloatType());
 	}
 
 	/**
 	 * Returns a list of all primitive type symbols in this symbol table.
 	 */
 	public List<PrimitiveTypeSymbol> getPrimitiveTypeSymbols() {
 		List<PrimitiveTypeSymbol> result = new ArrayList<>();
 		for (TypeSymbol typeSymbol : localSymbols()) {
 			if (typeSymbol instanceof PrimitiveTypeSymbol) {
 				result.add((PrimitiveTypeSymbol) typeSymbol);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns a list of all reference type symbols in this symbol table.
 	 */
 	public List<TypeSymbol> getReferenceTypeSymbols() {
 		List<TypeSymbol> result = new ArrayList<>();
 		for (TypeSymbol typeSymbol : localSymbols()) {
 			if (typeSymbol.isReferenceType()) {
 				result.add(typeSymbol);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns a list of all array type symbols in this symbol table.
 	 */
 	public List<ArrayTypeSymbol> getArrayTypeSymbols() {
 		List<ArrayTypeSymbol> result = new ArrayList<>();
 		for (TypeSymbol typeSymbol : localSymbols()) {
 			if (typeSymbol instanceof ArrayTypeSymbol) {
 				result.add((ArrayTypeSymbol) typeSymbol);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the super type of a type, if any.
 	 * 
 	 * @param sym
 	 *            the type symbol
 	 * @return the super type of the given type, or <code>null</code> if there
 	 *         is none
 	 * 
 	 * @todo This method was copied from {@link TypeChecker}. Eventually, it
 	 *       should be turned into a method of {@link TypeSymbol}.
 	 */
 	public TypeSymbol getSuperType(TypeSymbol sym) {
 		if (sym instanceof PrimitiveTypeSymbol) {
 			return null;
 		}
 		if (sym instanceof ArrayTypeSymbol) {
 			return getObjectType();
 		}
 		return ((ClassSymbol) sym).getSuperClass();
 	}
 
 	/**
 	 * Determines whether a type is a sub type of another.
 	 * 
 	 * @param sup
 	 *            the alleged super type
 	 * @param sub
 	 *            the alleged sub type
 	 * @return
 	 * 
 	 * @todo This method was copied from {@link TypeChecker}. Eventually, it
 	 *       should be turned into a method of {@link TypeSymbol}.
 	 */
 	public boolean isSubType(TypeSymbol sup, TypeSymbol sub) {
 		if (sub == getNullType()) {
 			return (sup.isReferenceType() || sup == getTopType());
 		}
 		if (sub == getBottomType()) {
 			return true;
 		}
 		if (sub == getTopType()) {
 			return (sup == sub);
 		}
 		if (sup == getTopType()) {
 			return true;
 		}
 		while (sub != null) {
 			if (sub == sup) {
 				return true;
 			}
 			sub = getSuperType(sub);
 		}
 		return false;
 	}
 
 	/**
	 * Returns the lowest common ancestor type of two types.
 	 * 
 	 * @todo The implementation is currently not very efficient and may even be
 	 *       incorrect
 	 */
 	public TypeSymbol getLCA(TypeSymbol first, TypeSymbol second) {
 		if (isSubType(first, second)) {
 			return first;
 		} else if (isSubType(second, first)) {
 			return second;
 		}
 
 		if (first.isReferenceType() && second.isReferenceType()) {
 			// Here we know that neither type is a subtype of the other
 			return getLCA(getSuperType(first), getSuperType(second));
 		} else {
 			return getTopType();
 		}
 	}
 
 	/**
 	 * Finds the symbol with the given name, or fails with a NO_SUCH_TYPE error.
 	 */
 	public TypeSymbol getType(String name) {
 		TypeSymbol res = get(name);
 		if (res == null) {
 			throw new SemanticFailure(Cause.NO_SUCH_TYPE,
 					"No type '%s' was found", name);
 		}
 		return res;
 	}
 
 }
