 package ru.aifgi.recognizer.view;
 
 /*
  * Copyright 2012 Alexey Ivanov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import javax.swing.*;
 
 /**
  * @author aifgi
  */
 
 public class ViewUtil {
     private static /*volatile*/ MainWindow ourMainWindow;
     private static final Thread.UncaughtExceptionHandler HANDLER = new Thread.UncaughtExceptionHandler() {
         @Override
         public void uncaughtException(final Thread t, final Throwable e) {
             showErrorMessage(e);
         }
     };
 
     // TODO: change return type to Window or make MainWindow interface?
     public static MainWindow getMainWindow() {
         if (ourMainWindow == null) {
             synchronized (ViewUtil.class) {
                 if (ourMainWindow == null) {
                     ourMainWindow = new MainWindow();
                 }
             }
         }
         return ourMainWindow;
     }
 
     // TODO: move to another place?
     public static Thread.UncaughtExceptionHandler getApplicationUncaughtExceptionHandler() {
         return HANDLER;
     }
 
     public static void showErrorMessage(final Throwable throwable) {
         showErrorMessage(throwable.getClass().getName(), throwable.getLocalizedMessage());
     }
 
     public static void showErrorMessage(final String title, final String message) {
         JOptionPane.showMessageDialog(ourMainWindow, message, title, JOptionPane.ERROR_MESSAGE);
     }
 
     private ViewUtil() {
     }
 }
