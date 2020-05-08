 package com.sjsu.wascengine;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.SortedSet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.lowagie.text.DocumentException;
 /**
  * This is going to be the "main" class for the App Engine. It
  * is going to listen on a certain port for an Http Request and
  * should receive a pdf file. It is then going to be doing the
  * work in order to figure out the appropriate rubric scores and
  * return the final result as a JSON object.
  * 
  * @author Tim Stullich
  *
  */
 @SuppressWarnings("serial")
 public class WASC_EngineServlet extends HttpServlet 
 {
    private final int RUBRICS = 5;
    private final int WEIGHT_CATEGORIES = 2;
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException
 	{
	   //Gonna try and capture the file that was sent
 	   //through the request servlet
 	   try{
 	      ServletFileUpload upload = new ServletFileUpload();
 	      //Sets the MIME type to be JSON
 	      resp.setContentType("text/json");
 	      
	      JsonArray resultsArray = new JsonArray();
 	      FileItemIterator iterator = upload.getItemIterator(req);
 	      
 	      while (iterator.hasNext())
	      {
 	         FileItemStream item = iterator.next();
 	         InputStream stream = item.openStream();
	         //The work for the files will be done here
	         resultsArray.add(testKeywordAnalyzer(resp, stream, item.getName()));
 	      }
 	      resp.getOutputStream().print(resultsArray.toString());
 	   }
 	   catch (Exception e)
 	   {
 	      throw new ServletException();
 	   }
 	}
 	
	public JsonObject testKeywordAnalyzer(HttpServletResponse resp, InputStream fileStream, String filename) 
 	      throws FileNotFoundException, IOException, DocumentException
     {
         // Test readKeywordFile
         KeywordAnalyzer instance = new KeywordAnalyzer();
         instance.readKeywordFile("testfiles/keywords.txt");
         
         // Get a test pdf and parse the text
         ArrayList<String> text = PdfExtract.convertToText(fileStream);
         instance.parseText(text);
         
         // Print a detailed report about the file
         int totOne = 0, totTwo = 0, total = 0, swap;
 
         //Create A JSON Object that will hold all the results
         JsonObject results = new JsonObject();
         results.addProperty("fileName", filename);
         
         int[][] wordCounts = instance.getWordCounts();
         SortedSet<String>[][] sets = instance.getKeywordsUsed();
         
         //Calculates Some General Statistics
         for (int i = 0; i < RUBRICS; ++i)
         {
             for (int j = 0; j < WEIGHT_CATEGORIES; ++j)
             {
                 if (j == 0)
                     totOne += wordCounts[i][j];
                 else
                     totTwo += wordCounts[i][j];
                 total += wordCounts[i][j];
             }
         }
         
         results.addProperty("totalKeywords", total);
         results.addProperty("weight1Keywords", totOne);
         results.addProperty("weight2Keywords", totTwo);
         results.addProperty("totalWords", instance.getTotalWords());
         
         //Scores for each individual rubric will be calculated here
         double[] scores = instance.calculateScores();
         
         /*This JSON array will hold JsonObjects that contain
          *more information on each rubric including scores,
          *weigths and the frequency of certain keywords.
          */
         JsonArray rubricScores = new JsonArray();
         for (int i = 0; i < RUBRICS; ++i)
         {
            JsonObject rubricScore = new JsonObject(); 
            rubricScore.addProperty("rubric" + (i + 1) + "score", scores[i]);
             for (int j = 0; j < 2; ++j)
             {
                rubricScore.addProperty("weight" + (j + 1) + "WordsUsed", wordCounts[i][j]);
                //Counts the frequency of each word based in each weight class
                JsonArray wordFrequency = new JsonArray();
                 for (String word : sets[i][j])
                 {
                     swap = instance.getKeywordOccurrences(word);
                     total += swap;
                     JsonObject aWord = new JsonObject();
                     //Add the word with the calculated frequency
                     aWord.addProperty(word, swap);
                     wordFrequency.add(aWord);
                 }
                 rubricScore.add("words"+ (j+1) + "Frequency", wordFrequency);
             }
             rubricScores.add(rubricScore);
         }
         results.add("rubricScores", rubricScores);
         return results;
     }
 }
