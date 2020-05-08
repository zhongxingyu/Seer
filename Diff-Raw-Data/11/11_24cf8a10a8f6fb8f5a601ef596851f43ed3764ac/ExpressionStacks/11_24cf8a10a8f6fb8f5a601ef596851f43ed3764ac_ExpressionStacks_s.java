 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.jamgotchian.abcd.core.analysis;
 
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import fr.jamgotchian.abcd.core.ast.expr.Expression;
 import fr.jamgotchian.abcd.core.ast.expr.ChoiceExpression;
 import fr.jamgotchian.abcd.core.ast.util.ExpressionEqualityChecker;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 class ExpressionStacks {
 
     private ExpressionStacks() {
     }
 
     static ExpressionStack merge(List<ExpressionStack> stacks) {
         if (stacks.size() <= 1) {
             throw new ABCDException("stacks.size() <= 1");
         }
        for (int i = 0; i < stacks.size()-1; i++) {
            if (stacks.get(i).size() != stacks.get(i+1).size()) {
                throw new ABCDException("Cannot merge stacks with differents sizes");
             }
         }
 
         ExpressionStack stackMerge = new ExpressionStackImpl();
 
         List<List<Expression>> toList = new ArrayList<List<Expression>>();
         for (int i = 0; i < stacks.size(); i++) {
             toList.add(stacks.get(i).toList());
         }
         for (int i = 0; i < stacks.get(0).size(); i++) {
             ChoiceExpression choiceExpr = new ChoiceExpression();
             for (int j = 0; j < stacks.size(); j++) {
                 choiceExpr.getChoices().add(toList.get(j).get(i));
             }
             stackMerge.push(choiceExpr);
         }
 
         return stackMerge;
     }
 
     static boolean equals(ExpressionStack stack1, ExpressionStack stack2) {
         return equals(Arrays.asList(stack1, stack2));
     }
 
     static boolean equals(List<ExpressionStack> stacks) {
         if (stacks.size() <= 1) {
             throw new ABCDException("stacks <= 1");
         }
 
         ExpressionStack stack0 = stacks.get(0);
         List<Expression> exprs0 = stack0.toList();
         for (int i = 1; i < stacks.size(); i++) {
             ExpressionStack stack = stacks.get(i);
             if (stack.size() != stack0.size()) {
                 return false;
             }
             List<Expression> exprs = stack.toList();
             for (int j = 0; j < exprs.size(); j++) {
                 Expression expr0 = exprs0.get(j);
                 Expression expr = exprs.get(j);
                 if (!ExpressionEqualityChecker.equal(expr0, expr)) {
                     return false;
                 }
             }
         }
 
         return true;
     }
 }
