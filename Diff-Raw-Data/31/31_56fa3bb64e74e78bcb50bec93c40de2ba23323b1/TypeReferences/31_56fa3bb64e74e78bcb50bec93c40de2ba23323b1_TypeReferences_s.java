 package org.zwobble.shed.parser.parsing;
 
 import java.util.List;
 
 import org.zwobble.shed.parser.parsing.nodes.TypeApplicationNode;
 import org.zwobble.shed.parser.parsing.nodes.TypeIdentifierNode;
 import org.zwobble.shed.parser.parsing.nodes.TypeReferenceNode;
 
 import static org.zwobble.shed.parser.parsing.Result.success;
 import static org.zwobble.shed.parser.parsing.Rules.firstOf;
 import static org.zwobble.shed.parser.parsing.Rules.oneOrMoreWithSeparator;
 import static org.zwobble.shed.parser.parsing.Rules.sequence;
 import static org.zwobble.shed.parser.parsing.Rules.symbol;
 import static org.zwobble.shed.parser.parsing.Rules.then;
 import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;
 import static org.zwobble.shed.parser.parsing.Separator.hardSeparator;
 import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;
 
 public class TypeReferences {
     @SuppressWarnings("unchecked")
     public static Rule<TypeReferenceNode> typeReference() {
         return new Rule<TypeReferenceNode>() {
             @Override
             public Result<TypeReferenceNode> parse(TokenIterator tokens) {
                 return firstOf("type reference",
                     typeApplication(),
                     typeIdentifier()
                 ).parse(tokens);
             }
         };
     }
     
     public static Rule<TypeIdentifierNode> typeIdentifier() {
         return then(
             tokenOfType(IDENTIFIER),
             new ParseAction<String, TypeIdentifierNode>() {
                 @Override
                 public Result<TypeIdentifierNode> apply(String result) {
                     return success(new TypeIdentifierNode(result));
                 }
             }
         );
     }
     
     public static Rule<TypeApplicationNode> typeApplication() {
        final Rule<TypeIdentifierNode> baseIdentifier = typeIdentifier();
         final Rule<List<TypeReferenceNode>> typeParameters = oneOrMoreWithSeparator(typeReference(), hardSeparator(symbol(",")));
         return then(
             sequence(OnError.FINISH,
                baseIdentifier,
                symbol("["),
                 typeParameters,
                 symbol("]")
             ),
             new ParseAction<RuleValues, TypeApplicationNode>() {
                 @Override
                 public Result<TypeApplicationNode> apply(RuleValues result) {
                     return success(new TypeApplicationNode(result.get(baseIdentifier), result.get(typeParameters)));
                 }
             }
         );
     }
 }
