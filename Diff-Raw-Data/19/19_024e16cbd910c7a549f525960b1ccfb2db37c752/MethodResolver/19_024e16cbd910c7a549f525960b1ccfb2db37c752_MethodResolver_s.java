 package qimpp;
 import xtc.tree.*;
 import java.util.*;
 
 public class MethodResolver {
 
   private static InheritanceTreeManager inheritanceTree;
 
   /**
    * @param methodName the unmangled method name, printers should mangle names by argument types
    * @param classDeclaration the Type node of the calling class
    * @param argumentTypes a Node containing the Types of the arguments
    * @param inheritanceTree the data structure describing the inheritance relationships between translated classes
    * @return a GNode the mangled method name, and the return Type node
   */
   public static GNode resolve (String methodName, GNode classType, GNode argTypes, InheritanceTreeManager inheritanceTree ) {
     //TODO: Implement overloading. For now we just return the first method with the right name
    GNode classDeclaration = inheritanceTree.getClassDeclarationNode(Disambiguator.getDotDelimitedName(classType.getGeneric(0)));
     ArrayList<GNode> nameMatches = findNameMatches(methodName, classDeclaration); 
     ArrayList<GNode> argLengthMatches = findArgLengthMatches(nameMatches, argTypes.size());
 
     GNode calledMethod = getMostSpecific(argLengthMatches);
 
     MethodResolver.inheritanceTree = inheritanceTree;
      
     return GNode.create("CallInfo", calledMethod.getString(0), calledMethod.getGeneric(1));
   }
 
   /**
    * Get the best match
    */
   private static GNode getMostSpecific(ArrayList<GNode> possibleMatches){
     // Bubble up, because I'm lazy
     // Also, this will work without complaint if the result is actually ambiguous,
     // we are assuming the Java program actually works
     for (int i = 0; i < possibleMatches.size() - 1; i++){
       if (isCastable(possibleMatches.get(i), possibleMatches.get(i + 1))){
         possibleMatches.set(i + 1, possibleMatches.get(i));
       }
     }
 
     return possibleMatches.get(possibleMatches.size() - 1);
   }
 
   /**
    * Check if one type is upcastable to another
    */
   private static boolean isCastable(GNode sourceArgTypes, GNode targetArgTypes){
    for (int i = 0; i < targetArgTypes.size(); i++){
     // They must both be PrimitiveType or both QualifiedIdentifier
     GNode targetType = targetArgTypes.getGeneric(i);
     GNode sourceType = sourceArgTypes.getGeneric(i);
 
     if (targetType.getGeneric(0).getName().equals(sourceType.getGeneric(0).getName())){
       return false;
     }
     
     if (targetArgTypes.getGeneric(0).equals("QualifiedIdentifier")){
       if (!isClassCastable(sourceType, targetType)){
         return false;
       }
     }
 
     // Primitive type
     else {
       if (!isPrimitiveTypeCastable(sourceType, targetType)){
         return false;
       }
     }
    }
 
    return true;
   } 
 
   /**
    * Determine if one class type is castable to another
    */
   private static boolean isClassCastable(GNode sourceType, GNode targetType){
     String sourceName = Disambiguator.getDotDelimitedName(sourceType.getGeneric(0));
     String targetName = Disambiguator.getDotDelimitedName(targetType.getGeneric(0));
 
     if (sourceName.equals(targetName)) return true;
 
     GNode sourceClassTreeNode = inheritanceTree.getClassTreeNode(sourceName);
     GNode targetClassTreeNode = inheritanceTree.getClassTreeNode(targetName);
 
     while (sourceName != "java.lang.Object"){
       sourceClassTreeNode = (GNode)sourceClassTreeNode.getProperty(InheritanceTreeManager.PARENT_CLASS);
      sourceName = sourceClassTreeNode.getStringProperty(InheritanceTreeManager.CLASS_NAME);
 
       if (sourceName.equals(targetName)) return true;
     }
 
     return false;
   }
 
   /**
    * Determine if an primitive argument of one type can be casted to another
    */
   private static boolean isPrimitiveTypeCastable(GNode sourceType, GNode targetType){
     String sourceName = sourceType.getGeneric(0).getString(0);
     String targetName = targetType.getGeneric(0).getString(0);
 
     return Type.canWiden(sourceName, targetName);    
   }
 
   /**
    * Get all the methods with matching length of arguments
    */
   private static ArrayList<GNode> findArgLengthMatches(ArrayList<GNode> nameMatches, int numArgs){
     ArrayList<GNode> lengthMatches = new ArrayList<GNode>();
 
     for (GNode n : nameMatches){
       GNode args = n.getGeneric(3);
       if (args.size() == numArgs){
         lengthMatches.add(n);
       }
     }
 
     return lengthMatches;
   }
 
   private static ArrayList<GNode> findNameMatches(String methodName, GNode classDeclaration){
     GNode methodContainer = classDeclaration.getGeneric(4);
     ArrayList<GNode> matches = new ArrayList<GNode>();
     for (int i = 0; i < methodContainer.size(); i++){
       GNode method = methodContainer.getGeneric(i);
       if (method.getName().equals("InheritedMethodContainer")){
         if (method.getGeneric(0).getString(0).equals(methodName))
           matches.add(method.getGeneric(0));
       }
       else{
         if (method.getString(0).equals(methodName)) {
           matches.add(method);
         }
       }
     }
     return matches;
   }
 } 
