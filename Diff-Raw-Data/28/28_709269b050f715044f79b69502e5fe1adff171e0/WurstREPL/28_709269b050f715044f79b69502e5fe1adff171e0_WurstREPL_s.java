 package de.peeeq.eclipsewurstplugin.console;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.ui.console.IOConsoleOutputStream;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import de.peeeq.eclipsewurstplugin.builder.ModelManager;
 import de.peeeq.wurstio.WurstCompilerJassImpl;
 import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
 import de.peeeq.wurstio.jassinterpreter.InterpreterException;
 import de.peeeq.wurstio.jassinterpreter.NativeFunctionsIO;
 import de.peeeq.wurstscript.RunArgs;
 import de.peeeq.wurstscript.WLogger;
 import de.peeeq.wurstscript.WurstConfig;
 import de.peeeq.wurstscript.ast.ClassDef;
 import de.peeeq.wurstscript.ast.CompilationUnit;
 import de.peeeq.wurstscript.ast.FuncDef;
 import de.peeeq.wurstscript.ast.LocalVarDef;
 import de.peeeq.wurstscript.ast.StmtSet;
 import de.peeeq.wurstscript.ast.TupleDef;
 import de.peeeq.wurstscript.ast.WPackage;
 import de.peeeq.wurstscript.ast.WParameter;
 import de.peeeq.wurstscript.ast.WStatement;
 import de.peeeq.wurstscript.ast.WurstModel;
 import de.peeeq.wurstscript.attributes.CompileError;
 import de.peeeq.wurstscript.gui.WurstGuiLogger;
 import de.peeeq.wurstscript.intermediateLang.ILconst;
 import de.peeeq.wurstscript.intermediateLang.ILconstReal;
 import de.peeeq.wurstscript.intermediateLang.ILconstTuple;
 import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
 import de.peeeq.wurstscript.intermediateLang.interpreter.ILStackFrame;
 import de.peeeq.wurstscript.intermediateLang.interpreter.LocalState;
 import de.peeeq.wurstscript.jassAst.JassProg;
 import de.peeeq.wurstscript.jassIm.ImFunction;
 import de.peeeq.wurstscript.jassIm.ImProg;
 import de.peeeq.wurstscript.jassIm.ImStmt;
 import de.peeeq.wurstscript.jassinterpreter.TestFailException;
 import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
 import de.peeeq.wurstscript.jassprinter.JassPrinter;
 import de.peeeq.wurstscript.parser.WPos;
 import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
 import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
 import de.peeeq.wurstscript.types.WurstType;
 import de.peeeq.wurstscript.types.WurstTypeNamedScope;
 import de.peeeq.wurstscript.types.WurstTypePrimitive;
 import de.peeeq.wurstscript.types.WurstTypeTuple;
 import de.peeeq.wurstscript.types.WurstTypeVoid;
 import de.peeeq.wurstscript.utils.ExecutiontimeMeasure;
 import de.peeeq.wurstscript.utils.Pair;
 import de.peeeq.wurstscript.utils.Utils;
 
 
 /**
  * 
  * Read eval print loop for wurst
  */
 public class WurstREPL {
 
 	
 	private static final String REPL_DUMMY_FILENAME = "<REPL>";
 	private static final String WURST_REPL_RESULT = "res";
 	private IOConsoleOutputStream out;
 	private ModelManager modelManager;
 	private ReplGui gui;
 	private CompilationUnit editorCu;
 	private ILInterpreter interpreter;
 	private Map<String, String> currentState;
 	private Set<String> importedPackages;
 	private Random rand = new Random();
 	
 	private class ReplGui extends WurstGuiLogger {
 
 		@Override
 		public void sendError(CompileError err) {
 			super.sendError(err);
 			throw err;
 		}
 
 	}
 	
 	
 	public WurstREPL(ModelManager mm, IOConsoleOutputStream s) {
 		this.modelManager = mm;
 		this.out = s;
 		gui = this.new ReplGui();
 		init();
 	}
 
 	private void init() {
 		currentState = Maps.newLinkedHashMap();
 		importedPackages = Sets.newLinkedHashSet();
 		RobustProgramState globalState = new RobustProgramState(null, gui);
 		interpreter = new ILInterpreter(null, gui, null, globalState);
 		
 		interpreter.addNativeProvider(new NativeFunctionsIO());
 //		interpreter.addNativeProvider(new CompiletimeNatives(globalState));
 		
 		globalState.setOutStream(new PrintStream(out));
 	}
 	
 	private void print(String msg) {
 		try {
 			out.write(msg);
 		} catch (IOException e) {
 			WLogger.severe(e);
 		}
 	}
 
 	
 	Pattern asignmentPattern = Pattern.compile("^\\s*([a-zA-Z0-9_]*\\s+)?([a-zA-Z][a-zA-Z0-9_]*)\\s*=.*");
 	
 	public void putLine(String line) {
 		
 		
 		try {
 			if (line.isEmpty()) {
 				return;
 			} else if (line.equals("reset")) {
 				init();
 				return;
 			} else if (line.equals("main")) {
 				if (translateProg() != null) {
 					interpreter.executeFunction("main");
 				}
 				return;
 			} else if (line.equals("help")) {
 				printHelp();
 				return;
 			} else if (line.equals("tests")) {
 				runTests();
 				return;
 			} else if (line.startsWith("compile")) {
 				compileProject(line.substring("compile".length()));
 				return;
 			} else if (line.startsWith("printClasses")) {
 				printClasses();
 				return;
 			}
 			
 			gui.clearErrors();
 			StringBuilder code = new StringBuilder();
 			code.append("package WurstREPL\n");
 			// import packages from current compilation unit
 			for (WPackage p : editorCu.getPackages()) {
 				importedPackages.add(p.getName());
 			}
 			for (String pName : importedPackages) {
 				code.append("import " + pName + "\n");
 			}
 			// TODO
 			// set current state
 			
 			code.append("public function repl_start()\n");
 			
 			
 			for (String cmd : currentState.values()) {
 				code.append("\t" + cmd + "\n");
 			}
 			String varName = addEnteredCommand(line, code);
 			
 			
 			WLogger.info("############ code:");
 			WLogger.info(code.toString());
 			
 			
 			CompilationUnit cu = parse(code);
 			WStatement assignment = getTranslatedCommand(cu);
 			
 			String tempName = null;
 			if (assignment instanceof LocalVarDef) {
 				LocalVarDef def = (LocalVarDef) assignment;
 				// change name to some temporary name
 				tempName = "tempName" + (rand.nextInt());
 				def.setName(tempName);
 			}
 			
 			LocalState val = null;
 			try {
 				val = executeReplCode(cu);
 			} catch (CompileError err) {
 				handleCompileError(err);
 				return;
 			} catch (TestFailException e) {
 				print(e + "\n");
 			} catch (TestSuccessException e) {
 				print("Test successful.\n");
 			} catch (Throwable t) {
 				// if there was an error, check if there is a problem in typechecking:
 				
 				try (ExecutiontimeMeasure tt = new ExecutiontimeMeasure("type checking")) {
 					modelManager.typeCheckModel(gui, false, false);
 				} catch (CompileError err) {
 					handleCompileError(err);
 					return;
 				}
 				
 				// if there was no comile error
 				// this is probably a bug
				if (t instanceof InterpreterException) {
 					print(t.getMessage() + "\n");
 				}  else {
 					print("You discovered a bug in the interpreter: \n");
 					print(t+"\n");
 				}
 				
 				
 				WPos source = interpreter.getLastStatement().attrTrace().attrSource();
 				print("When executing line " + source.getLine() + " in " + source.getFile() + "\n");
 				
 				for (ILStackFrame sf : Utils.iterateReverse(interpreter.getStackFrames())) {
 					print(sf.getMessage() + "\n");
 				}
 				
 				WLogger.severe(t);
 			}
 			
 			if (val == null) {
 				return;
 			}
 			
 			if (assignment instanceof LocalVarDef) {
 				LocalVarDef lvd = (LocalVarDef) assignment;
 				WurstType typ = lvd.attrTyp();
 				ILconst value = val.getVarValue(tempName);
 				
 				
 				
 				String valueTranslated = getTranslatedValue(typ, value);
 				if (valueTranslated == null) return;
 				
 				if (value instanceof ILconstReal) {
 					ILconstReal r = (ILconstReal) value;
 					if (Float.isNaN(r.getVal()) || r.getVal() == Float.NEGATIVE_INFINITY || r.getVal() == Float.POSITIVE_INFINITY) {
 						print("cannot store result of computation: " + r.getVal() + "\n");
 						return;
 					}
 				}
 				
 				print(varName + " = " + value + "     // " + typ + "\n");
 				
 				
 				
 				currentState.put(varName, "let " + varName + " = " + valueTranslated);
 				
 			} else if (assignment instanceof StmtSet) {
 				StmtSet stmtSet = (StmtSet) assignment;
 				WurstType typ = stmtSet.getRight().attrTyp();
 				ILconst value = interpreter.getGlobalState().getVarValue(varName);
 				print(varName + " = " + value + "     // " + typ + "\n");
 			}
 			
 			
 		} catch (CompileError e) {
 			handleCompileError(e);
 		} catch (DebugPrintError e) {
 			if (interpreter != null) {
 				print("Runtime error at: " + Utils.printElementWithSource(interpreter.getLastStatement().attrTrace()) 
 						 +"\n");
 			}
 			print(e.getMessage() + "\n");
 		} catch (Throwable e) {
 			print("Error: " + e + "\n");
 			e.printStackTrace();
 			return;
 		} finally {
 			// remove repl file again
 			modelManager.removeCompilationUnitByName(REPL_DUMMY_FILENAME);
 		}
 	}
 
 	private void printClasses() {
 		WurstModel model = modelManager.getModel();
 		for (CompilationUnit cu : model) {
 			for(ClassDef c: cu.attrGetByType().classes) {
 				String s = "class "+ c.getName();
 				if (c.attrExtendedClass() != null) {
 					s += " extends " + c.attrExtendedClass().getName();
 				}
 				s += "\n";
 				if (!c.getOnDestroy().attrHasEmptyBody()) {
 					s += "	ondestroy\n";
 					s += "		s+=\""+c.getName()+"\"\n";
 				}
 				print(s);
 			}
 		}
 		
 	}
 
 	public void handleCompileError(CompileError err) {
 		if (err.getSource().getFile().equals(REPL_DUMMY_FILENAME)) {
 			print("Error in last entered statement:\n" + err.getMessage() + "\n");
 		} else {
 			print(err.toString() + "\n");
 		}
 	}
 
 	public LocalState executeReplCode(CompilationUnit replCU)
 			throws IOException {
 		WurstModel model = replCU.getModel();
 		
 		
 		
 		ImProg imProg;
 		try (ExecutiontimeMeasure t = new ExecutiontimeMeasure("translation")) {
 			imProg = translate(model);
 		}
 		
 		LocalState val;
 		try (ExecutiontimeMeasure t = new ExecutiontimeMeasure("interpretation")) {
 			interpreter.setProgram(imProg);
 			val = interpreter.executeFunction("repl_start");
 		}
 		return val;
 	}
 
 	private void compileProject(String args) {
 		try {
 			args = args.trim();
 			String[] runArgs = args.split("\\s+");
 			modelManager.clean();
 			IProject project = modelManager.getNature().getProject();
 			print("compiling project "+project.getName()+", please wait ...\n");
 			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
 			WurstConfig config = new WurstConfig();
 			WurstCompilerJassImpl comp = new WurstCompilerJassImpl(config, gui, new RunArgs(runArgs));
 			WurstModel root = modelManager.getModel();
 			comp.checkAndTranslate(root);
 			JassProg jassProg = comp.getProg();
 			if (jassProg == null) {
 				print("Could not compile project\n");
 				return;
 			}
 
 
 			JassPrinter printer = new JassPrinter(true);
 			String mapScript = printer.printProg(jassProg);
 
 			IFile f = project.getFile("compiled.j");
 			if (f.exists()) {
 				f.delete(true, null);
 			}
 			InputStream source = new ByteArrayInputStream(mapScript.getBytes());
 
 			f.create(source, true, null);
 			
 			print("Output created in " + f.getFullPath() + "\n");
 		} catch (CoreException e) {
 			e.printStackTrace();
 			print(e.getMessage()+ "\n");
 		}
 	}
 
 	private void runTests() {
 		modelManager.removeCompilationUnit(null);
 		ImProg imProg = translateProg();
 		if (imProg == null) {
 			return;
 		}
 		List<ImFunction> successTests = Lists.newArrayList();
 		Map<ImFunction, Pair<ImStmt, String>> failTests = Maps.newLinkedHashMap();
 		for (ImFunction f : imProg.getFunctions()) {
 			if (f.hasFlag(FunctionFlag.IS_TEST)) {
 				print("Testing " + Utils.printElementWithSource(f.attrTrace()) + "	... ");
 				try {
 					interpreter.runVoidFunc(f);
 					successTests.add(f);
 				} catch (TestSuccessException e) {
 					print("  ✓");
 				} catch (TestFailException e) {
 					failTests.put(f, Pair.create(interpreter.getLastStatement(), e.toString()));
 					print("FAIL\n");
 					continue;
 				}
 				successTests.add(f);
 				print("✓\n");
 			}
 		}
 		print(successTests.size() + " tests OK, ");
 		print(failTests.size() + " tests failed\n");
 		for (Entry<ImFunction, Pair<ImStmt, String>> e : failTests.entrySet()) {
 			print(Utils.printElementWithSource(e.getKey().attrTrace()) 
 				+ "\n\t" + e.getValue().getB()
 				+ "\n\tat " + Utils.printElementWithSource(e.getValue().getA().attrTrace())+ "\n");
 		}
 	}
 
 	private ImProg translateProg() {
 		gui.clearErrors();
 		WurstModel model = modelManager.getModel();
 		modelManager.typeCheckModel(gui, false, false);
 		if (gui.getErrorCount() > 0) {
 			print(gui.getErrors() + "\n");
 			return null;
 		}
 		ImProg imProg = translate(model);
 		interpreter.setProgram(imProg);
 		return imProg;
 	}
 
 	private void printHelp() {
 		PrintWriter pw = new PrintWriter(out);
 		pw.println("Available commands: ");
 		pw.println("  'reset'    resets the console to the inital state.");
 		pw.println("  'main'     run the main function of the program. ");
 		pw.println("  'tests'    run all the tests (functions annotated with @test). ");
 		pw.println();
 		pw.println("Cou can write down any wurst expression, variable assignment or local variable definition.");
 		pw.println("All functions from the currently opened editor can be used.");
 		pw.flush();
 	}
 
 	private String getTranslatedValue(WurstType typ, ILconst value) {
 		String valueTranslated = null;
 		if (typ instanceof WurstTypePrimitive) {
 			valueTranslated = value + "";
 		} else if (typ instanceof WurstTypeNamedScope) {
 			WurstTypeNamedScope ct = (WurstTypeNamedScope) typ;
 			valueTranslated = value + " castTo " + ct.getName();
 		} else if (typ instanceof WurstTypeTuple) {
 			WurstTypeTuple tt = (WurstTypeTuple) typ;
 			TupleDef def = tt.getTupleDef();
 			valueTranslated = def.getName() + "(";
 			if (value instanceof ILconstTuple) {
 				ILconstTuple tuple = (ILconstTuple) value;
 				for (int i=0; i<def.getParameters().size(); i++) {
 					WParameter p = def.getParameters().get(i);
 					ILconst argVal = tuple.getValue(i);
 					if (i > 0) {
 						valueTranslated += ", ";
 					}
 					valueTranslated += getTranslatedValue(p.attrTyp(), argVal);
 				}
 			}
 			valueTranslated += ")";
 		} else if (typ instanceof WurstTypeVoid) {
 			// do nothing
 		} else {
 			print("typ = " + typ);
 		}
 		return valueTranslated;
 	}
 
 	private WStatement getTranslatedCommand(CompilationUnit cu) {
 		FuncDef f = (FuncDef) cu.getPackages().get(0).getElements().get(0);
 		return f.getBody().get(f.getBody().size()-2);
 	}
 
 	private ImProg translate(WurstModel model) {
 		ImTranslator imTranslator = new ImTranslator(model, false);
 		imTranslator.setEclipseMode(true);
 		ImProg imProg = imTranslator.translateProg();
 		return imProg;
 	}
 
 	private CompilationUnit parse(StringBuilder code) {
 		Reader source = new StringReader(code.toString());
 		CompilationUnit cu = modelManager.parse(gui, REPL_DUMMY_FILENAME, source);
 		return cu;
 	}
 
 	private String addEnteredCommand(String enteredLine, StringBuilder code) {
 		code.append("\t");
 		Matcher asignmentPatternMatcher = asignmentPattern.matcher(enteredLine);
 		String varName;
 		if (asignmentPatternMatcher.matches()) {
 			varName = asignmentPatternMatcher.group(2);
 			code.append(enteredLine);
 		} else {
 			varName = WURST_REPL_RESULT;
 			code.append("let "+varName+" = " + enteredLine);
 		}
 		code.append("\n");
 		return varName;
 	}
 
 	public void setModelManager(ModelManager modelManager2) {
 		this.modelManager = modelManager2;
 	}
 
 	public void setEditorCompilationUnit(CompilationUnit editorCu) {
 		this.editorCu = editorCu;
 		
 	}
 	
 }
