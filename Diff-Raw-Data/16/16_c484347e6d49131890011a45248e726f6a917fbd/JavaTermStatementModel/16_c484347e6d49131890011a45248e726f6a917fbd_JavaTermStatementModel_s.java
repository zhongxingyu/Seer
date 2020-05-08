 /*
  * JavaTermStatementModel.java
  *
  *
  * Copyright (c) 2006 GradSoft  Ukraine
  * All Rights Reserved
  */
 
 
 package ua.gradsoft.javachecker.models;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import ua.gradsoft.javachecker.EntityNotFoundException;
 import ua.gradsoft.javachecker.FileAndLine;
 import ua.gradsoft.javachecker.JUtils;
 import ua.gradsoft.javachecker.models.expressions.JavaTermSwitchConstantExpressionModel;
 import ua.gradsoft.termware.Term;
 import ua.gradsoft.termware.TermHelper;
 import ua.gradsoft.termware.TermWare;
 import ua.gradsoft.termware.TermWareException;
 import ua.gradsoft.termware.exceptions.AssertException;
 
 /**
  *Model for java statement.
  * @author Ruslan Shevchenko
  */
 public class JavaTermStatementModel implements JavaStatementModel {
     
     public JavaTermStatementModel(JavaTermTopLevelBlockModel blockModel,Term t,JavaTermStatementModel parent,JavaTermStatementModel previous) throws TermWareException {
         blockModel_=blockModel;
         t_=t;
         parent_=parent;
         previous_=previous;
         build(t);
     }
     
     
     
     private void build(Term t) throws TermWareException {
         JavaTypeModel enclosedType=this.getTopLevelBlockModel().getOwnerModel().getTypeModel();
         if (t.getName().equals("LabeledStatement")) {
             kind_=JavaStatementKind.LABELED_STATEMENT;
             childs_=new LinkedList<JavaStatementModel>();
             childs_.add(new JavaTermStatementModel(blockModel_,t.getSubtermAt(1),this,null));
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("AssertStatement")) {
             kind_=JavaStatementKind.ASSERT_STATEMENT;
             childs_=Collections.emptyList();
             extractAnonimousTypes(t.getSubtermAt(0));
             if (t.getArity() > 1) {
                 extractAnonimousTypes(t.getSubtermAt(0));
             }
             JavaTermExpressionModel e = JavaTermExpressionModel.create(t.getSubtermAt(0),this,enclosedType);
             expressions_=Collections.singletonList(e);
         }else if(t.getName().equals("Block")){
             kind_=JavaStatementKind.BLOCK;
             childs_=new LinkedList<JavaStatementModel>();
             Term cur=t.getSubtermAt(0);
             JavaTermStatementModel prevS=null;
             while(!cur.isNil()) {
                 Term st=cur.getSubtermAt(0);
                 cur=cur.getSubtermAt(1);
                 JavaTermStatementModel s=new JavaTermStatementModel(blockModel_,st,this,prevS);
                 childs_.add(s);
                 prevS=s;
             }
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("EmptyStatement")){
             kind_=JavaStatementKind.EMPTY_STATEMENT;
             childs_=Collections.emptyList();
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("StatementExpressionStatement")){
             kind_=JavaStatementKind.STATEMENT_EXPRESSION_STATEMENT;
             childs_=Collections.emptyList();
             extractAnonimousTypes(t.getSubtermAt(0));
             JavaTermExpressionModel e = JavaTermExpressionModel.create(t.getSubtermAt(0),this,enclosedType);
             expressions_=Collections.singletonList(e);
         }else if(t.getName().equals("SwitchStatement")){
             kind_=JavaStatementKind.SWITCH_STATEMENT;
             childs_=new LinkedList<JavaStatementModel>();
             //System.err.println("!!!Switch:"+TermHelper.termToString(t));
             extractAnonimousTypes(t.getSubtermAt(0));
             JavaTermExpressionModel switchExpr = JavaTermExpressionModel.create(t.getSubtermAt(0),this,enclosedType);
             expressions_=Collections.singletonList(switchExpr);
             Term switchStatementLabelBlockList = t.getSubtermAt(1);
             JavaTermStatementModel prevS=null;
             while(!switchStatementLabelBlockList.isNil()) {
                 Term switchStatementLabelBlock=switchStatementLabelBlockList.getSubtermAt(0);
                 switchStatementLabelBlockList=switchStatementLabelBlockList.getSubtermAt(1);
                 Term switchLabel = switchStatementLabelBlock.getSubtermAt(0);
                 Term blockStatementList = switchStatementLabelBlock.getSubtermAt(1);
                 Term newBlock=TermWare.getInstance().getTermFactory().createTerm("SwitchLabelBlock",switchLabel,blockStatementList);
                 JavaTermStatementModel s = new JavaTermStatementModel(blockModel_,newBlock,this,prevS);
                 childs_.add(s);
                 prevS=s;
             }
         }else if(t.getName().equals("SwitchLabelBlock")){
             kind_=JavaStatementKind.SWITCH_LABEL_BLOCK;
             childs_=new LinkedList<JavaStatementModel>();
             JavaTermExpressionModel caseExpr = new JavaTermSwitchConstantExpressionModel(parent_.getExpressions().get(0),t_.getSubtermAt(0),this,enclosedType);
             expressions_=Collections.singletonList(caseExpr);
             Term cur=t.getSubtermAt(1);
             JavaTermStatementModel prevS=null;
             while(!cur.isNil()) {
                 Term st=cur.getSubtermAt(0);
                 cur=cur.getSubtermAt(1);
                 JavaTermStatementModel s=new JavaTermStatementModel(blockModel_,st,this,prevS);
                 childs_.add(s);
                 prevS=s;
             }
         }else if(t.getName().equals("IfStatement")){
             kind_=JavaStatementKind.IF_STATEMENT;
             extractAnonimousTypes(t.getSubtermAt(0));
             childs_=new LinkedList<JavaStatementModel>();
             Term stTrue = t.getSubtermAt(1);
             JavaTermStatementModel stm=new JavaTermStatementModel(blockModel_,stTrue,this,null);
             childs_.add(stm);
             if (t.getArity()==3) {
                 Term stFalse = t.getSubtermAt(2);
                 JavaTermStatementModel stm1=new JavaTermStatementModel(blockModel_,stFalse,this,stm);
                 childs_.add(stm1);
             }
             JavaTermExpressionModel e = JavaTermExpressionModel.create(t.getSubtermAt(0),this,enclosedType);
             expressions_=Collections.singletonList(e);
         }else if(t.getName().equals("WhileStatement")){
             kind_=JavaStatementKind.WHILE_STATEMENT;
             extractAnonimousTypes(t.getSubtermAt(0));
             childs_=new LinkedList<JavaStatementModel>();
             JavaTermStatementModel stm=new JavaTermStatementModel(blockModel_,t.getSubtermAt(1),this,null);
             childs_.add(stm);
             JavaTermExpressionModel e = JavaTermExpressionModel.create(t.getSubtermAt(0),this,enclosedType);
             expressions_=Collections.singletonList(e);
         }else if(t.getName().equals("DoStatement")){
             kind_=JavaStatementKind.DO_STATEMENT;
             childs_=new LinkedList<JavaStatementModel>();
             JavaTermStatementModel stm=new JavaTermStatementModel(blockModel_,t.getSubtermAt(0),this,null);
             childs_.add(stm);
             extractAnonimousTypes(t.getSubtermAt(1));
             JavaTermExpressionModel e = JavaTermExpressionModel.create(t.getSubtermAt(1),this,enclosedType);
             expressions_=Collections.singletonList(e);
         }else if(t.getName().equals("ForStatement")){
             kind_=JavaStatementKind.FOR_STATEMENT;
             Term loopHead=t.getSubtermAt(0);
             childs_=new LinkedList<JavaStatementModel>();
             expressions_=new LinkedList<JavaTermExpressionModel>();
             extractLocalVariableExpressionsAndAnonimousTypesFromLoopHead(loopHead);
             Term st=t.getSubtermAt(1);
             JavaStatementModel stm = new JavaTermStatementModel(blockModel_,st,this,null);
             childs_.add(stm);
         }else if(t.getName().equals("BreakStatement")){
             kind_=JavaStatementKind.BREAK_STATEMENT;
             childs_=Collections.emptyList();
             //TODO: add LabelExpressionModel            
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("ContinueStatement")){
             kind_=JavaStatementKind.CONTINUE_STATEMENT;
             childs_=Collections.emptyList();
             //TODO: add LabelExpressionModel
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("ReturnStatement")){
             kind_=JavaStatementKind.RETURN_STATEMENT;
             childs_=Collections.emptyList();
             if (t.getArity()>0) {
                 extractAnonimousTypes(t.getSubtermAt(0));
                 JavaTermExpressionModel e = JavaTermExpressionModel.create(t.getSubtermAt(0),this,enclosedType);
                 expressions_=Collections.singletonList(e);
             }else{
                 expressions_=Collections.emptyList();
             }
         }else if(t.getName().equals("ThrowStatement")){
             kind_=JavaStatementKind.THROW_STATEMENT;
             childs_=Collections.emptyList();
             extractAnonimousTypes(t.getSubtermAt(0));
             JavaTermExpressionModel e = JavaTermExpressionModel.create(t.getSubtermAt(0),this,enclosedType);
             expressions_=Collections.singletonList(e);
         }else if(t.getName().equals("SynchronizedStatement")){
             kind_=JavaStatementKind.SYNCHRONIZED_STATEMENT;
             childs_=new LinkedList<JavaStatementModel>();
             JavaTermStatementModel prevS=null;
             Term blockTerm = t.getSubtermAt(1);
             JavaTermStatementModel st = new JavaTermStatementModel(blockModel_,blockTerm,this,null);
             childs_.add(st);
            //Term l=blockTerm.getSubtermAt(0);
            //while(!l.isNil()) {
            //    Term ct=l.getSubtermAt(0);
            //    l=l.getSubtermAt(1);
            //    JavaTermStatementModel s = new JavaTermStatementModel(blockModel_,ct,this,prevS);
            //    childs_.add(s);
            //    prevS=s;
            //}
            expressions_=Collections.emptyList();
         }else if(t.getName().equals("TryStatement")){
             kind_=JavaStatementKind.TRY_STATEMENT;
             childs_=new LinkedList<JavaStatementModel>();
             JavaTermStatementModel block = new JavaTermStatementModel(blockModel_,t.getSubtermAt(0),this,null);
             childs_.add(block);
             JavaTermStatementModel catchSeq = new JavaTermStatementModel(blockModel_,t.getSubtermAt(1),this,block);
             childs_.add(catchSeq);
             if (t.getArity()>2) {
                 JavaTermStatementModel fb=new JavaTermStatementModel(blockModel_,t.getSubtermAt(2),this,catchSeq);
                 childs_.add(fb);
             }
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("CatchSequence")){
             kind_=JavaStatementKind.CATCH_SEQUENCE;
             childs_=new LinkedList<JavaStatementModel>();
             JavaTermStatementModel prevS=null;
             Term l=t.getSubtermAt(0);
             while(!l.isNil()) {
                 Term ct=l.getSubtermAt(0);
                 l=l.getSubtermAt(1);
                 JavaTermStatementModel s = new JavaTermStatementModel(blockModel_,ct,this,prevS);
                 childs_.add(s);
                 prevS=s;
             }
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("Catch")){
             kind_=JavaStatementKind.CATCH;
             childs_=new LinkedList<JavaStatementModel>();
             extractLocalVariableFromFormalParameter(t.getSubtermAt(0));
             JavaTermStatementModel s = new JavaTermStatementModel(blockModel_,t.getSubtermAt(1),this,null);
             childs_.add(s);
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("LocalVariableDeclaration")){
             kind_=JavaStatementKind.LOCAL_VARIABLE_DECLARATION;
             childs_=Collections.emptyList();
             expressions_=new LinkedList<JavaTermExpressionModel>();
             extractLocalVariablesExpressionsAndAnonimousTypesFromLocalVariableDeclaration(t);
         }else if(t.getName().equals("ClassOrInterfaceDeclaration")){
             kind_=JavaStatementKind.CLASS_OR_INTERFACE_DECLARATION;
             childs_=Collections.emptyList();
             JavaTypeModel ownerTypeModel = blockModel_.getOwnerModel().getTypeModel();
             Term modifiersTerm = TermUtils.createTerm("Modifiers",TermUtils.createInt(0),TermUtils.createNil());
             JavaTermClassOrInterfaceModel iTypeModel = new JavaTermClassOrInterfaceModel(modifiersTerm,t,ownerTypeModel.getPackageModel(),ownerTypeModel.getUnitModel());
             addLocalType(iTypeModel);
             expressions_=Collections.emptyList();
         }else if(t.getName().equals("ExplicitSuperConstructorInvocation")){
             kind_=JavaStatementKind.EXPLICIT_SUPER_CONSTRUCTOR_INVOCATION;
             childs_=Collections.emptyList();
             expressions_=new LinkedList<JavaTermExpressionModel>();
             Term primary=TermUtils.createNil();
             Term args = TermUtils.createNil();
             if (t.getArity()==2) {
                 primary=t.getSubtermAt(0);
                 extractAnonimousTypes(primary);
                 JavaTermExpressionModel e = JavaTermExpressionModel.create(primary,this,enclosedType);
                 expressions_.add(e);
                 args=t.getSubtermAt(1);
             }else{
                 args=t.getSubtermAt(0);
             }
             Term l = args.getSubtermAt(0);
             while(!l.isNil()) {
                 Term et = l.getSubtermAt(0);
                 l=l.getSubtermAt(1);
                 extractAnonimousTypes(et);
                 JavaTermExpressionModel e = JavaTermExpressionModel.create(et,this,enclosedType);
                 expressions_.add(e);
             }
         }else if(t.getName().equals("ExplicitThisConstructorInvocation")){
             kind_=JavaStatementKind.EXPLICIT_THIS_CONSTRUCTOR_INVOCATION;
             childs_=Collections.emptyList();
             expressions_=new LinkedList<JavaTermExpressionModel>();
             Term args=t.getSubtermAt(0);
             Term l=args.getSubtermAt(0);
             while(!l.isNil()) {
                 Term et = l.getSubtermAt(0);
                 l=l.getSubtermAt(1);
                 extractAnonimousTypes(et);
                 JavaTermExpressionModel e = JavaTermExpressionModel.create(et,this,enclosedType);
                 expressions_.add(e);
             }
         }else{
             throw new AssertException("Invalid Statement:"+TermHelper.termToString(t));
         }
     }
     
     public JavaStatementKind       getKind() {
         return kind_;
     }
     
     
     public JavaTopLevelBlockModel  getTopLevelBlockModel() {
         return blockModel_;
     }
     
     public JavaTermTopLevelBlockModel  getTermTopLevelBlockModel() {
         return blockModel_; }
     
     public JavaStatementModel getParentStatementModel() {
         return parent_; }
     
     
     public JavaStatementModel getPreviousStatementModel() {
         return previous_; }
     
     public List<JavaStatementModel> getChildStatements() {
         return childs_;
     }
     
     public List<JavaLocalVariableModel> getLocalVariables() {
         return localVariables_; }
     
     
     /**
      * return localType, defined in this statement, or null if this statement
      *is not defintioon fo local type
      */
     public JavaTypeModel getLocalType() {
         return localType_;
     }
     
     
     /**
      *list of top-level expressions
      */
     public List<JavaExpressionModel>  getExpressions()
     { // change of type is possible only via unsafe interface
         List l = (List)expressions_; return (List<JavaExpressionModel>)l; 
     }
     
     /**
      * as was from parser
      */
     public Term getTerm() {
         return t_; }
     
     
     
     
     /**
      * StatementModel(term,context)
      */
     public Term getModelTerm() throws TermWareException, EntityNotFoundException {
         JavaPlaceContext ctx = JavaPlaceContextFactory.createNewStatementContext(this);
         Term tctx=TermUtils.createJTerm(ctx);
         Term retval=null;
         switch(getKind()) {
             case ASSERT_STATEMENT:
             {
                 retval = TermUtils.createTerm("AssertStatementModel",expressions_.get(0).getModelTerm(),tctx);
             }
             break;
             case BLOCK:
             {
                 Term childsListTerm=TermUtils.createNil();
                 for(JavaStatementModel child: getChildStatements()) {
                     Term cmt=child.getModelTerm();
                     childsListTerm=TermUtils.createTerm("cons",cmt,childsListTerm);
                 }
                 childsListTerm=TermUtils.reverseListTerm(childsListTerm);
                 retval = TermUtils.createTerm("BlockStatementModel",childsListTerm,tctx);
             }
             break;
             case BREAK_STATEMENT:
             {
                 Term breakLabel = TermUtils.createNil();
                 if (t_.getArity() > 0) {
                     breakLabel = t_.getSubtermAt(0);
                 }
                 retval = TermUtils.createTerm("BreakStatementModel",breakLabel,tctx);
             }
             break;
             case CATCH:
             {
                 Term fp = t_.getSubtermAt(0);
                 Term blockModel = childs_.get(0).getModelTerm();
                 retval = TermUtils.createTerm("CatchModel",fp,blockModel,tctx);
             }
             break;
             case CATCH_SEQUENCE:
             {
                 Term cl=TermUtils.createNil();
                 for(JavaStatementModel st: childs_) {
                     Term smt = st.getModelTerm();
                     cl=TermUtils.createTerm("cons",smt,cl);
                 }
                 cl=TermUtils.reverseListTerm(cl);
                 retval = TermUtils.createTerm("CatchSequenceModel",cl,tctx);
             }
             break;
             case CLASS_OR_INTERFACE_DECLARATION:
             {
                 retval = TermUtils.createTerm("ClassOrInterfaceDeclarationModel",getLocalType().getModelTerm(),tctx);
             }
             break;
             case CONTINUE_STATEMENT:
             {
                 Term label = TermUtils.createNil();
                 if (t_.getArity() > 0) {
                     label = t_.getSubtermAt(0);
                 }
                 retval = TermUtils.createTerm("ContinueStatementModel",label,tctx);
             }
             break;
             case DO_STATEMENT:
             {
                 Term im = childs_.get(0).getModelTerm();
                 Term em = expressions_.get(0).getModelTerm();
                 retval = TermUtils.createTerm("DoStatementModel",im,em,tctx);
             }
             break;
             case EMPTY_STATEMENT:
                 retval = TermUtils.createTerm("EmptyStatementModel",tctx);
                 break;
             case EXPLICIT_SUPER_CONSTRUCTOR_INVOCATION:
             {
                 Term primary=TermUtils.createNil();
                 int argsBegin=0;
                 if (t_.getArity()==2) {
                     primary=expressions_.get(0).getModelTerm();
                     argsBegin=1;
                 }
                 Term argsList = TermUtils.createNil();
                 for(JavaTermExpressionModel e: expressions_.subList(argsBegin,expressions_.size())) {
                     Term te = e.getModelTerm();
                     argsList = TermUtils.createTerm("cons",te,argsList);
                 }
                 argsList=TermUtils.reverseListTerm(argsList);
                 retval = TermUtils.createTerm("ExplicitSuperInvocationModel",primary,argsList,tctx);
             }
             break;
             case EXPLICIT_THIS_CONSTRUCTOR_INVOCATION:
             {
                 Term argsList = TermUtils.createNil();
                 for(JavaTermExpressionModel e: expressions_) {
                     Term te = e.getModelTerm();
                     argsList = TermUtils.createTerm("cons",te,argsList);
                 }
                 argsList=TermUtils.reverseListTerm(argsList);
                 retval = TermUtils.createTerm("ExplicitThisInvocationModel",argsList,tctx);
             }
             break;
             case FOR_STATEMENT:
             {
                 Term stm=childs_.get(0).getModelTerm();
                 Term loopHead = t_.getSubtermAt(0);
                 if (loopHead.getName().equals("TraditionalForLoopHead")) {
                     Term initTerm = loopHead.getSubtermAt(0);
                     int exprIndex=0;
                     if (!initTerm.isNil()) {
                         if (initTerm.getName().equals("ForInit")) {
                             initTerm = initTerm.getSubtermAt(0);
                         }
                         // we have localVariableDeclarations.
                         if (initTerm.getName().equals("LocalVariableDeclaration")) {
                             Term lvList = TermUtils.createNil();
                             for(JavaLocalVariableModel v : localVariables_) {
                                 Term vt=v.getModelTerm();
                                 lvList = TermUtils.createTerm("cons",vt,lvList);
                                 if (v.getInitExpressionModel()!=null) {
                                     ++exprIndex;
                                 }
                             }
                             lvList=TermUtils.reverseListTerm(lvList);
                             initTerm=lvList;
                         }else if(initTerm.getName().equals("StatementExpressionList")){
                             Term exprList = TermUtils.createNil();
                             Term l=initTerm.getSubtermAt(0);
                             while(!l.isNil()) {
                                 l=l.getSubtermAt(1);
                                 Term expr=expressions_.get(exprIndex).getModelTerm();
                                 exprList=TermUtils.createTerm("cons",expr,exprList);
                                 ++exprIndex;
                             }
                             exprList=TermUtils.reverseListTerm(exprList);
                             initTerm=exprList;
                         }else{
                             throw new InvalidJavaTermException("Invalid ForInit: LocalVariableDeclaration or StatementExpressionList expected",initTerm);
                         }
                     }
                     Term condExprTerm = loopHead.getSubtermAt(1);
                     if (!condExprTerm.isNil()) {
                         condExprTerm=expressions_.get(exprIndex).getModelTerm();
                         ++exprIndex;
                     }
                     Term incrExprTerm = loopHead.getSubtermAt(2);
                     if (!incrExprTerm.isNil()){
                         if (incrExprTerm.getName().equals("ForUpdate")) {
                             incrExprTerm=incrExprTerm.getSubtermAt(0);
                         }
                         Term exprList = TermUtils.createNil();
                         Term l = incrExprTerm.getSubtermAt(0);
                         while(!l.isNil()) {
                             l=l.getSubtermAt(1);
                             Term expr=expressions_.get(exprIndex).getModelTerm();
                             ++exprIndex;
                             exprList=TermUtils.createTerm("cons",expr,exprList);
                         }
                         exprList=TermUtils.reverseListTerm(exprList);
                         incrExprTerm=exprList;
                     }
                     loopHead = TermUtils.createTerm("TraditionalForLoopHeadModel",initTerm,condExprTerm,incrExprTerm);
                 }else if(loopHead.getName().equals("ForEachLoopHead")) {
                     // TODO:
                     //  s/getSunbtermAt(0)/typeTerm ?
                     loopHead = TermUtils.createTerm("ForEachLoopHeadModel",loopHead.getSubtermAt(0),loopHead.getSubtermAt(1),expressions_.get(0).getModelTerm());
                 }else{
                     throw new AssertException("Invalid for loop head:"+TermHelper.termToString(loopHead));
                 }
                 retval = TermUtils.createTerm("ForStatementModel",loopHead,stm,tctx);
             }
             break;
             case IF_STATEMENT:
             {
                 Term exprTerm = expressions_.get(0).getModelTerm();
                 Term ifTrue = childs_.get(0).getModelTerm();
                 Term ifFalse = TermUtils.createNil();
                 if (t_.getArity()==3) {
                     ifFalse = childs_.get(1).getModelTerm();
                 }
                 retval = TermUtils.createTerm("IfStatementModel",exprTerm,ifTrue,ifFalse,tctx);
             }
             break;
             case LABELED_STATEMENT:
             {
                 Term id=t_.getSubtermAt(0);
                 Term st=childs_.get(0).getModelTerm();
                 retval = TermUtils.createTerm("LabeledStatementModel",id,st,tctx);
             }
             break;
             case LOCAL_VARIABLE_DECLARATION:
             {
                 Term localVariableModels = TermUtils.createNil();
                 for(JavaLocalVariableModel vm: localVariables_) {
                     Term vmt=vm.getModelTerm();
                     localVariableModels = TermUtils.createTerm("cons",vmt,localVariableModels);
                 }
                 localVariableModels = TermUtils.reverseListTerm(localVariableModels);
                 retval = TermUtils.createTerm("LocalVariableDeclarationModel",localVariableModels,tctx);
             }
             break;
             case RETURN_STATEMENT:
             {
                 Term expr = TermUtils.createNil();
                 if (t_.getArity()>0) {
                     expr = expressions_.get(0).getModelTerm();
                 }
                 retval = TermUtils.createTerm("ReturnStatementModel",expr,tctx);
             }
             break;
             case STATEMENT_EXPRESSION_STATEMENT:
             {
                 retval = TermUtils.createTerm("StatementExpressionStatement",expressions_.get(0).getModelTerm(),tctx);
             }
             break;
             case SWITCH_STATEMENT:
             {
                 Term mslbl = TermUtils.createNil();
                 int i=0;
                 Term oslbl = t_.getSubtermAt(1);
                 while(!oslbl.isNil()) {
                     Term stm = childs_.get(i).getModelTerm();                  
                     mslbl = TermUtils.createTerm("cons",stm,mslbl);
                     oslbl=oslbl.getSubtermAt(1);
                     ++i;
                 }
                 mslbl = TermUtils.reverseListTerm(mslbl);
                 retval = TermUtils.createTerm("SwitchStatementModel",expressions_.get(0).getModelTerm(),mslbl,tctx);
             }
             break;
             case SWITCH_LABEL_BLOCK:
             {
                 Term childsListTerm = TermUtils.createNil();
                 for(JavaStatementModel cst: childs_) {
                     Term ctm = cst.getModelTerm();
                     childsListTerm = TermUtils.createTerm("cons",ctm,childsListTerm);
                 }
                 childsListTerm = TermUtils.reverseListTerm(childsListTerm);
                 retval = TermUtils.createTerm("SwitchLabelBlockModel",expressions_.get(0).getModelTerm(),childsListTerm,tctx);
             }
             break;
             case SYNCHRONIZED_STATEMENT:
             {
                 Term stm = childs_.get(0).getModelTerm();
                retval = TermUtils.createTerm("SynchronizedStatementModel",t_.getSubtermAt(0),stm,tctx);
             }
             break;
             case THROW_STATEMENT:
             {
                 retval = TermUtils.createTerm("ThrowStatementModel",expressions_.get(0).getModelTerm(),tctx);
             }
             break;
             case TRY_STATEMENT:
             {
                 Term blockModel = childs_.get(0).getModelTerm();
                 Term catchSequenceModel = childs_.get(1).getModelTerm();
                 Term finallyModel = TermUtils.createNil();
                 if (t_.getArity()==3) {
                     finallyModel = childs_.get(2).getModelTerm();
                 }
                 retval = TermUtils.createTerm("TryStatementModel",blockModel,catchSequenceModel,finallyModel,tctx);
             }
             break;
             case WHILE_STATEMENT:
             {
                 Term expr = expressions_.get(0).getModelTerm();
                 Term child = childs_.get(0).getModelTerm();
                 retval = TermUtils.createTerm("WhileStatementModel",expr,child,tctx);
             }
             break;
             default:
                 throw new AssertException("Unknown Statement Type:"+getKind().toString());
         }
         return retval;
     }
     
     private void extractLocalVariableExpressionsAndAnonimousTypesFromLoopHead(Term loopHead) throws TermWareException {
         if (loopHead.getName().equals("TraditionalForLoopHead")){
             Term initTerm=loopHead.getSubtermAt(0);
             if (!initTerm.isNil()) {
                 if (initTerm.getName().equals("ForInit")) {
                     initTerm=initTerm.getSubtermAt(0);
                 }
                 if (initTerm.getName().equals("LocalVariableDeclaration")) {
                     extractLocalVariablesExpressionsAndAnonimousTypesFromLocalVariableDeclaration(initTerm);
                 }else{
                     // statementExpressionList
                     Term l=initTerm.getSubtermAt(0);
                     while(!l.isNil()) {
                         Term te = l.getSubtermAt(0);
                         l=l.getSubtermAt(1);
                         extractAnonimousTypes(te);
                         JavaTermExpressionModel e = JavaTermExpressionModel.create(te,this,this.getTopLevelBlockModel().getOwnerModel().getTypeModel());
                         expressions_.add(e);
                     }
                 }
             }
             Term checkExprTerm = loopHead.getSubtermAt(1);
             if (!checkExprTerm.isNil()) {
                 extractAnonimousTypes(checkExprTerm);
                 JavaTermExpressionModel te = JavaTermExpressionModel.create(checkExprTerm,this,this.getTopLevelBlockModel().getOwnerModel().getTypeModel());
                 expressions_.add(te);
             }
             Term incrExprTerm = loopHead.getSubtermAt(2);
             if (!incrExprTerm.isNil()) {
                 if (incrExprTerm.getName().equals("ForUpdate")) {
                     incrExprTerm = incrExprTerm.getSubtermAt(0);
                 }
                 if (incrExprTerm.getName().equals("StatementExpressionList")) {
                     Term l = incrExprTerm.getSubtermAt(0);
                     while(!l.isNil()) {
                         Term te=l.getSubtermAt(0);
                         l=l.getSubtermAt(1);
                         extractAnonimousTypes(incrExprTerm);
                         JavaTermExpressionModel e = JavaTermExpressionModel.create(te,this,this.getTopLevelBlockModel().getOwnerModel().getTypeModel());
                         expressions_.add(e);
                     }
                 }
             }
         }else if(loopHead.getName().equals("ForEachLoopHead")){
             Term typeTerm = loopHead.getSubtermAt(0);
             Term identifierTerm = loopHead.getSubtermAt(1);
             String name=identifierTerm.getSubtermAt(0).getString();
             Term modifiersTerm=TermUtils.createTerm("Modifiers",TermUtils.createInt(0),TermUtils.createNil());
             localVariables_.add(new JavaTermLocalVariableModel(identifierTerm,JavaLocalVariableKind.FOR_EACH_LOOP_HEAD,modifiersTerm,typeTerm,loopHead.getSubtermAt(2),null, this));
             Term exprTerm = loopHead.getSubtermAt(2);
             JavaTermExpressionModel te = JavaTermExpressionModel.create(exprTerm,this,this.getTopLevelBlockModel().getOwnerModel().getTypeModel());
             expressions_.add(te);
             extractAnonimousTypes(exprTerm);
         }else{
             throw new AssertException("Invalid loop head:"+TermHelper.termToString(loopHead));
         }
     }
     
     private void extractLocalVariablesExpressionsAndAnonimousTypesFromLocalVariableDeclaration(Term dcl) throws TermWareException {
         Term modifiersTerm = dcl.getSubtermAt(0);
         Term typeTerm = dcl.getSubtermAt(1);
         Term dclList=dcl.getSubtermAt(2);
         
         while(!dclList.isNil()) {
             Term vdcl=dclList.getSubtermAt(0);
             dclList=dclList.getSubtermAt(1);
             Term identifier=null;
             Term refTypeTerm=typeTerm;
             Term vid=vdcl.getSubtermAt(0);
             if (vid.getName().equals("VariableDeclaratorId")) {
                 identifier=vid.getSubtermAt(0);
                 if (vid.getArity()>1) {
                     int nReferences=vid.getSubtermAt(1).getInt();
                     if(nReferences>0) {
                         Term it=TermWare.getInstance().getTermFactory().createInt(nReferences);
                         refTypeTerm=TermWare.getInstance().getTermFactory().createTerm("ReferenceType",it,typeTerm);
                     }
                 }
             }else if (vid.getName().equals("Identifier")) {
                 identifier=vid;
             }else{
                 System.err.println("dcl="+TermHelper.termToString(dcl));
                 FileAndLine fl = JUtils.getFileAndLine(dcl);                
                 throw new AssertException("Invalid variabl declarator :"+fl.getFname()+","+fl.getLine()+":"+TermHelper.termToString(vid));
             }
             Term initTerm=TermWare.getInstance().getTermFactory().createNIL();
             JavaTermExpressionModel expr=null;
             if (vdcl.getArity()>1) {
                 initTerm=vdcl.getSubtermAt(1);
                 extractAnonimousTypes(initTerm);
                 expr = JavaTermExpressionModel.create(initTerm,this,this.getTopLevelBlockModel().getOwnerModel().getTypeModel());
                 expressions_.add(expr);
             }
             //String name=identifier.getSubtermAt(0).getString();
             localVariables_.add(new JavaTermLocalVariableModel(identifier,JavaLocalVariableKind.LOCAL_VARIABLE_DCL,modifiersTerm,refTypeTerm,initTerm,expr,this));
             //!!!
            // if (this.getTopLevelBlockModel().getOwnerModel().getTypeModel().getName().endsWith("Hashtable")) {
            //     System.out.println("add lv "+name+", type: "+refTypeTerm.toString());
            // }
         }
     }
     
     private void extractLocalVariableFromFormalParameter(Term fp) throws TermWareException {
         Term modifiersTerm = fp.getSubtermAt(0);
         Term typeTerm = fp.getSubtermAt(1);
         Term vid = fp.getSubtermAt(2);
         Term refTypeTerm=typeTerm;
         Term init = TermWare.getInstance().getTermFactory().createNil();
         Term idTerm;
         if (vid.getName().equals("Identifier")) {
             idTerm=vid;
         }else if(vid.getName().equals("VariableDeclaratorId")) {
             idTerm=vid.getSubtermAt(0);
             if (vid.getArity()>1) {
                 int nReferences=vid.getSubtermAt(1).getInt();
                 if(nReferences>0) {
                     Term it=TermWare.getInstance().getTermFactory().createInt(nReferences);
                     refTypeTerm=TermWare.getInstance().getTermFactory().createTerm("ReferenceType",it,typeTerm);
                 }
             }
         }else{
             throw new AssertException("Invalid VariableDeclaratorId:"+TermHelper.termToString(vid));
         }
         //String name=idTerm.getSubtermAt(0).getString();
         localVariables_.add(new JavaTermLocalVariableModel(idTerm,JavaLocalVariableKind.CATCH_EXCEPTION,modifiersTerm,refTypeTerm,init,null,this));        
     }
     
     private void extractAnonimousTypes(Term expr) throws TermWareException {
         if (expr.isComplexTerm()) {
             if (expr.getName().equals("AllocationExpression")) {
                 Term acb = expr.getSubtermAt(3);
                 if (!acb.isNil()) {
                     JavaTermAnonimousTypeModel tm = new JavaTermAnonimousTypeModel(this,expr);
                     // tm added to parent in constructor. so - do nothing.
                 }
             }else{
                 // run on subterms.
                 // possible in fututr: optimize, by knowning expression model.
                 for(int i=0; i< expr.getArity(); ++i) {
                     extractAnonimousTypes(expr.getSubtermAt(i));
                 }
             }
         }
     }
     
     private void addLocalType(JavaTermClassOrInterfaceModel localType) {
         localType_=localType;
         localType.setIsLocal(this);
     }
     
     private JavaStatementKind kind_;
     private Term              t_;
     private JavaTermTopLevelBlockModel blockModel_;
     private JavaTermStatementModel parent_=null;
     private JavaTermStatementModel previous_=null;
     
     private List<JavaStatementModel> childs_;
     private List<JavaLocalVariableModel> localVariables_=new LinkedList<JavaLocalVariableModel>();
     private JavaTypeModel localType_=null;
     private List<JavaTermExpressionModel> expressions_=null;
     
 }
