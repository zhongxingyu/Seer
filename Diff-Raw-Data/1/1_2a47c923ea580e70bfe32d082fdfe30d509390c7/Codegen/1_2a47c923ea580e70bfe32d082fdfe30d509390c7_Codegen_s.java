 package x86;
 
 import util.List;
 
 import assem.Instr;
 import assem.OPER;
 import temp.Label;
 import temp.Temp;
 import tree.*;
 
 public class Codegen {
     Frame frame;
     private List<Instr> ilist = null, last = null;
     
     public Codegen(Frame f) {
         frame = f;
     }
 
     /**
      * Adds an instruction to the end of the list of instructions.
      * 
      * @param inst
      */
     private void emit(Instr inst) {
         if (last != null) {
             last = last.tail = new List<Instr>(inst, null);
         } else {
             last = ilist = new List<Instr>(inst, null);
         }
     }
 
     /**
      * Emits instructions for a given Tree.Stm node using the Maximal Munch
      * algorithm.
      * 
      * @param s
      */
     void munchStm(Stm s) {
         if (s instanceof EXPSTM) {
             munchStm((EXPSTM)s);
         } else if (s instanceof SEQ) {
             munchStm((SEQ)s);
         } else if (s instanceof LABEL) {
             munchStm((LABEL)s);
         } else if (s instanceof JUMP) {
             munchStm((JUMP)s);
         } else if (s instanceof CJUMP) {
             munchStm((CJUMP)s);
         } else if (s instanceof MOVE) {
             munchStm((MOVE)s);
         } else {
             System.err.println("Unrecognized Stm: " + s.getClass());
         }
     }
 
     /**
      * Emits instructions for a given Tree.Stm.EXPSTM.
      * 
      * @param e
      */
     void munchStm(EXPSTM e) {
         munchExp(e.getExpression());
     }
 
     /**
      * Emits instructions for a given Tree.Stm.SEQ.
      *     
      * @param s
      */
     void munchStm(SEQ s) {
         munchStm(s.getLeft());
         munchStm(s.getRight());
     }
 
     /**
      * Emits instructions for a given Tree.Stm.LABEL.
      * 
      * @param l
      */
     void munchStm(LABEL l) {
         emit(new assem.LABEL(l.getLabel().toString() + ":", l.getLabel()));
     }
 
     /**
      * Emits instructions for a given Tree.Stm.JUMP.
      * 
      * @param j
      */
     void munchStm(JUMP j) {
         Exp e = j.getExpression();
         if (e instanceof NAME) {
             // JUMP(NAME):
             Label l = ((NAME)e).getLabel();
             emit(new OPER("jmp `j0", new List<Label>(l, null)));
         } else {
             Temp u = munchExp(e);
             emit(new OPER("jmp `u0", null, new List<Temp>(u, null)));
         }
     }
 
     /**
      * Emits instructions for a given Tree.Stm.CJUMP.
      * 
      * @param c
      */
     void munchStm(CJUMP c) {
         String inst = "";
 
         switch (c.getOperation()) {
             case CJUMP.EQ:
                 inst = "jz";
                 break;
             case CJUMP.NE:
                 inst = "jnz";
                 break;
             case CJUMP.LT:
                 inst = "jl";
                 break;
             case CJUMP.LE:
                 inst = "jle";
                 break;
             case CJUMP.GT:
                 inst = "jg";
                 break;
             case CJUMP.GE:
                 inst = "jge";
                 break;
             case CJUMP.ULT:
                 inst = "jb";
                 break;
             case CJUMP.ULE:
                 inst = "jbe";
                 break;
             case CJUMP.UGT:
                 inst = "ja";
                 break;
             case CJUMP.UGE:
                 inst = "jae";
                 break;
         }
 
         Temp left = munchExp(c.getLeft());
         Temp right = munchExp(c.getRight());
 
         emit(new OPER(
             "cmp `u0, `u1",
             null,
             new List<Temp>(left, new List<Temp>(right, null))
         ));
         emit(new OPER(inst + " `j0", new List<Label>(c.getLabelTrue(), null)));
         emit(new OPER("jmp `j0", new List<Label>(c.getLabelFalse(), null)));
     }
 
     /**
      * Emits instructions for a given Tree.Stm.MOVE.
      * 
      * @param m
      */
     void munchStm(MOVE m) {
         Exp src = m.getSource();
         Exp dst = m.getDestination();
         if (dst instanceof MEM) {
             // MOVE(MEM, X):
             Temp tsrc = munchExp(src);
             Temp taddr = munchExp(((MEM)dst).getExpression());
             emit(new OPER(
                 "mov [`u0], `u1",
                 null,
                 new List<Temp>(taddr, new List<Temp>(tsrc, null))
             ));
         } else {
             emit(new assem.MOVE(munchExp(dst), munchExp(src)));
         }
     }
 
     /**
      * Emits instructions for a given Tree.Exp node using the Maximal Munch
      * algorithm. Returns the temporary register where the expression's result
      * is stored.
      * 
      * @param s
      */
     Temp munchExp(Exp e) {
         if (e instanceof TEMP) {
             return munchExp((TEMP)e);
         } else if (e instanceof ESEQ) {
             return munchExp((ESEQ)e);
         } else if (e instanceof NAME) {
             return munchExp((NAME)e);
         } else if (e instanceof CONST) {
             return munchExp((CONST)e);
         } else if (e instanceof MEM) {
             return munchExp((MEM)e);
         } else if (e instanceof CALL) {
             return munchExp((CALL)e);
         } else if (e instanceof BINOP) {
             return munchExp((BINOP)e);
         } else {
             System.err.println("Unrecognized Exp: " + e.getClass());
             return new Temp();
         }
     }
 
     /**
      * Returns the temporary register for a given Tree.Exp.TEMP.
      * 
      * @param t
      * @return
      */
     Temp munchExp(TEMP t) {
     	return t.getTemp();
     }
 
     /**
      * Emits instructions and returns the temporary register for a given
      * Tree.Exp.ESEQ.
      * 
      * @param e
      * @return
      */
     Temp munchExp(ESEQ e) {
         munchStm(e.getStatement());
         return munchExp(e.getExpression());
     }
 
     /**
      * Emits instructions and returns the temporary register for a given
      * Tree.Exp.NAME.
      * 
      * @param n
      * @return
      */
     Temp munchExp(NAME n) {
         Temp r = new Temp();
         emit(new OPER(
             "mov `d0, " + n.getLabel().toString(),
             new List<Temp>(r, null),
             null
         ));
         return r;
     }
 
     /**
      * Emits instructions and returns the temporary register for a given
      * Tree.Exp.CONST.
      * 
      * @param c
      * @return
      */
     Temp munchExp(CONST c) {
         Temp r = new Temp();
         emit(new OPER(
             "mov `d0, " + c.getValue(),
             new List<Temp>(r, null),
             null
         ));
         return r;
     }
 
     /**
      * Emits instructions and returns the temporary register for a given
      * Tree.Exp.MEM.
      * 
      * @param m
      * @return
      */
     Temp munchExp(MEM m) {
         Temp r = new Temp();
         Temp u = munchExp(m.getExpression());
         emit(new OPER(
             "mov `d0, [`u0]",
             new List<Temp>(r, null),
             new List<Temp>(u, null)
         ));
         return r;
     }
 
     /**
      * Emits instructions and returns the temporary register for a given
      * Tree.Exp.CALL.
      * 
      * @param c
      * @return
      */
     Temp munchExp(CALL c) {
         List<Exp> args = c.getArguments();
         List<Temp> ulist = munchArgs(args);
 
         String source = "";
         Exp e = c.getCallable();
         if (e instanceof NAME) {
             // CALL(NAME):
             source = ((NAME)e).getLabel().toString();
         } else {
             Temp u;
             if (e instanceof MEM) {
                 // CALL(MEM):
                 source = "[`u0]";
                 u = munchExp(((MEM)e).getExpression());
             } else {
             	source = "`u0";
                 u = munchExp(e);
             }
             ulist = new List<Temp>(u, ulist);
         }
         emit(new OPER("call " + source, frame.calleeDefs(), ulist));
 
         // Restore the stack:
         if (args.size() > 0) {
             emit(new OPER(
                 "add `d0, " + (4 * args.size()),
                 new List<Temp>(frame.SP(), null),
                 new List<Temp>(frame.SP(), null)
             ));
         }
 
         return frame.RV();
     }
         
     /**
      * Emits instructions to move all the CALL arguments to their correct
      * positions.
      * 
      * @param args
      * @return
      */
     List<Temp> munchArgs(List<Exp> args) {
         if (args == null) {
             return null;
         }
 
         // The parameters should be pushed in inverted order:
         List<Temp> rlist = munchArgs(args.tail);
 
         List<Temp> ulist;
         String source = "";
         if (args.head instanceof CONST) {
             // PUSH(CONST):
         	source += ((CONST)args.head).getValue();
         	ulist = new List<Temp>(frame.SP(), null);
         } else {
             Temp u;
             if (args.head instanceof MEM) {
                 // PUSH(MEM):
                 source = "[`u0]";
                 u = munchExp(((MEM)args.head).getExpression());
             } else {
             	source = "`u0";
             	u = munchExp(args.head);
             }
             rlist = new List<Temp>(u, rlist);
             ulist = new List<Temp>(u, new List<Temp>(frame.SP(), null));
         }
         
        Temp u = munchExp(args.head);
         emit(new OPER(
             "push " + source,
             new List<Temp>(frame.SP(), null),
             ulist
         ));
 
         return rlist;
     }
 
     /**
      * Emits instructions and returns the temporary register for a given
      * Tree.Exp.BINOP.
      * 
      * @param b
      * @return
      */
     Temp munchExp(BINOP b) {
         // BINOP(TIMES, A, B):
         if (b.getOperation() == BINOP.TIMES) {
             Temp left = munchExp(b.getLeft());
             Temp right = munchExp(b.getRight());
             
             // One operand should be in EAX:
             emit(new assem.MOVE(frame.eax, left));
             
             // Higher part of result in EAX and lower part in EDX:
             emit(new OPER(
                 "mul `u1",
                 new List<Temp>(frame.eax, new List<Temp>(frame.edx, null)),
                 new List<Temp>(frame.eax, new List<Temp>(right, null))
             ));
             return frame.eax;
         }
 
         // BINOP(DIV, A, B):
         if (b.getOperation() == BINOP.DIV) {
             Temp left = munchExp(b.getLeft());
             Temp right = munchExp(b.getRight());
             
             // Dividend should be in EDX:EAX: 
             emit(new assem.MOVE(frame.eax, left));
             emit(new OPER("cdq"));
 
             // Remainder in EDX and quotient in EAX:
             List<Temp> list;
             list = new List<Temp>(frame.eax, new List<Temp>(frame.edx, null));
             emit(new OPER(
                 "div `u0",
                 list,
                 new List<Temp>(right, list)
             ));
             return frame.eax;
         } 
 
         String inst = "";
         switch (b.getOperation()) {
             case BINOP.AND:
                 inst = "and";
                 break;
             case BINOP.ARSHIFT:
                 inst = "sar";
                 break;
             case BINOP.LSHIFT:
                 inst = "shl";
                 break;
             case BINOP.MINUS:
                 inst = "sub";
                 break;
             case BINOP.OR:
                 inst = "or";
                 break;
             case BINOP.PLUS:
                 inst = "add";
                 break;
             case BINOP.RSHIFT:
                 inst = "shr";
                 break;
             case BINOP.XOR:
                 inst = "xor";
                 break;
         }
 
         // TODO: larger tiles and logic operations.
         Temp r = new Temp();
 
         // Left operand:
         if (b.getLeft() instanceof CONST) {
             // BINOP(?, CONST, ?):
             emit(new OPER(
                 "mov `d0, " + ((CONST)b.getLeft()).getValue(),
                 new List<Temp>(r, null),
                 null 
             ));
         } else {
             Temp left = munchExp(b.getLeft());
             emit(new assem.MOVE(r, left));
         }
 
         // Right operand:
         String operand = "";
         List<Temp> ulist = new List<Temp>(r, null);
         if (b.getRight() instanceof CONST) {
             // BINOP(?, ?, CONST):
             operand += ((CONST)b.getRight()).getValue();
         } else {
             operand = "`u0";
             Temp right = munchExp(b.getRight());
             ulist = new List<Temp>(right, ulist);
         }
         emit(new OPER(
             inst + " `d0, " + operand,
             new List<Temp>(r, null),
             ulist
         ));
 
         return r;
     }
 
     /**
      * Generates (selects) list of instructions for a list of IR nodes.
      * 
      * @param body
      * @return
      */
     public List<Instr> codegen(List<Stm> body) {
         ilist = last = null;
         for (Stm s : body) {
             munchStm(s);
         }
         return ilist;
     }
 }
