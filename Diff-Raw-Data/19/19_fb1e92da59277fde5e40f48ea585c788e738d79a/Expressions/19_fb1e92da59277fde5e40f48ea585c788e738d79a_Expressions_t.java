 package org.zwobble.shed.compiler.parsing;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.zwobble.shed.compiler.Option;
 import org.zwobble.shed.compiler.parsing.nodes.AssignmentExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
 import org.zwobble.shed.compiler.parsing.nodes.CallNode;
 import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
 import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
 import org.zwobble.shed.compiler.parsing.nodes.PartialNode;
 import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
 import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
 
 import com.google.common.collect.Lists;
 
 import static org.zwobble.shed.compiler.parsing.Blocks.block;
 import static org.zwobble.shed.compiler.parsing.Rules.firstOf;
 import static org.zwobble.shed.compiler.parsing.Rules.guard;
 import static org.zwobble.shed.compiler.parsing.Rules.oneOrMoreWithSeparator;
 import static org.zwobble.shed.compiler.parsing.Rules.optional;
 import static org.zwobble.shed.compiler.parsing.Rules.sequence;
 import static org.zwobble.shed.compiler.parsing.Rules.symbol;
 import static org.zwobble.shed.compiler.parsing.Rules.then;
 import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
 import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMore;
 import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMoreWithSeparator;
 import static org.zwobble.shed.compiler.parsing.Separator.hardSeparator;
 import static org.zwobble.shed.compiler.parsing.Separator.softSeparator;
 import static org.zwobble.shed.compiler.parsing.SourceRange.range;
 import static org.zwobble.shed.compiler.parsing.TypeReferences.typeSpecifier;
 import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;
 
 
 public class Expressions {
     private static final Rule<?> COMMA = guard(symbol(","));
     
     private static interface PartialCallExpression extends PartialNode {
         ExpressionNode complete(ExpressionNode expression);
     }
     
     public static Rule<ExpressionNode> expression() {
         return new Rule<ExpressionNode>() {
             @Override
             public ParseResult<ExpressionNode> parse(TokenNavigator tokens) {
                 return assignmentExpression().parse(tokens);
             }
         }; 
     }
 
     public static Rule<ExpressionNode> typeExpression() {
         return new Rule<ExpressionNode>() {
             @Override
             public ParseResult<ExpressionNode> parse(TokenNavigator tokens) {
                 final Rule<ExpressionNode> left = variableIdentifier(); 
                 final Rule<List<PartialCallExpression>> typeApplications = zeroOrMore(typeApplication());
                 return then(
                     sequence(OnError.FINISH,
                         left,
                         typeApplications
                     ),
                     foldPartialCalls(left, typeApplications)
                 ).parse(tokens);
             }
         };
     }
     
     private static Rule<ExpressionNode> assignmentExpression() {
         return then(
             oneOrMoreWithSeparator(callExpression(), softSeparator(symbol("="))),
            new ParseAction<List<ExpressionNode>, ExpressionNode>() {
                 @Override
                public ParseResult<ExpressionNode> apply(ParseResult<List<ExpressionNode>> result) {
                    Iterator<ExpressionNode> iterator = Lists.reverse(result.get()).iterator();
                     ExpressionNode expression = iterator.next();
                    ParseResult<ExpressionNode> assignmentResult = result.changeValue(expression, result.locate(expression));
                    
                    SourcePosition end = result.locate(expression).getEnd();
                     while (iterator.hasNext()) {
                        ExpressionNode target = iterator.next();
                        expression = new AssignmentExpressionNode(target, expression);
                        assignmentResult = assignmentResult.changeValue(expression, range(result.locate(target).getStart(), end));
                     }
                    return assignmentResult;
                 }
             }
         );
     }
     
     private static Rule<ExpressionNode> callExpression() {
         final Rule<ExpressionNode> left = primaryExpression(); 
         final Rule<List<PartialCallExpression>> calls = partialCallExpression();
         return then(
             sequence(OnError.FINISH,
                 left,
                 calls
             ),
             foldPartialCalls(left, calls)
          );
     }
 
     private static ParseAction<RuleValues, ExpressionNode> foldPartialCalls(
         final Rule<ExpressionNode> left,
         final Rule<List<PartialCallExpression>> calls) {
         return new ParseAction<RuleValues, ExpressionNode>() {
             @Override
             public ParseResult<ExpressionNode> apply(ParseResult<RuleValues> valuesResult) {
                 RuleValues values = valuesResult.get();
                 ExpressionNode leftExpression = values.get(left);
                 ParseResult<ExpressionNode> result = valuesResult.changeValue(leftExpression);
                 List<PartialCallExpression> callExpressions = values.get(calls);
                 for (PartialCallExpression callExpression : callExpressions) {
                     SourcePosition start = result.locate(leftExpression).getStart();
                     SourcePosition end = result.locate(callExpression).getEnd();
                     SourceRange sourceRange = range(start, end);
                     leftExpression = callExpression.complete(leftExpression);
                     result = result.changeValue(leftExpression, sourceRange);
                 }
                 return result;
             }
         };
     }
 
     @SuppressWarnings("unchecked")
     private static Rule<List<PartialCallExpression>> partialCallExpression() {
         return zeroOrMore(
             firstOf("function call or member access",
                 functionCall(),
                 memberAccess(),
                 typeApplication()
             )
         );
     }
 
     @SuppressWarnings("unchecked")
     private static Rule<ExpressionNode> primaryExpression() {
         return guard(firstOf("expression",
             longLambdaExpression(),
             shortLambdaExpression(),
             variableIdentifier(),
             Literals.numberLiteral(),
             Literals.stringLiteral(),
             Literals.booleanLiteral(),
             Literals.unitLiteral(),
             expressionInParens()
         ));
     }
     
     private static Rule<PartialCallExpression> functionCall() {
         final Rule<List<ExpressionNode>> arguments = argumentList();
         
         return then(
             sequence(OnError.FINISH,
                 guard(symbol("(")),
                 arguments,
                 symbol(")")
             ),
             new SimpleParseAction<RuleValues, PartialCallExpression>() {
                 @Override
                 public PartialCallExpression apply(final RuleValues result) {
                     return new PartialCallExpression() {
                         @Override
                         public ExpressionNode complete(ExpressionNode expression) {
                             return new CallNode(expression, result.get(arguments));
                         }
                     };
                 }
             }
         );
     }
     
     private static Rule<PartialCallExpression> memberAccess() {
         final Rule<String> memberName = tokenOfType(IDENTIFIER);
         return then(
             sequence(OnError.FINISH,
                 guard(symbol(".")),
                 memberName
             ),
             new SimpleParseAction<RuleValues, PartialCallExpression>() {
                 @Override
                 public PartialCallExpression apply(final RuleValues result) {
                     return new PartialCallExpression() {
                         @Override
                         public ExpressionNode complete(ExpressionNode expression) {
                             return new MemberAccessNode(expression, result.get(memberName));
                         }
                     };
                 }
             }
         );
     }
     
     private static Rule<PartialCallExpression> typeApplication() {
         final Rule<List<ExpressionNode>> arguments = zeroOrMoreWithSeparator(typeExpression(), softSeparator(COMMA));
         
         return then(
             sequence(OnError.FINISH,
                 guard(symbol("[")),
                 arguments,
                 symbol("]")
             ),
             new SimpleParseAction<RuleValues, PartialCallExpression>() {
                 @Override
                 public PartialCallExpression apply(final RuleValues result) {
                     return new PartialCallExpression() {
                         @Override
                         public ExpressionNode complete(ExpressionNode expression) {
                             return new TypeApplicationNode(expression, result.get(arguments));
                         }
                     };
                 }
             }
         );
     }
 
     private static Rule<ExpressionNode> variableIdentifier() {
         return then(
             tokenOfType(IDENTIFIER),
             new SimpleParseAction<String, ExpressionNode>() {
                 @Override
                 public ExpressionNode apply(String result) {
                     return new VariableIdentifierNode(result);
                 }
             }
         );
     }
 
     private static Rule<ExpressionNode> expressionInParens() {
         final Rule<ExpressionNode> expression = expression();
         return then(
             sequence(OnError.FINISH,
                 guard(symbol("(")),
                 expression,
                 symbol(")")
             ),
             new SimpleParseAction<RuleValues, ExpressionNode>() {
                 @Override
                 public ExpressionNode apply(RuleValues result) {
                     return result.get(expression);
                 }
             }
         );
     }
     
     private static Rule<LongLambdaExpressionNode> longLambdaExpression() {
         final Rule<List<FormalArgumentNode>> formalArguments;
         final Rule<ExpressionNode> returnType;
         final Rule<BlockNode> functionBody;
         return then(
             sequence(OnError.FINISH,
                 formalArguments = guard(formalArgumentList()),
                 returnType = guard(typeSpecifier()),
                 guard(symbol("=>")),
                 functionBody = guard(block())
             ),
             new SimpleParseAction<RuleValues, LongLambdaExpressionNode>() {
                 @Override
                 public LongLambdaExpressionNode apply(RuleValues result) {
                     return new LongLambdaExpressionNode(
                         result.get(formalArguments),
                         result.get(returnType),
                         result.get(functionBody)
                     );
                 }
             }
         );
     }
     
     private static Rule<ShortLambdaExpressionNode> shortLambdaExpression() {
         final Rule<List<FormalArgumentNode>> formalArguments;
         final Rule<Option<ExpressionNode>> returnType;
         final Rule<ExpressionNode> functionBody = expression();
         return then(
             sequence(OnError.FINISH,
                 formalArguments = guard(formalArgumentList()),
                 returnType = optional(typeSpecifier()),
                 guard(symbol("=>")),
                 functionBody
             ),
             new SimpleParseAction<RuleValues, ShortLambdaExpressionNode>() {
                 @Override
                 public ShortLambdaExpressionNode apply(RuleValues result) {
                     return new ShortLambdaExpressionNode(
                         result.get(formalArguments),
                         result.get(returnType),
                         result.get(functionBody)
                     );
                 }
             }
         );
     }
     
     private static Rule<List<FormalArgumentNode>> formalArgumentList() {
         final Rule<List<FormalArgumentNode>> formalArguments = zeroOrMoreWithSeparator(formalArgument(), hardSeparator(COMMA));
         return then(
             sequence(OnError.FINISH,
                 guard(symbol("(")),
                 formalArguments,
                 guard(symbol(")"))
             ),
             new SimpleParseAction<RuleValues, List<FormalArgumentNode>>() {
                 @Override
                 public List<FormalArgumentNode> apply(RuleValues result) {
                     return result.get(formalArguments);
                 }
             }
         );
         
     }
 
     private static Rule<FormalArgumentNode> formalArgument() {
         final Rule<String> name;
         final Rule<ExpressionNode> type = typeSpecifier();
         return then(
             sequence(OnError.FINISH,
                 name = guard(tokenOfType(IDENTIFIER)),
                 type
             ),
             new SimpleParseAction<RuleValues, FormalArgumentNode>() {
                 @Override
                 public FormalArgumentNode apply(RuleValues result) {
                     return new FormalArgumentNode(result.get(name), result.get(type));
                 }
             }
         );
     }
     
     private static Rule<List<ExpressionNode>> argumentList() {
         return zeroOrMoreWithSeparator(expression(), softSeparator(COMMA));
     }
 }
