 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.*;
 import org.apache.bcel.*;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.generic.*;
 import org.apache.bcel.verifier.*;
 
 class Main{
 	static boolean loud = false;
 
 	public static void main(String[] args) throws Exception{
 		for(String arg : args){
 			if(arg.equals("-break")){
 				BreakMe.codeToBreak();
 				return;
 			}else if(arg.equals("-work")){
 				BreakMe.codeThatWorks();
 				return;
 			}else if(arg.equals("-loud")){
 				loud = true;
 			}
 			inject(arg);
 		}
 	}
 
 	private static void inject(String fname) throws Exception{
		JavaClass jc = new ClassParser(fname).parse();
 
 		ClassGen cg = new ClassGen(jc);
 		InstructionFactory f = new InstructionFactory(cg);
 
 		Method[] oldMethods = jc.getMethods();
 		Method[] newMethods = new Method[oldMethods.length];
 
 		for(int i = 0; i < oldMethods.length; i++){
 			Method m = oldMethods[i];
 			Code c = m.getCode();
 			if(c == null){
 				newMethods[i] = m;
 				continue;
 			}
 
 			MethodGen mg = new MethodGen(m, jc.getClassName(), f.getConstantPool());
 
 			byte[] bc = c.getCode();
 			InstructionList il = new InstructionList(bc);
 
 			BasicBlock bb = new BasicBlock(f, il, il.getStart(), il.getEnd());
 			boolean changed = bb.insertChecks();
 
 			mg.setInstructionList(il);
 			Method n = mg.getMethod();
 			newMethods[i] = n;
 
 			if(loud && changed)
 				System.out.println(n.getCode());
 		}
 
 		cg.setMethods(newMethods);
 
 		JavaClass njc = cg.getJavaClass();
 		njc.dump(fname);
 	}
 }
