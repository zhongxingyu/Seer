 /*
  *  This file is part of the jasm project (http://code.google.com/p/jasm).
  *
  *  This file is licensed to you under the BSD License; You may not use
  *  this file except in compliance with the License. See the LICENSE.txt
  *  file distributed with this work for a copy of the License and information
  *  regarding copyright ownership.
  */
 package jasm.tools.cisc.x86;
 
 import jasm.EnumerableArgument;
 import jasm.SymbolSet;
 import jasm.WordWidth;
 import jasm.tools.ArgumentRange;
 import jasm.tools.ExternalPresence;
 import jasm.tools.Immediate8Argument;
 import jasm.tools.InstructionConstraint;
 import jasm.tools.InstructionDescription;
 import jasm.tools.Template;
 import jasm.tools.TestArgumentExclusion;
 import jasm.tools.Trace;
 import jasm.tools.cisc.TemplateNotNeededException;
 import jasm.util.HexByte;
 import jasm.x86.FPStackRegister;
 import jasm.x86.GeneralRegister;
 import jasm.x86.SegmentRegister;
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class X86Template extends Template implements X86InstructionDescriptionVisitor {
   // Defaults initial capacity for the ArrayLists below.
   private static final int MAX_NUM_OF_OPERANDS = 3;
   private static final int MAX_NUM_OF_IMPLICIT_OPERANDS = 3;
   private static final int MAX_NUM_OF_PARAMETERS = 3;
 
   private final InstructionAssessment _instructionFamily;
   private boolean _hasSibByte;
   private final X86TemplateContext _context;
   private final X86InstructionPrefix _instructionSelectionPrefix;
   private HexByte _opcode1;
   private HexByte _opcode2;
   private ModRMGroup _modRMGroup;
   private ModRMGroup.Opcode _modRMGroupOpcode;
   private ArrayList<X86Operand> _operands = new ArrayList<X86Operand>(MAX_NUM_OF_OPERANDS);
   private ArrayList<X86ImplicitOperand> _implicitOperands = new ArrayList<X86ImplicitOperand>(MAX_NUM_OF_IMPLICIT_OPERANDS);
   private ArrayList<X86Parameter> _parameters = new ArrayList<X86Parameter>(MAX_NUM_OF_PARAMETERS);
   protected boolean _isLabelMethodWritten;
 
   protected X86Template(X86InstructionDescription instructionDescription, int serial, InstructionAssessment instructionFamily, X86TemplateContext context) {
     super(instructionDescription, serial);
     _instructionFamily = instructionFamily;
     _instructionSelectionPrefix = instructionDescription.getMandatoryPrefix();
     _context = context;
     Trace.line(2, "template #" + serial + ": " + context);
   }
 
   @Override
   public final X86InstructionDescription instructionDescription() {
     return (X86InstructionDescription) super.instructionDescription();
   }
 
   protected final X86TemplateContext context() {
     return _context;
   }
 
   public final X86InstructionPrefix instructionSelectionPrefix() {
     return _instructionSelectionPrefix;
   }
 
   public final HexByte opcode1() {
     return _opcode1;
   }
 
   public final HexByte opcode2() {
     return _opcode2;
   }
 
   public final boolean hasModRMByte() {
     return _instructionFamily.hasModRMByte();
   }
 
   public final ModCase modCase() {
     return _context.modCase();
   }
 
   public final ModRMGroup modRMGroup() {
     return _modRMGroup;
   }
 
   public final ModRMGroup.Opcode modRMGroupOpcode() {
     return _modRMGroupOpcode;
   }
 
   public final RMCase rmCase() {
     return _context.rmCase();
   }
 
   public final boolean hasSibByte() {
     return _hasSibByte;
   }
 
   protected final void haveSibByte() {
     _hasSibByte = true;
   }
 
   public final SibBaseCase sibBaseCase() {
     return _context.sibBaseCase();
   }
 
   public final WordWidth addressSizeAttribute() {
     return _context.addressSizeAttribute();
   }
 
   public final WordWidth operandSizeAttribute() {
     return _context.operandSizeAttribute();
   }
 
   private WordWidth _externalCodeSizeAttribute;
 
   public final WordWidth externalCodeSizeAttribute() {
     return _externalCodeSizeAttribute;
   }
 
   protected final void setExternalCodeSizeAttribute(WordWidth externalCodeSizeAttribute) {
     _externalCodeSizeAttribute = externalCodeSizeAttribute;
   }
 
   @Override
   public final String internalName() {
     String result = super.internalName();
     if (result != null && _internalOperandTypeSuffix != null) {
       result += _internalOperandTypeSuffix;
     }
     return result;
   }
 
   @Override
   public final String externalName() {
     if (instructionDescription().externalName() != null) {
       return instructionDescription().externalName();
     }
     String result = super.internalName();
     if (_externalOperandTypeSuffix != null) {
       result += _externalOperandTypeSuffix;
     }
     return result;
   }
 
   private String format(X86InstructionPrefix parameter) {
     return parameter == null ? "" : parameter.getValue().toString() + ", ";
   }
 
   private String format(HexByte parameter) {
     return parameter == null ? "" : parameter.toString() + ", ";
   }
 
   @Override
   public final String toString() {
     return "<X86Template #" + serial() + ": " + internalName() + " " +
            format(_instructionSelectionPrefix) +
            format(_opcode1) +
            format(_opcode2) +
            _operands + ">";
   }
 
   private String _namePrefix = "";
 
   protected final void useNamePrefix(String namePrefix) {
     if (_namePrefix.length() == 0) {
       _namePrefix = namePrefix;
     }
   }
 
   private String _assemblerMethodName;
 
   @Override
   public final String assemblerMethodName() {
     if (_assemblerMethodName == null) {
       _assemblerMethodName = _namePrefix + internalName();
       if (_implicitOperands.size() == 1) {
         final X86ImplicitOperand implicitOperand = _implicitOperands.get(0);
         switch (implicitOperand.designation()) {
           case DESTINATION:
           case OTHER:
             break;
           case SOURCE:
             _assemblerMethodName += "__";
             break;
         }
         _assemblerMethodName += "_" + implicitOperand.name();
       } else {
         for (X86ImplicitOperand implicitOperand : _implicitOperands) {
           _assemblerMethodName += "_" + implicitOperand.name();
         }
       }
     }
     return _assemblerMethodName;
   }
 
   public final boolean isExternalOperandOrderingInverted() {
     return instructionDescription().isExternalOperandOrderingInverted();
   }
 
   public final InstructionDescription modRMInstructionDescription() {
     if (_modRMGroup == null) {
       return null;
     }
     return _modRMGroup.getInstructionDescription(_modRMGroupOpcode);
   }
 
   protected final <Parameter_Type extends X86Parameter> Parameter_Type addParameter(Parameter_Type parameter) {
     _parameters.add(parameter);
     _operands.add(parameter);
     if (parameter instanceof X86AddressParameter) {
       useNamePrefix("m_");
     }
     return parameter;
   }
 
   protected final void addParameter(X86Parameter parameter, ArgumentRange argumentRange) {
     addParameter(parameter);
     parameter.setArgumentRange(argumentRange);
   }
 
   protected final void addParameter(X86Parameter parameter, ArgumentRange argumentRange, TestArgumentExclusion testArgumentExclusion) {
     addParameter(parameter, argumentRange);
     parameter.excludeTestArguments(testArgumentExclusion);
   }
 
  protected final <EnumerableArgument_Type extends EnumerableArgument> X86Parameter addEnumerableParameter(Designation designation, ParameterPlace parameterPlace,
                                                                                                            final SymbolSet<EnumerableArgument_Type> enumerator) {
     return addParameter(new X86EnumerableParameter<EnumerableArgument_Type>(designation, parameterPlace, enumerator));
   }
 
   protected final void addImplicitOperand(X86ImplicitOperand implicitOperand) {
     _implicitOperands.add(implicitOperand);
     _operands.add(implicitOperand);
   }
 
   public final List<X86ImplicitOperand> implicitOperands() {
     return _implicitOperands;
   }
 
   @Override
   public final List<X86Operand> operands() {
     return _operands;
   }
 
   @Override
   public final List<X86Parameter> parameters() {
     return _parameters;
   }
 
   public final void visitAddressingMethodCode(AddressingMethodCode addressingMethodCode, Designation designation) throws TemplateNotNeededException {
     switch (addressingMethodCode) {
       case M: {
         visitOperandCode(OperandCode.Mv, designation, ArgumentRange.UNSPECIFIED, TestArgumentExclusion.NONE);
         break;
       }
       default: {
         throw new IllegalStateException("don't know what to do with addressing method code: " + addressingMethodCode);
       }
     }
   }
 
   private String getOperandTypeSuffix(OperandTypeCode operandTypeCode) throws TemplateNotNeededException {
     switch (operandTypeCode) {
       case b:
         return "b";
       case z:
         if (operandSizeAttribute() != addressSizeAttribute()) {
           throw new TemplateNotNeededException();
         }
       case d_q:
       case v:
         switch (operandSizeAttribute()) {
           case BITS_16:
             return "w";
           case BITS_32:
             return "l";
           case BITS_64:
             return "q";
           default:
             throw new IllegalStateException();
         }
       default:
         break;
     }
     return operandTypeCode.name();
   }
 
   private void checkSuffix(String newSuffix, String oldSuffix) {
     if (oldSuffix != null) {
       if (!newSuffix.equals(oldSuffix))
         throw new IllegalStateException("conflicting operand type codes specified: " + newSuffix + " vs. " + oldSuffix);
     }
   }
 
   private String _externalOperandTypeSuffix;
 
   private void setExternalOperandTypeSuffix(String suffix) {
     checkSuffix(suffix, _externalOperandTypeSuffix);
     _externalOperandTypeSuffix = suffix;
   }
 
   protected final void setExternalOperandTypeSuffix(OperandTypeCode operandTypeCode) throws TemplateNotNeededException {
     setExternalOperandTypeSuffix(getOperandTypeSuffix(operandTypeCode));
   }
 
   private String _internalOperandTypeSuffix;
 
   protected final void setOperandTypeSuffix(String suffix) {
     setExternalOperandTypeSuffix(suffix);
     checkSuffix(suffix, _internalOperandTypeSuffix);
     _internalOperandTypeSuffix = suffix;
   }
 
   public final void visitOperandTypeCode(OperandTypeCode operandTypeCode) throws TemplateNotNeededException {
     setOperandTypeSuffix(getOperandTypeSuffix(operandTypeCode));
   }
 
   public final void visitGeneralRegister(GeneralRegister generalRegister, Designation designation, ExternalPresence externalPresence) {
     addImplicitOperand(new X86ImplicitOperand(designation, externalPresence, generalRegister));
   }
 
   public final void visitSegmentRegister(SegmentRegister segmentRegister, Designation designation) {
     addImplicitOperand(new X86ImplicitOperand(designation, ExternalPresence.EXPLICIT, segmentRegister));
   }
 
   public final void visitModRMGroup(ModRMGroup modRMGroup) throws TemplateNotNeededException {
     _modRMGroup = modRMGroup;
     final ModRMDescription instructionDescription = modRMGroup.getInstructionDescription(_context.modRMGroupOpcode());
     if (instructionDescription == null) {
       throw new TemplateNotNeededException();
     }
     _modRMGroupOpcode = instructionDescription.opcode();
     setInternalName(instructionDescription.name().toLowerCase());
   }
 
   public final void visitModCase(ModCase modCase) throws TemplateNotNeededException {
     if (_context.modCase() != ModCase.MOD_3) {
       throw new TemplateNotNeededException();
     }
   }
 
   public final void visitInstructionConstraint(InstructionConstraint constraint) {
   }
 
   protected abstract void organize_M(Designation designation) throws TemplateNotNeededException;
 
   protected final <EnumerableArgument_Type extends Enum<EnumerableArgument_Type> & EnumerableArgument> void organize_E(Designation designation, ParameterPlace place,
                                                                                                                        final SymbolSet<EnumerableArgument_Type> registerEnumerator, TestArgumentExclusion testArgumentExclusion) throws TemplateNotNeededException {
     if (context().modCase() == ModCase.MOD_3) {
       switch (context().rmCase()) {
         case NORMAL:
           addEnumerableParameter(designation, place, registerEnumerator).excludeTestArguments(testArgumentExclusion);
           break;
         default:
           throw new TemplateNotNeededException();
       }
     } else {
       organize_M(designation);
     }
   }
 
   public final void visitFloatingPointOperandCode(FloatingPointOperandCode floatingPointOperandCode, Designation designation,
                                                   final TestArgumentExclusion testArgumentExclusion) throws TemplateNotNeededException {
     switch (floatingPointOperandCode) {
       case ST_i:
         addEnumerableParameter(designation, ParameterPlace.OPCODE2, FPStackRegister.SYMBOLS).excludeTestArguments(testArgumentExclusion);
         break;
       default:
         setOperandTypeSuffix(floatingPointOperandCode.operandTypeSuffix());
         organize_M(designation);
         break;
     }
   }
 
   public final void visitFPStackRegister(FPStackRegister fpStackRegister, Designation designation) {
     addImplicitOperand(new X86ImplicitOperand(designation, ExternalPresence.EXPLICIT, fpStackRegister));
   }
 
   public final void visitString(String string) {
     assert internalName() == null;
     setInternalName(string.toLowerCase());
   }
 
   public final void visitInteger(Integer integer, Designation designation) {
     addImplicitOperand(new X86ImplicitOperand(designation, ExternalPresence.EXPLICIT, new Immediate8Argument((byte) integer.intValue())));
   }
 
   public final void visitHexByte(HexByte hexByte) throws TemplateNotNeededException {
     if (_opcode1 == null) {
       _opcode1 = hexByte;
     } else if (_opcode2 == null) {
       _opcode2 = hexByte;
     } else {
       throw new IllegalStateException("Unexpected byte " + hexByte);
     }
   }
 
   /**
    * @param other another template to compare against
    * @return whether both templates have the same name and operands and thus
    *         are assumed to implement the same machine instruction semantics,
    *         though potentially denoting different machine codes
    */
   public final boolean isRedundant(X86Template other) {
     if (!assemblerMethodName().equals(other.assemblerMethodName())) {
       return false;
     }
     if (_parameters.size() != other._parameters.size()) {
       return false;
     }
     for (int i = 0; i < _parameters.size(); i++) {
       if (!_parameters.get(i).type().equals(other._parameters.get(i).type())) {
         return false;
       }
     }
     return true;
   }
 }
