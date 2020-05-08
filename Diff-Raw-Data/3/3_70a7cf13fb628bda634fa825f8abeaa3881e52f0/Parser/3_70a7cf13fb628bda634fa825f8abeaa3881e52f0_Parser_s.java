 package haw.ci.lib;
 
 import static haw.ci.lib.Tokens.*;
 import haw.ci.lib.nodes.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class Parser {
 	private final boolean DEBUG = true;
 	private ITokenStream tokenStream;
 	private Yytoken current;
 	private Yytoken next;
 	public Parser(ITokenStream tokenStream) {
 		this.tokenStream = tokenStream;
 	}
 //IdentList = ident {, ident}.
 	private IdentListNode IdentList() throws ParserAcceptError {
 		IdentListNode identList = new IdentListNode();
 		if (test(IDENTIFER)) {
 			identList.add(Ident());
 		}
 		while (test(COMMA)) {
 			next();
 			identList.add(Ident());
 		}
 		return identList;
 	}
 //ArrayType = ARRAY [ IndexExpression ] OF Type.
 	private AbstractNode Arraytype() throws ParserAcceptError {
 		require(ARRAY);
 		require(BRACE_SQUARE_OPEN);
 		IndexExpression();
 		require(OF);
 		Type();
 		return null;
 	}
 //FieldList = [IdentList : Type].
 	private FieldListNode FieldList() throws ParserAcceptError {
 		IdentListNode identList = IdentList();
 
 		if (require(COLON)) {
 			return new FieldListNode(identList, Type());
 		}
 		// TODO should be some error
 		return null;
 	}
 //RecordType = RECORD FieldList {; FieldList} END.
 	private RecordTypeNode RecordType() throws ParserAcceptError {
 		// TODO throw some error if not record
 		require(RECORD);
 		List<FieldListNode> fieldLists = Arrays.asList(FieldList());
 
 		while(require(SEMICOLON)) {
 			fieldLists.add(FieldList());
 		}
 		// TODO throw some error if not end
 		require(END);
 
 		return new RecordTypeNode(fieldLists);
 	}
 //Type = ident | ArrayType | RecordType.
 	private TypeNode Type() throws ParserAcceptError {
 		TypeNode type = null;
 		switch (current.getToken()) {
 		case IDENTIFER:
 			type = new TypeNode(current.getValue());
 			next();
 			break;
 		case ARRAY:
 			Arraytype();
 			break;
 		case RECORD:
 			RecordType();
 			break;
 		}
 		return type;
 	}
 //FPSection = [VAR] IdentList : Type.
 	private FormalParameterSectionNode FPSection() throws ParserAcceptError {
 		require(VAR);
 		IdentListNode identList = IdentList();
 		require(COLON);
 		TypeNode type = Type();
 		return new FormalParameterSectionNode(identList, type);
 	}
 //FormalParameters = FPSection {; FPSection}.
 	private FormalParameterNode FormalParameters() throws ParserAcceptError {
 		FormalParameterNode formalParameter = new FormalParameterNode();
 		if (test(VAR)) {
 			formalParameter.add(FPSection());
 			while (test(SEMICOLON)) {
 				next();
 				formalParameter.add(FPSection());
 			}
 		}
 		return formalParameter;
 	}
 //ProcedureHeading = PROCEDURE ident ( [FormalParameters] ).
 	private ProcedureHeadingNode ProcedureHeading() throws ParserAcceptError {
 		debug("---------------------- ProcedureHeading");
 		require(PROCEDURE);
 		IdentNode ident = Ident();
 		require(BRACE_ROUND_OPEN);
 		FormalParameterNode formalParameter = FormalParameters();
 		require(BRACE_ROUND_CLOSE);
 		return new ProcedureHeadingNode(ident, formalParameter);
 	}
 //ProcedureBody = Declarations BEGIN StatementSequence END.
 	private ProcedureBodyNode ProcedureBody() throws ParserAcceptError {
 		debug("---------------------- ProcedureBody");
 		debug("proc body declarations");
 		DeclarationNode declaration = Declarations();
 		require(BEGIN);
 		debug("proc body statements");
 		StatementSequenceNode statementSequence = StatementSequence();
 		require(END);
 		return new ProcedureBodyNode(declaration, statementSequence);
 	}
 //ProcedureDeclaration = ProcedureHeading ; ProcedureBody ident.
 	private ProcedureNode ProcedureDeclatation() throws ParserAcceptError {
 		debug("---------------------- ProcedureDeclaration");
 		debug("get proc head");
 		ProcedureHeadingNode procedureHeading = ProcedureHeading();
 		require(SEMICOLON);
 		debug("get proc body");
 		ProcedureBodyNode procedureBody = ProcedureBody();
 		require(IDENTIFER);
 		require(SEMICOLON);
 		return new ProcedureNode(procedureHeading, procedureBody);
 	}
 //Declarations = 
 //	[CONST ident = Expression ; {ident = Expression ;}] 
 //	[TYPE ident = Type ; {ident = Type ;}]
 //	[VAR IdentList : Type ; {IdentList : Type ;}] 
 // 	{ProcedureDeclaration ;}.
 	private DeclarationNode Declarations() throws ParserAcceptError {
 		DeclarationNode declaration = new DeclarationNode();
 		if (test(CONST)) {
 			debug("const");
 			declaration.add(Const());
 		}
 		if (test(TYPE)) {
 			debug("type");
 			declaration.add(Type());
 		}
 		if (test(VAR)) {
 			debug("var");
 			declaration.add(Var());
 		}
 		while (test(PROCEDURE)) {
 			debug("procedure");
 			declaration.add(ProcedureDeclatation());
 		}
 		return declaration;
 	}
 
 //	[CONST ident = Expression ; {ident = Expression ;}]
 	private ConstListNode Const() throws ParserAcceptError {
 		ConstListNode constList = new ConstListNode();
 		require(CONST);
 		IdentNode ident = Ident();
 		require(EQUAL);
 		AbstractNode expression = Expression();
 		require(SEMICOLON);
 		constList.add(new ConstNode(ident, expression));
 		while (test(IDENTIFER)) {
 			ident = Ident();
 			require(EQUAL);
 			expression = Expression();
 			require(SEMICOLON);
 			constList.add(new ConstNode(ident, expression));
 		}
 		return constList;
 	}
 // VAR IdentList : Type ; {IdentList : Type ;}
 	private VarListNode Var() throws ParserAcceptError {
 		VarListNode varList = new VarListNode();
 		require(VAR);
 		IdentListNode identList = IdentList();
 		require(COLON);
 		TypeNode type = Type();
 		require(SEMICOLON);
 		varList.add(new VarNode(identList,type));
 		while(test(IDENTIFER)) {
 			identList = IdentList();
 			require(COLON);
 			type = Type();
 			require(SEMICOLON);
 			varList.add(new VarNode(identList,type));	
 		}
 		return varList;
 	}
 	
 //Module = MODULE ident ; Declarations BEGIN StatementSequence END ident ..
 	private AbstractNode Module() throws ParserAcceptError {
 		require(MODULE);
 		debug("ident");
 		IdentNode ident = Ident();
 		require(SEMICOLON);
 		debug("declaration");
 		DeclarationNode declaration = Declarations();
 		require(BEGIN);
 		debug("statementSequence");
 		StatementSequenceNode statementSequence = StatementSequence();
 		require(END);
 		require(IDENTIFER);
 		require(DOT);
 		return new ModuleNode(ident, declaration, statementSequence);
 	}
 //Assignment = ident Selector := Expression.
 	private AssignmentNode Assignment() throws ParserAcceptError {
 		AssignmentNode node;
 		IdentNode ident = Ident();
 		SelectorNode selector = Selector();
 		require(ASSIGN);
 
 		return new AssignmentNode(ident, selector, Expression());
 	}
 //ActualParameters = Expression {, Expression}.
 	private ActualParametersNode ActualParameters() throws ParserAcceptError {
 		ActualParametersNode expressions = new ActualParametersNode();
 		expressions.add(Expression());
		while(require(COMMA)) {
 			expressions.add(Expression());
 		}
 		return expressions;
 	}
 //ProcedureCall = ident ( [ActualParameters] ).
 	private AbstractNode ProcedureCall() throws ParserAcceptError {
 		IdentNode ident = Ident();
 		require(BRACE_ROUND_OPEN);
 		ActualParametersNode actualParameters = ActualParameters();
 		require(BRACE_ROUND_CLOSE);
 		return new ProcedureCallNode(ident, actualParameters);
 	}
 //IfStatement = IF Expression THEN StatementSequence {ELSIF Expression THEN StatementSequence} [ELSE StatementSequence] END.
 	private IfStatementNode IfStatement() throws ParserAcceptError {
 		AbstractNode node, expression = null, elsif = null;
 		StatementSequenceNode statementSeq1 = null, statementSeq2 = null;
 		if (require(IF)) {
 			expression = Expression();
 		}
 		if (require(THEN)) {
 			statementSeq1 = StatementSequence();
 		}
 		if (test(ELSIF)) {
 			elsif = Elsif();
 		}
 		if (test(ELSE)) {
 			statementSeq2 = StatementSequence();
 		}
 		require(END);
 
 		return new IfStatementNode(expression, statementSeq1, elsif, statementSeq2);
 	}
 	private IfStatementNode Elsif() throws ParserAcceptError {
 		AbstractNode expression = null, elsif = null;
 		StatementSequenceNode statementSeq = null;
 		if (require(ELSIF)) {
 			expression = Expression();
 		}
 		if (require(THEN)) {
 			statementSeq = StatementSequence();
 		}
 		if (test(ELSIF)) {
 			elsif = Elsif();
 		}
 		return new IfStatementNode(expression, statementSeq, elsif, null);
 	}
 	//WhileStatement = WHILE Expression DO StatementSequence END.
 	private AbstractNode WhileStatement() throws ParserAcceptError {
 		AbstractNode node, expression;
 		StatementSequenceNode statementSequence;
 		node = null;
 		if (require(WHILE)) {
 			debug(current.toString());
 			expression = Expression();
 			if (require(DO)) {
 				statementSequence = StatementSequence();
 				if (require(END)) {
 					node = new WhileStatementNode(expression, statementSequence);
 				}
 			}
 		}
 		return node;
 	}
 //RepeatStatement = REPEAT StatementSequence UNTIL Expression.
 	private AbstractNode RepeatStatement() throws ParserAcceptError {
 		require(REPEAT);
 		StatementSequence();
 		require(UNTIL);
 		Expression();
 		return new EmptyNode();
 	}
 //Statement = [Assignment | ProcedureCall | IfStatement | PRINT Expression | WhileStatement | RepeatStatement].
 	private AbstractNode Statement() throws ParserAcceptError {
 		if (test(IDENTIFER)) {
 			if (testNext(BRACE_ROUND_OPEN)) {
 				return ProcedureCall();
 			}
 			else {
 				debug("statement assignment");
 				return new StatementNode(Assignment());
 			}
 		}
 		if (test(IF)) {
 			return IfStatement();
 		}
 		if (test(PRINT)) {
 			return Print();
 		}
 		if (test(WHILE)) {
 			debug("while statement");
 			debug(current.toString());
 			debug(next.toString());
 			return WhileStatement();
 		}
 		if (test(REPEAT)) {
 			return null;
 		}
 		return null; // Throw Statement Missing
 	}
 	private PrintNode Print() throws ParserAcceptError {
 		require(PRINT);
 		PrintNode node;
 		node = new PrintNode(Expression());
 		return node;
 	}
 	//StatementSequence = Statement {; Statement}.
 	private StatementSequenceNode StatementSequence() throws ParserAcceptError {
 		StatementSequenceNode statementSequence = new StatementSequenceNode();
 		debug("statement");
 		statementSequence.add(Statement());
 		while (test(SEMICOLON)) {
 			debug("more statement");
 			next();
 			statementSequence.add(Statement());
 		}
 		return statementSequence;
 	}
 //Selector = {. ident | [ Expression ]}.
 	private SelectorNode Selector() throws ParserAcceptError {
 		if (test(DOT)) {
 			next();
 			return new SelectorNode(Ident(), Selector());
 		}
 		if (test(BRACE_SQUARE_OPEN)) {
 			next();
 			AbstractNode expression = Expression();
 			require(BRACE_SQUARE_CLOSE);
 			return new SelectorNode(Expression(), Selector());
 		}
 		return new SelectorNode();
 	}
 //Factor = ident Selector | integer | string | Read | ( Expression ).
 	private AbstractNode Factor() throws ParserAcceptError {
 		AbstractNode node = new EmptyNode();
 		switch(current.getToken()) {
 		case IDENTIFER:
 			node = Ident();
 			break;
 		case INTEGER:
 			node = new IntegerNode(Integer.getInteger(current.getValue()));
 			next();
 			break;
 		// TODO: String
 		case READ:
 			node = Read();
 			break;
 		case BRACE_ROUND_OPEN:
 			next();
 			Expression();
 			require(BRACE_ROUND_CLOSE);
 			break;
 		}
 		return node;
 	}
 //String
 	private StringNode String() throws ParserAcceptError {
 		StringNode node = null;
 		if (test(STRING)) {
 			node = new StringNode(current.getValue().substring(1, current.getValue().length() - 1));
 			next();
 		}
 		return node;
 	}
 //Read = READ [Prompt].
 	private ReadNode Read() throws ParserAcceptError {
 		require(READ);
 		ReadNode node;
 		if (current.getToken() == STRING) {
 			node = new ReadNode(String());
 		} else {
 			node = new ReadNode();
 		}
 
 		return node;
 	}
 //Prompt = string.
 	private AbstractNode Prompt() {
 		// TODO: implement
 		return new EmptyNode();
 	}
 //Term = Factor {(* | /) Factor}.
 	private AbstractNode Term() throws ParserAcceptError {
 		AbstractNode node = Factor();
 
 		if (test(MATH_MUL) || test(MATH_DIV)) {
 			next();
 			node = new ExpressionNode(current.getToken(), node, Factor());
 		}
 
 		return node;
 	}
 //SimpleExpression = [-] Term {(+ | -) Term}.
 	private AbstractNode SimpleExpression() throws ParserAcceptError {
 		AbstractNode node;
 		if (test(MATH_SUB)) {
 			next();
 			node = new NegatedNode(Term());
 		} else {
 			node = Term();
 		}
 
 		if (test(MATH_ADD) || test(MATH_SUB)) {
 			// TODO: next() needed?
 			Tokens token = current.getToken();
 			next();
 			node = new ExpressionNode(token, node, Term());
 		}
 
 		return node;
 	}
 //Expression = SimpleExpression [(= | # | < | <= | > | >=) SimpleExpression].
 	private AbstractNode Expression() throws ParserAcceptError {
 		AbstractNode node;
 		node = SimpleExpression();
 		while (test(EQUAL) || test(NOT_EQUAL) || test(LESS) || test(LESS_EQUAL) || test(MORE) || test(MORE_EQUAL)) {
 			Tokens token = current.getToken();
 			next();
 			node = new ExpressionNode(token, node, SimpleExpression());
 			debug(current.toString());
 		}
 
 		return node;
 	}
 //IndexExpression = integer | ConstIdent.
 	private AbstractNode IndexExpression() {
 		next();
 		AbstractNode node = null;
 		// Have to do it this way because calling Integer() or ConstIdent() will move to next Token
 		switch (current.getToken()) {
 		case INTEGER:
 			node = new IntegerNode(Integer.getInteger(current.getValue()));
 			break;
 		case IDENTIFER:
 			node = new IdentNode(current.getValue());
 		default:
 			break;
 		}
 		return node;
 	}
 //ConstIdent = ident.
 	private IdentNode ConstIdent() throws ParserAcceptError {
 		IdentNode node = null;
 		if (test(IDENTIFER)) {
 			node = new IdentNode(current.getValue());
 		}
 		return node;
 	}
 
 	private IdentNode Ident() {
 		IdentNode node = null;
 		if (test(IDENTIFER)) {
 			debug("IdentNode");
 			debug(current.getValue().toString());
 			node = new IdentNode(current.getValue());
 			next();
 			debug(current.toString());
 		}
 		return node;
 	}
 	
 	public AbstractNode build() throws ParserAcceptError {
 		init();
 		return Module();
 	}
 
 	private void init() {
 		next();next(); // accept, or die
 	}
 	
 	private Yytoken next() {
 		current = next;
 		next = tokenStream.nextToken();
 		return current;
 	}
 	
 	/**
 	 * Test if current token matchs required token and overread
 	 * @param token
 	 * @return
 	 * @throws ParserAcceptError 
 	 */
 	private boolean require(Tokens token) throws ParserAcceptError {
 		if (test(token)) {
 			next();
 			return true;
 		}
 		else {
 			throw new ParserAcceptError(token, current);
 		}
 	}
 	
 	/**
 	 * Test if current token matches required token
 	 * @param token
 	 * @return
 	 */
 	private boolean test(Tokens token) {
 		return current.getToken() == token;
 	}
 	
 	/**
 	 * Test if next token matches required token
 	 * @param token
 	 * @return
 	 */
 	private boolean testNext(Tokens token) {
 		return !next.equals(null) && next.getToken() == token;
 	}
 	
 	private void debug(String string) {
 		if (DEBUG) System.out.println("-- DEBUG -- "+string);
 	}
 	
 	public Yytoken getCurrent() {
 		return current;
 	}
 	public Yytoken getNext() {
 		return next;
 	}
 }
