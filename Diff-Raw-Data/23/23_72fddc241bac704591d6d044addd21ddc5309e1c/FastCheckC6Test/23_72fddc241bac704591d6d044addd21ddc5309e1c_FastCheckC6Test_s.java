 package fastboard.checkmove.fastcheck.ccolumn;
 
 import base.testcase.OthelloTestCase;
 import fastboard.checkmove.calc.FastCheckCalc;
 import fastboard.fastflip.FastBoardFlips;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 29, 2009
  * Time: 02:53:25 PM
 * Tests whether or not FastCheckB6 checks for valid moves properly
  */
 public class FastCheckC6Test extends OthelloTestCase {
     public void testIsMoveValidBlack() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForBlack();
         FastCheckC6 check = new FastCheckC6(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("xoooooox");
         assertFalse(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("xoooo_xx");
         assertTrue(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("xxxxx_ox");
         assertTrue(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("_oooo_xo");
         flips.a4_e8 = LineConverter.convertStringToLine("______oo");
         assertFalse(check.isValidMove(flips));
         flips.a4_e8 = LineConverter.convertStringToLine("______ox");
         assertTrue(check.isValidMove(flips));
         flips.a4_e8 = LineConverter.convertStringToLine("___xo___");
         assertTrue(check.isValidMove(flips));
 
         flips.a4_e8 = LineConverter.convertStringToLine("_____o_x");
         flips.a6_h6 = LineConverter.convertStringToLine("___xooox");
         assertFalse(check.isValidMove(flips));
         flips.a6_h6 = LineConverter.convertStringToLine("___oox__");
         assertTrue(check.isValidMove(flips));
         flips.a6_h6 = LineConverter.convertStringToLine("xo______");
         assertTrue(check.isValidMove(flips));
 
         flips.a6_h6 = LineConverter.convertStringToLine("__xoxoxo");
         flips.a8_h1 = LineConverter.convertStringToLine("__ooooox");
         assertFalse(check.isValidMove(flips));
         flips.a8_h1 = LineConverter.convertStringToLine("___oooox");
         assertTrue(check.isValidMove(flips));
     }
 
     public void testIsMoveValidWhite() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForWhite();
         FastCheckC6 check = new FastCheckC6(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("oxxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("oxxxx_oo");
         assertTrue(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("ooooo_xo");
         assertTrue(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("_xxxx_ox");
         flips.a4_e8 = LineConverter.convertStringToLine("______xx");
         assertFalse(check.isValidMove(flips));
         flips.a4_e8 = LineConverter.convertStringToLine("______xo");
         assertTrue(check.isValidMove(flips));
         flips.a4_e8 = LineConverter.convertStringToLine("___ox___");
         assertTrue(check.isValidMove(flips));
 
         flips.a4_e8 = LineConverter.convertStringToLine("_____x_o");
         flips.a6_h6 = LineConverter.convertStringToLine("___oxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a6_h6 = LineConverter.convertStringToLine("___xxo__");
         assertTrue(check.isValidMove(flips));
         flips.a6_h6 = LineConverter.convertStringToLine("ox______");
         assertTrue(check.isValidMove(flips));
 
         flips.a6_h6 = LineConverter.convertStringToLine("__oxoxox");
         flips.a8_h1 = LineConverter.convertStringToLine("__xxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a8_h1 = LineConverter.convertStringToLine("___xxxxo");
         assertTrue(check.isValidMove(flips));
     }
 }
