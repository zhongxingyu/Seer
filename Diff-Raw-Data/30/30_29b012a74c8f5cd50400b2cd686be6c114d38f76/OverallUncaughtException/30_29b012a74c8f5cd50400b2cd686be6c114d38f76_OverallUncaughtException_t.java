 package com.goodow.drive.android.toolutils;
 
import java.io.PrintWriter;
import java.io.StringWriter;
 import java.lang.Thread.UncaughtExceptionHandler;
 
 public enum OverallUncaughtException implements UncaughtExceptionHandler {
   OVERALLUNCAUGHTEXCEPTION;
 
   public static abstract class LoginAgain {
    public abstract void login(String errorinfo);
   }
 
   private LoginAgain loginAgain;
 
   public void setLoginAgain(LoginAgain loginAgain) {
     this.loginAgain = loginAgain;
   }
 
   @Override
   public void uncaughtException(Thread thread, Throwable ex) {
    StringBuffer sb = new StringBuffer();

    long mimutes = System.currentTimeMillis();
    sb.append("时间: " + mimutes + "\n");

    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    ex.printStackTrace(printWriter);

    String errorlog = writer.toString();
    sb.append(errorlog);

    loginAgain.login(sb.toString());
   }
 }
