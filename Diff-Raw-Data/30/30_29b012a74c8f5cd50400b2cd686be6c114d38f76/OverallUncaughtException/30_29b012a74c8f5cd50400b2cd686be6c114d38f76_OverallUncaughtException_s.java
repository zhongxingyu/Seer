 package com.goodow.drive.android.toolutils;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 public enum OverallUncaughtException implements UncaughtExceptionHandler {
   OVERALLUNCAUGHTEXCEPTION;
 
   public static abstract class LoginAgain {
    public abstract void login();
   }
 
   private LoginAgain loginAgain;
 
   public void setLoginAgain(LoginAgain loginAgain) {
     this.loginAgain = loginAgain;
   }
 
   @Override
   public void uncaughtException(Thread thread, Throwable ex) {
    loginAgain.login();
   }
 }
