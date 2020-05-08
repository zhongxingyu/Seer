 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.doms;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import joeq.Class.jq_Class;
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.RegisterFactory.Register;
 import chord.program.visitors.IMethodVisitor;
 import chord.project.Chord;
 import chord.project.analyses.ProgramDom;
 import joeq.Compiler.Quad.RegisterFactory;
 import joeq.Compiler.Quad.BasicBlock;
 import joeq.Compiler.Quad.ControlFlowGraph;
 import joeq.Compiler.Quad.Operator;
 import joeq.Compiler.Quad.Quad;
 import joeq.Compiler.Quad.Operand;
 import joeq.Compiler.Quad.Operand.RegisterOperand;
 import joeq.Compiler.Quad.Operand.ParamListOperand;
 import joeq.Class.jq_Type;
 import joeq.Util.Templates.ListIterator;
 
 /**
  * Domain of local variables of reference type.
  * <p>
  * Each local variable declared in each block of each method is
  * represented by a unique element in this domain.  Local variables
  * that have the same name but are declared in different methods
  * or in different blocks of the same method are represented by
  * different elements in this domain.
  * <p>
  * The set of local variables of a method is the disjoint union of
  * its argument variables and its temporary variables.  All local
  * variables of the same method are assigned contiguous indices in
  * this domain.  The argument variables are assigned contiguous
  * indices in order followed by the temporary variables.
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 @Chord(
 	name = "V"
 )
 public class DomV extends ProgramDom<Register> implements IMethodVisitor {
 	private Map<Register, jq_Method> varToMethodMap;
 	private jq_Method ctnrMethod;
 	public void init() {
 		varToMethodMap = new HashMap<Register, jq_Method>();
 	}
 	public jq_Method getMethod(Register v) {
 		return varToMethodMap.get(v);
 	}
 	public void visit(jq_Class c) { }
     public void visit(jq_Method m) {
         if (m.isAbstract())
             return;
 		ctnrMethod = m;
         ControlFlowGraph cfg = m.getCFG();
         RegisterFactory rf = cfg.getRegisterFactory();
         jq_Type[] paramTypes = m.getParamTypes();
         int numArgs = paramTypes.length;
         for (int i = 0; i < numArgs; i++) {
             jq_Type t = paramTypes[i];
             if (t.isReferenceType()) {
                 Register v = rf.get(i);
 				addVar(v);
             }
         }
         for (ListIterator.BasicBlock it = cfg.reversePostOrderIterator(); it.hasNext();) {
             BasicBlock bb = it.nextBasicBlock();
             for (ListIterator.Quad it2 = bb.iterator(); it2.hasNext();) {
                 Quad q = it2.nextQuad();
                 process(q.getOp1(), q);
                 process(q.getOp2(), q);
                 process(q.getOp3(), q);
                 process(q.getOp4(), q);
             }
         }
     }
 	private void addVar(Register v) {
 		varToMethodMap.put(v, ctnrMethod);
 		getOrAdd(v);
 	}
     private void process(Operand op, Quad q) {
         if (op instanceof RegisterOperand) {
             RegisterOperand ro = (RegisterOperand) op;
 			Register v = ro.getRegister();
             jq_Type t = ro.getType();
             if (t != null && t.isReferenceType()) {
 				addVar(v);
 			}
         } else if (op instanceof ParamListOperand) {
             ParamListOperand ros = (ParamListOperand) op;
             int n = ros.length();
             for (int i = 0; i < n; i++) {
                 RegisterOperand ro = ros.get(i);
				if (ro == null)
					continue;
 				jq_Type t = ro.getType();
 				if (t != null && t.isReferenceType()) {
					Register v = ro.getRegister();
 					addVar(v);
 				}
             }
 		}
     }
 	public String toUniqueString(Register v) {
 		return v + "!" + getMethod(v);
 	}
 }
