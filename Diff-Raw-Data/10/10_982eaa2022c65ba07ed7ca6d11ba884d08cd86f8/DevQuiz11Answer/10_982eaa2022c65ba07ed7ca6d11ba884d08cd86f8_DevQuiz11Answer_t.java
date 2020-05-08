 package net.kaoriya.android.apps.gddquiz;
 
 import java.util.List;
 
 import com.google.android.apps.gddquiz.IQuizService;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ServiceInfo;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.TextView;
 
 public class DevQuiz11Answer extends Activity
 {
     public static final String TAG = "DevQuiz11";
 
     private ServiceConnection conn;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         PackageManager pm = getPackageManager();
         List<PackageInfo> pkgs = pm.getInstalledPackages(
                 PackageManager.GET_SERVICES);
         for (PackageInfo pkg : pkgs) {
             String pn = pkg.packageName;
             if (pn.indexOf("gdd") >= 0) {
                 Log.v(TAG, "Package: " + pn);
                 if (pkg.services != null) {
                     for (ServiceInfo service : pkg.services) {
                         Log.v(TAG, "  Serivce: " + service.name);
                         Log.v(TAG, "    " + service.toString());
                     }
                 }
             }
         }
 
         connect("com.google.android.apps.gddquiz.gddquiz11service",
                 "com.google.android.apps.gddquiz.gddquiz11service.DevQuiz11Service");
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (this.conn != null) {
             unbindService(this.conn);
             this.conn = null;
         }
     }
 
     private void connect(String packageName, String className) {
         this.conn = new ServiceConnection() {
             @Override
             public void onServiceConnected(ComponentName name, IBinder service) {
                 setAnswer(IQuizService.Stub.asInterface(service));
             }
             @Override
             public void onServiceDisconnected(ComponentName name) {
             }
         };
 
         Intent service = new Intent();
         service.setClassName(packageName, className);
         bindService(service, this.conn, BIND_AUTO_CREATE);
     }
 
     private void setAnswer(IQuizService quiz) {
         try {
            String answer = quiz.getCode();
            TextView text = (TextView)findViewById(R.id.AnswerBox);
            text.setText(answer);
            Log.v(TAG, "answer=" + answer);
         } catch (Exception e) {
            Log.w(TAG, e);
         }
     }
 
 }
