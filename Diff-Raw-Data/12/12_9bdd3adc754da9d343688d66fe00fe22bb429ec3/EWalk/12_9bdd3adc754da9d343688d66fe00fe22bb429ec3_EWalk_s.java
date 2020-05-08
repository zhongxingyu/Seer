 package xtc.oop;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import java.util.ArrayList;
 import xtc.oop.ArrayMaker;
 
 /**
  * Does all the smart Translating, visits every node inside a method's block
  */ 
 public class EWalk //extends Visitor
 {
 	boolean VERBOSE = true; //debugging boolean
 	private InheritanceTree tree; //the given inheritanceTree Object passed in the constructor
 	private Declaration method; //the given Declaration Object passed in the constructor
 	private boolean isInstance; //check for callExpression (needed for chaining) checks if there is a receiver (b.someMethod())
 	private boolean isMethodChaining;//check if methodChaining is enacted (Starts if a CallExpression is the first child of another CallExpression)
 	private String savedReturnType; //saves the return type of a method for method chaining (starting at the bottom CallExpression
 		/**Constructor that takes an InheritanceTree Object, a Declaration Object, a GNode*/
 	String[] PrimTypes = {"String", "boolean", "double", "float", "long", "int", "short"};
 	public EWalk (final InheritanceTree treeClass, final Declaration treeMethod, GNode n) {
 		if(VERBOSE) System.out.println("EWalk Called....");
 		tree=treeClass;
 		method=treeMethod;
 		eWalker(n); //walk before you run...
 		eRunner(n);
 	}
 	
 	/**Handles, System.out.print, 
 	   Arrays
 	   Super
 	   Casts
 	   Method Calling/Chaining
 	*/
 	private void eWalker (final GNode n) { 
 		Node node = n;
 		new Visitor() {
 			boolean inCall = false,
 				isPrint = false,
 				isPrintln = false,
 				isArray = false,
 				isPrintString = false,	
 				isString = false,
 				isSuper;//flag that saves whether super is inside a call expression
 
 			StringBuffer fcName= new StringBuffer();
 			ArrayList<String> fcNameList=new ArrayList<String>();
 			
 			public void visitExpression(GNode n)
 			{
 				Node primary = n.getNode(0);
 				String instanceName=primary.getString(0);
 				Node castex = n.getNode(2);//get the third node
 				if(castex.getName().equals("CastExpression"))//see if its a castexpression
 					{
 						visitCastExpression(castex,instanceName);
 					}
 			}
 			/**visit the declarator and update the type */
 			public void visitDeclarator(GNode n)
 			{
 				
 			}
 			/**NOTE: This is not a VISIT method but my own created method*/
 			public void visitCastExpression(Node n, String name) {
 				//get and variable and the new types
 				Node qual = n.getNode(0);
 				String type =qual.getString(0);
 				
 				ArrayList<String> packages=getFcName(n);
 				//call the intheritiecne tree method string, arraylist of a new packages and type name
 				method.update_type(name,packages,type);
 			}
 			/*Checks to see if the given node has the name "Call Expression */
 			public boolean isCallExpression(Node n)
 			{
 				if(n!=null)
 					{
 						if(VERBOSE) System.out.println("IS CALL? "+n.getName());
 						if(n.getName().equals("CallExpression")) {
 							return true;
 						}
 				  
 					}
 				return false;
 			}
 			/**Visit a Call expression and call the necessary inheritence checks
 			   should have a check for superExpression*/
 			public void visitCallExpression (GNode n) {
 				//reset print values here
 				isPrint=false;
 				isPrintln=false;
 				fcName= new StringBuffer();
 				fcNameList=new ArrayList<String>();
 				if(VERBOSE) System.out.println("\nVisiting a Call Expression node:");
 				inCall = true; //start looking for fully qualified name
 				
 				//get the first child
 				Object first = n.get(0);
 				//check to see if its null
 				if(first!=null) {
 					//if it isn't null check to see if its primary or super
 					Node firstc= (Node) first;
 					if(firstc.getName().equals("SuperExpression")) {
 						//if its super set the is super flag true
 						isSuper=true;
 					}
 					if(VERBOSE) System.out.println("NAME:" +firstc.getName());
 					
 					dispatch(firstc); //visit the node
 
 				}
 				
 				String[] methodArray=setMethodInfo(n);
 				//new method name to override in the tree
 				String newMethod= methodArray[1];
 				savedReturnType = methodArray[0];
 				if(VERBOSE) System.out.println("NEW METHOD" + newMethod);
 				//code to replace the old method name with the new method name in the tree	
 				//where the current method name is current located as position 2
 				if(newMethod!=null)
 					n.set(2,newMethod);
 				visit(n);
 				//reset print values here
 				isPrint=false;
 				isPrintln=false;
 				
 								
 				
 			}
 			/**checks node name to see if the name is primaryIdentifier*/
 			public boolean isPrimaryIdentifier(Node n)
 			{
 				if(n.getName().equals("PrimaryIdentifier"))
 					{
 						return true;
 					}
 				else
 					{
 						return false;
 					}
 				   
 			}
 			/**does all of Call Expressions Heavy Lifting and returns the newmethod arraylist*/
 			public String[] setMethodInfo(Node n)
 			{
 				//gets the FCNameList Node and visits it using the getFCName method
 				ArrayList<String> fcNamelist = getFcName(n);
 				
 				String primaryIdentifier=" ";
 				Node firstChild=n.getNode(0);
 				if(isMethodChaining)
 					{
 						//store the method return type
 						primaryIdentifier=savedReturnType;
 					}
 				if(isCallExpression(firstChild))
 					{
 						isMethodChaining=true;
 						dispatch(firstChild);
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
 				
 				//visit the arguments node
 				Node arguments=n.getNode(3);
 				//create a new argumentTypes arraylist call the getarguments method on the arguments node
 				ArrayList<String> argumentTypes =getArgumentTypes(arguments);
 				if(VERBOSE) System.out.println("New Array List Created...\n");
 				
 				//get the method name
 				String[] methodArray= new String[2];
 				String methodName = n.getString(2);
 				//run checks for system.out.println and break from get method info
 				if(VERBOSE){System.out.print("isPrint???: " +isPrint);}
 				if(methodName.contains("std::cout<<"))  
 				{
 					//isPrintln=true;
 					return methodArray;
 				}
 				/*else if(methodName.contains("System->out->print")) {
 					//isPrint=true;
 					return methodArray;
 				}*/
 
 				if(VERBOSE){System.out.println("getting Method Info:" +primaryIdentifier+ ", " + methodName);}
 				//run a check for System.out.print and println
 				System.out.println(isPrintln);
 				//get an array of the method arrtibutes in the inheritance tree (return type and new method name)
 				methodArray = getMethodInfo(n,primaryIdentifier,fcNameList, methodName,argumentTypes);
 				return methodArray;
 			}
 			/**Helper method returns the arguments in an array list*/
 			public ArrayList<String> getArgumentTypes(Node n)
 			{
 				System.out.println("1hi---------------------------------");
 				//get the type for every argument in the argument Node
 				ArrayList<String> argumentList = new ArrayList<String>();
 								System.out.println("1hi---------------------------------");
 
 				for(int i=0;i<n.size();i++)	{
 					argumentList.add(getType(n.getNode(i)));
 				} //what about a method call here?
 								System.out.println("1hi---------------------------------");
 
 				/*if the argumentlist is empty return a arraylist of the string 0 */
				if (argumentList.isEmpty()) {
					argumentList.add("0");
				}
 								System.out.println("1hi---------------------------------");
 
 				return argumentList;
 				
 			}
 			/**Node that gets the FC name before a method and returns an array list of the fcNamelist*/
 			public ArrayList<String> getFcName(Node n){
 				
 				//visit(n);
 				fcName.append(n.getString(2));
 				fcNameList.add(n.getString(2)); 
 				int size = fcNameList.size();
 				
 				String methodName = fcNameList.get(size-1);
 				//String className = fcNameList.get(size-2);//SOURCE OF ARRAY OUT OF BOUNDS
 				fcNameList.remove(size-1);
 				//fcNameList.remove(size-2);//SOURCE OF ARRAY OUT OF BOUNDS
 				if(VERBOSE){System.out.println("FULLNAME To String" +fcName.toString());}
 				if (fcName.toString().contains("System->out->print")) {
 					isPrint = true;
 					if (fcName.toString().contains("System->out->println")) {
 						isPrintln = true;
 					}
 					fcName = new StringBuffer(); //clear the fcName
 					fcName.append("std::cout<<");
 					if(VERBOSE) {
 						System.out.println("Translating special case:\t\tSystem.out.print");
 						if (isPrintln) System.out.print("ln");
 						System.out.println("");
 					}
 				}
 				else {
 					//other
 				}
 				if(VERBOSE) System.out.println("Size of n is\t\t\t\t"+n.size());
 				//if(VERBOSE) System.out.println("Setting n[0] to:\t\t\t"+fcName);
 				if(VERBOSE) System.out.println("Println?" +isPrintln);
 				if(isPrintln) {
 					if(VERBOSE) System.out.println("Setting n[0] to:\t\t\t"+fcName);
 					n.set(0, null);
 					n.set(2,fcName.toString());
 					n.set(1,"<<std::endl");
 					if(VERBOSE) System.out.println("N STRING" +n.toString());
 				}
 				else if(isPrint) {
 					if(VERBOSE) System.out.println("Setting 1n[0] to:\t\t\t"+fcName);
 					
 					n.set(0,null);
 					n.set(1,"");
 					n.set(2,fcName.toString());
 					if(VERBOSE) System.out.println("N1 STRING" +n.toString());
 				}
 				visit(n.getNode(3));
 				//visit(n.getNode(3));
 				
 				return fcNameList;
 				
 			}
 			/** Grabs full qualified names i.e. java.lang */
 			public void visitSelectionExpression (GNode n) {
 				visit(n);	
 				if (inCall);
 				fcName.append(n.getString(1)+"->");
 				if (VERBOSE) System.out.println("FC:"+ n.getString(1));
 				fcNameList.add(n.getString(1));
 			}
 			/**End value of a static variable or just a local variable */
 			public void visitPrimaryIdentifier (GNode n) {
 				if (inCall) {
 					inCall = false;
 					fcName.append(n.getString(0)+"->");
 					if (VERBOSE) System.out.println("FC:"+ n.getString(0));
 
 					fcNameList.add(n.getString(0));
 				} else {
 					//Do something?
 				}
 				visit(n);
 			}
 			/**Visists additve expression and replaces + with <<*/
 			public void visitAdditiveExpression (GNode n) {
 				if(isPrint) {
 					String type = getType(n);
                     if(type.equals("String"))//gets the type for a AdditiveExpression (if its a string use the concat <<)
 					   {
 						if(isPrintString) n.set(1,"<<");
 						}
 				}
 				visit(n);
 			}
 			
 			/**Gets back ingormation inside identifier calls inheritencetree to update the method type  Also has support for an rray*/
 			public void visitFieldDeclaration (GNode n) {
 				//visit(n);
 				//get the packge information(Inside Qualified Identifier)
 				//get the type which is located at the second child
 				ArrayList<String> currentPackage = new ArrayList<String>();
 				Object o = n.get(1);
 				if(o!=null)
 					{
 						currentPackage=getPackage((Node)o);
 					}
 				//remove the last Value (its the object name.)
 				String newtype = currentPackage.remove(currentPackage.size()-1);
 				
 				//check to see if current package is now empty
 				//if it is append zero
 				if (currentPackage.isEmpty()) {
 					currentPackage.add("0");
 				}
 				
 				String name="";
 				//get the name Locaed under declarator
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
 				
 				//update the type of the variable
 				if (VERBOSE) System.out.println("Updating Type Information("+name +"," +newtype+")");
 
 				method.update_type(name,currentPackage, newtype);
 				if (isString) {
 					//visit(n);
 					isString = false;
 				}
 				visit(n);
 				if (isArray) {
 					if(VERBOSE) System.out.println("Entering array");
 					ArrayMaker goArray = new ArrayMaker (n);
 					isArray = false;
 				}
 				visit(n);
 			}
 			/**Helper method that gets an array list of a pack when given  node assuming qualifiedId or Prim*/
 			public ArrayList<String> getPackage(Node n)
 			{
 				ArrayList<String> packages = new ArrayList<String>();
 				//get every child of the given node (either PrimirtiveType or QualifiedIdentifier)
 				//System.out.println("currentNode" +n.getName());
 				Node node = n.getNode(0);
 				//System.out.println("currentNode" +n.getName());
 				for(int i=0; i<node.size(); i++){
 					//for every child in QualifiedIdentifier append it to the array list
 					packages.add(node.getString(i));
 				}
 					
 				
 				return packages;
 			}
 			public void visitDimensions (GNode n) {
 				if(!isArray) {
 					if(VERBOSE) System.out.println("Setting array flag to true...");
 					isArray = true;
 				}
 				visit(n);
 			}
 			public void visitNewArrayExpression (GNode n) {
 				if(!isArray) {
 					if(VERBOSE) System.out.println("Setting array flag to true...");
 					isArray = true;
 					visit(n);
 				}
 			}
 			
 			/**helper method that uses the inheritence tree search for method and 
 			   returns the givne string array that should contain the return type and methodname 
 			   puts in check for isSuper flag*/
 			public String[] getMethodInfo(Node n,String Identifier,ArrayList<String> nameList,String name, ArrayList<String> argumentList)
 			{
 				if(VERBOSE){System.out.println("Running"+ Identifier+"," + nameList +","+name+","+argumentList);}
 				//method .search for type with packages if you dont send a package its the package you're in
 				//what do you send if its your current package
 				
 				InheritanceTree b;
 				if(isInstance)
 					{
 					
 						ArrayList<String> qualities=method.search_for_type(Identifier);//send the primary Identifier
 						//remove the last value from the arrayList (thats always the class name
 						String className =(String)qualities.remove(1);
 						//get the inheritance name of 
 						b =tree.root.search(qualities,className); //search takes the current method name?
 						//when there are no arguments sends a NullPointerException
						if(VERBOSE){System.out.println("On Instance"+ isInstance+"," + method +","+argumentList+","+name);}
 					}
 				else if (isSuper) 
 					{
						if(VERBOSE){System.out.println("On Super"+ isInstance+"," + method +","+argumentList+","+name);}
 						//call the search for method on the superclass's tree
 						b=tree.superclass; //superclass is a public variable
 					
 						//if isSuper put the class name as a string on the first child
 						if(isSuper){
 							n.set(0,b.className);
 						}
 					}
 				else{
 					if(VERBOSE){System.out.println("Running"+ isInstance+"," + method +","+argumentList+","+name);}
 					b=tree;
 				}
 				//returns an array of string 0= return type and 1=new method string
 				return b.search_for_method(isInstance,argumentList,name);
 			}
 			/**Helper method that checks for the types in the subtree and returns them 
 			   is currently used when get the types for values in an argument*/
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
 				else if (n.getName().equals("CallExpression"))
 				{
 					//get an array of the method arrtibutes in the inheritance tree (return type and new method name)
 					String[] methodArray;					//return the return type gotten from getMethodInfo (located as the first item in the given array)
 					methodArray=setMethodInfo(n);
 					return methodArray[0];
 				}
 				else if(n.getName().equals("PrimaryIdentifier")){ //return the name of the primaryIdentifier
 					//call the method in the inheritence tree to get the type of the primaryIden
 					
 					if(VERBOSE)System.out.println("Current Node"+n.getString(0));
 					if(VERBOSE) System.out.println("CurrentNode"+n.toString());
 					String type = " ";
 					if(VERBOSE) System.out.println("in Prim:"+ isPrint);
 					if(!isPrint){
 						ArrayList<String> packageNType= method.search_for_type(n.getString(0));
 						type = packageNType.remove(packageNType.size()-1);
 					}
 					return type;
 				}
 				else {//expression inside a method call
 					return getExpressionType(n);
 				}
 
 				/**can put support for handling methods inside an argument here (use search for methods
 				   to find out what the method will return? Are we storing the return type of a method in inheritence tree?*/
 			}
 			/**Helper method returns the type of expressions*/
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
 						if(child instanceof Node ) //if its a node call get type
 						{
 							String type = getType((Node)child); //get the type of the value in the expression
 							
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
 							return PrimTypes[l];
 						}
 				}
 				return "ERROR";
 			}
 			public void visitQualifiedIdentifier (GNode n) {
 				if (!isString) {
 					String temp = n.getString(0);
 					if (temp.equals("String")) isString = true;
 				}
 				visit(n);
 			}
 			public void visitStringLiteral (GNode n) {
 				if (isString) {
 					String temp = n.getString(0);
 					n.set(0,"__rt::stringify("+temp+")");
 					isString = false; //make sure it only happens once
 				}
 				if (isPrint) isPrintString = true;
 				visit(n);
 			}
 			
 			/**Gets the classes super class 
 			   finds the proper method in the inheritence tree of the superclass
 			*/
 			public void visitSuperExpression(GNode n)
 			{ 
 				InheritanceTree superclass = tree.superclass;
 				
 				//get the parameters 
 				
 				//get the class name
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
 				} if (type.equals("boolean")) {
 					n.set(0,"bool");
 					if (VERBOSE) System.out.println("changing boolean to bool");
 				}
 				if (type.equals("byte")) {
 					n.set(0,"char");
 					if (VERBOSE) System.out.println("changing byte to char");
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
