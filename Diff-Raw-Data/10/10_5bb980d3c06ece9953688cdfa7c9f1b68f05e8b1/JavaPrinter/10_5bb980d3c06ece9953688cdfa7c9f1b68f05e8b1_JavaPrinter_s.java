 /*
  * JavaPrinter.java
  *
 * Copyright (c) 2004-2008 GradSoft  Ukraine
  * All Rights Reserved
  */
 
 
 package ua.gradsoft.printers.java5;
 
 import java.io.PrintWriter;
 import ua.gradsoft.parsers.java5.ParserHelper;
 import ua.gradsoft.parsers.java5.jjt.ModifierSet;
 import ua.gradsoft.termware.Term;
 import ua.gradsoft.termware.TermHelper;
 import ua.gradsoft.termware.TermWareException;
 import ua.gradsoft.termware.exceptions.AssertException;
 import ua.gradsoft.termware.printers.AbstractPrinter;
 
 
 
 /**
  *
  * @author Ruslan Shevchenko
  */
 public class JavaPrinter extends AbstractPrinter {
     
     /** Creates a new instance of JavaPrinter */
     public JavaPrinter(PrintWriter out, String outTag) {
         super(out,outTag);
     }
     
     
     public void writeTerm(Term t) throws TermWareException {
         writeTerm(t,0);
     }
     
     public void flush()
     {
       out_.flush();  
     }
     
     public void writeTerm(Term t, int level) throws TermWareException {
         writeTerm(t,level,-1);
     }
     
     public void writeTerm(Term t,int level,int currentPriority) throws TermWareException {
         //System.err.println("writeTerm:"+TermHelper.termToString(t));
         // System.out.println("writeTerm:"+TermHelper.termToString(t));
         //System.err.println("writeTerm:"+t.getName()+",class="+t.getClass().getName());
         //if (t instanceof Attributed) {
         //    System.err.println("t instanceof Attributed");
         //    Attributed a = (Attributed)t;
         //    for (Map.Entry<String,Term> at: a.getAttributes().entrySet()) {
         //        System.err.println("<"+at.getKey()+","+TermHelper.termToString(at.getValue())+">");
         //    }
         //}
         Term comment=TermHelper.getAttribute(t,"original_comment");
         if (!comment.isNil()) {
             out_.print(comment.getString());
             writeNextLine(level);
             //System.err.println("found comment:"+comment.getString());
         }
         if (t.isAtom()) {
             if (!t.getName().equals("BLANK")) {
                 // hack
                 out_.print(t.getName());
             }
         }else if (t.getName().equals("Type")) {
             writeType(t,level);
         }else if (t.getName().equals("TypeParameters")) {
             writeTypeParameters(t,level);
         }else if(t.getName().equals("TypeParameter")){
             writeTypeParameter(t,level);
         }else if(t.getName().equals("TypeBound")){
             writeTypeBound(t,level);
         }else if(t.getName().equals("TypeArguments")){
             writeTypeArguments(t,level);
         }else if(t.getName().equals("TypeArgument")){
             writeTypeArgument(t,level);
         }else if(t.getName().equals("WildcardBounds")){
             writeWildcardBounds(t,level);
         }else if(t.getName().equals("ReferenceType")){
             writeReferenceType(t,level);
         }else if(t.getName().equals("ClassOrInterfaceType")){
             writeClassOrInterfaceType(t,level);
         }else if(t.getName().equals("Identifier")){
             writeIdentifier(t,level);
         }else if(t.getName().equals("CompilationUnit")) {
             writeCompilationUnit(t,level);
         }else if(t.getName().equals("PackageDeclaration")) {
             writePackageDeclaration(t,level);
         }else if(t.getName().equals("ImportDeclaration")){
             writeImportDeclaration(t,level);
         }else if(t.getName().equals("Name")){
             writeName(t,level);
         }else if(t.getName().equals("NameList")){
             writeNameList(t,level);
         }else if(t.getName().equals("Modifiers")){
             writeModifiers(t,level);
         }else if(t.getName().equals("Annotation")){
             writeAnnotation(t,level);
         }else if(t.getName().equals("NormalAnnotation")){
             writeNormalAnnotation(t,level);
         }else if(t.getName().equals("SingleMemberAnnotation")){
             writeSingleMemberAnnotation(t,level);
         }else if(t.getName().equals("MarkerAnnotation")){
             writeMarkerAnnotation(t,level);
         }else if(t.getName().equals("MemberValuePairs")){
             writeMemberValuePairs(t,level);
         }else if(t.getName().equals("MemberValuePair")){
             writeMemberValuePair(t,level);
         }else if(t.getName().equals("MemberValue")) {
             writeMemberValue(t,level);
         }else if(t.getName().equals("TypeDeclaration")){
             writeTypeDeclaration(t,level);
         }else if(t.getName().equals("ClassOrInterfaceDeclaration")){
             writeClassOrInterfaceDeclaration(t,level);
         }else if(t.getName().equals("ExtendsList")){
             writeExtendsList(t,level);
         }else if(t.getName().equals("ImplementsList")){
             writeImplementsList(t,level);
         }else if(t.getName().equals("ClassOrInterfaceBody")){
             writeClassOrInterfaceBody(t,level);
         }else if(t.getName().equals("ClassOrInterfaceBodyDeclaration")){
             writeClassOrInterfaceBodyDeclaration(t,level);
         }else if(t.getName().equals("AnnotationTypeDeclaration")){
             writeAnnotationTypeDeclaration(t,level);
         }else if(t.getName().equals("AnnotationTypeBody")){
             writeAnnotationTypeBody(t,level);
         }else if(t.getName().equals("Initializer")){
             writeInitializer(t,level);
         }else if(t.getName().equals("ConstructorDeclaration")){
             writeConstructorDeclaration(t,level);
         }else if(t.getName().equals("Modifiers")) {
             writeModifiers(t,level);
         }else if(t.getName().equals("Block")){
             writeBlock(t,level);
         }else if(t.getName().equals("BlockStatement")){
             writeBlockStatement(t,level);
         }else if(t.getName().equals("LocalVariableDeclaration")) {
             writeLocalVariableDeclaration(t,level);
         }else if(t.getName().equals("VariableDeclarator")) {
             writeVariableDeclarator(t,level);
         }else if(t.getName().equals("MethodDeclaration")) {
             writeMethodDeclaration(t,level);
         }else if(t.getName().equals("ResultType")){
             writeResultType(t,level);
         }else if(t.getName().equals("MethodDeclarator")){
             writeMethodDeclarator(t,level);
         }else if(t.getName().equals("FormalParameters")){
             writeFormalParameters(t,level);
         }else if(t.getName().equals("FormalParameter")){
             writeFormalParameter(t,level);
         }else if(t.getName().equals("VariableDeclaratorId")){
             writeVariableDeclaratorId(t,level);
         }else if(t.getName().equals("Statement")){
             writeStatement(t,level);
         }else if(t.getName().equals("StatementExpressionStatement")){
             writeStatementExpressionStatement(t,level);
         }else if(t.getName().equals("LabeledStatement")){
             writeLabeledStatement(t,level);
         }else if(t.getName().equals("AssertStatement")){
             writeAssertStatement(t,level);
         }else if(t.getName().equals("EmptyStatement")){
             writeEmptyStatement(t,level);
         }else if(t.getName().equals("StatementExpression")){
             writeStatementExpression(t,level);
         }else if(t.getName().equals("SwitchStatement")){
             writeSwitchStatement(t,level);
         }else if(t.getName().equals("SwitchStatementLabelBlock")){
             writeSwitchStatementLabelBlock(t,level);
         }else if(t.getName().equals("SwitchLabel")){
             writeSwitchLabel(t,level);
         }else if(t.getName().equals("IfStatement")){
             writeIfStatement(t,level);
         }else if(t.getName().equals("WhileStatement")){
             writeWhileStatement(t,level);
         }else if(t.getName().equals("DoStatement")){
             writeDoStatement(t,level);
         }else if(t.getName().equals("ForStatement")){
             writeForStatement(t,level);
         }else if(t.getName().equals("TraditionalForLoopHead")){
             writeTraditionalForLoopHead(t,level);
         }else if(t.getName().equals("ForEachLoopHead")){
             writeForEachLoopHead(t,level);
         }else if(t.getName().equals("ForInit")){
             writeForInit(t,level);
         }else if(t.getName().equals("ForUpdate")){
             writeForUpdate(t,level);
         }else if(t.getName().equals("BreakStatement")){
             writeBreakStatement(t,level);
         }else if(t.getName().equals("ContinueStatement")){
             writeContinueStatement(t,level);
         }else if(t.getName().equals("ReturnStatement")){
             writeReturnStatement(t,level);
         }else if(t.getName().equals("ThrowStatement")){
             writeThrowStatement(t,level);
         }else if(t.getName().equals("SynchronizedStatement")){
             writeSynchronizedStatement(t,level);
         }else if(t.getName().equals("TryStatement")){
             writeTryStatement(t,level);
         }else if(t.getName().equals("AssignmentOperator")){
             writeAssignnmentOperator(t,level);
         }else if(t.getName().equals("PrimaryExpression")){
             writePrimaryExpression(t,level,currentPriority);
         }else if(t.getName().equals("FieldSelector")){
             writeFieldSelector(t,level);
         }else if(t.getName().equals("ArrayIndexSelector")){
             writeArrayIndexSelector(t,level);
         }else if(t.getName().equals("AllocationExpression")){
             writeAllocationExpression(t,level);
         }else if(t.getName().equals("Arguments")){
             writeArguments(t,level);
         }else if(t.getName().equals("StringLiteral")){
             writeStringLiteral(t,level);
         }else if(t.getName().equals("CharacterLiteral")) {
             writeCharacterLiteral(t,level);
         }else if(t.getName().equals("IntegerLiteral")){
             writeIntegerLiteral(t,level);
         }else if(t.getName().equals("BooleanLiteral")){
             writeBooleanLiteral(t,level);
         }else if(t.getName().equals("FloatingPointLiteral")){
             writeFloatingPointLiteral(t,level);
         }else if(t.getName().equals("FieldDeclaration")) {
             writeFieldDeclaration(t,level);
         }else if(t.getName().equals("Expression")) {
             writeExpression(t,level,currentPriority);
         }else if(t.getName().equals("VariableInitializer")) {
             writeVariableInitializer(t,level);
         }else if(t.getName().equals("ConditionalExpression")){
             writeConditionalExpression(t,level,currentPriority);
         }else if(t.getName().equals("ConditionalOrExpression")){
             writeConditionalOrExpression(t,level,currentPriority);
         }else if(t.getName().equals("ConditionalAndExpression")){
             writeConditionalAndExpression(t,level,currentPriority);
         }else if(t.getName().equals("InclusiveOrExpression")){
             writeInclusiveOrExpression(t,level,currentPriority);
         }else if(t.getName().equals("ExclusiveOrExpression")){
             writeExclusiveOrExpression(t,level,currentPriority);
         }else if(t.getName().equals("AndExpression")){
             writeAndExpression(t,level,currentPriority);
         }else if(t.getName().equals("EqualityExpression")) {
             writeEqualityExpression(t,level,currentPriority);
         }else if(t.getName().equals("InstanceOfExpression")) {
             writeInstanceOfExpression(t,level,currentPriority);
         }else if(t.getName().equals("RelationalExpression")) {
             writeRelationalExpression(t,level,currentPriority);
         }else if(t.getName().equals("ShiftExpression")){
             writeShiftExpression(t,level,currentPriority);
         }else if(t.getName().equals("ShiftExpressionOperand")){
             writeShiftExpressionOperand(t,level);
         }else if(t.getName().equals("AdditiveExpression")) {
             writeAdditiveExpression(t,level,currentPriority);
         }else if(t.getName().equals("MultiplicativeExpression")) {
             writeMultiplicativeExpression(t,level,currentPriority);
         }else if(t.getName().equals("MultiplicativeOperand")) {
             writeMultiplicativeOperand(t,level);
         }else if(t.getName().equals("UnaryExpression")){
             writeUnaryExpression(t,level,currentPriority);
         }else if(t.getName().equals("UnaryExpressionNotPlusMinus")){
             writeUnaryExpressionNotPlusMinus(t,level,currentPriority);
         }else if(t.getName().equals("CastExpression")) {
             writeCastExpression(t,level,currentPriority);
         }else if(t.getName().equals("PostfixExpression")){
             writePostfixExpression(t,level,currentPriority);
         }else if(t.getName().equals("PreIncrementExpression")){
             writePreIncrementExpression(t,level,currentPriority);
         }else if(t.getName().equals("PreDecrementExpression")){
             writePreDecrementExpression(t,level,currentPriority);
         }else if(t.getName().equals("StatementExpressionList")){
             writeStatementExpressionList(t,level);
         }else if(t.getName().equals("PrimaryPrefix")){
             writePrimaryPrefix(t,level,currentPriority);
         }else if(t.getName().equals("NullLiteral")) {
             writeNullLiteral(t,level);
         }else if(t.getName().equals("ExplicitSuperConstructorInvocation")) {
             writeExplicitSuperConstructorInvocation(t,level);
         }else if(t.getName().equals("ExplicitThisConstructorInvocation")) {
             writeExplicitThisConstructorInvocation(t,level);
         }else if(t.getName().equals("ArrayDimsAndInits")) {
             writeArrayDimsAndInits(t,level);
         }else if(t.getName().equals("ArrayDims")){
             writeArrayDims(t,level);
         }else if(t.getName().equals("ArrayDim")){
             writeArrayDim(t,level);
         }else if(t.getName().equals("ArrayInitializer")){
             writeArrayInitializer(t,level);
         }else if(t.getName().equals("CatchSequence")){
             writeCatchSequence(t,level);
         }else if(t.getName().equals("Catch")) {
             writeCatch(t,level);
         }else if(t.getName().equals("EnumDeclaration")){
             writeEnumDeclaration(t,level);
         }else if (t.getName().equals("EnumBody")){
             writeEnumBody(t,level);
         }else if(t.getName().equals("EnumConstant")){
             writeEnumConstant(t,level);
         }else if (t.getName().equals("Allocation")) {
             writeAllocation(t,level);
         }else if (t.getName().equals("FunctionCall")){
             writeFunctionCall(t,level);
         }else if (t.getName().equals("MethodCall")) {
             writeMethodCall(t,level);
         }else if (t.getName().equals("SpecializedMethodCall")) {
             writeSpecializedMethodCall(t,level);
         }else if (t.getName().equals("Field")){
             writeField(t,level);
         }else if (t.getName().equals("SpecializedField")){
             writeSpecializedField(t,level);
         }else if (t.getName().equals("This")) {
             writeThis(t,level);
         }else if (t.getName().equals("Super")) {
             writeSuper(t,level);
         }else if (t.getName().equals("SuperPrefix")){
             writeSuperPrefix(t,level);
         }else if (t.getName().equals("ClassLiteral")){
             writeClassLiteral(t,level);
         }else if (t.getName().equals("ArrayIndex")){
             writeArrayIndex(t,level);
         }else if (t.getName().equals("ThisPrefix")){
             writeThisPrefix(t,level);
         }else if (t.getName().equals("AnnotationTypeMemberDeclaration")){
             writeAnnotationTypeMemberDeclaration(t,level);
         }else{
             // yet not implemented or not java.
             t.print(out_);
         }
     }
     
     public void writeString(String s) {
         out_.print(s);
     }
     
     public void writeTypeParameters(Term t,int level) throws TermWareException {
         out_.print("<");
         Term l=t.getSubtermAt(0);
         while(!l.isNil()) {
             writeTerm(l.getSubtermAt(0),level);
             l=l.getSubtermAt(1);
             if (!l.isNil()){
                 out_.print(",");
             }
         }
         out_.print(">");
     }
     
     public void writeTypeParameter(Term t,int level) throws TermWareException {
         if (t.getArity()==1) {
             writeTerm(t.getSubtermAt(0),level);
         }else if(t.getArity()==2){
             writeTerm(t.getSubtermAt(0),level);
             out_.print(' ');
             writeTerm(t.getSubtermAt(1),level);
         }else{
             out_.print("<error:");
             t.print(out_);
             out_.print(">");
         }
     }
     
     public void writeTypeBound(Term t,int level) throws TermWareException {
         
         out_.print("extends ");
         t=t.getSubtermAt(0);
         while(!t.isNil()) {
             if (t.getArity() == 1) {
                 writeTerm(t.getSubtermAt(0),level);
                 break;
             }else{
                 writeTerm(t.getSubtermAt(0),level);
                 t=t.getSubtermAt(1);
                 if (!t.isNil()) {
                     out_.print(" & ");
                 }
             }
         }
     }
     
     public void writeTypeArguments(Term t,int level) throws TermWareException {
         out_.print("<");
         writeCommaList(t.getSubtermAt(0),level);
         out_.print(">");
     }
     
     public void writeTypeArgument(Term t,int level) throws TermWareException {
         if (t.getArity()==0) {
             out_.print('?');
         }else{
             Term t0=t.getSubtermAt(0);
             writeTerm(t0,level);
         }
     }
     
     public void writeWildcardBounds(Term t,int level) throws TermWareException {
         out_.print("? ");
         if (t.getArity()!=0) {
             Term ct=t.getSubtermAt(0);
             writeTerm(ct,level);
             out_.print(" ");
             if (t.getArity()>1) {
                 writeTerm(t.getSubtermAt(1),level);
                 out_.print(" ");
             }
         }
     }
     
     public void writeReferenceType(Term t,int level) throws TermWareException {
         int referenceLevel=t.getSubtermAt(0).getInt();
         writeTerm(t.getSubtermAt(1),level);
         while(referenceLevel>0) {
             out_.print("[]");
             --referenceLevel;
         }
     }
     
     public void writeClassOrInterfaceType(Term t,int level) throws TermWareException {
         int i=0;
         Term l=t.getSubtermAt(0);
         boolean prevTypeArguments=false;
         boolean curTypeArguments=false;
         while(!l.isNil()) {
             Term ct=l.getSubtermAt(0);
             curTypeArguments = (ct.getName().equals("TypeArguments"));
             if (i!=0 && !curTypeArguments && !prevTypeArguments) {
                 out_.print(".");
             }
             writeTerm(ct,level);
             prevTypeArguments = curTypeArguments;
             l=l.getSubtermAt(1);
             ++i;
         }
     }
     
     public void writeIdentifier(Term t,int level) throws TermWareException {
         Term it=t.getSubtermAt(0);
         if (it.isString()) {
             String s=it.getString();
             out_.print(s);
         }else{
             writeTerm(it,level);
         }
     }
     
     public void writeCompilationUnit(Term t,int level) throws TermWareException {
         for(int i=0; i<t.getArity(); ++i) {
             Term ct=t.getSubtermAt(i);
             writeTerm(ct,level);
             writeNextLine(level);
         }
     }
     
     public void writeIdent(int level) {
         for(int i=0; i<level*identSize_;++i)  {
             out_.print(' ');
         }
     }
     
     public void writeNextLine(int level) {
         out_.println();
         writeIdent(level);
     }
     
     public void writePackageDeclaration(Term t,int level) throws TermWareException {
         out_.print("package ");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(";");
     }
     
     public void writeImportDeclaration(Term t,int level) throws TermWareException {
         boolean isStatic=false;
         boolean isAll=false;
         Term m=t.getSubtermAt(0);
         while(!m.isNil()) {
             Term ct=m.getSubtermAt(0);
             if (ct.getName().equals("static")) {
                 isStatic=true;
             }else if(ct.getName().equals("all")) {
                 isAll=true;
             }
             m=m.getSubtermAt(1);
         }
         writeString("import ");
         if (isStatic) {
             writeString("static ");
         }
         writeTerm(t.getSubtermAt(1),level);
         if (isAll){
             writeString(".*");
         }
         writeString(";");
     }
     
     public void writeName(Term t,int level) throws TermWareException {
         Term l=t.getSubtermAt(0);
         while(!l.isNil()) {
             if (l.getArity()<2) {
                 if (l.getName().equals("Identifier")) {
                     out_.print(l.getSubtermAt(0).getString());
                     break;
                 }else{
                     throw new AssertException("Invalid name component:"+TermHelper.termToString(l));
                 }
             }
             writeTerm(l.getSubtermAt(0),level);
             l=l.getSubtermAt(1);
             if (!l.isNil()) {
                 writeString(".");
             }
         }
     }
     
     public void writeNameList(Term t, int level) throws TermWareException {
         Term l=t.getSubtermAt(0);
         writeCommaList(l,level);
     }
     
     public void writeInitializer(Term t, int level) throws TermWareException {
         writeModifiers(t.getSubtermAt(0),level);
         writeBlock(t.getSubtermAt(1),level+1);
     }
     
     public void writeModifiers(Term t,int level) throws TermWareException {
         if (!t.getSubtermAt(0).isInt()) {
             writeTerm(t.getSubtermAt(0));
         }else{
             int m=t.getSubtermAt(0).getInt();
             if ((m & ModifierSet.PUBLIC)!=0) {
                 out_.print("public ");
             }
             if ((m & ModifierSet.PROTECTED)!=0) {
                 out_.print("protected ");
             }
             if ((m & ModifierSet.PRIVATE)!=0) {
                 out_.print("private ");
             }
             if ((m & ModifierSet.STATIC)!=0) {
                 out_.print("static ");
             }
             if ((m & ModifierSet.FINAL)!=0) {
                 out_.print("final ");
             }
             if ((m & ModifierSet.ABSTRACT)!=0) {
                 out_.print("abstract ");
             }
             if ((m & ModifierSet.SYNCHRONIZED)!=0) {
                 out_.print("synchronized ");
             }
             if ((m & ModifierSet.NATIVE)!=0) {
                 out_.print("native ");
             }
             if ((m & ModifierSet.TRANSIENT)!=0) {
                 out_.print("transient ");
             }
             if ((m & ModifierSet.VOLATILE)!=0) {
                 out_.print("volatile ");
             }
             if ((m & ModifierSet.STRICTFP)!=0) {
                 out_.print("strictfp ");
             }
         }
         if (t.getArity()==2) {
             if (t.getSubtermAt(1).getName().equals("cons")) {
                 Term l=t.getSubtermAt(1);
                 while(!l.isNil()) {
                     writeTerm(l.getSubtermAt(0),level);
                     l=l.getSubtermAt(1);
                     if (!l.isNil()) {
                         out_.print(' ');
                     }
                 }
             } else if (t.getSubtermAt(1).isNil()) {
                 // do nothing.
             } else {
                 throw new AssertException("second argument of Modifiers must be a list");
             }
         }else if(t.getArity()>1) {
             for(int i=1; i<t.getArity(); ++i) {
                 writeTerm(t.getSubtermAt(i),level);
                 out_.print(' ');
             }
         }
     }
     
     public void writeAnnotation(Term t,int level) throws TermWareException {
         writeNextLine(level);
         Term it=t.getSubtermAt(0);
         writeTerm(it,level);
         writeNextLine(level);
     }
     
     public void writeNormalAnnotation(Term t,int level) throws TermWareException {
         out_.print("@");
         writeTerm(t.getSubtermAt(0),level);
         out_.print("(");
         writeTerm(t.getSubtermAt(1),level);
         out_.print(")");
     }
     
     public void writeSingleMemberAnnotation(Term t,int level) throws TermWareException {
         out_.print("@");
         writeTerm(t.getSubtermAt(0),level);
         out_.print("(");
         writeTerm(t.getSubtermAt(1),level);
         out_.print(")");
     }
     
     public void writeMarkerAnnotation(Term t,int level) throws TermWareException {
         out_.print("@");
         writeTerm(t.getSubtermAt(0),level);
     }
     
     public void writeMemberValuePairs(Term t,int level) throws TermWareException {
         Term l=t.getSubtermAt(0);
         while(!l.isNil()) {
             writeTerm(l.getSubtermAt(0),level);
             l=l.getSubtermAt(1);
             if(!l.isNil()) {
                 out_.print(",");
             }
         }
     }
     
     public void writeMemberValuePair(Term t,int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         out_.print("=");
         writeTerm(t.getSubtermAt(1),level);
     }
 
     public void writeMemberValue(Term t,int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
     }
 
     
     public void writeTypeDeclaration(Term t,int level) throws TermWareException {
         if (t.getArity()==2) {
             writeTerm(t.getSubtermAt(0),level);
             writeTerm(t.getSubtermAt(1),level);
         }else if(t.getArity()==0){
             out_.print(";");
         }else{
             throw new AssertException("TypeDeclaration must have arity 2 or 0");
         }
     }
     
     public void writeClassOrInterfaceDeclaration(Term t,int level) throws TermWareException {
         if (t.getArity()<6) {
             throw new AssertException("small arity for ClassOrInterfaceDeclaration:"+TermHelper.termToString(t));
         }
         Term kind=t.getSubtermAt(0);
         Term name=t.getSubtermAt(1);
         Term typeParameters=t.getSubtermAt(2);
         Term extendsList=t.getSubtermAt(3);
         Term implementsList=t.getSubtermAt(4);
         Term classOrInterfaceBody=t.getSubtermAt(5);
         if (kind.getName().equals("interface")){
             out_.print("interface ");
         }else{
             out_.print("class ");
         }
         writeTerm(name,level);
         out_.print(' ');
         if (!typeParameters.isNil()) {
             writeTerm(typeParameters,level);
         }
         out_.print(' ');
         if (!extendsList.isNil()) {
             out_.print(' ');
             writeTerm(extendsList,level);
         }
         out_.print(' ');
         if (!implementsList.isNil()) {
             out_.print(' ');
             writeTerm(implementsList,level);
         }
         writeNextLine(level);
         writeTerm(classOrInterfaceBody,level);
     }
     
     public void writeEnumDeclaration(Term t, int level) throws TermWareException {
         out_.print("enum ");
         writeTerm(t.getSubtermAt(0),level);
         if (!t.getSubtermAt(1).isNil()) {
             out_.print(" ");
             writeTerm(t.getSubtermAt(1),level);
         }
         writeTerm(t.getSubtermAt(2),level);
     }
     
     public void writeAnnotationTypeDeclaration(Term t, int level) throws TermWareException {
         out_.print("@interface ");
         writeTerm(t.getSubtermAt(0),level);
         writeTerm(t.getSubtermAt(1),level);
     }
     
     public void writeAnnotationTypeMemberDeclaration(Term t, int level) throws TermWareException {
         Term modifiers = t.getSubtermAt(0);
         writeTerm(modifiers,level);
         out_.print(" ");
         Term type = t.getSubtermAt(1);
         writeTerm(type,level);
         out_.print(" ");
         Term name = t.getSubtermAt(2);
         writeTerm(name,level);
         out_.print("() ");
         if (t.getArity()>3) {
             Term dft = t.getSubtermAt(3);
             if (!dft.isNil()) {
                 if (dft.getName().equals("DefaultValue")) {
                     Term tv = dft.getSubtermAt(0);
                     if (!tv.isNil()) {
                         out_.print("default ");
                     }
                     writeTerm(tv,level);
                 }else{
                     writeTerm(dft,level);
                 }
             }
         }
         out_.print(";");
         writeNextLine(level);
     }
     
     public void writeExtendsList(Term t,int level) throws TermWareException {
         if (t.isNil() || t.getSubtermAt(0).isNil())  {
             return;
         }
         if (!t.getName().equals("ExtendsList")) {
             throw new AssertException("Expect ExtendsList, have:"+TermHelper.termToString(t));
         }
         out_.print("extends ");
         Term l=t.getSubtermAt(0);
         writeCommaList(l,level);
     }
     
     public void writeImplementsList(Term t,int level) throws TermWareException {
         if (t.isNil()||t.getSubtermAt(0).isNil()) {
             return;
         }
         out_.print("implements ");
         Term l=t.getSubtermAt(0);
         writeCommaList(l,level);
     }
     
     public void writeClassOrInterfaceBody(Term t,int level) throws TermWareException {
         out_.print("{");
         writeNextLine(level+1);
         Term l=t.getSubtermAt(0);
         while(!l.isNil()) {
             writeTerm(l.getSubtermAt(0),level+1);
             writeNextLine(level+1);
             l=l.getSubtermAt(1);
         }
         writeNextLine(level);
         out_.print("}");
         writeNextLine(level);
     }
     
     public void writeAnnotationTypeBody(Term t, int level) throws TermWareException {
         out_.print("{");
         writeNextLine(level+1);
         Term l = t.getSubtermAt(0);
         while(!l.isNil()) {
             writeTerm(l.getSubtermAt(0),level+1);
             writeNextLine(level+1);
             l=l.getSubtermAt(1);
         }
         writeNextLine(level);
         out_.print("}");
         writeNextLine(level);
     }
     
     public void writeClassOrInterfaceBodyDeclaration(Term t,int level) throws TermWareException {
         if(t.getArity()==0) {
             out_.print(";");
         }else if(t.getArity()==1){
             writeTerm(t.getSubtermAt(0),level+1);
         }else if(t.getArity()==2) {
             writeTerm(t.getSubtermAt(0),level+1);
             writeTerm(t.getSubtermAt(1),level+1);
         }
     }
     
     public void writeEnumBody(Term t,int level) throws TermWareException {
         out_.print("{");
         writeNextLine(level+1);
         Term ct=t.getSubtermAt(0);
         boolean frs=true;
         boolean prevEnum=true;
         while(!ct.isNil()) {
             Term dcl=ct.getSubtermAt(0);
             if (dcl.getName().equals("EnumConstant")) {
                 if (!frs && prevEnum) {
                     out_.print(",");
                     writeNextLine(level+1);
                 }
                 writeTerm(dcl,level+1);
                 prevEnum=true;
             }else if (dcl.getName().equals("ClassOrInterfaceBodyDeclaration")) {
                 if (prevEnum) {
                     out_.print(";");
                     writeNextLine(level+1);
                 }
                 writeTerm(dcl,level+1);
             }else{
                 // impossible.
                 writeTerm(dcl,level+1);
             }
             frs=false;
             ct=ct.getSubtermAt(1);
         }
         out_.println();
         writeIdent(level);
         out_.println("}");
         writeIdent(level);
     }
     
     public void writeBlock(Term t,int level) throws TermWareException {
         //System.err.println("writeBlock:"+TermHelper.termToString(t));
         Term st=t.getSubtermAt(0);
         if (st.isNil()) {
             out_.println("{}");
             writeIdent(level);
         }else if (st.getName().equals("cons")) {
             out_.println("{");
             writeIdent(level+1);
             //out_.print("!1,level="+level);
             boolean frs=true;
             while(!st.isNil() && st.getName().equals("cons") && st.getArity()==2) {
                 if (!frs) {
                     writeIdent(level+1);
                     //out_.print("!2,level="+level);
                 }else{
                     frs=false;
                 }
                 writeTerm(st.getSubtermAt(0),level+1);
                 if (st.getSubtermAt(0).getName().equals("LocalVariableDeclaration")) {
                     out_.print(";");
                 }
                 out_.println();
                 st=st.getSubtermAt(1);
             }
             if (!st.isNil()) {
                 out_.print("[!");
                 writeTerm(st);
                 out_.print("!]");
             }
             writeIdent(level);
             //out_.print("!3,level="+level);
             out_.println("}");
             writeIdent(level);
             //out_.print("!4,level="+level);
         }else if(st.getName().equals("Identifier")){
             out_.print("%%BLOCK(");
             writeTerm(t,level+1);
             out_.print(")");
         }else{
             throw new TermWareException("Block subterm must be list or block placeholder");
         }
     }
     
     public void writeLocalVariableDeclaration(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level); // modifiers
         out_.print(" ");
         writeTerm(t.getSubtermAt(1),level); // Type
         out_.print(" ");
         if (t.getSubtermAt(2).getName().equals("cons")) {
             writeCommaList(t.getSubtermAt(2),level);
         }else{
             for(int i=2; i<t.getArity(); ++i){
                 writeTerm(t.getSubtermAt(1),level);
                 if (i!=t.getArity()-1) {
                     out_.print(",");
                 }
             }
         }
     }
     
     public void writeType(Term t, int level)  throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
     }
     
     public void writeBlockStatement(Term t, int level)  throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         if (t.getSubtermAt(0).getName().equals("LocalVariableDeclaration")) {
             out_.print(";");
         }
     }
     
     public void writeVariableDeclarator(Term t, int level)  throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         if (t.getArity()==2) {
             Term initializer = t.getSubtermAt(1);
             boolean toPrint=true;
             if (!initializer.isNil()) {
                 if (initializer.getName().equals("VariableInitializer")) {
                     if (initializer.getSubtermAt(0).isNil()) {
                         toPrint=false;
                     }
                 }
             }else{
                 toPrint=false;
             }
             if (toPrint) {
                 out_.print("=");
                 writeTerm(initializer,level);
             }
         }
     }
     
     public void writeMethodDeclaration(Term t, int level) throws TermWareException {
         Term typeParameters=t.getSubtermAt(0);
         Term resultType=t.getSubtermAt(1);
         Term methodDeclarator = t.getSubtermAt(2);
         Term throwsNameList = t.getSubtermAt(3);
         Term block = t.getSubtermAt(4);
         
         if (!typeParameters.isNil()) {
             writeTerm(typeParameters,level);
             out_.print(' ');
         }
         writeTerm(resultType,level);
         out_.print(' ');
         writeTerm(methodDeclarator,level);
         if (!throwsNameList.isNil()) {
             out_.print(" throws ");
             writeTerm(throwsNameList,level);
         }
         if (!block.isNil()) {
             out_.println();
             writeIdent(level);
             writeTerm(block,level);
         }else{
             out_.println(";");
             writeIdent(level);
         }
         
     }
     
     public void writeResultType(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
     }
     
     public void writeMethodDeclarator(Term t, int level)  throws TermWareException {
         writeSubterms(t,"",level);
     }
     
     public void writeFormalParameters(Term t, int level) throws TermWareException {
         out_.write('(');
         Term l=t.getSubtermAt(0);
         if (!l.isNil()) {
             writeCommaList(l,level);
         }
         out_.write(')');
     }
     
     public void writeFormalParameter(Term t, int level) throws TermWareException {
         Term modifiersTerm = t.getSubtermAt(0);
         if (modifiersTerm.getArity()<1 ||
                 !modifiersTerm.getSubtermAt(0).isInt()) {
             writeTerm(modifiersTerm,level);
             out_.print(" ");
             writeTerm(t.getSubtermAt(1),level);
             out_.print(" ");
             writeTerm(t.getSubtermAt(2),level);
         }else{
             int modifiers=modifiersTerm.getSubtermAt(0).getInt();
 //!!TODO: annotations
             if ((modifiers & ModifierSet.FINAL) != 0) {
                 out_.print("final ");
             }
             writeTerm(t.getSubtermAt(1),level);
             out_.print(' ');
             if ((modifiers & ModifierSet.VARARGS) != 0) {
                 out_.print("... ");
             }
             writeTerm(t.getSubtermAt(2),level);
         }
     }
     
     
     public void writeVariableDeclaratorId(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         int nArrays=t.getSubtermAt(1).getInt();
         for(int i=0; i<nArrays; ++i){
             out_.print("[]");
         }
     }
     
     public  void writeReturnStatement(Term t, int level) throws TermWareException {
         out_.print("return");
         if (t.getArity()>0) {
             if (!t.getSubtermAt(0).isNil()) {
                 out_.print(' ');
                 writeTerm(t.getSubtermAt(0),level);
             }
         }
         out_.print(";");
     }
     
     public void writeLabeledStatement(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         out_.print(" : ");
         writeTerm(t.getSubtermAt(1),level);
     }
     
     public void writeAssertStatement(Term t, int level) throws TermWareException {
         out_.print("assert ");
         writeTerm(t.getSubtermAt(0),level);
         if (t.getArity()>1) {
             out_.print(" : ");
             writeTerm(t.getSubtermAt(1),level);
         }
         out_.print(";");
     }
     
     public void writeStatement(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
     }
     
     public void writeEmptyStatement(Term t, int level) throws TermWareException {
         out_.print(";");
     }
     
     public void writeStatementExpressionStatement(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         out_.print(";");
     }
     
     public void writeStatementExpression(Term t, int level) throws TermWareException {
         if (t.getArity()==1) {
             writeTerm(t.getSubtermAt(0),level);
         }else if(t.getArity()==2){
             Term primaryExpression=t.getSubtermAt(0);
             writeTerm(primaryExpression,level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
             Term post=t.getSubtermAt(1);
             if (post.getName().equals("post_increment")) {
                 out_.print("++");
             }else if(post.getName().equals("post_decrement")){
                 out_.print("--");
             }else{
                 throw new AssertException("Incorrect StatementExpression, post_increment/post_decrement expected");
             }
         }else if(t.getArity()==3) {
             writeTerm(t.getSubtermAt(0),level);
             writeString(t.getSubtermAt(1).getString());
             writeTerm(t.getSubtermAt(2),level);
         }else{
             throw new AssertException("arity of StatementExpression must be 1 or 2 or 3");
         }
     }
     
     public void writeStatementExpressionList(Term t, int level) throws TermWareException {
         writeCommaList(t.getSubtermAt(0),level);
     }
     
     public void writeSwitchStatement(Term t, int level) throws TermWareException {
         out_.print("switch(");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(")");
         out_.println("{");
         if (t.getArity()>1) {
             Term l = t.getSubtermAt(1);
             while (!l.isNil()) {
                 writeIdent(level+1);
                 writeTerm(l.getSubtermAt(0),level+1);
                 out_.println();
                 l=l.getSubtermAt(1);
             }
         }
         writeIdent(level);
         out_.print("}");
     }
     
     public void writeSwitchStatementLabelBlock(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         out_.println();
         if (t.getArity()>1) {
             Term l=t.getSubtermAt(1);
             while(!l.isNil()) {
                 writeIdent(level+1);
                 Term st = l.getSubtermAt(0);
                 writeTerm(st,level+1);
                 if (st.getName().equals("LocalVariableDeclaration")) {
                     out_.print(";");
                 }
                 out_.println();
                 l=l.getSubtermAt(1);
             }
         }
         writeIdent(level);
     }
     
     public void writeSwitchLabel(Term t, int level) throws TermWareException {
         if (! (t.getSubtermAt(0).isAtom() && t.getSubtermAt(0).getName().equals("default"))) {
             out_.print("case ");
         }
         writeTerm(t.getSubtermAt(0),level);
         out_.print(":");
     }
     
     
     public void writeIfStatement(Term t, int level) throws TermWareException {
         out_.print("if (");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(")");
         writeTerm(t.getSubtermAt(1),level);
         if (t.getArity()==3) {
             if (!t.getSubtermAt(2).isNil()) {
                 out_.print(" else ");
                 writeTerm(t.getSubtermAt(2),level);
             }
         }
     }
     
     public void writeWhileStatement(Term t, int level) throws TermWareException {
         out_.print("while(");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(")");
         writeTerm(t.getSubtermAt(1),level);
     }
     
     public void writeDoStatement(Term t, int level) throws TermWareException {
         out_.print("do ");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(" while(");
         writeTerm(t.getSubtermAt(1),level);
         out_.print(");");
     }
     
     public void writeForStatement(Term t, int level) throws TermWareException {
         out_.print("for(");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(")");
         writeTerm(t.getSubtermAt(1),level+1);
     }
     
     public void writeBreakStatement(Term t, int level) throws TermWareException {
         out_.print("break");
         if (t.getArity()>0) {
             if (!t.getSubtermAt(0).isNil()) {
                 out_.print(' ');
                 writeTerm(t.getSubtermAt(0),level);
             }
         }
         out_.print(";");
     }
     
     public void writeContinueStatement(Term t, int level) throws TermWareException {
         out_.print("continue");
         if (t.getArity()>0) {
             if (!t.getSubtermAt(0).isNil()) {
                 out_.print(' ');
                 writeTerm(t.getSubtermAt(0),level);
             }
         }
         out_.print(";");
     }
     
     public void writeThrowStatement(Term t, int level) throws TermWareException {
         out_.print("throw ");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(";");
     }
     
     public void writeSynchronizedStatement(Term t, int level) throws TermWareException {
         out_.print("synchronized( ");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(")");
         writeTerm(t.getSubtermAt(1),level+1);
     }
     
     public void writeTryStatement(Term t, int level) throws TermWareException {
         out_.print("try ");
         writeTerm(t.getSubtermAt(0),level);
         writeTerm(t.getSubtermAt(1),level);
         if (t.getArity() > 2) {
             if (!t.getSubtermAt(2).isNil()) {
                 out_.print("finally ");
                 writeTerm(t.getSubtermAt(2),level);
             }
         }
     }
     
     public void writeCatchSequence(Term t, int level) throws TermWareException {
         writeList(t.getSubtermAt(0),"",level);
     }
     
     public void writeCatch(Term t, int level) throws TermWareException {
         out_.print("catch(");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(")");
         writeTerm(t.getSubtermAt(1),level+1);
     }
     
     public void writeExpression(Term t, int level, int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         if (t.getArity()==3) {
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.EXPRESSION_PRIORITY);
             if (t.getSubtermAt(1).isString()) {
                 out_.print(t.getSubtermAt(1).getString());
             }else{
                 writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.EXPRESSION_PRIORITY);
             }
             writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.EXPRESSION_PRIORITY);
         }else if (t.getArity()==1) {
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.EXPRESSION_PRIORITY);
         }else{
             throw new AssertException("arity of Expression must be 3 or 1");
         }
         if (topPriority > ExpressionPriorities.EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     public void writeAssignnmentOperator(Term t,int level) throws TermWareException {
         String op;
         Term st=t.getSubtermAt(0);
         if (st.isString()) {
             op=st.getString();
         }else{
             op=st.getName();
         }
         if (op.equals("op_assign")||op.equals("=")) {
             out_.print("=");
         }else if(op.equals("op_multiply_assign")||op.equals("*=")){
             out_.print("*=");
         }else if(op.equals("op_divide_assign")||op.equals("/=")){
             out_.print("/=");
         }else if(op.equals("op_module_assign")||op.equals("%=")){
             out_.print("%=");
         }else if(op.equals("op_plus_assign")||op.equals("+=")) {
             out_.print("+=");
         }else if(op.equals("op_minus_assign")||op.equals("-=")) {
             out_.print("-=");
         }else if(op.equals("op_left_shift_assign")||op.equals("<<=")){
             out_.print("<<=");
         }else if(op.equals("op_right_shift_assign")||op.equals(">>=")){
             out_.print(">>=");
         }else if(op.equals("op_right_sshift_assign")||op.equals(">>>=")){
             out_.print(">>>=");
         }else if(op.equals("op_and_assign")||op.equals("&=")){
             out_.print("&=");
         }else if(op.equals("op_xor_assign")||op.equals("^=")){
             out_.print("^=");
         }else if(op.equals("op_or_assign")||op.equals("|=")){
             out_.print("|=");
         }else{
             throw new AssertException("Invalid assigment operator:"+op);
         }
     }
     
     public void writeConditionalExpression(Term t,int level, int priority) throws TermWareException {
         if (priority > ExpressionPriorities.CONDITIONAL_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         if (t.getArity()>1) {
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.CONDITIONAL_EXPRESSION_PRIORITY);
             out_.print(" ? ");
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.CONDITIONAL_EXPRESSION_PRIORITY);
             out_.print(" : ");
             writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.CONDITIONAL_EXPRESSION_PRIORITY);
         }else{
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.CONDITIONAL_EXPRESSION_PRIORITY);
         }
         if (priority > ExpressionPriorities.CONDITIONAL_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     public void writeConditionalOrExpression(Term t,int level, int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.CONDITIONAL_OR_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.CONDITIONAL_OR_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             out_.print("||");
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.CONDITIONAL_OR_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.CONDITIONAL_OR_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     public void writeConditionalAndExpression(Term t,int level, int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.CONDITIONAL_AND_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.CONDITIONAL_AND_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             out_.print("&&");
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.CONDITIONAL_AND_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.CONDITIONAL_AND_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     public void writeInclusiveOrExpression(Term t,int level,int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.INCLUSIVE_OR_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.INCLUSIVE_OR_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             out_.print("|");
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.INCLUSIVE_OR_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.INCLUSIVE_OR_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     public void writeExclusiveOrExpression(Term t,int level,int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.EXCLUSIVE_OR_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.EXCLUSIVE_OR_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             out_.print("^");
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.EXCLUSIVE_OR_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.EXCLUSIVE_OR_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     public void writeAndExpression(Term t,int level, int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.AND_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.AND_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             out_.print("&");
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.AND_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.AND_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     
     public void writeEqualityExpression(Term t,int level,int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.EQUALITY_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.EQUALITY_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             String op=t.getSubtermAt(1).getString();
             out_.print(op);
             writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.EQUALITY_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.EQUALITY_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     
     
     public void writeInstanceOfExpression(Term t, int level, int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.INSTANCE_OF_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         if (t.getArity() > 1) {
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.INSTANCE_OF_EXPRESSION_PRIORITY);
             out_.print(" instanceof ");
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.INSTANCE_OF_EXPRESSION_PRIORITY);
         }else{
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.INSTANCE_OF_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.INSTANCE_OF_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     
     public void writeRelationalExpression(Term t,int level, int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.RELATIONAL_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.RELATIONAL_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             String op=t.getSubtermAt(1).getString();
             out_.print(op);
             writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.RELATIONAL_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.RELATIONAL_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     
     public void writeShiftExpression(Term t, int level,int topPriority) throws TermWareException {
         if (topPriority > ExpressionPriorities.SHIFT_EXPRESSION_PRIORITY) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.SHIFT_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             String op = t.getSubtermAt(1).getString();
             out_.print(op);
             writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.SHIFT_EXPRESSION_PRIORITY);
         }
         if (topPriority > ExpressionPriorities.SHIFT_EXPRESSION_PRIORITY) {
             out_.print(")");
         }
     }
     
     public void writeShiftExpressionOperand(Term t, int level) throws TermWareException {
         out_.print(t.getSubtermAt(0).getString());
         writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.SHIFT_EXPRESSION_PRIORITY);
     }
     
     public void writeAdditiveExpression(Term t, int level, int topPriority) throws TermWareException {
        boolean quoted=(topPriority > ExpressionPriorities.ADDITIVE_EXPRESSION_PRIORITY);
         if (quoted) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.ADDITIVE_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             out_.print(t.getSubtermAt(1).getString());
             writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.ADDITIVE_EXPRESSION_PRIORITY);
         }
         if (quoted) {
             out_.print(")");
         }
     }
     
     
     public void writeMultiplicativeExpression(Term t, int level, int topPriority) throws TermWareException {
        boolean quoted=(topPriority > ExpressionPriorities.MULTIPLICATIVE_EXPRESSION_PRIORITY);
         if (quoted) {
             out_.print("(");
         }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.MULTIPLICATIVE_EXPRESSION_PRIORITY);
         if (t.getArity()>1) {
             out_.print(t.getSubtermAt(1).getString());
             writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.MULTIPLICATIVE_EXPRESSION_PRIORITY);
         }
         if (quoted) {
             out_.print(")");
         }
     }
     
     public void writeMultiplicativeOperand(Term t, int level) throws TermWareException {
         String s=t.getSubtermAt(0).getString();
         out_.print(s);
         writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.MULTIPLICATIVE_EXPRESSION_PRIORITY);
     }
     
     
     public void writeUnaryExpression(Term t, int level, int topPriority) throws TermWareException {
         boolean quoted=(topPriority > ExpressionPriorities.UNARY_EXPRESSION_PRIORITY);
         if (quoted) out_.print("(");
         if (t.getArity()==2) {
             String op=t.getSubtermAt(0).getString();
             out_.print(op);
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.UNARY_EXPRESSION_PRIORITY);
         }else{
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.UNARY_EXPRESSION_PRIORITY);
         }
         if (quoted) out_.print(")");
     }
     
     public void writeUnaryExpressionNotPlusMinus(Term t, int level,int topPriority) throws TermWareException {
         boolean quoted=(topPriority > ExpressionPriorities.UNARY_EXPRESSION_PRIORITY);
         if (quoted) out_.print("(");
         if (t.getArity()==2) {
             String op=t.getSubtermAt(0).getString();
             out_.print(op);
             writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.UNARY_EXPRESSION_PRIORITY);
         }else{
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.UNARY_EXPRESSION_PRIORITY);
         }
         if (quoted) out_.print(")");
     }
     
     public void writeCastExpression(Term t, int level, int topPriority) throws TermWareException {
         boolean quoted=(topPriority > ExpressionPriorities.CAST_EXPRESSION_PRIORITY);
         if (quoted) { out_.print("("); }
         out_.print("(");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(")");
         writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.CAST_EXPRESSION_PRIORITY);
         if (quoted) { out_.print(")"); }
     }
     
     public void writePostfixExpression(Term t, int level, int topPriority) throws TermWareException {
         boolean quoted=(topPriority > ExpressionPriorities.POSTFIX_EXPRESSION_PRIORITY);
         if (quoted) {
             out_.print("(");
         }
         if (t.getArity()>1) {
             Term t1=t.getSubtermAt(1);
             String op=null;
             if (t1.isAtom()) {
                 if (t1.getName().equals("increment")) {
                     op="++";
                 }else if(t1.getName().equals("decrement")){
                     op="--";
                 }else{
                     throw new AssertException("invalid postfix expession atom:"+TermHelper.termToString(t1));
                 }
             }else if(t1.isString()) {
                 op=t1.getString();
             }else{
                 throw new AssertException("Invalid postfix expression:"+TermHelper.termToString(t)+", t1 type ="+t1.getPrimaryType1()+", t1="+TermHelper.termToString(t1));
             }
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.POSTFIX_EXPRESSION_PRIORITY);
             out_.print(op);
         }else{
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.POSTFIX_EXPRESSION_PRIORITY);
         }
         if (quoted) {
             out_.print(")");
         }
     }
     
     public void writePreIncrementExpression(Term t, int level, int topPriority) throws TermWareException {
         boolean quoted = topPriority > ExpressionPriorities.UNARY_EXPRESSION_PRIORITY;
         if (quoted) { out_.print("("); }
         out_.print("++");
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.UNARY_EXPRESSION_PRIORITY);
         if (quoted) { out_.print(")"); }
     }
     
     public void writePreDecrementExpression(Term t, int level, int topPriority) throws TermWareException {
         boolean quoted = topPriority > ExpressionPriorities.UNARY_EXPRESSION_PRIORITY;
         if (quoted) { out_.print("("); }
         out_.print("--");
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.UNARY_EXPRESSION_PRIORITY);
         if (quoted) { out_.print(")"); }
     }
     
     
     public void writePrimaryExpression(Term t, int level, int topPriority) throws TermWareException {
         boolean quoted=(topPriority > ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         if (quoted) { out_.print("("); }
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         if (t.getArity()==2) {
             writeList(t.getSubtermAt(1),"",level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         }
         if (quoted) { out_.print(")"); }
     }
     
     public void writePrimaryPrefix(Term t, int level, int topPriority) throws TermWareException {
         boolean quoted = (topPriority > ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         if (quoted) { out_.print("("); }
         if (t.getArity()==2) {
             Term t0=t.getSubtermAt(0);
             Term t1=t.getSubtermAt(1);
             if (t0.isAtom() && t0.getName().equals("super")) {
                 out_.print("super.");
                 writeTerm(t1,level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
             }else if (t1.isAtom() && t1.getName().equals("class")) {
                 writeTerm(t0,level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
                 out_.print(".class");
             }else{
                 throw new AssertException("Invalid PrimaryPrefix:"+TermHelper.termToString(t));
             }
         }else if (t.getArity()==1) {
             writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         }else{
             throw new AssertException("Invalid PrimaryPrefix:"+TermHelper.termToString(t));
         }
         if (quoted) { out_.print(")"); }
     }
     
     public void writeFieldSelector(Term t, int level) throws TermWareException {
         out_.print(".");
         writeTerm(t.getSubtermAt(0),level);
     }
     
     public void writeArrayIndexSelector(Term t, int level) throws TermWareException {
         out_.print("[");
         writeTerm(t.getSubtermAt(0),level);
         out_.print("]");
     }
     
     public void writeArrayIndex(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         out_.print("[");
         writeTerm(t.getSubtermAt(1),level);
         out_.print("]");
     }
     
     public void writeAllocationExpression(Term t, int level) throws TermWareException {
         out_.print("new ");
         writeTerm(t.getSubtermAt(0),level);
         if (!t.getSubtermAt(1).isNil()) {
             writeTerm(t.getSubtermAt(1),level);
         }
         writeTerm(t.getSubtermAt(2),level);
         if (!t.getSubtermAt(3).isNil()){
             writeTerm(t.getSubtermAt(3),level);
         }
     }
     
     public void writeAllocation(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         out_.print(".");
         writeTerm(t.getSubtermAt(1),level);
     }
     
     public void writeArguments(Term t, int level)  throws TermWareException {
         out_.print("(");
         if (t.getArity()>0) {
             writeList(t.getSubtermAt(0),",",level);
         }
         out_.print(")");
     }
     
     
     
     public void writeStringLiteral(Term t, int level) throws TermWareException {
         Term ct=t.getSubtermAt(0);
         if (ct.isString()) {
             out_.print(ParserHelper.codeStringLiteral(ct.getString()));
         }else{
             writeTerm(ct,level);
         }
     }
     
     public void writeCharacterLiteral(Term t, int level) throws TermWareException {
         Term ct=t.getSubtermAt(0);
         if (ct.isChar()) {
             out_.print(ParserHelper.codeCharLiteral(ct.getChar()));
         }else{
             writeTerm(ct,level);
         }
     }
     
     public void writeFloatingPointLiteral(Term t, int level) throws TermWareException {
         Term ct = t.getSubtermAt(0);
         if (ct.isFloat()) {
             out_.print(ct.getFloat());
             out_.print('F');
         }else if(ct.isDouble()){
             out_.print(ct.getDouble());
         }else if (ct.isString()) {
             out_.print(ct.getString());
         }else{
             writeTerm(ct,level);
         }
     }
     
     public void writeIntegerLiteral(Term t,int level) throws TermWareException {
         Term ct=t.getSubtermAt(0);
         if (ct.isInt()) {
             int x = ct.getInt();
             if (x!=0 && x == 0x80000000) {
                 // really needed: strange behaviour of JDK.
                 out_.print("0x80000000");
             }else{
                 out_.print(x);
             }
         }else if (ct.isLong()) {
             out_.print(ct.getLong());
             out_.print('L');
         }else if (ct.isString()) {
             out_.print(ct.getString());
         }else{
             writeTerm(ct,level);
         }
     }
     
     
     public void writeBooleanLiteral(Term t, int level) throws TermWareException {
         Term ct=t.getSubtermAt(0);
         if (t.isBoolean()) {
             if (t.getBoolean()) {
                 out_.print("true");
             }else{
                 out_.print("false");
             }
         }else{
             writeTerm(ct,level);
         }
     }
     
     
     public void writeFieldDeclaration(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         out_.print(" ");
         writeCommaList(t.getSubtermAt(1),level);
         out_.print(";");
     }
     
     public void  writeVariableInitializer(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
     }
     
     public void writeNullLiteral(Term t, int level) {
         out_.print("null");
     }
     
     public void writeTraditionalForLoopHead(Term t,int level) throws TermWareException {
         if (t.getArity()!=3) {
             throw new AssertException("TraditionalForLoopHead must have arity 3");
         }
         Term initTerm=t.getSubtermAt(0);
         if (!initTerm.isNil()) {
             writeTerm(initTerm,level);
         }
         out_.print(";");
         Term exprTerm=t.getSubtermAt(1);
         if (!exprTerm.isNil()) {
             writeTerm(exprTerm,level);
         }
         out_.print(";");
         Term updateTerm=t.getSubtermAt(2);
         if (!updateTerm.isNil()) {
             writeTerm(updateTerm,level);
         }
     }
     
     public void writeForInit(Term t,int level) throws TermWareException {
         if (!t.isNil()) {
             writeTerm(t.getSubtermAt(0),level);
         }
     }
     
     public void writeForUpdate(Term t,int level) throws TermWareException {
         if (!t.isNil()) {
             writeTerm(t.getSubtermAt(0),level);
         }
     }
     
     public void writeForEachLoopHead(Term t,int level) throws TermWareException {
         if (t.getArity()==3) {
             writeTerm(t.getSubtermAt(0),level);
             out_.print(" ");
             writeTerm(t.getSubtermAt(1),level);
             out_.print(" : ");
             writeTerm(t.getSubtermAt(2),level);
         }else{
             throw new AssertException("ForEachLoopHead must have arity 3");
         }
     }
     
     public void writeConstructorDeclaration(Term t, int level) throws TermWareException {
         if (!t.getSubtermAt(0).isNil()) {
             writeTerm(t.getSubtermAt(0),level);
             out_.print(" ");
         }
         writeTerm(t.getSubtermAt(1),level);
         out_.print(" ");
         writeTerm(t.getSubtermAt(2),level);
         if (!t.getSubtermAt(3).isNil()) {
             out_.print(" throws ");
             writeTerm(t.getSubtermAt(3),level);
         }
         out_.println("{");
         if (!t.getSubtermAt(4).isNil()) {
             writeIdent(level+1);
             writeTerm(t.getSubtermAt(4),level+1);
         }
         if (!t.getSubtermAt(5).isNil()) {
             out_.println();
             writeIdent(level+1);
             Term ct=t.getSubtermAt(5);
             if (ct.getName().equals("Block")) {
                 ct=ct.getSubtermAt(0);
             }
             while(!ct.isNil()) {
                 Term st=ct.getSubtermAt(0);
                 writeTerm(st,level+1);
                 if (st.getName().equals("LocalVariableDeclaration")) {
                     out_.print(";");
                 }
                 out_.println();
                 writeIdent(level+1);
                 ct=ct.getSubtermAt(1);
             }
         }
         writeIdent(level);
         out_.print("}");
     }
     
     public void writeExplicitThisConstructorInvocation(Term t, int level) throws TermWareException {
         out_.print("this");
         writeTerm(t.getSubtermAt(0),level);
         out_.print(";");
     }
     
     public void writeExplicitSuperConstructorInvocation(Term t, int level) throws TermWareException {
         if (t.getArity()==2) {
             if (!t.getSubtermAt(0).isNil()) {
                 writeTerm(t.getSubtermAt(0),level);
                 out_.print(".super");
             }else{
                 out_.print("super");
             }
             writeTerm(t.getSubtermAt(1),level);
             out_.print(";");
         }else if(t.getArity()==1) {
             out_.print("super");
             writeTerm(t.getSubtermAt(0),level);
             out_.print(";");
         }else{
             throw new AssertException("value of explicit constructor invocation must be 1 or 2");
         }
     }
     
     public void writeArrayDimsAndInits(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         if (t.getArity()>1) {
             writeTerm(t.getSubtermAt(1),level);
         }
     }
     
     public void writeArrayDims(Term t, int level) throws TermWareException {
         writeList(t.getSubtermAt(0),"",level);
     }
     
     public void writeArrayDim(Term t, int level) throws TermWareException {
         out_.print("[");
         if (t.getArity()>0) {
             writeTerm(t.getSubtermAt(0),level);
         }
         out_.print("]");
     }
     
     public void writeArrayInitializer(Term t, int level) throws TermWareException {
         out_.print("{");
         if (t.getArity()>0) {
             writeCommaList(t.getSubtermAt(0),level);
         }
         out_.print("}");
     }
     
     public void writeFunctionCall(Term t, int level) throws TermWareException {
         writeSubterms(t,"",level);
     }
     
     public void writeMethodCall(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         out_.print(".");
         writeTerm(t.getSubtermAt(1),level);
         writeTerm(t.getSubtermAt(2),level);
     }
     
     public void writeSpecializedMethodCall(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         out_.print(".");
         writeTerm(t.getSubtermAt(1),level);
         writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         writeTerm(t.getSubtermAt(3),level);
     }
     
     public void writeField(Term t, int level) throws TermWareException {
         
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         out_.print(".");
         writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
     }
     
     public void writeSpecializedField(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         out_.print(".");
         writeTerm(t.getSubtermAt(1),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         writeTerm(t.getSubtermAt(2),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
     }
     
     public void writeThis(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         out_.print(".this");
     }
     
     public void writeThisPrefix(Term t, int level) throws TermWareException {
         out_.print("this");
     }
     
     
     public void writeSuper(Term t, int level) throws TermWareException {
         out_.print("super.");
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.POSTFIX_EXPRESSION_PRIORITY);
     }
     
     public void writeSuperPrefix(Term t, int level) throws TermWareException {
         out_.print("super");
     }
     
     
     public void writeClassLiteral(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level,ExpressionPriorities.PRIMARY_EXPRESSION_PRIORITY);
         out_.print(".class");
     }
     
     public void writeEnumConstant(Term t, int level) throws TermWareException {
         writeTerm(t.getSubtermAt(0),level);
         out_.print(" ");
         if (t.getArity() > 1) {
             Term arguments=t.getSubtermAt(1);
             if (!arguments.isNil()) {
                 writeTerm(arguments,level);
             }
             out_.print(" ");
         }
         if (t.getArity() > 2) {
             Term body = t.getSubtermAt(2);
             if (!body.isNil()) {
                 writeTerm(body,level);
             }
         }
     }
     
     private void writeCommaList(Term t, int level) throws TermWareException {
         writeList(t,",",level);
     }
     
     private void writeList(Term t, String separator, int level) throws TermWareException {
         writeList(t,separator,level,-1);
     }
     
     private void writeList(Term t, String separator, int level, int topPriority) throws TermWareException {
         //System.err.println("writeList:"+TermHelper.termToString(t));
         Term sv = t;
         while(!t.isNil()) {
             if (t.getArity()!=2) {
                 throw new AssertException("InvalidList:"+TermHelper.termToString(sv));
             }
             writeTerm(t.getSubtermAt(0),level,topPriority);
             t=t.getSubtermAt(1);
             if (!t.isNil()) {
                 out_.print(separator);
             }
         }
     }
     
     
     
     private void writeSubterms(Term t, String separator, int level)  throws TermWareException {
         for(int i=0; i<t.getArity(); ++i){
             writeTerm(t.getSubtermAt(i),level);
             if (i!=t.getArity()-1) {
                 out_.print(separator);
             }
         }
     }
     
     private int identSize_=DEFAULT_IDENT_SIZE;
     
     public int getDEFAULT_IDENT_SIZE() {
         return DEFAULT_IDENT_SIZE;
     }
     
     public static final int DEFAULT_IDENT_SIZE=2;
 }
