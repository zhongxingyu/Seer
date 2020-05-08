 package edu.kit.checkstyle.checks;
 
 import java.util.List;
 
 import com.puppycrawl.tools.checkstyle.api.DetailAST;
 import com.puppycrawl.tools.checkstyle.api.TokenTypes;
 
 import edu.kit.checkstyle.TokenSearcherCheck;
 import static edu.kit.checkstyle.CollectionUtils.*;
 
 
 /**
  * Checks if the 'System.exit' method is called outside of the main method.
  *
  * @since JDK1.7, 07.06.2013
  */
 public class ExitUsageCheck extends TokenSearcherCheck {
 
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
 
   private static final String msg = "The usage of 'System.exit' outside of the main method is discouraged";
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
   * @param property the input string
    */
  public void setCheckedMethods(final String property) {
     final List<Prop> props = mkList();
    for (final String p : property.split(",")) {
       final String[] parts = p.split("[:.]");
       if (parts.length != 2 && parts.length != 3) {
        throw new IllegalArgumentException("format of property '" + property + "' is not supported");
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
