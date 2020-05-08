 /**
  * <p>
  * <u><b>Copyright Notice</b></u>
  * </p><p>
  * The copyright in this document is the property of
  * Bath Institute of Medical Engineering.
  * </p><p>
  * Without the written consent of Bath Institute of Medical Engineering
  * given by Contract or otherwise the document must not be copied, reprinted or
  * reproduced in any material form, either wholly or in part, and the contents
  * of the document or any method or technique available there from, must not be
  * disclosed to any other person whomsoever.
  *  </p><p>
  *  <b><i>Copyright 2013-2014 Bath Institute of Medical Engineering.</i></b>
  * --------------------------------------------------------------------------
  *
  */
 package com.projectnocturne.views;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.percolate.caffeine.MiscUtils;
 import com.projectnocturne.MainActivity;
 import com.projectnocturne.NocturneApplication;
 import com.projectnocturne.R;
 import com.projectnocturne.datamodel.DbMetadata;
 import com.projectnocturne.datamodel.DbMetadata.UserConnectionStatus;
 import com.projectnocturne.datamodel.RESTResponseMsg;
 import com.projectnocturne.datamodel.UserConnectDb;
 import com.projectnocturne.datamodel.UserDb;
 
 import java.util.List;
 
 public class ConnectToUserFragment extends NocturneFragment {
 
     private final String LOG_TAG = ConnectToUserFragment.class.getSimpleName() + "::";
     public TextView txtErrorMsg;
     public EditText txtEmailAddr;
     public ToggleButton swtchCarer;
     private TextWatcher textChangedWtchr = new TextWatcher() {
         @Override
         public void afterTextChanged(final Editable s) {
             ConnectToUserFragment.this.enableButton();
         }
 
         @Override
         public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
             // TODO Auto-generated method stub
         }
 
         @Override
         public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
         }
     };
     private Button btnConnect;
     private boolean readyFragment;
     private UserDb userDbObj;
     private UserConnectDb usrCnctDb = null;
 
     private Handler handler = new Handler() {
         @Override
         public void handleMessage(final Message msg) {
             NocturneApplication.d(LOG_TAG + "handleMessage()");
             txtErrorMsg.setVisibility(View.INVISIBLE);
             if (usrCnctDb == null) {
                 NocturneApplication.logMessage(Log.INFO, LOG_TAG + "handleMessage() Invalid");
                 usrCnctDb.setStatus(UserConnectionStatus.REQUEST_DENIED.toString());
                 NocturneApplication.getInstance().getDataModel().setUserConnection(usrCnctDb);
                 txtErrorMsg.setText("Invalid");
                 txtErrorMsg.setVisibility(View.VISIBLE);
             } else {
                 final RESTResponseMsg rspnsMsg = msg.getData().getParcelable("RESTResponseMsg");
                 if (msg.what == DbMetadata.RegistrationStatus_ACCEPTED) {
                     NocturneApplication.logMessage(Log.INFO, LOG_TAG + "handleMessage() RegistrationStatus_ACCEPTED");
                     usrCnctDb.setStatus(UserConnectionStatus.REQUEST_ACCEPTED.toString());
                     NocturneApplication.getInstance().getDataModel().setUserConnection(usrCnctDb);
                     Activity parentActivity=getActivity();
                    ((MainActivity) parentActivity).showScreen();
 
                 } else if (msg.what == DbMetadata.RegistrationStatus_DENIED) {
                     NocturneApplication.logMessage(Log.INFO, LOG_TAG + "handleMessage() RegistrationStatus_DENIED");
                     usrCnctDb.setStatus(UserConnectionStatus.REQUEST_DENIED.toString());
                     NocturneApplication.getInstance().getDataModel().setUserConnection(usrCnctDb);
                     txtErrorMsg.setText(rspnsMsg.getMessage());
                     txtErrorMsg.setVisibility(View.VISIBLE);
                 }
             }
         }
     };
 
     public ConnectToUserFragment() {
     }
 
     private void enableButton() {
         if (txtEmailAddr.getText().length() > 0 && MiscUtils.isValidEmail(txtEmailAddr.getText().toString())) {
             btnConnect.setEnabled(true);
         } else {
             btnConnect.setEnabled(false);
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_connect_to_user, container, false);
 
         txtErrorMsg = (TextView) rootView.findViewById(R.id.connect_user_error_msg);
         txtEmailAddr = (EditText) rootView.findViewById(R.id.connect_user_email);
         swtchCarer = (ToggleButton) rootView.findViewById(R.id.connect_user_switch_carer);
         btnConnect = (Button) rootView.findViewById(R.id.connect_user_button_connect);
 
         btnConnect.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(final View pView) {
                 txtErrorMsg.setVisibility(View.VISIBLE);
                 txtErrorMsg.setText(getResources().getString(R.string.connect_user_sending));
                 ConnectToUserFragment.this.sendConnectMessage();
             }
         });
 
         readyFragment = true;
         update();
 
         return rootView;
     }
 
     protected void sendConnectMessage() {
         txtErrorMsg.setVisibility(View.INVISIBLE);
         usrCnctDb = new UserConnectDb();
         if (swtchCarer.isChecked()) {
             usrCnctDb.setUser1(txtEmailAddr.getText().toString(), "PATIENT");
             usrCnctDb.setUser2(userDbObj.getEmail1(), "CARER");
         } else {
             usrCnctDb.setUser1(txtEmailAddr.getText().toString(), "CARER");
             usrCnctDb.setUser2(userDbObj.getEmail1(), "PATIENT");
         }
         usrCnctDb = NocturneApplication.getInstance().getDataModel().setUserConnection(usrCnctDb);
         NocturneApplication.getInstance().getServerComms().sendConnectToUserMessage(getActivity(), handler, usrCnctDb);
     }
 
     public void update() {
         if (!readyFragment) {
             NocturneApplication.logMessage(Log.INFO, LOG_TAG + "update() not ready");
             return;
         }
         NocturneApplication.logMessage(Log.INFO, LOG_TAG + "update() ready");
         final List<UserDb> users = NocturneApplication.getInstance().getDataModel().getUsers();
         if (users.size() == 1) {
             userDbObj = users.get(0);
         } else {
             userDbObj = new UserDb();
         }
     }
 }
