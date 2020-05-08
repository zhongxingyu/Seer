 package ch.bergturbenthal.image.provider.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import android.app.Notification;
 import android.app.Notification.Builder;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.os.Handler;
 import ch.bergturbenthal.image.data.model.state.Progress;
 import ch.bergturbenthal.image.provider.model.dto.ServerStateDto;
 
 public class ProgressNotification {
   protected static final String SERVICE_TAG = "ProgressNotification";
   private final Handler contextHandler;
 
   private final Collection<String> visibleProgress = new HashSet<String>();
   private final NotificationManager notificationManager;
   private final Context context;
 
   public ProgressNotification(final Context context) {
     this.context = context;
     contextHandler = new Handler(context.getMainLooper());
     notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
   }
 
   /**
    * poll all servers
    * 
    * @param connections
    */
   public synchronized void pollServerStates(final HashMap<String, ArchiveConnection> connections) {
     if (connections == null)
       return;
     final Collection<ServerStateDto> serverStates = new ArrayList<ServerStateDto>();
     for (final ArchiveConnection connection : connections.values()) {
       serverStates.addAll(connection.collectServerStates());
     }
 
     final Map<String, Notification.Builder> newBuilders = new HashMap<String, Notification.Builder>();
     for (final ServerStateDto serverStateDto : serverStates) {
       final String serverName = serverStateDto.getServerName();
       for (final Progress progress : serverStateDto.getServerState().getProgress()) {
 
         final Notification.Builder builder = new Notification.Builder(context);
         builder.setContentTitle(serverName + ": " + progress.getType().name() + " : " + progress.getProgressDescription());
         builder.setSmallIcon(android.R.drawable.ic_dialog_info);
         builder.setAutoCancel(false);
         builder.setContentText(progress.getCurrentStepDescription());
         builder.setProgress(progress.getStepCount(), progress.getCurrentStepNr(), progress.getStepCount() == 0);
         newBuilders.put(progress.getProgressId(), builder);
       }
     }
     contextHandler.post(new Runnable() {
 
       @Override
       public void run() {
         synchronized (ProgressNotification.this) {
           final Collection<String> notificationsToRemove = new HashSet<String>(visibleProgress);
           notificationsToRemove.removeAll(newBuilders.keySet());
           for (final String removeKey : notificationsToRemove) {
            notificationManager.cancel(removeKey, 0);
           }
           for (final Entry<String, Builder> builderEntry : newBuilders.entrySet()) {
            notificationManager.notify(builderEntry.getKey(), 0, builderEntry.getValue().getNotification());
           }
           visibleProgress.clear();
           visibleProgress.addAll(newBuilders.keySet());
         }
       }
     });
   }
 }
