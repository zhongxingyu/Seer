 package xtc.oop;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import java.util.ArrayList;
 
 /**
  * Does all the smart Translating, visits every node inside a method's block, 
  Edits Nodes for System.out.print, Method Calls, Casts and Super
  Uses Inheritance Builder to find the right method name for the method call (to support overriding methods)
  */ 
 public class EWalk //extends Visitor
 {
 	boolean VERBOSE = false; //debugging boolean
 	private InheritanceTree tree; //the given inheritanceTree Object passed in the constructor
 	private Declaration method; //the given Declaration Object passed in the constructor
 	//private boolean isInstance; //check for callExpression (needed for chaining) checks if there is a receiver (b.someMethod())
 	private boolean isMethodChaining;//check if methodChaining is enacted (Starts if a CallExpression is the first child of another CallExpression)
 	private String savedReturnType; //saves the return type of a method for method chaining (starting at the bottom CallExpression
 		/**Constructor that takes an InheritanceTree Object, a Declaration Object, a GNode*/
 	String[] PrimTypes = {"String", "boolean", "double", "float", "long", "int", "short"};
 	
 	/**Consructor that takes inheritance tree, Declaration method and Block GNode*/
 	public EWalk (final InheritanceTree treeClass, final Declaration treeMethod, GNode n) {
 		if(VERBOSE) System.out.println("EWalk Called....");
 		tree=treeClass;
 		method=treeMethod;
 		eWalker(n); //walk before you run...
 		eRunner(n); //Does all clean up work
 	}
 	
 	/**Handles, System.out.print, 
 	   *Arrays
 	   *Super
 	   *Casts
 	   *Method Calling/Chaining
 	*/
 	private void eWalker (final GNode n) { 
 		Node node = n;
 		new Visitor() {
 			/**Some Subtree Flags to check the current condition of the Subtree*/
 			boolean inCall = false,
 				isPrint = false,
 				isPrintln = false,
 				isArray = false,
 				isPrintString = false,	
 				isString = false,
 				isInstance=false,
 				isArgument=false,
 				isSuper;//flag that saves whether super is inside a call expression
 
 			/**StringBuffer and Arraylist to store the 
 			 *FullQualified Name i.e. java.lang etc */
 			StringBuffer fcName= new StringBuffer();
 			StringBuffer boundsChecks = new StringBuffer();
 			StringBuffer chainGang = new StringBuffer();
 			ArrayList<String> fcNameList=new ArrayList<String>();
 			
 			public String visitExpression(GNode n) {
 				if(VERBOSE)System.out.println(n.getName());
 				if (!n.getNode(0).getName().toString().equals("SubscriptExpression")) {
 					String instanceName = n.getNode(0).getString(0);//gets the primary ID
 						Node castex = n.getNode(2);//get the third node
 						if(castex.getName().equals("CastExpression")) {//see if its a castexpression
 							n.set(2,visitCastExpression(castex,instanceName));
 						}
 				} else {
 				}
 				visit(n);				
 				return null;
 			}
 			/*
 			public void visitSubscriptExpression (GNode n) {
 			if (n.getNode(0)!=null) {
 					if (n.getNode(0).getName().equals("SubscriptExpression")) {//should only happen if somebody tries a multi-dimensional array
 						System.out.println("Multidimensional arrays not allowed!");
 						System.exit(1);
 					}
 				}
 				GNode output = n;
 				output = output.ensureVariable(n);
 				String type = ""; // the data type of the array HELP - not neccessry now that operator[] is overloaded
 				String bounds=("__ArrayOf"+type+"::checkIndex("+output.getNode(0).getString(0)+","+output.getNode(1).getString(0)+");\n");
 				output.add(1,"->__data[");
 				output.add("]");
 				visit(output);
 				n.set(0,output);
 				n.set(1,""); //clear the old node;
 				boundsChecks.append(bounds+"\n");
 			}
 			*/
 			public void visitNewArrayExpression (GNode n) {
 				if(VERBOSE) System.out.println("Entering newArrayExpression");
 				GNode output = (GNode)n.getNode(1);
 				output = output.ensureVariable(output);
 				n.set(0,"new __Array<"+n.getNode(0).get(0).toString()+">(");
 				visit(output);
 				output.add(")");
 				n.set(1,output);
 			}
 			public void visitDimensions (GNode n) {
 				if (n.get(1)!=null) {
 					System.out.println("Multidimensional arrays not allowed!");
 					System.exit(1);
 				}
 				n.set(0,"");
 				visit(n);
 			}
 			/**visit the declarator and update the type */
 			public void visitDeclarator(GNode n)
 			{
 				String instanceName = n.getString(0);//gets the primary ID
 				Node castex = n.getNode(2);//get the third node
 				if(castex.getName().equals("CastExpression")) {//see if its a castexpression ?seems like cast expresions could appear in more places than this
 					n.set(2,visitCastExpression(castex,instanceName));
 				}
 				visit(n);
 			}
 			/**NOTE: This is not a VISIT method but my own created method*/
 			public Node visitCastExpression(Node n, String targetVariableName) {
 				String targetType = n.getNode(0).getNode(0).getString(0); //target type
 				String sourceVariable = n.getNode(1).getString(0);
 				Node targetTypeNode;
 				targetTypeNode = GNode.create("PrimaryIdentifier",sourceVariable); //node to send to send to getType();
 				String type = getType(targetTypeNode); //source type
				if(VERBOSE)System.out.println("Casting variable "+sourceVariable+" to type "+type+" and assigning to variable "+targetVariableName+" of type "+type);
 				if(!type.equals("int")&&
 				   !type.equals("double")&&
 				   !type.equals("long")&&
 				   !type.equals("char")&&
 				   !type.equals("bool")&&
 				   !type.equals("float")) {
 					//special cast shoudl use java_cast()
 					Node replacement;
 					replacement = GNode.create("java_castExpression","({__rt::java_cast<"+type+","+targetType+">("+sourceVariable+") })");
 					if(VERBOSE)System.out.println("Generated casting expression: "+replacement.getString(0));
 					n=replacement;
 					//fcNameList is a global ArrayList<String>
 				} else { }
 				method.update_type(targetVariableName,fcName.toString(),type);
 				return n;
 				
 			}
 
 
 			/*returns true if a node has the name "CallExpression" */
 			public boolean isCallExpression(Node n)
 			{
 				if(n!=null)
 					{
 						if(n.getName().equals("CallExpression")) {
 							return true;
 						}
 				  
 					}
 				return false;
 			}
 			/**Visit a CallExpression (Methods) i.e. m1() and 
 			 *  call the necessary inheritance checks on the InheritanceTree
 			 *  should have a check for superExpression*/
 			public void visitCallExpression (GNode n) {
 				//reset the full qualified name global variables
 				fcName= new StringBuffer();
 				fcNameList=new ArrayList<String>();
 				
 				inCall = true; //start looking for fully qualified name
 				
 				//Get the First child and check to see if its null
 				// if it isn't null run a check for SuperExpression and Primary Identifier
 				// Set the respective flags to be used later*/
 				Object first = n.get(0);
 				if(first!=null) {
 					Node firstc= (Node) first;
 					if(firstc.getName().equals("SuperExpression")) {
 						isSuper=true;
 					}
 					if (firstc.getName().equals("PrimaryIdentifier")) {
 							isInstance=true;
 					}
 					dispatch(firstc); //dispatch on the node
 				}
 				
 				/*create a string array to store the return type and newMethod name of the 
 				return method*/
 				String[] methodArray=setMethodInfo(n);
 				
 				//new method name to override in the tree
 				String newMethod= methodArray[1];
 				savedReturnType = methodArray[0];
 				if(VERBOSE)System.out.println("THE RETURN TYPE" +savedReturnType );
 				//replace the AST methodName with the given name
 				if(newMethod!=null)
 					n.set(2,newMethod);
 				
 				//reset flags
 				if(isMethodChaining) {
 					//set args of this call expression to the chainGang
 				//	n.getNode(3).add(0,newMethod);
                                         System.out.println("fix method chaining");
 					//n.getNode(3).add(0,newMethod);
 					if(VERBOSE)System.out.println("Added "+newMethod+" to arguments");
 				}
 				else
 				{
 					isInstance=false;
 				}
 				isSuper=false;
 				isMethodChaining=false;
 			}
 			/**Returns Tree if a Node has the Name "PrimaryIdentifier"*/
 			public boolean isPrimaryIdentifier(Node n)
 			{
 				if(n.getName().equals("PrimaryIdentifier"))
 						return true;
 				else
 						return false;
 			}
 			/**Checks MethodChainingFlag, Sets Flag if first Child of CallExpression is another
 			 Call Expression (b.m1().m2()*/
 			public String[] setMethodInfo(Node n)
 			{
 				//get the currentName of the method (Java Syntax)
 				String currentName= n.getString(2);
 				String primaryIdentifier=" ";
 				Node firstChild=n.getNode(0);
 				/**If methodChaining Flag is set save a the bottom methods return type to use 
 				 in Search_for_method later on up the tree*/
 				if(isCallExpression(firstChild))
 				{
 					if(VERBOSE)System.out.println("FIRST_CHILD");
 					isMethodChaining=true;
 					//dispatch(firstChild);
 				}
 				if(isMethodChaining)
 					{
 						//store the method return type for later use
 						if(VERBOSE)System.out.println("METHOD CHAINING");
 						primaryIdentifier=savedReturnType;
 						isInstance=true;
 					}
 				
 				else if(firstChild!=null)
 					{
 						//check to see if its primaryidentifier
 						if((isPrimaryIdentifier(firstChild)))
 							{
 								primaryIdentifier=firstChild.getString(0);
 							}
 						//else dispatch on the firstchild
 						else 
 							{
 								dispatch(firstChild);
 							}
 					}
 				//run a check for System.out.print
 				checkPrint(n);
 				/**Visit the Arguments and get their types, after wards run a check
 				 on the given types and store each type in an ArrayList<String>
 				 user them in GetMethod Info to get the right details
 				 to find the right method for overloading*/
 				Node arguments=n.getNode(3);
 				ArrayList<String> argumentTypes =getArgumentTypes(arguments);
 				//get the method name
 				String[] methodArray= new String[2];
 				if(VERBOSE)System.out.println(n.toString());
 				String methodName = n.getString(2);
 				//run checks for system.out.println and break from get method info Otherwise will crash
 				if(methodName.contains("std::cout<<")) 
 				{
 					//isPrintln=true;
 					return methodArray;
 				}
 				
 				/*if(isInstance)//check to see if the method call has a reciever
 				{
 					//Node  =new GNode();
 					//add the primary identifier to the Arguments Node
 					Node primary = n.getNode(0);
 					//get the arguments node (located at position 3)
 					Node argum = n.getNode(3);
 					//System.out.println(argum.MAX_FIXED + " " +argum.getName());
 					GNode gArgum=(GNode)argum;
 					GNode argue= gArgum.ensureVariable(gArgum);
 					//add the primary node to the first position in the arguments node
 					gArgum.add(0,primary);
 				}*/
 				if(VERBOSE){System.out.println("getting Method Info:" +primaryIdentifier+ ", " + methodName);}
 				//get an array of the method arrtibutes in the inheritance tree (return type and new method name)
 				methodArray = getMethodInfo(n,primaryIdentifier,fcNameList, methodName,argumentTypes);
 				return methodArray;
 			}
 			/**Helper method returns the types ofarguments in an array list
 			 Usefule for getting right methods for method overloading*/
 			public ArrayList<String> getArgumentTypes(Node n)
 			{
 				/**
 				 Set the argument Flag then for each Node in the argument Tree call
 				 the getType Method which will return the type for each value 
 				 getType should support Expressions, Identifiers and MethodCalls
 				 */
 				isArgument=true;
 				if(VERBOSE)System.out.println("getArgumentTypes 1");
 				ArrayList<String> argumentList = new ArrayList<String>();
 				if(VERBOSE)System.out.println("getArgumentTypes 2");
 				for(int i=0;i<n.size();i++)	{
 					argumentList.add(getType(n.getNode(i)));
 				}
 				if(VERBOSE)System.out.println("getArgumentTypes3");
 				isArgument=false;
 				return argumentList;
 			}
 			
 			/**Helper Method that checks for the System.out.print Special Case.
 			 It checks if FCName is System.out and then checks if the Method Name is Println or Print 
 			 and then updates the Node to C++ standard print Calls*/
 			public void checkPrint(Node n)
 			{
 				String methodName= n.getString(2);
 				
 				if(VERBOSE){System.out.println("FULLy Qualified Name:" +fcName.toString());}
 				/**Check for the System.out in the FCName and then check the method names 
 				for calls to print or println */
 				if (fcName.toString().contains("System->out->")) {
 					fcName = new StringBuffer(); 
 					fcNameList=new ArrayList<String>();
 					fcName.append("std::cout<<");
 
 					if(methodName.equals("print")){
 						n.set(0,null);
 						n.set(1,"");
 						n.set(2,fcName.toString());
 						
 						isPrint = true;
 					}
 					   else if (methodName.equals("println")){
 						   //append endl to keep the newLine behavior of System.out.println
 						   n.set(0, null);
 						   n.set(2,fcName.toString());
 						   n.set(1,"<<std::endl");
 						   isPrint=true;
 						isPrintln=true;
 					}
 					fcName = new StringBuffer(); 
 					fcNameList=new ArrayList<String>();
 				
 						if(VERBOSE) {
 						System.out.println("Translating special case:\t\tSystem.out.print");
 						if (isPrintln) System.out.print("ln");
 						System.out.println("");
 					}
 				}
 				else {
 					//other
 				}
 				//clear the FCName buffer 
 				fcName= new StringBuffer();
 				fcNameList=new ArrayList<String>();
 			}
 			/** Grabs full qualified names and appends them to the global fcName String Buffer
 			 i.e. java.lang ==> java->Lang-> */
 			public void visitSelectionExpression (GNode n) {
 				visit(n);	
 				if (inCall);
 				fcName.append(n.getString(1)+"->");
 				fcNameList.add(n.getString(1));
 			}
 			/**End value of a static variable or just a local variable
 			*/
 			public void visitPrimaryIdentifier (GNode n) {
 				if (inCall) {
 					inCall = false;
 					fcName.append(n.getString(0)+"->");
 					fcNameList.add(n.getString(0));
 				} else {
 					//Do something?
 				}
 				visit(n);
 			}
 			/**Visists additve expression and replaces + with <<
 			 Also This probably won't be visited either but I doubt it can cause harm with these
 			 Checks
 			 Expressions are handled in GetType*/
 			public void visitAdditiveExpression (GNode n) {
 				//should check the 0 and 2 children if they are stingliterals and if so, Encapsulat!
 
 				/*({ std::ostringstream sout;
 				  sout << n.getNode(0) << n.getNode(2);
 				  sout.str(); })*/
 
 				visit(n);
 				if(n.get(0) instanceof Node) {
 					if(getType(n.getNode(0)).equals("String")||getType(n.getNode(2)).equals("String")) {
 						// if either 0 or 2 is returns a string: HELP
 						isString = true;
 						Node side1;
 						Node side2;
 						side1 = GNode.create("StringLiteral",n.getNode(0));
 						side2 = GNode.create("StringLiteral",n.getNode(2));
 						GNode sideA = (GNode)n.getNode(0);
 						GNode sideB = (GNode)n.getNode(2);
 						sideA = sideA.ensureVariable((GNode)side1);
 						sideB = sideB.ensureVariable((GNode)side2);
 						sideA.add(0,"({ std::ostringstream sout;\nsout <<");
 						sideB.add(";\nsout.str(); })");
 						n.set(0,sideA); n.set(1,"<<"); n.set(2,sideB);
 					}
 				}
 			}
 			
 			/**Gets back information inside identifier calls inheritencetree to update the method type  Also has support for an rray*/
 			public void visitFieldDeclaration (GNode n) {
 				/*get the package information(Inside Qualified Identifier) 
 				 Get the Type = second child of the SubTree 
 				 Remove the last value from the currentPackages Arraylist (its the new type to update too
 				 */
 				String[] currentPackage= new String[]{"",""};
 				Object o = n.get(1);
 				if(o!=null)
 					{
 						currentPackage=getPackage((Node)o);
 					}
 				//remove the last Value (its the object name.)
 				String newtype = currentPackage[1];
 				
 				String name="";
 				//get the name Located under declarator node
 				Object declarators = n.get(2);
 				if(declarators!=null)
 					{
 						Node declarNode=(Node)declarators;
 						Object declarator=declarNode.get(0);
 						if(declarator!=null)
 							{
 								Node declaratorNode = (Node)declarator;
 								name = declaratorNode.getString(0);
 							}
 					}
 				
 				//update the type of the variable in the Declarator
 				if (VERBOSE) System.out.println("Updating Type Information("+name +"," +newtype+")");
 
 				method.update_type(name,currentPackage[0], newtype);
 				if (n.getNode(1).getNode(1) !=null ) {
 				if (n.getNode(1).getNode(1).getName().equals("Dimensions")) {
 					if(VERBOSE) System.out.println("Entering array field declaration...");
 					n.getNode(1).getNode(0).set(0,"ArrayOf"+visitPrimitiveType((GNode)n.getNode(1).getNode(0)));
 				}
 				}
 				visit(n);
 			}
 			public String visitPrimitiveType (GNode n) {
 				if (n.getString(0).equals("int")) return "Int";
 				return n.getString(0);
 			}
 			/**Helper method that gets an array list of a pack when given  node assuming qualifiedId or Prim*/
 			
 			/**Helper method that gets an array list of a package when given 
 			 node assuming it is a qualifiedId or PrimimitveType*/
 			public String[] getPackage(Node n)
 			{
 				String packages="";
 				String name="";
 				//get every child of the given node (either PrimirtiveType or QualifiedIdentifier)
 				Node node = n.getNode(0);
 				name = node.getString(node.size()-1);
 				for(int i=0; i<node.size()-1; i++){
 					//for every child in QualifiedIdentifier append it to the array list
 					if(i==0)packages =node.getString(i);
 					else packages+="."+node.getString(i);
 					
 				}
 				return new String[]{packages,name};
 			}
 			/**helper method that uses the inheritence tree search for method and 
 			   returns the givne string array that should contain the return type and c++ Standard methodname 
 			   puts in check for isSuper flag and isInstance Flag*/
 			public String[] getMethodInfo(Node n,String Identifier,ArrayList<String> nameList,String name, ArrayList<String> argumentList)
 			{
 				if (VERBOSE) System.out.println("\t\t Method Chaining?"+isMethodChaining+"getMethodInfo");
 				//method .search for type with packages if you dont send a package its the package you're in
 				InheritanceTree b; //will be current Class, the superclass or the instance's class
 				if(isInstance && !isMethodChaining)
 					{
 						String[] qualities=method.search_for_type(Identifier);//send the primary Identifier
 						if (VERBOSE)System.out.println("Method.Search_for_type:" + Identifier);
 						
 						//remove the last value from the arrayList (thats always the class name
 						String className =(String)qualities[1];
 						if(VERBOSE)System.out.println("isInstance:tree.root.search(" +qualities +","+className+")");
 						
 						//set the inheritance tree based on the found class in the package
                                                 String FullName = (qualities[0].equals("") ? "" : qualities[0]+".")+qualities[1];
 						System.out.println(FullName);
 						b =tree.root.search(FullName);
                                                 System.out.println(FullName);
 						if(VERBOSE){System.out.println("On Instance:"+ isInstance+"," + method +","+argumentList+","+name);}
 					}
 				else if (isMethodChaining)
 				{
 					String packages = "";
 					String FullName = "";
 					//currently not supporting classes outside of the current methdo
 					if(VERBOSE)System.out.print("Is MEthod Chaining:" +packages +"," + Identifier);
 					if(!packages.equals(""))FullName = packages+"."+Identifier;
 					else FullName =Identifier;
                                         
 					b=tree.root.search(FullName);
 					//what do i do to get the full package name?
 				}
 				else if (isSuper) 
 					{
 						b = tree.superclass;
 					}
 				else {
 					if(VERBOSE){System.out.println("Running"+ isInstance+"," + method +", "+argumentList+","+name);}
 					b=tree; //set be = to the current tree
 				}
 				if (b==null) {
 				System.out.println("null------------------------!\n");
 				System.exit(2);
 				}
 				//returns an array of string 0= return type and 1=new method string
 				if(b.search_for_method(isInstance,argumentList,name)==null){
 					System.out.println("No info found from search_for_method");
 					System.exit(1);
 				}
 				return b.search_for_method(isInstance,argumentList,name);
 			}
 			/**Helper method that checks for the types in the subtree and returns them 
 			   is currently used when get the types for values in an argument
 			 Works for Expressions (Highest Value(String Precedent)), Primitive Types, 
 			 Call Expressions (Return Type) and Primary Identifiers(Class Name)*/
 			public String getType(Node n)
 			{
 				//check for primitative types
 				if (n.getName().equals("IntegerLiteral")) {
 					return "int";
 				}
 				else if(n.getName().equals("StringLiteral")){
 					return "String";
 				}
 				else if(n.getName().equals("BooleanLiteral"))
 					{
 						return "boolean";
 					}
 				else if(n.getName().equals("NullLiteral"))
 					{
 						return "null";
 					}
 				else if(n.getName().equals("FloatingPointLiteral"))
 					{
 						return "float";
 					}
 				else if(n.getName().equals("CharLiteral"))
 					{
 						return "char";
 					}
 				//check for call expressions' return type
 				else if (n.getName().equals("CallExpression"))
 				{
 					String[] methodArray;
 					//return the return type gotten from getMethodInfo (located as the first item in the given array)
 					methodArray=setMethodInfo(n);
 					return methodArray[0];
 				}
 				//get the primary identifiers class Name
 				else if(n.getName().equals("PrimaryIdentifier")){ //return the name of the primaryIdentifier
 					//call the method in the inheritence tree to get the type of the primaryIde
 					String type = " ";
 					if(!isPrint){
 						String[] packageNType= method.search_for_type(n.getString(0));
 						type = packageNType[1];
 					}
 					return type;
 				}
 				//else get the expression type
 				else {
 					return getExpressionType(n);
 				}
 			}
 			/**Helper method returns the type of expression by getting the type of every value in the list
 			 and then returning the highest precent value (currently no support of Object or other classes */
 			public String getExpressionType(Node n)
 			{
 				int sizeOf = n.size();
 				//create an array the size of n's children +1
 				String[] exTypes = new String[sizeOf+1];
 				boolean[] typeOn = new boolean[7];
 				//visit each of the expression nodes chidren and get their type (if Node)
 				for( int i = 0; i<sizeOf; i++)
 				{
 					//check to make sure child is a node
 					Object child = n.get(i);
 					if(child !=null)
 					{
 						if(child instanceof Node ) 
 						{
 							String type = getType((Node)child); 
 							//get the type of the value in the expression
 							
 							if(type!=null)//make sure type isn't null
 							{
 								exTypes[i]=type;
 								for(int k =0; k<PrimTypes.length;k++)
 								{
 									if(type.equals(PrimTypes[k]))//check to see if type is set on
 									{
 										typeOn[k]=true;
 									}
 								}
 							}	
 						}
 					}
 				}//end of for loop
 				//after visiting each child and getting the type find 
 				//the largest precent type set and return that type
 				for(int l= 0; l<=PrimTypes.length; l++)
 				{
 						if (typeOn[l]) {//if that type is on return it
 							checkAdditiveExpression(n, PrimTypes[l]);
 							return PrimTypes[l];
 						}
 				}
 				
 				return "ERROR";
 			}
 			/**Check for Additive Expression Special Case,
 			 if its a print string then use << instead of addition*/
 			public void checkAdditiveExpression(Node n, String type)
 			{
 				if(n.getName().equals("AdditiveExpression"))
 				{
 					if(isPrint) {
 						if(type.equals("String"))
 						{
 							n.set(1,"<<");
 						}
 					}
 				}
 			}
 			public void visitStringLiteral (GNode n) {
 				if (isString) {
 					String temp = n.getString(0);
 					n.set(0,"__rt::stringify("+temp+")");
 					isString = false; //make sure it only happens once
 				}
 				//				if (isPrint) isPrintString = true;
 				visit(n);
 			}
 			
 			/**Default Visit Method*/
 			public void visit(Node n) {
 				if(VERBOSE)System.out.println(n.getName());
 				if(n!=null){
 					for (Object o : n){ 
 						if (o instanceof Node){ 
 							dispatch((Node)o);
 						
 						}
 					}
 				}//make sure n is not null If this happens if probably an error call from 
 				else { //something is wrong
 					System.out.println("ERROR: EWALK WAS SENT NULL");
 				}
 
 			}
 		}.dispatch(node); 
 	}//end of eWalker Method
 	
 	/**Follows eWalker. Handles: Types, Modifiers
 
 	 */
 	public void eRunner(final GNode n)
 	{
 		Node node = n;
 		
 		new Visitor(){
 			
 			public void visitPrimitiveType (GNode n) {
 				String type = n.getString(0);
 				if (type.equals("int")) {
 					n.set(0,"int32_t");
 					if (VERBOSE) System.out.println("changing int to int32_t");
 				} 
 				if (type.equals("boolean")) {
 					n.set(0,"bool");
 					if (VERBOSE) System.out.println("changing boolean to bool");
 				}
 				/*if (type.equals("char")) {
 					n.set(0,"int16_t");
 					if (VERBOSE) System.out.println("changing char to int16_t");
 				}*/
 				if (type.equals("long")) {
 					n.set(0,"int64_t");
 					if (VERBOSE) System.out.println("changing long and int64_t");
 				}
 				if (type.equals("short")) {
 					n.set(0,"int16_t");
 					if (VERBOSE) System.out.println("changing short to int16_t");
 				}
 				if (type.equals("byte")) {
 					n.set(0,"int8_t");
 					if (VERBOSE) System.out.println("changing byte to int8_t");
 				}
 
 				visit(n);
 			}
 			public void visitModifier (GNode n) {
 				String temp = n.getString(0);
 				if (temp.equals("final")) n.set(0,"const");
 			}			
 			public void visit(Node n) {
 				if(n!=null){
 					for (Object o : n){ 
 						if (o instanceof Node){ 
 							dispatch((Node)o);
 							
 						}
 					}
 				}//make sure n is not null
 			}		
 		}.dispatch(node);
 	}
 }
