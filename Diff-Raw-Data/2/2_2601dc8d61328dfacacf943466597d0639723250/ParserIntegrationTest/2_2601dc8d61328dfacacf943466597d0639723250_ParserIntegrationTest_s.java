 package org.sterling.source.parser;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.sterling.source.LocationRangeTest.range;
 import static org.sterling.source.scanner.ScannerFactoryTest.scanner;
 import static org.sterling.source.syntax.BooleanTokenTest.booleanToken;
 import static org.sterling.source.syntax.IntegerTokenTest.integer;
 import static org.sterling.source.syntax.NodeKind.*;
 import static org.sterling.source.syntax.SourceNodeFactory.node;
 import static org.sterling.source.syntax.SourceNodeFactory.tree;
 import static org.sterling.source.syntax.StringTokenTest.string;
 import static org.sterling.source.syntax.SubTreeMatcher.any;
 import static org.sterling.source.syntax.SubTreeMatcher.anySubTree;
 import static org.sterling.source.syntax.SubTreeMatcher.containsSubTree;
 import static org.sterling.source.syntax.SubTreeMatcher.subTree;
 import static org.sterling.source.syntax.TokenTest.token;
 import static org.sterling.util.StringUtil.asLines;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.sterling.SterlingException;
 import org.sterling.source.syntax.SourceNode;
 
 public class ParserIntegrationTest {
 
     private Parser parser;
 
     @Before
     public void setUp() {
         parser = new Parser();
     }
 
     @Test
     public void shouldGetTotalRangeOfProgram() throws SterlingException {
         assertThat(parse("main = weather.is_sunny").getRange(), equalTo(range(0, 0, 25)));
     }
 
     @Test
     public void shouldParseEmptyInput() throws SterlingException {
         assertThat(parse(""), equalTo(node(MODULE_DECLARATION, node(token(END_OF_INPUT, "\0", range(-1, 0, 1))))));
     }
 
     @Test
     public void shouldParseArgumentsExpression() throws SterlingException {
         assertThat(parse("main = apples 1 2 3"), containsSubTree(
             node(SELECTOR_EXPRESSION,
                 tree(
                     node(PRIMARY_EXPRESSION),
                     node(QUALIFIED_IDENTIFIER),
                     node(token(IDENTIFIER, "apples", range(0, 7, 6)))),
                 node(SELECTOR_EXPRESSION_TAIL,
                     node(ARGUMENTS_EXPRESSION,
                         tree(
                             node(ARGUMENT),
                             node(PRIMARY_EXPRESSION),
                             node(LITERAL_EXPRESSION),
                             node(INTEGER_LITERAL),
                             node(integer(token(INTEGER, "1", range(0, 14, 1)), 1))),
                         node(ARGUMENTS_EXPRESSION_TAIL,
                             tree(
                                 node(ARGUMENT),
                                 node(PRIMARY_EXPRESSION),
                                 node(LITERAL_EXPRESSION),
                                 node(INTEGER_LITERAL),
                                 node(integer(token(INTEGER, "2", range(0, 16, 1)), 2))),
                             node(ARGUMENTS_EXPRESSION_TAIL,
                                 tree(
                                     node(ARGUMENT),
                                     node(PRIMARY_EXPRESSION),
                                     node(LITERAL_EXPRESSION),
                                     node(INTEGER_LITERAL),
                                     node(integer(token(INTEGER, "3", range(0, 18, 1)), 3))))))))));
     }
 
     @Test
     public void shouldParseBooleanFalse() throws SterlingException {
         assertThat(parse("main = False"), containsSubTree(tree(
             node(PRIMARY_EXPRESSION),
             node(LITERAL_EXPRESSION),
             node(BOOLEAN_LITERAL),
             node(booleanToken(token(BOOLEAN, "False", range(0, 7, 5)), false))
         )));
     }
 
     @Test
     public void shouldParseBooleanTrue() throws SterlingException {
         assertThat(parse("main = True"), containsSubTree(tree(
             node(PRIMARY_EXPRESSION),
             node(LITERAL_EXPRESSION),
             node(BOOLEAN_LITERAL),
             node(booleanToken(token(BOOLEAN, "True", range(0, 7, 4)), true))
         )));
     }
 
     @Test
     public void shouldParseCompleteTernaryExpression() throws SterlingException {
         assertThat(parse("main = if apples then True else False"), containsSubTree(
             node(TERNARY_EXPRESSION,
                 node(token(KEYWORD_IF, "if", range(0, 7, 2))),
                 subTree(
                     node(PRIMARY_EXPRESSION),
                     node(QUALIFIED_IDENTIFIER),
                     node(token(IDENTIFIER, "apples", range(0, 10, 6)))),
                 node(token(TERNARY_TRUE, "then", range(0, 17, 4))),
                 subTree(
                     node(PRIMARY_EXPRESSION),
                     node(LITERAL_EXPRESSION),
                     node(BOOLEAN_LITERAL),
                     node(booleanToken(token(BOOLEAN, "True", range(0, 22, 4)), true))),
                 node(token(TERNARY_FALSE, "else", range(0, 27, 4))),
                 node(TERNARY_EXPRESSION_TAIL,
                     node(LOGICAL_OR_EXPRESSION,
                         subTree(
                             node(PRIMARY_EXPRESSION),
                             node(LITERAL_EXPRESSION),
                             node(BOOLEAN_LITERAL),
                             node(booleanToken(token(BOOLEAN, "False", range(0, 32, 5)), false))))))));
     }
 
     @Test
     public void shouldParseIdentifier() throws SterlingException {
         assertThat(parse("main = apples"), containsSubTree(tree(
             node(PRIMARY_EXPRESSION),
             node(QUALIFIED_IDENTIFIER),
             node(token(IDENTIFIER, "apples", range(0, 7, 6)))
         )));
     }
 
     @Test
     public void shouldParseFunctionAsArgument() throws SterlingException {
         assertThat(parse("main = 2.times (x) -> say x"), containsSubTree(tree(
             node(ARGUMENTS_EXPRESSION),
             node(ARGUMENT),
             node(PRIMARY_EXPRESSION),
             node(FUNCTION_LITERAL,
                 node(token(GROUP_OPEN, "(", range(0, 15, 1))),
                 node(FUNCTION_ARGUMENTS,
                     node(LAMBDA_ARGUMENT,
                         node(token(IDENTIFIER, "x", range(0, 16, 1))))),
                 node(token(GROUP_CLOSE, ")", range(0, 17, 1))),
                 node(token(ARROW, "->", range(0, 19, 2))),
                 any(EXPRESSION)))));
     }
 
     @Test
     public void shouldParseLambdaAsArgument() throws SterlingException {
         assertThat(parse("main = 2.times x -> say x"), containsSubTree(tree(
             node(ARGUMENTS_EXPRESSION),
             node(ARGUMENT),
             node(PRIMARY_EXPRESSION),
             node(LAMBDA_LITERAL,
                 node(LAMBDA_ARGUMENT,
                     node(token(IDENTIFIER, "x", range(0, 15, 1)))),
                 node(token(ARROW, "->", range(0, 17, 2))),
                 any(EXPRESSION)))));
     }
 
     @Test
     public void shouldParseLambdasOnTwoLines() throws SterlingException {
         assertThat(parse("apples = 1", "oranges = 2"), containsSubTree(
             node(DECLARATION_SEQUENCE,
                 node(DECLARATION,
                     node(token(IDENTIFIER, "apples", range(0, 0, 6))),
                     node(token(ASSIGN, "=", range(0, 7, 1))),
                     any(DECLARATION_LITERAL)),
                 node(DECLARATION_SEQUENCE_TAIL,
                     node(token(TERMINATOR, "\n", range(0, 10, 1))),
                     node(DECLARATION_SEQUENCE,
                         node(DECLARATION,
                             node(token(IDENTIFIER, "oranges", range(1, 0, 7))),
                             node(token(ASSIGN, "=", range(1, 8, 1))),
                             any(DECLARATION_LITERAL)),
                         node(DECLARATION_SEQUENCE_TAIL,
                             node(token(TERMINATOR, "\n", range(1, 11, 1)))))))));
     }
 
     @Test
     public void shouldParseMultipleExpressions() throws SterlingException {
         assertThat(parse("x = apples 1 2 3\ny = oranges 4 5"), containsSubTree(
             node(DECLARATION_SEQUENCE,
                 subTree(
                     node(SELECTOR_EXPRESSION,
                         tree(
                             node(PRIMARY_EXPRESSION),
                             node(QUALIFIED_IDENTIFIER),
                             node(token(IDENTIFIER, "apples", range(0, 4, 6)))),
                         any(SELECTOR_EXPRESSION_TAIL))),
                 node(DECLARATION_SEQUENCE_TAIL,
                     node(token(TERMINATOR, "\n", range(0, 16, 1))),
                     node(DECLARATION_SEQUENCE,
                         subTree(
                             node(SELECTOR_EXPRESSION,
                                 tree(
                                     node(PRIMARY_EXPRESSION),
                                     node(QUALIFIED_IDENTIFIER),
                                     node(token(IDENTIFIER, "oranges", range(1, 4, 7)))),
                                 any(SELECTOR_EXPRESSION_TAIL))),
                         node(DECLARATION_SEQUENCE_TAIL,
                             node(token(TERMINATOR, "\n", range(1, 15, 1)))))))));
     }
 
     @Test
     public void shouldParseFunction() throws SterlingException {
         assertThat(parse("y = (m x b) -> m * x + b"), containsSubTree(
             node(DECLARATION_SEQUENCE,
                 node(DECLARATION,
                     node(token(IDENTIFIER, "y", range(0, 0, 1))),
                     node(token(ASSIGN, "=", range(0, 2, 1))),
                     node(DECLARATION_LITERAL,
                         node(FUNCTION_LITERAL,
                             node(token(GROUP_OPEN, "(", range(0, 4, 1))),
                             node(FUNCTION_ARGUMENTS,
                                 node(LAMBDA_ARGUMENT,
                                     node(token(IDENTIFIER, "m", range(0, 5, 1)))),
                                 node(FUNCTION_ARGUMENTS_TAIL,
                                     node(LAMBDA_ARGUMENT,
                                         node(token(IDENTIFIER, "x", range(0, 7, 1)))),
                                     node(FUNCTION_ARGUMENTS_TAIL,
                                         node(LAMBDA_ARGUMENT,
                                             node(token(IDENTIFIER, "b", range(0, 9, 1))))))),
                             node(token(GROUP_CLOSE, ")", range(0, 10, 1))),
                             node(token(ARROW, "->", range(0, 12, 2))),
                             any(EXPRESSION)))),
                 node(DECLARATION_SEQUENCE_TAIL,
                     node(token(TERMINATOR, "\n", range(0, 24, 1)))))));
     }
 
     @Test
     public void shouldParseLambda() throws SterlingException {
         assertThat(parse("y = x -> m * x + b"), containsSubTree(
             node(DECLARATION,
                 node(token(IDENTIFIER, "y", range(0, 0, 1))),
                 node(token(ASSIGN, "=", range(0, 2, 1))),
                 node(DECLARATION_LITERAL,
                     node(LAMBDA_LITERAL,
                         node(LAMBDA_ARGUMENT,
                             node(token(IDENTIFIER, "x", range(0, 4, 1)))),
                         node(token(ARROW, "->", range(0, 6, 2))),
                         any(EXPRESSION))))));
     }
 
     @Test
     public void shouldParseConstantExpression() throws SterlingException {
         assertThat(parse("y = m * x + b"), containsSubTree(
             node(DECLARATION,
                 node(token(IDENTIFIER, "y", range(0, 0, 1))),
                 node(token(ASSIGN, "=", range(0, 2, 1))),
                 node(DECLARATION_LITERAL,
                     node(CONSTANT_EXPRESSION,
                         any(EXPRESSION))))));
     }
 
     @Test
     public void shouldParseNamedLambdaWithoutArgumentsAndParentheticalBody() throws SterlingException {
         SourceNode tree = parse("y = (m * x + b)");
         assertThat(tree, containsSubTree(
             node(DECLARATION,
                 node(token(IDENTIFIER, "y", range(0, 0, 1))),
                 node(token(ASSIGN, "=", range(0, 2, 1))),
                 node(DECLARATION_LITERAL,
                     node(CONSTANT_EXPRESSION,
                         any(EXPRESSION))))));
         assertThat(tree, containsSubTree(
             node(PARENTHETICAL_EXPRESSION,
                 node(token(GROUP_OPEN, "(", range(0, 4, 1))),
                 anySubTree(),
                 node(token(GROUP_CLOSE, ")", range(0, 14, 1))))));
     }
 
     @Test
     public void shouldParseParentheticalAsArgument() throws SterlingException {
         assertThat(parse("main = 2.times (oranges + 1)"), containsSubTree(tree(
             node(ARGUMENTS_EXPRESSION),
             node(ARGUMENT),
             node(PRIMARY_EXPRESSION),
             node(PARENTHETICAL_EXPRESSION,
                 node(token(GROUP_OPEN, "(", range(0, 15, 1))),
                 anySubTree(),
                 node(token(GROUP_CLOSE, ")", range(0, 27, 1)))))));
     }
 
     @Test
     public void shouldParseQualifiedIdentifier() throws SterlingException {
         assertThat(parse("main = weather.is_sunny"), containsSubTree(
             node(PRIMARY_EXPRESSION,
                 node(QUALIFIED_IDENTIFIER,
                     node(token(IDENTIFIER, "weather", range(0, 7, 7))),
                     node(QUALIFIED_IDENTIFIER_TAIL,
                         node(token(ACCESSOR, ".", range(0, 14, 1))),
                         node(token(IDENTIFIER, "is_sunny", range(0, 15, 8))))))));
     }
 
     @Test
     public void shouldParseSingleIdentifier() throws SterlingException {
         assertThat(parse("main = bananas"), containsSubTree(tree(
             node(PRIMARY_EXPRESSION),
             node(QUALIFIED_IDENTIFIER),
             node(token(IDENTIFIER, "bananas", range(0, 7, 7)))
         )));
     }
 
     @Test
     public void shouldParseSingleInteger() throws SterlingException {
         assertThat(parse("main = 2"), containsSubTree(tree(
             node(PRIMARY_EXPRESSION),
             node(LITERAL_EXPRESSION),
             node(INTEGER_LITERAL),
             node(integer(token(INTEGER, "2", range(0, 7, 1)), 2))
         )));
     }
 
     @Test
     public void shouldParseTwoPlusTwo() throws SterlingException {
         assertThat(parse("main = 2 + 2"), containsSubTree(
             node(ADDITIVE_EXPRESSION,
                 subTree(
                     node(PRIMARY_EXPRESSION),
                     node(LITERAL_EXPRESSION),
                     node(INTEGER_LITERAL),
                     node(integer(token(INTEGER, "2", range(0, 7, 1)), 2))),
                 node(ADDITIVE_EXPRESSION_TAIL,
                     node(token(ADD, "+", range(0, 9, 1))),
                     subTree(
                         node(PRIMARY_EXPRESSION),
                         node(LITERAL_EXPRESSION),
                         node(INTEGER_LITERAL),
                         node(integer(token(INTEGER, "2", range(0, 11, 1)), 2)))))));
     }
 
     @Test
     public void shouldParseSayExpression() throws SterlingException {
         assertThat(parse("main = say $ 2 + 2"), containsSubTree(
             node(SELECTOR_EXPRESSION,
                 tree(
                     node(PRIMARY_EXPRESSION),
                     node(QUALIFIED_IDENTIFIER),
                     node(token(IDENTIFIER, "say", range(0, 7, 3)))),
                 tree(
                     node(SELECTOR_EXPRESSION_TAIL),
                     node(ARGUMENTS_EXPRESSION),
                     node(ARGUMENT),
                     node(APPLY_EXPRESSION,
                         node(token(APPLY, "$", range(0, 11, 1))),
                         subTree(node(ADDITIVE_EXPRESSION,
                             subTree(
                                 node(PRIMARY_EXPRESSION),
                                 node(LITERAL_EXPRESSION),
                                 node(INTEGER_LITERAL),
                                 node(integer(token(INTEGER, "2", range(0, 13, 1)), 2))),
                             node(ADDITIVE_EXPRESSION_TAIL,
                                 node(token(ADD, "+", range(0, 15, 1))),
                                 subTree(
                                     node(PRIMARY_EXPRESSION),
                                     node(LITERAL_EXPRESSION),
                                     node(INTEGER_LITERAL),
                                     node(integer(token(INTEGER, "2", range(0, 17, 1)), 2)))))))))));
     }
 
     @Test
     public void shouldParseObjectWithArgumentsList() throws SterlingException {
         SourceNode tree = parse(
             "Python = object (x y) {",
             "    spam = y * 42",
             "    eggs = x",
             "    vikings = True",
             "    location = \"Green Midget Café in Bromley\"",
             "}"
         );
         assertThat(tree, containsSubTree(
             node(DECLARATION,
                 node(token(IDENTIFIER, "Python", range(0, 0, 6))),
                 node(token(ASSIGN, "=", range(0, 7, 1))),
                 tree(
                     node(DECLARATION_LITERAL),
                     node(OBJECT_LITERAL,
                         node(token(KEYWORD_OBJECT, "object", range(0, 9, 6))),
                         node(OBJECT_HEADER,
                             node(OBJECT_ARGUMENTS,
                                 node(token(GROUP_OPEN, "(", range(0, 16, 1))),
                                 node(OBJECT_ARGUMENTS_LIST,
                                     node(OBJECT_ARGUMENT,
                                         node(token(IDENTIFIER, "x", range(0, 17, 1)))),
                                     node(OBJECT_ARGUMENTS_LIST,
                                         node(OBJECT_ARGUMENT,
                                             node(token(IDENTIFIER, "y", range(0, 19, 1)))))),
                                 node(token(GROUP_CLOSE, ")", range(0, 20, 1))))),
                         node(OBJECT_BODY,
                             node(token(BLOCK_OPEN, "{", range(0, 22, 1))),
                             node(OBJECT_MEMBERS,
                                 any(OBJECT_MEMBER),
                                 node(OBJECT_MEMBERS_SUFFIX,
                                     node(token(TERMINATOR, "\n", range(1, 17, 1))),
                                     node(OBJECT_MEMBERS_TAIL,
                                         any(OBJECT_MEMBER),
                                         node(OBJECT_MEMBERS_SUFFIX,
                                             node(token(TERMINATOR, "\n", range(2, 12, 1))),
                                             node(OBJECT_MEMBERS_TAIL,
                                                 any(OBJECT_MEMBER),
                                                 node(OBJECT_MEMBERS_SUFFIX,
                                                     node(token(TERMINATOR, "\n", range(3, 18, 1))),
                                                     node(OBJECT_MEMBERS_TAIL,
                                                         any(OBJECT_MEMBER),
                                                         node(OBJECT_MEMBERS_SUFFIX,
                                                             node(token(TERMINATOR, "\n", range(4, 45, 1))))))))))),
                             node(token(BLOCK_CLOSE, "}", range(5, 0, 1)))))))));
     }
 
     @Test
     public void shouldParseObjectWithoutArguments() throws SterlingException {
         SourceNode tree = parse(
             "Thingy = object {",
             "    isTrue = True",
             "}"
         );
         assertThat(tree, containsSubTree(
             node(OBJECT_LITERAL,
                 node(token(KEYWORD_OBJECT, "object", range(0, 9, 6))),
                 node(OBJECT_HEADER),
                 node(OBJECT_BODY,
                     node(token(BLOCK_OPEN, "{", range(0, 16, 1))),
                     anySubTree(),
                     node(token(BLOCK_CLOSE, "}", range(2, 0, 1)))))));
     }
 
     @Test
     public void shouldParseObjectWithSingleArgument() throws SterlingException {
         SourceNode tree = parse(
             "Thingy = object x {",
             "    value = x",
             "}"
         );
         assertThat(tree, containsSubTree(
             node(OBJECT_LITERAL,
                 node(token(KEYWORD_OBJECT, "object", range(0, 9, 6))),
                 node(OBJECT_HEADER,
                     node(SINGLE_OBJECT_ARGUMENT,
                         node(OBJECT_ARGUMENT,
                             node(token(IDENTIFIER, "x", range(0, 16, 1)))))),
                 node(OBJECT_BODY,
                     node(token(BLOCK_OPEN, "{", range(0, 18, 1))),
                     anySubTree(),
                     node(token(BLOCK_CLOSE, "}", range(2, 0, 1)))))));
     }
 
     @Test
     public void shouldParseObjectLiteralAsArgument() throws SterlingException {
         assertThat(parse("main = 2.times object { value = false }"), containsSubTree(tree(
             node(ARGUMENTS_EXPRESSION),
             node(ARGUMENT),
             node(PRIMARY_EXPRESSION),
             node(OBJECT_LITERAL,
                 node(token(KEYWORD_OBJECT, "object", range(0, 15, 6))),
                 node(OBJECT_HEADER),
                 node(OBJECT_BODY,
                     node(token(BLOCK_OPEN, "{", range(0, 22, 1))),
                     anySubTree(),
                     node(token(BLOCK_CLOSE, "}", range(0, 38, 1))))))));
     }
 
     @Test
     public void shouldParseObjectWithOperatorLambdas() throws SterlingException {
         SourceNode tree = parse(
             "Object = object o {",
             "    * = a -> o.data * a",
             "    toString = o.toString",
             "    [] = b -> o.data[b] o",
             "}"
         );
         assertThat(tree, containsSubTree(
             node(OBJECT_LITERAL,
                 node(token(KEYWORD_OBJECT, "object", range(0, 9, 6))),
                 node(OBJECT_HEADER,
                     node(SINGLE_OBJECT_ARGUMENT,
                         node(OBJECT_ARGUMENT,
                             node(token(IDENTIFIER, "o", range(0, 16, 1)))))),
                 node(OBJECT_BODY,
                     node(token(BLOCK_OPEN, "{", range(0, 18, 1))),
                     node(OBJECT_MEMBERS,
                         node(OBJECT_MEMBER,
                             node(OBJECT_MEMBER_NAME,
                                 node(token(MULTIPLY, "*", range(1, 4, 1)))),
                             node(token(ASSIGN, "=", range(1, 6, 1))),
                             anySubTree()),
                         node(OBJECT_MEMBERS_SUFFIX,
                             node(token(TERMINATOR, "\n", range(1, 23, 1))),
                             node(OBJECT_MEMBERS_TAIL,
                                 node(OBJECT_MEMBER,
                                     node(OBJECT_MEMBER_NAME,
                                         node(token(IDENTIFIER, "toString", range(2, 4, 8)))),
                                     node(token(ASSIGN, "=", range(2, 13, 1))),
                                     anySubTree()),
                                 node(OBJECT_MEMBERS_SUFFIX,
                                     node(token(TERMINATOR, "\n", range(2, 25, 1))),
                                     node(OBJECT_MEMBERS_TAIL,
                                         node(OBJECT_MEMBER,
                                             node(OBJECT_MEMBER_NAME,
                                                 node(token(INDEXER_OPERATOR, "[]", range(3, 4, 2)))),
                                             node(token(ASSIGN, "=", range(3, 7, 1))),
                                             anySubTree()),
                                         node(OBJECT_MEMBERS_SUFFIX,
                                             node(token(TERMINATOR, "\n", range(3, 25, 1))))))))),
                     node(token(BLOCK_CLOSE, "}", range(4, 0, 1)))))));
     }
 
     @Test
     public void shouldParseObjectWithoutMembers() throws SterlingException {
         assertThat(parse("NullObject = object { }"), containsSubTree(
             node(OBJECT_LITERAL,
                 node(token(KEYWORD_OBJECT, "object", range(0, 13, 6))),
                 node(OBJECT_HEADER),
                 node(OBJECT_BODY,
                     node(token(BLOCK_OPEN, "{", range(0, 20, 1))),
                     node(token(BLOCK_CLOSE, "}", range(0, 22, 1)))))));
     }
 
     @Test
     public void shouldParseImportHeaders() throws SterlingException {
         SourceNode tree = parse(
             "import io.write",
             "main = write \"test.txt\" \"Hello, World!\""
         );
         assertThat(tree, containsSubTree(
             node(IMPORT_HEADERS,
                 node(IMPORT_HEADER,
                     node(IMPORT_STATEMENT,
                         node(token(KEYWORD_IMPORT, "import", range(0, 0, 6))),
                         node(IMPORT_IDENTIFIER,
                             node(token(IDENTIFIER, "io", range(0, 7, 2))),
                             node(IMPORT_IDENTIFIER_TAIL,
                                 node(token(ACCESSOR, ".", range(0, 9, 1))),
                                 node(token(IDENTIFIER, "write", range(0, 10, 5)))))),
                     node(IMPORT_HEADER_SUFFIX,
                         node(token(TERMINATOR, "\n", range(0, 15, 1))))))));
     }
 
     @Test
     public void shouldParseMultipleImportHeader() throws SterlingException {
         SourceNode tree = parse(
             "import io.write",
             "import std.Error",
             "main = write \"log\" Error.last"
         );
         assertThat(tree, containsSubTree(
             node(IMPORT_HEADERS,
                 node(IMPORT_HEADER,
                     node(IMPORT_STATEMENT,
                         node(token(KEYWORD_IMPORT, "import", range(0, 0, 6))),
                         node(IMPORT_IDENTIFIER,
                             node(token(IDENTIFIER, "io", range(0, 7, 2))),
                             node(IMPORT_IDENTIFIER_TAIL,
                                 node(token(ACCESSOR, ".", range(0, 9, 1))),
                                 node(token(IDENTIFIER, "write", range(0, 10, 5)))))),
                     node(IMPORT_HEADER_SUFFIX,
                         node(token(TERMINATOR, "\n", range(0, 15, 1))),
                         node(IMPORT_HEADER_TAIL,
                             node(IMPORT_HEADER,
                                 node(IMPORT_STATEMENT,
                                     node(token(KEYWORD_IMPORT, "import", range(1, 0, 6))),
                                     node(IMPORT_IDENTIFIER,
                                         node(token(IDENTIFIER, "std", range(1, 7, 3))),
                                         node(IMPORT_IDENTIFIER_TAIL,
                                             node(token(ACCESSOR, ".", range(1, 10, 1))),
                                             node(token(IDENTIFIER, "Error", range(1, 11, 5)))))),
                                 node(IMPORT_HEADER_SUFFIX,
                                     node(token(TERMINATOR, "\n", range(1, 16, 1)))))))))));
     }
 
     @Test
     public void shouldParseImportHeaderWithoutModuleHeader() throws SterlingException {
         SourceNode tree = parse(
             "import io.write",
             "main = write \"test.txt\" \"Can I get spam instead?\""
         );
         assertThat(tree, containsSubTree(
             node(IMPORT_HEADERS,
                 node(IMPORT_HEADER,
                     node(IMPORT_STATEMENT,
                         node(token(KEYWORD_IMPORT, "import", range(0, 0, 6))),
                         node(IMPORT_IDENTIFIER,
                             node(token(IDENTIFIER, "io", range(0, 7, 2))),
                             node(IMPORT_IDENTIFIER_TAIL,
                                 node(token(ACCESSOR, ".", range(0, 9, 1))),
                                 node(token(IDENTIFIER, "write", range(0, 10, 5)))))),
                     node(IMPORT_HEADER_SUFFIX,
                         node(token(TERMINATOR, "\n", range(0, 15, 1))))))));
     }
 
     @Test
     public void shouldParseFromHeader() throws SterlingException {
         SourceNode tree = parse(
             "from sterling.io import write",
             "main = write \"test.txt\" \"Hello, World!\""
         );
         assertThat(tree, containsSubTree(
             node(IMPORT_HEADERS,
                 node(IMPORT_HEADER,
                     node(FROM_STATEMENT,
                         node(token(KEYWORD_FROM, "from", range(0, 0, 4))),
                         node(FROM_IDENTIFIER,
                             node(token(IDENTIFIER, "sterling", range(0, 5, 8))),
                             node(FROM_IDENTIFIER_TAIL,
                                 node(token(ACCESSOR, ".", range(0, 13, 1))),
                                 node(token(IDENTIFIER, "io", range(0, 14, 2))))),
                         node(token(KEYWORD_IMPORT, "import", range(0, 17, 6))),
                         node(FROM_IDENTIFIERS,
                             node(FROM_IDENTIFIER_LIST,
                                 node(token(IDENTIFIER, "write", range(0, 24, 5)))))),
                     node(IMPORT_HEADER_SUFFIX,
                         node(token(TERMINATOR, "\n", range(0, 29, 1))))))));
     }
 
     @Test
     public void shouldParseFromHeaderWithMultipleImports() throws SterlingException {
         SourceNode tree = parse(
             "from sterling.io import read, write",
             "main = write \"output.txt\" $ read \"input.txt\""
         );
         assertThat(tree, containsSubTree(
             node(IMPORT_HEADERS,
                 node(IMPORT_HEADER,
                     node(FROM_STATEMENT,
                         node(token(KEYWORD_FROM, "from", range(0, 0, 4))),
                         node(FROM_IDENTIFIER,
                             node(token(IDENTIFIER, "sterling", range(0, 5, 8))),
                             node(FROM_IDENTIFIER_TAIL,
                                 node(token(ACCESSOR, ".", range(0, 13, 1))),
                                 node(token(IDENTIFIER, "io", range(0, 14, 2))))),
                         node(token(KEYWORD_IMPORT, "import", range(0, 17, 6))),
                         node(FROM_IDENTIFIERS,
                             node(FROM_IDENTIFIER_LIST,
                                 node(token(IDENTIFIER, "read", range(0, 24, 4))),
                                 node(FROM_IDENTIFIER_LIST_TAIL,
                                     node(token(SEPARATOR, ",", range(0, 28, 1))),
                                     node(FROM_IDENTIFIER_LIST,
                                         node(token(IDENTIFIER, "write", range(0, 30, 5)))))))),
                     node(IMPORT_HEADER_SUFFIX,
                         node(token(TERMINATOR, "\n", range(0, 35, 1))))))));
     }
 
     @Test
     public void shouldParseFromHeaderWithParentheses() throws SterlingException {
         SourceNode tree = parse(
             "from sterling.io import (read, write,)",
             "main = write \"output.txt\" $ read \"input.txt\""
         );
         assertThat(tree, containsSubTree(
             node(IMPORT_HEADERS,
                 node(IMPORT_HEADER,
                     node(FROM_STATEMENT,
                         node(token(KEYWORD_FROM, "from", range(0, 0, 4))),
                         node(FROM_IDENTIFIER,
                             node(token(IDENTIFIER, "sterling", range(0, 5, 8))),
                             node(FROM_IDENTIFIER_TAIL,
                                 node(token(ACCESSOR, ".", range(0, 13, 1))),
                                 node(token(IDENTIFIER, "io", range(0, 14, 2))))),
                         node(token(KEYWORD_IMPORT, "import", range(0, 17, 6))),
                         node(FROM_IDENTIFIERS,
                             node(token(GROUP_OPEN, "(", range(0, 24, 1))),
                             node(FROM_IDENTIFIER_LIST,
                                 node(token(IDENTIFIER, "read", range(0, 25, 4))),
                                 node(FROM_IDENTIFIER_LIST_TAIL,
                                     node(token(SEPARATOR, ",", range(0, 29, 1))),
                                     node(FROM_IDENTIFIER_LIST,
                                         node(token(IDENTIFIER, "write", range(0, 31, 5))),
                                         node(FROM_IDENTIFIER_LIST_TAIL,
                                             node(token(SEPARATOR, ",", range(0, 36, 1))))))),
                             node(token(GROUP_CLOSE, ")", range(0, 37, 1))))),
                     node(IMPORT_HEADER_SUFFIX,
                         node(token(TERMINATOR, "\n", range(0, 38, 1))))))));
     }
 
     @Test
     public void shouldParseFromHeaderWithAliases() throws SterlingException {
         SourceNode tree = parse(
             "from sterling.io import read as in, write as out",
             "main = write \"output.txt\" $ read \"input.txt\""
         );
         assertThat(tree, containsSubTree(
             node(IMPORT_HEADERS,
                 node(IMPORT_HEADER,
                     node(FROM_STATEMENT,
                         node(token(KEYWORD_FROM, "from", range(0, 0, 4))),
                         node(FROM_IDENTIFIER,
                             node(token(IDENTIFIER, "sterling", range(0, 5, 8))),
                             node(FROM_IDENTIFIER_TAIL,
                                 node(token(ACCESSOR, ".", range(0, 13, 1))),
                                 node(token(IDENTIFIER, "io", range(0, 14, 2))))),
                         node(token(KEYWORD_IMPORT, "import", range(0, 17, 6))),
                         node(FROM_IDENTIFIERS,
                             node(FROM_IDENTIFIER_LIST,
                                 node(token(IDENTIFIER, "read", range(0, 24, 4))),
                                 node(IMPORT_IDENTIFIER_ALIAS,
                                     node(token(KEYWORD_AS, "as", range(0, 29, 2))),
                                     node(token(IDENTIFIER, "in", range(0, 32, 2)))),
                                 node(FROM_IDENTIFIER_LIST_TAIL,
                                     node(token(SEPARATOR, ",", range(0, 34, 1))),
                                     node(FROM_IDENTIFIER_LIST,
                                         node(token(IDENTIFIER, "write", range(0, 36, 5))),
                                         node(IMPORT_IDENTIFIER_ALIAS,
                                             node(token(KEYWORD_AS, "as", range(0, 42, 2))),
                                             node(token(IDENTIFIER, "out", range(0, 45, 3))))))))),
                     node(IMPORT_HEADER_SUFFIX,
                         node(token(TERMINATOR, "\n", range(0, 48, 1))))))));
     }
 
     @Test
     public void shouldParseJavaExpression() throws SterlingException {
        String className = "sterling.system.Say";
         assertThat(parse("say = java \"" + className + "\""), containsSubTree(
             node(DECLARATION,
                 node(token(IDENTIFIER, "say", range(0, 0, 3))),
                 node(token(ASSIGN, "=", range(0, 4, 1))),
                 subTree(
                     node(SELECTOR_EXPRESSION,
                         node(PRIMARY_EXPRESSION,
                             node(JAVA_EXPRESSION,
                                 node(token(KEYWORD_JAVA, "java", range(0, 6, 4))))),
                     node(SELECTOR_EXPRESSION_TAIL,
                         subTree(
                             node(STRING_LITERAL),
                             node(string(token(STRING, '"' + className + '"', range(0, 11, 37)), className)))))))
         ));
     }
 
     @Test
     public void shouldParseIsNothingExpression() throws SterlingException {
         assertThat(parse("test = 2 is Nothing"), containsSubTree(
             node(EQUALITY_EXPRESSION,
                 any(SHIFT_EXPRESSION),
                 node(EQUALITY_EXPRESSION_TAIL,
                     node(token(EQUALS, "is", range(0, 9, 2))),
                     subTree(
                         node(NULL_LITERAL),
                         node(token(NOTHING, "Nothing", range(0, 12, 7))))))
         ));
     }
 
     @Test
     public void shouldParseIsNotNothingExpression() throws SterlingException {
         assertThat(parse("test = 2 is not Nothing"), containsSubTree(
             node(EQUALITY_EXPRESSION,
                 any(SHIFT_EXPRESSION),
                 node(EQUALITY_EXPRESSION_TAIL,
                     node(token(NOT_EQUALS, "is not", range(0, 9, 6))),
                     subTree(
                         node(NULL_LITERAL),
                         node(token(NOTHING, "Nothing", range(0, 16, 7))))))
         ));
     }
 
     @Test
     public void shouldParseEmptyListLiteral() throws SterlingException {
         assertThat(parse("test = []"), containsSubTree(
             node(PRIMARY_EXPRESSION,
                 node(LIST_EXPRESSION,
                     node(token(LIST_OPEN, "[", range(0, 7, 1))),
                     node(token(LIST_CLOSE, "]", range(0, 8, 1)))))
         ));
     }
 
     @Test
     public void shouldParseListLiteralWithOneElement() throws SterlingException {
         assertThat(parse("test = [1]"), containsSubTree(
             node(PRIMARY_EXPRESSION,
                 node(LIST_EXPRESSION,
                     node(token(LIST_OPEN, "[", range(0, 7, 1))),
                     node(LIST_ELEMENTS,
                         node(LIST_ELEMENT,
                             subTree(
                                 node(PRIMARY_EXPRESSION),
                                 node(LITERAL_EXPRESSION),
                                 node(INTEGER_LITERAL),
                                 node(integer(token(INTEGER, "1", range(0, 8, 1)), 1))))),
                     node(token(LIST_CLOSE, "]", range(0, 9, 1)))))
         ));
     }
 
     @Test
     public void shouldParseListLiteralWithOneElementAndTrailingComma() throws SterlingException {
         assertThat(parse("test = [1,]"), containsSubTree(
             node(PRIMARY_EXPRESSION,
                 node(LIST_EXPRESSION,
                     node(token(LIST_OPEN, "[", range(0, 7, 1))),
                     node(LIST_ELEMENTS,
                         node(LIST_ELEMENT,
                             subTree(
                                 node(PRIMARY_EXPRESSION),
                                 node(LITERAL_EXPRESSION),
                                 node(INTEGER_LITERAL),
                                 node(integer(token(INTEGER, "1", range(0, 8, 1)), 1)))),
                         node(LIST_ELEMENTS_SUFFIX,
                             node(token(SEPARATOR, ",", range(0, 9, 1))))),
                     node(token(LIST_CLOSE, "]", range(0, 10, 1)))))
         ));
     }
 
     @Test
     public void shouldParseListLiteralWithTwoElements() throws SterlingException {
         assertThat(parse("test = [1, 2]"), containsSubTree(
             node(PRIMARY_EXPRESSION,
                 node(LIST_EXPRESSION,
                     node(token(LIST_OPEN, "[", range(0, 7, 1))),
                     node(LIST_ELEMENTS,
                         node(LIST_ELEMENT,
                             subTree(
                                 node(PRIMARY_EXPRESSION),
                                 node(LITERAL_EXPRESSION),
                                 node(INTEGER_LITERAL),
                                 node(integer(token(INTEGER, "1", range(0, 8, 1)), 1)))),
                         node(LIST_ELEMENTS_SUFFIX,
                             node(token(SEPARATOR, ",", range(0, 9, 1))),
                             node(LIST_ELEMENTS_TAIL,
                                 node(LIST_ELEMENT,
                                     subTree(
                                         node(PRIMARY_EXPRESSION),
                                         node(LITERAL_EXPRESSION),
                                         node(INTEGER_LITERAL),
                                         node(integer(token(INTEGER, "2", range(0, 11, 1)), 2))))))),
                     node(token(LIST_CLOSE, "]", range(0, 12, 1)))))
         ));
     }
 
     @Test
     public void shouldParseListLiteralWithTrailingComma() throws SterlingException {
         assertThat(parse("test = [1, 2,]"), containsSubTree(
             node(PRIMARY_EXPRESSION,
                 node(LIST_EXPRESSION,
                     node(token(LIST_OPEN, "[", range(0, 7, 1))),
                     node(LIST_ELEMENTS,
                         node(LIST_ELEMENT,
                             subTree(
                                 node(PRIMARY_EXPRESSION),
                                 node(LITERAL_EXPRESSION),
                                 node(INTEGER_LITERAL),
                                 node(integer(token(INTEGER, "1", range(0, 8, 1)), 1)))),
                         node(LIST_ELEMENTS_SUFFIX,
                             node(token(SEPARATOR, ",", range(0, 9, 1))),
                             node(LIST_ELEMENTS_TAIL,
                                 node(LIST_ELEMENT,
                                     subTree(
                                         node(PRIMARY_EXPRESSION),
                                         node(LITERAL_EXPRESSION),
                                         node(INTEGER_LITERAL),
                                         node(integer(token(INTEGER, "2", range(0, 11, 1)), 2)))),
                                 node(LIST_ELEMENTS_SUFFIX,
                                     node(token(SEPARATOR, ",", range(0, 12, 1))))))),
                     node(token(LIST_CLOSE, "]", range(0, 13, 1)))))
         ));
     }
 
     @Test
     public void shouldParseListOfLists() throws SterlingException {
         assertThat(parse("test = [[1, 2], [3, 4,]]"), containsSubTree(
             node(PRIMARY_EXPRESSION,
                 node(LIST_EXPRESSION,
                     node(token(LIST_OPEN, "[", range(0, 7, 1))),
                     node(LIST_ELEMENTS,
                         node(LIST_ELEMENT,
                             subTree(
                                 node(PRIMARY_EXPRESSION),
                                 any(LIST_EXPRESSION))),
                         node(LIST_ELEMENTS_SUFFIX,
                             node(token(SEPARATOR, ",", range(0, 14, 1))),
                             node(LIST_ELEMENTS_TAIL,
                                 node(LIST_ELEMENT,
                                     subTree(
                                         node(PRIMARY_EXPRESSION),
                                         any(LIST_EXPRESSION)))))),
                     node(token(LIST_CLOSE, "]", range(0, 23, 1)))))
         ));
     }
 
     @Test
     public void shouldParseListAsArgument() throws SterlingException {
         assertThat(parse("test = apples [1, 2]"), containsSubTree(
             node(SELECTOR_EXPRESSION,
                 node(PRIMARY_EXPRESSION,
                     node(QUALIFIED_IDENTIFIER,
                         node(token(IDENTIFIER, "apples", range(0, 7, 6))))),
                 node(SELECTOR_EXPRESSION_TAIL,
                     subTree(
                         any(LIST_EXPRESSION))))
         ));
     }
 
     private SourceNode parse(String... inputs) throws SterlingException {
         return parser.parse(scanner(asLines(inputs)));
     }
 }
