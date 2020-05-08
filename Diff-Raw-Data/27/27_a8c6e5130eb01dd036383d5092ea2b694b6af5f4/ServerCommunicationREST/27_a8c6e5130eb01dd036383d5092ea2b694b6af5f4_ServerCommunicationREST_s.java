 /**
  * 
  */
 package de.fhb.mobile.ToDoListAndroidApp.communication;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.conn.ConnectTimeoutException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import de.fhb.mobile.ToDoListAndroidApp.communication.unmarshalling.TodoUnmarshaller;
 import de.fhb.mobile.ToDoListAndroidApp.models.Todo;
 import de.fhb.mobile.ToDoListAndroidApp.persistance.TodoDatabase;
 
 /**
  * This class implements the REST commincation.
  * 
  * @author Patrick
  * 
  */
 public class ServerCommunicationREST implements IServerCommunicationREST {
 
 	private static final String HOST_NAME = "10.0.2.2";
 
 	private static final String PORT = "8080";
 	private static final String PROJECT = "ToDoListAndroidServer";
 	private static final String HOST_ADDRESS = "http://" + HOST_NAME + ":"
 			+ PORT + "/" + PROJECT;
 	private static final String REST_FOLDER = "/api/rest/";
 	private static final String SERVER_REST_ADRESS = HOST_ADDRESS + REST_FOLDER;
 
 	@SuppressWarnings("finally")
 	@Override
 	public int authentification(String username, String password) {
 		String url = SERVER_REST_ADRESS + "authenticate";
 		JSONObject json;
 		int isAuthenticate = 0;
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 		nameValuePairs.add(new BasicNameValuePair("username", username));
 		nameValuePairs.add(new BasicNameValuePair("password", password));
 		try {
 			json = this.sendRequest(url, "POST", nameValuePairs);
 			if ((Boolean) json.get("isAuthenticate"))
 				isAuthenticate = 1;
 			else
 				isAuthenticate = 0;
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			if (e instanceof ConnectTimeoutException)
 				isAuthenticate = -1;
 			else
 				isAuthenticate = -2;
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			return isAuthenticate;
 		}
 	}
 
 	@SuppressWarnings("finally")
 	@Override
	public boolean synchronize() {
 		boolean isSynchronize = false;
 		JSONObject todoListJson = new JSONObject();
 		JSONObject json;
 		String url = SERVER_REST_ADRESS + "synchronize";
 		
		TodoDatabase db = new TodoDatabase();
 		db.open();
 		
 		List<Todo> todoList = db.getAllTodos(null);
 		try {
 			// TODO todoliste einfach in die schleife packen dann baut er das
 			// json fr die auslieferung
 			JSONArray jsonArray = new JSONArray();
 			for (Todo t : todoList) {
 				JSONObject object = new JSONObject();
 				object.put("id", t.getId());
 				object.put("description", t.getDescription());
 				object.put("name", t.getName());
 				object.put("expire", t.getExpireDate().getTime().getTime());
 				object.put("lastChange", t.getLastUpdated());
 				object.put("favourite", t.isFavorite());
 				object.put("finished", t.isFinished());
 				// object.put("user", "{}");
 				object.put("contacts", t.getContacts());
 				jsonArray.put(object);
 			}
 			todoListJson.put("list", jsonArray);
 
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 			// todoListJson.put("list", todoList);
 			nameValuePairs.add(new BasicNameValuePair("todoListJson",
 					todoListJson.toString()));
 			Log.i("#", todoListJson.toString());
 			json = this.sendRequest(url, "POST", nameValuePairs);
 			
 			todoList = TodoUnmarshaller.unmarshallList(json.getJSONArray("list"));
 			
 			//TODO sync list in die database speichern
 			todoList = db.synchronize(todoList);
 			
 			isSynchronize = (Boolean) json.get("isSynchronize");
 		} catch (ClientProtocolException e) {
 			Log.e("exception", e.getMessage());
 		} catch (IllegalStateException e) {
 			Log.e("exception", e.getMessage());
 		} catch (IOException e) {
 			Log.e("exception", e.getMessage());
 		} catch (JSONException e) {
 			Log.e("exception", e.getMessage());
 		} finally {
 			db.close();
 			return isSynchronize;
 		}
 	}
 
 	@SuppressWarnings("finally")
 	@Override
 	public List<Todo> getAllTodo() {
 		List<Todo> todoList = new ArrayList<Todo>();
 		HttpParams params = new BasicHttpParams();
 		JSONObject todoListJson = new JSONObject();
 		JSONObject json;
 		String url = SERVER_REST_ADRESS + "synchronize";
 		params.setParameter("todoListJson", todoListJson);
 
 		try {
 			json = this.sendRequest(url, "POST", null);
 
 			// TODO marschaller
 
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			return todoList;
 		}
 	}
 
 	@Override
 	public void addTodo(Todo todo) {
 		HttpParams params = new BasicHttpParams();
 		JSONObject todoJson = new JSONObject();
 		String url = SERVER_REST_ADRESS + "add";
 
 		// TODO todoJson marshaller
 
 		params.setParameter("todoJson", todoJson);
 
 		try {
 			this.sendRequest(url, "POST", null);
 
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void deleteTodo(Todo todo) {
 		String url = SERVER_REST_ADRESS + "delete/" + todo.getId();
 
 		try {
 			this.sendRequest(url, "DELETE", null);
 
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * send a request to the url and give a JSONObject of the REsponse.
 	 * 
 	 * @param url
 	 * @param method
 	 * @return Response in a JSONObject.
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws IllegalStateException
 	 * @throws JSONException
 	 */
 	private JSONObject sendRequest(String url, String method,
 			List<NameValuePair> params) throws ClientProtocolException,
 			IOException, IllegalStateException, JSONException {
 		HttpClient httpClient = new DefaultHttpClient();
 		httpClient.getParams()
 				.setIntParameter("http.connection.timeout", 30000);
 
 		HttpResponse response = httpClient.execute(createHttpRequest(url,
 				method, params));
 		return new JSONObject(responseHandle(response));
 	}
 
 	/**
 	 * create a Request for the given method and url.
 	 * 
 	 * @param url
 	 * @param method
 	 * @return Request Object to execute in a HttpClient
 	 * @throws UnsupportedEncodingException
 	 */
 	private HttpUriRequest createHttpRequest(String url, String method,
 			List<NameValuePair> params) throws UnsupportedEncodingException {
 		HttpUriRequest request;
 		if (method.toUpperCase().equals("GET")) {
 			request = new HttpGet(url);
 		} else if (method.toUpperCase().equals("POST")) {
 			HttpPost post = new HttpPost(url);
 			post.setEntity(new UrlEncodedFormEntity(params));
 			request = post;
 		} else if (method.toUpperCase().equals("PUT")) {
 			request = new HttpPut(url);
 		} else if (method.toUpperCase().equals("DELETE")) {
 			request = new HttpDelete(url);
 		} else {
 			request = new HttpGet();
 		}
 
 		return request;
 	}
 
 	/**
 	 * handle the response and get the responseContent.
 	 * 
 	 * @param response
 	 * @return ResponseString
 	 * @throws IllegalStateException
 	 * @throws IOException
 	 */
 	private static String responseHandle(HttpResponse response)
 			throws IllegalStateException, IOException {
 		HttpEntity entity = response.getEntity();
 		if (entity != null) {
 			InputStream inputStream = entity.getContent();
 			return ServerCommunicationREST.convertStreamToString(inputStream);
 		} else
 			return "";
 	}
 
 	/**
 	 * converting the response inputstream to stream. it reads the inputstream
 	 * and put it in to a string.
 	 * 
 	 * @param inputStream
 	 * @return String of a Inputstream
 	 */
 	private static String convertStreamToString(InputStream inputStream) {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				inputStream));
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				inputStream.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return sb.toString();
 	}
 }
