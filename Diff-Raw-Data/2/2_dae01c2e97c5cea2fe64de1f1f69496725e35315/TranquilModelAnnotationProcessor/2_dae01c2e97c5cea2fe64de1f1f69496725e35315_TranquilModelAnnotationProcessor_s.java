 package fi.tranquil;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import javax.lang.model.util.ElementFilter;
 import javax.tools.Diagnostic.Kind;
 
 import org.apache.commons.lang3.StringUtils;
 
 @SupportedAnnotationTypes("*")
 @SupportedSourceVersion(SourceVersion.RELEASE_7)
 public class TranquilModelAnnotationProcessor extends AbstractProcessor {
   
   private Collection<? extends TypeElement> typeElements;
   
   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
     if (round == 0) {
       try {
         typeElements = ElementFilter.typesIn(roundEnv.getRootElements());
   
         // Initialize class lookup property objects
         
         baseClasses = new HashMap<String, String>();
         compactClasses = new HashMap<String, String>();
         completeClasses = new HashMap<String, String>();
         
         // TODO: Should user be able to rename these classes?
 
         ModelClass baseLookup = new ModelClass("fi.tranquil", "BaseLookup");
         ModelClass compactLookup = new ModelClass("fi.tranquil", "CompactLookup");
         ModelClass completeLookup = new ModelClass("fi.tranquil", "CompleteLookup");
         
         // Process entities
         
         for (TypeElement type : typeElements) {
           processEntities(type);
         } 
         
         // Add lookup properties
         
        ModelMethod lookupFindMethod = new ModelMethod("public", "Class<?>", "findTranquileModel", "Class<?> entity", "    return classes.get(entity);");
         lookupFindMethod.addAnnotation("@Override");
         
         baseLookup.addInterface("fi.tranquil.processing.EntityLookup");
         baseLookup.addConstructor("public", constructLookupConstructor(baseClasses), null);
         baseLookup.addProperty("private", "java.util.Map<Class<?>, Class<?>>", "classes", " new java.util.HashMap<Class<?>, Class<?>>()", false, false);
         baseLookup.addMethod(lookupFindMethod);
         
         compactLookup.addInterface("fi.tranquil.processing.EntityLookup");
         compactLookup.addConstructor("public", constructLookupConstructor(compactClasses), null);
         compactLookup.addProperty("private", "java.util.Map<Class<?>, Class<?>>", "classes", " new java.util.HashMap<Class<?>, Class<?>>()", false, false);
         compactLookup.addMethod(lookupFindMethod);
         
         completeLookup.addInterface("fi.tranquil.processing.EntityLookup");
         completeLookup.addConstructor("public", constructLookupConstructor(completeClasses), null);
         completeLookup.addProperty("private", "java.util.Map<Class<?>, Class<?>>", "classes", " new java.util.HashMap<Class<?>, Class<?>>()", false, false);
         completeLookup.addMethod(lookupFindMethod);
         
         // Write lookup classes
         
         classWriter.writeClass(processingEnv.getFiler().createSourceFile("fi.tranquil.BaseLookup"), baseLookup);
         classWriter.writeClass(processingEnv.getFiler().createSourceFile("fi.tranquil.CompactLookup"), compactLookup);
         classWriter.writeClass(processingEnv.getFiler().createSourceFile("fi.tranquil.CompleteLookup"), completeLookup);
       } catch (IOException e) {
         processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
       }
     }
     
     round++;
 
     return false;
   }
 
   private String constructLookupConstructor(Map<String, String> classes) {
     StringBuilder bodyBuilder = new StringBuilder();
     boolean first = true;
 
     for (String entityClass : classes.keySet()) {
       String tranquilClass = classes.get(entityClass);
       
       if (!first) {
         bodyBuilder.append('\n');
       }
       
       bodyBuilder
         .append("    classes.put(")
         .append(entityClass)
         .append(".class, ")
         .append(tranquilClass)
         .append(".class);");
       first = false;
     }
     
     return bodyBuilder.toString();
   }
   
   private void note(String msg) {
     processingEnv.getMessager().printMessage(Kind.NOTE, msg);
   }
 
   private void processEntities(TypeElement type) throws IOException {
     if (isEntity(type)) {
       processEntity(type);
     }
   }
   
   /**
    * Resolves class hierarchy. If typeElement is interface all extended interfaces will be returned and 
    * if the typeElement is class all super classes will be returned
    * 
    * @param typeElement class or interface of which class hierarchy is to be resolved 
    * @return class hierarchy
    */
   private List<TypeElement> resolveClassTree(TypeElement typeElement) {
     List<TypeElement> classTree = new ArrayList<>();
     
     classTree.add(typeElement);
     
     if (typeElement.getKind() == ElementKind.INTERFACE) {
       for (TypeMirror superInterfaceMirror : typeElement.getInterfaces()) {
         DeclaredType superInterfaceDeclaredType = (DeclaredType) (superInterfaceMirror.getKind() == TypeKind.DECLARED ? superInterfaceMirror : null);
         if (superInterfaceDeclaredType != null) {
           TypeElement superInterfaceElement = (TypeElement) superInterfaceDeclaredType.asElement();
           classTree.addAll(resolveClassTree(superInterfaceElement));
         }
       };
     } else if (typeElement.getKind() == ElementKind.CLASS) {
       TypeMirror superclassTypeMirror = typeElement.getSuperclass();
       while ((superclassTypeMirror != null) && (superclassTypeMirror.getKind() != TypeKind.NONE)) {
         DeclaredType superclassDeclaredType = (DeclaredType) (superclassTypeMirror.getKind() == TypeKind.DECLARED ? superclassTypeMirror : null);
         if (superclassDeclaredType != null) {
           TypeElement superclassTypeElement = (TypeElement) superclassDeclaredType.asElement();
           if (superclassTypeElement.getSuperclass().getKind() != TypeKind.NONE) {
             classTree.add(superclassTypeElement);
           }
           
           superclassTypeMirror = superclassTypeElement.getSuperclass();
         }
       }
     }
     
     return classTree;
   }
   
   /**
    * Processes single entity
    * 
    * @param entity entity to be processed.
    * @throws IOException when class files could not be written.
    */
   private void processEntity(TypeElement entity) throws IOException {
     note("");
     note("-------------------------------------------------------------");
     note("Processing entity: " + entity);
     note("-------------------------------------------------------------");
     
     // Resolve class tree 
     
     List<TypeElement> classTree = resolveClassTree(entity);
         
     // Create classes
 
     String className = entity.getSimpleName().toString();
     String packageName = getPackage(entity);
     
     ModelClass baseClass = new ModelClass(packageName, className + "Base");
     ModelClass compactClass = new ModelClass(packageName, className + "Compact", baseClass);
     ModelClass completeClass = new ModelClass(packageName, className + "Complete", baseClass);
     
     // Add tranquil imports into all three classes
     
     for (ModelClass modelClass : Arrays.asList(baseClass, compactClass, completeClass)) {
       modelClass.addImport("fi.tranquil.TranquilModel");
       modelClass.addImport("fi.tranquil.TranquilModelType");
     }
     
     // Add tranquil annotations to all three classes
 
     baseClass.addClassAnnotation(String.format("@TranquilModel (entityClass = %s.class, entityType = TranquilModelType.BASE)", className));
     compactClass.addClassAnnotation(String.format("@TranquilModel  (entityClass = %s.class, entityType = TranquilModelType.COMPACT)", className));
     completeClass.addClassAnnotation(String.format("@TranquilModel (entityClass = %s.class, entityType = TranquilModelType.COMPLETE)", className));
 
     List<String> originalPropertiesBase = new ArrayList<String>();
     List<String> originalPropertiesCompact = new ArrayList<String>();
     List<String> originalPropertiesComplete = new ArrayList<String>();
 
     // Add TranquilModelEntity interface into base class
     
     baseClass.addInterface("fi.tranquil.TranquilModelEntity");
 
     // Read properties from entity and split them in three categories: base properties, complex properties and complex list properties
     
     List<Element> baseProperties = new ArrayList<Element>();
     List<Element> complexProperties = new ArrayList<Element>();
     List<Element> complexListProperties = new ArrayList<Element>();
     
     Set<String> processedProperties = new HashSet<>();
     
     for (int i = classTree.size() - 1; i >= 0; i--) {
       TypeElement currentClass = classTree.get(i);
         
       for (Element element : currentClass.getEnclosedElements()) {
 
         if (element.getKind() == ElementKind.METHOD) {
           String methodName = element.getSimpleName().toString();
           if (StringUtils.startsWith(methodName, "get")) {
             String propertyName = getPropertyName(element);
 
             // TODO: Remove XMLTransient, JSONIgnore ?
             if (!processedProperties.contains(propertyName)) {
               TypeMirror methodReturnType = getMethodReturnType(element);
               if (isEntity(methodReturnType)) {
                 complexProperties.add(element);
               } else {
                 if (isList(methodReturnType)) {
                   TypeMirror listGenericType = getListGenericType((DeclaredType) methodReturnType);
 
                   if (listGenericType.getKind() == TypeKind.DECLARED && isEntity(((DeclaredType) listGenericType).asElement())) {
                     complexListProperties.add(element);
                   } else {
                     baseProperties.add(element);
                   }
                 } else {
                   baseProperties.add(element); 
                 }
               }
               
               processedProperties.add(propertyName);
             }
           };
         }
       }
     }
     
     // Add base properties into base class
 
     for (Element element : baseProperties) {
       String propertyName =  getPropertyName(element);
       originalPropertiesBase.add(propertyName);
       baseClass.addProperty(getPropertyTypeName(element), propertyName);
     }
     
     // And complex properties into compact and complete classes
     
     for (Element element : complexProperties) {
       String propertyName = getPropertyName(element);
       Element propertyType = getPropertyType(element);
       String idType = getIdTypeName(propertyType);
       
       if (idType != null) {
         originalPropertiesCompact.add(propertyName);
         compactClass.addProperty(idType, propertyName + "Id");
       }
     }
 
     for (Element element : complexProperties) {
       String propertyName = getPropertyName(element);
       originalPropertiesComplete.add(propertyName);
       completeClass.addProperty("fi.tranquil.TranquilModelEntity", propertyName);
     }
     
     // Add complex list properties into compact and complete classes
     
     for (Element element : complexListProperties) {
       String propertyName = getPropertyName(element);
       DeclaredType listGenericType = (DeclaredType) getListGenericType((DeclaredType) getMethodReturnType(element));
       String idType = getIdTypeName(listGenericType.asElement());
       originalPropertiesCompact.add(propertyName);
       compactClass.addProperty("java.util.List<" + idType + ">", propertyName + "Ids");
     }
 
     for (Element element : complexListProperties) {
       String propertyName = getPropertyName(element);
       originalPropertiesComplete.add(propertyName);
       completeClass.addProperty("java.util.List<fi.tranquil.TranquilModelEntity>", propertyName);
     }
     
     // Add classes into lookup properties
     
     String fullyQualifiedClassName = packageName + '.' + className;
 
     baseClasses.put(fullyQualifiedClassName, baseClass.getFullyQualifiedName());
     compactClasses.put(fullyQualifiedClassName, compactClass.getFullyQualifiedName());
     completeClasses.put(fullyQualifiedClassName, completeClass.getFullyQualifiedName());
     
     // Add original properties field into tranquil class
     
     baseClass.addProperty("public final static", "String[]", "properties", "{" + joinProperties(originalPropertiesBase) + "}", false, false);
     compactClass.addProperty("public final static", "String[]", "properties", "{" + joinProperties(originalPropertiesCompact) + "}", false, false);
     completeClass.addProperty("public final static", "String[]", "properties", "{" + joinProperties(originalPropertiesComplete) + "}", false, false);
     
     // Write classes
     
     note("Writing class: " + baseClass.getFullyQualifiedName());
     classWriter.writeClass(processingEnv.getFiler().createSourceFile(processingEnv.getElementUtils().getBinaryName(entity) + "Base"), baseClass);
     
     note("Writing class: " + compactClass.getFullyQualifiedName());
     classWriter.writeClass(processingEnv.getFiler().createSourceFile(processingEnv.getElementUtils().getBinaryName(entity) + "Compact"), compactClass);
 
     note("Writing class: " + completeClass.getFullyQualifiedName());
     classWriter.writeClass(processingEnv.getFiler().createSourceFile(processingEnv.getElementUtils().getBinaryName(entity) + "Complete"), completeClass);
   }
 
   private String joinProperties(List<String> strings) {
     StringBuilder resultBuilder = new StringBuilder();
     
     for (int i = 0, l = strings.size(); i < l; i++) {
       resultBuilder
         .append('"')
         .append(strings.get(i))
         .append('"');
 
       if (i < (l - 1)) {
         resultBuilder.append(',');
       }
     }
     
     return resultBuilder.toString();
   }
 
   /**
    * Returns type of id property in entity class
    * 
    * @param classElement entity class
    * @return type of id property in entity class
    */
   private String getIdTypeName(Element classElement) {
     for (Element element : classElement.getEnclosedElements()) {
       if (element.getKind() == ElementKind.METHOD) {
         String methodName = element.getSimpleName().toString();
         if ("getId".equals(methodName)) {
           return getPropertyTypeName(element);
         }
       }
     }
     
     TypeElement classTypeElement = (TypeElement) classElement;
     TypeElement superClass = (TypeElement) processingEnv.getTypeUtils().asElement(classTypeElement.getSuperclass());
     
     if (superClass.getSuperclass().getKind() != TypeKind.NONE) {
       return getIdTypeName(superClass);
     }
 
     return null;
   }
 
   /**
    * Returns name of a property. 
    * 
    * @param element method or field of which name will be resolved.
    * @return name of property
    */
   private String getPropertyName(Element element) {
     switch (element.getKind()) {
       case METHOD:
         String methodName = element.getSimpleName().toString();
         String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
         return propertyName;
       default:
         return element.getSimpleName().toString();
     }
   }
   
   /**
    * Returns type name of a property. 
    * 
    * @param element method or field of which type will be resolved.
    * @return type name of property
    */
   private String getPropertyTypeName(Element element) {
     String propertyType = null;
     switch (element.getKind()) {
       case METHOD:
         ExecutableElement executableElement = (ExecutableElement) element;
         propertyType = executableElement.getReturnType().toString();
       break;
       default:
         propertyType = element.toString();
       break;
     }
 
     if (propertyType.startsWith("java.lang.") && propertyType.indexOf('.', 10) == -1)
       return propertyType.substring(10);
     
     return propertyType;
   }
   
   /**
    * Returns type of a property. 
    * 
    * @param element method or field of which type will be resolved.
    * @return type of property
    */
   private Element getPropertyType(Element element) {
     switch (element.getKind()) {
       case METHOD:
         ExecutableElement executableElement = (ExecutableElement) element;
         return processingEnv.getTypeUtils().asElement(executableElement.getReturnType());
       default:
         return element;
     }
   }
   
   /**
    * Resolves return type of a method
    * 
    * @param element method
    * @return return type of a method
    */
   private TypeMirror getMethodReturnType(Element element) {
     if (element instanceof ExecutableElement) {
       ExecutableElement executableElement = (ExecutableElement) element;
       TypeMirror returnTypeMirror = executableElement.getReturnType();
       return returnTypeMirror;
     }
     
     return null;
   }
   
   /**
    * Resolves a generic type of a java.util.List
    * 
    * @param listType a list
    * @return generic type of a java.util.List
    */
   private TypeMirror getListGenericType(DeclaredType listType) {
     List<? extends TypeMirror> typeArguments = listType.getTypeArguments();
     if (typeArguments != null && typeArguments.size() == 1) {
       return typeArguments.get(0);
     }
 
     return null;
   }
   
   /**
    * Returns whether type is entity or not
    * 
    * @param type type
    * @return whether type is entity or not
    */
   private boolean isEntity(TypeMirror type) {
     if (type.getKind() == TypeKind.DECLARED) {
       return isEntity(((DeclaredType) type).asElement());
     }
     
     return false;
   }
   
   /**
    * Returns whether element is entity or not
    * 
    * @param element element
    * @return whether element is entity or not
    */
   private boolean isEntity(Element element) {
     if (element.getAnnotation(javax.persistence.Entity.class) != null)
       return true;
 
     if (element.getAnnotation(fi.tranquil.TranquilEntity.class) != null)
       return true;
 
     return false;
   }
   
   /**
    * Returns whether type is java.util.List or not
    * 
    * @param type type
    * @return whether type is java.util.List or not
    */
   private boolean isList(TypeMirror type) {
     if (type.getKind() == TypeKind.DECLARED) {
       return isList((TypeElement) ((DeclaredType) type).asElement());
     }
     
     return false;
   }
   
   /**
    * Returns whether element is java.util.List or not
    * 
    * @param element element
    * @return whether element is java.util.List or not
    */
   private boolean isList(TypeElement element) {
     String className = element.getQualifiedName().toString();
     try {
       Class<?> returnTypeClass = Class.forName(className);
       if (returnTypeClass != null && returnTypeClass.isAssignableFrom(java.util.List.class)) {
         return true;
       }
     } catch (ClassNotFoundException e) {
     }
     
     return false;
   }
   
   /**
    * Returns package of a class or interface
    * 
    * @param entityType class or interface
    * @return package of a class or interface
    */
   private String getPackage(TypeElement entityType) {
     PackageElement elementPackage = processingEnv.getElementUtils().getPackageOf(entityType);
     return elementPackage.getQualifiedName().toString();
   }
 
   private int round = 0;
   private ClassWriter classWriter = new ClassWriter();
   private Map<String, String> baseClasses;
   private Map<String, String> compactClasses;
   private Map<String, String> completeClasses;   
 }
