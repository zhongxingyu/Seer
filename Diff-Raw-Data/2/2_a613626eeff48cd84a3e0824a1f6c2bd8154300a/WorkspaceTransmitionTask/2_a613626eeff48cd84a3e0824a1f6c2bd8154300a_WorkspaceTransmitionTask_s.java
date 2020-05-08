 package de.uniluebeck.itm.mdc.net;
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.widget.RemoteViews;
 
 import com.google.gson.Gson;
 
 import de.uniluebeck.itm.mdc.R;
 import de.uniluebeck.itm.mdc.TransferActivity;
 import de.uniluebeck.itm.mdc.net.ProgressStringEntity.ProgressListener;
 
 public class WorkspaceTransmitionTask extends AsyncTask<TransferRequest, Integer, Throwable> implements ProgressListener {
 	
 	private final Gson gson = new Gson();
 	
 	private final Context context;
 	
 	private final String url;
 	
 	private ProgressDialog progressDialog;
 	
 	private Notification notification;
 	
 	private final NotificationManager notificationManager;
 	
 	public WorkspaceTransmitionTask(Context context, String url) {
 		this.context = context;
 		this.url = url;
 		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 	}
 	
 	private void configureNotification() {
 		Intent intent = new Intent(context, TransferActivity.class);
         final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
 		
 		notification = new Notification(R.drawable.icon, "Uploading Workspace...", System.currentTimeMillis());
         notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
         notification.contentView = new RemoteViews(context.getPackageName(), R.layout.transfer_notification_progress);
         notification.contentIntent = pendingIntent;
         notification.contentView.setImageViewResource(R.id.transfer_notification_status_icon, R.drawable.ic_menu_save);
        notification.contentView.setTextViewText(R.id.transfer_notification_status_text, "simulation in progress");
         notification.contentView.setProgressBar(R.id.transfer_notification_status_progress, 100, 0, false);
         notificationManager.notify(R.id.transfer_notification_status_progress, notification);
 	}
 	
 	private void configureProgressDialog() {
 		progressDialog = new ProgressDialog(context);
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		progressDialog.setMessage("Sending Workspace...");
 		progressDialog.setCancelable(false);
 		progressDialog.show();
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		configureNotification();
 		configureProgressDialog();
 	}
 	
 	@Override
 	protected Throwable doInBackground(TransferRequest... requests) {
 		for (TransferRequest request : requests) {
 			String json = gson.toJson(request);
 			
 			try {
 				SimpleJsonClient.to(url).send(json, this);
 			} catch (ClientProtocolException e) {
 				return e;
 			} catch (IOException e) {
 				return e;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		int max = values[0];
 		int progress = values[1];
 		
 		progressDialog.setMax(max);
 		progressDialog.setProgress(progress);
 		notification.contentView.setProgressBar(R.id.transfer_notification_status_progress, max, progress, false);
 		notificationManager.notify(R.id.transfer_notification_status_progress, notification);
 	}
 
 	@Override
 	protected void onPostExecute(Throwable e) {
 		progressDialog.dismiss();
 		notificationManager.cancel(R.id.transfer_notification_status_progress);
 		if (e != null) {
 			showAlertDialog(e.getMessage() + ". Please retry...");
 		}
 	}
 	
 	private void showAlertDialog(String message) {
 		new AlertDialog.Builder(context)
 			.setTitle("Error")
 			.setMessage(message)
 			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			})
 			.show();
 	}
 	
 	@Override
 	public void onProgress(int progress, int size) {
 		publishProgress(size, progress);
 	}
 }
