 package ideah.findUsages;
 
 import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
 import com.intellij.lang.cacheBuilder.WordsScanner;
 import com.intellij.lang.findUsages.FindUsagesProvider;
 import com.intellij.psi.PsiElement;
 import ideah.lexer.HaskellLexer;
 import ideah.parser.HaskellParserDefinition;
 import ideah.psi.impl.HPIdentImpl;
 import org.jetbrains.annotations.NotNull;
 
 public final class HaskellFindUsagesProvider implements FindUsagesProvider {
 
     public WordsScanner getWordsScanner() {
         return new DefaultWordsScanner(new HaskellLexer(),
             HaskellParserDefinition.IDS, HaskellParserDefinition.COMMENTS, HaskellParserDefinition.STRINGS);
     }
 
     public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
         return psiElement instanceof HPIdentImpl;
     }
 
     public String getHelpId(@NotNull PsiElement psiElement) {
         return null;
     }
 
     @NotNull
     public String getType(@NotNull PsiElement psiElement) {
         return "symbol";
     }
 
     @NotNull
     public String getDescriptiveName(@NotNull PsiElement psiElement) {
        return "name"; // todo!!!
     }
 
     @NotNull
     public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
         if (element instanceof HPIdentImpl) {
             HPIdentImpl ident = (HPIdentImpl) element;
             String name = ident.getName();
             return name == null ? ident.getText() : name;
         }
         return element.getText();
     }
 }
