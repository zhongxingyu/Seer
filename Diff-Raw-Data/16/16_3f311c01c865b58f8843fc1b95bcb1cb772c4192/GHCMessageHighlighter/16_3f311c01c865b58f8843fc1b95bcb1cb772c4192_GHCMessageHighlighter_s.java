 package ideah.annotator;
 
 import com.intellij.lang.annotation.AnnotationHolder;
 import com.intellij.lang.annotation.ExternalAnnotator;
 import com.intellij.openapi.compiler.CompilerMessageCategory;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiFile;
 import ideah.compiler.GHCMessage;
 import ideah.compiler.LaunchGHC;
 import ideah.util.DeclarationPosition;
 import ideah.util.LineColRange;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
 
 public final class GHCMessageHighlighter extends ExternalAnnotator<PsiFile, AnnotationResult> {

    @Override
    public PsiFile collectionInformation(@NotNull PsiFile file) {
        return file;
    }

     @Override
     public AnnotationResult doAnnotate(PsiFile psiFile) {
         VirtualFile file = psiFile.getVirtualFile();
         if (file == null)
             return null;
         Module module = DeclarationPosition.getDeclModule(psiFile);
         if (module == null)
             return null;
         List<GHCMessage> ghcMessages = LaunchGHC.compile(null, file.getPath(), module, true);
         return new AnnotationResult(file, ghcMessages);
     }
 
     @Override
     public void apply(@NotNull PsiFile file, AnnotationResult result, @NotNull AnnotationHolder holder) {
         if (result == null)
             return;
         showMessages(file, holder, result.file, result.ghcMessages);
     }
 
     private static void showMessages(PsiFile psiFile, AnnotationHolder annotationHolder, VirtualFile file, List<GHCMessage> ghcMessages) {
         File mainFile = new File(file.getPath());
         for (GHCMessage ghcMessage : ghcMessages) {
             if (new File(ghcMessage.getFileName()).equals(mainFile)) {
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
