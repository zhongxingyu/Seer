 package com.lolcode.checker;
 
 import com.lolcode.tree.*;
 import com.lolcode.tree.exception.BaseAstException;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 7/22/13
  * Time: 12:59 PM
  */
 
 /**
  * Performs variable and function declaration checks, redeclaration checks and function call checks.<p><pre>
  * Works as follows:
  * <p>for functions it stores a map FuncName -> TreeFunc to check if function has already been declared,
  * to get number of function arguments and check it in funcall statement
  * to check if a function has been declared in funcall statement</p>
  * <p>for variables:
  * for variables checker stores scopes in a stack containing sets of variables;
  * some statements create new scopes, such as module body, function declaration, if/case/loop bodies;
  * expressions descend recursively to find variables and then (variables) check themselves to be present in scope stack
  * vardecl statements can override scopes, ie create variable with the same name in this scope(but only if
  * previous declaration was made in one of the previous scopes), variable redeclaration in same scope is prohibited
  * </p>
  * </pre></p>
  */
 public class ScopeChecker implements BaseASTVisitor {
     private LinkedList<Set<TreeVariable>> scopes;
     private HashMap<String, TreeFunction> functions;
 
     private enum scopeLocation {NOTFOUND, CURRENT, UPPER}
 
     public ScopeChecker() {
         scopes = new LinkedList<>();
         functions = new HashMap<>();
     }
 
     private scopeLocation findInScopes(TreeVariable var) {
         if (scopes.peek().contains(var)) return scopeLocation.CURRENT; //check current scope first
         ListIterator<Set<TreeVariable>> it = scopes.listIterator(scopes.size());
         while (it.hasPrevious()) {
             if (it.previous().contains(var))
                 return scopeLocation.UPPER; //this is needed by vardecl statement to override scopes
         }
         return scopeLocation.NOTFOUND;
     }
 
     private void checkInScopes(TreeVariable var) throws BaseAstException {
         if (findInScopes(var) == scopeLocation.NOTFOUND) {
             ErrorHandler.undeclaredVariable(var.getPos(), var.getName());
         }
     }
 
     private void visitBinaryExpr(TreeBinaryExpr expr) throws BaseAstException {
         expr.getLhs().accept(this);
         expr.getRhs().accept(this);
     }
 
     @Override
     public Object visit(TreeFunction func) throws BaseAstException {
         HashSet<TreeVariable> currentScope = new HashSet<>();
         currentScope.addAll(func.getParams());
         scopes.push(currentScope);
         for (TreeStatement statement : func.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         return null;
     }
 
     @Override
     public Object visit(TreeModule module) throws BaseAstException {
         for (TreeFunction func : module.getFunctions()) {     //firstly try to add all module function to map
             if (functions.containsKey(func.getName())) {
                 ErrorHandler.redeclaredFunction(func.getPos(), func);
             }
             functions.put(func.getName(), func);
         }
         for (TreeFunction func : module.getFunctions()) {
             visit(func);                                    //no need to accept() since type is known
         }
         scopes.push(new HashSet<TreeVariable>());           //module body has its
         for (TreeStatement statement : module.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         return null;
     }
 
     @Override
     public Object visit(TreeFunctionParameter param) {
         return null;
     }
 
     @Override
     public Object visit(TreeIfStmt ifStmt) throws BaseAstException {
         ifStmt.getCondition().accept(this);  //firstly let the condition check itself for validity
         Set<TreeVariable> current = new TreeSet<>();
         scopes.push(current);                //each if statement branch can have its own scope with own variables
         for (TreeStatement statement : ifStmt.getTrueBranch()) {
             statement.accept(this);          //check true branch statements
         }
         scopes.pop();                        //pop true branch scope
         for (TreeIfStmt elseIf : ifStmt.getElseIfs()) {
             elseIf.accept(this);             //check all else if branches, this method will be called recursively, so no need for making new scopes
         }
         current = new TreeSet<>();           // again, create new scope for else branch
         scopes.push(current);
         for (TreeStatement statement : ifStmt.getFalseBranch()) {
             statement.accept(this);
         }
         scopes.pop();                        //pop else branch scope
         return null;
     }
 
     @Override
     public Object visit(TreeLoopStmt loopStmt) throws BaseAstException {
         Set<TreeVariable> current = new HashSet<>();                //create new scope for loop body
         if (loopStmt.getVariable() != null) {                         //if loop is conditional
             checkInScopes(loopStmt.getVariable());                  //loop variable must be declared somewhere
             loopStmt.getExitCondition().accept(this);               //let condition check itself
             current.add(loopStmt.getVariable());                    //local loop variable should be added to loop scope
         }
         scopes.push(current);
         for (TreeStatement statement : loopStmt.getBody()) {
             statement.accept(this);
         }
         scopes.pop();
         return null;
     }
 
     @Override
     public Object visit(TreeAssignStmt assignStmt) throws BaseAstException {
         checkInScopes(assignStmt.getLhs());
         assignStmt.getRhs().accept(this);
         return null;
     }
 
     @Override
     public Object visit(TreeCaseStmt caseStmt) throws BaseAstException {
         caseStmt.getVal().accept(this);
         for (List<TreeStatement> branch : caseStmt.getBody().values()) {
             Set<TreeVariable> current = new HashSet<>();
             scopes.push(current);
             for (TreeStatement statement : branch) {
                 statement.accept(this);
             }
             scopes.pop();
         }
         return null;
     }
 
     @Override
     public Object visit(TreeVarDeclStmt varDeclStmt) throws BaseAstException {
         switch (findInScopes(varDeclStmt.getVar())) {
             case CURRENT: {
                 ErrorHandler.redeclaredVarible(varDeclStmt.getPos(), varDeclStmt.getVar());
             }
             case UPPER:
             case NOTFOUND:
                 // if a variable is either declared in previous scope or undeclared at all it's ok
                 scopes.peek().add(varDeclStmt.getVar()); //add it to current scope
         }
         return null;
     }
 
     @Override
     public Object visit(TreeVisibleStmt visibleStmt) throws BaseAstException {
         visibleStmt.getArgument().accept(this);
         return null;
     }
 
     @Override
     public Object visit(TreeGimmehStmt gimmehStmt) throws BaseAstException {
         checkInScopes(gimmehStmt.getVariable());
         return null;
     }
 
     @Override
     public Object visit(TreeDummyStmt dummyStmt) throws BaseAstException {
         dummyStmt.getBody().accept(this);
         return null;
     }
 
     @Override
     public Object visit(TreeFuncCallExpr funcCallStmt) throws BaseAstException {
         if (!functions.containsKey(funcCallStmt.getFuncName())) {
             ErrorHandler.undeclaredFunction(funcCallStmt.getPos(), funcCallStmt.getFuncName());
             return null;
         }
         TreeFunction func = functions.get(funcCallStmt.getFuncName());
         if (func.getParams().size() != funcCallStmt.getArguments().size()) {
             ErrorHandler.wrongArguments(funcCallStmt.getPos(), func, funcCallStmt.getArguments().size());
            return null;
         }
         for (TreeExpression expression : funcCallStmt.getArguments()) {
             expression.accept(this);
         }
         return null;
     }
 
     @Override
     public Object visit(TreeBreakStmt breakStmt) {
         return null;
     }
 
     @Override
     public Object visit(TreeReturnStmt returnStmt) throws BaseAstException {
         returnStmt.getRetValue().accept(this);
         return null;
     }
 
     @Override
     public Object visit(TreeVariable variable) throws BaseAstException {
         checkInScopes(variable);
         return null;
     }
 
     @Override
     public Object visit(TreeConstant constant) {
         return null;
     }
 
     @Override
     public Object visit(TreeSumExpr sumExpr) throws BaseAstException {
         visitBinaryExpr(sumExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeSubExpr subExpr) throws BaseAstException {
         visitBinaryExpr(subExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeMulExpr mulExpr) throws BaseAstException {
         visitBinaryExpr(mulExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeDivExpr divExpr) throws BaseAstException {
         visitBinaryExpr(divExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeModExpr modExpr) throws BaseAstException {
         visitBinaryExpr(modExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeMaxExpr maxExpr) throws BaseAstException {
         visitBinaryExpr(maxExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeMinExpr minExpr) throws BaseAstException {
         visitBinaryExpr(minExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeAndExpr andExpr) throws BaseAstException {
         visitBinaryExpr(andExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeOrExpr orExpr) throws BaseAstException {
         visitBinaryExpr(orExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeXorExpr xorExpr) throws BaseAstException {
         visitBinaryExpr(xorExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeNotExpr notExpr) throws BaseAstException {
         notExpr.getExpr().accept(this);
         return null;
     }
 
     @Override
     public Object visit(TreeEqualExpr equalExpr) throws BaseAstException {
         visitBinaryExpr(equalExpr);
         return null;
     }
 
     @Override
     public Object visit(TreeNequalExpr nequalExpr) throws BaseAstException {
         visitBinaryExpr(nequalExpr);
         return null;
     }
 }
