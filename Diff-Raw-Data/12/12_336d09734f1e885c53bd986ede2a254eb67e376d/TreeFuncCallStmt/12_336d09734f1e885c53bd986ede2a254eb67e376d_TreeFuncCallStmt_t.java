 package com.lolcode.tree;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 7/14/13
  * Time: 2:53 PM
  */
 
 /**
  * Implements lolcode function call statement. <p><pre>{@code
  *  I DUNNO HOW TO FUNCTION CALL
  * }</pre></p>
  */
 public class TreeFuncCallStmt extends TreeStatement {
     private String funcName; //!< name of function to call
    private ArrayList<TreeValue> arguments;
 
     public TreeFuncCallStmt() {
         funcName = "";
         arguments = new ArrayList<>();
     }
 
     public String getFuncName() {
         return funcName;
     }
 
     public void setFuncName(String funcName) {
         this.funcName = funcName;
     }
 
    public void addArgument(TreeValue arg) {
         arguments.add(arg);
     }
 
     @Override
     public void accept(BaseASTVisitor v) {
         v.visit(this);
     }
 }
