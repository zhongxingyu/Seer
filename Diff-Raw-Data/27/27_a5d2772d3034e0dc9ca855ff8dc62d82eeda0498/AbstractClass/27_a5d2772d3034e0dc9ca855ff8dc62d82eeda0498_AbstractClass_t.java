 package com.sdc.abstractLanguage;
 
 import com.sdc.ast.expressions.Expression;
 import com.sdc.util.DeclarationWorker;
 
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
     protected final String myName;
     protected String myFullClassName;
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
     protected InnerClassIdentifier myInnerClassIdentifier;
 
     protected Map<String, Exception> myInnerClassesErrors = new HashMap<String, Exception>();
 
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
 
     public void setFullClassName(final String fullClassName) {
         this.myFullClassName = fullClassName;
     }
 
     public String getFullClassName() {
         return myFullClassName;
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
         if (!hasImport(importName) && !checkImportNameForBeingInPackages(importName, myDefaultPackages)) {
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
 
    public boolean hasFieldInitializer(final String fieldName) {
        return getField(fieldName).getInitializer() != null;
    }

     public List<String> getGenericDeclaration() {
         List<String> result = new ArrayList<String>();
         for (int i = 0; i < myGenericTypes.size(); i++) {
             final String genericType = myGenericTypes.get(i);
             if (!genericType.equals("Object ") && !genericType.equals("Any?")) {
                 result.add(myGenericIdentifiers.get(i) + " " + getInheritanceIdentifier() + " " + genericType.trim());
             } else {
                 result.add(myGenericIdentifiers.get(i));
             }
         }
         return result;
     }
 
     public void addAnonymousClass(final String className, final AbstractClass decompiledClass) {
         myAnonymousClasses.put(className, decompiledClass);
     }
 
     public void addInnerClass(final String innerClassName, final AbstractClass decompiledClass) {
         myInnerClasses.put(innerClassName, decompiledClass);
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
             final InnerClassIdentifier innerClassIdentifier = innerClass.getValue().getInnerClassIdentifier();
             if (innerClassIdentifier.getName() == null && innerClassIdentifier.getOwner().equals(myName)) {
                 result.add(innerClass.getValue());
             }
         }
         return result;
     }
 
     public boolean hasAnonymousClass(final String name) {
         return myAnonymousClasses.containsKey(name);
     }
 
     public AbstractClass getAnonymousClass(final String name) {
         return myAnonymousClasses.get(name);
     }
 
     public void addInnerClassError(final String className, final Exception exception) {
         myInnerClassesErrors.put(className, exception);
     }
 
     public Map<String, Exception> getInnerClassesErrors() {
         return myInnerClassesErrors;
     }
 
     protected boolean checkImportNameForBeingInPackage(final String importName, final String packageName) {
         return importName.indexOf(packageName) == 0 && importName.lastIndexOf(".") == packageName.length();
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
 
     public String getClassName(final String fullClassName) {
         return DeclarationWorker.getClassName(fullClassName);
     }
 
     public String getDescriptor(final String descriptor, final int pos, List<String> imports
             , final DeclarationWorker.SupportedLanguage language)
     {
         return DeclarationWorker.getDescriptor(descriptor, pos, imports, language);
     }
 }
