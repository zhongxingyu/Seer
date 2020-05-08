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
 * Tests whether or not FastCheckB4 checks for valid moves properly
  */
 public class FastCheckC4Test extends OthelloTestCase {
     public void testIsMoveValidBlack() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForBlack();
         FastCheckC4 check = new FastCheckC4(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("xoooooox");
         assertFalse(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("xoo__xxx");
         assertTrue(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("xxx_ooox");
         assertTrue(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("_oooo_xo");
         flips.a2_g8 = LineConverter.convertStringToLine("__oo__o_");
         assertFalse(check.isValidMove(flips));
         flips.a2_g8 = LineConverter.convertStringToLine("____oxo_");
         assertTrue(check.isValidMove(flips));
         flips.a2_g8 = LineConverter.convertStringToLine("_xo_xxox");
         assertTrue(check.isValidMove(flips));
 
         flips.a2_g8 = LineConverter.convertStringToLine("___oo_xx");
         flips.a4_h4 = LineConverter.convertStringToLine("___xooox");
         assertFalse(check.isValidMove(flips));
         flips.a4_h4 = LineConverter.convertStringToLine("___oox__");
         assertTrue(check.isValidMove(flips));
         flips.a4_h4 = LineConverter.convertStringToLine("xo_o_xxx");
         assertTrue(check.isValidMove(flips));
 
         flips.a4_h4 = LineConverter.convertStringToLine("__xoxoxo");
         flips.a6_f1 = LineConverter.convertStringToLine("____ooox");
         assertFalse(check.isValidMove(flips));
         flips.a6_f1 = LineConverter.convertStringToLine("_____oox");
         assertTrue(check.isValidMove(flips));
         flips.a6_f1 = LineConverter.convertStringToLine("__xo____");
         assertTrue(check.isValidMove(flips));
     }
 
     public void testIsMoveValidWhite() {
         FastCheckCalc calc = new FastCheckCalc();
         boolean[][] fastCheckCalcArray = calc.calcIsMoveValidForWhite();
         FastCheckC4 check = new FastCheckC4(fastCheckCalcArray);
 
         FastBoardFlips flips = new FastBoardFlips();
 
         assertFalse(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("oxxxxxxo");
         assertFalse(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("oxx__ooo");
         assertTrue(check.isValidMove(flips));
         flips.c1_c8 = LineConverter.convertStringToLine("ooo_xxxo");
         assertTrue(check.isValidMove(flips));
 
         flips.c1_c8 = LineConverter.convertStringToLine("_xxxx_ox");
         flips.a2_g8 = LineConverter.convertStringToLine("__xx__x_");
         assertFalse(check.isValidMove(flips));
         flips.a2_g8 = LineConverter.convertStringToLine("____xox_");
         assertTrue(check.isValidMove(flips));
         flips.a2_g8 = LineConverter.convertStringToLine("_ox_ooxo");
         assertTrue(check.isValidMove(flips));
 
         flips.a2_g8 = LineConverter.convertStringToLine("___xx_oo");
         flips.a4_h4 = LineConverter.convertStringToLine("___oxxxo");
         assertFalse(check.isValidMove(flips));
         flips.a4_h4 = LineConverter.convertStringToLine("___xxo__");
         assertTrue(check.isValidMove(flips));
         flips.a4_h4 = LineConverter.convertStringToLine("ox_x_ooo");
         assertTrue(check.isValidMove(flips));
 
         flips.a4_h4 = LineConverter.convertStringToLine("__oxoxox");
         flips.a6_f1 = LineConverter.convertStringToLine("____xxxo");
         assertFalse(check.isValidMove(flips));
         flips.a6_f1 = LineConverter.convertStringToLine("_____xxo");
         assertTrue(check.isValidMove(flips));
         flips.a6_f1 = LineConverter.convertStringToLine("__ox____");
         assertTrue(check.isValidMove(flips));
     }
 }
