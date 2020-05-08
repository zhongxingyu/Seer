 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.*;
 import java.util.Enumeration;
 
 /** This class may be used to contain the semantic information such as
  * the inheritance graph.  You may use it or not as you like: it is only
  * here to provide a container for the supplied methods.  */
 class ClassTable {
     private int semantErrors;
     private PrintStream errorStream;
     private HashMap< String, ArrayList<String> > adjacencyList;
 	public HashMap<String, class_c> nameToClass;
 
     //this hashmap is for finding least common ancestors
     private HashMap<String, Integer> getDepth;
     private Classes basicClassList;
 
 
     /** Creates data structures representing basic Cool classes (Object,
      * IO, Int, Bool, String).  Please note: as is this method does not
      * do anything useful; you will need to edit it to make if do what
      * you want.
      * */
     private void installBasicClasses() {
 	AbstractSymbol filename 
 	    = AbstractTable.stringtable.addString("<basic class>");
 	
 	// The following demonstrates how to create dummy parse trees to
 	// refer to basic Cool classes.  There's no need for method
 	// bodies -- these are already built into the runtime system.
 
 	// IMPORTANT: The results of the following expressions are
 	// stored in local variables.  You will want to do something
 	// with those variables at the end of this method to make this
 	// code meaningful.
 
 	// The Object class has no parent class. Its methods are
 	//        cool_abort() : Object    aborts the program
 	//        type_name() : Str        returns a string representation 
 	//                                 of class name
 	//        copy() : SELF_TYPE       returns a copy of the object
 
 	class_c Object_class = 
 	    new class_c(0, 
 		       TreeConstants.Object_, 
 		       TreeConstants.No_class,
 		       new Features(0)
 			   .appendElement(new method(0, 
 					      TreeConstants.cool_abort, 
 					      new Formals(0), 
 					      TreeConstants.Object_, 
 					      new no_expr(0)))
 			   .appendElement(new method(0,
 					      TreeConstants.type_name,
 					      new Formals(0),
 					      TreeConstants.Str,
 					      new no_expr(0)))
 			   .appendElement(new method(0,
 					      TreeConstants.copy,
 					      new Formals(0),
 					      TreeConstants.SELF_TYPE,
 					      new no_expr(0))),
 		       filename);
 	
 	// The IO class inherits from Object. Its methods are
 	//        out_string(Str) : SELF_TYPE  writes a string to the output
 	//        out_int(Int) : SELF_TYPE      "    an int    "  "     "
 	//        in_string() : Str            reads a string from the input
 	//        in_int() : Int                "   an int     "  "     "
 
 	class_c IO_class = 
 	    new class_c(0,
 		       TreeConstants.IO,
 		       TreeConstants.Object_,
 		       new Features(0)
 			   .appendElement(new method(0,
 					      TreeConstants.out_string,
 					      new Formals(0)
 						  .appendElement(new formalc(0,
 								     TreeConstants.arg,
 								     TreeConstants.Str)),
 					      TreeConstants.SELF_TYPE,
 					      new no_expr(0)))
 			   .appendElement(new method(0,
 					      TreeConstants.out_int,
 					      new Formals(0)
 						  .appendElement(new formalc(0,
 								     TreeConstants.arg,
 								     TreeConstants.Int)),
 					      TreeConstants.SELF_TYPE,
 					      new no_expr(0)))
 			   .appendElement(new method(0,
 					      TreeConstants.in_string,
 					      new Formals(0),
 					      TreeConstants.Str,
 					      new no_expr(0)))
 			   .appendElement(new method(0,
 					      TreeConstants.in_int,
 					      new Formals(0),
 					      TreeConstants.Int,
 					      new no_expr(0))),
 		       filename);
 
 	// The Int class has no methods and only a single attribute, the
 	// "val" for the integer.
 
 	class_c Int_class = 
 	    new class_c(0,
 		       TreeConstants.Int,
 		       TreeConstants.Object_,
 		       new Features(0)
 			   .appendElement(new attr(0,
 					    TreeConstants.val,
 					    TreeConstants.prim_slot,
 					    new no_expr(0))),
 		       filename);
 
 	// Bool also has only the "val" slot.
 	class_c Bool_class = 
 	    new class_c(0,
 		       TreeConstants.Bool,
 		       TreeConstants.Object_,
 		       new Features(0)
 			   .appendElement(new attr(0,
 					    TreeConstants.val,
 					    TreeConstants.prim_slot,
 					    new no_expr(0))),
 		       filename);
 
 	// The class Str has a number of slots and operations:
 	//       val                              the length of the string
 	//       str_field                        the string itself
 	//       length() : Int                   returns length of the string
 	//       concat(arg: Str) : Str           performs string concatenation
 	//       substr(arg: Int, arg2: Int): Str substring selection
 
 	class_c Str_class =
 	    new class_c(0,
 		       TreeConstants.Str,
 		       TreeConstants.Object_,
 		       new Features(0)
 			   .appendElement(new attr(0,
 					    TreeConstants.val,
 					    TreeConstants.Int,
 					    new no_expr(0)))
 			   .appendElement(new attr(0,
 					    TreeConstants.str_field,
 					    TreeConstants.prim_slot,
 					    new no_expr(0)))
 			   .appendElement(new method(0,
 					      TreeConstants.length,
 					      new Formals(0),
 					      TreeConstants.Int,
 					      new no_expr(0)))
 			   .appendElement(new method(0,
 					      TreeConstants.concat,
 					      new Formals(0)
 						  .appendElement(new formalc(0,
 								     TreeConstants.arg, 
 								     TreeConstants.Str)),
 					      TreeConstants.Str,
 					      new no_expr(0)))
 			   .appendElement(new method(0,
 					      TreeConstants.substr,
 					      new Formals(0)
 						  .appendElement(new formalc(0,
 								     TreeConstants.arg,
 								     TreeConstants.Int))
 						  .appendElement(new formalc(0,
 								     TreeConstants.arg2,
 								     TreeConstants.Int)),
 					      TreeConstants.Str,
 					      new no_expr(0))),
 		       filename);
 
 	/* Do somethind with Object_class, IO_class, Int_class,
            Bool_class, and Str_class here */
     nameToClass.put(TreeConstants.Object_.getString(), Object_class);
     nameToClass.put(TreeConstants.IO.getString(), IO_class);
     nameToClass.put(TreeConstants.Int.getString(), Int_class);
     nameToClass.put(TreeConstants.Bool.getString(), Bool_class);
     nameToClass.put(TreeConstants.Str.getString(), Str_class);
     adjacencyList.put(TreeConstants.Object_.getString(), new ArrayList<String>() );
     adjacencyList.put(TreeConstants.IO.getString(), new ArrayList<String>() );
     adjacencyList.put(TreeConstants.Int.getString(), new ArrayList<String>() );
     adjacencyList.put(TreeConstants.Bool.getString(), new ArrayList<String>() );
     adjacencyList.put(TreeConstants.Str.getString(), new ArrayList<String>() );
     // Do the same for other basic classes
     basicClassList = new Classes(0);
     basicClassList.appendElement(Object_class);
     basicClassList.appendElement(IO_class);
     basicClassList.appendElement(Int_class);
     basicClassList.appendElement(Bool_class);
     basicClassList.appendElement(Str_class);
    
     }
 	
 
 
     public ClassTable(Classes cls) {
 	semantErrors = 0;
 	errorStream = System.err;
 	
 	/* Build graph first out of class declarations */
 	if (Flags.semant_debug) {
 		System.out.println("Building a graph");
 	}
 	adjacencyList = new HashMap< String, ArrayList<String>>();
 	nameToClass = new HashMap<String, class_c>();
 
         getDepth = new HashMap<String, Integer>();
 
 	installBasicClasses();
 	buildGraph(cls);
 	assignDepths("Object", 0); //assign depths starting at root node of tree (depth 0)
     }
 
     //return depth given name of node
 
     public int getDepthFromNode (String node){
    	 if (Flags.semant_debug){
    	 	System.out.println("NODE NAME IS :" + node);
    	 	System.out.println(getDepth);
	     }
         int depth = getDepth.get(node);
         return depth;
     }
     //assign depths to every node in inheritance tree
     private void assignDepths(String node, int depth){
         getDepth.put(node, depth);
 	ArrayList<String> children = adjacencyList.get(node);
 
         if (children == null) return;
  	//if there are children, assign depths to each, and recurse
         if (!children.isEmpty()){
	    	depth = depth +1;
	    	for (String child : children) {
	      	  assignDepths(child, depth);
             }
         }
         
     }
     /** Prints line number and file name of the given class.
      *
      * Also increments semantic error count.
      *
      * @param c the class
      * @return a print stream to which the rest of the error message is
      * to be printed.
      *
      * */
     public PrintStream semantError(class_c c) {
 	return semantError(c.getFilename(), c);
     }
 
     /** Prints the file name and the line number of the given tree node.
      *
      * Also increments semantic error count.
      *
      * @param filename the file name
      * @param t the tree node
      * @return a print stream to which the rest of the error message is
      * to be printed.
      *
      * */
     public PrintStream semantError(AbstractSymbol filename, TreeNode t) {
 	errorStream.print(filename + ":" + t.getLineNumber() + ": ");
 	return semantError();
     }
 
     /** Increments semantic error count and returns the print stream for
      * error messages.
      *
      * @return a print stream to which the error message is
      * to be printed.
      *
      * */
     public PrintStream semantError() {
 	semantErrors++;
 	return errorStream;
     }
 
     /** Returns true if there are any static semantic errors. */
     public boolean errors() {
 	return semantErrors != 0;
     }
 
     /** Builds an adjacency list representation of the inheritance graph 
      and also checks of the graph has no cycles **/
     private void buildGraph(Classes classes) {
 	
	adjacencyList.put(TreeConstants.Object_.getString(), new ArrayList<String>() );
	//add primitives to the children of object
    ArrayList<String> objectlist = adjacencyList.get(TreeConstants.Object_.getString());
    objectlist.add(TreeConstants.IO.getString());
    objectlist.add(TreeConstants.Int.getString());
    objectlist.add(TreeConstants.Bool.getString());
    objectlist.add(TreeConstants.Str.getString());
    adjacencyList.put(TreeConstants.Object_.getString(), objectlist);
 	for (Enumeration e = classes.getElements(); e.hasMoreElements(); ) {
 	    class_c currentClass = ((class_c)e.nextElement());
 
 	    // If the same class name is already present, that's a redefinition error
 	    String className = currentClass.getName().toString();
 	    if (!nameToClass.containsKey(className)) {
 	    	nameToClass.put(currentClass.getName().toString(), currentClass);
 	    } else {
 	    	semantError(currentClass).println("Class " + className + " was previously defined");
 	    	continue;
 	    }
 	    // if parent already present in HashMap, append child to list of children
 	    String parent = currentClass.getParent().toString();
 	    if ( !adjacencyList.containsKey(parent) ) {
 		adjacencyList.put(parent, new ArrayList<String>() );
 	    }
 	    adjacencyList.get(parent).add(currentClass.getName().toString());
 	}
 
     // Check if each parent in a parent-child inheritance is a valid class
     HashSet<String> bogusClasses = new HashSet<String>();
     for (String parent : adjacencyList.keySet()) {
     	if (!nameToClass.containsKey(parent)) {
     		for (String child: adjacencyList.get(parent)) {
     			semantError(nameToClass.get(child)).println("Class " + child + " inherits from an undefined class " + parent);
     		}
     		// Remove the bogus parent class from the graph
     		bogusClasses.add(parent);
     	}
     }
     // Remove the bogus parent class from the graph
     for (String bogus : bogusClasses) {
     	adjacencyList.remove(bogus);
     }
     if (Flags.semant_debug) {
     	System.out.println("Pruned out unreachable classes");
     }
 
     // Also check if someone's inheriting from the Basic classes other than Object & IO
     for (String child : adjacencyList.get(TreeConstants.Int.getString())) {
     	semantError(nameToClass.get(child)).println("Class " + child + " illegally inherits from class Int");
     }
     for (String child : adjacencyList.get(TreeConstants.Str.getString())) {
     	semantError(nameToClass.get(child)).println("Class " + child + " illegally inherits from class Str");
     }
     for (String child : adjacencyList.get(TreeConstants.Bool.getString())) {
     	semantError(nameToClass.get(child)).println("Class " + child + " illegally inherits from class Bool");
     }
     // No point in continuing further. The above classes are going to propagate more errors
     if (Flags.semant_debug) {
     	System.out.println("Checked for simple inheritance errors");
     }
     if (errors()) {
     	return;
     }
 
 	// Now check for cycles
 	// Do the depth first search of this adjacency list starting from Object
 	HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
 	for (String key : adjacencyList.keySet() ) {
 		visited.put(key, false);
 		for ( String value : adjacencyList.get(key) ) {
 			visited.put(value, false);
 		}
 	}
 	depthFirstSearch(visited, TreeConstants.Object_.toString());
 	// It is legal to inherit from the IO class. So mark classes down that tree as well
	
	/*depthFirstSearch(visited, TreeConstants.IO.getString());
 	// Check for unreachable components - unreachable classes are cycles
 	// Except the Bool, IO, Int and String. Hack - set them to true
 	visited.put(TreeConstants.IO.getString(), true);
 	visited.put(TreeConstants.Bool.getString(), true);
 	visited.put(TreeConstants.Str.getString(), true);
 	visited.put(TreeConstants.Int.getString(), true);
	*/
 	for (String key : visited.keySet()) {
 		if (!visited.get(key)) {
 			semantError(nameToClass.get(key)).println("Class " + key + " or an ancestor is involved in an inheritance cycle.");
 		}
 	} 
 
 	if (Flags.semant_debug) {
 		System.out.println("Checked for cycles");
 	}
 	}
 
     /** Depth first traversal of the graph, checking for cycles **/
     private Boolean depthFirstSearch(HashMap<String, Boolean> visited, String node) {
 	if (visited.get(node)) {
 		// This can actually never happen
 		if (Flags.semant_debug) {
 			System.out.println("Node: " + node + " visited twice");
 		}
 		semantError(nameToClass.get(node)).println("Class " + node + ", or its ancestors, is invloved in a cycle");
 		return false;
 	}
 	visited.put(node, true);
 	if (adjacencyList.get(node) == null) {
 		return true;
 	}
 	for (String child : adjacencyList.get(node)) {
 		if (Flags.semant_debug) {
 			System.out.println("Traversing " + node + " --> " + child);
 		}
 		depthFirstSearch(visited, child);
 	}
 	return true;
     } 
 
     public Boolean isValidType(AbstractSymbol type) {
     	return nameToClass.containsKey(type.toString());
     }
 
     /** Checks if C1 <= C2		**/
     public Boolean checkConformance(AbstractSymbol C1, AbstractSymbol C2) {
     	if ( C1 == C2 ) {
     		return true;
     	}
     	// Check for a path from C2 to reach C1
     	return isReachable(C1.getString(), C2.getString());
     }
 
     /** Checks if C1 is reachable from C2 **/
     private Boolean isReachable(String C1, String C2) {
     	if (adjacencyList.get(C2) == null) {
     		return false;
     	}
     	for (String child : adjacencyList.get(C2)) {
     		if (child.equals(C1)) {
     			return true;
     		} else if ( isReachable(C1, child ) ) {
     			return true;
     		}
     	}
     	return false;
     }
 
     /** Returns the name of the parent class of a given class **/
     public String getParent(String child) {
     	if ( child.equals(TreeConstants.Object_.getString())) {
     		return TreeConstants.No_class.toString();
     	}
     	for (String parent : adjacencyList.keySet()) {
     		ArrayList<String> listOfChildren = adjacencyList.get(parent);
     		if (listOfChildren.indexOf(child) != -1) {
     			return parent;
     		}
     	}
     	// This shouldn't happen
     	return null;
     }
 
     public ArrayList<String> getChildren(String parent) {
     	return adjacencyList.get(parent);
     }
 
     public Classes getBasicClassList() {
     	return basicClassList;
     }
 
     public class_c getClass (String className){
     	return nameToClass.get(className);
     }
 }
