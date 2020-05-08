 package com.reubenpeeris.wippen.examples;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import com.reubenpeeris.wippen.expression.Add;
 import com.reubenpeeris.wippen.expression.Card;
 import com.reubenpeeris.wippen.expression.Divide;
import com.reubenpeeris.wippen.expression.Equals;
 import com.reubenpeeris.wippen.expression.Expression;
 import com.reubenpeeris.wippen.expression.Move;
 import com.reubenpeeris.wippen.expression.Multiply;
 import com.reubenpeeris.wippen.expression.NodeBuilder;
 import com.reubenpeeris.wippen.expression.Pile;
 import com.reubenpeeris.wippen.expression.Subtract;
 
 import static com.reubenpeeris.wippen.expression.Move.Type.*;
 
 public class PairCapturer extends SingleValueMatcher {
     @Override
     public Move takeTurn(Collection<Pile> table, Collection<Card> hand) {
         for (Pile pile1 : table) {
             for (Pile pile2 : table) {
                for (NodeBuilder nodeBuilder : Arrays.asList(Add.builder(), Subtract.builder(), Multiply.builder(), Divide.builder(),
                        Equals.builder())) {
                    Expression expression = nodeBuilder.left(pile1).right(pile2).build();
                    for (Card card : hand) {
                        Move move = Move.create(CAPTURE, card, expression, table, hand, getMe());
                        if (move != null) {
                            return move;
                         }
                     }
                 }
             }
         }
 
         return super.takeTurn(table, hand);
     }
 }
