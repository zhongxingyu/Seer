 package org.jboss.errai.idea.plugin;
 
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Key;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.JavaPsiFacade;
 import com.intellij.psi.PsiAnnotation;
 import com.intellij.psi.PsiAnnotationParameterList;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiDirectory;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiField;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiLiteralExpression;
 import com.intellij.psi.PsiManager;
 import com.intellij.psi.PsiMethod;
 import com.intellij.psi.PsiModifierList;
 import com.intellij.psi.PsiModifierListOwner;
 import com.intellij.psi.PsiNameValuePair;
 import com.intellij.psi.PsiParameter;
 import com.intellij.psi.search.GlobalSearchScope;
 import com.intellij.psi.util.PsiUtil;
 import com.intellij.psi.xml.XmlAttribute;
 import com.intellij.psi.xml.XmlFile;
 import com.intellij.psi.xml.XmlTag;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Mike Brock
  */
 public class Util {
   private static final Key<DataFieldCacheHolder> dataFieldsCacheKey = Key.create("dataFieldsCache");
 
   public static class TemplateMetaData {
     private final TemplateReference templateReference;
     private boolean defaultReference;
     private final PsiNameValuePair attribute;
     private final PsiClass templateClass;
     private final VirtualFile templateFile;
     private final XmlTag rootTag;
 
     public TemplateMetaData(TemplateReference templateReference,
                             boolean defaultReference,
                             PsiNameValuePair attribute,
                             PsiClass templateClass,
                             VirtualFile templateFile,
                             XmlTag rootTag) {
       this.templateReference = templateReference;
       this.defaultReference = defaultReference;
       this.attribute = attribute;
       this.templateClass = templateClass;
       this.templateFile = templateFile;
       this.rootTag = rootTag;
     }
 
     public TemplateReference getTemplateReference() {
       return templateReference;
     }
 
     public boolean isDefaultReference() {
       return defaultReference;
     }
 
     public PsiClass getTemplateClass() {
       return templateClass;
     }
 
     public VirtualFile getTemplateFile() {
       return templateFile;
     }
 
     public PsiNameValuePair getAttribute() {
       return attribute;
     }
 
     public XmlTag getRootTag() {
       return rootTag;
     }
   }
 
   public static class TemplateReference {
     private final String fileName;
     private final String rootNode;
 
     public TemplateReference(String fileName, String rootNode) {
       this.fileName = fileName;
       this.rootNode = rootNode;
     }
 
     public String getFileName() {
       return fileName;
     }
 
     public String getRootNode() {
       return rootNode;
     }
   }
 
   public static class DataFieldReference {
     private final XmlTag tag;
     private final String dataFieldName;
 
     public DataFieldReference(XmlTag tag, String dataFieldName) {
       this.tag = tag;
       this.dataFieldName = dataFieldName;
     }
 
     public XmlTag getTag() {
       return tag;
     }
 
     public String getDataFieldName() {
       return dataFieldName;
     }
 
     @Override
     public String toString() {
       return "DataFieldReference{" +
           "tag=" + tag +
           ", dataFieldName='" + dataFieldName + '\'' +
           '}';
     }
   }
 
   public static class DataFieldCacheHolder {
     private final long time;
     private final XmlTag tag;
     private final Map<String, DataFieldReference> value;
 
     public DataFieldCacheHolder(long time, XmlTag tag, Map<String, DataFieldReference> value) {
       this.time = time;
       this.tag = tag;
       this.value = value;
     }
 
     public long getTime() {
       return time;
     }
 
     public XmlTag getTag() {
       return tag;
     }
 
     public Map<String, DataFieldReference> getValue() {
       return value;
     }
   }
 
   public static class AnnotationValueElement {
     private final boolean isDefault;
     private final String value;
     private final PsiElement logicalElement;
 
     public AnnotationValueElement(boolean aDefault, String value, PsiElement logicalElement) {
       isDefault = aDefault;
       this.value = value;
       this.logicalElement = logicalElement;
     }
 
     public boolean isDefault() {
       return isDefault;
     }
 
     public String getValue() {
       return value;
     }
 
     public PsiElement getLogicalElement() {
       return logicalElement;
     }
   }
 
   public static class AnnotationSearchResult {
     private final PsiAnnotation annotation;
     private final PsiElement owningElement;
 
     public AnnotationSearchResult(PsiAnnotation annotation, PsiElement owningElement) {
       this.annotation = annotation;
       this.owningElement = owningElement;
     }
 
     public PsiAnnotation getAnnotation() {
       return annotation;
     }
 
     public PsiElement getOwningElement() {
       return owningElement;
     }
   }
 
   public static TemplateReference parseReference(String referenceString) {
     int nodeSpecifier = referenceString.indexOf('#');
 
     final String fileName;
     final String rootNode;
     if (nodeSpecifier == -1) {
       fileName = referenceString;
       rootNode = "";
     }
     else {
       fileName = referenceString.substring(0, nodeSpecifier);
       rootNode = referenceString.substring(nodeSpecifier + 1);
     }
 
     return new TemplateReference(fileName.trim(), rootNode.trim());
   }
 
 
   /**
    * Finds all "data-field" tags for the specified {@link TemplateMetaData}.
    *
    * @param templateMetaData
    *     the {@link TemplateMetaData} to use to find the tag.
    * @param project
    *     the IntelliJ <tt>Project</tt> reference.
    * @param includeRoot
    *     boolean indicating whether or not to consider the root node in the search. If set to <tt>false</tt>,
    *     only children of the node are considered. If Set to <tt>true</tt>, the root node itself is checked
    *     to see if it is a data-field.
    *
    * @return
    */
   public static Map<String, DataFieldReference> findAllDataFieldTags(TemplateMetaData templateMetaData,
                                                                      Project project,
                                                                      boolean includeRoot) {
     return findAllDataFieldTags(templateMetaData.getTemplateFile(), templateMetaData.getRootTag(), project, includeRoot);
   }
 
   private static Map<String, DataFieldReference> findAllDataFieldTags(VirtualFile vf,
                                                                       XmlTag rootTag,
                                                                       Project project,
                                                                       boolean includeRoot) {
     if (vf == null) {
       return Collections.emptyMap();
     }
 
     final PsiManager instance = PsiManager.getInstance(project);
     final PsiFile file = instance.findFile(vf);
 
     if (file == null) {
       return Collections.emptyMap();
     }
 
     if (rootTag == null) {
       rootTag = ((XmlFile) file).getRootTag();
     }
 
     return findAllDataFieldTags(file, rootTag, includeRoot);
   }
 
   private static Map<String, DataFieldReference> findAllDataFieldTags(PsiFile file, XmlTag rootTag, boolean includeRoot) {
     final DataFieldCacheHolder copyableUserData = file.getCopyableUserData(dataFieldsCacheKey);
     if (copyableUserData != null
         && copyableUserData.getTime() == file.getModificationStamp()
         && copyableUserData.getTag() == rootTag) {
       return copyableUserData.getValue();
     }
 
     final Map<String, DataFieldReference> allDataFieldTags = findAllDataFieldTags(rootTag, includeRoot);
     file.putCopyableUserData(dataFieldsCacheKey, new DataFieldCacheHolder(file.getModificationStamp(), rootTag, allDataFieldTags));
     return allDataFieldTags;
   }
 
   private static Map<String, DataFieldReference> findAllDataFieldTags(XmlTag rootTag, boolean includeRoot) {
     Map<String, DataFieldReference> references = new HashMap<String, DataFieldReference>();
    if (rootTag == null) {
      return references;
    }

    if (includeRoot &&  rootTag.getAttribute("data-field") != null) {
       final String value = rootTag.getAttribute("data-field").getValue();
       references.put(value, new DataFieldReference(rootTag, value));
     }
     _findDataFieldTags(references, rootTag);
     return references;
   }
 
   private static void _findDataFieldTags(Map<String, DataFieldReference> foundTags, XmlTag root) {
     for (XmlTag xmlTag : root.getSubTags()) {
       XmlAttribute xmlAttribute = xmlTag.getAttribute("data-field");
       if (xmlAttribute != null) {
         foundTags.put(xmlAttribute.getValue(), new DataFieldReference(xmlTag, xmlAttribute.getValue()));
       }
       _findDataFieldTags(foundTags, xmlTag);
     }
   }
 
 
   public static PsiAnnotation findTemplatedAnnotation(PsiElement element) {
     final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
 
     final PsiAnnotation[] annotations = topLevelClass.getModifierList().getAnnotations();
     for (PsiAnnotation psiAnnotation : annotations) {
       if (psiAnnotation.getQualifiedName().equals(ErraiUISupport.TEMPLATED_ANNOTATION_NAME)) {
         return psiAnnotation;
       }
     }
     return null;
   }
 
   public static TemplateMetaData getTemplateMetaData(PsiElement element, Project project) {
     return getTemplateMetaData(findTemplatedAnnotation(element), project);
   }
 
   public static TemplateMetaData getTemplateMetaData(PsiAnnotation annotation, Project project) {
     if (annotation == null) return null;
 
     if (!annotation.getQualifiedName().equals(ErraiUISupport.TEMPLATED_ANNOTATION_NAME)) {
       annotation = findTemplatedAnnotation(annotation);
       if (annotation == null) return null;
     }
 
 
     final PsiClass templateClass = PsiUtil.getTopLevelClass(annotation);
     final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
 
     final String templateName;
     if (attributes.length == 0) {
       templateName = templateClass.getName() + ".html";
     }
     else {
       final PsiLiteralExpression literalExpression = (PsiLiteralExpression) attributes[0].getValue();
       final String text = literalExpression.getText();
       templateName = text.substring(1, text.length() - 1);
     }
 
     final PsiFile containingFile = templateClass.getContainingFile().getOriginalFile();
     PsiDirectory containerDir = containingFile.getParent();
 
     final Util.TemplateReference reference = Util.parseReference(templateName);
 
     final VirtualFile virtualFile = containerDir.getVirtualFile();
    VirtualFile fileByRelativePath = virtualFile.findFileByRelativePath(reference.getFileName());
    if (fileByRelativePath != null && fileByRelativePath.isDirectory()) {
      fileByRelativePath = null;
    }
 
     final Map<String, DataFieldReference> allDataFieldTags = findAllDataFieldTags(fileByRelativePath, null, project, true);
 
     final XmlTag rootTag;
     if (fileByRelativePath == null) {
       rootTag = null;
     }
     else if (reference.getRootNode().equals("")) {
       final PsiFile file = PsiManager.getInstance(project).findFile(fileByRelativePath);
       if (file != null) {
         rootTag = ((XmlFile) file).getRootTag();
       }
       else {
         rootTag = null;
       }
     }
     else {
       final DataFieldReference dataFieldReference = allDataFieldTags.get(reference.getRootNode());
       if (dataFieldReference != null) {
         rootTag = dataFieldReference.getTag();
       }
       else {
         rootTag = null;
       }
     }
 
     return new TemplateMetaData(reference,
         attributes.length == 0,
         attributes.length == 0 ? null : attributes[0],
         templateClass,
         fileByRelativePath,
         rootTag);
   }
 
   public static PsiElement getImmediateOwnerElement(PsiElement element) {
     PsiElement el = element;
     while ((el = el.getParent()) != null) {
       if (el instanceof PsiField) {
         return el;
       }
       else if (el instanceof PsiParameter) {
         return el;
       }
       else if (el instanceof PsiMethod) {
         return el;
       }
       else if (el instanceof PsiClass) {
         return el;
       }
     }
     return null;
   }
 
   private static PsiElement findFieldOrMethod(final PsiElement element) {
     PsiElement e = element;
     do {
       if (e instanceof PsiField) {
         return e;
       }
       else if (e instanceof PsiMethod) {
         return e;
       }
       else if (e instanceof PsiClass) {
         return null;
       }
     }
     while ((e = e.getParent()) != null);
     return null;
   }
 
   public static PsiAnnotation getAnnotationFromElement(PsiElement element, String annotationType) {
     if (element instanceof PsiModifierListOwner) {
       final PsiModifierList modifierList = ((PsiModifierListOwner) element).getModifierList();
       if (modifierList != null) {
         for (PsiAnnotation annotation : modifierList.getAnnotations()) {
           if (annotationType.equals(annotation.getQualifiedName())) {
             return annotation;
           }
         }
       }
     }
     return null;
   }
 
   public static boolean elementIsAnnotated(PsiElement element, String annotationType) {
     return getAnnotationFromElement(element, annotationType) != null;
   }
 
   public static boolean fieldOrMethodIsAnnotated(PsiElement element, String annotationType) {
     final PsiElement e = findFieldOrMethod(element);
 
     if (e instanceof PsiField) {
       final PsiModifierList modifierList = ((PsiField) e).getModifierList();
 
       if (modifierList != null) {
         for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
           final String qualifiedName = psiAnnotation.getQualifiedName();
           if (qualifiedName != null && qualifiedName.equals(annotationType)) {
             return true;
           }
         }
       }
     }
     else if (e instanceof PsiMethod) {
       for (PsiAnnotation psiAnnotation : ((PsiMethod) e).getModifierList().getAnnotations()) {
         final String qualifiedName = psiAnnotation.getQualifiedName();
         if (qualifiedName != null && qualifiedName.equals(annotationType)) {
           return true;
         }
       }
     }
 
     return false;
   }
 
   public static Collection<String> extractDataFieldList(Collection<AnnotationSearchResult> dataFieldElements) {
     final List<String> elements = new ArrayList<String>();
     for (AnnotationSearchResult element : dataFieldElements) {
       elements.add(getValueStringFromAnnotationWithDefault(element.getAnnotation()).getValue());
     }
     return elements;
   }
 
   public static AnnotationValueElement getValueStringFromAnnotationWithDefault(PsiAnnotation annotation) {
     final PsiAnnotationParameterList parameterList = annotation.getParameterList();
     final PsiNameValuePair[] attributes = parameterList.getAttributes();
     final PsiElement logicalElement = Util.getImmediateOwnerElement(annotation);
 
     if (logicalElement == null) {
       return null;
     }
 
     final String value;
     final PsiElement errorElement;
 
     if (attributes.length == 0) {
       value = Util.getNameOfElement(logicalElement);
       errorElement = annotation;
     }
     else {
       final String text = attributes[0].getText();
       value = text.substring(1, text.length() - 1);
       errorElement = attributes[0];
     }
 
     return new AnnotationValueElement(attributes.length == 0, value, errorElement);
   }
 
   public static Collection<AnnotationSearchResult> findAllAnnotatedElements(PsiElement element, String annotation) {
     final PsiClass bean = PsiUtil.getTopLevelClass(element);
 
     final List<AnnotationSearchResult> elementList = new ArrayList<AnnotationSearchResult>();
     PsiAnnotation a;
     for (PsiField e : bean.getAllFields()) {
       a = getAnnotationFromElement(e, annotation);
       if (a != null) {
         elementList.add(new AnnotationSearchResult(a, e));
       }
     }
 
     for (PsiMethod e : bean.getAllMethods()) {
       a = getAnnotationFromElement(e, annotation);
 
       if (a != null) {
         elementList.add(new AnnotationSearchResult(a, e));
       }
 
       for (PsiParameter p : e.getParameterList().getParameters()) {
         a = getAnnotationFromElement(p, annotation);
 
         if (a != null) {
           elementList.add(new AnnotationSearchResult(a, p));
         }
       }
     }
     for (PsiMethod e : bean.getConstructors()) {
       a = getAnnotationFromElement(e, annotation);
 
       if (a != null) {
         elementList.add(new AnnotationSearchResult(a, e));
       }
 
       for (PsiParameter p : e.getParameterList().getParameters()) {
         a = getAnnotationFromElement(p, annotation);
 
         if (a != null) {
           elementList.add(new AnnotationSearchResult(a, p));
         }
       }
     }
 
     return elementList;
   }
 
   public static Map<String, ConsolidateDataFieldElementResult> getConsolidatedDataFields(PsiElement element, Project project) {
 
     final Util.TemplateMetaData metaData = Util.getTemplateMetaData(element, project);
     final String beanClass = PsiUtil.getTopLevelClass(element).getQualifiedName();
 
     final Map<String, ConsolidateDataFieldElementResult> results = new LinkedHashMap<String, ConsolidateDataFieldElementResult>();
 
     final Collection<Util.AnnotationSearchResult> allInjectionPoints
         = Util.findAllAnnotatedElements(element, ErraiUISupport.DATAFIELD_ANNOTATION_NAME);
 
     for (Util.AnnotationSearchResult r : allInjectionPoints) {
       final String value = Util.getValueStringFromAnnotationWithDefault(r.getAnnotation()).getValue();
       results.put(value, new ConsolidateDataFieldElementResult(value, beanClass, r.getOwningElement(), true));
     }
 
     final Map<String, Util.DataFieldReference> allDataFieldTags = Util.findAllDataFieldTags(metaData, project, false);
     for (Util.DataFieldReference ref : allDataFieldTags.values()) {
       if (results.containsKey(ref.getDataFieldName())) continue;
 
       results.put(ref.getDataFieldName(), new ConsolidateDataFieldElementResult(ref.getDataFieldName(),
           metaData.getTemplateReference().getFileName(), ref.getTag(), false));
     }
 
     return results;
   }
 
 
   public static boolean fieldElementIsInitialized(PsiElement element) {
     if (element instanceof PsiField) {
       final PsiField psiField = (PsiField) element;
       if (psiField.getInitializer() != null) {
         return !"null".equals(psiField.getInitializer().getText());
       }
     }
     return false;
   }
 
   public static String getNameOfElement(PsiElement element) {
     if (element instanceof PsiField) {
       return ((PsiField) element).getName();
     }
     else if (element instanceof PsiParameter) {
       return ((PsiParameter) element).getName();
     }
     return null;
   }
 
   public static PsiClass getTypeOfElement(PsiElement element, Project project) {
     final String name;
     if (element instanceof PsiField) {
       name = ((PsiField) element).getType().getCanonicalText();
     }
     else if (element instanceof PsiParameter) {
       name = ((PsiParameter) element).getType().getCanonicalText();
     }
     else {
       return null;
     }
     return JavaPsiFacade.getInstance(project).findClass(name, GlobalSearchScope.allScope(project));
   }
 
 
   public static boolean typeIsAssignableFrom(PsiClass from, String... toFQN) {
     if (from == null) return false;
 
     Set<String> matching = new HashSet<String>(Arrays.asList(toFQN));
     PsiClass cls = from;
     do {
       if (matching.contains(cls.getQualifiedName())) return true;
 
       for (PsiClass interfaceClass : cls.getInterfaces()) {
         if (typeIsAssignableFrom(interfaceClass, toFQN)) {
           return true;
         }
       }
     }
     while ((cls = cls.getSuperClass()) != null);
 
     return false;
   }
 }
