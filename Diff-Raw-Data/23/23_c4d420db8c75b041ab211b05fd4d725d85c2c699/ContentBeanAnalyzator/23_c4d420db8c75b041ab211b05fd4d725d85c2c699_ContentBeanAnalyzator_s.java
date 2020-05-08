 package com.coremedia.beanmodeller.processors.analyzator;
 
 import com.coremedia.beanmodeller.annotations.ContentBean;
 import com.coremedia.beanmodeller.annotations.ContentProperty;
 import com.coremedia.beanmodeller.beaninformation.AbstractPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.AnalyzatorContentBeanInformation;
 import com.coremedia.beanmodeller.beaninformation.BlobPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.BooleanPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.ContentBeanHierarchy;
 import com.coremedia.beanmodeller.beaninformation.ContentBeanInformation;
 import com.coremedia.beanmodeller.beaninformation.DatePropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.EmptyContentBeanInformation;
 import com.coremedia.beanmodeller.beaninformation.GrammarInformation;
 import com.coremedia.beanmodeller.beaninformation.IntegerPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.LinkListPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.MarkupPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.PropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.StringPropertyInformation;
 import com.coremedia.beanmodeller.beaninformation.UnknownPropertyInformation;
 import com.coremedia.beanmodeller.processors.MavenProcessor;
 import com.coremedia.cap.common.Blob;
 import com.coremedia.objectserver.beans.AbstractContentBean;
 import com.coremedia.xml.Markup;
 
 import javax.activation.MimeType;
 import javax.activation.MimeTypeParseException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.WildcardType;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * The implementation for the content bean analyzer
  */
 public class ContentBeanAnalyzator extends MavenProcessor {
   private List<Class> beansToAnalyze = new LinkedList<Class>();
 
   // see https://documentation.coremedia.com/servlet/permalink/285388/286256/en/5.3
   public static final int MAX_CONTENT_TYPE_LENGTH = 15;
 
   // see https://documentation.coremedia.com/servlet/permalink/285388/286228/en/5.3
   // TODO 15 for date property?!
   // TODO 18 is only true for Oracle! MySQL for instance allows more chars.
   public static final int MAX_PROPERTY_LENGTH = 18;
 
   private static final ContentBeanInformation PROPERTY_DEFAULT_LINKLIST_TYPE = EmptyContentBeanInformation.getInstance();
 
   // this map is used during analyzation to track methods to consider as content bean properties, which is used later
   private Set<Method> foundContentBeanProperties = new HashSet<Method>();
 
   //the method return types we accept as content bean property methods
   private static final Set<Class> VALID_METHOD_RETURN_TYPES = new HashSet<Class>();
 
   static {
     VALID_METHOD_RETURN_TYPES.add(Boolean.class);
     VALID_METHOD_RETURN_TYPES.add(Integer.class);
     VALID_METHOD_RETURN_TYPES.add(Date.class);
     VALID_METHOD_RETURN_TYPES.add(Calendar.class);
     VALID_METHOD_RETURN_TYPES.add(List.class);
     VALID_METHOD_RETURN_TYPES.add(String.class);
     VALID_METHOD_RETURN_TYPES.add(Markup.class);
     VALID_METHOD_RETURN_TYPES.add(Blob.class);
   }
 
   private int propertyDefaultStringLength = 32;
   private String propertyDefaultMarkupGrammar = MarkupPropertyInformation.COREMEDIA_RICHTEXT_GRAMMAR_NAME;
   private int propertyDefaultLinkListMin = 0;
   private int propertyDefaultLinkListMax = Integer.MAX_VALUE;
 
   /**
    * <p>Finds all candidate content beans that reside in a "packageName". Call this method
    * before calling analyzeContentBeanInformation() method, if you want Analyzer to search annotated
    * candidate content bean classes automatically. </p>
    *
    * @param packageName package name to search for candidate content bean classes, e.g., "com.coremedia"
    */
   public void findContentBeans(String packageName) {
     if (getLog().isDebugEnabled()) {
       getLog().debug("Searching for content beans in package " + packageName);
     }
     //we create the scanner
     ClassPathContentBeanScanner scanner = new ClassPathContentBeanScanner();
     //and force it to look for content beans in the given package
     Set<Class> candidateCBs = scanner.findCandidateContentBeanClasses(packageName, getLog());
     getLog().info("Found " + candidateCBs.size() + " beans in package " + packageName);
     //add al found content beans for further analysation
     for (Class contentBean : candidateCBs) {
       this.addContentBean(contentBean);
     }
   }
 
   /**
    * manually add a content bean for analyzation.
    *
    * @param bean the bean to add for analyzation
    */
   public void addContentBean(Class bean) {
     if (getLog().isDebugEnabled()) {
       getLog().debug("Adding class " + bean.getCanonicalName() + " to the analysation list");
     }
     beansToAnalyze.add(bean);
   }
 
   /**
    * analyze the content beans either added by addContentBean or findContentBeans
    *
    * @return the analyzed bean hierarchy
    * @throws ContentBeanAnalyzationException
    *          if there was any error in the bean definitions - all found errors are returned
    */
   public ContentBeanHierarchy analyzeContentBeanInformation() throws ContentBeanAnalyzationException {
     ContentBeanHierarchy hierarchy = new ContentBeanHierarchy();
     //to mark that we have analyzed the beans we create the root bean information object
 
     getLog().debug("Starting content bean analyzation");
 
     //we first create an exception - perhaps we add errors later
     ContentBeanAnalyzationException potentialException = new ContentBeanAnalyzationException();
 
     extractBeanClassHierarchy(potentialException, hierarchy);
 
     //from this point on we can assume that all content beans are found and rootContentBeans &
     // allFoundContentBeanInformation are properly filled.
     //so first of all let's see if there is a problem in the hierarchy
     checkBeanClassHierarchy(potentialException, hierarchy);
 
     extractDocTypeBasicInformation(potentialException, hierarchy);
 
     extractDocProperties(potentialException, hierarchy);
 
     //have there been errors in the analyzation?
     if (potentialException.hasErrors()) {
       //and throw the prepared exception
       throw potentialException;
     }
     getLog().info("Content bean analyzation successfully performed");
     logHierarchyInformation(hierarchy);
     //and return the results
     return hierarchy;
   }
 
   private void logHierarchyInformation(ContentBeanHierarchy hierarchy) {
     //this is the string builder were we collect the debug string
     StringBuilder builder = new StringBuilder();
     builder.append("Extracted Content Bean Hierarchy:\n");
     //for each root bean we print the methods and the child beans
     for (ContentBeanInformation beanInfo : hierarchy.getRootBeanInformation()) {
       //the indentationLevel is representating the indentation
       int indentationLevel = 0;
       logHierarchyInformation(beanInfo, indentationLevel, builder);
     }
     getLog().info(builder.toString());
   }
 
   private void logHierarchyInformation(ContentBeanInformation beanInfo, int indentationLevel, StringBuilder builder) {
     logHierarchyInformationAddIdent(indentationLevel, builder);
     builder.append("Content Bean: ");
     builder.append(beanInfo.getHumanUnderstandableRepresentation());
     builder.append('\n');
     for (PropertyInformation propertyInformation : beanInfo.getProperties()) {
       logHierarchyInformationAddIdent(indentationLevel + 2, builder);
       builder.append("+ ");
       builder.append(propertyInformation.getHumanUnderstandableRepresentation());
       builder.append('\n');
     }
     for (ContentBeanInformation childBean : beanInfo.getChilds()) {
       logHierarchyInformation(childBean, indentationLevel + 1, builder);
     }
   }
 
   private void logHierarchyInformationAddIdent(int indentationLevel, StringBuilder builder) {
     for (int i = 0; i < indentationLevel; i++) {
       builder.append(' ');
     }
   }
 
   private void checkBeanClassHierarchy(ContentBeanAnalyzationException potentialException, ContentBeanHierarchy hierarchy) {
     getLog().debug("checking bean class hierarchy");
 
     //to avoid potential loops we note which bean we already have analyzed
     Set<Class> visitedClasses = new HashSet<Class>(hierarchy.getAllFoundContentBeans().size());
 
     //then we simply analyze each bean
     for (ContentBeanInformation bean : hierarchy.getAllContentBeanInformation()) {
       //first look if there is no problem in the hierarchy between bean and parent
       Class contentBean = bean.getContentBean();
 
       //this mad for is a cycle to go up the complete hierarchy up to abstract content bean
       // or if we find an already analyzed class
       for (Class currentClass = contentBean;
            !currentClass.equals(AbstractContentBean.class) && !visitedClasses.contains(currentClass.getClass());
            currentClass = currentClass.getSuperclass()) {
         if (getLog().isDebugEnabled()) {
           getLog().debug("checking class in hierarchy: " + currentClass);
         }
 
         //first of all note that we visited the class
         visitedClasses.add(currentClass);
 
         //analyze the methods
         analyzeMethods(potentialException, contentBean, currentClass, hierarchy);
       }
     }
   }
 
   private void analyzeMethods(ContentBeanAnalyzationException potentialException, Class contentBean, Class currentClass, ContentBeanHierarchy hierarchy) {
     //it is a content bean if there is an annotation - we do not really care about details here
     boolean isContentBean = currentClass.getAnnotation(ContentBean.class) != null;
 
     // stores propertyNames for each class to prevent double property declarations
     Set<String> foundPropertyNames = new HashSet<String>();
 
     for (Method method : findDeclaredMethods(currentClass, contentBean)) {
       ContentProperty methodAnnotation = method.getAnnotation(ContentProperty.class);
       boolean isValidPropertyMethod = isValidPropertyMethod(method);
       boolean hasValidReturnType = hasValidReturnType(method, hierarchy);
       boolean methodIsContentBeanMethod = false;
       if (methodAnnotation != null) {
         methodIsContentBeanMethod = analyzeAnnotatedMethod(potentialException, contentBean, currentClass, isContentBean, method, isValidPropertyMethod, hasValidReturnType, methodAnnotation);
       }
       else {
         methodIsContentBeanMethod = analyzeNotAnnotatedMethod(potentialException, contentBean, method, isValidPropertyMethod, hasValidReturnType);
       }
 
       //if the analyzation was successfull we note it for later property generation
       if (methodIsContentBeanMethod) {
         // do some more sanity checks, like property length
         // this method will add the method to the final result set, too
         checkMethod(potentialException, currentClass, foundPropertyNames, method);
       }
     }
   }
 
 
   private boolean analyzeNotAnnotatedMethod(ContentBeanAnalyzationException potentialException, Class contentBean, Method method, boolean validPropertyMethod,
                                             boolean hasValidReturnType) {
     boolean methodIsContentBeanMethod = false;
     //we want to look at abstract methods only
     if (Modifier.isAbstract(method.getModifiers())) {
       // no annotation
       // don't be so strict.. if it passes "isValidPropertyMethod" then "hasValidReturnType" must pass too, though.
       if (!validPropertyMethod) {
         getLog().info("Method " + method + " has been ignored since it is no valid property method" +
             ContentBeanAnalyzationException.VALID_METHOD_HINTS_MESSAGE);
       }
       else if (!hasValidReturnType) {
         getLog().error("Added potential exception for invalid return type for " + contentBean + " " + method);
         potentialException.addError(contentBean, method.getName(), ContentBeanAnalyzationException.INVALID_RETURN_TYPES_MESSAGE + VALID_METHOD_RETURN_TYPES);
       }
       else {
         methodIsContentBeanMethod = true;
       }
     }
     else {
       getLog().debug("Method " + method.getName() + " has been ignored since only abstract methods are considered.");
     }
     return methodIsContentBeanMethod;
   }
 
   private boolean analyzeAnnotatedMethod(ContentBeanAnalyzationException potentialException, Class contentBean, Class currentClass, boolean isContentBean, Method method,
                                          boolean validPropertyMethod, boolean hasValidReturnType, ContentProperty methodAnnotation) {
     boolean methodIsContentBeanMethod = false;
     //bean properties only in content beans
     if (!isContentBean) {
       potentialException.addError(contentBean, ContentBeanAnalyzationException.PROPERTY_NOT_IN_CB_MESSAGE +
           "In bean " + currentClass.getCanonicalName() + "the method " + method.getName() + " is marked as bean property but the class is no content bean");
       //bean properties must be abstract
     }
     else if (methodAnnotation.thisAintOne()) {
       // this ain't definately no property method
       if (getLog().isDebugEnabled()) {
         getLog().debug(method + " is marked as not a ContentProperty explicitly");
       }
       return false;
     }
     else if (!validPropertyMethod) {
       potentialException.addError(contentBean, method.getName(), ContentBeanAnalyzationException.INVALID_PROPERTY_MESSAGE +
           "In bean " + currentClass.getCanonicalName() + "the method " + method.getName() + " is not valid." +
           ContentBeanAnalyzationException.VALID_METHOD_HINTS_MESSAGE
       );
       // methods must have specific return types
     }
     else if (!hasValidReturnType) {
       potentialException.addError(contentBean, method.getName(), ContentBeanAnalyzationException.INVALID_RETURN_TYPES_MESSAGE + VALID_METHOD_RETURN_TYPES);
     }
     else {
       methodIsContentBeanMethod = true;
     }
     return methodIsContentBeanMethod;
   }
 
   private boolean checkMethod(ContentBeanAnalyzationException potentialException, Class currentClass, Set<String> foundPropertyNames, Method method) {
     String documentTypePropertyName = getDocumentTypePropertyNameFromMethod(method);
     // VALIDATE
     //check if the property name name is too long
     if (documentTypePropertyName.length() > MAX_PROPERTY_LENGTH) {
       potentialException.addError(currentClass, documentTypePropertyName, ContentBeanAnalyzationException.METHODNAME_TOO_LOGN_FOR_DOCTPYENAME_MESSAGE + "max is " +
           MAX_PROPERTY_LENGTH);
       //we ignore this method
       return true;
     }
     //check if we have seen this property name before
     if (foundPropertyNames.contains(documentTypePropertyName)) {
       potentialException.addError(currentClass, documentTypePropertyName, ContentBeanAnalyzationException.DUPLICATE_PROPERTY_NAMES_MESSAGES);
       //and ignore this method
       return true;
     }
     if (getLog().isDebugEnabled()) {
       getLog().debug("Found property for " + currentClass.getCanonicalName() + ": " + documentTypePropertyName + "(" + method.getName() + ")");
     }
 
     foundContentBeanProperties.add(method);
     // save property name for validation of duplicate entries in next iteration for each class
     foundPropertyNames.add(documentTypePropertyName);
     return false;
   }
 
   private void extractBeanClassHierarchy(ContentBeanAnalyzationException potentialException, ContentBeanHierarchy hierarchy) {
     getLog().debug("Extracting bean hierarchy");
     for (Class bean : beansToAnalyze) {
       //all content bean must extend AbstractContentBean – if not we must mark is as an error
       if (!(AbstractContentBean.class.isAssignableFrom(bean))) {
         potentialException.addError(bean, ContentBeanAnalyzationException.NOT_INHERITING_ABSTRACT_CONTENT_BEAN_MESSAGE + AbstractContentBean.class.getCanonicalName());
         break;
       }
       //ok it is a content bean lets see if we can find any of its ancessors in our list
       AnalyzatorContentBeanInformation lastBeanInformation = null;
       //we go up the hierarchy until we hit the abstract content bean
       for (Class currentClass = bean; !AbstractContentBean.class.equals(currentClass); currentClass = currentClass.getSuperclass()) {
         if (getLog().isDebugEnabled()) {
           getLog().debug("Extracting hierarchy from " + currentClass.getCanonicalName());
         }
         //is it a content bean?
         ContentBean beanAnnotation = (ContentBean) currentClass.getAnnotation(ContentBean.class);
         if (beanAnnotation != null) {
           //have we already seen it?
           AnalyzatorContentBeanInformation contentBeanInformation = createBeanInformation(currentClass, hierarchy);
           //if it is in a hierarchy we have to model the hierarchy in the bean information
           if (lastBeanInformation != null) {
             lastBeanInformation.setParent(contentBeanInformation);
           }
           //since we are going upwards we have to remember the bean information, since we have to set the parent is we find another content bean in the hierarchy
           lastBeanInformation = contentBeanInformation;
         }
       }
       if (lastBeanInformation != null) {
         hierarchy.getRootBeanInformation().add(lastBeanInformation);
       }
     }
 
   }
 
   /**
    * Create the bean information for given class or retrieve from hierarchy.
    * Adds the bean information to the hierarchy
    *
    * @param currentClass
    * @param hierarchy
    * @return bean information newly created or found in hierarchy
    */
   private AnalyzatorContentBeanInformation createBeanInformation(Class currentClass, ContentBeanHierarchy hierarchy) {
     AnalyzatorContentBeanInformation contentBeanInformation = (AnalyzatorContentBeanInformation) hierarchy.getContentBeanInformation(currentClass);
     // if we have not seen the class create a content bean information
     if (contentBeanInformation == null) {
       if (getLog().isDebugEnabled()) {
         getLog().debug("Found content bean " + currentClass.toString());
       }
       contentBeanInformation = new AnalyzatorContentBeanInformation(currentClass);
       //and we save the information that we have a bean infor for that class
       hierarchy.addContentBeanInformation(currentClass, contentBeanInformation);
     }
     return contentBeanInformation;
   }
 
   private void extractDocTypeBasicInformation(ContentBeanAnalyzationException potentialException, ContentBeanHierarchy hierarchy) {
     Set<Class> classesToAnalyze = hierarchy.getAllFoundContentBeans();
     Map<String, Class> foundDocTypeNames = new HashMap<String, Class>();
 
     //we simply go through all classes and check & update all bean info
     for (Class classToAnalyze : classesToAnalyze) {
       AnalyzatorContentBeanInformation beanInformation = (AnalyzatorContentBeanInformation) hierarchy.getContentBeanInformation(classToAnalyze);
 
       //get our content bean annotation
       ContentBean beanAnnotation = (ContentBean) classToAnalyze.getAnnotation(ContentBean.class);
 
       //and create the content bean name either from the annotation or the class name
       String docTypeName;
       final boolean isDoctype = !beanAnnotation.doctypeName().equals(ContentBean.DOC_TYPE_NAME_USE_CLASS_NAME);
       final boolean isAspectDoctype = !beanAnnotation.aspectDoctypeName().equals(ContentBean.DOC_TYPE_ASPECT_DISABLED);
       if (isDoctype) {
         docTypeName = beanAnnotation.doctypeName();
         if (getLog().isDebugEnabled()) {
           getLog().debug("Using annotation name " + docTypeName + " as content type name for " + classToAnalyze.getCanonicalName() + ".");
         }
       }
       else if (isAspectDoctype) {
         docTypeName = beanAnnotation.aspectDoctypeName();
         if (getLog().isDebugEnabled()) {
           getLog().debug("Using annotation name " + docTypeName + " as aspect content type name for " + classToAnalyze.getCanonicalName() + ".");
         }
       }
       else /*if (!isDoctype && !isAspectDoctype)*/ {
         // neither is set
         docTypeName = classToAnalyze.getSimpleName();
         if (getLog().isDebugEnabled()) {
           getLog().debug("Using class name as content type name for " + classToAnalyze.getCanonicalName() + " since it is the default");
         }
       }
 
       //check if the doctpye name name is too long
       if (docTypeName.length() > MAX_CONTENT_TYPE_LENGTH) {
         potentialException.addError(classToAnalyze, ContentBeanAnalyzationException.CLASSNAME_TOO_LOGN_FOR_DOCTPYENAME_MESSAGE + classToAnalyze.getCanonicalName()
             + " (the name \'" + docTypeName + "\' is " + docTypeName.length() + " - max is " + MAX_CONTENT_TYPE_LENGTH + ").");
         // and ignore property
         break;
       }
       //check if we have seen this doctype name before
       Class otherClass = foundDocTypeNames.get(docTypeName);
       if (otherClass != null) {
         potentialException.addError(classToAnalyze, ContentBeanAnalyzationException.DUPLICATE_DOCTYPE_NAMES_MESSAGE
             + classToAnalyze.getCanonicalName() + " and " + otherClass.getCanonicalName()
             + " use both " + docTypeName + " as document type name");
         //and ignore this property
         break;
       }
       //if everything is ok we have to remember that this class uses that doctype name
       //for the above check
       foundDocTypeNames.put(docTypeName, classToAnalyze);
       //and set the bean information
       beanInformation.setDocumentName(docTypeName);
 
       //check if it is an abstract doctype
       beanInformation.setAbstract(beanAnnotation.isAbstract());
 
       // get hint for external doctype
       beanInformation.setAspectDocumentName(beanAnnotation.aspectDoctypeName());
     }
   }
 
   private void extractDocProperties(ContentBeanAnalyzationException potentialException, ContentBeanHierarchy hierarchy) {
     Set<Class> classesToAnalyze = hierarchy.getAllFoundContentBeans();
 
     //we simply go through all classes and check & update all bean info
     for (Class classToAnalyze : classesToAnalyze) {
       AnalyzatorContentBeanInformation beanInformation = (AnalyzatorContentBeanInformation) hierarchy.getContentBeanInformation(classToAnalyze);
 
       if (getLog().isDebugEnabled()) {
         getLog().debug("Analyzing methods for " + classToAnalyze.getCanonicalName());
       }
 
       // create property information for each abstract method
       for (Method method : findDeclaredMethods(classToAnalyze, classToAnalyze)) {
         //we only have to consider methods which have been checked earlier
         if (foundContentBeanProperties.contains(method)) {
           // property information
           AbstractPropertyInformation propertyInformation;
 
           String documentTypePropertyName = getDocumentTypePropertyNameFromMethod(method);
 
           try {
             propertyInformation = getPropertyInformationForMethod(method, hierarchy);
           }
           catch (ContentBeanAnalyzatorInternalException e) {
             potentialException.addError(classToAnalyze, documentTypePropertyName, e.getMessage());
 
             // don't include this method in result
             break;
           }
 
           // POST CHECK
           if (propertyInformation instanceof UnknownPropertyInformation) {
             potentialException.addError(classToAnalyze, documentTypePropertyName, ContentBeanAnalyzationException.PROPERTY_RETURN_TYPE_UNKNOWN_MESSAGE + method
                 .getReturnType());
 
             // don't include this method in result
             break;
           }
 
           propertyInformation.setDocumentTypePropertyName(documentTypePropertyName);
 
           // add propertyInformation to contentBeanInformation
           beanInformation.addProperty(propertyInformation);
         }
       }
     }
   }
 
   /**
    * Throws error if there are analyzation errors.
    *
    * @param method to analyze
    * @return PropertyInformation for this error.
    * @throws ContentBeanAnalyzatorInternalException
    *          analyzation error occurred. Exception message should be included to "potentialException" list.
    */
   private AbstractPropertyInformation getPropertyInformationForMethod(Method method, ContentBeanHierarchy hierarchy) throws ContentBeanAnalyzatorInternalException {
     // SWITCH FOR EACH RETURN TYPE
     final Class<?> returnType = method.getReturnType();
     if (returnType.equals(Integer.class)) {
       return new IntegerPropertyInformation(method);
     }
     else if (returnType.equals(Boolean.class)) {
       return new BooleanPropertyInformation(method);
     }
     else if (returnType.equals(Calendar.class) || returnType.equals(Date.class)) {
       return new DatePropertyInformation(method);
     }
     else if (returnType.equals(Markup.class)) {
       return getMarkupPropertyInformation(method);
     }
     else if (returnType.equals(String.class)) {
       return getStringPropertyInformation(method);
     }
     else if (returnType.equals(List.class)) {
       return getLinkListPropertyInformation(method, hierarchy);
     }
     else if (hierarchy.getAllFoundContentBeans().contains(returnType)) {
       return getLinkListPropertyInformationSingle(method, (AnalyzatorContentBeanInformation) hierarchy.getContentBeanInformation(returnType));
     }
     else if (returnType.equals(Blob.class)) {
       return getBlobPropertyInformation(method);
     }
     else {
       // default
       return new UnknownPropertyInformation(method);
     }
   }
 
   private AbstractPropertyInformation getBlobPropertyInformation(Method method) throws ContentBeanAnalyzatorInternalException {
     ContentProperty annotation = method.getAnnotation(ContentProperty.class);
     String mimeTypeName = (annotation != null) ? annotation.propertyBlobMimeType() : ContentProperty.BLOB_PROPERTY_DEFAULT_MIME_TYPE;
     try {
       MimeType mimetype = new MimeType(mimeTypeName); //NOSONAR - this is a test if it is a proper mime type
     }
     catch (MimeTypeParseException e) {
       throw new ContentBeanAnalyzatorInternalException(ContentBeanAnalyzationException.INVALID_MIME_TYPE_MESSAGE + mimeTypeName + " is not a valid mime type (it should " +
           "have the pattern X/Y", e);
     }
 
     BlobPropertyInformation blobPropertyInformation = new BlobPropertyInformation(method);
     blobPropertyInformation.setAllowedMimeTypes(mimeTypeName);
     return blobPropertyInformation;
   }
 
   /**
    * Create PropertyInformation for a LinkList containing at most 1 item.
    *
    * @param method     Method to create the PropertyInformation for
    * @param linkedBean Object of the ContentBeanInformation that is referenced by the Link.
    * @return PropertyBeaninformation
    */
   private AbstractPropertyInformation getLinkListPropertyInformationSingle(Method method, AnalyzatorContentBeanInformation linkedBean) {
     LinkListPropertyInformation linkListPropertyInformation = new LinkListPropertyInformation(method);
     linkListPropertyInformation.setLinkType(linkedBean);
     linkListPropertyInformation.setMax(1);
 
     // todo read from annotation
     linkListPropertyInformation.setMin(propertyDefaultLinkListMin);
 
     return linkListPropertyInformation;
   }
 
   private AbstractPropertyInformation getLinkListPropertyInformation(Method method, ContentBeanHierarchy hierarchy) throws ContentBeanAnalyzatorInternalException {
     LinkListPropertyInformation linkListPropertyInformation = new LinkListPropertyInformation(method);
 
     // todo read from annotation
     linkListPropertyInformation.setMin(propertyDefaultLinkListMin);
     linkListPropertyInformation.setMax(propertyDefaultLinkListMax);
 
     // read Linktype from return type of the method
     Type genericReturnType = method.getGenericReturnType();
 
     if (genericReturnType instanceof ParameterizedType) {
       // return Type is parameterized -> an explicit type is given
       final Type listType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
       Class returnTypeLinkType;
 
       if (listType instanceof Class) {
         returnTypeLinkType = (Class) listType;
       }
       else if (listType instanceof WildcardType) {
         // whoo, still quite optimistic cast!?
         // this handles cases like public abstract List<? extends CBGImage> getImages();
         returnTypeLinkType = (Class) ((WildcardType) listType).getUpperBounds()[0];
 
       }
       else {
        throw new ContentBeanAnalyzatorInternalException(ContentBeanAnalyzationException.LINKED_DOCTYPE_UNKNOWN + listType);
       }
 
       // find the ContentBeanInformation that represents this LinkType
       ContentBeanInformation contentBeanInformationLinkType = hierarchy.getContentBeanInformation(returnTypeLinkType);
 
       if (contentBeanInformationLinkType == null) {
         // ouh, not found by class. let's try to look it up by document type/class name
         for (ContentBeanInformation beanInformation : hierarchy.getAllContentBeanInformation()) {
           if (beanInformation.getDocumentName().equals(returnTypeLinkType.getCanonicalName())) {
             contentBeanInformationLinkType = beanInformation;
           }
         }
       }
       // ## validate
       if (contentBeanInformationLinkType == null) {
        throw new ContentBeanAnalyzatorInternalException(ContentBeanAnalyzationException.LINKED_DOCTYPE_UNKNOWN + returnTypeLinkType);
       }
 
       linkListPropertyInformation.setLinkType(contentBeanInformationLinkType);
     }
     else {
       // no explicit type for linktype given -> use the default
       linkListPropertyInformation.setLinkType(PROPERTY_DEFAULT_LINKLIST_TYPE);
     }
 
     return linkListPropertyInformation;
   }
 
   private AbstractPropertyInformation getStringPropertyInformation(Method method) throws ContentBeanAnalyzatorInternalException {
     // method annotation
     final ContentProperty methodAnnotation = method.getAnnotation(ContentProperty.class);
 
     // ## VALIDATE
     int stringLength;
     if (methodAnnotation == null || methodAnnotation.stringLength() == ContentProperty.STRING_PROPERTY_DEFAULT_LENGTH) {
       // use property default value
       stringLength = getPropertyDefaultStringLength();
     }
     else {
       // it is defined and not the "marker default", use that value then, of course
       stringLength = methodAnnotation.stringLength();
     }
 
     // raise error if stringLength is negative
     if (stringLength < 0) {
       throw new ContentBeanAnalyzatorInternalException(ContentBeanAnalyzationException.STRING_PROPERTY_TOO_SHORT_MESSAGE + stringLength);
     }
 
     StringPropertyInformation stringPropertyInformation = new StringPropertyInformation(method);
     stringPropertyInformation.setLength(stringLength);
 
     return stringPropertyInformation;
   }
 
   private AbstractPropertyInformation getMarkupPropertyInformation(Method method) throws ContentBeanAnalyzatorInternalException {
     // method annotation
     final ContentProperty methodAnnotation = method.getAnnotation(ContentProperty.class);
 
     final MarkupPropertyInformation markupInformation = new MarkupPropertyInformation(method);
 
     if (methodAnnotation == null || ContentProperty.MARKUP_PROPERTY_DEFAULT_GRAMMAR.equals(methodAnnotation.propertyXmlGrammar())) {
       // use default grammar, return immediately.
 
       return markupInformation;
     }
 
 
     // read grammarinformation from annotation
     String grammarNames = methodAnnotation.propertyXmlGrammar();
     GrammarInformation grammarInformation = new GrammarInformation();
 
 
     // tokenize on whitespaces
     boolean firstGrammarName = true;
     for (String grammarName : grammarNames.split("\\s")) {
       URL grammarURL;
       String grammarLocation = null;
 
       // distinguish between classpath and http locations
       if (grammarName.startsWith("classpath:")) {
         String grammarSource = grammarName.substring("classpath:".length());
         // get URL just like com.coremedia.io.ResourceLoader (line 119) gets it
         grammarURL = Thread.currentThread().getContextClassLoader().getResource(grammarSource);
         grammarLocation = grammarName;
         if (grammarName.contains("/")) { // get plain grammar name without any path information
           grammarName = grammarName.substring(grammarName.lastIndexOf('/') + 1);
         }
       }
       else {
         try {
           grammarURL = new URL(grammarName);
         }
         catch (MalformedURLException e) {
           throw new ContentBeanAnalyzatorInternalException(ContentBeanAnalyzationException.SCHEMA_DEFINITION_NOT_FOUND_MESSAGE + grammarName + ". It is not decodable " +
               "as URL", e);
         }
       }
       if (grammarURL == null) {
         throw new ContentBeanAnalyzatorInternalException(ContentBeanAnalyzationException.SCHEMA_DEFINITION_NOT_FOUND_MESSAGE + grammarName);
       }
 
       // set GrammarName and URL only for the first Grammar specified
       if (firstGrammarName) {
         grammarInformation.setGrammarName(grammarName);
         grammarInformation.setGrammarURL(grammarURL);
       }
 
       // add location for all given Schemas
       grammarInformation.addGrammarLocation(grammarLocation);
     }
 
     //only add grammar information for no default grammars.
     if (grammarInformation != null && !grammarInformation.getGrammarName().equals(getPropertyDefaultMarkupGrammar())) {
       markupInformation.setGrammarInformation(grammarInformation);
     }
 
     return markupInformation;
   }
 
   /**
    * Just calculate the name. Take method name or annotation, if available. Method name definitions are stripped of "is" and "get" prefixes.
    *
    * @param method method to calculate name for
    * @return documenttypeproperty name
    */
   private String getDocumentTypePropertyNameFromMethod(Method method) {
     final ContentProperty methodAnnotation = method.getAnnotation(ContentProperty.class);
     String documentTypePropertyName = method.getName().startsWith("is") ? method.getName().substring(2) : method.getName().substring(3);
 
     //ensure that the first letter of the name is lower case
     documentTypePropertyName = documentTypePropertyName.substring(0, 1).toLowerCase() + documentTypePropertyName.substring(1);
 
     // get method name from Annotation if available.
     if (methodAnnotation != null && !methodAnnotation.propertyName().equals(ContentProperty.PROPERTY_NAME_USE_METHOD_NAME)) {
       // if annotation is not set to default (extract from method name), then set property name from annotation
       documentTypePropertyName = methodAnnotation.propertyName();
     }
 
     return documentTypePropertyName;
   }
 
   private boolean isValidPropertyMethod(Method method) {
 // methods must be:
     // - abstract
     // - public or protected
     // - start with "get" or "is"
     // - have no parameters
     if (!Modifier.isAbstract(method.getModifiers())) {
       return false;
     }
     if (!Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {
       return false;
     }
     boolean isBoolean = Boolean.class.isAssignableFrom(method.getReturnType());
     String methodPrefix = (isBoolean) ? "is" : "get";
     if (!(method.getName().startsWith(methodPrefix))) {
       return false;
     }
     if (method.getParameterTypes().length > 0) {
       return false;
     }
     return true;
   }
 
   private boolean hasValidReturnType(Method method, ContentBeanHierarchy hierarchy) {
     final Class<?> returnType = method.getReturnType();
 
     return !returnType.isPrimitive()
         && (VALID_METHOD_RETURN_TYPES.contains(returnType)
         || hierarchy.getAllFoundContentBeans().contains(returnType));
   }
 
 
   private Collection<Method> findDeclaredMethods(Class currentClass, Class latestContentBean) {
     // it's a set to filter duplicates
     final Collection<Method> declaredMethods = new HashSet<Method>();
 
     // methods directly declared in this class
     Collections.addAll(declaredMethods, currentClass.getDeclaredMethods());
 
     // look in interfaces directly implemented by this class; recursively
     // filter methods that are not abstract in the latestContentBean anymore
     addDeclaredMethodsFromInterfaces(latestContentBean.getMethods(), currentClass, declaredMethods);
 
     if (getLog().isDebugEnabled()) {
       StringBuilder debugMessage = new StringBuilder();
       debugMessage.append("Found methods for class ").append(currentClass.getCanonicalName()).append(":\n");
       for (Method declaredMethod : declaredMethods) {
         debugMessage.append(declaredMethod).append("\n");
       }
 
       getLog().debug(debugMessage);
     }
 
     return declaredMethods;
   }
 
   private void addDeclaredMethodsFromInterfaces(final Method[] baseMethods, Class currentClass, Collection<Method> declaredMethods) {
     for (Class implementedInterface : currentClass.getInterfaces()) {
       for (Method method : implementedInterface.getDeclaredMethods()) {
         if (!isImplemented(baseMethods, method) && !isListed(declaredMethods, method)) {
           declaredMethods.add(method);
         }
       }
       // call recursively
       addDeclaredMethodsFromInterfaces(baseMethods, implementedInterface, declaredMethods);
     }
   }
 
   /**
    * if method is found by name in baseMethods and is not abstract -> ignore because it is already implemented
    */
   private boolean isImplemented(final Method[] baseMethods, Method compareMethod) {
     for (Method baseMethod : baseMethods) {
       if (baseMethod.getName().equals(compareMethod.getName())) {
         // return true if method is among baseMethods and _not_ abstract
         return !Modifier.isAbstract(baseMethod.getModifiers());
       }
     }
 
     // method not found among baseMethods
     return false;
   }
 
 
   /**
    * is method listed in declaredMethods? compared by name
    *
    * @param declaredMethods
    * @param method
    * @return true if a method of name method.getName() is already listed in declared methods.
    */
   private boolean isListed(Collection<Method> declaredMethods, Method method) {
     for (Method declaredMethod : declaredMethods) {
       if (declaredMethod.getName().equals(method.getName())) {
         return true;
       }
     }
 
     return false;
   }
 
 
   public int getPropertyDefaultStringLength() {
     return propertyDefaultStringLength;
   }
 
   public void setPropertyDefaultStringLength(int propertyDefaultStringLength) {
     this.propertyDefaultStringLength = propertyDefaultStringLength;
   }
 
   public String getPropertyDefaultMarkupGrammar() {
     return propertyDefaultMarkupGrammar;
   }
 
   public void setPropertyDefaultMarkupGrammar(String propertyDefaultMarkupGrammar) {
     this.propertyDefaultMarkupGrammar = propertyDefaultMarkupGrammar;
   }
 }
