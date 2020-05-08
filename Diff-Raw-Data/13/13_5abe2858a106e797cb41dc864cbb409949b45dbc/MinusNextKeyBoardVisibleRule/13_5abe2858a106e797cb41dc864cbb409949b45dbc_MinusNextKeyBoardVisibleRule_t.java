 package pl.wtopolski.android.polishnotation.support.model.rules;
 
 import android.text.TextUtils;
 
 import static pl.wtopolski.android.polishnotation.support.view.KeyBoard.*;
 
 public class MinusNextKeyBoardVisibleRule extends KeyBoardVisibleRule {
     @Override
     public boolean pass(int position, String content) {
         String prev = getPrevChar(position, content);
         String next = getNextChar(position, content);
         String secondNext = getNextChar(position, content, 1);
 
        if (valueIsOperation(next)) {
            if (prev == null) {
                // ex. |-5...
                return false;
            } else if (valueIsOperation(secondNext)) {
                // ex. ...3|--5...
                return false;
            } else if (valueIsBracket(secondNext)) {
                // ex. ...6|-(4...
                return false;
            }
         }
 
         return true;
     }
 }
