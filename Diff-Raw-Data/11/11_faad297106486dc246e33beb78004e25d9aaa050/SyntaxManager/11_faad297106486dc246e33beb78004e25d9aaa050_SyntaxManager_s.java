 package com.anychart.plugins.GoogleClosurePlugin.managers;
 
 import com.anychart.plugins.GoogleClosurePlugin.managers.syntax.SyntaxManagerResult;
 import com.intellij.openapi.editor.Editor;
 
 import java.util.ArrayList;
 
 /**
  * Author: Anton Saukh saukham@gmail.com
  * Date: 04.12.11
  * Time: 1:40
  */
 public class SyntaxManager {
     public SyntaxManager(ContentManager contentManager) {
         this.contentManager = contentManager;
     }
 
     private ContentManager contentManager;
 
     private boolean isPrivate;
     private boolean isProtected;
     private boolean isStatic;
 
     public SyntaxManagerResult parseInput(String inputString, Editor editor) throws Exception {
         inputString = inputString.trim();
         String[] firstDivision = inputString.split(" ", 2);
         firstDivision[0] = firstDivision[0].trim();
         isPrivate = firstDivision[0].equalsIgnoreCase("private") || firstDivision[0].equalsIgnoreCase("p");
         isProtected = firstDivision[0].equalsIgnoreCase("protected") || firstDivision[0].equalsIgnoreCase("prot");
         if (isPrivate || isProtected || firstDivision[0].equalsIgnoreCase("public") ||
                 firstDivision[0].equalsIgnoreCase("internal")) {
             if (firstDivision.length == 1)
                 return null;
                 //throw new Exception("Bad input string: expected something more than just \"" + inputString + "\"");
             firstDivision = firstDivision[1].split(" ", 2);
             firstDivision[0] = firstDivision[0].trim();
         }
         isStatic = firstDivision[0].equalsIgnoreCase("static") || firstDivision[0].equalsIgnoreCase("s");
         if (isStatic) {
             if (firstDivision.length == 1)
                 return null;
                 //throw new Exception("Bad input string: expected something more than just \"" + inputString + "\"");
             firstDivision = firstDivision[1].split(" ", 2);
             firstDivision[0] = firstDivision[0].trim();
         }
         if (firstDivision[0].equalsIgnoreCase("class") || firstDivision[0].equalsIgnoreCase("c")) {
             return parseClass(firstDivision, editor);
         } else if (firstDivision[0].equalsIgnoreCase("interface") || firstDivision[0].equalsIgnoreCase("i")) {
             return parseInterface(firstDivision, editor);
         } else if (firstDivision[0].equalsIgnoreCase("function") || firstDivision[0].equalsIgnoreCase("f")) {
             return parseFunction(firstDivision, editor);
         } else if (firstDivision[0].equalsIgnoreCase("var") || firstDivision[0].equalsIgnoreCase("v")) {
             return parseProp(firstDivision, editor);
         } else
             return null;
             //throw new Exception("Bad input string: expected class, interface, function or variable definition");
     }
 
     private SyntaxManagerResult parseClass(String[] firstDivision, Editor editor) throws Exception {
         if (firstDivision.length == 1)
             return null;
             //throw new Exception("Bad input string: expected something more than just \"" + firstDivision[0] + "\"");
         String className;
         String ancestor = null;
         String[] interfaces = null;
         
         String[] args = firstDivision[1].split("\\(", 2);
         
         String[] fastVariant = args[0].split(":", 3);
         if (fastVariant.length > 1){
             className = fastVariant[0].trim();
 
             
             if (fastVariant[1].length() > 0)
                 ancestor = fastVariant[1].trim();
 
             if (fastVariant.length == 3)
                 interfaces = fastVariant[2].split(",");
         } else {
             String[] components = args[0].split(" ", 2);
             className = components[0].trim();
             
             if (components.length > 1) {
                 components = components[1].split(" ", 2);
                 components[0] = components[0].trim();
                 if ((components[0].equalsIgnoreCase("extends") || components[0].equalsIgnoreCase("e")) && 
                         components.length > 1) {
                         //throw new Exception("Bad input string: class extension needs ancestor name");
                     components = components[1].split(" ", 2);
                     ancestor = components[0].trim();
                     if (components.length > 1)
                         components = components[1].split(" ", 2);
                     else
                         components = null;
                 }
                 if (components != null && components.length > 1 && 
                         (components[0].equalsIgnoreCase("implements") || components[0].equalsIgnoreCase("i"))) {
                     //if (components.length == 1)
                         //throw new Exception("Bad input string: class interface implementation needs interface name");
                     interfaces = components[1].split(",");
                     //components = null;
                 }
                 /*if (components != null)
                     return new SyntaxManagerResult(this.inputStringBackup);*/
                     /*if (ancestor != null)
                         throw new Exception("Bad input string: expected class extending or interface implementation");
                     else
                         throw new Exception("Bad input string: expected class interface implementation");*/
             }
         }
 
         ArrayList<String> paramNames = new ArrayList<String>();
         ArrayList<String> paramTypes = new ArrayList<String>();
         ArrayList<String> paramDefs = new ArrayList<String>();
 
         if (args.length > 1) {
             args = args[1].split(",");
             for (int i = 0; i < args.length - 1; i++) {
                 String[] tmp = args[i].split(":", 2);
                 String paramName = tmp[0].trim();
                 if (tmp.length > 1){
                     String[] paramTypeComponents = tmp[1].trim().split("=", 2);
                     String paramT = paramTypeComponents[0].trim();
                     Boolean optParam = paramName.startsWith("opt_");
                     Boolean optParamType = paramT.endsWith("=");
                     if (optParam && !optParamType)
                         paramT += "=";
                     else if (optParamType && !optParam)
                         paramName = "opt_" + paramName;
                     paramTypes.add(paramT);
                     paramDefs.add((paramTypeComponents.length > 1) ? paramTypeComponents[1].trim() : null);
                 } else  {
                     paramTypes.add("*");
                     paramDefs.add(null);
                 }
                 paramNames.add(paramName);
             }
 
             args = args[args.length - 1].split("\\)", 2);
             if (args[0].trim().length() > 0) {
                 String[] tmp = args[0].split(":", 2);
                 String paramName = tmp[0].trim();
                 if (tmp.length > 1){
                     String[] paramTypeComponents = tmp[1].trim().split("=", 2);
                     String paramT = paramTypeComponents[0].trim();
                     Boolean optParam = paramName.startsWith("opt_");
                     Boolean optParamType = paramT.endsWith("=");
                     if (optParam && !optParamType)
                         paramT += "=";
                     else if (optParamType && !optParam)
                         paramName = "opt_" + paramName;
                     paramTypes.add(paramT);
                     paramDefs.add((paramTypeComponents.length > 1) ? paramTypeComponents[1].trim() : null);
                 } else  {
                     paramTypes.add("*");
                     paramDefs.add(null);
                 }
                 paramNames.add(paramName);
             }
         }
 
         if (className.charAt(className.length() - 1) != '_') {
             if (isPrivate) className += '_';
         } else {
             if (!isPrivate) isPrivate = true;
         }
         
         ArrayList<String> requires = new ArrayList<String>();
         StringBuilder result = new StringBuilder();
         result.append("/**\n");
         result.append(" * ND: Needs doc!\n");
         result.append(" * @constructor\n");
         if (isPrivate)
             result.append(" * @private\n");
         if (ancestor != null) {
             result.append(" * @extends {");
             result.append(ancestor);
             result.append("}\n");
             requires.add(getClassNameSpace(ancestor));
         }
         if (interfaces != null)
             for (String anInterface : interfaces) {
                 result.append(" * @implements {");
                 result.append(anInterface.trim());
                 result.append("}\n");
                 requires.add(getClassNameSpace(anInterface.trim()));
             }
         result.append(" * \n");
         for (int i = 0, len = paramNames.size(); i < len; i++) {
             result.append(" * @param {");
             result.append(paramTypes.get(i));
             result.append("} ");
             result.append(paramNames.get(i));
             if (paramDefs.get(i) != null) {
                 result.append(" Default: ");
                 result.append(paramDefs.get(i));
             }
             result.append("\n");
         }
         result.append(" */\n");
         String namespace = contentManager.getNamespace(editor);
         result.append(namespace);
         result.append(".");
         result.append(className);
         result.append(" = function(");
         if (paramNames.size() > 0)
             result.append(paramNames.get(0));
         for (int i = 1, len = paramNames.size(); i < len; i++) {
             result.append(", ");
             result.append(paramNames.get(i));
         }
         if (ancestor != null) {
             result.append(") {\n    goog.base(this");
 
            for (int i = 0, len = paramNames.size(); i < len; i++) {
                 result.append(", ");
                result.append(paramNames.get(i));
             }
             
             result.append(");\n};\n");
             result.append("goog.inherits(");
             result.append(namespace);
             result.append(".");
             result.append(className);
             result.append(", ");
             result.append(ancestor);
             result.append(");\n");
         } else {
             result.append(") {};");
         }
         return new SyntaxManagerResult(requires.toArray(), result.toString());
     }
 
     private SyntaxManagerResult parseInterface(String[] firstDivision, Editor editor) throws Exception {
         if (firstDivision.length == 1)
             return null;
 //            throw new Exception("Bad input string: expected something more than just \"" + firstDivision[0] + "\"");
 
         String interfaceName;
         String[] interfaces = null;
 
         String[] fastVariant = firstDivision[1].split(":", 2);
 
         if (fastVariant.length == 2){
             interfaceName = fastVariant[0].trim();
             interfaces = fastVariant[1].split(",");
         } else {
             String[] components = firstDivision[1].split(" ", 2);
             interfaceName = components[0].trim();
 
             if (components.length > 1) {
                 components = components[1].split(" ", 2);
                 components[0] = components[0].trim();
                 if ((components[0].equalsIgnoreCase("extends") || components[0].equalsIgnoreCase("e")) &&
                         components.length > 1) {
                     interfaces = components[1].split(",");
                     //components = null;
                 }
                 /*if (components != null)
                     throw new Exception("Bad input string: expected interface extension");*/
             }
         }
         if (interfaceName.charAt(interfaceName.length() - 1) != '_') {
             if (isPrivate) interfaceName += '_';
         } else {
             if (!isPrivate) isPrivate = true;
         }
 
         ArrayList<String> requires = new ArrayList<String>();
         StringBuilder result = new StringBuilder();
         result.append("/**\n");
         result.append(" * ND: Needs doc!\n");
         result.append(" * @interface\n");
         if (isPrivate)
             result.append(" * @private\n");
         if (interfaces != null)
             for (String anInterface : interfaces) {
                 result.append(" * @extends {");
                 result.append(anInterface.trim());
                 result.append("}\n");
                 requires.add(getClassNameSpace(anInterface.trim()));
             }
         result.append(" */");
         result.append(contentManager.getNamespace(editor));
         result.append(".");
         result.append(interfaceName);
         result.append(" = function() {};\n");
         return new SyntaxManagerResult(requires.toArray(), result.toString());
     }
 
     private SyntaxManagerResult parseFunction(String[] firstDivision, Editor editor) throws Exception {
         if (firstDivision.length == 1)
             return null;
             //throw new Exception("Bad input string: expected something more than just \"" + firstDivision[0] + "\"");
         String[] components = firstDivision[1].split("\\(", 2);
         StringBuilder result;
 
         String functionName = components[0].trim();
         Boolean override = functionName.startsWith("@");
         if (override)
             functionName = functionName.substring(1);
         if (functionName.charAt(functionName.length() - 1) != '_') {
             if (isPrivate) functionName += '_';
         } else {
             if (!isPrivate) isPrivate = true;
         }
 
         ArrayList<String> paramNames = new ArrayList<String>();
         ArrayList<String> paramTypes = new ArrayList<String>();
         ArrayList<String> paramDefs = new ArrayList<String>();
         String functionType = "void";
         Boolean endingTypeDenoter = false;
 
         if (components.length > 1) {
             components = components[1].split(",");
             for (int i = 0; i < components.length - 1; i++) {
                 String[] tmp = components[i].split(":", 2);
                 String paramName = tmp[0].trim();
                 if (tmp.length > 1){
                     String[] paramTypeComponents = tmp[1].trim().split("=", 2);
                     String paramT = paramTypeComponents[0].trim();
                     Boolean optParam = paramName.startsWith("opt_");
                     Boolean optParamType = paramT.endsWith("=");
                     if (optParam && !optParamType)
                         paramT += "=";
                     else if (optParamType && !optParam)
                         paramName = "opt_" + paramName;
                     paramTypes.add(paramT);
                     paramDefs.add((paramTypeComponents.length > 1) ? paramTypeComponents[1].trim() : null);
                 } else  {
                     paramTypes.add("*");
                     paramDefs.add(null);
                 }
                 paramNames.add(paramName);
             }
 
             components = components[components.length - 1].split("\\)", 2);
             if (components[0].trim().length() > 0) {
                 String[] tmp = components[0].split(":", 2);
                 String paramName = tmp[0].trim();
                 if (tmp.length > 1){
                     String[] paramTypeComponents = tmp[1].trim().split("=", 2);
                     String paramT = paramTypeComponents[0].trim();
                     Boolean optParam = paramName.startsWith("opt_");
                     Boolean optParamType = paramT.endsWith("=");
                     if (optParam && !optParamType)
                         paramT += "=";
                     else if (optParamType && !optParam)
                         paramName = "opt_" + paramName;
                     paramTypes.add(paramT);
                     paramDefs.add((paramTypeComponents.length > 1) ? paramTypeComponents[1].trim() : null);
                 } else  {
                     paramTypes.add("*");
                     paramDefs.add(null);
                 }
                 paramNames.add(paramName);
             }
 
             if (components.length > 1){
                 components = components[1].split(":", 2);
 
                 if (components.length > 1)
                     functionType = components[1].trim();
 
                 Character tmp = functionType.charAt(functionType.length() - 1);
                 endingTypeDenoter = tmp == '{';
                 if (tmp == ';' || tmp == '{')
                     functionType = functionType.substring(0, functionType.length() - 1).trim();
             }
         }
 
         result = new StringBuilder();
         if (override)
             result.append("/** @inheritDoc */\n");
         else {
             result.append("/**\n");
             result.append(" * ND: Needs doc!\n");
             if (isPrivate)
                 result.append(" * @private\n");
             else if (isProtected)
                 result.append(" * @protected\n");
             for (int i = 0, len = paramNames.size(); i < len; i++) {
                 result.append(" * @param {");
                 result.append(paramTypes.get(i));
                 result.append("} ");
                 result.append(paramNames.get(i));
                 if (paramDefs.get(i) != null) {
                     result.append(" Default: ");
                     result.append(paramDefs.get(i));
                 }
                 result.append("\n");
             }
             if (!functionType.equalsIgnoreCase("void")) {
                 result.append(" * @return {");
                 result.append(functionType);
                 result.append("}\n");
             }
             result.append(" */\n");
         }
 
         if (isStatic) {
             String tmp = contentManager.getClassName(editor);
             if (tmp == null)
                 tmp = contentManager.getNamespace(editor);
             result.append(tmp);
             result.append(".");
         } else {
             result.append(contentManager.getClassName(editor));
             result.append(".prototype.");
         }
         result.append(functionName);
         result.append(" = function(");
         for (int i = 0, len = paramNames.size(); i < len; i++) {
             result.append(paramNames.get(i));
             if (i < len - 1)
                 result.append(", ");
         }
         result.append(") {");
         if (!endingTypeDenoter) {
             if (override) {
                 result.append("\n    goog.base(this, '");
                 result.append(functionName);
                 result.append("'");
                for (int i = 0, len = paramNames.size(); i < len; i++) {
                     result.append(", ");
                    result.append(paramNames.get(i));
                 }
                 result.append(");\n};\n");
             } else
                 result.append("};\n");
         }
         return new SyntaxManagerResult(null, result.toString());
     }
 
     private SyntaxManagerResult parseProp(String[] firstDivision, Editor editor) throws Exception {
         if (firstDivision.length == 1)
             return null;
             //throw new Exception("Bad input string: expected something more than just \"" + firstDivision[0] + "\"");
         String[] components = firstDivision[1].split(":", 2);
         String propName = components[0].trim();
         if (propName.charAt(propName.length() - 1) != '_') {
             if (isPrivate) propName += '_';
         } else {
             if (!isPrivate) isPrivate = true;
         }
         String propType = (components.length > 1) ? components[1].trim() : "*";
         if (propType.charAt(propType.length() - 1) == ';')
             propType = propType.substring(0, propType.length() - 1).trim();
         StringBuilder result = new StringBuilder();
         result.append("/**\n");
         result.append(" * ND: Needs doc!\n");
         if (isPrivate)
             result.append(" * @private\n");
         else if (isProtected)
             result.append(" * @protected\n");
         result.append(" * @type {");
         result.append(propType);
         result.append("}\n */\n");
         
         if (isStatic) {
             String tmp = contentManager.getClassName(editor);
             if (tmp == null)
                 tmp = contentManager.getNamespace(editor);
             result.append(tmp);
             result.append(".");
         } else {
             result.append(contentManager.getClassName(editor));
             result.append(".prototype.");
         }
         result.append(propName);
         result.append(" = null;\n");
         return new SyntaxManagerResult(null, result.toString());
     }
 
     private String getClassNameSpace(String className) {
         String[] temp = className.split(".");
         StringBuilder res = new StringBuilder();
         for (int i = 0; i < temp.length - 2; i++) {
             res.append(temp[i]);
             res.append(".");
         }
         return res.toString();
     }
 }
