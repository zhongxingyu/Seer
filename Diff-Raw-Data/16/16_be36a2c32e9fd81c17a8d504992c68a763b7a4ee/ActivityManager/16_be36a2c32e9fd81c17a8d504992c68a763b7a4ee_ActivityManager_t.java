 package com.standny.ui;
 
 import java.lang.ref.SoftReference;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import android.app.Activity;
 
 public class ActivityManager {
 	//private Context context;
     
     private static ActivityManager activityManager;
      
     public static ActivityManager getActivityManager(){
             if(activityManager == null){
                     activityManager = new ActivityManager();
             }
             return activityManager;
     }
      
    private ActivityManager() {
        // this.context = context;
     }
      
     /**
     * task map�����ڼ�¼activityջ�������˳��������Ϊ�˲�Ӱ��ϵͳ����activity�������������ã�
      */
     private final HashMap<String, SoftReference<Activity>> taskMap = new HashMap<String, SoftReference<Activity>>();
      
     /**
     * ��Ӧ��task map����activity
      */
     public final void putActivity(Activity atv) {
             taskMap.put(atv.toString(), new SoftReference<Activity>(atv));
     }
      
     /**
     * ��Ӧ��task map����activity
      */
     public final void removeActivity(Activity atv) {
             taskMap.remove(atv.toString());
     }
      
     /**
     * ���Ӧ�õ�taskջ����������������ᵼ��Ӧ���˻ص�����
      */
     public final void exit() {
             for (Iterator<Entry<String, SoftReference<Activity>>> iterator = taskMap.entrySet().iterator(); iterator.hasNext();) {
                     SoftReference<Activity> activityReference =  iterator.next().getValue();
                     Activity activity = activityReference.get();
                     if (activity != null) {
                             activity.finish();
                     }
             }
             taskMap.clear();
     }
 }
