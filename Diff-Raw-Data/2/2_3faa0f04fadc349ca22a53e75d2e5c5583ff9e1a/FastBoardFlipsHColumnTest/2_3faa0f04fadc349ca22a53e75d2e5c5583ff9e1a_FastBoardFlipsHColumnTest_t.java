 package fastboard.fastflip;
 
 import base.testcase.OthelloTestCase;
 import fastboard.lineconverter.LineConverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Dec 9, 2009
  * Time: 8:37:37 PM
  * This tests whether or not we are dealing with flipping and placing discs on the h column properly
  */
 public class FastBoardFlipsHColumnTest extends OthelloTestCase {
     public void testClackPlaceH1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.blackPlaceH1();
 
         assertEquals("x_______", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testWhitePlaceH1() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h1));
 
         flips.whitePlaceH1();
 
         assertEquals("o_______", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a1_h1));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a8_h1));
     }
 
     public void testClackPlaceH2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.blackPlaceH2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("_______x", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testClackFlipH2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.h1_h8 = LineConverter.convertStringToLine("_o______");
         flips.a2_h2 = LineConverter.convertStringToLine("_______o");
         flips.b8_h2 = LineConverter.convertStringToLine("_______o");
 
         flips.blackFlipH2();
 
         assertEquals("_x______", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("_______x", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testWhitePlaceH2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.whitePlaceH2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("_______o", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testWhiteFlipH2() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("________", LineConverter.convertLineToString(flips.b8_h2));
 
         flips.h1_h8 = LineConverter.convertStringToLine("_x______");
         flips.a2_h2 = LineConverter.convertStringToLine("_______x");
         flips.b8_h2 = LineConverter.convertStringToLine("_______x");
 
         flips.whiteFlipH2();
 
         assertEquals("_o______", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a2_h2));
         assertEquals("_______o", LineConverter.convertLineToString(flips.b8_h2));
     }
 
     public void testClackPlaceH3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.c8_h3));
 
         flips.blackPlaceH3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_______x", LineConverter.convertLineToString(flips.c8_h3));
     }
 
     public void testClackFlipH3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.c8_h3));
 
         flips.h1_h8 = LineConverter.convertStringToLine("__o_____");
         flips.f1_h3 = LineConverter.convertStringToLine("_______o");
         flips.a3_h3 = LineConverter.convertStringToLine("_______o");
         flips.c8_h3 = LineConverter.convertStringToLine("_______o");
 
         flips.blackFlipH3();
 
         assertEquals("__x_____", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_______x", LineConverter.convertLineToString(flips.c8_h3));
     }
 
     public void testWhitePlaceH3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.c8_h3));
 
         flips.whitePlaceH3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_______o", LineConverter.convertLineToString(flips.c8_h3));
     }
 
     public void testWhiteFlipH3() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("________", LineConverter.convertLineToString(flips.c8_h3));
 
         flips.h1_h8 = LineConverter.convertStringToLine("__x_____");
         flips.f1_h3 = LineConverter.convertStringToLine("_______x");
         flips.a3_h3 = LineConverter.convertStringToLine("_______x");
         flips.c8_h3 = LineConverter.convertStringToLine("_______x");
 
         flips.whiteFlipH3();
 
         assertEquals("__o_____", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.f1_h3));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a3_h3));
         assertEquals("_______o", LineConverter.convertLineToString(flips.c8_h3));
     }
 
     public void testClackPlaceH4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.d8_h4));
 
         flips.blackPlaceH4();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("_______x", LineConverter.convertLineToString(flips.d8_h4));
     }
 
     public void testClackFlipH4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.d8_h4));
 
         flips.h1_h8 = LineConverter.convertStringToLine("___o____");
         flips.e1_h4 = LineConverter.convertStringToLine("_______o");
         flips.a4_h4 = LineConverter.convertStringToLine("_______o");
         flips.d8_h4 = LineConverter.convertStringToLine("_______o");
 
         flips.blackFlipH4();
 
         assertEquals("___x____", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("_______x", LineConverter.convertLineToString(flips.d8_h4));
     }
 
     public void testWhitePlaceH4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.d8_h4));
 
         flips.whitePlaceH4();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("_______o", LineConverter.convertLineToString(flips.d8_h4));
     }
 
     public void testWhiteFlipH4() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("________", LineConverter.convertLineToString(flips.d8_h4));
 
         flips.h1_h8 = LineConverter.convertStringToLine("___x____");
         flips.e1_h4 = LineConverter.convertStringToLine("_______x");
         flips.a4_h4 = LineConverter.convertStringToLine("_______x");
         flips.d8_h4 = LineConverter.convertStringToLine("_______x");
 
         flips.whiteFlipH4();
 
         assertEquals("___o____", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.e1_h4));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a4_h4));
         assertEquals("_______o", LineConverter.convertLineToString(flips.d8_h4));
     }
 
     public void testClackPlaceH5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.e8_h5));
 
         flips.blackPlaceH5();
 
         assertEquals("____x___", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("_______x", LineConverter.convertLineToString(flips.e8_h5));
     }
 
     public void testClackFlipH5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.e8_h5));
 
         flips.h1_h8 = LineConverter.convertStringToLine("____o___");
         flips.d1_h5 = LineConverter.convertStringToLine("_______o");
         flips.a5_h5 = LineConverter.convertStringToLine("_______o");
         flips.e8_h5 = LineConverter.convertStringToLine("_______o");
 
         flips.blackFlipH5();
 
         assertEquals("____x___", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("_______x", LineConverter.convertLineToString(flips.e8_h5));
     }
 
     public void testWhitePlaceH5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.e8_h5));
 
         flips.whitePlaceH5();
 
         assertEquals("____o___", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("_______o", LineConverter.convertLineToString(flips.e8_h5));
     }
 
     public void testWhiteFlipH5() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("________", LineConverter.convertLineToString(flips.e8_h5));
 
         flips.h1_h8 = LineConverter.convertStringToLine("____x___");
         flips.d1_h5 = LineConverter.convertStringToLine("_______x");
         flips.a5_h5 = LineConverter.convertStringToLine("_______x");
         flips.e8_h5 = LineConverter.convertStringToLine("_______x");
 
         flips.whiteFlipH5();
 
         assertEquals("____o___", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.d1_h5));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a5_h5));
         assertEquals("_______o", LineConverter.convertLineToString(flips.e8_h5));
     }
 
     public void testClackPlaceH6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.f8_h6));
 
         flips.blackPlaceH6();
 
         assertEquals("_____x__", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("_______x", LineConverter.convertLineToString(flips.f8_h6));
     }
 
     public void testClackFlipH6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.f8_h6));
 
         flips.h1_h8 = LineConverter.convertStringToLine("_____o__");
         flips.c1_h6 = LineConverter.convertStringToLine("_______o");
         flips.a6_h6 = LineConverter.convertStringToLine("_______o");
         flips.f8_h6 = LineConverter.convertStringToLine("_______o");
 
         flips.blackFlipH6();
 
         assertEquals("_____x__", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("_______x", LineConverter.convertLineToString(flips.f8_h6));
     }
 
     public void testWhitePlaceH6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.f8_h6));
 
         flips.whitePlaceH6();
 
         assertEquals("_____o__", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("_______o", LineConverter.convertLineToString(flips.f8_h6));
     }
 
     public void testWhiteFlipH6() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("________", LineConverter.convertLineToString(flips.f8_h6));
 
         flips.h1_h8 = LineConverter.convertStringToLine("_____x__");
         flips.c1_h6 = LineConverter.convertStringToLine("_______x");
         flips.a6_h6 = LineConverter.convertStringToLine("_______x");
         flips.f8_h6 = LineConverter.convertStringToLine("_______x");
 
         flips.whiteFlipH6();
 
         assertEquals("_____o__", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.c1_h6));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a6_h6));
         assertEquals("_______o", LineConverter.convertLineToString(flips.f8_h6));
     }
 
     public void testClackPlaceH7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
 
         flips.blackPlaceH7();
 
         assertEquals("______x_", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a7_h7));
     }
 
     public void testClackFlipH7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
 
         flips.h1_h8 = LineConverter.convertStringToLine("______o_");
         flips.b1_h7 = LineConverter.convertStringToLine("_______o");
         flips.a7_h7 = LineConverter.convertStringToLine("_______o");
 
         flips.blackFlipH7();
 
         assertEquals("______x_", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a7_h7));
     }
 
     public void testWhitePlaceH7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
 
         flips.whitePlaceH7();
 
         assertEquals("______o_", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a7_h7));
     }
 
     public void testWhiteFlipH7() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("________", LineConverter.convertLineToString(flips.a7_h7));
 
         flips.h1_h8 = LineConverter.convertStringToLine("______x_");
         flips.b1_h7 = LineConverter.convertStringToLine("_______x");
         flips.a7_h7 = LineConverter.convertStringToLine("_______x");
 
         flips.whiteFlipH7();
 
         assertEquals("______o_", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.b1_h7));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a7_h7));
     }
 
     public void testClackPlaceH8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
 
         flips.blackPlaceH8();
 
         assertEquals("_______x", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_______x", LineConverter.convertLineToString(flips.a8_h8));
 
     }
 
     public void testWhitePlaceH8() {
         FastBoardFlips flips = new FastBoardFlips();
         assertEquals("________", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("________", LineConverter.convertLineToString(flips.a8_h8));
 
        flips.whitePlaceH8();
 
         assertEquals("_______o", LineConverter.convertLineToString(flips.h1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a1_h8));
         assertEquals("_______o", LineConverter.convertLineToString(flips.a8_h8));
     }
 }
