 package com.sdc.ast;
 
 import com.sdc.languages.general.printers.OperationPrinter;
 import com.sdc.languages.java.printers.JavaOperationPrinter;
 
 import java.util.HashSet;
 
 import static com.sdc.ast.Type.PrimitiveType.*;
 
 public class Type {
     public static final Type BOOLEAN_TYPE = new Type(BOOLEAN);
     public static final Type INT_TYPE = new Type(INT);
     public static final Type DOUBLE_TYPE = new Type(DOUBLE);
     public static final Type CHAR_TYPE = new Type(CHAR);
     public static final Type FLOAT_TYPE = new Type(FLOAT);
     public static final Type BYTE_TYPE = new Type(BYTE);
     public static final Type SHORT_TYPE = new Type(SHORT);
     public static final Type LONG_TYPE = new Type(LONG);
     public static final Type VOID = new Type((PrimitiveType) null);
 
     public enum PrimitiveType {
         BOOLEAN, INT, CHAR, SHORT, DOUBLE, FLOAT, BYTE, LONG
     }
 
     private final PrimitiveType myType;
     private final String myClassName;
     private String myOriginalClassName = null;
     private final int myDimensions; //for arrays with primitives types or classes
     private boolean myIsExtends = false;
 
     private static  HashSet<String> myPrimitiveTypes = new HashSet<String>();
 
     static {
         for( PrimitiveType type: PrimitiveType.values()){
             myPrimitiveTypes.add(type.name().toLowerCase());
         }
     }
 
     private Type(PrimitiveType type, String className, int dimensions,String originalClassName, boolean isExtends) {
         myType = type;
         myClassName = className;
         myDimensions = dimensions;
         myOriginalClassName = originalClassName;
         myIsExtends = isExtends;
     }
 
     public Type(PrimitiveType type) {
         myType = type;
         myClassName = null;
         myDimensions = 0;
         myOriginalClassName = null;
     }
 
     public Type(String className) {
         this(className,0);
     }
 
     public Type(PrimitiveType type, int dimensions) {
         myType = type;
         myDimensions = dimensions;
         myClassName = null;
     }
 
     public Type(String className, int dimensions) {
         myOriginalClassName = className;
 
         if (className == null) {
             myClassName = null;
             myType = null;
             myDimensions = 0;
             return;
         }
 
        String newClassName = className.trim();
         while (newClassName.endsWith("[]")) {
             dimensions++;
             newClassName = newClassName.substring(0, newClassName.length() - 2);
         }
         while (newClassName.startsWith("Array<") && newClassName.endsWith(">")) {
             dimensions++;
             newClassName = newClassName.substring("Array<".length(), newClassName.length() - 1);
         }
 
         myDimensions = dimensions;
        if (myPrimitiveTypes.contains(newClassName.toLowerCase())) {
            myType = PrimitiveType.valueOf(newClassName.toUpperCase());
             myClassName = null;
         } else {
            myClassName = newClassName;
             myType = null;
         }
     }
 
 
     public String toString(OperationPrinter operationPrinter) {
         //correct for kotlin and java
         String res = "";
         res = toStringWithoutBrackets(operationPrinter).trim();
         for (int i = 0; i < myDimensions; i++) {
             res = operationPrinter.getTypeWithBracketsView(res);
         }
         return res + operationPrinter.getSpaceAfterType();
     }
 
     public String toStringWithoutBrackets(OperationPrinter operationPrinter) {
         String res = "";
         if (!isPrimitive()) {
             res += operationPrinter.getNotPrimitiveView(myClassName);
         } else {
             switch (myType) {
                 case BOOLEAN:
                     res += operationPrinter.getBooleanTypeView();
                     break;
                 case INT:
                     res += operationPrinter.getIntTypeView();
                     break;
                 case CHAR:
                     res += operationPrinter.getCharTypeView();
                     break;
                 case SHORT:
                     res += operationPrinter.getShortTypeView();
                     break;
                 case DOUBLE:
                     res += operationPrinter.getDoubleTypeView();
                     break;
                 case FLOAT:
                     res += operationPrinter.getFloatTypeView();
                     break;
                 case BYTE:
                     res += operationPrinter.getByteTypeView();
                     break;
                 case LONG:
                     res += operationPrinter.getLongTypeView();
                     break;
                 default:
                     return "";
             }
         }
         res = res + operationPrinter.getSpaceAfterType();
         return res;
     }
 
     public String toStringWithoutBrackets() {
         return toStringWithoutBrackets(JavaOperationPrinter.getInstance());
     }
     @Override
     public String toString() {
         if(myOriginalClassName!=null) return myOriginalClassName;
         return toString(JavaOperationPrinter.getInstance());
     }
 
     public boolean isPrimitive() {
         return myClassName == null && myType != null;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Type type = (Type) o;
 
         if (myDimensions != type.myDimensions) return false;
         if (myClassName != null ? !myClassName.equals(type.myClassName) : type.myClassName != null) return false;
         if (myType != type.myType) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = myType != null ? myType.hashCode() : 0;
         result = 31 * result + (myClassName != null ? myClassName.hashCode() : 0);
         result = 31 * result + myDimensions;
         return result;
     }
 
     public static Type getStrongerType(Type type1, Type type2) {
         if (type1.isPrimitive() && type2.isPrimitive()) {
             if (type1 == DOUBLE_TYPE || type2 == DOUBLE_TYPE) {
                 return DOUBLE_TYPE;
             } else if (type1 == FLOAT_TYPE || type2 == FLOAT_TYPE) {
                 return FLOAT_TYPE;
             } else if (type1 == LONG_TYPE || type2 == LONG_TYPE) {
                 return LONG_TYPE;
             } else if (type1 == INT_TYPE || type2 == INT_TYPE) {
                 return INT_TYPE;
             } else if (type1 == SHORT_TYPE || type2 == SHORT_TYPE) {
                 return SHORT_TYPE;
             } else if (type1 == CHAR_TYPE || type2 == CHAR_TYPE) {
                 return CHAR_TYPE;
             } else if (type1 == BYTE_TYPE || type2 == BYTE_TYPE) {
                 return BYTE_TYPE;
             } else if (type1 == BOOLEAN_TYPE || type2 == BOOLEAN_TYPE) {
                 return BOOLEAN_TYPE;
             }
         }
         return new Type("Object");
     }
 
     public boolean isDoubleLength() {
         return myType == DOUBLE || myType == LONG;
     }
 
     public boolean isBoolean() {
         return myType == BOOLEAN && myDimensions == 0;
     }
 
     public int getDimensions() {
         return myDimensions;
     }
 
     public PrimitiveType getType() {
         return myType;
     }
 
     public String getClassName() {
         return myClassName;
     }
 
     public Type getTypeWithOnPairOfBrackets() {
         if (myDimensions <= 0) {
             return new Type(myType, myClassName, 0,null,false);
         } else {
             return new Type(myType, myClassName, myDimensions - 1,null,false);
         }
     }
 
     public Type getTypeWithoutBrackets() {
         return new Type(myType, myClassName, 0,myOriginalClassName,myIsExtends);
     }
 
     public boolean isVOID() {
         return myClassName == null && myType == null;
     }
 
     public boolean isArray() {
         return myDimensions > 0;
     }
 }
