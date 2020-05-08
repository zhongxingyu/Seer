 package net.ian.dcpu;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 public class Assembler {
 	// Blanks correspond to unspecified operations.
 	// Should probably move this elsewhere.
 	public static final String[] basicOps = {
 		"SET", "ADD", "SUB", "MUL", "MLI", "DIV", "DVI", "MOD",
 		"MDI", "AND", "BOR", "XOR", "SHR", "ASR", "SHL", "IFB",
 		"IFC", "IFE", "IFN", "IFG", "IFA", "IFL", "IFU", "   ",
 		"   ", "ADX", "SBX", "   ", "   ", "STI", "STD"
 		};
 
 	public static final String[] specialOps = {
 		"JSR", "   ", "   ", "   ", "   ", "   ", "HCF", "INT",
 		"IAG", "IAS", "RFI", "IAQ", "   ", "   ", "   ", "HWN",
 		"HWQ", "HWI", "   ", "   ", "   ", "   ", "   ", "   ",
 		"   ", "   ", "   ", "   ", "   ", "   ", "   "
 		};
 	public static final String[] registers;
 	public static final String[] special = { "SP", "PC", "EX" };
 
 	public ArrayList<Character> instructions;
 	
 	public Map<String, Integer> labels;
 	public Map<Integer, String> fixes;
 		
 	static {
 		DCPU.Register regs[] = DCPU.Register.values();
 		registers = new String[regs.length];
 		for (DCPU.Register r : regs)
 			registers[r.ordinal()] = r.toString();
 	}
 	
 	private static class Argument {
 		public List<Character> code;
 		public String label = null;
 		
 		@SuppressWarnings("unused")
 		public Argument(Character... args) {
 			code = Arrays.asList(args);
 		}
 		
 		// I immensely dislike how Java makes a big
 		// distinction between integer data types.
 		public Argument(int... args) {
 			code = new ArrayList<Character>();
 			for (int i : args)
 				code.add((char)i);
 		}
 		
 		@SuppressWarnings("unused")
 		public Argument(String label, Character... args) {
 			code = Arrays.asList(args);
 			this.label = label;
 		}
 		
 		public Argument(String label, int... args) {
 			this(args);
 			this.label = label;
 		}
 	}
 	
 	public List<Character> assemble(String code) {
 		instructions = new ArrayList<Character>();
 		labels = new HashMap<String, Integer>();
 		fixes = new HashMap<Integer, String>();
 		
 		String[] lines = code.trim().split("\\s*\n\\s*");
 		for (String line : lines) {
 			// Comments
 			String[] c = line.split(";");
 
 			if (c.length == 0)
 				continue;
			line = c[0].trim();
 			if (line.isEmpty())
 				continue;
 			
 			// Labels
 			if (line.startsWith(":")) {
 				String[] tmp = line.split("\\s+", 2);
 				String label = tmp[0].substring(1).toUpperCase();
 				if (labels.containsKey(label))
 					System.err.println("Error: Label " + label + " defined twice!");
 				else
 					labels.put(label, instructions.size());
 				if (tmp.length < 2)
 					continue;
 				line = tmp[1];
 				if (line.isEmpty())
 					continue;
 			}
 			
 			// Split instruction and args
 			String[] tokens = line.split("\\s+", 2);
 
 			if (tokens.length < 2)
 				continue;
 
 			if (tokens[0].equalsIgnoreCase("DAT")) {
 				instructions.addAll(parseDat(line));
 				continue;
 			}
 			
 			String op = tokens[0];
 
 			// Split args separated by comma
 			// Nested expressions such as "PICK N" will not be splitted
 			String[] args = tokens[1].split("\\s*,\\s*");
 
 			// more than 2 arguments ? error
 			if (args.length > 2)
 				continue;
 
 			String arg1 = args[0];
 			String arg2 = null;
 			if (args.length == 2)
 				arg2 = args[1];
 			List<Character> assembled = assemble(op, arg1, arg2);
 			if (assembled == null)
 				instructions.add((char)0);
 			else
 				instructions.addAll(assembled);
 		}
 		
 		insertLabels();
 		
 		return instructions;
 	}
 	
 	private List<Character> parseDat(String line) {
 		List<Character> data = new ArrayList<>();
 		
 		line = line.trim();
 		line = line.substring(3); // Length of "DAT"
 		line = line.trim();
 		while (!line.isEmpty()) {
 			if (line.charAt(0) == ';')
 				break;
 			else if (line.charAt(0) == '"') {
 				// Handle string literals.
 				line = line.substring(1);
 				int i = 0;
 				while (true) {
 					if (line.charAt(i) == '\\') {
 						if (line.charAt(i+1) == 'n')
 							data.add('\n');
 						else if (line.charAt(i+1) == 't')
 							data.add('\t');
 						else if (line.charAt(i+1) == '"')
 							data.add('"');
 						else if (line.charAt(i+1) == '0')
 							data.add('\0');
 						else
 							data.add(line.charAt(i+1));
 						i += 2;
 						continue;
 					} else if (line.charAt(i) == '"')
 						break;
 					data.add(line.charAt(i));
 					i++;
 				}
 				line = line.substring(i+1);
 			} else {
 				String[] split = line.split("\\s*(,|\\s)\\s*", 2);
 				String num = split[0];
 				if (num.isEmpty()) {
 					if (split.length < 2)
 						break;
 					line = split[1];
 					continue;
 				}
 				data.add((char)parseInt(num));
 				line = line.substring(num.length());
 			}
 			line = line.trim();
 		}
 		return data;
 	}
 
 	// This inserts labels in parts of the program where they
 	// were used before they existed, via the "fixes" map.
 	private void insertLabels() {
 		for (Map.Entry<Integer, String> entry : fixes.entrySet()) {
 			int index = entry.getKey();
 			String label = entry.getValue();
 			System.err.printf("Fixing: %s at %d\n", label, index);
 			Integer loc;
 			if ((loc = labels.get(label)) != null) {
 				instructions.set(index, (char)(int)loc);
 			} else
 				System.err.printf("Error: True assembly error (in insertLabels): %s at %d\n", label, index);
 		}
 	}
 	
 	public List<Character> assemble(String sOp, String sArg1, String sArg2) {
 		sOp = sOp.toUpperCase();
 		boolean isBasic = (sArg2 != null);
 		int op = Arrays.asList(isBasic ? basicOps : specialOps).indexOf(sOp) + 1;
 		if (op < 1)
 			System.err.println("Broken OP! \"" + sOp + "\" isBasic = " + isBasic);
 		int a, b = -1;
 		int instructionCount = instructions.size();
 		
 		sArg1 = sArg1.toUpperCase();
 		if (isBasic)
 			sArg2 = sArg2.toUpperCase();
 		
 		Argument argA = handleArgument(isBasic ? sArg2 : sArg1);
 		List<Character> codeA = argA.code;
 		instructionCount += codeA.size() - 1;
 		
 		if (argA.label != null)
 			fixes.put(instructionCount, argA.label);
 		
 		a = codeA.get(0);
 			
 		Argument argB = null;
 		List<Character> codeB = null;
 		if (isBasic) {
 			argB = handleArgument(sArg1);
 			codeB = argB.code;
 			instructionCount += codeB.size() - 1;
 			if (argB.label != null)
 				fixes.put(instructionCount, argB.label);
 			b = codeB.get(0);
 		}
 		
 		List<Character> words = new ArrayList<>();
 		words.add(compile(op, a, b));
 		words.addAll(codeA.subList(1, codeA.size()));
 		if (argB != null)
 			words.addAll(codeB.subList(1, codeB.size()));
 				
 		return words;
 	}
 	
 	public List<Character> assemble(String op, String arg) {
 		return assemble(op, arg, null);
 	}
 	 
 	private Argument handleArgument(String arg) {
 		int index = Arrays.asList(registers).indexOf(arg);
 		if (index != -1)
 			return new Argument(index);
 		
 		if ((index = Arrays.asList(special).indexOf(arg)) != -1)
 			return new Argument(index + 0x1b);
 
 		Argument argument;
 		if ((argument = handleStack(arg)) != null)
 			return argument;
 		
 		if (labels.containsKey(arg)) {
 			int loc = labels.get(arg);
 			if (loc < 30)
 				return new Argument(loc + 0x21);
 			return new Argument(0x1f, loc);
 		}
 		
 		int n;
 		try {
 			n = parseInt(arg);
 			if (n >= -1 && n < 30)
 				return new Argument(n + 0x21);
 			return new Argument(0x1f, n);
 		} catch (NumberFormatException _) {}
 		
 		if (arg.startsWith("[")) {
 			if (!arg.endsWith("]")) {
 				System.err.println("Error: No closing square bracket in argument: \"" + arg + "\"");
 				return null;
 			}
 			arg = arg.substring(1, arg.length() - 1);
 			if ((index = Arrays.asList(registers).indexOf(arg)) != -1)
 				return new Argument(index + 0x8);
 			try {
 				n = parseInt(arg);
 				return new Argument(0x1e, n);
 			} catch (NumberFormatException _) {}
 				
 			if (labels.containsKey(arg))
 				return new Argument(0x1e, labels.get(arg));
 			if (arg.contains("+")) {
 				String split[] = arg.split("\\s*\\+\\s*", 2);
 				String other;
 				for (int i = 0; i < 2; i++) {
 					if ((index = Arrays.asList(registers).indexOf(split[i])) != -1) {
 						other = split[1 - i];
 						try {
 							n = parseInt(other);
 							return new Argument(index + 0x10, n);
 						} catch (NumberFormatException _) {}
 						return new Argument(other, index + 0x10, -1);
 					}
 				}
 				
 			}
 			
 			// This didn't match anything. It might be referencing a label (this gets fixed in insertLabels()).
 			return new Argument(arg, 0x1e, -1);
 		}
 		
 		// Same here.
 		return new Argument(arg, 0x1f, -1);
 	}
 	
 	private static int parseInt(String s) throws NumberFormatException {
 		try {
 			int n = Integer.parseInt(s);
 			return n;
 		} catch (NumberFormatException _) {
 			// Whelp, it wasn't a decimal number.
 		}
 		int sign = 1;
 		if (s.startsWith("+") || s.startsWith("-")) {
 			sign = s.startsWith("+") ? 1 : -1;
 			s = s.substring(1);
 		}
 		if (s.toLowerCase().startsWith("0x")) {
 			try {
 				int n = Integer.parseInt(s.substring(2), 16);
 				return n * sign;
 			} catch (NumberFormatException _) {
 				// Whelp, it wasn't a hexadecimal number.				
 			}
 		}
 		if (s.toLowerCase().startsWith("0b")) {
 			try {
 				int n = Integer.parseInt(s.substring(2), 2);
 				return n * sign;
 			} catch (NumberFormatException _) {
 				// Also not binary.
 			}
 		}
 		
 		// I don't care if it's used by Integer.parseInt(), I'm stealing it! Muahahahaha!
 		throw new NumberFormatException("Could not convert string \"" + s + "\" to a decimal, hex, or binary number.");
 	}
 	
 	private static Argument handleStack(String s) {
 		if (s.equals("POP") || s.equals("PUSH"))
 			return new Argument(0x18);
 		if (s.equals("PEEK"))
 			return new Argument(0x19);
 		if (s.startsWith("PICK")) {
 			String[] split = s.split("\\s*(\\s|,)\\s*", 2);
 			return new Argument(0x1a, parseInt(split[1]));
 		}
 		return null;
 	}
 	
 	// Changes arguments into machine code.
 	public static char compile(int op, int a, int b) {
 		boolean isBasic = (b != -1);
 		
 		String sOp = String.format("%05d", Integer.parseInt(Integer.toBinaryString(op)));
 		String sA = String.format("%06d", Integer.parseInt(Integer.toBinaryString(a)));
 		String sB = isBasic ? String.format("%05d", Integer.parseInt(Integer.toBinaryString(b))) : "";
 		
 		return (char)Integer.parseInt(sA + sB + sOp + (isBasic ? "" : "00000"), 2);
 	}
 	
 	public static char compile(int op, int arg) {
 		return compile(op, arg, -1);
 	}
 	
 	public static void main(String args[]) {
 		String input = new Scanner(System.in).useDelimiter("\\A").next();
 		Assembler as = new Assembler();
 		for (int word : as.assemble(input))
 			System.out.printf("%04x\n", word);
 	}
 }
