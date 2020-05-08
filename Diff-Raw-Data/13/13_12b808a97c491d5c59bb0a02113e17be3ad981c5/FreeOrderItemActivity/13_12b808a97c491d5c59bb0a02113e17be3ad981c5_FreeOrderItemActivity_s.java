 package ru.peppers;
 
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import model.Driver;
 import model.Order;
 import orders.CostOrder;
 import orders.NoCostOrder;
 import orders.PreliminaryOrder;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class FreeOrderItemActivity extends Activity {
 
     private Order order;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.order);
 
         Bundle bundle = getIntent().getExtras();
        int id = bundle.getInt("id");
         int index = bundle.getInt("index");
         if (bundle.getBoolean("service")) {
             int type = bundle.getInt("type");
             int orderindex = bundle.getInt("orderindex");
             SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
             Date date = null;
             try {
                 date = format.parse(bundle.getString("date"));
             } catch (ParseException e) {
                 e.printStackTrace();
             }
             String carClass = bundle.getString("class");
             String adress = bundle.getString("adress");
             String where = bundle.getString("where");
             int costOrder = bundle.getInt("costOrder");
 
             if (type == 0) {
                 int cost = bundle.getInt("cost");
                 String costType = bundle.getString("costType");
                 String text = bundle.getString("text");
                 order = new CostOrder(costOrder, orderindex, date, adress, carClass, text, where, cost, costType);
             }
             if (type == 1) {
                 String text = bundle.getString("text");
                 order = new NoCostOrder(costOrder, orderindex, date, adress, carClass, text, where);
             }
             if (type == 2) {
                 String text = bundle.getString("text");
                 order = new PreliminaryOrder(costOrder, orderindex, date, adress, carClass, text, where);
             }
         } else
             order = TaxiApplication.getDriver().getFreeOrders().get(index);
 
         ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, order.toArrayList());
 
         ListView lv = (ListView) findViewById(R.id.listView1);
 
         lv.setAdapter(arrayAdapter);
 
         Button button = (Button) findViewById(R.id.button1);
         button.setText("");
         button.setOnClickListener(new Button.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 AlertDialog.Builder alert = new AlertDialog.Builder(FreeOrderItemActivity.this);
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
                         nameValuePairs.add(new BasicNameValuePair("order_id", String.valueOf(order
                                 .get_index())));
                         //nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
                         nameValuePairs.add(new BasicNameValuePair("minutes", String.valueOf(cs[which])));
                         Document doc = PhpData.postData(FreeOrderItemActivity.this, nameValuePairs);
                         if (doc != null) {
                             Node errorNode = doc.getElementsByTagName("error").item(0);
                             if (Integer.parseInt(errorNode.getTextContent()) == 1)
                                 new AlertDialog.Builder(FreeOrderItemActivity.this).setTitle("")
                                         .setMessage("  .  .")
                                         .setNeutralButton("", null).show();
                             else {
                                 Driver driver = TaxiApplication.getDriver();
                                 if (driver.getOrders() != null)
                                     driver.getOrders().add(order);
                                 else {
                                     ArrayList<Order> arrayList = new ArrayList<Order>();
                                     arrayList.add(order);
                                     driver.setOrders(arrayList);
                                 }
                                 driver.setStatus(3);
 
                                 Intent intent = new Intent(FreeOrderItemActivity.this,
                                         MyOrderItemActivity.class);
                                 Bundle bundle = new Bundle();
                                 //bundle.putInt("id", id);
                                 bundle.putInt("index", driver.getOrders().size() - 1);
 
                                 Calendar cal = Calendar.getInstance();
                                 cal.setTime(new Date());
                                 cal.add(Calendar.MINUTE, Integer.valueOf((String) cs[which]));
 
                                 order.setTimerDate(cal.getTime());
                                 // set Date and add minutes // in order just count minutes date-currendate);
                                 intent.putExtras(bundle);
                                 startActivity(intent);
                                 finish();
                             }
                         }
                     }
 
                 });
                 alert.show();
             }
 
         });
     }
 }
