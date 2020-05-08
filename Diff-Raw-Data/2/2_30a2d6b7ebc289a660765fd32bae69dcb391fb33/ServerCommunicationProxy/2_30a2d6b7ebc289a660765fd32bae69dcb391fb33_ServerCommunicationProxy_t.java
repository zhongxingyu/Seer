 package epfl.sweng.servercomm;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicHttpResponse;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import epfl.sweng.authentication.SessionManager;
 import epfl.sweng.cache.CacheManager;
 import epfl.sweng.globals.Globals;
 import epfl.sweng.quizquestions.QuizQuestion;
 
 /**
  * 
  * @author cyril
  *
  */
 public class ServerCommunicationProxy extends ServerCommunication implements IServerCommunication  {
 	
	private IServerCommunication mServerCommunication = new ServerCommunication();
 	
 	public HttpResponse execute(HttpUriRequest request) throws ClientProtocolException, IOException {
 		
 		String url = request.getRequestLine().getUri();
 		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_NOTFOUND, "Not found");
 		
 		try {
 			
 			if (SessionManager.getInstance().isOnline()) {
 				response = mServerCommunication.execute(request);
 				cacheData(request, response, url);
 			} else {			
 				if (url.equals(Globals.RANDOM_QUESTION_URL)) {
 					response = loadRandomQuestion();
 				} else if (url.equals(Globals.SUBMIT_QUESTION_URL)) {
 					response = submitQuestion(request);
 					cacheQuestion(response);
 				} else if (url.endsWith("rating") && request instanceof HttpGet) {
 					response = getRating(request);
 				} else if (url.endsWith("rating") && request instanceof HttpPost) {
 					response = cacheVerdict(response, request);
 				} else if (url.endsWith("ratings")) {
 					response = getRatings(request);
 				}
 			}
 		} catch (JSONException e) {
 			
 		}
 		return response;
 	}
 	
 	private void cacheData(HttpUriRequest request, HttpResponse response, String url)
 		throws ClientProtocolException, JSONException, IOException {
 
 		if (url.equals(Globals.RANDOM_QUESTION_URL)) {
 			cacheQuestion(response);
 		} else if (url.equals(Globals.SUBMIT_QUESTION_URL)) {
 			cacheQuestion(response);
 		} else if (url.endsWith("rating")) {
 			cacheVerdict(response, request);
 		} else if (url.endsWith("ratings")) {
 			cacheRatings(response, request);
 		}
 	}
 
 	private HttpResponse getRatings(HttpUriRequest request) throws JSONException, UnsupportedEncodingException {
 		
 		String url = request.getRequestLine().getUri();
 		
 		url = url.replace("https://sweng-quiz.appspot.com/quizquestions/", "");
 		url = url.replace("/ratings", "");
 		
 		int id = Integer.parseInt(url);
 		
 		
 		for (QuizQuestion question : CacheManager.getInstance().getCachedQuestions()) {
 			if (question.getId() == id) {
 				HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_OK, "Ok");
 				JSONObject responseJSON = new JSONObject();
 				responseJSON.put("likeCount", question.getLikeCount());
 				responseJSON.put("dislikeCount", question.getDislikeCount());
 				responseJSON.put("incorrectCount", question.getIncorrectCount());
 				response.setEntity(new StringEntity(responseJSON.toString()));
 				return response;
 			}
 		}
 		return new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_NOTFOUND, "Not found");
 	}
 
 	private HttpResponse getRating(HttpUriRequest request) throws JSONException, UnsupportedEncodingException {
 		
 		String url = request.getRequestLine().getUri();
 		
 		url = url.replace("https://sweng-quiz.appspot.com/quizquestions/", "");
 		url = url.replace("/rating", "");
 		
 		int id = Integer.parseInt(url);
 		
 		String verdict ="";
 		boolean found = false;
 		
 		for (QuizQuestion question : CacheManager.getInstance().getCachedQuestions()) {
 			if (question.getId() == id) {
 				verdict = question.getVerdict();
 				found = true;
 			}
 		}	
 		if (found) {
 			if (verdict.equals("")) {
 				return new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_NOCONTENT, "No Content");
 			} else {
 				HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_OK, "Ok");
 				JSONObject responseJSON = new JSONObject();
 				responseJSON.put("verdict", verdict);
 				response.setEntity(new StringEntity(responseJSON.toString()));
 				return response;
 			}
 		} else {
 			return new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_NOTFOUND, "Not found");
 		}
 		
 	}
 	
 
 	private HttpResponse submitQuestion(HttpUriRequest request) throws ParseException, JSONException, IOException {
 		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_CREATED, "Created");
 		JSONObject questionJSON = new JSONObject(ContentHelper.getPostContent((HttpPost) request));
 		
 		int freeQuestionId=0;
 		for (QuizQuestion question : CacheManager.getInstance().getCachedQuestions()) {
 			freeQuestionId = Math.max(freeQuestionId, question.getId());
 		}
 		freeQuestionId++;
 		questionJSON.put("id", freeQuestionId);
 		QuizQuestion question = new QuizQuestion(questionJSON);
 		CacheManager.getInstance().addCachedQuestionToSubmit(question);
 		CacheManager.getInstance().addCachedQuestion(question);
 		response.setEntity(new StringEntity(question.getJSONString()));
 		return response;
 	}
 
 	private HttpResponse loadRandomQuestion() throws UnsupportedEncodingException, JSONException {
 		
 		if (CacheManager.getInstance().getCachedQuestions().size() > 0) {
 			
 			HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_OK, "OK");
 			
 			response.setEntity(new StringEntity(
 					CacheManager.getInstance().getRandomQuestion().getJSONString()));
 			return response;
 		} else {
 			return new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_NOTFOUND, "No Questions cached");
 		}
 	}
 
 	private void cacheRatings(HttpResponse response, HttpUriRequest request) 
 		throws ParseException, JSONException, IOException {
 		
 		String url = request.getRequestLine().getUri();
 		
 		url = url.replace("https://sweng-quiz.appspot.com/quizquestions/", "");
 		url = url.replace("/ratings", "");
 		
 		int id = Integer.parseInt(url);
 		
 		JSONObject ratings =  new JSONObject(ContentHelper.getResponseContent(response));	
 		
 		
 		for (QuizQuestion question : CacheManager.getInstance().getCachedQuestions()) {
 			if (question.getId() == id) {
 				question.setVerdictStats(ratings);
 			}
 		}
 	}
 
 
 	private HttpResponse cacheVerdict(HttpResponse response, HttpUriRequest request) 
 		throws ParseException, JSONException, IOException {
 		
 		String url = request.getRequestLine().getUri();
 		
 		url = url.replace("https://sweng-quiz.appspot.com/quizquestions/", "");
 		url = url.replace("/rating", "");
 		
 		int id = Integer.parseInt(url);
 		
 		JSONObject verdict =  new JSONObject();
 		if (request instanceof HttpPost) {
 			verdict =  new JSONObject(ContentHelper.getPostContent((HttpPost) request));
 		} else {
 			verdict = new JSONObject(ContentHelper.getResponseContent(response));
 		}
 		
 
 		for (QuizQuestion question : CacheManager.getInstance().getCachedQuestions()) {
 			if (question.getId() == id) {
 				String oldverdict = question.getVerdict();
 				question.setVerdictAndUpdateStats(verdict);
 				
 				if (!SessionManager.getInstance().isOnline()) {
 					CacheManager.getInstance().addVerdictToSubmit(question);
 				}
 				
 				if (oldverdict.equals("")) {
 					response = new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_CREATED, "Created");
 					JSONObject responseJSON = new JSONObject();
 					responseJSON.put("verdict", question.getVerdict());
 					response.setEntity(new StringEntity(responseJSON.toString()));
 					return response;
 				} else {
 					response = new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_OK, "OK");
 					JSONObject responseJSON = new JSONObject();
 					responseJSON.put("verdict", question.getVerdict());
 					response.setEntity(new StringEntity(responseJSON.toString()));
 					return response;
 				}
 					
 			}
 		}
 		return new BasicHttpResponse(HttpVersion.HTTP_1_1, Globals.STATUSCODE_NOTFOUND, "Not found");
 	}
 
 	private void cacheQuestion(HttpResponse response) throws ClientProtocolException, JSONException, IOException {
 		QuizQuestion newQuestion = new QuizQuestion(ContentHelper.getResponseContent(response));
 		
 		for (QuizQuestion question : CacheManager.getInstance().getCachedQuestions()) {
 			if (question.getId() == newQuestion.getId()) {
 				return;
 			}
 		}
 		
 		CacheManager.getInstance().addCachedQuestion(newQuestion);
 	}
 
 	
 }
