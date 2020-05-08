 package no.hist.aitel.android.tictactoe;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.text.format.Formatter;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class NetworkActivity extends Activity {
 
     private static final int JOIN_DIALOG_ID = 0;
     private static final int HOST_DIALOG_ID = 1;
     private String ip;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.network);
         findViewById(R.id.button_join).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDialog(JOIN_DIALOG_ID);
             }
         });
         findViewById(R.id.button_host).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 final Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                 intent.putExtra("mode", GameActivity.MODE_MULTIPLAYER_HOST);
                 startActivity(intent);
             }
         });
         this.ip = findIpAddress();
     }
 
     private String findIpAddress() {
         final WifiInfo wifiInfo = ((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo();
         return Formatter.formatIpAddress(wifiInfo.getIpAddress());
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case JOIN_DIALOG_ID: {
                 final Dialog joinDialog = new Dialog(NetworkActivity.this);
                 joinDialog.setContentView(R.layout.joingamedialog);
                 joinDialog.setTitle("Join game");
                 joinDialog.setCancelable(true);
                 TextView tv_joingame = (TextView) joinDialog.findViewById(R.id.textview_joingame);
                 tv_joingame.setText(R.string.join_game_dialog);
                 Button button_ok = (Button) joinDialog.findViewById(R.id.button_joingame_ok);
                final EditText remoteIp = (EditText) joinDialog.findViewById(R.id.edittext_ip);
                 button_ok.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         final Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                         intent.putExtra("mode", GameActivity.MODE_MULTIPLAYER_JOIN);
                         intent.putExtra("remoteIp", remoteIp.getText().toString());
                         startActivity(intent);
                         joinDialog.dismiss();
                     }
                 });
                 Button button_cancel = (Button) joinDialog.findViewById(R.id.button_joingame_cancel);
                 button_cancel.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         joinDialog.dismiss();
                     }
                 });
                 return joinDialog;
             }
             case HOST_DIALOG_ID: {
                 final Dialog hostDialog = new Dialog(NetworkActivity.this);
                 hostDialog.setContentView(R.layout.hostgamedialog);
                 hostDialog.setTitle("Host game");
                 hostDialog.setCancelable(true);
                 TextView tv_hostgame = (TextView) hostDialog.findViewById(R.id.textview_hostgame);
                 tv_hostgame.setText(getResources().getString(R.string.host_game_dialog, ip));
                 Button button_cancel = (Button) hostDialog.findViewById(R.id.button_hostgame_cancel);
                 button_cancel.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         hostDialog.dismiss();
                     }
                 });
                 return hostDialog;
             }
             default: {
                 return null;
             }
         }
     }
 }
