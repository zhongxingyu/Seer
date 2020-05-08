 package edu.utwente.vb.symbols;
 
 import java.util.List;
 import java.util.Set;
 
 import org.antlr.runtime.CommonToken;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 import static edu.utwente.vb.symbols.ExampleType.*;
 import static edu.utwente.vb.example.util.CheckerHelper.*;
 
 import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
 import edu.utwente.vb.tree.FunctionNode;
 import edu.utwente.vb.tree.TypedNode;
 
 public class Prelude {
 	public final Set<FunctionId<TypedNode>> builtins;
 	
 	@SuppressWarnings("unchecked")
 	public Prelude() throws IllegalFunctionDefinitionException{
 		ImmutableSet.Builder<FunctionId<TypedNode>> builder = ImmutableSet.builder();
 		//Voeg operatoren toe
 		// apply/becomes varianten
 		builder.add(createBuiltin("=", BOOL, BOOL, BOOL));
 		builder.add(createBuiltin("=", INT, INT, INT));
 		builder.add(createBuiltin("=", CHAR, CHAR, CHAR));
 		builder.add(createBuiltin("=", STRING, STRING, STRING));
 		// boolean
 		builder.add(createBuiltin("==", BOOL, BOOL, BOOL));
 		builder.add(createBuiltin("!=", BOOL, BOOL, BOOL));
 		builder.add(createBuiltin("and", BOOL, BOOL, BOOL));
 		builder.add(createBuiltin("or", BOOL, BOOL, BOOL));
 		builder.add(createFunctionId("!", ExampleType.BOOL, createVariableId("rhs", ExampleType.BOOL)));
 		// int
 		builder.add(createBuiltin("+", INT, INT, INT));
 		builder.add(createBuiltin("-", INT, INT, INT));
 		builder.add(createBuiltin("*", INT, INT, INT));
 		builder.add(createBuiltin("/", INT, INT, INT));
 		builder.add(createBuiltin("%", INT, INT, INT));
 		// boolean + int
 		builder.add(createBuiltin("==", BOOL, INT, INT));
 		builder.add(createBuiltin("!=", BOOL, INT, INT));
 		builder.add(createBuiltin("<", BOOL, INT, INT));
 		builder.add(createBuiltin("<=", BOOL, INT, INT));
 		builder.add(createBuiltin(">", BOOL, INT, INT));
 		builder.add(createBuiltin(">=", BOOL, INT, INT));
 		// string -> tijdelijk met +..
 		builder.add(createBuiltin("+", STRING, STRING, STRING));
 		builder.add(createBuiltin("+", STRING, STRING, CHAR));
 		builder.add(createBuiltin("+", STRING, STRING, INT));
 		builder.add(createBuiltin("+", STRING, STRING, BOOL));
 		builder.add(createBuiltin("==", BOOL, STRING, STRING));
 		builder.add(createBuiltin("!=", BOOL, STRING, STRING));
 		// char
 		builder.add(createBuiltin("==", BOOL, CHAR, CHAR));
 		builder.add(createBuiltin("!=", BOOL, CHAR, CHAR));
 		builder.add(createBuiltin("<", BOOL, CHAR, CHAR));
 		builder.add(createBuiltin("<=", BOOL, CHAR, CHAR));
 		builder.add(createBuiltin(">", BOOL, CHAR, CHAR));
 		builder.add(createBuiltin(">=", BOOL, CHAR, CHAR));
 		//Builting functions
 		builder.add(createFunctionId("print", ExampleType.VOID, 	createVariableId("str", ExampleType.STRING)));
 		builder.add(createFunctionId("print", ExampleType.VOID, 	createVariableId("str", ExampleType.BOOL)));
 		builder.add(createFunctionId("print", ExampleType.VOID, 	createVariableId("str", ExampleType.INT)));
 		builder.add(createFunctionId("print", ExampleType.VOID, 	createVariableId("str", ExampleType.CHAR)));
 		
 		FunctionNode readNode = byToken("read", ExampleType.VOID);
 		VarArgsFunctionId<TypedNode> varArgs = new VarArgsFunctionId<TypedNode>(readNode, ExampleType.VOID, ExampleType.CHAR, ExampleType.INT, ExampleType.STRING);
 		readNode.setBoundMethod(varArgs);
 		builder.add(varArgs);
 		
 		builder.add(createFunctionId("ensure", ExampleType.VOID,	createVariableId("expr", ExampleType.BOOL)));
		//Sla op
		builtins = builder.build();
 	}
 	
 	public void inject(SymbolTable<TypedNode> t) throws IllegalFunctionDefinitionException{
 		for(FunctionId<TypedNode> node : builtins){
 			t.put(node);
 		}
 	}
 }
