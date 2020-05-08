 package com.example.spanishtalk.questions;
 
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.view.View;
 import android.widget.TextView;
 
 import com.example.datasource.QuestionDataSource;
 import com.example.logic.CheckLogin;
 import com.example.logic.HttpApi;
 import com.example.logic.SpanishTalkAsyncTask;
 import com.example.tables.Question;
 
 
 public class FetchQuestions {
 	public static List<Question> questionList;
 	public boolean loading;
 	private TextView vListNotice;
 	
 	public FetchQuestions(final Activity t, final String url) {
 		new SpanishTalkAsyncTask<String>() {
 			@Override
 			protected HttpResponse doPost() {
 				HttpResponse response = HttpApi.getQuestions(url);
 				return response;
 			}
 			
 			@Override
 			protected void onSuccess(HttpResponse response) {
 				new GetTask().execute();
 			}
 			
 			
 			@Override
 			protected void showNoticeView() {
 				new CheckLogin(t);
 				
 				loading = true;
 				
 				vListNotice.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
 			}
 					
 		}.execute();
 	}
 	
 	public class GetTask extends AsyncTask<HttpResponse, Void, Void> {
 		@Override
 		protected Void doInBackground(HttpResponse... responses) {
 			questionList = QuestionDataSource.getQuestions(responses[0]);
 			return null;
 		}
 		
 	}
 }
