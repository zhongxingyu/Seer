 /*
  *  This file is part of the jasm project (http://code.google.com/p/jasm).
  *
  *  This file is licensed to you under the BSD License; You may not use
  *  this file except in compliance with the License. See the LICENSE.txt
  *  file distributed with this work for a copy of the License and information
  *  regarding copyright ownership.
  */
 package jasm.gen.risc;
 
 import jasm.Argument;
 import jasm.LabelOffsetInstruction;
 import jasm.SymbolicArgument;
 import jasm.gen.AssemblerGenerator;
 import jasm.gen.Assembly;
 import jasm.gen.ImmediateArgument;
 import jasm.gen.InstructionConstraint;
 import jasm.gen.LabelParameter;
 import jasm.gen.OffsetParameter;
 import jasm.gen.Parameter;
 import jasm.gen.risc.bitRange.BitRange;
 import jasm.gen.risc.field.InputOperandField;
 import jasm.gen.risc.field.OperandField;
 import jasm.util.Ints;
 import jasm.util.collect.AppendableSequence;
 import jasm.util.collect.Sequence;
 import jasm.util.io.IndentWriter;
 import jasm.util.program.ProgramError;
 
 /**
  *
  *
  * @author Bernd Mathiske
  * @author Doug Simon
  */
 public abstract class RiscAssemblerGenerator<Template_Type extends RiscTemplate> extends AssemblerGenerator<Template_Type> {
 
     protected RiscAssemblerGenerator(Assembly<Template_Type> assembly) {
         super(assembly, false);
     }
 
     private String encode(OperandField operandField, String val) {
         String value = val;
         // Convert the argument value to the operand value
         if (operandField.zeroes() != 0) {
             value = "(" + value + " >> " + operandField.zeroes() + ")";
         }
         return operandField.bitRange().encodingString(value, operandField.isSigned(), false);
     }
 
     @Override
     protected int printMethod(IndentWriter writer, Template_Type template) {
         final int startLineCount = writer.lineCount();
         writer.print("public void ");
         writer.print(template.assemblerMethodName() + "(");
         writer.print(formatParameterList("final ", template.parameters(), false));
         writer.println(") {");
         writer.indent();
         writer.println("int instruction = " + Ints.toHexLiteral(template.opcode()) + ";");
 
         // Print argument constraint checking statements
         final Sequence<InstructionConstraint> constraints = template.instructionDescription().constraints();
         for (InstructionConstraint constraint : constraints) {
             final String constraintExpr = constraint.asJavaExpression();
             writer.println("checkConstraint(" + constraintExpr + ", \"" + constraintExpr + "\");");
         }
 
         for (OperandField operandField : template.operandFields()) {
             if (operandField instanceof InputOperandField) {
                 continue;
             }
             writer.println("instruction |= " + encode(operandField, operandField.valueString()) + ";");
         }
 
         writer.println("emitInt(instruction);");
         writer.outdent();
         writer.println("}");
         return writer.lineCount() - startLineCount;
     }
 
     private void printOffsetLabelMethod(IndentWriter writer, Template_Type template) {
         final Sequence<Parameter> parameters = printLabelMethodHead(writer, template);
         writer.println("emitInt(0); // instruction place holder");
         writer.print("new " + LabelOffsetInstruction.class.getSimpleName());
         writer.println("(this, " + parameters.get(template.labelParameterIndex()).variableName() + ") {");
         writer.indent();
         writer.println("@Override");
         writer.println("protected int templateSerial() { return " + template.serial() + "; }");
         writer.println("@Override");
         writer.println("protected void assemble() throws AssemblyException {");
         writer.indent();
         printRawCall(writer, template, parameters);
         writer.outdent();
         writer.println("}");
         writer.outdent();
         writer.println("};");
         writer.outdent();
         writer.println("}");
         writer.println();
     }
 
     @Override
     protected int printLabelMethod(IndentWriter writer, Template_Type labelTemplate, Sequence<Template_Type> labelTemplates) {
         final int startLineCount = writer.lineCount();
         printOffsetLabelMethod(writer, labelTemplate);
         return writer.lineCount() - startLineCount;
     }
 
     /**
      * Prints the reference to the raw method from which a synthetic method was defined.
      */
     @Override
     protected void printExtraMethodJavadoc(IndentWriter writer, Template_Type template, AppendableSequence<String> extraLinks) {
         if (template.instructionDescription().isSynthetic()) {
          final RiscTemplate rawTemplate = template.synthesizedFrom();
             final Sequence<? extends Parameter> parameters = getParameters(rawTemplate);
             final String ref = rawTemplate.internalName() + "(" + formatParameterList("", parameters, true) + ")";
             writer.println(" * <p>");
             writer.print(" * This is a synthetic instruction equivalent to: {@code " + rawTemplate.internalName() + "(");
             extraLinks.append("#" + ref);
 
             boolean firstOperand = true;
             for (OperandField rawOperand : rawTemplate.operandFields()) {
                 if (!firstOperand) {
                     writer.print(", ");
                 }
                writer.print(getRawOperandReplacement(template, rawOperand));
                 firstOperand = false;
             }
 
             writer.println(")}");
         }
     }
 
     /**
      * Gets the expression in terms of the parameters and opcode of a synthetic instruction that replaces a parameter of
      * the raw instruction from which the synthetic operand was derived.
      *
      * @param syntheticTemplate
      *                the synthetic instruction
      * @param rawOperand
 *                a parameter of {@code rawTemplate}
      */
    private String getRawOperandReplacement(RiscTemplate syntheticTemplate,
                                            OperandField rawOperand) {
         if (Sequence.Static.containsIdentical(syntheticTemplate.operandFields(), rawOperand)) {
             if (rawOperand instanceof OffsetParameter && generatingLabelAssembler()) {
                 return new LabelParameter().variableName();
             }
             return rawOperand.variableName();
         }
 
         final int rawOperandMask = rawOperand.bitRange().instructionMask();
         String expression = null;
         if ((syntheticTemplate.opcodeMask() & rawOperandMask) != 0) {
             // Some or all bits of the raw operand are encoded as part of the synthetic instruction opcode
             final Argument value = rawOperand.disassemble(syntheticTemplate.opcode());
             assert value != null;
             if (value instanceof SymbolicArgument) {
                 expression = ((SymbolicArgument) value).name();
             } else if (value instanceof Enum) {
                 expression = ((Enum) value).name();
             } else if (value instanceof ImmediateArgument) {
                expression = Long.toString(value.asLong());
             } else {
                 ProgramError.unexpected("unknown type of disassembled value: " + value.getClass().getName());
             }
         }
         if ((syntheticTemplate.opcodeMask() & rawOperandMask) != rawOperandMask) {
             // Some or all bits of the raw operand are given as a parameter of the synthetic instruction
             for (OperandField syntheticOperand : syntheticTemplate.operandFields()) {
                 final int syntheticOperandMask = syntheticOperand.bitRange().instructionMask();
                 if ((syntheticOperandMask & rawOperandMask) != 0) {
                     final String term;
                     if (syntheticOperand.boundTo() != null) {
                         term = syntheticOperand.boundTo().valueString();
                     } else {
                         assert (syntheticOperandMask & rawOperandMask) == syntheticOperandMask :
                             "cannot handle synthetic parameter that defines bits that are not a subset of bits defined by a raw parameter";
                         final BitRange subFieldRange = syntheticOperand.bitRange().move(false, syntheticOperand.bitRange().numberOfLessSignificantBits());
                         final int shift = syntheticOperand.bitRange().numberOfLessSignificantBits() - rawOperand.bitRange().numberOfLessSignificantBits();
                         final String value = syntheticOperand.variableName();
                         final String assembledSubField = subFieldRange.encodingString(value, syntheticOperand.isSigned(), true);
                         if (shift != 0) {
                             term = "(" + assembledSubField + " * " + (1 << shift) + ")";
                         } else {
                             term = assembledSubField;
                         }
                     }
 
                     if (expression != null && !expression.equals("0")) {
                         expression += " | " + term;
                     } else {
                         expression = term;
                     }
                 }
             }
         }
         assert expression != null;
         return expression;
     }
 }
