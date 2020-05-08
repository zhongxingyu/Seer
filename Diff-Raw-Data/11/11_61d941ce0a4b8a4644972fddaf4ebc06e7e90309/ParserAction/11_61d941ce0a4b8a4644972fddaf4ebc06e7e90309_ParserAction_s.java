 /**
  * 
  */
 package ee.olegus.fortran.parser;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.antlr.runtime.Token;
 
 import ee.olegus.fortran.ast.AccessStatement;
 import ee.olegus.fortran.ast.ActualArgument;
 import ee.olegus.fortran.ast.AllocateStatement;
 import ee.olegus.fortran.ast.ArrayConstructor;
 import ee.olegus.fortran.ast.ArraySpecificationElement;
 import ee.olegus.fortran.ast.AssignmentStatement;
 import ee.olegus.fortran.ast.Attribute;
 import ee.olegus.fortran.ast.AttributeDeclaration;
 import ee.olegus.fortran.ast.CaseStatement;
 import ee.olegus.fortran.ast.CloseStatement;
 import ee.olegus.fortran.ast.Code;
 import ee.olegus.fortran.ast.CommonStatement;
 import ee.olegus.fortran.ast.Constant;
 import ee.olegus.fortran.ast.CycleStatement;
 import ee.olegus.fortran.ast.DataReference;
 import ee.olegus.fortran.ast.Declaration;
 import ee.olegus.fortran.ast.DoStatement;
 import ee.olegus.fortran.ast.Entity;
 import ee.olegus.fortran.ast.ExitStatement;
 import ee.olegus.fortran.ast.Expression;
 import ee.olegus.fortran.ast.FunctionCall;
 import ee.olegus.fortran.ast.GotoStatement;
 import ee.olegus.fortran.ast.Identifier;
 import ee.olegus.fortran.ast.IfConstruct;
 import ee.olegus.fortran.ast.IfStatement;
 import ee.olegus.fortran.ast.ImplicitStatement;
 import ee.olegus.fortran.ast.ImpliedDo;
 import ee.olegus.fortran.ast.InquireStatement;
 import ee.olegus.fortran.ast.IntentStatement;
 import ee.olegus.fortran.ast.Interface;
 import ee.olegus.fortran.ast.NullifyStatement;
 import ee.olegus.fortran.ast.OpenStatement;
 import ee.olegus.fortran.ast.OperatorExpression;
 import ee.olegus.fortran.ast.PrintStatement;
 import ee.olegus.fortran.ast.ProcedureStatement;
 import ee.olegus.fortran.ast.ProgramUnit;
 import ee.olegus.fortran.ast.ReadStatement;
 import ee.olegus.fortran.ast.ReturnStatement;
 import ee.olegus.fortran.ast.SectionSubscript;
 import ee.olegus.fortran.ast.SelectCaseStatement;
 import ee.olegus.fortran.ast.SpecialExpression;
 import ee.olegus.fortran.ast.Statement;
 import ee.olegus.fortran.ast.StopStatement;
 import ee.olegus.fortran.ast.Subprogram;
 import ee.olegus.fortran.ast.SubroutineCall;
 import ee.olegus.fortran.ast.Subscript;
 import ee.olegus.fortran.ast.Type;
 import ee.olegus.fortran.ast.TypeDeclaration;
 import ee.olegus.fortran.ast.TypeSpecification;
 import ee.olegus.fortran.ast.Use;
 import ee.olegus.fortran.ast.WriteStatement;
 import ee.olegus.fortran.ast.Attribute.IntentType;
 import fortran.ofp.parser.java.FortranParser;
 import fortran.ofp.parser.java.IActionEnums;
 import fortran.ofp.parser.java.IFortranParserAction;
 import ee.olegus.fortran.ast.CommonBlock;
 import ee.olegus.fortran.ast.RewindStatement;
 import ee.olegus.fortran.ast.DataStatement;
 
 /**
  * @author olegus
  * 
  */
 public class ParserAction implements IFortranParserAction {
 
 	private FortranParser parser;
 	private ParserStack<Object> parseStack = new ParserStack<Object>();
 	
 	private Map<String, ProgramUnit> programUnits = new HashMap<String, ProgramUnit>();
 	private Map<String, Subprogram> subprograms = new HashMap<String, Subprogram>();
 	
 	private ParserActionUtils parserUtils;
 
 	public ParserAction(String[] args, FortranParser parser, String filename) {
 		this.parser = parser;
 		parserUtils = new ParserActionUtils(parser);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#ac_implied_do()
 	 */
 	@SuppressWarnings("unchecked")
 	public void ac_implied_do() {
 		ImpliedDo expr = (ImpliedDo) parseStack.pop();
 		expr.setValues((List<Expression>) parseStack.pop());
 		
 		parseStack.push(expr);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#ac_implied_do_control(boolean)
 	 */
 	public void ac_implied_do_control(boolean hasStride) {
 		ImpliedDo expr = new ImpliedDo();
 		
 		if(hasStride)
 			expr.setStride((Expression) parseStack.pop());
 		
 		expr.setLast((Expression) parseStack.pop());
 		expr.setFirst((Expression) parseStack.pop());
 		//expr.setVariable((DataReference) parseStack.pop()); // no IDENT yet parsed
 		
 		parseStack.push(expr);
 	}
 
 	public void ac_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void ac_value() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#ac_value_list(int)
 	 */
 	public void ac_value_list(int count) {
 		int startIndex = parseStack.popMark();
 		int actualCount = parseStack.size() - startIndex;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" ac elements instead of "+ count + " at " + parser.getTokenStream().LT(1));
 
 		List<Expression> elements = new ArrayList<Expression>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			Expression element = (Expression) parseStack.pop();
 			elements.add(element);
 		}
 		
 		parseStack.push(elements);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#ac_value_list__begin()
 	 */
 	public void ac_value_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void access_id() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#access_id_list(int)
 	 */
 	public void access_id_list(int count) {
 		int actualCount = parseStack.size() - parseStack.popMark();
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" access ids instead of "+ count + " at " + parser.getTokenStream().LT(1));			
 		
 		List<String> ids = new ArrayList<String>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			ids.add(0, (String) parseStack.pop());
 		}
 
 		parseStack.push(ids);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#access_id_list__begin()
 	 */
 	public void access_id_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void access_spec(Token keyword, int type) {
 		if(type == IActionEnums.AttrSpec_PUBLIC)
 			parseStack.push(Attribute.Type.PUBLIC);
 		else
 			parseStack.push(Attribute.Type.PRIVATE);		
 	}
 
 	public void access_stmt(Token label, Token eos, boolean hasList) {
 		AccessStatement access = new AccessStatement();
 		
 		if(hasList)
 			access.setNames((List<String>) parseStack.pop());
 		
 		access.setType((Attribute.Type) parseStack.pop());
 		
 		parseStack.push(access);
 	}
 
 	public void action_stmt() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#actual_arg(boolean,
 	 *      org.antlr.runtime.Token)
 	 */
 	public void actual_arg(boolean hasExpr, Token label) {
 		Expression expression = null;
 		if(hasExpr)
 			expression = (Expression) parseStack.pop();
 		ActualArgument arg = new ActualArgument(expression);
 		
 		parseStack.push(arg);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#actual_arg_spec(org.antlr.runtime.Token)
 	 */
 	public void actual_arg_spec(Token keyword) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#actual_arg_spec_list(int)
 	 */
 	public void actual_arg_spec_list(int count) {
 		// in case parser finds it to be substring range not arg list
 		// it informs this way about this error, so we rollback
 		if(count == -1) {
 			parseStack.popMark();
 			return;
 		}
 		
 		int initialSize = parseStack.popMark();
 		int actualCount = parseStack.size() - initialSize;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" actual args instead of "+ count + " at " + parser.getTokenStream().LT(1));
 		
 		List<ActualArgument> args = new ArrayList<ActualArgument>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			ActualArgument element = (ActualArgument) parseStack.pop();
 			args.add(element);
 		}
 		
 		parseStack.push(args);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#actual_arg_spec_list__begin()
 	 */
 	public void actual_arg_spec_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void add_op(Token addKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#add_operand(org.antlr.runtime.Token,
 	 *      int)
 	 */
 	public void add_operand(Token addOp, int numAddOps) {
 		parserUtils.collapseOperand(parseStack, numAddOps);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#add_operand__add_op(org.antlr.runtime.Token)
 	 */
 	public void add_operand__add_op(Token addOp) {
 		parseStack.push(addOp);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#alloc_opt(org.antlr.runtime.Token)
 	 */
 	public void alloc_opt(Token allocOpt) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#alloc_opt_list(int)
 	 */
 	public void alloc_opt_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#alloc_opt_list__begin()
 	 */
 	public void alloc_opt_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void allocatable_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void allocatable_stmt(Token label, Token keyword, Token eos, int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void allocate_co_array_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void allocate_co_shape_spec(boolean hasExpr) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocate_co_shape_spec_list(int)
 	 */
 	public void allocate_co_shape_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocate_co_shape_spec_list__begin()
 	 */
 	public void allocate_co_shape_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void allocate_object() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocate_object_list(int)
 	 */
 	public void allocate_object_list(int count) {
 		int startIndex = parseStack.popMark();
 		int actualCount = parseStack.size() - startIndex;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" alloc objects instead of "+ count + " at " + parser.getTokenStream().LT(1));
 
 		List<DataReference> elements = new ArrayList<DataReference>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			DataReference element = (DataReference) parseStack.pop();
 			elements.add(element);
 		}
 		
 		parseStack.push(elements);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocate_object_list__begin()
 	 */
 	public void allocate_object_list__begin() {
 		parseStack.pushMark();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocate_shape_spec(boolean,
 	 *      boolean)
 	 */
 	public void allocate_shape_spec(boolean hasLowerBound, boolean hasUpperBound) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocate_shape_spec_list(int)
 	 */
 	public void allocate_shape_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocate_shape_spec_list__begin()
 	 */
 	public void allocate_shape_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void allocate_stmt(Token label, Token allocateKeyword, Token eos, boolean hasTypeSpec, boolean hasAllocOptList) {
 		AllocateStatement stmt = new AllocateStatement(AllocateStatement.Type.ALLOCATE);
 		stmt.setDesignators((List<DataReference>) parseStack.pop());
 		
 		stmt.getLocation().start = parserUtils.findReverseToken(allocateKeyword, parser.getTokenStream());
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(stmt);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocation(boolean, boolean)
 	 */
 	public void allocation(boolean hasAllocateShapeSpecList,
 			boolean hasAllocateCoArraySpec) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocation_list(int)
 	 */
 	public void allocation_list(int count) {
 		int startIndex = parseStack.popMark();
 		int actualCount = parseStack.size() - startIndex;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" ac elements instead of "+ count + " at " + parser.getTokenStream().LT(1));
 
 		List<DataReference> elements = new ArrayList<DataReference>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			DataReference element = (DataReference) parseStack.pop();
 			elements.add(element);
 		}
 		
 		parseStack.push(elements);		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#allocation_list__begin()
 	 */
 	public void allocation_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void and_op(Token andOp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#and_operand(boolean, int)
 	 */
 	public void and_operand(boolean hasNotOp, int numAndOps) {
 		List<Expression> reversed = new ArrayList<Expression>(numAndOps+1);
 		for(int i=0; i<numAndOps+1; i++)
 			reversed.add((Expression) parseStack.pop());
 		
 		Expression left =  reversed.get(0);
 		for(int i=1; i<=numAndOps; i++) {
 			Expression right = reversed.get(i);
 			OperatorExpression op = new OperatorExpression(OperatorExpression.Operator.AND);
 			op.setLeftExpression(left);
 			op.setRightExpression(right);
 			left = op;
 		}
 		
 		parseStack.push(left);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#and_operand__not_op(boolean)
 	 */
 	public void and_operand__not_op(boolean hasNotOp) {
 		if(hasNotOp) {
 			Expression expr = (Expression) parseStack.pop();
 			OperatorExpression opExpr = new OperatorExpression(OperatorExpression.Operator.NOT);
 			opExpr.setLeftExpression(expr);
 			parseStack.push(opExpr);
 		}
 	}
 
 	public void arithmetic_if_stmt(Token label, Token ifKeyword, Token label1, Token label2, Token label3, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#array_constructor()
 	 */
 	@SuppressWarnings("unchecked")
 	public void array_constructor() {
 		ArrayConstructor ac = new ArrayConstructor();
 		ac.setValues((List<Expression>) parseStack.pop());
 		
 		parseStack.push(ac);		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#array_spec(int)
 	 */
 	public void array_spec(int count) {
 		List<ArraySpecificationElement> elements = new ArrayList<ArraySpecificationElement>(count);
 		for(int i=0; i<count; i++) {
 			ArraySpecificationElement element = (ArraySpecificationElement) parseStack.pop();
 			elements.add(element);
 		}
 		
 		parseStack.push(elements);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#array_spec_element(int)
 	 */
 	public void array_spec_element(int type) {
 		ArraySpecificationElement element;
 
 		switch (type) {
 		case IActionEnums.ArraySpecElement_expr:
 			element = new ArraySpecificationElement(ArraySpecificationElement.Type.EXPR);
 			element.setFirst((Expression) parseStack.pop());
 			break;
 		case IActionEnums.ArraySpecElement_colon:
 			element = new ArraySpecificationElement(ArraySpecificationElement.Type.COLON);
 			break;
 		case IActionEnums.ArraySpecElement_asterisk:
 			element = new ArraySpecificationElement(ArraySpecificationElement.Type.ASTERISK);
 			break;
 		case IActionEnums.ArraySpecElement_expr_colon:
 			element = new ArraySpecificationElement(ArraySpecificationElement.Type.EXPR_COLON);
 			element.setFirst((Expression) parseStack.pop());
 			break;
 		case IActionEnums.ArraySpecElement_expr_colon_asterisk:
 			element = new ArraySpecificationElement(ArraySpecificationElement.Type.EXPR_COLON_ASTERISK);
 			element.setFirst((Expression) parseStack.pop());
 			break;
 		case IActionEnums.ArraySpecElement_expr_colon_expr:
 			element = new ArraySpecificationElement(ArraySpecificationElement.Type.EXPR_COLON_EXPR);
 			element.setLast((Expression) parseStack.pop());
 			element.setFirst((Expression) parseStack.pop());
 			break;
 		default:
 			System.err.println("Invalid array specification element type "+type);
 			return;
 		}
 		
 		parseStack.push(element);
 	}
 
 	public void assign_stmt(Token label1, Token assignKeyword, Token label2, Token toKeyword, Token name, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void assigned_goto_stmt(Token label, Token goKeyword, Token toKeyword, Token name, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void assignment_stmt(Token label, Token eos) {
 		AssignmentStatement stmt = new AssignmentStatement();
 		stmt.setExpression((Expression) parseStack.pop());
 		stmt.setTarget((DataReference) parseStack.pop());
 
 		stmt.getLocation().start = parserUtils.findReverseToken(
 				FortranParser.T_ASSIGNMENT_STMT, parser.getTokenStream()) + 1;
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		parseStack.push(stmt);
 	}
 
 	public void associate_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void associate_stmt(Token label, Token id, Token associateKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void association(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#association_list(int)
 	 */
 	public void association_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#association_list__begin()
 	 */
 	public void association_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void asynchronous_stmt(Token label, Token keyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void attr_spec(Token attrKeyword, int attr) {
 		Attribute attribute = parserUtils.parseAttribute(parseStack, attr);
 		if(attribute == null)
 			return;
 		parseStack.push(attribute);	}
 
 
 	public void backspace_stmt(Token label, Token backspaceKeyword, Token eos, boolean hasPositionSpecList) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#bind_entity(org.antlr.runtime.Token,
 	 *      boolean)
 	 */
 	public void bind_entity(Token entity, boolean isCommonBlockName) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#bind_entity_list(int)
 	 */
 	public void bind_entity_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#bind_entity_list__begin()
 	 */
 	public void bind_entity_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void bind_stmt(Token label, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void binding_attr(Token bindingAttr, int attr, Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#binding_attr_list(int)
 	 */
 	public void binding_attr_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#binding_attr_list__begin()
 	 */
 	public void binding_attr_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void binding_private_stmt(Token label, Token privateKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void block() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void block_data() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void block_data_stmt(Token label, Token blockKeyword, Token dataKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void block_data_stmt__begin() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void block_do_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void bounds_remapping() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#bounds_remapping_list(int)
 	 */
 	public void bounds_remapping_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#bounds_remapping_list__begin()
 	 */
 	public void bounds_remapping_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void bounds_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#bounds_spec_list(int)
 	 */
 	public void bounds_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#bounds_spec_list__begin()
 	 */
 	public void bounds_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void boz_literal_constant(Token constant) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void call_stmt(Token label, Token callKeyword, Token eos, boolean hasActualArgSpecList) {
 		SubroutineCall call = new SubroutineCall();
 		if(hasActualArgSpecList) {
 			List<ActualArgument> list = (List<ActualArgument>) parseStack.pop();
 			Collections.reverse(list);
 			call.setArguments(list);
 		}
 		
 		call.setDesignator((DataReference) parseStack.pop());
 
 		call.getLocation().start = parserUtils.findReverseToken(callKeyword, parser.getTokenStream());
 		call.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(call);
 	}
 
 	public void case_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void case_selector(Token defaultToken) {
 		if(defaultToken!=null)
 			parseStack.push(defaultToken);
 	}
 
 	public void case_stmt(Token label, Token caseKeyword, Token id, Token eos) {
 		CaseStatement stmt = new CaseStatement();
 		
 		if(parseStack.peek() instanceof List) // selector (not default)
 			stmt.setSelector((List<Expression[]>) parseStack.pop());
 		else {
 			// Token T_DEFAULT
 			Object obj = parseStack.pop();
 		}
 		
 		parseStack.push(stmt);
 	}
 
 	public void case_value() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void case_value_range() {
 		// either this method or case_value_range_suffix() method construct range
 		// this method handles case with no start value given
 		
 		if(parseStack.peek() instanceof Expression) {
 			Expression[] range = new Expression[2];
 			range[1] = (Expression) parseStack.pop();
 
 			parseStack.push(range);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#case_value_range_list(int)
 	 */
 	public void case_value_range_list(int count) {
 		List<Expression[]> caseRange = new ArrayList<Expression[]>(2);
 		for(int i=0; i<count; i++)
 			caseRange.add(0,(Expression[]) parseStack.pop());
 		
 		parseStack.push(caseRange);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#case_value_range_list__begin()
 	 */
 	public void case_value_range_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void case_value_range_suffix() {
 		// either this method or case_value_range() method construct range
 		// this method handles case with start value given
 		
 		// TODO notify that grammar does not distinguish between value and value range
 		
 		Expression expr = (Expression) parseStack.pop();
 		
 		Expression[] range = new Expression[2];
 		range[0] = expr;
 		
 		if(parseStack.peek() instanceof Expression) {
 			// stop value is also given
 			range[1] = range[0];
 			range[0] = (Expression) parseStack.pop();
 		}
 		
 		parseStack.push(range);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#char_constant(org.antlr.runtime.Token)
 	 */
 	public void char_constant(Token id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#char_length(boolean)
 	 */
 	public void char_length(boolean hasTypeParamValue) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#char_literal_constant(org.antlr.runtime.Token,
 	 *      org.antlr.runtime.Token, org.antlr.runtime.Token)
 	 */
 	public void char_literal_constant(Token digitString, Token id, Token str) {
 		Constant constant = new Constant(Constant.Type.CHARACTER);
 		constant.setValue(str.getText());
 		parseStack.push(constant);
 	}
 
 	public void char_selector(Token tk1, Token tk2, int kindOrLen1, int kindOrLen2, boolean hasAsterisk) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void char_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void cleanUp() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void close_spec(Token closeSpec) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#close_spec_list(int)
 	 */
 	public void close_spec_list(int count) {
 		int actualCount = parseStack.size() - parseStack.popMark();
 		List<Expression> args = new ArrayList<Expression>(actualCount);
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" args instead of "+ count + " at " + parser.getTokenStream().LT(1));		
 		
 		
 		for(int i=0; i<actualCount; i++) {
 			args.add(0, (Expression) parseStack.pop());
 		}
 		
 		parseStack.push(args);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#close_spec_list__begin()
 	 */
 	public void close_spec_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void close_stmt(Token label, Token closeKeyword, Token eos) {
 		CloseStatement stmt = new CloseStatement();
 		parseStack.pop(); // args
 		
 		parseStack.push(stmt);
 	}	
 	
 	public void co_array_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void common_block_name(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void common_block_object(Token id, boolean hasShapeSpecList) {
 		Entity entity = new Entity(id.getText());
 		if(hasShapeSpecList)
 			entity.setArraySpecification((List<ArraySpecificationElement>)parseStack.pop());
 		parseStack.push(entity);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#common_block_object_list(int)
 	 */
 	public void common_block_object_list(int count) {
 		CommonBlock block=new CommonBlock();
 		List<Entity> entities=new ArrayList<Entity>(count);
 		for(int i=0; i<count; i++)
 			entities.add((Entity)parseStack.pop());
 		Collections.reverse(entities);
 		block.setEntities(entities);
 		parseStack.push(block);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#common_block_object_list__begin()
 	 */
 	public void common_block_object_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void common_stmt(Token label, Token commonKeyword, Token eos, int numBlocks) {
 		CommonStatement stmt = new CommonStatement();
 		for (int i=0; i<numBlocks; i++)
 			stmt.addBlock((CommonBlock)parseStack.pop());
 		parseStack.push(stmt);
 	}
 
 	public void complex_literal_constant() {
 		Constant constant = new Constant(Constant.Type.COMPLEX);
 		Constant realConstant = (Constant) parseStack.pop();
 		Constant imagConstant = (Constant) parseStack.pop();
 		parseStack.push(constant);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_array_spec(boolean)
 	 */
 	public void component_array_spec(boolean isExplicit) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void component_attr_spec(Token attrKeyword, int specType) {
 		Attribute attribute = parserUtils.parseAttribute(parseStack, specType);
 		if(attribute == null)
 			return;
 		parseStack.push(attribute);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_attr_spec_list(int)
 	 */
 	public void component_attr_spec_list(int count) {
 		int startIndex = parseStack.popMark();
 		int actualCount = parseStack.size() - startIndex;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" attributes instead of "+ count + " at " + parser.getTokenStream().LT(1));
 
 		Set<Attribute> attributes = new HashSet<Attribute>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			Attribute attribute = (Attribute) parseStack.pop();
 			attributes.add(attribute);
 		}
 		
 		parseStack.push(attributes);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_attr_spec_list__begin()
 	 */
 	public void component_attr_spec_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void component_data_source() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_decl(org.antlr.runtime.Token,
 	 *      boolean, boolean, boolean, boolean)
 	 */
 	@SuppressWarnings("unchecked")
 	public void component_decl(Token id, boolean hasComponentArraySpec,
 			boolean hasCoArraySpec, boolean hasCharLength,
 			boolean hasComponentInitialization) {
 		Entity entity = new Entity(id.getText());
 		
 		if(hasComponentInitialization) {
 			entity.setInitialization((Expression) parseStack.pop());
 		}
 		
 		if(hasComponentArraySpec) {
 			entity.setArraySpecification((List<ArraySpecificationElement>) parseStack.pop());
 		}
 		
 		parseStack.push(entity);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_decl_list(int)
 	 */
 	public void component_decl_list(int count) {
 		int actualCount = parseStack.size() - parseStack.popMark();
 		
 		if(actualCount != count) {
 			System.err.println("Parser incomplete: " + actualCount + " comp_decl instead of " +
 					count + " at " + parser.getTokenStream().LT(1));
 		}
 		
 		List<Entity> entities = new LinkedList<Entity>();
 		for(int i=0; i<actualCount; i++) {
 			entities.add(0, (Entity) parseStack.pop());
 		}
 		
 		parseStack.push(entities);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_decl_list__begin()
 	 */
 	public void component_decl_list__begin() {
 		parseStack.pushMark();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_def_stmt(int)
 	 */
 	public void component_def_stmt(int type) {
 		// TODO Auto-generated method stub
 
 	}	
 	
 	public void component_initialization() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void component_spec(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_spec_list(int)
 	 */
 	public void component_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#component_spec_list__begin()
 	 */
 	public void component_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void computed_goto_stmt(Token label, Token goKeyword, Token toKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void concat_op(Token concatKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void connect_spec(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#connect_spec_list(int)
 	 */
 	public void connect_spec_list(int count) {
 		int actualCount = parseStack.size() - parseStack.popMark();
 		List<Expression> args = new ArrayList<Expression>(actualCount);
 
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" args instead of "+ count + " at " + parser.getTokenStream().LT(1));		
 		
 		for(int i=0; i<actualCount; i++) {
 			args.add(0, (Expression) parseStack.pop());
 		}
 		
 		parseStack.push(args);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#connect_spec_list__begin()
 	 */
 	public void connect_spec_list__begin() {
 		parseStack.pushMark();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#constant(org.antlr.runtime.Token)
 	 */
 	public void constant(Token id) {
 		System.out.println(id);
 	}
 
 	public void contains_stmt(Token label, Token keyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void continue_stmt(Token label, Token continueKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void cycle_stmt(Token label, Token cycleKeyword, Token id, Token eos) {
 		parseStack.push(new CycleStatement());
 	}
 
 	public void data_component_def_stmt(Token label, Token eos, boolean hasSpec) {
 		TypeDeclaration typeDeclaration = new TypeDeclaration();
 		
 		typeDeclaration.setEntities((List<Entity>) parseStack.pop());
 		if(hasSpec)
 			typeDeclaration.setAttributes((Set<Attribute>) parseStack.pop());
 		
 		TypeSpecification typeSpec = (TypeSpecification) parseStack.pop();
 		typeDeclaration.setTypeSpecification(typeSpec);
 		
 		typeDeclaration.getLocation().start = typeSpec.getLocation().start;
 		typeDeclaration.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 
 		parseStack.push(typeDeclaration);
 	}
 
 	public void data_i_do_object() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#data_i_do_object_list(int)
 	 */
 	public void data_i_do_object_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#data_i_do_object_list__begin()
 	 */
 	public void data_i_do_object_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void data_implied_do(Token id, boolean hasThirdExpr) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void data_pointer_object() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#data_ref(int)
 	 */
 	public void data_ref(int numPartRef) {
 		
 		Stack<DataReference> reverseStack = new Stack<DataReference>();
 		for(int i=0; i<numPartRef; i++) {
 			reverseStack.push((DataReference) parseStack.pop());
 		}
 		
 		DataReference data, component;
 		for(int i=0; i<numPartRef-1; i++) {
 			component = (DataReference) reverseStack.pop();
 			data = (DataReference) reverseStack.pop();
 			data.setBase(component);
 			reverseStack.push(data);
 		}
 		
 		data = reverseStack.pop();
 		
 		parseStack.push(data);
 	}
 
 	public void data_stmt(Token label, Token keyword, Token eos, int count) {
 		DataStatement stmt = new DataStatement();
 		stmt.setValues((List<Object>)parseStack.pop());
 		stmt.setObjects((List<DataReference>)parseStack.pop());
 		parseStack.push(stmt);
 	}
 
 	public void data_stmt_constant() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void data_stmt_object() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#data_stmt_object_list(int)
 	 */
 	public void data_stmt_object_list(int count) {
 		List<DataReference> objs = new ArrayList<DataReference>(count);
 		for(int i=0; i<count; i++) {
 			objs.add((DataReference)parseStack.pop());
 		}
 		Collections.reverse(objs);
 		parseStack.push(objs);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#data_stmt_object_list__begin()
 	 */
 	public void data_stmt_object_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void data_stmt_set() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void data_stmt_value(Token asterisk) {
 		if(asterisk!=null) {
 			Constant c = (Constant) parseStack.pop();
 			parseStack.pop(); // ignore multiplicity for now
 			parseStack.push(c);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#data_stmt_value_list(int)
 	 */
 	public void data_stmt_value_list(int count) {
 		List<Object> values = new ArrayList<Object>(count);
 		for(int i=0; i<count; i++) {
 			values.add(parseStack.pop());
 		}
 		Collections.reverse(values);
 		parseStack.push(values);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#data_stmt_value_list__begin()
 	 */
 	public void data_stmt_value_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void dealloc_opt(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#dealloc_opt_list(int)
 	 */
 	public void dealloc_opt_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#dealloc_opt_list__begin()
 	 */
 	public void dealloc_opt_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void deallocate_stmt(Token label, Token deallocateKeyword, Token eos, boolean hasDeallocOptList) {
 		AllocateStatement stmt = new AllocateStatement(AllocateStatement.Type.DEALLOCATE);
 		stmt.setDesignators((List<DataReference>) parseStack.pop());
 
 		stmt.getLocation().start = parserUtils.findReverseToken(deallocateKeyword, parser.getTokenStream());
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		parseStack.push(stmt);
 	}
 
 	public void declaration_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void declaration_type_spec(Token udtKeyword, int type) {
 		TypeSpecification typeSpecification;
 		
 		switch (type) {
 		case IActionEnums.DeclarationTypeSpec_INTRINSIC:
 			break;
 		case IActionEnums.DeclarationTypeSpec_TYPE:
 			typeSpecification = new TypeSpecification((String) parseStack.pop());
 			typeSpecification.getLocation().start = parserUtils.findReverseToken(udtKeyword, parser.getTokenStream());
 			typeSpecification.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 			parseStack.push(typeSpecification);
 			break;
 
 		default:
 			System.err.println("Parser incomplete: decl type spec at "+ parser.getTokenStream().LT(1));
 			return;
 		}		
 	}
 
 
 	public void default_char_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void default_logical_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void deferred_co_shape_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#deferred_co_shape_spec_list(int)
 	 */
 	public void deferred_co_shape_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#deferred_co_shape_spec_list__begin()
 	 */
 	public void deferred_co_shape_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#deferred_shape_spec_list(int)
 	 */
 	public void deferred_shape_spec_list(int count) {
 		List<ArraySpecificationElement> arraySpec = new ArrayList<ArraySpecificationElement>(count);
 		
 		for(int i=0; i<count; i++) {
 			arraySpec.add(new ArraySpecificationElement(ArraySpecificationElement.Type.COLON));
 		}
 		
 		parseStack.push(arraySpec);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#deferred_shape_spec_list__begin()
 	 */
 	public void deferred_shape_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void defined_binary_op(Token binaryOp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void defined_operator(Token definedOp, boolean isExtended) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void defined_unary_op(Token definedOp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void derived_type_def() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#derived_type_spec(org.antlr.runtime.Token,
 	 *      boolean)
 	 */
 	public void derived_type_spec(Token typeName, boolean hasTypeParamSpecList) {
 		parseStack.push(typeName.getText());
 		// TODO handle list
 	}
 
 	public void derived_type_stmt(Token label, Token keyword, Token id, Token eos, boolean hasTypeAttrSpecList, boolean hasGenericNameList) {
 		Type type = new Type(id.getText());
 		
 		type.getLocation().start = parserUtils.findReverseToken(keyword, parser.getTokenStream());
 		parseStack.push(type);
 		parseStack.pushMark();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#designator(boolean)
 	 */
 	public void designator(boolean hasSubstringRange) {
 		
 		if(hasSubstringRange) { 
 			List<Subscript> list = new ArrayList<Subscript>(1);
 			list.add((Subscript) parseStack.pop());
 			
 			DataReference ref = new DataReference();
 			Collections.reverse(list);
 			ref.setSections(list);
 			
 			DataReference component = (DataReference) parseStack.pop();
 			ref.setBase(component);
 						
 			parseStack.push(ref);
 		}
 	}
 
 	public void designator_or_func_ref() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	public void dimension_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {
 		Entity entity = new Entity(id.getText());
 		if(hasArraySpec)
 			entity.setArraySpecification((List<ArraySpecificationElement>) parseStack.pop());
 		
 		parseStack.push(entity);
 	}
 
 	public void dimension_spec(Token dimensionKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void dimension_stmt(Token label, Token keyword, Token eos, int count) {
 		AttributeDeclaration decl = new AttributeDeclaration(Attribute.Type.DIMENSION);
 		
 		for(int i=0; i<count; i++) {
 			Entity entity = (Entity) parseStack.pop();
 			decl.getEntities().add(entity);
 		}
 		parseStack.push(decl);
 	}
 
 	public void do_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void do_stmt(Token label, Token id, Token doKeyword, Token digitString, Token eos, boolean hasLoopControl) {
 		DoStatement stmt;
 		
 		if(hasLoopControl) {
 			Expression expr = (Expression) parseStack.pop();
 			
 			if(parseStack.peek() instanceof Expression) { // for do
 				stmt = new DoStatement(DoStatement.Type.FOR);
 				List<Expression> exprs = new ArrayList<Expression>();
 				exprs.add(0, expr);
 				for(int i=0; i<3 && parseStack.size()>0 && parseStack.peek() instanceof Expression; i++) {
 					exprs.add(0,(Expression) parseStack.pop());
 				}
 				stmt.setVariable((DataReference) exprs.get(0));
 				stmt.setFirst(exprs.get(1));
 				stmt.setLast(exprs.get(2));
 				if(exprs.size()>3)
 					stmt.setStep(exprs.get(3));
 				
 			} else { // while do
 				stmt = new DoStatement(DoStatement.Type.WHILE);
 				stmt.setCondition(expr);
 			}
 		
 		} else { // no loop control
 			stmt = new DoStatement(DoStatement.Type.NONE);
 		}
 		
 		if(label!=null)
 			stmt.setLabel(label.getText());
 		
 		if(id!=null)
 			stmt.setDoId(id.getText());
 
 		if(digitString!=null)
 			stmt.setDoLabel(digitString.getText());
 		
 		stmt.getLocation().start = parserUtils.findReverseToken(doKeyword, parser.getTokenStream());
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(stmt);
 		parseStack.pushMark();
 	}
 
 	public void do_term_action_stmt(Token label, Token endKeyword, Token doKeyword, Token id, Token eos) {
 		parserUtils.collapseLoopsOnLabel(label, parseStack);
 	}
 
 	public void do_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void dtio_generic_spec(Token rw, Token format, int type) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void dtv_type_spec(Token typeKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#dummy_arg(org.antlr.runtime.Token)
 	 */
 	@SuppressWarnings("unchecked")
 	public void dummy_arg(Token dummy) {
 		parseStack.add(dummy.getText());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#dummy_arg_list(int)
 	 */
 	public void dummy_arg_list(int count) {
 		List<String> argumentNames = new ArrayList<String>(count);
 		for(int i=count-1; i>=0; i--)
 			argumentNames.add(0, (String) parseStack.pop());
 		parseStack.push(argumentNames);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#dummy_arg_list__begin()
 	 */
 	public void dummy_arg_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void else_if_stmt(Token label, Token elseKeyword, Token ifKeyword, Token thenKeyword, Token id, Token eos) {
 		IfStatement stmt = new IfStatement(IfStatement.Type.ELSEIFTHEN);
 		stmt.setCondition((Expression) parseStack.pop());
 		
 		List<Statement> stmts = parserUtils.collectStatements(parseStack);
 		IfStatement prevStmt = (IfStatement) parseStack.peek();
 		prevStmt.setStatements(stmts);
 		
 		stmt.getLocation().start = parserUtils.findReverseToken(elseKeyword, parser.getTokenStream());
 		prevStmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream(), stmt.getLocation().start);
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.popMark();
 		parseStack.push(stmt);
 		parseStack.pushMark();
 	}
 
 	public void else_stmt(Token label, Token elseKeyword, Token id, Token eos) {
 		IfStatement stmt = new IfStatement(IfStatement.Type.ELSE);
 				
 		List<Statement> stmts = parserUtils.collectStatements(parseStack);
 		IfStatement prevStmt = (IfStatement) parseStack.peek();
 		prevStmt.setStatements(stmts);
 				
 		stmt.getLocation().start = parserUtils.findReverseToken(elseKeyword, parser.getTokenStream());
 		prevStmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream(), stmt.getLocation().start);		
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 
 		parseStack.popMark();
 		parseStack.push(stmt);
 		parseStack.pushMark();
 	}
 
 	public void elsewhere_stmt(Token label, Token elseKeyword, Token whereKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void elsewhere_stmt__end(int numBodyConstructs) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void end_associate_stmt(Token label, Token endKeyword, Token associateKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void end_block_data_stmt(Token label, Token endKeyword, Token blockKeyword, Token dataKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void end_do() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void end_do_stmt(Token label, Token endKeyword, Token doKeyword, Token id, Token eos) {
 		List<Statement> statements = parserUtils.collectStatements(parseStack);
 		DoStatement stmt = (DoStatement) parseStack.peek();
 		stmt.setStatements(statements);
 		
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.popMark();
 	}
 
 	public void end_enum_stmt(Token label, Token endKeyword, Token enumKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void end_forall_stmt(Token label, Token endKeyword, Token forallKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void end_function_stmt(Token label, Token keyword1, Token keyword2, Token name, Token eos) {
 		List<Subprogram> subprograms = parserUtils.collectSubprograms(parseStack);
 		List<Statement> stmts = parserUtils.collectStatements(parseStack);
 		parseStack.popMark();
 		
 		Subprogram function = (Subprogram) parseStack.pop();
 		function.addSubprograms(subprograms);
 		function.setConstructs(stmts);
 		function.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(function);
 	}
 
 	public void end_if_stmt(Token label, Token endKeyword, Token ifKeyword, Token id, Token eos) {
 		List<Statement> stmts = parserUtils.collectStatements(parseStack);
 		IfStatement prevStmt = (IfStatement) parseStack.peek();		
 		prevStmt.setStatements(stmts);
 		prevStmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.popMark();		
 	}
 
 	public void end_interface_stmt(Token label, Token kw1, Token kw2, Token eos, boolean hasGenericSpec) {
 		List<Statement> statements = new ArrayList<Statement>(2);
 		List<Subprogram> subprograms = new ArrayList<Subprogram>(2);
 		
 		int index = parseStack.popMark();
 		while(parseStack.size() > index) {
 			if(parseStack.peek() instanceof Statement)
 				statements.add(0, (Statement) parseStack.pop());
 			else
 				subprograms.add(0, (Subprogram) parseStack.pop());
 		}
 		
 		Interface intf = (Interface) parseStack.pop();
 		intf.setStatements(statements);
 		// TODO add subprograms
 		
 		parseStack.push(intf);
 	}
 
 	public void end_module_stmt(Token label, Token endKeyword, Token moduleKeyword, Token id, Token eos) {
 		List<Subprogram> subprograms = parserUtils.collectSubprograms(parseStack);
 		
 		ProgramUnit currentUnit = (ProgramUnit) parseStack.pop();
 		currentUnit.addSubprograms(subprograms);
 		
 		currentUnit.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());		
 		
 		programUnits.put(currentUnit.getName().toLowerCase(), currentUnit);
 		parseStack.popMark();		
 	}
 
 	public void end_of_file(String filename) {
 	}
 
 	public void end_of_stmt(Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void end_program_stmt(Token label, Token endKeyword, Token programKeyword, Token id, Token eos) {
 		List<Subprogram> subprograms = parserUtils.collectSubprograms(parseStack); 
 		List<Statement> statements = parserUtils.collectStatements(parseStack);
 		parseStack.popMark();		
 		
 		ProgramUnit currentUnit = (ProgramUnit) parseStack.pop();
 		currentUnit.addSubprograms(subprograms);
 		currentUnit.setConstructs(statements);
 
 		currentUnit.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());		
 		
 		programUnits.put(currentUnit.getName(), currentUnit);
 	}
 
 	public void end_select_stmt(Token label, Token endKeyword, Token selectKeyword, Token id, Token eos) {
 		List list = new ArrayList();
 		while(!(parseStack.peek() instanceof SelectCaseStatement)) {
 			list.add(parseStack.pop());
 		}
 		
 		SelectCaseStatement stmt = (SelectCaseStatement) parseStack.pop();
 		Collections.reverse(list);
 		
 		CaseStatement caseStatement = null;
 		for(int i=0; i<list.size(); i++) {
 			if(list.get(i) instanceof CaseStatement) {
 				caseStatement = (CaseStatement) list.get(i);
 				stmt.getCaseStatements().add(caseStatement);
 			} else
 				caseStatement.getBlock().add((Statement) list.get(i));
 		}
 		
 		parseStack.push(stmt);
 	}
 
 	public void end_select_type_stmt(Token label, Token endKeyword, Token selectKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void end_subroutine_stmt(Token label, Token keyword1, Token keyword2, Token name, Token eos) {
 		List<Subprogram> subprograms = parserUtils.collectSubprograms(parseStack); 
 		List<Statement> stmts = parserUtils.collectStatements(parseStack);
 		parseStack.popMark();
 		
 		Subprogram subroutine = (Subprogram) parseStack.peek();
 		subroutine.addSubprograms(subprograms);
 		subroutine.setConstructs(stmts);
 		subroutine.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 	}
 
 	public void end_type_stmt(Token label, Token endKeyword, Token typeKeyword, Token id, Token eos) {
 		
 		int actualCount = parseStack.size() - parseStack.popMark();
 		
 		List<Declaration> decls = new ArrayList<Declaration>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			decls.add(0, (Declaration)parseStack.pop());
 		}
 		
 		Type derivedType = 	(Type) parseStack.pop(); // type
 		derivedType.setDeclarations(decls);
 		
 		derivedType.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(derivedType);
 	}
 
 	public void end_where_stmt(Token label, Token endKeyword, Token whereKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void endfile_stmt(Token label, Token endKeyword, Token fileKeyword, Token eos, boolean hasPositionSpecList) {
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#entity_decl(org.antlr.runtime.Token)
 	 */
 	@SuppressWarnings("unchecked")
 	public void entity_decl(Token id) {
 		Entity entity = new Entity(id.getText());
 		
 		// TODO for now expr before is assumed to be initalization
 		// only if it is within declaration list
 		if(parseStack.peekMark() < parseStack.size() &&
 				parseStack.peek() instanceof Expression) {
 			entity.setInitialization((Expression) parseStack.pop());
 		}
 		
 		// TODO for now List before is assumed to be array specification
 		// only if it is within declaration list
 		if(parseStack.peekMark() < parseStack.size() &&
 				parseStack.peek() instanceof List) {
 			entity.setArraySpecification((List<ArraySpecificationElement>) parseStack.pop());
 		}
 		
 		parseStack.push(entity);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#entity_decl_list(int)
 	 */
 	public void entity_decl_list(int count) {
 		int startIndex = parseStack.popMark();
 		int actualCount = parseStack.size() - startIndex;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" entities instead of "+ count + " at " + parser.getTokenStream().LT(1));
 
 		List<Entity> entities = new ArrayList<Entity>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			Entity element = (Entity) parseStack.pop();
 			entities.add(element);
 		}
 		
 		parseStack.push(entities);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#entity_decl_list__begin()
 	 */
 	public void entity_decl_list__begin() {
 		parseStack.pushMark();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#enum_def(int)
 	 */
 	public void enum_def(int numEls) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#enum_def_stmt(org.antlr.runtime.Token,
 	 *      org.antlr.runtime.Token)
 	 */
 	public void enum_def_stmt(Token label, Token id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void enum_def_stmt(Token label, Token enumKeyword, Token bindKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void enumerator(Token id, boolean hasExpr) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void enumerator_def_stmt(Token label, Token enumeratorKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#enumerator_list(int)
 	 */
 	public void enumerator_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#enumerator_list__begin()
 	 */
 	public void enumerator_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void equiv_op(Token equivOp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#equiv_operand(int)
 	 */
 	public void equiv_operand(int numEquivOps) {
 		parserUtils.collapseOperand(parseStack, numEquivOps);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#equiv_operand__equiv_op(org.antlr.runtime.Token)
 	 */
 	public void equiv_operand__equiv_op(Token equivOp) {
 		parseStack.push(equivOp);
 	}
 
 	public void equivalence_object() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#equivalence_object_list(int)
 	 */
 	public void equivalence_object_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#equivalence_object_list__begin()
 	 */
 	public void equivalence_object_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void equivalence_set() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#equivalence_set_list(int)
 	 */
 	public void equivalence_set_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#equivalence_set_list__begin()
 	 */
 	public void equivalence_set_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void equivalence_stmt(Token label, Token equivalenceKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void executable_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void execution_part() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void execution_part_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void exit_stmt(Token label, Token exitKeyword, Token id, Token eos) {
 		ExitStatement stmt = new ExitStatement();
 		if (id!=null)
 			stmt.setKeyword(id.getText());
 		parseStack.push(stmt);
 	}
 
 	public void explicit_co_shape_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void explicit_co_shape_spec_suffix() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#explicit_shape_spec(boolean)
 	 */
 	public void explicit_shape_spec(boolean hasUpperBound) {
 		ArraySpecificationElement elem = new ArraySpecificationElement(ArraySpecificationElement.Type.COLON);
 		
 		if(hasUpperBound)
 			elem.setLast((Expression) parseStack.pop());
 		elem.setFirst((Expression) parseStack.pop());
 		
 		parseStack.push(elem);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#explicit_shape_spec_list(int)
 	 */
 	public void explicit_shape_spec_list(int count) {
 		int actualCount = parseStack.size() - parseStack.popMark();
 		List<ArraySpecificationElement> arraySpec = new ArrayList<ArraySpecificationElement>(actualCount);
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" array spec elements instead of "+ count + " at " + parser.getTokenStream().LT(1));
 		
 		for(int i=0; i<actualCount; i++) {
 			arraySpec.add((ArraySpecificationElement) parseStack.pop());
 		}
 		
 		parseStack.push(arraySpec);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#explicit_shape_spec_list__begin()
 	 */
 	public void explicit_shape_spec_list__begin() {
 		parseStack.pushMark();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#expr()
 	 */
 	public void expr() {
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#ext_function_subprogram(boolean)
 	 */
 	public void ext_function_subprogram(boolean hasPrefix) {
 	}
 
 	public void extended_intrinsic_op() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void external_stmt(Token label, Token externalKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void file_unit_number() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void final_binding(Token finalKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void flush_spec(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#flush_spec_list(int)
 	 */
 	public void flush_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#flush_spec_list__begin()
 	 */
 	public void flush_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	public void flush_stmt(Token label, Token flushKeyword, Token eos, boolean hasFlushSpecList) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#forall_assignment_stmt(boolean)
 	 */
 	public void forall_assignment_stmt(boolean isPointerAssignment) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void forall_body_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void forall_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void forall_construct_stmt(Token label, Token id, Token forallKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void forall_header() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void forall_stmt(Token label, Token forallKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void forall_stmt__begin() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#forall_triplet_spec(org.antlr.runtime.Token,
 	 *      boolean)
 	 */
 	public void forall_triplet_spec(Token id, boolean hasStride) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#forall_triplet_spec_list(int)
 	 */
 	public void forall_triplet_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#forall_triplet_spec_list__begin()
 	 */
 	public void forall_triplet_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void format() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#format_item(org.antlr.runtime.Token,
 	 *      boolean)
 	 */
 	public void format_item(Token descOrDigit, boolean hasFormatItemList) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#format_item_list(int)
 	 */
 	public void format_item_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#format_item_list__begin()
 	 */
 	public void format_item_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#format_specification(boolean)
 	 */
 	public void format_specification(boolean hasFormatItemList) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void format_stmt(Token label, Token formatKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#function_reference(boolean)
 	 */
 	public void function_reference(boolean hasActualArgSpecList) {
 		List argList = null;
 		if(hasActualArgSpecList) {
 			 argList = (List) parseStack.pop();
 			 Collections.reverse(argList);
 		}
 		
 		DataReference dataReference = (DataReference) parseStack.pop();
 		FunctionCall call = new FunctionCall(dataReference);
 		if(argList!=null)
 			call.setArguments(argList);
 
 		parseStack.push(call);
 	}
 
 	public void function_stmt(Token label, Token keyword, Token name, Token eos, boolean hasGenericNameList, boolean hasSuffix) {
 		Subprogram function = new Subprogram();
 		function.setType(Subprogram.Type.FUNCTION);
 		function.setName(name.getText());
 		if(hasGenericNameList)
 			function.setArgumentNames((List<String>) parseStack.pop());
 		
 		int statementStartIndex = (Integer)parseStack.pop();
 		function.getLocation().start = statementStartIndex;
 		
 		parseStack.push(function);
 		parseStack.pushMark();
 	}
 
 	public void function_stmt__begin() {
 		parseStack.push(parser.getTokenStream().index());
 	}
 
 	public void function_subprogram(boolean hasExePart, boolean hasIntSubProg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void generic_binding(Token genericKeyword, boolean hasAccessSpec) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#generic_name_list(int)
 	 */
 	public void generic_name_list(int count) {
 		List<String> argumentNames = new ArrayList<String>();
 		for(int i=count-1; i>=0; i--)
 			argumentNames.add(0, (String) parseStack.pop());
 		parseStack.push(argumentNames);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#generic_name_list__begin()
 	 */
 	public void generic_name_list__begin() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#generic_name_list_part(org.antlr.runtime.Token)
 	 */
 	public void generic_name_list_part(Token ident) {
 		parseStack.push(ident.getText());
 	}
 
 	public void generic_spec(Token keyword, Token name, int type) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public Stack<Object> getParseStack() {
 		return parseStack;
 	}
 
 	public Map<String, ProgramUnit> getProgramUnits() {
 		return programUnits;
 	}
 
 	public void goto_stmt(Token goKeyword, Token toKeyword, Token label, Token eos) {
 		GotoStatement stmt = new GotoStatement();
 		
 		parseStack.push(stmt);
 	}
 
 	public void hollerith_constant(Token hollerithConstant) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void if_construct() {
 		IfConstruct construct = new IfConstruct();
 		
 		List<IfStatement> list = new ArrayList<IfStatement>();
 		while(parseStack.peek() instanceof IfStatement) {
 			IfStatement stmt = (IfStatement) parseStack.pop(); 
 			list.add(stmt);
 			if (stmt.getType()==IfStatement.Type.IF || stmt.getType()==IfStatement.Type.IFTHEN)
 				break;			
 		}
 		
 		Collections.reverse(list);
 		construct.setIfStatements(list);
 		
 		parseStack.push(construct);
 	}
 
 	public void if_stmt(Token label, Token ifKeyword) {
 		IfStatement stmt = new IfStatement(IfStatement.Type.IF);
 		
 		// check it because of bug in parser (T_ASSIGNMENT_STMT not inserted)
 		if (parseStack.peek() instanceof Statement)
 			stmt.setAction((Statement) parseStack.pop());
 		stmt.setCondition((Expression) parseStack.pop());
 		
 		stmt.getLocation().start = parserUtils.findReverseToken(ifKeyword, parser.getTokenStream());
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(stmt);
 	}
 
 	public void if_stmt__begin() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void if_then_stmt(Token label, Token id, Token ifKeyword, Token thenKeyword, Token eos) {
 		IfStatement stmt = new IfStatement(IfStatement.Type.IFTHEN);
 		stmt.setCondition((Expression) parseStack.pop());
 
 		stmt.getLocation().start = parserUtils.findReverseToken(ifKeyword, parser.getTokenStream());
 		stmt.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(stmt);
 		parseStack.pushMark();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#imag_part(boolean, boolean,
 	 *      org.antlr.runtime.Token)
 	 */
 	public void imag_part(boolean hasIntConstant, boolean hasRealConstant,
 			Token id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void image_selector(int exprCount) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void implicit_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#implicit_spec_list(int)
 	 */
 	public void implicit_spec_list(int count) {
 		// for the moment ignore
 		for(int i=0; i<count; i++)
 			parseStack.pop(); // type
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#implicit_spec_list__begin()
 	 */
 	public void implicit_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void implicit_stmt(Token label, Token implicitKeyword, Token noneKeyword, Token eos, boolean hasImplicitSpecList) {
 		Declaration decl = new ImplicitStatement();
 		parseStack.push(decl);
 	}
 
 	public void import_stmt(Token label, Token importKeyword, Token eos, boolean hasGenericNameList) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#initialization(boolean, boolean)
 	 */
 	public void initialization(boolean hasExpr, boolean hasNullInit) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void input_item() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#input_item_list(int)
 	 */
 	public void input_item_list(int count) {
 		int initialSize = parseStack.popMark();
 		int actualCount = parseStack.size() - initialSize;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" input args instead of "+ count + " at " + parser.getTokenStream().LT(1));
 		
 		List<Expression> args = new ArrayList<Expression>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			Expression expr = (Expression) parseStack.pop();
 			args.add(expr);
 		}
 		
 		parseStack.push(args);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#input_item_list__begin()
 	 */
 	public void input_item_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void inquire_spec(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#inquire_spec_list(int)
 	 */
 	public void inquire_spec_list(int count) {
 		int initialSize = parseStack.popMark();
 		int actualCount = parseStack.size() - initialSize;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" inquire elements instead of "+ count + " at " + parser.getTokenStream().LT(1));
 		
 		List<Expression> args = new ArrayList<Expression>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			Expression expr = (Expression) parseStack.pop();
 			args.add(expr);
 		}
 		
 		parseStack.push(args);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#inquire_spec_list__begin()
 	 */
 	public void inquire_spec_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void inquire_stmt(Token label, Token inquireKeyword, Token id, Token eos, boolean isType2) {
 		InquireStatement stmt = new InquireStatement();
 		parseStack.pop(); // inquire list
 		
 		parseStack.push(stmt);	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#int_constant(org.antlr.runtime.Token)
 	 */
 	public void int_constant(Token id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#int_literal_constant(org.antlr.runtime.Token,
 	 *      org.antlr.runtime.Token)
 	 */
 	public void int_literal_constant(Token digitString, Token kindParam) {
 		Constant constant = new Constant(Constant.Type.INTEGER);
 		constant.setValue(digitString.getText());
 		if(kindParam!=null)
 			constant.setKind(kindParam.getText());
 		parseStack.push(constant);
 	}
 
 	public void int_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void intent_spec(Token intentKeyword1, Token intentKeyword2, int intent) {
 		if(intent==IActionEnums.IntentSpec_IN)
 			parseStack.push(Attribute.IntentType.IN);
 		else if(intent==IActionEnums.IntentSpec_OUT)
 			parseStack.push(Attribute.IntentType.OUT);
 		else if(intent==IActionEnums.IntentSpec_INOUT)
 			parseStack.push(Attribute.IntentType.INOUT);
 	}
 
 	public void intent_stmt(Token label, Token keyword, Token eos) {
 		IntentStatement stmt = new IntentStatement();
 		stmt.setEntities((List<String>) parseStack.pop());
 		stmt.setIntentType((IntentType) parseStack.pop());
 		
 		parseStack.push(stmt);
 	}
 
 	public void interface_block() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void interface_body(boolean hasPrefix) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void interface_specification() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void interface_stmt(Token label, Token abstractToken, Token keyword, Token eos, boolean hasGenericSpec) {
 		Interface intf = new Interface();
 		//if(hasGenericSpec) // generic_spec not in parser
 		//	intf.setName((String) parseStack.pop());
 		
 		parseStack.push(intf);
 		parseStack.pushMark();		
 	}
 
 	public void interface_stmt__begin() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void internal_subprogram() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#internal_subprogram_part(int)
 	 */
 	public void internal_subprogram_part(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void intrinsic_operator() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void intrinsic_stmt(Token label, Token intrinsicToken, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void intrinsic_type_spec(Token keyword1, Token keyword2, int type, boolean hasKindSelector) {
 		TypeSpecification typeSpecification;
 		
 		switch (type) {
 		case IActionEnums.IntrinsicTypeSpec_INTEGER:
 			typeSpecification = new TypeSpecification(TypeSpecification.Intrinsic.INTEGER.name());
 			break;
 		case IActionEnums.IntrinsicTypeSpec_REAL:
 			typeSpecification = new TypeSpecification(TypeSpecification.Intrinsic.REAL.name());
 			break;
 		case IActionEnums.IntrinsicTypeSpec_COMPLEX:
 			typeSpecification = new TypeSpecification(TypeSpecification.Intrinsic.COMPLEX.name());
 			break;
 		case IActionEnums.IntrinsicTypeSpec_LOGICAL:
 			typeSpecification = new TypeSpecification(TypeSpecification.Intrinsic.LOGICAL.name());
 			break;
 		case IActionEnums.IntrinsicTypeSpec_CHARACTER:
 			typeSpecification = new TypeSpecification(TypeSpecification.Intrinsic.CHARACTER.name());
 			break;			
 		case IActionEnums.IntrinsicTypeSpec_DOUBLEPRECISION:
 			typeSpecification = new TypeSpecification(TypeSpecification.Intrinsic.DOUBLEPRECISION.name());
 			break;			
 		case IActionEnums.IntrinsicTypeSpec_DOUBLECOMPLEX:
 			typeSpecification = new TypeSpecification(TypeSpecification.Intrinsic.DOUBLECOMPLEX.name());
 			break;			
 
 		default:
 			System.err.println("Unknown intrinsic "+type);
 			return;
 		}
 		
 		if(hasKindSelector)
 			typeSpecification.setKind((Expression) parseStack.pop());
 		
 		typeSpecification.getLocation().start = parserUtils.findReverseToken(keyword1, parser.getTokenStream());
 		typeSpecification.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(typeSpecification);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#io_control_spec(boolean,
 	 *      org.antlr.runtime.Token, boolean)
 	 */
 	public void io_control_spec(boolean hasExpression, Token keyword,
 			boolean hasAsterisk) {
 		
 		Expression expr = null;
 		if(hasExpression)
 			expr = (Expression) parseStack.pop();
 		
 		ActualArgument arg = new ActualArgument(expr);
 		if(keyword != null)
 			arg.setName(keyword.getText());
 		
 		parseStack.push(arg);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#io_control_spec_list(int)
 	 */
 	public void io_control_spec_list(int count) {
 		int initialSize = parseStack.popMark();
 		int actualCount = parseStack.size() - initialSize;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" io args instead of "+ count + " at " + parser.getTokenStream().LT(1));
 		
 		List<ActualArgument> args = new ArrayList<ActualArgument>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			ActualArgument element = (ActualArgument) parseStack.pop();
 			args.add(element);
 		}
 		
 		parseStack.push(args);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#io_control_spec_list__begin()
 	 */
 	public void io_control_spec_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void io_implied_do() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void io_implied_do_control() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void io_implied_do_object() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void io_unit() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void keyword() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void kind_param(Token kind) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see parser.java.IFortranParserAction#kind_selector(org.antlr.runtime.Token, org.antlr.runtime.Token, boolean)
 	 */
 	public void kind_selector(Token token1, Token token2, boolean hasExpression) {
 		if(!hasExpression) {
 			Constant constant = new Constant(Constant.Type.INTEGER);
 			constant.setValue(token2.getText());
 			parseStack.push(constant);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#label(org.antlr.runtime.Token)
 	 */
 	public void label(Token lbl) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void label_do_stmt(Token label, Token id, Token doKeyword, Token digitString, Token eos, boolean hasLoopControl) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#label_list(int)
 	 */
 	public void label_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#label_list__begin()
 	 */
 	public void label_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void language_binding_spec(Token keyword, Token id, boolean hasName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void length_selector(Token len, int kindOrLen, boolean hasAsterisk) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void letter_spec(Token id1, Token id2) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#letter_spec_list(int)
 	 */
 	public void letter_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#letter_spec_list__begin()
 	 */
 	public void letter_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#level_1_expr(org.antlr.runtime.Token)
 	 */
 	public void level_1_expr(Token definedUnaryOp) {
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#level_2_expr(int)
 	 */
 	public void level_2_expr(int numConcatOps) {
 
 		List<Object> reversed = new ArrayList<Object>(numConcatOps);
 		for(int i=0; i<numConcatOps+1; i++)
 			reversed.add(parseStack.pop());
 		
 		Expression left = (Expression) reversed.get(0);
 		for(int i=1; i<=numConcatOps; i++) {
 			Expression right = (Expression) reversed.get(i);
 			OperatorExpression op = new OperatorExpression(OperatorExpression.Operator.CONCAT);
 			op.setLeftExpression(left);
 			op.setRightExpression(right);
 			left = op;
 		}
 		
 		parseStack.push(left);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#level_3_expr(org.antlr.runtime.Token)
 	 */
 	public void level_3_expr(Token relOp) {
 		if(relOp==null)
 			return;
 		
 		OperatorExpression.Operator op = parserUtils.findOperator(relOp.getType());
 		OperatorExpression opExpr = new OperatorExpression(op);
 		opExpr.setRightExpression((Expression) parseStack.pop());
 		opExpr.setLeftExpression((Expression) parseStack.pop());
 		
 		parseStack.push(opExpr);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#level_5_expr(int)
 	 */
 	public void level_5_expr(int numDefinedBinaryOps) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#level_5_expr__defined_binary_op(org.antlr.runtime.Token)
 	 */
 	public void level_5_expr__defined_binary_op(Token definedBinaryOp) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void literal_constant() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void logical_literal_constant(Token logicalValue, boolean isTrue, Token kindParam) {
 		Constant constant = new Constant(Constant.Type.LOGICAL);
 		if(kindParam!=null)
 			constant.setKind(kindParam.getText());
 		parseStack.push(constant);
 	}
 
 	public void logical_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void loop_control(Token whileKeyword, boolean hasOptExpr) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#main_program(boolean, boolean,
 	 *      boolean)
 	 */
 	public void main_program(boolean hasProgramStmt, boolean hasExecutionPart,
 			boolean hasInternalSubprogramPart) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#main_program__begin()
 	 */
 	public void main_program__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void masked_elsewhere_stmt(Token label, Token elseKeyword, Token whereKeyword, Token id, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void masked_elsewhere_stmt__end(int numBodyConstructs) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void module() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void module_nature(Token nature) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void module_stmt(Token label, Token moduleKeyword, Token id, Token eos) {
 		ProgramUnit currentUnit = new ProgramUnit();
 		currentUnit.setUnitType(ProgramUnit.UnitType.MODULE);
 		currentUnit.setName(id.getText());
 		
 		currentUnit.getLocation().start = parserUtils.findReverseToken(moduleKeyword, parser.getTokenStream());
 		
 		parseStack.push(currentUnit);
 		parseStack.pushMark();
 	}
 
 	public void module_stmt__begin() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void module_subprogram(boolean hasPrefix) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void module_subprogram_part() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mult_op(Token multKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#mult_operand(int)
 	 */
 	public void mult_operand(int numMultOps) {
 		parserUtils.collapseOperand(parseStack, numMultOps);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#mult_operand__mult_op(org.antlr.runtime.Token)
 	 */
 	public void mult_operand__mult_op(Token multOp) {
 		parseStack.push(multOp);
 	}
 
 	public void name(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void named_constant_def(Token id) {
 		Entity entity = new Entity(id.getText());
 		entity.setInitialization((Expression)parseStack.pop()); // value
 		parseStack.push(entity);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#named_constant_def_list(int)
 	 */
 	public void named_constant_def_list(int count) {
 		List<Entity> entities = new ArrayList<Entity>();
 		for(int i=0; i<count; i++)
 			entities.add((Entity)parseStack.pop());
 		Collections.reverse(entities);
 		parseStack.push(entities);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#named_constant_def_list__begin()
 	 */
 	public void named_constant_def_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void namelist_group_name(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void namelist_group_object(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#namelist_group_object_list(int)
 	 */
 	public void namelist_group_object_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#namelist_group_object_list__begin()
 	 */
 	public void namelist_group_object_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void namelist_stmt(Token label, Token keyword, Token eos, int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void not_op(Token notOp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#null_init(org.antlr.runtime.Token)
 	 */
 	public void null_init(Token id) {
 		Constant nul = new Constant(Constant.Type.NULL);
 		parseStack.push(nul);
 	}
 
 	public void nullify_stmt(Token label, Token nullifyKeyword, Token eos) {
 		NullifyStatement stmt = new NullifyStatement();
 		stmt.setArguments((List<DataReference>) parseStack.pop());
 		
 		parseStack.push(stmt);
 	}
 
 	public void only(boolean hasGenericSpec, boolean hasRename, boolean hasOnlyUseName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#only_list(int)
 	 */
 	public void only_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#only_list__begin()
 	 */
 	public void only_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void open_stmt(Token label, Token openKeyword, Token eos) {
 		OpenStatement stmt = new OpenStatement();
 		parseStack.pop(); // args
 		
 		parseStack.push(stmt);
 	}
 
 	public void optional_stmt(Token label, Token keyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void or_op(Token orOp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#or_operand(int)
 	 */
 	public void or_operand(int numOrOps) {
 		List<Expression> reversed = new ArrayList<Expression>(numOrOps);
 		for(int i=0; i<numOrOps+1; i++)
 			reversed.add((Expression) parseStack.pop());
 		
 		Expression left =  reversed.get(0);
 		for(int i=1; i<=numOrOps; i++) {
 			Expression right = reversed.get(i);
 			OperatorExpression op = new OperatorExpression(OperatorExpression.Operator.OR);
 			op.setLeftExpression(left);
 			op.setRightExpression(right);
 			left = op;
 		}
 		
 		parseStack.push(left);
 	}
 
 	public void output_item() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#output_item_list(int)
 	 */
 	public void output_item_list(int count) {
 		int initialSize = parseStack.popMark();
 		int actualCount = parseStack.size() - initialSize;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" output args instead of "+ count + " at " + parser.getTokenStream().LT(1));
 		
 		List<Expression> args = new ArrayList<Expression>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			Expression expr = (Expression) parseStack.pop();
 			args.add(expr);
 		}
 		
 		parseStack.push(args);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#output_item_list__begin()
 	 */
 	public void output_item_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void parameter_stmt(Token label, Token keyword, Token eos) {
 	    AttributeDeclaration decl = new AttributeDeclaration(Attribute.Type.PARAMETER);
 	    decl.setEntities((List<Entity>)parseStack.pop());
 	    parseStack.push(decl);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void part_ref(Token id, boolean hasSelectionSubscriptList, boolean hasImageSelector) {
 
 		DataReference data = new DataReference();
 		data.setId(new Identifier(id.getText()));
 		if(hasSelectionSubscriptList) {
 			List<Subscript> list = (List<Subscript>) parseStack.pop();
 			Collections.reverse(list);
 			data.setSections(list);
 		}
 		
 		int index = parserUtils.findReverseToken(id, parser.getTokenStream());
 		data.getId().getLocation().start = index;		
 		data.getId().getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());		
 		
 		parseStack.push(data);		
 	}
 
 	public void pause_stmt(Token label, Token pauseKeyword, Token constant, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void pointer_assignment_stmt(Token label, Token eos, boolean hasBoundsSpecList, boolean hasBRList) {
 		// TODO handle bounds
 		AssignmentStatement stmt = new AssignmentStatement(AssignmentStatement.Type.POINTER_ASSIGNMENT);
 		
 		stmt.setExpression((Expression) parseStack.pop());
 		stmt.setTarget((DataReference) parseStack.pop());
 		
 		parseStack.push(stmt);
 	}
 
 	public void pointer_decl(Token id, boolean hasSpecList) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#pointer_decl_list(int)
 	 */
 	public void pointer_decl_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#pointer_decl_list__begin()
 	 */
 	public void pointer_decl_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void pointer_object() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#pointer_object_list(int)
 	 */
 	public void pointer_object_list(int count) {
 		int actualCount = parseStack.size() - parseStack.popMark();
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" pointers instead of "+ count + " at " + parser.getTokenStream().LT(1));
 		
 		List<DataReference> subscripts = new ArrayList<DataReference>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			DataReference element = (DataReference) parseStack.pop();
 			subscripts.add(element);
 		}
 		
 		parseStack.push(subscripts);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#pointer_object_list__begin()
 	 */
 	public void pointer_object_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void pointer_stmt(Token label, Token keyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void position_spec(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#position_spec_list(int)
 	 */
 	public void position_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#position_spec_list__begin()
 	 */
 	public void position_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void power_op(Token powerKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#power_operand(boolean)
 	 */
 	public void power_operand(boolean hasPowerOperand) {
 		if(hasPowerOperand) {
 			Expression right = (Expression) parseStack.pop();
 			Expression left = (Expression) parseStack.pop();
 			OperatorExpression opExpr = new OperatorExpression(OperatorExpression.Operator.POW);
 			opExpr.setLeftExpression(left);
 			opExpr.setRightExpression(right);
 			parseStack.push(opExpr);
 			
 			// commented until power_operand__power_op is called from parser
 			//parserUtils.collapseOperand(parseStack, 1);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#power_operand__power_op(org.antlr.runtime.Token)
 	 */
 	public void power_operand__power_op(Token powerOp) {
 		// Not called in original parser at the moment
 		//parseStack.push(powerOp);
 	}
 
 	public void prefix(int specCount) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void prefix_spec(boolean isDecTypeSpec) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#primary()
 	 */
 	public void primary() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void print_stmt(Token label, Token printKeyword, Token eos, boolean hasOutputItemList) {
 		PrintStatement stmt = new PrintStatement();
 		
 		if(hasOutputItemList) {
 			stmt.setOutputs((List<Expression>) parseStack.pop());
 		}
 		
 		// TODO problem is that asterisk is not reported and we have to guess
 		// say char constant is what we are looking for
 		if(parseStack.size() > 0 && parseStack.peek() instanceof Constant &&
 				((Constant)parseStack.peek()).getConstantType()==Constant.Type.CHARACTER)
 			stmt.setFormat((Expression) parseStack.pop());
 		
 		parseStack.push(stmt);
 	}
 
 	public void private_components_stmt(Token label, Token privateKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void private_or_sequence() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void proc_attr_spec(Token attrKeyword, Token id, int spec) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void proc_binding_stmt(Token label, int type, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void proc_component_attr_spec(Token attrSpecKeyword, Token id, int specType) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#proc_component_attr_spec_list(int)
 	 */
 	public void proc_component_attr_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#proc_component_attr_spec_list__begin()
 	 */
 	public void proc_component_attr_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void proc_component_def_stmt(Token label, Token procedureKeyword, Token eos, boolean hasInterface) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#proc_decl(org.antlr.runtime.Token,
 	 *      boolean)
 	 */
 	public void proc_decl(Token id, boolean hasNullInit) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#proc_decl_list(int)
 	 */
 	public void proc_decl_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#proc_decl_list__begin()
 	 */
 	public void proc_decl_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#proc_interface(org.antlr.runtime.Token)
 	 */
 	public void proc_interface(Token id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void proc_language_binding_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void proc_pointer_object() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void procedure_declaration_stmt(Token label, Token procedureKeyword, Token eos, boolean hasProcInterface, int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void procedure_designator() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void procedure_stmt(Token label, Token module, Token procedureKeyword, Token eos) {
 		ProcedureStatement stmt = new ProcedureStatement();
 		stmt.setModule(module.getText());
 		stmt.setNames((List<String>) parseStack.pop());
 		
 		parseStack.push(stmt);
 	}
 
 	public void program_stmt(Token label, Token programKeyword, Token id, Token eos) {
 		ProgramUnit currentUnit = new ProgramUnit();
 		currentUnit.setUnitType(ProgramUnit.UnitType.PROGRAM);
 		currentUnit.setName(id.getText());
 		
 		currentUnit.getLocation().start = parserUtils.findReverseToken(programKeyword, parser.getTokenStream());		
 		
 		parseStack.push(currentUnit);
 		parseStack.pushMark();
 	}
 
 	public void protected_stmt(Token label, Token keyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void read_stmt(Token label, Token readKeyword, Token eos, boolean hasInputItemList) {
 		ReadStatement stmt = new ReadStatement();
 		if(hasInputItemList)
 			stmt.setInputs((List<Expression>) parseStack.pop());
 		
 		// TODO problem is that asterisk is not reported and we have to guess
 		// say char constant is what we are looking for
 		if(parseStack.size() > 0 && parseStack.peek() instanceof Constant &&
 				((Constant)parseStack.peek()).getConstantType()==Constant.Type.CHARACTER) {
 			List<ActualArgument> args = new ArrayList<ActualArgument>(1);
 			args.set(0, new ActualArgument((Constant)parseStack.pop()));
 			stmt.setControls(args);
 		} else if(parseStack.size() > 0 && parseStack.peek() instanceof List) {
 			// another posibility is io-control-spec list
 			stmt.setControls((List<ActualArgument>) parseStack.pop());
 		}
 		
 		parseStack.push(stmt);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#real_literal_constant(org.antlr.runtime.Token,
 	 *      org.antlr.runtime.Token)
 	 */
 	public void real_literal_constant(Token realConstant, Token kindParam) {
 		Constant constant = new Constant(Constant.Type.REAL);
 		constant.setValue(realConstant.getText());
 		if(kindParam!=null)
 			constant.setKind(kindParam.getText());
 		parseStack.push(constant);
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#real_part(boolean, boolean,
 	 *      org.antlr.runtime.Token)
 	 */
 	public void real_part(boolean hasIntConstant, boolean hasRealConstant,
 			Token id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void rel_op(Token relOp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void rename(Token id1, Token id2, Token op1, Token defOp1, Token op2, Token defOp2) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#rename_list(int)
 	 */
 	public void rename_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#rename_list__begin()
 	 */
 	public void rename_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void result_name() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void return_stmt(Token label, Token keyword, Token eos, boolean hasScalarIntExpr) {
 		ReturnStatement stmt = new ReturnStatement();
 		
 		if(hasScalarIntExpr)
 			stmt.setExpression((Expression) parseStack.pop());
 		
 		parseStack.push(stmt);
 	}
 
 	public void rewind_stmt(Token label, Token rewindKeyword, Token eos, boolean hasPositionSpecList) {
 		parseStack.pop(); // file unit number or spec
 		parseStack.push(new RewindStatement());
 	}
 
 	public void save_stmt(Token label, Token keyword, Token eos, boolean hasSavedEntityList) {
 		AttributeDeclaration decl = new AttributeDeclaration(Attribute.Type.SAVE);
 		if(hasSavedEntityList) {
 			decl.setEntities((List<Entity>)parseStack.pop());
 		}
 		parseStack.push(decl);
 	}
 
 	public void saved_entity(Token id, boolean isCommonBlockName) {
 		if(!isCommonBlockName) {
 			Entity entity = new Entity(id.getText());
 			parseStack.push(entity);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#saved_entity_list(int)
 	 */
 	public void saved_entity_list(int count) {
 		List<Entity> entities = new ArrayList<Entity>(count);
 		for(int i=0; i<count; i++) {
 			entities.add((Entity)parseStack.pop());
 		}
 		Collections.reverse(entities);
 		parseStack.push(entities);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#saved_entity_list__begin()
 	 */
 	public void saved_entity_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void scalar_char_constant() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void scalar_constant() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void scalar_default_char_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void scalar_default_logical_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void scalar_int_constant() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void scalar_int_literal_constant() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void scalar_int_variable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#section_subscript(boolean, boolean,
 	 *      boolean, boolean)
 	 */
 	public void section_subscript(boolean hasLowerBound, boolean hasUpperBound,
 			boolean hasStride, boolean isAmbiguous) {
 		if(isAmbiguous) {
 			Subscript subscript = new Subscript((Expression) parseStack.pop());
 			parseStack.push(subscript);
 			
 		} else {
 			SectionSubscript subscript = new SectionSubscript();
 			if(hasStride)
 				subscript.setStride((Expression) parseStack.pop());
 			if(hasUpperBound)
 				subscript.setLast((Expression) parseStack.pop());
 			if(hasLowerBound)
 				subscript.setFirst((Expression) parseStack.pop());
 			parseStack.push(subscript);			
 		}		
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#section_subscript_list(int)
 	 */
 	public void section_subscript_list(int count) {
 		int initialSize = parseStack.popMark();
 		int actualCount = parseStack.size() - initialSize;
 		
 		if(actualCount != count)
 			System.err.println("Parser incomplete: "+actualCount+
 					" subscripts instead of "+ count + " at " + parser.getTokenStream().LT(1));
 		
 		List<Subscript> subscripts = new ArrayList<Subscript>(actualCount);
 		for(int i=0; i<actualCount; i++) {
 			Subscript element = (Subscript) parseStack.pop();
 			subscripts.add(element);
 		}
 		
 		// try to decide whether these are subscripts or arguments
 		//boolean areArguments = parserUtils.isArgumentList(subscripts);
 		
 		parseStack.push(subscripts);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#section_subscript_list__begin()
 	 */
 	public void section_subscript_list__begin() {
 		parseStack.pushMark();
 	}
 
 	public void select_case_stmt(Token label, Token id, Token selectKeyword, Token caseKeyword, Token eos) {
 		SelectCaseStatement stmt = new SelectCaseStatement();
 		
 		stmt.setExpression((Expression) parseStack.pop());
 		
 		parseStack.push(stmt);
 	}
 
 	public void select_type(Token selectKeyword, Token typeKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void select_type_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void select_type_stmt(Token label, Token selectConstructName, Token associateName, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void selector() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void sequence_stmt(Token label, Token sequenceKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#signed_int_literal_constant(org.antlr.runtime.Token)
 	 */
 	public void signed_int_literal_constant(Token sign) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#signed_real_literal_constant(org.antlr.runtime.Token)
 	 */
 	public void signed_real_literal_constant(Token sign) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void specific_binding(Token procedureKeyword, Token interfaceName, Token bindingName, Token procedureName, boolean hasBindingAttrList) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#specification_part(int, int, int)
 	 */
 	public void specification_part(int numUseStmts, int numImportStmts,
 			int numDeclConstructs) {
 		
 		// declarations
 		List<Declaration> declarations = new LinkedList<Declaration>();
 		for(int i=0; i<numDeclConstructs; i++) {
 			Object obj = parseStack.pop();
 			if(!(obj instanceof Declaration)) {
 				System.err.println("Not declaration: " + obj );
 				continue;
 			}
 			declarations.add(0, (Declaration) obj);
 		}
 
 		// uses
 		List<Use> uses = new ArrayList<Use>();
 		for(int i=0; i<numUseStmts; i++) {
 			uses.add((Use) parseStack.pop());
 		}
 		
 		if(parseStack.size()>0) {
 			if(parseStack.peek() instanceof Code) {
 				Code code = (Code)parseStack.peek(); 
 				code.addUses(uses);
 				code.setDeclarations(declarations);
 			}
 		}
 
 	}
 
 	public void specification_stmt() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void stmt_function_stmt(Token label, Token functionName, Token eos, boolean hasGenericNameList) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void stmt_label_list() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#stop_code(org.antlr.runtime.Token)
 	 */
 	public void stop_code(Token digitString) {
		// TODO Auto-generated method stub

 	}
 
 	public void stop_stmt(Token label, Token stopKeyword, Token eos, boolean hasStopCode) {
 		StopStatement stmt = new StopStatement();
 		if (hasStopCode)
 			stmt.setStopCode((Expression) parseStack.pop());
 		parseStack.push(stmt);
 	}
 
 	public void structure_constructor(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void subroutine_stmt(Token label, Token keyword, Token name, Token eos, boolean hasPrefix, boolean hasDummyArgList, boolean hasBindingSpec, boolean hasArgSpecifier) {
 		Subprogram subroutine = new Subprogram();
 		subroutine.setType(Subprogram.Type.SUBROUTINE);
 		subroutine.setName(name.getText());
 		if(hasDummyArgList)
 			subroutine.setArgumentNames((List<String>) parseStack.pop());
 		
 		int statementStopIndex = parserUtils.findReverseToken(parser.getTokenStream());
 		int statementStartIndex = (Integer) parseStack.pop();
 		subroutine.getLocation().start = statementStartIndex;
 		
 		parseStack.push(subroutine);
 		parseStack.pushMark();
 	}
 
 	public void subroutine_stmt__begin() {
 		parseStack.push(parser.getTokenStream().index());
 	}
 
 	public void substr_range_or_arg_list_suffix() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#substring(boolean)
 	 */
 	public void substring(boolean hasSubstringRange) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#substring_range(boolean, boolean)
 	 */
 	public void substring_range(boolean hasLowerBound, boolean hasUpperBound) {
 		SectionSubscript subscript = new SectionSubscript();
 		
 		if(hasUpperBound)
 			subscript.setLast((Expression) parseStack.pop());
 		if(hasLowerBound)
 			subscript.setFirst((Expression) parseStack.pop());
 		
 		parseStack.push(subscript);
 	}
 
 	public void substring_range_or_arg_list() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void suffix(Token resultKeyword, boolean hasProcLangBindSpec) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void t_prefix(int specCount) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void t_prefix_spec(Token spec) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void target_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void target_stmt(Token label, Token keyword, Token eos, int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void type_attr_spec(Token keyword, Token id, int specType) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_attr_spec_list(int)
 	 */
 	public void type_attr_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_attr_spec_list__begin()
 	 */
 	public void type_attr_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void type_bound_procedure_part(int count, boolean hasBindingPrivateStmt) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void type_declaration_stmt(Token label, int numAttributes, Token eos) {
 		TypeDeclaration typeDeclaration = new TypeDeclaration();
 		typeDeclaration.setEntities((List<Entity>) parseStack.pop());
 		
 		for(int i=0; i<numAttributes; i++) {
 			typeDeclaration.getAttributes().add((Attribute) parseStack.pop());
 		}
 		
 		TypeSpecification typeSpec = (TypeSpecification) parseStack.pop();
 		typeDeclaration.setTypeSpecification(typeSpec);
 		
 		typeDeclaration.getLocation().start = typeSpec.getLocation().start;
 		typeDeclaration.getLocation().stop = parserUtils.findReverseToken(parser.getTokenStream());
 		
 		parseStack.push(typeDeclaration);
 	}
 
 	public void type_guard_stmt(Token label, Token typeKeyword, Token isOrDefaultKeyword, Token selectConstructName, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void type_param_attr_spec(Token kindOrLen) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_param_decl(org.antlr.runtime.Token,
 	 *      boolean)
 	 */
 	public void type_param_decl(Token id, boolean hasInit) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_param_decl_list(int)
 	 */
 	public void type_param_decl_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_param_decl_list__begin()
 	 */
 	public void type_param_decl_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void type_param_or_comp_def_stmt(Token eos, int type) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void type_param_or_comp_def_stmt_list() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_param_spec(org.antlr.runtime.Token)
 	 */
 	public void type_param_spec(Token keyWord) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_param_spec_list(int)
 	 */
 	public void type_param_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_param_spec_list__begin()
 	 */
 	public void type_param_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#type_param_value(boolean, boolean,
 	 *      boolean)
 	 */
 	public void type_param_value(boolean hasExpr, boolean hasAsterisk,
 			boolean hasColon) {
 		if(hasAsterisk)
 			parseStack.push(new SpecialExpression(SpecialExpression.Symbol.ASTERISK));
 		if(hasColon)
 			parseStack.push(new SpecialExpression(SpecialExpression.Symbol.COLON));
 
 	}
 
 	public void type_spec() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void use_stmt(Token label, Token useKeyword, Token id, Token onlyKeyword, Token eos, boolean hasModuleNature, boolean hasRenameList, boolean hasOnly) {
 		Use stmt = new Use(id.getText());
 		parseStack.push(stmt);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#v_list(int)
 	 */
 	public void v_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#v_list__begin()
 	 */
 	public void v_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#v_list_part(org.antlr.runtime.Token,
 	 *      org.antlr.runtime.Token)
 	 */
 	public void v_list_part(Token plus_minus, Token digitString) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void value_stmt(Token label, Token keyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#variable()
 	 */
 	public void variable() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void vector_subscript() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void volatile_stmt(Token label, Token keyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void wait_spec(Token id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#wait_spec_list(int)
 	 */
 	public void wait_spec_list(int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see parser.java.IFortranParserAction#wait_spec_list__begin()
 	 */
 	public void wait_spec_list__begin() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void wait_stmt(Token label, Token waitKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void where_body_construct() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void where_construct(int numConstructs, boolean hasMaskedElsewhere, boolean hasElsewhere) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void where_construct_stmt(Token id, Token whereKeyword, Token eos) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void where_stmt(Token label, Token whereKeyword) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void where_stmt__begin() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void write_stmt(Token label, Token writeKeyword, Token eos, boolean hasOutputItemList) {
 		WriteStatement stmt = new WriteStatement();
 		if(hasOutputItemList)
 			stmt.setOutputs((List<Expression>) parseStack.pop());
 		stmt.setControls((List<ActualArgument>) parseStack.pop());
 		
 		parseStack.push(stmt);
 	}
 
 	public void end_of_file() {
 		System.err.println("--end of file");
 		for(int i=0; i<parseStack.size(); i++) {
 			if (parseStack.peek() instanceof Subprogram) {
 				Subprogram subprogram = (Subprogram) parseStack.peek();
 				subprograms.put(subprogram.getName().toLowerCase(), subprogram);
 			}
 		}
 	}
 
 	public void entry_stmt(Token label, Token keyword, Token id, Token eos, boolean hasDummyArgList, boolean hasSuffix) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void start_of_file(String fileName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public Map<String, Subprogram> getSubprograms() {
 		return subprograms;
 	}
 	
 	
 }
