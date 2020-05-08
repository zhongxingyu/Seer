 package com.flaptor.util;
 
 public class LangUtilsTest extends TestCase{
 	
     @TestInfo (testType = TestInfo.TestType.UNIT)
 	public void testIdentifier() {
    	filterOutputRegex(".*(parsing\\sjar|identifier).*");
 		assertEquals("en", LangUtils.identify("The police got a confession from the robber that I caught,and the police used this info to capture the second thief.I got the impression that the police "));
 		assertEquals("es", LangUtils.identify("y entonces llegó el olvido para decirme casi en secreto: no la verás ya más. y respondí con mis ojos de mudo con mis labios de ciego:¿a quién?"));
 		assertTrue(LangUtils.isEnglish("that book is full of mouthwatering outrageous and over-the-top treats that are bound to spike your blood sugar through the roof. My sweet tooth just"));
		unfilterOutput();
 	}
 }	
 
