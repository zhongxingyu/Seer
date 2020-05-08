 package com.example.datasource;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 import com.example.lib.HttpPack;
 import com.example.tables.Question;
 
 
 public class QuestionDataSource
 {
 	private static QuestionDataSource datasource = null;
 	
 	private List<Question> data = null;
 	
 	private static int SIZE;
 	private static int TOTAL;
 	
 //	public static QuestionDataSource getInstance(Context context, String url)
 //	{
 //		if (datasource == null) {
 //			datasource = new QuestionDataSource(context, url);
 //		}
 //		return datasource;
 //	}
 	
 	public QuestionDataSource(Context context, String url)
 	{
 		JSONObject question, creator;
 		String username, time;
 		
 		HttpResponse response = HttpPack.sendRequest(context, url);
 		
 		if (response == null) {
 			return;
 		}
 		
 		JSONObject r = HttpPack.getJsonByResponse(response);
 		
 		data = new ArrayList<Question>(SIZE);
 		try {
 			JSONArray questions = r.getJSONArray("questions");
 			TOTAL = Integer.parseInt(r.getString("total"));
 			SIZE = questions.length();
 			
 			
			for (int i = 0 ; i <= SIZE; i++) {
 				Question qr = new Question();
 				question = questions.getJSONObject(i);
 				
 				qr.setID(Integer.parseInt(question.getString("id")));
 				qr.setTitle(question.getString("title"));
 				
 				creator = question.getJSONObject("creator");
 				username = creator.getString("username");
 				time = question.getString("created_at");
 				
 				qr.setCreatedAt(username + ", " + time.substring(0, 10));
 				
 				data.add(qr);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	 
 	
 	public int getSize()
 	{
 		return TOTAL;
 	}
 	
 
 	public List<Question> getData() throws IOException
 	{
 		List<Question> newList = new ArrayList<Question>();
 		if (data == null) {
 			return newList;
 		}
 		newList.addAll(data);
 		return newList;		
 	}
 
 }
