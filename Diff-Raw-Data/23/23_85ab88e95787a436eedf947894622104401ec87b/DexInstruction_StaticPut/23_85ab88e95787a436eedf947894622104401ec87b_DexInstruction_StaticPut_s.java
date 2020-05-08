 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.FieldIdItem;
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Format.Instruction21c;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
 import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
 
 public class DexInstruction_StaticPut extends DexInstruction {
 
   @Getter private final DexRegister regTo;
   @Getter private final DexClassType fieldClass;
   @Getter private final DexRegisterType fieldType;
   @Getter private final String fieldName;
   @Getter private final Opcode_GetPut opcode;
 
   public DexInstruction_StaticPut(DexCode methodCode, DexRegister to, DexClassType fieldClass, DexRegisterType fieldType, String fieldName, Opcode_GetPut opcode) {
     super(methodCode);
 
     this.regTo = to;
     this.fieldClass = fieldClass;
     this.fieldType = fieldType;
     this.fieldName = fieldName;
     this.opcode = opcode;
 
     Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
   }
 
   public DexInstruction_StaticPut(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
     super(methodCode);
 
     if (insn instanceof Instruction21c && Opcode_GetPut.convert_SPUT(insn.opcode) != null) {
 
       val insnStaticPut = (Instruction21c) insn;
       val refItem = (FieldIdItem) insnStaticPut.getReferencedItem();
       regTo = parsingState.getRegister(insnStaticPut.getRegisterA());
       fieldClass = DexClassType.parse(
                      refItem.getContainingClass().getTypeDescriptor(),
                      parsingState.getCache());
       fieldType = DexRegisterType.parse(
                     refItem.getFieldType().getTypeDescriptor(),
                     parsingState.getCache());
       fieldName = refItem.getFieldName().getStringValue();
       opcode = Opcode_GetPut.convert_SPUT(insn.opcode);
 
     } else
       throw new InstructionParsingException("Unknown instruction format or opcode");
 
     Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
   }
 
   @Override
   public String getOriginalAssembly() {
     return "sput-" + opcode.getAssemblyName() + " v" + regTo.getOriginalIndexString() + ", " + fieldClass.getPrettyName() + "." + fieldName;
   }
 
   @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    val definedRegs = new HashSet<DexRegister>();
    definedRegs.add(regTo);
    return definedRegs;
   }
 
   @Override
   public Set<GcRangeConstraint> gcRangeConstraints() {
     val constraints = new HashSet<GcRangeConstraint>();
     constraints.add(new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT));
     return constraints;
   }
 
   @Override
   protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
     return new DexInstruction_StaticPut(getMethodCode(), mapping.get(regTo), fieldClass, fieldType, fieldName, opcode);
   }
 
   @Override
   public Instruction[] assembleBytecode(Map<DexRegister, Integer> regAlloc, DexAssemblingCache cache) {
     int rTo = regAlloc.get(regTo);
 
     if (fitsIntoBits_Unsigned(rTo, 8)) {
       return new Instruction[] {
                new Instruction21c(Opcode_GetPut.convert_SPUT(opcode), (short) rTo, cache.getField(fieldClass, fieldType, fieldName))
              };
     } else
       return throwCannotAssembleException("No suitable instruction format found");
   }
 }
