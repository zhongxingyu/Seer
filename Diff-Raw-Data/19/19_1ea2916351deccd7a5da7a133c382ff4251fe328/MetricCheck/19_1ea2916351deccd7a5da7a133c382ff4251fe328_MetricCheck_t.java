 package edu.kit.checkstyle.checks.metrics;
 
 import com.puppycrawl.tools.checkstyle.api.Check;
 import com.puppycrawl.tools.checkstyle.api.DetailAST;
 import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
 
 
 /**
  * Base class for checks that report metrics. All metric messages sent by this
  * class are reported as {@link SeverityLevel#INFO}.
  *
  * @since JDK1.7, Jul 14, 2013
  */
 public abstract class MetricCheck extends Check {
 
   @Override
   public final void init() {
     setSeverity(SeverityLevel.INFO.getName());
   }
 
   /**
    * Overridden to catch exceptions on which it is not ease to react on
    * afterwards because Checkstyle just prints them out to stdout.
    *
    * Implement {@link this#execute(DetailAST)} to visit a token.
    */
   @Override
   public final void visitToken(final DetailAST ast) {
     try {
       execute(ast);
     } catch (final Exception e) {
      System.err.println("Error in AST '" + ast + "' in file '" + getFileContents().getFilename() + "'");
       e.printStackTrace();
     }
   }
 
   /**
    * Needs to be implemented instead of {@link Check#visitToken(DetailAST)}.
    */
   protected abstract void execute(DetailAST ast);
 
   /**
    * Logs a metric message.
    *
    * @param ast
    *        The AST the metric should be reported at
    * @param value
    *        The value calculated by the metric
    */
   protected final void logMetric(final DetailAST ast, final Object value) {
     log(ast, "metric:" + metric() + ":" + value);
   }
 
   /**
    * The message that classifies the metric.
    */
   protected abstract String metric();
 
   /**
    * Counts the number of token of a specific type that are children of another
    * token.
    *
    * @param ast
    *        the token that is searched
    * @param type
    *        the token type to search for
    * @return the number of tokens found
    */
   protected int countTokenType(final DetailAST ast, final int type) {
     int counter = 0;
     DetailAST cur = ast.findFirstToken(type);
     while (cur != null) {
       if (cur.getType() == type) {
         counter += 1;
       }
       cur = cur.getNextSibling();
     }
     return counter;
   }
 
 }
