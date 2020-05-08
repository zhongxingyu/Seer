package org.lenition.singleton;
 
 import com.google.gson.Gson;
 import org.lenition.domain.Factbook;
 
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.Map;
 
 public enum Questionnaire {
 
     INSTANCE;
     Factbook factbook;
 
     Map<String, Integer> categoryWeights = new HashMap<String, Integer>() {{
         put("area", 5);
         put("population", 5);
         put("gdpPerCapita", 5);
         put("healthExp", 5);
         put("gini", 5);
     }};
 
     public String getQuiz(int numberOfQuestions) {
         Gson gson = new Gson();
 
         Reader reader = new InputStreamReader(Questionnaire.class.getClassLoader().getResourceAsStream("factbook-countries.json"));
         factbook = gson.fromJson(reader, Factbook.class);
 
         StringBuilder json = new StringBuilder();
         int q = 0;
 
 
         while(q < numberOfQuestions) {
 
             q++;
         }
 
 
         return json.toString();
     }
 
 }
