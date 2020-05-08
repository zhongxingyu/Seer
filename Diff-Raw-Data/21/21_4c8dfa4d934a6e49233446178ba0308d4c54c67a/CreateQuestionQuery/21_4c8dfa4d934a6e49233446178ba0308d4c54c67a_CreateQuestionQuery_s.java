 package com.columbia.server;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 
 import android.content.Intent;
 import android.os.Bundle;
 
 public class CreateQuestionQuery extends ServerQuery {
 	
 	Intent intent;
 	String query;
 	int interactionType;
 	String question;
 	String answer;
 	String questId;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		intent = getIntent();
 		question = intent.getStringExtra("question");
 		answer = intent.getStringExtra("answer");
 		questId = intent.getStringExtra("questId");
 		interactionType = intent.getIntExtra("interactionType",0);
 		this.execute();
 	}
 	
 	public void run() {
		question = question.replaceAll(" ","&nbsp;");
		answer = answer.replaceAll(" ", "&nbsp;");
 		query = address + "/addquestion/?questid=" + questId + "&type=OpenQuestion&sentence=" + question + "&longitude=-73&latitude=40&difficulty=1&answer=" + answer + "&username=" + ServerHelper.email + "&password=" + ServerHelper.password;
 		
 		HttpResponse response = null;
 		if (ServerQuery.GET == interactionType) {
 			response = get();
 		}
 		else if (ServerQuery.POST == interactionType) {
 			response = post();
 		}
 		HttpEntity entity = response.getEntity();
 		String sResponse = null;
 		try {
 			sResponse = ServerHelper.getResponseBody(entity);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		
 		intent.putExtra("xml", sResponse);
     	setResult(0,intent);
     	finish();
 	}
 
 	@Override
 	public HttpResponse get() {		// get quests
 		HttpGet httpGet = new HttpGet(query);
 		try {
 			return httpClient.execute(httpGet);
 		} catch (ClientProtocolException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	@Override
 	public HttpResponse post() { // check answer
 		return null;
 	}
 
 }
