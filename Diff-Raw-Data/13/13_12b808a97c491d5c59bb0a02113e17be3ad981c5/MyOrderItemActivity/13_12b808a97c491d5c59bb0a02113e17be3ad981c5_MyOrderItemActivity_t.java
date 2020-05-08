 package ru.peppers;
 
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import model.Order;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.InputType;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MyOrderItemActivity extends Activity {
 
     private CountDownTimer timer;
     private ArrayList<String> orderList;
     private TextView counterView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.myorder);
 
         Bundle bundle = getIntent().getExtras();
        // int id = bundle.getInt("id");
         int index = bundle.getInt("index");
 
         Order order = TaxiApplication.getDriver().getOrder(index);
 
         orderList = order.toArrayList();
 
         counterView = (TextView) findViewById(R.id.textView1);
 
         TextView tv = (TextView) findViewById(R.id.textView2);
 
         int arraySize = orderList.size();
         for(int i = 0; i < arraySize; i++) {
         	tv.append(orderList.get(i));
         	tv.append("\n");
         }
 
         //arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, orderList);
 
         if (order.getTimerDate() != null) {
             timerInit(order);
         }
 
         //ListView lv = (ListView) findViewById(R.id.listView1);
 
         //lv.setAdapter(arrayAdapter);
 
 
 
         Button button = (Button) findViewById(R.id.button1);
         button.setText(" ");
         button.setOnClickListener(new Button.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 final CharSequence[] items = { "", "", "", "  " };
                 AlertDialog.Builder builder = new AlertDialog.Builder(MyOrderItemActivity.this);
                 builder.setTitle(" ");
                 builder.setItems(items, onContextMenuItemListener());
                 AlertDialog alert = builder.create();
                 alert.show();
             }
 
         });
     }
 
     private OnClickListener onContextMenuItemListener() {
         return new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 Log.d("My_tag", " - " + item);
                 dialog.dismiss();
                 switch (item) {
                     case 0:
                         inviteDialog();
                         break;
                     case 1:
                         timeDialog();
                         break;
                     case 2:
                         priceDialog();
                         break;
                     case 3:
                         Bundle bundle = getIntent().getExtras();
                         //int id = bundle.getInt("id");
                         int index = bundle.getInt("index");
                         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                         nameValuePairs.add(new BasicNameValuePair("action", "calloffice"));
                         //nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
                         nameValuePairs.add(new BasicNameValuePair("index", String.valueOf(index)));
 
                         Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs);
                         if (doc != null) {
                             Node errorNode = doc.getElementsByTagName("error").item(0);
 
                             if (Integer.parseInt(errorNode.getTextContent()) == 1)
                                 new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                                         .setMessage("  .  .")
                                         .setNeutralButton("", null).show();
                             else {
                                 new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                                         .setMessage("  .  .")
                                         .setNeutralButton("", null).show();
                             }
                         }
                         break;
 
                     default:
                         break;
                 }
             }
 
         };
     }
 
     private void inviteDialog() {
         Bundle bundle = getIntent().getExtras();
         //int id = bundle.getInt("id");
         int index = bundle.getInt("index");
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
         nameValuePairs.add(new BasicNameValuePair("action", "invite"));
         //nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
         nameValuePairs.add(new BasicNameValuePair("index", String.valueOf(index)));
 
         Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs);
         if (doc != null) {
             Node errorNode = doc.getElementsByTagName("error").item(0);
 
             if (Integer.parseInt(errorNode.getTextContent()) == 1)
                 new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                         .setMessage("  .  .")
                         .setNeutralButton("", null).show();
             else {
                 new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                         .setMessage(" .").setNeutralButton("", null).show();
             }
         }
     }
 
     private void timeDialog() {
         AlertDialog.Builder alert = new AlertDialog.Builder(MyOrderItemActivity.this);
         alert.setTitle("");
         final CharSequence cs[];
 
         cs = new String[]{"3","5","7","10","15","20","25","30","35"};
 
         alert.setItems(cs, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 Bundle extras = getIntent().getExtras();
                 //int id = extras.getInt("id");
                 int index = extras.getInt("index");
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                 nameValuePairs.add(new BasicNameValuePair("action", "savetime"));
                // nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
                 nameValuePairs.add(new BasicNameValuePair("index", String.valueOf(index)));
                 nameValuePairs.add(new BasicNameValuePair("value", String.valueOf(cs[which])));
                 Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs);
                 if (doc != null) {
                     Node errorNode = doc.getElementsByTagName("error").item(0);
                     if (Integer.parseInt(errorNode.getTextContent()) == 1)
                         new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                                 .setMessage("  .  .")
                                 .setNeutralButton("", null).show();
                     else {
 
                         final Order order = TaxiApplication.getDriver().getOrder(index);
 
                         if (order.getTimerDate() != null) {
                             Calendar cal = Calendar.getInstance();
                             cal.add(Calendar.MINUTE, Integer.valueOf((String) cs[which]));
 
                             order.setTimerDate(cal.getTime());
                             timer.cancel();
                             timerInit(order);
                         }
                         new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                                 .setMessage(" .").setNeutralButton("", null).show();
                     }
                 }
             }
 
         });
         alert.show();
 
 
 
     }
 
     private void timerInit(final Order order) {
         long diffInMs = order.getTimerDate().getTime() - new Date().getTime();
 
         timer = new CountDownTimer(diffInMs, 1000) {
 
             public void onTick(long millisUntilFinished) {
             	int seconds = ((int) millisUntilFinished / 1000) % 60;
             	String secondsStr = String.valueOf(seconds);
             	if(seconds<=9)
             		secondsStr = "0"+seconds;
 
 
             	counterView.setText(((int) millisUntilFinished / 1000) / 60 + ":"
                         + secondsStr);
                 if((((int) millisUntilFinished / 1000) / 60)==1 && (((int) millisUntilFinished / 1000) % 60)==0)
                 {
                     final CharSequence[] items = { "", "", "", "  " };
                     AlertDialog.Builder builder = new AlertDialog.Builder(MyOrderItemActivity.this);
                     builder.setTitle(" ");
                     builder.setItems(items, onContextMenuItemListener());
                     AlertDialog alert = builder.create();
                     alert.show();
                 }
             }
 
             public void onFinish() {
             	counterView.setText("");
                 alertDelay(order);
 
                 MediaPlayer mp = MediaPlayer.create(getBaseContext(), (R.raw.sound));
                 mp.start();
 
             }
         }.start();
     }
 
    @Override
    protected void onPause() {
        timer.cancel();
        super.onPause();
    }

     private void alertDelay(final Order order) {
         AlertDialog.Builder alert = new AlertDialog.Builder(MyOrderItemActivity.this);
         alert.setTitle("");
         final CharSequence cs[];
 
         cs = new String[]{"3","5","7","10","15","20","25","30","35"};
 
         alert.setItems(cs, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 //Bundle extras = getIntent().getExtras();
                 //int id = extras.getInt("id");
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                 nameValuePairs.add(new BasicNameValuePair("action", "saveminutes"));
                 nameValuePairs.add(new BasicNameValuePair("order_id", String.valueOf(order.get_index())));
                 //nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
                 nameValuePairs.add(new BasicNameValuePair("minutes", String.valueOf(cs[which])));
                 Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs);
                 if (doc != null) {
                     Node errorNode = doc.getElementsByTagName("error").item(0);
                     if (Integer.parseInt(errorNode.getTextContent()) == 1)
                         new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                                 .setMessage("  .  .")
                                 .setNeutralButton("", null).show();
                     else {
                         Calendar cal = Calendar.getInstance();
                         cal.setTime(new Date());
                         cal.add(Calendar.MINUTE, Integer.valueOf((String) cs[which]));
                         order.setTimerDate(cal.getTime());
                         timerInit(order);
                     }
                 }
             }
 
         });
         alert.show();
     }
 
     private void priceDialog() {
         AlertDialog.Builder alert = new AlertDialog.Builder(MyOrderItemActivity.this);
 
         alert.setTitle("");
         alert.setMessage(" ");
 
         // Set an EditText view to get user input
         final EditText input = new EditText(MyOrderItemActivity.this);
         input.setInputType(InputType.TYPE_CLASS_NUMBER);
         alert.setView(input);
 
         alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 String value = input.getText().toString();
                 Bundle bundle = getIntent().getExtras();
                 int id = bundle.getInt("id");
                 int index = bundle.getInt("index");
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                 nameValuePairs.add(new BasicNameValuePair("action", "savecost"));
                 nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
                 nameValuePairs.add(new BasicNameValuePair("index", String.valueOf(index)));
                 nameValuePairs.add(new BasicNameValuePair("value", value));
 
                 Document doc = PhpData.postData(MyOrderItemActivity.this, nameValuePairs);
                 if (doc != null) {
                     Node errorNode = doc.getElementsByTagName("error").item(0);
 
                     if (Integer.parseInt(errorNode.getTextContent()) == 1)
                         new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                                 .setMessage("  .  .")
                                 .setNeutralButton("", null).show();
                     else {
                         new AlertDialog.Builder(MyOrderItemActivity.this).setTitle("")
                                 .setMessage(" .")
                                 .setNeutralButton("", new OnClickListener() {
 
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         timer.cancel();
                                         Bundle bundle = getIntent().getExtras();
                                         Intent intent = new Intent(MyOrderItemActivity.this,
                                                 MainListActivity.class);
                                         intent.putExtras(bundle);
                                         startActivity(intent);
                                         finish();
                                     }
                                 }).show();
                     }
                 }
             }
         });
         alert.show();
     }
 }
