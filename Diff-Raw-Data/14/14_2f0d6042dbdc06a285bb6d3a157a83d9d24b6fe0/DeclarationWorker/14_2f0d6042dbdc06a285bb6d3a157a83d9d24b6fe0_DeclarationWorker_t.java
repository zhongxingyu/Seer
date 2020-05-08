 package com.sdc.util;
 
 import com.sdc.abstractLanguage.AbstractFrame;
 import com.sdc.abstractLanguage.AbstractMethod;
 import org.objectweb.asm.Opcodes;
 
 import java.util.*;
 
 public class DeclarationWorker {
     public enum SupportedLanguage {
         JAVA, JAVASCRIPT, KOTLIN
     }
 
     public static String getAccess(final int access, final SupportedLanguage language)
     {
         switch (language) {
             case JAVA:
             case JAVASCRIPT:
                 return getJavaAccess(access);
             case KOTLIN:
                 return getKotlinAccess(access);
         }
         return "";
     }
 
     public static String getJavaAccess(final int access) {
         StringBuilder sb = new StringBuilder("");
 
         if ((access & Opcodes.ACC_PUBLIC) != 0) {
             sb.append("public ");
         }
         if ((access & Opcodes.ACC_PRIVATE) != 0) {
             sb.append("private ");
         }
         if ((access & Opcodes.ACC_PROTECTED) != 0) {
             sb.append("protected ");
         }
         if ((access & Opcodes.ACC_FINAL) != 0) {
             sb.append("final ");
         }
         if ((access & Opcodes.ACC_STATIC) != 0) {
             sb.append("static ");
         }
         if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
             sb.append("synchronized ");
         }
         if ((access & Opcodes.ACC_VOLATILE) != 0) {
             sb.append("volatile ");
         }
         if ((access & Opcodes.ACC_TRANSIENT) != 0) {
             sb.append("transient ");
         }
         if ((access & Opcodes.ACC_ABSTRACT) != 0) {
             sb.append("abstract ");
         }
         if ((access & Opcodes.ACC_STRICT) != 0) {
             sb.append("strictfp ");
         }
         if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
             sb.append("synthetic ");
         }
         if ((access & Opcodes.ACC_ENUM) != 0) {
             sb.append("enum ");
         }
         if ((access & Opcodes.ACC_BRIDGE) != 0) {
             sb.append("bridge ");
         }
 
         return sb.toString();
     }
 
     public static String getKotlinAccess(final int access) {
         StringBuilder sb = new StringBuilder("");
 
         if ((access & Opcodes.ACC_PUBLIC) != 0) {
             sb.append("public ");
         }
         if ((access & Opcodes.ACC_PRIVATE) != 0) {
             sb.append("private ");
         }
         if ((access & Opcodes.ACC_PROTECTED) != 0) {
             sb.append("protected ");
         }
         if ((access & Opcodes.ACC_FINAL) == 0) {
             sb.append("open ");
         }
         if ((access & Opcodes.ACC_ABSTRACT) != 0) {
             sb.append("abstract ");
         }
         if ((access & Opcodes.ACC_BRIDGE) != 0) {
             sb.append("bridge ");
         }
 
         return sb.toString();
     }
 
     public static String getDescriptor(final String descriptor, final int pos, List<String> imports
             , final SupportedLanguage language)
     {
         String result = "";
         switch (language) {
             case JAVA:
             case JAVASCRIPT:
                 result = getJavaDescriptor(descriptor, pos, imports);
                 break;
             case KOTLIN:
                 result = getKotlinDescriptor(descriptor, pos, imports);
                 break;
         }
 
         result = replaceInnerClassName(result).trim();
         switch (language) {
             case JAVA:
             case JAVASCRIPT:
                 result = result + " ";
         }
 
         return result;
     }
 
     public static String getJavaDescriptor(final String descriptor, final int pos, List<String> imports) {
         switch (descriptor.charAt(pos)) {
             case 'V':
                 return "void";
             case 'B':
                 return "byte";
             case 'J':
                 return "long";
             case 'Z':
                 return "boolean";
             case 'I':
                 return "int";
             case 'S':
                 return "short";
             case 'C':
                 return "char";
             case 'F':
                 return "float";
             case 'D':
                 return "double";
             case 'L':
                 if (!descriptor.contains(";")) {
                     return "long";
                 }
                 if (descriptor.indexOf("<", pos) == -1 || descriptor.indexOf("<", pos) > descriptor.indexOf(";", pos)) {
                     return getSimpleClassName(descriptor, pos, imports);
                 } else {
                     return getClassNameWithGenerics(descriptor, pos, imports, SupportedLanguage.JAVA);
                 }
             case 'T':
                 return descriptor.substring(pos + 1, descriptor.indexOf(";", pos));
             case '+':
                 return "? extends " + getJavaDescriptor(descriptor, pos + 1, imports);
             case '-':
                 return "? super " + getJavaDescriptor(descriptor, pos + 1, imports);
             case '*':
                 return "?";
             case '[':
                 return getJavaDescriptor(descriptor, pos + 1, imports).trim() + "[]";
             default:
                 return "Object";
         }
     }
 
     public static String getKotlinDescriptor(final String descriptor, final int pos, List<String> imports) {
         switch (descriptor.charAt(pos)) {
             case 'V':
                 return "Unit";
             case 'B':
                 return "Byte";
             case 'J':
                 return "Long";
             case 'Z':
                 return "Boolean";
             case 'I':
                 return "Int";
             case 'S':
                 return "Short";
             case 'C':
                 return "Char";
             case 'F':
                 return "Float";
             case 'D':
                 return "Double";
             case 'L':
                 if (!descriptor.contains(";")) {
                     return "long";
                 }
                 if (descriptor.indexOf("<", pos) == -1 || descriptor.indexOf("<", pos) > descriptor.indexOf(";", pos)) {
                     final String actualClassName = getSimpleClassName(descriptor, pos, imports);
                     if (isPrimitiveClass(actualClassName)) {
                         return convertJavaPrimitiveClassToKotlin(actualClassName) + "?";
                     } else {
                         return actualClassName;
                     }
                 } else {
                     return convertJetFunctionRawType(getClassNameWithGenerics(descriptor, pos, imports, SupportedLanguage.KOTLIN));
                 }
             case 'T':
                 return descriptor.substring(pos + 1, descriptor.indexOf(";", pos));
             case '+':
                 return "out " + getKotlinDescriptor(descriptor, pos + 1, imports);
             case '-':
                 return "in " + getKotlinDescriptor(descriptor, pos + 1, imports);
             case '*':
                 return "? ";
             case '@':
                 return getKotlinDescriptor(descriptor, pos + 1, imports);
             case '[':
                 return "Array<" + getKotlinDescriptor(descriptor, pos + 1, imports) + ">";
             default:
                 return "Any";
         }
     }
 
     public static String getDescriptorByInt(final int desc, final SupportedLanguage language) {
         switch (language) {
             case JAVA:
             case JAVASCRIPT:
                 return getJavaDescriptorByInt(desc);
             case KOTLIN:
                 return getKotlinDescriptorByInt(desc);
             default:
                 return "";
         }
     }
 
     public static String getJavaDescriptorByInt(final int desc) {
         switch (desc) {
             case 1:
                 return "int ";
             case 2:
                 return "float ";
             case 3:
                 return "double ";
             case 4:
                 return "long ";
             default:
                 return "";
         }
     }
 
     public static String getKotlinDescriptorByInt(final int i) {
         switch (i) {
             case 1:
                 return "Int ";
             case 2:
                 return "Float ";
             case 3:
                 return "Double ";
             case 4:
                 return "Long ";
             default:
                 return "";
         }
     }
 
     public static int getParametersCount(final String descriptor) {
         int result = 0;
         int pos = 1;
         while (pos < descriptor.indexOf(")")) {
             pos = getNextTypePosition(descriptor, pos);
             result++;
         }
         return result;
     }
 
     public static void addInformationAboutParameters(final String descriptor, final AbstractMethod abstractMethod
             , final int startIndex, final SupportedLanguage language)
     {
         int count = startIndex - 1;
         int pos = 0;
        int index = count;
 
         AbstractFrame rootFrame = abstractMethod.getCurrentFrame();
        Map<String, Integer> typeNameIndices = new HashMap<String, Integer>();

         while (pos < descriptor.length()) {
             final int backupPos = pos;
             final int backupCount = count;
             final String type = getDescriptor(descriptor, backupPos, abstractMethod.getImports(), language);
 
             boolean isPrimitiveClass = false;
             switch (descriptor.charAt(pos)) {
                 case 'B':
                 case 'Z':
                 case 'I':
                 case 'S':
                 case 'C':
                 case 'F':
                 case 'T':
                 case '[':
                 case '+':
                 case '-':
                 case '*':
                     count++;
                     break;
                 case 'J':
                 case 'D':
                     count += 2;
                     break;
                 case 'L':
                     count++;
                     isPrimitiveClass = isPrimitiveClass(type);
                     break;
             }
 
             pos = getNextTypePosition(descriptor, pos);
            index = (count - backupCount) == 1 ? count : count - 1;
 
             //final String name = "x" + index;
 
             String variableType = type;
             String name = getNewTypeName(typeNameIndices, variableType);
 
             if (language == SupportedLanguage.KOTLIN) {
                 variableType = isPrimitiveClass ? convertJavaPrimitiveClassToKotlin(type) + "?" : type;
             }
 
             rootFrame.createAndInsertVariable(index, variableType, name);
         }
 
        rootFrame.setLastMethodParameterIndex(index);
 
        abstractMethod.setLastLocalVariableIndex(index);
     }
 
     private static String getNewTypeName(Map<String, Integer> typeNameIndices, String variableType) {
         char firstChar = variableType.charAt(0);
         String name = firstChar + "";
         Integer typeNameIndex;
         if (Character.isLowerCase(firstChar)) {
             //primitive type
             typeNameIndex = typeNameIndices.get(name);
 
         } else {
             //for Classes
             typeNameIndex = typeNameIndices.get(variableType);
             name = "a" + variableType.trim();
 
         }
         if (typeNameIndex == null) {
             typeNameIndices.put(name, 1);
         } else {
             name += typeNameIndex;
         }
         return name;
     }
 
     public static void parseGenericDeclaration(final String signature, List<String> genericTypesList,
                                                List<String> genericIdentifiersList, List<String> genericTypesImports, final SupportedLanguage language)
     {
         if (signature != null && signature.indexOf('<') == 0) {
             int endPos = 1;
             int startPos = 1;
             boolean isGenericName = true;
             while (signature.charAt(endPos) != '>') {
                 switch (signature.charAt(endPos)) {
                     case ':':
                         if (startPos != endPos) {
                             genericIdentifiersList.add(signature.substring(startPos, endPos));
                         }
                         endPos++;
                         startPos = endPos;
                         isGenericName = false;
                         break;
                     default:
                         if (!isGenericName) {
                             genericTypesList.add(getDescriptor(signature, endPos, genericTypesImports, language));
                             endPos = getNextTypePosition(signature, endPos);
                             startPos = endPos;
                             isGenericName = true;
                         } else {
                             endPos++;
                         }
                 }
             }
         }
     }
 
     public static boolean isPrimitiveClass(final String type) {
         Set<String> primitiveTypes = new HashSet<String>(Arrays.asList("Byte", "Long", "Boolean", "Integer", "Int", "Short", "Character", "Float", "Double", "Object"));
         return primitiveTypes.contains(type);
     }
 
     public static String convertJavaPrimitiveClassToKotlin(final String javaClass) {
         if (javaClass.equals("Integer")) {
             return "Int";
         } else if (javaClass.equals("Character")) {
             return "Char";
         } else if (javaClass.equals("Object")) {
             return "Any";
         }
         return javaClass;
     }
 
     public static String decompileClassNameWithOuterClasses(final String byteCodeFullClassName) {
         final String[] classPackageParts = byteCodeFullClassName.contains("/") ? byteCodeFullClassName.split("/") : new String[] { byteCodeFullClassName };
         final String actualClassName = classPackageParts[classPackageParts.length - 1];
 
         return replaceInnerClassName(actualClassName.replace("$", "."));
     }
 
     public static String decompileSimpleClassName(final String byteCodeFullClassName) {
         final String className = decompileClassNameWithOuterClasses(byteCodeFullClassName);
         final String[] innerClassesParts = className.contains(".") ? className.split("\\.") : new String[] { className };
 
         return innerClassesParts[innerClassesParts.length - 1];
     }
 
     public static String decompileFullClassName(final String byteCodeFullClassName) {
         return replaceInnerClassName(byteCodeFullClassName.replace("/", ".").replace("$", "."));
     }
 
     public static String decompileClassNameForImport(final String byteCodeFullClassName) {
         final String className = byteCodeFullClassName.replace("/", ".");
         final int innerClassStartIndex = className.indexOf("$");
 
         return replaceInnerClassName(innerClassStartIndex == -1 ? className : className.substring(0, innerClassStartIndex));
     }
 
     private static String getSimpleClassName(final String descriptor, final int pos, List<String> imports) {
         final String className = descriptor.substring(pos + 1, descriptor.indexOf(";", pos));
         imports.add(decompileFullClassName(className));
 
         return decompileClassNameWithOuterClasses(className);
     }
 
     private static String replaceInnerClassName(final String className) {
         final String result = convertInnerClassesToAcceptableName(className.trim());
         return className.contains(" ") ? result + " " : result;
     }
 
     private static String convertInnerClassesToAcceptableName(final String initialClassName) {
         final String[] classParts = initialClassName.contains(".") ? initialClassName.split("\\.") : new String[] { initialClassName };
         StringBuilder result = new StringBuilder("");
 
         for (final String classPart : classParts) {
             int pos = 0;
             while (pos < classPart.length() && Character.isDigit(classPart.charAt(pos))) {
                 pos++;
             }
             if (pos > 0) {
                 final String anonymousClassIdentifier = pos == classPart.length() ? "AnonymousClass" : "";
                 result = result.append(".").append(classPart.substring(pos)).append(anonymousClassIdentifier).append("__").append(classPart.substring(0, pos));
             } else {
                 result = result.append(".").append(classPart);
             }
         }
 
         return result.deleteCharAt(0).toString();
     }
 
     private static int getNextTypePosition(final String descriptor, final int startPos) {
         switch (descriptor.charAt(startPos)) {
             case 'B':
             case 'J':
             case 'Z':
             case 'I':
             case 'S':
             case 'C':
             case 'F':
             case 'D':
                 return startPos + 1;
             case 'L':
                 final int semicolonIndex = descriptor.indexOf(";", startPos);
                 final int bracketIndex = descriptor.indexOf("<", startPos);
                 if (bracketIndex == -1 || bracketIndex > semicolonIndex) {
                     return semicolonIndex + 1;
                 } else {
                     return skipGenericTypePart(descriptor, semicolonIndex) + 1;
                 }
             case 'T':
                 return descriptor.indexOf(";", startPos) + 1;
             case '[':
             case '+':
             case '-':
             case '*':
             default:
                 return getNextTypePosition(descriptor, startPos + 1);
         }
     }
 
     private static int skipGenericTypePart(final String descriptor, final int startPos) {
         Stack stack = new Stack();
         stack.push(0);
         int pos = startPos + 1;
         while (!stack.isEmpty()) {
             switch (descriptor.charAt(pos)) {
                 case '<':
                     stack.push(0);
                     break;
                 case '>':
                     stack.pop();
                     break;
             }
             pos++;
         }
         return pos;
     }
 
     private static String convertJetFunctionRawType(final String type) {
         final int prefixLength = "Function".length();
 
         if (type.startsWith("Function") && Character.isDigit(type.charAt(prefixLength))) {
             final int parametersCount = Integer.valueOf(type.substring(prefixLength, type.indexOf("<")));
 
             StringBuilder result = new StringBuilder("(");
             int beginPartPos = type.indexOf("<") + 1;
             int endPartPos = beginPartPos;
             List<String> parts = new ArrayList<String>();
 
             Stack stack = new Stack();
             while (endPartPos < type.length()) {
                 switch (type.charAt(endPartPos)) {
                     case '<':
                     case '(':
                         stack.push(0);
                         break;
                     case ')':
                         stack.pop();
                         break;
                     case '>':
                         if (type.charAt(endPartPos - 1) == '-') {
                             break;
                         } else if (endPartPos != type.length() - 1) {
                             stack.pop();
                             endPartPos++;
                             continue;
                         }
                     case ',':
                         if (stack.isEmpty()) {
                             parts.add(type.substring(beginPartPos, endPartPos));
                             endPartPos += 2;
                             beginPartPos = endPartPos;
                             continue;
                         }
                         break;
                 }
                 endPartPos++;
             }
 
             if (parametersCount >= 1) {
                 for (int i = 0; i < parametersCount - 1; i++) {
                     result = result.append(parts.get(i)).append(", ");
                 }
                 result = result.append(parts.get(parametersCount - 1));
             }
             result = result.append(") -> ").append(parts.get(parametersCount));
 
 //            return result.toString().replaceAll("([\\( ])(in|out) ", "$1");
             return result.toString();
         } else {
             return type;
         }
     }
 
     private static String getClassNameWithGenerics(final String descriptor, final int pos, List<String> imports, final SupportedLanguage language) {
         final String className = descriptor.substring(pos + 1, descriptor.indexOf("<", pos));
         imports.add(decompileFullClassName(className));
 
         StringBuilder result = new StringBuilder(decompileClassNameWithOuterClasses(className));
         result = result.append("<");
 
         final int lastClassNamePos = skipGenericTypePart(descriptor, descriptor.indexOf("<", pos));
         int curPos = pos + className.length() + 2;
 
         while (curPos < lastClassNamePos - 1) {
             final String genericType = getDescriptor(descriptor, curPos, imports, language).trim();
             result = result.append(genericType);
             curPos = getNextTypePosition(descriptor, curPos);
             if (curPos < lastClassNamePos - 1) {
                 result = result.append(", ");
             }
         }
         result = result.append(">");
 
         return result.toString();
     }
 }
