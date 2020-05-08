 package fastboard.checkmove.fastcheck.ecolumn;
 
 import base.testcase.OthelloTestCase;
 import fastboard.checkmove.calc.FastCheckCalc;
 import fastboard.checkmove.fastcheck.dcolumn.FastCheckD2;
 import fastboard.fastflip.FastBoardFlips;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 22, 2009
  * Time: 12:59:03 AM
  * Tests whether or not FastCheckE2 checks for valid moves properly
  */
 public class FastCheckE2Test extends OthelloTestCase {
     public void testIsMoveValidBlack() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForBlack();
        FastCheckD2 check = new FastCheckD2(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.e1_e8 = LineConverter.convertStringToLine("__oooooo");
         assertFalse(check.isValidMove(flips));
         flips.e1_e8 = LineConverter.convertStringToLine("__ooooox");
         assertTrue(check.isValidMove(flips));
 
         flips.e1_e8 = LineConverter.convertStringToLine("_oooo_xo");
         flips.d1_h5 = LineConverter.convertStringToLine("_____o_o");
         assertFalse(check.isValidMove(flips));
         flips.d1_h5 = LineConverter.convertStringToLine("_____oxo");
         assertTrue(check.isValidMove(flips));
 
         flips.d1_h5 = LineConverter.convertStringToLine("_____xox");
         flips.a2_h2 = LineConverter.convertStringToLine("_____xox");
         assertFalse(check.isValidMove(flips));
         flips.a2_h2 = LineConverter.convertStringToLine("_____ox_");
         assertTrue(check.isValidMove(flips));
         flips.a2_h2 = LineConverter.convertStringToLine("__xo_oo_");
         assertTrue(check.isValidMove(flips));
 
         flips.a2_h2 = LineConverter.convertStringToLine("_____xo_");
         flips.a6_f1 = LineConverter.convertStringToLine("___xox__");
         assertFalse(check.isValidMove(flips));
         flips.a6_f1 = LineConverter.convertStringToLine("___xxo__");
         assertTrue(check.isValidMove(flips));
     }
 }
