 package org.zwobble.shed.compiler.typechecker;
 
 import java.util.Collections;
 
 import org.junit.Test;
 import org.zwobble.shed.compiler.parsing.CompilerError;
 import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
 import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
 import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
 import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
 import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
 import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
 import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
 import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
 import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
 import org.zwobble.shed.compiler.types.CoreTypes;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.zwobble.shed.compiler.Option.none;
 import static org.zwobble.shed.compiler.Option.some;
 import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
 import static org.zwobble.shed.compiler.parsing.SourceRange.range;
 
 public class TypeCheckerTest {
     private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
     private final TypeLookup typeLookup = new TypeLookup(nodeLocations);
     private final TypeInferer typeInferer = new TypeInferer(nodeLocations, typeLookup);
     private final TypeChecker typeChecker = new TypeChecker(nodeLocations, typeLookup, typeInferer);
     
     private final StaticContext staticContext = new StaticContext();
     
     @Test public void
     noErrorsIfEverythingTypeChecks() {
         SourceNode source = new SourceNode(
             new PackageDeclarationNode(asList("shed", "example")),
             Collections.<ImportNode>emptyList(),
             new PublicDeclarationNode(asList("x")),
             asList((StatementNode)new ImmutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true)))
         );
         assertThat(typeChecker.typeCheck(source, staticContext).isSuccess(), is(true));
     }
     
     @Test public void
     errorsIfAttemptingToInitialiseAVariableWithExpressionOfWrongType() {
         StatementNode variableNode = new ImmutableVariableNode(
             "x",
             some((TypeReferenceNode)new TypeIdentifierNode("String")),
             new BooleanLiteralNode(true)
         );
         nodeLocations.put(variableNode, range(position(4, 12), position(6, 6)));
         
         SourceNode source = new SourceNode(
             new PackageDeclarationNode(asList("shed", "example")),
             Collections.<ImportNode>emptyList(),
             new PublicDeclarationNode(asList("x")),
             asList(variableNode)
         );
         staticContext.add("String", CoreTypes.classOf(CoreTypes.STRING));
         assertThat(
             typeChecker.typeCheck(source, staticContext),
             is(TypeResult.<Void>failure(asList(new CompilerError(
                 range(position(4, 12), position(6, 6)),
                "Cannot initialise variable of type \"String\" with expression of type \"Boolean\"))"
             ))))
         );
     }
 }
