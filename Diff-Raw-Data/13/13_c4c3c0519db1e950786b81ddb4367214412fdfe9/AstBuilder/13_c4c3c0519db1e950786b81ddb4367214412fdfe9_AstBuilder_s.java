 package com.lolcode.tree;
 
 import com.lolcode.checker.ErrorHandler;
 import com.lolcode.lolcodeBaseVisitor;
 import com.lolcode.lolcodeParser;
 import org.antlr.v4.runtime.misc.NotNull;
 
 import java.util.ArrayList;
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
  */
 public class AstBuilder extends lolcodeBaseVisitor<TreeNode> {
     private TreeNode root;
     private static Logger log = Logger.getLogger(AstBuilder.class.getName());
     private final String filename;
 
     public AstBuilder(String filename) {
         this.filename = filename;
         root = null;
     }
 
 
     @Override
     public TreeConstant visitValue(@NotNull lolcodeParser.ValueContext ctx) {
         TreeConstant constant = new TreeConstant();
         constant.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         if (ctx.BOOL() != null) {
             constant.setType(TYPE.BOOL);
             constant.fromString(ctx.BOOL().toString());
         } else if (ctx.INT() != null) {
             constant.setType(TYPE.INT);
             constant.fromString(ctx.INT().toString());
         } else if (ctx.FLOAT() != null) {
             constant.setType(TYPE.FLOAT);
             constant.fromString(ctx.FLOAT().toString());
         } else if (ctx.STRING() != null) {
             constant.setType(TYPE.STRING);
            constant.fromString(ctx.STRING().toString().replaceAll("\"", ""));
         } else {
             log.warning("All types for constant failed to parse");
             constant.setType(TYPE.UNKNOWN);
         }
         return constant;
     }
 
     @Override
     public TreeFunctionParameter visitFormalParameter(lolcodeParser.FormalParameterContext ctx) {
         TreeFunctionParameter param = new TreeFunctionParameter();
         param.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         param.setName(ctx.ID().toString());
         return param;
     }
 
     @Override
     public TreeVarDeclStmt visitVarDecl(lolcodeParser.VarDeclContext ctx) {
         TreeVarDeclStmt varDeclStmt = new TreeVarDeclStmt();
         TreeNode.position pos = new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine());
         varDeclStmt.setPos(pos);
         TreeVariable variable = new TreeVariable();
         variable.setPos(pos);
         variable.setName(ctx.ID().toString());
         if (ctx.expr() != null) {
             varDeclStmt.setInitialValue((TreeExpression) visit(ctx.expr()));
         }
         varDeclStmt.setVar(variable);
         return varDeclStmt;
     }
 
     @Override
     public TreeNode visitArrayDecl(@NotNull lolcodeParser.ArrayDeclContext ctx) {
         TreeArrayDeclStmt arrayDeclStmt = new TreeArrayDeclStmt();
         TreeNode.position pos = new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine());
         arrayDeclStmt.setPos(pos);
         TreeVariable array = new TreeVariable();
         array.setPos(new TreeNode.position(ctx.start.getLine(), ctx.ID().getSymbol().getCharPositionInLine()));
         array.setName(ctx.ID().toString());
         array.setType(TYPE.ARRAY);
         arrayDeclStmt.setArray(array);
         return arrayDeclStmt;
     }
 
     @Override
     public TreeNode visitArrayputstat(@NotNull lolcodeParser.ArrayputstatContext ctx) {
         TreeArrayPutStmt arrayPutExpr = new TreeArrayPutStmt();
         TreeNode.position pos = new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine());
         arrayPutExpr.setPos(pos);
         TreeVariable array = new TreeVariable();
         array.setName(ctx.ID().toString());
         array.setPos(new TreeNode.position(ctx.start.getLine(), ctx.ID().getSymbol().getCharPositionInLine()));
         arrayPutExpr.setArray(array);
         arrayPutExpr.setKey((TreeExpression) visit(ctx.expr().get(0)));
         arrayPutExpr.setValue((TreeExpression) visit(ctx.expr().get(1)));
         return arrayPutExpr;
     }
 
     @Override
     public TreeNode visitArraygetexpr(@NotNull lolcodeParser.ArraygetexprContext ctx) {
         TreeArrayGetExpr arrayGetExpr = new TreeArrayGetExpr();
         TreeNode.position pos = new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine());
         arrayGetExpr.setPos(pos);
         TreeVariable array = new TreeVariable();
         array.setName(ctx.ID().toString());
         array.setPos(new TreeNode.position(ctx.start.getLine(), ctx.ID().getSymbol().getCharPositionInLine()));
         arrayGetExpr.setArray(array);
         arrayGetExpr.setKey((TreeExpression) visit(ctx.expr()));
         return arrayGetExpr;
     }
 
     @Override
     public TreeNode visitBlock(lolcodeParser.BlockContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeGimmehStmt visitGimstat(lolcodeParser.GimstatContext ctx) {
         TreeNode.position pos = new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine());
         TreeGimmehStmt gimmehStmt = new TreeGimmehStmt();
         TreeVariable var = new TreeVariable();
         gimmehStmt.setPos(pos);
         var.setPos(pos);
         var.setName(ctx.ID().toString());
         gimmehStmt.setVariable(var);
         return gimmehStmt;
     }
 
     @Override
     public TreeNode visitExpr(lolcodeParser.ExprContext ctx) {
         if (ctx.ID() != null) {
             TreeVariable variable = new TreeVariable();
             variable.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
             variable.setName(ctx.ID().toString());
             return variable;
         }
         return visitChildren(ctx);
     }
 
     @Override
     public TreeAssignStmt visitAssstat(lolcodeParser.AssstatContext ctx) {
         TreeAssignStmt assignStmt = new TreeAssignStmt();
         assignStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         TreeVariable lhs = new TreeVariable();
         lhs.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         lhs.setName(ctx.ID().toString());
         assignStmt.setLhs(lhs);
         assignStmt.setRhs((TreeExpression) visit(ctx.expr()));
         return assignStmt;
     }
 
     @Override
     public TreeVisibleStmt visitVisstat(lolcodeParser.VisstatContext ctx) {
         TreeVisibleStmt visibleStmt = new TreeVisibleStmt();
         visibleStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         visibleStmt.setArgument((TreeExpression) visit(ctx.expr()));
         return visibleStmt;
     }
 
     @Override
     public TreeNotExpr visitNotexpr(lolcodeParser.NotexprContext ctx) {
         TreeNotExpr notExpr = new TreeNotExpr();
         notExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         notExpr.setExpr((TreeExpression) visit(ctx.expr()));
         return notExpr;
     }
 
     private class CasePair extends PositionedTreeNode {
         public TreeConstant constant;
         public List<TreeStatement> body;
 
         private CasePair(TreeConstant constant, List<TreeStatement> body) {
             this.constant = constant;
             this.body = body;
         }
 
         @Override
         public <T> T accept(BaseASTVisitor<T> v) {
             return null;
         }
     }
 
     @Override
     public TreeCaseStmt visitCasestat(lolcodeParser.CasestatContext ctx) {
         TreeCaseStmt caseStmt = new TreeCaseStmt();
         caseStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         TreeExpression expression = (TreeExpression) visit(ctx.expr());
         caseStmt.setVal(expression);
         for (lolcodeParser.CaseblockContext blk : ctx.caseblock()) {
             CasePair pair = (CasePair) visit(blk);
             if (!caseStmt.addStatement(pair.constant, pair.body)) {
                 ErrorHandler.warnCaseOverride(pair.constant.getPos(), pair.constant.getRealValue());
             }
         }
         if (ctx.OMGWTF() != null) {
             for (lolcodeParser.StatContext stat : ctx.block().stat()) {
                 caseStmt.addDefaultStmt((TreeStatement) visit(stat));
             }
         }
         return caseStmt;
     }
 
     @Override
     public CasePair visitCaseblock(@NotNull lolcodeParser.CaseblockContext ctx) {
         TreeConstant constant = (TreeConstant) visit(ctx.value());
         ArrayList<TreeStatement> lst = new ArrayList<>();
         for (lolcodeParser.StatContext stat : ctx.block().stat()) {
             lst.add((TreeStatement) visit(stat));
         }
         return new CasePair(constant, lst);
     }
 
     @Override
     public TreeModule visitFile(lolcodeParser.FileContext ctx) {
         if (root != null) {
             log.log(Level.SEVERE, "visitFile() called more than once for same file!");
         }
         TreeModule module = new TreeModule();
         module.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         module.setModuleName(filename);
         if (!ctx.functionDecl().isEmpty()) {
             for (lolcodeParser.FunctionDeclContext fDecl : ctx.functionDecl()) {
                 module.addFunction((TreeFunction) visit(fDecl));
             }
         }
         if (ctx.main() == null) {
             log.severe("No module body found in module.");
         } else {
             if (ctx.main().isEmpty()) {
                 log.info("No body statements found in module.");
             } else {
                 for (lolcodeParser.StatContext statement : ctx.main().stat()) {
                     module.addStatement((TreeStatement) visit(statement));
                 }
             }
         }
         root = module;
         return module;
     }
 
     @Override
     public TreeIfStmt visitIfstat(lolcodeParser.IfstatContext ctx) {
         TreeIfStmt ifStmt = new TreeIfStmt();
         ifStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         List<lolcodeParser.ExprContext> exprs = ctx.expr();
         TreeExpression predicate = (TreeExpression) visit(exprs.get(0)); // first predicate is guaranteed to be present
         ifStmt.setCondition(predicate);
         for (lolcodeParser.StatContext stat : ctx.block(0).stat()) {
             ifStmt.addTrueStmt((TreeStatement) visit(stat));
         }
         if (ctx.MEBBE() != null) {                //add else if branches if present
             for (int i = 0; i < ctx.MEBBE().size(); ++i) {
                 TreeIfStmt tmp = new TreeIfStmt();
                 tmp.setPos(new TreeNode.position(ctx.MEBBE(i).getSymbol().getLine(), ctx.MEBBE(i).getSymbol().getCharPositionInLine()));
                 tmp.setCondition((TreeExpression) visit(ctx.expr().get(i + 1)));
                 for (lolcodeParser.StatContext stat : ctx.block().get(i + 1).stat()) {
                     tmp.addTrueStmt((TreeStatement) visit(stat));
                 }
                 ifStmt.addElseIf(tmp);
             }
         }
         if (ctx.NOWAI() != null) {            //add else branch if present
             for (lolcodeParser.StatContext stat : ctx.block().get(ctx.block().size() - 1).stat()) {
                 ifStmt.addFalseStmt((TreeStatement) visit(stat));
             }
         }
         return ifStmt;
     }
 
     @Override
     public TreeNode visitExprList(lolcodeParser.ExprListContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeOrExpr visitOneofexpr(lolcodeParser.OneofexprContext ctx) {
         TreeOrExpr orExpr = new TreeOrExpr();
         orExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         orExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         orExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return orExpr;
     }
 
     @Override
     public TreeLoopStmt visitLoopstat(lolcodeParser.LoopstatContext ctx) {
         TreeLoopStmt loopStmt = new TreeLoopStmt();
         loopStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         loopStmt.setLabel(ctx.ID(0).toString());
         if (ctx.expr() != null) {         //not an infinite loop
             if (ctx.UPPIN() != null) {
                 loopStmt.setoType(TreeLoopStmt.opType.UPPIN);
             } else if (ctx.NERFIN() != null) {
                 loopStmt.setoType(TreeLoopStmt.opType.NERFIN);
             }
             TreeVariable loopVar = new TreeVariable();
             loopVar.setPos(new TreeNode.position(ctx.ID(1).getSymbol().getLine(), ctx.ID(1).getSymbol().getCharPositionInLine()));
             loopVar.setName(ctx.ID(1).toString());
             loopStmt.setVariable(loopVar);
             if (ctx.WHILE() != null) {
                 loopStmt.setlType(TreeLoopStmt.loopType.WHILE);
             } else {
                 loopStmt.setlType(TreeLoopStmt.loopType.TIL);
             }
             TreeExpression condition = (TreeExpression) visit(ctx.expr());
             loopStmt.setExitCondition(condition);
         }
         for (lolcodeParser.StatContext stat : ctx.block().stat()) {
             loopStmt.addStatement((TreeStatement) visit(stat));
         }
         return loopStmt;
     }
 
     @Override
     public TreeFuncCallExpr visitFunexpr(lolcodeParser.FunexprContext ctx) {
         TreeFuncCallExpr funcCallStmt = new TreeFuncCallExpr();
         funcCallStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         funcCallStmt.setFuncName(ctx.ID().toString());
         if (ctx.exprList() != null) {
             for (lolcodeParser.ExprContext param : ctx.exprList().expr()) {
                 funcCallStmt.addArgument((TreeExpression) visit(param));
             }
         }
         return funcCallStmt;
     }
 
     @Override
     public TreeDummyStmt visitDummystmt(@NotNull lolcodeParser.DummystmtContext ctx) {
         TreeDummyStmt dummyStmt = new TreeDummyStmt();
         dummyStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         dummyStmt.setBody((TreeExpression) visit(ctx.expr()));
         return dummyStmt;
     }
 
     @Override
     public TreeMaxExpr visitMaxexpr(@NotNull lolcodeParser.MaxexprContext ctx) {
         TreeMaxExpr maxExpr = new TreeMaxExpr();
         maxExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         maxExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         maxExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return maxExpr;
     }
 
     @Override
     public TreeMinExpr visitMinexpr(@NotNull lolcodeParser.MinexprContext ctx) {
         TreeMinExpr minExpr = new TreeMinExpr();
         minExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         minExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         minExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return minExpr;
     }
 
     @Override
     public TreeModExpr visitModexpr(lolcodeParser.ModexprContext ctx) {
         TreeModExpr modExpr = new TreeModExpr();
         modExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         modExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         modExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return modExpr;
     }
 
     @Override
     public TreeAndExpr visitBothofexpr(lolcodeParser.BothofexprContext ctx) {
         TreeAndExpr andExpr = new TreeAndExpr();
         andExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         andExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         andExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return andExpr;
     }
 
     @Override
     public TreeSumExpr visitAddexpr(lolcodeParser.AddexprContext ctx) {
         TreeSumExpr sumExpr = new TreeSumExpr();
         sumExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         sumExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         sumExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return sumExpr;
     }
 
     @Override
     public TreeNode visitStat(lolcodeParser.StatContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeFunction visitFunctionDecl(lolcodeParser.FunctionDeclContext ctx) {
         TreeFunction func = new TreeFunction();
         func.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         func.setName(ctx.ID().toString());
         if (ctx.formalParameters() != null) {
             for (lolcodeParser.FormalParameterContext param : ctx.formalParameters().formalParameter()) {
                 func.addParam((TreeFunctionParameter) visit(param));
             }
         }
         for (lolcodeParser.StatContext stat : ctx.block().stat()) {
             func.addStmt((TreeStatement) visit(stat));
         }
         return func;
     }
 
     @Override
     public TreeNode visitMain(lolcodeParser.MainContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeDivExpr visitDivexpr(@NotNull lolcodeParser.DivexprContext ctx) {
         TreeDivExpr divExpr = new TreeDivExpr();
         divExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         divExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         divExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return divExpr;
     }
 
     @Override
     public TreeStatement visitRetpart(lolcodeParser.RetpartContext ctx) {
         String tmp = ctx.getChild(0).getText();
         if (tmp.equals("GTFO")) {
             TreeBreakStmt breakStmt = new TreeBreakStmt();
             breakStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
             return breakStmt;
         } else {
             TreeReturnStmt returnStmt = new TreeReturnStmt();
             returnStmt.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
             returnStmt.setRetValue((TreeExpression) visit(ctx.expr()));
             return returnStmt;
         }
     }
 
     @Override
     public TreeNode visitFormalParameters(lolcodeParser.FormalParametersContext ctx) {
         return visitChildren(ctx);
     }
 
     @Override
     public TreeSubExpr visitSubexpr(@NotNull lolcodeParser.SubexprContext ctx) {
         TreeSubExpr subExpr = new TreeSubExpr();
         subExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         subExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         subExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return subExpr;
     }
 
     @Override
     public TreeXorExpr visitEitherexpr(lolcodeParser.EitherexprContext ctx) {
         TreeXorExpr xorExpr = new TreeXorExpr();
         xorExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         xorExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         xorExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return xorExpr;
     }
 
     @Override
     public TreeEqualExpr visitEquexpr(lolcodeParser.EquexprContext ctx) {
         TreeEqualExpr equalExpr = new TreeEqualExpr();
         equalExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         equalExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         equalExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return equalExpr;
     }
 
     @Override
     public TreeNequalExpr visitNequexpr(@NotNull lolcodeParser.NequexprContext ctx) {
         TreeNequalExpr nequalExpr = new TreeNequalExpr();
         nequalExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         nequalExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         nequalExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return nequalExpr;
     }
 
     @Override
     public TreeMulExpr visitMultexpr(lolcodeParser.MultexprContext ctx) {
         TreeMulExpr mulExpr = new TreeMulExpr();
         mulExpr.setPos(new TreeNode.position(ctx.start.getLine(), ctx.start.getCharPositionInLine()));
         mulExpr.setLhs((TreeExpression) visit(ctx.expr().get(0)));
         mulExpr.setRhs((TreeExpression) visit(ctx.expr().get(1)));
         return mulExpr;
     }
 }
