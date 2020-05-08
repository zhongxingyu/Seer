 package org.jboss.errai.idea.plugin.util;
 
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Key;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.JavaPsiFacade;
 import com.intellij.psi.PsiAnnotation;
 import com.intellij.psi.PsiAnnotationMemberValue;
 import com.intellij.psi.PsiAnnotationParameterList;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiClassObjectAccessExpression;
 import com.intellij.psi.PsiClassType;
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
 import com.intellij.psi.PsiType;
 import com.intellij.psi.search.GlobalSearchScope;
 import com.intellij.psi.util.PsiUtil;
 import com.intellij.psi.xml.XmlAttribute;
 import com.intellij.psi.xml.XmlAttributeValue;
 import com.intellij.psi.xml.XmlFile;
 import com.intellij.psi.xml.XmlTag;
 import org.jboss.errai.idea.plugin.ui.TemplateDataField;
 import org.jboss.errai.idea.plugin.ui.model.ConsolidateDataFieldElementResult;
 import org.jboss.errai.idea.plugin.ui.model.DataFieldCacheHolder;
 import org.jboss.errai.idea.plugin.ui.model.TemplateExpression;
 import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
 
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
 import java.util.Stack;
 
 /**
  * @author Mike Brock
  */
 public class Util {
   public static final String INTELLIJ_MAGIC_STRING = "IntellijIdeaRulezzz";
   // private static final Key<Set<PsiClass>> templateClassOwners = Key.create("templateClassOwners");
 
   public static final Key<Ownership> OWNERSHIP_CACHE = Key.create("OWNERSHIP_CACHE");
   private static final Key<DataFieldCacheHolder> dataFieldsCacheKey = Key.create("dataFieldsCache");
 
   public static PsiClass getErasedTypeParam(Project project, String signature) {
     final String typeParam;
     int typeParamBegin = signature.indexOf('<');
     if (typeParamBegin == -1) {
       typeParam = null;
     }
     else {
       String s = signature.substring(typeParamBegin + 1, signature.indexOf('>'));
       typeParam = getErasedCanonicalText(s);
     }
 
     if (typeParam != null) {
       return JavaPsiFacade.getInstance(project)
           .findClass(typeParam, GlobalSearchScope.allScope(project));
     }
     return null;
   }
 
   public static List<String> getErasedTypeParamsCanonicalText(final String signature) {
    if (signature == null) {
      return Collections.emptyList();
    }
     int typeParamBegin = signature.indexOf('<');
     if (typeParamBegin == -1) {
       return Collections.emptyList();
     }
     List<String> erasedParms = new ArrayList<String>();
     String str = signature.substring(typeParamBegin + 1);
 
     boolean noOpen = str.indexOf('<') == -1;
     int start = 0;
     int idx = 0;
     boolean skipNext = false;
 
     while (idx < str.length()) {
 
       int nextComma = str.indexOf(',', start);
       idx = nextComma;
 
       if (!noOpen) {
         int nextOpen = str.indexOf('<', start);
         if (nextOpen != -1 && nextOpen < nextComma) {
           idx = nextOpen;
           skipNext = true;
         }
       }
 
       if (idx == -1) {
         idx = str.length() - 1;
       }
 
       String tok = str.substring(start, idx).trim();
       if (tok.length() > 0) {
         erasedParms.add(tok);
       }
 
       if (skipNext) {
         idx++;
         int b = 1;
         Capture:
         for (; idx < str.length(); idx++) {
           switch (str.charAt(idx)) {
             case '<':
               b++;
             case '>':
               b--;
               if (b == 0) {
                 break Capture;
               }
           }
         }
 
         idx++;
         skipNext = false;
         start = idx;
       }
       else {
         start = ++idx;
       }
     }
 
     return erasedParms;
   }
 
   public static String getErasedCanonicalText(String typeName) {
     int paramStart = typeName.indexOf('<');
     if (paramStart != -1) {
       typeName = typeName.substring(0, paramStart);
     }
     return typeName;
   }
 
   public static class Ownership {
     private final Set<PsiClass> templateClasses = new HashSet<PsiClass>();
     private final PsiFile associatedFile;
     private final long lastModified;
 
     public Ownership(PsiFile associatedFile) {
       this.associatedFile = associatedFile;
       this.lastModified = associatedFile.getModificationStamp();
     }
 
     public Set<PsiClass> getTemplateClasses() {
       return templateClasses;
     }
 
     public long getLastModified() {
       return lastModified;
     }
 
     public boolean isValid() {
       return true;
     }
   }
 
   public static TemplateExpression parseReference(String referenceString) {
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
 
     return new TemplateExpression(fileName.trim(), rootNode.trim());
   }
 
 
   /**
    * Finds all "data-field" tags for the specified {@link org.jboss.errai.idea.plugin.ui.model.TemplateMetaData}.
    *
    * @param templateMetaData
    *     the {@link org.jboss.errai.idea.plugin.ui.model.TemplateMetaData} to use to find the tag.
    * @param project
    *     the IntelliJ <tt>Project</tt> reference.
    * @param includeRoot
    *     boolean indicating whether or not to consider the root node in the search. If set to <tt>false</tt>,
    *     only children of the node are considered. If Set to <tt>true</tt>, the root node itself is checked
    *     to see if it is a data-field.
    *
    * @return
    */
   public static Map<String, TemplateDataField> findAllDataFieldTags(TemplateMetaData templateMetaData,
                                                                     Project project,
                                                                     boolean includeRoot) {
     VirtualFile vf = templateMetaData.getTemplateFile();
     XmlTag rootTag = templateMetaData.getRootTag();
     if (vf == null) {
       return Collections.emptyMap();
     }
 
     final PsiManager instance = PsiManager.getInstance(project);
     final PsiFile file = instance.findFile(vf);
 
     if (file == null) {
       return Collections.emptyMap();
     }
 
     final XmlFile xmlFile = (XmlFile) file;
     if (rootTag == null) {
 
       rootTag = xmlFile.getRootTag();
     }
 
     declareOwner(xmlFile, templateMetaData.getTemplateClass());
 
     return findAllDataFieldTags(file, rootTag, includeRoot);
   }
 
   private static Map<String, TemplateDataField> findAllDataFieldTags(VirtualFile vf,
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
 
   public static Map<String, TemplateDataField> findAllDataFieldTags(final PsiFile templateFile,
                                                                     final XmlTag rootTag,
                                                                     final boolean includeRoot) {
     return getOrCreateCache(dataFieldsCacheKey, templateFile, new CacheProvider<DataFieldCacheHolder>() {
       @Override
       public DataFieldCacheHolder provide() {
         final Map<String, TemplateDataField> allDataFieldTags = findAllDataFieldTags(rootTag, includeRoot);
         return new DataFieldCacheHolder(templateFile.getModificationStamp(), rootTag, allDataFieldTags);
       }
 
       @Override
       public boolean isCacheValid(DataFieldCacheHolder dataFieldCacheHolder) {
         return dataFieldCacheHolder.getTime() == templateFile.getModificationStamp();
       }
     }).getValue();
   }
 
   private static Map<String, TemplateDataField> findAllDataFieldTags(XmlTag rootTag, boolean includeRoot) {
     Map<String, TemplateDataField> references = new HashMap<String, TemplateDataField>();
     if (rootTag == null) {
       return references;
     }
 
     if (includeRoot && rootTag.getAttribute("data-field") != null) {
       final String value = rootTag.getAttribute("data-field").getValue();
       references.put(value, new TemplateDataField(rootTag, value));
     }
     _findDataFieldTags(references, rootTag);
     return references;
   }
 
   private static void _findDataFieldTags(Map<String, TemplateDataField> foundTags, XmlTag root) {
     for (XmlTag xmlTag : root.getSubTags()) {
       XmlAttribute xmlAttribute = xmlTag.getAttribute("data-field");
       if (xmlAttribute != null) {
         foundTags.put(xmlAttribute.getValue(), new TemplateDataField(xmlTag, xmlAttribute.getValue()));
       }
       _findDataFieldTags(foundTags, xmlTag);
     }
   }
 
 
   public static PsiAnnotation findTemplatedAnnotation(PsiElement element) {
     final PsiClass topLevelClass;
 
     if (element.getParent() == null) {
       if (element instanceof PsiClass) {
         topLevelClass = (PsiClass) element;
       }
       return null;
     }
     else {
       topLevelClass = PsiUtil.getTopLevelClass(element);
     }
 
     final PsiAnnotation[] annotations = topLevelClass.getModifierList().getAnnotations();
     for (PsiAnnotation psiAnnotation : annotations) {
       if (psiAnnotation.getQualifiedName().equals(Types.TEMPLATED_ANNOTATION_NAME)) {
         return psiAnnotation;
       }
     }
     return null;
   }
 
   public static TemplateMetaData getTemplateMetaData(PsiElement element) {
     return getTemplateMetaData(findTemplatedAnnotation(element), element.getProject());
   }
 
   private static TemplateMetaData getTemplateMetaData(PsiAnnotation annotation, Project project) {
     if (annotation == null) return null;
 
     if (!annotation.getQualifiedName().equals(Types.TEMPLATED_ANNOTATION_NAME)) {
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
       if (literalExpression == null) {
         return null;
       }
 
       String text = literalExpression.getText().replace(INTELLIJ_MAGIC_STRING, "");
       templateName = text.substring(1, text.length() - 1);
     }
 
     final PsiFile containingFile = templateClass.getContainingFile().getOriginalFile();
     PsiDirectory containerDir = containingFile.getParent();
 
     final TemplateExpression reference = Util.parseReference(templateName);
 
     final String fileName;
     if ("".equals(reference.getFileName())) {
       fileName = templateClass.getName() + ".html";
     }
     else {
       fileName = reference.getFileName();
     }
 
     final VirtualFile virtualFile = containerDir.getVirtualFile();
     VirtualFile fileByRelativePath = virtualFile.findFileByRelativePath(fileName);
     if (fileByRelativePath != null && fileByRelativePath.isDirectory()) {
       fileByRelativePath = null;
     }
 
     final Map<String, TemplateDataField> allDataFieldTags = findAllDataFieldTags(fileByRelativePath, null, project, true);
 
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
       final TemplateDataField dataFieldReference = allDataFieldTags.get(reference.getRootNode());
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
         rootTag,
         project);
   }
 
   public static PsiElement getImmediateOwnerElement(PsiElement element) {
     PsiElement el = element;
     do {
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
     while ((el = el.getParent()) != null);
 
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
 
     final TemplateMetaData metaData = Util.getTemplateMetaData(element);
     final String beanClass = PsiUtil.getTopLevelClass(element).getQualifiedName();
 
     final Map<String, ConsolidateDataFieldElementResult> results = new LinkedHashMap<String, ConsolidateDataFieldElementResult>();
 
     final Collection<AnnotationSearchResult> allInjectionPoints
         = Util.findAllAnnotatedElements(element, Types.DATAFIELD_ANNOTATION_NAME);
 
     for (AnnotationSearchResult r : allInjectionPoints) {
       final String value = Util.getValueStringFromAnnotationWithDefault(r.getAnnotation()).getValue();
       results.put(value, new ConsolidateDataFieldElementResult(value, beanClass, r.getOwningElement(), true));
     }
 
     final Map<String, TemplateDataField> allDataFieldTags = Util.findAllDataFieldTags(metaData, project, false);
     for (TemplateDataField ref : allDataFieldTags.values()) {
       final XmlAttributeValue valueElement = ref.getTag().getAttribute("data-field").getValueElement();
       if (results.containsKey(ref.getDataFieldName())) {
         results.get(ref.getDataFieldName()).setLinkingElement(valueElement);
         continue;
       }
 
       results.put(ref.getDataFieldName(), new ConsolidateDataFieldElementResult(ref.getDataFieldName(),
           metaData.getTemplateExpression().getFileName(), valueElement, false));
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
 
 
   public static PsiClass getTypeInformation(PsiClass from, String... toFQN) {
     if (from == null) return null;
 
     Set<String> matching = new HashSet<String>(Arrays.asList(toFQN));
     PsiClass cls = from;
     do {
       if (matching.contains(cls.getQualifiedName())) return cls;
 
       for (PsiClass interfaceClass : cls.getInterfaces()) {
         if (typeIsAssignableFrom(interfaceClass, toFQN)) {
           return interfaceClass;
         }
       }
     }
     while ((cls = cls.getSuperClass()) != null);
 
     return null;
   }
 
   public static SuperTypeInfo getTypeInformation(PsiClass from, String toFQN) {
     final PsiClassType[] superTypes = from.getSuperTypes();
 
     Stack<PsiClassType> toSearch = new Stack<PsiClassType>();
     for (PsiClassType type : superTypes) {
       toSearch.push(type);
     }
 
     while (!toSearch.isEmpty()) {
       PsiClassType type = toSearch.pop();
       for (PsiType psiType : type.getSuperTypes()) {
         if (psiType instanceof PsiClassType) {
           PsiClassType t = (PsiClassType) psiType;
           if (!t.getCanonicalText().equals("java.lang.Object")) {
             toSearch.push(t);
           }
         }
       }
 
       if (type.getCanonicalText().startsWith(toFQN)) {
         final String className = getErasedCanonicalText(type.getCanonicalText());
         final List<String> parms = getErasedTypeParamsCanonicalText(type.getCanonicalText());
         return new SuperTypeInfo(className, parms);
       }
     }
     return null;
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
 
   public static void declareOwner(final PsiFile file, final PsiClass psiClass) {
     if (psiClass == null) {
       return;
     }
     getOrCreateCache(OWNERSHIP_CACHE, file, new CacheProvider<Ownership>() {
       @Override
       public Ownership provide() {
         //     System.out.println("declareOwner: " + file.getName() + "; " + psiClass.getQualifiedName());
         return new Ownership(file);
       }
 
       @Override
       public boolean isCacheValid(Ownership ownership) {
         return ownership.isValid();
       }
     }).getTemplateClasses().add(psiClass);
   }
 
   public static Set<PsiClass> getOwners(PsiFile file) {
     final Ownership ownership = file.getOriginalFile().getCopyableUserData(OWNERSHIP_CACHE);
     if (ownership == null || !ownership.isValid()) {
       return Collections.emptySet();
     }
     else {
       return ownership.getTemplateClasses();
     }
   }
 
 
   public static PsiAnnotationMemberValue getAnnotationMemberValue(PsiAnnotation annotation, String attributeName) {
     final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
     for (PsiNameValuePair attribute : attributes) {
       if (attributeName.equals(attribute.getName())) {
         final PsiAnnotationMemberValue value = attribute.getValue();
         if (value != null) {
           return value;
         }
         break;
       }
     }
     return null;
   }
 
   public static String getAttributeValue(PsiAnnotation annotation, String attributeName, DefaultPolicy policy) {
     final PsiAnnotationMemberValue value = getAnnotationMemberValue(annotation, attributeName);
     if (value != null) {
       if (value instanceof PsiClassObjectAccessExpression) {
         final PsiType type = ((PsiClassObjectAccessExpression) value).getType();
         if (type == null) {
           return null;
         }
         return type.getCanonicalText();
       }
       else {
         final String text = value.getText();
         return text.substring(1, text.length() - 1);
       }
     }
 
     if (policy == DefaultPolicy.OWNER_IDENTIFIER_NAME) {
       return PsiUtil.getName(getImmediateOwnerElement(annotation));
     }
     else {
       return null;
     }
   }
 
   public static <T> T getOrCreateCache(Key<T> cacheKey, PsiElement element, CacheProvider<T> provider) {
     final PsiFile containingFile;
     if (element instanceof PsiFile) {
       containingFile = (PsiFile) element;
     }
     else {
       final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
 
       if (topLevelClass == null) {
         return provider.provide();
       }
       containingFile = topLevelClass.getContainingFile();
     }
 
     if (containingFile == null) {
       return provider.provide();
     }
 
     final PsiFile originalFile = containingFile.getOriginalFile();
     T copyableUserData = originalFile.getCopyableUserData(cacheKey);
 
     if (copyableUserData != null) {
       if (!provider.isCacheValid(copyableUserData)) {
         copyableUserData = null;
         //  System.out.println("key '" + cacheKey.toString() + "' has been invalidated");
       }
     }
 
     if (copyableUserData == null) {
       originalFile.putCopyableUserData(cacheKey, copyableUserData = provider.provide());
     }
     else {
       //  System.out.println("servicing key '" + cacheKey.toString() + "' from cache");
     }
 
     return copyableUserData;
   }
 }
