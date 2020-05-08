 package org.zwobble.shed.compiler.codegenerator;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Test;
 import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptExpressionNode;
 import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
 import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
 import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatementNode;
 
 import com.google.common.collect.ImmutableMap;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class JavaScriptWriterTest {
     private final JavaScriptWriter writer = new JavaScriptWriter();
     private final JavaScriptNodes js = new JavaScriptNodes();
     
     @Test public void
     booleanIsWrittenAsTrueOrFalseKeywords() {
         assertThat(writer.write(js.bool(true)), is("true"));
         assertThat(writer.write(js.bool(false)), is("false"));
     }
     
     @Test public void
     numberIsWrittenAsValue() {
         assertThat(writer.write(js.number("-4.2")), is("-4.2"));
     }
     
     @Test public void
     stringIsEnclosedInDoubleQuotes() {
         assertThat(writer.write(js.string("Dustbowl Dance")), is("\"Dustbowl Dance\""));
     }
     
     @Test public void
     stringsAreEscaped() {
         assertThat(writer.write(js.string("\"Invisible\nLight\"")), is("\"\\\"Invisible\\nLight\\\"\""));
     }
     
     @Test public void
     identifiersWriteOutIdentifiersVerbatim() {
         assertThat(writer.write(js.id("this.is.the.life")), is("this.is.the.life"));
     }
     
     @Test public void
     functionCallsIncludeObjectAndArguments() {
         assertThat(
             writer.write(js.call(js.id("_.map"), js.id("cushions"), js.id("embroidered"))),
             is("_.map(cushions, embroidered)")
         );
     }
     
     @Test public void
     anonymousFunctionsAreWrappedInParenthesesBeforeCall() {
         JavaScriptExpressionNode node = js.func(Collections.<String>emptyList(), Collections.<JavaScriptStatementNode>emptyList());
         assertThat(
             writer.write(js.call(node)),
             is("(function() {\n})()")
         );
     }
     
     @Test public void
     returnHasReturnKeywordWithValue() {
         assertThat(
             writer.write(js.ret(js.bool(true))),
             is("return true;")
         );
     }
     
     @Test public void
     variableDeclarationsIncludeVariableNameAndInitialValue() {
         assertThat(
             writer.write(js.var("theLoneliestNumber", js.number("1"))),
             is("var theLoneliestNumber = 1;")
         );
     }
     
     @Test public void
     listOfStatementsIsConvertedWithNewlinesBetweenStatements() {
         assertThat(
             writer.write(js.statements(js.var("it", js.string("what")), js.ret(js.bool(true)))),
             is("var it = \"what\";\nreturn true;")
         );
     }
     
     @Test public void
     functionDeclarationsIncludeArgumentsAndClosingBraceOnNewLine() {
         assertThat(
             writer.write(js.func(asList("telegraph", "road"), Collections.<JavaScriptStatementNode>emptyList())),
             is("function(telegraph, road) {\n}")
         );
     }
     
     @Test public void
     bodiesOfFunctionsAreIndented() {
         assertThat(
             writer.write(js.func(Collections.<String>emptyList(), asList(
                 js.var("listeningTo", js.func(Collections.<String>emptyList(), asList((JavaScriptStatementNode)js.ret(js.string("Boys of Summer"))))),
                 js.ret(js.call(js.id("listeningTo")))
             ))),
             is("function() {\n" +
                "    var listeningTo = function() {\n" +
                "        return \"Boys of Summer\";\n" +
                "    };\n" +
                "    return listeningTo();\n" +
                "}")
         );
     }
     
     @Test public void
     expressionStatementsAreAsExpressionButWithTrailingSemiColon() {
         assertThat(
             writer.write(js.expressionStatement(js.bool(true))),
             is("true;")
         );
     }
     
     @Test public void
     emptyObjectIsOpeningAndClosingBraceWithoutSeparatingWhitespace() {
         assertThat(
             writer.write(js.object(ImmutableMap.<String, JavaScriptExpressionNode>of())),
             is("{}")
         );
     }
     
     @Test public void
     objectIsCommaSeparatedPropertiesSurroundedByBraces() {
         assertThat(
            writer.write(js.object(ImmutableMap.<String, JavaScriptExpressionNode>of(
                "name", js.id("name"),
                "job", js.undefined()
            ))),
            is("{\n    \"name\": name,\n    \"job\": undefined\n}")
         );
     }
     
     @Test public void
     propertyAccessIsExpressionAndPropertyNameSeparatedByDot() {
         assertThat(
             writer.write(js.propertyAccess(js.id("user"), "height")),
             is("user.height")
         );
     }
     
     @Test public void
     ifThenElseWritesConditionAndBranches() {
         assertThat(
             write(js.ifThenElse(
                 js.id("isMorning"),
                 Arrays.<JavaScriptStatementNode>asList(js.expressionStatement(js.call(js.id("eatCereal")))),
                 Arrays.<JavaScriptStatementNode>asList(js.expressionStatement(js.call(js.id("eatPudding"))))
             ), 1),
             is("    if (isMorning) {\n        eatCereal();\n    } else {\n        eatPudding();\n    }")
         );
     }
     
     @Test public void
     whileLoopWritesConditionAndBody() {
         assertThat(
             write(js.whileLoop(
                 js.id("isMorning"),
                 js.expressionStatement(js.call(js.id("beSleepy")))
             ), 1),
             is("    while (isMorning) {\n        beSleepy();\n    }")
         );
     }
     
     @Test public void
     emptyWhileLoopSplitsBracesWithNewLine() {
         assertThat(
             write(js.whileLoop(
                 js.id("isMorning")
             ), 1),
             is("    while (isMorning) {\n    }")
         );
     }
     
     @Test public void
     binaryOperatorWritesOperatorAndBothOperands() {
         assertThat(
             write(js.operator("!==", js.id("first"), js.id("second")), 0),
             is("first !== second")
         );
     }
     
     @Test public void
     assignmentWritesOutBothSides() {
         assertThat(
             write(js.assign(js.id("first"), js.id("second")), 0),
             is("first = second")
         );
     }
     
     private String write(JavaScriptNode node, int indentationLevel) {
         StringBuilder builder = new StringBuilder();
         writer.write(node, builder, indentationLevel);
         return builder.toString();
     }
 }
