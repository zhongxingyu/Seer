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
 import jasm.tools.InstructionDescription;
 import jasm.tools.Template;
 import jasm.tools.TestArgumentExclusion;
 import jasm.tools.Trace;
 import jasm.tools.cisc.TemplateNotNeededException;
 import jasm.x86.FPStackRegister;
 import jasm.x86.GeneralRegister;
 import jasm.x86.SegmentRegister;
 import jasm.x86.X86InstructionPrefix;
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class X86Template<Template_Type extends X86Template<Template_Type>>
     extends Template<Template_Type>
     implements X86InstructionDescriptionVisitor {
   // Defaults initial capacity for the ArrayLists below.
   private static final int MAX_NUM_OF_OPERANDS = 3;
   private static final int MAX_NUM_OF_IMPLICIT_OPERANDS = 3;
   private static final int MAX_NUM_OF_PARAMETERS = 3;
 
   private final X86TemplateContext _context;
   private ArrayList<X86Operand> _operands = new ArrayList<X86Operand>(MAX_NUM_OF_OPERANDS);
   private ArrayList<X86ImplicitOperand> _implicitOperands = new ArrayList<X86ImplicitOperand>(MAX_NUM_OF_IMPLICIT_OPERANDS);
   private ArrayList<X86Parameter> _parameters = new ArrayList<X86Parameter>(MAX_NUM_OF_PARAMETERS);
   private String _namePrefix = "";
   private String _assemblerMethodName;
   private String _internalOperandTypeSuffix;
   private String _externalOperandTypeSuffix;
   private WordWidth _externalCodeSizeAttribute;
 
   protected X86Template(X86InstructionDescription instructionDescription,
                         int serial,
                         X86TemplateContext context) {
     super(instructionDescription, serial);
     final String name = instructionDescription.name();
     if (null != name) setInternalName(name);
     _context = context;
     Trace.line(2, "template #" + serial + ": " + context);
   }
 
   @Override
   public final X86InstructionDescription description() {
     return (X86InstructionDescription) super.description();
   }
 
   protected final X86TemplateContext context() {
     return _context;
   }
 
   public final boolean hasModRMByte() {
     return null != context().modCase();
   }
 
   public final ModCase modCase() {
     return context().modCase();
   }
 
   public final ModRMOpcode modRMGroupOpcode() {
     return context().modRMGroupOpcode();
   }
 
   public final RMCase rmCase() {
     return context().rmCase();
   }
 
   public final boolean hasSibByte() {
     return rmCase() == RMCase.SIB;
   }
 
   public final SibBaseCase sibBaseCase() {
     return context().sibBaseCase();
   }
 
   public final WordWidth addressSizeAttribute() {
     return context().addressSizeAttribute();
   }
 
   public final WordWidth operandSizeAttribute() {
     return context().operandSizeAttribute();
   }
 
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
     if (description().externalName() != null) {
       return description().externalName();
     }
     String result = super.internalName();
     if (_externalOperandTypeSuffix != null) {
       result += _externalOperandTypeSuffix;
     }
     return result;
   }
 
   @Override
   public final String toString() {
     final X86InstructionDescription d = description();
     final X86InstructionPrefix group1Prefix = d.mandatoryGroup1Prefix();
     final String group1PrefixDesc = group1Prefix == null ? "" : group1Prefix.getValue() + ", ";
     final String operandPrefixDesc =
         d.hasOperandPrefix() ? X86InstructionPrefix.OPERAND_SIZE.getValue() + ", " : "";
 
     final String opcode2Desc = d.opcode2() == null ? "" : d.opcode2() + ", ";
     return "<X86Template #" + serial() + ": " + internalName() + " " +
            group1PrefixDesc +
            operandPrefixDesc +
           d.opcode1() + ", " +
            opcode2Desc +
            _operands + ">";
   }
 
   protected final void useNamePrefix(String namePrefix) {
     if (_namePrefix.length() == 0) {
       _namePrefix = namePrefix;
     }
   }
 
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
     return description().isExternalOperandOrderingInverted();
   }
 
   public final InstructionDescription modRMInstructionDescription() {
     final ModRMGroup modRMGroup = description().modRMGroup();
     if (null == modRMGroup) {
       return null;
     }
     return modRMGroup.getDescription(modRMGroupOpcode());
   }
 
   protected final void addParameter(X86Parameter parameter, ArgumentRange argumentRange) {
     addOperand(parameter);
     parameter.setArgumentRange(argumentRange);
   }
 
   protected final void addParameter(X86Parameter parameter, ArgumentRange argumentRange, TestArgumentExclusion testArgumentExclusion) {
     addParameter(parameter, argumentRange);
     parameter.excludeTestArguments(testArgumentExclusion);
   }
 
   protected final <EnumerableArgument_Type extends Enum<EnumerableArgument_Type> & EnumerableArgument> X86Parameter addEnumerableParameter(Designation designation, ParameterPlace parameterPlace,
                                                                                                                                            final SymbolSet<EnumerableArgument_Type> enumerator) {
     return addOperand(new X86EnumerableParameter<EnumerableArgument_Type>(designation, parameterPlace, enumerator));
   }
 
   protected final <Operand_Type extends X86Operand> Operand_Type addOperand(Operand_Type operand) {
     if (operand instanceof X86ImplicitOperand) {
       _implicitOperands.add((X86ImplicitOperand) operand);
     } else if (operand instanceof X86Parameter) {
       _parameters.add((X86Parameter) operand);
       if (operand instanceof X86AddressParameter) {
         useNamePrefix("m_");
       }
     }
     _operands.add(operand);
     return operand;
   }
 
   public final List<X86ImplicitOperand> implicitOperands() {
     return _implicitOperands;
   }
 
   @Override
   public final List<? extends X86Operand> operands() {
     return _operands;
   }
 
   @Override
   public final List<? extends X86Parameter> parameters() {
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
 
   private void setExternalOperandTypeSuffix(String suffix) {
     checkSuffix(suffix, _externalOperandTypeSuffix);
     _externalOperandTypeSuffix = suffix;
   }
 
   protected final void setExternalOperandTypeSuffix(OperandTypeCode operandTypeCode) throws TemplateNotNeededException {
     setExternalOperandTypeSuffix(getOperandTypeSuffix(operandTypeCode));
   }
 
   protected final void setOperandTypeSuffix(String suffix) {
     setExternalOperandTypeSuffix(suffix);
     checkSuffix(suffix, _internalOperandTypeSuffix);
     _internalOperandTypeSuffix = suffix;
   }
 
   public final void visitOperandTypeCode(OperandTypeCode operandTypeCode) throws TemplateNotNeededException {
     setOperandTypeSuffix(getOperandTypeSuffix(operandTypeCode));
   }
 
   public final void visitGeneralRegister(GeneralRegister generalRegister, Designation designation, ExternalPresence externalPresence) {
     addOperand(new X86ImplicitOperand(designation, externalPresence, generalRegister));
   }
 
   public final void visitSegmentRegister(SegmentRegister segmentRegister, Designation designation) {
     addOperand(new X86ImplicitOperand(designation, ExternalPresence.EXPLICIT, segmentRegister));
   }
 
   public final void visitModRMGroup(ModRMGroup modRMGroup) throws TemplateNotNeededException {
     final ModRMDescription instructionDescription = modRMGroup.getDescription(context().modRMGroupOpcode());
     if (instructionDescription == null) throw new TemplateNotNeededException();
     setInternalName(instructionDescription.name());
   }
 
   public final void visitModCase(ModCase modCase) throws TemplateNotNeededException {
     if (_context.modCase() != ModCase.MOD_3) {
       throw new TemplateNotNeededException();
     }
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
     addOperand(new X86ImplicitOperand(designation, ExternalPresence.EXPLICIT, fpStackRegister));
   }
 
   public final void visitInteger(Integer integer, Designation designation) {
     addOperand(new X86ImplicitOperand(designation, ExternalPresence.EXPLICIT, new Immediate8Argument((byte) integer.intValue())));
   }
 
   /**
    * @param other another template to compare against
    * @return whether both templates have the same name and operands and thus
    *         are assumed to implement the same machine instruction semantics,
    *         though potentially denoting different machine codes
    */
   public final boolean isRedundant(X86Template<?> other) {
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
