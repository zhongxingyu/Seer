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
      * Helper function that returns the correct string for a memory access in an
      * instruction. Valid memory accesses are:
      * [reg], [reg+const] and [reg-const].
      * 
      * @param m
      * @param idx Base index for the first register in the string (`u<idx>).
      * @return
      */
     private String getMemAddressString(MEM m, int idx) {
         // TODO: try [reg0+reg1] and [`u0+4*`u1] tiles.
         Exp e = m.getExpression();
         
         // MEM(BINOP(-, TEMP, CONST)):
         if (e instanceof BINOP &&
             ((BINOP)e).getOperation() == BINOP.MINUS &&
             ((BINOP)e).getLeft() instanceof TEMP &&
             ((BINOP)e).getRight() instanceof CONST) {
             long c = ((CONST)((BINOP)e).getRight()).getValue();
             return "[`u" + idx + ((c != 0) ? ("-" + c) : "") + "]";
         }
 
         // MEM(BINOP(+, TEMP, CONST)):
         if (e instanceof BINOP &&
             ((BINOP)e).getOperation() == BINOP.PLUS &&
             ((BINOP)e).getLeft() instanceof TEMP &&
             ((BINOP)e).getRight() instanceof CONST) {
             long c = ((CONST)((BINOP)e).getRight()).getValue();
             return "[`u" + idx + ((c != 0) ? ("+" + c) : "") + "]";
         }
 
         // MEM(BINOP(+, CONST, TEMP)):
         if (e instanceof BINOP &&
             ((BINOP)e).getOperation() == BINOP.PLUS &&
             ((BINOP)e).getLeft() instanceof CONST &&
             ((BINOP)e).getRight() instanceof TEMP) {
             long c = ((CONST)((BINOP)e).getLeft()).getValue();
             return "[`u" + idx + ((c != 0) ? ("+" + c) : "") + "]";
         }
         
         // MEM(BINOP(+, EXP, EXP)):
         if (e instanceof BINOP && ((BINOP)e).getOperation() == BINOP.PLUS) {
             return "[`u" + idx + "+`u" + (idx + 1) + "]";
         }
 
         return "[`u" + idx + "]";
     }
 
     /**
      * Helper function like the one above that returns the temporary register
      * instead.
      * 
      * @param m
      * @return
      */
     private List<Temp> getMemAddressTempList(MEM m) {
         Exp e = m.getExpression();
 
         // MEM(BINOP(-, TEMP, CONST)):
         if (e instanceof BINOP &&
             ((BINOP)e).getOperation() == BINOP.MINUS &&
             ((BINOP)e).getLeft() instanceof TEMP &&
             ((BINOP)e).getRight() instanceof CONST) {
             return new List<Temp>(munchExp(((BINOP)e).getLeft()), null);
         }
 
         // MEM(BINOP(+, TEMP, CONST)):
         if (e instanceof BINOP &&
             ((BINOP)e).getOperation() == BINOP.PLUS &&
             ((BINOP)e).getLeft() instanceof TEMP &&
             ((BINOP)e).getRight() instanceof CONST) {
             return new List<Temp>(munchExp(((BINOP)e).getLeft()), null);
         }
 
         // MEM(BINOP(+, CONST, TEMP)):
         if (e instanceof BINOP &&
             ((BINOP)e).getOperation() == BINOP.PLUS &&
             ((BINOP)e).getLeft() instanceof CONST &&
             ((BINOP)e).getRight() instanceof TEMP) {
             return new List<Temp>(munchExp(((BINOP)e).getRight()), null);
         }
 
         // MEM(BINOP(+, TEMP, TEMP)):
         if (e instanceof BINOP && ((BINOP)e).getOperation() == BINOP.PLUS) {
             return new List<Temp>(
                 munchExp(((BINOP)e).getLeft()),
                 new List<Temp>(munchExp(((BINOP)e).getRight()), null)
             );
         }
 
         return new List<Temp>(munchExp(e), null);
     }
 
     /**
      * Helper function that evaluates a BINOP between two constants.
      * 
      * @param op
      * @param c1
      * @param c2
      * @return
      */
     private long evaluateBinop(int op, CONST c1, CONST c2) {
         long result = 0;
         long v1 = c1.getValue();
         long v2 = c2.getValue();
 
         switch (op) {
             case BINOP.AND:
                 result = v1 & v2;
                 break;
             case BINOP.ARSHIFT:
                 result = v1 >> v2;
                 break;
             case BINOP.DIV:
                 result = v1 / v2;
                 break;
             case BINOP.LSHIFT:
                 result = v1 << v2;
                 break;
             case BINOP.MINUS:
                 result = v1 - v2;
                 break;
             case BINOP.OR:
                 result = v1 | v2;
                 break;
             case BINOP.PLUS:
                 result = v1 + v2;
                 break;
             case BINOP.RSHIFT:
                 result = v1 >>> v2;
                 break;
             case BINOP.TIMES:
                 result = v1 * v2;
                 break;
             case BINOP.XOR:
                 result = v1 ^ v2;
                 break;          
         }
 
         return result;
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
             emit(new OPER(
                 "jmp `u0",
                 null,
                 new List<Temp>(u, null),
                 j.getTargets()
             ));
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
 
         if (c.getLeft() instanceof MEM) {
             String cmp0 = this.getMemAddressString((MEM)c.getLeft(), 0);
             List<Temp> ulist = this.getMemAddressTempList((MEM)c.getLeft());
             
             String cmp1 = "";
             if (c.getRight() instanceof CONST) {
                 // CJUMP(OP, MEM, CONST):
                 cmp0 = "dword " + cmp0;
                 cmp1 += ((CONST)c.getRight()).getValue();
             } else {
                 // CJUMP(OP, MEM, EXP):
                 ulist.addAll(new List<Temp>(munchExp(c.getRight()), null));
                 cmp1 = "`u" + (ulist.size() - 1);
             }             
             
             emit(new OPER("cmp " + cmp0 + ", " + cmp1, null, ulist));            
         } else {
             Temp u0 = munchExp(c.getLeft());
 
             String cmp1 = "";
             List <Temp> ulist;
             if (c.getRight() instanceof CONST) {
                 // CJUMP(OP, EXP, CONST):
                 cmp1 += ((CONST)c.getRight()).getValue();
                 ulist = new List<Temp>(u0, null);
             } else if (c.getRight() instanceof MEM) {
                 // CJUMP(OP, EXP, MEM):
                 cmp1 = this.getMemAddressString((MEM)c.getRight(), 1);
                 ulist = this.getMemAddressTempList((MEM)c.getRight());
                 ulist = new List<Temp>(u0, ulist);
             } else {
                 cmp1 = "`u1";
                 Temp u1 = munchExp(c.getRight());
                 ulist = new List<Temp>(u0, new List<Temp>(u1, null));
             }
             
             emit(new OPER("cmp `u0, " + cmp1, null, ulist));
         }
 
         Label next = new Label();
         Label ltrue = c.getLabelTrue();
         Label lfalse = c.getLabelFalse();
         emit(new OPER(
             inst + " `j0",
             new List<Label>(ltrue, new List<Label>(next, null))
         ));
         emit(new assem.LABEL(next.toString() + ":", next));
         emit(new OPER("jmp `j0", new List<Label>(lfalse, null)));
     }
 
     /**
      * Emits instructions for a given Tree.Stm.MOVE.
      *
      * @param m
      */
     void munchStm(MOVE m) {
         Exp src = m.getSource();
         Exp dst = m.getDestination();
 
         // MOVE(MEM, EXP):
         if (dst instanceof MEM) {
             String sdst = this.getMemAddressString((MEM)dst, 0);
             List<Temp> ulist = this.getMemAddressTempList((MEM)dst);
             
             String ssrc = "";
             if (src instanceof CONST) {
                 // MOVE(MEM, CONST):
                 sdst = "dword " + sdst;
                 ssrc += ((CONST)src).getValue();
             } else if (src instanceof BINOP &&
                 ((BINOP)src).getLeft() instanceof CONST &&
                 ((BINOP)src).getRight() instanceof CONST) {
                 // MOVE(MEM, BINOP(?, CONST, CONST)):
                 sdst = "dword " + sdst;
                 ssrc += this.evaluateBinop(
                     ((BINOP)src).getOperation(),
                     (CONST)((BINOP)src).getLeft(),
                     (CONST)((BINOP)src).getRight()
                 );
             } else {
                 ulist.addAll(new List<Temp>(munchExp(src), null));
                 ssrc = "`u" + (ulist.size() - 1);
             }             
             
             emit(new OPER("mov " + sdst + ", " + ssrc, null, ulist)); 
             return;
         }
 
         // MOVE(TEMP, CONST):
         if (src instanceof CONST) {
             Temp tdst = munchExp(dst);
             emit(new OPER(
                 "mov `d0, " + ((CONST)src).getValue(),
                 new List<Temp>(tdst, null),
                 null
             ));
             return;
         }
         
         // MOVE(TEMP, MEM):
         if (src instanceof MEM) {
             Temp tdst = munchExp(dst);
             emit(new OPER(
                "mov `d0, " + this.getMemAddressString((MEM)src, 0),
                 new List<Temp>(tdst, null),
                 this.getMemAddressTempList((MEM)src)
             ));
             return;
         }
         
         // MOVE(TEMP, BINOP(?, CONST, CONST)):
         if (src instanceof BINOP &&
             ((BINOP)src).getLeft() instanceof CONST &&
             ((BINOP)src).getRight() instanceof CONST) {
             Temp tdst = munchExp(dst);
             long result = this.evaluateBinop(
                 ((BINOP)src).getOperation(),
                 (CONST)((BINOP)src).getLeft(),
                 (CONST)((BINOP)src).getRight()
             );
             emit(new OPER(
                 "mov `d0, " + result,
                 new List<Temp>(tdst, null),
                 null
             ));
             return;           
         }
 
         // MOVE(TEMP, EXP):
         emit(new assem.MOVE(munchExp(dst), munchExp(src)));
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
         emit(new OPER(
             "mov `d0, " + this.getMemAddressString(m, 0),
             new List<Temp>(r, null),
             this.getMemAddressTempList(m)
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
         List<Temp> ulist;
         List<Exp> args = c.getArguments();
 
         String source = "";
         Exp e = c.getCallable();
         if (e instanceof NAME) {
             // CALL(NAME):
             source = ((NAME)e).getLabel().toString();
             ulist = munchArgs(args);
         } else {
             Temp u;
             if (e instanceof MEM) {
                 // CALL(MEM):
                 // TODO: use getMemAddress* once we treat [reg+4*reg].
                 source = "[`u0]";
                 u = munchExp(((MEM)e).getExpression());
             } else {
             	source = "`u0";
                 u = munchExp(e);
             }
             ulist = new List<Temp>(u, munchArgs(args));
         }
         emit(new OPER("call " + source, frame.calleeDefs(), ulist));
 
         // Restore the stack:
         if (args.size() > 0) {
             emit(new OPER(
                 "add `d0, " + (frame.wordsize() * args.size()),
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
         } else if (args.head instanceof NAME) {
             // PUSH(NAME):
             source += ((NAME)args.head).getLabel().toString();
             ulist = new List<Temp>(frame.SP(), null);
         } else if (args.head instanceof MEM) {
             // PUSH(MEM):
             source += "dword " + this.getMemAddressString(((MEM)args.head), 0);
             ulist = this.getMemAddressTempList(((MEM)args.head));
             ulist.addAll(new List<Temp>(frame.SP(), null));
         } else if (args.head instanceof BINOP &&
             (((BINOP)args.head).getLeft() instanceof CONST) &&
             (((BINOP)args.head).getRight() instanceof CONST)) {
             // PUSH(BINOP(?, CONST, CONST)):
             source += this.evaluateBinop(
                 ((BINOP)args.head).getOperation(),
                 (CONST)((BINOP)args.head).getLeft(),
                 (CONST)((BINOP)args.head).getRight()
             );
             ulist = new List<Temp>(frame.SP(), null);
         } else {
         	source = "`u0";
             Temp u = munchExp(args.head);
             rlist = new List<Temp>(u, rlist);
             ulist = new List<Temp>(u, new List<Temp>(frame.SP(), null));
         }
 
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
         Temp r = new Temp();
 
         // BINOP(CONST, CONST):
         if ((b.getLeft() instanceof CONST) && (b.getRight() instanceof CONST)) {
             long result = this.evaluateBinop(
                 b.getOperation(),
                 (CONST)b.getLeft(),
                 (CONST)b.getRight()
             );
         	emit(new OPER("mov `d0, " + result, new List<Temp>(r, null), null));
         }
 
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
 
             emit(new assem.MOVE(r, frame.eax));
             return r;
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
 
             emit(new assem.MOVE(r, frame.eax));
             return r;
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
