 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.*;
 import org.apache.bcel.*;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.generic.*;
 
 import static edu.unh.cs.tact.Util.*;
 
 class Injector{
 	public final ConstantPoolGen cp;
 	public final InstructionFactory f;
 	public final MethodGen mg;
 	public final InstructionList list;
 	public final InstructionHandle begin, end;
 
 	public Injector(ConstantPoolGen cp, InstructionFactory f, MethodGen mg){
 		this.f = notNull(f, "f");
 		this.mg = notNull(mg, "mg");
 		this.list = mg.getInstructionList();
 		this.begin = list.getStart();
 		this.end = list.getEnd();
 		this.cp = notNull(cp, "cp");
 	}
 
 	public boolean inject(){
 		boolean changed = false;
 		InstructionHandle end = this.end.getNext();
 		for(InstructionHandle h = begin; h != end; h = h.getNext()){
 			CheckInserter ins = getInserter(h);
 			if(ins == null)
 				continue;
 
 			Check chk = getCheck(h);
 			if(chk == null)
 				continue;
 
 			ins.insert(chk);
 			changed = true;
 		}
 
 		if(changed)
 			mg.setMaxStack();
 
 		return changed;
 	}
 
 	/**
 	Implementations of CheckInserter emit any necessary prologue and epilogue
 	bytecode around chk.insert().
 	*/
 	private interface CheckInserter{
 		void insert(Check chk);
 	}
 
 	/** Each implementation of Check emits a call to one of Check's public methods. */
 	private interface Check{
 		void insert(InstructionHandle h);
 	}
 
 	private CheckInserter getInserter(InstructionHandle h){
 		Instruction code = h.getInstruction();
 		if(code instanceof PUTFIELD || code instanceof GETFIELD){
 			FieldInstruction fi = (FieldInstruction)code;
 			if(fi.getFieldName(cp).equals("this$0"))
 				return null; // skip inner class's outer reference
 
 			if(code instanceof GETFIELD)
 				return new CheckGetRef(h);
 
 			return new CheckPutRef(fi, h);
 		}
 		if(code instanceof PUTSTATIC || code instanceof GETSTATIC){
 			return new CheckStatic((FieldInstruction)code, h);
 		}
 		if(isArrayStore(code)){
 			return new CheckArrayStore((ArrayInstruction)code, h);
 		}
 		if(isForNew(code, h)){ // ignore super's ctors
 			return new CheckConstruct(h);
 		}
 		return null;
 	}
 
 	private Check getCheck(InstructionHandle h){
 		Instruction code = h.getInstruction();
 		if(!(code instanceof FieldInstruction))
 			return new Strict();
 
 		FieldInstruction pf = (FieldInstruction)code;
 		JavaClass jc = classFor(pf);
 		if(jc == null)
 			return null;
 
 		Field f = fieldFor(jc, pf);
 		if(f == null)
 			return null;
 
 		String guard = guardName(f);
 		if(f.isFinal() && guard == null)
 			return null;
 		if(guard == null)
 			return new Strict();
 		if(guard.equals("this"))
 			return new ThisGuard();
 		return new StaticGuard(guard);
 	}
 
 	private boolean isArrayStore(Instruction h){
 		return h instanceof AASTORE
 			|| h instanceof BASTORE
 			|| h instanceof CASTORE
 			|| h instanceof DASTORE
 			|| h instanceof FASTORE
 			|| h instanceof IASTORE
 			|| h instanceof LASTORE
 			|| h instanceof SASTORE
 			;
 	}
 
 	private boolean isForNew(Instruction code, InstructionHandle h){
 		if(!(code instanceof INVOKESPECIAL))
 			return false;
 
 		int stk = 0;
 		while(h != null){
 			code = h.getInstruction();
 
 			stk += code.produceStack(cp);
 			stk -= code.consumeStack(cp);
 			if(stk == 1)
 				return code instanceof NEW;
 
 			h = h.getPrev();
 		}
 
 		return false;
 	}
 
 	private JavaClass classFor(FieldInstruction fi){
 		ReferenceType rt = fi.getReferenceType(cp);
 		if(!(rt instanceof ObjectType))
 			return null;
 
 		ObjectType ot = (ObjectType)rt;
 		try{
 			return Repository.lookupClass(ot.getClassName());
 		}catch(ClassNotFoundException e){
 			throw new RuntimeException(e);
 		}
 	}
 
 	private Field fieldFor(JavaClass jc, FieldInstruction fi){
 		for(Field f : jc.getFields()){
 			if(f.getName().equals(fi.getFieldName(cp)))
 				return f;
 		}
 		return null;
 	}
 
 	private String guardName(Field f){
 		for(AnnotationEntry ae : f.getAnnotationEntries()){
 			if(!ae.getAnnotationType().equals("Ledu/unh/cs/tact/GuardedBy;"))
 				continue;
 
 			for(ElementValuePair ev : ae.getElementValuePairs())
 				if(ev.getNameString().equals("value"))
 					return ev.getValue().stringifyValue();
 		}
 		return null;
 	}
 
 
 	private void insertCheck(String fname, InstructionHandle h, Type... args){
 		assert h != null;
 		list.insert(
 			h,
 			f.createInvoke(
 				"edu.unh.cs.tact.Checker",
 				fname,
 				Type.VOID,
 				args,
 				Constants.INVOKESTATIC
 			)
 		);
 	}
 
 	private class Strict implements Check{
 		public void insert(InstructionHandle h){
 			insertCheck("check", h, Type.OBJECT);
 		}
 	}
 
 	private class ThisGuard implements Check{
 		public void insert(InstructionHandle h){
 			insertCheck("guardByThis", h, Type.OBJECT);
 		}
 	}
 
 	private class StaticGuard implements Check{
 		private final String guard;
 		public StaticGuard(String guard){
 			this.guard = notNull(guard, "guard");
 		}
 
 		public void insert(InstructionHandle h){
 			insertCheck("guardByStatic", h, Type.OBJECT, Type.STRING);
 		}
 	}
 
 
 	private class CheckPutRef implements CheckInserter{
 		FieldInstruction pf;
 		InstructionHandle h;
 		public CheckPutRef(FieldInstruction pf, InstructionHandle h){
 			this.pf = pf;
 			this.h = h;
 		}
 
 		public void insert(Check chk){
 			if(mg.getName().equals("<init>") && chk instanceof ThisGuard)
 				return;
 
 			switch(pf.getType(cp).getSize()){
 			case 1:
 				insert32(chk);
 				break;
 			case 2:
 				insert64(chk);
 				break;
 			default:
 				assert false : "A different size of field???";
 			}
 		}
 
 		public void insert32(Check chk){
 			list.insert(h, new SWAP());
 			list.insert(h, new DUP());
 			chk.insert(h);
 			list.insert(h, new SWAP());
 		}
 
 		public void insert64(Check chk){
 			list.insert(h, new DUP2_X1());
 			list.insert(h, new POP2());
 			list.insert(h, new DUP_X2());
 			chk.insert(h);
 		}
 	}
 
 	private class CheckGetRef implements CheckInserter{
 		private final InstructionHandle h;
 		public CheckGetRef(InstructionHandle h){
 			this.h = h;
 		}
 
 		public void insert(Check chk){
 			list.insert(h, new DUP());
 			chk.insert(h);
 		}
 	}
 
 	private class CheckStatic implements CheckInserter{
 		private final FieldInstruction code;
 		private final InstructionHandle h;
 		public CheckStatic(FieldInstruction code, InstructionHandle h){
 			this.code = code;
 			this.h = h;
 		}
 
 		public void insert(Check chk){
 			int i = code.getIndex();
 			Constant c = cp.getConstant(i);
 			if(!(c instanceof ConstantFieldref))
 				throw new AssertionError("Flawed static field check");
 
 			ConstantFieldref cfr = (ConstantFieldref)c;
 			list.insert(h, new LDC_W(cfr.getClassIndex()));
 			chk.insert(h);
 		}
 	}
 
 	private class CheckArrayStore implements CheckInserter{
 		ArrayInstruction pa;
 		InstructionHandle h;
 		CheckArrayStore(ArrayInstruction pa, InstructionHandle h){
 			this.pa = pa;
 			this.h = h;
 		}
 
 		public void insert(Check chk){
 			switch(pa.getType(cp).getSize()){
 			case 1:
 				insert32(chk);
 				break;
 			case 2:
 				insert64(chk);
 				break;
 			default:
 				assert false : "A different size of field???";
 			}
 		}
 
 		public void insert32(Check chk){
 			list.insert(h, new DUP2_X1());
 			list.insert(h, new POP2());
 			list.insert(h, new DUP_X2());
 			chk.insert(h);
 		}
 	
 		public void insert64(Check chk){
 			list.insert(h, new DUP2_X2());
 			list.insert(h, new POP2());
 			list.insert(h, new DUP2());
 			list.insert(h, new POP());
 			chk.insert(h);
 			list.insert(h, new DUP2_X2());
 			list.insert(h, new POP2());
 		}
 	}
 
 	private class CheckConstruct implements CheckInserter{
 		InstructionHandle h;
 		CheckConstruct(InstructionHandle h){
 			this.h = h.getNext(); // ref should be on top after call to <init>
 			assert this.h != null;
 		}
 
 		public void insert(Check chk){
 			list.insert(h, new DUP());
 			chk.insert(h);
 		}
 	}
 }
