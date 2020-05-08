 package org.sherman.kproblem.test;
 
 import org.sherman.kproblem.core.CellIndex;
 import org.sherman.kproblem.core.ConstantValue;
 import org.sherman.kproblem.core.SimpleCell;
 import org.sherman.kproblem.core.SimpleSheet;
 import org.testng.annotations.Test;
 
 public class SheetTest {
     @Test
     public void createSheet() {
         SimpleSheet sheet = new SimpleSheet(2, 2);
     }
 
     @Test
     public void checkRowBounds() {
         SimpleSheet sheet = new SimpleSheet(2, 2);
        sheet.putCell(new CellIndex(2, "A"), new SimpleCell<ConstantValue<Integer>, Integer>());
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void checkRowTopBound() {
         SimpleSheet sheet = new SimpleSheet(2, 2);
        sheet.putCell(new CellIndex(3, "A"), new SimpleCell<ConstantValue<Integer>, Integer>());
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void checkBottomTopBound() {
         SimpleSheet sheet = new SimpleSheet(2, 2);
        sheet.putCell(new CellIndex(0, "A"), new SimpleCell<ConstantValue<Integer>, Integer>());
     }
 
     @Test
     public void checkColumnBounds() {
         SimpleSheet sheet = new SimpleSheet(2, 2);
        sheet.putCell(new CellIndex(2, "A"), new SimpleCell<ConstantValue<Integer>, Integer>());
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void checkColumnBottomBound() {
         SimpleSheet sheet = new SimpleSheet(2, 2);
        sheet.putCell(new CellIndex(1, "0"), new SimpleCell<ConstantValue<Integer>, Integer>());
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void checkColumnTopBound() {
         SimpleSheet sheet = new SimpleSheet(2, 2);
        sheet.putCell(new CellIndex(1, "C"), new SimpleCell<ConstantValue<Integer>, Integer>());
     }
     
     @Test
     public void createSheetWithDiffCells() {
         SimpleSheet sheet = new SimpleSheet(2, 1);
         sheet.putCell(
             new CellIndex(1, "A"),
            new SimpleCell<ConstantValue<Integer>, Integer>("42")
         );
     }
 }
