 package visitor;
 import syntaxtree.*;
 import java.util.*;
 import set.*;
 public class MhpVisitor extends DepthFirstVisitor {
     public StringPairSet M;
     public StringSet L;
     public StringSet O;
     private File _file;
 
     
     public MhpVisitor(){
 	M = new StringPairSet();
 	L = new StringSet();
 	O = new StringSet();
     }
 
 
     /**
     * f0 -> ( TopLevelDeclaration() )*
     * f1 -> <EOF>
     */
 
     public void visit(File n ) {
 	_file = n;
 	n.f0.accept(this);
 	n.f1.accept(this);
 	System.out.println("MHP (End)");
 	System.out.println(this.M);
     }
 
 
        /**
     * f0 -> "public"
     * f1 -> "class"
     * f2 -> Identifier()
     * f3 -> "{"
     * f4 -> "public"
     * f5 -> "static"
     * f6 -> "void"
     * f7 -> "main"
     * f8 -> "("
     * f9 -> "String"
     * f10 -> "["
     * f11 -> "]"
     * f12 -> Identifier()
     * f13 -> ")"
     * f14 -> "{"
     * f15 -> Statement()
     * f16 -> "}"
     * f17 -> "}"
     */
    public void visit(MainClass n) {
       n.f0.accept(this);
       n.f1.accept(this);
       n.f2.accept(this);
       n.f3.accept(this);
       n.f4.accept(this);
       n.f5.accept(this);
       n.f6.accept(this);
       n.f7.accept(this);
       n.f8.accept(this);
       n.f9.accept(this);
       n.f10.accept(this);
       n.f11.accept(this);
       n.f12.accept(this);
       n.f13.accept(this);
       n.f14.accept(this);
       n.f15.accept(this);
       n.f16.accept(this);
       n.f17.accept(this);
    }
 
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( ClassMember() )*
     * f4 -> "}"
     */
    public void visit(ClassDeclaration n) {
        // we don't visit class declarations when reading the file
        // we will visit functions of a particular class
        // if they are called
 
        //n.f0.accept(this);
        //n.f1.accept(this);
        //n.f2.accept(this);
        //n.f3.accept(this);
        //n.f4.accept(this);
        return;
    }
 
 
     /**
      * f0 -> Assignment()
      *       | AsyncStatement()
      *       | Block()
      *       | FinishStatement()
      *       | IfStatement()
      *       | LoopStatement()
      *       | PostfixStatement()
      *       | PrintlnStatement()
      *       | ReturnStatement()
      *       | ThrowStatement()
      *       | WhileStatement()
      */
     public void visit(Statement n) {
 	n.f0.accept(this);
 	Object o = n.f0.choice;
 	copySets(n, o);
 	printStatement(n);
     }
 
     /**
      * f0 -> "finish"
      * f1 -> Statement()
      */
     public void visit(FinishStatement n) {
 	n.f0.accept(this);
 	n.f1.accept(this);
 	n.M = n.f1.M;
 	n.L = n.f1.L;
 	
 	// Clear the global MHP with all the statements
 	// which were executed in this finish block
 	this.M = this.M.subtract(n.L);
 
 	// clear the global O with all the statements
 	// which were added to it in this finish block
 	this.O = this.O.subtract(n.L);
 
 	// add the list of statements executed to the global one
 	this.L = this.L.union(n.f1.L);
 	n.M = this.M;
 	n.O.clear();
     }
 
 
     /**
      * f0 -> "while"
      * f1 -> "("
      * f2 -> Expression()
      * f3 -> ")"
      * f4 -> Statement()
      */    
 
     public void visit(WhileStatement n) {
 	while(true) {
 	    n.f0.accept(this);
 	    n.f1.accept(this);
 	    n.f2.accept(this);
 	    n.f3.accept(this);
 	    n.f4.accept(this);
 	    
 	    Statement s1 = n.f4;
 	    // Apply the rule loop s1
 	    StringPairSet mdash = this.M.union(s1.O.symcross(s1.L));
 	    if(mdash.equals(this.M))
 		break;
 	}
 	n.O = n.f4.O;
 	n.L = n.f4.L;
 	n.M = n.f4.M;
 	    
 	this.O = this.O.union(n.f4.O);
 	this.L = this.L.union(n.f4.L);
 	
     }
 
     /**
      * f0 -> "for"
      * f1 -> "("
      * f2 -> PointType()
      * f3 -> ExplodedSpecification()
      * f4 -> ":"
      * f5 -> Expression()
      * f6 -> ")"
      * f7 -> Statement()
      */
 
     public void visit(LoopStatement n) {
 
 	while(true) {
 	    n.f0.accept(this);
 	    n.f1.accept(this);
 	    n.f2.accept(this);
 	    n.f3.accept(this);
 	    n.f4.accept(this);
 	    n.f5.accept(this);
 	    n.f6.accept(this);
 	    n.f7.accept(this);
 	    
 	    Statement s1 = n.f7;
 	    // Apply the rule loop s1
 	    StringPairSet mdash = this.M.union(s1.O.symcross(s1.L));
 	    if(mdash.equals(this.M))
 		break;
 	}
 	n.O = n.f7.O;
 	n.L = n.f7.L;
 	n.M = n.f7.M;
 	    
 	this.O = this.O.union(n.f7.O);
 	this.L = this.L.union(n.f7.L);
 	
 	
     }
 
 
     /**
      * f0 -> Expression()
      * f1 -> ";"
      */
 
     public void visit(PostfixStatement statement) {
 	// TODO: Need to check whether Expression is a function
 	Expression expr = statement.f0;
 	TextVisitor t = new TextVisitor();
 	t.visit(expr);
 	
 	this.visit(expr);
 	if(expr.f0.choice instanceof DotMethodCall == false) {
 	    // Add the expression to the L field of statement
 	    statement.L.add(statement.getLabel() + t.getText());
 	}
 	else {	    
 	    DotMethodCall method = (DotMethodCall)expr.f0.choice;
 	    statement.L = method.L;
	}
 
	updateBlockProduction(statement);
     }
 
     /**
      * f0 -> "System.out.println"
      * f1 -> "("
      * f2 -> Expression()
      * f3 -> ")"
      * f4 -> ";"
      */
     public void visit(PrintlnStatement n) {
 	n.f0.accept(this);
 	n.f1.accept(this);
 	n.f2.accept(this);
 	n.f3.accept(this);
 	n.f4.accept(this);
 	
 	TextVisitor t = new TextVisitor();
 	t.visit(n);
 	n.L.add(n.getLabel() + t.getText());
 	updateBlockProduction(n);
     }
 
 
 
 
     /**
      * f0 -> Expression()
      * f1 -> "="
      * f2 -> Expression()
      * f3 -> ";"
      */
 
     public void visit(Assignment n) { 
 	TextVisitor t = new TextVisitor();
 	n.f0.accept(t);
 	n.f1.accept(t);
 	n.f2.accept(t);
 	n.f3.accept(t);
 	n.L.add(n.getLabel() + t.getText());
 	updateBlockProduction(n);
     }
 
 
     /**
      * f0 -> "final"
      * f1 -> Type()
      * f2 -> Identifier()
      * f3 -> "="
      * f4 -> Expression()
      * f5 -> ";"
      */
 
     public void visit(FinalVariableDeclaration n) {
 	TextVisitor t = new TextVisitor();
 	t.visit(n);
 	n.L.add(n.getLabel() + t.getText());
 	updateBlockProduction(n);
 	printFinalDeclaration(n);
     }
 
     /**
      * f0 -> Type()
      * f1 -> Identifier()
      * f2 -> "="
      * f3 -> Expression()
      * f4 -> ";"
      */
 
     public void visit(UpdatableVariableDeclaration n) {
 	TextVisitor t = new TextVisitor();
 	t.visit(n);
 	n.L.add(n.getLabel() + t.getText());
 	updateBlockProduction(n);
 	printUpdatableDeclaration(n);
     }
 
     /**
      * f0 -> "async"
      * f1 -> "("
      * f2 -> Expression()
      * f3 -> ")"
      * f4 -> Block()
      */
 
     public void visit(AsyncStatement n) {
 	n.f0.accept(this);
 	n.f1.accept(this);
 	n.f2.accept(this);
 	n.f3.accept(this);
 	n.f4.accept(this);
 	copySets(n, n.f4);
 	n.O = n.O.union(n.L);
 	updateAsyncProduction(n);
     }
 
     
     /**
      * f0 -> "{"
      * f1 -> ( BlockStatement() )*
      * f2 -> "}"
      */
     public void visit(Block n) {
 	n.f0.accept(this);
 	n.f1.accept(this);
 	n.f2.accept(this);
 
 	// Run through all the blocks and pick up the M, L and O values
 	if(n.f1.present()) {
 	    for(Enumeration e = n.f1.elements(); e.hasMoreElements(); ) {
 		Object node = e.nextElement();
 		unionSets(n, node);
 	    }
 	}
     }
 
     /**
      * f0 -> FinalVariableDeclaration()
      *       | UpdatableVariableDeclaration()
      *       | Statement()
      */
 
     public void visit(BlockStatement n) {
 	n.f0.accept(this);
 	Object o = n.f0.choice;
 	copySets(n, o);
     }
 
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "."
      * f2 -> Identifier()
      * f3 -> "("
      * f4 -> ( ExpressionList() )?
      * f5 -> ")"
      */
 
     public void visit(DotMethodCall n) {
 	n.f0.accept(this);
 	n.f1.accept(this);
 	n.f2.accept(this);
 	n.f3.accept(this);
 	n.f4.accept(this);
 	n.f5.accept(this);
 
 	// Find the method declaration and visit it
 	TextVisitor t = new TextVisitor();
 	t.visit(n.f2);
 	String methodName = t.getText();
 	
 	MethodFindVisitor finder = new MethodFindVisitor();
 	MethodDeclaration method = finder.find(_file, methodName);
 	this.visit(method);
 
 	t = new TextVisitor();
 	t.visit(n);
 
 	n.L = method.L;	      
 	n.O = method.O;
 	n.M = method.M;
 
     }
 
        /**
     * f0 -> "public"
     * f1 -> ReturnType()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> Block()
     */
    public void visit(MethodDeclaration n) {
       n.f0.accept(this);
       n.f1.accept(this);
       n.f2.accept(this);
       n.f3.accept(this);
       n.f4.accept(this);
       n.f5.accept(this);
       n.f6.accept(this);
       n.L = n.f6.L;
       n.M = n.f6.M;
       n.O = n.f6.O;
 	  
    }
 
 
     private void updateBlockProduction(MhpStatement statement) {
 	// Update the global and local M, O, L values
 	// s1;s2 = 
 	// M' = M1 U M2 U symcross(O1, L2)
 	// O' = O1 U O2
 	// L' = L1 U L2
 	StringPairSet mdash = this.M.union(statement.M);
 	mdash = mdash.union(this.O.symcross(statement.L));
 	
 	this.M = mdash;
 	statement.M = mdash;
 	
 	StringSet odash = this.O.union(statement.O);
 	this.O = odash;
 	statement.O = odash;
 
 	StringSet ldash = this.L.union(statement.L);
 	      
 	this.L = ldash;
 
     }
 
     private void updateAsyncProduction(MhpStatement statement) {
 	this.O = this.O.union(statement.L);
 	this.L = this.L.union(statement.L);	    
     }
     
     private void copySets(MhpStatement n, Object o) {
 	if(o instanceof MhpStatement) {
 	    MhpStatement statement = (MhpStatement) o;
 	    n.M = statement.M;
 	    n.L = statement.L;
 	    n.O = statement.O;
 	}
     }
 
     private void unionSets(MhpStatement n, Object o) {
 	if(o instanceof MhpStatement) {
 	    MhpStatement statement = (MhpStatement) o;
 	    n.M = n.M.union(statement.M);
 	    n.L = n.L.union(statement.L);
 	    n.O = n.O.union(statement.O);
 	}
     }
 
     private void print(String s) {
 	System.out.println(s);
     }
 
     private void printFinalDeclaration(FinalVariableDeclaration s) {
 	TextVisitor t = new TextVisitor();
 	t.visit(s);
 	printMOL(s, t.getText());
     }
 
     private void printUpdatableDeclaration(UpdatableVariableDeclaration s) {
 	TextVisitor t = new TextVisitor();
 	t.visit(s);
 	printMOL(s, t.getText());
     }
     private void printStatement(Statement s) {
 	TextVisitor t = new TextVisitor();
 	t.visit(s);
 	printMOL(s, t.getText());
     }
 
     private void printMOL(MhpStatement s, String label) {
 	System.out.println(label);
 	System.out.println("MHP: ");
 	System.out.println(s.M);
 	System.out.println("O: ");
 	System.out.println(s.O);
 	System.out.println("L: ");
 	System.out.println(s.L);
 
     }
 
 
 }
