 package cd.semantic.ti;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
 import cd.ir.symbols.VariableSymbol;
 import cd.semantic.TypeSymbolTable;
 import cd.semantic.ti.constraintSolving.TypeVariable;
 
 /**
  * Context used for constraint generation in global type inference.
  * 
  * Most importantly, it provides a static construction method
  * {@code #of(TypeSymbolTable)} that initializes the context.
  * 
  * Secondly, it holds all type variables for the return value of each method.
  */
 public final class GlobalConstraintGeneratorContext extends
 		ConstraintGeneratorContext {
 
 	private final Map<MethodSymbol, TypeVariable> returnTypeSets;
 
 	private GlobalConstraintGeneratorContext(TypeSymbolTable typeSymbols) {
 		super(typeSymbols);
 
 		this.returnTypeSets = new HashMap<>();
 	}
 
 	/**
 	 * Constructs a new constraint generation context containing <b>all</b>
 	 * variable symbols in the whole type symbol table.
 	 * 
 	 * @param typeSymbols
 	 * @return the newly constructed context
 	 */
 	public static GlobalConstraintGeneratorContext of(
 			TypeSymbolTable typeSymbols) {
 		GlobalConstraintGeneratorContext result = new GlobalConstraintGeneratorContext(
 				typeSymbols);
 		for (ClassSymbol classSymbol : typeSymbols.getClassSymbols()) {
 			String prefix = classSymbol.getName() + "_";
 
 			for (MethodSymbol methodSymbol : classSymbol.getDeclaredMethods()) {
 				String methodPrefix = prefix + methodSymbol.getName() + "_";
 
 				for (VariableSymbol variable : methodSymbol
 						.getLocalsAndParameters()) {
					String desc = methodPrefix + variable.getName();
 					result.addVariableTypeSet(variable, desc);
 				}
 
 				TypeVariable returnTypeVariable = result.getConstraintSystem()
 						.addTypeVariable(methodPrefix + "return");
 				result.returnTypeSets.put(methodSymbol, returnTypeVariable);
 			}
 
 			for (VariableSymbol field : classSymbol.getDeclaredFields()) {
 				String desc = prefix + field.getName();
 				result.addVariableTypeSet(field, desc);
 			}
 		}
 		return result;
 	}
 
 	public Map<MethodSymbol, TypeVariable> getReturnTypeSets() {
 		return Collections.unmodifiableMap(returnTypeSets);
 	}
 
 	@Override
 	public TypeVariable getReturnTypeSet(MethodSymbol method) {
 		return returnTypeSets.get(method);
 	}
 
 }
