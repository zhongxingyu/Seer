 package ru.peppers;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import model.Driver;
 import model.Order;
 import model.SubDistrict;
 import myorders.MyCostOrder;
 import orders.CostOrder;
 import orders.NoCostOrder;
 import orders.PreliminaryOrder;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class FreeOrderActivity extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
 		nameValuePairs.add(new BasicNameValuePair("action", "list"));
 		nameValuePairs.add(new BasicNameValuePair("mode", "available"));
 		nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
 		nameValuePairs.add(new BasicNameValuePair("object", "order"));
 
 		Document doc = PhpData.postData(this, nameValuePairs, PhpData.newURL);
 		if (doc != null) {
 			Node responseNode = doc.getElementsByTagName("response").item(0);
 			Node errorNode = doc.getElementsByTagName("message").item(0);
 
 			if (responseNode.getTextContent().equalsIgnoreCase("failure"))
 				PhpData.errorFromServer(this, errorNode);
 			else {
 				try {
 					initMainList(doc);
 				} catch (Exception e) {
 					e.printStackTrace();
 					Log.d("My_tag", e.toString());
 					errorHandler();
 				}
 			}
 		}
 		// TODO: 
 		/*
 		 * new
 		 * AlertDialog.Builder(this).setTitle(this.getString(R.string.info)).
 		 * setMessage(this.getString(R.string.noOrders))
 		 * .setNeutralButton(this.getString(R.string.close), new
 		 * OnClickListener() {
 		 *
 		 * @Override public void onClick(DialogInterface dialog, int which) { //
 		 * Bundle extras = getIntent().getExtras(); // int id =
 		 * extras.getInt("id");
 		 *
 		 * Intent intent = new Intent(FreeOrderActivity.this,
 		 * MainListActivity.class); // Bundle bundle = new Bundle(); //
 		 * bundle.putInt("id", id); // intent.putExtras(bundle);
 		 * startActivity(intent); } }).show();
 		 */
 	}
 
 	private void errorHandler() {
 		new AlertDialog.Builder(this).setTitle(this.getString(R.string.error_title))
 				.setMessage(this.getString(R.string.error_message))
 				.setNeutralButton(this.getString(R.string.close), null).show();
 	}
 
 	private void initMainList(Document doc) throws DOMException, ParseException {
 		NodeList nodeList = doc.getElementsByTagName("item");
 		ArrayList<Order> orders = new ArrayList<Order>();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			// nominalcost -   
 			// class -   (0 -  , 1 - , 2 - ,
 			// 3 - )
 			// addressdeparture -   
 			// departuretime -  ( )
 			// paymenttype -   (0 - , 1 - )
 			// invitationtime -   ( )
 			// quantity -     
 			// comment - 
 			// nickname -   ( )
 			// registrationtime -   
 			// addressarrival -  
 
 			Element item = (Element) nodeList.item(i);
 
 			Node nominalcostNode = item.getElementsByTagName("nominalcost").item(0);
 			Node classNode = item.getElementsByTagName("class").item(0);
 			Node addressdepartureNode = item.getElementsByTagName("addressdeparture").item(0);
 			Node departuretimeNode = item.getElementsByTagName("departuretime").item(0);
 			Node paymenttypeNode = item.getElementsByTagName("paymenttype").item(0);
 			Node quantityNode = item.getElementsByTagName("quantity").item(0);
 			Node commentNode = item.getElementsByTagName("comment").item(0);
 			Node nicknameNode = item.getElementsByTagName("nickname").item(0);
 			Node registrationtimeNode = item.getElementsByTagName("registrationtime").item(0);
 			Node addressarrivalNode = item.getElementsByTagName("addressarrival").item(0);
 			Node orderIdNode = item.getElementsByTagName("orderid").item(0);
 
 			Integer nominalcost = null;
 			Integer carClass = 0;
 			String addressdeparture = null;
 			Date departuretime = null;
 			Integer paymenttype = null;
 			Integer quantity = null;
 			String comment = null;
 			String nickname = null;
 			Date registrationtime = null;
 			String addressarrival = null;
 			String orderId = null;
 
 			// if(departuretime==null)
 			// //TODO: 
 			// else
 			// //TODO:
 
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
 
 			if (!classNode.getTextContent().equalsIgnoreCase(""))
 				carClass = Integer.valueOf(classNode.getTextContent());
 
 			if (!nominalcostNode.getTextContent().equalsIgnoreCase(""))
 				nominalcost = Integer.parseInt(nominalcostNode.getTextContent());
 
 			if (!registrationtimeNode.getTextContent().equalsIgnoreCase(""))
 				registrationtime = format.parse(registrationtimeNode.getTextContent());
 
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
 
 			
 			orders.add(new CostOrder(this,orderId, nominalcost, registrationtime, addressdeparture, carClass, comment,
 					addressarrival, paymenttype, departuretime));
 
 			if (!nicknameNode.getTextContent().equalsIgnoreCase("")) {
 				nickname = nicknameNode.getTextContent();
 
 				if (!quantityNode.getTextContent().equalsIgnoreCase(""))
 					quantity = Integer.parseInt(quantityNode.getTextContent());
 				orders.get(i).setAbonent(nickname);
 				orders.get(i).setRides(quantity);
 			}
 		}
 
 		Driver driver = TaxiApplication.getDriver();
 		driver.setFreeOrders(orders);
 		// driver = new Driver(status, carClass, ordersCount, district,
 		// subdistrict);
 
 		// itemsList = new ArrayList<Map<String, String>>();
 		// itemsList.add(createItem("item", " : " +
 		// driver.getOrdersCount()));
 		// itemsList.add(createItem("item", ": " +
 		// driver.getStatusString()));
 		// itemsList.add(createItem("item", " "));
 		// if (driver.getStatus() != 1)
 		// itemsList
 		// .add(createItem("item", ": " + driver.getDistrict() + "," +
 		// driver.getSubdistrict()));
 		// itemsList.add(createItem("item", ": " +
 		// driver.getClassAutoString()));
 		// itemsList.add(createItem("item", ""));
 		// itemsList.add(createItem("item", "  "));
 		// itemsList.add(createItem("item", ""));
 
 		ListView lv = (ListView) findViewById(R.id.mainListView);
 
 		ArrayAdapter<Order> arrayAdapter = new ArrayAdapter<Order>(this, android.R.layout.simple_list_item_1, orders);
 
 		lv.setAdapter(arrayAdapter);
 		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long index) {
 				// Bundle extras = getIntent().getExtras();
 				// int id = extras.getInt("id");
 
 				Intent intent = new Intent(FreeOrderActivity.this, FreeOrderItemActivity.class);
 				Bundle bundle = new Bundle();
 				// bundle.putInt("id", id);
 				bundle.putInt("index", position);
 				intent.putExtras(bundle);
 				startActivity(intent);
 			}
 		});
 	}
 }
