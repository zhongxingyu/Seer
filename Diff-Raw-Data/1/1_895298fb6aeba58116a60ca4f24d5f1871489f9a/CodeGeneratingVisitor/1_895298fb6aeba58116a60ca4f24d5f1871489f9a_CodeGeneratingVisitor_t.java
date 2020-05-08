 package back_end;
 
 import util.ast.node.*;
 import util.ast.AbstractSyntaxTree;
 import util.type.Types;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * Visitor class for generating Java source.
  * 
  * This is the fourth (and final) walk performed after construction of the AST
  * from source. CodeGeneratingVisitor generates a massive String representing
  * the translated Hog program.
  * 
  * 
  * @author Samuel Messing
  * @author Kurry Tran
  * 
  */
 public class CodeGeneratingVisitor implements Visitor {
 
 	protected final static Logger LOGGER = Logger
 			.getLogger(CodeGeneratingVisitor.class.getName());
 	/**
 	 * The format of the input files to the <code>map</code> class. Currently
 	 * Hog only supports text formats, so this doesn't need to be set by the
 	 * constructor.
 	 */
 	protected final String inputFormatClass = "TextInputFormat.class";
 	/**
 	 * The format of the output files from the <code>reduce</code> class. See
 	 * {@link #inputFormatClass inputFormatClass} for more details.
 	 */
 	protected final String outputFormatClass = "TextInputFormat.class";
 
 	protected AbstractSyntaxTree tree;
 	protected StringBuilder code;
 	protected String outputKeyClass;
 	protected String outputValueClass;
 	protected String inputFile = "example.txt";
 	protected String outputFile = "example.txt";
 	/**
 	 * Remember when recursing if we're dealing with a declaration statement, as
 	 * the handling both DerivedTypeNodes and IdNodes is context-specific.
 	 */
 	protected boolean declarationStatement = false;
 	protected boolean rValue = false;
 	/**
 	 * Remember if we're writing the emit() function, as we need to cast to
 	 * Hadoop's Writable types;
 	 */
 	protected boolean emit = false;
 
 	/**
 	 * Construct a CodeGeneratingVisitor, but don't specify input file or output
 	 * file.
 	 * <p>
 	 * Mainly used for testing/development purposes.
 	 * 
 	 * @param root
 	 *            the root of the AST representing the Hog source program.
 	 */
 	public CodeGeneratingVisitor(AbstractSyntaxTree root) {
 
 		this.tree = root;
 		this.code = new StringBuilder();
 
 	}
 
 	/**
 	 * Construct a CodeGeneratingVisitor, specifying the input file name and the
 	 * output file name for the corresponding Hadoop job.
 	 * 
 	 * <pre>
 	 * public {@link CodeGeneratingVisitor}({@link AbstractSyntaxTree} root, {@link String} inputFile, {@link String} outputFile)
 	 * </pre>
 	 * 
 	 * @param root
 	 *            The root node of the Hog source program's AST.
 	 * @param inputFile
 	 *            The inputFile for the <code>map</code> class to read from.
 	 * @param outputFile
 	 *            The outputFile for the <code>reduce</code> class to write to.
 	 */
 	public CodeGeneratingVisitor(AbstractSyntaxTree root, String inputFile,
 			String outputFile) {
 
 		this(root);
 		this.inputFile = inputFile;
 		this.outputFile = outputFile;
 
 	}
 
 	/**
 	 * Return the Java source code translated from the AST.
 	 * 
 	 * @return a string representation (formatted) of the java source code.
 	 */
 	public String getCode() {
 		formatCode();
 		return code.toString();
 	}
 
 	@Override
 	public void walk() {
 
 		writeHeader();
 
 		// start recursive walk:
 		walk(tree.getRoot());
 
 		code.append("}");
 
 	}
 
 	private void walk(Node node) {
 
 		node.accept(this);
 
 		if (node.isEndOfLine()) {
 			writeStatement();
 		}
 
 		// base cases (sometimes recursion needs to go through visit methods
 		// as with If Else statements).
 
 		boolean baseCase = false;
 
 		if (node instanceof IfElseStatementNode) {
 			baseCase = true;
 		} else if (node instanceof SectionNode) {
 			baseCase = true;
 		} else if (node instanceof FunctionNode) {
 			baseCase = true;
 		} else if (node instanceof JumpStatementNode) {
 			baseCase = true;
 		} else if (node instanceof StatementNode) {
 			baseCase = true;
 		} else if (node instanceof StatementListNode) {
 			baseCase = true;
 		} else if (node.getChildren().isEmpty()) {
 			baseCase = true;
 		}
 
 		// continue recursion if not base case:
 
 		if (!baseCase) {
 			for (Node child : node.getChildren()) {
 				walk(child);
 			}
 		}
 
 	}
 
 	private void writeHeader() {
 		LOGGER.fine("Writing header to code.");
 		code.append("import java.io.IOException;");
 		code.append("import java.util.*;");
 		code.append("import org.apache.hadoop.fs.Path;");
 		code.append("import org.apache.hadoop.conf.*;");
 		code.append("import org.apache.hadoop.io.*;");
 		code.append("import org.apache.hadoop.mapred.*;");
 		code.append("public class Hog {");
 	}
 
 	private void writeMapReduce() {
 		LOGGER.fine("Writing mapReduce initialization code.");
 		code.append("JobConf conf = new JobConf(Hog.class);");
 		code.append("conf.setJobName(\"hog\");");
 		code.append("conf.setOutputKeyClass(" + outputKeyClass + ".class);");
 		code
 				.append("conf.setOutputValueClass(" + outputValueClass
 						+ ".class);");
 		code.append("conf.setMapperClass(Map.class);");
 		code.append("conf.setCombinerClass(Reduce.class);");
 		code.append("conf.setReducerClass(Reduce.class);");
 		code.append("conf.setInputFormat(" + inputFormatClass + ");");
 		code.append("conf.setOutputFormat(" + outputFormatClass + ");");
 		code.append("FileInputFormat.setInputPaths(conf, new Path(\""
 				+ inputFile + "\"));");
 		code.append("FileOutputFormat.setOutputPath(conf, new Path(\""
 				+ outputFile + "\"));");
 		code.append("JobClient.runJob(conf);");
 	}
 
 	/**
 	 * Write the end of a statement.
 	 * 
 	 * Protects against writing multiple semicolons, as an ease for the
 	 * programmer.
 	 */
 	private void writeStatement() {
 		if (!code.toString().endsWith("}") && !code.toString().endsWith(";")) {
 			code.append(";");
 		}
 	}
 
 	/**
 	 * <code>this.code</code> is originally built as a monolithic string without
 	 * newlines and other formatting. <code>formatCode()</code> adds both
 	 * newlines after statements and proper indentation based on scope.
 	 */
 	private void formatCode() {
 		int scopeCount = 0;
 		StringBuilder indentedCode = new StringBuilder();
 		for (int i = 0; i < code.length(); i++) {
 			switch (code.charAt(i)) {
 			case '{':
 				scopeCount++;
 				indentedCode.append("{\n");
 				indentedCode.append(repeat(' ', 4 * scopeCount));
 				break;
 			case '}':
 				scopeCount--;
 				// we're reducing scope, so need to undo the spaces previously
 				// written
 				indentedCode.delete(indentedCode.length() - 4, indentedCode
 						.length());
 				indentedCode.append("}\n");
 				indentedCode.append(repeat(' ', 4 * scopeCount));
 				break;
 			case ';':
 				indentedCode.append(";\n");
 				indentedCode.append(repeat(' ', 4 * scopeCount));
 				break;
 			default:
 				indentedCode.append(code.charAt(i));
 			}
 		}
 		code = indentedCode;
 	}
 
 	/**
 	 * Repeat a character n times.
 	 * 
 	 * @param toRepeat
 	 *            the character to repeat
 	 * @param times
 	 *            the number of times to repeat <code>toRepeat</code>
 	 * @return the String formed by repeating <code>toRepeat</code> n=
 	 *         <code>times</code> times in a row.
 	 */
 	private String repeat(char toRepeat, int times) {
 		StringBuilder repeated = new StringBuilder();
 		for (int i = 0; i < times; i++)
 			repeated.append(toRepeat);
 		return repeated.toString();
 	}
 
 	@Override
 	public void visit(ArgumentsNode node) {
 		LOGGER.finer("visit(ArgumentsNode node) called on " + node);
 
 		if (node.hasMoreArgs()) {
 			walk(node.getMoreArgs());
 			code.append(", ");
 		}
 
 		walk(node.getArg());
 
 	}
 
 	@Override
 	public void visit(BiOpNode node) {
 		LOGGER.finer("visit(BiOpNode node) called on " + node);
 		walk(node.getLeftNode());
 		switch (node.getOpType()) {
 		case ASSIGN:
 			code.append(" = ");
 			rValue = true;
 			break;
 		case DBL_EQLS:
 			code.append(" == ");
 			break;
 		case NOT_EQLS:
 			code.append(" != ");
 			break;
 		case PLUS:
 			code.append(" + ");
 			break;
 		case OR:
 			code.append(" || ");
 			break;
 		case TIMES:
 			code.append(" * ");
 			break;
 		case MINUS:
 			code.append(" - ");
 			break;
 		case LESS:
 			code.append(" < ");
 			break;
 		case LESS_EQL:
 			code.append(" <= ");
 			break;
 		case GRTR:
 			code.append(" > ");
 			break;
 		case GRTR_EQL:
 			code.append(" >= ");
 			break;
 		case DIVIDE:
 			code.append(" / ");
 			break;
 		case MOD:
 			code.append(" % ");
 			break;
 		case AND:
 			code.append(" && ");
 			break;
 		}
 
 		walk(node.getRightNode());
 
 		// unset declaration flag that may have been set (when node.getOpType ==
 		// ASSIGN)
 		declarationStatement = false;
 		// unset the rValue flag that may have been set (when node.getOpType ==
 		// ASSIGN)
 		rValue = false;
 
 	}
 
 	@Override
 	public void visit(CatchesNode node) {
 		LOGGER.finer("visit(CatchesNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(ConstantNode node) {
 		LOGGER.finer("visit(ConstantNode node) called on " + node);
 		code.append(node.getValue());
 	}
 
 	@Override
 	public void visit(DerivedTypeNode node) {
 		LOGGER.finer("visit(DerivedTypeNode node) called on " + node);
 
 		if (declarationStatement)
 			code.append("new ");
 
 		switch (node.getLocalType()) {
 		case LIST:
 			if (declarationStatement && rValue)
 				code.append("ArrayList<");
 			else
 				code.append("List<");
 			break;
 		case ITER:
 			code.append("Iterator<");
 			break;
 		case DICT:
 			throw new UnsupportedOperationException(
 					"Dictionaries not yet supported!");
 		case MULTISET:
 			throw new UnsupportedOperationException("Multisets not supported!");
 		case SET:
 			if (declarationStatement && rValue)
 				code.append("HashSet<");
 			else
 				code.append("Set<");
 			break;
 		}
 
 		// remember state for this particular node, but forget it for recursing
 		boolean declaration = declarationStatement;
 		declarationStatement = false;
 
 		walk(node.getInnerTypeNode());
 
 		// close inner types
 		code.append(">");
 
 		if (declaration)
 			code.append("()");
 
 	}
 
 	@Override
 	public void visit(DictTypeNode node) {
 		LOGGER.finer("visit(DictTypeNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(ElseIfStatementNode node) {
 		LOGGER.finer("visit(ElseIfStatementNode node) called on " + node);
 		code.append("} else if (");
 		walk(node.getCondition());
 		code.append(") {");
 		walk(node.getIfCondTrue());
 		if (node.getIfCondFalse() != null) {
 			walk(node.getIfCondFalse());
 		}
 
 	}
 
 	@Override
 	public void visit(ElseStatementNode node) {
 		LOGGER.finer("visit(ElseStatementNode node) called on " + node);
 		code.append("} else {");
 		walk(node.getBlock());
 		code.append("}");
 
 	}
 
 	@Override
 	public void visit(ExceptionTypeNode node) {
 		LOGGER.finer("visit(ExceptionTypeNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(ExpressionNode node) {
 		LOGGER.finer("visit(ExpressionNode node) called on " + node);
 		// ExpressionNode is to general, so move to a more specific case:
 		node.accept(this);
 	}
 
 	@Override
 	public void visit(FunctionNode node) {
 		LOGGER.finer("visit(FunctionNodeNode node) called on " + node);
 		code.append("public static ");
 		walk(node.getType());
 		code.append(" ");
 		code.append(node.getIdentifier());
 		ParametersNode params = node.getParametersNode();
 		code.append("(");
 		walk(params.getType());
 		code.append(" " + params.getIdentifier());
 		code.append(")");
 		code.append(" {");
 		walk(node.getInstructions());
 
 	}
 
 	@Override
 	public void visit(GuardingStatementNode node) {
 		LOGGER.finer("visit(GuardingStatementNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(IdNode node) {
 		LOGGER
 				.finer("visit(IdNode node) called on " + node + ", emit: "
 						+ emit);
 
 		if (node.isDeclaration()) {
 			walk(node.getType());
 			code.append(" ");
 			// set a flag so when writing the right side of an assignment
 			// statement
 			// we handle things appropriately.
 			declarationStatement = true;
 		}
 
 		if (emit) {
 			walk(node.getType());
 			code.append("(");
 		}
 
 		code.append(node.getIdentifier());
 
 		// derived IdNodes need to be instantiated manually
 		if (declarationStatement && node.getType() instanceof DerivedTypeNode) {
 			code.append(" = ");
 			rValue = true;
 			walk(node.getType());
 			rValue = false;
 		} else if (emit) {
 			code.append(")");
 		}
 
 	}
 
 	@Override
 	public void visit(IfElseStatementNode node) {
 		LOGGER.finer("visit(IfElseStatementNode node) called on " + node);
 		code.append("if (");
 		walk(node.getCondition());
 		code.append(") {");
 		walk(node.getIfCondTrue());
 		// check that buffer cleared
 		if (node.getCheckNext() != null) {
 			walk(node.getCheckNext());
 		}
 		if (node.getIfCondFalse() != null) {
 			walk(node.getIfCondFalse());
 		}
 		code.append("}");
 	}
 
 	@Override
 	public void visit(IterationStatementNode node) {
 		LOGGER.finer("visit(IterationStatementNode node) called on " + node);
 
 		switch (node.getIterationType()) {
 		case FOR:
 			code.append("for (");
 			walk(node.getInitial());
 			code.append("; ");
 			walk(node.getCheck());
 			code.append("; ");
 			walk(node.getIncrement());
 			code.append(") {");
 			walk(node.getBlock());
 			writeStatement();
 			break;
 		case FOREACH:
 			code.append("for (");
 			code.append(Types.getHadoopType((PrimitiveTypeNode) node.getPart()
 					.getType()));
 			code.append(" ");
 			walk(node.getPart());
 			code.append(" : ");
 			walk(node.getWhole());
 			code.append(") {");
 			walk(node.getBlock());
 			writeStatement();
 			break;
 		case WHILE:
 			code.append("while (");
 			walk(node.getCheck());
 			code.append(") {");
 			walk(node.getBlock());
 			writeStatement();
 			break;
 		}
 		code.append("}");
 	}
 
 	@Override
 	public void visit(JumpStatementNode node) {
 		LOGGER.finer("visit(JumpStatementNode node) called on " + node);
 
 		switch (node.getJumpType()) {
 		case RETURN:
 			code.append("return ");
 			break;
 		case BREAK:
 			code.append("break");
 			break;
 		case CONTINUE:
 			code.append("continue");
 			break;
 		}
 
 		if (node.getExpressionNode() != null) {
 			walk(node.getExpressionNode());
 		}
 
 		writeStatement();
 	}
 
 	@Override
 	public void visit(MockNode node) {
 		LOGGER.finer("visit(MockNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(MockExpressionNode node) {
 		LOGGER.finer("visit(MockExpressionNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(Node node) {
 		LOGGER.finer("visit(Node node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(ParametersNode node) {
 		LOGGER.finer("visit(ParametersNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(PostfixExpressionNode node) {
 		LOGGER.finer("visit(PostfixExpressionNode node) called on " + node);
 
 		switch (node.getPostfixType()) {
 		case METHOD_NO_PARAMS:
 			IdNode objectOfMethod = node.getObjectOfMethod();
 			IdNode methodNameNoParam = node.getMethodName();
 			code.append(objectOfMethod.getIdentifier() + "."
 					+ methodNameNoParam.getIdentifier() + "()");
 			break;
 		case METHOD_WITH_PARAMS:
 			IdNode objectName = node.getObjectName();
 			IdNode methodName = node.getMethodName();
 			if (node.hasArguments()) {
 				ExpressionNode argsList = node.getArgsList();
 				code.append(objectName.getIdentifier() + "."
 						+ methodName.getIdentifier() + "("
 						+ argsList.toSource() + ")");
 			}
 			break;
 		case FUNCTION_CALL:
 			IdNode functionIdNode = node.getFunctionName();
 			// check if this is our special mapReduce() call:
 			if (functionIdNode.getIdentifier().equals("mapReduce")) {
 				writeMapReduce();
 				return;
 			}
 
 			if (!node.getFunctionName().getIdentifier().equals("emit")) {
 				code.append("Functions.");
 				walk(node.getFunctionName());
 			} else {
 				code.append("output.Collect");
 				emit = true;
 			}
 
 			code.append("(");
 
 			// check for arguments
 			if (node.hasArguments())
 				walk(node.getArgsList());
 
 			code.append(")");
 
 			// unset emit flag which may have been set:
 			emit = false;
 
 		}
 	}
 
 	@Override
 	public void visit(PrimaryExpressionNode node) {
 		LOGGER.finer("visit(PrimaryExpressionNode node) called on " + node);
 		code.append(node.toSource());
 
 	}
 
 	@Override
 	public void visit(PrimitiveTypeNode node) {
 		LOGGER.finer("visit(PrimitiveTypeNode node) called on " + node);
 
 		if (emit) {
 			code.append(Types.getHadoopType(node));
 		} else {
 			code.append(Types.getJavaType(node));
 		}
 
 	}
 
 	@Override
 	public void visit(ProgramNode node) {
 		LOGGER.finer("visit(ProgramNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(RelationalExpressionNode node) {
 		LOGGER.finer("visit(RelationalNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(ReservedWordTypeNode node) {
 		LOGGER.finer("visit(ReservedWordTypeNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(SectionNode node) {
 		LOGGER.finer("visit(SectionNode node) called on " + node);
 
 		SectionNode.SectionName sectionKind = node.getSectionName();
 
 		switch (sectionKind) {
 		case FUNCTIONS:
 			code.append("public static class Functions {");
 			break;
 		case MAP:
 			code
 					.append("public static class Map extends MapReduceBase implements Mapper");
 			walk(node.getSectionTypeNode());
 			break;
 		case REDUCE:
 			code
 					.append("public static class Reduce extends MapReduceBase implements Reducer");
 			walk(node.getSectionTypeNode());
 
 			break;
 		case MAIN:
 			code
 					.append("public static void main(String[] args) throws Exception {");
 			break;
 		}
 		walk(node.getBlock());
 		code.append("}");
 
 		// need to write an additional block for inner methods in reduce and
 		// map:
 
 		switch (sectionKind) {
 		case MAP:
 		case REDUCE:
 			code.append("}");
 		}
 
 	}
 
 	@Override
 	public void visit(SectionTypeNode node) {
 		LOGGER.finer("visit(SectionTypeNode node) called on " + node);
 		// if we're at @Reduce, need to see output types for main
 		if (node.getSectionParent().getSectionName() == SectionNode.SectionName.REDUCE) {
 			outputKeyClass = Types.getHadoopType((PrimitiveTypeNode) node
 					.getReturnKey());
 			outputValueClass = Types.getHadoopType((PrimitiveTypeNode) node
 					.getReturnValue());
 		}
 
 		code.append("<"
 				+ Types.getHadoopType(node.getInputKeyIdNode().getType())
 				+ ", "
 				+ Types.getHadoopType(node.getInputValueIdNode().getType())
 				+ ", " + Types.getHadoopType(node.getReturnKey()) + ", "
 				+ Types.getHadoopType(node.getReturnValue()) + "> {");
 		if (node.getSectionParent().getSectionName() == SectionNode.SectionName.REDUCE) {
 			code.append("public void reduce(");
 			code
 					.append(Types.getHadoopType(node.getInputKeyIdNode()
 							.getType()));
 			code.append(" key, Iterator<");
 			code.append(Types.getHadoopType(node.getInputValueIdNode()
 					.getType()));
 			code.append("> values,  OutputCollector<");
 			code.append(Types.getHadoopType(node.getReturnKey()));
 			code.append(", ");
 			code.append(Types.getHadoopType(node.getReturnValue()));
 			code.append("> output, Reporter reporter) throws IOException {");
 		} else {
 			code.append("public void map(");
 			code
 					.append(Types.getHadoopType(node.getInputKeyIdNode()
 							.getType()));
 			code.append(" key, ");
 			code.append(Types.getHadoopType(node.getInputValueIdNode()
 					.getType()));
 			code.append(" value,  OutputCollector<");
 			code.append(Types.getHadoopType(node.getReturnKey()));
 			code.append(", ");
 			code.append(Types.getHadoopType(node.getReturnValue()));
 			code.append("> output, Reporter reporter) throws IOException {");
 		}

 	}
 
 	@Override
 	public void visit(SelectionStatementNode node) {
 		LOGGER.finer("visit(SelectionStatementNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(StatementListNode node) {
 		LOGGER.finer("visit(StatementListNode node) called on " + node);
 		for (Node child : node.getChildren()) {
 			walk(child);
 			writeStatement();
 		}
 
 	}
 
 	@Override
 	public void visit(StatementNode node) {
 		LOGGER.finer("visit(StatementNode node) called on " + node);
 		for (Node child : node.getChildren()) {
 			walk(child);
 		}
 		writeStatement();
 	}
 
 	@Override
 	public void visit(SwitchStatementNode node) {
 		LOGGER.finer("visit(SwitchStatementNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(TypeNode node) {
 		LOGGER.finer("visit(TypeNode node) called on " + node);
 		// type node is too general, so call something more specific:
 		node.accept(this);
 	}
 
 	@Override
 	public void visit(UnOpNode node) {
 		LOGGER.finer("visit(UnOpNode node) called on " + node);
 
 		switch (node.getOpType()) {
 		case UMINUS:
 			code.append("-");
 			walk(node.getChildNode());
 			break;
 		case NOT:
 			code.append("!");
 			walk(node.getChildNode());
 			break;
 		case INCR:
 			walk(node.getChildNode());
 			code.append("++");
 			break;
 		case DECR:
 			walk(node.getChildNode());
 			code.append("--");
 			break;
 		case CAST:
 			throw new UnsupportedOperationException(
 					"Cast statements are NOT supported yet!");
 		case NONE:
 			// none means no unary operator applied.
 			walk(node.getChildNode());
 			break;
 		}
 
 	}
 
 }
