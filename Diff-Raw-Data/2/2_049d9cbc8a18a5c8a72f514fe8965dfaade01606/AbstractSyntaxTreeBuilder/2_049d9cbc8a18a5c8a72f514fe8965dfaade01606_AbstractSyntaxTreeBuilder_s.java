 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.ast;
 
 import fr.jamgotchian.abcd.core.ast.expr.ArrayCreationExpression;
 import fr.jamgotchian.abcd.core.ast.expr.AssignOperator;
 import fr.jamgotchian.abcd.core.ast.expr.ASTBinaryOperator;
 import fr.jamgotchian.abcd.core.ast.expr.Expression;
 import fr.jamgotchian.abcd.core.ast.expr.Expressions;
 import fr.jamgotchian.abcd.core.ast.expr.ObjectCreationExpression;
 import fr.jamgotchian.abcd.core.ast.expr.TypeExpression;
 import fr.jamgotchian.abcd.core.ast.expr.ASTUnaryOperator;
 import fr.jamgotchian.abcd.core.ast.expr.VariableExpression;
 import fr.jamgotchian.abcd.core.ast.stmt.BlockStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.BreakStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.DoWhileStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.ExpressionStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.IfStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.LabeledStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.MonitorEnterStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.MonitorExitStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.ReturnStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.Statement;
 import fr.jamgotchian.abcd.core.ast.stmt.SwitchCaseStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.SwitchCaseStatement.CaseStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.ThrowStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.TryCatchFinallyStatement;
 import fr.jamgotchian.abcd.core.ast.stmt.TryCatchFinallyStatement.CatchClause;
 import fr.jamgotchian.abcd.core.ast.stmt.WhileStatement;
 import fr.jamgotchian.abcd.core.ast.util.ExpressionInverter;
 import fr.jamgotchian.abcd.core.ir.IRInstSeq;
 import fr.jamgotchian.abcd.core.ir.BasicBlock;
 import fr.jamgotchian.abcd.core.ir.ControlFlowGraph;
 import fr.jamgotchian.abcd.core.ir.ArrayLengthInst;
 import fr.jamgotchian.abcd.core.ir.AssignConstInst;
 import fr.jamgotchian.abcd.core.ir.AssignVarInst;
 import fr.jamgotchian.abcd.core.ir.BasicBlockPropertyName;
 import fr.jamgotchian.abcd.core.ir.BasicBlockType;
 import fr.jamgotchian.abcd.core.ir.BinaryInst;
 import fr.jamgotchian.abcd.core.ir.ByteConst;
 import fr.jamgotchian.abcd.core.ir.CallMethodInst;
 import fr.jamgotchian.abcd.core.ir.CallStaticMethodInst;
 import fr.jamgotchian.abcd.core.ir.CaseValues;
 import fr.jamgotchian.abcd.core.ir.CastInst;
 import fr.jamgotchian.abcd.core.ir.ChildType;
 import fr.jamgotchian.abcd.core.ir.ChoiceInst;
 import fr.jamgotchian.abcd.core.ir.ClassConst;
 import fr.jamgotchian.abcd.core.ir.ConditionalInst;
 import fr.jamgotchian.abcd.core.ir.Const;
 import fr.jamgotchian.abcd.core.ir.DoubleConst;
 import fr.jamgotchian.abcd.core.ir.ExceptionHandlerInfo;
 import fr.jamgotchian.abcd.core.ir.FloatConst;
 import fr.jamgotchian.abcd.core.ir.GetArrayInst;
 import fr.jamgotchian.abcd.core.ir.GetFieldInst;
 import fr.jamgotchian.abcd.core.ir.GetStaticFieldInst;
 import fr.jamgotchian.abcd.core.ir.InstanceOfInst;
 import fr.jamgotchian.abcd.core.ir.IntConst;
 import fr.jamgotchian.abcd.core.ir.JumpIfInst;
 import fr.jamgotchian.abcd.core.ir.LongConst;
 import fr.jamgotchian.abcd.core.ir.MonitorEnterInst;
 import fr.jamgotchian.abcd.core.ir.MonitorExitInst;
 import fr.jamgotchian.abcd.core.ir.NewArrayInst;
 import fr.jamgotchian.abcd.core.ir.NewObjectInst;
 import fr.jamgotchian.abcd.core.ir.NullConst;
 import fr.jamgotchian.abcd.core.ir.ParentType;
 import fr.jamgotchian.abcd.core.ir.PhiInst;
 import fr.jamgotchian.abcd.core.ir.Region;
 import fr.jamgotchian.abcd.core.ir.ReturnInst;
 import fr.jamgotchian.abcd.core.ir.SetArrayInst;
 import fr.jamgotchian.abcd.core.ir.SetFieldInst;
 import fr.jamgotchian.abcd.core.ir.SetStaticFieldInst;
 import fr.jamgotchian.abcd.core.ir.ShortConst;
 import fr.jamgotchian.abcd.core.ir.StringConst;
 import fr.jamgotchian.abcd.core.ir.SwitchInst;
 import fr.jamgotchian.abcd.core.ir.IRInst;
 import fr.jamgotchian.abcd.core.ir.ThrowInst;
 import fr.jamgotchian.abcd.core.ir.UnaryInst;
 import fr.jamgotchian.abcd.core.ir.Variable;
 import fr.jamgotchian.abcd.core.ir.VariableID;
 import fr.jamgotchian.abcd.core.ir.EmptyIRInstVisitor;
 import fr.jamgotchian.abcd.core.ir.LiveVariablesAnalysis;
 import fr.jamgotchian.abcd.core.type.JavaType;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class AbstractSyntaxTreeBuilder {
 
     private static final Logger LOGGER = Logger.getLogger(AbstractSyntaxTreeBuilder.class.getName());
 
     private final ControlFlowGraph cfg;
 
     private final ImportManager importManager;
 
     private final Region rootRegion;
 
     private final BlockStatement methodBody;
 
     private Map<BasicBlock, Set<Variable>> liveVariables;
 
     private final Map<VariableID, Expression> expressions;
 
     private class RegionIRInstVisitor extends EmptyIRInstVisitor<Void, BlockStatement> {
 
         private RegionIRInstVisitor() {
         }
 
         private boolean isliveAtBB(BasicBlock bb, Variable var) {
             // is var live in bb ?
             for (IRInst inst : bb.getInstructions()) {
                 if (inst.getUses().contains(var)) {
                     return true;
                 }
             }
             // is var live at entry of predecessors
             for (BasicBlock s : cfg.getSuccessorsOf(bb)) {
                 if (liveVariables.get(s).contains(var)) {
                     return true;
                 }
             }
             return false;
         }
 
         private Expression getVarExpr(Variable var) {
             if (var.isTemporary()) {
                 Expression expr = expressions.get(var.getID());
                 if (expr == null) {
                     throw new IllegalStateException("Expression not found for variable "
                             + var.getID());
                 }
                 return expr;
             } else {
                 return Expressions.newVarExpr(var);
             }
         }
 
         private void addVarAssignExpr(Variable leftVar, Expression rightExpr,
                                       BlockStatement blockStmt) {
             if (leftVar.isTemporary()) {
                 expressions.put(leftVar.getID(), rightExpr);
             } else {
                 Expression varExpr
                         = Expressions.newVarExpr(leftVar);
                 Expression assignExpr
                         = Expressions.newAssignExpr(varExpr, rightExpr,
                                                     AssignOperator.ASSIGN);
                 blockStmt.add(new ExpressionStatement(assignExpr));
             }
         }
 
         @Override
         public Void visit(IRInstSeq seq, BlockStatement arg) {
             for (IRInst inst : seq) {
                 if (!inst.isIgnored()) {
                     inst.accept(this, arg);
                 }
             }
             return null;
         }
 
         @Override
         public Void visit(ArrayLengthInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable arrayVar = inst.getArray();
             Expression arrayExpr = getVarExpr(arrayVar);
             Expression lengthExpr = Expressions.newArrayLength(arrayExpr);
             addVarAssignExpr(resultVar, lengthExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(AssignConstInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Const _const = inst.getConst();
             Expression constExpr = null;
             if (_const instanceof IntConst) {
                 constExpr = Expressions.newIntExpr(((IntConst) _const).getValue());
             } else if (_const instanceof LongConst) {
                 constExpr = Expressions.newLongExpr(((LongConst) _const).getValue());
             } else if (_const instanceof ByteConst) {
                 constExpr = Expressions.newByteExpr(((ByteConst) _const).getValue());
             } else if (_const instanceof FloatConst) {
                 constExpr = Expressions.newFloatExpr(((FloatConst) _const).getValue());
             } else if (_const instanceof DoubleConst) {
                 constExpr = Expressions.newDoubleExpr(((DoubleConst) _const).getValue());
             } else if (_const instanceof ShortConst) {
                 constExpr = Expressions.newShortExpr(((ShortConst) _const).getValue());
             } else if (_const instanceof StringConst) {
                 constExpr = Expressions.newStringExpr(((StringConst) _const).getValue());
             } else if (_const instanceof ClassConst) {
                 constExpr = Expressions.newClsExpr(((ClassConst) _const).getClassName());
             } else if (_const instanceof NullConst) {
                 constExpr = Expressions.newNullExpr();
             } else {
                 throw new InternalError();
             }
             addVarAssignExpr(resultVar, constExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(AssignVarInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable valueVar = inst.getValue();
             Expression valueExpr = getVarExpr(valueVar);
             addVarAssignExpr(resultVar, valueExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(BinaryInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable leftVar = inst.getLeft();
             Variable rightVar = inst.getRight();
             Expression leftExpr = getVarExpr(leftVar);
             Expression rightExpr = getVarExpr(rightVar);
             ASTBinaryOperator op;
             switch (inst.getOperator()) {
                 case AND: op = ASTBinaryOperator.AND; break;
                 case DIV: op = ASTBinaryOperator.DIV; break;
                 case EQ: op = ASTBinaryOperator.EQ; break;
                 case GE: op = ASTBinaryOperator.GE; break;
                 case GT: op = ASTBinaryOperator.GT; break;
                 case LE: op = ASTBinaryOperator.LE; break;
                 case LOGICAL_SHIFT_RIGHT: op = ASTBinaryOperator.LOGICAL_SHIFT_RIGHT; break;
                 case LT: op = ASTBinaryOperator.LT; break;
                 case MINUS: op = ASTBinaryOperator.MINUS; break;
                 case MUL: op = ASTBinaryOperator.MUL; break;
                 case NE: op = ASTBinaryOperator.NE; break;
                 case OR: op = ASTBinaryOperator.OR; break;
                 case PLUS: op = ASTBinaryOperator.PLUS; break;
                 case REMAINDER: op = ASTBinaryOperator.REMAINDER; break;
                 case SHIFT_LEFT: op = ASTBinaryOperator.SHIFT_LEFT; break;
                 case SHIFT_RIGHT: op = ASTBinaryOperator.SHIFT_RIGHT; break;
                 case BITWISE_AND: op = ASTBinaryOperator.BITWISE_AND; break;
                 case BITWISE_OR: op = ASTBinaryOperator.BITWISE_OR; break;
                 case BITWISE_XOR: op = ASTBinaryOperator.BITWISE_XOR; break;
                 default: throw new InternalError();
             }
             Expression binExpr = Expressions.newBinExpr(leftExpr, rightExpr, op);
             addVarAssignExpr(resultVar, binExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(CallMethodInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             BasicBlock bb = resultVar.getBasicBlock();
             Variable objVar = inst.getObject();
             Expression objExpr = getVarExpr(objVar);
             List<Expression> argsExpr = new ArrayList<Expression>(inst.getArgumentCount());
             for (Variable argVar : inst.getArguments()) {
                 argsExpr.add(getVarExpr(argVar));
             }
             if (inst.getSignature().isConstructor()) {
                 // contructor call
                 if (objExpr instanceof ObjectCreationExpression) {
                     ((ObjectCreationExpression) objExpr).setArguments(argsExpr);
                 } else {
                     // TODO
                 }
             } else {
                 Expression callExpr
                         = Expressions.newMethodExpr(objExpr, inst.getSignature().getMethodName(),
                                                     argsExpr);
                 if (isliveAtBB(bb, resultVar)) {
                     expressions.put(resultVar.getID(), callExpr);
                 } else {
                     blockStmt.add(new ExpressionStatement(callExpr));
                 }
             }
             return null;
         }
 
         @Override
         public Void visit(CallStaticMethodInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             BasicBlock bb = resultVar.getBasicBlock();
             List<Expression> argsExpr = new ArrayList<Expression>(inst.getArgumentCount());
             for (Variable argVar : inst.getArguments()) {
                 argsExpr.add(getVarExpr(argVar));
             }
             Expression typeExpr
                     = Expressions.newTypeExpr(JavaType.newRefType(inst.getScope()));
             Expression callExpr
                     = Expressions.newMethodExpr(typeExpr, inst.getSignature().getMethodName(),
                                                 argsExpr);
             if (isliveAtBB(bb, resultVar)) {
                 expressions.put(resultVar.getID(), callExpr);
             } else {
                 blockStmt.add(new ExpressionStatement(callExpr));
             }
             return null;
         }
 
         @Override
         public Void visit(CastInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable var = inst.getVar();
             Expression expr = getVarExpr(var);
             Expression castExpr = Expressions.newCastExpr(inst.getCastType(), expr);
             addVarAssignExpr(resultVar, castExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(ConditionalInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable condVar = inst.getCond();
             Variable thenVar = inst.getThen();
             Variable elseVar = inst.getElse();
             Expression condExpr = ExpressionInverter.invert(getVarExpr(condVar));
             Expression thenExpr = getVarExpr(thenVar);
             Expression elseExpr = getVarExpr(elseVar);
             Expression ternExpr = Expressions.newCondExpr(condExpr, elseExpr, thenExpr);
             addVarAssignExpr(resultVar, ternExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(GetArrayInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable arrayVar = inst.getArray();
             Variable indexVar = inst.getIndex();
             Expression arrayExpr = getVarExpr(arrayVar);
             Expression indexExpr = getVarExpr(indexVar);
             Expression accessExpr = Expressions.newArrayAccess(arrayExpr, indexExpr);
             addVarAssignExpr(resultVar, accessExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(SetArrayInst inst, BlockStatement blockStmt) {
             Variable arrayVar = inst.getArray();
             Variable indexVar = inst.getIndex();
             Variable valueVar = inst.getValue();
             Expression arrayExpr = getVarExpr(arrayVar);
             Expression indexExpr = getVarExpr(indexVar);
             Expression valueExpr = getVarExpr(valueVar);
             if (arrayExpr instanceof ArrayCreationExpression) {
                 ((ArrayCreationExpression) arrayExpr).addInitValue(valueExpr);
             } else {
                 Expression accessExpr = Expressions.newArrayAccess(arrayExpr, indexExpr);
                 Expression assignExpr
                             = Expressions.newAssignExpr(accessExpr, valueExpr,
                                                         AssignOperator.ASSIGN);
                 blockStmt.add(new ExpressionStatement(assignExpr));
             }
             return null;
         }
 
         @Override
         public Void visit(GetFieldInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable objVar = inst.getObject();
             Expression objExpr = getVarExpr(objVar);
             Expression fieldExpr = Expressions.newFieldAccesExpr(objExpr, inst.getFieldName());
             addVarAssignExpr(resultVar, fieldExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(SetFieldInst inst, BlockStatement blockStmt) {
             Variable valueVar = inst.getValue();
             Expression valueExpr = getVarExpr(valueVar);
             Variable objVar = inst.getObject();
             Expression objExpr = getVarExpr(objVar);
             Expression fieldExpr = Expressions.newFieldAccesExpr(objExpr, inst.getFieldName());
             Expression assignExpr
                     = Expressions.newAssignExpr(fieldExpr, valueExpr,
                                                 AssignOperator.ASSIGN);
             blockStmt.add(new ExpressionStatement(assignExpr));
             return null;
         }
 
         @Override
         public Void visit(JumpIfInst inst, BlockStatement blockStmt) {
             Variable condVar = inst.getCond();
             Expression condExpr = getVarExpr(condVar);
             blockStmt.add(new IfStatement(condExpr));
             return null;
         }
 
         @Override
         public Void visit(InstanceOfInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable var = inst.getVar();
             Expression expr = getVarExpr(var);
             Expression typeExpr = Expressions.newTypeExpr(inst.getType());
             Expression instOfExpr = Expressions.newBinExpr(expr, typeExpr, ASTBinaryOperator.INSTANCE_OF);
             addVarAssignExpr(resultVar, instOfExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(MonitorEnterInst inst, BlockStatement blockStmt) {
             Variable objVar = inst.getObj();
             Expression objExpr = getVarExpr(objVar);
             blockStmt.add(new MonitorEnterStatement(objExpr));
             return null;
         }
 
         @Override
         public Void visit(MonitorExitInst inst, BlockStatement blockStmt) {
             Variable objVar = inst.getObj();
             Expression objExpr = getVarExpr(objVar);
             blockStmt.add(new MonitorExitStatement(objExpr));
             return null;
         }
 
         @Override
         public Void visit(NewArrayInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             List<Expression> dimExprs = new ArrayList<Expression>(inst.getDimensionCount());
             for (Variable dimVar : inst.getDimensions()) {
                 dimExprs.add(getVarExpr(dimVar));
             }
             Expression newArrExpr = Expressions.newArrayCreatExpr(inst.getType(), dimExprs);
             addVarAssignExpr(resultVar, newArrExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(NewObjectInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             List<Expression> argExprs = new ArrayList<Expression>(inst.getArgumentCount());
             for (Variable argVar : inst.getArguments()) {
                 argExprs.add(getVarExpr(argVar));
             }
             Expression newObjExpr = Expressions.newObjCreatExpr(inst.getType(), argExprs);
             addVarAssignExpr(resultVar, newObjExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(ReturnInst inst, BlockStatement blockStmt) {
             Variable var = inst.getVar();
             Statement retStmt;
             if (var == null) {
                 retStmt = new ReturnStatement();
             } else {
                 retStmt = new ReturnStatement(getVarExpr(var));
             }
             blockStmt.add(retStmt);
             return null;
         }
 
         @Override
         public Void visit(SwitchInst inst, BlockStatement blockStmt) {
             Variable indexVar = inst.getIndex();
             Expression indexExpr = getVarExpr(indexVar);
             SwitchCaseStatement switchStmt = new SwitchCaseStatement(indexExpr);
             blockStmt.add(switchStmt);
             return null;
         }
 
         @Override
         public Void visit(ThrowInst inst, BlockStatement blockStmt) {
             Variable var = inst.getVar();
             blockStmt.add(new ThrowStatement(getVarExpr(var)));
             return null;
         }
 
         @Override
         public Void visit(UnaryInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             Variable var = inst.getVar();
             Expression expr = getVarExpr(var);
             ASTUnaryOperator op;
             switch (inst.getOperator()) {
                 case MINUS:
                     op = ASTUnaryOperator.MINUS;
                     break;
 
                 case NOT:
                     op = ASTUnaryOperator.NOT;
                     break;
 
                 case NONE:
                     op = ASTUnaryOperator.NONE;
                     break;
 
                 default:
                     throw new InternalError();
             }
             Expression unaryExpr = Expressions.newUnaryExpr(expr, op);
             addVarAssignExpr(resultVar, unaryExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(ChoiceInst inst, BlockStatement blockStmt) {
             throw new InternalError();
         }
 
         @Override
         public Void visit(PhiInst inst, BlockStatement blockStmt) {
             throw new InternalError();
         }
 
         @Override
         public Void visit(GetStaticFieldInst inst, BlockStatement blockStmt) {
             Variable resultVar = inst.getResult();
             JavaType type = JavaType.newRefType(inst.getScope());
             TypeExpression typeExpr = Expressions.newTypeExpr(type);
             Expression fieldExpr = Expressions.newFieldAccesExpr(typeExpr, inst.getFieldName());
             addVarAssignExpr(resultVar, fieldExpr, blockStmt);
             return null;
         }
 
         @Override
         public Void visit(SetStaticFieldInst inst, BlockStatement blockStmt) {
             Variable valueVar = inst.getValue();
             Expression valueExpr = getVarExpr(valueVar);
             JavaType type = JavaType.newRefType(inst.getScope());
             TypeExpression typeExpr = Expressions.newTypeExpr(type);
             Expression fieldExpr = Expressions.newFieldAccesExpr(typeExpr, inst.getFieldName());
             Expression assignExpr
                     = Expressions.newAssignExpr(fieldExpr, valueExpr,
                                                 AssignOperator.ASSIGN);
             blockStmt.add(new ExpressionStatement(assignExpr));
             return null;
         }
     }
 
     public AbstractSyntaxTreeBuilder(ControlFlowGraph cfg, ImportManager importManager,
                                      Region rootRegion, BlockStatement methodBody) {
         this.cfg = cfg;
         this.importManager = importManager;
         this.rootRegion = rootRegion;
         this.methodBody = methodBody;
         expressions = new HashMap<VariableID, Expression>();
     }
 
     public void build() {
         this.liveVariables = new LiveVariablesAnalysis(cfg).analyse();
         buildAST(rootRegion, methodBody);
     }
 
     private void buildAST(Region region, BlockStatement blockStmt) {
         LOGGER.log(Level.FINEST, "Build AST from region {0} {1}",
                 new Object[] {region, region.getParentType()});
 
         switch (region.getParentType()) {
             case ROOT:
                 buildAST(region.getEntryChild(), blockStmt);
                 break;
 
             case TRIVIAL:
                 buildAST(region.getFirstChild(), blockStmt);
                 break;
 
             case BASIC_BLOCK: {
                 BasicBlock bb = region.getEntry();
                 if (bb.getType() != BasicBlockType.EMPTY) {
                     RegionIRInstVisitor visitor = new RegionIRInstVisitor();
                     bb.getInstructions().accept(visitor, blockStmt);
                 }
                 if (bb.hasProperty(BasicBlockPropertyName.BREAK_LABEL_EXIT_SOURCE)) {
                    blockStmt.add(new BreakStatement("L" + bb.getProperty(BasicBlockPropertyName.BREAK_LABEL_EXIT_TARGET)));
                 }
                 break;
             }
 
             case SEQUENCE:
                 buildAST(region.getFirstChild(ChildType.FIRST), blockStmt);
                 buildAST(region.getFirstChild(ChildType.SECOND), blockStmt);
                 break;
 
             case IF_THEN_ELSE: {
                 buildAST(region.getFirstChild(ChildType.IF), blockStmt);
                 IfStatement ifStmt = (IfStatement) blockStmt.getLast();
                 ifStmt.invertCondition();
                 BlockStatement thenBlockStmt = new BlockStatement();
                 ifStmt.setThen(thenBlockStmt);
                 BlockStatement elseBlockStmt = new BlockStatement();
                 ifStmt.setElse(elseBlockStmt);
                 buildAST(region.getFirstChild(ChildType.ELSE), elseBlockStmt);
                 buildAST(region.getFirstChild(ChildType.THEN), thenBlockStmt);
                 if (ifStmt.getThen().isEmpty() && ifStmt.getElse().isEmpty()) {
                     ifStmt.remove();
                 }
                 break;
             }
 
             case IF_THEN:
             case IF_NOT_THEN: {
                 buildAST(region.getFirstChild(ChildType.IF), blockStmt);
                 IfStatement ifStmt = (IfStatement) blockStmt.getLast();
                 if (region.getParentType() == ParentType.IF_NOT_THEN) {
                     ifStmt.invertCondition();
                 }
                 BlockStatement thenBlockStmt = new BlockStatement();
                 ifStmt.setThen(thenBlockStmt);
                 Region thenRegion = region.getFirstChild(ChildType.THEN);
                 buildAST(thenRegion, thenBlockStmt);
                 if (ifStmt.getThen().isEmpty()) {
                     ifStmt.remove();
                 }
                 break;
             }
 
             case BREAK_LABEL: {
                 BlockStatement bodyBlockStmt = new BlockStatement();
                 buildAST(region.getFirstChild(), bodyBlockStmt);
 
                 blockStmt.add(new LabeledStatement("L" + region.getData(), bodyBlockStmt));
 
                 break;
             }
 
             case WHILE_LOOP:
             case WHILE_LOOP_INVERTED_COND: {
                 BlockStatement bodyBlockStmt = new BlockStatement();
                 buildAST(region.getFirstChild(ChildType.LOOP_HEAD), bodyBlockStmt);
                 IfStatement ifStmt = (IfStatement) bodyBlockStmt.getLast();
                 if (bodyBlockStmt.hasSingleStatement()) {
                     ifStmt.remove();
                     Expression condition = ExpressionInverter.invert(ifStmt.getCondition());
                     buildAST(region.getFirstChild(ChildType.LOOP_TAIL), bodyBlockStmt);
                     blockStmt.add(new WhileStatement(condition, bodyBlockStmt));
                 } else {
                     BlockStatement thenStmt = new BlockStatement();
                     thenStmt.add(new BreakStatement());
                     ifStmt.setThen(thenStmt);
                     buildAST(region.getFirstChild(ChildType.LOOP_TAIL), bodyBlockStmt);
                     blockStmt.add(new WhileStatement(Expressions.newBooleanExpr(true), bodyBlockStmt));
                 }
                 break;
             }
 
             case DO_WHILE_LOOP: {
                 BlockStatement bodyBlockStmt = new BlockStatement();
                 buildAST(region.getFirstChild(), bodyBlockStmt);
                 IfStatement ifStmt = (IfStatement) bodyBlockStmt.getLast();
                 ifStmt.remove();
                 blockStmt.add(new DoWhileStatement(bodyBlockStmt, ifStmt.getCondition()));
                 break;
             }
 
             case SWITCH_CASE: {
                 Region switchRegion = region.getFirstChild(ChildType.SWITCH);
                 buildAST(switchRegion, blockStmt);
                 SwitchCaseStatement switchStmt
                         = (SwitchCaseStatement) blockStmt.getLast();
 
                 Map<CaseValues, Region> caseRegions = new TreeMap<CaseValues, Region>();
                 for (Region caseRegion : region.getChildren(ChildType.CASE)) {
                     CaseValues values = (CaseValues) caseRegion.getData();
                     caseRegions.put(values, caseRegion);
                 }
 
                 for (Map.Entry<CaseValues, Region> entry : caseRegions.entrySet()) {
                     CaseValues values = entry.getKey();
                     Region caseRegion = entry.getValue();
 
                     List<Statement> caseStmts = new ArrayList<Statement>();
 
                     BlockStatement caseCompoundStmt = new BlockStatement();
                     buildAST(caseRegion, caseCompoundStmt);
                     for (Statement stmt : caseCompoundStmt) {
                         caseStmts.add(stmt);
                     }
                     caseCompoundStmt.clear();
                     if (caseStmts.isEmpty() || !(caseStmts.get(caseStmts.size()-1) instanceof ReturnStatement)) {
                         caseStmts.add(new BreakStatement());
                     }
 
                     switchStmt.addCase(new CaseStatement(values, caseStmts));
                 }
 
                 // empty cases
                 if (switchRegion.getData() != null) {
                     switchStmt.addCase(new CaseStatement((CaseValues) switchRegion.getData(),
                                                          Collections.<Statement>singletonList(new BreakStatement())));
                 }
 
                 break;
             }
 
             case INLINED_FINALLY: {
                 // do nothing
                 break;
             }
 
             case TRY_CATCH_FINALLY: {
                 BlockStatement tryBlockStmt = new BlockStatement();
                 buildAST(region.getFirstChild(ChildType.TRY), tryBlockStmt);
 
                 List<CatchClause> catchClauses = new ArrayList<CatchClause>();
                 BlockStatement finallyBlockStmt = null;
 
                 for (Region catchRegion : region.getChildren(ChildType.CATCH)) {
                     BlockStatement catchBlockStmt = new BlockStatement();
                     buildAST(catchRegion, catchBlockStmt);
 
                     ExceptionHandlerInfo info
                             = (ExceptionHandlerInfo) catchRegion.getEntry().getProperty(BasicBlockPropertyName.EXCEPTION_HANDLER_ENTRY);
                     Variable excVar = info.getVariable();
                     VariableExpression excVarExpr = Expressions.newVarExpr(excVar);
                     catchClauses.add(new CatchClause(catchBlockStmt, excVarExpr));
                 }
                 Region finallyRegion = region.getFirstChild(ChildType.FINALLY);
                 if (finallyRegion != null) {
                     finallyBlockStmt = new BlockStatement();
                     buildAST(finallyRegion, finallyBlockStmt);
                 }
 
                 TryCatchFinallyStatement tryCatchStmt
                         = new TryCatchFinallyStatement(tryBlockStmt, catchClauses, finallyBlockStmt);
                 blockStmt.add(tryCatchStmt);
 
                 break;
             }
         }
     }
 }
