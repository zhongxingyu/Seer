 package org.sherman.kproblem.util;
 
 import java.util.Iterator;
 
 import org.sherman.kproblem.core.Cell;
 import org.sherman.kproblem.core.CellIndex;
 import org.sherman.kproblem.core.EvaluationStrategy;
 import org.sherman.kproblem.core.Sheet;
 import org.sherman.kproblem.core.SimpleCell;
 import org.sherman.kproblem.core.SimpleSheet;
import org.sherman.kproblem.parser.Parser;
 
 public class Sheets {
     private Sheets() {}
     
     public static Sheet buildFrom(String[][] rawCells) {
         Sheet sheet = new SimpleSheet(rawCells.length, rawCells[0].length);
         
         for (int i = 0; i < rawCells.length; i++) {
             Iterator<Character> columnIndex =
                 Cells.getNextColumnIndexIterator();
             
             for (int j = 0; j < rawCells[i].length; j++) {
                 // FIXME: move this check to Validator?
                 if (!columnIndex.hasNext())
                     throw new IllegalStateException("Too much columns");
                 
                 CellIndex index =
                     new CellIndex(i + 1, columnIndex.next().toString());
                 
                 EvaluationStrategy<?> eval = Parser.parse(rawCells[i][j].trim());
                 Cell cell = new SimpleCell(eval);
                 sheet.putCell(index, cell);
             }
         }
         
         return sheet;
     }
 }
