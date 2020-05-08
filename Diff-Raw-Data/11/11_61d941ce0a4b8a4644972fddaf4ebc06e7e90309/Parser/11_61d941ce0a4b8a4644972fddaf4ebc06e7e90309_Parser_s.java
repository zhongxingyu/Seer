 package ee.olegus.fortran;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.antlr.runtime.RecognitionException;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 import fortran.ofp.parser.java.FortranLexer;
 import fortran.ofp.parser.java.FortranLexicalPrepass;
 import fortran.ofp.parser.java.FortranParser;
 import fortran.ofp.parser.java.FortranStream;
 import fortran.ofp.parser.java.FortranTokenStream;
 import ee.olegus.fortran.ast.Code;
 import ee.olegus.fortran.ast.ProgramUnit;
 import ee.olegus.fortran.ast.Subprogram;
 import ee.olegus.fortran.ast.Type;
 import ee.olegus.fortran.ast.TypeShape;
 import ee.olegus.fortran.ast.cpr.PotentialCheckpoint;
 import ee.olegus.fortran.model.Module;
 import ee.olegus.fortran.parser.ParserAction;
 import ee.olegus.fortran.visitors.DataReferenceResolver;
 import ee.olegus.fortran.visitors.ModuleDependencyCollector;
 
 public class Parser implements XMLGenerator {
 
 	private static final Logger LOG = LogManager.getLogger(Parser.class);
 
 	public static final int UNKNOWN_SOURCE_FORM = -1;
 	public static final int FREE_FORM = 1;
 	public static final int FIXED_FORM = 2;
 	
 	private Map<String,Type> builtinTypes = new HashMap<String, Type>();
 	private List<PotentialCheckpoint> potentialCheckpoints = new ArrayList<PotentialCheckpoint>();
 
 	private Map<File, FortranFile> fortranFiles =  new HashMap<File, FortranFile>();
 	
 	public Parser() {
 		builtinTypes.put("integer", new Type("integer"));
 		builtinTypes.put("real", new Type("real"));
 		builtinTypes.put("logical", new Type("logical"));
 		builtinTypes.put("complex", new Type("complex"));
 	}
 
 	public List<PotentialCheckpoint> getPotentialCheckpoints() {
 		return potentialCheckpoints;
 	}
 	
     private static boolean parseMainProgram(FortranTokenStream tokens,
 			FortranParser parser, int start) throws RecognitionException {
 		// try parsing the main program
 		parser.main_program();
 
 		return parser.hasErrorOccurred;
 	}// end parseMainProgram()
 
 	private static int determineSourceForm(String fileName) {
 		if (fileName.endsWith(new String(".f")) == true
 				|| fileName.endsWith(new String(".F")) == true) {
 			return FIXED_FORM;
 		} else {
 			return FREE_FORM;
 		}
 	}// end determineSourceForm()	
 	
 	   private static boolean parseModule(FortranTokenStream tokens,
 			FortranParser parser, int start) throws RecognitionException {
 		parser.module();
 		return parser.hasErrorOccurred;
 	}// end parseModule()
 
 	private static boolean parseBlockData(FortranTokenStream tokens,
 			FortranParser parser, int start) throws RecognitionException {
 		parser.block_data();
 
 		return parser.hasErrorOccurred;
 	}// end parseBlockData()
 
 	private static boolean parseSubroutine(FortranTokenStream tokens,
 			FortranParser parser, int start) throws RecognitionException {
 		parser.subroutine_subprogram();
 
 		return parser.hasErrorOccurred;
 	}// end parserSubroutine()
 
 	private static boolean parseFunction(FortranTokenStream tokens,
 			FortranParser parser, int start) throws RecognitionException {
 		parser.ext_function_subprogram();
 		return parser.hasErrorOccurred;
 	}// end parseFunction()
 	
  private static boolean parseProgramUnit(FortranLexer lexer,
 			FortranTokenStream tokens, FortranParser parser) {
 		int firstToken;
 		int lookAhead = 1;
 		int start;
 		boolean error = false;
 
 		// first token on the *line*. will check to see if it's
 		// equal to module, block, etc. to determine what rule of
 		// the grammar to start with.
 		try {
 			lookAhead = 1;
 			do {
 				firstToken = tokens.LA(lookAhead);
 				lookAhead++;
 			} while (firstToken == FortranLexer.LINE_COMMENT
 					|| firstToken == FortranLexer.T_EOS);
 
 			// mark the location of the first token we're looking at
 			start = tokens.mark();
 
 			// attempt to match the program unit
 			// each of the parse routines called will first try and match
 			// the unit they represent (function, block, etc.). if that
 			// fails, they may or may not try and match it as a main
 			// program; it depends on how it fails.
 			//
 			// due to Sale's algorithm, we know that if the token matches
 			// then the parser should be able to successfully match.
 			if (firstToken != FortranLexer.EOF) {
 				if (firstToken == FortranLexer.T_MODULE
 						&& tokens.LA(lookAhead) != FortranLexer.T_PROCEDURE) {
 					// try matching a module
 					error = parseModule(tokens, parser, start);
 				} else if (firstToken == FortranLexer.T_BLOCK
 						|| firstToken == FortranLexer.T_BLOCKDATA) {
 					// try matching block data
 					error = parseBlockData(tokens, parser, start);
 				} else if (tokens.lookForToken(FortranLexer.T_SUBROUTINE) == true) {
 					// try matching a subroutine
 					error = parseSubroutine(tokens, parser, start);
 				} else if (tokens.lookForToken(FortranLexer.T_FUNCTION) == true) {
 					// try matching a function
 					error = parseFunction(tokens, parser, start);
 				} else {
 					// what's left should be a main program
 					error = parseMainProgram(tokens, parser, start);
 				}// end else(unhandled token)
 			}// end if(file had nothing but comments empty)
 		} catch (RecognitionException e) {
 			e.printStackTrace();
 			error = true;
 		}// end try/catch(parsing program unit)
 
 		return error;
 	}// end parseProgramUnit()
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws RecognitionException 
 	 */
 	public void feed(File parent, String filename, String[] args) throws IOException, RecognitionException {
 		LOG.info("Parsing "+filename);
 		
 		File file = new File(parent, filename);
 		
 		int sourceForm = determineSourceForm(filename);
 		LOG.info("Source form: "+sourceForm);
 		FortranLexer lexer = new FortranLexer(new FortranStream(file.getPath(),
 				sourceForm));
 		FortranTokenStream tokens = new FortranTokenStream(lexer);
 		FortranParser parser = new FortranParser(args, tokens,
 				ParserAction.class.getName(), filename);
 		FortranLexicalPrepass prepass = new FortranLexicalPrepass(lexer,
 				tokens, parser);
 		
 		prepass.setSourceForm(sourceForm);
 		prepass.performPrepass();
 		// overwrite the old token stream with the (possibly) modified one
 		tokens.finalizeTokenStream();
 
 		// parse each program unit in a given file
 		while (tokens.LA(1) != FortranLexer.EOF) {
 			Boolean error = false;
 			// attempt to parse the current program unit
 			try {
 				error = parseProgramUnit(lexer, tokens, parser);
 			} catch(RuntimeException e) {
 				LOG.error("At "+parser.getTokenStream().LT(1), e);
 				error = true;
 				throw e;
 			}
 
 			// see if we successfully parse the program unit or not
 			if (error != false) {
 				System.out.println("Parser failed");
 			}
 		}// end while(not end of file)
 			
 		ParserAction action = (ParserAction) parser.getAction();
 		
 		/*
 		 * create new FortranFile
 		 */
 		FortranFile fortranFile = new FortranFile(filename);
 		fortranFile.setProgramUnits(action.getProgramUnits());
 		fortranFile.setSubprograms(action.getSubprograms());
 		fortranFile.setTokenStream(tokens);
 		fortranFiles.put(file,fortranFile);
 
 		System.out.println("size "+action.getParseStack().size());
 
 		for(int i=0; i<action.getParseStack().size(); i++) {
 			if(!(action.getParseStack().get(i) instanceof Subprogram))
 				continue;
 			
 			Subprogram subprogram = (Subprogram) action.getParseStack().get(i);
 			action.getSubprograms().put(subprogram.getName().toLowerCase(), subprogram);
 		}
 		
 		/*
 		for (Iterator unitIterator = action.getProgramUnits().values().iterator(); unitIterator.hasNext();) {
 			ProgramUnit unit = (ProgramUnit) unitIterator.next();
 			System.out.println("--- "+unit.getName());
 			System.out.println(unit.toLongString());
 		}*/
 		
 		/*
 		FortranStream stream = new FortranStream(new File(parent, filename).getPath());
 		FortranLexer lexer = new FortranLexer(stream);
 		CPRFortranTokenStream tokenStream = new CPRFortranTokenStream(lexer);
 		FortranParser parser = new FortranParser(tokenStream);
 		FortranParser.program_return program = parser.program();
 		
 		fileTokenStreams.put(filename, tokenStream);
 		
 		CommonTree tree = (CommonTree)program.getTree();
 		if(LOG.isDebugEnabled()) {
 			LOG.debug("Parse tree of "+ filename+":\n"+tree.toStringTree());
 		}
 		Map<String,ProgramUnit> newUnits = parse(tree);
 		units.putAll(newUnits);
 
 		// initialize found potential checkpoints
 		List<CommonToken> cprTokens = new ArrayList<CommonToken>(tokenStream.getPotentialCheckpoints());
 		for(CommonToken cprToken: cprTokens) {
 			PotentialCheckpoint cp = new PotentialCheckpoint(cprToken);
 			// find code location for this point
 			for(ProgramUnit unit: newUnits.values()) {
 				Code code = findCodeLocation(unit, cp.getLocation().start);
 				if(code != null) {
 					cp.getLocation().code = code;
 					break;
 				}
 			}
 			
 			potentialCheckpoints.add(cp);
 		}
 		//Subprogram subprogram = units.get("qsort_m").subprograms.get("qsort_i");
 		//tokenStream.insertBefore(((CommonTree)subprogram.tree).startIndex, "INSERT HERE");
 		 */
 		
 	}
 	
 	private Code findCodeLocation(Code code, int start) {
 		
 		// is it within given code
 		if(start>=code.getLocation().start &&
 				start<code.getLocation().stop) {
 		
 			// is it within any subprogram
 			for(Subprogram subprogram: code.getSubprograms().values()) {
 				Code c = findCodeLocation(subprogram, start);
 				if(c!= null) return c;
 			}
 			
 			return code;
 		}
 
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer str = new StringBuffer();
 		for (FortranFile file: fortranFiles.values()) {
 			str.append(file);
 		}
 		return str.toString();
 	}
 
 	public Subprogram getSubroutine(String name, List<? extends TypeShape> arguments, boolean immediateScope) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public TypeShape getVariableOrFunction(String name, List<? extends TypeShape> arguments,  boolean immediateScope) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Type getType(String name) {
 		// check built in
 		if(builtinTypes.containsKey(name.toLowerCase()))
 			return builtinTypes.get(name.toLowerCase());
 		
 		return null;
 	}
 
 	public Iterator<Code> codeIterator() {
 		return new CodeIterator(null);
 	}
 
 	public void generateXML(ContentHandler handler) throws SAXException {
 		for(FortranFile file: fortranFiles.values()) {
 			file.generateXML(handler);
 		}
 	}
 
 	/**
 	 * Resolve function/array references, etc.
 	 */
 	public void postProcess() {
 		Model model = new Model();
 		
 		// copy subprograms
 		for(FortranFile file: fortranFiles.values()) {
 			model.getSubpograms().putAll(file.getSubprograms());
 		}
 		
 		// create moduleToFile map and fill modules
 		Map<String,Module> modules = new HashMap<String, Module>(); 
 		Map<String,FortranFile> moduleToFileMap = new HashMap<String, FortranFile>();
 		for(FortranFile file: fortranFiles.values()) {
 			for(ProgramUnit unit: file.getProgramUnits().values()) {
 				if(unit.getUnitType()==ProgramUnit.UnitType.MODULE) {
 					moduleToFileMap.put(unit.getName().toLowerCase(), file);
 					unit.setScope(model);
 					Module module = new Module(unit);
 					modules.put(unit.getName().toLowerCase(), module);
 				}
 			}			
 		}
 		model.setModules(modules);		
 		
 		// resolve order in which modules should be inspected
 		ModuleDependencyCollector collector = new ModuleDependencyCollector();
 		
 		for(FortranFile file: fortranFiles.values()) {
 			file.astWalk(collector);
 		}
 		collector.solveDependencies();
 		model.setModuleSequence(collector.getModuleSequence());
 		
 		// create program unit sequence
 		List<ProgramUnit> unitSequence = new ArrayList<ProgramUnit>();
 		// first modules
 		List<String> moduleNames = collector.getModuleSequence();
 		for(String moduleName: moduleNames) {
 			ProgramUnit module = moduleToFileMap.get(moduleName).getProgramUnits().get(moduleName);
 			unitSequence.add(module);
 			moduleToFileMap.remove(moduleName);
 		}
 		// then programs, TODO external subroutines
 		for(FortranFile file: fortranFiles.values()) {
 			for(ProgramUnit unit: file.getProgramUnits().values()) {
 				if(unit.getUnitType()==ProgramUnit.UnitType.PROGRAM)
 					unitSequence.add(unit);
 			}
 		}
 		
 		// resolve data references as function calls or array references
 		DataReferenceResolver resolver = new DataReferenceResolver(model);
 		for(Subprogram sub: model.getSubpograms().values()) {
 			sub.astWalk(resolver);
 		}
 		for(ProgramUnit unit: unitSequence) {
 			unit.astWalk(resolver);
 		}
 	}	
 }
 
 /**
  * @author olegus
  *
  */
 class CodeIterator implements Iterator<Code> {
 	private Iterator<? extends Code> codeIterator;
 	private CodeIterator subcodeIterator;
 	
 	CodeIterator(Collection<? extends Code> codes) {
 		codeIterator = codes.iterator();
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.util.Iterator#hasNext()
 	 */
 	public boolean hasNext() {
 		if(subcodeIterator == null) return codeIterator.hasNext();
 		return true;
 	}
 	/* (non-Javadoc)
 	 * @see java.util.Iterator#next()
 	 */
 	public Code next() {
 		Code nextCode;
 		if(subcodeIterator == null) {
 			nextCode = codeIterator.next();
 			subcodeIterator = new CodeIterator(nextCode.getSubprograms().values());
 		} else {
 			nextCode = subcodeIterator.next();
 		}
 		
 		// nullify if empty
 		if(!subcodeIterator.hasNext()) {
 			subcodeIterator = null;
 		}
 		
 		return nextCode;
 	}
 	public void remove() {
 		// TODO Auto-generated method stub
 	}
 }
