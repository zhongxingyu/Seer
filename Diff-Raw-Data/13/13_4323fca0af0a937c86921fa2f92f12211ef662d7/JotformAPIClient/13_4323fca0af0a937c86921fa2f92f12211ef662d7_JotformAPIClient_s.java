 package com.jotform.api;
 
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 import org.json.*;
 
 import android.util.Log;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 public class JotformAPIClient {
 
 	private static final String BASE_URL = "http://api.jotform.com/";
 	private static final String VERSION = "v1/";
 	private AsyncHttpClient client;
 	private String apiKey;
 
 	public static final String TAG = "Jotform API Client";
 
 	public JotformAPIClient(String apiKey) {
 		client = new AsyncHttpClient();
 		this.apiKey = apiKey;
 	}
 
 	public JotformAPIClient() {
 		client = new AsyncHttpClient();
 		this.apiKey = "";
 	}
 
 	public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
 
 		client.addHeader("apiKey", apiKey);
 		client.get(getAbsoluteUrl(url), params, responseHandler);
 	}
 
 	public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
 
 		client.addHeader("apiKey", apiKey);
 		client.post(getAbsoluteUrl(url), params, responseHandler);
 	}
 
 	public void delete(String url, AsyncHttpResponseHandler responseHandler) {
 
 		client.addHeader("apiKey", apiKey);		
 		client.delete(getAbsoluteUrl(url), responseHandler);
 	}
 
 	public void put(String url, JSONObject params, AsyncHttpResponseHandler responseHandler) {
 
 		client.addHeader("apiKey", apiKey);
 		client.addHeader("Content-Type", "application/json");
 		
 		try {
 			
 			StringEntity s = new StringEntity(params.toString());
     	    s.setContentEncoding((Header) new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
     	    HttpEntity entity = s;
     	    
     	    client.put(null, getAbsoluteUrl(url), entity, null, responseHandler);
     	    
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private String getAbsoluteUrl(String relativeUrl) {
 
 		return BASE_URL + VERSION + relativeUrl;
 	}
 
 	private RequestParams createHistoryQuery(String action, String date, String sortBy, String startDate, String endDate) {
 
 		RequestParams args = new RequestParams();
 		args.put("action", action);
 		args.put("date", date);
 		args.put("sortBy", sortBy);
 		args.put("startDate", startDate);
 		args.put("endDate", endDate);
 
 		return args;
 	}
 		
 	/**
 	 * Login user with given credentials
 	 * @param credentials Username, password, application name and access type of user
 	 * @return Returns logged in user's settings and app key
 	 */
 	public void login(HashMap<String, String> userinfo, AsyncHttpResponseHandler responseHandler){
 
 		RequestParams params = new RequestParams();
 		
 		Set<String> keys = userinfo.keySet();
 		
 		for(String key: keys) {
 			params.put(key, userinfo.get(key));
 		}
 
 		post("user/login", params, responseHandler);
 	}
 
     
 	/**
 	 * Register with username, password and email
 	 * @param userDetails Username, password and email to register a new user
 	 * @return Returns new user's details
 	 */
 	public void registerUser(HashMap<String, String> userDetails, AsyncHttpResponseHandler responseHandler) {
 		
 		RequestParams params = new RequestParams();
 		
 		Set<String> keys = userDetails.keySet();
 		
 		for(String key: keys) {
 			params.put(key, userDetails.get(key));
 		}
 		
 		post("user/register", params, responseHandler);
 	}
 
 	
 	public void getForms(AsyncHttpResponseHandler responseHandler) {
 		get("user/forms", null, responseHandler);
 	}
 
 
 	public void getSubmissions(AsyncHttpResponseHandler responseHandler) {
 
 		get("user/submissions", null, responseHandler);
 	}
 
 	public void getSubmissions(
 			Integer limit,
 			String orderBy,
 			JSONObject filter,
 			AsyncHttpResponseHandler responseHandler){
 
 		RequestParams params = new RequestParams();
 
 		if (limit != null) {
 			params.put("limit", String.valueOf(limit));
 		}
 
 		if (orderBy != null) {
 			params.put("order_by", orderBy);
 		}
 
 		// Make sure all filter parameters and values are String formatted as :
 		// filter = {
 		// 		"id": "236344132991249332",
 		// 		"form_id": "31564842891967",
 		// 		"ip": "176.42.170.199",
 		// 		"created_at": "2013-06-06 12:08:52",
 		// 		"status": "ACTIVE",
 		// 		"new": "0",
 		// 		"flag": "0",
 		// 		"updated_at": "2013-06-24 08:17:44"
 		// }
 		
 		if (filter != null) {
 			
 			Iterator<String> keys = filter.keys();
 			while (keys.hasNext()) {
 
 				String key = keys.next();
 
 				try {
 					filter.put(key, String.valueOf(filter.getJSONObject(key)));
 				} catch (org.json.JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 			params.put("filter", filter.toString());
 		}
 
 		Log.d(TAG, "url :" + "user/submissions");
 		Log.d(TAG, "params : " + params.toString());
 
 		get("user/submissions", params, responseHandler);
 	}
 
 	public void getFormSubmissions(long formId,
 			AsyncHttpResponseHandler responseHandler) {
 		RequestParams params = new RequestParams();
 		params.put("qid_enabled", "true");
 		get("form/" + String.valueOf(formId) + "/submissions", params,
 				responseHandler);
 	}
 
 	public void getFormSubmissions(
 			long formId,
 			Integer limit,
 			String orderBy,
 			JSONObject filter,
 			AsyncHttpResponseHandler responseHandler) {
 
 		try {
 			filter.put("form_id", formId);
 		} catch (JSONException e) {
 
 		}
 
 		getSubmissions(limit, orderBy, filter, responseHandler);
 	}
 
 	/**
 	 * Get user account details for a JotForm user.
 	 * @return Returns user account type, avatar URL, name, email, website URL and account limits.
 	 */
 	public void getUser(AsyncHttpResponseHandler responseHandler) {
 		get("user", null, responseHandler);
 	}
 
 	/**
 	 * Get number of form submissions received this month.
 	 * @return Returns number of submissions, number of SSL form submissions, payment form submissions and upload space used by user.
 	 */
 	public void getUsage(AsyncHttpResponseHandler responseHandler) {
 		get("user/usage", null, responseHandler);
 	}
 
 	/**
 	 * Get a list of sub users for this account.
 	 * @return Returns list of forms and form folders with access privileges.
 	 */
 	public void getSubUsers(AsyncHttpResponseHandler responseHandler) {
 		get("user/subusers", null, responseHandler);
 	}
 
 	/**
 	 * Get a list of form folders for this account.
 	 * @return Returns name of the folder and owner of the folder for shared folders.
 	 */
 	public void getFolders(AsyncHttpResponseHandler responseHandler) {
 
 		get("user/folders", null, responseHandler);
 	}
 	
 	/**
      * Get folder details
      * @param folderID You can get a list of folders from /user/folders.
      * @return Returns a list of forms in a folder, and other details about the form such as folder color.
      */
 	public void getFolder(long folderId, AsyncHttpResponseHandler responseHandler) {
 		
 		get("folder/" + String.valueOf(folderId), null, responseHandler);
 	}
 	
 	/**
 	 * List of URLS for reports in this account.
 	 * @return Returns reports for all of the forms. ie. Excel, CSV, printable charts, embeddable HTML tables.
 	 */
 	public void getReports(AsyncHttpResponseHandler responseHandler) {
 
 		get("user/reports", null, responseHandler);
 	}
     
     /**
 	 * Create new report of a form with intended fields, type and title.
 	 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @title title is report title.
 	 * @list_type You can specify report type. 'csv', 'excel', 'grid', 'table', 'rss'
 	 * @fields you can specify fields, User IP, submission date(dt) and question IDs
 	 * @return Report details and URL.
 	 */
 	
 	public void createReport(long formId, String title, String list_type, String fields, AsyncHttpResponseHandler responseHandler) {
 		
 		RequestParams params = new RequestParams();
         
 		params.put("title", title);
 		params.put("list_type", list_type);
 		params.put("fields", fields);
         
 		post("form/" + String.valueOf(formId) + "/reports", params, responseHandler);
 	}
 
 	/**
 	 * Get user's settings for this account.
 	 * @return Returns user's time zone and language.
 	 */
 	public void getSettings(AsyncHttpResponseHandler responseHandler) {
 
 		get("user/settings", null, responseHandler);
 	}
 
 	/**
      * Update user's settings
      * @param settings New user setting values with setting keys
      * @return Returns changes on user settings.
      */
     public void updateSettings(HashMap<String, String> settings, AsyncHttpResponseHandler responseHandler) {
     	
     	RequestParams params = new RequestParams();
 
 		Set<String> keys = settings.keySet();
 		
 		for(String key: keys) {
 			params.put(key, settings.get(key));
 		}
     	
     	post("user/settings", params, responseHandler);
     }
     
 	/**
 	 * Get user activity log.
 	 * @return Returns activity log about things like forms created/modified/deleted, account logins and other operations.
 	 */
 	public void getHistory(AsyncHttpResponseHandler responseHandler) {
 
 		get("user/history", null, responseHandler);
 	}
 
 	/**
 	 * Get user activity log.
 	 * @param action Filter results by activity performed. Default is 'all'.
 	 * @param date Limit results by a date range. If you'd like to limit results by specific dates you can use startDate and endDate fields instead.
 	 * @param sortBy Lists results by ascending and descending order.
 	 * @param startDate Limit results to only after a specific date. Format: MM/DD/YYYY.
 	 * @param endDate Limit results to only before a specific date. Format: MM/DD/YYYY.
 	 * @return Returns activity log about things like forms created/modified/deleted, account logins and other operations.
 	 */
 	public void getHistory(String action, String date, String sortBy, String startDate, String endDate, AsyncHttpResponseHandler responseHandler) {
 
 		RequestParams params = this.createHistoryQuery(action, date, sortBy, startDate, endDate);
 
 		get("user/history", params, responseHandler);
 	}
 
 	/**
 	 * Get basic information about a form.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @return Returns form ID, status, update and creation dates, submission count etc.
 	 */
 	public void getForm(long formId, AsyncHttpResponseHandler responseHandler) {
 
 		get("form/" + String.valueOf(formId), null, responseHandler);
 	}
 
 	/**
 	 * Get a list of all questions on a form.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @return Returns question properties of a form.
 	 */
 	public void getFormQuestions(long formId, AsyncHttpResponseHandler responseHandler) {
 
 		get("form/" + String.valueOf(formId) + "/questions", null, responseHandler);
 	}		
 
 	/**
 	 * Get details about a question
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param qid Identifier for each question on a form. You can get a list of question IDs from /form/{id}/questions.
 	 * @return Returns question properties like required and validation.
 	 */
 	public void getFormQuestion(long formId, long qid, AsyncHttpResponseHandler responseHandler) {
 
 		get("form/" + String.valueOf(formId) + "/question/" + qid, null, responseHandler);
 	}
 
 	/**
 	 * Submit data to this form using the API.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param submission Submission data with question IDs.
 	 * @return Returns posted submission ID and URL.
 	 */
 	public void createFormSubmissions(long formId, HashMap<String, String> submission, AsyncHttpResponseHandler responseHandler) {

 		RequestParams parameters = new RequestParams();

 		Set<String> keys = submission.keySet();

 		for(String key: keys) {
 			if (key.contains("_")) {
 				parameters.put("submission[" + key.substring(0, key.indexOf("_")) + "][" + key.substring(key.indexOf("_") + 1) + "]", submission.get(key));
 			} else {
 				parameters.put("submission[" + key + "]", submission.get(key));
 			}
 		}

		post("form/" + String.valueOf(formId), parameters, responseHandler);
 	}
 
 	/**
 	 * List of files uploaded on a form.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @return Returns uploaded file information and URLs on a specific form.
 	 */
 	public void getFormFiles(long formId, AsyncHttpResponseHandler responseHandler) {
 
 		get("form/" + String.valueOf(formId) + "/files", null, responseHandler);
 	}
 
 	/**
 	 * Get list of webhooks for a form
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @return Returns list of webhooks for a specific form.
 	 */
 	public void getFormWebhooks(long formId, AsyncHttpResponseHandler responseHandler) {
 
 		get("form/" + String.valueOf(formId) + "/webhooks", null, responseHandler);
 	}
 
 	/**
 	 * Add a new webhook
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param webhookURL Webhook URL is where form data will be posted when form is submitted.
 	 * @return Returns list of webhooks for a specific form.
 	 */
 	public void createFormWebhook(long formId, String webhookURL, AsyncHttpResponseHandler responseHandler) {
 
 		RequestParams params = new RequestParams();
 		params.put("webhookURL", webhookURL);
 
 		post("form/" + String.valueOf(formId) + "/webhooks", params, responseHandler);
 	}
 
     /**
      * Delete a specific webhook of a form.
      * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
      * @param webhookID You can get webhook IDs when you call /form/{formID}/webhooks.
      * @return Returns remaining webhook URLs of form.
      */
     public void deleteFormWebhook(long formId, long webhookId, AsyncHttpResponseHandler responseHandler) {
     	
     	delete("form/" + String.valueOf(formId) + "/webhooks/" + String.valueOf(webhookId), responseHandler);
     }
     
 	
 	/**
 	 * Get submission data
 	 * @param sid You can get submission IDs when you call /form/{id}/submissions.
 	 * @return Returns information and answers of a specific submission.
 	 */
 	public void getSubmission(long sid, AsyncHttpResponseHandler responseHandler) {
 
 		get("submission/" + String.valueOf(sid), null, responseHandler);
 	}
 
 	/**
 	 * Get report details
 	 * @param reportID You can get a list of reports from /user/reports.
 	 * @return Returns properties of a specific report like fields and status.
 	 */
 	public void getReport(long reportId, AsyncHttpResponseHandler responseHandler) {
 		get("report/" + String.valueOf(reportId), null, responseHandler);
 	}
 
 	/**
 	 * Get a list of all properties on a form.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @return Returns form properties like width, expiration date, style etc.
 	 */
 	public void getFormProperties(long formId, AsyncHttpResponseHandler responseHandler) {
 
 		get("form/" + String.valueOf(formId) + "/properties", null, responseHandler);
 	}
 
 	/**
 	 * Get a specific property of the form.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param propertyKey You can get property keys when you call /form/{id}/properties.
 	 * @return Returns given property key value.
 	 */
 	public void getFormProperty(long formId, String propertyKey, AsyncHttpResponseHandler responseHandler ) {
 
 		get("form/" + String.valueOf(formId) + "/properties/" + propertyKey, null, responseHandler);
 	}
 
 	/**
 	 * Delete a single submission.
 	 * @param sid You can get submission IDs when you call /user/submissions.
 	 * @return Returns status of request.
 	 */
 	public void deleteSubmission(long sid, AsyncHttpResponseHandler responseHandler) {
 
 		delete("submission/" + String.valueOf(sid), responseHandler);
 	}
 
 	/**
 	 * Edit a single submission.
 	 * @param sid You can get submission IDs when you call /form/{id}/submissions.
 	 * @param submission New submission data with question IDs.
 	 * @return Returns status of request.
 	 */
 	public void editSubmission(long sid, HashMap<String, String> submission, AsyncHttpResponseHandler responseHandler) {
 
 		RequestParams parameters = new RequestParams();
 
 		Set<String> keys = submission.keySet();
 
 		for(String key: keys) {
 			if (key.contains("_")) {
 				parameters.put("submission[" + key.substring(0, key.indexOf("_")) + "][" + key.substring(key.indexOf("_") + 1) + "]", submission.get(key));
 			} else {
 				parameters.put("submission[" + key + "]", submission.get(key));
 			}
 		}
 
 		post("submission/" + String.valueOf(sid), parameters, responseHandler);
 	}
 
 	/**
 	 * Clone a single form.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @return Returns status of request.
 	 */
 	public void cloneForm(long formId, AsyncHttpResponseHandler responseHandler) {
 
 		post("form/" + String.valueOf(formId) + "/clone", null, responseHandler);
 	}
 
 	/**
 	 * Delete a single form question.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param qid Identifier for each question on a form. You can get a list of question IDs from /form/{id}/questions.
 	 * @return Returns status of request.
 	 */
 	public void deleteFormQuestion(long formId, long qid, AsyncHttpResponseHandler responseHandler) {
 
 		delete("form/" + String.valueOf(formId) + "/question/" + String.valueOf(qid), responseHandler);
 	}
 
 	/**
 	 * Add new question to specified form.
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param question New question properties like type and text.
 	 * @return Returns properties of new question.
 	 */
 	public void createFormQuestion(long formId, HashMap<String, String> question, AsyncHttpResponseHandler responseHandler) {
 
 		RequestParams params = new RequestParams();
 
 		Set<String> keys = question.keySet();
 
 		for(String key: keys) {
 			params.put("question[" + key + "]", question.get(key));
 		}
 
 		post("form/" + String.valueOf(formId) + "/questions", params, responseHandler);
 	}
 
 	/**
 	 *  Add new questions to specified form.
 	 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param questions New question properties like type and text.
 	 * @return Returns properties of new questions.
 	 */
 	public void createFormQuestions(long formId, Map<?, ?> questions, AsyncHttpResponseHandler responseHandler) {
 
 		JSONObject question;
 		
 		try {
 			
 			question = (JSONObject) JsonHelper.toJSON(questions);
 			put("form/" + String.valueOf(formId) + "/questions", question, responseHandler);
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**
 	 * Edit a single question properties.
 	 * @param formId Form ID is the numbers you sonee on a form URL. You can get form IDs when you call /user/forms.
 	 * @param qid Identifier for each question  a form. You can get a list of question IDs from /form/{id}/questions.
 	 * @param questionProperties New question properties like text and order.
 	 * @return Returns edited property and type of question.
 	 */
 	public void editFormQuestion(long formId, long qid, HashMap<String, String> questionProperties, AsyncHttpResponseHandler responseHandler) {
 
 		RequestParams question = new RequestParams();
 
 		Set<String> keys = questionProperties.keySet();
 
 		for(String key: keys) {
 			question.put("question[" + key + "]", questionProperties.get(key));
 		}
 
 		post("form/" + String.valueOf(formId) + "/question/" + qid, question, responseHandler);
 	}
 
 	/**
 	 * Add or edit properties of a specific form
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param formProperties New properties like label width.
 	 * @return Returns edited properties.
 	 */
 	public void setFormProperties(long formId, HashMap<String, String> formProperties, AsyncHttpResponseHandler responseHandler) {
 
 		RequestParams properties = new RequestParams();
 
 		Set<String> keys = formProperties.keySet();
 
 		for(String key: keys) {
 			properties.put("properties[" + key + "]", formProperties.get(key));
 		}
 
 		post("form/" + String.valueOf(formId) + "/properties", properties, responseHandler);
 	}
 
 	/**
 	 * Add or edit properties of a specific form
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @param formProperties New properties like label width.
 	 * @return Returns edited properties.
 	 */
 	public void setMultipleFormProperties(long formId, Map<?, ?> params, AsyncHttpResponseHandler responseHandler) {
 
 		JSONObject param;
 		
 		try {
 			param = (JSONObject) JsonHelper.toJSON(params);
 			put("form/" + String.valueOf(formId) + "/properties", param, responseHandler);
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
      * Create a new form
      * @param form Questions, properties and emails of new form.
      * @return Returns new form.
      */
     public void createForm(Map form, AsyncHttpResponseHandler responseHandler) {
     	
 		RequestParams params = new RequestParams();
 		
 		Set<String> formKeys = form.keySet();
 		
 		for( String formKey: formKeys ) {
 			
 			if( formKey.equals("properties") ) {
 				
 				HashMap<String, String> properties = (HashMap<String, String>) form.get(formKey);
 				Set<String> propertyKeys = properties.keySet();
 				
 				for( String propertyKey : propertyKeys)
 					params.put(formKey + "[" + propertyKey + "]", properties.get(propertyKey));
 				
 			} else {
 				
 				Map formItem = (Map) form.get(formKey);
 				Set<String> formItemKeys = formItem.keySet();
 				
 				for(String formItemKey: formItemKeys) {
 					
 					HashMap<String, String> fi = (HashMap<String, String>) formItem.get(formItemKey);
 					
 					Set<String> fiKeys = fi.keySet();
 					
 					for(String fiKey: fiKeys)
 						params.put(formKey + "[" + formItemKey + "][" + fiKey + "]", fi.get(fiKey));
 					
 				}
 			}
 			
 		}
 		
     	post("user/forms", params, responseHandler);
     }
 
 	/**
 	 * Create a new form
 	 * @param form Questions, properties and emails of new form.
 	 * @return Returns new form.
 	 */
 	public void createForms(Map<?, ?> form, AsyncHttpResponseHandler responseHandler) {
 
 		JSONObject param;
 		
 		try {
 			
 			param = (JSONObject) JsonHelper.toJSON(form);
 			put("user/forms", param, responseHandler);
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 			
 	}
 
 
 	/**
 	 * Delete a single form
 	 * @param formId Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 	 * @return Properties of deleted form.
 	 */
 	public void deleteForm(long formId, AsyncHttpResponseHandler responseHandler) {
 
 		delete("form/" + String.valueOf(formId), responseHandler);
 	}
 	
 	
 }
