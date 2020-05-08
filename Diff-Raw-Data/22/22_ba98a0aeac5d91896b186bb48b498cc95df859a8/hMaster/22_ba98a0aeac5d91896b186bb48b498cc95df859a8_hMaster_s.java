 
 package oop;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 import xtc.tree.Visitor;
 
 public class hMaster {
 	LinkedList<vTableClass> classes;
 	HashSet<String> classlist;
 	
 	LinkedList<String> waitqueue;
 	LinkedList<vTableClass> fileprint;
 	//used by cc translator for method overloading
 	HashSet<String> overloads;
 	
 	public hMaster(HashSet dependencies, File hostDirectory){
 		classes = new LinkedList<vTableClass>();
 		waitqueue = new LinkedList<String>();
 		classlist = new HashSet<String>();
 		overloads = new HashSet<String>();
 		hardIncludeJavaLangObject();
 		Iterator iterate = dependencies.iterator();
 		
 		while(iterate.hasNext()){
 			String currentfilename = (String)iterate.next();
 			//File test = new File(currentfilename);
 			translate(currentfilename, hostDirectory);
 		}
 		
 		//do not use for now
 		
 		while(!(waitqueue.isEmpty())){
 			System.out.println("waitqueue is not empty");
 			translate(waitqueue.pop(), hostDirectory);
 		}
 		
 		//setup a file
 		//write the forward declarations in the saved namespace
 		//write the rest
 		//includes
 	}
 	
 	public void hardIncludeJavaLangObject(){
 		vTableClass javaobject = new vTableClass("Object");
 		javaobject.setNoWrite();
 		javaobject.addIncludes("<stdint.h>");
 		javaobject.addIncludes("<string>");
 		javaobject.addIncludes("<iostream>");
 		javaobject.addIncludes("<cstring>");
 		javaobject.addIncludes("\"ptr.h\"");
 		
 		hardIncludeJavaLangMethod(javaobject);
 		hardIncludeJavaLangTable(javaobject);
 		hardIncludeJavaLangAddress(javaobject);
 		
 		//classlist name subject to change
 		classes.add(javaobject); //classes.add(javaclass); classes.add(javastring);
 		classlist.add("Object"); //classlist.add("Class"); classlist.add("String");
 	}
 	
 	//static void __delete(__Object*); declaration
 	//void (*__delete)(__Object*); layout
 	//__delete(&__Object::__delete),  address
 	public void hardIncludeJavaLangTable(vTableClass javaobject){
 		javaobject.newTableLayout();
 		javaobject.appendTableLayout("ReturnType", "Class");
 		javaobject.appendTableLayout("MethodName", "__isa");
 		javaobject.appendTableLayout("ReferenceType", "Object");
 		javaobject.addTableLayout();
 		javaobject.newTableLayout();
 		javaobject.appendTableLayout("ReturnType", "void");
 		javaobject.appendTableLayout("MethodName", "__delete");
 		javaobject.appendTableLayout("ReferenceType", "Object*");
 		javaobject.addTableLayout();
 		javaobject.newTableLayout();
 		javaobject.appendTableLayout("ReferenceType", "Object");
 		javaobject.appendTableLayout("ReturnType", "int32_t");
 		javaobject.appendTableLayout("MethodName", "hashCode");
 		javaobject.addTableLayout();
 		javaobject.newTableLayout();
 		javaobject.appendTableLayout("ReferenceType", "Object");
 		javaobject.appendTableLayout("ReturnType", "bool");
 		javaobject.appendTableLayout("MethodName", "equals");
 		javaobject.appendTableLayout("Parameters", "Object");
 		javaobject.addTableLayout();
 		javaobject.newTableLayout();
 		javaobject.appendTableLayout("ReferenceType", "Object");
 		javaobject.appendTableLayout("ReturnType", "Class");
 		javaobject.appendTableLayout("MethodName", "getClass");
 		javaobject.addTableLayout();
 		javaobject.newTableLayout();
 		javaobject.appendTableLayout("ReferenceType", "Object");
 		javaobject.appendTableLayout("ReturnType", "String");
 		javaobject.appendTableLayout("MethodName", "toString");
 		javaobject.addTableLayout();
 	}
 	
 	public void hardIncludeJavaLangAddress(vTableClass javaobject){
 		javaobject.newTableAddress();
 		javaobject.appendAddress("ClassName", "Object");
 		javaobject.appendAddress("MethodName", "__isa");
 		javaobject.addTableAddress();
 		javaobject.newTableAddress();
 		javaobject.appendAddress("ClassName", "Object");
 		javaobject.appendAddress("MethodName", "__delete");
 		javaobject.addTableAddress();
 		javaobject.newTableAddress();
 		javaobject.appendAddress("ClassName", "Object");
 		javaobject.appendAddress("MethodName", "hashCode");
 		javaobject.addTableAddress();
 		javaobject.newTableAddress();
 		javaobject.appendAddress("ClassName", "Object");
 		javaobject.appendAddress("MethodName", "equals");
 		javaobject.addTableAddress();
 		javaobject.newTableAddress();
 		javaobject.appendAddress("ClassName", "Object");
 		javaobject.appendAddress("MethodName", "getClass");
 		javaobject.addTableAddress();
 		javaobject.newTableAddress();
 		javaobject.appendAddress("ClassName", "Object");
 		javaobject.appendAddress("MethodName", "toString");
 		javaobject.addTableAddress();
 	}
 	
 	public void hardIncludeJavaLangMethod(vTableClass javaobject){
 		javaobject.newMethodLayout();
 		//javaobject.appendMethod("Modifier", "static");
 		javaobject.appendMethod("MethodName", "__delete");
 		javaobject.appendMethod("ReturnType", "void");
 		javaobject.appendMethod("ReferenceType", "Object*");
 		javaobject.appendMethod("ObjectVisiblity", "public");
 		javaobject.addMethod();
 		javaobject.newMethodLayout();
 		//javaobject.appendMethod("Modifier", "static");
 		javaobject.appendMethod("MethodName", "hashCode");
 		javaobject.appendMethod("ReturnType", "int32_t");
 		javaobject.appendMethod("ReferenceType", "Object");
 		javaobject.appendMethod("ObjectVisiblity", "public");
 		javaobject.addMethod();
 		javaobject.newMethodLayout();
 		//javaobject.appendMethod("Modifier", "static");
 		javaobject.appendMethod("MethodName", "equals");
 		javaobject.appendMethod("ReturnType", "bool");
 		javaobject.appendMethod("ReferenceType", "Object");
 		javaobject.appendMethod("Parameters", "Object");
 		javaobject.appendMethod("ObjectVisiblity", "public");
 		javaobject.addMethod();
 		javaobject.newMethodLayout();
 		//javaobject.appendMethod("Modifier", "static");
 		javaobject.appendMethod("MethodName", "getClass");
 		javaobject.appendMethod("ReturnType", "Class");
 		javaobject.appendMethod("ReferenceType", "Object");
 		javaobject.appendMethod("ObjectVisiblity", "public");
 		javaobject.addMethod();
 		javaobject.newMethodLayout();
 		//javaobject.appendMethod("Modifier", "static");
 		javaobject.appendMethod("MethodName", "toString");
 		javaobject.appendMethod("ReturnType", "String");
 		javaobject.appendMethod("ReferenceType", "Object");
 		javaobject.appendMethod("ObjectVisiblity", "public");
 		javaobject.addMethod();
 		
 		/*
 		vTableClass javaclass = new vTableClass("Class", "Object");
 		vTableClass javastring = new vTableClass("String", "Object");
 		*/
 	}
 	
 //	//temporary testing platform for hMaster
 //	public static void main(String[] args){
 //		DependencyMaster dependsource = new DependencyMaster();
 //		//System.out.println(args[0]);
 //		HashSet<String> set = dependsource.getDependencies(args[0]);//new HashSet<String>();
 //		//set.add(args[0]); //testing one file for now so we can get away with this
 //		//Iterator iterate = set.iterator();
 //		//System.out.println(iterate.next());
 //		//System.out.println(iterate.next());
 //		hMaster tester = new hMaster(set);
 //	}
 	
 	//question, how to sort out which classes belong to which file
 	//likely solution would be to keep a list of all classes belonging to the current file
 	public void translate(String sourcefile, File hostDirectory){
 		//String[] args = {"-printJavaAST",sourcefile};
 		//String[] args = {"-returnJavaAST", sourcefile};
 		Node sourceAST = new ASTGenerator().generateAST(sourcefile);
 		fileprint = new LinkedList<vTableClass>();
 		final String hostPath = hostDirectory.getAbsolutePath();
 //		final File leadfile = new File(sourcefile);
 //		String formatedFile = leadfile.getName();
 //		final File file = new File(hostPath + "/" + formatedFile.replace(".java", ".h"));
 //		try {
 //			file.createNewFile();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 		//System.out.println(sourceAST.isEmpty());
 		final String currentsource = sourcefile;
 		final String newhfile = sourcefile.replace(".java", ".h");
 		new Visitor() {
 			//class variables
 			vTableClass currentclass;
 			LinkedList<String> namespace;
 			boolean missingsuper;
 			String dataLayout;
 			String operation;
 			
 			public void printClass(File file, vTableClass classy){
 				//Iterator<vTableClass> iterate = fileprint.iterator();
 				FileWriter writee;
 				BufferedWriter writer = null;
 				try {
 					writee = new FileWriter(file);
 					writer = new BufferedWriter(writee);
 					//forwarddeclarations.writefile(writer);
 					Iterator classiterate = classy.overloadedmethods.iterator();
 					while(classiterate.hasNext()){
 						overloads.add(classy.classname + "^" + (String)classiterate.next());
 					}
 					classy.resolveOverloads();
 					//System.out.println("printing the class " + classy.classname);
 					//classy.printLines();
 					classy.writeFile(writer);	
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 			
 			//potentially outdated
 //			public void printFile(File file){
 //				//Iterator<vTableClass> iterate = fileprint.iterator();
 //				FileWriter writee;
 //				BufferedWriter writer = null;
 //				try {
 //					writee = new FileWriter(file);
 //					writer = new BufferedWriter(writee);
 //					forwarddeclarations.writefile(writer);
 //					while(!fileprint.isEmpty()){
 //						vTableClass classy = fileprint.pop();
 //						Iterator classiterate = classy.overloadedmethods.iterator();
 //						while(classiterate.hasNext()){
 //							overloads.add(classy.classname + "^" + (String)classiterate.next());
 //						}
 //						classy.resolveOverloads();
 //						//System.out.println("printing the class " + classy.classname);
 //						//classy.printLines();
 //						classy.writeFile(writer);	
 //						//System.out.println("finished printing the class " + classy.classname);
 //					}
 //				} catch (IOException e1) {
 //					// TODO Auto-generated catch block
 //					e1.printStackTrace();
 //				}
 //			}
 			
 			/**
 			 * Possible Parents: None
 			 * Writes to Elements: No
 			 * 
 			 * @param n GNode from the parser
 			 */
 			public void visitCompilationUnit(GNode n){
 				//System.out.println("made it");
 				namespace = new LinkedList<String>();
 				for(Object o : n){
 					if (o instanceof Node){ 
 						missingsuper = false;
 						dispatch((Node)o);
 					}
 				}
 				//if there are no explicit constructors then add the default constructor...if needed, check that
 				//implement this property in the vTableClass printline method instead of here
 				//visit(n);
 				//printFile(file);
 			}    		
 
 			/**
 			 * Possible Parents: 
 			 * Writes to Elements: No
 			 * 
 			 * covers byte, int, short, long
 			 * @param n
 			 */
 			public void visitIntegerLiteral(GNode n){
 				String returnable = ccHelper.convertType(n.getString(0));
 //				if(operation.equals("dataLayout")){
 //					dataLayout = dataLayout + returnable;
 //				}
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents: 
 			 * Writes to Elements: No
 			 * 
 			 * @param n
 			 */
 			public void visitBooleanLiteral(GNode n){
 				String returnable = ccHelper.convertType(n.getString(0));
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents:
 			 * Writes to Elements: No
 			 * 
 			 * @param n
 			 */
 			public void visitFloatingPointLiteral(GNode n){
 				String returnable = ccHelper.convertType(n.getString(0));
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents:
 			 * Writes to Elements: No
 			 * 
 			 * @param n
 			 */
 			public void visitStringLiteral(GNode n){
 				String returnable = n.getString(0);
 				visit(n);
 			}
 			
 			/**
 			 * 
 			 * @param n
 			 */
 			public void visitFieldDeclaration(GNode n){
 				if(operation.equals("dataLayout")){
 					dataLayout = "";
 				}
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents: FieldDeclaration, MethodDeclaration
 			 * Writes to Elements: No
 			 * 
 			 * @param n
 			 */
 			public void visitVoidType(GNode n){
 				if(operation.equals("Method")){
 					currentclass.appendMethod("ReturnType", "void");
 				}
 				visit(n);
 			}
 			
 			/**
 			 * Possible Parents: FieldDeclaration
 			 * Writes to Elements: No
 			 * 
 			 * @param n
 			 */
 			public void visitType(GNode n){
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents: Type
 			 * Writes to Elements: NoAlert("Program ended with balance of " + AccountBalance());
 			 * 
 			 * primitive type specific, ex int, double, float, etc.
 			 * @param n
 			 */
 			public void visitPrimitiveType(GNode n){
 				String returntype = ccHelper.convertType(n.getString(0));
 				//naming checks
 //				if(returntype.equals("boolean")){
 //					returntype = "bool";
 //				}
 //				else if(returntype.equals("int")){
 //					returntype = "int32_t";
 //				}
 //				else if(returntype.equals("void")){
 //					returntype = "Void";
 //				}
 				
 				if(operation.equals("dataLayout")){
 					dataLayout = dataLayout + returntype + " ";
 				}
 				else if(operation.equals("Constructor")){
 					dataLayout = dataLayout + returntype + " ";
 				}
 				else if(operation.equals("Method")){
 					currentclass.appendMethod("ReturnType", returntype);
 					currentclass.appendTableLayout("ReturnType", returntype);
 				}
 				else if(operation.equals("MethodParameter")){
 					currentclass.appendMethod("Parameters", returntype);
 					currentclass.appendTableLayout("Parameters", returntype);
 				}
 			}
 
 			/**
 			 * Possible Parents: Expression
 			 * Writes to Elements: No
 			 * 
 			 * the name of the variable
 			 * @param n
 			 */
 			public void visitPrimaryIdentifier(GNode n){
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents: PackageDeclaration, Type
 			 * Writes to Elements: No
 			 * 
 			 * 
 			 * @param n
 			 */
 			public void visitQualifiedIdentifier(GNode n){
 				String returnable = n.getString(0);
 //				if(returnable.equals("void")){
 //					returnable = "Void";
 //				}
 				
 				if(operation.equals("Extension")){
 					addSuperClass(returnable);
 				}
 				else if(operation.equals("dataLayout")){
 					dataLayout = dataLayout + returnable + " ";
 				}
 				else if(operation.equals("Constructor")){
 					dataLayout = dataLayout + returnable + " ";
 				}
 				else if(operation.equals("Method")){
 					currentclass.appendMethod("ReturnType", returnable);
 					currentclass.appendTableLayout("ReturnType", returnable);
 					//System.out.println("returntype " + returnable + " for " + currentclass.currentmethod.methodname);
 					//System.out.println("for node " + n.hashCode());
 				}
 				else if(operation.equals("MethodParameter")){
 					currentclass.appendMethod("Parameters", returnable);
 					currentclass.appendTableLayout("Parameters", returnable);
 					//System.out.println("parameter " + returnable + " for " + currentclass.currentmethod.methodname);
 					//System.out.println("for node " + n.hashCode());
 				}
 			}
 
 			public void visitBlock(GNode n){
 				//nothing for now, may have to add special cases for method call chains
 			}
 			
 			/**
 			 * Possible Parents: ClassDeclaration, FieldDeclaration, ConstructorDeclaration, MethodDeclaration
 			 * Writes to Elements: No
 			 * 
 			 * 
 			 * @param n
 			 */
 			public void visitModifiers(GNode n){
 				if(operation.equals("Constructor")){
 					
 				}
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents: Modifiers
 			 * Writes to Elements: No
 			 * 
 			 * public, private, protected, abstract, static, etc.
 			 * @param n
 			 */
 			public void visitModifier(GNode n){
 				String returnable = n.getString(0);
 				if(operation.equals("ClassDeclaration")){
 					currentclass.setModifier(returnable);
 				}
 				else if(operation.equals("dataLayout")){
 					//dataLayout = dataLayout + returnable + " ";
 					if(returnable.equals("final")){
 						dataLayout = dataLayout + "const ";
 					}
 				}
 				else if(operation.equals("Constructor") && returnable.equals("final")){
 					dataLayout = dataLayout + "const" + " ";
 				}
 				else if(operation.equals("Method")){ //&& 
 					//System.out.println("method for " + returnable);
 					currentclass.appendMethod("Modifier",returnable);
 //					if(returnable.equals("private")){
 //						currentclass.currentconstructor.setVisibility("private");
 //					}
 					
 				}
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents: FieldDeclaration
 			 * Writes to Elements: No
 			 * 
 			 * 
 			 * @param n
 			 */
 			public void visitDeclarators(GNode n){
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents: Declarators
 			 * Writes to Elements: methodCC, dataLayoutH
 			 * 
 			 * Large construct for declaring a variable
 			 * @param n
 			 */
 			public void visitDeclarator(GNode n){
 				String addable = n.getString(0); 
 				if(operation.equals("dataLayout")){
 					dataLayout = dataLayout + addable;
 //					if(!(n.getNode(2) == null)){
 //						dataLayout = dataLayout + "=";
 //					}
 				}
 				visit(n);
 				if(operation.equals("dataLayout")){
 					dataLayout = dataLayout + ";\r";
 					currentclass.addDataLayout(dataLayout);
 					//System.out.print("added datalayout " + dataLayout);
 				}
 			}
 
 			/**
 			 * Possible Parents: ClassDeclaration
 			 * Writes to Elements: No
 			 * 
 			 * @param n
 			 */
 			public void visitClassBody(GNode n){
 				operation = "dataLayout";
 				//System.out.println("Message: started on classbody " + currentclass.classname);
 				visit(n);
 			}
 
 			/**
 			 * Possible Parents: ClassBody
 			 * Writes to Elements: No
 			 * 
 			 * @param n
 			 */
 			public void visitConstructorDeclaration(GNode n){
 				operation = "Constructor";
 				dataLayout = "";
 				currentclass.newConstructor();
 				visit(n);
 				currentclass.addConstructor();
 				operation = "dataLayout";
 			}
 			
 			/**
 			 * Possible Parents: ConstructorDeclaration, MethodDeclaration
 			 * Writes to Elements: No
 			 * 
 			 * method arguments
 			 * @param n
 			 */
 			public void visitFormalParameters(GNode n){
 					if(!operation.equals("Constructor")){
 						operation = "MethodParameter";
 					}
 					
 					int i = 0;
 					for(Object o: n){
 						if (o instanceof Node){
 							dataLayout = "";
 							dispatch((Node)o);
 						}
 						i++;
 					}
 			}
 
 			/**
 			 * Possible Parents: FormalParameters
 			 * Writes to Elements: No
 			 * 
 			 * @param n
 			 */
 			public void visitFormalParameter(GNode n){
 //				for(Object o: n){
 //					if (o instanceof Node){
 //						dispatch((Node)o);
 //					}
 //				}			
 				dispatch(n.getNode(0)); dispatch(n.getNode(1));
 				dataLayout = dataLayout + n.getString(3);
 				if(operation.equals("Constructor")){
 					currentclass.appendConstructor(dataLayout);
 				}
 			}
 			
 			/**
 			* Possible Parents: ExpressionStatement, EqualityExpression, AdditiveExpression, Expression
 			* Writes to Elements: No
 			*
 			* @param n
 			*/
 			public void visitCallExpression(GNode n){
 				for(Object o: n){
 					if (o instanceof Node){
 						Node c = (Node)o;
 						if (c.hasName("SelectionExpression")){
 							dispatch((Node)o);
 						}
 					}
 				}			
 				visit(n);
 			}
 			
 			public void visitExtension(GNode n){
 				operation = "Extension";
 				visit(n);
 			}
 			
 			/**
 			 * Retreive the namespaces for the class
 			 * @param n
 			 */
 			public void visitPackageDeclaration(GNode n){
 				//System.out.println("package seen");
 				Node qualifier = n.getNode(1);
 				operation = "PackageDeclaration";
 				for(Object o : qualifier){
 					//System.out.println((String)o);
 					namespace.add((String)o);
 				}
 			}
 
 			/**
 			 * Possible Parents: CompilationUnit
 			 * Writes to Elements: forwardDeclarationsH, dataLayoutH
 			 * 
 			 * @param n
 			 */
 			public void visitClassDeclaration(GNode n){
 				if(classlist.contains(n.getString(1))){
 					//System.out.println("Message: already did this class");
 					return;
 				}
 				currentclass = new vTableClass(n.getString(1));
 				//System.out.println("Message: created new class" + currentclass.classname);
 				Iterator<String> iterate = namespace.iterator();
 				while(iterate.hasNext()){
 					String spacable = iterate.next();
 					currentclass.addNameSpace(spacable);
 					//System.out.println("Message: found namespace " + spacable);
 				}
 				
 				//missingsuper = false;  //set in compilationunit instead
 				
 				//if there is no explicit extension node, then inherits Object by default
 				if(n.getNode(3) == null){
 					addSuperClass("Object");
 					//System.out.println("Message: added Object inheritance");
 					//System.out.println("Message: missingsuper test " + missingsuper);
 				}
 				//System.out.println("classname is " + n.getString(1));
 				
 				currentclass.addForwardDeclaration(currentclass.classname);
 //				forwarddeclarations.addForwardDeclaration(currentclass.classname);
 //				forwarddeclarations.addForwardVTable(currentclass.classname);
 //				forwarddeclarations.addTypeDeclarations(currentclass.classname);
 				
 				operation = "ClassDeclaration";
 				
 				visit(n);
 				
 				if(missingsuper == true){
 					//System.out.println("Message: missing superclass");
 					return;
 				}
 				else{
 					//eventually add currentclass to the classlist and to the classes linked list
 				}
 				//System.out.println("missingsuper test = " + missingsuper);
 				
 				//currentclass.printLines();
 				classlist.add(currentclass.classname);
 				classes.add(currentclass);
 				fileprint.add(currentclass);
 				
 				File file = new File(hostPath + "/" + currentclass.classname + ".h");
 				try {
 					file.createNewFile();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				printClass(file, currentclass);
 			}
 
 			public void visitArguments(GNode n){
 				visit(n);
 			}
 			
 			public void visitMethodDeclaration(GNode n){
 				operation = "Method";
 				if(!(n.getString(3).equals(("main")))){
 					currentclass.newMethodLayout();
 					currentclass.appendMethod("MethodName", n.getString(3));
 					currentclass.appendMethod("ReferenceType", currentclass.classname);
 					currentclass.newTableAddress();
 					currentclass.appendAddress("MethodName", n.getString(3));
 					currentclass.appendAddress("ClassName", currentclass.classname);
 					currentclass.newTableLayout();
 					currentclass.appendTableLayout("MethodName", n.getString(3));
 					currentclass.appendTableLayout("ReferenceType", currentclass.classname);
 					visit(n);
 					
 					currentclass.addMethod();
 					boolean hasStaticPrivate = currentclass.checkStaticPrivate();
 					if(hasStaticPrivate == false){
 						//currentclass.addMethod();
 						currentclass.addTableAddress();
 						currentclass.addTableLayout();
 					}
 				}
 			}
 			
 			public void addSuperClass(String s){
 				//System.out.println("currentclass " + currentclass.classname);
 				if(classlist.contains(s)){
 					//System.out.println("classlist contains " + s);
 					Iterator iterate = classes.iterator();
 					while(iterate.hasNext()){
 						vTableClass classy = (vTableClass) iterate.next();
 						if(classy.classname.equals(s)){
 							//System.out.println("Found desired superclass " + classy.classname);
 							currentclass.addSuperClass(classy);
 							break;
 						}
 					}
 				}
 				//add to the queue
 				else{
 					//System.out.println("classlist does not contains " + s);
 					waitqueue.add(currentsource);
 					missingsuper = true;
 				}
 			}
 			
 			/**
 			 * 
 			 * @param n
 			 */
 			public void visit(Node n) {
 				//System.out.println("test");
 				for (Object o : n)
 					if(missingsuper == true){
 						return;
 					}
 					else if (o instanceof Node){ 
 						dispatch((Node)o);
 					}
 			}
 		}.dispatch(sourceAST);
 		
 		//printFile(file);
 	}
 }	
