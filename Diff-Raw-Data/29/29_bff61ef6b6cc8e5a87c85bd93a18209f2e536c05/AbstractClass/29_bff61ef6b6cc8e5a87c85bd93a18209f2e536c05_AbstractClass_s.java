 package com.sdc.abstractLanguage;
 
 import com.sdc.ast.expressions.Expression;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class AbstractClass {
     public class InnerClassIdentifier {
         private final String myOwner;
         private final String myName;
         private final String myDescriptor;
 
         public InnerClassIdentifier(final String owner, final String name, final String descriptor) {
             this.myOwner = owner;
             this.myName = name;
             this.myDescriptor = descriptor;
         }
 
         public String getOwner() {
             return myOwner;
         }
 
         public String getName() {
             return myName;
         }
 
         public String getDescriptor() {
             return myDescriptor;
         }
     }
 
     protected final String myModifier;
     protected final String myType;
     protected String myName;
     protected final String myPackage;
 
     protected final String mySuperClass;
     protected final List<String> myImplementedInterfaces;
 
     protected final List<String> myGenericTypes;
     protected final List<String> myGenericIdentifiers;
 
     protected List<AbstractClassField> myFields = new ArrayList<AbstractClassField>();
     protected List<AbstractMethod> myMethods = new ArrayList<AbstractMethod>();
 
     protected List<AbstractAnnotation> myAnnotations = new ArrayList<AbstractAnnotation>();
 
     protected List<String> myImports = new ArrayList<String>();
 
     protected List<String> myDefaultPackages = new ArrayList<String>();
 
     protected boolean myIsNormalClass = true;
     protected boolean myIsLambdaFunctionClass = false;
     protected boolean myIsNestedClass = false;
 
     protected Map<String, AbstractClass> myAnonymousClasses = new HashMap<String, AbstractClass>();
     protected Map<String, AbstractClass> myInnerClasses = new HashMap<String, AbstractClass>();
     protected Map<String, String> myInnerClassNames = new HashMap<String, String>();
     protected InnerClassIdentifier myInnerClassIdentifier;
 
     protected final int myTextWidth;
     protected final int myNestSize;
 
     public AbstractClass(final String modifier, final String type, final String name, final String packageName,
             final List<String> implementedInterfaces, final String superClass,
             final List<String> genericTypes, final List<String> genericIdentifiers,
             final int textWidth, final int nestSize)
     {
         this.myModifier = modifier;
         this.myType = type;
         this.myName = name;
         this.myPackage = packageName;
         this.myImplementedInterfaces = implementedInterfaces;
         this.mySuperClass = superClass;
         this.myGenericTypes = genericTypes;
         this.myGenericIdentifiers = genericIdentifiers;
         this.myTextWidth = textWidth;
         this.myNestSize = nestSize;
     }
 
     protected abstract String getInheritanceIdentifier();
 
     public String getModifier() {
         return myModifier;
     }
 
     public String getType() {
         return myType;
     }
 
     public String getName() {
         return myName;
     }
 
     public String getPackage() {
         return myPackage;
     }
 
     public List<String> getImports() {
         return myImports;
     }
 
     public List<String> getImplementedInterfaces() {
         return myImplementedInterfaces;
     }
 
     public String getSuperClass() {
         return mySuperClass;
     }
 
     public List<AbstractClassField> getFields() {
         return myFields;
     }
 
     public List<AbstractMethod> getMethods() {
         return myMethods;
     }
 
     public int getNestSize() {
         return myNestSize;
     }
 
     public int getTextWidth() {
         return myTextWidth;
     }
 
     public void setIsNormalClass(final boolean isNormalClass) {
         this.myIsNormalClass = isNormalClass;
     }
 
     public boolean isNormalClass() {
         return myIsNormalClass;
     }
 
     public void setIsLambdaFunctionClass(final boolean isLambdaFunctionClass) {
         this.myIsLambdaFunctionClass = isLambdaFunctionClass;
     }
 
     public void setIsNestedClass(final boolean isNestedClass) {
         this.myIsNestedClass = isNestedClass;
     }
 
     public boolean isNestedClass() {
         return myIsNestedClass;
     }
 
     public boolean isLambdaFunctionClass() {
         return myIsLambdaFunctionClass;
     }
 
     public void appendField(final AbstractClassField field) {
         myFields.add(field);
     }
 
     public void appendMethod(final AbstractMethod method) {
         myMethods.add(method);
     }
 
     public void appendImports(final List<String> imports) {
         for (final String importName : imports) {
             appendImport(importName);
         }
     }
 
     public void appendImport(final String importName) {
         if (!hasImport(importName) && checkImportNameForBeingInPackages(importName, myDefaultPackages)) {
             myImports.add(importName);
         }
     }
 
     public void appendAnnotation(final AbstractAnnotation annotation) {
         myAnnotations.add(annotation);
     }
 
     public List<AbstractAnnotation> getAnnotations() {
         return myAnnotations;
     }
 
     public boolean isGenericType(final String className) {
         return myGenericTypes.contains(className);
     }
 
     public String getGenericIdentifier(final String className) {
         return myGenericIdentifiers.get(myGenericTypes.indexOf(className));
     }
 
     public void addInitializerToField(final String fieldName, final Expression initializer) {
         getField(fieldName).setInitializer(initializer);
     }
 
     public AbstractClassField getField(final String fieldName) {
         for (AbstractClassField field : myFields) {
             if (field.getName().equals(fieldName)) {
                 return field;
             }
         }
         return null;
     }
 
     public boolean hasField(final String fieldName) {
         for (AbstractClassField field : myFields) {
             if (field.getName().equalsIgnoreCase(fieldName)) {
                 return true;
             }
         }
         return false;
     }
 
     public List<String> getGenericDeclaration() {
         List<String> result = new ArrayList<String>();
         for (int i = 0; i < myGenericTypes.size(); i++) {
             if (!myGenericTypes.get(i).equals("java/lang/Object")) {
                 final String[] classParts = myGenericTypes.get(i).split("/");
                 result.add(myGenericIdentifiers.get(i) + " " + getInheritanceIdentifier() + " " + classParts[classParts.length - 1]);
             } else {
                 result.add(myGenericIdentifiers.get(i));
             }
         }
         return result;
     }
 
     public void addAnonymousClass(final String className, final AbstractClass decompiledClass) {
         myAnonymousClasses.put(className, decompiledClass);
     }
 
     public void addInnerClass(final String innerClassNameWithDollars, final AbstractClass decompiledClass) {
         myInnerClasses.put(innerClassNameWithDollars, decompiledClass);
     }
 
     public void setInnerClassIdentifier(final String owner, final String name, final String descriptor) {
         myInnerClassIdentifier = new InnerClassIdentifier(owner, name, descriptor);
     }
 
     public InnerClassIdentifier getInnerClassIdentifier() {
         return myInnerClassIdentifier;
     }
 
     public List<AbstractClass> getMethodInnerClasses(final String methodName, final String descriptor) {
         List<AbstractClass> result = new ArrayList<AbstractClass>();
         for (final Map.Entry<String, AbstractClass> innerClass : myInnerClasses.entrySet()) {
             final String name = innerClass.getValue().getInnerClassIdentifier().getName();
             final String desc = innerClass.getValue().getInnerClassIdentifier().getDescriptor();
 
             if (name != null && name.equals(methodName) && desc.equals(descriptor)) {
                 result.add(innerClass.getValue());
             }
         }
         return result;
     }
 
     public List<AbstractClass> getClassBodyInnerClasses() {
         List<AbstractClass> result = new ArrayList<AbstractClass>();
         for (final Map.Entry<String, AbstractClass> innerClass : myInnerClasses.entrySet()) {
             if (innerClass.getValue().getInnerClassIdentifier().getName() == null) {
                 result.add(innerClass.getValue());
             }
         }
         return result;
     }
 
     public void setName(final String name) {
         this.myName = name;
     }
 
    public boolean checkForInnerClass(final String name) {
         for (final String innerClass : myInnerClassNames.keySet()) {
             if (innerClass.equals(name)) {
                 return true;
             }
         }
         return false;
     }
 
     public String getInnerClassName(final String classNameWithDollars) {
         return myInnerClassNames.get(classNameWithDollars);
     }
 
     public void addInnerClassName(final String classNameWithDollars, final String className) {
         final int startIndex = classNameWithDollars.lastIndexOf("$");
         final int endIndex = classNameWithDollars.length() - className.length();
 
         final String realClassName = endIndex - startIndex > 1 ? className + classNameWithDollars.substring(startIndex, endIndex) : className;
         myInnerClassNames.put(classNameWithDollars, realClassName);
     }
 
     protected boolean checkImportNameForBeingInPackage(final String importName, final String packageName) {
         return importName.indexOf(packageName) == 0 && importName.lastIndexOf(".") == packageName.length() + 1;
     }
 
     protected boolean checkImportNameForBeingInPackages(final String importName, final List<String> packageNames) {
         boolean result = false;
         for (final String packageName : packageNames) {
             result |= checkImportNameForBeingInPackage(importName, packageName);
         }
         return result;
     }
 
     protected boolean hasImport(final String importName) {
         return myImports.contains(importName);
     }
 }
