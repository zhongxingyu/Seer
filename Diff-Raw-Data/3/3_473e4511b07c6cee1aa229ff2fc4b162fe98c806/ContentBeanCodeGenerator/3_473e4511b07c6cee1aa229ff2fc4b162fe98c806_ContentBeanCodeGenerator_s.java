 package com.coremedia.beanmodeller.processors.codegenerator;
 
 import com.coremedia.beanmodeller.beaninformation.BlobPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.BooleanPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.CacheKeyableMethodInformation;
 import com.coremedia.beanmodeller.beaninformation.ContentBeanInformation;
 import com.coremedia.beanmodeller.beaninformation.DatePropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.IntegerPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.LinkListPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.MarkupPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.PropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.StringPropertyInformation;
 import com.coremedia.beanmodeller.processors.MavenProcessor;
 import com.coremedia.cache.Cache;
 import com.coremedia.cache.CacheKey;
 import com.coremedia.cap.content.Content;
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JClassAlreadyExistsException;
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JConditional;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JDocComment;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JFieldVar;
 import com.sun.codemodel.JInvocation;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JMod;
 import com.sun.codemodel.JPackage;
 import com.sun.codemodel.JType;
 import com.sun.codemodel.JVar;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Telekom .COM Relaunch 2011
  * User: marcus
  * Date: 01.02.11
  * Time: 17:49
  * <p/>
  * Starts generating classes beginning with every given root bean.
  * Walks down the tree hierarchy while passing down information which property-methods have to be generated for every Accessorizor.
  */
 public class ContentBeanCodeGenerator extends MavenProcessor {
 
   private String packageName = "com.telekom.myproject";
   //TODO this is a silly name and needs a better alternative
   public static final String IMPL_SUFFIX = "BeanAccessorizor";
 
   private boolean generateSetters = false;
 
   public JCodeModel generateCode(Set<ContentBeanInformation> rootBeans) {
     getLog().info("Starting code generation for content beans.");
     JCodeModel contentBeanCodeModel = new JCodeModel();
     JPackage beanPackage = contentBeanCodeModel._package(packageName);
     for (ContentBeanInformation bean : rootBeans) {
       try {
         generateClass(beanPackage, bean, contentBeanCodeModel);
       }
       catch (JClassAlreadyExistsException e) {
         throw new IllegalStateException("Error handling must be implemented!", e);
       }
     }
     return contentBeanCodeModel;
   }
 
   private void generateClass(JPackage beanPackage, ContentBeanInformation bean, JCodeModel contentBeanCodeModel) throws JClassAlreadyExistsException {
     generateClass(beanPackage, bean, contentBeanCodeModel, new HashSet<PropertyInformation>());
   }
 
   private void generateClass(JPackage beanPackage, ContentBeanInformation bean, JCodeModel codeModel, Set<PropertyInformation> propertiesInTheHierarchySoFar) throws JClassAlreadyExistsException {
     //create a new Set of the accumulated properties of this class
     // set the Set's comparator to compare by name only to avoid clashes with re-defined properties
     Set<PropertyInformation> allMyProperties = new TreeSet<PropertyInformation>(new Comparator<PropertyInformation>() {
       @Override
       public int compare(PropertyInformation o1, PropertyInformation o2) {
         return o1.getDocumentTypePropertyName().compareTo(o2.getDocumentTypePropertyName());
       }
     });
 
     //collect the properties defined in this class
     for (PropertyInformation property : bean.getProperties()) {
       allMyProperties.add(property);
     }
 
     allMyProperties.addAll(propertiesInTheHierarchySoFar);
 
     //we only generate classes for non abstract content beans
     if (!bean.isAbstract()) {
       //the content accessor class is derrived from the bean class
       Class parentClass = bean.getContentBean();
       //generate the class
       JDefinedClass beanClass = beanPackage._class(bean.getName() + IMPL_SUFFIX);
       //log what we are doing
       getLog().info("Generating accessorizer class " + beanClass.fullName() + " for " + parentClass.getCanonicalName());
 
       // set parent, check if it is a generic and set a type by "narrow"ing it
       JClass parentBeanClass = beanPackage.owner().ref(parentClass);
       if (parentClass.getTypeParameters().length > 0 &&
           parentClass.getTypeParameters()[0].getBounds()[0] instanceof Class &&
           ((Class) parentClass.getTypeParameters()[0].getBounds()[0]).isAssignableFrom(parentClass)
           ) {
         // assign self as parameterized class, for the pattern where we inject our type down the hierarchy via generics
         parentBeanClass = parentBeanClass.narrow(parentClass);
       }
       beanClass._extends(parentBeanClass);
 
       //TODO this comment has to be better
       //add some javadoc
       JDocComment javaDoc = beanClass.javadoc();
       javaDoc.add("Content Accessor for " + bean + "\n");
       javaDoc.add("You can safely ignore this implementation, since it just fills your abstract getter Methods with content access implementtions\n");
       javaDoc.add("<b>Do never, ever use this class directly in your code - or even change it</b>, please.");
 
       //generate getter for each property
       for (PropertyInformation property : allMyProperties) {
         generatePropertyMethod(beanClass, property, codeModel);
 
         if (generateSetters) {
           generatePropertySetterMethod(beanClass, property, codeModel);
         }
       }
 
       // generate CacheKeys for CacheKeyables
       for (CacheKeyableMethodInformation cacheKeyableMethodInformation : bean.getCacheKeyables()) {
         generateCacheKeyMethod(beanClass, cacheKeyableMethodInformation, codeModel);
       }
     }
     else {
       getLog().info(bean.getContentBean().getCanonicalName() + " is an abstract document - no accessorizer is generated");
     }
     //and go down the hierarchy
     for (ContentBeanInformation childBean : bean.getChilds()) {
       generateClass(beanPackage, childBean, codeModel, allMyProperties);
     }
   }
 
   private void generatePropertyMethod(JDefinedClass beanClass, PropertyInformation propertyInformation, JCodeModel codeModel) {
     //we will use this quite often
     Method method = propertyInformation.getMethod();
     Class<?> methodReturnType = method.getReturnType();
 
     if (getLog().isDebugEnabled()) {
       getLog().debug("Generating method for " + method.toString());
     }
 
     //construct the correct modifiers
     int modifiers = getModifiersForMethod(method);
     //create the method
     JMethod propertyMethod = beanClass.method(modifiers, methodReturnType, method.getName());
     //TODO this comment has to be better
     //generate some java doc for the method
     JDocComment javadoc = propertyMethod.javadoc();
     javadoc.add("Getter for " + propertyInformation);
     //create the call to the content object returning th neccessary information
     //we need the basic getter call anyway
     JInvocation getterCall;
     if (propertyInformation instanceof BlobPropertyInformation) {
       // make an exception for blobs...
       getterCall = JExpr.invoke("getContent").invoke("getBlobRef").arg(JExpr.lit(propertyInformation.getDocumentTypePropertyName()));
     }
     else if (propertyInformation instanceof MarkupPropertyInformation) {
       // and another exception for Markup, there you go
       getterCall = JExpr.invoke("getMarkup").arg(JExpr.lit(propertyInformation.getDocumentTypePropertyName()));
     }
     else {
       getterCall = JExpr.invoke("getContent").invoke("get").arg(JExpr.lit(propertyInformation.getDocumentTypePropertyName()));
     }
     //and we will have to return the correct type anyway
     JType returnType = codeModel.ref(methodReturnType);
     if ((propertyInformation instanceof StringPropertyInformation)
         || (propertyInformation instanceof IntegerPropertyInformation)
         || (propertyInformation instanceof MarkupPropertyInformation)
         || (propertyInformation instanceof BlobPropertyInformation)) {
       //we directly return the property, casted to target type
       propertyMethod.body()._return(JExpr.cast(returnType, getterCall));
     }
     else if ((propertyInformation instanceof LinkListPropertyInformation)) {
       createLinkListMethod(codeModel, methodReturnType, propertyMethod, propertyInformation, returnType);
 
     }
     else if (propertyInformation instanceof DatePropertyInformation) {
       createTimePropertyMethod(codeModel, methodReturnType, propertyMethod, getterCall, returnType);
     }
     else if (propertyInformation instanceof BooleanPropertyInformation) {
       createBooleanPropertyMethod(codeModel, methodReturnType, propertyMethod, getterCall, returnType);
     }
     //get the return type of the method
   }
 
 
   private void generatePropertySetterMethod(JDefinedClass beanClass, PropertyInformation propertyInformation, JCodeModel codeModel) {
     //we will use this quite often
     final Method method = propertyInformation.getMethod();
     final String documentTypePropertyName = propertyInformation.getDocumentTypePropertyName();
     Class<?> propertyType = method.getReturnType();
 
     if (getLog().isDebugEnabled()) {
       getLog().debug("Generating corresponding setter method for " + method.toString());
     }
 
     //construct the correct modifiers
     int modifiers = getModifiersForMethod(method);
     //create the method
     // strip name of 'get' or 'is' and add setter
     final String methodName = method.getName().startsWith("is") ? method.getName().substring(2) : method.getName().substring(3);
 
     JMethod propertyMethod = beanClass.method(modifiers, void.class, "set" + methodName);
     //generate some java doc for the method
     JDocComment javadoc = propertyMethod.javadoc();
     javadoc.add("Setter for " + propertyInformation);
 
     // param list
     propertyMethod.param(propertyType, documentTypePropertyName);
 
     // content setter method
     JInvocation setterCall = JExpr.invoke("getContent").invoke("set").arg(JExpr.lit(documentTypePropertyName)).arg(JExpr.direct(documentTypePropertyName));
     propertyMethod.body().add(setterCall);
   }
 
 
   private void createBooleanPropertyMethod(JCodeModel codeModel, Class<?> methodReturnType, JMethod propertyMethod, JInvocation getterCall, JType returnType) {
     // Boolean is mapped to an int, where 1 equals true, false otherwise
     JVar integerObject = propertyMethod.body().decl(codeModel.ref(Integer.class), "integerObject", JExpr.cast(codeModel.ref(Integer.class), getterCall));
     propertyMethod.body()._return(integerObject.ne(JExpr._null()).cand(integerObject.eq(JExpr.direct("1"))));
   }
 
   private void createTimePropertyMethod(JCodeModel codeModel, Class<?> methodReturnType, JMethod propertyMethod, JInvocation getterCall, JType returnType) {
     if (Calendar.class.isAssignableFrom(methodReturnType)) {
       //we directly return the property, casted to target type
       propertyMethod.body()._return(JExpr.cast(returnType, getterCall));
     }
     else if (Date.class.isAssignableFrom(methodReturnType)) {
       // get the calendar
       JVar calendarObject = propertyMethod.body().decl(codeModel.ref(Calendar.class), "calendar", getterCall);
       JInvocation returnObject = calendarObject.invoke("getTimer");
       propertyMethod.body()._return(returnObject);
       /*
       code = "Calendar cal = getContent().get(\"" + propertyInformation.getDocumentTypePropertyName() + "\");"+
           "return cal.getTime();";
       */
     }
     else {
       throw new IllegalStateException("End of implementation reached");
     }
   }
 
   private void createLinkListMethod(JCodeModel codeModel, Class<?> methodReturnType, JMethod propertyMethod, PropertyInformation propertyInformation, JType returnType) {
     //we redefine the getter call to avoid nulll as list return value - empty list are easier to handle
     JInvocation getterCall = JExpr.invoke("getContent").invoke("getLinks").arg(JExpr.lit(propertyInformation.getDocumentTypePropertyName()));
     //get the content as a list
     JVar contentList = propertyMethod.body().decl(codeModel.ref(List.class), "contentList", JExpr.cast(codeModel.ref(List.class), getterCall));
     if (Collection.class.isAssignableFrom(methodReturnType)) {
       //convert the content to beans
       JInvocation beanConversion = JExpr.invoke("getContentBeanFactory").invoke("createBeansFor").arg(contentList);
       propertyMethod.body()._return(beanConversion);
       /* it goes like this:
       code = "List content = getContent().get(\"" + propertyInformation.getDocumentTypePropertyName() + "\");"
           + "List result = getContentBeanFactory.createBeansFor(content);"
           + " return result;";
       */
     }
     else {
       //if the list is empty return null
       JConditional listEmptyCondition = propertyMethod.body()._if(contentList.invoke("size").eq(JExpr.lit(0)));
       listEmptyCondition._then()._return(JExpr._null());
       //else get the first content element
       JInvocation firstElement = contentList.invoke("get").arg(JExpr.lit(0));
       JInvocation createBean = JExpr.invoke("getContentBeanFactory").invoke("createBeanFor").arg(JExpr.cast(codeModel.ref(Content.class), firstElement));
       listEmptyCondition._else()._return(JExpr.cast(returnType, createBean));
       /* it goes like this:
       code = "List content = getContent().get(\"" + propertyInformation.getDocumentTypePropertyName() + "\");"
           + "if (content.size==0) {"
           + "return null;"
           + "} else {"//TODO do we need a warning log for size>1?
           + "return getContentBeanFactory.createBeanFor(content.get(0));";
       */
     }
   }
 
   private int getModifiersForMethod(Method method) {
     //construct the correct modifiers
     int modifiers = 0;
     int abstractMethodModifiers = method.getModifiers();
     if (Modifier.isPrivate(abstractMethodModifiers)) {
       modifiers |= JMod.PRIVATE;
     }
     else if (Modifier.isProtected(abstractMethodModifiers)) {
       modifiers |= JMod.PROTECTED;
     }
     else if (Modifier.isPublic(abstractMethodModifiers)) {
       modifiers |= JMod.PUBLIC;
     }
     //make it final - don't know if it is good for anything
     // modifiers |= JMod.FINAL;
     return modifiers;
   }
 
 
   private void generateCacheKeyMethod(JDefinedClass beanClass, CacheKeyableMethodInformation cacheKeyableMethodInformation, JCodeModel codeModel) throws JClassAlreadyExistsException {
     //we will use this quite often
     Method method = cacheKeyableMethodInformation.getMethod();
     Class<?> methodReturnType = method.getReturnType();
 
     if (getLog().isDebugEnabled()) {
       getLog().debug("Generating cached method for " + method.toString());
     }
 
     String shortMethodName = method.getName().startsWith("is") ? method.getName().substring(2) : method.getName().substring(3);
 
     // !! create Uncached methodcall
     JMethod uncachedMethod = beanClass.method(JMod.PROTECTED, methodReturnType, method.getName() + "Uncached");
     uncachedMethod.body()._return(JExpr._super().invoke(method.getName()));
 
 
     // !! create CacheKey calling uncached method
     JDefinedClass cacheKeyClass = beanClass._class(shortMethodName + "CacheKey");
     cacheKeyClass._extends(CacheKey.class);
     JFieldVar delegateField = cacheKeyClass.field(JMod.PRIVATE | JMod.FINAL, beanClass, "delegate");
     JMethod constructor = cacheKeyClass.constructor(JMod.PUBLIC);
     JVar delegateParam = constructor.param(beanClass, "delegate");
     constructor.body().assign(JExpr._this().ref(delegateField), delegateParam);
     JMethod evaluate = cacheKeyClass.method(JMod.PUBLIC, Object.class, "evaluate");
     evaluate.param(Cache.class, "cache");
     evaluate._throws(Exception.class);
     evaluate.body()._return(JExpr._this().ref(delegateField).invoke(uncachedMethod));
 
     JMethod hashCode = cacheKeyClass.method(JMod.PUBLIC, codeModel.INT, "hashCode");
     hashCode.body()._return(JExpr._this().ref(delegateField).invoke("hashCode"));
     JMethod equals = cacheKeyClass.method(JMod.PUBLIC, codeModel.BOOLEAN, "equals");
     JVar o = equals.param(Object.class, "o");
    equals.body()._return(JExpr._this().ref(delegateField).invoke("equals").arg(o));
 
     JMethod cacheClass = cacheKeyClass.method(JMod.PUBLIC, String.class, "cacheClass");
     cacheClass.param(Cache.class, "cache");
     cacheClass.param(Object.class, "value");
     cacheClass.body()._return(JExpr.lit("beanmodeller.cacheclass"));
 
     // !! overwrite the method to be cached
     //construct the correct modifiers
     int modifiers = getModifiersForMethod(method);
     //create the method
     JMethod cachedMethod = beanClass.method(modifiers, methodReturnType, method.getName());
 
     JVar cacheKey = cachedMethod.body().decl(cacheKeyClass, "cacheKey", JExpr._new(cacheKeyClass).arg(JExpr._this()));
     JInvocation cacheCall = JExpr.invoke("getContent").invoke("getRepository").invoke("getConnection").invoke("getCache").invoke("get").arg(cacheKey);
     cachedMethod.body()._return(JExpr.cast(codeModel.ref(methodReturnType), cacheCall));
   }
 
 
 
   public String getPackageName() {
     return packageName;
   }
 
   public void setPackageName(String packageName) {
     if (packageName.endsWith(".")) {
       this.packageName = packageName.substring(0, packageName.length() - 1);
     }
     else {
       //TODO do we nee further validations? -> InvalidArgumentException??
       this.packageName = packageName;
     }
   }
 
   public String getCanonicalGeneratedClassName(ContentBeanInformation beanInformation) {
     Class beanClass = beanInformation.getContentBean();
     return packageName + "." + beanClass.getSimpleName() + IMPL_SUFFIX;
   }
 
   public boolean isGenerateSetters() {
     return generateSetters;
   }
 
   public void setGenerateSetters(boolean generateSetters) {
     this.generateSetters = generateSetters;
   }
 }
