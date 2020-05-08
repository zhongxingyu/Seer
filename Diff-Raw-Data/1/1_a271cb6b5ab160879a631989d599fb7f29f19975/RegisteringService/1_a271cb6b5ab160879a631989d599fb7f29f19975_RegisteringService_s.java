 package com.frca.vsexam.helper;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.frca.vsexam.entities.base.Exam;
 import com.frca.vsexam.network.HttpRequestBuilder;
 import com.frca.vsexam.network.NetworkInterface;
 import com.frca.vsexam.network.Response;
 import com.frca.vsexam.network.tasks.BaseNetworkTask;
 import com.frca.vsexam.network.tasks.TextNetworkTask;
 
 import org.apache.http.client.methods.HttpRequestBase;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 
 public class RegisteringService extends Service {
 
     public static final String EXTRA_ID = "id";
     public static final String EXTRA_PERFORM_NOW = "perform_now";
 
     public static final int REQUEST_TIME_DIFF_MS = 100;
 
     private Exam exam;
     private DataHolder dataHolder;
     private HttpRequestBase registerRequest;
 
     private Thread thread;
 
     private List<TextNetworkTask> tasks = new ArrayList<TextNetworkTask>();
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         int examId = intent.getIntExtra(EXTRA_ID, 0);
         Helper.appendLog("Registering process starting");
         if (examId != 0) {
             exam = (Exam) Exam.getFromFile(Exam.class, this, examId);
         }
 
         if (exam.isRegistered()) {
             Log.e(getClass().getName(), "ERROR: Exam is already registered");
         }
         dataHolder = DataHolder.getInstance(this);
         registerRequest = HttpRequestBuilder.getRegisterRequest(dataHolder, exam, true);
         thread = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     runRegistering();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         });
         thread.start();
 
         return START_STICKY;
     }
 
     private void runRegistering() throws InterruptedException {
         long startTime = System.currentTimeMillis();
         HttpRequestBase request = HttpRequestBuilder.getRegisterRequest(dataHolder, exam, true);
         Helper.appendLog("Preparing premature request");
         Response response = dataHolder.getNetworkInterface().execute(request, Response.Type.TEXT);
         Helper.appendLog("Premature request complete, code: " + String.valueOf(response.getStatusCode()) + ", headers:");
         Helper.appendLog(response.getHeadersString());
 
         long serverTime = response.getServerTime().getTime();
         long serverTimeToReg = exam.getRegisterStart().getTime() - serverTime;
         Helper.appendLog("Actual registering will start in " + String.valueOf(serverTimeToReg / 1000L));
         if (serverTimeToReg > 0) {
             long delay = System.currentTimeMillis() - startTime;
             long timeToReg = serverTimeToReg - delay;
             if (timeToReg > 5000) {
                 Thread.sleep(timeToReg - 5000);
             }
         } else
             Helper.appendLog("Request starting should start ASAP");
 
         Helper.appendLog("Actual request sending starting now");
 
 
         // now spam registering
         while(!exam.isRegistered()) {
             final TextNetworkTask task = new TextNetworkTask(this, registerRequest);
             task.setResponseCallback(new BaseNetworkTask.ResponseCallback() {
                 @Override
                 public void onSuccess(Response response) {
                     tasks.remove(task);
                     Helper.appendLog("Task finished, status code: " + String.valueOf(response.getStatusCode()) + ", headers:");
                     Helper.appendLog(response.getHeadersString());
                     // lets consider it for the time being as a way to know we were registered successfully
                     // it returns code 200, but it will be returned probably even if registration is unsuccessful
                     /*if (response.getStatusCode() == 302) {
                         Helper.appendLog("Probably registered");
                         onRegistered();
                         return;
                     }*/
 
                     Helper.appendLog("Not yet registered, returned code id: " + String.valueOf(response.getStatusCode()));
 
                     // TODO: find a way to properly detect wrong register
                 }
             });
 
             tasks.add(task);
             BaseNetworkTask.run(task);
             Helper.appendLog("Starting new task");
             Thread.sleep(REQUEST_TIME_DIFF_MS);
         }
     }
 
     private void onRegistered() {
         exam.setRegistered(true);
         Helper.appendLog("Handling after registered, stopping concurrent tasks");
         for (TextNetworkTask task : tasks) {
             if (task != null && task.getStatus() != AsyncTask.Status.FINISHED)
                 task.cancel(true);
         }
 
         stopSelf();
     }
 
     public static AlarmManager getAlarmManager(Context context) {
         return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
     }
 
     public static void setExamRegister(Context context, Exam exam) {
         NetworkInterface networkInterface = DataHolder.getInstance(context).getNetworkInterface();
         long targetInMillis = exam.getRegisterStart().getTime() - networkInterface.getLastServerLocalTimeDiff();
         targetInMillis -= 30 * 1000;
         long startInMillis = targetInMillis - System.currentTimeMillis();
 
         Helper.appendLog("Planing exam registering. Registering should start on `" +
             Helper.getDateOutput(targetInMillis, Helper.DateOutputType.DATE_TIME) +
         "`, which is in " + String.valueOf(startInMillis / 1000L) + " seconds");
 
         if (startInMillis < 0) {
             Helper.appendLog("Exam register time is too soon, registering should start right away.");
             sendPendingIntent(getAproxRegisterPI(context, exam, false));
         } else {
             Toast.makeText(context, "Exam will be registered in " + Helper.secondsCountdown(startInMillis, false), Toast.LENGTH_LONG).show();
             Helper.appendLog("Exam registering will start in " + Helper.secondsCountdown(startInMillis, false));
             getAlarmManager(context).set(AlarmManager.RTC_WAKEUP, targetInMillis, getAproxRegisterPI(context, exam, false));
         }
     }
 
     public static void cancelExamRegister(Context context, Exam exam) {
         PendingIntent pendingIntent = getAproxRegisterPI(context, exam, exam.getRegisterStart().before(new Date()));
         getAlarmManager(context).cancel(pendingIntent);
     }
 
     private static void sendPendingIntent(PendingIntent pendingIntent) {
         try {
             pendingIntent.send();
         } catch (PendingIntent.CanceledException e) {
             e.printStackTrace();
         }
     }
 
     private static PendingIntent getAproxRegisterPI(Context context, Exam exam, boolean performNow) {
         Intent intent = new Intent(context, RegisteringService.class);
         intent.putExtra(RegisteringService.EXTRA_ID, exam.getId());
         intent.putExtra(RegisteringService.EXTRA_PERFORM_NOW, performNow);
         return PendingIntent.getService(context, exam.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
     }
 }
