 package fastboard;
 
 import base.testcase.OthelloTestCase;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 22, 2009
  * Time: 1:03:18 AM
  * This class tests whether or not FastBoardFlip does what it is supposed to do
  */
 public class FastBoardFlipsTest extends OthelloTestCase {
     public void testBlackPlaceA1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
 
         flips.blackPlaceA1();
 
         assertEquals("x_______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a1_h1));
     }
 
     public void testWhitePlaceA1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
 
         flips.whitePlaceA1();
 
         assertEquals("o_______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a1_h1));
     }
 
     public void testBlackPlaceA2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
 
         flips.blackPlaceA2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a2_h2));
     }
 
     public void testBlackFlipA2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_o______");
         flips.a2_g8 = LineConverter.convertStringToLine("o_______");
         flips.a2_h2 = LineConverter.convertStringToLine("o_______");
 
         flips.blackFlipA2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a2_h2));
     }
 
     public void testWhitePlaceA2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
 
         flips.whitePlaceA2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a2_h2));
     }
 
     public void testWhiteFlipA2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
 
         flips.a1_a8 = LineConverter.convertStringToLine("_x______");
         flips.a2_g8 = LineConverter.convertStringToLine("x_______");
         flips.a2_h2 = LineConverter.convertStringToLine("x_______");
 
         flips.whiteFlipA2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a2_h2));
     }
 
     public void testBlackPlaceA3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
 
         flips.blackPlaceA3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a3_h3));
     }
 
     public void testBlackFlipA3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
 
         flips.a1_a8 = LineConverter.convertStringToLine("__o_____");
         flips.a3_f8 = LineConverter.convertStringToLine("o_______");
         flips.a3_h3 = LineConverter.convertStringToLine("o_______");
 
         flips.blackFlipA3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a3_h3));
     }
 
     public void testWhitePlaceA3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
 
         flips.whitePlaceA3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a3_h3));
     }
 
     public void testWhiteFlipA3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
 
         flips.a1_a8 = LineConverter.convertStringToLine("__x_____");
         flips.a3_f8 = LineConverter.convertStringToLine("x_______");
         flips.a3_h3 = LineConverter.convertStringToLine("x_______");
 
         flips.whiteFlipA3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a3_h3));
     }
 
     public void testBlackPlaceA4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
 
        flips.blackPlaceA3();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a4_h4));
     }
 
     public void testBlackFlipA4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
 
         flips.a1_a8 = LineConverter.convertStringToLine("___o____");
         flips.a4_e8 = LineConverter.convertStringToLine("o_______");
         flips.a4_h4 = LineConverter.convertStringToLine("o_______");
 
        flips.blackFlipA3();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("x_______", LineConverter.convertLineToString(flips.a4_h4));
     }
 
     public void testWhitePlaceA4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
 
        flips.whitePlaceA3();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a4_h4));
     }
 
     public void testWhiteFlipA4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
 
         flips.a1_a8 = LineConverter.convertStringToLine("___x____");
         flips.a4_e8 = LineConverter.convertStringToLine("x_______");
         flips.a4_h4 = LineConverter.convertStringToLine("x_______");
 
        flips.whiteFlipA3();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.a1_a8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("o_______", LineConverter.convertLineToString(flips.a4_h4));
     }
 }
