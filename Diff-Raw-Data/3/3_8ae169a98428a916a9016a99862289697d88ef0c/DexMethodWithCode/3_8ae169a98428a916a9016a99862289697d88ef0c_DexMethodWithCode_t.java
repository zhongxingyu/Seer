 package uk.ac.cam.db538.dexter.dex.method;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.AnnotationSetItem;
 import org.jf.dexlib.AnnotationVisibility;
 import org.jf.dexlib.ClassDataItem.EncodedMethod;
 import org.jf.dexlib.CodeItem;
 import org.jf.dexlib.DebugInfoItem;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.Util.AccessFlags;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.GraphColoring;
 import uk.ac.cam.db538.dexter.dex.DexAnnotation;
 import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
 
 public abstract class DexMethodWithCode extends DexMethod {
 
   @Getter protected DexCode code;
   @Getter private final boolean direct;
 
   private final NoDuplicatesList<DexRegister> parameterRegisters;
   private final Map<DexRegister, DexRegister> parameterRegistersMappings;
 
   public DexMethodWithCode(DexClass parent, String name, Set<AccessFlags> accessFlags,
                            DexPrototype prototype, DexCode code,
                            Set<DexAnnotation> annotations,
                            boolean direct) {
     super(parent, name, accessFlags, prototype, annotations);
     this.code = code;
     this.direct = direct;
     this.parameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
     this.parameterRegistersMappings = new HashMap<DexRegister, DexRegister>();
 
     if (this.code != null)
       this.code.setParentMethod(this);
   }
 
   public DexMethodWithCode(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations, boolean parseInstructions) {
     super(parent, methodInfo, encodedAnnotations);
     this.direct = methodInfo.isDirect();
     this.parameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
     this.parameterRegistersMappings = new HashMap<DexRegister, DexRegister>();
 
     if (parseInstructions && methodInfo.codeItem != null) {
       this.code = new DexCode(methodInfo.codeItem, this, parent.getParentFile().getParsingCache());
 
       val prototype = this.getPrototype();
       val isStatic = this.isStatic();
       val clazz = this.getParentClass();
 
       // create the parameter-register mappings
       val regCount = methodInfo.codeItem.getRegisterCount();
       val paramCount = prototype.getParameterCount(isStatic);
       for (int i = 0; i < paramCount; ++i) {
         val paramRegId = prototype.getParameterRegisterId(i, regCount, isStatic);
         val paramType = prototype.getParameterType(i, isStatic, clazz);
         switch (paramType.getTypeSize()) {
         case SINGLE:
           val regSingle = code.getRegisterByOriginalNumber(paramRegId);
           addParameterMapping_Single(i, regSingle);
           break;
         case WIDE:
           val regWide1 = code.getRegisterByOriginalNumber(paramRegId);
           val regWide2 = code.getRegisterByOriginalNumber(paramRegId + 1);
           addParameterMapping_Wide(i, regWide1, regWide2);
           break;
         }
       }
     } else
       this.code = null;
   }
 
   private void addParameterMapping_Single(int paramIndex, DexRegister codeReg) {
 //    if (!code.getUsedRegisters().contains(codeReg))
 //      return;
 
     val paramType = this.getPrototype().getParameterType(paramIndex, this.isStatic(), this.getParentClass());
 
     val regIndex = this.getPrototype().getFirstParameterRegisterIndex(paramIndex, isStatic());
     val paramReg = parameterRegisters.get(regIndex);
 
     val moveInsn = new DexInstruction_Move(code, codeReg, paramReg, paramType instanceof DexReferenceType);
     moveInsn.setAuxiliaryElement(true);
     code.insertBefore(moveInsn, code.getStartingLabel());
 
     if (parameterRegistersMappings.containsKey(paramReg))
       throw new RuntimeException("Multiple mappings of the same parameter");
     else
       parameterRegistersMappings.put(paramReg, codeReg);
   }
 
   private void addParameterMapping_Wide(int paramIndex, DexRegister codeReg1, DexRegister codeReg2) {
 //    if (!code.getUsedRegisters().contains(codeReg1) && !code.getUsedRegisters().contains(codeReg2))
 //      return;
 
     val firstRegIndex = this.getPrototype().getFirstParameterRegisterIndex(paramIndex, isStatic());
 
     val paramReg1 = parameterRegisters.get(firstRegIndex);
     val paramReg2 = parameterRegisters.get(firstRegIndex + 1);
 
     val moveInsn = new DexInstruction_MoveWide(code, codeReg1, codeReg2, paramReg1, paramReg2);
     moveInsn.setAuxiliaryElement(true);
     code.insertBefore(moveInsn, code.getStartingLabel());
 
     if (parameterRegistersMappings.containsKey(paramReg1) || parameterRegistersMappings.containsKey(paramReg2))
       throw new RuntimeException("Multiple mappings of the same parameter");
     else {
       parameterRegistersMappings.put(paramReg1, codeReg1);
       parameterRegistersMappings.put(paramReg2, codeReg2);
     }
   }
 
   public List<DexRegister> getParameterMappedRegisters() {
     val list = new ArrayList<DexRegister>(parameterRegisters.size());
 
     for (val paramReg : parameterRegisters) {
       val codeReg = parameterRegistersMappings.get(paramReg);
 //		  if (codeReg == null)
 //			  throw new RuntimeException("Missing parameter register mapping (" + getParentClass().getType().getPrettyName() + "." + getName() + ")");
       list.add(codeReg);
     }
 
     return list;
   }
 
   @Override
   public boolean isVirtual() {
     return !direct;
   }
 
   @Override
   public void instrument(DexInstrumentationCache cache) {
     if (code != null)
       code.instrument(cache);
 
     if (isVirtual())
       this.addAnnotation(
         new DexAnnotation(getParentFile().getInternalMethodAnnotation_Type(),
                           AnnotationVisibility.RUNTIME));
   }
 
   @Override
   protected CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache) {
	  if (code == null)
		  return null;
	  
     // do register allocation
     // note that this changes the code itself
     // (adds temporaries, inserts move instructions)
     val codeColoring = new GraphColoring(code); // changes the code itself (potentially)
 
     // add parameter registers to the register allocation
     val registerAllocation = new HashMap<DexRegister, Integer>(codeColoring.getColoring());
     int registerCount = codeColoring.getColorsUsed();
     val inWords = this.getPrototype().countParamWords(this.isStatic());
 //    if (registerCount >= inWords) {
 //      int startReg = registerCount - inWords;
 //      for (int i = 0; i < inWords; ++i)
 //        registerAllocation.put(parameterRegisters.get(i), startReg + i);
 //    } else {
 //      for (int i = 0; i < inWords; ++i)
 //        registerAllocation.put(parameterRegisters.get(i), i);
 //      registerCount = inWords;
 //    }
     if (registerCount + inWords >= (1 << 16))
       throw new RuntimeException("Cannot allocate paramter registers");
     for (int i = 0; i < inWords; ++i)
       registerAllocation.put(parameterRegisters.get(i), registerCount++);
 
     // sometimes a register is not used in the code
     // and thus would not get allocated...
     // but if it's mapped to a parameter, assembling
     // the move instruction would fail...
     // so add these into the register allocation...
     // the color doesn't matter
     for (val reg : parameterRegistersMappings.values())
       if (!registerAllocation.containsKey(reg))
         registerAllocation.put(reg, 0);
 
 //    val assembledMoveInstructions = parameterMoveInstructions.assembleBytecode(registerAllocation, cache, 0);
 //    val assembledCode = code.assembleBytecode(registerAllocation, cache, assembledMoveInstructions.getTotalCodeLength());
     val assembledCode = code.assembleBytecode(registerAllocation, cache, 0);
 
 //    List<Instruction> instructions = new ArrayList<Instruction>();
 //    instructions.addAll(assembledMoveInstructions.getInstructions());
 //    instructions.addAll(assembledCode.getInstructions());
     val instructions = assembledCode.getInstructions();
 
 //    List<TryItem> tries = new ArrayList<TryItem>();
 //    tries.addAll(assembledMoveInstructions.getTries());
 //    tries.addAll(assembledCode.getTries());
     val tries = assembledCode.getTries();
 
 //    List<EncodedCatchHandler> catchHandlers = new ArrayList<EncodedCatchHandler>();
 //    catchHandlers.addAll(assembledMoveInstructions.getCatchHandlers());
 //    catchHandlers.addAll(assembledCode.getCatchHandlers());
     val catchHandlers = assembledCode.getCatchHandlers();
 
     int outWords = code.getOutWords();
 
     DebugInfoItem debugInfo = null;
 
     return CodeItem.internCodeItem(outFile, registerCount, inWords, outWords, debugInfo, instructions, tries, catchHandlers);
   }
 
   @Override
   public void markMethodOriginal() {
     if (code != null)
       code.markAllInstructionsOriginal();
   }
 }
