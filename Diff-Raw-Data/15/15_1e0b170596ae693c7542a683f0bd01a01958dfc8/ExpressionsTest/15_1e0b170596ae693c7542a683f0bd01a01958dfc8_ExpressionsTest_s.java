 package org.zwobble.shed.compiler.parsing;
 
 import java.util.Collections;
 
 import org.junit.Test;
 import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
 import org.zwobble.shed.compiler.parsing.nodes.CallNode;
 import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
 import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
 import org.zwobble.shed.compiler.parsing.nodes.Nodes;
 import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
 import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
 import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
 import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
 import static org.zwobble.shed.compiler.Option.none;
 import static org.zwobble.shed.compiler.parsing.ParserTesting.isSuccessWithNode;
 import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;
 import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
 import static org.zwobble.shed.compiler.parsing.SourceRange.range;
 
 public class ExpressionsTest {
     @Test public void
     numberLiteralIsExpression() {
         assertThat(
             Expressions.expression().parse(tokens("42")),
             isSuccessWithNode(new NumberLiteralNode("42"))
         );
     }
     
     @Test public void
     stringLiteralIsExpression() {
         assertThat(
             Expressions.expression().parse(tokens("\"Nom nom nom\"")),
             isSuccessWithNode(new StringLiteralNode("Nom nom nom"))
         );
     }
     
     @Test public void
     errorIfValueIsNotExpression() {
         assertThat(
             errorStrings(Expressions.expression().parse(tokens("{"))),
             is(asList("Expected expression but got symbol \"{\""))
         );
     }
     
     @Test public void
     canParseShortLambdaExpressionWithNoArguments() {
         assertThat(
             Expressions.expression().parse(tokens("() => 2")),
             isSuccessWithNode(new ShortLambdaExpressionNode(
                 Collections.<FormalArgumentNode>emptyList(),
                 none(ExpressionNode.class),
                 new NumberLiteralNode("2")
             ))
         );
     }
     
     @Test public void
     canParseShortLambdaExpressionWithOneArgument() {
         assertThat(
             Expressions.expression().parse(tokens("(num : Integer) => 2")),
             isSuccessWithNode(new ShortLambdaExpressionNode(
                 asList(new FormalArgumentNode("num", new VariableIdentifierNode("Integer"))),
                 none(ExpressionNode.class),
                 new NumberLiteralNode("2")
             ))
         );
     }
     
     @Test public void
     canParseShortLambdaExpressionWithMultipleArguments() {
         assertThat(
             Expressions.expression().parse(tokens("(num : Integer, name: String) => 2")),
             isSuccessWithNode(new ShortLambdaExpressionNode(
                 asList(
                     new FormalArgumentNode("num", new VariableIdentifierNode("Integer")),
                     new FormalArgumentNode("name", new VariableIdentifierNode("String"))
                 ),
                 none(ExpressionNode.class),
                 new NumberLiteralNode("2")
             ))
         );
     }
     
     @Test public void
     canParseLongLambdaExpressionWithNoArguments() {
         assertThat(
             Expressions.expression().parse(tokens("() : String => { val x = 2; return 3; }")),
             isSuccessWithNode(new LongLambdaExpressionNode(
                 Collections.<FormalArgumentNode>emptyList(),
                 new VariableIdentifierNode("String"),
                 Nodes.block(
                     Nodes.immutableVar("x", new NumberLiteralNode("2")),
                     new ReturnNode(new NumberLiteralNode("3"))
                 )
             ))
         );
     }
     
     @Test public void
     canEncloseExpressionInParens() {
         assertThat(
             Expressions.expression().parse(tokens("(3)")),
             isSuccessWithNode(new NumberLiteralNode("3"))
         );
     }
     
     @Test public void
     canReferenceVariableByIdentifier() {
         assertThat(
             Expressions.expression().parse(tokens("value")),
             isSuccessWithNode(new VariableIdentifierNode("value"))
         );
     }
     
     @Test public void
     canParseTrueLiteral() {
         assertThat(
             Expressions.expression().parse(tokens("true")),
             isSuccessWithNode(new BooleanLiteralNode(true))
         );
     }
     
     @Test public void
     canParseFalseLiteral() {
         assertThat(
             Expressions.expression().parse(tokens("false")),
             isSuccessWithNode(new BooleanLiteralNode(false))
         );
     }
     
     @Test public void
     canParseSingleFunctionCallWithNoArguments() {
         assertThat(
             Expressions.expression().parse(tokens("now()")),
             isSuccessWithNode(new CallNode(new VariableIdentifierNode("now"), Collections.<ExpressionNode>emptyList()))
         );
     }
     
     @Test public void
     canParseRepeatedFunctionCallsWithNoArguments() {
         assertThat(
             Expressions.expression().parse(tokens("self()()()")),
             isSuccessWithNode(Nodes.call(Nodes.call(Nodes.call(Nodes.id("self")))))
         );
     }
     
     @Test public void
     canParseSingleFunctionCallWithArguments() {
         assertThat(
             Expressions.expression().parse(tokens("max(4, 10)")),
             isSuccessWithNode(Nodes.call(Nodes.id("max"), Nodes.number("4"), Nodes.number("10")))
         );
     }
     
     @Test public void
     canParseRepeatedFunctionCallsWithArguments() {
         assertThat(
             Expressions.expression().parse(tokens("self(1)(2, 3)")),
             isSuccessWithNode(Nodes.call(Nodes.call(Nodes.id("self"), Nodes.number("1")), Nodes.number("2"), Nodes.number("3")))
         );
     }
     
     @Test public void
     canParseMemberAccess() {
         assertThat(
             Expressions.expression().parse(tokens("person.name")),
             isSuccessWithNode(Nodes.member(Nodes.id("person"), "name"))
         );
     }
     
     @Test public void
     canLocateSubMemberAccesses() {
         ParseResult<ExpressionNode> parsedExpression = Expressions.expression().parse(tokens("dancing.tears.eyes"));
         ExpressionNode subAccess = ((MemberAccessNode)parsedExpression.get()).getExpression();
         assertThat(
             parsedExpression.locate(subAccess),
             is(range(position(1, 1), position(1, 14)))
         );
         assertThat(
             parsedExpression.locate(((MemberAccessNode)subAccess).getExpression()),
             is(range(position(1, 1), position(1, 8)))
         );
     }
     
     @Test public void
     canParseTypeApplication() {
         assertThat(
             Expressions.expression().parse(tokens("Map[String, Number]")),
             isSuccessWithNode(Nodes.typeApply(Nodes.id("Map"), Nodes.id("String"), Nodes.id("Number")))
         );
     }
     
     @Test public void
     canParseTypeApplicationAsTypeExpression() {
         assertThat(
             Expressions.typeExpression().parse(tokens("Map[String, Number]")),
             isSuccessWithNode(Nodes.typeApply(Nodes.id("Map"), Nodes.id("String"), Nodes.id("Number")))
         );
     }
     
     @Test public void
     canParseUnitLiteral() {
         assertThat(
             Expressions.expression().parse(tokens("()")),
             isSuccessWithNode(Nodes.unit())
         );
     }
     
     @Test public void
     canParseSimpleAssignment() {
         assertThat(
             Expressions.expression().parse(tokens("x = 4")),
             isSuccessWithNode(Nodes.assign(Nodes.id("x"), Nodes.number("4")))
         );
     }
     
     @Test public void
     assignmentsAreRightAssociative() {
         assertThat(
             Expressions.expression().parse(tokens("x = y = 4")),
             isSuccessWithNode(Nodes.assign(Nodes.id("x"), Nodes.assign(Nodes.id("y"), Nodes.number("4"))))
         );
     }
 }
