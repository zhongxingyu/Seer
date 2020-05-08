 package edu.osu.cse.mmxi.asm;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import edu.osu.cse.mmxi.asm.InstructionFormat.IFRecord;
 import edu.osu.cse.mmxi.asm.error.ErrorCodes;
 import edu.osu.cse.mmxi.asm.error.ParseException;
 import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
 import edu.osu.cse.mmxi.asm.symb.SymbolExpression.OpExp;
 import edu.osu.cse.mmxi.asm.table.PsuedoOpTable;
 import edu.osu.cse.mmxi.common.Utilities;
 
 public class CommonParser {
     private static final String OPS, PSEUDO_OPS;
     static {
         final SortedSet<String> ao = new TreeSet<String>();
         for (final List<IFRecord> i : InstructionFormat.instructions.values())
             for (final IFRecord j : i)
                 ao.add(j.name.toUpperCase());
         String aoS = "", poS = "";
         for (final String op : ao)
             aoS += "|" + op;
         for (final String op : PsuedoOpTable.table.keySet())
             poS += "|" + op.substring(1).toUpperCase();
         OPS = aoS.substring(1);
         PSEUDO_OPS = "[.](" + poS.substring(1) + ")";
     }
 
     public static String[] parseLine(String line) throws ParseException {
         if (line.contains(";"))
             line = line.substring(0, line.indexOf(';'));
         if (line.trim().length() == 0)
             return new String[] { null, null };
         // instructions start with a white space, labels and programs name do not
         return parseLine(line, line.substring(0, 1).matches("\\S"));
     }
 
     /**
      * Parses a line. Really no checking occurs before this. It is presumed to be an
      * instruction.
      * 
      * @param line
      * @param hasLabel
      *            True if this is a label, False if this is an instruciton
      * @return
      * @throws ParseException
      */
     private static String[] parseLine(final String line, final boolean hasLabel)
         throws ParseException {
         final int l = hasLabel ? 1 : 0;
         final String[] tokens = line.trim().split("\\s+", l + 2);
         final String label = hasLabel ? tokens[0] : null;
 
         if (tokens.length <= l + 1)
             return new String[] { label,
                     tokens.length == l ? null : tokens[l].toUpperCase() };
         final String[] args;
         if (tokens[l + 1].matches("\\s*\".*(?<!\\\\)\"\\s*")) {
             String str = tokens[l + 1].trim();
             str = str.substring(1, str.length() - 1);
             args = new String[] { "\"" + Utilities.parseString(str) + "\"" };
         } else
             args = tokens[l + 1].split(",");
         final String[] ret = new String[args.length + 2];
         ret[0] = label;
         ret[1] = tokens[l].toUpperCase();
         for (int i = 0; i < args.length; i++)
             ret[i + 2] = args[i].trim();
         return ret;
     }
 
     public static String[] checkLine(final String[] line) throws ParseException {
         if (line[0] != null) {
             if (Utilities.parseShort(line[0]) != -1)
                 throw new ParseException(ErrorCodes.P1_INST_BAD_LABEL);
             if (line[0].matches("[rR][0-7]"))
                 throw new ParseException(ErrorCodes.P1_INST_BAD_REG);
         }
         if (line[1] != null)
             if (line[1].matches(OPS)) {
                 if (!InstructionFormat.instructions.containsKey(line[1] + ":"
                     + (line.length - 2)))
                     throw new ParseException(ErrorCodes.P1_INST_WRONG_PARAMS);
             } else if (line[1].matches(PSEUDO_OPS))
                 ;
             else if (line[0] != null
                 && (line[0].matches(OPS) || line[0].matches(PSEUDO_OPS)))
                 throw new ParseException(ErrorCodes.P1_INST_BAD_LINE_FORMAT);
             else
                throw new ParseException(ErrorCodes.P1_INST_BAD_OP_CODE, line[1]);
         return line;
     }
 
     public static void errorOnUndefinedSymbols(final SymbolExpression se)
         throws ParseException {
         errorOnUndefinedSymbols(undefinedSymbols(se));
     }
 
     public static void errorOnUndefinedSymbols(final Set<Symbol> undef)
         throws ParseException {
         if (undef.size() != 0) {
             String str = "undefined symbols: ";
             for (final Symbol s : undef)
                 str += s.name + ", ";
 
             str = str.substring(0, str.length() - 2);
             throw new ParseException(ErrorCodes.P1_INST_BAD_SYMBOL, str);
         }
     }
 
     private static Set<Symbol> undefinedSymbols(final SymbolExpression se) {
         final Set<Symbol> undef = new HashSet<Symbol>();
         undefinedSymbols(undef, se);
         return undef;
     }
 
     public static void undefinedSymbols(final Set<Symbol> undef, final SymbolExpression se) {
         if (se == null)
             return;
         if (se instanceof OpExp)
             for (final SymbolExpression operand : ((OpExp) se).operands)
                 undefinedSymbols(undef, operand);
         else if (se instanceof Symbol)
             if (((Symbol) se).value != null)
                 undefinedSymbols(undef, ((Symbol) se).value);
             else if (se != Symbol.getSymb(":START"))
                 undef.add((Symbol) se);
     }
 }
