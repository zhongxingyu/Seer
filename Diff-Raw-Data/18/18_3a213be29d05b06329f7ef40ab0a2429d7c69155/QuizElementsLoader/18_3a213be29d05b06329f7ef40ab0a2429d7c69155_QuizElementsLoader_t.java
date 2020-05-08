 package com.quizmania.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.quizmania.entities.QuizElement;
 import com.quizmania.entities.QuizElementNames;
 
 public class QuizElementsLoader {
 
 	public static List<QuizElement> loadElementsFromLevel(String level,
 			Context activity) throws IOException {
 
 		StringBuilder fileContent = getQuizElementsDeclarationsFileContent(activity);		
 		List<QuizElement> levelElements = getQuizLevelElements(level,
 				fileContent);
 		fillAutocompleteSuggestionsWithAllElements(levelElements);
 		return levelElements;
 
 	}
 
 	private static List<QuizElement> getQuizLevelElements(String level,
 			StringBuilder fileContent) {
 		
 		List<QuizElement> allQuizElements= getAllQuizElementsFromFileContent(fileContent);		
 		List<QuizElement> filteredByLevelList = filterQuizElementsByLevel(level,allQuizElements);
 		
 		return filteredByLevelList ;
 						
 	}
 
 	
 
 	private static void fillAutocompleteSuggestionsWithAllElements(
 			List<QuizElement> allQuizElements) {
 			String language = UserConfig.getInstance().getLanguage();			
 			StaticGlobalVariables.quizSuggestions = new ArrayList<String>();
 			for(QuizElement quizElement : allQuizElements){
 				
 				StaticGlobalVariables.quizSuggestions.add(quizElement.getLanguageToNamesMap().get(language).getNames().get(0));	
 			}
 			Collections.sort(StaticGlobalVariables.quizSuggestions);							
 		
 	}
 
 	private static List<QuizElement> filterQuizElementsByLevel(String level,
 			List<QuizElement> allQuizElements) {
 		
 		List<QuizElement> filteredLevels = new ArrayList<QuizElement>();
 		for(QuizElement element : allQuizElements){
 			if(element.getLevels().contains(level))
 				filteredLevels.add(element);
 		}
 		return filteredLevels;
 		
 	}
 
 	private static List<QuizElement> getAllQuizElementsFromFileContent(StringBuilder fileContent) {
 		List<QuizElement> allQuizElements = new ArrayList<QuizElement>();
 		JsonElement element = new JsonParser().parse(fileContent.toString());
 		Gson gson = new Gson();
 		JsonArray values = element.getAsJsonArray();
 		Iterator<JsonElement> iterator = values.iterator();
 
 		while (iterator.hasNext()) {
 			JsonElement quizElementJsonElement = iterator.next();
 			QuizElement quizElement = gson.fromJson(quizElementJsonElement, QuizElement.class);		
 			quizElement.setLanguageToNamesMap(parseLanguageToNamesMap(gson, iterator, quizElementJsonElement));
 			System.out.println("Good parsing!" + quizElement);
 			allQuizElements.add(quizElement);		
 		}
 		return allQuizElements;
 	}
 
 	private static Map<String,QuizElementNames> parseLanguageToNamesMap(Gson gson,
 			Iterator<JsonElement> iterator, JsonElement quizElementJsonElement) {
 		JsonObject languageToMapJsonObject = getLanguateToNamesMapJsonObject(quizElementJsonElement);
 		Set<Entry<String,JsonElement>> languagueToNamesUnparsedMap = languageToMapJsonObject.entrySet();
 		
 		
 		Map<String, QuizElementNames> languageToNamesMap = createJavaMapFromJsonEntries(
 				gson, languagueToNamesUnparsedMap);
 		
 		return languageToNamesMap;
 	}
 
 	private static Map<String, QuizElementNames> createJavaMapFromJsonEntries(
 			Gson gson,
 			Set<Entry<String, JsonElement>> languagueToNamesUnparsedMap) {
 		Map<String,QuizElementNames> languageToNamesMap = new HashMap<String, QuizElementNames>();
 		for(Entry<String,JsonElement> languageToNameEntry : languagueToNamesUnparsedMap){
 			
 			String language = languageToNameEntry.getKey();
 			QuizElementNames quizElementNames = gson.fromJson(languageToNameEntry.getValue(), QuizElementNames.class);
 			languageToNamesMap.put(language, quizElementNames);
 		}
 		return languageToNamesMap;
 	}
 
 	private static JsonObject getLanguateToNamesMapJsonObject(
 			JsonElement quizElementJsonElement) {
 		JsonObject quizElementJsonObject = quizElementJsonElement.getAsJsonObject();
 		JsonObject languageToMapJsonObject = quizElementJsonObject.getAsJsonObject("languageToNamesMap");
 		return languageToMapJsonObject;
 	}
 
 	private static StringBuilder getQuizElementsDeclarationsFileContent(
 			Context activity) throws IOException {
 
 		AssetManager assetManager = activity.getAssets();
 		InputStream stream = assetManager.open("quiz_elements.json");
 		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream,"UTF-8"));
 		StringBuilder fileContent = new StringBuilder();
 		String line;
 		while ((line = reader.readLine()) != null) {
 			fileContent.append(line);
 		}
 		reader.close();
 		return fileContent;
 	}
 }
