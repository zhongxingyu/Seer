 package essua.idea.m2estorm.dic;
 
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.PsiElement;
 import com.jetbrains.php.PhpIndex;
 import com.jetbrains.php.lang.psi.elements.MethodReference;
 import com.jetbrains.php.lang.psi.elements.Method;
 import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
 import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
 import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
 import essua.idea.m2estorm.M2EProjectComponent;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.*;
 import java.util.regex.Pattern;
 
 public class FactoryTypeProvider implements PhpTypeProvider2 {
 
     private Map<String, Integer> factoryMap = null;
     final static char TRIM_KEY = '\u0180';
     final static String PARAM_SEPARATOR = "---";
     final static String FAKE_PARAMETER = "___";
     private static Pattern helperRegExpPattern = Pattern.compile("Mage.helper.M2ePro/Component");
 
     @Override
     public char getKey() {
         return 'Ъ';
     }
 
     @Nullable
     @Override
     public String getType(PsiElement e) {
         if (!(e instanceof MethodReference)) {
             return null;
         }
 
         String refSignature = ((MethodReference)e).getSignature();
 
         if (!helperRegExpPattern.matcher(refSignature).find()) {
             return null;
         }
 
         PsiElement[] parameters = ((MethodReference)e).getParameters();
 
         if (parameters.length == 0) {
             return null;
         }
 
         String stringParameters = getStringParameters(parameters);
 
         if (stringParameters.length() == 0) {
             return null;
         }
 
         return refSignature + TRIM_KEY + stringParameters;
     }
 
     private String getStringParameters(PsiElement[] parameters) {
         String result = "";
 
         for (int i = 0; i < parameters.length; i++) {
             PsiElement parameter = parameters[i];
 
             if (result.length() != 0) {
                 result += PARAM_SEPARATOR;
             }
 
             if (!(parameter instanceof StringLiteralExpression)) {
                 result += FAKE_PARAMETER;
                 continue;
             }
 
             String stringParameter = ((StringLiteralExpression)parameter).getContents();
 
             result += stringParameter;
         }
 
         return result;
     }
 
     @Override
     public Collection<? extends PhpNamedElement> getBySignature(String expression, Project project) {
 
         // get back our original call
         String originalSignature = expression.substring(0, expression.lastIndexOf(TRIM_KEY));
         String stringParameters = expression.substring(expression.lastIndexOf(TRIM_KEY) + 1);
 
         // search for called method
         PhpIndex phpIndex = PhpIndex.getInstance(project);
         Collection<? extends PhpNamedElement> phpNamedElementCollections = phpIndex.getBySignature(originalSignature, null, 0);
         if (phpNamedElementCollections.size() == 0) {
             return Collections.emptySet();
         }
 
         // get first matched item
         PhpNamedElement phpNamedElement = phpNamedElementCollections.iterator().next();
         if (!(phpNamedElement instanceof Method)) {
             return Collections.emptySet();
         }
 
         int backslash = originalSignature.lastIndexOf('\\');
 
         if (backslash == -1) {
             return Collections.emptySet();
         }
 
         String signature = originalSignature.substring(backslash + 1);
         Map<String, Integer> factoryMap = getFactoryMap(project);
 
         if (!factoryMap.containsKey(signature)) {
             return Collections.emptySet();
         }
 
         Integer entityArgumentPosition = factoryMap.get(signature);
         String[] stringParametersArray = stringParameters.split(PARAM_SEPARATOR);
 
        if (stringParametersArray.length < entityArgumentPosition) {
             return Collections.emptySet();
         }
 
         String stringParameter = stringParametersArray[entityArgumentPosition];
         String fqn;
 
         if (signature.endsWith("getCollection")) {
             fqn = "\\Ess_M2ePro_Model_Mysql4_" + stringParameter + "_Collection";
         } else {
             fqn = "\\Ess_M2ePro_Model_" + stringParameter;
         }
 
         return PhpIndex.getInstance(project).getAnyByFQN(fqn);
     }
 
     private Map<String, Integer> getFactoryMap(Project p) {
         if (null == factoryMap) {
             factoryMap = p.getComponent(M2EProjectComponent.class).getFactoryMap();
         }
 
         return factoryMap;
     }
 }
