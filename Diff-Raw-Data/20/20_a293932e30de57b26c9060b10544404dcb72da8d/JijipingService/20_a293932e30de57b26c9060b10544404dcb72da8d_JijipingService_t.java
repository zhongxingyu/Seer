 /**
  * Copyright (C) 2013 uphy.jp
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jp.uphy.jijiping;
 
 import jp.uphy.jijiping.app.ErrorNotifier;
 import jp.uphy.jijiping.common.Answers;
 import jp.uphy.jijiping.common.JijipingClient;
 
 import roboguice.service.RoboService;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 
 import com.google.inject.Inject;
 
 
 /**
  * @author Yuhi Ishikura
  */
 public class JijipingService extends RoboService implements JijipingClient.Receiver {
 
   public static final String INTENT_CHECKID = "checkid"; //$NON-NLS-1$
 
   private IBinder binder = new JijipingLocalBinder();
   private JijipingClient client;
   @Inject
   private ErrorNotifier errorNotifier;
 
   public class JijipingLocalBinder extends Binder {
 
     public JijipingService getService() {
       return JijipingService.this;
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void onStart(Intent intent, int startId) {
     super.onStart(intent, startId);
     try {
      this.client = new JijipingClient("uphy-home.ddo.jp", 12542, this);
     } catch (Throwable e) {
       this.errorNotifier.notifyError(e, "Failed to start client.");
       stopSelf();
       return;
     }
     final String checkinId = intent.getStringExtra(INTENT_CHECKID);
     if (checkinId == null) {
       stopSelf();
       return;
     }
 
     this.client.checkin(checkinId);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public IBinder onBind(Intent intent) {
     return this.binder;
   }
 
   public void sendQuestion(String question, Answers answers) {
     this.client.sendQuestion(question, answers);
   }
 
   public void sendAnswer(int answerIndex) {
     this.client.sendAnswer(answerIndex);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void answerReceived(int answerIndex) {
     final Intent intent = new Intent(this, YoungrFamilyActivity.class);
     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     startActivity(intent);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void questionReceived(String question, Answers answer) {
     final Intent intent = new Intent(this, AgedFamilyActivity.class);
     intent.putExtra(AgedFamilyActivity.INTENT_ANSWERS, answer);
     intent.putExtra(AgedFamilyActivity.INTENT_QUESTION, question);
     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     startActivity(intent);
   }
 }
