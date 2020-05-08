 package edu.kit.checkstyle.checks.metrics;
 
 import com.puppycrawl.tools.checkstyle.api.DetailAST;
 import com.puppycrawl.tools.checkstyle.api.TokenTypes;
 
 
 /**
  * Counts the maximum depth size of a method, which is the same as the number of
  * nested blocks.
  * <p>
  * As block counts every pair of matching braces, which means that occurrences
  * of control structures increase the depth by 1. If there are no blocks in a
  * method, the depth is zero.
  *
  * @since JDK1.7, Jul 18, 2013
  */
 public class MaxNestedBlockDepthCheck extends MetricCheck {
 
   public static final String METRIC = "max-nested-block-depth";
 
   @Override
   protected String metric() {
     return METRIC;
   }
 
   @Override
   public int[] getDefaultTokens() {
     return new int[] { TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF, TokenTypes.INSTANCE_INIT, TokenTypes.STATIC_INIT };
   }
 
   @Override
   protected void execute(final DetailAST ast) {
     final DetailAST body = ast.findFirstToken(TokenTypes.SLIST);
    logMetric(ast, maxDepthOfAST(body, 0));
   }
 
   private int maxDepthOfAST(final DetailAST ast, final int depth) {
     int max = depth;
     for (DetailAST cur = ast.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
       final int m = maxDepthOfToken(cur, depth);
       max = m > max ? m : max;
     }
     return max;
   }
 
   private int maxDepthOfToken(final DetailAST ast, final int depth) {
     switch (ast.getType()) {
       case TokenTypes.LITERAL_SWITCH:
         int switchMax = depth + 1;
         for (DetailAST cur = ast.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
           if (cur.getType() == TokenTypes.CASE_GROUP) {
             final int m = maxDepthOfToken(cur, switchMax);
             switchMax = m > switchMax ? m : switchMax;
           }
         }
         return switchMax;
 
       case TokenTypes.CASE_GROUP:
       case TokenTypes.LITERAL_FOR:
       case TokenTypes.LITERAL_WHILE:
       case TokenTypes.LITERAL_DO:
       case TokenTypes.LITERAL_CATCH:
       case TokenTypes.LITERAL_FINALLY:
       case TokenTypes.LITERAL_SYNCHRONIZED:
         return maxDepthOfAST(ast.findFirstToken(TokenTypes.SLIST), depth + 1);
 
       case TokenTypes.LITERAL_TRY:
         int tryMax = depth;
         for (DetailAST cur = ast.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
           switch (cur.getType()) {
             case TokenTypes.SLIST:
               final int m = maxDepthOfAST(cur, depth + 1);
               tryMax = m > tryMax ? m : tryMax;
               break;
             case TokenTypes.LITERAL_CATCH:
             case TokenTypes.LITERAL_FINALLY:
               final int n = maxDepthOfToken(cur, depth);
               tryMax = n > tryMax ? n : tryMax;
           }
         }
         return tryMax;
 
       case TokenTypes.LITERAL_IF:
         int ifMax = depth;
         for (DetailAST cur = ast.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
           switch (cur.getType()) {
             case TokenTypes.SLIST:
               final int m = maxDepthOfAST(cur, depth + 1);
               ifMax = m > ifMax ? m : ifMax;
               break;
             case TokenTypes.LITERAL_ELSE:
               final int n = maxDepthOfToken(cur, depth);
               ifMax = n > ifMax ? n : ifMax;
           }
         }
         return ifMax;
 
       case TokenTypes.LITERAL_ELSE:
         final DetailAST elseBody = ast.findFirstToken(TokenTypes.SLIST);
         return elseBody == null ?
           maxDepthOfToken(ast.findFirstToken(TokenTypes.LITERAL_IF), depth) :
           maxDepthOfAST(elseBody, depth + 1);
 
       case TokenTypes.LCURLY:
         return maxDepthOfAST(ast, depth + 1);
 
       default:
         return 0;
     }
   }
 }
