 package descent.internal.formatter;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.text.edits.TextEdit;
 
 import descent.core.dom.*;
 import descent.internal.compiler.parser.Lexer;
 import descent.internal.compiler.parser.TOK;
 import descent.internal.formatter.DefaultCodeFormatterOptions.BracePosition;
 
 /**
  * The class that visits everything in the source tree and formats it by sending
  * it to the Scribe class.
  * 
  * @author Robert Fraser
  */
 public class CodeFormatterVisitor extends ASTVisitor
 {
 	public final static boolean	        DEBUG = true;
 	
 	public DefaultCodeFormatterOptions	prefs;
 	public Scribe						scribe;
 	public Lexer						lexer;
 	
 	private List<Type>                  postfixes = new LinkedList<Type>();
 	private boolean                     nameAlreadyPrinted = false;
 	
 	/**
 	 * Since the initializer of a for is a statement, it contains
 	 * the semicolon. So it's difficult to insert a space before
 	 * that semicolon in a for. The hack is to use a variable indicating
 	 * that we are in a for initializer. Some common statements
 	 * are aware of this (VariableDeclaration, ExpressionStatement
 	 * and others), and print the semicolon only if this variable
 	 * is false. Further, they reset this variable (in case some
 	 * other statement was not taken into account, the formatter
 	 * doesn't die).
 	 */
 	private boolean						inForInitializer = false;
 	
 	public CodeFormatterVisitor(DefaultCodeFormatterOptions $preferences,
 			Map settings, int offset, int length, CompilationUnit unit)
 	{
 		prefs = $preferences;
 		scribe = new Scribe(this, 0, offset, length, unit);
 	}
 	
 	public TextEdit format(String src, Block block)
 	{
 		try
 		{
 			startup(src, block);
 			formatStatements(block.statements(), false, false);
 			if (hasComments())
 				scribe.printNewLine();
 			scribe.printComment();
 		} catch (AbortFormatting e)
 		{
 			return failedToFormat(e);
 		}
 		return scribe.getRootEdit();
 	}
 	
 	public TextEdit format(String src, CompilationUnit compilationUnit)
 	{
 		try
 		{
 			startup(src, compilationUnit);
 			compilationUnit.accept(this);
 		} catch (AbortFormatting e)
 		{
 			return failedToFormat(e);
 		}
 		return scribe.getRootEdit();
 	}
 	
 	public TextEdit format(String src, Expression expression)
 	{
 		try
 		{
 			startup(src, expression);
 			expression.accept(this);
 			scribe.printComment();
 		} catch (AbortFormatting e)
 		{
 			return failedToFormat(e);
 		}
 		return scribe.getRootEdit();
 	}
 	
 	
 	public boolean visit(AggregateDeclaration node)
 	{
 		formatModifiers(true);
 		
 		switch(node.getKind())
 		{
 			case CLASS:
 				scribe.printNextToken(TOK.TOKclass);
 				break;
 			case INTERFACE:
 				scribe.printNextToken(TOK.TOKinterface);
 				break;
 			case STRUCT:
 				scribe.printNextToken(TOK.TOKstruct);
 				break;
 			case UNION:
 				scribe.printNextToken(TOK.TOKunion);
 				break;
 			default:
 				throw new UnsupportedOperationException();
 		}
 		
 		// Handle anonymous types (I think just anonymous unions; anonymous
 		// classes are handled elsewhere).
 		SimpleName name = node.getName();
 		if(name != null)
 		{
 			scribe.space();
 			node.getName().accept(this);
 		}
 		
 		List<TemplateParameter> templateParams = node.templateParameters();
 		if(!templateParams.isEmpty())
 			formatTemplateParams(node.templateParameters(),
 					prefs.insert_space_before_opening_paren_in_class_template_params,
 					prefs.insert_space_after_opening_paren_in_class_template_params,
 					prefs.insert_space_before_closing_paren_in_class_template_params,
 					prefs.insert_space_before_comma_in_aggregate_template_parameters,
 					prefs.insert_space_after_comma_in_aggregate_template_parameters);
 		else if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_class_template_params);
 			if(prefs.insert_space_after_opening_paren_in_class_template_params ||
 					prefs.insert_space_between_empty_parens_in_class_template_params)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_class_template_params);
 		}
 		List<BaseClass> baseClasses = node.baseClasses();
 		if(!baseClasses.isEmpty())
 		{
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_base_class_lists);
 			if(prefs.insert_space_after_colon_in_base_class_lists)
 				scribe.space();
 			formatCSV(baseClasses,
 					prefs.insert_space_before_comma_in_base_class_lists,
 					prefs.insert_space_after_comma_in_base_class_lists);
 		}
 		
 		formatDeclarationBlock(node.declarations(), prefs.brace_position_for_type_declaration, prefs.indent_body_declarations_compare_to_type_header);
 		
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon, prefs.insert_space_before_semicolon);
 		}
 		scribe.printTrailingComment();
 		
 		return false;
 	}
 
 	public boolean visit(AliasDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKalias);
 		scribe.space();
 		node.getType().accept(this);
 		if(!nameAlreadyPrinted)
 			scribe.space();
 		formatCSV(node.fragments(),
 				this.prefs.insert_space_before_comma_in_multiple_field_declarations,
 				this.prefs.insert_space_after_comma_in_multiple_field_declarations);
 		
 		printSemicolonIfNextIsSemicolonAndNotInForInitializer();
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(AliasDeclarationFragment node)
 	{
 		if(!nameAlreadyPrinted)
 			node.getName().accept(this);
 		else
 			nameAlreadyPrinted = false;
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(AliasTemplateParameter node)
 	{
 		scribe.printNextToken(TOK.TOKalias);
 		scribe.space();
 		node.getName().accept(this);
 		Type specificType = node.getSpecificType();
 		if(null != specificType)
 		{
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_template_specific_type);
 			if(prefs.insert_space_after_colon_in_template_specific_type)
 				scribe.space();
 			specificType.accept(this);
 		}
 		Type defaultType = node.getDefaultType();
 		if(null != defaultType)
 		{
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_default_template_arguments);
 			if(prefs.insert_space_after_equals_in_default_template_arguments)
 				scribe.space();
 			defaultType.accept(this);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(AlignDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKalign);
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_align_declarations);
 			if(prefs.insert_space_after_opening_paren_in_align_declarations)
 				scribe.space();
 			scribe.printNextToken(literalOrIdentiferTokenList());
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_align_declarations);
 		}
 		formatDeclarationBlock(node.declarations(), prefs.brace_position_for_align_declaration, prefs.indent_body_declarations_compare_to_align_header);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(Argument node)
 	{
 		formatModifiers(true); // Print the passage mode(s)
 		Type type = node.getType();
 		boolean hasType = false;
 		if(null != type && !((type instanceof PrimitiveType) && 
 				(((PrimitiveType) type).getPrimitiveTypeCode() ==
 					PrimitiveType.Code.VOID) && 
 					!isNextToken(TOK.TOKvoid)))
 		{
 			hasType = true;
 			type.accept(this);
 		}
 		
 		SimpleName name = node.getName();
 		if(null != name)
 			if(!nameAlreadyPrinted)
 			{
 				if(hasType)
 					scribe.space();
 				name.accept(this);
 			}
 			else
 				nameAlreadyPrinted = false;
 		formatPostfixedDeclarations();
 		postfixes.clear();
 		
 		Expression defaultValue = node.getDefaultValue();
 		if (null != defaultValue)
 		{
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_default_function_arguments);
 			if(prefs.insert_space_after_equals_in_default_function_arguments)
 				scribe.space();
 			defaultValue.accept(this);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(ArrayAccess node)
 	{
 		node.getArray().accept(this);
 		scribe.printNextToken(TOK.TOKlbracket, 
 				prefs.insert_space_before_opening_bracket_in_array_access);
 		if(prefs.insert_space_after_opening_bracket_in_array_access)
 			scribe.space();
 		formatCSV(node.indexes(),
 				prefs.insert_space_before_comma_in_array_access,
 				prefs.insert_space_after_comma_in_array_access);
 		scribe.printNextToken(TOK.TOKrbracket, 
 				prefs.insert_space_before_closing_bracket_in_array_access);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 		return false;
 	}
 	
 	public boolean visit(ArrayInitializer node)
 	{
 		scribe.printNextToken(TOK.TOKlbracket);
 		if(prefs.insert_space_after_opening_bracket_in_array_literals)
 			scribe.space();
 		formatCSV(node.fragments(),
 				prefs.insert_space_before_comma_in_array_literal,
 				prefs.insert_space_after_comma_in_array_literal);
 		// Support trailing comma
 		if(isNextToken(TOK.TOKcomma))
 		{
 			scribe.printNextToken(TOK.TOKcomma, prefs.insert_space_before_trailing_comma_in_array_initializer);
 			if(prefs.insert_space_after_trailing_comma_in_array_initializer)
 				scribe.space();
 		}
 		scribe.printNextToken(TOK.TOKrbracket,
 				prefs.insert_space_before_closing_bracket_in_array_literals);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 		return false;
 	}
 	
 	public boolean visit(ArrayInitializerFragment node)
 	{
 		Expression expression = node.getExpression();
 		if(null != expression)
 		{
 			expression.accept(this);
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_array_initializer);
 			if(prefs.insert_space_after_colon_in_array_initializer)
 				scribe.space();
 		}
 		node.getInitializer().accept(this);
 		return false;
 	}
 	
 	public boolean visit(ArrayLiteral node)
 	{
 		scribe.printNextToken(TOK.TOKlbracket);
 		if(prefs.insert_space_after_opening_bracket_in_array_literals)
 			scribe.space();
 		formatCSV(node.arguments(),
 				prefs.insert_space_before_comma_in_array_literal,
 				prefs.insert_space_after_comma_in_array_literal);
 		scribe.printNextToken(TOK.TOKrbracket,
 				prefs.insert_space_before_closing_bracket_in_array_literals);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 		return false;
 	}
 	
 	// TODO pretty up ASM formatting (i.e., indentations, modifiers, etc.)
 	public boolean visit(AsmBlock node)
 	{
 		scribe.printComment();
 		scribe.dontFormat(node.getStartPosition(), node.getLength());
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(AssertExpression node)
 	{
 		scribe.printNextToken(TOK.TOKassert);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_assert_statements);
 		if(prefs.insert_space_after_opening_paren_in_assert_statements)
 			scribe.space();
 		node.getExpression().accept(this);
 		Expression message = node.getMessage();
 		if(null != message)
 		{
 			scribe.printNextToken(TOK.TOKcomma, prefs.insert_space_before_comma_in_assert_statements);
 			if(prefs.insert_space_after_comma_in_assert_statements)
 				scribe.space();
 			message.accept(this);
 		}
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_assert_statements);
 		return false;
 	}
 	
 	public boolean visit(Assignment node)
 	{
 		node.getLeftHandSide().accept(this);
 		scribe.printNextToken(assignmentOperatorTokenList(),
 				prefs.insert_space_before_assignment_operator);
 		if(prefs.insert_space_after_assignment_operator)
 			scribe.space();
 		node.getRightHandSide().accept(this);
 		return false;
 	}
 	
 	public boolean visit(AssociativeArrayLiteral node)
 	{
 		scribe.printNextToken(TOK.TOKlbracket);
 		if(prefs.insert_space_after_opening_bracket_in_array_literals)
 			scribe.space();
 		formatCSV(node.fragments(),
 				prefs.insert_space_before_comma_in_array_literal,
 				prefs.insert_space_after_comma_in_array_literal);
 		if(isNextToken(TOK.TOKcomma))
 		{
 			scribe.printNextToken(TOK.TOKcomma, prefs.insert_space_before_trailing_comma_in_array_initializer);
 			if(prefs.insert_space_after_trailing_comma_in_array_initializer)
 				scribe.space();
 		}
 		scribe.printNextToken(TOK.TOKrbracket,
 				prefs.insert_space_before_closing_bracket_in_array_literals);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 		return false;
 	}
 
 	public boolean visit(AssociativeArrayLiteralFragment node)
 	{
 		node.getKey().accept(this);
 		scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_array_initializer);
 		if(prefs.insert_space_after_colon_in_array_initializer)
 			scribe.space();
 		node.getValue().accept(this);
 		return false;
 	}
 
 	public boolean visit(AssociativeArrayType node)
 	{
 		node.getComponentType().accept(this);
 		if(isNextToken(TOK.TOKlbracket))
 			formatPostfix(node, false);
 		else
 			postfixes.add(node);
 		return false;
 	}
 	
 	public boolean visit(BaseClass node)
 	{
 		Modifier modifier = node.getModifier();
 		if(null != modifier && !isNextToken(TOK.TOKidentifier))
 		{
 			modifier.accept(this);
 			scribe.space();
 		}
 		node.getType().accept(this);
 		return false;
 	}
 	
 	public boolean visit(Block node)
 	{
 		formatSubStatement(node, false, true, true, BracePosition.END_OF_LINE);
 		return false;
 	}
 	
 	public boolean visit(BooleanLiteral node)
 	{
 		scribe.printNextToken(booleanLiteralTokenList());
 		return false;
 	}
 	
 	public boolean visit(BreakStatement node)
 	{
 		scribe.printNextToken(TOK.TOKbreak);
 		SimpleName label = node.getLabel();
 		if(null != label)
 		{
 			scribe.space();
 			label.accept(this);
 		}
 		scribe.printNextToken(TOK.TOKsemicolon, prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(CallExpression node)
 	{
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_function_invocation);
 		if(prefs.insert_space_after_opening_paren_in_function_invocation)
 			scribe.space();
 		List<Expression> arguments = node.arguments();
 		if(null != arguments && !arguments.isEmpty())
 			formatCSV(arguments,
 					prefs.insert_space_before_comma_in_function_invocation_arguments,
 					prefs.insert_space_after_comma_in_function_invocation_arguments);
 		else if(prefs.insert_space_between_empty_parens_in_function_invocation)
 			scribe.space();
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_function_invocation);
 		if(isNextToken(TOK.TOKlparen) && prefs.insert_space_between_succesive_opcalls)
 			scribe.space();
 		return false;
 	}
 	
 	public boolean visit(CastExpression node)
 	{
 		scribe.printNextToken(TOK.TOKcast);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_casts);
 		if(prefs.insert_space_after_opening_paren_in_casts)
 			scribe.space();
 		node.getType().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_casts);
 		if(prefs.insert_space_after_closing_paren_in_casts)
 			scribe.space();
 		node.getExpression().accept(this);
 		return false;
 	}
 	
 	public boolean visit(CastToModifierExpression node)
 	{
 		scribe.printNextToken(TOK.TOKcast);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_casts);
 		if(prefs.insert_space_after_opening_paren_in_casts)
 			scribe.space();
 		node.getModifier().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_casts);
 		if(prefs.insert_space_after_closing_paren_in_casts)
 			scribe.space();
 		node.getExpression().accept(this);
 		return false;
 	}
 	
 	public boolean visit(CatchClause node)
 	{
 		scribe.printNextToken(TOK.TOKcatch);
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_catch);
 			if(prefs.insert_space_after_opening_paren_in_catch)
 				scribe.space();
 			node.getType().accept(this);
 			SimpleName name = node.getName();
 			if(null != name)
 			{
 				scribe.space();
 				node.getName().accept(this);
 			}
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_catch);
 		}
 		boolean simple = formatSubStatement(node.getBody(), false, true, !prefs.keep_simple_catch_statement_on_same_line, prefs.brace_position_for_try_catch_finally);
 		if (simple) {
 			scribe.printNewLine();
 		}
 		return false;
 	}
 	
 	public boolean visit(CharacterLiteral node)
 	{
 		scribe.printNextToken(characterLiteralTokenList());
 		return false;
 	}
 	
 	public boolean visit(CompilationUnit node)
 	{
 		// fake new line to handle empty lines before package declaration or import declarations
 		this.scribe.lastNumberOfNewLines = 1;
 		/* 
 		 * Module declaration
 		 */
 		final ModuleDeclaration moduleDeclaration = node.getModuleDeclaration();
 		final boolean hasModule = moduleDeclaration != null;
 		if(hasModule) {
 			if (hasComments()) {
 				this.scribe.printComment();
 			}
 			int blankLinesBeforePackage = this.prefs.blank_lines_before_module;
 			if (blankLinesBeforePackage > 0) {
 				this.scribe.printEmptyLines(blankLinesBeforePackage);
 			}
 			
 			moduleDeclaration.accept(this);
 			
 			int blankLinesAfterPackage = this.prefs.blank_lines_after_module;
 			if (blankLinesAfterPackage > 0) {
 				this.scribe.printEmptyLines(blankLinesAfterPackage);
 			} else {
 				this.scribe.printNewLine();
 			}
 		} else {
 			scribe.printComment();
 		}
 		formatDeclarations(node.declarations());
 		scribe.printEndOfCompilationUnit();
 		return false;
 	}
 	
 	public boolean visit(ConditionalExpression node)
 	{
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKquestion, prefs.insert_space_before_question_mark_in_conditional_expressions);
 		if(prefs.insert_space_after_question_mark_in_conditional_expressions)
 			scribe.space();
 		node.getThenExpression().accept(this);
 		scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_conditional_expressions);
 		if(prefs.insert_space_after_colon_in_conditional_expressions)
 			scribe.space();
 		node.getElseExpression().accept(this);
 		return false;
 	}
 	
 	public boolean visit(ConstructorDeclaration node)
 	{
 		formatModifiers(true);
 		switch(node.getKind())
 		{
 			case CONSTRUCTOR:
 			case STATIC_CONSTRUCTOR: // "static" will be picked up in formatModifiers()
 				scribe.printNextToken(TOK.TOKthis);
 				break;
 			case DESTRUCTOR:
 			case STATIC_DESTRUCTOR:
 				scribe.printNextToken(TOK.TOKtilde);
 				scribe.printNextToken(TOK.TOKthis);
 				break;
 			case NEW:
 				scribe.printNextToken(TOK.TOKnew);
 				break;
 			case DELETE:
 				scribe.printNextToken(TOK.TOKdelete);
 				break;
 		}
 		formatFunction(node, prefs.brace_position_for_function_declaration);
 		return false;
 	}
 	
 	public boolean visit(ContinueStatement node)
 	{
 		scribe.printNextToken(TOK.TOKcontinue);
 		SimpleName label = node.getLabel();
 		if(null != label)
 		{
 			scribe.space();
 			node.getLabel().accept(this);
 		}
 		scribe.printNextToken(TOK.TOKsemicolon,
 				prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(DebugAssignment node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKdebug);
 		scribe.printNextToken(TOK.TOKassign,
 				prefs.insert_space_before_equals_in_version_debug_assignment);
 		if(prefs.insert_space_after_equals_in_version_debug_assignment)
 			scribe.space();
 		node.getVersion().accept(this);
 		scribe.printNextToken(TOK.TOKsemicolon, 
 				prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(DebugDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKdebug);
 		Version version = node.getVersion();
 		if(null != version)
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_version_debug);
 			if(prefs.insert_space_after_opening_paren_in_version_debug)
 				scribe.space();
 			version.accept(this);
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_version_debug);
 		}
 		formatConditionalDeclaration(node);
 		return false;
 	}
 	
 	public boolean visit(DebugStatement node)
 	{
 		scribe.printNextToken(TOK.TOKdebug);
 		Version version = node.getVersion();
 		if(null != version)
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_version_debug);
 			if(prefs.insert_space_after_opening_paren_in_version_debug)
 				scribe.space();
 			version.accept(this);
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_version_debug);
 		}
 		formatConditionalStatement(node);
 		return false;
 	}
 	
 	public boolean visit(DeclarationStatement node)
 	{
 		node.getDeclaration().accept(this);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(DefaultStatement node)
 	{
 		scribe.printNextToken(TOK.TOKdefault);
 		scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_case_default_statement);
 		if(prefs.insert_space_after_colon_in_case_default_statement)
 			scribe.space();
 		formatCaseOrDefaultStatementBody((Block) node.getBody());
 		return false;
 	}
 	
 	public boolean visit(DelegateType node)
 	{
 		node.getReturnType().accept(this);
 		boolean cStyle = false;
 		if(isNextToken(TOK.TOKlparen)) //Handle a C-style function pointer
 		{
 			cStyle = true;
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_c_style_fp);
 			if(prefs.insert_space_after_opening_paren_in_c_style_fp)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKmul);
 			if(isNextToken(TOK.TOKidentifier))
 			{
 				scribe.printNextToken(TOK.TOKidentifier,
 						prefs.insert_space_after_star_in_c_style_fp);
 				nameAlreadyPrinted = true;
 			}
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_c_style_fp);
 		}
 		else
 		{
 			scribe.space();
 			if(node.isFunctionPointer())
 				scribe.printNextToken(TOK.TOKfunction);
 			else
 				scribe.printNextToken(TOK.TOKdelegate);
 		}
 		boolean spaceBeforeParen = prefs.insert_space_before_opening_paren_in_delegate ||
 			(cStyle && prefs.insert_space_between_name_and_args_in_c_style_fp);
 		scribe.printNextToken(TOK.TOKlparen, spaceBeforeParen);
 		if(prefs.insert_space_after_opening_paren_in_delegate)
 			scribe.space();
 		List<Argument> arguments = node.arguments();
 		if(null != arguments && !arguments.isEmpty())
 			formatCSV(arguments,
 					prefs.insert_space_before_comma_in_delegates,
 					prefs.insert_space_after_comma_in_delegates);
 		else if(prefs.insert_space_between_empty_parens_in_delegate)
 			scribe.space();
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_delegate);
 		return false;
 	}
 	
 	public boolean visit(DeleteExpression node)
 	{
 		scribe.printNextToken(TOK.TOKdelete);
 		scribe.space();
 		node.getExpression().accept(this);
 		return false;
 	}
 	
 	public boolean visit(DollarLiteral node)
 	{
 		scribe.printNextToken(TOK.TOKdollar);
 		return false;
 	}
 	
 	public boolean visit(DoStatement node)
 	{
 		scribe.printNextToken(TOK.TOKdo);
 		formatSubStatement(node.getBody(), prefs.insert_new_line_before_while_in_do_statement, true, true, prefs.brace_position_for_loop_statement);
 		scribe.printNextToken(TOK.TOKwhile);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_while_loops);
 		if(prefs.insert_space_after_opening_paren_in_while_loops)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_while_loops);
 		return false;
 	}
 	
 	public boolean visit(DotIdentifierExpression node)
 	{
 		// Handle module-scoped dot-identifier expressions (".name")
 		Expression exp = node.getExpression();
 		if(null != exp)
 			exp.accept(this);
 		
 		scribe.printNextToken(TOK.TOKdot, 
 				prefs.insert_space_before_dot_in_qualified_names);
 		if(prefs.insert_space_after_dot_in_qualified_names)
 			scribe.space();
 		node.getName().accept(this);
 		return false;
 	}
 	
 	public boolean visit(DotTemplateTypeExpression node)
 	{
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKdot, 
 				prefs.insert_space_before_dot_in_qualified_names);
 		if(prefs.insert_space_after_dot_in_qualified_names)
 			scribe.space();
 		node.getTemplateType().accept(this);
 		return false;
 	}
 	
 	public boolean visit(DynamicArrayType node)
 	{
 		node.getComponentType().accept(this);
 		if(isNextToken(TOK.TOKlbracket))
 			formatPostfix(node, false);
 		else
 			postfixes.add(node);
 		return false;
 	}
 	
 	public boolean visit(EmptyStatement node)
 	{
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(EnumDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKenum);
 		
 		Name name = node.getName();
 		if(null != name)
 		{
 			scribe.space();
 			node.getName().accept(this);
 		}
 		
 		Type baseType = node.getBaseType();
 		if(null != baseType)
 		{
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_base_class_lists);
 			if(prefs.insert_space_after_colon_in_base_class_lists)
 				scribe.space();
 			baseType.accept(this);
 		}
 		
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon,
 					prefs.insert_space_before_semicolon);
 		}
 		else
 		{
 			formatOpeningBrace(prefs.brace_position_for_enum_declaration, prefs.indent_enum_members_compare_to_enum_header);
 			scribe.printNewLine();
 			for(EnumMember member : node.enumMembers())
 			{
 				member.accept(this);
 				if(isNextToken(TOK.TOKcomma)) {
 					scribe.printNextToken(TOK.TOKcomma,
 							prefs.insert_space_before_comma_in_enum_member_lists);
 				}
 				scribe.printTrailingComment();
 				scribe.printNewLine();
 			}
 			formatClosingBrace(prefs.brace_position_for_enum_declaration, prefs.indent_enum_members_compare_to_enum_header);
 		}
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon,
 					prefs.insert_space_before_semicolon);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(EnumMember node)
 	{
 		node.getName().accept(this);
 		Expression value = node.getValue();
 		if(null != value)
 		{
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_enum_constants);
 			if(prefs.insert_space_after_equals_in_enum_constants)
 				scribe.space();
 			value.accept(this);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(ExpressionInitializer node)
 	{
 		node.getExpression().accept(this);
 		return false;
 	}
 	
 	public boolean visit(ExpressionStatement node)
 	{
 		node.getExpression().accept(this);
 		printSemicolonIfNextIsSemicolonAndNotInForInitializer();
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(ExternDeclaration node)
 	{
 		formatModifiers(false); // Picks up the "extern"
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_extern_declarations);
 			if(prefs.insert_space_after_opening_paren_in_extern_declarations)
 				scribe.space();
 			if(node.getLinkage() != ExternDeclaration.Linkage.DEFAULT)
 				scribe.printNextToken(TOK.TOKidentifier);
 			else if(prefs.insert_space_between_empty_parens_in_extern_declarations)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_extern_declarations);
 		}
 		formatDeclarationBlock(node.declarations(), prefs.brace_position_for_modifiers, true);
 		return false;
 	}
 	
 	public boolean visit(FileImportExpression node)
 	{
 		scribe.printNextToken(TOK.TOKimport);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_file_imports);
 		if(prefs.insert_space_after_opening_paren_in_file_imports)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_file_imports);
 		return false;
 	}
 	
 	public boolean visit(ForeachRangeStatement node)
 	{
 		if(node.isReverse())
 			scribe.printNextToken(TOK.TOKforeach_reverse);
 		else
 			scribe.printNextToken(TOK.TOKforeach);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_foreach_loops);
 		if(prefs.insert_space_after_opening_paren_in_foreach_loops)
 			scribe.space();
 		node.getArgument().accept(this);
 		scribe.printNextToken(TOK.TOKsemicolon, prefs.insert_space_before_semicolon_in_foreach_statement);
 		scribe.printTrailingComment();
 		if (prefs.insert_space_after_semicolon_in_foreach_statement) {
 			scribe.space();
 		}
 		node.getFromExpression().accept(this);
 		scribe.printNextToken(TOK.TOKslice, 
 				prefs.insert_space_before_slice_operator_in_foreach_range_statement);
 		if(prefs.insert_space_after_slice_operator_in_foreach_range_statement)
 			scribe.space();
 		node.getToExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_foreach_loops);
 		formatSubStatement(node.getBody(), false, true, !prefs.keep_simple_loop_statement_on_same_line, prefs.brace_position_for_loop_statement);
 		return false;
 	}
 	
 	public boolean visit(ForeachStatement node)
 	{
 		if(node.isReverse())
 			scribe.printNextToken(TOK.TOKforeach_reverse);
 		else
 			scribe.printNextToken(TOK.TOKforeach);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_foreach_loops);
 		if(prefs.insert_space_after_opening_paren_in_foreach_loops)
 			scribe.space();
 		formatCSV(node.arguments(),
 				prefs.insert_space_before_comma_in_foreach_statement,
 				prefs.insert_space_after_comma_in_foreach_statement);
 		scribe.printNextToken(TOK.TOKsemicolon, prefs.insert_space_before_semicolon_in_foreach_statement);
 		scribe.printTrailingComment();
 		if (prefs.insert_space_after_semicolon_in_foreach_statement) {
 			scribe.space();
 		}
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_foreach_loops);
 		formatSubStatement(node.getBody(), false, true, !prefs.keep_simple_loop_statement_on_same_line, prefs.brace_position_for_loop_statement);
 		return false;
 	}
 	
 	public boolean visit(ForStatement node)
 	{
 		Statement initializer = node.getInitializer();
 		Expression condition = node.getCondition();
 		Expression increment = node.getIncrement();
 		
 		scribe.printNextToken(TOK.TOKfor);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_for_loops);
 		if(prefs.insert_space_after_opening_paren_in_for_loops)
 			scribe.space();
 		if(null != initializer)
 		{
 			this.inForInitializer = true;
 			initializer.accept(this);
 		}
 		
 		if (!inForInitializer) {
 			scribe.printNextToken(TOK.TOKsemicolon, prefs.insert_space_before_semicolon_in_for_statement);
 		}
 		inForInitializer = false;
 		
 		if (prefs.insert_space_after_semicolon_in_for_statement) {
 			scribe.space();
 		}
 		
 		if(null != condition)
 		{
 			condition.accept(this);
 		}
 		
 		scribe.printNextToken(TOK.TOKsemicolon, prefs.insert_space_before_semicolon_in_for_statement);
 		if (prefs.insert_space_after_semicolon_in_for_statement) {
 			scribe.space();
 		}
 		
 		if(null != increment) {
 			increment.accept(this);
 		}
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_for_loops);
 		formatSubStatement(node.getBody(), false, true, !prefs.keep_simple_loop_statement_on_same_line, prefs.brace_position_for_loop_statement);
 		return false;
 	}
 	
 	public boolean visit(FunctionDeclaration node)
 	{
 		boolean templated = false;
 		formatModifiers(true);
 		node.getReturnType().accept(this);
 		scribe.space();
 		node.getName().accept(this);
 		List<TemplateParameter> tp = node.templateParameters();
 		if(null != tp && !tp.isEmpty())
 		{
 			formatTemplateParams(node.templateParameters(),
 					prefs.insert_space_before_opening_paren_in_function_template_args,
 					prefs.insert_space_after_opening_paren_in_function_template_args,
 					prefs.insert_space_before_closing_paren_in_function_template_args,
 					prefs.insert_space_before_comma_in_function_template_parameters,
 					prefs.insert_space_after_comma_in_function_template_parameters);
 			templated = true;
 		}
 		else if(hasEmptyTemplateParamList(node))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_function_template_args);
 			if(prefs.insert_space_after_opening_paren_in_function_template_args ||
 					prefs.insert_space_between_empty_parens_in_function_template_args)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_function_template_args);
 			templated = true;
 		}
 		if(templated && prefs.insert_space_between_template_and_arg_parens_in_function_declaration)
 			scribe.space();
 		formatFunction(node, prefs.brace_position_for_function_declaration);
 		return false;
 	}
 	
 	public boolean visit(FunctionLiteralDeclarationExpression node)
 	{
 		switch(node.getSyntax())
 		{
 			case FUNCTION:
 				scribe.printNextToken(TOK.TOKfunction);
 				scribe.space();
 				break;
 			case DELEGATE:
 				scribe.printNextToken(TOK.TOKdelegate);
 				scribe.space();
 				break;
 			case EMPTY:
 			default:
 				break;
 		}
 		
 		if (node.getReturnType() != null) {
 			node.getReturnType().accept(this);
 			scribe.space();
 		}
 		
 		formatFunction(node, prefs.brace_position_for_function_literal);
 		return false;
 	}
 	
 	public boolean visit(GotoCaseStatement node)
 	{
 		scribe.printNextToken(TOK.TOKgoto);
 		scribe.space();
 		scribe.printNextToken(TOK.TOKcase);
 		scribe.space();
 		Expression label = node.getLabel();
 		if(null != label)
 		{
 			scribe.space();
 			label.accept(this);
 		}
 		scribe.printNextToken(TOK.TOKsemicolon,
 				prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(GotoDefaultStatement node)
 	{
 		scribe.printNextToken(TOK.TOKgoto);
 		scribe.space();
 		scribe.printNextToken(TOK.TOKdefault);
 		scribe.printNextToken(TOK.TOKsemicolon,
 				prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(GotoStatement node)
 	{
 		scribe.printNextToken(TOK.TOKgoto);
 		scribe.space();
 		node.getLabel().accept(this);
 		scribe.printNextToken(TOK.TOKsemicolon,
 				prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(IfStatement node)
 	{
 		scribe.printNextToken(TOK.TOKif);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_if_statements);
 		if(prefs.insert_space_after_opening_paren_in_if_statements)
 			scribe.space();
 		Argument arg = node.getArgument();
 		if(null != arg)
 		{
 			arg.accept(this);
 			scribe.space();
 			scribe.printNextToken(TOK.TOKassign);
 			scribe.space();
 		}
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_if_statements);
 		formatConditionalStatement(node);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(Import node)
 	{
 		SimpleName alias = node.getAlias();
 		if (null != alias)
 		{
 			alias.accept(this);
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_renamed_imports);
 			if(prefs.insert_space_after_equals_in_renamed_imports)
 				scribe.space();
 		}
 		node.getName().accept(this);
 		List<SelectiveImport> imports = node.selectiveImports();
 		if(null != imports && !imports.isEmpty())
 		{
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_selective_imports);
 			if(prefs.insert_space_after_colon_in_selective_imports)
 				scribe.space();
 			formatCSV(imports,
 					prefs.insert_space_before_comma_in_selective_imports,
 					prefs.insert_space_after_comma_in_selective_imports);
 		}
 		return false;
 	}
 	
 	public boolean visit(ImportDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKimport);
 		scribe.space();
 		formatCSV(node.imports(),
 				prefs.insert_space_before_comma_in_multiple_imports,
 				prefs.insert_space_after_comma_in_multiple_imports);
 		scribe.printNextToken(TOK.TOKsemicolon, this.prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		scribe.printNewLine();
 		return false;
 	}
 	
 	public boolean visit(InfixExpression node)
 	{
 		node.getLeftOperand().accept(this);
 		
 		if(isNextToken(TOK.TOKnot))
 		{
 			scribe.printNextToken(TOK.TOKnot,
 					prefs.insert_space_before_infix_operator);
 			if(isNextToken(TOK.TOKis))
 				scribe.printNextToken(TOK.TOKis);
 		}
 		else
 			scribe.printNextToken(infixOperatorTokenList(),
 					prefs.insert_space_before_infix_operator);
 		if(prefs.insert_space_after_infix_operator)
 			scribe.space();
 		node.getRightOperand().accept(this);
 		return false;
 	}
 	
 	public boolean visit(InvariantDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKinvariant);
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen,
 					prefs.insert_space_before_opening_paren_in_class_invariants);
 			scribe.printNextToken(TOK.TOKrparen,
 					prefs.insert_space_between_empty_parens_in_class_invariants);
 		}
 		scribe.space();
 		node.getBody().accept(this);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(IsTypeExpression node)
 	{
 		scribe.printNextToken(TOK.TOKis);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_is_expressions);
 		if(prefs.insert_space_after_opening_paren_in_is_expressions)
 			scribe.space();
 		node.getType().accept(this);
 		SimpleName name = node.getName();
 		if(null != name)
 		{
 			if(!nameAlreadyPrinted)
 			{
 				scribe.space();
 				name.accept(this);
 			}
 			else
 				nameAlreadyPrinted = false;
 		}
 		Type specialization = node.getSpecialization();
 		if(null != specialization)
 		{
 			scribe.space();
 			scribe.printNextToken(node.isSameComparison() ? 
 					TOK.TOKequal : TOK.TOKcolon);
 			scribe.space();
 			specialization.accept(this);
 		}
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_is_expressions);
 		return false;
 	}
 	
 	public boolean visit(IsTypeSpecializationExpression node)
 	{
 		scribe.printNextToken(TOK.TOKis);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_is_expressions);
 		if(prefs.insert_space_after_opening_paren_in_is_expressions)
 			scribe.space();
 		node.getType().accept(this);
 		SimpleName name = node.getName();
 		if(null != name)
 		{
 			if(!nameAlreadyPrinted)
 			{
 				scribe.space();
 				name.accept(this);
 			}
 			else
 				nameAlreadyPrinted = false;
 		}
 		scribe.space();
 		scribe.printNextToken(node.isSameComparison() ? 
 				TOK.TOKequal : TOK.TOKcolon);
 		scribe.space();
 		scribe.printNextToken(typeSpecializationTokenList());
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_is_expressions);
 		return false;
 	}
 	
 	public boolean visit(LabeledStatement node)
 	{
 		node.getLabel().accept(this);
 		scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_statement_labels);
 		if(prefs.insert_space_after_colon_in_statement_labels)
 			scribe.space();
 		if(prefs.insert_new_line_after_label)
 			scribe.printNewLine();
 		node.getBody().accept(this);
 		scribe.printTrailingComment();
 		return false;
 	}
 
 	public boolean visit(MixinDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKmixin);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_mixins);
 		if(prefs.insert_space_after_opening_paren_in_mixins)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_mixins);
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon,
 					prefs.insert_space_before_semicolon);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 
 	public boolean visit(MixinExpression node)
 	{
 		scribe.printNextToken(TOK.TOKmixin);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_mixins);
 		if(prefs.insert_space_after_opening_paren_in_mixins)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_mixins);
 		return false;
 	}
 	
 	public boolean visit(ModifiedType node)
 	{
 		node.getModifier().accept(this);
 		scribe.printNextToken(TOK.TOKlparen,
 				prefs.insert_space_before_opening_paren_in_modified_type);
 		if(prefs.insert_space_after_opening_paren_in_modified_type)
 			scribe.space();
 		node.getComponentType().accept(this);
 		scribe.printNextToken(TOK.TOKrparen,
 				prefs.insert_space_before_closing_paren_in_modified_type);
 		return false;
 	}
 	
 	public boolean visit(Modifier node)
 	{
 		scribe.printNextToken(modifierTokenList());
 		return false;
 	}
 	
 	public boolean visit(ModifierDeclaration node)
 	{
 		formatModifiers(false);
 		if(!isNextToken(TOK.TOKcolon)) // "private:" instead of "private :"
 			scribe.space();
 		formatDeclarationBlock(node.declarations(), prefs.brace_position_for_modifiers, prefs.indent_body_declarations_compare_to_modifier_header);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(ModuleDeclaration node)
 	{
 		scribe.printNextToken(TOK.TOKmodule);
 		scribe.space();
 		node.getName().accept(this);
 		scribe.printNextToken(TOK.TOKsemicolon, this.prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(NewAnonymousClassExpression node)
 	{
 		Expression exp = node.getExpression();
 		if(null != exp)
 		{
 			exp.accept(this);
 			scribe.printNextToken(TOK.TOKdot,
 					prefs.insert_space_before_dot_in_qualified_names);
 			if(prefs.insert_space_after_dot_in_qualified_names)
 				scribe.space();
 		}
 		scribe.printNextToken(TOK.TOKnew);
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_new_arguments);
 			if(prefs.insert_space_after_opening_paren_in_new_arguments)
 				scribe.space();
 			List<Expression> newArguments = node.newArguments();
 			if(null != newArguments && !newArguments.isEmpty())
 				formatCSV(node.newArguments(),
 						prefs.insert_space_before_comma_in_new_arguments,
 						prefs.insert_space_after_comma_in_new_arguments);
 			else if(prefs.insert_space_between_empty_parens_in_new_arguments)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_new_arguments);
 		}
 		scribe.space();
 		scribe.printNextToken(TOK.TOKclass);
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_function_invocation);
 			if(prefs.insert_space_after_opening_paren_in_function_invocation)
 				scribe.space();
 			List<Expression> constructorArguments = node.constructorArguments();
 			if(null != constructorArguments && !constructorArguments.isEmpty())
 				formatCSV(node.constructorArguments(),
 						prefs.insert_space_before_comma_in_function_invocation_arguments,
 						prefs.insert_space_after_comma_in_function_invocation_arguments);
 			else if(prefs.insert_space_between_empty_parens_in_function_invocation)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_function_invocation);
 		}
 		List<BaseClass> baseClasses = node.baseClasses();
 		if(null != baseClasses && !baseClasses.isEmpty());
 		{
 			scribe.space();
 			formatCSV(baseClasses,
 					prefs.insert_space_before_comma_in_base_class_lists,
 					prefs.insert_space_after_comma_in_base_class_lists);
 		}
 		formatDeclarationBlock(node.declarations(), prefs.brace_position_for_anonymous_type, true);
 		return false;
 	}
 	
 	public boolean visit(NewExpression node)
 	{
 		Expression exp = node.getExpression();
 		if(null != exp)
 		{
 			exp.accept(this);
 			scribe.printNextToken(TOK.TOKdot,
 					prefs.insert_space_before_dot_in_qualified_names);
 			if(prefs.insert_space_after_dot_in_qualified_names)
 				scribe.space();
 		}
 		scribe.printNextToken(TOK.TOKnew);
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_new_arguments);
 			if(prefs.insert_space_after_opening_paren_in_new_arguments)
 				scribe.space();
 			List<Expression> newArguments = node.newArguments();
 			if(null != newArguments && !newArguments.isEmpty())
 				formatCSV(node.newArguments(),
 						prefs.insert_space_before_comma_in_new_arguments,
 						prefs.insert_space_after_comma_in_new_arguments);
 			else if(prefs.insert_space_between_empty_parens_in_new_arguments)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_new_arguments);
 		}
 		scribe.space();
 		Type type = node.getType();
 		if(type instanceof DynamicArrayType)
 		{
 			DynamicArrayType arr = (DynamicArrayType) type;
 			arr.getComponentType().accept(this);
 			scribe.printNextToken(TOK.TOKlbracket,
 					prefs.insert_space_before_opening_bracket_in_array_constructors);
 			if(prefs.insert_space_after_opening_bracket_in_array_constructors)
 				scribe.space();
 			formatCSV(node.constructorArguments(),
 					prefs.insert_space_before_comma_in_function_invocation_arguments,
 					prefs.insert_space_after_comma_in_function_invocation_arguments);
 			scribe.printNextToken(TOK.TOKrbracket, 
 					prefs.insert_space_before_closing_bracket_in_array_constructors);
 			if(isNextToken(TOK.TOKlbracket) && 
 					prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 				scribe.space();
 			return false;
 		}
 		type.accept(this);
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_function_invocation);
 			if(prefs.insert_space_after_opening_paren_in_function_invocation)
 				scribe.space();
 			List<Expression> constructorArguments = node.constructorArguments();
 			if(null != constructorArguments && !constructorArguments.isEmpty())
 				formatCSV(node.constructorArguments(),
 						prefs.insert_space_before_comma_in_function_invocation_arguments,
 						prefs.insert_space_after_comma_in_function_invocation_arguments);
 			else if(prefs.insert_space_between_empty_parens_in_function_invocation)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_function_invocation);
 		}
 		return false;
 	}
 	
 	public boolean visit(NullLiteral node)
 	{
 		scribe.printNextToken(TOK.TOKnull);
 		return false;
 	}
 	
 	public boolean visit(NumberLiteral node)
 	{
 		scribe.printNextToken(numericLiteralTokenList());
 		return false;
 	}
 	
 	public boolean visit(ParenthesizedExpression node)
 	{
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_parenthesized_expressions);
 		if(prefs.insert_space_after_opening_paren_in_parenthesized_expressions)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_parenthesized_expressions);
 		return false;
 	}
 	
 	public boolean visit(PointerType node)
 	{
 		node.getComponentType().accept(this);
 		scribe.printNextToken(TOK.TOKmul);
 		return false;
 	}
 	
 	public boolean visit(PostfixExpression node)
 	{
 		node.getOperand().accept(this);
 		scribe.printNextToken(postfixOperatorTokenList(),
 				prefs.insert_space_before_postfix_operator);
 		if(prefs.insert_space_after_postfix_operator)
 			scribe.space();
 		return false;
 	}
 	
 	public boolean visit(Pragma node)
 	{
 		scribe.printNextToken(TOK.TOKPRAGMA);
 		scribe.printNewLine();
 		return false;
 	}
 	
 	public boolean visit(PragmaDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKpragma);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_pragmas);
 		if(prefs.insert_space_after_opening_paren_in_pragmas)
 			scribe.space();
 		node.getName().accept(this);
 		List<Expression> args = node.arguments();
 		if(!args.isEmpty())
 		{
 			scribe.printNextToken(TOK.TOKcomma, prefs.insert_space_before_comma_in_pragmas);
 			if(prefs.insert_space_after_comma_in_pragmas)
 				scribe.space();
 			formatCSV(args,
 					prefs.insert_space_before_comma_in_pragmas,
 					prefs.insert_space_after_comma_in_pragmas);
 		}
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_pragmas);
 		if (isNextToken(TOK.TOKlcurly)) {
 			formatDeclarationBlock(node.declarations(), prefs.brace_position_for_pragmas, prefs.indent_body_declarations_compare_to_pragma_header);
 		}
 		if(isNextToken(TOK.TOKsemicolon))
 			scribe.printNextToken(TOK.TOKsemicolon,
 					prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(PragmaStatement node)
 	{
 		scribe.printNextToken(TOK.TOKpragma);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_pragmas);
 		if(prefs.insert_space_after_opening_paren_in_pragmas)
 			scribe.space();
 		node.getName().accept(this);
 		List<Expression> args = node.arguments();
 		if(!args.isEmpty())
 		{
 			scribe.printNextToken(TOK.TOKcomma, prefs.insert_space_before_comma_in_pragmas);
 			if(prefs.insert_space_after_comma_in_pragmas)
 				scribe.space();
 			formatCSV(args,
 					prefs.insert_space_before_comma_in_pragmas,
 					prefs.insert_space_after_comma_in_pragmas);
 		}
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_pragmas);
 		Statement body = node.getBody();
 		if(null != body) {
 			formatSubStatement(body, false, true, true, prefs.brace_position_for_pragmas);
 		}
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon,
 					prefs.insert_space_before_semicolon);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(PrefixExpression node)
 	{
 		scribe.printNextToken(prefixOperatorTokenList(),
 				prefs.insert_space_before_prefix_operator);
 		if(prefs.insert_space_after_prefix_operator)
 			scribe.space();
 		node.getOperand().accept(this);
 		return false;
 	}
 	
 	public boolean visit(PrimitiveType node)
 	{
 		if(node.getPrimitiveTypeCode() == PrimitiveType.Code.VOID)
 		{
 			// If an argument has no type, getType() returns void. So, void
 			// can mean that there is an explicit void token or not
 			if(isNextToken(TOK.TOKvoid))
 				scribe.printNextToken(TOK.TOKvoid);
 		}
 		else
 		{
 			scribe.printNextToken(primitiveTypesTokenList());
 		}
 		return false;
 	}
 	
 	public boolean visit(QualifiedName node)
 	{
 		node.getQualifier().accept(this);
 		scribe.printNextToken(TOK.TOKdot, 
 				prefs.insert_space_before_dot_in_qualified_names);
 		if(prefs.insert_space_after_dot_in_qualified_names)
 			scribe.space();
 		node.getName().accept(this);
 		return false;
 	}
 	
 	public boolean visit(QualifiedType node)
 	{
 		Type qualifier = node.getQualifier();
 		if(null != qualifier)
 			qualifier.accept(this);
 		scribe.printNextToken(TOK.TOKdot, 
 				prefs.insert_space_before_dot_in_qualified_names);
 		if(prefs.insert_space_after_dot_in_qualified_names)
 			scribe.space();
 		node.getType().accept(this);
 		return false;
 	}
 	
 	public boolean visit(ReturnStatement node)
 	{
 		scribe.printNextToken(TOK.TOKreturn);
 		Expression exp = node.getExpression();
 		if(null != exp)
 		{
 			scribe.space();
 			exp.accept(this);
 		}
 		scribe.printNextToken(TOK.TOKsemicolon,
 				prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(ScopeStatement node)
 	{
 		scribe.printNextToken(TOK.TOKscope);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_scope_statements);
 		if(prefs.insert_space_after_opening_paren_in_scope_statements)
 			scribe.space();
 		scribe.printNextToken(TOK.TOKidentifier);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_scope_statements);
 		formatSubStatement(node.getBody(), false, true, !prefs.keep_simple_scope_statement_on_same_line, prefs.brace_position_for_scope_statement);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(SelectiveImport node)
 	{
 		SimpleName alias = node.getAlias();
 		if (null != alias)
 		{
 			alias.accept(this);
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_renamed_imports);
 			if(prefs.insert_space_after_equals_in_renamed_imports)
 				scribe.space();
 		}
 		
 		node.getName().accept(this);
 		
 		return false;
 	}
 	
 	public boolean visit(SimpleName node)
 	{
 		// If the name is in parentheses, but nothing else is there, the
 		// parser might make it a SimpleName, not a ParenthesizedExpression
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_parenthesized_expressions);
 			if(prefs.insert_space_after_opening_paren_in_parenthesized_expressions)
 				scribe.space();
 			scribe.printNextToken(TOK.TOKidentifier);
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_parenthesized_expressions);
 		}
 		else
 			scribe.printNextToken(TOK.TOKidentifier);
 		return false;
 	}
 	
 	public boolean visit(SimpleType node)
 	{
 		node.getName().accept(this);
 		return false;
 	}
 	
 	public boolean visit(SliceExpression node)
 	{
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKlbracket,
 				prefs.insert_space_before_opening_bracket_in_slices);
 		if(prefs.insert_space_after_opening_bracket_in_slices)
 			scribe.space();
 		// Make sure it's not a slice of the whole array, i.e. arr[]
 		Expression fromExpression = node.getFromExpression();
 		if(null != fromExpression)
 		{
 			node.getFromExpression().accept(this);
 			scribe.printNextToken(TOK.TOKslice,
 					prefs.insert_space_before_slice_operator);
 			if(prefs.insert_space_after_slice_operator)
 				scribe.space();
 			node.getToExpression().accept(this);
 		}
 		else if(prefs.insert_space_between_empty_brackets_in_slice)
 			scribe.space();
 		scribe.printNextToken(TOK.TOKrbracket,
 				prefs.insert_space_before_closing_bracket_in_slices);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 		return false;
 	}
 	
 	public boolean visit(SliceType node)
 	{
 		node.getComponentType().accept(this);
 		if(isNextToken(TOK.TOKlbracket))
 			formatPostfix(node, false);
 		else
 			postfixes.add(node);
 		return false;
 	}
 	
 	public boolean visit(StaticArrayType node)
 	{
 		node.getComponentType().accept(this);
 		if(isNextToken(TOK.TOKlbracket))
 			formatPostfix(node, false);
 		else
 			postfixes.add(node);
 		return false;
 	}
 	
 	public boolean visit(StaticAssert node)
 	{
 		formatModifiers(true); // Picks up the "static"
 		scribe.printNextToken(TOK.TOKassert);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_assert_statements);
 		if(prefs.insert_space_after_opening_paren_in_assert_statements)
 			scribe.space();
 		node.getExpression().accept(this);
 		Expression message = node.getMessage();
 		if(null != message)
 		{
 			scribe.printNextToken(TOK.TOKcomma, prefs.insert_space_before_comma_in_assert_statements);
 			if(prefs.insert_space_after_comma_in_assert_statements)
 				scribe.space();
 			message.accept(this);
 		}
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_assert_statements);
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon,
 					prefs.insert_space_before_semicolon);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(StaticAssertStatement node)
 	{
 		node.getStaticAssert().accept(this);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(StaticIfDeclaration node)
 	{
 		formatModifiers(true); //Picks up the "static"
 		scribe.printNextToken(TOK.TOKif);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_if_statements);
 		if(prefs.insert_space_after_opening_paren_in_if_statements)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_if_statements);
 		formatConditionalDeclaration(node);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(StaticIfStatement node)
 	{
 		scribe.printNextToken(TOK.TOKstatic);
 		scribe.space();
 		scribe.printNextToken(TOK.TOKif);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_if_statements);
 		if(prefs.insert_space_after_opening_paren_in_if_statements)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_if_statements);
 		formatConditionalStatement(node);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(StringLiteral node)
 	{
 		scribe.printNextToken(TOK.TOKstring);
 		return false;
 	}
 	
 	public boolean visit(StringsExpression node)
 	{
 		List<StringLiteral> literals = node.stringLiterals();
 		int len = literals.size();
 		if(len == 0)
 			return false;
 		
 		for (int i = 0; i < len - 1; i++)
 		{
 			literals.get(i).accept(this);
 			scribe.space();
 		}
 		literals.get(len - 1).accept(this);
 		return false;
 	}
 	
 	public boolean visit(StructInitializer node)
 	{
 		scribe.printNextToken(TOK.TOKlcurly);
 		formatCSV(node.fragments(),
 				prefs.insert_space_before_comma_in_struct_initializer,
 				prefs.insert_space_after_comma_in_struct_initializer);
 		scribe.printNextToken(TOK.TOKrcurly);
 		return false;
 	}
 	
 	public boolean visit(StructInitializerFragment node)
 	{
 		SimpleName name = node.getName();
 		if(null != name)
 		{
 			name.accept(this);
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_struct_initializer);
 			if(prefs.insert_space_after_colon_in_struct_initializer)
 				scribe.space();
 		}
 		node.getInitializer().accept(this);
 		return false;
 	}
 	
 	public boolean visit(SuperLiteral node)
 	{
 		scribe.printNextToken(TOK.TOKsuper);
 		return false;
 	}
 	
 	public boolean visit(SwitchCase node)
 	{
 		scribe.printNextToken(TOK.TOKcase);
 		scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_case_default_statement);
 		if(prefs.insert_space_after_colon_in_case_default_statement)
 			scribe.space();
 		formatCaseOrDefaultStatementBody((Block) node.getBody());
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(SwitchStatement node)
 	{
 		scribe.printNextToken(TOK.TOKswitch);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_switch_statements);
 		if(prefs.insert_space_after_opening_paren_in_switch_statements)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_switch_statements);
 		formatSubStatement(
 				node.getBody(),
 				false, 
 				prefs.indent_cases_compare_to_switch, 
 				true,
 				prefs.brace_position_for_switch_statement
 			);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(SynchronizedStatement node)
 	{
 		scribe.printNextToken(TOK.TOKsynchronized);
 		Expression expression = node.getExpression();
 		if(null != expression)
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_synchronized_statement);
 			if(prefs.insert_space_after_opening_paren_in_synchronized_statement)
 				scribe.space();
 			expression.accept(this);
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_synchronized_statement);
 		}
 		formatSubStatement(node.getBody(), false, true, !prefs.keep_simple_synchronized_statement_on_same_line, prefs.brace_position_for_synchronized_statement);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(TemplateDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKtemplate);
 		scribe.space();
 		node.getName().accept(this);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_template_declarations);
 		if(prefs.insert_space_after_opening_paren_in_template_declarations)
 			scribe.space();
 		List<TemplateParameter> templateParameters = node.templateParameters();
 		if(null != templateParameters && !templateParameters.isEmpty())
 			formatCSV(node.templateParameters(),
 					prefs.insert_space_before_comma_in_template_declaration,
 					prefs.insert_space_after_comma_in_template_declaration);
 		else if(prefs.insert_space_between_empty_parens_in_template_declarations)
 			scribe.space();
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_template_declarations);
 		formatDeclarationBlock(node.declarations(), prefs.brace_position_for_template_declaration, prefs.indent_body_declarations_compare_to_template_header);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(TemplateMixinDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKmixin);
 		scribe.space();
 		node.getType().accept(this);
 		SimpleName name = node.getName();
 		if(null != name)
 		{
 			scribe.space();
 			name.accept(this);
 		}
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon,
 					prefs.insert_space_before_semicolon);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(TemplateType node)
 	{
 		node.getName().accept(this);
 		scribe.printNextToken(TOK.TOKnot,
 				prefs.insert_space_before_exclamation_point_in_template_invocation);
 		if(prefs.insert_space_after_exclamation_point_in_template_invocation)
 			scribe.space();
 		scribe.printNextToken(TOK.TOKlparen);
 		if(prefs.insert_space_after_opening_paren_in_template_invocation)
 			scribe.space();
 		
 		List<ASTNode> arguments = node.arguments();
 		if(null != arguments && !arguments.isEmpty())
 			formatCSV(node.arguments(),
 					prefs.insert_space_before_comma_in_template_invocation,
 					prefs.insert_space_after_comma_in_template_invocation);
 		else if(prefs.insert_space_between_empty_parens_in_template_invocation)
 			scribe.space();
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_template_invocation);
 		if(isNextToken(TOK.TOKlparen) && prefs.insert_space_between_template_args_and_function_args)
 			scribe.space();
 		return false;
 	}
 	
 	public boolean visit(ThisLiteral node)
 	{
 		scribe.printNextToken(TOK.TOKthis);
 		return false;
 	}
 	
 	public boolean visit(ThrowStatement node)
 	{
 		scribe.printNextToken(TOK.TOKthrow);
 		scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKsemicolon,
 				prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(TraitsExpression node)
 	{
 		scribe.printNextToken(TOK.TOKtraits);
 		scribe.printNextToken(TOK.TOKlparen,
 				prefs.insert_space_before_opening_paren_in_traits_expression);
 		if(prefs.insert_space_after_opening_paren_in_traits_expression)
 			scribe.space();
 		List<ASTNode> args = new ArrayList<ASTNode>();
 		args.add(node.getName());
 		args.addAll(node.arguments());
 		formatCSV(args,
 				prefs.insert_space_before_comma_in_traits_expression,
 				prefs.insert_space_after_comma_in_traits_expression);
 		scribe.printNextToken(TOK.TOKrparen,
 				prefs.insert_space_before_closing_paren_in_traits_expression);
 		return false;
 	}
 	
 	public boolean visit(TryStatement node)
 	{
 		scribe.printNextToken(TOK.TOKtry);
 		boolean simple = formatSubStatement(node.getBody(), prefs.insert_new_line_before_catch, true, !prefs.keep_simple_try_statement_on_same_line, prefs.brace_position_for_try_catch_finally);
 		if (simple) {
 			scribe.printNewLine();
 		}
 		List<CatchClause> catchClauses = node.catchClauses();
 		for(CatchClause c : catchClauses) {
 			c.accept(this);
 		}
 		Statement $finally = node.getFinally();
 		if(null != $finally)
 		{
 			if (prefs.insert_new_line_before_finally) {
 				scribe.printNewLine();
 			}
 			scribe.printNextToken(TOK.TOKfinally);
 			formatSubStatement($finally, false, true, !prefs.keep_simple_finally_statement_on_same_line, prefs.brace_position_for_try_catch_finally);
 		}
 		scribe.printTrailingComment();
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(TupleTemplateParameter node)
 	{
 		node.getName().accept(this);
 		scribe.printNextToken(TOK.TOKdotdotdot,
 				prefs.insert_space_before_elipsis_in_tuples);
 		if(prefs.insert_space_after_elipsis_in_tuples)
 			scribe.space();
 		return false;
 	}
 	
 	public boolean visit(TypedefDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKtypedef);
 		scribe.space();
 		node.getType().accept(this);
 		if(!nameAlreadyPrinted)
 			scribe.space();
 		formatCSV(node.fragments(),
 				prefs.insert_space_before_comma_in_multiple_field_declarations,
 				prefs.insert_space_after_comma_in_multiple_field_declarations);
 		printSemicolonIfNextIsSemicolonAndNotInForInitializer();
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(TypedefDeclarationFragment node)
 	{
 		if(!nameAlreadyPrinted)
 			node.getName().accept(this);
 		else
 			nameAlreadyPrinted = false;
 		Initializer init = node.getInitializer();
 		if(null != init)
 		{
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_variable_inits);
 			if(prefs.insert_space_after_equals_in_variable_inits)
 				scribe.space();
 			init.accept(this);
 		}
 		return false;
 	}
 	
 	public boolean visit(TypeDotIdentifierExpression node)
 	{
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_type_dot_identifier_expression);
 			if(prefs.insert_space_after_opening_paren_in_type_dot_identifier_expression)
 				scribe.space();
 			node.getType().accept(this);
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_type_dot_identifier_expression);
 		}
 		else
 			node.getType().accept(this);
 		scribe.printNextToken(TOK.TOKdot, 
 				prefs.insert_space_before_dot_in_type_dot_identifier_expressions);
 		if(prefs.insert_space_after_dot_in_type_dot_identifier_expressions)
 			scribe.space();
 		node.getName().accept(this);
 		return false;
 	}
 	
 	public boolean visit(TypeExpression node)
 	{
 		node.getType().accept(this);
 		return false;
 	}
 	
 	public boolean visit(TypeidExpression node)
 	{
 		scribe.printNextToken(TOK.TOKtypeid);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_typeid_statements);
 		if(prefs.insert_space_after_opening_paren_in_typeid_statements)
 			scribe.space();
 		node.getType().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_typeid_statements);
 		return false;
 	}
 	
 	public boolean visit(TypeofType node)
 	{
 		scribe.printNextToken(TOK.TOKtypeof);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_typeof_statements);
 		if(prefs.insert_space_after_opening_paren_in_typeof_statements)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_typeof_statements);
 		return false;
 	}
 	
 	public boolean visit(TypeTemplateParameter node)
 	{
 		node.getName().accept(this);
 		Type specificType = node.getSpecificType();
 		if(null != specificType)
 		{
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_template_specific_type);
 			if(prefs.insert_space_after_colon_in_template_specific_type)
 				scribe.space();
 			specificType.accept(this);
 		}
 		Type defaultType = node.getDefaultType();
 		if(null != defaultType)
 		{
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_default_template_arguments);
 			if(prefs.insert_space_after_equals_in_default_template_arguments)
 				scribe.space();
 			defaultType.accept(this);
 		}
 		return false;
 	}
 	
 	public boolean visit(UnitTestDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKunittest);
 		scribe.space();
 		node.getBody().accept(this);
 		if(isNextToken(TOK.TOKsemicolon)) {
 			scribe.printNextToken(TOK.TOKsemicolon,
 					prefs.insert_space_before_semicolon);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(ValueTemplateParameter node)
 	{
 		node.getType().accept(this);
 		if(!nameAlreadyPrinted)
 		{
 			scribe.space();
 			node.getName().accept(this);
 		}
 		else
 			nameAlreadyPrinted = false;
 		Expression specificValue = node.getSpecificValue();
 		if(null != specificValue)
 		{
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_template_specific_type);
 			if(prefs.insert_space_after_colon_in_template_specific_type)
 				scribe.space();
 			specificValue.accept(this);
 		}
 		Expression defaultValue = node.getDefaultValue();
 		if(null != defaultValue)
 		{
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_default_template_arguments);
 			if(prefs.insert_space_after_equals_in_default_template_arguments)
 				scribe.space();
 			defaultValue.accept(this);
 		}
 		return false;
 	}
 
 	public boolean visit(VariableDeclaration node)
 	{
 		formatModifiers(true);
 		Type type = node.getType();
 		if(null != type)
 		{
 			type.accept(this);
 			if(!nameAlreadyPrinted)
 				scribe.space();
 		}
 		formatCSV(node.fragments(), 
 				prefs.insert_space_before_comma_in_multiple_field_declarations,
 				prefs.insert_space_after_comma_in_multiple_field_declarations);
 		postfixes.clear();
 		
 		printSemicolonIfNextIsSemicolonAndNotInForInitializer();
 		scribe.printTrailingComment();
 		return false;
 	}
 
 	public boolean visit(VariableDeclarationFragment node)
 	{
 		if(!nameAlreadyPrinted)
 			node.getName().accept(this);
 		else
 			nameAlreadyPrinted = false;
 		formatPostfixedDeclarations();
 		Initializer init = node.getInitializer();
 		if(null != init)
 		{
 			scribe.printNextToken(TOK.TOKassign,
 					prefs.insert_space_before_equals_in_variable_inits);
 			if(prefs.insert_space_after_equals_in_variable_inits)
 				scribe.space();
 			init.accept(this);
 		}
 		return false;
 	}
 
 	public boolean visit(Version node)
 	{
 		scribe.printNextToken(literalOrIdentiferTokenList());
 		return false;
 	}
 	
 	public boolean visit(VersionAssignment node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKversion);
 		scribe.printNextToken(TOK.TOKassign,
 				prefs.insert_space_before_equals_in_version_debug_assignment);
 		if(prefs.insert_space_after_equals_in_version_debug_assignment)
 			scribe.space();
 		node.getVersion().accept(this);
 		scribe.printNextToken(TOK.TOKsemicolon,
 				prefs.insert_space_before_semicolon);
 		scribe.printTrailingComment();
 		return false;
 	}
 
 	public boolean visit(VersionDeclaration node)
 	{
 		formatModifiers(true);
 		scribe.printNextToken(TOK.TOKversion);
 		Version version = node.getVersion();
 		if(null != version)
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_version_debug);
 			if(prefs.insert_space_after_opening_paren_in_version_debug)
 				scribe.space();
 			version.accept(this);
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_version_debug);
 		}
 		formatConditionalDeclaration(node);
 		scribe.printTrailingComment();
 		return false;
 	}
 
 	public boolean visit(VersionStatement node)
 	{
 		scribe.printNextToken(TOK.TOKversion);
 		Version version = node.getVersion();
 		if(null != version)
 		{
 			scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_version_debug);
 			if(prefs.insert_space_after_opening_paren_in_version_debug)
 				scribe.space();
 			version.accept(this);
 			scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_version_debug);
 		}
 		formatConditionalStatement(node);
 		scribe.printTrailingComment();
 		return false;
 	}
 	
 	public boolean visit(VoidInitializer node)
 	{
 		scribe.printNextToken(TOK.TOKvoid);
 		return false;
 	}
 
 	public boolean visit(VolatileStatement node)
 	{
 		scribe.printNextToken(TOK.TOKvolatile);
 		Statement body = node.getBody();
 		if(body instanceof Block)
 			formatSubStatement(body, false, true, true, prefs.brace_position_for_synchronized_statement);
 		else
 		{
 			scribe.space();
 			body.accept(this);
 		}
 		scribe.printTrailingComment();
 		return false;
 	}
 
 	public boolean visit(WhileStatement node)
 	{
 		scribe.printNextToken(TOK.TOKwhile);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_while_loops);
 		if(prefs.insert_space_after_opening_paren_in_while_loops)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_while_loops);
 		formatSubStatement(node.getBody(), false, true, !prefs.keep_simple_loop_statement_on_same_line, prefs.brace_position_for_loop_statement);
 		scribe.printTrailingComment();
 		return false;
 	}
 
 	public boolean visit(WithStatement node)
 	{
 		scribe.printNextToken(TOK.TOKwith);
 		scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_with_statements);
 		if(prefs.insert_space_after_opening_paren_in_with_statements)
 			scribe.space();
 		node.getExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_with_statements);
 		formatSubStatement(node.getBody(), false, true, !prefs.keep_simple_with_statement_on_same_line, prefs.brace_position_for_with_statement);
 		scribe.printTrailingComment();
 		return false;
 	}
 
 	private void formatCaseOrDefaultStatementBody(Block body)
 	{
 		if(body.statements().size() == 1 && 
 				body.statements().get(0) instanceof Block)
 		{
 			formatSubStatement(body.statements().get(0),
 					true,
 					prefs.indent_statements_compare_to_case, 
 					false, 
 					prefs.brace_position_for_switch_case);
 		}
 		else if(body.statements().size() > 0)
 		{
 			if(prefs.insert_new_line_after_case_or_default_statement)
 				scribe.printNewLine();
 			boolean unIndentBreak = !prefs.indent_break_compare_to_switch
 					&& prefs.indent_statements_compare_to_case
 					&& body.statements().get(body.statements().size() - 1)
 							instanceof BreakStatement;
 			if(prefs.indent_statements_compare_to_case)
 				scribe.indent();
 			formatStatements(body.statements(), true, unIndentBreak);
 			if(prefs.indent_statements_compare_to_case && !unIndentBreak)
 				scribe.unIndent();
 		}
 	}
 
 	private void formatConditionalDeclaration(ConditionalDeclaration node)
 	{
 		boolean simple = formatDeclarationBlock(node.thenDeclarations(), prefs.brace_position_for_conditional_declaration, true, !prefs.keep_simple_then_declaration_on_same_line);
 		if(isNextToken(TOK.TOKelse))
 		{
 			if (prefs.insert_new_line_before_else || simple) {
 				scribe.printNewLine();
 			} else {
 				scribe.space();
 			}
 			scribe.printNextToken(TOK.TOKelse);
 			
 			// handle "else if/debug/version"
 			if (node.elseDeclarations().size() == 1 
 					&& node.elseDeclarations().get(0).getClass().equals(node.getClass())
 					&& !isNextToken(TOK.TOKlcurly) && !isNextToken(TOK.TOKcolon)) {
 				if (prefs.keep_else_conditional_on_one_line) {
 					scribe.space();
 				} else {
 					scribe.printNewLine();
 				}
 				node.elseDeclarations().get(0).accept(this);
 			} else {
 				formatDeclarationBlock(node.elseDeclarations(), prefs.brace_position_for_conditional_declaration, true, !prefs.keep_simple_else_declaration_on_same_line);
 			}
 		}
 	}
 	
 	private void formatConditionalStatement(ConditionalStatement node)
 	{
 		boolean simple = formatSubStatement(node.getThenBody(), false, true, !prefs.keep_simple_then_statement_on_same_line, prefs.brace_position_for_conditional_statement);
 		if(isNextToken(TOK.TOKelse))
 		{
 			if (prefs.insert_new_line_before_else || (simple && prefs.keep_simple_then_statement_on_same_line)) {
 				scribe.printNewLine();
 			} else {
 				scribe.space();
 			}
 			scribe.printNextToken(TOK.TOKelse);
 			Statement elseBody = node.getElseBody();
 			
 			// handle "else if/debug/version"
 			if(elseBody.getClass().equals(node.getClass()))
 			{
 				if (prefs.keep_else_conditional_on_one_line) {
 					scribe.space();
 				} else {
 					scribe.printNewLine();
 				}
 				formatSubStatement(elseBody, false, false, false, prefs.brace_position_for_conditional_statement);
 			}
 			else
 			{
 				formatSubStatement(elseBody, false, true, !prefs.keep_simple_else_statement_on_same_line, prefs.brace_position_for_conditional_statement);
 			}
 		}
 	}
 	
 	/**
 	 * Formats a list of comma-separated values. This may not be appropriate for
 	 * all CSV lists (for example, if the values need to be on separate lines),
 	 * but should increase code reuse since the JDT repeats the logic here about
 	 * 20 times.
 	 */
 	private void formatCSV(List<? extends ASTNode> values, boolean insertSpacesBeforeComma, boolean insertSpacesAfterComma)
 	{
 		int len = values.size();
 		if(len == 0)
 			return;
 		
 		for (int i = 0; i < len - 1; i++)
 		{
 			values.get(i).accept(this);
 			scribe.printNextToken(TOK.TOKcomma, insertSpacesBeforeComma);
 			if(insertSpacesAfterComma) {
 				scribe.space();
 			}
 		}
 		
 		values.get(len - 1).accept(this);
 		
 	}
 	
 	// Returns true if it was a simple declaration block (no "{" or ":") 
 	private boolean formatDeclarationBlock(List<Declaration> declarations, 
 			BracePosition bracePosition, boolean indent) {
 		return formatDeclarationBlock(declarations, bracePosition, indent, true);
 	}
 	
 	private boolean formatDeclarationBlock(List<Declaration> declarations, 
 			BracePosition bracePosition, boolean indent, boolean identIfSimple)
 	{
 		if(isNextToken(TOK.TOKlcurly))
 		{
 			formatOpeningBrace(bracePosition, indent);
 			scribe.printNewLine();
 			formatDeclarations(declarations);
 			scribe.printNewLine();
 			formatClosingBrace(bracePosition, indent);
 		}
 		else if(isNextToken(TOK.TOKcolon))
 		{
 			scribe.printNextToken(TOK.TOKcolon, prefs.insert_space_before_colon_in_modifiers);
 			scribe.printNewLine();
 			
 			// The AST often renders these as empty -- if so, just return
 			if(declarations.isEmpty())
 				return false;
 			
 			if(indent)
 				scribe.indent();
 			formatDeclarations(declarations);
 			if(indent)
 				scribe.unIndent();
 		}
 		else
 		{
 			if (identIfSimple) {
 				scribe.printNewLine();
 				scribe.indent();
 			} else {
 				scribe.space();
 			}
 			formatDeclarations(declarations);
 			if (identIfSimple) {
 				scribe.unIndent();
 			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private void formatDeclarations(List<Declaration> declarations)
 	{
 		if(null == declarations || declarations.isEmpty())
 			return;
 		
 		Declaration prev = null;
 		int len = declarations.size();
 		for (int i = 0; i < len; i++)
 		{
 			Declaration cur = declarations.get(i);
 			
 			// Determine how many lines to put above this declararation
 			int blankLines = 0;
 			
 			if (i != 0) {
 				blankLines++;
 			}
 			
 			// No blank lines before first (yet; this will be configurable)
 			if(prev == null || 
 			   (prev instanceof AliasDeclaration && cur instanceof AliasDeclaration) ||
 			   (prev instanceof AlignDeclaration && cur instanceof AlignDeclaration) ||
 			   (prev instanceof DebugAssignment && cur instanceof DebugAssignment) ||
 			   (prev instanceof ImportDeclaration && cur instanceof ImportDeclaration) ||
 			   (prev instanceof InvariantDeclaration && cur instanceof InvariantDeclaration) ||
 			   (prev instanceof MixinDeclaration && cur instanceof MixinDeclaration) ||
 			   (prev instanceof PragmaDeclaration && cur instanceof PragmaDeclaration) ||
 			   (prev instanceof StaticAssert && cur instanceof StaticAssert) ||
 			   (prev instanceof TypedefDeclaration && cur instanceof TypedefDeclaration) ||
 			   (prev instanceof VariableDeclaration && cur instanceof VariableDeclaration) ||
 			   (prev instanceof VersionAssignment && cur instanceof VersionAssignment) ||
 			   (prev instanceof DebugAssignment && cur instanceof VersionAssignment) ||
 			   (prev instanceof VersionAssignment && cur instanceof DebugAssignment)
 			  ) {
 				// nothing
 			} else {
 				blankLines++;
 			}
 			
 			// I don't know why, but this works
 			if(blankLines == 1) {
 				scribe.printNewLine();
 			} else if (blankLines > 1) {
 				scribe.printEmptyLines(blankLines - 1);
 			}
 			
 			cur.accept(this);
 			prev = cur;
 		}
 	}
 	
 	/**
 	 * Formats the given function. This is used because functions will be
 	 * visited as FunctionDeclarations and ConstructorDeclarations.
 	 * @param bracePosition the position to sue for the brace
 	 */
 	private void formatFunction(IFunctionDeclaration node, BracePosition bracePosition)
 	{
 		if(isNextToken(TOK.TOKlparen))
 		{
 			scribe.printNextToken(TOK.TOKlparen, this.prefs.insert_space_before_opening_paren_in_function_declaration_parameters);
 			if(prefs.insert_space_after_opening_paren_in_function_declaration_parameters)
 				scribe.space();
 			
 			if (node.arguments().size() == 0 && !node.isVariadic())
 			{
 				if (this.prefs.insert_space_between_empty_parens_in_function_declaration_parameters) {
 					this.scribe.space();
 				}
 			}
 			if (node.arguments().size() > 0)
 			{
 				if (this.prefs.insert_space_after_opening_paren_in_function_declaration_parameters) {
 					this.scribe.space();
 				}
 				formatCSV(node.arguments(),
 						prefs.insert_space_before_comma_in_function_declaration_parameters,
 						prefs.insert_space_after_comma_in_function_declaration_parameters);
 			}
 			if (node.isVariadic())
 			{
 				if (node.arguments().size() > 0)
 				{
 					// Varidic functions can take a named varidic paramater, eg.
 					// void foo(int[] bar...) { } or an un-named parameter
 					// preceded by a comma as in void foo(int x, ...)
 					if(isNextToken(TOK.TOKcomma))
 					{
 						scribe.printNextToken(TOK.TOKcomma, prefs.insert_space_before_comma_in_function_declaration_parameters);
 						if(prefs.insert_space_after_comma_in_function_declaration_parameters)
 							scribe.space();
 					}
 				}
 				scribe.printNextToken(TOK.TOKdotdotdot,
 						prefs.insert_space_before_elipsis_in_function_varargs);
 				if(prefs.insert_space_after_elipsis_in_function_varargs)
 					scribe.space();
 			}
 			scribe.printNextToken(TOK.TOKrparen, this.prefs.insert_space_before_closing_paren_in_function_declaration_parameters);
 		}
 		
 		Block in   = (Block) node.getPrecondition();
 		Block out  = (Block) node.getPostcondition();
 		Block body = (Block) node.getBody();
 		
 		boolean lastWasInOrOutOrBody = false;
 		if (null != in || null != out)
 		{
 			scribe.printNewLine();
 			if (prefs.indent_in_out_body_compare_to_function_header) {
 				scribe.indent();
 			}
 			loop: do
 				switch (nextNonCommentToken())
 				{
 					case TOKin:
 						if (lastWasInOrOutOrBody) {
 							scribe.printNewLine();
 						}
 						lastWasInOrOutOrBody = true;
 						scribe.printNextToken(TOK.TOKin);
 						formatSubStatement(in, false, prefs.indent_statements_compare_to_function_in_header, true, bracePosition);
 						continue loop;
 					case TOKout:
 						if (lastWasInOrOutOrBody) {
 							scribe.printNewLine();
 						}
 						lastWasInOrOutOrBody = true;
 						scribe.printNextToken(TOK.TOKout);
 						if(isNextToken(TOK.TOKlparen))
 						{
 							scribe.printNextToken(TOK.TOKlparen, prefs.insert_space_before_opening_paren_in_out_declaration);
 							if(prefs.insert_space_after_opening_paren_in_out_declaration)
 								scribe.space();
 							SimpleName name = node.getPostconditionVariableName();
 							if(null != name)
 								scribe.printNextToken(TOK.TOKidentifier);
 							else if(prefs.insert_space_between_empty_parens_in_out_declaration)
 								scribe.space();
 							scribe.printNextToken(TOK.TOKrparen, prefs.insert_space_before_closing_paren_in_out_declaration);
 						}
 						formatSubStatement(out, false, prefs.indent_statements_compare_to_function_out_header, true, bracePosition);
 						continue loop;
 					case TOKbody:
 						if (lastWasInOrOutOrBody) {
 							scribe.printNewLine();
 						}
 						lastWasInOrOutOrBody = true;
 						scribe.printNextToken(TOK.TOKbody);
 						formatSubStatement(body, false, prefs.indent_statements_compare_to_function_body_header, true, bracePosition);
 						continue loop;
 					default:
 						break loop;
 				}
 			while (true);
 			if (prefs.indent_in_out_body_compare_to_function_header) {
 				scribe.unIndent();
 			}
 		}
 		else if (isNextToken(TOK.TOKbody))
 		{
 			scribe.printNewLine();
 			if (prefs.indent_in_out_body_compare_to_function_header) {
 				scribe.indent();
 			}
 			scribe.printNextToken(TOK.TOKbody);
 			formatSubStatement(body, false, prefs.indent_statements_compare_to_function_body_header, true, bracePosition);
 			if (prefs.indent_in_out_body_compare_to_function_header) {
 				scribe.unIndent();
 			}
 		}
 		else {
 			if (body != null) {
 				if (this.prefs.keep_functions_with_no_statement_in_one_line
 						&& body.statements().size() == 0) {
 					
 					scribe.space();
 					scribe.printNextToken(TOK.TOKlcurly);
 					scribe.space();
 					scribe.printNextToken(TOK.TOKrcurly);
 					
 				} else if (this.prefs.keep_functions_with_one_statement_in_one_line
 						&& body.statements().size() == 1) {
 					
 					scribe.space();
 					scribe.printNextToken(TOK.TOKlcurly);
 					scribe.space();
 					body.statements().get(0).accept(this);
 					scribe.space();
 					scribe.printNextToken(TOK.TOKrcurly);
 					
 				} else {
 					
 					formatSubStatement(body, false, prefs.indent_statements_compare_to_function_header, true, bracePosition);
 					
 				}
 			} else {
 				// no method body
 				scribe.printNextToken(TOK.TOKsemicolon, this.prefs.insert_space_before_semicolon);
 			}
 		}
 		
 		scribe.printTrailingComment();
 	}
 	
 	/**
 	 * Prints all modifiers (which may include ones that are normally part of
 	 * the declaration, such as a "static if").
 	 * 
 	 * @param spaceAtEnd insert a space after the last modifier is printed (if
 	 *                   no modifiers were printed, no space will be inserted,
 	 *                   regardless of the value of the parameter).
 	 */
 	private void formatModifiers(boolean spaceAtEnd)
 	{
 		boolean printed = false; // Has anything been printed?
 		loop: while(true)
 		{
 			switch(nextNonCommentToken())
 			{
 				case TOKprivate:
 				case TOKprotected:
 				case TOKpackage:
 				case TOKpublic:
 				case TOKexport:
 				case TOKfinal:
 				case TOKdeprecated:
 				case TOKconst:
 				case TOKscope:
 				case TOKstatic:
 				case TOKabstract:
 				case TOKoverride:
 				case TOKauto:
 				case TOKsynchronized:
 				case TOKextern:
 				case TOKinvariant:
 				case TOKin:
 				case TOKout:
 				case TOKinout:
 				case TOKref:
 				case TOKlazy:
 					scribe.printNextToken(modifierTokenList(), printed);
 					printed = true;
 					break;
 				default:
 					break loop;
 			}
 		}
 		if(printed && spaceAtEnd)
 			scribe.space();
 	}
 	
 	/**
 	 * Given a series of statements (such as in a function body), formats them
 	 * such that only a single non-empty statement appears per line. Calls
 	 * accept() on each node.
 	 * @param unIndentAtBreak ugly hack to allow break statements to be
 	 *                        indented separately from cases. Seemed better
 	 *                        to do it this way that re-implement this method
 	 *                        in formatCaseOrDefaultStatementBody
 	 */
 	private void formatStatements(final List<Statement> statements,
 			boolean insertNewLineAfterLastStatement, boolean unIndentAtBreak)
 	{
 		int statementsLength = statements.size();
 		if (statementsLength == 0)
 			return;
 		else if (statementsLength == 1) {
 			if (statements.get(0) instanceof BreakStatement && unIndentAtBreak)
 				scribe.unIndent();
 			statements.get(0).accept(this);
 		} else {
 			Statement previousStatement = statements.get(0);
 			previousStatement.accept(this);
 			int previousStatementNodeType = previousStatement.getNodeType();
 			for (int i = 1; i < statementsLength - 1; i++) {
 				Statement statement = statements.get(i);
 				int statementNodeType = statement.getNodeType();
 				if ((previousStatementNodeType == ASTNode.EMPTY_STATEMENT && statementNodeType != ASTNode.EMPTY_STATEMENT)
 						|| (previousStatementNodeType != ASTNode.EMPTY_STATEMENT && statementNodeType != ASTNode.EMPTY_STATEMENT))
 					scribe.printNewLine();
 				statement.accept(this);
 				previousStatement = statement;
 			}
 			Statement statement = statements.get(statementsLength - 1);
 			int statementNodeType = statement.getNodeType();
 			if ((previousStatementNodeType == ASTNode.EMPTY_STATEMENT && statementNodeType != ASTNode.EMPTY_STATEMENT)
 					|| (previousStatementNodeType != ASTNode.EMPTY_STATEMENT && statementNodeType != ASTNode.EMPTY_STATEMENT))
 				scribe.printNewLine();
 			if (statement instanceof BreakStatement && unIndentAtBreak)
 				scribe.unIndent();
 			statement.accept(this);
 		}
 
 		if (insertNewLineAfterLastStatement)
 			scribe.printNewLine();
 	}
 	
 	/**
 	 * Returns true if this was a simple sub-statement.
 	 */
 	private boolean formatSubStatement(Statement statement, boolean newLineAtEnd,
 			boolean indent, boolean indentSimple, BracePosition bracePosition)
 	{
 		boolean simple = false;
 		if(statement instanceof EmptyStatement)
 		{
 			statement.accept(this);
 			simple = true;
 		}
 		
 		if(statement instanceof Block)
 		{
 			formatOpeningBrace(bracePosition, indent);
 			scribe.printNewLine();
 			formatStatements(((Block) statement).statements(), true, false);
 			formatClosingBrace(bracePosition, indent);
 		}		
 		else
 		{
 			scribe.space();
 			if(indentSimple) {
 				scribe.printNewLine();
 				scribe.indent();
 			}
 			statement.accept(this);
 			if(indentSimple) {
 				scribe.unIndent();
 				scribe.printNewLine();
 			}
 			
 			simple = true;
 		}
 		
 		if(newLineAtEnd) {
 			scribe.printNewLine();
 		} else {
 			scribe.space();
 		}
 		
 		return simple;
 	}
 	
 	private void formatOpeningBrace(BracePosition bracePosition, boolean indent)
 	{
 		if (bracePosition == BracePosition.NEXT_LINE) {
 			scribe.printNewLine();
 		} else if (bracePosition == BracePosition.NEXT_LINE_SHIFTED) {
 			scribe.printNewLine();
 			if(indent)
 			{
 				scribe.indent();
 			}
 		} else {
 			scribe.space();
 		}
 		scribe.printNextToken(TOK.TOKlcurly);
 		if (indent) {
 			scribe.indent();
 		}
 		scribe.printTrailingComment();
 	}
 	
 	private void formatClosingBrace(BracePosition bracePosition, boolean indent)
 	{
 		if(!indent)
 			scribe.printNextToken(TOK.TOKrcurly);
 		else
 		{
 			if(bracePosition == BracePosition.NEXT_LINE_SHIFTED)
 			{
 				scribe.unIndent();
 				scribe.printNextToken(TOK.TOKrcurly);
 				scribe.unIndent();
 			}
 			else
 			{
 				scribe.unIndent();
 				scribe.printNextToken(TOK.TOKrcurly);
 			}
 		}
 	}
 	
 	private void formatTemplateParams(List<TemplateParameter> tp,
 			boolean spaceBeforeOpeningParen,
 			boolean spaceAfterOpeningParen,
 			boolean spaceBeforeClosingParen, boolean spaceBeforeComma, boolean spaceAfterComma)
 	{
 		if(tp.isEmpty())
 			return;
 		scribe.printNextToken(TOK.TOKlparen, spaceBeforeOpeningParen);
 		if(spaceAfterOpeningParen)
 			scribe.space();
 		formatCSV(tp, spaceBeforeComma, spaceAfterComma);
 		scribe.printNextToken(TOK.TOKrparen, spaceBeforeClosingParen);
 	}
 	
 	private void formatPostfixedDeclarations()
 	{
 		if (postfixes.isEmpty())
 			return;
 
 		for (Type node : postfixes)
 		{
 			if (null != node)
 			{
 				if (node instanceof DynamicArrayType)
 					formatPostfix((DynamicArrayType) node, true);
 				else if (node instanceof AssociativeArrayType)
 					formatPostfix((AssociativeArrayType) node, true);
 				else if (node instanceof StaticArrayType)
 					formatPostfix((StaticArrayType) node, true);
 				else if (node instanceof SliceType)
 					formatPostfix((SliceType) node, true);
 			}
 		}
 		
 		/**
 		 * The list of postfixes can't be emptied here, in the rare case that there are
 		 * multiple variable declarations in a declaration list (which all must be the
 		 * same according to the D spec), for example: "char v1[][3], v2[][3];". The list
 		 * is cleared after it's used. This also allows combined prefix and postfix
 		 * declarations, i.e.:
 		 * 
 		 * char[] var[3];
 		 */
 	}
 
 	private void formatPostfix(DynamicArrayType node, boolean postfixed)
 	{
 		scribe.printNextToken(TOK.TOKlbracket);
 		scribe.printNextToken(TOK.TOKrbracket, 
 				prefs.insert_space_between_empty_brackets_in_dynamic_array_type);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 	}
 
 	private void formatPostfix(AssociativeArrayType node, boolean postfixed)
 	{
 		scribe.printNextToken(TOK.TOKlbracket, 
 				prefs.insert_space_before_opening_bracket_in_associative_arrays);
 		if(prefs.insert_space_after_opening_bracket_in_associative_arrays)
 			scribe.space();
 		node.getKeyType().accept(this);
 		scribe.printNextToken(TOK.TOKrbracket, 
 				prefs.insert_space_before_closing_bracket_in_associative_arrays);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 	}
 
 	private void formatPostfix(StaticArrayType node, boolean postfixed)
 	{
 		scribe.printNextToken(TOK.TOKlbracket,
 				prefs.insert_space_before_opening_bracket_in_static_arrays);
 		if(prefs.insert_space_after_opening_bracket_in_static_arrays)
 			scribe.space();
 		node.getSize().accept(this);
 		scribe.printNextToken(TOK.TOKrbracket,
 				prefs.insert_space_before_closing_bracket_in_static_arrays);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 	}
 
 	private void formatPostfix(SliceType node, boolean postfixed)
 	{
 		scribe.printNextToken(TOK.TOKlbracket,
 				prefs.insert_space_before_opening_bracket_in_slices);
 		if(prefs.insert_space_after_opening_bracket_in_slices)
 			scribe.space();
 		node.getFromExpression().accept(this);
 		scribe.printNextToken(TOK.TOKslice,
 				prefs.insert_space_before_slice_operator);
 		if(prefs.insert_space_after_slice_operator)
 			scribe.space();
 		node.getToExpression().accept(this);
 		scribe.printNextToken(TOK.TOKrbracket,
 				prefs.insert_space_before_closing_bracket_in_slices);
 		if(isNextToken(TOK.TOKlbracket) && 
 				prefs.insert_space_between_adjacent_brackets_in_multidimensional_arrays)
 			scribe.space();
 	}
 	
 	private TextEdit failedToFormat(Throwable e)
 	{
 		if (DEBUG)
 			e.printStackTrace();
 		return null;
 	}
 	
 	private boolean hasComments()
 	{
 		lexer.reset(scribe.lexer.base, scribe.scannerEndPosition - 1);
 		return isComment(lexer.nextToken());
 	}
 	
 	/**
 	 * This method exists because it's legal to have a function with an empty
 	 * template parameter list. Consider:
 	 *     int fn()() // Totally legal in D, believe it or not!
 	 * The only way I can think of to handle formatting this is to look ahead
 	 * three tokens for "()(", which is what this method does.
 	 * 
 	 * @param node the function declaration to check
 	 */
 	private boolean hasEmptyTemplateParamList(FunctionDeclaration node)
 	{
 		lexer.reset(scribe.lexer.p, scribe.scannerEndPosition - 1);
 		TOK tok1;
 		do
 			tok1 = lexer.nextToken();
 		while (isComment(tok1));
 		
 		TOK tok2;
 		do
 			tok2 = lexer.nextToken();
 		while (isComment(tok2));
 		
 		TOK tok3;
 		do
 			tok3 = lexer.nextToken();
 		while (isComment(tok3));
 		
 		return(tok1 == TOK.TOKlparen && tok2 == TOK.TOKrparen &&
 			   tok3 == TOK.TOKlparen);
 	}
 	
 	/**
 	 * Checks whether the token is a comment
 	 * 
 	 * @param token
 	 * @return
 	 */
 	private boolean isComment(TOK token)
 	{
 		switch (token)
 		{
 			case TOKlinecomment:
 			case TOKblockcomment:
 			case TOKpluscomment:
 			case TOKdoclinecomment:
 			case TOKdocblockcomment:
 			case TOKdocpluscomment:
 				return true;
 			default:
 				return false;
 		}
 	}
 	
 	private boolean isNextToken(TOK test)
 	{
 		TOK token = nextNonCommentToken();
 		return token == test;
 	}
 	
 	/**
 	 * Kills off comments in the lexer until a non-comment token is reached
 	 */
 	private TOK nextNonCommentToken()
 	{
 		lexer.reset(scribe.lexer.p, scribe.scannerEndPosition - 1);
 		TOK token;
 		do
 			token = lexer.nextToken();
 		while (isComment(token));
 		return token;
 	}
 	
 	private void printSemicolonIfNextIsSemicolonAndNotInForInitializer() {
 		if(isNextToken(TOK.TOKsemicolon) && !inForInitializer) {
 			scribe.printNextToken(TOK.TOKsemicolon, prefs.insert_space_before_semicolon);
 		}
 		inForInitializer = false;
 	}
 	
 	/**
 	 * Resets the lexer with the specified source
 	 * 
 	 * @param source
 	 */
 	private void restartLexer(String source)
 	{
 		if (null == lexer)
			lexer = new Lexer(source, true, true, false, false, AST.D2);
 		else
 			lexer.reset(source.toCharArray(), 0, source.length(), true, true,
 					false, false);
 	}
 	
 	private void startup(String src, ASTNode node) throws AbortFormatting
 	{
 		if ((node.getFlags() & ASTNode.MALFORMED) != 0)
 			throw new AbortFormatting("Malformed AST node");
 		scribe.reset();
 		restartLexer(src);
 		scribe.initializeScanner(src);
 	}
 	
 	private static TOK[] PRIMITIVE_TYPES_LIST;
 	private static TOK[] LITERAL_OR_IDENTIFIER;
 	private static TOK[] MODIFIERS;
 	private static TOK[] ASSIGNMENT_OPERATORS;
 	private static TOK[] PREFIX_OPERATORS;
 	private static TOK[] POSTFIX_OPERATORS;
 	private static TOK[] INFIX_OPERATORS;
 	private static TOK[] BOOLEAN_LITERALS;
 	private static TOK[] CHARACTER_LITERALS;
 	private static TOK[] NUMERIC_LITERALS;
 	private static TOK[] TYPE_SPECIALIZATIONS;
 	
 	private static TOK[] assignmentOperatorTokenList()
 	{
 		if(null == ASSIGNMENT_OPERATORS)
 		{
 			ASSIGNMENT_OPERATORS = new TOK[13];
 			
 			ASSIGNMENT_OPERATORS[0] = TOK.TOKassign;
 			ASSIGNMENT_OPERATORS[1] = TOK.TOKmulass;
 			ASSIGNMENT_OPERATORS[2] = TOK.TOKdivass;
 			ASSIGNMENT_OPERATORS[3] = TOK.TOKmodass;
 			ASSIGNMENT_OPERATORS[4] = TOK.TOKaddass;
 			ASSIGNMENT_OPERATORS[5] = TOK.TOKminass;
 			ASSIGNMENT_OPERATORS[6] = TOK.TOKcatass;
 			ASSIGNMENT_OPERATORS[7] = TOK.TOKandass;
 			ASSIGNMENT_OPERATORS[8] = TOK.TOKxorass;
 			ASSIGNMENT_OPERATORS[9] = TOK.TOKorass;
 			ASSIGNMENT_OPERATORS[10] = TOK.TOKshlass;
 			ASSIGNMENT_OPERATORS[11] = TOK.TOKshrass;
 			ASSIGNMENT_OPERATORS[12] = TOK.TOKushrass;
 			Arrays.sort(ASSIGNMENT_OPERATORS);
 		}
 		return ASSIGNMENT_OPERATORS;
 	}
 	
 	
 	
 	private static TOK[] booleanLiteralTokenList()
 	{
 		if(null == BOOLEAN_LITERALS)
 		{
 			BOOLEAN_LITERALS = new TOK[2];
 			
 			// I bet you can guess what comes next...
 			BOOLEAN_LITERALS[0] = TOK.TOKtrue;
 			BOOLEAN_LITERALS[1] = TOK.TOKfalse;
 			Arrays.sort(BOOLEAN_LITERALS);
 		}
 		return BOOLEAN_LITERALS;
 	}
 	
 	private static TOK[] characterLiteralTokenList()
 	{
 		if(null == CHARACTER_LITERALS)
 		{
 			CHARACTER_LITERALS = new TOK[3];
 			
 			CHARACTER_LITERALS[0] = TOK.TOKcharv;
 			CHARACTER_LITERALS[1] = TOK.TOKwcharv;
 			CHARACTER_LITERALS[2] = TOK.TOKdcharv;
 			Arrays.sort(CHARACTER_LITERALS);
 		}
 		return CHARACTER_LITERALS;
 	}
 	
 	private static TOK[] infixOperatorTokenList()
 	{
 		if(null == INFIX_OPERATORS)
 		{
 			INFIX_OPERATORS = new TOK[33];
 			
 			INFIX_OPERATORS[0] = TOK.TOKmul;
 			INFIX_OPERATORS[1] = TOK.TOKdiv;
 			INFIX_OPERATORS[2] = TOK.TOKmod;
 			INFIX_OPERATORS[3] = TOK.TOKadd;
 			INFIX_OPERATORS[4] = TOK.TOKmin;
 			INFIX_OPERATORS[5] = TOK.TOKtilde;
 			INFIX_OPERATORS[6] = TOK.TOKshl;
 			INFIX_OPERATORS[7] = TOK.TOKshr;
 			INFIX_OPERATORS[8] = TOK.TOKushr;
 			INFIX_OPERATORS[9] = TOK.TOKlt;
 			INFIX_OPERATORS[10] = TOK.TOKgt;
 			INFIX_OPERATORS[11] = TOK.TOKle;
 			INFIX_OPERATORS[12] = TOK.TOKge;
 			INFIX_OPERATORS[13] = TOK.TOKequal;
 			INFIX_OPERATORS[14] = TOK.TOKnotequal;
 			INFIX_OPERATORS[15] = TOK.TOKxor;
 			INFIX_OPERATORS[16] = TOK.TOKand;
 			INFIX_OPERATORS[17] = TOK.TOKor;
 			INFIX_OPERATORS[18] = TOK.TOKandand;
 			INFIX_OPERATORS[19] = TOK.TOKoror;
 			INFIX_OPERATORS[20] = TOK.TOKcomma;
 			INFIX_OPERATORS[21] = TOK.TOKin;
 			INFIX_OPERATORS[22] = TOK.TOKis;
 			INFIX_OPERATORS[23] = TOK.TOKidentity;
 			INFIX_OPERATORS[24] = TOK.TOKnotidentity;
 			INFIX_OPERATORS[25] = TOK.TOKunord;
 			INFIX_OPERATORS[26] = TOK.TOKlg;
 			INFIX_OPERATORS[27] = TOK.TOKleg;
 			INFIX_OPERATORS[28] = TOK.TOKug;
 			INFIX_OPERATORS[29] = TOK.TOKuge;
 			INFIX_OPERATORS[30] = TOK.TOKul;
 			INFIX_OPERATORS[31] = TOK.TOKule;
 			INFIX_OPERATORS[32] = TOK.TOKue;
 			Arrays.sort(INFIX_OPERATORS);
 			
 		}
 		return INFIX_OPERATORS;
 	}
 	
 	private static TOK[] literalOrIdentiferTokenList()
 	{
 		if(null == LITERAL_OR_IDENTIFIER)
 		{
 			LITERAL_OR_IDENTIFIER = new TOK[18];
 			
 			LITERAL_OR_IDENTIFIER[0] = TOK.TOKint32v;
 			LITERAL_OR_IDENTIFIER[1] = TOK.TOKuns32v;
 			LITERAL_OR_IDENTIFIER[2] = TOK.TOKint64v;
 			LITERAL_OR_IDENTIFIER[3] = TOK.TOKuns64v;
 			LITERAL_OR_IDENTIFIER[4] = TOK.TOKfloat32v;
 			LITERAL_OR_IDENTIFIER[5] = TOK.TOKfloat64v;
 			LITERAL_OR_IDENTIFIER[6] = TOK.TOKfloat80v;
 			LITERAL_OR_IDENTIFIER[7] = TOK.TOKimaginary32v;
 			LITERAL_OR_IDENTIFIER[8] = TOK.TOKimaginary64v;
 			LITERAL_OR_IDENTIFIER[9] = TOK.TOKimaginary80v;
 			LITERAL_OR_IDENTIFIER[10] = TOK.TOKcharv;
 			LITERAL_OR_IDENTIFIER[11] = TOK.TOKwcharv;
 			LITERAL_OR_IDENTIFIER[12] = TOK.TOKdcharv;
 			LITERAL_OR_IDENTIFIER[13] = TOK.TOKidentifier;
 			LITERAL_OR_IDENTIFIER[14] = TOK.TOKstring;
 			LITERAL_OR_IDENTIFIER[15] = TOK.TOKtrue;
 			LITERAL_OR_IDENTIFIER[16] = TOK.TOKfalse;
 			LITERAL_OR_IDENTIFIER[17] = TOK.TOKnull;
 			Arrays.sort(LITERAL_OR_IDENTIFIER);
 		}
 		return LITERAL_OR_IDENTIFIER;
 	}
 	
 	private static TOK[] modifierTokenList()
 	{
 		if(null == MODIFIERS)
 		{
 			MODIFIERS = new TOK[21];
 			
 			MODIFIERS[0] = TOK.TOKprivate;
 			MODIFIERS[1] = TOK.TOKprotected;
 			MODIFIERS[2] = TOK.TOKpackage;
 			MODIFIERS[3] = TOK.TOKpublic;
 			MODIFIERS[4] = TOK.TOKexport;
 			MODIFIERS[5] = TOK.TOKstatic;
 			MODIFIERS[6] = TOK.TOKfinal;
 			MODIFIERS[7] = TOK.TOKabstract;
 			MODIFIERS[8] = TOK.TOKoverride;
 			MODIFIERS[9] = TOK.TOKauto;
 			MODIFIERS[10] = TOK.TOKsynchronized;
 			MODIFIERS[11] = TOK.TOKdeprecated;
 			MODIFIERS[12] = TOK.TOKextern;
 			MODIFIERS[13] = TOK.TOKconst;
 			MODIFIERS[14] = TOK.TOKscope;
 			MODIFIERS[15] = TOK.TOKinvariant;
 			MODIFIERS[16] = TOK.TOKin;
 			MODIFIERS[17] = TOK.TOKout;
 			MODIFIERS[18] = TOK.TOKlazy;
 			MODIFIERS[19] = TOK.TOKinout;
 			MODIFIERS[20] = TOK.TOKref;
 			Arrays.sort(MODIFIERS);
 		}
 		return MODIFIERS;
 	}
 	
 	private static TOK[] numericLiteralTokenList()
 	{
 		if(null == NUMERIC_LITERALS)
 		{
 			NUMERIC_LITERALS = new TOK[10];
 			
 			NUMERIC_LITERALS[0] = TOK.TOKint32v;
 			NUMERIC_LITERALS[1] = TOK.TOKuns32v;
 			NUMERIC_LITERALS[2] = TOK.TOKint64v;
 			NUMERIC_LITERALS[3] = TOK.TOKuns64v;
 			NUMERIC_LITERALS[4] = TOK.TOKfloat32v;
 			NUMERIC_LITERALS[5] = TOK.TOKfloat64v;
 			NUMERIC_LITERALS[6] = TOK.TOKfloat80v;
 			NUMERIC_LITERALS[7] = TOK.TOKimaginary32v;
 			NUMERIC_LITERALS[8] = TOK.TOKimaginary64v;
 			NUMERIC_LITERALS[9] = TOK.TOKimaginary80v;
 			Arrays.sort(NUMERIC_LITERALS);
 		}
 		return NUMERIC_LITERALS;
 	}
 	
 	private static TOK[] postfixOperatorTokenList()
 	{
 		if(null == POSTFIX_OPERATORS)
 		{
 			POSTFIX_OPERATORS = new TOK[2];
 			
 			POSTFIX_OPERATORS[0] = TOK.TOKplusplus;
 			POSTFIX_OPERATORS[1] = TOK.TOKminusminus;
 			Arrays.sort(POSTFIX_OPERATORS);
 		}
 		return POSTFIX_OPERATORS;
 	}
 	
 	private static TOK[] prefixOperatorTokenList()
 	{
 		if(null == PREFIX_OPERATORS)
 		{
 			PREFIX_OPERATORS = new TOK[8];
 			
 			PREFIX_OPERATORS[0] = TOK.TOKplusplus;
 			PREFIX_OPERATORS[1] = TOK.TOKminusminus;
 			PREFIX_OPERATORS[2] = TOK.TOKand;
 			PREFIX_OPERATORS[3] = TOK.TOKmul;
 			PREFIX_OPERATORS[4] = TOK.TOKadd;
 			PREFIX_OPERATORS[5] = TOK.TOKmin;
 			PREFIX_OPERATORS[6] = TOK.TOKtilde;
 			PREFIX_OPERATORS[7] = TOK.TOKnot;
 			Arrays.sort(PREFIX_OPERATORS);
 		}
 		return PREFIX_OPERATORS;
 	}
 	
 	private static TOK[] primitiveTypesTokenList()
 	{
 		if (null == PRIMITIVE_TYPES_LIST)
 		{
 			PRIMITIVE_TYPES_LIST = new TOK[25];
 			
 			PRIMITIVE_TYPES_LIST[0] = TOK.TOKauto;
 			PRIMITIVE_TYPES_LIST[1] = TOK.TOKint8;
 			PRIMITIVE_TYPES_LIST[2] = TOK.TOKuns8;
 			PRIMITIVE_TYPES_LIST[3] = TOK.TOKint16;
 			PRIMITIVE_TYPES_LIST[4] = TOK.TOKuns16;
 			PRIMITIVE_TYPES_LIST[5] = TOK.TOKint32;
 			PRIMITIVE_TYPES_LIST[6] = TOK.TOKuns32;
 			PRIMITIVE_TYPES_LIST[7] = TOK.TOKint64;
 			PRIMITIVE_TYPES_LIST[8] = TOK.TOKuns64;
 			PRIMITIVE_TYPES_LIST[9] = TOK.TOKfloat32;
 			PRIMITIVE_TYPES_LIST[10] = TOK.TOKfloat64;
 			PRIMITIVE_TYPES_LIST[11] = TOK.TOKfloat80;
 			PRIMITIVE_TYPES_LIST[12] = TOK.TOKimaginary32;
 			PRIMITIVE_TYPES_LIST[13] = TOK.TOKimaginary64;
 			PRIMITIVE_TYPES_LIST[14] = TOK.TOKimaginary80;
 			PRIMITIVE_TYPES_LIST[15] = TOK.TOKcomplex32;
 			PRIMITIVE_TYPES_LIST[16] = TOK.TOKcomplex64;
 			PRIMITIVE_TYPES_LIST[17] = TOK.TOKcomplex80;
 			PRIMITIVE_TYPES_LIST[18] = TOK.TOKchar;
 			PRIMITIVE_TYPES_LIST[19] = TOK.TOKwchar;
 			PRIMITIVE_TYPES_LIST[20] = TOK.TOKdchar;
 			PRIMITIVE_TYPES_LIST[21] = TOK.TOKbit;
 			PRIMITIVE_TYPES_LIST[22] = TOK.TOKbool;
 			PRIMITIVE_TYPES_LIST[23] = TOK.TOKcent;
 			PRIMITIVE_TYPES_LIST[24] = TOK.TOKucent;
 			Arrays.sort(PRIMITIVE_TYPES_LIST);
 		}
 		return PRIMITIVE_TYPES_LIST;
 	}
 	
 	private static TOK[] typeSpecializationTokenList()
 	{
 		if(null == TYPE_SPECIALIZATIONS)
 		{
 			TYPE_SPECIALIZATIONS = new TOK[10];
 			
 			TYPE_SPECIALIZATIONS[0] = TOK.TOKtypedef;
 			TYPE_SPECIALIZATIONS[1] = TOK.TOKstruct;
 			TYPE_SPECIALIZATIONS[2] = TOK.TOKunion;
 			TYPE_SPECIALIZATIONS[3] = TOK.TOKclass;
 			TYPE_SPECIALIZATIONS[4] = TOK.TOKenum;
 			TYPE_SPECIALIZATIONS[5] = TOK.TOKinterface;
 			TYPE_SPECIALIZATIONS[6] = TOK.TOKfunction;
 			TYPE_SPECIALIZATIONS[7] = TOK.TOKdelegate;
 			TYPE_SPECIALIZATIONS[8] = TOK.TOKreturn;
 			TYPE_SPECIALIZATIONS[9] = TOK.TOKsuper;
 			Arrays.sort(TYPE_SPECIALIZATIONS);
 		}
 		return TYPE_SPECIALIZATIONS;
 	}
 }
