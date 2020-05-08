 package org.zwobble.shed.compiler.parsing;
 
 import java.util.List;
 
 import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
 
 import static org.zwobble.shed.compiler.parsing.Rules.guard;
 import static org.zwobble.shed.compiler.parsing.Rules.sequence;
 import static org.zwobble.shed.compiler.parsing.Rules.symbol;
 import static org.zwobble.shed.compiler.parsing.Rules.then;
 import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
 import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMoreWithSeparator;
 import static org.zwobble.shed.compiler.parsing.Separator.hardSeparator;
 import static org.zwobble.shed.compiler.parsing.TypeReferences.typeSpecifier;
 import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;
 
 public class Arguments {
     public static Rule<List<FormalArgumentNode>> formalArgumentList() {
         final Rule<List<FormalArgumentNode>> formalArguments = zeroOrMoreWithSeparator(formalArgument(), hardSeparator(guard(symbol(","))));
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
        final Rule<ExpressionNode> type = guard(typeSpecifier());
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
 }
