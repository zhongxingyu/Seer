 package fastboard.lineflipper.calc;
 
 import base.testcase.OthelloTestCase;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ed
  * Date: Jan 10, 2010
  * Time: 1:59:41 PM
  * This test that we are computing the LineFlipperNumFlip arrays properly
  */
 public class LineFlipperNumFlipCalcTest extends OthelloTestCase {
     public void testNumFlipForBlackForThisLine() {
         LineFlipperNumFlipCalc calc = new LineFlipperNumFlipCalc();
 
         int line;
         int index;
         NumFlip numFlip;
 
         line = LineConverter.convertStringToLine("________");
         index = 0;
         numFlip = calc.numFlipForBlackForThisLine(line, index);
         assertEquals(0,numFlip.upNum);
         assertEquals(0,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("__xo_xo_");
         index = 0;
         numFlip = calc.numFlipForBlackForThisLine(line, index);
         assertEquals(1,numFlip.upNum);
         assertEquals(0,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("_o_xoo__");
         index = 1;
         numFlip = calc.numFlipForBlackForThisLine(line, index);
         assertEquals(2,numFlip.upNum);
         assertEquals(0,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("___oox_x");
         index = 5;
         numFlip = calc.numFlipForBlackForThisLine(line, index);
         assertEquals(0,numFlip.upNum);
         assertEquals(2,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("_oooooox");
         index = 7;
         numFlip = calc.numFlipForBlackForThisLine(line, index);
         assertEquals(0,numFlip.upNum);
         assertEquals(6,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("xoooooo_");
         index = 0;
         numFlip = calc.numFlipForBlackForThisLine(line, index);
         assertEquals(6,numFlip.upNum);
         assertEquals(0,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("xoo_ooox");
         index = 4;
         numFlip = calc.numFlipForBlackForThisLine(line, index);
         assertEquals(2,numFlip.upNum);
         assertEquals(3,numFlip.downNum);
     }
 
    public void testCalcNumFlipForBlack() {
         LineFlipperNumFlipCalc calc = new LineFlipperNumFlipCalc();
 
         NumFlip[][] result = calc.calcNumFlipForBlack();
 
         int line = LineConverter.convertStringToLine("xoo__oox");
         assertEquals(new NumFlip(0,0),result[0][line]);
         assertEquals(new NumFlip(0,0),result[1][line]);
         assertEquals(new NumFlip(0,0),result[2][line]);
         assertEquals(new NumFlip(0,2),result[3][line]);
         assertEquals(new NumFlip(2,0),result[4][line]);
         assertEquals(new NumFlip(0,0),result[5][line]);
         assertEquals(new NumFlip(0,0),result[6][line]);
         assertEquals(new NumFlip(0,0),result[7][line]);
     }
 
     public void testNumFlipForWhiteForThisLine() {
         LineFlipperNumFlipCalc calc = new LineFlipperNumFlipCalc();
 
         int line;
         int index;
         NumFlip numFlip;
 
         line = LineConverter.convertStringToLine("________");
         index = 0;
         numFlip = calc.numFlipForWhiteForThisLine(line, index);
         assertEquals(0,numFlip.upNum);
         assertEquals(0,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("__ox_ox_");
         index = 0;
         numFlip = calc.numFlipForWhiteForThisLine(line, index);
         assertEquals(1,numFlip.upNum);
         assertEquals(0,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("_x_oxx__");
         index = 1;
         numFlip = calc.numFlipForWhiteForThisLine(line, index);
         assertEquals(2,numFlip.upNum);
         assertEquals(0,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("___xxo_o");
         index = 5;
         numFlip = calc.numFlipForWhiteForThisLine(line, index);
         assertEquals(0,numFlip.upNum);
         assertEquals(2,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("_xxxxxxo");
         index = 7;
         numFlip = calc.numFlipForWhiteForThisLine(line, index);
         assertEquals(0,numFlip.upNum);
         assertEquals(6,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("oxxxxxx_");
         index = 0;
         numFlip = calc.numFlipForWhiteForThisLine(line, index);
         assertEquals(6,numFlip.upNum);
         assertEquals(0,numFlip.downNum);
 
         line = LineConverter.convertStringToLine("oxx_xxxo");
         index = 4;
         numFlip = calc.numFlipForWhiteForThisLine(line, index);
         assertEquals(2,numFlip.upNum);
         assertEquals(3,numFlip.downNum);
     }
 
     public void testCalcIsMoveValidForWhite() {
         LineFlipperNumFlipCalc calc = new LineFlipperNumFlipCalc();
 
         NumFlip[][] result = calc.calcNumFlipForWhite();
 
         int line = LineConverter.convertStringToLine("oxx__xxo");
         assertEquals(new NumFlip(0,0),result[0][line]);
         assertEquals(new NumFlip(0,0),result[1][line]);
         assertEquals(new NumFlip(0,0),result[2][line]);
         assertEquals(new NumFlip(0,2),result[3][line]);
         assertEquals(new NumFlip(2,0),result[4][line]);
         assertEquals(new NumFlip(0,0),result[5][line]);
         assertEquals(new NumFlip(0,0),result[6][line]);
         assertEquals(new NumFlip(0,0),result[7][line]);
     }
 }
