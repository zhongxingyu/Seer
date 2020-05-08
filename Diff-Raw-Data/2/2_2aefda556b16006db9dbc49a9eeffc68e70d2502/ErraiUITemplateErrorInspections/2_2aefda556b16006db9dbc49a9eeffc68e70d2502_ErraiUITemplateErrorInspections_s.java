 package org.jboss.errai.idea.plugin;
 
 import com.intellij.codeHighlighting.HighlightDisplayLevel;
 import com.intellij.codeInsight.daemon.GroupNames;
 import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
 import com.intellij.codeInspection.LocalQuickFix;
 import com.intellij.codeInspection.ProblemDescriptor;
 import com.intellij.codeInspection.ProblemsHolder;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.JavaElementVisitor;
 import com.intellij.psi.JavaPsiFacade;
 import com.intellij.psi.PsiAnnotation;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiElementFactory;
 import com.intellij.psi.PsiElementVisitor;
 import com.intellij.psi.PsiImportStatement;
 import com.intellij.psi.PsiJavaCodeReferenceElement;
 import com.intellij.psi.PsiJavaFile;
 import com.intellij.psi.PsiMethod;
 import com.intellij.psi.PsiNameValuePair;
 import com.intellij.psi.PsiParameter;
 import com.intellij.psi.PsiReferenceList;
 import com.intellij.psi.search.ProjectScope;
 import com.intellij.psi.util.PsiUtil;
 import org.jetbrains.annotations.Nls;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Map;
 
 /**
  * @author Mike Brock
  */
 @SuppressWarnings("ALL")
 public class ErraiUITemplateErrorInspections extends BaseJavaLocalInspectionTool {
   @Nls
   @NotNull
   @Override
   public String getDisplayName() {
     return "Perform validity checks on Errai UI @Templated classes";
   }
 
   @Nls
   @NotNull
   @Override
   public String getGroupDisplayName() {
     return GroupNames.BUGS_GROUP_NAME;
   }
 
   @NotNull
   @Override
   public String getShortName() {
     return "ErraiUITemplateChecks";
   }
 
   @Override
   public boolean isEnabledByDefault() {
     return true;
   }
 
   @NotNull
   @Override
   public HighlightDisplayLevel getDefaultLevel() {
     return HighlightDisplayLevel.ERROR;
   }
 
   @NotNull
   @Override
   public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
     return new MyJavaElementVisitor(holder);
   }
 
   private static class MyJavaElementVisitor extends JavaElementVisitor {
     private final ProblemsHolder holder;
 
     public MyJavaElementVisitor(ProblemsHolder holder) {
       this.holder = holder;
     }
 
     @Override
     public void visitAnnotation(PsiAnnotation annotation) {
       final String qualifiedName = annotation.getQualifiedName();
       if (qualifiedName != null) {
         if (qualifiedName.equals(ErraiFrameworkSupport.TEMPLATED_ANNOTATION_NAME)) {
           final PsiClass templateClass = ((PsiClass) annotation.getParent().getParent());
           ensureTemplateExists(holder, annotation);
           ensureTemplateClassIsComposite(holder, templateClass);
         }
         else if (qualifiedName.equals(ErraiFrameworkSupport.DATAFIELD_ANNOTATION_NAME)) {
           ensureDataFieldIsValid(holder, annotation);
         }
         else if (qualifiedName.equals(ErraiFrameworkSupport.EVENTHANDLER_ANNOTATION_NAME)) {
           ensureEventHandlerIsValid(holder, annotation);
         }
       }
     }
   }
 
 
   private static void ensureTemplateExists(ProblemsHolder holder,
                                            PsiAnnotation annotation) {
 
     final Util.TemplateMetaData metaData = Util.getTemplateMetaData(annotation, holder.getProject());
 
     final VirtualFile templateVF = metaData.getTemplateFile();
     final PsiNameValuePair attribute = metaData.getAttribute();
 
     if (templateVF == null) {
       if (annotation != null && metaData.isDefaultReference()) {
         holder.registerProblem(annotation, "Could not find companion Errai UI template: " + metaData.getTemplateReference().getFileName());
       }
       else if (attribute != null) {
         holder.registerProblem(attribute, "Errai UI template file cannot be resolved.");
       }
     }
     else if (attribute != null && !metaData.getTemplateReference().getRootNode().equals("")) {
       final Map<String, Util.DataFieldReference> allDataFieldTags
           = Util.findAllDataFieldTags(metaData, holder.getProject(), true);
 
       if (!allDataFieldTags.containsKey(metaData.getTemplateReference().getRootNode())) {
         holder.registerProblem(attribute, "The data-field element specified for the root " +
             "note does not exist: " + metaData.getTemplateReference().getRootNode());
       }
     }
   }
 
   public static void ensureTemplateClassIsComposite(final ProblemsHolder holder, final PsiClass templateClass) {
     boolean isComposite = false;
     PsiClass cls = templateClass;
     while ((cls = cls.getSuperClass()) != null) {
       if (cls.getQualifiedName().equals(ErraiFrameworkSupport.GWT_COMPOSITE_REF)) {
         isComposite = true;
         break;
       }
     }
 
     if (!isComposite) {
       if (templateClass == null) {
         return;
       }
 
      holder.registerProblem(templateClass, "Errai UI @Templated bean must extend " + ErraiFrameworkSupport.GWT_COMPOSITE_REF,
           new LocalQuickFix() {
             @NotNull
             @Override
             public String getName() {
               return "Make bean extend " + ErraiFrameworkSupport.GWT_COMPOSITE_REF;
             }
 
             @NotNull
             @Override
             public String getFamilyName() {
               return GroupNames.BUGS_GROUP_NAME;
             }
 
             @Override
             public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
               final PsiReferenceList extendsList = templateClass.getExtendsList();
               final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
               final PsiElementFactory elementFactory = instance.getElementFactory();
               final PsiJavaCodeReferenceElement classRef
                   = elementFactory.createReferenceElementByFQClassName(ErraiFrameworkSupport.GWT_COMPOSITE_REF, ProjectScope.getAllScope(project));
 
               if (extendsList != null) {
                 if (extendsList.getReferenceElements().length > 0) {
                   for (PsiJavaCodeReferenceElement psiJavaCodeReferenceElement : extendsList.getReferenceElements()) {
                     psiJavaCodeReferenceElement.delete();
                   }
                 }
                 extendsList.add(classRef);
               }
             }
           });
     }
   }
 
   public static void ensureDataFieldIsValid(ProblemsHolder holder,
                                             PsiAnnotation annotation) {
 
     final Util.TemplateMetaData templateMetaData = Util.getTemplateMetaData(annotation, holder.getProject());
     final Project project = holder.getProject();
     final Map<String, Util.DataFieldReference> allDataFieldTags
         = Util.findAllDataFieldTags(templateMetaData, project, false);
 
     final PsiElement ownerElement = Util.getImmediateOwnerElement(annotation);
 
     final Util.AnnotationValueElement annotationValue
         = Util.getValueStringFromAnnotationWithDefault(annotation);
 
     if (annotationValue == null) {
       return;
     }
 
     if (!allDataFieldTags.containsKey(annotationValue.getValue())) {
       holder.registerProblem(annotationValue.getLogicalElement(),
           "No corresponding data-field element in template: " + annotationValue.getValue());
     }
 
     final PsiClass typeOfElement = Util.getTypeOfElement(ownerElement, project);
     if (!Util.typeIsAssignableFrom(typeOfElement, ErraiFrameworkSupport.GWT_ELEMENT_TYPE, ErraiFrameworkSupport.GWT_WIDGET_TYPE)) {
       holder.registerProblem(ownerElement, "Type is not a valid template part (must be Element or Widget)");
     }
   }
 
   public static void ensureEventHandlerIsValid(ProblemsHolder holder,
                                                PsiAnnotation annotation) {
     final Util.TemplateMetaData templateMetaData = Util.getTemplateMetaData(annotation, holder.getProject());
     final Project project = holder.getProject();
 
     final PsiClass bean = PsiUtil.getTopLevelClass(annotation);
     final PsiElement owner = Util.getImmediateOwnerElement(annotation);
     final boolean hasSinkEvent = Util.fieldOrMethodIsAnnotated(owner, ErraiFrameworkSupport.SINKNATIVE_ANNOTATION_NAME);
 
     if (!(owner instanceof PsiMethod)) return;
 
     final PsiMethod psiMethod = (PsiMethod) owner;
     final PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
     if (psiParameters.length == 0) {
       holder.registerProblem(psiMethod.getParameterList(), "Event handler method must accept one parameter");
       return;
     }
     else if (psiParameters.length > 1) {
       holder.registerProblem(psiMethod.getParameterList(), "Event handler method must only accept one parameter");
       return;
     }
 
     final PsiParameter psiParameter = psiParameters[0];
     final String parameterTypeFQN = psiParameter.getType().getCanonicalText();
     final PsiClass psiClassParameterType = JavaPsiFacade.getInstance(project)
         .findClass(parameterTypeFQN, ProjectScope.getAllScope(project));
 
     final Map<String, Util.DataFieldReference> inScopeDataFields = Util.findAllDataFieldTags(templateMetaData, project, false);
     final Map<String, ConsolidateDataFieldElementResult> dataFields = Util.getConsolidatedDataFields(owner, project);
     final Util.AnnotationValueElement annoValueEl = Util.getValueStringFromAnnotationWithDefault(annotation);
     final String annoValue = annoValueEl.getValue();
 
     final Util.DataFieldReference result = inScopeDataFields.get(annoValue);
     if (result == null) {
       if (dataFields.containsKey(annoValue)) {
         holder.registerProblem(annoValueEl.getLogicalElement(), "Data-field is out of scope (it is not an descendant of the template root node)");
       }
       else {
         holder.registerProblem(annoValueEl.getLogicalElement(), "Cannot resolve data-field: " + annoValueEl.getValue());
       }
     }
     else if (!hasSinkEvent && !dataFields.get(annoValue).isDataFieldInClass()) {
       holder.registerProblem(annotation, "Non-injected data-field element is missing @SinkNative", new LocalQuickFix() {
         @NotNull
         @Override
         public String getName() {
           return "Add @SinkNative";
         }
 
         @NotNull
         @Override
         public String getFamilyName() {
           return GroupNames.BUGS_GROUP_NAME;
         }
 
         @Override
         public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
 
           final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
           final PsiImportStatement importSinkNative = instance.getElementFactory()
               .createImportStatement(
                   instance.findClass(ErraiFrameworkSupport.SINKNATIVE_ANNOTATION_NAME,
                       ProjectScope.getAllScope(project))
               );
 
           final PsiImportStatement importDomEvent = instance.getElementFactory()
               .createImportStatement(
                   instance.findClass(ErraiFrameworkSupport.GWT_DOM_EVENT_TYPE,
                       ProjectScope.getAllScope(project))
               );
 
 
           ((PsiJavaFile) bean.getParent()).getImportList().add(importSinkNative);
           ((PsiJavaFile) bean.getParent()).getImportList().add(importDomEvent);
 
           psiMethod.getModifierList().addAnnotation("SinkNative(Event.ONCLICK)");
         }
       });
     }
 
     final boolean isGWTeventType = Util.typeIsAssignableFrom(psiClassParameterType, ErraiFrameworkSupport.GWT_EVENT_TYPE);
 
     // if (!Util.typeIsAssignableFrom(psiClassParameterType, ErraiFrameworkSupport.GWT_DOM_EVENT_TYPE)) {
     if (isGWTeventType && hasSinkEvent && dataFields.containsKey(annoValue) && dataFields.get(annoValue).isDataFieldInClass()) {
       final PsiAnnotation sinkNativeAnnotation = Util.getAnnotationFromElement(psiMethod, ErraiFrameworkSupport.SINKNATIVE_ANNOTATION_NAME);
 
       holder.registerProblem(sinkNativeAnnotation, "Handler that extends GwtEvent is incompatible with @SinkNative",
           new LocalQuickFix() {
             @NotNull
             @Override
             public String getName() {
               return "Remove @SinkNative";
             }
 
             @NotNull
             @Override
             public String getFamilyName() {
               return GroupNames.BUGS_GROUP_NAME;
             }
 
             @Override
             public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
               final PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();
               for (PsiAnnotation a : annotations) {
                 if (a.getQualifiedName().equals(ErraiFrameworkSupport.SINKNATIVE_ANNOTATION_NAME)) {
                   a.delete();
                   return;
                 }
               }
             }
           });
     }
 
     if (isGWTeventType && !Util.typeIsAssignableFrom(psiClassParameterType, ErraiFrameworkSupport.GWT_EVENT_TYPE)) {
       holder.registerProblem(psiParameter, "The specified event type is not a valid event handler type");
     }
 
     if (isGWTeventType && dataFields.containsKey(annoValue) && !dataFields.get(annoValue).isDataFieldInClass()) {
       holder.registerProblem(psiParameter, "DOM based event binding cannot use a GwtEvent",
           new LocalQuickFix() {
             @NotNull
             @Override
             public String getName() {
               return "Change handled event type to: " + ErraiFrameworkSupport.GWT_DOM_EVENT_TYPE;
             }
 
             @NotNull
             @Override
             public String getFamilyName() {
               return GroupNames.BUGS_GROUP_NAME;
             }
 
             @Override
             public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
               final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
               final PsiClass psiClass = instance.findClass(ErraiFrameworkSupport.GWT_DOM_EVENT_TYPE,
                   ProjectScope.getAllScope(project));
 
               final PsiParameter[] parameters = psiParameters;
               final PsiElementFactory elementFactory = instance.getElementFactory();
               final PsiParameter parameter = parameters[0];
               parameter.replace(elementFactory.createParameter(parameter.getName(), elementFactory.createType(psiClass)));
             }
           });
     }
   }
 }
