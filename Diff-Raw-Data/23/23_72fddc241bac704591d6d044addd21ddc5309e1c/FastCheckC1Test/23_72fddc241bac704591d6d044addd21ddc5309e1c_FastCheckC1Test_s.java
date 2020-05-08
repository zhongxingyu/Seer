 package fastboard.checkmove.fastcheck.ccolumn;
 
 import base.testcase.OthelloTestCase;
 import fastboard.checkmove.calc.FastCheckCalc;
 import fastboard.fastflip.FastBoardFlips;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 22, 2009
  * Time: 12:59:03 AM
 * Tests whether or not FastCheckB1 checks for valid moves properly
  */
 public class FastCheckC1Test extends OthelloTestCase {
     public void testIsMoveValidBlack() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForBlack();
         FastCheckC1 check = new FastCheckC1(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("_ooooooo");
         assertFalse(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("_oooooox");
         assertTrue(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("_oooo_xo");
         flips.c1_h6 = LineConverter.convertStringToLine("__oo_o__");
         assertFalse(check.isValidMove(flips));
         flips.c1_h6 = LineConverter.convertStringToLine("___oxoo_");
         assertTrue(check.isValidMove(flips));
 
         flips.c1_h6 = LineConverter.convertStringToLine("___o_xxx");
         flips.a1_h1 = LineConverter.convertStringToLine("___xooox");
         assertFalse(check.isValidMove(flips));
         flips.a1_h1 = LineConverter.convertStringToLine("___ox___");
         assertTrue(check.isValidMove(flips));
 
         flips.a1_h1 = LineConverter.convertStringToLine("___xo___");
         flips.a3_c1 = LineConverter.convertStringToLine("_____ox_");
         assertFalse(check.isValidMove(flips));
         flips.a3_c1 = LineConverter.convertStringToLine("_____xo_");
         assertTrue(check.isValidMove(flips));
     }
 
     public void testIsMoveValidWhite() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForWhite();
         FastCheckC1 check = new FastCheckC1(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("_xxxxxxx");
         assertFalse(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("_xxxxxxo");
         assertTrue(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("_xxxx_ox");
         flips.c1_h6 = LineConverter.convertStringToLine("__xx_x__");
         assertFalse(check.isValidMove(flips));
         flips.c1_h6 = LineConverter.convertStringToLine("___xoxx_");
         assertTrue(check.isValidMove(flips));
 
         flips.c1_h6 = LineConverter.convertStringToLine("___x_ooo");
         flips.a1_h1 = LineConverter.convertStringToLine("___oxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a1_h1 = LineConverter.convertStringToLine("___xo___");
         assertTrue(check.isValidMove(flips));
 
         flips.a1_h1 = LineConverter.convertStringToLine("___ox___");
         flips.a3_c1 = LineConverter.convertStringToLine("_____xo_");
         assertFalse(check.isValidMove(flips));
         flips.a3_c1 = LineConverter.convertStringToLine("_____ox_");
         assertTrue(check.isValidMove(flips));
     }
 }
