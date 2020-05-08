 /* 
  * Copyright (c) 2011 Accenture
  * Licensed under the MIT open source license
  * http://www.opensource.org/licenses/mit-license.php
  */
 package no.uka.findmyapp.android.rest.client;
 
 import java.io.Serializable;
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import no.uka.findmyapp.android.rest.client.RestMethod.HTTPStatusException;
 import no.uka.findmyapp.android.rest.contracts.UkaEvents.UkaEventContract;
 import no.uka.findmyapp.android.rest.datamodels.core.Credentials;
 import no.uka.findmyapp.android.rest.datamodels.core.ServiceModel;
 import no.uka.findmyapp.android.rest.datamodels.enums.HttpType;
 import no.uka.findmyapp.android.rest.helpers.ContentHelper;
 
 import org.apache.http.HttpException;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 /**
  * The Class RestProcessor.
  */
 public class RestProcessor {
 	
 	/** The Constant debug. */
 	private static final String debug = "RestProcessor"; 
 	
 	private static final String sModelPackage = 
 		"no.uka.findmyapp.android.rest.datamodels.models.";
 	
 	/** The rest method. */
 	private RestMethod mRestMethod;
 	
 	/** The gson. */
 	private Gson mGson;
 	
 	/** The context. */
 	private Context mContext; 
 	
 	private Credentials mCredentials;
 	
 	/**
 	 * Instantiates a new rest processor.
 	 *
 	 * @param context the context
 	 */
 	public RestProcessor(Context context, Credentials credentials) {
 		Log.v(debug, "Inside RestProcessor creator");
 		
 		mRestMethod = new RestMethod(credentials);
 		mGson = new GsonBuilder().create();
 		mContext = context; 
 	}
 	
 	/**
 	 * Call rest.
 	 *
 	 * @param serviceModel the service model
 	 * @throws HttpException 
 	 * @throws HTTPStatusException 
 	 */
 	public void callRest(ServiceModel serviceModel, String userToken) {
 		Log.v(debug, "Inside callRest");
 		try {
 			switch(serviceModel.getHttpType()) {
 				case GET :
 				Serializable returnedObject;
 					returnedObject = executeAndParse(serviceModel, userToken);
 					saveAndReturnData(serviceModel, returnedObject);
 				break;
 				case POST :
 					Serializable postReturnedObject = executeAndParse(serviceModel, userToken);
 					saveAndReturnData(serviceModel, postReturnedObject);
 				break;
 			}
 		}
 		catch (HTTPStatusException e) {
 			sendIntentBroadcast(IntentMessages.BROADCAST_HTTP_STATUS_EXCEPTION, e);
 		}
 	}
 
 	private void saveAndReturnData(ServiceModel serviceModel,
 			Serializable returnedObject) {
 		if(serviceModel.getContentProviderUri() != null) {
 			try {
 				if(isInstanceOfJavaNativeType(Class.forName(serviceModel.getReturnType())) == false) {
 					prepareAndSendToContentProvider(serviceModel, returnedObject);
 				}
 			}
 			catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		if(serviceModel.getBroadcastNotification() != null) {
 			sendIntentBroadcast(serviceModel.getBroadcastNotification(), 
 					returnedObject);
 		}
 	}
 
 	private void prepareAndSendToContentProvider(ServiceModel serviceModel,
 			Serializable returnedObject) {
 		sendToContentProvider(Uri.parse(
 			serviceModel.getContentProviderUri().toString()),
 			returnedObject, serviceModel.getReturnType());
 	}
 	
 	/**
 	 * Execute and parse.
 	 *
 	 * @param serviceModel the service model
 	 * @return the serializable
 	 * @throws HTTPStatusException 
 	 * @throws Exception 
 	 */
 	private Serializable executeAndParse(ServiceModel serviceModel, String userToken) 
 			throws HTTPStatusException {
 		mRestMethod.setUri(createURI(serviceModel, userToken));
 		String response = httpRequest(serviceModel);
 			
 		Log.v(debug, "executeAndParse: Response " + response);
 		Serializable s = null;
 		
 		if(serviceModel.getReturnType() != null) {
 			String returnType = serviceModel.getReturnType();
 			if(returnType.indexOf(".") == -1) {
 				returnType = sModelPackage + returnType;
 			}
 			//no.uka.findmyapp.android.rest.datamodels.models
 			Class theClass;
 			try {
 				theClass = Class.forName(returnType);
 			} catch (ClassNotFoundException e) {
 				Log.e(debug, e.getMessage() 
 						+ " Returning data in String format!");
 				return response; 
 			}
 			new TypeToken<Object>(){};
 			Type t1 = TypeToken.get(theClass).getType();;
 			try {
 				s = parseFromJson(response, t1);
 			} catch (JSONException e) {
 				Log.e(debug, e.getMessage() 
 						+ " Returning data in String format!");
 				return response; 
 			}
 		}
 		else {
 			return response; 
 		}
 		
 		return s;
 	}
 	
 	private URI createURI(ServiceModel serviceModel, String userToken) {
 		String tempUri = serviceModel.getUri().toString().replace("?", "%s");
 		tempUri = String.format(tempUri, serviceModel.getParameters());
 		tempUri = tempUri + "?token=" + userToken; 
 		URI returURI;
 		try {
 			returURI = new URI(tempUri);
 		}
 		catch (URISyntaxException e) {
 			Log.e(debug, e.getMessage());
 			return null; 
 		}
 		return returURI; 
 	}
 
 	private String httpRequest(ServiceModel serviceModel)
 			throws HTTPStatusException {
 		String response = "";
 			Log.v(debug, "executeAndParse: ServiceModel " + serviceModel.toString());
 			try {
 				response = execute(serviceModel);
 			} catch (HTTPStatusException e) {
 				Log.v(debug, "executeAndParse: Exception " + e.getMessage());
 				throw e; 
 			}
 		return response;
 	}
 
 	private Serializable parseFromJson(String response, Type t1)
 			throws JSONException {
 		Serializable s;
 		if(response.substring(0,1).equals("[")) {
 			s = parseListFromJson(response, t1);
 		} else {
 			Log.v(debug, "executeAndParse: Is not list");
 			s = (Serializable)mGson.fromJson(response, t1);
 			Log.v(debug, "executeAndParse: Serializable " + s.toString());
 		}
 		return s;
 	}
 
 	private Serializable parseListFromJson(String response, Type t1)
 			throws JSONException {
 		Serializable s;
 		Log.v(debug, "executeAndParse: Is list");
 		JSONArray array = new JSONArray(response);
 		List<Serializable> list = new ArrayList<Serializable>();
 		for (int i = 0; i < array.length(); i++) {
 			list.add((Serializable)mGson.fromJson(array.get(i).toString(), t1));
 		}
 		s = (Serializable) list;
 		Log.v(debug, "executeAndParse: Serializable " + s.toString());
 		return s;
 	}
 	
 	private String execute(ServiceModel serviceModel) throws HTTPStatusException {
 		if(serviceModel.getHttpType() == HttpType.GET) {
 			return mRestMethod.get();
 		}
 		else if (serviceModel.getHttpType() == HttpType.POST) {
 			Gson gson = new Gson();
 			return mRestMethod.post(gson.toJson(serviceModel.getData()));
 		}
 		return null; 
 	}
 	
 	/**
 	 * Send to content provider.
 	 *
 	 * @param uri the uri
 	 * @param object the object
 	 * @param returnType the return type
 	 */
 	private void sendToContentProvider(Uri uri, Serializable object, String returnType) {
 		Log.v(debug, "sendToContentProvider: serializable object " + object.getClass().getName());
 
 		ContentResolver cr = mContext.getContentResolver();
 		
 		if(object instanceof List) {
 
 			Class theClass = null;
 			try {
 				theClass = Class.forName(returnType);
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			List<ContentValues> list = ContentHelper.getContentValuesList(object, theClass);
 			Log.v(debug, "parsing contentvalue array");
 			ContentValues[] cva = new ContentValues[list.size()];
 			for(ContentValues cv : list) {
 				cva[list.indexOf(cv)] = cv; 
 			}
 			Log.v(debug, cva.toString());
 			cr.bulkInsert( UkaEventContract.EVENT_CONTENT_URI, cva);
 		} else {
 			ContentValues cv = new ContentValues(ContentHelper.getContentValues(object)); 
 			cr.insert(uri, cv);
 		}
 	}
 	
 	private boolean isInstanceOfJavaNativeType(Serializable object) {
 		Log.v(debug, "Type of " + object.toString());
 		if(object instanceof String || object instanceof Integer) {
 			return true; 
 		}
 		Log.v(debug, "isInstanceOfJavaNativeType: false");
 		return false; 
 	}
 	
 	/**
 	 * Send intent broadcast.
 	 *
 	 * @param intentString the intent string
 	 * @param obj the obj
 	 */
 	private void sendIntentBroadcast(String intentString, Serializable obj) {
 		Log.v(debug, "sendIntentBroadcast: sending broadcast, object name " 
 				+ obj.getClass().getName());
 		Log.v(debug, "sendIntentBroadcast: broadcast sent, extradata identifier: " 
 				+ IntentMessages.BROADCAST_RETURN_VALUE_NAME);
 		Log.v(debug, "sendIntentBroadcast: Putting extra " + obj.toString());
 		Intent broadcastedIntent = new Intent(); 
 		broadcastedIntent.putExtra(IntentMessages.BROADCAST_RETURN_VALUE_NAME, obj);
 		broadcastedIntent.setAction(intentString);
 
 		
 		mContext.sendBroadcast(broadcastedIntent);
 	}
 }
