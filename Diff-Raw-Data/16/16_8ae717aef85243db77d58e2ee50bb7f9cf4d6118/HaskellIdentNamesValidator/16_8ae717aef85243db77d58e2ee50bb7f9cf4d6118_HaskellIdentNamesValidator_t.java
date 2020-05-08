 package ideah.psi.api.util;
 
 import com.intellij.lang.refactoring.NamesValidator;
 import com.intellij.openapi.project.Project;
 import ideah.lexer.HaskellLexer;
 
 import java.lang.String;
 
 public class HaskellIdentNamesValidator implements NamesValidator {
 
     public boolean isKeyword(final String name, final Project project) {
         for (String s : HaskellLexer.getKeywords()) {
             if (name.equals(s))
                 return true;
         }
         return false;
     }
 
     public boolean isIdentifier(final String name, final Project project) { // todo: use lexer
         if (!Character.isJavaIdentifierStart(name.charAt(0)))
             return false;
         if (name.length() > 1) {
             for (char c : name.substring(1).toCharArray()) {
                if (!(Character.isLetterOrDigit(c) || c == '_' || c == '\''))
                     return false;
             }
         }
         return !isKeyword(name, project); 
     }
 }
