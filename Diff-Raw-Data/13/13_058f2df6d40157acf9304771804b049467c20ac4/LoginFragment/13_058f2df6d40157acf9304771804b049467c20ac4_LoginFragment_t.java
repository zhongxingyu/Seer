 package com.noughmad.plusfive;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 
 import java.io.IOException;
 
 /**
  * Created by miha on 8/11/13.
  */
 public class LoginFragment extends DialogFragment {
     private View mView;
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
 
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         LayoutInflater inflater = getActivity().getLayoutInflater();
 
         mView = inflater.inflate(R.layout.login_dialog, null, false);
         builder.setView(mView)
                 .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog, int id) {
 
                     }
                 })
                 .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.dismiss();
                     }
                 })
                 .setTitle(R.string.login);
         return builder.create();
     }
 
     @Override
     public void onStart()
     {
         super.onStart();
 
         SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
         if (prefs.contains("nickname") && prefs.contains("password")) {
             refreshLogin(prefs.getString("nickname", ""), prefs.getString("password", ""));
             ((AlertDialog)getDialog()).getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
         }
 
         AlertDialog d = (AlertDialog)getDialog();
         if(d != null)
         {
             Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
             positiveButton.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(final View v)
                 {
                     final String nickname = ((EditText) mView.findViewById(R.id.editNickname)).getText().toString();
                     final String password = ((EditText) mView.findViewById(R.id.editPassword)).getText().toString();
 
                     v.setEnabled(false);
 
                     AsyncTask<String, Void, Connection.Response> task = new AsyncTask<String, Void, Connection.Response>() {
 
                         @Override
                         protected Connection.Response doInBackground(String... params) {
                             Log.d("LoginTask", String.valueOf(params));
                             try {
                                 return Jsoup.connect("http://slashdot.org/my/login")
                                         .data("unickname", params[0])
                                         .data("upasswd", params[1])
                                         .method(Connection.Method.POST).execute();
                             } catch (IOException e) {
                                 return null;
                             }
                         }
 
                         @Override
                         protected void onPostExecute(Connection.Response response) {
                             if (response != null && response.hasCookie("user")) {
                                 getActivity().getSharedPreferences("cookie", Context.MODE_PRIVATE).edit().putString("user", response.cookie("user")).commit();
                                getActivity().invalidateOptionsMenu();
                                 Toast.makeText(getActivity(), getResources().getString(R.string.login_success, nickname), Toast.LENGTH_SHORT).show();
 
                                 getActivity().getPreferences(Context.MODE_PRIVATE).edit()
                                         .putString("nickname", nickname)
                                         .putString("password", password).commit();
 
                                 getDialog().dismiss();
                             } else {
                                 Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_LONG).show();
                                 v.setEnabled(true);
                             }
                         }
                     };
 
                     task.execute(nickname, password);
                 }
             });
         }
     }
 
     void refreshLogin(String nickname, String password) {
         AsyncTask<String, Void, Connection.Response> task = new AsyncTask<String, Void, Connection.Response>() {
 
             @Override
             protected Connection.Response doInBackground(String... params) {
                 Log.d("LoginTask", String.valueOf(params));
                 try {
                     return Jsoup.connect("http://slashdot.org/my/login")
                             .data("unickname", params[0])
                             .data("upasswd", params[1])
                             .method(Connection.Method.POST).execute();
                 } catch (IOException e) {
                     return null;
                 }
             }
 
             @Override
             protected void onPostExecute(Connection.Response response) {
                 if (response != null && response.hasCookie("user")) {
                     try {
                         getActivity().getSharedPreferences("cookie", Context.MODE_PRIVATE).edit().putString("user", response.cookie("user")).commit();
                         Toast.makeText(getActivity(), getResources().getString(R.string.login_success, getActivity().getPreferences(Context.MODE_PRIVATE).getString("nickname", "")), Toast.LENGTH_SHORT).show();
                         getDialog().dismiss();
                     } catch (NullPointerException e) {
                         e.printStackTrace();
                     }
                 } else {
                     Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_LONG).show();
                     ((AlertDialog)getDialog()).getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                 }
             }
         };
 
         task.execute(nickname, password);
     }
 
     @Override
     public void onDismiss(DialogInterface dialog) {
         super.onDismiss(dialog);
 
         if (getActivity() instanceof ReplyActivity) {
             ((ReplyActivity)getActivity()).onLoggedIn();
         }
     }
 }
