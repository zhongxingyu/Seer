 /*
  * Copyright (c) 2013 Noveo Group
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * Except as contained in this notice, the name(s) of the above copyright holders
  * shall not be used in advertising or otherwise to promote the sale, use or
  * other dealings in this Software without prior written authorization.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.noveogroup.android.task.ui;
 
 import android.content.Context;
 import com.noveogroup.android.task.*;
 
 import java.util.concurrent.Executors;
 
 ////////////////////////////////////////////////////////////////////////////////
 // todo сделать аналог AsyncTask, сделать стандартное решение с диалогами
 // todo как через один менеджер гонять задачи для UI и для фона
 // todo интегрироваться с Context
 // todo add UIHandler to task environment
 ////////////////////////////////////////////////////////////////////////////////
 public class AndroidTaskExecutor extends SimpleTaskExecutor<AndroidTaskEnvironment> {
 
     private final Context context;
     private ProgressManager progressManager;
 
     public AndroidTaskExecutor(Context context) {
         super(Executors.newFixedThreadPool(7));
         this.context = context;
     }
 
     public void onResume() {
         progressManager = new ProgressManager(context) {
             @Override
             protected void onCancel() {
                 queue().interrupt();
             }
         };
         addTaskListener(new TaskListener.Default() {
             @Override
             public void onCreate(TaskHandler handler) {
                 if (!queue().isEmpty()) {
                     progressManager.show();
                 }
             }
 
             @Override
             public void onDestroy(TaskHandler handler) {
                 if (queue().isEmpty()) {
                     progressManager.hide();
                 }
 
                 if (handler.getState() == TaskHandler.State.FAILED) {
                     progressManager.error(handler.getThrowable());
                 }
             }
         });
     }
 
     public void onPause() {
         progressManager.destroy();
         queue().interrupt();
     }
 
     @Override
     protected <T extends Task> AndroidTaskEnvironment createTaskEnvironment(TaskHandler<T, AndroidTaskEnvironment> taskHandler) {
        return new AndroidTaskEnvironment(taskHandler, context);
     }
 
 }
