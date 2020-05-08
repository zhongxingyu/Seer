 package assemblernator;
 
 import static assemblernator.ErrorReporting.makeError;
 import instructions.UIG_Equated;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.ErrorReporting.URBANSyntaxException;
 import assemblernator.Instruction.Operand;
 import assemblernator.Instruction.Usage;
 
 /**
  * A class representing an assembled urban module.
  * 
  * <pre>
  * {@code assembly = a sequence of Instructions.
  * symbolTable is a Module.SymbolTable
  * startAddr is the integer address at which the module starts.
  * moduleLength is the integer length of the module.}
  * </pre>
  * 
  * Module = (assembly, symbolTable, startAddr, moduleLength).
  * 
  * @author Josh Ventura
  * @date Apr 5, 2012; 7:15:44 PM
  */
 public class Module {
 	/**
 	 * <pre>
 	 * symbolTable is a sequence of entry's where an entry = (label, address, usage, string);
 	 * 	for an instruction with the label, 
 	 * 	the address is the location of the instruction,
 	 * 	the usage is how the instruction is used,
 	 * 	and the string is the string of characters that the label is equated to if 
 	 * 	the opcode of the instruction = EQU or EQUe.
 	 * SymbolTable.symbols union SymbolTable.extEntSymbols = SymbolTable.
 	 * </pre>
 	 */
 	public static class SymbolTable implements
 			Iterable<Map.Entry<String, Instruction>> {
 		/**
 		 * Compares two Map.Entry's.
 		 * 
 		 * @author Noah
 		 * @date Apr 9, 2012; 3:38:54 AM
 		 */
 		private final class MapEntryComparator implements
 				Comparator<Map.Entry<String, Instruction>> {
 			/** @see java.util.Comparator#compare(Object, Object) */
 			@Override public int compare(Map.Entry<String, Instruction> o1,
 					Map.Entry<String, Instruction> o2) {
 				return String.CASE_INSENSITIVE_ORDER.compare(o1.getKey(),
 						o2.getKey()); // same ordering as values.
 			}
 		}
 
 		/**
 		 * let (label, Instruction) = p.
 		 * symbols is a sorted Map of p's.
 		 * Each p is a single entry in SymbolTable, where
 		 * label = Instruction.label,
 		 * address = Instruction.lc,
 		 * usage = Instruction.usage,
 		 * and string = the value of the operand in Instruction.
 		 * 
 		 * does not include Instructions extEntInstr, where instr.opID = "EXT"
 		 * or "ENT".
 		 */
 		private SortedMap<String, Instruction> symbols = new TreeMap<String, Instruction>(
 				String.CASE_INSENSITIVE_ORDER);
 
 		/**
 		 * let (label, Instruction) = p.
 		 * symbols is a sorted Map of p's.
 		 * Each p is a single entry in SymbolTable, where
 		 * label = Instruction.label,
 		 * address = Instruction.lc,
 		 * usage = Instruction.usage,
 		 * and string = the value of the operand in Instruction.
 		 * 
 		 * only includes Instructions extEntInstr, where instr.opID = "EXT" or
 		 * "ENT".
 		 */
 		private Map<String, Instruction> extEntSymbols = new TreeMap<String, Instruction>();
 
 		/**
 		 * adds an entry into the symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 6, 2012; 1:56:43 PM
 		 * @modified Apr 18, 2012 PM: Added checks for duplicate symbols. -Noah
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param instr
 		 *            instruction to add.
 		 * @param hErr
 		 *            handles errors.
 		 * @specRef N/A
 		 */
 		public void addEntry(Instruction instr, ErrorHandler hErr) {
 			// keep track of instructions w/ opID "ENT" and "EXT" separately.
 			if (instr.getOpId().equalsIgnoreCase("ENT")
 					|| instr.getOpId().equalsIgnoreCase("EXT")) {
 				// put each operand as a separate entry into the symbol table.
				for (int i = 1; i < instr.countOperand("LR"); i++) {
 					String lbl = instr.getOperand("LR", i);
 					if (instr.usage == Usage.EXTERNAL
 							&& symbols.containsKey(lbl)) { // EXT label and is
 															// already in local
 						hErr.reportError(
 								makeError("shadowLabel", lbl, Integer
 										.toString(symbols.get(lbl).lineNum)),
 								instr.lineNum, -1);
 						// don't add.
 					}
 					else { // add.
 						extEntSymbols.put(instr.getOperand("LR", i), instr);
 					}
 				}
 			}
 			else {
 				if (extEntSymbols.containsKey(instr.label)
 						&& extEntSymbols.get(instr.label).usage == Usage.EXTERNAL) {
 					Instruction ext = extEntSymbols.remove(instr.label); // remove
 																			// ext
 																			// label
 																			// from
 																			// symbol
 																			// table.
 					hErr.reportError(
 							makeError("shadowLabel", instr.label,
 									Integer.toString(ext.lineNum)),
 							instr.lineNum, -1);
 					symbols.put(instr.label, instr); // put local label in
 														// symbol table.
 				}
 				else if (!symbols.containsKey(instr.label)) { // no duplicates
 																// allowed.
 					symbols.put(instr.label, instr);
 				}
 				else {
 					hErr.reportError(
 							makeError("duplicateSymbol", instr.label, Integer
 									.toString(symbols.get(instr.label).lineNum)),
 							instr.lineNum, -1);
 				}
 			}
 
 		}
 
 		/**
 		 * Returns a reference to the Instruction w/ the label given
 		 * from the symbol table.
 		 * Assumes label is in symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 7, 2012; 10:22:52 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param label
 		 *            label of Instruction to get reference to from symbol <br>
 		 *            table.
 		 * @return Instruction with label, label from symbol table.
 		 * @specRef N/A
 		 */
 		public Instruction getEntry(String label) {
 			Instruction entry;
 
 			if (symbols.containsKey(label)) {
 				entry = symbols.get(label);
 			}
 			else {
 				entry = extEntSymbols.get(label);
 			}
 			return entry;
 		}
 
 		/**
 		 * Get an ext or ent label.
 		 * 
 		 * @author Noah
 		 * @date Apr 17, 2012; 12:48:36 AM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param label
 		 *            label of entry to get.
 		 * @return for entry: (label, Instruction), return Instruction.
 		 * @specRef N/A
 		 */
 		public Instruction getEntExtEntry(String label) {
 			return extEntSymbols.get(label);
 		}
 
 		/**
 		 * Checks whether an entry with the label given exists locally.
 		 * 
 		 * @author Noah
 		 * @date Apr 17, 2012; 1:22:33 AM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param label
 		 *            label of entry to look up.
 		 * @return if entry with label label exists in symbols, return true,
 		 *         else
 		 *         return false.
 		 * @specRef N/A
 		 */
 		public boolean hasLocalEntry(String label) {
 			return symbols.containsKey(label);
 		}
 
 		/**
 		 * provides an Iterator over the elements of the symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 7, 2012; 4:19:44 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return an Iterator over elements of symbol table.
 		 * @specRef N/A
 		 */
 		@Override public Iterator<Map.Entry<String, Instruction>> iterator() {
 			List<Map.Entry<String, Instruction>> combinedSymbols = new ArrayList<Map.Entry<String, Instruction>>();
 			combinedSymbols.addAll(symbols.entrySet()); // combine
 			combinedSymbols.addAll(extEntSymbols.entrySet()); // combine
 
 			return combinedSymbols.iterator();
 		}
 
 		/**
 		 * Returns a String table representation of the symbol table.
 		 * 
 		 * @author Noah
 		 * @date Apr 15, 2012; 1:19:32 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return as table of symbol table entries, where each entry is
 		 *         represented as a string.
 		 *         The first row of the table = ["Label", "LC", "Usage",
 		 *         "Equate".
 		 * @specRef N/A
 		 */
 		public String[][] toStringTable() {
 			List<Map.Entry<String, Instruction>> combinedSymbols = new ArrayList<Map.Entry<String, Instruction>>();
 			combinedSymbols.addAll(symbols.entrySet()); // combine
 			combinedSymbols.addAll(extEntSymbols.entrySet()); // combine
 
 			Collections.sort(combinedSymbols, new MapEntryComparator()); // sort
 
 			// iterator over elements of set of label, Instruction pairs.
 			Iterator<Entry<String, Instruction>> tableIt = combinedSymbols
 					.iterator();
 
 			// all entries + 1 header entry.
 			String[][] stringTable = new String[combinedSymbols.size() + 1][4];
 
 			int x = 0;
 			stringTable[x][0] = "Label";
 			stringTable[x][1] = "LC";
 			stringTable[x][2] = "Usage";
 			stringTable[x][3] = "Equate";
 
 			while (tableIt.hasNext()) {
 				x++;
 
 				// gets the set values <K,V> stored into a map entry which can
 				// be used to get the values/key of K and V
 				Map.Entry<String, Instruction> entry = tableIt.next();
 
 				String label = entry.getKey();
 				Instruction instr = entry.getValue();
 				String addr = IOFormat.formatHexInteger(instr.lc, 4);
 				Usage usage = instr.usage;
 
 				stringTable[x][0] = label;
 				stringTable[x][1] = addr;
 				stringTable[x][2] = usage.toString();
 
 				// since equate are the only one with a string in the symbol
 				// table i use this to get the value of that string
 				if (usage == Usage.EQUATE) {
 					// gets iterator over set of operands.
 					Iterator<Operand> operandsIt = instr.operands.iterator();
 
 					if (operandsIt.hasNext()) {
 						stringTable[x][3] = operandsIt.next().expression;
 					}
 				}
 			}
 
 			return stringTable;
 		}
 
 		/**
 		 * String representation of the symbol table.
 		 * 
 		 * @author Eric
 		 * @date Apr 6, 2012; 8:58:56 AM
 		 * @modified Apr 9, 11:23:41 AM; combined separate maps into one symbol
 		 *           table, and made a comparator to sort. -Noah <br>
 		 *           Apr 7, 1:26:50 PM; added opcode to lines. - Noah <br>
 		 *           Apr 6, 11:02:08 AM; removed remove() call to prevent
 		 *           destruction of symbol table, also, cleaned code up. -Noah<br>
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return <pre>
 		 * {@code let line = a string of character representing a single entry
 		 * 	in the symbol table, concatenated w/ the opcode of the instruction with
 		 * 	the label in hex format, uppercased.  specifically:
 		 * 		label + "\t" + address + "\t" + "\t" + usage + "\t" + string + "\n".
 		 * 	each line is unique.
 		 * returns "Label:\tLC:\tUsage:\tEquString:\n" + a sequence of lines for all the entries in the symbol table.}
 		 * </pre>
 		 * @specRef N/A
 		 */
 		@Override public String toString() {
 
 			List<Map.Entry<String, Instruction>> combinedSymbols = new ArrayList<Map.Entry<String, Instruction>>();
 			combinedSymbols.addAll(symbols.entrySet()); // combine
 			combinedSymbols.addAll(extEntSymbols.entrySet()); // combine
 
 			Collections.sort(combinedSymbols, new MapEntryComparator()); // sort
 
 			// way of storing each line of the symbol table
 			List<String> completeTable = new ArrayList<String>();
 
 			// iterator over elements of set of label, Instruction pairs.
 			Iterator<Entry<String, Instruction>> tableIt = combinedSymbols
 					.iterator();
 
 			String megaTable = "Label:\tLC:\tUsage:\tEquString:\n";
 			// loop runs through the whole symbol table
 			while (tableIt.hasNext()) {
 				// gets the set values <K,V> stored into a map entry which can
 				// be used to get the values/key of K and V
 				Map.Entry<String, Instruction> entry = tableIt.next();
 
 				String label = entry.getKey();
 				Instruction instr = entry.getValue();
 				String addr = IOFormat.formatHexInteger(instr.lc, 4);
 				Usage usage = instr.usage;
 
 				String oneLine = label + "\t " + addr + "\t" + usage;
 
 				// since equate are the only one with a string in the symbol
 				// table i use this to get the value of that string
 				if (usage == Usage.EQUATE) {
 					// gets iterator over set of operands.
 					Iterator<Operand> operandsIt = instr.operands.iterator();
 
 					if (operandsIt.hasNext()) {
 						// only one operand if usage = EQUATE.
 						oneLine = oneLine + "\t" + operandsIt.next().expression;
 					}
 				}
 
 				oneLine = oneLine + "\n";
 
 				completeTable.add(oneLine);
 
 			}
 
 			// makes one big string out of each line of the symbol table
 			while (!completeTable.isEmpty()) {
 				megaTable = megaTable + completeTable.remove(0);
 			}
 
 			return megaTable;
 		}
 
 
 	}
 
 	/** An array of parsed instructions in the order they appeared in the file. **/
 	List<Instruction> assembly = new ArrayList<Instruction>();
 
 	/**
 	 * a symbol table.
 	 */
 	private SymbolTable symbolTable = new SymbolTable();
 
 	/**
 	 * The address at which execution will begin; set from the KICKO instruction
 	 * or by using the AEXS instruction. Must be between 0 and 1023, according
 	 * to specification.
 	 * 
 	 * @specRef OB1.5
 	 */
 	int startAddr;
 
 	/**
 	 * The name of the module = label of KICKO directive.
 	 */
 	public String programName;
 	/**
 	 * The length of this module, in instructions, between 0 and 1023, according
 	 * to specification.
 	 * 
 	 * @specRef OB1.3
 	 */
 	int moduleLength;
 
 	/**
 	 * Returns a reference to the symbol table.
 	 * 
 	 * @author Noah
 	 * @date Apr 7, 2012; 10:24:23 AM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return this.symbolTable
 	 * @specRef N/A
 	 */
 	public SymbolTable getSymbolTable() {
 		return symbolTable;
 	}
 
 	/**
 	 * Evaluates an expression in terms of constants and EQU directives.
 	 * 
 	 * @author Josh, Noah
 	 * @date Apr 8, 2012; 1:34:17 AM
 	 * @modified Apr 8, 2012; 11:44:46 PM: Changed to support all possible
 	 *           operands to EQU. - Josh
 	 *           Apr 12, 2012; 8:50:02 PM: modified for readability. - Noah
 	 *           Apr 13, 2012; 11:23:33 PM: modified for correctness - Noah
 	 *           Apr 13, 2012; 9:34:50 PM: symbolTable.getEntry(exp) can return
 	 *           null, so added i == null check - Noah
 	 * 
 	 *           Apr 17, 2012; 1:59:37 AM: Recoded to support compound
 	 *           expressions. -Josh
 	 * 
 	 *           Apr 18, 2012; 5:35:00 AM: Added check for empty expression.
 	 * 
 	 * @tested Apr 17, 2012; 2:33:20 AM: Field tested with five term sums in
 	 *         nested EQUs. Worked provided expression did not contain spaces.
 	 * @errors Reports errors 021-039.
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param exp
 	 *            The expression to be evaluated.
 	 * @param MREF
 	 *            True if this expression is a memory reference. Enabling this
 	 *            parameter enables forward-referencing of EQ Labels as well as
 	 *            address referencing of regular labels.
 	 * @param hErr
 	 *            The error handler which will receive problems in evaluation.
 	 * @param caller
 	 *            The instruction requesting this expression.
 	 * @param pos
 	 *            The position in the given line at which this expression
 	 *            starts.
 	 * @return The value of the expression.
 	 * @specRef N/A
 	 */
 	public int evaluate(String exp, boolean MREF, ErrorHandler hErr,
 			Instruction caller, int pos) {
 		exp = exp.trim(); // trim off leading and trailing white-space.
 
 		if (exp.length() == 0) {
 			hErr.reportError(makeError("emptyExpr"), caller.lineNum, pos);
 			return 0;
 		}
 
 		// First, check if we have a standard label
 		if (IOFormat.isValidLabel(exp)) {
 			Instruction i = symbolTable.getEntry(exp);
 			if (i == null) {
 				hErr.reportError(makeError("undefEqLabel", exp),
 						caller.lineNum, pos);
 				return 0;
 			}
 			if (i.usage == Usage.EQUATE)
 				return ((UIG_Equated) i).value;
 			if (MREF)
 				return i.lc;
 			hErr.reportError(makeError("illegalRefAmp", exp), caller.lineNum,
 					pos);
 			return 0;
 		}
 
 		// Turns out, we don't. Maybe we have a number?
 		if (IOFormat.isNumeric(exp)) {
 			try {
 				return Integer.parseInt(exp);
 			} catch (NumberFormatException nfe) {}
 			return 0;
 		}
 
 		// Guess we're a compound expression.
 
 		int lhs = 0, i = 0;
 		if (Character.isLetter(exp.charAt(0))) {
 			String lbl;
 			try {
 				lbl = IOFormat.readLabel(exp, 0);
 			} catch (URBANSyntaxException e) {
 				hErr.reportError(
 						makeError("compilerError",
 								"Invalid label should have been reported earlier"),
 						caller.lineNum, pos);
 				return 0;
 			}
 			lhs = evaluate(lbl, MREF, hErr, caller, pos);
 			i += lbl.length();
 		}
 		else if (Character.isDigit(exp.charAt(0))) {
 			// We won't hit end of char doing this, or parseInt would have
 			// succeeded.
 			while (Character.isDigit(exp.charAt(++i)));
 			lhs = evaluate(exp.substring(0, i), MREF, hErr, caller, pos);
 		}
 		else if (exp.charAt(i) == '*') {
 			lhs = caller.lc;
 			++i;
 		}
 
 		// This will not overflow, because our string is trimmed.
 		while (Character.isWhitespace(exp.charAt(i)))
 			++i;
 
 		if (exp.charAt(i) == '+')
 			return lhs
 					+ evaluate(exp.substring(i + 1), MREF, hErr, caller, pos
 							+ i + 1);
 		else if (exp.charAt(i) == '-')
 			return lhs
 					- evaluate(exp.substring(i + 1), MREF, hErr, caller, pos
 							+ i + 1);
 
 		hErr.reportError(makeError("unexpSymExp", "" + exp.charAt(i)),
 				caller.lineNum, pos + i);
 		return 0;
 	}
 
 	/**
 	 * Returns a string representation of Module.
 	 * 
 	 * @author Noah
 	 * @date Apr 8, 2012; 12:26:15 PM: Added output of binary equivalent for
 	 *       operands. Now calls toString() of Instruction.
 	 * @modified Apr 17, output
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return <pre>
 	 * {@code symbTableStr is the string representation of the symbol table. 
 	 * assemblyStr the string representation of assembly;
 	 * 	The string representation of assembly is a sequence of instrBreaks,
 	 * 		where for an Instruction instr in assembly, the corresponding 
 	 * 			instrBreak = instr.toString() i.e. String representation of Instruction.
 	 * 
 	 * returns "Symbol Table:\n" + symbTableStr + "\nInstruction breakdowns:\n" + assemblyStr;}
 	 * </pre>
 	 * 
 	 * @specRef N/A
 	 */
 	@Override public String toString() {
 		String rep = "Symbol Table:\n" + symbolTable.toString()
 				+ "\nInstruction breakdowns:\n";
 		Iterator<Instruction> assemblyIt = assembly.iterator();
 
 		while (assemblyIt.hasNext()) {
 			Instruction instr = assemblyIt.next();
 
 			rep = rep + instr.toString() + "\n";
 
 		}
 
 		return rep;
 	}
 }
