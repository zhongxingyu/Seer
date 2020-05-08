 package qimpp;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 import xtc.tree.Printer;
 
 import java.util.ArrayList;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /** Maintains a tree with pointers class definitions for all classes referenced in the program being translated 
  *
  *  @author QIMPP
  * */
 
 public class InheritanceTreeManager {
 
   ///////////////////
   // STRING CONSTANTS
   ///////////////////
 
 
   /* TODO: Implement package-based dereferencing: right now we're just naively looking by name */
   /** Maps class names to their class tree nodes */
   HashMap<String, GNode> classNameMap;
 
   /** Root of the package/class tree */
   GNode root;
   /** The class everyone inherits from (Object) */
   GNode rootClassNode;
 
   //////////////////
   // STATE VARIABLES
   //////////////////
 
   public static final String CLASS_TREE_NODE = "ClassTreeNode";
   public static final String PACKAGE_QUALIFIER = "PackageQualifier";
   public static final String CLASS_STRUCTURE_ROOT = "ClassStructureRoot";
 
   public static final String QUALIFIER_NAME = "QualifierName";
   public static final String CLASS_NAME = "ClassName";
   public static final String CLASS_DECLARATION = "ClassDeclaration";
   public static final String PARENT_CLASS = "ParentClass";
 
   
   /** The package of the sourc
   GNode currentNamespace;);
   GNode javaLangNamespace;
   G
 
   /** Construct the tree with the given root object node (java.lang.Object)
    *
    * @param rootClass The fully qualified name of the root class ({"java", "lang", "Object"})
    * @param rootClassDeclaration the ClassDeclaration of the root class containing its fields and methods
    * */
   public InheritanceTreeManager(ArrayList<String> rootClass, GNode rootClassDeclaration){
     ////System.out.println("Running main constructor");
       
     root = GNode.create(CLASS_STRUCTURE_ROOT);
 
     GNode rootClassTreeNode = GNode.create(CLASS_TREE_NODE);
     rootClassTreeNode.setProperty("ClassName", "java.lang.Object");;
     rootClassTreeNode.setProperty("ClassDeclaration", rootClassDeclaration);
     // Set a direct reference to what everyone inherits from
     this.rootClassNode = rootClassTreeNode;
     this.rootClassName = rootClass;
 
     insertClass(rootClass, rootClassTreeNode);
 
 
     // Insert dummy classes for java.lang parented to Object
     GNode stringClassDeclaration = GNode.create("ClassDeclaration");
     stringClassDeclaration.add("java.lang.String");
     GNode classClassDeclaration = GNode.create("ClassDeclaration");
     classClassDeclaration.add("java.lang.Class");
     String[] stringQualified = {"java", "lang", "String"};
     String[] classQualified = {"java", "lang", "Class"};
     insertClass(new ArrayList<String>(Arrays.asList(stringQualified)), null, stringClassDeclaration);
     insertClass(new ArrayList<String>(Arrays.asList(stringQualified)), null, classClassDeclaration);
   }
 
   private ArrayList<String> rootClassName; 
 
   /** An overload where java.lang.Object is implicit */
   public InheritanceTreeManager(GNode rootClassDeclaration) {  
     
     this( new ArrayList<String>( Arrays.asList("java", "lang","Object")), rootClassDeclaration);
   
   }
 
   /** A toString using a Visitor for debugging */
   public String toString(){
 
     final StringBuilder b = new StringBuilder();
     b.append("Class Inheritance Tree:\n");
 
     new Visitor () {
 
       public void visitPackageQualifier( GNode n ){
         b.append("Package qualifier: " + n.getStringProperty(QUALIFIER_NAME) + "\n");
         visit(n);
         b.append("End " + n.getStringProperty(QUALIFIER_NAME) + "\n");
       }
 
       public void visitClassTreeNode( GNode n ){
         b.append("ClassTreeNode: " + n.getStringProperty(CLASS_NAME) + "\n");
       }
 
       public void visit(Node n) {
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
       }
     }.dispatch(root);
    
     return b.toString();
   }
   
 
   //
   // State variables for dereference()
   //
 
   /** The node found by dereference() */
   private GNode foundNode;
 
   /** The furthest we got dereferencing the node in the package tree
    * */
   private GNode deepestPackageNode;
 
   /** Index of the qualifier we're up to */
   private int dereferenceDepth;
 
   /** Utility function to get the parent ClassTreeNode */
   public GNode getParent( GNode classTreeNode ){
     return (GNode)classTreeNode.getProperty(PARENT_CLASS);
   }
 
   /** Takes a class name and gets the fully qualified class name, 
    *  or null if it cannot be found.   
    *  *  
    *  */
   public ArrayList<String> disambiguate( String className ) {
     return new ArrayList<String>(Arrays.asList(className.split("\\.")));
   } 
 
   /** Figure out which class we're referring to, and get its
    *  ClassTreeNode to know its inheritance hierarchy */
   public GNode getClassTreeNode(String className){
     return dereference(disambiguate(className));
   }
 
   /**
    * Utility function return the ClassDeclaration for a given name
    * @return the ClassDeclaration GNode
    */
   public GNode getClassDeclarationNode(String className) {
     GNode classTreeNode = getClassTreeNode(className);
     if (classTreeNode != null){
       return (GNode)classTreeNode.getProperty(CLASS_DECLARATION);
     }
     return null;
   }
   
   /** An overload-alias to minimize necessary brain-space */
   public GNode getClassTreeNode(ArrayList<String> className){
     return dereference(className);
   }
   
   int deepestDereference;
 
   /** Get the ClassNode based on its fully qualified package name 
    *  Also sets deepestPackageNode
    * */
   public GNode dereference(final ArrayList<String> className){
     // Traverse with state variables
     //System.err.println(this);
     //System.err.print("finding ");
     for (String s: className){
       //System.err.print(s);
     }
     //System.err.println();
     
     foundNode = null;
     dereferenceDepth = 0;
     deepestDereference = 0;
     deepestPackageNode = root; 
 
     new Visitor() {
 
       public void visitClassTreeNode(GNode n){
         if (
         getClassTreeNodeName(n).equals
           (className.get(dereferenceDepth))
         ){
           //////System.out.println("Found it!");
           foundNode = n;
         }
         else {
           //////System.out.println("Class " + n.getStringProperty(CLASS_NAME) + " not what we're looking for " + className.get(dereferenceDepth));
         }
       }
 
       public void visitPackageQualifier(GNode n){
         if (n.getStringProperty(QUALIFIER_NAME).equals(className.get(dereferenceDepth))) {
           dereferenceDepth++;
           if (dereferenceDepth > deepestDereference)
             deepestDereference = dereferenceDepth;
           deepestPackageNode = n;
           visit(n);
           dereferenceDepth--;
         }
         else {
             //////System.out.println("Not in " + n.getStringProperty(QUALIFIER_NAME));
         }
       }
 
       public void visit(Node n) {
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
       }
     }.dispatch(root);
 
     //////System.out.println("RootClassNode: " + Boolean.toString(rootClassNode == null));
 
     return foundNode;
   }
   
   public void reparent(ArrayList<String> child, ArrayList<String> parent){
     GNode childNode = dereference(child);
     GNode parentNode = dereference(parent);
     
     if ( null == childNode || null == parentNode ) {
       System.err.println(this);
       throw new RuntimeException("Failed to reparent");
     } 
     
     childNode.setProperty(PARENT_CLASS, parentNode);
   }
 
   private String getClassTreeNodeName(GNode n){
     return n.getStringProperty(CLASS_NAME);
   }
 
   /** Gets an ordered list of the inheritance hierarchy for a class 
    *
    * @param className an array of Strings defining the fully 
    * qualified name of the class we want
    * For example, the array for Point in package org.xtc should be 
    * ["org", "xtc", "Point"]
    * For Point in the root package (the "src" directory) should just
    * be ["Point"]
    * */
   public ArrayList<GNode> getParentList(ArrayList<String> className){
     return null;
   }
 
   /** Inserts a class into the tree and parents it to the given object
    *
    *  @param className the fully-qualified name of the class to insert
    *  @param parentName the fully-qualified name of the class's 
    *  parent, or null if it's Object
    *
    *  @throws RuntimeException with the message 
    *  "No such parent class [className]"
    *  */
   public GNode insertClass(ArrayList<String> className, ArrayList<String> parentName, GNode classDeclaration){
     GNode classTreeNode = GNode.create(CLASS_TREE_NODE);
     if ( null == parentName ) {
       // If the parent isn't specified, set it to Object
       classTreeNode.setProperty("ParentClass", rootClassNode);
     } 
     else{
       GNode parent = dereference(parentName);
       ////System.out.println("Parent exists: " + Boolean.toString( parent != null));
       // If the parent doesn't exist, throw a runtime exception
       if ( null == parent) {
 
         String parentNameString = "";
         for (int i = 0; i < parentName.size(); i++){
           parentNameString += parentName.get(i) + ".";
         }
 
         throw new RuntimeException(
             "No such parent class " + 
             parentName.get(parentName.size() - 1));
       }
 
       classTreeNode.setProperty("ParentClass", parent);
     }
 
     classTreeNode.setProperty(
        "ClassName", className.get(className.size() - 1));
 
     classTreeNode.setProperty("ClassDeclaration", classDeclaration);
     insertClass(className, classTreeNode);
     return classTreeNode;
   } 
 
   /** Insert a tree node into the tree. Its name and parent should 
    *  already be set 
    *  @param className the fully qualified name of the node
    *  @param classTreeNode the node containing the classDefinition 
    *  and a pointer to the parent of this object*/
   private boolean insertClass(ArrayList<String> className, GNode classTreeNode){
     // dereference sets deepestPackageNode
     if ( null != dereference(className)){
       // This class should not already be inserted! 
       ////System.out.println("This class is inserted already!");
       return false;
     }
     else{
       // Starting at the deepest already-defined point in the tree, make new entries
       GNode n = deepestPackageNode;
       ////System.out.println(className.get(className.size() - 1) + " starts at " + Integer.toString(dereferenceDepth));
       GNode current = n;
       for( int i = deepestDereference; i < className.size() - 1; i++ ){
         GNode newQualifier = GNode.create(PACKAGE_QUALIFIER);
         newQualifier.setProperty(QUALIFIER_NAME, className.get(i));
         current.add(newQualifier);
         ////System.out.println("Added " + className.get(i) + " to " + current.getName());
         current = newQualifier;
 
       }
 
       // Finally, add the actual class tree node
       current.add(classTreeNode);
       return true; //success
     }
   }
 
 
 }
