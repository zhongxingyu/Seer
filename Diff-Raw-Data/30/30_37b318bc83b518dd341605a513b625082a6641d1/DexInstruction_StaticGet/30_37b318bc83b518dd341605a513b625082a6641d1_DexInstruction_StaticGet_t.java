 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.FieldIdItem;
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Format.Instruction21c;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
 import uk.ac.cam.db538.dexter.dex.DexField;
 import uk.ac.cam.db538.dexter.dex.DexUtils;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
 
 public class DexInstruction_StaticGet extends DexInstruction {
 
   @Getter private final DexRegister regTo;
   @Getter private final DexClassType fieldClass;
   @Getter private final DexRegisterType fieldType;
   @Getter private final String fieldName;
   @Getter private final Opcode_GetPut opcode;
 
   public DexInstruction_StaticGet(DexCode methodCode, DexRegister to, DexClassType fieldClass, DexRegisterType fieldType, String fieldName, Opcode_GetPut opcode) {
     super(methodCode);
 
     this.regTo = to;
     this.fieldClass = fieldClass;
     this.fieldType = fieldType;
     this.fieldName = fieldName;
     this.opcode = opcode;
 
     Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
   }
 
   public DexInstruction_StaticGet(DexCode methodCode, DexRegister to, DexField field) {
     super(methodCode);
 
     if (!field.isStatic())
       throw new InstructionArgumentException("Expected static field");
 
     this.regTo = to;
     this.fieldClass = field.getParentClass().getType();
     this.fieldType = field.getType();
     this.fieldName = field.getName();
     this.opcode = Opcode_GetPut.getOpcodeFromType(field.getType());
   }
 
   public DexInstruction_StaticGet(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
     super(methodCode);
 
     if (insn instanceof Instruction21c && Opcode_GetPut.convert_SGET(insn.opcode) != null) {
 
       val insnStaticGet = (Instruction21c) insn;
       val refItem = (FieldIdItem) insnStaticGet.getReferencedItem();
       regTo = parsingState.getRegister(insnStaticGet.getRegisterA());
       fieldClass = DexClassType.parse(
                      refItem.getContainingClass().getTypeDescriptor(),
                      parsingState.getCache());
       fieldType = DexRegisterType.parse(
                     refItem.getFieldType().getTypeDescriptor(),
                     parsingState.getCache());
       fieldName = refItem.getFieldName().getStringValue();
       opcode = Opcode_GetPut.convert_SGET(insn.opcode);
 
     } else
       throw FORMAT_EXCEPTION;
 
     Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
   }
 
   @Override
   public String getOriginalAssembly() {
     return "sget-" + opcode.getAssemblyName() + " v" + regTo.getOriginalIndexString() + ", " + fieldClass.getPrettyName() + "." + fieldName;
   }
 
   @Override
   public Set<DexRegister> lvaDefinedRegisters() {
     return createSet(regTo);
   }
 
   @Override
   public Set<GcRangeConstraint> gcRangeConstraints() {
     return createSet(new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT));
   }
 
   @Override
   protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
     return new DexInstruction_StaticGet(getMethodCode(), mapping.get(regTo), fieldClass, fieldType, fieldName, opcode);
   }
 
   @Override
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     int rTo = state.getRegisterAllocation().get(regTo);
 
     if (fitsIntoBits_Unsigned(rTo, 8)) {
       return new Instruction[] {
                new Instruction21c(Opcode_GetPut.convert_SGET(opcode), (short) rTo, state.getCache().getField(fieldClass, fieldType, fieldName))
              };
     } else
       return throwNoSuitableFormatFound();
   }
 
   @Override
   public void instrument(DexCode_InstrumentationState state) {
     val code = getMethodCode();
     val classHierarchy = getParentFile().getClassHierarchy();
 
     if (opcode != Opcode_GetPut.Object) {
       val regValueTaint = state.getTaintRegister(regTo);
      val fieldDeclaringClass = classHierarchy.getAccessedFieldDeclaringClass(fieldClass, fieldName, fieldType, true);
 
       if (fieldDeclaringClass.isDefinedInternally()) {
         // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
         // retrieve taint from the adjoined field
         val field = DexUtils.getField(getParentFile(), fieldDeclaringClass, fieldName, fieldType);
 
         code.replace(this,
                      new DexCodeElement[] {
                        this,
                        new DexInstruction_StaticGet(code, regValueTaint, state.getCache().getTaintField(field))
                      });
 
       } else {
         // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
         // need to figure out!!!
         super.instrument(state);
       }
     } else {
       // FIELD OF REFERENCE TYPE
       // the object itself has taint, no need to do anything
     }
   }
 }
