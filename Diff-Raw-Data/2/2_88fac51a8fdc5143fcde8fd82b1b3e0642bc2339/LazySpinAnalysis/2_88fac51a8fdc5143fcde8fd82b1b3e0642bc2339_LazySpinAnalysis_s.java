 package src.lazyspinfrontend;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PushbackReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import src.etch.checker.Check;
 import src.etch.checker.Checker;
 import src.etch.env.EnvEntry;
 import src.etch.env.ProctypeEntry;
 import src.etch.env.VarEntry;
 import src.etch.typeinference.ConstraintSet;
 import src.etch.typeinference.Substituter;
 import src.etch.typeinference.Unifier;
 import src.etch.types.ArrayType;
 import src.etch.types.ChanType;
 import src.etch.types.EtchTypeFactory;
 import src.etch.types.VisibleType;
 import src.promela.lexer.Lexer;
 import src.promela.lexer.LexerException;
 import src.promela.node.Node;
 import src.promela.parser.Parser;
 import src.promela.parser.ParserException;
 import src.symmextractor.PidAwareChecker;
 import src.symmextractor.types.PidType;
 import src.symmreducer.InsensitiveVariableReference;
 import src.symmreducer.PidSwapper;
 import src.symmreducer.SymmetryApplier;
 import src.utilities.BooleanOption;
 import src.utilities.Config;
 import src.utilities.ProgressPrinter;
 
 public class LazySpinAnalysis {
 
 	public static boolean FULL_REDUCTION = false;
 	public static boolean USE_MARKERS = false;
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 				
 		if(args.length < 1 || args.length > 3)
 		{
 			System.out.println("Error - usage: LazySpinAnalysis [full,markers] <file>");
 			System.exit(1);
 		}
 		
 		int argNo;
 		for(argNo = 0; argNo < args.length - 1; argNo++)
 		{
 			if(args[argNo].equals("full")) {
 				FULL_REDUCTION = true;
 			} else if(args[argNo].equals("markers")) {
 				USE_MARKERS = true;
 				System.out.println("Symmetry marker support not yet implemented.");
 				System.exit(1);
 			} else {
 				System.out.println("Ignoring unknown option: " + args[argNo]);
 			}
 				
 		}
 		
 		final String sourceName = args[argNo];
 
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
 		os.write("State tmp_now; // Global state used as temp during state canonization\n\n");
 		os.write("Perm  tmp_perm; // Permutation associated with tmp_now during state canonization\n\n");
 
 		SymmetryApplier.writePreprocessorMacros(os);
 		
 		PidSwapper pidSwapper = new PidSwapper(repGenerator, os, 0, null);
 		pidSwapper.writeApplyPrSwapToState("State");
 		
 		writeSameCell(os);
 
 		if(FULL_REDUCTION)
 		{
 			writeLessThanBetweenProcesses(repGenerator, os);
 			writeEquallyInsensitive(repGenerator, os);
 		} else {
 			writeLessThanBetweenStates(repGenerator, os);
 		}
 		
 		if(FULL_REDUCTION)
 		{
 			writeRepFull(os, N);
 		} else {
 			writeRepMemcpy(os, N);
 		}
 
 		writeSymHash(repGenerator, os);
 
 		os.flush();
 		
 	}
 
 
 
 	private static ProctypeEntry getTheSingleProctypeEntry(
 			LazySpinChecker repGenerator) {
 		ProctypeEntry theProctypeEntry = null;
 		for(EnvEntry entry : repGenerator.getEnv().getTopEntries().values())
 		{
 			if(entry instanceof ProctypeEntry)
 			{
 				theProctypeEntry = (ProctypeEntry) entry;
 				break;
 			}
 		}
 		return theProctypeEntry;
 	}
 
 	interface RelationWriter
 	{
 		String writeRelation(List<InsensitiveVariableReference> referencesI, List<InsensitiveVariableReference> referencesJ, int index);
 	}
 		
 	private static void writeInsensitiveComparison(LazySpinChecker repGenerator, OutputStreamWriter os, RelationWriter rw) throws IOException
 	{
 		os.write("  /* Id-insensitive local variables first */\n");
 		
 		ProctypeEntry theProctypeEntry = getTheSingleProctypeEntry(repGenerator);
 		assert(null != theProctypeEntry);
 		
 		List<InsensitiveVariableReference> insensitiveVarReferencesForI = repGenerator.insensitiveVariableReferencesForProcess(theProctypeEntry, "i", "s");
 		List<InsensitiveVariableReference> insensitiveVarReferencesForJ = repGenerator.insensitiveVariableReferencesForProcess(theProctypeEntry, "j", "s");
 
 		assert(insensitiveVarReferencesForI.size() == insensitiveVarReferencesForJ.size());
 		for(int ref = 0; ref < insensitiveVarReferencesForI.size(); ref++) {
 			os.write(rw.writeRelation(insensitiveVarReferencesForI, insensitiveVarReferencesForJ, ref));
 		}
 		
 		os.write("  /* Id-insensitive global array indices second */\n");
 		Map<String, EnvEntry> globalVariables = repGenerator.getGlobalVariables();
 
 		List<InsensitiveVariableReference> insensitiveGlobalPidIndexedArrayElementsForI = new ArrayList<InsensitiveVariableReference>();
 		List<InsensitiveVariableReference> insensitiveGlobalPidIndexedArrayElementsForJ = new ArrayList<InsensitiveVariableReference>();
 
 		for(String name : globalVariables.keySet()) {
 			EnvEntry entry = globalVariables.get(name);
 			if(entry instanceof VarEntry && !((VarEntry)entry).isHidden() && ((VarEntry)entry).getType() instanceof ArrayType &&
 					PidType.isPid((VisibleType)((ArrayType)(((VarEntry)entry).getType())).getIndexType())) {
 				String prefixI = "s->" + name + "[i]";
 				String prefixJ = "s->" + name + "[j]";
 				insensitiveGlobalPidIndexedArrayElementsForI.addAll(InsensitiveVariableReference.getInsensitiveVariableReferences("", ((ArrayType)((VarEntry)entry).getType()).getElementType(), prefixI, repGenerator));
 				insensitiveGlobalPidIndexedArrayElementsForJ.addAll(InsensitiveVariableReference.getInsensitiveVariableReferences("", ((ArrayType)((VarEntry)entry).getType()).getElementType(), prefixJ, repGenerator));
 			}
 		}
 
 		assert(insensitiveGlobalPidIndexedArrayElementsForI.size() == insensitiveGlobalPidIndexedArrayElementsForJ.size());
 
 		for(int ref = 0; ref < insensitiveGlobalPidIndexedArrayElementsForI.size(); ref++)
 		{
 			os.write(rw.writeRelation(insensitiveGlobalPidIndexedArrayElementsForI, insensitiveGlobalPidIndexedArrayElementsForJ, ref));
 		}
 		
 	}
 	
 	private static void writeEquallyInsensitive(LazySpinChecker repGenerator,
 			OutputStreamWriter os) throws IOException {
 		os.write("int equally_insensitive(State* s, int i, int j)\n");
 		os.write("{\n");
 		writeInsensitiveComparison(repGenerator, os, new RelationWriter() {
 			public String writeRelation(
 					List<InsensitiveVariableReference> referencesI, List<InsensitiveVariableReference> referencesJ, int index) {
 				return "  if((" + referencesI.get(index) + ") != (" + referencesJ.get(index) + ")) return 0;\n";
 			}
 		});
 		os.write("  return 1;\n");
 		os.write("}\n\n");		
 	}
 	
 	private static void writeLessThanBetweenProcesses(LazySpinChecker repGenerator, OutputStreamWriter os) throws IOException {
 		os.write("int less_than_between_processes(State* s, int i, int j)\n");
 		os.write("{\n");
 		writeInsensitiveComparison(repGenerator, os, new RelationWriter() {
 			public String writeRelation(
 					List<InsensitiveVariableReference> referencesI, List<InsensitiveVariableReference> referencesJ, int index) {
 				return "  if((" + referencesI.get(index) + ") < (" + referencesJ.get(index) + ")) return 1;\n" +
 						"  if((" + referencesI.get(index) + ") > (" + referencesJ.get(index) + ")) return 0;\n";
 			}
 		});
 		os.write("  return 0;\n");
 		os.write("}\n\n");
 	}
 
 	private static void writeLessThanBetweenStates(LazySpinChecker repGenerator, OutputStreamWriter os) throws IOException {
 		os.write("int less_than_between_states(State* s, State* t)\n");
 		os.write("{\n");
 		os.write("  return memcmp(s, t, vsize) < 0;\n");
 		os.write("}\n\n");
 	}
 		
 	private static void writeRepFull(OutputStreamWriter os, final String N)
 	throws IOException {
 		os.write("int num_blocks;\n");
 		os.write("int block_start[" + N + "], block_size[" + N + "];\n");
 		os.write("\n");
 		os.write("void permute_blocks(int block, int start, int size, Perm* alpha);\n");
 		os.write("void swap_in_block(int block, int p1, int p2, Perm* alpha);\n");
 		os.write("\n");
 		os.write("void swap_in_block(int block, int p1, int p2, Perm* alpha) {\n");
 		os.write("  /* apply transposition p1 <-> p2 */\n");
 		os.write("  applyPrSwapToState(&tmp_now, p1, p2);\n");
 		os.write("  if(alpha) {\n");
 		os.write("    unsigned char t;\n");
 		os.write("    t = tmp_perm.p_vector[p1];\n");
 		os.write("    tmp_perm.p_vector[p1] = tmp_perm.p_vector[p2];\n");
 		os.write("    tmp_perm.p_vector[p2] = t;\n");
 		os.write("  }\n");
 		os.write("  if (memcmp(&tmp_now, &min_now, vsize) < 0) {\n");
 		os.write("    memcpy(&min_now, &tmp_now, vsize);\n");
 		os.write("    if(alpha) {\n");
 		os.write("      memcpy(alpha->p_vector, tmp_perm.p_vector, alpha->n_indices);\n");
 		os.write("    }\n");
 		os.write("  }\n");
 		os.write("  /* permute the next block */\n");
 		os.write("  if (++block < num_blocks)\n");
 		os.write("    permute_blocks(block, block_start[block], block_size[block], alpha);\n");
 		os.write("}\n");
 		os.write("\n");
 		os.write("void permute_blocks(int block, int start, int size, Perm* alpha)\n");
 		os.write("{\n");
 		os.write("  int i, p, offset, pos[size], dir[size];\n");
 		os.write("  /* go to the last block */\n");
 		os.write("  if(++block < num_blocks)\n");
 		os.write("  {\n");
 		os.write("    permute_blocks(block, block_start[block], block_size[block], alpha);\n");
 		os.write("  }\n");
 		os.write("  block--;\n");
 		os.write("  for (i = 0; i < size; i++) {\n");
 		os.write("    pos[i] = 1; dir[i] = 1;\n");
 		os.write("  }\n");
 		os.write("  pos[size-1] = 0;\n");
 		os.write("  i = 0;\n");
 		os.write("  while (i < size-1) {\n");
 		os.write("    for (i = offset = 0; pos[i] == size-i; i++) {\n");
 		os.write("      pos[i] = 1; dir[i] = !dir[i];\n");
 		os.write("      if (dir[i]) offset++;\n");
 		os.write("    }\n");
 		os.write("    if (i < size-1) {\n");
 		os.write("      p = offset-1 + (dir[i] ? pos[i] : size-i-pos[i]);\n");
 		os.write("      pos[i]++;\n");
 		os.write("      swap_in_block(block, p, p+1, alpha);\n");
 		os.write("    }\n");
 		os.write("  }\n");
 		os.write("}\n");
 		os.write("\n");
 		os.write("\n");
 		os.write("State* rep(State* orig, Perm* alpha)\n");
 		os.write("{\n");
 		os.write("  int i, j, changed, current_block_start;\n");
 		os.write("  if(alpha) perm_set_to_id(alpha);\n");
 		os.write("  memcpy(&min_now, orig, vsize); // Representative first set to be original state\n");
 		os.write("\n\n");
 		os.write("  /* First, we normalise the state */\n");
 		os.write("  changed = 1;\n");
 		os.write("  while(changed)\n");
 		os.write("  {\n");
 		os.write("    changed = 0;\n");
 		os.write("    for(i = 0; i < " + N + "-1; i++)\n");		
 		os.write("    {\n");
 		os.write("      for(j = i+1; j < " + N + "; j++)\n");		
 		os.write("      {\n");
 		os.write("        if(same_cell(&min_now, i, j) && less_than_between_processes(&min_now, j, i))\n");		
 		os.write("        {\n");
 		os.write("          applyPrSwapToState(&min_now, i, j);\n");
 		os.write("          changed = 1;\n");
 		os.write("          if(alpha) {\n");
 		os.write("            unsigned char t;\n");
 		os.write("            t = alpha->p_vector[i];\n");
 		os.write("            alpha->p_vector[i] = alpha->p_vector[j];\n");
 		os.write("            alpha->p_vector[j] = t;\n");
 		os.write("          }\n");
 		os.write("        }\n");
 		os.write("      }\n");
 		os.write("    }\n");
 		os.write("  }\n\n");
 		os.write("  /* The state is now normalized.  We must now canonize it */\n");
 		os.write("\n");
 		os.write("  /* In preparation for this, we copy the current minimal state into a temporary state */\n");
 		os.write("  /* We will apply lots of permutations to this temporary state, and update the minimum along the way */\n");
 		os.write("  memcpy(&tmp_now, &min_now, vsize);\n");
 		os.write("  /* In addition, we need to track the permutation associated with tmp_now as we apply permutations to it */\n");
 		os.write("  /* Initially, this permutation is the one we have computed for min_now */\n");
		os.write("  if(alpha) tmp_perm = mk_perm(alpha);\n");
 		os.write("\n");
 		os.write("  num_blocks = 0;\n");
 		os.write("  current_block_start = 0;\n");
 		os.write("  while(current_block_start < " + N + ")\n");
 		os.write("  {\n");
 		os.write("    int current_block_size = 1;\n");
 		os.write("    while(((current_block_start+current_block_size) < " + N + ") && same_cell(&min_now, current_block_start, current_block_start + current_block_size) && equally_insensitive(&min_now, current_block_start, current_block_start + current_block_size))\n");
 		os.write("    {\n");
 		os.write("      current_block_size++;\n");
 		os.write("    }\n");
 		os.write("    if(current_block_size > 1)\n");
 		os.write("    {\n");
 		os.write("      block_start[num_blocks] = current_block_start;\n");
 		os.write("      block_size[num_blocks] = current_block_size;\n");
 		os.write("      num_blocks++;\n");
 		os.write("    }\n");
 		os.write("    current_block_start += current_block_size;\n");
 		os.write("  }\n");
 		os.write("  if(num_blocks > 0)\n");
 		os.write("  {\n");
 		os.write("    permute_blocks(0, block_start[0], block_size[0], alpha);\n");
 		os.write("  }\n");
 		os.write("  if(alpha) permx_free(&tmp_perm);\n");
 		os.write("  return &min_now;\n");
 		os.write("}\n\n");
 	}
 	
 	
 	private static void writeRepMemcpy(OutputStreamWriter os, final String N)
 			throws IOException {
 		os.write("State* rep(State* orig, Perm* alpha)\n");
 		os.write("{\n");
 		os.write("  int i, j, changed;\n");
 		os.write("  State temp;\n");
 		os.write("  if(alpha) perm_set_to_id(alpha);\n");
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
 		os.write("          if(less_than_between_states(&temp, &min_now))\n");
 		os.write("          {\n");
 		os.write("            changed = 1;\n");
 		os.write("            memcpy(&min_now, &temp, vsize);\n");
 		os.write("            if(alpha) {\n");
 		os.write("              unsigned char t;\n");
 		os.write("              t = alpha->p_vector[i];\n");
 		os.write("              alpha->p_vector[i] = alpha->p_vector[j];\n");
 		os.write("              alpha->p_vector[j] = t;\n");
 		os.write("            }\n");
 		os.write("          }\n");
 		os.write("        }\n");
 		os.write("      }\n");
 		os.write("    }\n");
 		os.write("  }\n");
 		os.write("  return &min_now;\n");
 		os.write("}\n\n");
 	}
 
 	private static void writeSymHash(LazySpinChecker repGenerator,
 			OutputStreamWriter os) throws IOException {
 		os.write("int sym_hash(State* s)\n");
 		os.write("{\n");
 		os.write("  #ifndef SYM_HASH_PRIME\n");
 		os.write("  #define SYM_HASH_PRIME 23\n");
 		os.write("  #endif\n");
 		os.write("  int result = 0;\n\n");
 		
 		List<List<InsensitiveVariableReference>> insensitiveVarReferences = new ArrayList<List<InsensitiveVariableReference>>();
 		
 		os.write("  /* Contribution from insensitive local variables */\n");
 		
 		for(int i = 0; i < LazySpinChecker.numberOfRunningProcesses(); i++) {
 			insensitiveVarReferences.add(repGenerator.insensitiveVariableReferencesForProcess(i, "s"));
 		}
 		
 		for(int ref = 0; ref < insensitiveVarReferences.get(0).size(); ref++) {
 		
 			assert(!(insensitiveVarReferences.get(0).get(ref).getType() instanceof PidType || insensitiveVarReferences.get(0).get(ref).getType() instanceof ChanType));
 			os.write("  result += ");
 			for(int i = 0; i < LazySpinChecker.numberOfRunningProcesses(); i++)	{
 				if(i > 0) {
 					os.write(" + ");
 				}
 				os.write("(" + insensitiveVarReferences.get(i).get(ref) + ")");
 			}
 			os.write(";\n");
 			os.write("  result *= SYM_HASH_PRIME;\n");
 		}
 
 		Map<String, EnvEntry> globalVariables = repGenerator.getGlobalVariables();
 
 		List<List<InsensitiveVariableReference>> insensitiveGlobalPidIndexedArrayElements = new ArrayList<List<InsensitiveVariableReference>>();
 		for(int i = 0; i < LazySpinChecker.numberOfRunningProcesses(); i++) {
 			insensitiveGlobalPidIndexedArrayElements.add(new ArrayList<InsensitiveVariableReference>());
 		}
 
 		os.write("\n");
 		os.write("  /* Contribution from global pid-indexed arrays (which are essentially local variables) */\n");
 		for(String name : globalVariables.keySet()) {
 			EnvEntry entry = globalVariables.get(name);
 			if(entry instanceof VarEntry && !((VarEntry)entry).isHidden() && ((VarEntry)entry).getType() instanceof ArrayType &&
 					PidType.isPid((VisibleType)((ArrayType)(((VarEntry)entry).getType())).getIndexType())) {
 				for(int i = 0; i < LazySpinChecker.numberOfRunningProcesses(); i++) {
 					String prefix = "s->" + name + "[" + i + "]";
 					insensitiveGlobalPidIndexedArrayElements.get(i).addAll(InsensitiveVariableReference.getInsensitiveVariableReferences("", ((ArrayType)((VarEntry)entry).getType()).getElementType(), prefix, repGenerator));
 				}
 			}
 		}
 		
 		for(int ref = 0; ref < insensitiveGlobalPidIndexedArrayElements.get(0).size(); ref++) {
 			os.write("  result += ");
 			for(int i = 0; i < LazySpinChecker.numberOfRunningProcesses(); i++) {
 				if(i > 0) {
 					os.write(" + ");
 				}
 				os.write("(" + insensitiveGlobalPidIndexedArrayElements.get(i).get(ref) + ")");
 			}
 			os.write(";\n");
 			os.write("  result *= SYM_HASH_PRIME;\n");
 		}
 		
 		os.write("\n");
 		os.write("  /* Contribution from insensitive globals */\n");
 
 		String referencePrefix = "s->";
 		List<InsensitiveVariableReference> insensitiveGlobals = new ArrayList<InsensitiveVariableReference>();
 
 		for (String name : globalVariables.keySet()) {
 			EnvEntry entry = globalVariables.get(name);
 			if(entry instanceof VarEntry && !((VarEntry)entry).isHidden()) {	
 				insensitiveGlobals.addAll(InsensitiveVariableReference.getInsensitiveVariableReferences(name, ((VarEntry)entry).getType(), referencePrefix, repGenerator));
 			}
 		}
 		
 		for(InsensitiveVariableReference ref : insensitiveGlobals) {
 			os.write("  result += (" + ref + ");\n");
 			os.write("  result *= SYM_HASH_PRIME;\n");
 		}
 			
 		os.write("\n");
 		os.write("  return result;\n");
 		os.write("}\n\n");
 	}
 
 	private static void writeSameCell(OutputStreamWriter os) throws IOException {
 		os.write("int same_cell(State* s, int i, int j)\n");
 		os.write("{\n");
 		os.write("  return part_same_cell(s->_prt, i, j);\n");
 		os.write("}\n\n");
 	}
 
 }
