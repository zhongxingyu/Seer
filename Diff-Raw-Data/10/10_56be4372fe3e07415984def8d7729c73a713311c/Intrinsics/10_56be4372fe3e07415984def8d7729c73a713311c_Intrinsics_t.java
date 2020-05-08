 package titocc.compiler;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import titocc.compiler.elements.ExternalDeclaration;
 import titocc.compiler.types.CType;
 import titocc.compiler.types.FunctionType;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.Token;
 import titocc.tokenizer.TokenStream;
 import titocc.tokenizer.Tokenizer;
 
 /**
  * Manages intrinsic functions/objects that are implemented using C language. They can be called
  * during the compilation process to implement other operations.
  */
 class Intrinsics
 {
 	private static class Intrinsic
 	{
 		Symbol symbol;
 
 		String definition;
 
 		boolean compiled = false;
 
 		Intrinsic(Symbol symbol, String definition)
 		{
 			this.symbol = symbol;
 			this.definition = definition;
 		}
 
 		boolean define(Assembler asm, Scope scope) throws IOException, SyntaxException
 		{
 			if (symbol.getUseCount() == 0 || compiled)
 				return false;
 
 			Tokenizer tokenizer = new Tokenizer(new StringReader(definition));
 			TokenStream tokenStream = new TokenStream(tokenizer.tokenize());
 			ExternalDeclaration declaration = ExternalDeclaration.parse(tokenStream);
 			if (declaration == null) {
 				Token token = tokenStream.getFurthestReadToken();
 				throw new SyntaxException("Unexpected token \"" + token + "\".",
 						token.getPosition());
 			}
 			declaration.compile(asm, scope);
 			compiled = true;
 			return true;
 		}
 	}
 
 	private List<Intrinsic> intrinsics = new ArrayList<Intrinsic>();
 
 	/**
 	 * Constructor. Sets up all necessary data for declaring/defining the intrinsics.
 	 */
 	Intrinsics()
 	{
 		// The "middle" 32-bit value (2^31).
 		add(new Symbol(
 				"__m",
 				CType.INT,
 				Symbol.Category.GlobalVariable,
 				StorageClass.Static,
 				false),
 				"int __m = 0x80000000;");
 
 		// Unsigned division using only signed operations.
 		add(new Symbol(
 				"__udiv",
 				new FunctionType(CType.INT, CType.INT, CType.INT),
 				Symbol.Category.Function,
 				StorageClass.Extern,
 				false),
 				"int __udiv(int a, int b)"
 				+ "{"
 				+ "  if (b & __m) {"
 				+ "      return a + __m >= b + __m;"
 				+ "  } else {"
 				+ "    int r = 0;"
 				+ "    int c;"
 				+ "    while (a & __m) {"
 				+ "      r += c = (__m - 1) / b;"
 				+ "      a -= c * b;"
 				+ "    }"
 				+ "    return r + a / b;"
 				+ "  }"
 				+ "}");
 	}
 
 	/**
 	 * Declares the intrinsic symbols.
 	 *
 	 * @param scope scope for the declarations (should be global scope)
 	 */
 	void declare(Scope scope)
 	{
 		for (Intrinsic intr : intrinsics) {
 			scope.add(intr.symbol);
 		}
 	}
 
 	/**
 	 * Compiles and defines the intrinsics conditionally. I.e. the assembly code for intrinsic
 	 * is only emitted if the symbol is used.
 	 *
 	 * @param asm assembler used for code generation
 	 * @param scope scope where they intrinsics are compiled
 	 * @throws IOException if assembler throws
 	 */
 	void define(Assembler asm, Scope scope) throws IOException
 	{
 		try {
			boolean newDefinitions;
 			do {
				newDefinitions = false;
 				for (Intrinsic intr : intrinsics)
					newDefinitions |= intr.define(asm, scope);
			} while (newDefinitions);
 		} catch (SyntaxException e) {
 			throw new InternalCompilerException("Compiler error in intrinsic function: "
 					+ e.getMessage());
 		}
 	}
 
 	private void add(Symbol symbol, String definition)
 	{
 		intrinsics.add(new Intrinsic(symbol, definition));
 	}
 }
