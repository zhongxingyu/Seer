 package com.goldmanalpha.dailydo;
 
 import android.app.Application;
 import com.com.goldmanalpha.dailydo.db.DatabaseRoot;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Aaron
  * Date: 1/9/12
  * Time: 10:21 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DailyDoApp extends Application{
 
     public DailyDoApp() {
         DatabaseRoot.setContext(this);
     }
 }
