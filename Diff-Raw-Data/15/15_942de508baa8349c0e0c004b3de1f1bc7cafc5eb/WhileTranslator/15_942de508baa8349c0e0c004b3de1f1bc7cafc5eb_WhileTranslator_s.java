 /**
  * WhileTranslator.java
  *
  * A class used for translating an AST of the While language into Abstract
  * Machine code.
  */
 
 package wam.visitor;
 
 import java.util.*;
 
 import wam.ast.*;
 import wam.code.*;
 import wam.exception.*;
 
 public class WhileTranslator extends WhileVisitor<Stack<Instruction>> {
     @Override
     public Stack<Instruction> visit(final CompoundStatement cs) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         for (Statement s : cs.getStatements()) {
             Stack<Instruction> tmp = s.accept(this);
             tmp.addAll(code);
             code = tmp;
         }
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final While w) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         code.add(new Loop(w, w.getCondition().accept(this), w.getStatement().accept(this)));
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final IfElse ie) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         code.push(new Branch(ie, ie.getIfStatement().accept(this), ie.getElseStatement().accept(this)));
         code.addAll(ie.getCondition().accept(this));
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final Assignment a) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         code.push(new Store(a, a.getIdentifier().getName()));
         code.addAll(a.getValue().accept(this));
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final Skip s) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         code.push(new Instruction(s, Instruction.Type.SKIP));
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final BinaryOperation bo) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         code.push(new Instruction(bo, Instruction.operatorToType(bo.getOperator())));
        code.addAll(bo.getRightHand().accept(this));
         code.addAll(bo.getLeftHand().accept(this));
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final UnaryOperation uo) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         code.push(new Instruction(uo, Instruction.operatorToType(uo.getOperator())));
         code.addAll(uo.getExpression().accept(this));
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final IntegerValue v) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         code.push(new Push(v, v.getValue()));
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final BooleanValue v) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         Instruction.Type type = v.getValue() ? Instruction.Type.TRUE : Instruction.Type.FALSE;
         code.push(new Instruction(v, type));
         return code;
     }
 
     @Override
     public Stack<Instruction> visit(final Identifier id) throws WamException {
         Stack<Instruction> code = new Stack<Instruction>();
         code.push(new Fetch(id, id.getName()));
         return code;
     }
 }
