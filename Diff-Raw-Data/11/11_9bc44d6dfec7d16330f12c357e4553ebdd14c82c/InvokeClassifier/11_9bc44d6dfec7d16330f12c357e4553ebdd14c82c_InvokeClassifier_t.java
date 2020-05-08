 package uk.ac.cam.db538.dexter.transform.taint;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition.CallDestinationType;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 import com.rx201.dx.translator.DexCodeAnalyzer;
 import com.rx201.dx.translator.RopType.Category;
 
 public class InvokeClassifier {
 
 	private InvokeClassifier() { }
 	
 	public static Pair<DexCode, ? extends Map<DexInstruction_Invoke, CallDestinationType>> classifyMethodCalls(DexCode code) {
 		DexCodeAnalyzer analyzedCode = new DexCodeAnalyzer(code);
 		analyzedCode.analyze();
 		
 		val classification = new HashMap<DexInstruction_Invoke, CallDestinationType>();
 		
 		// analyze each invoke instruction
 		
 		for (val insn : code.getInstructionList()) {
 			if (insn instanceof DexInstruction_Invoke) {
 				val invokeInsn = (DexInstruction_Invoke) insn;
				
				if (invokeInsn.getMethodId().getName().equals("getDescription"))
					System.console();
				
 				DexReferenceType calledClassType = invokeInsn.getClassType();
 				Opcode_Invoke calledOpcode = invokeInsn.getCallType();
 				
 				// for -virtual and -interface calls, use DexCodeAnalyzer to
 				// more precisely determine the type of the object the instruction
 				// is invoked on
 				
 				if (calledOpcode == Opcode_Invoke.Virtual || calledOpcode == Opcode_Invoke.Interface) {
 					val analyzedInsn = analyzedCode.reverseLookup(invokeInsn);
 					val thisArgReg = invokeInsn.getArgumentRegisters().get(0);
 					val calledClassRopType = analyzedInsn.getUsedRegisterType(thisArgReg);
 					if (calledClassRopType.category == Category.Reference)
 						calledClassType = calledClassRopType.type;
 				}
 				
 				val calledClassDef = code.getHierarchy().getBaseClassDefinition(calledClassType);
 				val destType = calledClassDef.getMethodDestinationType(invokeInsn.getMethodId(), calledOpcode);
 				
 				if (destType == CallDestinationType.Undecidable) {
 					// TODO: replace instruction with a macro that will
 					// check the destination type dynamically
 					// and add the invoke in each branch as external/internal
 					throw new UnsupportedOperationException("Undecidable calls need to be handled!!!");
 				}
 				
 				classification.put(invokeInsn, destType);
 			}
 		}
 		
 		return Pair.create(code, classification);
 	}
 }
