 package edu.kit.checkstyle.checks.metrics;
 
 import com.puppycrawl.tools.checkstyle.api.DetailAST;
 import com.puppycrawl.tools.checkstyle.api.TokenTypes;
 
 
 /**
  * Counts the number of statements per method. As method count normal methods
  * and constructors as well as instance and static initializers.
  * <p>
  * Every Statement in counted with exception of the ones that occur together
  * with a keyword. That means that 'return 1+1;' or 'if (1+1 == 2)' do not count
  * as statements.
  *
  * @since JDK1.7, Jul 17, 2013
  */
 public class NumberOfStatementsPerMethodCheck extends MetricCheck {
 
   public static final String METRIC = "number-of-statements-per-class";
 
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
    if (body != null) {
      logMetric(ast, countOfAST(body));
    }
   }
 
   private int countOfAST(final DetailAST ast) {
     int counter = 0;
     for (DetailAST cur = ast.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
       counter += countOfToken(cur);
     }
     return counter;
   }
 
   private int countOfToken(final DetailAST ast) {
     switch (ast.getType()) {
       case TokenTypes.VARIABLE_DEF:
       case TokenTypes.EXPR:
       case TokenTypes.RESOURCE:
       case TokenTypes.LITERAL_NEW:
       case TokenTypes.LITERAL_ASSERT:
         return 1;
 
       case TokenTypes.LITERAL_SWITCH:
         int switchCounter = 0;
         for (DetailAST cur = ast.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
           if (cur.getType() == TokenTypes.CASE_GROUP) {
             switchCounter += countOfToken(cur);
           }
         }
         return switchCounter;
 
       case TokenTypes.CASE_GROUP:
       case TokenTypes.LITERAL_FOR:
       case TokenTypes.LITERAL_WHILE:
       case TokenTypes.LITERAL_DO:
       case TokenTypes.LITERAL_CATCH:
       case TokenTypes.LITERAL_FINALLY:
       case TokenTypes.LITERAL_SYNCHRONIZED:
         return countOfAST(ast.findFirstToken(TokenTypes.SLIST));
 
       case TokenTypes.LITERAL_TRY:
         int tryCounter = 0;
         for (DetailAST cur = ast.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
           switch (cur.getType()) {
             case TokenTypes.SLIST:
               tryCounter += countOfAST(cur); break;
             case TokenTypes.LITERAL_CATCH:
             case TokenTypes.LITERAL_FINALLY:
               tryCounter += countOfToken(cur);
           }
         }
         return tryCounter;
 
       case TokenTypes.LITERAL_IF:
         int ifCounter = 0;
         for (DetailAST cur = ast.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
           switch (cur.getType()) {
             case TokenTypes.SLIST:
               ifCounter += countOfAST(cur); break;
             case TokenTypes.LITERAL_ELSE:
               ifCounter += countOfToken(cur);
           }
         }
         return ifCounter;
 
       case TokenTypes.LITERAL_ELSE:
         final DetailAST elseBody = ast.findFirstToken(TokenTypes.SLIST);
         return elseBody == null ?
           countOfToken(ast.findFirstToken(TokenTypes.LITERAL_IF)) :
           countOfAST(elseBody);
 
       case TokenTypes.RESOURCE_SPECIFICATION:
         return countOfAST(ast.findFirstToken(TokenTypes.RESOURCES));
 
       case TokenTypes.LCURLY:
         return countOfAST(ast);
 
       default:
         return 0;
     }
   }
 
 }
