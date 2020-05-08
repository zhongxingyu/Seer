 package com.carit.flashman;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.ContactsContract;
 import android.text.Html;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class GetSMSActivity extends Activity implements OnClickListener {
     
     private boolean isBind;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         setContentView(R.layout.get_sms);
         getPeople(getIntent().getStringExtra("number"));
         findViewById(R.id.show).setOnClickListener(this);
         findViewById(R.id.cancel).setOnClickListener(this);
         super.onCreate(savedInstanceState);
     }
     public void getPeople(String number) { 
         if(number.length()>11){
             number = number.substring(number.length()-11, number.length());
         }
         String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,   
                                 ContactsContract.CommonDataKinds.Phone.NUMBER};   
    
            
         // 将自己添加到 msPeers 中   
         Cursor cursor = this.getContentResolver().query(   
                 ContactsContract.CommonDataKinds.Phone.CONTENT_URI,   
                 projection,    // Which columns to return.   
                 ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + number + "'", // WHERE clause.   
                 null,          // WHERE clause value substitution   
                 null);   // Sort order.   
    
         if( cursor == null ||!cursor.moveToFirst()) {   
             String text = String.format(getString(R.string.sms_from), number);
             CharSequence styledText = Html.fromHtml(text);
             ((TextView)findViewById(R.id.sms_label)).setText(styledText); 
             return;   
         }   
         for( int i = 0; i < cursor.getCount(); i++ )   
         {   
             cursor.moveToPosition(i);   
                
             // 取得联系人名字   
             int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);      
             String name = cursor.getString(nameFieldColumnIndex);   
             Log.i("Contacts", "" + name + " .... " + nameFieldColumnIndex); // 这里提示 force close
             String text = String.format(getString(R.string.sms_from), name);
             CharSequence styledText = Html.fromHtml(text);
             ((TextView)findViewById(R.id.sms_label)).setText(styledText);
             getIntent().putExtra("name", name);
             return;
         } 
         
     }
     @Override
     public void onClick(View v) {
         if(v.getId()==R.id.show){
             Thread thread = new Thread(){
 
                 @Override
                 public void run() {
                     Intent i = new Intent();
                     i.setClass(GetSMSActivity.this, FlashManService.class);
                     bindService(i, mServiceConnection, BIND_AUTO_CREATE);
                 }
                 
             };
             thread.start();
 //            Intent it = getIntent();
 //            it.setClass(getBaseContext(), MainActivity.class);
 //            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 //            it.putExtra("from", MainActivity.INTENT_SMS);
 //            startActivity(it);
             
         }else{
             finish();
         }
         
     } 
     
     private ServiceConnection mServiceConnection = new ServiceConnection() {
         // 当我bindService时，让TextView显示MyService里getSystemTime()方法的返回值
         public void onServiceConnected(ComponentName name, IBinder service) {
             // TODO Auto-generated method stub
             FlashManService  mMyService = ((FlashManService.MyBinder) service).getService();
             mMyService.receiveSMS(getIntent());
             isBind = true;
             finish();
         }
 
         public void onServiceDisconnected(ComponentName name) {
 
         }
     };
 
     @Override
     protected void onDestroy() {
         if(isBind)
         unbindService(mServiceConnection);
         super.onDestroy();
     }
     
     
     
 
 }
