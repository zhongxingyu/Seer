 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JExpression;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.BlacklistRule;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorJobConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.WhitelistRule;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jan Hýbl
  * Date: 10.2.12
  * Time: 13:59
  */
 public abstract class Generator implements IAPIVisitor {
 
     protected GeneratorConfiguration configuration;
     protected GeneratorJobConfiguration jobConfiguration;
 
     protected JCodeModel cm;
     private Map<String, JClass> classMap;
 
     private API currentAPI;
 
     // defines package name for currently generated package content
     protected String currentPackageName;
 
     public Generator(GeneratorConfiguration configuration) {
         this.configuration = configuration;
     }
 
     public void generate(API api, GeneratorJobConfiguration job) {
         jobConfiguration = job;
         cm = new JCodeModel();
         classMap = new HashMap<>();
         currentAPI = api;
         // create directory for package
         File outputDir = new File(jobConfiguration.getOutputDir());
         if (!outputDir.exists()) outputDir.mkdirs();
         visit(api);
         try {
             cm.build(outputDir);
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     /**
      * Process all packages in given API.
      *
      * @param api
      */
     @Override
     public void visit(API api) {
         for (APIPackage pkg : api.getPackages()) {
             pkg.accept(this);
         }
     }
 
     /**
      * For every original package from API creates new test package where test classes will be placed.
      * New packages are named by original name of package and configuration string from jobConfiguration field.
      * After visiting all classes in the package, directory for the package is created and classes from cuBuffer are generated.
      *
      * @param apiPackage
      */
     @Override
     public void visit(APIPackage apiPackage) {
         currentPackageName = jobConfiguration.getOutputPackage().replaceAll("%s", apiPackage.getName());
         if (!isEnabled(apiPackage.getName(), WhitelistRule.RuleItem.ALL) || (jobConfiguration.isSkipDeprecated() && apiPackage.isDepreacated())) {
             return;
         }
         for (APIClass cls : apiPackage.getClasses()) {
             cls.accept(this);
         }
 
 
     }
 
     /**
      * Can be used for generating names of methods and classes. Replace "%s" sequence with {@code originalName}.
      * If original class is generic, method remove generics from class name.
      *
      * @param pattern      All "%s" sequences will be replaced with originalName
      * @param originalName Original name of class/method.
      * @return new name
      */
     protected static String generateName(String pattern, String originalName) {
         try {
             // escape $ char.
             originalName = originalName.replaceAll("\\$", "\\\\\\$");
             if (originalName.contains("<") && originalName.contains(">")) {
                 String name = originalName.substring(0, originalName.indexOf("<"));
                 return pattern.replaceAll("%s", simplifyName(name));
             } else {
                 return pattern.replaceAll("%s", simplifyName(originalName));
             }
         } catch (IllegalArgumentException e) {
             System.err.printf("Can't generate name using pattern \"%s\" and original name \"%s\".", pattern, originalName);
         }
         return null;
     }
 
     /**
      * Check if given item is enabled for generating in white and black lists in jobConfiguration.
      *
      * @param itemSignature Unique string that can be used for identification of the item. For class it's
      *                      full class name (with package name), for methods it's string containing className,
      *                      methodName, parameters and return type.
      * @param target
      * @return
      */
     protected boolean isEnabled(String itemSignature, WhitelistRule.RuleItem target) {
         boolean enabled = false;
         if (jobConfiguration.getWhitelistRules().size() > 0) {
             for (WhitelistRule rule : jobConfiguration.getWhitelistRules()) {
                 if ((rule.getRule().contains(itemSignature) || (itemSignature.contains(rule.getRule())))
                         && (rule.getItem().equals(target) || rule.getItem().equals(WhitelistRule.RuleItem.ALL)) || target.equals(WhitelistRule.RuleItem.ALL)) {
                     enabled = true;
                     break;
                 }
             }
         } else enabled = true;  // if there is no rules for whitelist, enable all items.
 
         for (BlacklistRule rule : jobConfiguration.getBlacklistRules()) {
            if ((rule.getRule().equals(itemSignature) || (itemSignature.contains(rule.getRule()))) && (rule.getItem().equals(target) || rule.getItem().equals(WhitelistRule.RuleItem.ALL))) {
                 enabled = false;    // disable item if there is blacklist rule for it.
                 break;
             }
         }
 
         return enabled;
     }
 
     /**
      * Generates unique identification string for method. Used for identification in blacklist and whitelist
      * in configuration.
      *
      * @param method
      * @param className
      * @return
      */
     protected String methodSignature(APIMethod method, String className) {
         StringBuilder sb = new StringBuilder();
         sb.append(className).append(".");
         if (method.getName().contains("."))
             sb.append(method.getName().substring(method.getName().lastIndexOf(".") + 1));
         else sb.append(method.getName());
         sb.append('(');
         // list of Params
         Iterator<APIMethodParameter> iter = method.getParameters().iterator();
         if (iter.hasNext()) {
             sb.append(iter.next().getType());
             while (iter.hasNext()) {
                 sb.append(", ");
                 sb.append(iter.next().getType());
             }
         }
         sb.append(')');
         String result = sb.toString();
         return sb.toString();
     }
 
     /**
      * Returns default value for given type.
      *
      * @param name
      * @return
      */
     public JExpression getPrimitiveValue(String name) {
         if (name.equals("byte")) return JExpr.cast(cm.BYTE, JExpr.lit(0));
         if (name.equals("short")) return JExpr.cast(cm.SHORT, JExpr.lit(0));
         if (name.equals("int")) return JExpr.lit(0);
         if (name.equals("long")) return JExpr.lit(0L);
         if (name.equals("float")) return JExpr.lit(0.0F);
         if (name.equals("double")) return JExpr.lit(0.0D);
         if (name.equals("boolean")) return JExpr.lit(false);
         if (name.equals("char")) return JExpr.lit('a');
         return JExpr._null();
     }
 
     /**
      * Returns default value for given type.
      *
      * @param type
      * @return
      */
     private JExpression getPrimitiveValue(APIType type) {
         if (type.getName().equals("byte")) return JExpr.cast(cm.BYTE, JExpr.lit(0));
         if (type.getName().equals("short")) return JExpr.cast(cm.SHORT, JExpr.lit(0));
         if (type.getName().equals("int")) return JExpr.lit(0);
         if (type.getName().equals("long")) return JExpr.lit(0L);
         if (type.getName().equals("float")) return JExpr.lit(0.0F);
         if (type.getName().equals("double")) return JExpr.lit(0.0D);
         if (type.getName().equals("boolean")) return JExpr.lit(false);
         if (type.getName().equals("char")) return JExpr.lit('a');
         return JExpr._null();
     }
 
     /**
      * Returns default value for given type.
      *
      * @param name
      * @return
      */
     public static String getPrimitiveValueString(String name) {
         if (name.equals("byte")) return "0";
         if (name.equals("short")) return "0";
         if (name.equals("int")) return "0";
         if (name.equals("long")) return "0";
         if (name.equals("float")) return "0.0";
         if (name.equals("double")) return "0.0";
         if (name.equals("boolean")) return "false";
         if (name.equals("char")) return "a";
         return "null";
     }
 
     @Deprecated
     protected JClass getClassRef(String className) {
 
         try {
             APIClass cls = currentAPI.findClass(className);
             if (cls.getModifiers().contains(APIModifier.PROTECTED)) {
                 className = className.substring(className.lastIndexOf('.') + 1);
             }
             if (cls.isNested()) {
                 className.replace('$', '.');
             }
         } catch (ClassNotFoundException e) {
         }
 
         // check if it's array
         if (className.endsWith("[]")) {
             return getClassRef(className.substring(0, className.length() - 2)).array();
         }
 
         if (className.contains("<")) {
             className = className.substring(0, className.indexOf("<"));
         }
         if (classMap.containsKey(className)) {
             return classMap.get(className);
         } else {
             JClass classReference = cm.ref(className);
             classMap.put(className, classReference);
             return classReference;
         }
     }
 
     @Deprecated
     protected JClass getGenericsClassRef(String className) {
 
         try {
             APIClass cls = currentAPI.findClass(className);
             //System.out.println("Class found: " + className);
             if (cls.getModifiers().contains(APIModifier.PROTECTED)) {
                 className = className.substring(className.lastIndexOf('.') + 1);
             }
         } catch (ClassNotFoundException e) {
             //System.err.println("Class not found: " + className);
         }
 
         // check if it's arra<y
         if (className.endsWith("[]")) {
             return getClassRef(className.substring(0, className.length() - 2)).array();
         }
 
         if (classMap.containsKey(className)) {
             return classMap.get(className);
         } else {
             JClass classReference = cm.ref(className);
             classMap.put(className, classReference);
             return classReference;
         }
     }
 
     @Deprecated
     protected JClass getGenericsClassRef(String className, List<String> typeParams) {
         if (className.endsWith("[]")) {
             return getGenericsClassRef(className.substring(0, className.length() - 2), typeParams).array();
         }
 
         if (classMap.containsKey(className)) {
             return classMap.get(className);
         } else {
             JClass classReference = cm.ref(className);
             classMap.put(className, classReference);
             return classReference;
         }
     }
 
     /**
      * Search current API for class with given name.
      *
      * @param name full class name
      * @return class info
      * @throws ClassNotFoundException if class isn't part of API
      */
     protected APIClass findClass(String name) throws ClassNotFoundException {
         return currentAPI.findClass(name);
     }
 
     /**
      * Search current API for class with given name.
      *
      * @param type full class name
      * @return class info
      * @throws ClassNotFoundException if class isn't part of API
      */
     protected APIClass findClass(APIType type) throws ClassNotFoundException {
         return currentAPI.findClass(type);
     }
 
     public static String simplifyName(String originalName) {
         if (originalName.contains(".")) return originalName.substring(originalName.lastIndexOf(".") + 1);
         return originalName;
     }
 
     public static Set<String> getTypesList(String typeString) {
         Set<String> result = new HashSet<>();
         typeString = typeString.replaceAll(">", "");
         typeString = typeString.replaceAll("\\[", "");
         typeString = typeString.replaceAll("\\]", "");
         typeString = typeString.replaceAll("<", ",");
         typeString = typeString.replaceAll("extends", ",");
         typeString = typeString.replaceAll("super", ",");
         String[] s1 = typeString.split(",");
         for (String str1 : s1) {
             String cleanName = str1.trim();
             if (cleanName.length() > 0) result.add(cleanName);
         }
         return result;
     }
 }
