 package com.lolcode.tree;
 
 import com.lolcode.lolcodeBaseVisitor;
 import com.lolcode.lolcodeParser;
 import org.antlr.v4.runtime.misc.NotNull;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 7/16/13
  * Time: 4:43 PM
  */
 
 /**
  * Builds AST from parse tree. Resulting tree is stored in root.
  *
  * @param <T>
  */
 public class AstBuilder<T extends TreeNode> extends lolcodeBaseVisitor<T> {
     private TreeNode root;
     private static Logger log = Logger.getLogger(AstBuilder.class.getName());
 
     public AstBuilder() {
         root = null;
     }
 
     @Override
     public T visitFuncall(lolcodeParser.FuncallContext ctx) {
         //funcall is actually funexpr, stupid i know
         return super.visitFuncall(ctx);
     }
 
     @Override
     public T visitValue(@NotNull lolcodeParser.ValueContext ctx) {
         TreeConstant constant = new TreeConstant();
         if (ctx.BOOL() != null) {
             constant.setType(TreeTypedValue.TYPE.BOOL);
             constant.fromString(ctx.BOOL().toString());
         } else if (ctx.INT() != null) {
             constant.setType(TreeTypedValue.TYPE.INT);
             constant.fromString(ctx.INT().toString());
         } else if (ctx.FLOAT() != null) {
             constant.setType(TreeTypedValue.TYPE.FLOAT);
             constant.fromString(ctx.FLOAT().toString());
         } else if (ctx.STRING() != null) {
             constant.setType(TreeTypedValue.TYPE.STRING);
             constant.fromString(ctx.STRING().toString());
         } else {
             log.warning("All types for constant failed to parse");
             constant.setType(TreeTypedValue.TYPE.UNKNOWN);
         }
         return (T) constant;
     }
 
     @Override
     public T visitFormalParameter(lolcodeParser.FormalParameterContext ctx) {
         TreeFunctionParameter param = new TreeFunctionParameter();
         param.setName(ctx.ID().toString());
         return (T) param;
     }
 
     @Override
     public T visitVarDecl(lolcodeParser.VarDeclContext ctx) {
         TreeVarDeclStmt varDeclStmt = new TreeVarDeclStmt();
         TreeVariable variable = new TreeVariable();
         variable.setName(ctx.ID().toString());
         if (ctx.expr() != null) {
             varDeclStmt.setInitialValue((TreeExpression) visit(ctx.expr()));
         }
         varDeclStmt.setVar(variable);
         return (T) varDeclStmt;
     }
 
     @Override
     public T visitBlock(lolcodeParser.BlockContext ctx) {
         return super.visitBlock(ctx);
     }
 
     @Override
     public T visitGimstat(lolcodeParser.GimstatContext ctx) {
         return super.visitGimstat(ctx);
     }
 
     @Override
     public T visitExpr(lolcodeParser.ExprContext ctx) {
         if (ctx.ID() != null) {
             TreeVariable variable = new TreeVariable();
             variable.setName(ctx.ID().toString());
             return (T) variable;
         }
         return super.visitExpr(ctx);
     }
 
     @Override
     public T visitAssstat(lolcodeParser.AssstatContext ctx) {
         TreeAssignStmt assignStmt = new TreeAssignStmt();
         if (ctx.ID() != null) {
             TreeVariable lhs = new TreeVariable();
             lhs.setName(ctx.ID().toString());
             assignStmt.setLhs(lhs);
         } else {
             log.severe("variable id in assignment failed to parse");
         }
         if (ctx.expr() != null) {
             TreeExpression expression = (TreeExpression) visit(ctx.expr());
             assignStmt.setRhs(expression);
         } else {
             log.severe("rhs part of assignment failed to parse");
         }
         return (T) assignStmt;
     }
 
     @Override
     public T visitVisstat(lolcodeParser.VisstatContext ctx) {
         TreeVisibleStmt visibleStmt = new TreeVisibleStmt();
         if (ctx.expr() == null) {
             log.log(Level.SEVERE, "Visible statement has no argument");
         } else {
             visibleStmt.setArgument((TreeExpression) visit(ctx.expr()));
         }
         return (T) visibleStmt;
     }
 
     // AM I DOING IT RIGHT??????
     @Override
     public T visitNotexpr(lolcodeParser.NotexprContext ctx) {
         TreeNotExpr notExpr = new TreeNotExpr();
         if (ctx.expr() == null) {
             log.log(Level.SEVERE, "Not expression has no argument");
         } else {
             notExpr.setExpr((TreeExpression) visit(ctx.expr()));
         }
         //return super.visitNotexpr(ctx);
         return (T) notExpr;
     }
 
     @Override
     public T visitCasestat(lolcodeParser.CasestatContext ctx) {
 
         return super.visitCasestat(ctx);
     }
 
     @Override
     public T visitFile(lolcodeParser.FileContext ctx) {
         if (root != null) {
             log.log(Level.SEVERE, "visitFile() called more than once for same file!");
         }
         TreeModule module = new TreeModule();
         module.setModuleName("MODULENAME");
         if (ctx.functionDecl().isEmpty()) {
             log.log(Level.WARNING, "No function declarations found in module.");
         } else {
             int i = 0;
             for (lolcodeParser.FunctionDeclContext fDecl : ctx.functionDecl()) {
                 module.addFunction((TreeFunction) visit(fDecl));
                 i++;
             }
             log.log(Level.INFO, "Got " + i + " functions in a module");
         }
         if (ctx.main() == null) {
             log.log(Level.SEVERE, "No module body found in module.");
         } else {
             if (ctx.main().isEmpty()) {
                 log.log(Level.WARNING, "No body statements found in module.");
             } else {
                 for (lolcodeParser.StatContext statement : ctx.main().stat()) {
                     module.addStatement((TreeStatement) visit(statement));
                 }
             }
         }
         root = module;
         return (T) module;
     }
 
     @Override
     public T visitIfstat(lolcodeParser.IfstatContext ctx) {
         return super.visitIfstat(ctx);
     }
 
     @Override
     public T visitExprList(lolcodeParser.ExprListContext ctx) {
         return super.visitExprList(ctx);
     }
 
     @Override
     public T visitOneofexpr(lolcodeParser.OneofexprContext ctx) {
         return super.visitOneofexpr(ctx);
     }
 
     @Override
     public T visitLoopstat(lolcodeParser.LoopstatContext ctx) {
         return super.visitLoopstat(ctx);
     }
 
     @Override
     public T visitFunexpr(lolcodeParser.FunexprContext ctx) {
         TreeFuncCallStmt funcCallStmt = new TreeFuncCallStmt();
         funcCallStmt.setFuncName(ctx.ID().toString());
         if (ctx.exprList() == null) {
             log.info("function call " + funcCallStmt.getFuncName() + "has no parameters");
         } else {
             for (lolcodeParser.ExprContext param : ctx.exprList().expr()) {
                funcCallStmt.addArgument((TreeFunctionParameter) visit(param));
             }
         }
         return (T) funcCallStmt;
         //return super.visitFunexpr(ctx);
     }
 
     @Override
     public T visitMaxexpr(@NotNull lolcodeParser.MaxexprContext ctx) {
         TreeMaxExpr maxExpr = new TreeMaxExpr();
         List<lolcodeParser.ExprContext> list = ctx.expr();
         TreeExpression lhs = (TreeExpression) visit(list.get(0));
         maxExpr.setLhs(lhs);
         TreeExpression rhs = (TreeExpression) visit(list.get(0));
         maxExpr.setRhs(rhs);
         return (T) maxExpr;
         //return super.visitMaxexpr(ctx);
     }
 
     @Override
     public T visitMinexpr(@NotNull lolcodeParser.MinexprContext ctx) {
         TreeMinExpr minExpr = new TreeMinExpr();
         List<lolcodeParser.ExprContext> list = ctx.expr();
         TreeExpression lhs = (TreeExpression) visit(list.get(0));
         minExpr.setLhs(lhs);
         TreeExpression rhs = (TreeExpression) visit(list.get(0));
         minExpr.setRhs(rhs);
         return (T) minExpr;
         //return super.visitMinexpr(ctx);
     }
 
     //AM I DOING IT RIGHT???
     @Override
     public T visitModexpr(lolcodeParser.ModexprContext ctx) {
         TreeModExpr modExpr = new TreeModExpr();
         List<lolcodeParser.ExprContext> list = ctx.expr();
         if (list.get(0) != null) {
             TreeExpression lhs = (TreeExpression) visit(list.get(0));
             modExpr.setLhs(lhs);
         } else {
             log.severe("lhs of modExpr failed to parse");
         }
         if (list.get(1) != null) {
             TreeExpression rhs = (TreeExpression) visit(list.get(1));
             modExpr.setRhs(rhs);
         } else {
             log.severe("rhs of modExpr failed to parse");
         }
         return (T) modExpr;
         //return super.visitModexpr(ctx);
     }
 
     @Override
     public T visitBothofexpr(lolcodeParser.BothofexprContext ctx) {
         return super.visitBothofexpr(ctx);
     }
 
     @Override
     public T visitAddexpr(lolcodeParser.AddexprContext ctx) {
         TreeSumExpr sumExpr = new TreeSumExpr();
         List<lolcodeParser.ExprContext> list = ctx.expr();
         if (list.get(0) != null) {
             TreeExpression lhs = (TreeExpression) visit(list.get(0));
             sumExpr.setLhs(lhs);
         } else {
             log.severe("lhs of sumExpr failed to parse");
         }
         if (list.get(1) != null) {
             TreeExpression rhs = (TreeExpression) visit(list.get(1));
             sumExpr.setRhs(rhs);
         } else {
             log.severe("rhs of SumExpr failed to parse");
         }
         return (T) sumExpr;
     }
 
     @Override
     public T visitStat(lolcodeParser.StatContext ctx) {
         return super.visitStat(ctx);
     }
 
     @Override
     public T visitFunctionDecl(lolcodeParser.FunctionDeclContext ctx) {
         TreeFunction func = new TreeFunction();
         func.setName(ctx.ID().toString());
         if (ctx.formalParameters() == null) {
             log.info("function " + func.getName() + "has no parameters");
         } else {
             for (lolcodeParser.FormalParameterContext param : ctx.formalParameters().formalParameter()) {
                 func.addParam((TreeFunctionParameter) visit(param));
             }
         }
         if (ctx.block() == null) {
             log.log(Level.SEVERE, "function body failed to parse");
         } else {
             for (lolcodeParser.StatContext stat : ctx.block().stat()) {
                 func.addStmt((TreeStatement) visit(stat));
             }
         }
         return (T) func;
     }
 
     @Override
     public T visitMain(lolcodeParser.MainContext ctx) {
         return super.visitMain(ctx);
     }
 
     //AM I DOING IT RIGHT???
     @Override
     public T visitDivexpr(@NotNull lolcodeParser.DivexprContext ctx) {
         TreeDivExpr divExpr = new TreeDivExpr();
         List<lolcodeParser.ExprContext> list = ctx.expr();
         if (list.get(0) != null) {
             TreeExpression lhs = (TreeExpression) visit(list.get(0));
             divExpr.setLhs(lhs);
         } else {
             log.severe("lhs of Div Expression failed to parse");
         }
         if (list.get(1) != null) {
             TreeExpression rhs = (TreeExpression) visit(list.get(1));
             divExpr.setRhs(rhs);
         } else {
             log.severe("rgs of Div Expression failed to parse");
         }
         return (T) divExpr;
         //return super.visitDivexpr(ctx);
     }
 
     @Override
     public T visitRetpart(lolcodeParser.RetpartContext ctx) {
         return super.visitRetpart(ctx);
     }
 
     @Override
     public T visitFormalParameters(lolcodeParser.FormalParametersContext ctx) {
 
         return super.visitFormalParameters(ctx);
     }
 
     @Override
     public T visitSubexpr(@NotNull lolcodeParser.SubexprContext ctx) {
         return super.visitSubexpr(ctx);
     }
 
     @Override
     public T visitEitherexpr(lolcodeParser.EitherexprContext ctx) {
         return super.visitEitherexpr(ctx);
     }
 
     @Override
     public T visitEquexpr(lolcodeParser.EquexprContext ctx) {
         return super.visitEquexpr(ctx);
     }
 
     @Override
     public T visitNequexpr(@NotNull lolcodeParser.NequexprContext ctx) {
         return super.visitNequexpr(ctx);
     }
 
     //AM I DOING IT RIGHT???
     @Override
     public T visitMultexpr(lolcodeParser.MultexprContext ctx) {
         TreeMulExpr mulExpr = new TreeMulExpr();
         List<lolcodeParser.ExprContext> list = ctx.expr();
         if (list.get(0) != null) {
             TreeExpression lhs = (TreeExpression) visit(list.get(0));
             mulExpr.setLhs(lhs);
         } else {
             log.severe("lhs of Mul Expression failed to parse");
         }
         if (list.get(1) != null) {
             TreeExpression rhs = (TreeExpression) visit(list.get(1));
             mulExpr.setRhs(rhs);
         } else {
             log.severe("rgs of Mul Expression failed to parse");
         }
         return (T) mulExpr;
         //return super.visitMultexpr(ctx);
     }
 }
