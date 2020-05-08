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
 
 package org.jboss.errai.idea.plugin.util;
 
 import com.intellij.ide.IdeView;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.DataKeys;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.roots.ProjectFileIndex;
 import com.intellij.openapi.roots.ProjectRootManager;
 import com.intellij.openapi.util.Key;
 import com.intellij.psi.JavaDirectoryService;
 import com.intellij.psi.JavaPsiFacade;
 import com.intellij.psi.PsiAnnotation;
 import com.intellij.psi.PsiAnnotationMemberValue;
 import com.intellij.psi.PsiAnnotationParameterList;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiClassObjectAccessExpression;
 import com.intellij.psi.PsiClassType;
 import com.intellij.psi.PsiDirectory;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiExpression;
 import com.intellij.psi.PsiField;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiInvalidElementAccessException;
 import com.intellij.psi.PsiMethod;
 import com.intellij.psi.PsiModifierList;
 import com.intellij.psi.PsiModifierListOwner;
 import com.intellij.psi.PsiNameValuePair;
 import com.intellij.psi.PsiParameter;
 import com.intellij.psi.PsiParameterList;
 import com.intellij.psi.PsiType;
 import com.intellij.psi.PsiVariable;
 import com.intellij.psi.search.FilenameIndex;
 import com.intellij.psi.search.GlobalSearchScope;
 import com.intellij.psi.util.PsiUtil;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 
 /**
  * @author Mike Brock
  */
 public class Util {
   public static final String INTELLIJ_MAGIC_STRING = "IntellijIdeaRulezzz";
 
   public static PsiClass getErasedTypeParam(Project project, String signature) {
     final String typeParam;
     int typeParamBegin = signature.indexOf('<');
     if (typeParamBegin == -1) {
       typeParam = null;
     }
     else {
       typeParam = getErasedCanonicalText(signature.substring(typeParamBegin + 1, signature.indexOf('>')));
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
 
   public static boolean isChild(PsiElement child, PsiElement parent) {
     PsiElement el = child;
     while ((el = el.getParent()) != null) {
       if (el.equals(parent)) return true;
     }
     return false;
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
 
   public static PsiElement getMethodOrField(PsiElement element) {
     PsiElement el = element;
     do {
       if (el instanceof PsiField) {
         return el;
       }
       else if (el instanceof PsiMethod) {
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
 
   public static boolean typeIsAnnotated(PsiClass psiClass, String annotationType) {
     final PsiModifierList modifierList = psiClass.getModifierList();
 
     if (modifierList != null) {
       for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
         final String qualifiedName = psiAnnotation.getQualifiedName();
         if (qualifiedName != null && qualifiedName.equals(annotationType)) {
           return true;
         }
       }
     }
     return false;
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
 
   public static AnnotationValueElement getValueStringFromAnnotationWithDefault(PsiAnnotation annotation) {
     final PsiAnnotationParameterList parameterList = annotation.getParameterList();
     final PsiNameValuePair[] attributes = parameterList.getAttributes();
     final PsiElement logicalElement = getImmediateOwnerElement(annotation);
 
     if (logicalElement == null) {
       return null;
     }
 
     final String value;
     final PsiElement errorElement;
 
     if (attributes.length == 0) {
       value = getNameOfElement(logicalElement);
       errorElement = annotation;
     }
     else {
       final String text = attributes[0].getText();
       value = text.substring(1, text.length() - 1);
       errorElement = attributes[0];
     }
 
     return new AnnotationValueElement(value, errorElement);
   }
 
   public static Collection<AnnotationSearchResult> findAllAnnotatedElements(PsiElement element, String annotation) {
     final PsiClass bean = PsiUtil.getTopLevelClass(element);
     if (bean == null) {
       return Collections.emptyList();
     }
 
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
 
     return elementList;
   }
 
   public static boolean fieldElementIsInitialized(PsiElement element) {
     if (element instanceof PsiField) {
       final PsiField psiField = (PsiField) element;
       final PsiExpression initializer = psiField.getInitializer();
       if (initializer != null) {
         return !"null".equals(initializer.getText());
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
 
   public static PsiClass getTypeOfElement(PsiElement element) {
     final String name;
     if (element instanceof PsiVariable) {
       name = Util.getErasedCanonicalText(((PsiVariable) element).getType().getCanonicalText());
     }
     else {
       return null;
     }
     return JavaPsiFacade.getInstance(element.getProject()).findClass(name, GlobalSearchScope.allScope(element.getProject()));
   }
 
   public static PsiVariable getEnclosingVariable(PsiElement element) {
     PsiElement el = element;
     while ((el = el.getParent()) != null) {
       if (el instanceof PsiVariable) {
         return (PsiVariable) el;
       }
     }
 
     return null;
   }
 
   public static SuperTypeInfo getTypeInformation(PsiClass from, String toFQN) {
     Stack<PsiClassType> toSearch = new Stack<PsiClassType>();
     for (PsiClassType type : from.getSuperTypes()) {
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
         final List<String> parms = getErasedTypeParamsCanonicalText(type.getCanonicalText());
         return new SuperTypeInfo(parms);
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
 
   public static <T> void invalidateCache(Key<T> cacheKey, PsiElement element) {
     element.getOriginalElement().putCopyableUserData(cacheKey, null);
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
       }
     }
 
     if (copyableUserData == null) {
       originalFile.putCopyableUserData(cacheKey, copyableUserData = provider.provide());
     }
 
     return copyableUserData;
   }
 
   public static PsiFile[] getAllErraiAppProperties(Project project) {
     return FilenameIndex.getFilesByName(project, "ErraiApp.properties", GlobalSearchScope.allScope(project));
   }
 
   public static long getLastUpdate(PsiElement element) {
     try {
       final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
       if (topLevelClass == null) {
         return -1;
       }
       final PsiFile containingFile = topLevelClass.getContainingFile();
       return containingFile.getOriginalFile().getModificationStamp();
     }
     catch (PsiInvalidElementAccessException e) {
       return -1;
     }
   }
 
   public static boolean isDefaultInstantiable(PsiClass psiClass) {
     final PsiMethod[] constructors = psiClass.getConstructors();
 
     if (constructors.length == 0) {
       return true;
     }
 
     for (PsiMethod psiMethod : constructors) {
       final PsiParameterList parameterList = psiMethod.getParameterList();
       if (psiMethod.getModifierList().hasModifierProperty("public") && parameterList.getParameters().length == 0) {
         return true;
       }
     }
 
     return false;
   }
 
   public static Collection<PsiElement> findChildrenElements(PsiElement element, ElementFilter filter) {
     final List<PsiElement> found = new ArrayList<PsiElement>();
     _findChildrenElements(found, element, filter);
     return found;
   }
 
   private static void _findChildrenElements(Collection<PsiElement> found, PsiElement element, ElementFilter filter) {
     for (PsiElement e : element.getChildren()) {
       if (filter.filter(e)) {
         found.add(e);
       }
 
       _findChildrenElements(found, e, filter);
     }
   }
 
   public static boolean isInsideProjectSources(AnActionEvent event) {
     ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(event.getProject()).getFileIndex();
      final IdeView view = event.getData(DataKeys.IDE_VIEW);
 
      for (PsiDirectory dir : view.getDirectories()) {
        if (projectFileIndex.isInSourceContent(dir.getVirtualFile()) && JavaDirectoryService.getInstance().getPackage(dir) != null) {
          return true;
        }
      }
     return false;
   }
 
   public static String boxedType(String type) {
     if ("int".equals(type)) {
       return Integer.class.getName();
     }
     else if ("double".equals(type)) {
       return Double.class.getName();
     }
     else if ("boolean".equals(type)) {
      return Double.class.getName();
     }
     else if ("float".equals(type)) {
      return Double.class.getName();
     }
     else if ("short".equals(type)) {
      return Double.class.getName();
     }
     else if ("byte".equals(type)) {
      return Double.class.getName();
     }
     else if ("long".equals(type)) {
      return Double.class.getName();
     }
 
     return type;
   }
 
 }
