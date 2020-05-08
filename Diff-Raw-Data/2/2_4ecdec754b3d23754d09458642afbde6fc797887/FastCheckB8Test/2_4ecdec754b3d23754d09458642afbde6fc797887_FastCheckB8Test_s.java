 package fastboard.checkmove.fastcheck.bcolumn;
 
 import base.testcase.OthelloTestCase;
 import fastboard.checkmove.calc.FastCheckCalc;
 import fastboard.fastflip.FastBoardFlips;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 02:53:25 PM
 * Tests whether or not FastCheckA8 checks for valid moves properly
  */
 public class FastCheckB8Test extends OthelloTestCase {
     public void testIsMoveValidBlack() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForBlack();
         FastCheckB8 check = new FastCheckB8(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.b1_b8 = LineConverter.convertStringToLine("xoooooox");
         assertFalse(check.isValidMove(flips));
         flips.b1_b8 = LineConverter.convertStringToLine("xoooooo_");
         assertTrue(check.isValidMove(flips));
 
         flips.b1_b8 = LineConverter.convertStringToLine("_oooo_xo");
         flips.a8_h8 = LineConverter.convertStringToLine("x_xoooox");
         assertFalse(check.isValidMove(flips));
         flips.a8_h8 = LineConverter.convertStringToLine("__oox___");
         assertTrue(check.isValidMove(flips));
 
         flips.a8_h8 = LineConverter.convertStringToLine("_xooxoxo");
         flips.b8_h2 = LineConverter.convertStringToLine("__xoooox");
         assertFalse(check.isValidMove(flips));
         flips.b8_h2 = LineConverter.convertStringToLine("__ooooox");
         assertTrue(check.isValidMove(flips));
     }
 
     public void testIsMoveValidWhite() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForWhite();
         FastCheckB8 check = new FastCheckB8(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.b1_b8 = LineConverter.convertStringToLine("oxxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.b1_b8 = LineConverter.convertStringToLine("oxxxxxx_");
         assertTrue(check.isValidMove(flips));
 
         flips.b1_b8 = LineConverter.convertStringToLine("_xxxx_ox");
         flips.a8_h8 = LineConverter.convertStringToLine("o_oxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a8_h8 = LineConverter.convertStringToLine("__xxo___");
         assertTrue(check.isValidMove(flips));
 
         flips.a8_h8 = LineConverter.convertStringToLine("_oxxoxox");
         flips.b8_h2 = LineConverter.convertStringToLine("__oxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.b8_h2 = LineConverter.convertStringToLine("__xxxxxo");
         assertTrue(check.isValidMove(flips));
     }
 }
