 package org.zwobble.shed.parser.parsing;
 
 import java.util.Collections;
 
 import org.junit.Test;
 import org.zwobble.shed.parser.parsing.nodes.ImmutableVariableNode;
 import org.zwobble.shed.parser.parsing.nodes.ImportNode;
 import org.zwobble.shed.parser.parsing.nodes.MutableVariableNode;
 import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
 import org.zwobble.shed.parser.parsing.nodes.PackageDeclarationNode;
 import org.zwobble.shed.parser.parsing.nodes.PublicDeclarationNode;
 import org.zwobble.shed.parser.parsing.nodes.SourceNode;
 import org.zwobble.shed.parser.parsing.nodes.StatementNode;
 import org.zwobble.shed.parser.parsing.nodes.TypeReferenceNode;
 import org.zwobble.shed.parser.tokeniser.Tokeniser;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.zwobble.shed.parser.Option.none;
 import static org.zwobble.shed.parser.parsing.Result.success;
 
 public class ParserTest {
     private final Tokeniser tokeniser = new Tokeniser();
     private final Parser parser = new Parser();
     
     @Test public void
     canParseEmptyString() {
         assertThat(parser.source().parse(tokens("")).anyErrors(),
                    is(true));
     }
     
     @Test public void
     packageDeclarationIsListOfIdentifiersJoinedByDots() {
         assertThat(parser.packageDeclaration().parse(tokens("package shed.util.collections;")),
                    is(success(new PackageDeclarationNode(asList("shed", "util", "collections")))));
     }
     
     @Test public void
     sourceNodeCanHaveNoImportNodes() {
         assertThat(parser.source().parse(tokens("package shed.util.collections;\n\npublic List;\nval x = 1;")),
             is(success(new SourceNode(
                 new PackageDeclarationNode(asList("shed", "util", "collections")),
                 Collections.<ImportNode>emptyList(),
                 new PublicDeclarationNode(asList("List")),
                 asList((StatementNode)new ImmutableVariableNode("x", none(TypeReferenceNode.class), new NumberLiteralNode("1")))
             )))
         );
     }
     
     @Test public void
     sourceNodeHasPackageDeclarationAndImportNodesAndPublicDeclarationAndStatements() {
         assertThat(parser.source().parse(tokens("package shed.util.collections;\n\nimport shed.util;\npublic List, Set;" +
                 "val x = 1; var y = 2;")),
             is(success(new SourceNode(
                 new PackageDeclarationNode(asList("shed", "util", "collections")),
                 asList(
                     new ImportNode(asList("shed", "util"))
                 ),
                 new PublicDeclarationNode(asList("List", "Set")),
                 asList(
                     new ImmutableVariableNode("x", none(TypeReferenceNode.class), new NumberLiteralNode("1")),
                     new MutableVariableNode("y", none(TypeReferenceNode.class), new NumberLiteralNode("2"))
                 )
             )))
         );
     }
     
     @Test public void
     errorsAreCombinedIfPackageDeclarationAndImportsHaveErrors() {
         assertThat(parser.source().parse(tokens("packag shed.util.collections; import shed import shed.collections;\nblah")).getErrors(),
             is(asList(
                 new CompilerError(1, 1, "Expected keyword \"package\" but got identifier \"packag\""),
                 new CompilerError(1, 43, "Expected symbol \";\" but got keyword \"import\""),
                 new CompilerError(2, 1, "Expected keyword \"public\" but got identifier \"blah\""),
                 new CompilerError(2, 5, "Expected statement but got end of source")
             ))
         );
     }
     
     @Test public void
     sourceNodeAttemptsToParseUpToEnd() {
         assertThat(parser.source().parse(tokens("package shed.util.collections; public x;\nval x = 1; a")).getErrors(),
             is(asList(
                 new CompilerError(2, 12, "Expected end of source but got identifier \"a\"")
             ))
         );
     }
     
     @Test public void
     errorsIfImportIsMissingSemicolon() {
         assertThat(parser.source().parse(tokens("package shed.util.collections; import shed import shed.collections;public List;val x = 1;")).getErrors(),
             is(asList(new CompilerError(1, 44, "Expected symbol \";\" but got keyword \"import\"")))
         );
     }
     
     @Test public void
     parserAttemptsToParseRestOfSourceFileIfErrorIsFound() {
         String source = "package shed.util.collections; import shed import shed.collections; import shed.stuff; val x; val y = 2;";
         assertThat(parser.source().parse(tokens(source)).get(),
             is(new SourceNode(
                 new PackageDeclarationNode(asList("shed", "util", "collections")),
                 asList(new ImportNode(asList("shed", "stuff"))),
                 null,
                 asList((StatementNode)new ImmutableVariableNode("y", none(TypeReferenceNode.class), new NumberLiteralNode("2")))
             ))
         );
     }
     
     @Test public void
     errorIsRaisedIfPackageDeclarationDoesNotStartWithPackageKeyword() {
         assertThat(parser.packageDeclaration().parse(tokens("packag shed.util.collections;")).getErrors(),
                    is(asList(new CompilerError(1, 1, "Expected keyword \"package\" but got identifier \"packag\""))));
     }
     
     @Test public void
     errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfDot() {
         assertThat(parser.packageDeclaration().parse(tokens("package shed .util.collections;")).getErrors(),
                    is(asList(new CompilerError(1, 14, "Expected symbol \";\" but got symbol \".\""))));
     }
     
     @Test public void
     errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfIdentifier() {
         assertThat(parser.packageDeclaration().parse(tokens("package shed. util.collections;")).getErrors(),
             is(asList(
                 new CompilerError(1, 14, "Expected identifier but got whitespace \" \"")
             ))
         );
     }
     
     @Test public void
     canImportPackages() {
         assertThat(parser.importNode().parse(tokens("import shed.util.collections;")),
                    is(success(new ImportNode(asList("shed", "util", "collections")))));
     }
     
     @Test public void
     canDeclarePublicVariables() {
         assertThat(
             parser.publicDeclaration().parse(tokens("public List, Set;")),
             is(success(new PublicDeclarationNode(asList("List", "Set"))))
         );
     }
     
     private TokenIterator tokens(String input) {
         return new TokenIterator(tokeniser.tokenise(input));
     }
 }
