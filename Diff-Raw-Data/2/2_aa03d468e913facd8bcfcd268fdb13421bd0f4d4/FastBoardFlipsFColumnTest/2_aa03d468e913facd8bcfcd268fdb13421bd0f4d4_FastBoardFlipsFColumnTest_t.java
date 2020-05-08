 package fastboard.fastflip;
 
 import base.testcase.OthelloTestCase;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Dec 9, 2009
  * Time: 8:37:37 PM
 * This tests whether or not we are dealing with flipping and placing discs on the f column properly
  */
 public class FastBoardFlipsFColumnTest extends OthelloTestCase {
     public void testClackPlaceF1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.blackPlaceF1();
 
         assertEquals("x_______", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testClackFlipF1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.f1_f8 = LineConverter.convertStringToLine("o_______");
         flips.f1_h3 = LineConverter.convertStringToLine("_____o__");
         flips.a1_h1 = LineConverter.convertStringToLine("_____o__");
         flips.a6_f1 = LineConverter.convertStringToLine("_______o");
 
         flips.blackFlipF1();
 
         assertEquals("x_______", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testWhitePlaceF1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.whitePlaceF1();
 
         assertEquals("o_______", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testWhiteFlipF1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_f1));
 
         flips.f1_f8 = LineConverter.convertStringToLine("x_______");
         flips.f1_h3 = LineConverter.convertStringToLine("_____x__");
         flips.a1_h1 = LineConverter.convertStringToLine("_____x__");
         flips.a6_f1 = LineConverter.convertStringToLine("_______x");
 
         flips.whiteFlipF1();
 
         assertEquals("o_______", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a6_f1));
     }
 
     public void testClackPlaceF2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.blackPlaceF2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("______x_", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testClackFlipF2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.f1_f8 = LineConverter.convertStringToLine("_o______");
         flips.e1_h4 = LineConverter.convertStringToLine("_____o__");
         flips.a2_h2 = LineConverter.convertStringToLine("_____o__");
         flips.a7_g1 = LineConverter.convertStringToLine("______o_");
 
         flips.blackFlipF2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("______x_", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testWhitePlaceF2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.whitePlaceF2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("______o_", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testWhiteFlipF2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_g1));
 
         flips.f1_f8 = LineConverter.convertStringToLine("_x______");
         flips.e1_h4 = LineConverter.convertStringToLine("_____x__");
         flips.a2_h2 = LineConverter.convertStringToLine("_____x__");
         flips.a7_g1 = LineConverter.convertStringToLine("______x_");
 
         flips.whiteFlipF2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("______o_", LineConverter.convertLineToString(flips.a7_g1));
     }
 
     public void testClackPlaceF3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.blackPlaceF3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testClackFlipF3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.f1_f8 = LineConverter.convertStringToLine("__o_____");
         flips.d1_h5 = LineConverter.convertStringToLine("_____o__");
         flips.a3_h3 = LineConverter.convertStringToLine("_____o__");
         flips.a8_h1 = LineConverter.convertStringToLine("_____o__");
 
         flips.blackFlipF3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testWhitePlaceF3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.whitePlaceF3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testWhiteFlipF3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.f1_f8 = LineConverter.convertStringToLine("__x_____");
         flips.d1_h5 = LineConverter.convertStringToLine("_____x__");
         flips.a3_h3 = LineConverter.convertStringToLine("_____x__");
         flips.a8_h1 = LineConverter.convertStringToLine("_____x__");
 
         flips.whiteFlipF3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testClackPlaceF4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.blackPlaceF4();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testClackFlipF4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.f1_f8 = LineConverter.convertStringToLine("___o____");
         flips.c1_h6 = LineConverter.convertStringToLine("_____o__");
         flips.a4_h4 = LineConverter.convertStringToLine("_____o__");
         flips.b8_h2 = LineConverter.convertStringToLine("_____o__");
 
         flips.blackFlipF4();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testWhitePlaceF4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.whitePlaceF4();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testWhiteFlipF4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.f1_f8 = LineConverter.convertStringToLine("___x____");
         flips.c1_h6 = LineConverter.convertStringToLine("_____x__");
         flips.a4_h4 = LineConverter.convertStringToLine("_____x__");
         flips.b8_h2 = LineConverter.convertStringToLine("_____x__");
 
         flips.whiteFlipF4();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testClackPlaceF5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.c8_h3));
 
         flips.blackPlaceF5();
 
         assertEquals("____x___", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.c8_h3));
     }
 
     public void testClackFlipF5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.c8_h3));
 
         flips.f1_f8 = LineConverter.convertStringToLine("____o___");
         flips.b1_h7 = LineConverter.convertStringToLine("_____o__");
         flips.a5_h5 = LineConverter.convertStringToLine("_____o__");
         flips.c8_h3 = LineConverter.convertStringToLine("_____o__");
 
         flips.blackFlipF5();
 
         assertEquals("____x___", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.c8_h3));
     }
 
     public void testWhitePlaceF5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.c8_h3));
 
         flips.whitePlaceF5();
 
         assertEquals("____o___", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.c8_h3));
     }
 
     public void testWhiteFlipF5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.c8_h3));
 
         flips.f1_f8 = LineConverter.convertStringToLine("____x___");
         flips.b1_h7 = LineConverter.convertStringToLine("_____x__");
         flips.a5_h5 = LineConverter.convertStringToLine("_____x__");
         flips.c8_h3 = LineConverter.convertStringToLine("_____x__");
 
         flips.whiteFlipF5();
 
         assertEquals("____o___", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.c8_h3));
     }
 
     public void testClackPlaceF6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.d8_h4));
 
         flips.blackPlaceF6();
 
         assertEquals("_____x__", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.d8_h4));
     }
 
     public void testClackFlipF6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.d8_h4));
 
         flips.f1_f8 = LineConverter.convertStringToLine("_____o__");
         flips.a1_h8 = LineConverter.convertStringToLine("_____o__");
         flips.a6_h6 = LineConverter.convertStringToLine("_____o__");
         flips.d8_h4 = LineConverter.convertStringToLine("_____o__");
 
         flips.blackFlipF6();
 
         assertEquals("_____x__", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.d8_h4));
     }
 
     public void testWhitePlaceF6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.d8_h4));
 
         flips.whitePlaceF6();
 
         assertEquals("_____o__", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.d8_h4));
     }
 
     public void testWhiteFlipF6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.d8_h4));
 
         flips.f1_f8 = LineConverter.convertStringToLine("_____x__");
         flips.a1_h8 = LineConverter.convertStringToLine("_____x__");
         flips.a6_h6 = LineConverter.convertStringToLine("_____x__");
         flips.d8_h4 = LineConverter.convertStringToLine("_____x__");
 
         flips.whiteFlipF6();
 
         assertEquals("_____o__", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.d8_h4));
     }
 
     public void testClackPlaceF7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.e8_h5));
 
         flips.blackPlaceF7();
 
         assertEquals("______x_", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("______x_", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.e8_h5));
     }
 
     public void testClackFlipF7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.e8_h5));
 
         flips.f1_f8 = LineConverter.convertStringToLine("______o_");
         flips.a2_g8 = LineConverter.convertStringToLine("______o_");
         flips.a7_h7 = LineConverter.convertStringToLine("_____o__");
         flips.e8_h5 = LineConverter.convertStringToLine("_____o__");
 
         flips.blackFlipF7();
 
         assertEquals("______x_", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("______x_", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.e8_h5));
     }
 
     public void testWhitePlaceF7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.e8_h5));
 
         flips.whitePlaceF7();
 
         assertEquals("______o_", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("______o_", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.e8_h5));
     }
 
     public void testWhiteFlipF7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.e8_h5));
 
         flips.f1_f8 = LineConverter.convertStringToLine("______x_");
         flips.a2_g8 = LineConverter.convertStringToLine("______x_");
         flips.a7_h7 = LineConverter.convertStringToLine("_____x__");
         flips.e8_h5 = LineConverter.convertStringToLine("_____x__");
 
         flips.whiteFlipF7();
 
         assertEquals("______o_", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("______o_", LineConverter.convertLineToString(flips.a2_g8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a7_h7));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.e8_h5));
     }
 
     public void testClackPlaceF8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.f8_h6));
 
         flips.blackPlaceF8();
 
         assertEquals("_______x", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.f8_h6));
     }
 
     public void testClackFlipF8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.f8_h6));
 
         flips.f1_f8 = LineConverter.convertStringToLine("_______o");
         flips.a3_f8 = LineConverter.convertStringToLine("_______o");
         flips.a8_h8 = LineConverter.convertStringToLine("_____o__");
         flips.f8_h6 = LineConverter.convertStringToLine("_____o__");
 
         flips.blackFlipF8();
 
         assertEquals("_______x", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("_____x__", LineConverter.convertLineToString(flips.f8_h6));
     }
 
     public void testWhitePlaceF8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.f8_h6));
 
         flips.whitePlaceF8();
 
         assertEquals("_______o", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.f8_h6));
     }
 
     public void testWhiteFlipF8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.f8_h6));
 
         flips.f1_f8 = LineConverter.convertStringToLine("_______x");
         flips.a3_f8 = LineConverter.convertStringToLine("_______x");
         flips.a8_h8 = LineConverter.convertStringToLine("_____x__");
         flips.f8_h6 = LineConverter.convertStringToLine("_____x__");
 
         flips.whiteFlipF8();
 
         assertEquals("_______o", LineConverter.convertLineToString(flips.f1_f8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a3_f8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.a8_h8));
         assertEquals("_____o__", LineConverter.convertLineToString(flips.f8_h6));
     }
 }
