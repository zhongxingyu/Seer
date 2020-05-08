 /*
  * Copyright (C) 2010 Takuo Kitame
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jp.takuo.android.mmsreq;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Context;
 
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import android.os.AsyncTask;
 import android.app.ProgressDialog;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class MMSReq extends Activity {
 
     private Context mContext;
     private ProgressDialog mProgressDialog;
     private TextView mTextResult;
     private TextView mTextAPN;
     private CheckBox mCheckBox;
 
     private Request mRequest;
     private boolean mResult = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mContext = getApplicationContext();
         setContentView(R.layout.main);
         mTextResult = (TextView) findViewById(R.id.t_result);
         mTextAPN = (TextView) findViewById(R.id.text_apn_name);
         Button b = (Button) findViewById(R.id.b_request);
 
         b.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 mRequest = new Request(mContext, Preferences.getMmsType(mContext));
                 AsyncRequest req = new AsyncRequest();
                 req.execute();
             }
         });
 
         mCheckBox = (CheckBox) findViewById(R.id.check_auto_exit);
         mCheckBox.setChecked(Preferences.getAutoExit(mContext));
         mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Preferences.setAutoExit(mContext, isChecked);
             }
         });
         mCheckBox = (CheckBox) findViewById(R.id.check_auto_request);
         mCheckBox.setChecked(Preferences.getAutoRequest(mContext));
         mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Preferences.setAutoRequest(mContext, isChecked);
             }
         });
         mCheckBox = (CheckBox) findViewById(R.id.check_toast);
         mCheckBox.setChecked(Preferences.getEnableToast(mContext));
         mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Preferences.setEnableToast(mContext, isChecked);
             }
         });
         int type = Preferences.getMmsType(mContext);
         switch (type) {
         case Request.APN_PROFILE_SMILE:
             mTextAPN.setText("MMS APN: smile.world");
             break;
         case Request.APN_PROFILE_SBMMS:
             mTextAPN.setText("MMS APN: mailwebservice.softbank.ne.jp");
             break;
         case Request.APN_PROFILE_OPEN:
             mTextAPN.setText("MMS APN: open.softbank.ne.jp");
             break;
         default:
            mTextAPN.setText(getString(R.string.invalid_apn_setting));
             b.setEnabled(false);
         }
     }
 
     // Background Task class
     class AsyncRequest extends AsyncTask<Void, String, String> {
         public AsyncRequest() {
             mProgressDialog = new ProgressDialog(MMSReq.this);
             mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
 
         @Override
         protected String doInBackground(Void... params) {
             return requestMMS();
         }
 
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
             if(mProgressDialog != null &&
                 mProgressDialog.isShowing()) {
                 mProgressDialog.dismiss();
                 mProgressDialog = null;
             }
             if (mResult && Preferences.getAutoExit(mContext)) {
                 if (Preferences.getEnableToast(mContext))
                     Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
                 finish();
             } else {
                 DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");
                 mTextResult.setText(result + "\n" + df.format(new Date()));
             }
         }
 
         protected void onPreExecute() {
             super.onPreExecute();
             mTextResult.setText("");
             mProgressDialog.setIndeterminate(true);
             mProgressDialog.setTitle(R.string.start_request);
             mProgressDialog.show();
         }
 
         protected void onProgressUpdate(String... progress) {
             mProgressDialog.setMessage(progress[0]);
         }
 
         protected String requestMMS() {
             String message = null;
             try {
                 publishProgress(getString(R.string.connect_to_mobile));
                 mRequest.getConnectivity();
                 publishProgress(getString(R.string.connect_to_server));
                 mRequest.tryConnect();
                 publishProgress(getString(R.string.request_to_server));
                 message = mRequest.httpRequest();
                 mResult = true;
             } catch (Request.NoConnectivityException e) {
                 if (message == null)
                     message = getString(R.string.failed_to_connect);
                 message = message + "\n" + e.getMessage();
             } catch (Request.NoRouteToHostException e) {
                 if (message == null)
                     message = getString(R.string.failed_to_connect);
                 message = message + "\n" + e.getMessage();
             } catch (Request.ConnectTimeoutException e) {
                 if (message == null)
                     message = getString(R.string.failed_to_connect);
                 message = message + "\n" + e.getMessage();
             } catch (Exception e) {
                 if (message == null)
                     message = getString(R.string.failed_to_connect);
                 message = message + "\n" + e.getMessage();
                 e.printStackTrace();
             } finally {
                 mRequest.disconnect();
             }
             return message;
         }
     } /* AsyncRequest */
 }
