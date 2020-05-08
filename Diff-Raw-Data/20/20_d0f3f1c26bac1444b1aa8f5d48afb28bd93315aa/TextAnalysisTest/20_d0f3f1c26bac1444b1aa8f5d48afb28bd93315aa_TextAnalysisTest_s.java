 package com;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TextAnalysisTest {
 	ExtractPageContent epc;
 	ExtractPageContent epc1;
 	ExtractPageContent epc2;
 	TextAnalysis ta;
 	TextAnalysis ta1;
 	TextAnalysis ta2;
 	
 	@Before
 	public void setUp()
 	{
 		epc = new ExtractPageContent("example_pdfs/3.pdf");
 		epc1 = new ExtractPageContent("example_pdfs/2.pdf");
 		epc2 = new ExtractPageContent("example_pdfs/4eng.pdf");
 		
 		ta = new TextAnalysis(epc.getFileContents());
 		ta1 = new TextAnalysis(epc1.getFileContents());
 		ta2 = new TextAnalysis(epc2.getFileContents());
 	}
 	
 	@Test
 	public void testLanguageDetection()
 	{
 		Assert.assertEquals("Polish", ta.detectLanguage());
 		Assert.assertEquals("English", ta2.detectLanguage());
 	}
 	
 	@Test
 	public void testGetNumerOfSigns()
 	{
 		Assert.assertEquals(new Integer(34), ta.getNumberOfSigns());
 		Assert.assertEquals(new Integer(16658), ta1.getNumberOfSigns());
 		Assert.assertEquals(new Integer(164748), ta2.getNumberOfSigns());
 	}
 	@Test
 	public void testGetNumerOfWords()
 	{
 		Assert.assertEquals(new Integer(6), ta.getNumberOfWords());
 		Assert.assertEquals(new Integer(2509), ta1.getNumberOfWords());
 		Assert.assertEquals(new Integer(28820), ta2.getNumberOfWords());
 	}
 	
 	@Test
 	public void testNumberOfSentences() {
 		Assert.assertEquals(new Integer(3), ta.getNumberOfSentences());
 		Assert.assertEquals(new Integer(220), ta1.getNumberOfSentences());
 		Assert.assertEquals(new Integer(2588), ta2.getNumberOfSentences());
 	}
 	
 	@Test
 	public void testMinSentenceLength() {
 		Assert.assertEquals(new Integer(4), ta.getMinSentenceLength());
 		Assert.assertEquals(new Integer(22), ta1.getMinSentenceLength());
 		Assert.assertEquals(new Integer(1), ta2.getMinSentenceLength());
 	}
 	
 	@Test
 	public void testMaxSentenceLength() {
 		Assert.assertEquals(new Integer(17), ta.getMaxSentenceLength());
 		Assert.assertEquals(new Integer(133), ta1.getMaxSentenceLength());
 		Assert.assertEquals(new Integer(912), ta2.getMaxSentenceLength());
 	}
 	
 	@Test
 	public void testNumberOfSignsWtSpaces(){
 		Assert.assertEquals(new Integer(29), ta.getNumberOfSignsWtSpaces());
 		Assert.assertEquals(new Integer(14128), ta1.getNumberOfSignsWtSpaces());
 		Assert.assertEquals(new Integer(132259), ta2.getNumberOfSignsWtSpaces());
 	}
 }
