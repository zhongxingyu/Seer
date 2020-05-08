 package sdf;
 
 import groovy.lang.Closure;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import aterm.*;
 import aterm.pure.SingletonFactory;
 
 import de.tud.stg.parlex.core.Grammar;
 import de.tud.stg.parlex.parser.earley.Chart;
 import de.tud.stg.parlex.parser.earley.EarleyParser;
 
 import de.tud.stg.popart.builder.core.annotations.DSLMethod.PreferencePriority;
 import de.tud.stg.popart.builder.core.annotations.DSLParameter;
 import de.tud.stg.popart.builder.core.annotations.DSLMethod;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.typeHandling.TypeHandler;
 
 import sdf.model.*;
 import sdf.util.GrammarDebugPrinter;
 
 /**
  * An implementation of the Syntax Definition Formalism (SDF) as a DSL.
  * 
  * @author Pablo Hoch
  * @see <a href="http://homepages.cwi.nl/~daybuild/daily-books/syntax/sdf/sdf.html">SDF Documentation</a>
  * @see <a href="http://homepages.cwi.nl/~daybuild/daily-books/technology/aterm-guide/aterm-guide.html">ATerm Documentation</a>
  * @see sdf.model
  *
  */
 import de.tud.stg.popart.builder.core.annotations.DSLClass;
 @DSLClass(	whitespaceEscape = " ", stringQuotation = "(\"([^\"\\\\]|\\\\.)*\")",
 		typeRules = {
 				SdfDSL.SortSymbolType.class,
 				SdfDSL.ModuleIdType.class,
 				SdfDSL.CharacterClassSymbolType.class,
 				SdfDSL.CaseInsensitiveLiteralSymbolType.class,
 				SdfDSL.SymbolLabelType.class,
 				// ATerm type handlers (used inside production attributes)
 				ATermTypeHandlers.IntConstantTypeHandler.class,
 				ATermTypeHandlers.RealConstantTypeHandler.class,
 				ATermTypeHandlers.FunctionNameTypeHandler.class
 		})
 public class SdfDSL implements de.tud.stg.popart.dslsupport.DSL {
 
 	/**
 	 * All unmodified modules as they appear in the input specification
 	 */
 	private HashMap<String, Module> modules;
 	
 	private ATermFactory atermFactory;
 	
 	
 	public SdfDSL() {
 		this.modules = new HashMap<String, Module>();
 		this.atermFactory = SingletonFactory.getInstance();
 	}
 
 	
 	public HashMap<String, Module> getModules() {
 		return modules;
 	}
 	
 	/**
 	 * Returns the definition of the module with the given name.
 	 * 
 	 * @param moduleName	name of the module
 	 * @return the Module if found, otherwise null
 	 */
 	public Module getModule(String moduleName) {
 		return modules.get(moduleName);
 	}
 	
 	/**
 	 * Transforms the module with the given name into a parlex grammar, processing SDF macros,
 	 * imports and renamings.
 	 * 
 	 * <p>The grammar is also cleaned, i.e. unused rules are removed.
 	 * 
 	 * @param topLevelModuleName	name of the top-level module
 	 * @return the generated Grammar for the given Module
 	 */
 	public Grammar getGrammar(String topLevelModuleName) {
 		return getGrammar(topLevelModuleName, true);
 	}
 	
 	/**
 	 * Transforms the module with the given name into a parlex grammar, processing SDF macros,
 	 * imports and renamings.
 	 * 
 	 * <p>The grammar can also be cleaned, i.e. unused rules are removed.
 	 * 
 	 * @param topLevelModuleName	name of the top-level module
 	 * @param cleanGrammar			if true, unused rules are removed from the generated grammar.
 	 * @return the generated Grammar for the given Module
 	 */
 	public Grammar getGrammar(String topLevelModuleName, boolean cleanGrammar) {
 		// find top level module
 		Module topLevelModule = modules.get(topLevelModuleName);
 		
 		// merge imports in top level module (and imported modules)
 		ModuleMerger merger = new ModuleMerger(this);
 		Module mainModule = merger.processModule(topLevelModule);
 		
 		// convert sdf model -> parlex grammar
 		SdfToParlexGrammarConverter converter = new SdfToParlexGrammarConverter(this);
 		Grammar g = converter.getGrammar(mainModule);
 
 		// remove unused rules if requested
 		if (cleanGrammar) {
 			return GrammarCleaner.clean(g);
 		} else {
 			return g;
 		}
 	}
 	
 	public ATermFactory getAtermFactory() {
 		return atermFactory;
 	}
 
 
 	public Object eval(HashMap map, Closure cl) {
 		cl.setDelegate(this);
 		cl.setResolveStrategy(Closure.DELEGATE_FIRST);
 		return cl.call();
 	}
 	
 	public Object sdf(Closure cl) {
 		return eval(null,cl);
 	}
 	
 	//// TOP LEVEL ELEMENTS ////
 	
 	
 	// TODO: Definition: either refactor this so that a Definition is always created (and used in other classes
 	// such as the converters instead of SdfDSL), or remove the Definition class. TBD.
 
 	// TODO: currently not supported (because additional methods would be required for each case):
 	// - modules with only imports but no exports/hiddens (cases for module with and without parameters)
 	// - modules with neither imports nor exports (makes no sense, but still legal)
 
 	// module p0 p1 p2 (imports, no parameters)
 	@DSLMethod(production = "module p0 p1 p2", topLevel = true)
 	public Module moduleWithoutParameters(
 			ModuleId name,
 			@DSLParameter(arrayDelimiter = " ") Imports[] imports,
 			@DSLParameter(arrayDelimiter = " ") ExportOrHiddenSection[] exportOrHiddenSections) {
 		Module mod = new Module(name);
 
 		mod.setImportSections(new ArrayList<Imports>(Arrays.asList(imports)));
 		mod.setExportOrHiddenSections(new ArrayList<ExportOrHiddenSection>(Arrays.asList(exportOrHiddenSections)));
 
 		modules.put(name.toString(), mod);
 
 		return mod;
 	}
 	
 	// module p0 p1 (no imports, no parameters)
 	@DSLMethod(production = "module p0 p1", topLevel = true)
 	public Module moduleWithoutParameters(
 			ModuleId name,
 			@DSLParameter(arrayDelimiter = " ") ExportOrHiddenSection[] exportOrHiddenSections) {
 		Module mod = new Module(name);
 
 		mod.setExportOrHiddenSections(new ArrayList<ExportOrHiddenSection>(Arrays.asList(exportOrHiddenSections)));
 
 		modules.put(name.toString(), mod);
 
 		return mod;
 	}
 	
 	// module p0[p1] p2 p3
 	@DSLMethod(production = "module p0  [  p1  ] p2 p3")
 	public Module moduleWithParameters(
 			ModuleId name,
 			@DSLParameter(arrayDelimiter = ",") Symbol[] params,
 			@DSLParameter(arrayDelimiter = " ") Imports[] imports,
 			@DSLParameter(arrayDelimiter = " ") ExportOrHiddenSection[] exportOrHiddenSections) {
 		Module mod = moduleWithoutParameters(name, imports, exportOrHiddenSections);
 
 		mod.setParameters(new ArrayList<Symbol>(Arrays.asList(params)));
 
 		return mod;
 	}
 	
 
 	/**
 	 * Parses an input string using the grammar generated for the given top-level module.
 	 * Returns a ParseResult which contains both the parse tree generated by the earley parser,
 	 * as well as the AST created using the cons attributes of the grammar.
 	 * 
 	 * @param topLevelModule
 	 * @param input
 	 * @return
 	 */
 	@DSLMethod(production = "parse p0 p1")
 	public ParseResult parseString(String topLevelModule, String input) {
 		Grammar grammar = getGrammar(topLevelModule);
 		
 		EarleyParser parser = new EarleyParser(grammar);
 		parser.detectUsedOracles();
 		Chart chart = (Chart) parser.parse(input);
 		
 		return new ParseResult(chart);
 		
 	}
 	
 	@DSLMethod(production = "printGeneratedGrammar p0")
 	public void printGeneratedGrammar(String topLevelModule) {
 		Grammar grammar = getGrammar(topLevelModule);
 		
 		System.out.println("Generated grammar for module " + topLevelModule + ":");
 		System.out.println(grammar);
 		System.out.println();
 	}
 	
 	@DSLMethod(production = "printGeneratedGrammarHTML p0 p1")
 	public void printGeneratedGrammarHTML(String topLevelModule, String fileName) {
 		Grammar grammar = getGrammar(topLevelModule);
 		
 		File file = new File(fileName);
 		try {
 			FileOutputStream fos = new FileOutputStream(fileName);
 			GrammarDebugPrinter gdp = new GrammarDebugPrinter(grammar, fos);
 			gdp.printGrammar(topLevelModule + " Grammar");
 			fos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println("Grammar for module " + topLevelModule + " saved to: " + file.toURI().toString());
 	}
 	
 	
 	
 	
 	//// SYMBOLS ////
 	
 	
 	
 	
 	// "p0"
 	@DSLMethod(production = "p0", topLevel = false)
 	public LiteralSymbol caseSensitiveLiteralSymbol(String text) {
 		return new LiteralSymbol(text, true);
 	}
 	
 	// 'p0'
 	@DSLMethod(production = "p0", topLevel = false)
 	public LiteralSymbol caseInsensitiveLiteralSymbol(CaseInsensitiveLiteralSymbol sym) {
 		return sym;
 	}
 	
 	// convenience method for manual tests
 	public LiteralSymbol caseInsensitiveLiteralSymbol(String str) {
 		return caseInsensitiveLiteralSymbol(new CaseInsensitiveLiteralSymbol("'" + str + "'"));
 	}
 	
 	// p0
 	// convenience method for manual tests
 	//@DSLMethod(production = "p0", topLevel = false)
 	public SortSymbol sortSymbol(String name) {
 		return new SortSymbol(name);
 	}
 	
 	// [p0]
 	// convenience method for manual tests
 //	@DSLMethod(production = "[p0]", topLevel = false)
 	public CharacterClassSymbol characterClassSymbol(String pattern) {
 		return new CharacterClassSymbol(pattern);
 	}
 	
 	// ~p0
 	@DSLMethod(production = "~  p0", topLevel = false)
 	public CharacterClassComplement characterClassComplement(CharacterClassSymbol sym) {
 		return new CharacterClassComplement(sym);
 	}
 	
 	// p0/p1
 	@DSLMethod(production = "p0  /  p1", topLevel = false)
 	public CharacterClassDifference characterClassDifference(CharacterClassSymbol left, CharacterClassSymbol right) {
 		return new CharacterClassDifference(left, right);
 	}
 	
 	// p0/\p1
 	@DSLMethod(production = "p0  /\\  p1", topLevel = false)
 	public CharacterClassIntersection characterClassIntersection(CharacterClassSymbol left, CharacterClassSymbol right) {
 		return new CharacterClassIntersection(left, right);
 	}
 	
 	// p0\/p1
 	@DSLMethod(production = "p0  \\/  p1", topLevel = false)
 	public CharacterClassUnion characterClassUnion(CharacterClassSymbol left, CharacterClassSymbol right) {
 		return new CharacterClassUnion(left, right);
 	}
 	
 	// p0?
 	@DSLMethod(production = "p0  ?", topLevel = false)
 	public OptionalSymbol optionalSymbol(Symbol symbol) {
 		return new OptionalSymbol(symbol);
 	}
 	
 	// p0*
 	@DSLMethod(production = "p0  *", topLevel = false)
 	public RepetitionSymbol repetitionSymbolAtLeastZero(Symbol symbol) {
 		return new RepetitionSymbol(symbol, false);
 	}
 	
 	// p0+
 	@DSLMethod(production = "p0  +", topLevel = false)
 	public RepetitionSymbol repetitionSymbolAtLeastOnce(Symbol symbol) {
 		return new RepetitionSymbol(symbol, true);
 	}
 	
 	// (p0)
 	@DSLMethod(production = "(  p0  )", topLevel = false)
 	public SequenceSymbol sequenceSymbol(@DSLParameter(arrayDelimiter = " ")Symbol[] symbols) {
 		return new SequenceSymbol(new ArrayList<Symbol>(Arrays.asList(symbols)));
 	}
 	
 	// {p0 p1}*
 	@DSLMethod(production = "{  p0 p1  }  *", topLevel = false)
 	public ListSymbol listSymbolAtLeastZero(Symbol element, Symbol seperator) {
 		return new ListSymbol(element, seperator, false);
 	}
 	
 	// {p0 p1}+
 	@DSLMethod(production = "{  p0 p1  }  +", topLevel = false)
 	public ListSymbol listSymbolAtLeastOnce(Symbol element, Symbol seperator) {
 		return new ListSymbol(element, seperator, true);
 	}
 	
 	// p0 | p1
 	@DSLMethod(production = "p0  |  p1", topLevel = false)
 	public AlternativeSymbol alternativeSymbol(Symbol left, Symbol right) {
 		return new AlternativeSymbol(left, right);
 	}
 	
 	// <p0>
 	@DSLMethod(production = "<  p0  >", topLevel = false)
 	public TupleSymbol tupleSymbol(@DSLParameter(arrayDelimiter = ",")Symbol[] symbol) {
 		return new TupleSymbol(new ArrayList<Symbol>(Arrays.asList(symbol)));
 	}
 	
 	// (p0 => p1)
 	@DSLMethod(production = "(  p0  =>  p1  )", topLevel = false)
 	public FunctionSymbol functionSymbol(@DSLParameter(arrayDelimiter = " ")Symbol[] left, Symbol right) {
 		return new FunctionSymbol(new ArrayList<Symbol>(Arrays.asList(left)), right);
 	}
 	
 	// p0:p1
 	@DSLMethod(production = "p0  :  p1", topLevel = false)
 	public Symbol labeledSymbol(String label, Symbol sym) {
 		sym.setLabel(label);
 		return sym;
 	}
 	
 	// p0:p1
 	@DSLMethod(production = "p0  :  p1", topLevel = false)
 	public Symbol labeledSymbol(SymbolLabel label, Symbol sym) {
 		sym.setLabel(label.getLabel());
 		return sym;
 	}
 	
 	// Methods to convert symbol subclasses to symbol
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(SortSymbol s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(LiteralSymbol s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(AlternativeSymbol s) { return s; }	
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(ListSymbol s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(OptionalSymbol s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(RepetitionSymbol s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(SequenceSymbol s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(CharacterClass s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public CharacterClass characterClass(CharacterClassSymbol s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public CharacterClass characterClass(CharacterClassComplement s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public CharacterClass characterClass(CharacterClassDifference s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public CharacterClass characterClass(CharacterClassIntersection s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public CharacterClass characterClass(CharacterClassUnion s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(TupleSymbol s) { return s; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Symbol symbol(FunctionSymbol s) { return s; }
 	
 	
 	//// MODULE LEVEL /////
 	
 	
 	
 	@DSLMethod(production = "exports p0", topLevel = false)
 	public Exports exports(@DSLParameter(arrayDelimiter = " ")GrammarElement[] grammarElements) {
 		return new Exports(new ArrayList<GrammarElement>(Arrays.asList(grammarElements)));
 	}
 	
 	@DSLMethod(production = "hiddens p0", topLevel = false)
 	public Hiddens hiddens(@DSLParameter(arrayDelimiter = " ")GrammarElement[] grammarElements) {
 		return new Hiddens(new ArrayList<GrammarElement>(Arrays.asList(grammarElements)));
 	}
 	
 	// Methods to convert Exports/Hiddens to ExportOrHiddenSection
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public ExportOrHiddenSection exportOrHiddenSection(Exports e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public ExportOrHiddenSection exportOrHiddenSection(Hiddens e) { return e; }
 	
 	
 	
 	//// GRAMMAR ELEMENTS ////
 	
 	
 	
 	
 	// imports p0
 	@DSLMethod(production = "imports p0", topLevel = false)
 	public Imports importsStatement(@DSLParameter(arrayDelimiter = " ")Import[] importList) {
 		return new Imports(new ArrayList<Import>(Arrays.asList(importList)));
 	}
 	
 	// p0
 	@DSLMethod(production = "p0", topLevel = false)
 	public Import importModuleWithoutParameters(ModuleId moduleName) {
 		return new Import(moduleName.toString());
 	}
 	
 	// p0[p1]
 	@DSLMethod(production = "p0  [  p1  ]", topLevel = false)
 	public Import importModuleWithParameters(ModuleId moduleName, @DSLParameter(arrayDelimiter = ",")Symbol[] params) {
 		return new Import(moduleName.toString(), new ArrayList<Symbol>(Arrays.asList(params)));
 	}
 	
 	// p0[p1]
 	@DSLMethod(production = "p0  [  p1  ]", topLevel = false)
 	public Import importModuleWithRenamings(ModuleId moduleName, @DSLParameter(arrayDelimiter = ",")Renaming[] renamings) {
 		return new Import(moduleName.toString(),
 				new ArrayList<Symbol>(),
 				new ArrayList<Renaming>(Arrays.asList(renamings)));
 	}
 	
 	// p0[p1][p2]
 	@DSLMethod(production = "p0  [  p1  ]  [  p2  ]", topLevel = false)
 	public Import importModuleWithParametersAndRenamings(ModuleId moduleName,
 			@DSLParameter(arrayDelimiter = ",")Symbol[] params,
 			@DSLParameter(arrayDelimiter = ",")Renaming[] renamings) {
 		return new Import(moduleName.toString(),
 				new ArrayList<Symbol>(Arrays.asList(params)),
 				new ArrayList<Renaming>(Arrays.asList(renamings)));
 	}
 	
 	// p0 => p1
 	@DSLMethod(production = "p0  =>  p1", topLevel = false)
 	public Renaming renaming(Symbol oldSymbol, Symbol newSymbol) {
 		return new Renaming(oldSymbol, newSymbol);
 	}
 	
 	// sorts p0
 	@DSLMethod(production = "sorts p0", topLevel = false)
 	public Sorts sortsDeclaration(@DSLParameter(arrayDelimiter = " ")SortSymbol[] sortSymbols) {
 		return new Sorts(new ArrayList<SortSymbol>(Arrays.asList(sortSymbols)));
 	}
 	
 	// lexical syntax p0
 	@DSLMethod(production = "lexical syntax p0", topLevel = false)
 	public LexicalSyntax lexicalSyntax(@DSLParameter(arrayDelimiter = " ")Production[] productions) {
 		return new LexicalSyntax(new ArrayList<Production>(Arrays.asList(productions)));
 	}
 	
 	// context-free syntax p0
 	@DSLMethod(production = "context-free syntax p0", topLevel = false)
 	public ContextFreeSyntax contextFreeSyntax(@DSLParameter(arrayDelimiter = " ")Production[] productions) {
 		return new ContextFreeSyntax(new ArrayList<Production>(Arrays.asList(productions)));
 	}
 	
 	// lexical start-symbols p0
 	@DSLMethod(production = "lexical start-symbols p0", topLevel = false)
 	public LexicalStartSymbols lexicalStartSymbols(@DSLParameter(arrayDelimiter = " ")Symbol[] symbols) {
 		return new LexicalStartSymbols(new ArrayList<Symbol>(Arrays.asList(symbols)));
 	}
 	
 	// context-free start-symbols p0
 	@DSLMethod(production = "context-free start-symbols p0", topLevel = false)
 	public ContextFreeStartSymbols contextFreeStartSymbols(@DSLParameter(arrayDelimiter = " ")Symbol[] symbols) {
 		return new ContextFreeStartSymbols(new ArrayList<Symbol>(Arrays.asList(symbols)));
 	}
 	
 	// aliases p0
 	@DSLMethod(production = "aliases p0", topLevel = false)
 	public Aliases aliases(@DSLParameter(arrayDelimiter = " ")Alias[] aliases) {
 		return new Aliases(new ArrayList<Alias>(Arrays.asList(aliases)));
 	}
 	
 	// p0 -> p1		(alias)
 	@DSLMethod(production = "p0  ->  p1", topLevel = false)
 	public Alias alias(Symbol original, Symbol aliasName) {
 		return new Alias(original, aliasName);
 	}
 	
 	// p0 -> p1		(production)
 	@DSLMethod(production = "p0  ->  p1", topLevel = false/*,
 			uniqueIdentifier = "sdf.production"*/)
 	public Production production(@DSLParameter(arrayDelimiter = " ")Symbol[] lhs, Symbol rhs) {
 		return new Production(new ArrayList<Symbol>(Arrays.asList(lhs)), rhs);
 	}
 	
 	//  -> p0		(production with empty LHS)
 	@DSLMethod(production = "  -> p0", topLevel = false/*,
 			uniqueIdentifier = "sdf.production.empty"*/)
 	public Production production(Symbol rhs) {
 		return new Production(new ArrayList<Symbol>(), rhs);
 	}
 	
 	// p0 -> p1		(production with attributes)
 	@DSLMethod(production = "p0  ->  p1  {  p2  }", topLevel = false/*,
 			uniqueIdentifier = "sdf.production+attr",
 			priorityHigherThan = "sdf.production"*/,
 			preferencePriority = PreferencePriority.Prefer)
 	public Production productionWithAttributes(
 			@DSLParameter(arrayDelimiter = " ")Symbol[] lhs,
 			Symbol rhs,
 			@DSLParameter(arrayDelimiter=",")ATerm[] attributes) {
 		return new Production(
 				new ArrayList<Symbol>(Arrays.asList(lhs)),
 				rhs,
 				new ArrayList<ATerm>(Arrays.asList(attributes)));
 	}
 
 	//  -> p0		(production with empty LHS but attributes)
 	@DSLMethod(production = "  ->  p0  {  p1  }", topLevel = false/*,
 			uniqueIdentifier = "sdf.production.empty+attr",
 			priorityHigherThan = "sdf.production.empty"*/,
 			preferencePriority = PreferencePriority.Prefer)
 	public Production productionWithAttributes(
 			Symbol rhs,
 			@DSLParameter(arrayDelimiter=",")ATerm[] attributes) {
 		return new Production(
 				new ArrayList<Symbol>(),
 				rhs,
 				new ArrayList<ATerm>(Arrays.asList(attributes)));
 	}
 	
 	// lexical priorities p0
 	@DSLMethod(production = "lexical priorities p0", topLevel = false)
 	public LexicalPriorities lexicalPriorities(
 			@DSLParameter(arrayDelimiter=",")Priority[] priorities) {
 		return new LexicalPriorities(new ArrayList<Priority>(Arrays.asList(priorities)));
 	}
 	
 	// context-free priorities p0
 	@DSLMethod(production = "context-free priorities p0", topLevel = false)
 	public ContextFreePriorities contextFreePriorities(
 			@DSLParameter(arrayDelimiter=",")Priority[] priorities) {
 		return new ContextFreePriorities(new ArrayList<Priority>(Arrays.asList(priorities)));
 	}
 	
 	// TODO: currently in the DSL only transitive priorities are possible.
 	// in order to also parse non-transitive priorities (>. instead of >),
 	// some intermediate classes are needed.
 	// (the model classes are modelled after the SDF syntax definition written in SDF.
 	//  however, in the SDF *syntax definition*, groups ending with a "." are considered
 	//  non-transitive (i.e. .>). in the *documentation*, >. is used instead. not sure
 	//  which one is correct...)
 	
 	// p0
 	@DSLMethod(production = "p0", topLevel = false)
 	public Priority priority(
 			@DSLParameter(arrayDelimiter=">")PriorityGroup[] groups) {
 		return new Priority(new ArrayList<PriorityGroup>(Arrays.asList(groups)));
 	}
 	
 	// p0
 	@DSLMethod(production = "{  p0  }", topLevel = false)
 	public PriorityGroup priorityGroup(
 			@DSLParameter(arrayDelimiter=" ")Production[] productions) {
 		return new PriorityGroup(new ArrayList<Production>(Arrays.asList(productions)));
 	}
 	
 	// p0
 	@DSLMethod(production = "p0", topLevel = false)
 	public PriorityGroup priorityGroup(Production production) {
 		return new PriorityGroup(new ArrayList<Production>(Arrays.asList(production)));
 	}
 	
 	// p0: p1
 	@DSLMethod(production = "{  p0  :  p1  }", topLevel = false)
 	public PriorityGroup priorityGroupWithAssociativityAnnotation(
 			ATerm associativityAnnotation,
 			@DSLParameter(arrayDelimiter=" ")Production[] productions) {
 		return new PriorityGroup(new ArrayList<Production>(Arrays.asList(productions)),
 				associativityAnnotation, true);
 	}
 	
 	// Methods to convert grammar elements to GrammarElement
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public GrammarElement grammarElement(Imports e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public GrammarElement grammarElement(Sorts e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public GrammarElement grammarElement(StartSymbols e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public GrammarElement grammarElement(Syntax e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public GrammarElement grammarElement(LexicalPriorities e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public GrammarElement grammarElement(ContextFreePriorities e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public GrammarElement grammarElement(Aliases e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public StartSymbols startSymbols(ContextFreeStartSymbols e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public StartSymbols startSymbols(LexicalStartSymbols e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Syntax syntax(ContextFreeSyntax e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public Syntax syntax(LexicalSyntax e) { return e; }
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public GrammarElement syntax(Syntax e) { return e; }
 	
 	
 	
 	
 	
 	//// PRODUCTION ATTRIBUTES /////
 	
 	
 	@DSLMethod(production = "p0  (  p1  )", topLevel = false)
 	public ATerm atermFunctionApplication(AFun fun, @DSLParameter(arrayDelimiter=",")ATerm[] args) {
 		// fix arity, because it is initially set to 0 in the typehandler
 		AFun fixedFun = atermFactory.makeAFun(fun.getName(), args.length, fun.isQuoted());
 		return atermFactory.makeAppl(fixedFun, args);
 	}
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public ATerm atermFunctionApplication(AFun fun) {
 		return atermFactory.makeAppl(fun);
 	}
 	
 	// additional method required for tigerseye
 	@DSLMethod(production = "p0  (  )", topLevel = false)
 	public ATerm atermFunctionApplicationWithoutArguments(AFun fun) {
 		return atermFactory.makeAppl(fun);
 	}
 	
 	@DSLMethod(production = "[  p0  ]", topLevel = false)
 	public ATerm atermList(@DSLParameter(arrayDelimiter=",")ATerm[] items) {
 		if (items.length == 0) {
 			// this cannot happen when called from tigerseye, but can happen when called manually
 			return atermFactory.makeList();
 		}
 		
 		// build list
 		ATermList list = atermFactory.makeList(items[items.length - 1]);
 		
 		for (int i = items.length - 2; i > 0; i--) {
 			list = atermFactory.makeList(items[i], list);
 		}
 		
 		return list;
 	}
 	
 	@DSLMethod(production = "[  ]", topLevel = false)
 	public ATerm atermList() {
 		return atermFactory.makeList();
 	}
 	
 	// Methods to convert the type handler helper classes to ATerms
 	// (all ATerms are returned as "ATerm", because we don't need to differentiate between
 	// them here. the only exception is AFun, which needs to be turned into a function application).
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public ATerm aterm(ATermTypeHandlers.IntConstantTypeHandler.IntConstant helper) {
 		return helper.getATerm();
 	}
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	public ATerm aterm(ATermTypeHandlers.RealConstantTypeHandler.RealConstant helper) {
 		return helper.getATerm();
 	}
 	
 	@DSLMethod(production = "p0", topLevel = false)
 	// notice that this function returns AFun. this must then be passed to atermFunctionApplication
 	// to get an ATerm.
 	public AFun aterm(ATermTypeHandlers.FunctionNameTypeHandler.FunctionNameConstant helper) {
 		return helper.getATerm();
 	}
 	
 	// helper function
 	public ATerm customATerm(String text) {
 		return atermFactory.parse(text);
 	}
 	
 	// helper function, returns an aterm of the form cons("Constructor")
 	public ATerm consATerm(String constructor) {
 		return customATerm("cons(\"" + constructor + "\")");
 	}
 	
 	
 	
 	//// TYPE HANDLERS ////
 	
 	/**
 	 * A sort corresponds to a non-terminal, e.g., Bool. Sort names always start with a capital letter and may be followed by
 	 * letters and/or digits. Hyphens (-) may be embedded in a sort name. 
 	 * <p>
 	 * Parameterized sort names (TODO): {@code <Sort>[[<Symbol1>, <Symbol2>, ... ]]}
 	 * 
 	 */
 	public static class SortSymbolType extends TypeHandler {
 
 		@Override
 		public Class<?> getMainType() {
 			return SortSymbol.class;
 		}
 
 		@Override
 		public String getRegularExpression() {
			return "([A-Z][-A-Za-z0-9]*)";
 		}
 		
 	}
 	
 	/**
 	 * A module name consists of letters, numbers, hyphens and underscores, potentionally
 	 * seperated by slashes (like a path name).
 	 * 
 	 * @author Pablo Hoch
 	 * 
 	 */
 	public static class ModuleIdType extends TypeHandler {
 
 		@Override
 		public Class<?> getMainType() {
 			return ModuleId.class;
 		}
 
 		@Override
 		public String getRegularExpression() {
			return "(/?([-_A-Za-z0-9]+)(/[-_A-Za-z0-9]+)*)";
 		}
 		
 	}
 	
 	public static class CharacterClassSymbolType extends TypeHandler {
 
 		@Override
 		public Class<?> getMainType() {
 			return CharacterClassSymbol.class;
 		}
 
 		@Override
 		public String getRegularExpression() {
 //			return "\\[([^\\]]+)\\]";
 			// this version allows all escapes inside the character class
 			return "\\[([^\\]\\\\]|\\\\.)*\\]";
 		}
 		
 	}
 	
 	public static class CaseInsensitiveLiteralSymbolType extends TypeHandler {
 
 		@Override
 		public Class<?> getMainType() {
 			return CaseInsensitiveLiteralSymbol.class;
 		}
 
 		@Override
 		public String getRegularExpression() {
 			// matches single quoted strings.
 			// allowed escapes inside the string: \' and \\
 			return "'([^'\\\\]|\\\\['\\\\])*'";
 		}
 		
 	}
 	
 	public static class SymbolLabelType extends TypeHandler {
 
 		@Override
 		public Class<?> getMainType() {
 			return SymbolLabel.class;
 		}
 
 		@Override
 		public String getRegularExpression() {
 			return "[a-zA-Z_][a-zA-Z0-9_]*";
 		}
 		
 	}
 	
 	public static class SymbolLabel {
 		private String label;
 
 		public SymbolLabel(String label) {
 			super();
 			this.label = label;
 		}
 
 		public String getLabel() {
 			return label;
 		}
 		
 	}
 }
