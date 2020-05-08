 package titocc.compiler;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Stack;
 import titocc.compiler.elements.TranslationUnit;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.Tokenizer;
 
 public class Compiler
 {
 	private Reader reader;
 	private TranslationUnit trUnit;
 
 	public Compiler(Reader reader)
 	{
 		this.reader = reader;
 		this.trUnit = null;
 	}
 
 	public Compiler(TranslationUnit trUnit)
 	{
 		this.reader = null;
 		this.trUnit = trUnit;
 	}
 
 	public void compile(Writer writer) throws IOException, SyntaxException
 	{
 		if (trUnit == null)
 			tokenizeAndParse();
		trUnit.compile(new Assembler(writer), new Scope(null, ""), getUsableRegisters());
 	}
 
 	private void tokenizeAndParse() throws IOException, SyntaxException
 	{
 		Tokenizer tokenizer = new Tokenizer(reader);
 		Parser parser = new Parser(tokenizer.tokenize());
 		trUnit = parser.parse();
 	}
 
 	private Stack<Register> getUsableRegisters()
 	{
 		// Use all general purpose registers except R0 because it behaves differently.
 		Stack<Register> registers = new Stack<Register>();
 		registers.push(Register.R5);
 		registers.push(Register.R4);
 		registers.push(Register.R3);
 		registers.push(Register.R2);
 		registers.push(Register.R1);
 		return registers;
 	}
 }
