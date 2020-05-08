 package org.healthonnet.spellchecker.client.test;
 
 import static org.healthonnet.spellchecker.client.SpellcheckDictionary.English;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.healthonnet.spellchecker.client.SpellcheckRequester;
 import org.healthonnet.spellchecker.client.data.SpellcheckResponse;
 import org.healthonnet.spellchecker.client.data.Suggestion;
 import org.junit.Assert;
 import org.junit.Test;
 
 public class JsonParsingTest {
 	
 	@Test
 	public void testSingleWordSuggestion() throws IOException {
 		
 		SpellcheckResponse spellcheckResponse = SpellcheckRequester.getSpellcheckResponse(English, 1, "alzeimer");
 		
 		List<Suggestion> suggestions = spellcheckResponse.getSpellcheck().getSuggestions();
 		
 		Assert.assertEquals(1, suggestions.size());
 		Assert.assertEquals("alzeimer", suggestions.get(0).getOriginalString());
 		Assert.assertEquals(1, suggestions.get(0).getSuggestedCorrections().size());
 		Assert.assertEquals("alzheimer", suggestions.get(0).getSuggestedCorrections().get(0).getWord());
 	}
 	
 	@Test
 	public void testMultipleWordSuggestion() throws IOException {
 		SpellcheckResponse spellcheckResponse = SpellcheckRequester.getSpellcheckResponse(English, 1, "alzeimer", "diseas");
 		
 		List<Suggestion> suggestions = spellcheckResponse.getSpellcheck().getSuggestions();
 		
 		Assert.assertEquals(2, suggestions.size());
 		Assert.assertEquals("alzeimer", suggestions.get(0).getOriginalString());
 		Assert.assertEquals(1, suggestions.get(0).getSuggestedCorrections().size());
 		Assert.assertEquals("alzheimer", suggestions.get(0).getSuggestedCorrections().get(0).getWord());
 		
 		Assert.assertEquals("diseas", suggestions.get(1).getOriginalString());
 		Assert.assertEquals(1, suggestions.get(1).getSuggestedCorrections().size());
 		Assert.assertEquals("disease", suggestions.get(1).getSuggestedCorrections().get(0).getWord());
 		
 	}
 	
 	@Test
 	public void testMultipleSuggestions() throws IOException {
 		SpellcheckResponse spellcheckResponse = SpellcheckRequester.getSpellcheckResponse(English, 15, "diabetis");
 		
 		List<Suggestion> suggestions = spellcheckResponse.getSpellcheck().getSuggestions();
 		
 		Assert.assertEquals(1, suggestions.size());
 		Assert.assertEquals("diabetis", suggestions.get(0).getOriginalString());
 		Assert.assertEquals(15, suggestions.get(0).getSuggestedCorrections().size());
 		
 		// "diabetes" should have the highest score
 		Assert.assertEquals("diabetes", suggestions.get(0).getSuggestedCorrections().get(0).getWord());
 		Assert.assertEquals(15, suggestions.get(0).getNumFound());
 		Assert.assertEquals(0, suggestions.get(0).getStartOffset());
 		Assert.assertEquals(8, suggestions.get(0).getEndOffset());
		Assert.assertTrue(suggestions.get(0).getOrigFreq() >= 0);
 	}
 }
