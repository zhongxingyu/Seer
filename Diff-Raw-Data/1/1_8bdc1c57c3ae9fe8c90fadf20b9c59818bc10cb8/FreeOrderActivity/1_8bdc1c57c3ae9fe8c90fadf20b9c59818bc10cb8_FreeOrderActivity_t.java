 package ru.peppers;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import model.Driver;
 import model.Order;
 import orders.CostOrder;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class FreeOrderActivity extends BalanceActivity implements AsyncTaskCompleteListener<Document> {
     protected static final int REQUEST_EXIT = 0;
     private ArrayList<Order> orders = new ArrayList<Order>();
     private Timer myTimer = new Timer();
     private ArrayAdapter<Order> arrayAdapter;
     private Integer refreshperiod = null;
     private boolean start = false;
     private PhpService myService;
     private boolean bound;
     private String candidateId = "";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         ListView lv = (ListView) findViewById(R.id.mainListView);
 
         arrayAdapter = new ArrayAdapter<Order>(this, android.R.layout.simple_list_item_1, orders);
 
         lv.setAdapter(arrayAdapter);
         lv.setEmptyView(findViewById(R.id.empty));
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
             public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long index) {
                 // Bundle extras = getIntent().getExtras();
                 // int id = extras.getInt("id");
 
                 Intent intent = new Intent(FreeOrderActivity.this, FreeOrderItemActivity.class);
                 Bundle bundle = new Bundle();
                 // bundle.putInt("id", id);
                 bundle.putInt("index", position);
                 intent.putExtras(bundle);
 
                 if (PhpData.isNetworkAvailable(FreeOrderActivity.this))
                     startActivityForResult(intent, REQUEST_EXIT);
             }
         });
 
     }
 
     private final ServiceConnection serviceConnection = new ServiceConnection() {
 
         @Override
         public void onServiceDisconnected(ComponentName className) {
 
             myService = null;
             bound = false;
         }
 
         @Override
         public void onServiceConnected(ComponentName arg0, IBinder service) {
 
             myService = ((PhpService.ServiceBinder) service).getService();
             myService.isStop = false;
             bound = true;
         }
     };
 
     void doBindService() {
 
         boolean bound = bindService(new Intent(this, PhpService.class), serviceConnection,
                 Context.BIND_AUTO_CREATE);
         if (bound) {
             Log.d("My_tag", "Successfully bound to service");
         } else {
 
             Log.d("My_tag", "Failed to bind service");
         }
     }
 
     void doUnbindService() {
         myService.isStop = true;
        myService.candidateId = candidateId;
         unbindService(serviceConnection);
     }
 
     @Override
     public void onTaskComplete(Document doc) {
         if (doc != null) {
             Node responseNode = doc.getElementsByTagName("response").item(0);
             Node errorNode = doc.getElementsByTagName("message").item(0);
 
             if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                 PhpData.errorFromServer(this, errorNode);
             else {
                 try {
                     initMainList(doc);
                 } catch (Exception e) {
                     PhpData.errorHandler(this, e);
                 }
             }
         }
     }
 
     private void getOrders() {
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
         nameValuePairs.add(new BasicNameValuePair("action", "list"));
         nameValuePairs.add(new BasicNameValuePair("mode", "available"));
         nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
         nameValuePairs.add(new BasicNameValuePair("object", "order"));
 
         ProgressDialog progress = new ProgressDialog(this);
         progress.setMessage("Loading...");
         new MyTask(this, progress, this).execute(nameValuePairs);
 
     }
 
     private void initMainList(Document doc) throws DOMException, ParseException {
         NodeList nodeList = doc.getElementsByTagName("item");
         int length = orders.size();
         orders.clear();
         for (int i = 0; i < nodeList.getLength(); i++) {
             // nominalcost - рекомендуемая стоимость заказа
             // class - класс автомобля (0 - все равно, 1 - Эконом, 2 - Стандарт,
             // 3 - Базовый)
             // addressdeparture - адрес подачи автомобиля
             // departuretime - время подачи(если есть)
             // paymenttype - форма оплаты (0 - наличные, 1 - безнал)
             // invitationtime - время приглашения (если пригласили)
             // quantity - количество заказов от этого клиента
             // comment - примечание
             // nickname - ник абонента (если есть)
             // registrationtime - время регистрации заказа
             // addressarrival - куда поедут
 
             Element item = (Element) nodeList.item(i);
 
             Node nominalcostNode = item.getElementsByTagName("nominalcost").item(0);
             Node classNode = item.getElementsByTagName("classid").item(0);
             Node addressdepartureNode = item.getElementsByTagName("addressdeparture").item(0);
             Node departuretimeNode = item.getElementsByTagName("departuretime").item(0);
             Node paymenttypeNode = item.getElementsByTagName("paymenttype").item(0);
             Node quantityNode = item.getElementsByTagName("quantity").item(0);
             Node commentNode = item.getElementsByTagName("comment").item(0);
             Node nicknameNode = item.getElementsByTagName("nickname").item(0);
             Node addressarrivalNode = item.getElementsByTagName("addressarrival").item(0);
             Node orderIdNode = item.getElementsByTagName("orderid").item(0);
 
             String nominalcost = null;
             Integer carClass = 0;
             String addressdeparture = null;
             Date departuretime = null;
             Integer paymenttype = null;
             Integer quantity = null;
             String comment = null;
             String nickname = null;
             // Date registrationtime = null;
             String addressarrival = null;
             String orderId = null;
 
             // if(departuretime==null)
             // //TODO:не предварительный
             // else
             // //TODO:предварительный
 
             SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
 
             if (!classNode.getTextContent().equalsIgnoreCase(""))
                 carClass = Integer.valueOf(classNode.getTextContent());
 
             if (!nominalcostNode.getTextContent().equalsIgnoreCase(""))
                 nominalcost = nominalcostNode.getTextContent();
 
             // if (!registrationtimeNode.getTextContent().equalsIgnoreCase(""))
             // registrationtime = format.parse(registrationtimeNode.getTextContent());
 
             if (!addressdepartureNode.getTextContent().equalsIgnoreCase(""))
                 addressdeparture = addressdepartureNode.getTextContent();
 
             if (!addressarrivalNode.getTextContent().equalsIgnoreCase(""))
                 addressarrival = addressarrivalNode.getTextContent();
 
             if (!paymenttypeNode.getTextContent().equalsIgnoreCase(""))
                 paymenttype = Integer.parseInt(paymenttypeNode.getTextContent());
 
             if (!departuretimeNode.getTextContent().equalsIgnoreCase(""))
                 departuretime = format.parse(departuretimeNode.getTextContent());
 
             if (!commentNode.getTextContent().equalsIgnoreCase(""))
                 comment = commentNode.getTextContent();
 
             if (!orderIdNode.getTextContent().equalsIgnoreCase(""))
                 orderId = orderIdNode.getTextContent();
 
             orders.add(new CostOrder(this, orderId, nominalcost, addressdeparture, carClass, comment,
                     addressarrival, paymenttype, departuretime));
 
             if (!nicknameNode.getTextContent().equalsIgnoreCase("")) {
                 nickname = nicknameNode.getTextContent();
 
                 if (!quantityNode.getTextContent().equalsIgnoreCase(""))
                     quantity = Integer.parseInt(quantityNode.getTextContent());
                 orders.get(i).setAbonent(nickname);
                 orders.get(i).setRides(quantity);
             }
         }
 
         Node candidateNode = doc.getElementsByTagName("candidateorderid").item(0);
         String candidate = "";
         if (!candidateNode.getTextContent().equalsIgnoreCase(""))
             candidate = candidateNode.getTextContent();
         Log.d("My_tag", candidate + " " + candidateId);
         if (candidate != "" && !candidateId.equalsIgnoreCase(candidate)) {
             candidateId = candidate;
             Intent intent = new Intent(this, CandidateOrderActivity.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             Bundle bundle = new Bundle();
             bundle.putString("id", candidate);
             intent.putExtras(bundle);
             startActivity(intent);
         }
 
 
         Driver driver = TaxiApplication.getDriver(this);
         driver.setFreeOrders(orders);
         arrayAdapter.notifyDataSetChanged();
 
         if (length != orders.size()) {
             MediaPlayer mp = MediaPlayer.create(getBaseContext(), (R.raw.new_sound));
             mp.start();
         }
 
         // driver = new Driver(status, carClass, ordersCount, district,
         // subdistrict);
 
         // itemsList = new ArrayList<Map<String, String>>();
         // itemsList.add(createItem("item", "Мои закакзы: " +
         // driver.getOrdersCount()));
         // itemsList.add(createItem("item", "Статус: " +
         // driver.getStatusString()));
         // itemsList.add(createItem("item", "Свободные заказы"));
         // if (driver.getStatus() != 1)
         // itemsList
         // .add(createItem("item", "Район: " + driver.getDistrict() + "," +
         // driver.getSubdistrict()));
         // itemsList.add(createItem("item", "Класс: " +
         // driver.getClassAutoString()));
         // itemsList.add(createItem("item", "Отчет"));
         // itemsList.add(createItem("item", "Звонок из офиса"));
         // itemsList.add(createItem("item", "Настройки"));
         Node refreshperiodNode = doc.getElementsByTagName("refreshperiod").item(0);
         Integer newrefreshperiod = null;
         if (!refreshperiodNode.getTextContent().equalsIgnoreCase(""))
             newrefreshperiod = Integer.valueOf(refreshperiodNode.getTextContent());
 
         boolean update = false;
 
         Log.d("My_tag", refreshperiod + " " + newrefreshperiod + " " + update);
 
         if (newrefreshperiod != null) {
             if (refreshperiod != newrefreshperiod) {
                 refreshperiod = newrefreshperiod;
                 update = true;
             }
         }
 
         Log.d("My_tag", refreshperiod + " " + newrefreshperiod + " " + update);
 
         if (update && refreshperiod != null) {
             if (start) {
                 myTimer.cancel();
                 start = true;
                 Log.d("My_tag", "cancel timer");
             }
             final Handler uiHandler = new Handler();
 
             TimerTask timerTask = new TimerTask() { // Определяем задачу
                 @Override
                 public void run() {
                     uiHandler.post(new Runnable() {
                         @Override
                         public void run() {
                             getOrders();
                         }
                     });
                 }
             };
 
             myTimer.schedule(timerTask, 1000 * refreshperiod, 1000 * refreshperiod);
         }
 
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         doUnbindService();
         if (myTimer != null)
             myTimer.cancel();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         doBindService();
         getOrders();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         if (requestCode == REQUEST_EXIT) {
             if (resultCode == RESULT_OK) {
                 this.finish();
             }
         }
     }
 
 }
