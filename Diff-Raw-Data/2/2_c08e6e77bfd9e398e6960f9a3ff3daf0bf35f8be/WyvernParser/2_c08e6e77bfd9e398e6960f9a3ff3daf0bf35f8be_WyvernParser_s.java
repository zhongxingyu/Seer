 /**
  * 
  */
 package compiler.wyvern;
 
 import static compiler.wyvern.WyvernLexer.*;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import compiler.Context;
 import compiler.SymbolType;
 import compiler.parse.Associativity;
 import compiler.parse.CheckedProductionSet;
 import compiler.parse.Grammar;
 import compiler.parse.LALRGenerator;
 import compiler.parse.Parser;
 import compiler.parse.ParserGenerator;
 import compiler.parse.Precedence;
 import compiler.parse.Precedence.ProductionPrecedence;
 import compiler.parse.Production;
 
 /**
  * @author Michael
  * 
  */
 public class WyvernParser {
 	public static Grammar GRAMMAR;
 	public static Parser PARSER;
 
 	// non-terminal symbols
 	public static SymbolType EXP = CONTEXT.getNonTerminalSymbolType("exp"),
 			STMT = CONTEXT.getNonTerminalSymbolType("stmt"), PROGRAM = CONTEXT
 					.getNonTerminalSymbolType("program"),
 			PACKAGE_STMT = CONTEXT.getNonTerminalSymbolType("package-stmt"),
 			USING_STMT = CONTEXT.getNonTerminalSymbolType("using-stmt"),
 			TYPE_DECL = CONTEXT.getNonTerminalSymbolType("type-decl"),
 			TYPE_NAME = CONTEXT.getNonTerminalSymbolType("type-name"),
 			ATTRIBUTE = CONTEXT.getNonTerminalSymbolType("attribute"),
 			GENERIC_PARAMETERS = CONTEXT
 					.getNonTerminalSymbolType("generic-list"),
 			PROPERTY_DECL = CONTEXT.getNonTerminalSymbolType("property-decl"),
 			METHOD_DECL = CONTEXT.getNonTerminalSymbolType("method-decl"),
 			MEMBER_DECL = CONTEXT.getNonTerminalSymbolType("member-decl"),
 			INHERITS_FROM_CLAUSE = CONTEXT
 					.getNonTerminalSymbolType("inherits-from-clause"),
 			WHERE_CLAUSE = CONTEXT.getNonTerminalSymbolType("where-clause"),
 			QUALIFIER = CONTEXT.getNonTerminalSymbolType("qualifier"),
 			ARGS_LIST = CONTEXT.getNonTerminalSymbolType("args-list"),
 			GETTER = CONTEXT.getNonTerminalSymbolType("getter"),
 			SETTER = CONTEXT.getNonTerminalSymbolType("setter"),
 			PROPERTY_DEFAULT = CONTEXT.getNonTerminalSymbolType("default");
 
 	static {
 		GRAMMAR = buildGrammar();
 		ParserGenerator gen = new LALRGenerator();
 		ParserGenerator.Result result = gen.generate(GRAMMAR);
 		PARSER = result.parser();
 	}
 
 	private static Grammar buildGrammar() {
 		CheckedProductionSet productions = new CheckedProductionSet();
 		LinkedHashMap<Set<SymbolType>, Associativity> symbolTypePrecedences = buildSymbolTypePrecedences();
 		Map<Production, SymbolType> precedenceAssignments = new LinkedHashMap<Production, SymbolType>();
 
 		// define a type name as one of the non-generic aliases first so that we
 		// can use
 		// TYPE_NAME recursively in other productions
 		productions.add(new Production(TYPE_NAME, CONTEXT.oneOf(INT_ALIAS,
 				BOOLEAN_ALIAS, STRING_ALIAS, OBJECT_ALIAS), CONTEXT
 				.optional(QUESTION_MARK)));
 
 		// a qualifier is a type name list or an optional package name
 		// qualifying a type name list (e. g. for a nested type)
 		productions.add(new Production(QUALIFIER, CONTEXT.listOf(IDENTIFIER,
 				ACCESS)));
 		productions.add(new Production(QUALIFIER, CONTEXT.optional(CONTEXT
 				.tuple(CONTEXT.listOf(IDENTIFIER, ACCESS), ACCESS)), CONTEXT
 				.listOf(TYPE_NAME, ACCESS)));
 
 		// generic parameters are just <type names>
 		productions.add(new Production(GENERIC_PARAMETERS, LCARET, CONTEXT
 				.listOf(TYPE_NAME, COMMA), RCARET));
 
 		// the "full" definition for a type name is [qualifier.](custom type
 		// name | seq)[generic params][?]
 		productions.add(new Production(TYPE_NAME, CONTEXT.optional(CONTEXT
 				.tuple(QUALIFIER, ACCESS)), CONTEXT.oneOf(TYPE_IDENTIFIER,
 				SEQUENCE_ALIAS), CONTEXT.optional(GENERIC_PARAMETERS), CONTEXT
 				.optional(QUESTION_MARK)));
 
 		// package & using stmts
 		productions.add(new Production(PACKAGE_STMT, PACKAGE, CONTEXT.listOf(
 				IDENTIFIER, ACCESS), STMT_END));
 		productions.add(new Production(USING_STMT, USING, QUALIFIER, STMT_END));
 
 		// attributes
 		// keyword attributes
 		productions.add(new Production(ATTRIBUTE, CONTEXT
 				.oneOf(PRIVATE, FAMILY, DEFAULT)));
 		// productions.add(new Production(ATTRIBUTE, TYPE_NAME, CONTEXT
 		// .optional(ARGS_LIST)));
 
 		productions.add(new Production(INHERITS_FROM_CLAUSE, IS, CONTEXT
 				.listOf(TYPE_NAME, COMMA)));
 		productions.add(new Production(WHERE_CLAUSE, WHERE, TYPE_NAME,
 				INHERITS_FROM_CLAUSE));
 
 		/* MEMBER DECLARATIONS */
 		// properties: [attributes] [Type] Name ;|{ [vars ...] [getter] [setter] } [= exp]
		productions.add(new Production)
 		
 		// type declaration: [attributes] type Name [is BaseTypes] [where ...] {
 		// members }
 		productions.add(new Production(TYPE_DECL, CONTEXT.listOf(ATTRIBUTE),
 				TYPE, TYPE_NAME, CONTEXT.optional(INHERITS_FROM_CLAUSE),
 				CONTEXT.listOf(WHERE_CLAUSE), LBRACE, CONTEXT.listOf(
 						MEMBER_DECL, Context.ListOption.AllowEmpty), RBRACE));
 
 		// note that a member declaration can ALSO be a type declaration!
 		productions.add(new Production(MEMBER_DECL, TYPE_DECL));
 		
 		/* PROGRAM */
 		// a program specifies the package and imports, then optionally has the
 		// file type, then has the main script
 		productions.add(new Production(PROGRAM, PACKAGE_STMT, CONTEXT
 				.listOf(USING_STMT), CONTEXT.optional(TYPE_DECL), CONTEXT
 				.listOf(STMT)));
 
 		return new Grammar(CONTEXT, "Wyvern", PROGRAM, productions,
 				Precedence.createFunction(symbolTypePrecedences,
 						ProductionPrecedence.LeftmostTerminal,
 						precedenceAssignments));
 	}
 
 	private static LinkedHashMap<Set<SymbolType>, Associativity> buildSymbolTypePrecedences() {
 		LinkedHashMap<Set<SymbolType>, Associativity> map = new LinkedHashMap<Set<SymbolType>, Associativity>();
 
 		return map;
 	}
 }
