 package edu.scu.jjni.aotc;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import cn.edu.sjtu.jllvm.VMCore.Module;
 import cn.edu.sjtu.jllvm.VMCore.Constants.Constant;
 import cn.edu.sjtu.jllvm.VMCore.Constants.LocalVariable;
 import cn.edu.sjtu.jllvm.VMCore.Instructions.Instruction;
 import cn.edu.sjtu.jllvm.VMCore.Operators.InstType;
 import cn.edu.sjtu.jllvm.VMCore.Types.Type;
 import cn.edu.sjtu.jllvm.VMCore.Types.TypeFactory;
 import edu.scu.llvm.asm.InstFactory;
 import edu.scu.llvm.translate.FunctionConverter;
 import edu.scu.llvm.translate.VariableMapper;
 import edu.scu.llvm.translate.VariableMapper.Operator;
 
 public class LLVM2Jni extends FunctionConverter {
 
 	private static final String PREAMBLE = "dat/jnipreamble.ll";
 	
 	public LLVM2Jni(Module _module, String[] _functionNames) throws Exception {
 		super(_module, _functionNames, getJNIPreamble(), getJNIMapper(), getIgnoreCalls(),
 				getIgnoreFAttr());		
 	}
 
 	static private String JNIPreamble;
 	static private String getJNIPreamble() throws Exception {
 		String content = "";
 		try {
 			content = new Scanner(new File(PREAMBLE)).useDelimiter("\\Z").next();
 		} catch (FileNotFoundException e) {
 			System.err.println("Cannot open JNI preamble file: " + PREAMBLE);
 			throw e;
 		}
 		return content;
 	}
 	
 	protected static void addStructTransform(VariableMapper mapper) {
 		InstFactory fac = new InstFactory();
 		
 		// Initialize the mapper with rich type support
 		Type javaStructBase = new Type(Type.StructTyID, "%\"struct.*\""); // TODO handle the wild card
 		Type javaStructPtr = TypeFactory.getPointerType(javaStructBase);		
 		
 		VariableMapper.OpRecognizer elemOpRec = new VariableMapper.OpRecognizer(VariableMapper.Opcode.GET_STRUCT_ELEM,
 				javaStructPtr, null);
 		
 		int vnum = 0;
 		
 		Constant var0 = new LocalVariable(VariableMapper.OpRecognizer.getTmpName(vnum++));
 		Instruction ins = fac.createOperationInst(var0, 0, null, null, "bitcast");
 		
 		elemOpRec.addInstruction(ins);
 	}
 
 	/*
 	protected static void addArrayTransform(VariableMapper mapper) {		
 		// Initialize the mapper with rich type support
 		Type javaArrayIntBase = new Type(Type.StructTyID, "%\"struct.int[]\"");
 		Type javaArrayInt = TypeFactory.getPointerType(javaArrayIntBase);
 		
 		Type jniArrayIntBase = TypeFactory.getInt8Type();
 		Type jniArrayInt = TypeFactory.getPointerType(jniArrayIntBase);			
 		
 		VariableMapper.TypeMap tmArrayInt = mapper.addGlobalTypeMap(javaArrayInt, jniArrayInt);		
 		
 		tmArrayInt.addOp(
 				new VariableMapper.Operator(VariableMapper.Opcode.READ) {
 					@Override
 					public List<Instruction> exec(Object... args) {
 						return null;
 					}
 				}
 				);
 	}
 	*/	
 	
 	private static VariableMapper getJNIMapper() {
 		VariableMapper mapper = new VariableMapper();
 		
		addStructTransform(mapper);
 		
 		return mapper;
 	}
 	
 	private static List<String> getIgnoreCalls() {
 		List<String> ignoreCalls = new ArrayList<String>();
 		ignoreCalls.add("@llvm.dbg.declare");
 		return ignoreCalls;
 	}
 	
 	private static List<String> getIgnoreFAttr() {
 		List<String> ignoreFAttr = new ArrayList<String>();
 		ignoreFAttr.add("uwtable");
 		return ignoreFAttr;
 	}
 }
