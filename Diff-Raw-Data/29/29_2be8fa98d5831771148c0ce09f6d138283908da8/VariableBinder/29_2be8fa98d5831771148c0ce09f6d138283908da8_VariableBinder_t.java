 package com.lolcode.checker;
 
 import com.lolcode.tree.*;
 import com.lolcode.tree.exception.BaseAstException;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 7/24/13
  * Time: 4:02 PM
  */
 
 /**
  * Links variables in statements and expressions to appropriate variables in proper scopes.
  * <p>For example:<pre>{@code
  * I HAS A VAR1
  * SUM OF VAR1 AN 123
  * }</pre>
  * VAR1 from SUM will be linked to previously declared VAR1.
  * If scopes are nested, variable from current scope will be linked.
  * terrible calls like someclass.setSmth(someclass.getSmth().accept(this)) simply overwrite current
  * property with the correct one got from recursive walk.
  * </p>
  */
 public class VariableBinder implements BaseASTVisitor<TreeExpression> {
     LinkedList<Map<String, TreeVariable>> scopes;
 
     public VariableBinder() {
         scopes = new LinkedList<>();
     }
 
     private TreeExpression visitBinaryExpr(TreeBinaryExpr binaryExpr) throws BaseAstException {
         binaryExpr.setLhs(binaryExpr.getLhs().accept(this));
         binaryExpr.setRhs(binaryExpr.getRhs().accept(this));
         return binaryExpr;
     }
 
    private TreeVariable findVariable(TreeVariable var) throws BaseAstException {
         ListIterator<Map<String, TreeVariable>> it = scopes.listIterator();
         while (it.hasNext()) {
            TreeVariable found = it.next().get(var.getName());
            if (found != null) return found;
         }
//        throw new BaseAstException(name + "is undefined. This is an error of scope checker");
        return var;
     }
 
     @Override
     public TreeExpression visit(TreeFunction func) throws BaseAstException {
         HashMap<String, TreeVariable> current = new HashMap<>(func.getParams().size());
         for (TreeFunctionParameter parameter : func.getParams()) {
             current.put(parameter.getName(), parameter);
         }
         scopes.add(current);
         for (TreeStatement statement : func.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeModule module) throws BaseAstException {
         for (TreeFunction function : module.getFunctions()) {
             visit(function);
         }
         HashMap<String, TreeVariable> current = new HashMap<>();
         scopes.push(current);
         for (TreeStatement statement : module.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeFunctionParameter param) throws BaseAstException {
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeIfStmt ifStmt) throws BaseAstException {
         ifStmt.setCondition(ifStmt.getCondition().accept(this));
         HashMap<String, TreeVariable> current = new HashMap<>();
         scopes.push(current);
         for (TreeStatement statement : ifStmt.getTrueBranch()) {
             statement.accept(this);          //check true branch statements
         }
         scopes.pop();                        //pop true branch scope
         for (TreeIfStmt elseIf : ifStmt.getElseIfs()) {
             elseIf.accept(this);             //check all else if branches, this method will be called recursively, so no need for making new scopes
         }
         current = new HashMap<>();           // again, create new scope for else branch
         scopes.push(current);
         for (TreeStatement statement : ifStmt.getFalseBranch()) {
             statement.accept(this);
         }
         scopes.pop();                        //pop else branch scope
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeLoopStmt loopStmt) throws BaseAstException {
         HashMap<String, TreeVariable> current = new HashMap<>();
         if (loopStmt.getVariable() != null) {                         //if loop is conditional
            TreeVariable weak = findVariable(loopStmt.getVariable());
             /*
                 this is not very obvious point. Because loop makes new variable, inheriting previous' value and
                 name(but it's still a new variable in a different scope), we need to keep track of the previous' one
                 to be able to infer proper type for loop-local variable.
              */
             loopStmt.weakref = weak;
             TreeVariable local = new TreeVariable(weak);
             loopStmt.setVariable(local);
             current.put(loopStmt.getVariable().getName(), local);
             scopes.push(current);         //why not push this down? because exitCondition uses loop-local scope
             loopStmt.setExitCondition(loopStmt.getExitCondition().accept(this));//so we need to push new scope prior to checking condition
         } else {
             scopes.push(current);
         }
         for (TreeStatement statement : loopStmt.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeAssignStmt assignStmt) throws BaseAstException {
        assignStmt.setLhs(findVariable(assignStmt.getLhs()));
         assignStmt.setRhs(assignStmt.getRhs().accept(this));
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeCaseStmt caseStmt) throws BaseAstException {
         caseStmt.setVal(caseStmt.getVal().accept(this));
         for (List<TreeStatement> branch : caseStmt.getBody().values()) {
             HashMap<String, TreeVariable> current = new HashMap<>();
             scopes.push(current);
             for (TreeStatement statement : branch) {
                 statement.accept(this);
             }
             scopes.pop();
         }
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeVarDeclStmt varDeclStmt) throws BaseAstException {
         if (varDeclStmt.getInitialValue() != null)
             varDeclStmt.setInitialValue(varDeclStmt.getInitialValue().accept(this));
         scopes.peek().put(varDeclStmt.getVar().getName(), varDeclStmt.getVar());
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeVisibleStmt visibleStmt) throws BaseAstException {
         visibleStmt.setArgument(visibleStmt.getArgument().accept(this));
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeGimmehStmt gimmehStmt) throws BaseAstException {
        gimmehStmt.setVariable(findVariable(gimmehStmt.getVariable()));
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeFuncCallExpr funcCallStmt) throws BaseAstException {
         for (int i = 0; i < funcCallStmt.getArguments().size(); ++i) {
             TreeExpression expr = funcCallStmt.getArguments().get(i).accept(this);
             funcCallStmt.getArguments().set(i, expr);
         }
         return funcCallStmt;
     }
 
     @Override
     public TreeExpression visit(TreeDummyStmt dummyStmt) throws BaseAstException {
         dummyStmt.setBody(dummyStmt.getBody().accept(this));
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeBreakStmt breakStmt) throws BaseAstException {
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeReturnStmt returnStmt) throws BaseAstException {
         returnStmt.setRetValue(returnStmt.getRetValue().accept(this));
         return null;
     }
 
     @Override
     public TreeExpression visit(TreeVariable variable) throws BaseAstException {
        return findVariable(variable);
     }
 
     @Override
     public TreeExpression visit(TreeConstant constant) throws BaseAstException {
         return constant;
     }
 
     @Override
     public TreeExpression visit(TreeSumExpr sumExpr) throws BaseAstException {
         return visitBinaryExpr(sumExpr);
     }
 
     @Override
     public TreeExpression visit(TreeSubExpr subExpr) throws BaseAstException {
         return visitBinaryExpr(subExpr);
     }
 
     @Override
     public TreeExpression visit(TreeMulExpr mulExpr) throws BaseAstException {
         return visitBinaryExpr(mulExpr);
     }
 
     @Override
     public TreeExpression visit(TreeDivExpr divExpr) throws BaseAstException {
         return visitBinaryExpr(divExpr);
     }
 
     @Override
     public TreeExpression visit(TreeModExpr modExpr) throws BaseAstException {
         return visitBinaryExpr(modExpr);
     }
 
     @Override
     public TreeExpression visit(TreeMaxExpr maxExpr) throws BaseAstException {
         return visitBinaryExpr(maxExpr);
     }
 
     @Override
     public TreeExpression visit(TreeMinExpr minExpr) throws BaseAstException {
         return visitBinaryExpr(minExpr);
     }
 
     @Override
     public TreeExpression visit(TreeAndExpr andExpr) throws BaseAstException {
         return visitBinaryExpr(andExpr);
     }
 
     @Override
     public TreeExpression visit(TreeOrExpr orExpr) throws BaseAstException {
         return visitBinaryExpr(orExpr);
     }
 
     @Override
     public TreeExpression visit(TreeXorExpr xorExpr) throws BaseAstException {
         return visitBinaryExpr(xorExpr);
     }
 
     @Override
     public TreeExpression visit(TreeNotExpr notExpr) throws BaseAstException {
         notExpr.setExpr(notExpr.getExpr().accept(this));
         return notExpr;
     }
 
     @Override
     public TreeExpression visit(TreeEqualExpr equalExpr) throws BaseAstException {
         return visitBinaryExpr(equalExpr);
     }
 
     @Override
     public TreeExpression visit(TreeNequalExpr nequalExpr) throws BaseAstException {
         return visitBinaryExpr(nequalExpr);
     }
 }
