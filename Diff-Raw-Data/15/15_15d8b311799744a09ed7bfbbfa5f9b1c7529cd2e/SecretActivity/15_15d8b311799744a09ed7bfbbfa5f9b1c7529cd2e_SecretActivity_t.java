 /*
  * Copyright (c) Paolo Andreetto
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *     http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package oss.crypto.casket;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 public class SecretActivity
     extends Activity
     implements DialogInterface.OnClickListener {
 
     private GroupOfSecretView viewBox = null;
 
     private String login;
 
     private String password;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         Intent intent = this.getIntent();
         login = intent.getStringExtra(CasketConstants.LOGIN_TAG);
         password = intent.getStringExtra(CasketConstants.PWD_TAG);
         String secId = intent.getStringExtra(CasketConstants.SECID_TAG);
 
         if (login == null || password == null) {
 
             showError(R.string.nologorpwd);
 
         } else {
 
             try {
 
                 if (secId == null) {
 
                     viewBox = new GroupOfSecretView(this);
 
                 } else {
                     SecretManager secMan = SecretManager.getManager(this, login, password);
                     Secret currentSec = secMan.getSecret(secId);
                     viewBox = (GroupOfSecretView) SecretViewFactory.getSecretView(this, currentSec);
                 }
 
                 this.setContentView(viewBox);
 
             } catch (SecretException sEx) {
 
                 showError(sEx.getMsgRef());
 
             }
         }
     }
 
     @Override
     public void onStop() {
         super.onStop();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         menu.add(Menu.NONE, 1, Menu.NONE, R.string.save_all);
         menu.add(Menu.NONE, 2, Menu.NONE, R.string.rmv_all);
         menu.add(Menu.NONE, 3, Menu.NONE, R.string.desel_all);
 
         if (viewBox instanceof GroupOfSecretView) {
             menu.add(Menu.NONE, 4, Menu.NONE, R.string.add_prop);
         }
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case 1:
 
             try {
                 Secret newSecret = SecretViewFactory.getSecret(viewBox);
                 SecretManager.getManager(this, login, password).putSecret(newSecret);
             } catch (SecretException sEx) {
                 showError(sEx.getMsgRef());
             }
 
            finish();
 
             return true;
         case 2:
 
             boolean foundSelected = false;
 
             for (int k = viewBox.getChildCount() - 1; k >= 0; k--) {
                 View tmpView = viewBox.getChildAt(k);
                 if (tmpView.isSelected()) {
                     viewBox.removeViewAt(k);
                     foundSelected = true;
                 }
             }
 
             if (foundSelected) {
                 try {
                     Secret newSecret = SecretViewFactory.getSecret(viewBox);
                     SecretManager.getManager(this, login, password).putSecret(newSecret);
                 } catch (Exception ex) {
                     showError(R.string.casket_operr);
                 }
             }
 
            finish();
 
             return true;
 
         case 3:
             return true;
 
         case 4:
 
             GroupOfSecretView gView = (GroupOfSecretView) viewBox;
             PropertySecretView pView = new PropertySecretView(this);
             gView.addView(pView);
             return true;
 
         }
         return false;
     }
 
     public void onClick(DialogInterface dialog, int id) {
 
     }
 
     private void showError(int msgId) {
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.err_dmsg);
         builder.setMessage(msgId);
 
         builder.setPositiveButton(R.string.ok_dbtn, this);
 
         AlertDialog errDialog = builder.create();
         errDialog.show();
 
     }
 
 }
