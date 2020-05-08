 package com.gentics.cr.portalnode.expressions;
 
 import com.gentics.api.lib.datasource.Datasource;
 import com.gentics.api.lib.exception.ParserException;
 import com.gentics.api.lib.expressionparser.Expression;
 import com.gentics.api.lib.expressionparser.ExpressionEvaluator;
 import com.gentics.api.lib.expressionparser.ExpressionParser;
 import com.gentics.api.lib.expressionparser.ExpressionParserException;
 import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
 import com.gentics.api.lib.resolving.Resolvable;
 
 /**
  * Helper Class for accessing ExpressionParser methods.
  * @author bigbear3001
  *
  */
 public final class ExpressionParserHelper {
 
   /**
    * {@link ExpressionParser} instance from Gentics Portal.Node API.
    */
   private static ExpressionParser expressionParser =
     ExpressionParser.getInstance();
 
   /**
    * {@link ExpressionEvaluator} instance from Gentics Portal.Node API.
    */
   private static ExpressionEvaluator expressionEvaluator =
     new ExpressionEvaluator();
 
   /**
    * private constructor for the utility class.
    */
   private ExpressionParserHelper() { }
   
   /**
    * Parse the given expression {@link String} into an {@link Expression}.
    * @param rule {@link String} String containing the rule
    * @return {@link Expression} for the rule
    * @throws ParserException when the string cannot be parsed into an
    * {@link Expression}.
    */
   public static Expression parse(final String rule) throws ParserException {
     return expressionParser.parse(rule);
   }
 
   /**
    * Tests if the given {@link Resolvable} matches the {@link Expression}.
    * @param expression {@link Expression} that the {@link Resolvable} must
    * match.
    * @param objectToMatch {@link Resolvable} to test for matching the
    * {@link Expression}.
    * @return true if the {@link Resolvable} matches the {@link Expression}.
    * @throws ExpressionParserException when the expression cannot be matched
    * (is not boolean or contains errors)
    */
   public static boolean match(final Expression expression,
     final Resolvable objectToMatch) throws ExpressionParserException {
     return expressionEvaluator.match(expression, objectToMatch);
   }
 
   /**
    * Tests if the given {@link Resolvable} matches the {@link Expression}.
    * @param rule rule that the {@link Resolvable} must match.
    * @param objectToMatch {@link Resolvable} to test for matching the
    * {@link Expression}.
    * @return true if the {@link Resolvable} matches the {@link Expression}.
    * @throws ExpressionParserException when the expression cannot be matched
    * (is not boolean or contains errors)
    * @throws ParserException when the rule cannot be parsed into an
    * {@link Expression}.
    */
   public static boolean match(final String rule, final Resolvable objectToMatch)
       throws ExpressionParserException, ParserException {
     Expression expression = parse(rule);
     return expressionEvaluator.match(expression, objectToMatch);
   }
   
   /**
    * Create a {@link DatasourceFilter} for the given rule. With the
    * DatasourceFilter you can get Objects from a {@link Datasource}.
    * @param rule Rule to generate the {@link DatasourceFilter} for.
    * @return DatasourceFilter for the rule.
    * @throws ParserException when the rule cannot be parsed into an
    * {@link Expression}.
    * @throws ExpressionParserException when the expression cannot be used with
    * this {@link Datasource}. Gentics Portal.Node API Documentation
    * (http://www.gentics.com/help/topic/com.gentics.portalnode.sdk.doc/misc/doc/apijavadoc/com/gentics/api/lib/datasource/Datasource.html#createDatasourceFilter(com.gentics.api.lib.expressionparser.Expression))
    * doesn't specify when this exception is thrown. Documententation request was
    * sent to Gentics Support @2010-06-07, Ticket number: 37059
    */
   public static DatasourceFilter createDatasourceFilter(final String rule,
       final Datasource ds) throws ParserException, ExpressionParserException {
     Expression expression = parse(rule);
     return ExpressionParserHelper.createDatasourceFilter(expression, ds);
   }
   
   /**
    * Create a {@link DatasourceFilter} for the given expression. With the
    * DatasourceFilter you can get Objects from a {@link Datasource}.
    * @param expression {@link Expression} to generate the
    * {@link DatasourceFilter} for.
    * @return DatasourceFilter for the expression.
    * @throws ParserException when the expression cannot be parsed into an
    * {@link Expression}.
    * @throws ExpressionParserException when the expression cannot be used with
    * this {@link Datasource}. Gentics Portal.Node API Documentation
    * (http://www.gentics.com/help/topic/com.gentics.portalnode.sdk.doc/misc/doc/apijavadoc/com/gentics/api/lib/datasource/Datasource.html#createDatasourceFilter(com.gentics.api.lib.expressionparser.Expression))
    * doesn't specify when this exception is thrown. Documententation request was
    * sent to Gentics Support @2010-06-07, Ticket number: 37059
    */
   public static DatasourceFilter createDatasourceFilter(final Expression expression,
       final Datasource ds) throws ExpressionParserException {
    return ds.createDatasourceFilter(expression);
   }
 
 }
