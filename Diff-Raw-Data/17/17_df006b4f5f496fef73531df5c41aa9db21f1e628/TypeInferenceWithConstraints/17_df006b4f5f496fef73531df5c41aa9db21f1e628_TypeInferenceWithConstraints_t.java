 package cd.semantic.ti;
 
 import java.util.Set;
 
 import cd.exceptions.SemanticFailure;
 import cd.exceptions.SemanticFailure.Cause;
 import cd.ir.symbols.TypeSymbol;
 import cd.semantic.TypeSymbolTable;
 import cd.semantic.ti.constraintSolving.TypeSet;
 
 /**
  * Base class for type inference implementations based on constraint solving.
  */
 public abstract class TypeInferenceWithConstraints implements TypeInference {
 
 	public TypeSymbol makeStaticType(TypeSymbolTable typeSymbols,
 			TypeSet typeSet, String name) {
 		Set<TypeSymbol> possibleTypes = typeSet.getTypes();
 		TypeSymbol type = null;
 		if (possibleTypes.isEmpty()) {
 			// Use the bottom type if there are no types in the type
 			// set. Since the constraint system has been solved
 			// successfully, this usually (always?) means that the
 			// variable symbol is not used at all.

			// TODO: The above is no true. if e.g. a field of an object is only
			// assigned to to the same field of another object of the same class
			// we do not have any constraints and therefore get bottom. This is
			// not ok, since it's relevant for the code generator!

 			type = typeSymbols.getBottomType();
 		} else if (possibleTypes.size() == 1) {
 			type = possibleTypes.iterator().next();
 		} else if (possibleTypes.size() > 1) {
 			// NOTE: we may still try to take the join (lca). This is
 			// sometimes necessary.
 			TypeSymbol[] typesArray = possibleTypes
 					.toArray(new TypeSymbol[possibleTypes.size()]);
 			TypeSymbol lca = typeSymbols.getLCA(typesArray);
			// The LCA must itself be among the possible types. Otherwise, the
			// type may not be specific enough to satisfy all constraints.
			if (possibleTypes.contains(lca)) {
 				type = lca;
 			} else {
 				throw new SemanticFailure(Cause.TYPE_INFERENCE_ERROR,
 						"Type inference resulted in ambiguous type for " + name);
 			}
 		}
 		return type;
 	}
 
 }
