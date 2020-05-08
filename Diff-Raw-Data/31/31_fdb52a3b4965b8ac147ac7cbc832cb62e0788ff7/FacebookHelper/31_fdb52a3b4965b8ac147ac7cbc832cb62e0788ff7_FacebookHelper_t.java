 package gecko.mybaby.model;
 
 import gecko.mybaby.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import com.facebook.*;
 import com.facebook.model.GraphObject;
 import com.facebook.model.GraphUser;
 import com.facebook.widget.*;
 
 public class FacebookHelper {
 	
 	public Activity activity;
 	
 	private static final String PERMISSION = "publish_actions";
 	private PendingAction pendingAction = PendingAction.NONE;
 	private boolean canPresentShareDialog;
 	private GraphUser user;
 	
 	private enum PendingAction {
         NONE,
         POST_STATUS_UPDATE
     }
 	
 	private UiLifecycleHelper uiHelper;
 
     private Session.StatusCallback callback = new Session.StatusCallback() {
         @Override
         public void call(Session session, SessionState state, Exception exception) {
             onSessionStateChange(session, state, exception);
         }
     };
 
     private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
         @Override
         public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
             Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
         }
 
         @Override
         public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
             Log.d("HelloFacebook", "Success!");
         }
     };
 
 	public FacebookHelper(Activity activity) {
 		this.activity = activity;
 		
 		uiHelper = new UiLifecycleHelper(this.activity, callback);
 		
 		this.canPresentShareDialog = true;
 	}
 	
 	public void setUser(GraphUser user){
 		this.user = user;
 	}
 	
	public GraphUser getUser(){
		return this.user;
	}
	
 	public void loadUser(){
 	
 	}
 	
 	public void postMyBabyLink(){
 		Session session = Session.getActiveSession();
         boolean sessionResult = (session != null && session.isOpened());
         
         
 		if(this.user!=null && sessionResult){
 			Log.v("Sucesso", "Usuario carregado com sucesso");
 			performPublish(PendingAction.POST_STATUS_UPDATE, this.canPresentShareDialog);
 		}else
 			Log.v("Erro", "Erro ao carregar usuario");
 	}
 	
 	public void getActivitySession(int requestCode, int resultCode, Intent data){
 		Session.getActiveSession().onActivityResult(this.activity, requestCode, resultCode, data);
 	}
 	
 	private interface GraphObjectWithId extends GraphObject {
         String getId();
     }
 
     private void onSessionStateChange(Session session, SessionState state, Exception exception) {
         if (pendingAction != PendingAction.NONE &&
                 (exception instanceof FacebookOperationCanceledException ||
                 exception instanceof FacebookAuthorizationException)) {
                 new AlertDialog.Builder(activity)
                     .setTitle(R.string.cancelled)
                     .setMessage(R.string.permission_not_granted)
                     .setPositiveButton(R.string.ok, null)
                     .show();
             pendingAction = PendingAction.NONE;
         } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
             handlePendingAction();
         }
     }
     
     public void performPublish(PendingAction action, boolean allowNoSession) {
         Session session = Session.getActiveSession();
         if (session != null) {
             pendingAction = action;
             if (hasPublishPermission()) {
                 // We can do the action right away.
                 handlePendingAction();
                 return;
             } else if (session.isOpened()) {
                 // We need to get new permissions, then complete the action when we get called back.
                 session.requestNewPublishPermissions(new Session.NewPermissionsRequest(activity, PERMISSION));
                 return;
             }
         }
 
         if (allowNoSession) {
             pendingAction = action;
             handlePendingAction();
         }
     }
     
     @SuppressWarnings("incomplete-switch")
     public void handlePendingAction() {
         PendingAction previouslyPendingAction = pendingAction;
         // These actions may re-set pendingAction if they are still pending, but we assume they
         // will succeed.
         pendingAction = PendingAction.NONE;
 
         switch (previouslyPendingAction) {
             case POST_STATUS_UPDATE:
                 postStatusUpdate();
                 break;
         }
     }
     
     private void postStatusUpdate() {
         if (canPresentShareDialog) {
             FacebookDialog shareDialog = createShareDialogBuilder().build();
             uiHelper.trackPendingDialogCall(shareDialog.present());
         } else if (user != null && hasPublishPermission()) {
         	final String mensagem = "Conhea o aplicativo para iPhone que vai te dar mais tempo para curtir seu filho.";
         	final String link = "http://itunes.apple.com/br/app/meu-bebe/id570007996?mt=8";
             final String message = String.format("%s \n%s", mensagem, link);
             Request request = Request
                     .newStatusUpdateRequest(Session.getActiveSession(), message, null, null, new Request.Callback() {
                         @Override
                         public void onCompleted(Response response) {
                             showPublishResult(message, response.getGraphObject(), response.getError());
                         }
                     });
             request.executeAsync();
         } else {
             pendingAction = PendingAction.POST_STATUS_UPDATE;
         }
     }
     
     private boolean hasPublishPermission() {
         Session session = Session.getActiveSession();
         return session != null && session.getPermissions().contains("publish_actions");
     }
     
     private FacebookDialog.ShareDialogBuilder createShareDialogBuilder() {
         return new FacebookDialog.ShareDialogBuilder(this.activity)
                 .setName("Meu Beb")
                 .setDescription("Conhea o aplicativo para iOS/Android que vai te dar mais tempo para curtir seu filho.")
                 .setLink("http://itunes.apple.com/br/app/meu-bebe/id570007996?mt=8")
                 .setApplicationName("Meu Beb");
     }
     
     private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
         String title = null;
         String alertMessage = null;
         if (error == null) {
         	title = "Muito obrigado por compartilhar o Meu Beb";
             alertMessage = String.format("Compartilhado com sucesso!");
         } else {
             title = "Erro";
             alertMessage = error.getErrorMessage();
         }
 
         new AlertDialog.Builder(activity)
                 .setTitle(title)
                 .setMessage(alertMessage)
                 .setPositiveButton(R.string.ok, null)
                 .show();
     }
 	
 }
