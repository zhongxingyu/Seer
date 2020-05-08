 /*
 Copyright (c) 2005-2006, MentorGen, LLC
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without 
 modification, are permitted provided that the following conditions are met:
 
 + Redistributions of source code must retain the above copyright notice, 
   this list of conditions and the following disclaimer.
 + Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation 
   and/or other materials provided with the distribution.
 + Neither the name of MentorGen LLC nor the names of its contributors may be 
   used to endorse or promote products derived from this software without 
   specific prior written permission.
 
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
   ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
   LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
   INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
   POSSIBILITY OF SUCH DAMAGE.
  */
 package com.mentorgen.tools.profile.instrument;
 
 import org.objectweb.asm.jip.Label;
 import org.objectweb.asm.jip.MethodAdapter;
 import org.objectweb.asm.jip.MethodVisitor;
 import org.objectweb.asm.jip.Opcodes;
 
 import com.mentorgen.tools.profile.Controller;
 
 import static org.objectweb.asm.jip.Opcodes.INVOKESTATIC;
 
 /**
  * This class is responsible for instrumenting a method to 
  * call the profiler in order for performance
  * data to be gathered. The basic idea is that the profiler is called
  * when a method starts and when it exists which allows the profiler
  * to gather performance data (note that a method can be exited from
  * when an exception is thrown as well as when return is called). The
  * one big caveate is static initializers. They are not called as part
  * of the flow of the program &mdash; they are called by the classloader.
  * Since they whole premise of the profiler is built on the idea of a
  * orderly call stack, static initializers are not instrumented. 
  * 
  * @author Andrew Wilcox
  *
  */
 public class PerfMethodAdapter extends MethodAdapter {
 	private String _className, _methodName;
 	private boolean _clinit = false;
 	private boolean _init = false;
 	
 	public PerfMethodAdapter(MethodVisitor visitor, 
 			String className,
 			String methodName) { 
 		super(visitor);
 		_className = className;
 		_methodName = methodName;
 
 		// Static initializers are excluded. The reason for this
 		// is the the profiling algorithm we're using mirrors the call stack.
 		// Since static initializers are called by the classloader
 		// and therefore aren't part of the programs flow of control,
 		// static initializers can really mess up the profiler, especially
 		// when they're called before the program's flow of control is started
 		// (for example, the when the class with the main() method has a 
 		// static initalizer). So yes, this is a short comming in the
 		// design of the profiler, but we're willing to live with it because
 		// this profiler is lightweight and allows us to use it interactively.
 		//
 		if (methodName.equals("<clinit>")) {
 			_clinit = true;
 		} else if (methodName.startsWith("<init>")) {
 			_init = true;
 		}
 	}
 
 	public void visitCode() {
 		if (_clinit) {
 			super.visitCode();
 			return;
 		} 
 		
 		// Because the alloc method looks at the class + method of the caller
 		// this call needs to come before the call to Profile.start
 		//
 		if (Controller._trackObjectAlloc && _init) {
 			this.visitLdcInsn(_className);
 			this.visitMethodInsn(INVOKESTATIC, 
 					Controller._profiler, 
 					"alloc", 
 					"(Ljava/lang/String;)V");			
 		}
 		
 		this.visitLdcInsn(_className);
 		this.visitLdcInsn(_methodName);
 		this.visitMethodInsn(INVOKESTATIC, 
 				Controller._profiler, 
 				"start", 
 				"(Ljava/lang/String;Ljava/lang/String;)V");
 		
 		super.visitCode();
 	}
 
 	public void visitInsn(int inst) {
 		if (_clinit) {
 			super.visitInsn(inst);
 			return;
 		}
 
 		switch (inst) {
 		case Opcodes.ARETURN:
 		case Opcodes.DRETURN:
 		case Opcodes.FRETURN:
 		case Opcodes.IRETURN:
 		case Opcodes.LRETURN:
 		case Opcodes.RETURN:
 		case Opcodes.ATHROW:
 			
 			this.visitLdcInsn(_className);
 			this.visitLdcInsn(_methodName);
 						
 			this.visitMethodInsn(INVOKESTATIC, 
 					Controller._profiler, 
 					"end", 
 					"(Ljava/lang/String;Ljava/lang/String;)V");
 			break;
 
 		default:
 			break;
 		}
 		
 		if (Opcodes.MONITORENTER == inst) {
 			this.visitLdcInsn(_className);
 			this.visitLdcInsn(_methodName);
 						
 			this.visitMethodInsn(INVOKESTATIC, 
 					Controller._profiler, 
 					"beginWait", 
 					"(Ljava/lang/String;Ljava/lang/String;)V");
 			
 			super.visitInsn(inst);
 
 			this.visitLdcInsn(_className);
 			this.visitLdcInsn(_methodName);
 						
 			this.visitMethodInsn(INVOKESTATIC, 
 					Controller._profiler, 
 					"endWait", 
 					"(Ljava/lang/String;Ljava/lang/String;)V");			
 		} else {
 			super.visitInsn(inst);
 		}
 	}
 
 	@Override
 	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
 		if (isWaitInsn(opcode, owner, name, desc)) {
 
 			this.visitLdcInsn(_className);
 			this.visitLdcInsn(_methodName);
 						
 			this.visitMethodInsn(INVOKESTATIC, 
 					Controller._profiler, 
 					"beginWait", 
 					"(Ljava/lang/String;Ljava/lang/String;)V");
 			
 			super.visitMethodInsn(opcode, owner, name, desc);
 
 			this.visitLdcInsn(_className);
 			this.visitLdcInsn(_methodName);
 						
 			this.visitMethodInsn(INVOKESTATIC, 
 					Controller._profiler, 
 					"endWait", 
 					"(Ljava/lang/String;Ljava/lang/String;)V");
 		} else {
 			super.visitMethodInsn(opcode, owner, name, desc);
 		}
 	}
 	
 	//
 	// code to handle unwinding the call stack when an exception is thrown
	// (many thanks to Fredrik Svarén for posting this code in the help forum!)
 	//
 	
 	@Override
 	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
 		super.visitTryCatchBlock(start, end, handler, type);
 		
 		// Note: static initializers aren't measured, so make sure that the exception
 		// isn't being caught in one
 		if (type != null && !_clinit) {
 			handler.info = new ExceptionInfo(type);
 		}
 	}
 	
 	@Override
 	public void visitLabel(Label label) {
 		super.visitLabel(label);
 		
 		if (label.info instanceof ExceptionInfo) {
 			this.visitLdcInsn(_className);
 			this.visitLdcInsn(_methodName);
 			this.visitLdcInsn(((ExceptionInfo)label.info).type);
 
 			this.visitMethodInsn(INVOKESTATIC, 
 					Controller._profiler, 
 					"unwind", 
 					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
 		}
 	}
 
 	class ExceptionInfo {
 		String type;
 		ExceptionInfo(String type) {
 			this.type = type;
 		}
 	}
 	
 	//
 	// private methods
 	//
 
 	private static boolean isWaitInsn(int opcode, String owner, String name, String desc) {
 		boolean isWait = (opcode == Opcodes.INVOKEVIRTUAL 
 				&& "java/lang/Object".equals(owner) 
 				&& "wait".equals(name)
 				&& ("()V".equals(desc) || "(J)V".equals(desc) || "(JI)V".equals(desc)));		
 		if (isWait) return true;
 
 		isWait = (opcode == Opcodes.INVOKEVIRTUAL
 				&& "java/lang/Thread".equals(owner) 
 				&& "join".equals(name)
 				&& ("()V".equals(desc) || "(J)V".equals(desc) || "(JI)V".equals(desc)));
 		if (isWait) return true;
 		
 		isWait = (opcode == Opcodes.INVOKESTATIC 
 				&& "java/lang/Thread".equals(owner) 
 				&& "sleep".equals(name)
 				&& ("(J)V".equals(desc) || "(JI)V".equals(desc)));
 		if (isWait) return true;
 		
 		isWait = (opcode == Opcodes.INVOKESTATIC 
 				&& "java/lang/Thread".equals(owner) 
 				&& "yield".equals(name)
 				&& "()V".equals(desc));
 		if (isWait) return true;
 		
 		return isWait;
 	}
 	
 	
 	
 }
 
