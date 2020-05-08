 package cd.semantic;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import cd.Main;
 import cd.exceptions.SemanticFailure;
 import cd.exceptions.SemanticFailure.Cause;
 import cd.ir.ast.ClassDecl;
 import cd.ir.ast.MethodDecl;
 import cd.ir.ast.VarDecl;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
import cd.ir.symbols.TypeSymbol;
 import cd.ir.symbols.VariableSymbol;
 import cd.ir.symbols.VariableSymbol.Kind;
 import cd.ir.AstVisitor;
 
 /**
  * A helper class for {@link SemanticAnalyzer} which iterates through a class
  * definition and creates symbols for all of its fields and methods. For each
  * method, we also create symbols for parameters and local variables.
  */
 public class SymbolCreator extends Object {
 
 	private final Main main;
	private final SymbolTable<TypeSymbol> typesTable;
 
	public SymbolCreator(Main main, SymbolTable<TypeSymbol> typesTable) {
 		this.main = main;
		this.typesTable = typesTable;
 	}
 
 	public void createSymbols(ClassDecl cd) {
 		// lookup the super class. the grammar guarantees that this
 		// will refer to a class, if the lookup succeeds.
		cd.sym.superClass = (ClassSymbol) typesTable.getType(cd.superClass);
 		new ClassSymbolCreator(cd.sym).visitChildren(cd, null);
 	}
 
 	/**
 	 * Creates symbols for all fields/constants/methods in a class. Uses
 	 * {@link MethodSymbolCreator} to create symbols for all parameters and
 	 * local variables to each method as well. Checks for duplicate members.
 	 */
 	private class ClassSymbolCreator extends AstVisitor<Void, Void> {
 
 		private final ClassSymbol classSym;
 
 		public ClassSymbolCreator(ClassSymbol classSym) {
 			this.classSym = classSym;
 		}
 
 		@Override
 		public Void varDecl(VarDecl ast, Void arg) {
 			ast.sym = new VariableSymbol(ast.name,
					typesTable.getType(ast.type), Kind.FIELD);
 			classSym.addField(ast.sym);
 			return null;
 		}
 
 		@Override
 		public Void methodDecl(MethodDecl ast, Void arg) {
 			ast.sym = new MethodSymbol(ast.name, classSym);
 			classSym.addMethod(ast.sym);
 
 			// create return type symbol
 			if (ast.returnType.equals("void")) {
 				ast.sym.returnType = main.typeSymbols.getVoidType();
 			} else {
				ast.sym.returnType = typesTable.getType(ast.returnType);
 			}
 
 			// create symbols for each parameter
 			Set<String> pnames = new HashSet<>();
 			for (int i = 0; i < ast.argumentNames.size(); i++) {
 				String argumentName = ast.argumentNames.get(i);
 				String argumentType = ast.argumentTypes.get(i);
 				if (pnames.contains(argumentName))
 					throw new SemanticFailure(Cause.DOUBLE_DECLARATION,
 							"Method '%s' has two parameters named '%s'",
 							ast.sym, argumentName);
 				pnames.add(argumentName);
 				VariableSymbol vs = new VariableSymbol(argumentName,
						typesTable.getType(argumentType));
 				ast.sym.addParameter(vs);
 			}
 
 			// create symbols for the local variables
 			new MethodSymbolCreator(ast.sym).visitChildren(ast.decls(), null);
 
 			return null;
 		}
 
 	}
 
 	private class MethodSymbolCreator extends AstVisitor<Void, Void> {
 
 		final MethodSymbol methodSym;
 
 		public MethodSymbolCreator(MethodSymbol methodSym) {
 			this.methodSym = methodSym;
 		}
 
 		@Override
 		public Void methodDecl(MethodDecl ast, Void arg) {
 			throw new SemanticFailure(Cause.NESTED_METHODS_UNSUPPORTED,
 					"Nested methods are not supported.");
 		}
 
 		@Override
 		public Void varDecl(VarDecl ast, Void arg) {
 			ast.sym = new VariableSymbol(ast.name,
					typesTable.getType(ast.type), Kind.LOCAL);
 			methodSym.addLocal(ast.sym);
 			return null;
 		}
 
 	}
 }
