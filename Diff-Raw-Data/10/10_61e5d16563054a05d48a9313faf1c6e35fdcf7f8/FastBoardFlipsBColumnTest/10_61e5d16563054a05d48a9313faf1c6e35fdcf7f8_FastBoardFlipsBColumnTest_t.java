 package fastboard.fastflip;
 
 import base.testcase.OthelloTestCase;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Nov 22, 2009
  * Time: 1:03:18 AM
  * This class tests whether or not FastBoardFlip does what it is supposed to do for column B
  */
 public class FastBoardFlipsBColumnTest extends OthelloTestCase {
     public void testBlackPlaceB1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
 
         flips.blackPlaceB1();
 
         assertEquals("x_______", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a1_h1));
     }
 
     public void testBlackFlipB1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("o_______");
         flips.b1_h7 = LineConverter.convertStringToLine("_o______");
         flips.a1_h1 = LineConverter.convertStringToLine("_o______");
 
         flips.blackFlipB1();
 
         assertEquals("x_______", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a1_h1));
     }
 
     public void testWhitePlaceB1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
 
         flips.whitePlaceB1();
 
         assertEquals("o_______", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a1_h1));
     }
 
     public void testWhiteFlipB1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("x_______");
         flips.b1_h7 = LineConverter.convertStringToLine("_x______");
         flips.a1_h1 = LineConverter.convertStringToLine("_x______");
 
         flips.whiteFlipB1();
 
         assertEquals("o_______", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a1_h1));
     }
 
     public void testBlackPlaceB2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_c1));
 
         flips.blackPlaceB2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("______x_", LineConverter.convertLineToString(flips.a3_c1));
     }
 
     public void testBlackFlipB2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_c1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("_o______");
         flips.a1_h8 = LineConverter.convertStringToLine("_o______");
         flips.a2_h2 = LineConverter.convertStringToLine("_o______");
         flips.a3_c1 = LineConverter.convertStringToLine("______o_");
 
         flips.blackFlipB2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("______x_", LineConverter.convertLineToString(flips.a3_c1));
     }
 
     public void testWhitePlaceB2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_c1));
 
         flips.whitePlaceB2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("______o_", LineConverter.convertLineToString(flips.a3_c1));
     }
 
     public void testWhiteFlipB2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_c1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("_x______");
         flips.a1_h8 = LineConverter.convertStringToLine("_x______");
         flips.a2_h2 = LineConverter.convertStringToLine("_x______");
         flips.a3_c1 = LineConverter.convertStringToLine("______x_");
 
         flips.whiteFlipB2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("______o_", LineConverter.convertLineToString(flips.a3_c1));
     }
 
     public void testBlackPlaceB3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_d1));
 
         flips.blackPlaceB3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a4_d1));
     }
 
     public void testBlackFlipB3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_d1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("__o_____");
         flips.a2_g8 = LineConverter.convertStringToLine("__o_____");
         flips.a3_h3 = LineConverter.convertStringToLine("_o______");
         flips.a4_d1 = LineConverter.convertStringToLine("_____o__");
 
         flips.blackFlipB3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a4_d1));
     }
 
     public void testWhitePlaceB3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_d1));
 
         flips.whitePlaceB3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a4_d1));
     }
 
     public void testWhiteFlipB3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_d1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("__x_____");
         flips.a2_g8 = LineConverter.convertStringToLine("__x_____");
         flips.a3_h3 = LineConverter.convertStringToLine("_x______");
         flips.a4_d1 = LineConverter.convertStringToLine("_____x_x");
 
         flips.whiteFlipB3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____o_x", LineConverter.convertLineToString(flips.a4_d1));
     }
 
     public void testBlackPlaceB4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_e1));
 
         flips.blackPlaceB4();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("___x____", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("____x___", LineConverter.convertLineToString(flips.a5_e1));
     }
 
     public void testBlackFlipB4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_e1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("___o____");
         flips.a3_f8 = LineConverter.convertStringToLine("___o____");
         flips.a4_h4 = LineConverter.convertStringToLine("_o______");
         flips.a5_e1 = LineConverter.convertStringToLine("____o___");
 
         flips.blackFlipB4();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("___x____", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("____x___", LineConverter.convertLineToString(flips.a5_e1));
     }
 
     public void testWhitePlaceB4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_e1));
 
         flips.whitePlaceB4();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("___o____", LineConverter.convertLineToString(flips.a3_f8));
        assertEquals("_o______", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("____o___", LineConverter.convertLineToString(flips.a5_e1));
     }
 
     public void testWhiteFlipB4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_e1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("___x____");
         flips.a3_f8 = LineConverter.convertStringToLine("___x____");
        flips.a4_h4 = LineConverter.convertStringToLine("_x______");
         flips.a5_e1 = LineConverter.convertStringToLine("____x___");
 
         flips.whiteFlipB4();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("___o____", LineConverter.convertLineToString(flips.a3_f8));
        assertEquals("_o______", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("____o___", LineConverter.convertLineToString(flips.a5_e1));
     }
     
 }
