 package de.plushnikov.intellij.lombok.psi;
 
 import com.intellij.lang.Language;
 import com.intellij.psi.JavaElementVisitor;
 import com.intellij.psi.JavaPsiFacade;
 import com.intellij.psi.Modifier;
 import com.intellij.psi.PsiAnnotation;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiElementFactory;
 import com.intellij.psi.PsiElementVisitor;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiManager;
 import com.intellij.psi.PsiModifier;
 import com.intellij.psi.PsiModifierList;
 import com.intellij.psi.impl.light.LightElement;
 import com.intellij.util.IncorrectOperationException;
 import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.containers.CollectionFactory;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Plushnikov Michail
  */
 public class LombokLightModifierList extends LightElement implements PsiModifierList {
   private static final Set<String> MODIFIERS = new HashSet<String>(Arrays.asList(
       PsiModifier.PUBLIC, PsiModifier.PACKAGE_LOCAL, PsiModifier.PROTECTED, PsiModifier.PRIVATE, PsiModifier.FINAL, PsiModifier.STATIC,
       PsiModifier.ABSTRACT, PsiModifier.SYNCHRONIZED, PsiModifier.TRANSIENT, PsiModifier.VOLATILE, PsiModifier.NATIVE));
 
   private final Set<String> myModifiers;
   private final PsiElement myParentElement;
   private final Map<String, PsiAnnotation> myAnnotations;
 
   public LombokLightModifierList(@NotNull PsiManager manager, @NotNull Language language, @NotNull PsiElement parent) {
     super(manager, language);
     myParentElement = parent;
    myModifiers = CollectionFactory.newTroveSet();
    myAnnotations = CollectionFactory.newTroveMap();
   }
 
   public void addModifier(@Modifier @NotNull @NonNls String modifier) {
     myModifiers.add(modifier);
   }
 
   public String getAllModifierProperties() {
     final StringBuilder builder = StringBuilderSpinAllocator.alloc();
     try {
       for (String modifier : myModifiers) {
         if (!PsiModifier.PACKAGE_LOCAL.equals(modifier)) {
           builder.append(modifier).append(' ');
         }
       }
       return builder.toString();
     } finally {
       StringBuilderSpinAllocator.dispose(builder);
     }
   }
 
   public void clearModifiers() {
     myModifiers.clear();
   }
 
   @Override
   public String getText() {
     return null;
   }
 
   @Override
   public PsiElement copy() {
     return null;
   }
 
   @Override
   public PsiElement getParent() {
     return myParentElement;
   }
 
   @Override
   public PsiFile getContainingFile() {
     return myParentElement.getContainingFile();
   }
 
   public boolean hasModifierProperty(@Modifier @NotNull @NonNls String name) {
     return myModifiers.contains(name);
   }
 
   public boolean hasExplicitModifier(@Modifier @NotNull @NonNls String name) {
     return myModifiers.contains(name);
   }
 
   public void setModifierProperty(@Modifier @NotNull @NonNls String name, boolean value) throws IncorrectOperationException {
     if (value) {
       myModifiers.add(name);
     } else {
       myModifiers.remove(name);
     }
   }
 
   public void checkSetModifierProperty(@Modifier @NotNull @NonNls String name, boolean value) throws IncorrectOperationException {
     throw new IncorrectOperationException();
   }
 
   @NotNull
   public PsiAnnotation[] getApplicableAnnotations() {
     return getAnnotations();
   }
 
   @Override
   @NotNull
   public PsiAnnotation addAnnotation(@NotNull @NonNls String qualifiedName) {
     final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(getProject()).getElementFactory();
     final PsiAnnotation psiAnnotation = elementFactory.createAnnotationFromText('@' + qualifiedName, null);
     myAnnotations.put(qualifiedName, psiAnnotation);
     return psiAnnotation;
   }
 
   @Override
   public PsiAnnotation findAnnotation(@NotNull String qualifiedName) {
     return myAnnotations.get(qualifiedName);
   }
 
   @Override
   @NotNull
   public PsiAnnotation[] getAnnotations() {
     PsiAnnotation[] result = PsiAnnotation.EMPTY_ARRAY;
     if (!myAnnotations.isEmpty()) {
       Collection<PsiAnnotation> annotations = myAnnotations.values();
       result = annotations.toArray(new PsiAnnotation[annotations.size()]);
     }
     return result;
   }
 
   public void accept(@NotNull PsiElementVisitor visitor) {
     if (visitor instanceof JavaElementVisitor) {
       ((JavaElementVisitor) visitor).visitModifierList(this);
     } else {
       visitor.visitElement(this);
     }
   }
 
   public String toString() {
     return "LombokLightModifierList";
   }
 }
