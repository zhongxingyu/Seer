 package xtc.oop;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 import xtc.util.Runtime;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /*
  * Builds a CPP AST tree using a Java AST tree
  * Uses new types of nodes and new tree structure
  *
  */
 
 
 interface CPPUtil {
     // Custom GNode types:
     public static final String kHeadDec = "HeaderDeclaration";
     public static final String kImplDec = "ImplementationDeclaration";
     public static final String kStrmOut = "StreamOutputList";
 	
     // xtc GNode types:
     public static final String kCmpStmt = "CompoundStatement";
     public static final String kDecl = "Declaration";
     public static final String kDeclSpef = "DeclarationSpecifiers";
     public static final String kFuncDecltor = "FunctionDeclarator";
     public static final String kFuncDef = "FunctionDefinition";
     public static final String kInitDecl = "InitializedDeclarator";
     public static final String kInitDeclList = "InitializedDeclaratorList";
     public static final String kInt = "Int";
     public static final String kPrimID = "PrimaryIdentifier";
     public static final String kPtr = "Pointer";
     public static final String kRoot = "TranslationUnit";
     public static final String kSimpDecl = "SimpleDeclarator";
     public static final String kStrLtrl = "StringLiteral";
     public static final String kStruct = "StructureTypeDefinition";
     public static final String kStructDecl = "StructureDeclaration";
     public static final String kStructDeclList = "StructureDeclarationList";
     public static final String kTypedef = "TypedefSpecifier";
 
 }
 
 public class LeafTransplant extends Visitor implements CPPUtil {
     
 
     public static boolean DEBUG = false;
 
 
     GNode javaTree;
     GNode cppTree;
     ClassLayoutParser clp;
     
     // Pros/Cons of global variables?
     String className;
     GNode classImplementation;
     GNode expressionStatement;
 
 
     GNode thisClassDataLayoutStructDeclList; 
     GNode thisClassVTableStructDeclList;
     
 
     // ------------------------------------------
     // ------------- Constructor ----------------
     // ------------------------------------------
 
     // @param clp To gain access to vtables and data layouts
     // @param javaAST
     public LeafTransplant(ClassLayoutParser clp, GNode javaAST, boolean db) { 
 		this.cppTree = GNode.create("TranslationUnit");
 		this.javaTree = javaAST;
 		this.clp = clp;
 		DEBUG = db;
 		
 		// Create a CPP tree as we visit the Java AST
 		createCPPTree();
     } 
     
     // ------------------------------------------
     // --------- Begin CPP Translation  ---------
     // ------------------------------------------
 
 
     public void createCPPTree() {
 	// We add nodes to the CPP AST in 3 stages:
 	// ImportDeclaration(s), ClassDeclaration - for header, 
 	// and ClassDeclaration - for body nodes.
 
 	// TODO: Vtable lookups at method invocation
 
 	new Visitor() {
 
 	    public void visitImportDeclaration(GNode n) {
        		// Add import node to cpp tree
 		addImportNode(n);
 	    }
 
 	    public void visitClassDeclaration(GNode n) {
 		// Build and add header node
 		translateClassDeclaration(n);
 		// Build and add implementation node
    		translateClassBody(n);
        
 	    }
 	    
 	  
 	    public void visit(GNode n) {
 		// Need to override visit to work for GNodes
 		for( Object o : n) {
 		    if (o instanceof Node) dispatch((GNode)o);
 		}
 	    }
 	    
 	}.dispatch(javaTree);
 
 
 	cppTree.add(className);
     }
 
     // ------------------------------------------
     // ----- Translate ImportDeclarations  ------
     // ------------------------------------------
 
     // Global variable required to ensure placement of ImportDeclarations 
     // at beginning of CPP AST
     GNode importDeclarations = GNode.create("ImportDeclarations");
 
     // Creates corresponding CPP import nodes
     // TODO: Printer will add appropriate syntax
     // @param n ImportDeclaration node from Java AST
     public void addImportNode (GNode n) {
 
 	GNode importNode  = n;
 	importDeclarations.add(importNode);
     }
 
     // ------------------------------------------
     // -------- Build HeaderDeclaration ---------
     // ------------------------------------------
 
 
     // At ClassDeclaration, we build hNode to hold info. for the header file
     // @param n ClassDeclaration node from Java AST
     public void translateClassDeclaration (GNode n) {
 
 	// TODO: Use className to name output files
 	className = n.get(1).toString();
 
 	// hNode contains all information for the .h file to be printed
 	GNode hNode = buildHeader(n);
 
 	cppTree.add(hNode);
 	
     }
 
     // Creates the header node for a Class
     // @param n ClassDeclaration node from Java AST
     GNode buildHeader(GNode n) {
 	// Nodes are created "inside out" from the leaves up
 
 	GNode hNode = GNode.create("HeaderDeclaration"); 
 
 	GNode typedef = buildTypedef(n);
 	hNode.addNode(typedef);
 
 	GNode dataLayout = buildDataLayout(n);
 	hNode.addNode(dataLayout);
 
 	GNode vtable = buildVTable(n);
 	hNode.addNode(vtable);
 
 	return hNode;
     }
 
     // Build CPP Data Layout nodes to create struct __Class { }
     // @param n ClassDeclaration node from Java AST
     public GNode buildDataLayout(GNode n) {
 	// Nodes are created "inside out" from the leaves up
        
 	// Populate our Data Layout with information
     	GNode dataDeclarationList = GNode.create("StructureDeclarationList");
 	GNode dl = clp.getDataLayout(className);
 
 	// Copy data layout members to StructureDeclarationList
 	for (Iterator<?> iter = dl.iterator(); iter.hasNext(); ) {
 	    dataDeclarationList.add(iter.next());  
 	}
 
 	// -------------------------------------------------------------------
 
 	// Build the skeleton of the Data Layout struct
 	GNode structTypeDef = 
 	    GNode.create("StructureTypeDefinition", "DataLayout", className, 
 			 dataDeclarationList, null);
        	
 	//	GNode declarationSpecifiers = GNode.create("DeclarationSpecifiers", 
 	//						   structTypeDef);
 
 
 	GNode dataLayout = GNode.create("Declaration", structTypeDef);
 
 
 	return dataLayout;
     }
     
     // Build CPP VTable nodes to create struct __Class_VT { }
     // @param n ClassDeclaration node from Java AST
     public GNode buildVTable(GNode n) {
 	// Nodes are created "inside out" from the leaves up
 
 	GNode vtableDeclarationList = GNode.create("StructureDeclarationList");
 	GNode vt = clp.getVTable(className);
 
 	// Copy vtable members to StructureDeclarationList
 	for (Iterator<?> iter = vt.iterator(); iter.hasNext(); ) {
 	    vtableDeclarationList.add(iter.next());  
 	}
 	
 	// --------------------------------------------------------------------
 
 	// Build skeleton of VTable struct
 	GNode vtableStructDefinition = 
 	    GNode.create("StructureTypeDefinition", "VTable", className, 
 			 vtableDeclarationList, null);
 
 	//	GNode vtableDeclarationSpecifiers = 
 	//	    GNode.create("DeclarationSpecifiers", vtableStructDefinition);
 
 	GNode vtable = 
 	    GNode.create("Declaration", vtableStructDefinition);
        
 	return vtable;
     }
 
     // Build CPP typedef node to create typedef __Class* Class;
     // Typedefs are declarations for Objects 
     // @param n ClassDeclaration node from Java AST
     public GNode buildTypedef(GNode n) {
 	// Nodes are created "inside out" from the leaves up
 
 	GNode simpleDeclarator = GNode.create("SimpleDeclarator", className);
 	
 	GNode initializedDeclarator =
 	    GNode.create("InitializedDeclarator", null, simpleDeclarator, 
 			 null,null, null);
 	    
 	GNode initializedDeclaratorList = 
 	    GNode.create("InitializedDeclaratorList", initializedDeclarator);
 	    
 	// ----------------------------------------------------------------
 	    
 	GNode typedefSpecifier = GNode.create("TypedefSpecifier");
 	
 	// removed * because Smart Pointers don't use *
 	// removed __ because need clean classname
 	GNode classIdentifier = 
 	    createPrimaryIdentifier(className);
 
 	GNode td = GNode.create("TypedefDeclaration", 
 				typedefSpecifier, classIdentifier);
 	
 	// ----------------------------------------------------------------
 
 	GNode forwardDeclaration = GNode.create("ForwardDeclaration", 
 						initializedDeclaratorList, td);
 
 	GNode typedef = GNode.create("Declaration", forwardDeclaration);
 
 	/**
 	GNode typedef = 
 	    GNode.create("Declaration", null, typedefDeclarationSpecifiers, 
 		     initializedDeclaratorList);
 	**/
 
 	return typedef;
     }
 
 
 
     // ------------------------------------------
     // -------------- Build ccNode --------------
     // ------------------------------------------
 
     // At ClassBody, we build the node holding all .cc information
     // @param n ClassBody node from Java AST
     public void translateClassBody(GNode n) {
 
 	// NOTE: ccNode is now nameed "ClassBody" and must use it's visitor
 	GNode ccNode = n;
 	buildImplementation(ccNode);
 	cppTree.add(importDeclarations);
 	cppTree.add(ccNode);
 
     }
 
     // Build ccNode to hold info for the class.cc file
     // @param n ccNode, a copy of the Java ClassDeclaration node.  
     // @return Transformed ClassDeclaration node, ready for the CPPPrinter
     // NOTE: Global variable thisClass to make "this" explicit
     String thisClass = "";
     public GNode buildImplementation(GNode n) {
 
 	// FIXME: n is fixed, can'tn.addNode(importDeclarations);
 	// I wanted to put all import declarations under ClassDeclaration node.
 	// but no biggie
 	//	if(DEBUG) System.out.println("--- node " + n.getName() + " hasVariable() " + n.hasVariable());
 
 	// TODO: translate method invocations using vtable
 
 	// What can we copy directly?
 	// What needs to be 'translated' in the printer?
 	// What needs to be translated here?
 	// This needs to be done with visit methods?
 	// Does order matter? yes
 
 	new Visitor () {
 
 	    // QUERY: At FieldDeclaration, can we copy Type to subsequent 
 	    // PrimaryIdentifiers?
 	    public void visitConstructorDeclaration(GNode n) {
 		// Get .this' Class for explicit method invocation
 		// FIXME: In Java AST, the instance name is not stored.
 		// Do we need it for cpp, and how do we get it?
 		thisClass = n.getString(2);
 		//		System.out.println("\t--- Entered class " + thisClass);
 		visit(n);
 	    }
 
 	    public void visitExpressionStatement(GNode n) {
 		// Set a global variable for tree traversal: 
 		expressionStatement = n;
 		visit(n);
 	    }
 	    
 	    // Make the Class calling a method explicit
 	    // Also, translate System methods
 	    public void visitCallExpression(GNode n) {
 		//n always has 4 children
 	
 		// 1. Identify the PrimaryIdentifier - calling Class
 		String primaryIdentifier = null;
 	
 
 		if(n.getNode(0) == null || 
 		   n.getNode(0).hasName("ThisExpression")) {
 		    
 		    primaryIdentifier = thisClass;
 		    if(DEBUG) System.out.println("\t--- primaryIdentifier = " + 
 				       primaryIdentifier);
 		}
 		else if(n.getNode(0).hasName("SelectionExpression")) {
 		  
 		    primaryIdentifier = n.getNode(0).getNode(0).getString(0);
 		    if(DEBUG) System.out.println("\t--- primaryIdentifier = " 
 						 + primaryIdentifier);
 
 
 		    // Are only System.outs wrapped in SelectionExpression 
 		    // nodes? Is this if needed?
 		    if("System".equals(primaryIdentifier)) {
 			
 			// Change any + to >>
 			new Visitor () {
 			    
 			    public void visitAdditiveExpression(GNode n) {
 				if("+".equals(n.getString(1))) {
 				    n.set(1, "<<");
 				}
 			    }
 
 			    public void visit(GNode n) {
 				// Need to override visit to work for GNodes
 				for( Object o : n) {
 				    if (o instanceof Node) dispatch((GNode)o);
 				}
 			    }
 
 			}.dispatch(n); // end Visitor
 
 			if( "println".equals(n.getString(2)) ) {
 			    GNode strOut = GNode.create(kStrmOut);
 			    strOut.add(0, GNode.create( kPrimID ).add(0, "std::cout") );
 			    // Add all arguments to System.out.println
 			    for(int i = 0; i < n.getNode(3).size(); i++) {
 				// removed addindex 1
 				strOut.add(n.getNode(3).get(i) ); 
 			    }
 			    
 			    // removed add index 2
 			    strOut.add(GNode.create( kPrimID ).add(0, "std::endl") );
 			    
 			    expressionStatement.set(0, strOut);
 			}
 			
 			else if("print".equals(n.getString(2))) {
 			    GNode strOut = GNode.create(kStrmOut);
 			    strOut.add(0, GNode.create( kPrimID ).add(0, "std::cout") );
 			    // Add all arguments to System.out.print
 			    for(int i = 0; i < n.getNode(3).size(); i++) {
 				strOut.add(1, n.getNode(3).get(i) ); 
 			    }
 			    expressionStatement.set(0, strOut);
 			} 
 			
 		    }// end if "System"
 		} // end SelectionExpression
 
 		else if(n.getNode(0).hasName("PrimaryIdentifier")){
 		    // Do nothing
 		    primaryIdentifier = n.getNode(0).getString(0);
 		    if(DEBUG) System.out.println("\t--- primaryIdentifier = " 
 						 + primaryIdentifier);
 		}
 		else if(n.getNode(0).hasName("SuperExpression")) {
 
 		    // Replace Java keyword super with actual class
 		    GNode pI = GNode.create("PrimaryIdentifier");
 		   
 		    GNode vtList = clp.getVTable(thisClass);
 		    GNode superNode = clp.getSuperclass(className);
 		    String superName = clp.getName(superNode);
 		    pI.add(0, superName);
 		    n.set(0, pI);
 		}
 		else { // catch all
		    System.out.println("\t--- Didn't translate node " + 
 				       n.getNode(0).toString());
 		}
 		
 		visit(n);
 	    }// End visitCall Expression
 
 
 	    public void visitFieldDeclaration(GNode n) {
 		// Translate Arrays - ned to get Type
 		if(null != n.getNode(2).getNode(0).getNode(2) && 
 		   n.getNode(2).getNode(0).getNode(2).hasName("ArrayInitializer")) 
 		    {
 
 			// Can't add Type to Declarators, Declarator, 
 			// or ArrayInitializer as fixed num children
 			// FUCK YOU, 
 
 			// Does this remove Type node from FieldDeclaration?
 
 			//			n.getNode(2).getNode(0).getNode(2).add(n.getNode(1));
 		    }
 	
 	    }
 	    
 	    public void visit(GNode n) {
 		// Need to override visit to work for GNodes
 		for( Object o : n) {
 		    if (o instanceof Node) dispatch((GNode)o);
 		}
 	    }
 	    
 	}.dispatch(n);//end Visitor
 	
 	return n;
     }
 
  
     // ------------------------------------------
     // ----------- Internal Methods  ------------
     // ------------------------------------------
 
     // Creates and returns a simple primary identifier node, 
     // which is really just a string name of a variable.
     GNode createPrimaryIdentifier( String contents ) {
 
 	return (GNode)GNode.create( kPrimID ).add(contents);
     }
 
 
    // ------------------------------------------
     // ------------- Getter Methods  ------------
     // ------------------------------------------
     
     public GNode getCPPTree() { return cppTree; }
     
 
 }
