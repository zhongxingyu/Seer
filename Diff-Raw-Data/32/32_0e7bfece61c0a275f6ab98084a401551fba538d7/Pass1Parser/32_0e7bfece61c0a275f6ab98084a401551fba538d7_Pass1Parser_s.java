 package edu.osu.cse.mmxi.asm;
 
 import static edu.osu.cse.mmxi.asm.CommonParser.checkLine;
 import static edu.osu.cse.mmxi.asm.CommonParser.parseLine;
 
 import java.io.IOException;
 import java.util.List;
 
 import edu.osu.cse.mmxi.asm.error.Error;
 import edu.osu.cse.mmxi.asm.error.ErrorCodes;
 import edu.osu.cse.mmxi.asm.error.ParseException;
 import edu.osu.cse.mmxi.asm.line.AssemblyLine.ExpressionArg;
 import edu.osu.cse.mmxi.asm.line.AssemblyLine.InstructionLine;
 import edu.osu.cse.mmxi.asm.line.AssemblyLine.Label;
 import edu.osu.cse.mmxi.asm.line.AssemblyLine.StringArg;
 import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
 import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
 import edu.osu.cse.mmxi.common.Utilities;
 
 public class Pass1Parser {
     private final Assembler   a;
     private Symbol            lcBase;
     private short             lc;
     private int               lineNumber;
     private int               tempNumber;
     private String            line;
     private Label             label;
     private InstructionLine   inst;
     private final List<Error> errors;
 
     public Pass1Parser(final Assembler a, final List<Error> errors) {
         this.a = a;
         this.errors = errors;
     }
 
     public String parse() throws IOException, ParseException {
         lcBase = Symbol.getSymb(":START");
         lc = 0;
         lineNumber = 1;
         tempNumber = 0;
         while ((line = a.io.getLine()) != null) {
             try {
                 final String[] parsed = checkLine(parseLine(line));
 
                 label = parsed[0] == null ? null : new Label(parsed[0]);
 
                 inst = parsed[1] == null ? null : new InstructionLine(parsed);
 
                 if ((inst == null || !inst.opcode.matches("[.]ORIG|[.]EQU"))
                     && label != null)
                     label.symb.set(ArithmeticParser.parseF(":0 + :1", lcBase, lc));
 
                 if (inst != null)
                     if (inst.opcode.charAt(0) == '.') {
                         if (inst.args.length != 1 && !inst.opcode.equals(".ORIG"))
                             throw new ParseException(lineNumber,
                                 ErrorCodes.P1_INST_WRONG_PARAMS, inst.opcode
                                     + " takes exactly one argument");
                         if (inst.opcode.equals(".ORIG"))
                             parseORIG();
                         else if (inst.opcode.equals(".EQU"))
                             parseEQU();
                         else if (inst.opcode.equals(".END"))
                             parseEND();
                         else if (inst.opcode.equals(".STRZ"))
                             parseSTRZ();
                         else if (inst.opcode.equals(".FILL"))
                             parseFILL();
                         else if (inst.opcode.equals(".BLKW"))
                             parseBLKW();
                     } else
                         parseInstruction();
             } catch (final ParseException e) {
                 // handle the error
                 String msg = e.getMessage();
                 msg = msg == null ? "" : "\"" + line + "\"\n" + msg;
                 final Error err = new Error(lineNumber, msg, e.getErrorCode());
                 errors.add(err);
             }
             lineNumber++;
         }
         cleanupSymbols();
         return Symbol.printSymbs();
     }
 
     private void parseORIG() throws ParseException {
         if (inst.args.length > 1)
             throw new ParseException(ErrorCodes.P1_INST_BAD_ORIG_ARGS);
         if (label == null)
             throw new ParseException(ErrorCodes.P1_INST_BAD_LABEL);
         Symbol.removeSymb(a.segName = label.symb.name);
         if (a.segName.length() > 6)
             a.segName = a.segName.substring(0, 6);
         if (inst.args.length > 0) {
             if (!(inst.args[0] instanceof ExpressionArg))
                 throw new ParseException(ErrorCodes.P1_INST_BAD_ORIG_TYPE);
             Symbol.getSymb(":START").set(((ExpressionArg) inst.args[0]).val);
 
             // verify the addres is within range
             final int parsedAddress = Utilities
                .parseShort(((ExpressionArg) inst.args[0]).val.toString());
            if (parsedAddress < 0 || parsedAddress > 0xFFFF) {
                System.out.println(parsedAddress);
                 throw new ParseException(ErrorCodes.P1_INST_BAD_ORIG_ADDR);
            }
         }
     }
 
     private void parseEQU() throws ParseException {
         if (label == null)
             throw new ParseException(ErrorCodes.P1_INST_BAD_EQU_LABEL);
         if (!(inst.args[0] instanceof ExpressionArg))
             throw new ParseException(ErrorCodes.P1_INST_BAD_EQU_IMM);
         label.symb.set(((ExpressionArg) inst.args[0]).val);
     }
 
     private void parseEND() throws ParseException {
         if (!(inst.args[0] instanceof ExpressionArg))
             throw new ParseException(ErrorCodes.P1_INST_BAD_END_IMM);
         Symbol.getSymb(":EXEC").set(((ExpressionArg) inst.args[0]).val);
     }
 
     private void parseSTRZ() throws ParseException {
         if (!(inst.args[0] instanceof StringArg))
             throw new ParseException(ErrorCodes.P1_INST_BAD_STRZ);
         lc += ((StringArg) inst.args[0]).arg.length() + 1;
     }
 
     private void parseFILL() throws ParseException {
         if (!(inst.args[0] instanceof ExpressionArg))
             throw new ParseException(ErrorCodes.P1_INST_BAD_FILL);
         lc++;
     }
 
     private void parseBLKW() throws ParseException {
         if (!(inst.args[0] instanceof ExpressionArg))
             throw new ParseException(ErrorCodes.P1_INST_BAD_BLKW);
         final SymbolExpression len = ((ExpressionArg) inst.args[0]).val;
         final Short val = len.evaluate();
         if (val != null)
             lc += val;
         else {
             lcBase = Symbol.getSymb(":T" + ++tempNumber).set(
                 ArithmeticParser.parseF(":0 + :1 + :2", lcBase, lc, len));
             lc = 0;
         }
     }
 
     private void parseInstruction() throws ParseException {
         final SymbolExpression len = InstructionFormat.getLength(inst);
         final Short val = len.evaluate();
         if (val != null)
             lc += val;
         else {
             lcBase = Symbol.getSymb(":T" + ++tempNumber).set(
                 ArithmeticParser.parseF(":0 + :1 + :2", lcBase, lc, len));
             lc = 0;
         }
     }
 
     private void cleanupSymbols() {
         try {
             Symbol.getSymb(":END").set(ArithmeticParser.parseF(":0 + :1", lcBase, lc));
             Literal.complete = true;
         } catch (final ParseException e) {
             System.err.println(e.getMessage());
         }
         for (final Symbol s : Symbol.symbs.values())
             s.expand();
         for (final Symbol s : Literal.table.values())
             s.expand();
     }
 }
