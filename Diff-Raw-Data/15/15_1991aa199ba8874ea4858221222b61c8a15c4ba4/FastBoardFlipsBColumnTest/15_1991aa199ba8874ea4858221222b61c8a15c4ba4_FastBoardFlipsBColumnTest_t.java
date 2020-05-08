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
 
     public void testBlackPlaceB5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.blackPlaceB5();
 
         assertEquals("____x___", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("____x___", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("___x____", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testBlackFlipB5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("____o___");
         flips.a4_e8 = LineConverter.convertStringToLine("____o___");
         flips.a5_h5 = LineConverter.convertStringToLine("_o______");
         flips.a6_f1 = LineConverter.convertStringToLine("___o____");
 
         flips.blackFlipB5();
 
         assertEquals("____x___", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("____x___", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("___x____", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testWhitePlaceB5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.whitePlaceB5();
 
         assertEquals("____o___", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("____o___", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("___o____", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testWhiteFlipB5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("____x___");
         flips.a4_e8 = LineConverter.convertStringToLine("____x___");
         flips.a5_h5 = LineConverter.convertStringToLine("_x______");
         flips.a6_f1 = LineConverter.convertStringToLine("___x____");
 
         flips.whiteFlipB5();
 
         assertEquals("____o___", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("____o___", LineConverter.convertLineToString(flips.a4_e8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("___o____", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testBlackPlaceB6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.blackPlaceB6();
 
         assertEquals("_____x__", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testBlackFlipB6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("_____o__");
         flips.a5_d8 = LineConverter.convertStringToLine("_____o__");
         flips.a6_h6 = LineConverter.convertStringToLine("_o______");
         flips.a7_g1 = LineConverter.convertStringToLine("__o_____");
 
         flips.blackFlipB6();
 
         assertEquals("_____x__", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("__x_____", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testWhitePlaceB6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.whitePlaceB6();
 
         assertEquals("_____o__", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testWhiteFlipB6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("_____x__");
         flips.a5_d8 = LineConverter.convertStringToLine("_____x__");
         flips.a6_h6 = LineConverter.convertStringToLine("_x______");
         flips.a7_g1 = LineConverter.convertStringToLine("__x_____");
 
         flips.whiteFlipB6();
 
         assertEquals("_____o__", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a5_d8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("__o_____", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testBlackPlaceB7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.blackPlaceB7();
 
         assertEquals("______x_", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("______x_", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testBlackFlipB7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("______o_");
         flips.a6_c8 = LineConverter.convertStringToLine("______o_");
         flips.a7_h7 = LineConverter.convertStringToLine("_o______");
         flips.a8_h1 = LineConverter.convertStringToLine("_o______");
 
         flips.blackFlipB7();
 
         assertEquals("______x_", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("______x_", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testWhitePlaceB7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.whitePlaceB7();
 
         assertEquals("______o_", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("______o_", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testWhiteFlipB7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.b1_b8 = LineConverter.convertStringToLine("______x_");
         flips.a6_c8 = LineConverter.convertStringToLine("______x_");
         flips.a7_h7 = LineConverter.convertStringToLine("_x______");
         flips.a8_h1 = LineConverter.convertStringToLine("_x______");
 
         flips.whiteFlipB7();
 
         assertEquals("______o_", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("______o_", LineConverter.convertLineToString(flips.a6_c8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testBlackPlaceB8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.blackPlaceB8();
 
         assertEquals("_______x", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testBlackFlipB8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
        flips.b1_b8 = LineConverter.convertStringToLine("__xo___o");
        flips.a8_h8 = LineConverter.convertStringToLine("_o______");
         flips.b8_h2 = LineConverter.convertStringToLine("_o______");
 
         flips.blackFlipB8();
 
        assertEquals("__xo___x", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("_x______", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testWhitePlaceB8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.whitePlaceB8();
 
         assertEquals("_______o", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testWhiteFlipB8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
        flips.b1_b8 = LineConverter.convertStringToLine("__oxo__x");
        flips.a8_h8 = LineConverter.convertStringToLine("_x______");
         flips.b8_h2 = LineConverter.convertStringToLine("_x______");
 
         flips.whiteFlipB8();
 
        assertEquals("__oxo__o", LineConverter.convertLineToString(flips.b1_b8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("_o______", LineConverter.convertLineToString(flips.b8_h2));
     }
 }
