 package src.lazyspinfrontend;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PushbackReader;
 
 import src.etch.checker.Check;
 import src.etch.checker.Checker;
 import src.etch.typeinference.ConstraintSet;
 import src.etch.typeinference.Substituter;
 import src.etch.typeinference.Unifier;
 import src.etch.types.EtchTypeFactory;
 import src.promela.lexer.Lexer;
 import src.promela.lexer.LexerException;
 import src.promela.node.Node;
 import src.promela.parser.Parser;
 import src.promela.parser.ParserException;
import src.symmextractor.PidAwareChecker;
 import src.symmreducer.PidSwapper;
 import src.symmreducer.SymmetryApplier;
 import src.utilities.BooleanOption;
 import src.utilities.Config;
 import src.utilities.ProgressPrinter;
 
 public class LazySpinAnalysis {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		
		PidAwareChecker.HACK_FOR_SPIN_2011 = true;
		
 		if(args.length != 1)
 		{
 			System.out.println("Error - usage: LazySpinAnalysis <file>");
 			System.exit(1);
 		}
 				
 		final String sourceName = args[0];
 
 		Config.resetConfiguration();
 		Config.setUnspecifiedOptionsToDefaultValues();
 		Config.initialiseCommandLineSwitches();
 		Config.setBooleanOption(BooleanOption.VERBOSE, true);
 		
 		BufferedReader br = null;
 		try {
 			br = Check.getBufferForInputSpecification(sourceName);
 		} catch (FileNotFoundException e) {
 			ProgressPrinter.println("Cannot access source file " + sourceName);
 			System.exit(1);
 		}
 		
 		Node theAST = null;
 		
 		try {
 			theAST = new Parser(new Lexer(new PushbackReader(br, 1024))).parse();
 		} catch (ParserException e) {
 			e.printStackTrace();
 			System.exit(1);
 		} catch (LexerException e) {
 			e.printStackTrace();
 			System.exit(1);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		Checker checker = new Checker(new EtchTypeFactory(), new ConstraintSet(new Unifier()));
 		theAST.apply(checker);
 		if(checker.getErrorTable().hasErrors())	{
 			System.err.println(checker.getErrorTable().output("while processing " + sourceName, sourceName));
 			System.exit(1);
 		}
 		
 		LazySpinFindNumProcesses findNumProcesses = new LazySpinFindNumProcesses();
 		theAST.apply(findNumProcesses);
 
 		LazySpinChecker repGenerator = new LazySpinChecker(findNumProcesses);
 		theAST.apply(repGenerator);
 
 		Substituter substituter = repGenerator.unify();
 		
 		substituter.setTypeInformation(repGenerator);
 		
 		if(repGenerator.getErrorTable().hasErrors()) {
 			System.err.println(repGenerator.getErrorTable().output("while processing " + sourceName, sourceName));
 			System.exit(1);
 		}
 		
 		theAST.apply(substituter);
 		
 		OutputStreamWriter os = new OutputStreamWriter(System.out);		
 		
 		final String N = "LAZYSPIN_NUM_PROCESSES";
 		
 		os.write("#define " + N + " " + LazySpinChecker.numberOfRunningProcesses() + "\n\n");
 		os.write("State min_now; // Global state used as target for state canonization\n\n");
 
 		SymmetryApplier.writePreprocessorMacros(os);
 		
 		PidSwapper pidSwapper = new PidSwapper(repGenerator, os, 0, null);
 		pidSwapper.writeApplyPrSwapToState("State");
 		
 		os.write("int same_cell(State* s, int i, int j)\n");
 		os.write("{\n");
 		os.write("  return part_same_cell(s->_prt, i, j);\n");
 		os.write("}\n\n");
 
 		os.write("int less_than(State* s, State* t)\n");
 		os.write("{\n");
 		os.write("  return memcmp(s, t, vsize) < 0;\n");
 		os.write("}\n\n");
 
 		os.write("\n");
 		os.write("State* rep(State* orig, Perm** p)\n");
 		os.write("{\n");
 		os.write("  int i, j, changed;\n");
 		os.write("  State temp;\n");
 		os.write("  *p = perm_id(orig->_nr_pr);\n");
 		os.write("  memcpy(&min_now, orig, vsize); // Representative first set to be original state\n");
 		os.write("  changed = 1;\n");
 		os.write("  while(changed)\n");
 		os.write("  {\n");
 		os.write("    changed = 0;\n");
         os.write("    for(i = 0; i < " + N + "-1; i++)\n");		
 		os.write("    {\n");
         os.write("      for(j = i+1; j < " + N + "; j++)\n");		
 		os.write("      {\n");
         os.write("        if(same_cell(orig, i, j))\n");		
 		os.write("        {\n");
 		os.write("          memcpy(&temp, &min_now, vsize);\n");
 		os.write("          applyPrSwapToState(&temp, i, j);\n");
		os.write("          if(less_than(&temp, &min_now))\n");
 		os.write("          {\n");
 		os.write("            unsigned char t;\n");
 		os.write("            changed = 1;\n");
 		os.write("            memcpy(&min_now, &temp, vsize);\n");
 		os.write("            t = (*p)->p_vector[i];\n");
 		os.write("            (*p)->p_vector[i] = (*p)->p_vector[j];\n");
 		os.write("            (*p)->p_vector[j] = t;\n");
 		os.write("          }\n");
 		os.write("        }\n");
 		os.write("      }\n");
 		os.write("    }\n");
 		os.write("  }\n");
 		os.write("  return &min_now;\n");
 		os.write("}\n\n");
 
 		os.flush();
 		
 	}
 
 }
