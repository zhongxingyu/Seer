 package haskell.annotator;
 
 import com.intellij.lang.annotation.AnnotationHolder;
 import com.intellij.lang.annotation.ExternalAnnotator;
 import com.intellij.openapi.compiler.CompilerMessageCategory;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.roots.ProjectRootManager;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiFile;
 import haskell.compiler.GHCMessage;
 import haskell.compiler.LaunchGHC;
 import haskell.util.LineColRange;
 
 import java.io.File;
 import java.util.List;
 
 public final class GHCMessageHighlighter implements ExternalAnnotator {
 
     public void annotate(PsiFile psiFile, AnnotationHolder annotationHolder) {
         VirtualFile file = psiFile.getVirtualFile();
         Project project = psiFile.getProject();
         ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
         Module module = rootManager.getFileIndex().getModuleForFile(file);
         List<GHCMessage> ghcMessages = LaunchGHC.getGHCMessages(null, file.getPath(), module, true);
         for (GHCMessage ghcMessage : ghcMessages) {
            if (new File(ghcMessage.getFileName()).equals(new File(psiFile.getVirtualFile().getPath()))) {
                 LineColRange lcRange = ghcMessage.getRange();
                 TextRange range = lcRange.getRange(psiFile);
                 String message = ghcMessage.getErrorMessage();
                 CompilerMessageCategory category = ghcMessage.getCategory();
                 switch (category) {
                 case ERROR:
                     annotationHolder.createErrorAnnotation(range, message);
                     break;
                 case WARNING:
                     annotationHolder.createWarningAnnotation(range, message);
                     break;
                 case INFORMATION:
                     annotationHolder.createInfoAnnotation(range, message);
                     break;
                 case STATISTICS:
                     break;
                 }
             }
         }
     }
 }
