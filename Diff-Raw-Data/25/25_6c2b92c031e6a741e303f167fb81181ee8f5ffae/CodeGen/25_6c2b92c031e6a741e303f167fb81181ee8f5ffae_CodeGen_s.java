 package com.hexcore.cas.rulesystems;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 
 import com.hexcore.cas.rulesystems.Parser.AddOpE;
 import com.hexcore.cas.rulesystems.Parser.MulOpE;
 import com.hexcore.cas.rulesystems.Parser.RelOpE;
 
 /**
  * Class CodeGen
 
  * @authors Karl Zoller
  */
 
 public class CodeGen implements org.objectweb.asm.Opcodes
 {
 	/////////////////////////////////////////////
 	/// Public Variables
 	public static ArrayList<byte[]>		code = new ArrayList<byte[]>();
 	public static ClassWriter			cw;
 	public static String				name;
 	
 	/////////////////////////////////////////////
 	/// Private Variables
 	private static ArrayList<String>	properties;
 	private static boolean				debugEnabled = true;
 	private static int					currentFrameworkIndex = 0;
 	private static int					numProperties = 1;
 	private static int					propertyIndex = 1;
 	private static int					varIndex = 5;
 	private static Label				defaultLabel;
 	private static Label				executeBegin;
 	private static Label				executeEnd;
 	private static Label[]				frameworkLabels;
 	private static MethodVisitor		executeVisitor;
 	private static String				ruleSetName;
 	
 	
 	public static int declareLocalVariable(String name)
 	{
 		debug("Declare var index: " + varIndex);
 		//executeVisitor.visitLocalVariable(name, "D", null, executeBegin, executeEnd, varIndex);
 		int result = varIndex;
 		varIndex += 2;
 		return result;
 	}
 	
 	public static int declareLoopVariables()
 	{
 		debug("Declare loop var index: " + varIndex);
 		int result = varIndex;
 		varIndex += 6;
 		return result;
 	}
 	
 	public static int declareProperty(String name)
 	{
 		debug("Declare property index: " + propertyIndex);
 		properties.add(name);
 		numProperties++;
 		return propertyIndex++;
 	}
 	
 
 	
 	public static void derefArrayRef()
 	{
 		debug("Dereferencing array");
 		executeVisitor.visitInsn(D2I);
 		executeVisitor.visitInsn(AALOAD);
 	}
 	
 	public static void derefProperty(int index)
 	{
 		debug("Deref property: " + index);
 		//executeVisitor.visitVarInsn(ALOAD, 1);
 		executeVisitor.visitLdcInsn(new Integer(index));
 		executeVisitor.visitMethodInsn(INVOKEVIRTUAL, "com/hexcore/cas/model/Cell", "getValue", "(I)D");
 	}
 	
 	public static void derefRef(int index)
 	{
 		debug("Deref ref: " + index);
 		executeVisitor.visitVarInsn(ALOAD, index);
 	}
 	
 	public static void derefVariable(int index)
 	{
 		debug("Deref var: " + index);
 		executeVisitor.visitVarInsn(DLOAD, index);
 	}
 	
 	public static Label engineJump(int stepNum)
 	{
 		debug("N-Step Jump");
 		executeVisitor.visitFieldInsn(GETSTATIC, ruleSetName, "engineStep", "I");
 		loadConstantInteger(stepNum);
 		
 		Label nextStep = new Label();
 		
 		executeVisitor.visitJumpInsn(IF_ICMPNE, nextStep);
 		return nextStep;
 	}
 	
 	public static void endClass()
 	{
 		implementPropertyCountFunction();
 		implementPropertyListFunction();
 		debug("End class");
 		
 		
 		//End class
 		cw.visitEnd();
 	}
 	
 	public static void endExecute()
 	{
 		debug("End Execute");
 		executeVisitor.visitLabel(defaultLabel);
 		executeVisitor.visitInsn(RETURN);
 		executeEnd = new Label();
 		executeVisitor.visitLabel(executeEnd);
 		executeVisitor.visitLocalVariable("this", "Lcom/hexcore/cas/rulesystems/"+name+"Rule;", null, executeBegin, executeEnd, 0);
 		executeVisitor.visitLocalVariable("self", "Lcom/hexcore/cas/model/Cell;", null, executeBegin, executeEnd, 1);
 		executeVisitor.visitLocalVariable("neighbours", "[Lcom/hexcore/cas/model/Cell;", null, executeBegin, executeEnd, 2);
 		executeVisitor.visitLocalVariable("temp", "D", null, executeBegin, executeEnd, 3);
 		executeVisitor.visitMaxs(0, 0);
 		executeVisitor.visitEnd();
 	}
 	
 	public static void endLoop(Label[] labels, int index)
 	{
 		debug("Ending Loop index: " + index);
 		executeVisitor.visitVarInsn(DLOAD, index);
 		executeVisitor.visitLdcInsn(new Double(1.0));
 		executeVisitor.visitInsn(DADD);
 		executeVisitor.visitVarInsn(DSTORE, index);
 		executeVisitor.visitJumpInsn(GOTO, labels[0]);
 		executeVisitor.visitLabel(labels[1]);
 	}
 	
 	public static void endType()
 	{
 		debug("End Type");
 		executeVisitor.visitJumpInsn(GOTO, defaultLabel);
 	}
 	
 	public static void generatePropertyArray(int index)
 	{
 		debug("Generating property array.");
 		executeVisitor.visitLdcInsn(new Integer(index));
 		executeVisitor.visitMethodInsn(INVOKESTATIC, "com/hexcore/cas/rulesystems/StdLib", "generatePropertyArray", "([Lcom/hexcore/cas/model/Cell;I)[D");
 	}
 	
 	public static byte[] getCode()
 	{
 		return cw.toByteArray();
 	}
 	
 	public static ArrayList<String> getPropertyList()
 	{
 		return properties;
 	}
 	
 	
 	public static void implementPropertyCountFunction()
 	{
 		debug("Implement property count");
 		
 		MethodVisitor mVisitor = cw.visitMethod(ACC_PUBLIC, "getNumProperties", "()I", null, null);
 		mVisitor.visitCode();
 		mVisitor.visitLdcInsn(new Integer(numProperties));
 		mVisitor.visitInsn(IRETURN);
 		mVisitor.visitMaxs(0, 0);
 		mVisitor.visitEnd();
 	}
 	
 	public static void implementPropertyListFunction()
 	{
 		debug("Implement property list");
 		
 		MethodVisitor mVisitor = cw.visitMethod(ACC_PUBLIC, "getPropertyList", "()Ljava/util/ArrayList;", null, null);
 		mVisitor.visitCode();
 		mVisitor.visitMethodInsn(INVOKESTATIC, "com/hexcore/cas/rulesystems/CodeGen", "getPropertyList", "()Ljava/util/ArrayList;");
 		mVisitor.visitInsn(ARETURN);
 		mVisitor.visitMaxs(0, 0);
 		mVisitor.visitEnd();
 	}
 	
 	public static void implementResetStepFunction()
 	{
 		debug("Implementing resetStepFunction");
 		MethodVisitor mVisitor = cw.visitMethod(ACC_PUBLIC, "resetStep", "()V", null, null);
 		mVisitor.visitCode();
 		mVisitor.visitLdcInsn(new Integer(0));
 		mVisitor.visitFieldInsn(PUTSTATIC, ruleSetName, "engineStep", "I");
 		mVisitor.visitInsn(RETURN);
 		mVisitor.visitMaxs(0,0);
 		mVisitor.visitEnd();
 	}
 	
 	public static void implementStepFunction(int numSteps)
 	{
 		debug("implementing step function");
 		MethodVisitor stepVisitor = cw.visitMethod(ACC_PUBLIC, "step", "()V", null, null);
 		stepVisitor.visitCode();
 
 		/*
 		 * Push current step number onto stack. Add 1 to it (increment instruction not used as it requires a var.
 		 */
 		stepVisitor.visitFieldInsn(GETSTATIC, ruleSetName, "engineStep", "I");
 		stepVisitor.visitLdcInsn(new Integer(1));
 		stepVisitor.visitInsn(IADD);
 		
 		/*
 		 * Push the total number of steps onto the stack. MOD the previous result with it.
 		 * Store the new value.
 		 */
 		stepVisitor.visitLdcInsn(new Integer(numSteps));
 		stepVisitor.visitInsn(IREM);
 		stepVisitor.visitFieldInsn(PUTSTATIC, ruleSetName, "engineStep", "I");
 		
 
 		stepVisitor.visitInsn(RETURN);
 
 		stepVisitor.visitMaxs(0, 0);
 		stepVisitor.visitEnd();
 	}
 	
 	static void initClass(String ruleset)
 	{
 		debug("initClass");
 		varIndex = 5;
 		currentFrameworkIndex = 0;
 		propertyIndex = 1;
 		numProperties = 1;
 		properties = new ArrayList<String>();
 		properties.clear();
 		
 		properties.add("type");
 
 		name = ruleset;
 		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
 		
 		//Class initialisation
 		ruleSetName = "com/hexcore/cas/rulesystems/"+ruleset+"Rule";
 		String[] interfaces = {"com/hexcore/cas/rulesystems/Rule"};
 		cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, ruleSetName, null, "java/lang/Object", interfaces);
 		
 		cw.visitField(ACC_PRIVATE | ACC_STATIC, "engineStep", "I", null, new Integer(0));
 		
 		//Visit constructor
 		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
 		mv.visitCode();
 		Label startScope = new Label();
 		mv.visitLabel(startScope);
 		mv.visitVarInsn(ALOAD, 0);
 		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
 		mv.visitInsn(RETURN);
 		Label endScope = new Label();
 		mv.visitLabel(endScope);
 		mv.visitLocalVariable("this", "Lcom/hexcore/cas/rulesystems/"+ruleset+"Rule;", null, startScope, endScope, 0);
 		mv.visitMaxs(0, 0);
 		
 		mv.visitEnd();		
 	}
 	
 	public static void initExecute()
 	{
 		debug("initExecute");
 		executeVisitor = cw.visitMethod(ACC_PUBLIC, "run", "(Lcom/hexcore/cas/model/Cell;[Lcom/hexcore/cas/model/Cell;)V", null, null);
 		executeVisitor.visitCode();
 		executeBegin = new Label();
 		executeVisitor.visitLabel(executeBegin);
 	}
 	
 	public static void initFramework(int numTypes)
 	{
 		debug("initFramework: " + numTypes + " types");
 		currentFrameworkIndex = 0;
 		frameworkLabels = new Label[numTypes];
 		for(int i = 0; i < numTypes; i++)
 		{
 			frameworkLabels[i] = new Label();
 		}
 		
 		defaultLabel = new Label();
 		
 		//Current index required
 		derefRef(1);			//Self
 		derefProperty(0);		//Self.type
 		
 		executeVisitor.visitInsn(D2I);
 		executeVisitor.visitTableSwitchInsn(0, numTypes-1, defaultLabel, frameworkLabels);
 	}
 	
 	public static Label[] initIf()
 	{
 		debug("Start if");
 		Label negative = new Label();
 		Label end = new Label();
 		
 		executeVisitor.visitJumpInsn(IFEQ, negative);
 		Label[] pointers = {negative, end};
 		return pointers;
 	}
 	
 	public static Label[] initLoop(int index)
 	{
 		debug("Init loop with index: " + index);
 		
 		//Initialise variables (cnt, low, high)
 		executeVisitor.visitVarInsn(DSTORE, index+4);
 		executeVisitor.visitVarInsn(DSTORE, index+2);
 		executeVisitor.visitVarInsn(DLOAD, index+2);
 		executeVisitor.visitVarInsn(DSTORE, index);
 		
 		//Start of loop
 		Label endLabel = new Label();
 		Label loopLabel = new Label();
 		executeVisitor.visitLabel(loopLabel);
 		
 		//Comparison
 		
 		executeVisitor.visitVarInsn(DLOAD, index+4);
 		executeVisitor.visitVarInsn(DLOAD, index);
 		executeVisitor.visitInsn(DSUB);
 		executeVisitor.visitInsn(D2I);
 		executeVisitor.visitJumpInsn(IFLE, endLabel);
 		
 		Label[] labels = new Label[]{loopLabel, endLabel};
 		return labels;
 		
 	}
 	
	public static void initNStepEngine()
	{	
		//debug("Initialising N-Step Engine");
		//loadConstantInteger(0);
		//executeVisitor.visitFieldInsn(PUTSTATIC, ruleSetName, "engineStep", "I");
	}
	
 	public static void initType()
 	{
 		debug("initType");
 		IndexOutOfBoundsException ex = new IndexOutOfBoundsException();
 		
 		try
 		{
 			if(currentFrameworkIndex >= frameworkLabels.length)
 				throw ex;
 			
 			executeVisitor.visitLabel(frameworkLabels[currentFrameworkIndex]);
 			
 			currentFrameworkIndex++;
 		}
 		catch(IndexOutOfBoundsException e)
 		{
 			
 		}
 	}
 	
 	public static void invokeStandardArrayMethod(String name, TableEntry.Type argType, TableEntry.Type returnType)
 	{
 		String aType = "";
 		String rType = "";
 		
 		switch(argType)
 		{
 			case INT: 		aType = "[I"; break;
 			case DOUBLE: 	aType = "[D"; break;
 			case CELL: 		aType = "[Lcom/hexcore/cas/model/Cell"; break;
 			default: 		throw new InvalidTypeException();
 		}
 		
 		switch(returnType)
 		{
 			case INT: 		rType = "I"; break;
 			case DOUBLE: 	rType = "D"; break;
 			default:		throw new InvalidTypeException();
 		}
 		
 		
 		debug("Invoking StdLib array method: " + name + " with argtype " + aType + " and rType " + rType);		
 		
 		executeVisitor.visitMethodInsn(INVOKESTATIC, "com/hexcore/cas/rulesystems/StdLib", name, "(" + aType + ")" + rType);
 		if(returnType == TableEntry.Type.INT)
 			executeVisitor.visitInsn(I2D);
 	}
 	
 	public static void invokeStandardScalarMethod(String name, TableEntry.Type argType,  TableEntry.Type returnType)
 	{
 		String aType = "";
 		String rType = "";
 		
 		switch(argType)
 		{
 			case INT: aType = "I"; break;
 			case DOUBLE: aType = "D"; break;
 			case CELL: aType = "Lcom/hexcore/cas/Cell"; break;
 			default: throw new InvalidTypeException();
 		}
 		
 		switch(returnType)
 		{
 			case INT: rType = "I"; break;
 			case DOUBLE: rType = "D"; break;
 			default: throw new InvalidTypeException();
 		}
 		
 		
 		debug("Invoking StdLib scalar method: " + name + " with argtype " + aType + " and rType " + rType);		
 		
 		executeVisitor.visitMethodInsn(INVOKESTATIC, "com/hexcore/cas/rulesystems/StdLib", name, "(" + aType + ")" + rType);
 		if(returnType == TableEntry.Type.INT)
 			executeVisitor.visitInsn(I2D);
 	}
 	
 	public static void jump(Label label)
 	{
 		debug("Jump to: " + label);
 		executeVisitor.visitJumpInsn(GOTO, label);
 	}
 	
 	public static void loadConstantDouble(double value)
 	{
 		debug("Loading constant double: " + value);
 		executeVisitor.visitLdcInsn(new Double(value));
 	}
 	
 	public static void loadConstantInteger(int value)
 	{
 		debug("Loading constant integer: " + value);
 		executeVisitor.visitLdcInsn(new Integer(value));
 	}
 	
 	public static void negate()
 	{
 		debug("Negate");
 		executeVisitor.visitInsn(DNEG);
 	}
 	
 	public static void performAddOp(AddOpE op)
 	{
 		debug("Add op: " + op.toString());
 		switch(op)
 		{
 			case ADD: executeVisitor.visitInsn(DADD); break;
 			case SUB: executeVisitor.visitInsn(DSUB); break;
 			case OR: executeVisitor.visitInsn(IOR); break;
 			default: throw new UnsupportedOpException();
 		}
 	}
 	
 	public static void performMulOp(MulOpE op)
 	{
 		debug("Mul op: " + op.toString());
 		switch(op)
 		{
 			case MUL: executeVisitor.visitInsn(DMUL); break;
 			case DIV: executeVisitor.visitInsn(DDIV); break;
 			case MOD: executeVisitor.visitInsn(DREM); break;
 			case AND: executeVisitor.visitInsn(IAND); break;
 			default: throw new UnsupportedOpException();
 		}
 	}
 	
 	public static void performPostOp(int index, int delta)
 	{
 		debug("Post op");
 		executeVisitor.visitVarInsn(DLOAD, index);
 		executeVisitor.visitLdcInsn(new Double(delta));
 		executeVisitor.visitInsn(DADD);
 		executeVisitor.visitVarInsn(DSTORE, index);
 	}
 	
 	public static void performRelationalOp(RelOpE op)
 	{
 		debug("Relational op: " + op.toString());
 		Label negative = new Label();
 		Label end = new Label();
 		executeVisitor.visitInsn(DCMPG);
 		switch(op)
 		{
 			case LT:	executeVisitor.visitJumpInsn(IFGE, negative); break;
 			case GT:	executeVisitor.visitJumpInsn(IFLE, negative); break;
 			case LE:	executeVisitor.visitJumpInsn(IFGT, negative); break;
 			case GE:	executeVisitor.visitJumpInsn(IFLT, negative); break;
 			case EQ:	executeVisitor.visitJumpInsn(IFNE, negative); break;
 			case NE:	executeVisitor.visitJumpInsn(IFEQ, negative); break;
 			default: throw new UnsupportedOpException();
 		}
 		
 		executeVisitor.visitInsn(ICONST_1);
 		executeVisitor.visitJumpInsn(GOTO, end);
 		executeVisitor.visitLabel(negative);
 		executeVisitor.visitInsn(ICONST_0);
 		executeVisitor.visitLabel(end);
 	}
 	
 	public static void storeProperty(int index)
 	{
 		debug("Storing at property: " + index);
 		//Store result in temp variable in preparation for function call.
 		executeVisitor.visitVarInsn(DSTORE, 3);
 		//executeVisitor.visitVarInsn(ALOAD, 1);
 		executeVisitor.visitLdcInsn(new Integer(index));
 		
 		//Restore value
 		executeVisitor.visitVarInsn(DLOAD, 3);
 		
 		executeVisitor.visitMethodInsn(INVOKEVIRTUAL, "com/hexcore/cas/model/Cell", "setValue", "(ID)V");
 	}
 	
 	public static void storeVariable(int index)
 	{
 		debug("Storing at: " + index);
 		executeVisitor.visitVarInsn(DSTORE, index);
 	}
 	
 	public static void toDouble()
 	{
 		debug("Convert to double");
 		executeVisitor.visitInsn(I2D);
 	}
 	
 	public static void toFile(String fileName)
 	{
 		try
 		{
 			FileOutputStream out = new FileOutputStream(new File(name+".class"));
 			out.write(cw.toByteArray());
 			out.close();
 		} catch (IOException e)
 		{
 			System.err.println("Could not write class file");
 		}
 	}
 	
 	public static void visitLabel(Label label)
 	{
 		debug("Label visit: " + label);
 		executeVisitor.visitLabel(label);
 	}
 	
 	/////////////////////////////////////////////
 	/// Private functions
 	private static void debug(String msg)
 	{
 		if(debugEnabled)
 			System.out.println(msg);
 	}
 }
