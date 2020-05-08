 package org.sherman.kproblem.core;
 
 import org.sherman.kproblem.parser.*;
 
 public class SimpleCell implements Cell {
     private final Expression expression;
     private final Sheet sheet;
     private final CellIndex index;
     private final SheetContext sheetContext;
     
     public SimpleCell(
         Sheet sheet,
         CellIndex index,
         Expression expression
     ) {
         this.sheet = sheet;
         this.index = index;
         this.expression = expression;
         this.sheetContext = new SheetContext(sheet, index);
     }
 
     @Override
    public <T> Value<T> getValue() {
        return expression.eval(sheetContext);
     }
 
     @Override
     public Expression getExpression() {
         return expression;
     }
 }
