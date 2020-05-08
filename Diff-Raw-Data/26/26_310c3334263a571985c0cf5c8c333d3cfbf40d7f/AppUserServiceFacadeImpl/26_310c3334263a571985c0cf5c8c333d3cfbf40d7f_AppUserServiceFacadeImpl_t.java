 package org.iplantc.de.client.services.impl;
 
 import org.iplantc.de.client.models.DEProperties;
 import org.iplantc.de.client.models.UserInfo;
 import org.iplantc.de.client.models.apps.AppGroup;
 import org.iplantc.de.client.services.AppUserServiceFacade;
 import org.iplantc.de.client.services.DEServiceFacade;
 import org.iplantc.de.client.services.converters.AppGroupListCallbackConverter;
 import org.iplantc.de.client.util.JsonUtil;
import org.iplantc.de.shared.ConfluenceException;
 import org.iplantc.de.shared.services.ConfluenceServiceFacade;
 import org.iplantc.de.shared.services.EmailServiceFacade;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import com.sencha.gxt.data.shared.SortDir;
 
 import java.util.List;
 
 /**
  * Provides access to remote services for operations related to analysis
  * submission templates.
  *
  * @author Dennis Roberts
  */
 public class AppUserServiceFacadeImpl implements AppUserServiceFacade {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
     public void getPublicAppGroups(AsyncCallback<List<AppGroup>> callback) {
 		String address = DEProperties.getInstance()
 				.getUnproctedMuleServiceBaseUrl()
 				+ "public-app-groups"; //$NON-NLS-1$
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppGroupListCallbackConverter(callback));
 	}
 
     @Override
     public void getAppGroups(AsyncCallback<List<AppGroup>> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl()
                 + "app-groups"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, new AppGroupListCallbackConverter(callback));
     }
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	public void getApps(String analysisGroupId, AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "get-analyses-in-group/" //$NON-NLS-1$
 				+ analysisGroupId;
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	@Override
 	public void getPagedApps(String analysisGroupId, int limit,
 			String sortField, int offset, SortDir sortDir,
 			AsyncCallback<String> asyncCallback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "get-analyses-in-group/" //$NON-NLS-1$
 				+ analysisGroupId + "?limit=" + limit + "&sortField="
 				+ sortField + "&sortDir=" + sortDir.toString() + "&offset="
 				+ offset;
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, asyncCallback);
 	}
 
 	/**
 	 *
 	 * @param appId
 	 * @param callback
 	 */
 	@Override
 	public void getDCDetails(String appId, AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "get-components-in-analysis/" + appId; //$NON-NLS-1$
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void getDataObjectsForApp(String analysisId,
 			AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance()
 				.getUnproctedMuleServiceBaseUrl()
 				+ "analysis-data-objects/"
 				+ analysisId;
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void publishToWorld(JSONObject application,
 			AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "make-analysis-public"; //$NON-NLS-1$
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.POST, address, application.toString());
 
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void getAppDetails(String id, AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance()
 				.getMuleServiceBaseUrl() + "app-details/" + id; //$NON-NLS-1$
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.GET, address);
 
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	@Override
 	public void rateApp(final String analysisId, final int rating,
 			final String appName, String comment, final String authorEmail,
 			final AsyncCallback<String> callback) {
 		// add comment to wiki page, then call rating service, then update avg
 		// on wiki page
 		final ConfluenceServiceFacade confluenceService = ConfluenceServiceFacade
 				.getInstance();
 		String username = UserInfo.getInstance().getUsername();
 		confluenceService.addComment(appName, rating, username, comment,
 				new AsyncCallback<String>() {
 					@Override
 					public void onSuccess(final String commentId) {
 						// wrap the callback so it returns the comment id on
 						// success
 						rateAnalysis(appName, analysisId, rating, commentId,
 								authorEmail, new AsyncCallback<String>() {
 									@Override
 									public void onSuccess(String result) {
 										callback.onSuccess(commentId);
 									}
 
 									@Override
 									public void onFailure(Throwable caught) {
 										callback.onFailure(caught);
 									}
 								});
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						callback.onFailure(caught);
 					}
 				});
 	}
 
 	/**
 	 * calls /rate-analysis and if that is successful, calls
 	 * updateDocumentationPage()
 	 */
 	private void rateAnalysis(final String appName, String analysisId,
 			int rating, final String commentId, final String authorEmail,
 			final AsyncCallback<String> callback) {
 		JSONObject body = new JSONObject();
 		body.put("analysis_id", new JSONString(analysisId)); //$NON-NLS-1$
 		body.put("rating", new JSONNumber(rating)); //$NON-NLS-1$
 		body.put("comment_id", new JSONNumber(Long.valueOf(commentId))); //$NON-NLS-1$
 
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "rate-analysis"; //$NON-NLS-1$
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.POST, address, body.toString());
 		DEServiceFacade.getInstance().getServiceData(wrapper,
 				new AsyncCallback<String>() {
 					@Override
 					public void onSuccess(String result) {
 						sendRatingEmail(appName, authorEmail);
						updateDocumentationPage(appName, result, this);
 						callback.onSuccess(result);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						callback.onFailure(caught);
 					}
 				});
 	}
 
 	private void sendRatingEmail(final String appName, final String emailAddress) {
 		EmailServiceFacade.getInstance().sendEmail(
 				org.iplantc.de.resources.client.messages.I18N.DISPLAY.ratingEmailSubject(appName),
 				org.iplantc.de.resources.client.messages.I18N.DISPLAY.ratingEmailText(appName),
 				"noreply@iplantcollaborative.org", emailAddress, //$NON-NLS-1$
 				new AsyncCallback<Void>() {
 					@Override
 					public void onSuccess(Void arg0) {
 					}
 
 					@Override
 					public void onFailure(Throwable arg0) {
 						// don't bother the user if email sending fails
 					}
 				});
 	}
 
    private void updateDocumentationPage(String appName, String avgJson, final AsyncCallback<?> callback) {
 		JSONObject json = JSONParser.parseStrict(avgJson).isObject();
 		if (json != null) {
 			Number avg = JsonUtil.getNumber(json, "avg"); //$NON-NLS-1$
 			int avgRounded = (int) Math.round(avg.doubleValue());
             ConfluenceServiceFacade.getInstance().updateDocumentationPage(appName, avgRounded,
                     new AsyncCallback<Void>() {
 
                         @Override
                         public void onFailure(Throwable caught) {
                            callback.onFailure(new ConfluenceException(caught));
                         }
 
                         @Override
                         public void onSuccess(Void result) {
                             // Do nothing intentionally
                         }
                     });
 		}
 	}
 
 	@Override
 	public void updateRating(final String analysisId, final int rating,
 			final String appName, final Long commentId, final String comment,
 			final String authorEmail, final AsyncCallback<String> callback) {
 		// update comment on wiki page, then call rating service, then update
 		// avg on wiki page
 		String username = UserInfo.getInstance().getUsername();
         ConfluenceServiceFacade.getInstance().editComment(appName, rating, username, commentId, comment,
                 new AsyncCallback<Void>() {
                     @Override
                     public void onSuccess(Void result) {
                         rateAnalysis(appName, analysisId, rating, String.valueOf(commentId),
                                 authorEmail, callback);
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         callback.onFailure(caught);
                     }
                 });
 	}
 
 	@Override
 	public void deleteRating(final String analysisId, final String toolName,
 			final Long commentId, final AsyncCallback<String> callback) {
 		// call rating service, then delete comment from wiki page
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "delete-rating"; //$NON-NLS-1$
 
 		JSONObject body = new JSONObject();
 		body.put("analysis_id", new JSONString(analysisId)); //$NON-NLS-1$
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.POST, address, body.toString());
 		DEServiceFacade.getInstance().getServiceData(wrapper,
 				new AsyncCallback<String>() {
 					@Override
 					public void onSuccess(String result) {
					    updateDocumentationPage(toolName, result, this);
 						if (commentId != null) {
 							try {
								removeComment(toolName, commentId, this);
 							} catch (Exception e) {
 								onFailure(e);
 							}
 						}
 
 						callback.onSuccess(result);
 
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						callback.onFailure(caught);
 					}
 				});
 	}
 
    private void removeComment(String toolName, long commentId, final AsyncCallback<?> callback) {
         ConfluenceServiceFacade.getInstance().removeComment(toolName, commentId,
                 new AsyncCallback<Void>() {
 
                     @Override
                     public void onFailure(Throwable caught) {
                        callback.onFailure(new ConfluenceException(caught));
                     }
 
                     @Override
                     public void onSuccess(Void result) {
                         // Do nothing intentionally
                     }
                 });
 	}
 
 	@Override
 	public void favoriteApp(String workspaceId, String analysisId, boolean fav,
 			AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "update-favorites";
 
 		JSONObject body = new JSONObject();
 		body.put("workspace_id", new JSONString(workspaceId));
 		body.put("analysis_id", new JSONString(analysisId));
 		body.put("user_favorite", JSONBoolean.getInstance(fav));
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.POST, address, body.toString());
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	@Override
 	public void appExportable(String analysisId, AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance()
 				.getUnproctedMuleServiceBaseUrl() + "can-export-analysis";
 
 		JSONObject body = new JSONObject();
 		body.put("analysis_id", new JSONString(analysisId));
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.POST, address, body.toString());
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	@Override
 	public void copyApp(String analysisId, AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "copy-template/" + analysisId;
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.GET, address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	@Override
     public void deleteAppFromWorkspace(String user, String fullUsername, List<String> analysisIds,
             AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() + "delete-workflow"; //$NON-NLS-1$
 
         JSONObject body = new JSONObject();
         body.put("analysis_ids", JsonUtil.buildArrayFromStrings(analysisIds)); //$NON-NLS-1$
         body.put("user", new JSONString(user)); //$NON-NLS-1$
         body.put("full_username", new JSONString(fullUsername)); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 body.toString());
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void searchApp(String search, AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "search-analyses?search=" //$NON-NLS-1$
 				+ URL.encodeQueryString(search);
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void publishWorkflow(String body, AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "update-workflow";
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.POST, address, body);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void editWorkflow(String workflowId, AsyncCallback<String> callback) {
 		String address = "org.iplantc.services.zoidberg.edit-workflow/" + workflowId; //$NON-NLS-1$
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	@Override
 	public void copyWorkflow(String workflowId, AsyncCallback<String> callback) {
 		String address = "org.iplantc.services.zoidberg.copy-workflow/" + workflowId; //$NON-NLS-1$
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 }
