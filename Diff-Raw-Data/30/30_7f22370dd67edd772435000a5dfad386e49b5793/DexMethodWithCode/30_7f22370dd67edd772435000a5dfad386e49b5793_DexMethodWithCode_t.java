 package uk.ac.cam.db538.dexter.dex.method;
 
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.ClassDataItem.EncodedMethod;
 import org.jf.dexlib.CodeItem;
 import org.jf.dexlib.CodeItem.EncodedCatchHandler;
 import org.jf.dexlib.CodeItem.TryItem;
 import org.jf.dexlib.DebugInfoItem;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Util.AccessFlags;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.GraphColoring;
 import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
 import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
 
 public abstract class DexMethodWithCode extends DexMethod {
 
   @Getter private DexCode code;
   @Getter private final boolean direct;
 
   private final NoDuplicatesList<DexRegister> parameterRegisters;
   private final DexCode parameterMoveInstructions;
  private final Set<DexRegister> parameterMappedRegisters;
 
   public DexMethodWithCode(DexClass parent, String name, Set<AccessFlags> accessFlags,
                            DexPrototype prototype, DexCode code,
                            boolean direct) {
     super(parent, name, accessFlags, prototype);
     this.code = code;
     this.direct = direct;
     this.parameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
     this.parameterMoveInstructions = new DexCode();
    this.parameterMappedRegisters = new HashSet<DexRegister>();
   }
 
   public DexMethodWithCode(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, InstructionParsingException {
     super(parent, methodInfo);
     if (methodInfo.codeItem == null)
       code = new DexCode();
     else
       code = new DexCode(methodInfo.codeItem, parent.getParentFile().getParsingCache());
     direct = methodInfo.isDirect();
     parameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
     parameterMoveInstructions = new DexCode();
    parameterMappedRegisters = new HashSet<DexRegister>();
 
     val prototype = this.getPrototype();
     val isStatic = this.isStatic();
     val clazz = this.getParentClass();
 
     // create the parameter-register mappings
     val regCount = methodInfo.codeItem.getRegisterCount();
     val paramCount = prototype.getParameterCount(isStatic);
     for (int i = 0; i < paramCount; ++i) {
       val paramRegId = prototype.getParameterRegisterId(i, regCount, isStatic, clazz);
       val paramType = prototype.getParameterType(i, isStatic, clazz);
       switch (paramType.getTypeSize()) {
       case SINGLE:
        val regSingle = code.getRegisterByOriginalNumber(paramRegId);
        addParameterMapping_Single(i, regSingle);
        parameterMappedRegisters.add(regSingle);
         break;
       case WIDE:
        val regWide1 = code.getRegisterByOriginalNumber(paramRegId);
        val regWide2 = code.getRegisterByOriginalNumber(paramRegId + 1);
        addParameterMapping_Wide(i, regWide1, regWide2);
        parameterMappedRegisters.add(regWide1);
        parameterMappedRegisters.add(regWide2);
         break;
       }
     }
   }
 
   public void addParameterMapping_Single(int paramIndex, DexRegister codeReg) {
     val paramType = this.getPrototype().getParameterType(paramIndex, this.isStatic(), this.getParentClass());
     val paramReg = parameterRegisters.get(paramIndex);
     parameterMoveInstructions.add(new DexInstruction_Move(code, codeReg, paramReg, paramType instanceof DexReferenceType));
   }
 
   public void addParameterMapping_Wide(int paramIndex, DexRegister codeReg1, DexRegister codeReg2) {
     val paramReg1 = parameterRegisters.get(paramIndex);
     val paramReg2 = parameterRegisters.get(paramIndex + 1);
 
     parameterMoveInstructions.add(new DexInstruction_MoveWide(code, codeReg1, codeReg2, paramReg1, paramReg2));
   }
 
   @Override
   public boolean isVirtual() {
     return !direct;
   }
 
   @Override
   public void instrument() {
     code = code.instrument();
   }
 
   @Override
   protected CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache) {
     // do register allocation
     // note that this changes the code itself
     // (adds temporaries, inserts move instructions)
     val codeColoring = new GraphColoring(code);
     val modifiedCode = codeColoring.getModifiedCode();
 
     // add parameter registers to the register allocation
     val registerAllocation = new HashMap<DexRegister, Integer>(codeColoring.getColoring());
     int registerCount = codeColoring.getColorsUsed();
     val inWords = this.getPrototype().countParamWords(this.isStatic());
     if (registerCount >= inWords) {
       int startReg = registerCount - inWords;
       for (int i = 0; i < inWords; ++i)
         registerAllocation.put(parameterRegisters.get(i), startReg + i);
     } else {
       for (int i = 0; i < inWords; ++i)
         registerAllocation.put(parameterRegisters.get(i), i);
       registerCount = inWords;
     }
 
    // sometimes a register is not used in the code
    // and thus would not get allocated...
    // but if it's mapped to a parameter, assembling
    // the move instruction would fail...
    // so add these into the register allocation...
    // the color doesn't matter
    for (val reg : parameterMappedRegisters)
      if (!registerAllocation.containsKey(reg))
        registerAllocation.put(reg, 0);

     val assembledMoveInstructions = parameterMoveInstructions.assembleBytecode(registerAllocation, cache, 0);
     val assembledCode = modifiedCode.assembleBytecode(registerAllocation, cache, assembledMoveInstructions.getTotalCodeLength());
 
     List<Instruction> instructions = new ArrayList<Instruction>();
     instructions.addAll(assembledMoveInstructions.getInstructions());
     instructions.addAll(assembledCode.getInstructions());
 
     List<TryItem> tries = new ArrayList<TryItem>();
     tries.addAll(assembledMoveInstructions.getTries());
     tries.addAll(assembledCode.getTries());
 
     List<EncodedCatchHandler> catchHandlers = new ArrayList<EncodedCatchHandler>();
     catchHandlers.addAll(assembledMoveInstructions.getCatchHandlers());
     catchHandlers.addAll(assembledCode.getCatchHandlers());
 
     int outWords = modifiedCode.getOutWords();
 
     DebugInfoItem debugInfo = null;
 
     return CodeItem.internCodeItem(outFile, registerCount, inWords, outWords, debugInfo, instructions, tries, catchHandlers);
   }
 }
