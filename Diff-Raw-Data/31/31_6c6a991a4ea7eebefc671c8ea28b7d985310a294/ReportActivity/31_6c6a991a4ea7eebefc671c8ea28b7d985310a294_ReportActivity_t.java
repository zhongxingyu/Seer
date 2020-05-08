 package ru.peppers;
 
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import model.Driver;
 import model.Order;
 import orders.CostOrder;
 import orders.NoCostOrder;
 import orders.PreliminaryOrder;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class ReportActivity extends Activity {
     private ArrayAdapter<String> simpleAdpt;
     private ArrayList<String> itemsList;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.report);
 
         Bundle bundle = getIntent().getExtras();
        //int id = bundle.getInt("id");
 
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
         nameValuePairs.add(new BasicNameValuePair("action", "reportdata"));
        //nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
 
         Document doc = PhpData.postData(this, nameValuePairs);
         if (doc != null) {
             Node errorNode = doc.getElementsByTagName("error").item(0);
 
             if (Integer.parseInt(errorNode.getTextContent()) == 1)
                 new AlertDialog.Builder(this).setTitle("")
                         .setMessage("  .  .")
                         .setNeutralButton("", null).show();
             else {
                 try {
                     initMainList(doc);
                 } catch (DOMException e) {
                     e.printStackTrace();
                     new AlertDialog.Builder(this).setTitle("")
                             .setMessage("  .  .")
                             .setNeutralButton("", null).show();
                 } catch (ParseException e) {
                     e.printStackTrace();
                     new AlertDialog.Builder(this).setTitle("")
                             .setMessage("  .  .")
                             .setNeutralButton("", null).show();
                 }
             }
         } else {
             initList();
         }
     }
 
     private void initMainList(Document doc) throws DOMException, ParseException {
         NodeList nodeList = doc.getElementsByTagName("order");
         ArrayList<Order> reports = new ArrayList<Order>();
         for (int i = 0; i < nodeList.getLength(); i++) {
             NamedNodeMap attributes = nodeList.item(i).getAttributes();
 
             int index = Integer.parseInt(attributes.getNamedItem("index").getTextContent());
             int type = Integer.parseInt(attributes.getNamedItem("type").getTextContent());
             SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
             Date date = format.parse(attributes.getNamedItem("date").getTextContent());
             String carClass = attributes.getNamedItem("class").getTextContent();
             String adress = attributes.getNamedItem("adress").getTextContent();
             String where = attributes.getNamedItem("where").getTextContent();
             int costOrder = Integer.parseInt(attributes.getNamedItem("costOrder").getTextContent());
 
             if (type == 0) {
                 int cost = Integer.parseInt(attributes.getNamedItem("cost").getTextContent());
                 String costType = attributes.getNamedItem("costType").getTextContent();
                 String text = nodeList.item(i).getTextContent();
         //        reports.add(new CostOrder(this,costOrder,index,date, adress, carClass, text, where, cost, costType));
             }
             if (type == 1) {
                 String text = nodeList.item(i).getTextContent();
           //      reports.add(new NoCostOrder(this,costOrder,index,date, adress, carClass, text, where));
             }
             if (type == 2) {
                 String text = nodeList.item(i).getTextContent();
           //      reports.add(new PreliminaryOrder(this,costOrder,index,date, adress, carClass, text, where));
             }
             if(attributes.getNamedItem("abonent") != null){
                 String abonent = attributes.getNamedItem("abonent").getTextContent();
                 int rides = Integer.parseInt(attributes.getNamedItem("rides").getTextContent());
                 reports.get(i).setAbonent(abonent);
                 reports.get(i).setRides(rides);
             }
 
         }
         int balance = Integer.parseInt(doc.getElementsByTagName("balance").item(0).getTextContent());
         int inorder = Integer.parseInt(doc.getElementsByTagName("inorder").item(0).getTextContent());
         int indistrict = Integer.parseInt(doc.getElementsByTagName("indistrict").item(0).getTextContent());
         int inall = Integer.parseInt(doc.getElementsByTagName("inall").item(0).getTextContent());
         Driver driver = TaxiApplication.getDriver();
         driver.setBalance(balance);
         driver.setReports(reports);
         driver.setInorder(inorder);
         driver.setInall(inall);
         driver.setIndistrict(indistrict);
         initList();
     }
 
     public void initList() {
         final Driver driver = TaxiApplication.getDriver();
         //       
         TextView balance = (TextView) findViewById(R.id.textView1);
         TextView number1 = (TextView) findViewById(R.id.textView2);
         TextView number2 = (TextView) findViewById(R.id.textView3);
         TextView number3 = (TextView) findViewById(R.id.textView4);
         balance.setText(": " + driver.getBalance() + " .");
         number1.setText(" : " + driver.getInorder());
         number2.setText(" : " + driver.getIndistrict());
         number3.setText(" : " + driver.getInall());
         itemsList = new ArrayList<String>();
         itemsList.add(": " + driver.getStatusString());
         itemsList.add(": " + driver.getClassAutoString());
         itemsList.add(": " + driver.reportsCount());
 
         ListView lv = (ListView) findViewById(R.id.listView1);
 
          simpleAdpt = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, itemsList);
 
         lv.setAdapter(simpleAdpt);
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
             public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long index) {
                 Bundle extras = getIntent().getExtras();
                 //int id = extras.getInt("id");
                 Intent intent;
                 Bundle bundle;
                 if (position == 2) {
                     intent = new Intent(ReportActivity.this, ReportListActivity.class);
 //                    bundle = new Bundle();
 //                    bundle.putInt("id", id);
 //                    intent.putExtras(bundle);
                     startActivity(intent);
                 }
                 if (position == 1) {
                     final CharSequence[] items = { "", "", "" };
                     AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                     builder.setTitle(" ");
                     builder.setSingleChoiceItems(items, driver.getClassAuto(),
                             onClassContextMenuItemListener(position));
                     AlertDialog alert = builder.create();
                     alert.show();
                 }
                 if (position == 0) {
                     final CharSequence[] items = { "", "", "" };
                     AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                     builder.setTitle(" ");
                     builder.setSingleChoiceItems(items, driver.getStatus(),
                             onStatusContextMenuItemListener(position));
                     AlertDialog alert = builder.create();
                     alert.show();
                 }
 
             }
         });
     }
     private OnClickListener onStatusContextMenuItemListener(final int position) {
         return new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 Driver driver = TaxiApplication.getDriver();
                 if (item != driver.getStatus()) {
 
                     if (item == 0 && driver.getDistrict() == "") {
                         Bundle extras = getIntent().getExtras();
                         //int id = extras.getInt("id");
                         Intent intent = new Intent(ReportActivity.this, DistrictActivity.class);
 //                        Bundle bundle = new Bundle();
 //                        bundle.putInt("id", id);
 //                        intent.putExtras(bundle);
                         startActivity(intent);
 
                         dialog.dismiss();
                         return;
                     }
 
                     if (item == 2 && driver.getOrdersCount() != 0) {
                         new AlertDialog.Builder(ReportActivity.this).setTitle("")
                                 .setMessage("       .")
                                 .setNeutralButton("", null).show();
                         dialog.dismiss();
                         return;
                     }
 
                     driver.setStatus(item);
 
                     List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                     nameValuePairs.add(new BasicNameValuePair("action", "status"));
                    //nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(TaxiApplication
                    //         .getDriverId())));
                     nameValuePairs.add(new BasicNameValuePair("status", String.valueOf(TaxiApplication
                             .getDriver().getStatus())));
 
 
                     PhpData.postData(ReportActivity.this, nameValuePairs);
 
 //                    if (item != 1 && itemsList.size() == 8) {
 //                        String rayonString = "";
 //                        if (driver.getDistrict() != "")
 //                            rayonString = driver.getDistrict() + "," + driver.getSubdistrict();
 //                        else
 //                            rayonString = " ";
 //                        itemsList.add(3, createItem("item", ": " + rayonString));
 //                    }
 //                    if (item == 1)
 //                        itemsList.remove(3);
                     itemsList.set(position, ": " + driver.getStatusString());
                     simpleAdpt.notifyDataSetChanged();
                 }
                 dialog.dismiss();
             }
         };
     }
 
     private OnClickListener onClassContextMenuItemListener(final int position) {
         return new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 Driver driver = TaxiApplication.getDriver();
                 if (item != driver.getClassAuto()) {
                     driver.setClassAuto(item);
                     itemsList.set(position,": " + driver.getClassAutoString());
                     simpleAdpt.notifyDataSetChanged();
                     // TODO:post to php data if needed
                 }
                 dialog.dismiss();
             }
         };
     }
 
 }
