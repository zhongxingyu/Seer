 package edu.kit.checkstyle.checks;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.puppycrawl.tools.checkstyle.api.DetailAST;
 import com.puppycrawl.tools.checkstyle.api.TokenTypes;
 
 import edu.kit.checkstyle.TokenSearcherCheck;
 import static edu.kit.checkstyle.CollectionUtils.*;
 
 
 /**
  * Checks for the existence of method calls and in which method they are called.
  * <p>
  * Some method calls are discouraged thus this rule checks if these methods are
  * called and reports an error if this is the case.
  * <p>
  * It is possible to specify "free" methods. This means that all occurrences of
  * given method calls will not lead to an error if they occur in such "free"
  * methods. If no "free" methods are specified every found and specified method
  * call leads to an error.
  *
  * @since JDK1.7, 07.06.2013
  */
 public class DiscouragedMethodCallCheck extends TokenSearcherCheck {
 
   static class Prop {
 
     final String allowedMethod;
     final String className;
     final String methodName;
 
     public Prop(final String allowedMethod, final String className, final String methodName) {
       this.allowedMethod = allowedMethod;
       this.className = className;
       this.methodName = methodName;
     }
   }
 
   private List<Prop> props = mkList();
 
   /**
    * Sets all the methods this check searches for.
    *
    * The expected format is a comma separated list where each part is of format
    * {@code <allowedMethod>:<class>.<method>} or {@code <class>.<method>}
    *
    * Example input string:
    *
    * "main:System.exit,System.out,System.in"
    *
    * @param value the input string
    */
   public void setCheckedMethods(final String value) {
     final List<Prop> props = mkList();
     for (final String p : StringUtils.split(value, ",")) {
       final String[] parts = p.split("[:.]");
       if (parts.length != 2 && parts.length != 3) {
         throw new IllegalArgumentException("format of value '" + value + "' is not supported");
       }
 
       final boolean hasMethodScope = parts.length == 3;
       props.add(hasMethodScope ?
           new Prop(parts[0], parts[1], parts[2]) :
           new Prop("", parts[0], parts[1]));
     }
     this.props = props;
   }
 
   @Override
   public int[] getDefaultTokens() {
     return new int[] {
         TokenTypes.IDENT
     };
   }
 
   @Override
   public void visitToken(final DetailAST ast) {
     final int line = ast.getLineNo();
     final int column = ast.getColumnNo();
 
     for (final Prop prop : props) {
       if (propFound(ast, prop) && !isInAllowedMethod(ast, prop)) {
         final String context = prop.allowedMethod.isEmpty() ? "" :
             String.format(" outside of the %s method", prop.allowedMethod);
         final String msg = String.format(
             "The usage of ''%s.%s''%s is discouraged",
             prop.className, prop.methodName, context);
         log(line, column, msg);
       }
     }
   }
 
   private boolean propFound(final DetailAST ast, final Prop prop) {
    return eqName(ast, prop.methodName) && eqName(ast.getPreviousSibling(), prop.className);
   }
 
   private boolean isInAllowedMethod(final DetailAST ast, final Prop prop) {
     return !prop.allowedMethod.isEmpty() && eqName(getContainingMethodName(ast), prop.allowedMethod);
   }
 
   private DetailAST getContainingMethodName(final DetailAST ast) {
     return getContainingMethod(ast).findFirstToken(TokenTypes.IDENT);
   }
 }
