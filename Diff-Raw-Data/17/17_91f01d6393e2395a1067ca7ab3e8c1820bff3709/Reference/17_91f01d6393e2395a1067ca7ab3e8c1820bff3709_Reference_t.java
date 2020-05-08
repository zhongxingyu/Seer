 package pal.plugins.intellij.jumptousage.wrappers;
 
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.fileEditor.FileEditor;
 import com.intellij.openapi.fileEditor.FileEditorManager;
 import com.intellij.openapi.fileEditor.OpenFileDescriptor;
 import com.intellij.openapi.fileEditor.TextEditor;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.pom.Navigatable;
 import com.intellij.psi.*;
 
 public class Reference {
     private PsiElement psiElement;
 
     public Reference(PsiReference reference) {
         this.psiElement = reference.getElement();
     }
 
     public Navigatable location() {
         return new OpenFileDescriptor(DataHolder.getInstance().PROJECT, containingVirtualFile(), psiElement.getTextOffset());
     }
 
     public VirtualFile containingVirtualFile() {
         return containingFile().getVirtualFile();
     }
 
     public PsiMethod containingMethod() {
         PsiElement parent;
         PsiElement current = psiElement;
         while (true) {
             parent = current.getParent();
             if (parent instanceof PsiFile) return null;
             if (parent instanceof PsiMethod) return (PsiMethod) parent;
             current = parent;
         }
     }
 
     public PsiClass containingClass() {
         PsiElement parent;
         PsiElement current = psiElement;
         while (true) {
             parent = current.getParent();
             if (parent instanceof PsiFile) return null;
             if (parent instanceof PsiClass) return (PsiClass) parent;
             current = parent;
         }
     }
 
     public PsiFile containingFile() {
         return psiElement.getContainingFile();
     }
 
     public int line() {
         Editor editor = DataHolder.getInstance().EDITOR;
         FileEditor[] fileEditors = FileEditorManager.getInstance(DataHolder.getInstance().PROJECT).getEditors(containingVirtualFile());
         for (FileEditor fileEditor : fileEditors) {
             if (fileEditor instanceof TextEditor)
                 editor = ((TextEditor) fileEditor).getEditor();
         }
 
         return editor.offsetToVisualPosition(psiElement.getTextOffset()).line + 1;
     }
 
     public int column() {
         Editor editor = DataHolder.getInstance().EDITOR;
         FileEditor[] fileEditors = FileEditorManager.getInstance(DataHolder.getInstance().PROJECT).getEditors(containingVirtualFile());
         for (FileEditor fileEditor : fileEditors) {
             editor = ((TextEditor) fileEditor).getEditor();
         }
 
         return editor.offsetToVisualPosition(psiElement.getTextOffset()).column + 1;
     }
 
     public String containingPackage() {
         String fullPackageName = "default";
         try {
             PsiPackage psiPackage = psiElement.getContainingFile().getContainingDirectory().getPackage();
             if (!psiPackage.getQualifiedName().trim().equals(""))
                 fullPackageName = psiPackage.getQualifiedName();
         } catch (NullPointerException e) {
             fullPackageName = "default";
         }
         return fullPackageName;
     }
 
     //MessageBuilder?!
     public String description() {
         StringBuffer description = new StringBuffer();
         PsiClass containingClass = containingClass();
         PsiMethod containingMethod = containingMethod();
         PsiFile containingFile = containingFile();
 
 
         if (containingClass != null && containingClass.getName() != "")
            description.append(getContainingClassName(containingClass));
         else
             description.append(containingFile.getName());
 
         if (containingMethod != null)
             description.append(".").append(containingMethod.getName());
 
         if ((containingClass != null && containingClass.getName() != "") || containingFile.getName() != "" || (containingMethod != null && containingMethod.getName() != ""))
             description.append(": ");
 
         try {
             int lineNumber = line();
             int columnNumber = column();
             description.append("Line ").append(lineNumber).append(", Column ").append(columnNumber);
         } catch (Exception e) {
         }
 
         String inPackage = containingPackage();
         if (inPackage != null && inPackage != "") {
             description.append(" (in ").append(inPackage).append(")");
         }
 
         return description.toString();
     }

    private String getContainingClassName(PsiClass containingClass) {
        String className = containingClass.getName();
        if (className == null)
            return "Anonymous class in " + getContainingClassOrFile(containingClass);
        return className;
    }

    private String getContainingClassOrFile(PsiClass theClass) {
        if (theClass.getContainingClass() != null)
            return theClass.getContainingClass().getName();
        else
            return theClass.getContainingFile().getName();
    }
 }
