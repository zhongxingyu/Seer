 /**
  * 
  */
 package ch.zhaw.mdp.lhb.citr.com.rest.facade;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 import android.os.AsyncTask;
 import android.util.Log;
 import ch.zhaw.mdp.lhb.citr.activities.CitrBaseActivity;
 import ch.zhaw.mdp.lhb.citr.com.rest.RESTBackgroundTask;
 import ch.zhaw.mdp.lhb.citr.dto.GroupDTO;
 import ch.zhaw.mdp.lhb.citr.exceptions.CitrCommunicationException;
 import ch.zhaw.mdp.lhb.citr.exceptions.CitrExceptionTypeEnum;
 import ch.zhaw.mdp.lhb.citr.rest.IRGroupServices;
 import ch.zhaw.mdp.lhb.citr.util.PropertyHelper;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 /**
  * @author Daniel Brun
  * 
  *         Client implementation of the Service-Interface
  *         {@link IRGroupServices}.
  */
 public class ClientIRGroupServicesImpl implements IRGroupServices {
 
 	public static final String TAG = "ClientIRGroupServicesImpl";
 
 	private CitrBaseActivity activity;
 	private RESTBackgroundTask restTask;
 	private ObjectMapper mapper;
 
 	/**
 	 * Creates a new instance of this class.
 	 * 
 	 * @param anActivity
 	 *            The underlining activity.
 	 */
 	public ClientIRGroupServicesImpl(CitrBaseActivity anActivity) {
 		activity = anActivity;
 
 		mapper = new ObjectMapper();
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.mdp.lhb.citr.rest.IRGroupServices#createGroup(ch.zhaw.mdp.lhb.citr.dto.GroupDTO)
 	 */
 	@Override
 	public boolean createGroup(GroupDTO aGroup) {
 		if (aGroup == null) {
 			throw new IllegalArgumentException(
 					"The argument aGroup must not be null");
 		}
 
 		preInit(RESTBackgroundTask.HTTP_POST_TASK);
 
         int mode = 1;
         if (aGroup.isPublicGroup()) {
             mode = 0;
         }
 
 		try {
 			restTask.addParameter("group", mapper.writeValueAsString(aGroup));
 		} catch (JsonProcessingException e) {
 			Log.e(TAG, "Exception during JSON serialization prcoess.", e);
 			throw new CitrCommunicationException(
 					"Exception during JSON serialization prcoess.", e,
 					CitrExceptionTypeEnum.SERIALIZATION_ERROR);
 		}
 
 		StringBuffer url = new StringBuffer();
 		url.append(PropertyHelper.get("rest.service.group"));
 		url.append("create");
 
 		String result = execute(url.toString());
 
 		Log.e(TAG, "TEST: " + result);
 
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.mdp.lhb.citr.rest.IRGroupServices#getAllGroups()
 	 */
 	@Override
 	public List<GroupDTO> getAllGroups() {
 		throw new NotImplementedException();
 	}
 
 	/**
 	 * Performs the pre initialization.
 	 * 
 	 * @param aHttpType
 	 *            The http type.
 	 */
 	private void preInit(int aHttpType) {
 		restTask = new RESTBackgroundTask(activity);
 		restTask.setHttpRequestType(aHttpType);
 	}
 
 	/**
 	 * Executes the prepared call with the given url.
 	 * 
 	 * @param aUrl
 	 *            The url to call.
 	 * @return the result string.
 	 */
 	private String execute(String aUrl) {
 		if (aUrl == null) {
 			throw new IllegalArgumentException(
 					"The argument aUrl must not be null!");
 		}
 
 		String result = null;
 		try {
 			AsyncTask<String, Integer, String> task = restTask.execute(aUrl);
 			if (task != null) {
 				result = task.get();
 			}
 		} catch (InterruptedException e) {
 			Log.e(TAG, "Exception during task processing", e);
 			throw new CitrCommunicationException(
 					"Exception during task processing", e,
 					CitrExceptionTypeEnum.BACKGROUND_ERROR);
 		} catch (ExecutionException e) {
 			Log.e(TAG, "Exception during task processing", e);
 			throw new CitrCommunicationException(
 					"Exception during task processing", e,
 					CitrExceptionTypeEnum.BACKGROUND_ERROR);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Mapps the given string to an object.
 	 * 
 	 * @param aResult
 	 *            The string to map.
 	 * @return the mapped object.
 	 */
 	private GroupDTO map(String aResult) {
 		GroupDTO group = null;
 
 		if (aResult != null && aResult.trim().length() > 0) {
 			try {
 				group = mapper.readValue(aResult,
 						new TypeReference<GroupDTO>() {
 						});
 			} catch (JsonParseException e) {
 				Log.e(TAG, "Exception during parse process of JSON-Data", e);
 				throw new CitrCommunicationException(
 						"Exception during parse process of JSON-Data", e,
 						CitrExceptionTypeEnum.DESERIALIZATION_ERROR);
 			} catch (JsonMappingException e) {
 				Log.e(TAG, "Exception during map process of JSON-Data", e);
 				throw new CitrCommunicationException(
 						"Exception during map process of JSON-Data", e,
 						CitrExceptionTypeEnum.DESERIALIZATION_ERROR);
 			} catch (IOException e) {
 				Log.e(TAG, "Exception during io process of JSON-Data", e);
 				throw new CitrCommunicationException(
 						"Exception during io process of JSON-Data", e,
 						CitrExceptionTypeEnum.DESERIALIZATION_ERROR);
 			}
 		}
 		return group;
 	}
 
 }
