 /*
 Copyright (c) 2012.
 The Board of Trustees of the Leland Stanford Junior University.
 All rights reserved.
 
 Redistribution and use of this distribution in source and binary forms, with or
 without modification, are permitted provided that: The above copyright notice
 and this permission notice appear in all copies and supporting documentation;
 The name, identifiers, and trademarks of The Board of Trustees of the Leland
 Stanford Junior University are not used in advertising or publicity without the
 express prior written permission of The Board of Trustees of the Leland Stanford
 Junior University; Recipients acknowledge that this distribution is made
 available as a research courtesy, "as is", potentially with defects, without
  any obligation on the part of The Board of Trustees of the Leland Stanford
  Junior University to provide support, services, or repair;
 
 THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY DISCLAIMS ALL
 WARRANTIES, EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT
 LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE BOARD OF TRUSTEES OF THE LELAND
 STANFORD JUNIOR UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING NEGLIGENCE) OR STRICT
 LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 SOFTWARE.
 */
 
 package edu.stanford.lucene.analysis.cjk;
 
 import java.io.*;
 
 import org.apache.lucene.analysis.*;
 import org.apache.lucene.analysis.icu.segmentation.ICUTokenizer;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.junit.*;
 
 /**
  * @author Naomi Dushay
  */
 public class TestCJKSieveFilter extends BaseTokenStreamTestCase
 {
     static {
     	System.setProperty("tests.asserts.gracious", "true");
     }
 
 @Test
 	public void testJapaneseEmitsIfHiraganaPresent() throws Exception
 	{
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.JAPANESE),
 			"近世仮名遣い論の研究 -- chars 6, 8: い の are hiragana",
 			new String[] { "近", "世", "仮", "名", "遣", "い", "論", "の", "研", "究", "chars", "6", "8", "い", "の", "are", "hiragana" },
 			new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 14, 20, 23, 26, 28, 30, 34 },   // startOffsets
 			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 19, 21, 24, 27, 29, 33, 42 },  // endOffsets
 			new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<HIRAGANA>", "<IDEOGRAPHIC>", "<HIRAGANA>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<ALPHANUM>", "<NUM>", "<NUM>", "<HIRAGANA>", "<HIRAGANA>", "<ALPHANUM>", "<ALPHANUM>"},
 			new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});  // positionIncrements
 	}
 
 @Test
 	public void testJapaneseEmitsIfKatakanaPresent() throws Exception
 	{
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.JAPANESE),
 			"マンガ is katakana",
 			new String[] { "マンガ", "is",  "katakana" },
 			new int[] { 0, 4, 7 },   // startOffsets
 			new int[] { 3, 6, 15 },  // endOffsets
 			new String[] { "<KATAKANA>", "<ALPHANUM>", "<ALPHANUM>" },
 			new int[] { 1, 1, 1});  // positionIncrements
 	}
 
 @Test
 	public void testJapaneseEmitsIfBothPresent() throws Exception
 	{
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.JAPANESE),
 			"日本マンガを知るためのブック・ガイド",
 			new String[] { "日", "本", "マンガ", "を", "知", "る", "た", "め", "の", "ブック", "ガイド" },
 			new int[] { 0, 1, 2, 5, 6, 7, 8, 9, 10, 11, 15 },   // startOffsets
 			new int[] { 1, 2, 5, 6, 7, 8, 9, 10, 11, 14, 18 },  // endOffsets
 			new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<KATAKANA>", "<HIRAGANA>", "<IDEOGRAPHIC>", "<HIRAGANA>", "<HIRAGANA>", "<HIRAGANA>", "<HIRAGANA>", "<KATAKANA>", "<KATAKANA>" },
 			new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });  // positionIncrements
 	}
 
 @Test
 	public void testJapaneseDoesNotEmitIfScriptsAbsent() throws Exception
 	{
 		Analyzer a = getStdTokenAnalyzer(CJKEmitType.JAPANESE);
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("仏教学 乱 禅 modern kanji")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("南滿洲鐵道株式會社 traditional han only")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("Simplified  中国地方志集成")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("한국경제 hangul only")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("한국사 의 壇君 인식 hangul and hancha")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("No CJK here ... Des mot clés À LA CHAÎNE À Á ")), new String[] {});
 	}
 
 @Test
 	public void testHangulEmitIfPresent() throws Exception
 	{
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.HANGUL),
 			"한국경제 hangul",
 			new String[] { "한국경제", "hangul" },
 			new int[] { 0, 5 },   // startOffsets
 			new int[] { 4, 11 },  // endOffsets
 			new String[] { "<HANGUL>", "<ALPHANUM>" },
 			new int[] { 1, 1 });  // positionIncrements
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.HANGUL),
 			"한국사 의 壇君 인식 hangul and hancha",
 			new String[] { "한국사", "의", "壇", "君", "인식", "hangul", "and", "hancha" },
 			new int[] { 0, 4, 6, 7, 9, 12, 19, 23 },   // startOffsets
 			new int[] { 3, 5, 7, 8, 11, 18, 22, 29 },  // endOffsets
 			new String[] { "<HANGUL>", "<HANGUL>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<HANGUL>", "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>" },
 			new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });  // positionIncrements
 	}
 
 @Test
 	public void testHangulDoesNotEmitIfAbsent() throws Exception
 	{
 		Analyzer a = getStdTokenAnalyzer(CJKEmitType.HANGUL);
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("南滿洲鐵道株式會社 traditional han only")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("Simplified  中国地方志集成")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("仏教学 乱 禅 modern kanji")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("を知るための is hiragana except 知 which is kanji")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("マンガ is katakana")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("日本マンガを知るためのブック・ガイド")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("No CJK here ... Des mot clés À LA CHAÎNE À Á ")), new String[] {});
 	}
 
 @Test
 	public void testHanSoloEmitsIfHanOnly() throws Exception
 	{
 		// traditional
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.HAN_SOLO),
 			"南滿洲鐵道株式會社 traditional han",
 			new String[] { "南", "滿", "洲", "鐵", "道", "株", "式", "會", "社", "traditional", "han" },
 			new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 22 },   // startOffsets
 			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 21, 25 },  // endOffsets
 			new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<ALPHANUM>", "<ALPHANUM>" },
 			new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });  // positionIncrements
 
 		// simplified
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.HAN_SOLO),
 			"中国地方志集成",
 			new String[] { "中", "国", "地", "方", "志", "集", "成" },
 			new int[] { 0, 1, 2, 3, 4, 5, 6 },   // startOffsets
 			new int[] { 1, 2, 3, 4, 5, 6, 7 },  // endOffsets
 			new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>" },
 			new int[] { 1, 1, 1, 1, 1, 1, 1 });  // positionIncrements
 
 		// japanese modern kanji
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.HAN_SOLO),
 			"仏教学 乱 禅",
 			new String[] { "仏", "教", "学", "乱", "禅" },
 			new int[] { 0, 1, 2, 4, 6 },   // startOffsets
 			new int[] { 1, 2, 3, 5, 7 },  // endOffsets
 			new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>" },
 			new int[] { 1, 1, 1, 1, 1 });  // positionIncrements
 
 		// from korean: 壇君
 		assertAnalyzesTo( getStdTokenAnalyzer(CJKEmitType.HAN_SOLO),
 			"壇君",
 			new String[] { "壇", "君" },
 			new int[] { 0, 1 },   // startOffsets
 			new int[] { 1, 2 },  // endOffsets
 			new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>" },
 			new int[] { 1, 1 });  // positionIncrements
 	}
 
 @Test
 	public void testHanSoloDoesNotEmitIfHanAbsent() throws Exception
 	{
		Analyzer a = getStdTokenAnalyzer(CJKEmitType.HAN_SOLO);
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("マンガ is katakana")), new String[] {});
		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("한국경제의 hangul only")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("No CJK here ... Des mot clés À LA CHAÎNE À Á ")), new String[] {});
 	}
 
 @Test
 	public void testHanSoloDoesNotEmitIfOtherPresent() throws Exception
 	{
		Analyzer a = getStdTokenAnalyzer(CJKEmitType.HAN_SOLO);
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("don't pass 多くの学生が me 試験に落ちた thru")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("を知るための is hiragana except 知 which is kanji")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("日本マンガを知るためのブック・ガイド")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("한국사 의 壇君 인식 hangul and hancha")), new String[] {});
 	}
 
 @Test
 	public void testNoCJKEmitsIfCJKAbsent() throws Exception
 	{
 		assertAnalyzesTo(
 			getStdTokenAnalyzer(CJKEmitType.NO_CJK),
 			"No CJK here",
 			new String[] { "No", "CJK", "here" },
 			new int[] { 0, 3, 7 },   // startOffsets
 			new int[] { 2, 6, 11 },  // endOffsets
 			new String[] { "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>"},
 			new int[] { 1, 1, 1 });  // positionIncrements
 	}
 
 @Test
 	public void testNoCJKDoesNotEmitIfCJKPresent() throws Exception
 	{
 		Analyzer a = getStdTokenAnalyzer(CJKEmitType.NO_CJK);
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("don't pass 多くの学生が me 試験に落ちた thru")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("南滿洲鐵道株式會社 traditional han only")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("Simplified  中国地方志集成")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("仏教学 乱 禅 modern kanji")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("を知るための is hiragana except 知 which is kanji")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("マンガ is katakana")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("日本マンガを知るためのブック・ガイド")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("한국경제 hangul only")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("한국사 의 壇君 인식 hangul and hancha")), new String[] {});
 	}
 
 //FIXME:  fails - thread safety issues for cache?
 //@Test
 //	public void testReusableTokenStream() throws Exception
 //	{
 //	    Analyzer a = getStdTokenAnalyzer(CJKEmitType.HAN_SOLO);
 //	    assertAnalyzesToReuse(a, "我购买 Tests 了道具和服装",
 //	        new String[] { "我", "购", "买", "Tests", "了", "道", "具", "和", "服", "装"},
 //	        new int[] { 0, 1, 2, 4, 10, 11, 12, 13, 14, 15 },
 //	        new int[] { 1, 2, 3, 9, 11, 12, 13, 14, 15, 16 });
 //	    assertAnalyzesToReuse(a, "我购买了道具和服装。",
 //	        new String[] { "我", "购", "买", "了", "道", "具", "和", "服", "装"},
 //	        new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 },
 //	        new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
 //	 }
 //
 ////FIXME:  fails - thread safety issues for cache?
 //	 /** blast some random strings through the analyzer */
 //@Test
 //	 public void testRandomStrings() throws Exception {
 //	    checkRandomData(random, getStdTokenAnalyzer(CJKEmitType.HAN_SOLO), 10000*RANDOM_MULTIPLIER);
 //	 }
 //
 ////FIXME:  fails - thread safety issues for cache?
 //	 /** blast some random large strings through the analyzer */
 //@Test
 //	 public void testRandomHugeStrings() throws Exception {
 //	    checkRandomData(random, getStdTokenAnalyzer(CJKEmitType.HAN_SOLO), 200*RANDOM_MULTIPLIER, 8192);
 //	 }
 
 @Test
 	 public void testEmptyTerm() throws IOException
 	 {
 	    Analyzer a = new ReusableAnalyzerBase() {
 	      @Override @Ignore
 	      protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
 	        Tokenizer tokenizer = new KeywordTokenizer(reader);
 	        return new TokenStreamComponents(tokenizer, new CJKSieveFilter(tokenizer, CJKEmitType.HAN_SOLO));
 	      }
 	    };
 	    checkAnalysisConsistency(random, a, random.nextBoolean(), "");
 	 }
 
 	 // LUCENE-3026 (for SmartCNAnalyzer)
 @Test
 	 public void testLargeDocument() throws Exception
 	 {
 	    StringBuilder sb = new StringBuilder();
 	    for (int i = 0; i < 5000; i++) {
 	      sb.append("我购买了道具和服装。");
 	    }
 	    Analyzer analyzer = getStdTokenAnalyzer(CJKEmitType.HAN_SOLO);
 	    TokenStream stream = analyzer.reusableTokenStream("", new StringReader(sb.toString()));
 	    stream.reset();
 	    while (stream.incrementToken())
 	    {
 	    }
 	 }
 
 @Test
 	public void testWithICUTokenizer() throws Exception
 	{
 		Analyzer a = new ReusableAnalyzerBase()
 		{
 			@Ignore
 			protected TokenStreamComponents createComponents(String fieldName, Reader reader)
 			{
 				Tokenizer t = new ICUTokenizer(reader);
 				// Tokenizer t = new MockTokenizer(reader, MockTokenizer.WHITESPACE, false);
 				return new TokenStreamComponents(t, new CJKSieveFilter(t, CJKEmitType.HANGUL));
 			}
 		};
 
 		assertAnalyzesTo(a, "한국경제 hangul", new String[] { "한국경제", "hangul" });
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("南滿洲鐵道株式會社 traditional han only")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("Simplified  中国地方志集成")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("仏教学 乱 禅 modern kanji")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("を知るための is hiragana except 知 which is kanji")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("マンガ is katakana")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("日本マンガを知るためのブック・ガイド")), new String[] {});
 		assertTokenStreamContents(a.tokenStream("dummy", new StringReader("No CJK here ... Des mot clés À LA CHAÎNE À Á ")), new String[] {});
 	}
 
 @Test
 	public void testReadCharsForType() throws Exception
 	{
 		Analyzer hangul = getWhitespaceTokenAnalyzer(CJKEmitType.HANGUL);
 		assertAnalyzesTo(hangul, "한국경제 hangul", new String[] { "한국경제", "hangul" });
 		assertAnalyzesTo(hangul, "を知るための is hiragana except 知 which is kanji", new String[] {});
 		assertAnalyzesTo(hangul, "No CJK here ... Des mot clés À LA CHAÎNE À Á ", new String[] {});
 
 		Analyzer japanese = getWhitespaceTokenAnalyzer(CJKEmitType.JAPANESE);
 		assertAnalyzesTo(japanese, "マンガ is katakana", new String[] {"マンガ", "is", "katakana"});
 		assertAnalyzesTo(japanese, "近世仮名遣い論の研究 has hiragana", new String[] {"近世仮名遣い論の研究", "has", "hiragana"});
		assertAnalyzesTo(japanese, "日本マンガを知るための", new String[] {"日本マンガを知るための"});
 		assertAnalyzesTo(japanese, "한국경제 hangul", new String[] {});
 		assertAnalyzesTo(japanese, "No CJK here ... Des mot clés À LA CHAÎNE À Á ", new String[] {});
 
 		Analyzer han_solo = getWhitespaceTokenAnalyzer(CJKEmitType.HAN_SOLO);
 		assertAnalyzesTo(han_solo, "南滿洲鐵道株式會社 traditional han only", new String[] {"南滿洲鐵道株式會社", "traditional", "han", "only"});
 		assertAnalyzesTo(han_solo, "Simplified  中国地方志集成", new String[] {"Simplified", "中国地方志集成"});
 		assertAnalyzesTo(han_solo, "を知るための is hiragana except 知 which is kanji", new String[] {});
 		assertAnalyzesTo(han_solo, "한국경제 hangul", new String[] {});
 		assertAnalyzesTo(han_solo, "No CJK here ... Des mot clés À LA CHAÎNE À Á ", new String[] {});
 
 		Analyzer no_cjk = getWhitespaceTokenAnalyzer(CJKEmitType.NO_CJK);
 		assertAnalyzesTo(no_cjk, "No CJK here", new String[] {"No", "CJK", "here"});
 		assertAnalyzesTo(no_cjk, "南滿洲鐵道株式會社 traditional han only", new String[] {});
 		assertAnalyzesTo(no_cjk, "Simplified  中国地方志集成", new String[] {});
 		assertAnalyzesTo(no_cjk, "を知るための is hiragana except 知 which is kanji", new String[] {});
 		assertAnalyzesTo(no_cjk, "한국경제 hangul", new String[] {});
 	}
 
 	/**
 	 * @return Analyzer of a StandardTokenizer followed by CJKSieveFilter
 	 */
 	@Ignore
 	private Analyzer getStdTokenAnalyzer(final CJKEmitType emitType) {
 		Analyzer analyzer = new ReusableAnalyzerBase()
 		{
 			protected TokenStreamComponents createComponents(String fieldName, Reader reader)
 			{
 				Tokenizer t = new StandardTokenizer(TEST_VERSION_CURRENT, reader);
 				// Tokenizer t = new MockTokenizer(reader, MockTokenizer.WHITESPACE, false);
 				return new TokenStreamComponents(t, new CJKSieveFilter(t, emitType));
 			}
 		};
 		return analyzer;
 	}
 
 
 	/**
 	 * @return Analyzer of a WhitespaceTokenizer followed by CJKSieveFilter
 	 */
 	@Ignore
 	private Analyzer getWhitespaceTokenAnalyzer(final CJKEmitType emitType) {
 		Analyzer analyzer = new ReusableAnalyzerBase()
 		{
 			protected TokenStreamComponents createComponents(String fieldName, Reader reader)
 			{
 				Tokenizer t = new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader);
 				// Tokenizer t = new MockTokenizer(reader, MockTokenizer.WHITESPACE, false);
 				return new TokenStreamComponents(t, new CJKSieveFilter(t, emitType));
 			}
 		};
 		return analyzer;
 	}
 
 }
