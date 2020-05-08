 package com.github.dansmithy.sanjuan;
 
 import static com.github.dansmithy.sanjuan.json.JsonMatchers.containsJson;
 import static com.github.dansmithy.sanjuan.json.JsonMatchers.whenTranslatedBy;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.github.dansmithy.sanjuan.driver.DefaultValues;
 import com.github.dansmithy.sanjuan.driver.MapBuilder;
 import com.github.dansmithy.sanjuan.driver.RequestValues;
 import com.github.dansmithy.sanjuan.driver.TranslatedValues;
 
 public class TestMatchers {
 
 	@Test
 	public void testReplaceValue() {
 
		TranslatedValues translatedValues = new TranslatedValues(MapBuilder.simple().add("alice", "alice-123").build());
 		String actual = "{ 'players' : [ { 'name' : 'alice-123', victoryPoints: 30, 'ignore' : 'this' } ] }";
 		
 		String expectedGame = "{ 'players^name' : [ { 'name' : '#alice', victoryPoints: 30 } ] }";
 		
 		Assert.assertThat(actual, containsJson(expectedGame, whenTranslatedBy(translatedValues)));
 		
 	}
 	
 	@Test
 	public void testRequestCreate() {
 		TranslatedValues translatedValues = new TranslatedValues();
 		RequestValues values = new RequestValues().addAll(DefaultValues.USER).addReadableData("username : #danny, password : testPassword");
 		RequestValues newValues = translatedValues.translateRequestValues(values);
 		System.out.println(newValues.toJson());
 		
 		RequestValues values2 = new RequestValues().addReadableData("user : #danny");
 		RequestValues values3 = translatedValues.translateRequestValues(values2);
 		System.out.println(values3.toJson());
 		
 	}	
 	
 
 }
