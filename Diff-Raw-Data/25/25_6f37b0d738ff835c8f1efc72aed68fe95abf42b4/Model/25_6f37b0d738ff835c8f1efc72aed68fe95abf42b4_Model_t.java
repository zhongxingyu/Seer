 package no.henning.restful.model;
 
 import java.lang.reflect.Type;
 
 import org.apache.http.client.methods.HttpUriRequest;
 import org.json.JSONException;
 
 import android.util.Log;
 import no.henning.restful.RestfulApplication;
 import no.henning.restful.callback.Callback;
 import no.henning.restful.converter.json.JsonParser;
 import no.henning.restful.converter.json.JsonWriter;
 import no.henning.restful.http.HttpRestClient;
 import no.henning.restful.http.builder.RestHttpRequestDetail;
 import no.henning.restful.http.callback.HttpRestClientResponseCallback;
 import no.henning.restful.http.status.HttpRestResponse;
 import no.henning.restful.model.interfaces.DefaultRestActions;
 import no.henning.restful.model.util.ModelUtil;
 import no.henning.restful.utils.CallbackHelper;
 import no.henning.restful.utils.GenericHelper;
 import no.henning.restful.utils.HttpHelper;
 
 public class Model implements DefaultRestActions {
 
 	public Model() {
 
 	}
 
 	@Override
 	public <T> void get(final Callback<T> callback) {
 		performRequest("GET", this, callback);
 	}
 
 	@Override
 	public void get() {
 		get(null);
 	}
 
 	@Override
 	public void get(Object id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public <T> void get(Object id, Callback<T> callback) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void save(Callback<Model> callback) {
 		performRequest("POST", this, callback);
 	}
 
 	@Override
 	public void save() {
 		save(null);
 	}
 
 	@Override
 	public void update(Callback<Model> callback) {
 		performRequest("PUT", this, callback);
 	}
 
 	@Override
 	public void update() {
 		update(null);
 	}
 
 	@Override
 	public void delete() {
 		delete(null);
 	}
 
 	@Override
 	public void delete(Object id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public <T> void delete(Object id, Callback<T> callback) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public <T> void delete(Callback<T> callback) {
 		performRequest("DELETE", this, callback);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> void performRequest(String httpVerb, Model body, final Callback<T> callback) {
 		RestHttpRequestDetail detail = new RestHttpRequestDetail(this, httpVerb);
 
 		HttpUriRequest request = detail.buildRequest();
 
 		final T that = (T) this;
 
 		HttpRestClient.request(request, new HttpRestClientResponseCallback() {
 
 			@Override
 			public void onDone(HttpRestResponse response) {
 				// TODO Auto-generated method stub
 				if (RestfulApplication.DEBUG)
 					Log.d("restful", "Response: " + response.getResponse());
 
				if (response != null && HttpHelper.isSuccessfulResponse(response)) {
 					if (RestfulApplication.DEBUG)
 						Log.d("restful", "Request was successful!");
 					parseResponse(response, callback);
 				} else {
					if (response != null) {
						if (RestfulApplication.DEBUG) {
							Log.d("restful", "Something happened with the request..");
							Log.d("restful", "" + response.getStatusCode() + ": " + response.getStatusReason());
						}
						response = parseResponseError(response);
					} else {
						response = HttpRestResponse.newBadConnectionResponse();
 					}
 					if (callback != null)
 						callback.onError(response);
 				}
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> void parseResponse(HttpRestResponse response, final Callback<T> callback) {
 		// If callback is null, let's assume we can update this model's
 		// values with the response and head on out of here!
 
 		if (callback == null) {
 			if (RestfulApplication.DEBUG)
 				Log.d("restful", "parseResponse: Callback was empty, trying to update model's values with response");
 
 			updateValues(response.getResponse());
 			return;
 		}
 
 		// Retrieves the type of the callback: Callback<Type>
 		Type callbackType = CallbackHelper.getCallbackType(callback);
 
 		if (GenericHelper.isCollection(callbackType) || GenericHelper.isArray(callbackType)) {
 			if (RestfulApplication.DEBUG)
 				Log.d("restful",
 						"parseResponse: Callback type was an array or a collection as return type, trying to parse");
 
 			try {
 				T parsedObjects = JsonParser.parse(response.getResponse(), callback);
 				callback.onSuccess(parsedObjects);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} else {
 			// If Callback is an object and can be cast to Model, try to
 			// update values
 			if (RestfulApplication.DEBUG)
 				Log.d("restful", "parseResponse: Callback type: " + callbackType);
 
 			if (((Class<?>) callbackType).isInstance(this)) {
 				if (RestfulApplication.DEBUG)
 					Log.d("restful", "parseResponse: Callback type was a single entity, trying to update values");
 				updateValues(response.getResponse());
 
 				callback.onSuccess((T) this);
 			}
 		}
 
 	}
 
 	private void updateValues(String json) {
 		try {
 			Model model = JsonParser.parse(json, this.getClass());
 			updateValues(model);
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private void updateValues(Model fromModel) {
 		ModelUtil.replaceValues(fromModel, this);
 	}
 
 	@Override
 	public String toString() {
 		return JsonWriter.from(this).toString();
 	}
 
 	public static HttpRestResponse parseResponseError(HttpRestResponse response) {
 		try {
 			ResponseError error = JsonParser.parse(response.getResponse(), ResponseError.class);
 			HttpRestResponse newResponse = new HttpRestResponse(response.getStatusCode(), response.getStatusReason(),
 					error.getError().getMessage());
 			return newResponse;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return response;
 		}
 	}
 
 }
