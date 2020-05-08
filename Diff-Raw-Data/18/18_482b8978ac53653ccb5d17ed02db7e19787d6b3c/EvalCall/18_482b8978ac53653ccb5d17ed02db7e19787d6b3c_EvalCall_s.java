 package xtc.oop.helper;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.FileReader;
 import java.util.Arrays;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 
 
 import java.util.ArrayList;
 import xtc.util.SymbolTable;
 
 /** A visitor that will be able to give you the return types of a call expression
  */
 
 /*Need to make://{{{
  x additive exp
  x basic cast exp
  x bitwise and exp
  x "" or
  x "" negation
  x "" xor
  x Call expression
  x cast expression
  x class lit
  x conditional
  x expresison;
  x equality expr
  x expression list?
  x instance of expression
  x logical and
  x "" negation
  x "" or
  x multiplicative ex
  x new array ex
  x new class ex
  x postfix ex
  x realtional ex
  x selection ex
  x shift expression
  x super expression
  x this expression
  x unary expression
 *///}}}
 
 //EvalCall e = new EvalCall()
 //Strng retType = e.dispatch(CENodE)
 public class EvalCall extends Visitor{
 
     Bubble curBub;
     ArrayList<Bubble> bubbleList;
     SymbolTable table;
 
     public EvalCall(Bubble curBub, ArrayList<Bubble> bubbleList, SymbolTable table){
         this.curBub = curBub;
         this.bubbleList = bubbleList;
         this.table = table;
     }
 
 
     public String visitUnaryExpression(GNode n){
         return visit(n.getNode(1));
     }
     public String visitThisExpression(GNode n){
         //probably dont need this
         return "XXXerror in EvalCALLXXX";
     }
     public String visitSuperExpression(GNode n){
         //probably dont need this
         return "XXXerror in EvalCALLXXX";
     }
     public String visitShiftExpression(GNode n){
         return visit(n.getNode(0));
     }
     public String visitSelectionExpression(GNode n){
         //this is a bit hacky VV we could instead use curBub to trickle up (TODO) AF
         if(n.getNode(0).hasName("SuperExpression")){
             String ty =  (String)(table.lookup("$"+ n.getNode(0).getString(0)));
             if(ty == null)
                 return (String)(table.lookup(n.getNode(0).getString(0)));
         }
 
         if(n.getNode(0).hasName("ThisExpression"))
             return (String)(table.lookup(n.getNode(0).getString(0)));
         System.out.println("please inspect something is wrong in EvalCall visit Selection Expression");
         return "Problem";
     }
 
     public String visitPostfixExpression(GNode n){
         return visit(n.getNode(0));
     }
     public String visitNewClassExpression(GNode n){
         return n.getNode(2).getString(0) ;
     }
     public String visitNewArrayExpression(GNode n){
         String toRet = "";
         toRet += n.getNode(0).getString(0);
         int dims = n.getNode(1).size();
         for(int i =0; i < dims; i++){
             toRet += "[]" ;
         }
         return toRet;
     }
     public String visitMultiplicativeExpression(GNode n){
         String a = visit(n.getNode(0));
         String b = visit(n.getNode(2));
         //if boolean, boolean
         //if a long is involved, long
         //else int
         if (a.equals("double") ||b.equals("double"))
             return "double";
         if (a.equals("float") ||b.equals("float"))
             return "float";
         if (a.equals("long") ||b.equals("long"))
             return "long";
         if(a.equals("boolean") ||b.equals("boolean"))
             return "boolean";
         return "int";
     }
     public String visitLogicalNegationExpression(GNode n){
         return "boolean";
     }
     public String visitLogicalOrExpression(GNode n){
         return "boolean";
     }
     public String visitLogicalAndExpression(GNode n){
         return "boolean";
     }
     public String visitInstanceOfExpression(GNode n){
         return "boolean";
     }
 
     public String visitExpressionList(GNode n)
     {
         //probably dont need this
         return "XXXerror in EvalCALLXXX";
     }
 
     public String visitRelationalExpression(GNode n)
     {
         return "boolean";
     }
     public String visitEqualityExpression(GNode n){
         return "boolean";
     }
 
     public String visitExpression(GNode n){
         return visit(n.getNode(0));
     }
 
     public String visitConditionalExpression(GNode n){
         //This cannot happen
         System.out.println("hmmm problem in EvalCall");
         return "Shouldnt happen";
     }
     public String visitExpressionStatement(GNode n){
         return visit(n.getNode(0));//assuming only one child
     }
 
     public String visitClassLiteralExpression(GNode n){
         return "Class";
     }
 
     public String visitCastExpression(GNode n){
         return n.getNode(0).getNode(0).getString(0);
     }
     public String visitCallExpression(GNode n){
         //evaluate arguments and find correct method based on them
         String arguments  =  visit(n.getNode(3));
         String methodName = n.getString(2);
         //catch System.out.*
         if(n.getNode(0) != null && n.getNode(0).hasName("SelectionExpression")){
             if(n.getNode(0).getNode(0) != null && n.getNode(0).getNode(0).getString(0).equals("System"))
                 return "";
         }
         String[] splitArgs = arguments.trim().split(" ");
         ArrayList<String> paramsList =new ArrayList<String> (Arrays.asList(splitArgs));
         //now find method based on name and parameters
        //curBub does not work here- it is not ensured that curBub is the class that the method belongs to -AF
 
        String key = curBub.getName(); //hmm is this the right default option? what about selectionexpression? -AF
         //will method chaining screw this up as well?> AF
         if(n.getNode(0) != null && n.getNode(0).hasName("PrimaryIdentifier")){
             key = n.getNode(0).getString(0);
         }
 		if(n.getNode(0) != null && n.getNode(0).hasName("CallExpression")){
 			key = visit(n.getNode(0));
 		}
         table = curBub.getTable();
         String type = (String)table.lookup(key);
         if(type == null || type.equals("constructor"))
             type = key;
         Bubble papa = new Bubble();
        //System.out.println("looking for a bubble with type ::" + type);
 
         for(Bubble b: bubbleList){
             if (b.getName().equals(type))
                 papa = b;
         }
 
         //System.out.println("bout to call find method for || " + methodName + " || with bubble name :: " + papa.getName());
         Mubble theMub = papa.findMethod(bubbleList, methodName, paramsList);
 		//System.out.println("do you come here ever");
         //System.out.println(theMub);
         return theMub.getReturnType();
     }
 
     public String visitArguments(GNode n){
 
         //System.out.println("enter args");
         //System.out.println(n.size());
         if(n.size() == 0){
             //System.out.println("size 0");
             return "";
         }
         else{
             //System.out.println("not size 0");
             String toRet = "";
             for(Object c : n){
                 toRet += visit((Node)c) ;
             }
             return toRet;
         }
     }
 
     public String visitBitwiseAndExpression(GNode n){
         String a = visit(n.getNode(0));
         String b = visit(n.getNode(1));
         //if boolean, boolean
         //if a long is involved, long
         //else int
         if(a.equals("boolean") ||b.equals("boolean"))
             return "boolean";
         else if (a.equals("long") ||b.equals("long"))
             return "long";
         else
             return "int";
     }
 
     public String visitBitwiseOrExpression(GNode n){
         String a = visit(n.getNode(0));
         String b = visit(n.getNode(1));
         //if boolean, boolean
         //if a long is involved, long
         //else int
         if(a.equals("boolean") ||b.equals("boolean"))
             return "boolean";
         else if (a.equals("long") ||b.equals("long"))
             return "long";
         else
             return "int";
     }
 
     public String visitBitwiseNegationExpression(GNode n){
         String a = visit(n.getNode(0));
         String b = visit(n.getNode(1));
         //if boolean, boolean
         //if a long is involved, long
         //else int
         if(a.equals("boolean") ||b.equals("boolean"))
             return "boolean";
         else if (a.equals("long") ||b.equals("long"))
             return "long";
         else
             return "int";
     }
 
     public String visitBitwiseXorExpression(GNode n){
         String a = visit(n.getNode(0));
         String b = visit(n.getNode(1));
         //if boolean, boolean
         //if a long is involved, long
         //else int
         if(a.equals("boolean") ||b.equals("boolean"))
             return "boolean";
         else if (a.equals("long") ||b.equals("long"))
             return "long";
         else
             return "int";
     }
 
     public String visitBasicCastExpression(GNode n){
         return n.getNode(0).getString(0);
     }
 
     public String visitAdditiveExpression(GNode n){
         String a = visit(n.getNode(0));
         String b = visit(n.getNode(2));
         //if boolean, boolean
         //if a long is involved, long
         //else int
         if (a.equals("double") ||b.equals("double"))
             return "double";
         if (a.equals("float") ||b.equals("float"))
             return "float";
         if (a.equals("long") ||b.equals("long"))
             return "long";
         if(a.equals("boolean") ||b.equals("boolean"))
             return "boolean";
         return "int";
     }
 
     public String visitPrimaryIdentifier(GNode n){
         //need to look up your type in the table
         return (String)(table.lookup(n.getString(0)));
     }
     // shouldnt need these VV
     /*
     public String visitFieldDeclaration(GNode n){//{{{
         visit(n);
     }
 
     public String visitDimensions(GNode n) {
         visit(n);
     }
 
     public String visitModifiers(GNode n){
         visit(n);
 
     }
 
     public String visitMethodDeclaration(GNode n){
 
         visit(n);
     }
 
     public String visitModifier(GNode n){
         visit(n);
 
     }
 
     public String visitDeclarators(GNode n) {
         visit(n);
     }
 
     public String visitDeclarator(GNode n) {
         visit(n);
     }
     */
 
     public String visitIntegerLiteral(GNode n) {
         return "integer";
     }
 
     public String visitCharacterLiteral(GNode n) {
         return "char";
     }
 
     /*
     public String visitClassBody(GNode n){
         visit(n);
     }
 
     public String visitClassDeclaration(GNode n){
         visit(n);
     }
 
     public String visitFormalParameters(GNode n){
         visit(n);
     }
 
     public String visitFormalParameter(GNode n) {
 
         visit(n);
     }
 
     public String visitQualifiedIdentifier(GNode n){
         visit(n);
     }
 
     public String visitImportDeclaration(GNode n){
         visit(n);
     }
 
     public String visitForStatement(GNode n)
     {
         visit(n);
     }
 
     public String visitBasicForControl(GNode n)
     {
         visit(n);
     }
 
     public String visitPrimitiveType(GNode n) {
         visit(n);
     }
 
     public String visitType(GNode n)
     {
         visit(n);
     }//}}}
     */
 
     public String visit(Node o)
     {
         return (String)(dispatch((Node)o));
     }
 }
 
 
 
 
 
