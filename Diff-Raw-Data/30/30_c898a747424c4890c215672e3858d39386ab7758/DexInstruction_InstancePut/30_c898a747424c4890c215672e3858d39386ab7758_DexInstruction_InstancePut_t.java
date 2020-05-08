 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.FieldIdItem;
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Format.Instruction22c;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
 import uk.ac.cam.db538.dexter.dex.DexField;
 import uk.ac.cam.db538.dexter.dex.DexUtils;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetObjectTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_SetObjectTaint;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
 
 public class DexInstruction_InstancePut extends DexInstruction {
 
   @Getter private final DexRegister regFrom;
   @Getter private final DexRegister regObject;
   @Getter private final DexClassType fieldClass;
   @Getter private final DexRegisterType fieldType;
   @Getter private final String fieldName;
   @Getter private final Opcode_GetPut opcode;
 
   public DexInstruction_InstancePut(DexCode methodCode, DexRegister from, DexRegister obj, DexClassType fieldClass, DexRegisterType fieldType, String fieldName, Opcode_GetPut opcode) {
     super(methodCode);
 
     this.regFrom = from;
     this.regObject = obj;
     this.fieldClass = fieldClass;
     this.fieldType = fieldType;
     this.fieldName = fieldName;
     this.opcode = opcode;
 
     Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
   }
 
   public DexInstruction_InstancePut(DexCode methodCode, DexRegister from, DexRegister obj, DexField field) {
     super(methodCode);
 
     if (field.isStatic())
       throw new InstructionArgumentException("Expected instance field");
 
     this.regFrom = from;
     this.regObject = obj;
     this.fieldClass = field.getParentClass().getType();
     this.fieldType = field.getType();
     this.fieldName = field.getName();
     this.opcode = Opcode_GetPut.getOpcodeFromType(field.getType());
   }
 
   public DexInstruction_InstancePut(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
     super(methodCode);
 
     if (insn instanceof Instruction22c && Opcode_GetPut.convert_IPUT(insn.opcode) != null) {
 
       val insnStaticPut = (Instruction22c) insn;
       val refItem = (FieldIdItem) insnStaticPut.getReferencedItem();
       regFrom = parsingState.getRegister(insnStaticPut.getRegisterA());
       regObject = parsingState.getRegister(insnStaticPut.getRegisterB());
       fieldClass = DexClassType.parse(
                      refItem.getContainingClass().getTypeDescriptor(),
                      parsingState.getCache());
       fieldType = DexRegisterType.parse(
                     refItem.getFieldType().getTypeDescriptor(),
                     parsingState.getCache());
       fieldName = refItem.getFieldName().getStringValue();
       opcode = Opcode_GetPut.convert_IPUT(insn.opcode);
 
     } else
       throw FORMAT_EXCEPTION;
 
     Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
   }
 
   @Override
   public String getOriginalAssembly() {
     return "iput-" + opcode.getAssemblyName() + " v" + regFrom.getOriginalIndexString() + ", {v" + regObject.getOriginalIndexString() + "}" + fieldClass.getPrettyName() + "." + fieldName;
   }
 
   @Override
   public Set<DexRegister> lvaReferencedRegisters() {
     return createSet(regFrom, regObject);
   }
 
   @Override
   public Set<GcRangeConstraint> gcRangeConstraints() {
     return createSet(
              new GcRangeConstraint(regFrom, ColorRange.RANGE_4BIT),
              new GcRangeConstraint(regObject, ColorRange.RANGE_4BIT));
   }
 
   @Override
   protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
     return new DexInstruction_InstancePut(getMethodCode(), mapping.get(regFrom), mapping.get(regObject), fieldClass, fieldType, fieldName, opcode);
   }
 
   @Override
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     val regAlloc = state.getRegisterAllocation();
     int rFrom = regAlloc.get(regFrom);
     int rObject = regAlloc.get(regObject);
 
     if (fitsIntoBits_Unsigned(rFrom, 4) && fitsIntoBits_Unsigned(rObject, 4)) {
       return new Instruction[] {
                new Instruction22c(Opcode_GetPut.convert_IPUT(opcode), (byte) rFrom, (byte) rObject, state.getCache().getField(fieldClass, fieldType, fieldName))
              };
     } else
       return throwNoSuitableFormatFound();
   }
 
   @Override
   public void instrument(DexCode_InstrumentationState state) {
     val code = getMethodCode();
     val classHierarchy = getParentFile().getClassHierarchy();
 
    val fieldDeclaringClass = classHierarchy.getAccessedFieldDeclaringClass(fieldClass, fieldName, fieldType, false);
 
     if (opcode != Opcode_GetPut.Object) {
       val regValueTaint = state.getTaintRegister(regFrom);
 
       if (fieldDeclaringClass.isDefinedInternally()) {
         // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
         // store the taint to the taint field
         val field = DexUtils.getField(getParentFile(), fieldDeclaringClass, fieldName, fieldType);
         code.replace(this,
                      new DexCodeElement[] {
                        this,
                        new DexInstruction_InstancePut(code, regValueTaint, regObject, state.getCache().getTaintField(field)),
                      });
 
       } else
         // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
         // assign the same taint to the object
         code.replace(this,
                      new DexCodeElement[] {
                        this,
                        new DexPseudoinstruction_SetObjectTaint(code, regObject, regValueTaint)
                      });
 
     } else {
       if (!fieldDeclaringClass.isDefinedInternally()) {
         // FIELD OF REFERENCE TYPE DEFINED EXTERNALLY
         // need to copy the taint of the field value to the containing object
         val regValueTaint = new DexRegister();
         code.replace(this,
                      new DexCodeElement[] {
                        this,
                        new DexPseudoinstruction_GetObjectTaint(code, regValueTaint, regFrom),
                        new DexPseudoinstruction_SetObjectTaint(code, regObject, regValueTaint)
                      });
       }
 
       // FIELD OF REFERENCE TYPE DEFINED INTERNALLY
       // no need to do anything
     }
   }
 }
