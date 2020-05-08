 /*
  * Copyright 2013 Red Hat, Inc.
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package org.jboss.errai.idea.plugin.ui;
 
 import static com.intellij.psi.search.GlobalSearchScope.projectScope;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.Multimap;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Key;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.JavaPsiFacade;
 import com.intellij.psi.PsiAnnotation;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiDirectory;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiLiteralExpression;
 import com.intellij.psi.PsiManager;
 import com.intellij.psi.PsiModifierList;
 import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.search.FilenameIndex;
 import com.intellij.psi.search.GlobalSearchScope;
 import com.intellij.psi.search.searches.ClassInheritorsSearch;
 import com.intellij.psi.util.PsiUtil;
 import com.intellij.psi.xml.XmlAttribute;
 import com.intellij.psi.xml.XmlAttributeValue;
 import com.intellij.psi.xml.XmlFile;
 import com.intellij.psi.xml.XmlTag;
 import org.jboss.errai.idea.plugin.ui.model.ConsolidateDataFieldElementResult;
 import org.jboss.errai.idea.plugin.ui.model.DataFieldCacheHolder;
 import org.jboss.errai.idea.plugin.ui.model.TemplateExpression;
 import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
 import org.jboss.errai.idea.plugin.util.AnnotationSearchResult;
 import org.jboss.errai.idea.plugin.util.AnnotationValueElement;
 import org.jboss.errai.idea.plugin.util.CacheProvider;
 import org.jboss.errai.idea.plugin.util.Types;
 import org.jboss.errai.idea.plugin.util.Util;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Mike Brock
  */
 public class TemplateUtil {
   public static final String DATA_FIELD_TAG_ATTRIBUTE = "data-field";
   public static final String ID_ATTRIBUTE = "id";
 
   private static final Key<DataFieldCacheHolder> dataFieldsCacheKey = Key.create("dataFieldsCache");
 
   public static DataFieldExistence dataFieldExistenceCheck(PsiAnnotation annotation, TemplateMetaData metaData) {
     final Multimap<String, TemplateDataField> inScopeDataFields = metaData.getAllDataFieldsInTemplate(false);
     final Map<String, ConsolidateDataFieldElementResult> dataFields = metaData.getConsolidatedDataFields();
 
     final AnnotationValueElement annoValueEl = Util.getValueStringFromAnnotationWithDefault(annotation);
     final String annoValue = annoValueEl.getValue();
 
     final Collection<TemplateDataField> result = inScopeDataFields.get(annoValue);
     if (result.isEmpty()) {
       if (dataFields.containsKey(annoValue)) {
         return DataFieldExistence.OUT_OF_SCOPE;
       }
       else {
         return DataFieldExistence.DOES_NOT_EXIST;
       }
     }
     else {
       return DataFieldExistence.EXISTS;
     }
   }
 
   public static enum DataFieldExistence {
     EXISTS, DOES_NOT_EXIST, OUT_OF_SCOPE;
   }
 
   public static class Ownership {
     private final Set<PsiClass> templateClasses;
 
     public Ownership(Set<PsiClass> templateClasses) {
       this.templateClasses = templateClasses;
     }
 
     public Set<PsiClass> getTemplateClasses() {
       return templateClasses;
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
    * @return Map of all datafields in the template class.
    */
   @NotNull
   public static Multimap<String, TemplateDataField> findAllDataFieldTags(TemplateMetaData templateMetaData,
                                                                          Project project,
                                                                          boolean includeRoot) {
     if (!templateMetaData.getTemplateExpression().hasRootNode()) {
       includeRoot = true;
     }
 
     VirtualFile vf = templateMetaData.getTemplateFile();
     XmlTag rootTag = templateMetaData.getRootTag();
     if (vf == null) {
       return ImmutableMultimap.of();
     }
 
     final PsiManager instance = PsiManager.getInstance(project);
     final PsiFile file = instance.findFile(vf);
 
     if (file == null) {
       return ImmutableMultimap.of();
     }
 
     final XmlFile xmlFile = (XmlFile) file;
     if (rootTag == null) {
       rootTag = xmlFile.getRootTag();
     }
 
     return findAllDataFieldTags(file, rootTag, includeRoot);
   }
 
   private static Multimap<String, TemplateDataField> findAllDataFieldTags(VirtualFile vf,
                                                                           XmlTag rootTag,
                                                                           Project project,
                                                                           boolean includeRoot) {
     if (vf == null) {
       return ImmutableMultimap.of();
     }
 
     final PsiManager instance = PsiManager.getInstance(project);
     final PsiFile file = instance.findFile(vf);
 
     if (file == null) {
       return ImmutableMultimap.of();
 
     }
 
     if (rootTag == null) {
       rootTag = ((XmlFile) file).getRootTag();
     }
     else {
     }
 
     return findAllDataFieldTags(file, rootTag, includeRoot);
   }
 
   @NotNull
   public static Multimap<String, TemplateDataField> findAllDataFieldTags(final PsiFile templateFile,
                                                                          final XmlTag rootTag,
                                                                          final boolean includeRoot) {
     final Multimap<String, TemplateDataField> value
         = Util.getOrCreateCache(dataFieldsCacheKey, templateFile, new CacheProvider<DataFieldCacheHolder>() {
       @Override
       public DataFieldCacheHolder provide() {
         final Multimap<String, TemplateDataField> allDataFieldTags = findAllDataFieldTags(rootTag, includeRoot);
         return new DataFieldCacheHolder(templateFile.getModificationStamp(), allDataFieldTags);
       }
 
       @Override
       public boolean isCacheValid(DataFieldCacheHolder dataFieldCacheHolder) {
         return dataFieldCacheHolder.getTime() == templateFile.getModificationStamp();
       }
     }).getValue();
 
     final Multimap<String, TemplateDataField> templateDataFields = HashMultimap.create(value);
     Iterator<TemplateDataField> iterator = templateDataFields.values().iterator();
     final PsiElement rootElement = rootTag.getOriginalElement();
 
     while (iterator.hasNext()) {
       TemplateDataField field = iterator.next();
       final PsiElement originalElement = field.getTag().getOriginalElement();
 
       if (!includeRoot && !Util.isChild(originalElement, rootElement)) {
         iterator.remove();
       }
 
     }
     return templateDataFields;
   }
 
   private static Multimap<String, TemplateDataField> findAllDataFieldTags(XmlTag rootTag, boolean includeRoot) {
     Multimap<String, TemplateDataField> references = HashMultimap.create();
     if (rootTag == null) {
       return references;
     }
 
     if (includeRoot
         && rootTag.getAttribute(DATA_FIELD_TAG_ATTRIBUTE) != null || rootTag.getAttribute(ID_ATTRIBUTE) != null) {
       final XmlAttribute attribute;
       if (rootTag.getAttribute(DATA_FIELD_TAG_ATTRIBUTE) != null) {
         attribute = rootTag.getAttribute(DATA_FIELD_TAG_ATTRIBUTE);
       } else {
         attribute = rootTag.getAttribute(ID_ATTRIBUTE);
       }
 
       if (attribute == null) {
         return references;
       }
 
       final String value = attribute.getValue();
       references.put(value, new TemplateDataField(rootTag, value));
     }
     _findDataFieldTags(references, rootTag);
     return references;
   }
 
   private static void _findDataFieldTags(Multimap<String, TemplateDataField> foundTags, XmlTag root) {
     PsiElement n = root;
     do {
       if (!(n instanceof XmlTag)) {
         continue;
       }
       _scanSubTags(foundTags, (XmlTag) n);
     }
     while ((n = n.getNextSibling()) != null);
   }
 
   private static void _scanSubTags(Multimap<String, TemplateDataField> foundTags, XmlTag root) {
     _scanTag(foundTags, root);
     for (XmlTag xmlTag : root.getSubTags()) {
       _scanSubTags(foundTags, xmlTag);
     }
   }
 
   private static void _scanTag(Multimap<String, TemplateDataField> foundTags, XmlTag xmlTag) {
     _scanTag(foundTags, xmlTag, DATA_FIELD_TAG_ATTRIBUTE);
     _scanTag(foundTags, xmlTag, ID_ATTRIBUTE);
   }
 
   private static void _scanTag(Multimap<String, TemplateDataField> foundTags, XmlTag xmlTag, String dataFieldTagAttribute) {
     XmlAttribute xmlAttribute = xmlTag.getAttribute(dataFieldTagAttribute);
     if (xmlAttribute != null) {
       foundTags.put(xmlAttribute.getValue(), new TemplateDataField(xmlTag, xmlAttribute.getValue()));
     }
   }
 
   public static PsiAnnotation findTemplatedAnnotation(PsiElement element) {
     final PsiClass topLevelClass;
 
     if (element.getParent() == null) {
       if (element instanceof PsiClass) {
         topLevelClass = (PsiClass) element;
       }
       else {
         return null;
       }
     }
     else {
       topLevelClass = PsiUtil.getTopLevelClass(element);
     }
 
     if (topLevelClass == null) {
       return null;
     }
 
     final PsiModifierList modifierList = topLevelClass.getModifierList();
 
     if (modifierList == null) {
       return null;
     }
 
     final PsiAnnotation[] annotations = modifierList.getAnnotations();
     for (PsiAnnotation psiAnnotation : annotations) {
       final String qualifiedName = psiAnnotation.getQualifiedName();
       if (qualifiedName == null) {
         continue;
       }
       if (qualifiedName.equals(Types.TEMPLATED)) {
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
 
     final String qualifiedName = annotation.getQualifiedName();
 
     if (qualifiedName == null) return null;
 
     if (!qualifiedName.equals(Types.TEMPLATED)) {
       annotation = findTemplatedAnnotation(annotation);
       if (annotation == null) return null;
     }
 
     final PsiClass templateClass = PsiUtil.getTopLevelClass(annotation);
 
     if (templateClass == null) {
       return null;
     }
 
     final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
 
     final String templateName;
     if (attributes.length == 0) {
       templateName = templateClass.getName() + ".html";
     }
     else {
       if (!(attributes[0].getValue() instanceof PsiLiteralExpression)) {
         return null;
       }
 
       final PsiLiteralExpression literalExpression = (PsiLiteralExpression) attributes[0].getValue();
       if (literalExpression == null) {
         return null;
       }
 
       String text = literalExpression.getText().replace(Util.INTELLIJ_MAGIC_STRING, "");
       templateName = text.substring(1, text.length() - 1);
     }
 
     final PsiFile containingFile = templateClass.getContainingFile().getOriginalFile();
     PsiDirectory containerDir = containingFile.getParent();
 
     if (containerDir == null) {
       return null;
     }
 
     final TemplateExpression reference = TemplateUtil.parseReference(templateName);
 
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
 
	// if we didn't find the file in the current container, 
	// look for it by it's name (ie it might be under resources)
    if (fileByRelativePath == null) {
      PsiFile[] files = FilenameIndex.getFilesByName(project, fileName, projectScope(project));
      if (files.length == 1) {
        fileByRelativePath = files[0].getVirtualFile();
      }
    }

 
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
       Multimap<String, TemplateDataField> allDataFieldTags
           = findAllDataFieldTags(fileByRelativePath, null, project, true);
 
       final Collection<TemplateDataField> dataFieldReference = allDataFieldTags.get(reference.getRootNode());
      // if both data-field and id are the same, dataFieldReference will have
      // two reference to the same element. So, the root tag is valid if we have
      // any values in the iterator
      Iterator<TemplateDataField> dataFieldIterator = dataFieldReference.iterator();
      if (dataFieldIterator.hasNext()) {
        rootTag = dataFieldIterator.next().getTag();
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
 
   public static Collection<String> extractDataFieldList(Collection<AnnotationSearchResult> dataFieldElements) {
     final List<String> elements = new ArrayList<String>();
     for (AnnotationSearchResult element : dataFieldElements) {
       elements.add(Util.getValueStringFromAnnotationWithDefault(element.getAnnotation()).getValue());
     }
     return elements;
   }
 
   public static Map<String, ConsolidateDataFieldElementResult> getConsolidatedDataFields(PsiElement element, Project project) {
     final TemplateMetaData metaData = TemplateUtil.getTemplateMetaData(element);
     if (metaData == null) {
       return Collections.emptyMap();
     }
 
     final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
     if (topLevelClass == null) {
       return Collections.emptyMap();
     }
 
     final String beanClass = topLevelClass.getQualifiedName();
 
     final Map<String, ConsolidateDataFieldElementResult> results = new LinkedHashMap<String, ConsolidateDataFieldElementResult>();
 
     final Collection<AnnotationSearchResult> allInjectionPoints
         = Util.findAllAnnotatedElements(element, Types.DATAFIELD);
 
     for (AnnotationSearchResult r : allInjectionPoints) {
       final String value = Util.getValueStringFromAnnotationWithDefault(r.getAnnotation()).getValue();
       results.put(value, new ConsolidateDataFieldElementResult(value, beanClass, r.getOwningElement(), true));
     }
 
     final Multimap<String, TemplateDataField> allDataFieldTags = TemplateUtil.findAllDataFieldTags(metaData, project, false);
     for (TemplateDataField ref : allDataFieldTags.values()) {
       final XmlAttribute attribute = ref.getTag().getAttribute("data-field");
       if (attribute == null) {
         continue;
       }
 
       final XmlAttributeValue valueElement = attribute.getValueElement();
       if (results.containsKey(ref.getDataFieldName())) {
         results.get(ref.getDataFieldName()).setLinkingElement(valueElement);
         continue;
       }
 
       results.put(ref.getDataFieldName(), new ConsolidateDataFieldElementResult(ref.getDataFieldName(),
           metaData.getTemplateExpression().getFileName(), valueElement, false));
     }
 
     return results;
   }
 
   public static Collection<TemplateMetaData> getTemplateOwners(final PsiFile file) {
     final List<TemplateMetaData> templateOwners = new ArrayList<TemplateMetaData>();
     final PsiClass psiClass = JavaPsiFacade.getInstance(
         file.getProject()).findClass(Types.GWT_COMPOSITE,
         GlobalSearchScope.allScope(file.getProject())
     );
 
     if (psiClass == null) {
       return Collections.emptyList();
     }
 
     for (PsiClass c : ClassInheritorsSearch.search(psiClass, projectScope(file.getProject()), true)) {
       final TemplateMetaData templateMetaData = TemplateUtil.getTemplateMetaData(c);
       if (templateMetaData == null) {
         continue;
       }
       final VirtualFile vTemplateFile = templateMetaData.getTemplateFile();
       if (vTemplateFile == null) {
         continue;
       }
       final String templateFile = vTemplateFile.getCanonicalPath();
       final VirtualFile virtualFile = file.getOriginalFile().getVirtualFile();
       if (virtualFile == null || templateFile == null) {
         continue;
       }
       if (templateFile.equals(virtualFile.getCanonicalPath())) {
         templateOwners.add(templateMetaData);
       }
     }
 
     return templateOwners;
   }
 
   public static PsiFile getFileFromElement(PsiElement element) {
     do {
       if (element instanceof PsiFile) {
         return (PsiFile) element;
       }
     }
     while ((element = element.getParent()) != null);
     return null;
   }
 }
