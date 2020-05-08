 package qimpp;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Printer;
 
 import xtc.util.Tool;
 
 import java.util.HashMap;
 
 
 /**
  * Converts Java PrimitiveType GNode objects into the corresponding C++ types.
  *
  * @author QIMPP
  */
 class Type {
   /**
    * For reference:
    * Java primitive types: int, long, short, float, double, char, boolean
    * C++ primitive types: int, long int, short int, float, double, char, bool
    *
    * Then there's weird stuff with signed vs unsigned char that Grimm will
    * try to trip us up on.
    */
 
   // not using generics to specify key, value types, so need to suppress warnings
   @SuppressWarnings(value = "unchecked")
   static HashMap<String, String> primitives = new HashMap<String, String>() {{
     put("long", "int64_t");
     put("int", "int32_t");
     put("short", "int16_t");
     put("byte", "int8_t");
     put("float", "float");
     put("double", "double");
     put("char", "char");
     put("boolean", "bool");
     put("void", "void");
   }};
 
   static String[] priorities = 
   { "double", "float", "long", "int", "short", "byte", "char", "boolean" };
 
   /** The conversions for char that do not lose information */
   static String[] charWidening = { "int", "long", "float", "double" };
   
   static String[] shortWidening = { "int", "long", "float", "double" };
 
   static String[] byteWidening = { "short", "int", "long", "float", "double" };
 
   static String[] intWidening = { "long", "float", "double" };
 
   static String[] longWidening = { "float", "double" };
 
   static String[] floatWidening = { "double" };
 
   /**
    * Figure out if one type can be cast to another
    */
   static boolean canWiden(String sourceType, String targetType){
     if (sourceType.equals(targetType)){
       return true;
     }
     if (sourceType.equals("char")){
       for (int i = 0; i < charWidening.length; i++){
         if (targetType.equals(charWidening[i])) return true;
       }
     }
     else if (sourceType.equals("short")){
       for (int i = 0; i < shortWidening.length; i++){
         if (targetType.equals(shortWidening[i])) return true;
       }
     }
     else if (sourceType.equals("byte")){
       for (int i = 0; i < byteWidening.length; i++){
         if (targetType.equals(byteWidening[i])) return true;
       }
     }
     else if (sourceType.equals("int")){
       for (int i = 0; i < intWidening.length; i++){
         if (targetType.equals(intWidening[i])) return true;
       }
     }
     else if (sourceType.equals("long")){
       for (int i = 0; i < longWidening.length; i++){
         if (targetType.equals(longWidening[i])) return true;
       }
     }
     else if (sourceType.equals("float")){
       for (int i = 0; i < floatWidening.length; i++){
         if (targetType.equals(floatWidening[i])) return true;
       }
     }
     return false; 
   }
 
 
   /**
    * Get the higher priority of two types
    */
   static String compare(String type1, String type2){
    for (int i = 0; i < priorities.length; i++){
      if (type1.equals(priorities[i]))
        return type1;
      if (type2.equals(priorities[i]))
        return type2;
    }
    return null;
   }
 
   /**
    * Get C++ equivalent of primative type.
    *
    * @param type Name of primitive type.
    * @returns C++ equivalent of primative type.
    */
   static String primitiveType(String type){
     if(primitives.containsKey(type)) return primitives.get(type);
     return type;
   }
 
   /**
    * Get a method name based on its arguments
    * @param method the Java AST MethodDeclaration node
    */
   static String getJavaMangledMethodName(GNode method){
     StringBuilder mangledNameBuilder = new StringBuilder();
     mangledNameBuilder.append(method.getString(3));
 
     GNode args = method.getGeneric(4);
 
     for ( Object o : args ) {
       GNode type = ((GNode)o).getGeneric(1);
       mangledNameBuilder.append("_");
       if ( type.getGeneric(0).getName().equals("PrimitiveType") ){
         mangledNameBuilder.append(type.getGeneric(0).getString(0));
       }
       else {
         mangledNameBuilder.append(Disambiguator.getUnderscoreDelimitedName(type.getGeneric(0)));
       }
     }
 
     return mangledNameBuilder.toString();
   }
 
   /**
    * Get a method name based on its arguments
    * @param method the CPP AST MethodDeclaration node
    */
   static String getCppMangledMethodName(GNode method){
     StringBuilder mangledNameBuilder = new StringBuilder();
     mangledNameBuilder.append(method.getString(0));
 
     GNode args = method.getGeneric(2);
 
     for ( Object o : args ) {
       GNode type = ((GNode)o).getGeneric(1);
       mangledNameBuilder.append("_");
       if ( type.getGeneric(0).getName().equals("PrimitiveType") ){
         mangledNameBuilder.append(type.getGeneric(0).getString(0));
       }
       else {
         mangledNameBuilder.append(Disambiguator.getUnderscoreDelimitedName(type.getGeneric(0)));
       }
     }
     return mangledNameBuilder.toString();
   }
  
   /**
    * Get C++ qualified identifier.
    *
    * @param type Name of qualified type.
    * @returns C++ equivalent of qualified type.
    */
   static String qualifiedIdentifier(String type){
     if(type.equals("String")) return "java::lang::String";
     else return type;
   }
 
   static String getNamespace(String qualifiedName) {
     String[] qualifiers = qualifiedName.split("\\.");
     StringBuilder namespace = new StringBuilder();
     for ( int i = 0; i < qualifiers.length - 1; i++ ){
       namespace.append(qualifiers[i]);
       namespace.append("::");
     }
     return namespace.toString();
   }
 
   static String getClassName(String qualifiedName){
     String[] qualifiers = qualifiedName.split("\\.");
     return qualifiers[qualifiers.length - 1];
   }
   
   static String getClassTypeName(String qualifiedName){
     return getNamespace(qualifiedName) + "__" + getClassName(qualifiedName);
   }
 
   static String getClassInstanceName(String qualifiedName){
     return getNamespace(qualifiedName) + getClassName(qualifiedName);
   }
 
   static String getInstanceName(GNode qualifiedIdentifier){
     String qualifiedName = Disambiguator.getDotDelimitedName(qualifiedIdentifier);
     return getNamespace(qualifiedName) + getClassName(qualifiedName);
   }
 
   static String getTypeInstanceName(GNode typeNode){
     //TODO: Support arrays
     return getInstanceName(typeNode.getGeneric(0));
   }
 
   static String getTypeName(GNode typeNode){
     //TODO: Support arrays
     return getClassTypeName(typeNode.getGeneric(0));
   }
 
   static String getClassTypeName(GNode qualifiedIdentifier){
     String qualifiedName = Disambiguator.getDotDelimitedName(qualifiedIdentifier);
     return getNamespace(qualifiedName) + "__" +  getClassName(qualifiedName);  
   }
 }
