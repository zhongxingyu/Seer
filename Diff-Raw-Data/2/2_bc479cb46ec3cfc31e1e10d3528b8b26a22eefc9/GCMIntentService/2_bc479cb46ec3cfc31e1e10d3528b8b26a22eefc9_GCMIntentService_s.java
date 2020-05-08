 package br.com.thiagopagonha.psnapi;
 
 import static br.com.thiagopagonha.psnapi.utils.CommonUtilities.REFRESH_FRIENDS;
 import static br.com.thiagopagonha.psnapi.utils.CommonUtilities.SENDER_ID;
 import static br.com.thiagopagonha.psnapi.utils.CommonUtilities.displayMessage;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import br.com.thiagopagonha.psnapi.gcm.ServerUtilities;
 import br.com.thiagopagonha.psnapi.model.FriendsDBHelper;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 /**
  * IntentService responsible for handling GCM messages.
  * 
  * AVISO:
  * 
  * Essa merda de classe tem que ficar no mesmo pacote da permissão do CD2_MESSAGE
  * declarada no AndroidManifest.xml !!!
  * 
  */
 public class GCMIntentService extends GCMBaseIntentService {
 
     private static final String TAG = "GCMIntentService";
 
     public GCMIntentService() {
         super(SENDER_ID);
     }
 
     @Override
     protected void onRegistered(Context context, String registrationId) {
         Log.i(TAG, "Device registered: regId = " + registrationId);
         displayMessage(context, getString(R.string.gcm_registered));
         ServerUtilities.register(context, registrationId);
     }
 
     @Override
     protected void onUnregistered(Context context, String registrationId) {
         Log.i(TAG, "Device unregistered");
         displayMessage(context, getString(R.string.gcm_unregistered));
         ServerUtilities.unregister(context, registrationId);
     }
 
     @Override
     protected void onMessage(Context context, Intent intent) {
         Log.i(TAG, "Received message");
         
         String playing = intent.getStringExtra("Playing");
         String psnId = intent.getStringExtra("PsnId");
         String avatarSmall = intent.getStringExtra("AvatarSmall");
         
         // -- Maquiagem do playing
         if(playing == null) {
         	playing = "Offline";
         } else if (playing.equals("")) {
         	playing = "Online";
         }
         
         String message = psnId + " is " + playing;
 
         // -- Mostra Mensagem no Log
         displayMessage(context, message);
         // -- Atualiza Informações do amigo
         if(!updateUserInfo(psnId,playing,avatarSmall) && !playing.startsWith("Last")) {
         	// -- Manda atualizar a view
         	context.sendBroadcast(new Intent(REFRESH_FRIENDS));
         	// -- Só gera notificação para o usuário caso não seja o mesmo jogo
         	generateNotification(context, message);
         }
 
     }
 
     private boolean updateUserInfo(String psnId, String playing, String avatarSmall) {
     	// -- Dicionário no SQLite
     	Log.d(TAG, "updateUserInfo");
     	FriendsDBHelper friendsDBHelper = new FriendsDBHelper(getApplicationContext());
 		boolean sameGame = friendsDBHelper.saveFriend(psnId, playing, avatarSmall);
 		friendsDBHelper.close();
 		return sameGame;
 	}
 
 	@Override
     protected void onDeletedMessages(Context context, int total) {
         Log.i(TAG, "Received deleted messages notification");
         String message = getString(R.string.gcm_deleted, total);
         displayMessage(context, message);
         // notifies user
         generateNotification(context, message);
     }
 
     @Override
     public void onError(Context context, String errorId) {
         Log.i(TAG, "Received error: " + errorId);
         displayMessage(context, getString(R.string.gcm_error, errorId));
     }
 
     @Override
     protected boolean onRecoverableError(Context context, String errorId) {
         // log message
         Log.i(TAG, "Received recoverable error: " + errorId);
         displayMessage(context, getString(R.string.gcm_recoverable_error,
                 errorId));
         return super.onRecoverableError(context, errorId);
     }
 
     /**
      * Issues a notification to inform the user that server has sent a message.
      */
     private static void generateNotification(Context context, String message) {
     	Log.d(TAG, "generateNotification");
     	
    	Intent notificationIntent = new Intent(context, FriendFragment.class);
     	PendingIntent contentIntent = PendingIntent.getActivity(context,
     	        0, notificationIntent,
     	        PendingIntent.FLAG_UPDATE_CURRENT);
 
     	NotificationManager notificationManager = (NotificationManager) context
     	        .getSystemService(Context.NOTIFICATION_SERVICE);
 
     	Resources res = context.getResources();
     	NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
 
     	builder.setContentIntent(contentIntent)
     	            .setSmallIcon(R.drawable.ic_launcher)
     	            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
     	            .setTicker(message)
     	            .setWhen(System.currentTimeMillis())
     	            .setAutoCancel(true)
     	            .setContentTitle(res.getString(R.string.app_name))
     	            .setContentText(message);
     	
     	Notification notification = builder.build();
 
     	notificationManager.notify(0, notification);
 
     }
 
 }
