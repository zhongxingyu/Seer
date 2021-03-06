 package gov.nysenate.inventory.android;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.text.Html;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Delivery3 extends SenateActivity
 {
     public TextView loc_details;
     String location = "";
     String nuxrpd = "";
     String status = "";
     String URL = "";
     String res = "";
     ListView listview;
     String NUXRACCPTSIGN = "";
     String NADELIVERBY = "";
     String NAACCEPTBY = "";
     private SignatureView sign;
     private byte[] imageInByte = {};
     private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
     ClearableEditText commentsEditText;
     int nuxrefem = -1;
     public ArrayList<Employee> employeeHiddenList = new ArrayList<Employee>();
     public ArrayList<String> employeeNameList = new ArrayList<String>();
     ClearableAutoCompleteTextView naemployeeView;
     String requestTaskType = "";
     String employeeList = "";
     private String DECOMMENTS = null;
     Button btnDeliv3ClrSig;
     Button btnDelivery3Back;
     Button btnDelivery3Cont;
     InvItem invItem;
     InvSelListViewAdapter invAdapter;
     public static ProgressBar progBarDelivery3;
     boolean positiveButtonPressed = false;
     Activity currentActivity;
     String timeoutFrom = "delivery1";    
     public final int DELIVERYDETAILS_TIMEOUT = 101, POSITIVEDIALOG_TIMEOUT = 102, KEEPALIVE_TIMEOUT = 103;        
 
     public ArrayList<InvItem> invList = new ArrayList<InvItem>();
 
     // ArrayList<DeliveryItem> deliveryList = new ArrayList<DeliveryItem>();
     // DeliveryItemViewAdapter adapter2;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_delivery3);
         registerBaseActivityReceiver();
         currentActivity = this;
 
         // Get the data from previous activity
         Intent intent = getIntent();
         location = intent.getStringExtra("location");
         nuxrpd = intent.getStringExtra("nuxrpd").trim();
 
         // Set the location in textview
         loc_details = (TextView) findViewById(R.id.textView2);
         loc_details.setText("Delivery to: " + location);
         // Get the barcode numbers from the server and set it to the listview
 
         // Configure Image Buttons
         btnDeliv3ClrSig = (Button) findViewById(R.id.btnDeliv3ClrSig);
         btnDelivery3Back = (Button) findViewById(R.id.btnDelivery3Back);
         btnDelivery3Cont = (Button) findViewById(R.id.btnDelivery3Cont);
 
         // Setup ProgressBar
         progBarDelivery3 = (ProgressBar) findViewById(R.id.progBarDelivery3);
 
         // Setup the Signature Field (sign) and Delivery Comments
         // (commentsEditText)
         sign = (SignatureView) findViewById(R.id.sv_accept_sign_delivery3);
         sign.setMinDimensions(200, 100);
         commentsEditText = (ClearableEditText) findViewById(R.id.deliveryCommentsEditText);
         commentsEditText
                 .setClearMsg("Do you want to clear the Delivery Comments?");
         commentsEditText.showClearMsg(true);
 
         naemployeeView = (ClearableAutoCompleteTextView) findViewById(R.id.naemployee);
         naemployeeView
                 .setClearMsg("Do you want to clear the name of the signer?");
         naemployeeView.showClearMsg(true);
         naemployeeView
                 .setOnItemClickListener(new AdapterView.OnItemClickListener()
                 {
                     @Override
                     public void onItemClick(AdapterView<?> parent, View view,
                             int position, long id) {
                         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                         imm.hideSoftInputFromWindow(
                                 naemployeeView.getWindowToken(), 0);
                     }
                 });
 
         getDeliveryDetails();
 
         // list of name of the employee for autocomplete (get from server and
         // populate the autocomplete textview)
 
         // Signature from 'Accepted By'
 
         // Save the signature on server (Received By), comments, Name
         // currently hardcoding
         // Brian : Please assign values to following variables after saving the
         // signature and name
        NUXRACCPTSIGN = "1111";
         NADELIVERBY = "BH";
 
         NAACCEPTBY = "Abc,xyz";// note : we need to have comma in name (query is
                                // formated that way)
 
         // Get the results for the Employee List and now do the actual setting
         // of the Signing Employee
         // Dropdown.
         try {
             JSONArray jsonArray = new JSONArray(employeeList);
             for (int x = 0; x < jsonArray.length(); x++) {
                 JSONObject jo = new JSONObject();
                 jo = jsonArray.getJSONObject(x);
                 Employee currentEmployee = new Employee();
                 currentEmployee.setEmployeeData(jo.getInt("nuxrefem"),
                         jo.getString("naemployee"));
                 employeeHiddenList.add(currentEmployee);
                 employeeNameList.add(jo.getString("naemployee"));
             }
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         Collections.sort(employeeNameList);
 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_dropdown_item_1line, employeeNameList);
 
         // for origin dest code
         naemployeeView.setThreshold(1);
        naemployeeView.setAdapter(adapter);
         Delivery2.progBarDelivery2.setVisibility(View.INVISIBLE);
 
     }
     
     public void startTimeout(int timeoutType) {
         Intent intentTimeout = new Intent(this, LoginActivity.class);
         intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
         startActivityForResult(intentTimeout, timeoutType);
     }
     
 
     @Override
     protected void onResume() {
         super.onResume();
         positiveButtonPressed = false;
     }
 
     public void noServerResponse() {
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
         // set title
         alertDialogBuilder.setTitle("NO SERVER RESPONSE");
 
         // set dialog message
         alertDialogBuilder
                 .setMessage(
                         Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                 .setCancelable(false)
                 .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         // if this button is clicked, just close
                         // the dialog box and do nothing
                         Context context = getApplicationContext();
 
                         CharSequence text = "No action taken due to NO SERVER RESPONSE";
                         int duration = Toast.LENGTH_SHORT;
 
                         Toast toast = Toast.makeText(context, text, duration);
                         toast.setGravity(Gravity.CENTER, 0, 0);
                         toast.show();
 
                         dialog.dismiss();
                     }
                 });
 
         // create alert dialog
         AlertDialog alertDialog = alertDialogBuilder.create();
 
         // show it
         alertDialog.show();
     }
 
     public int findEmployee(String employeeName) {
         for (int x = 0; x < employeeHiddenList.size(); x++) {
             if (employeeName
                     .equals(employeeHiddenList.get(x).getEmployeeName())) {
                 return x;
             }
         }
         return -1;
     }
 
     public void continueButton(View view) {
         String employeePicked = naemployeeView.getEditableText().toString();
         NAACCEPTBY = "";
         if (employeePicked.trim().length() > 0) {
             int foundEmployee = this.findEmployee(employeePicked);
 
             if (foundEmployee < 0) {
                 nuxrefem = -1;
             } else {
                 nuxrefem = this.employeeHiddenList.get(foundEmployee)
                         .getEmployeeXref();
             }
         } else {
             nuxrefem = -1;
         }
 
         if (nuxrefem < 0) {
             Context context = getApplicationContext();
             int duration = Toast.LENGTH_SHORT;
             if (naemployeeView.getEditableText().toString().trim().length() > 0) {
                 Toast toast = Toast.makeText(context,
                         "!!ERROR: No xref# found for employee", duration);
                 toast.setGravity(Gravity.CENTER, 0, 0);
                 toast.show();
             } else {
                 Toast toast = Toast
                         .makeText(
                                 context,
                                 "!!ERROR: You must first pick an employee name for the signature.",
                                 3000);
                 toast.setGravity(Gravity.CENTER, 0, 0);
                 toast.show();
             }
             return;
         }
 
         if (!sign.isSigned()) {
             Context context = getApplicationContext();
             Toast toast = Toast.makeText(context,
                     "!!ERROR: Employee must also sign within the Red box.",
                     3000);
             toast.setGravity(Gravity.CENTER, 0, 0);
             toast.show();
             return;
         }
         if (!keepAlive()) {
             return;
         }
         
         int numItemsDelivered = invAdapter.getSelectedItems(true).size();
         AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
         confirmDialog.setTitle("Delivery Confirmation");
         confirmDialog.setMessage("Are you sure you want to deliver these "
                 + numItemsDelivered + " items?");
         confirmDialog.setPositiveButton("Yes",
                 new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         /*
                          * Prevent Multiple clicks on button, which will cause
                          * issues witn the database inserting multiple nuxrpds
                          * for the same pickup.
                          */
 
                         if (positiveButtonPressed) {
                             /*
                              * Context context = getApplicationContext(); int
                              * duration = Toast.LENGTH_SHORT;
                              * 
                              * Toast toast = Toast.makeText(context,
                              * "Button was already been pressed.",
                              * Toast.LENGTH_SHORT);
                              * toast.setGravity(Gravity.CENTER, 0, 0);
                              * toast.show();
                              */
 
                         } else {
                             positiveButtonPressed = true;
                             positiveDialog();
                         }
 
                     }
                 });
 
         confirmDialog.setNegativeButton("No",
                 new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         // Continue in same activity.
                     }
                 });
 
         AlertDialog dialog = confirmDialog.create();
         dialog.show();
     }
 
     private void positiveDialog() {
         progBarDelivery3.setVisibility(ProgressBar.VISIBLE);
         this.btnDelivery3Cont.getBackground().setAlpha(45);
 
         try {
             NAACCEPTBY = URLEncoder.encode(this.naemployeeView.getText()
                     .toString(), "UTF-8");
         } catch (UnsupportedEncodingException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
 
         if (this.invAdapter == null) {
             this.invAdapter = (InvSelListViewAdapter) this.listview
                     .getAdapter();
         }
         int NUSENATE = new InvItem().NUSENATE;
         DECOMMENTS = null;
         try {
             DECOMMENTS = URLEncoder.encode(this.commentsEditText.getText()
                     .toString(), "UTF-8");
         } catch (UnsupportedEncodingException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
 
         // check network connection
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             // fetch data
             status = "yes";
 
             AsyncTask<String, String, String> resr1;
             try {
                 // Get the URL from the properties
                 String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                         .toString();
                 this.requestTaskType = "Delivery";
                 String deliveryURL = URL
                         + "/DeliveryConfirmation?NUXRPD="
                         + nuxrpd
                         + "&NADELIVERBY="
                         + LoginActivity.nauser
                         + "&NAACCEPTBY="
                         + NAACCEPTBY
                         + Formatter.generateGetArray("deliveryItemsStr[]", this.invAdapter.getAllItems())
                         + Formatter.generateGetArray("checkedStr[]", this.invAdapter.getSelectedItems(true));
 
                 resr1 = new RequestTask().execute(URL + "/ImgUpload?nauser="
                         + LoginActivity.nauser + "&nuxrefem=" + nuxrefem,
                         deliveryURL);
 
                 try {
                     res = null;
                     res = resr1.get().trim().toString();
                     if (res == null) {
                         noServerResponse();
                         return;
                     } else if (res.indexOf("Session timed out") > -1) {
                         startTimeout(this.POSITIVEDIALOG_TIMEOUT);
                         return;
                     }
                     
                 } catch (NullPointerException e) {
                     noServerResponse();
                     return;
                 }
                 Log.i("Confirm Response", "res:" + res);
 
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (ExecutionException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             status = "yes1";
         } else {
             // display error
             status = "no";
         }
 
         // Display Toster
         Context context = getApplicationContext();
         CharSequence text = res;
         if (res.length() == 0) {
             text = "Database not updated";
         }
 
         int duration = Toast.LENGTH_SHORT;
         Toast toast = Toast.makeText(context, text, duration);
         toast.setGravity(Gravity.CENTER, 0, 0);
         toast.show();
 
         // 3. send the intent to the Move Menu Activity
 
         // Intent intent = new Intent(this, MenuActivity.class);
         Intent intent = new Intent(this, Move.class);
         startActivity(intent);
         overridePendingTransition(R.anim.in_right, R.anim.out_left);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
         case android.R.id.home:
             Toast toast = Toast.makeText(getApplicationContext(), "Going Back",
                     Toast.LENGTH_SHORT);
             toast.setGravity(Gravity.CENTER, 0, 0);
             toast.show();
             this.onBackPressed();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_delivery3, menu);
         return true;
     }
 
     public void clearSignatureButton(View view) {
         Bitmap clearedSignature = BitmapFactory.decodeResource(getResources(),
                 R.drawable.simplethinborder);
         if (clearedSignature == null) {
             Log.i("ClearSig", "Signature drawable was NULL");
         } else {
             Log.i("ClearSig", "Signature size:" + clearedSignature.getWidth()
                     + " x " + clearedSignature.getHeight());
         }
         sign.clearSignature();
     }
 
     /**
      * Fire an intent to start the speech recognition activity.
      */
     public void startCommentsSpeech(View view) {
         if (view.getId() == R.id.deliveryCommentsSpeechButton) {
             Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
             intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                     RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
             intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                     "Delivery Comments Speech");
             startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
         }
     }
 
     /**
      * Handle the results from the recognition activity.
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.i("onActivityResult", "requestCode:"+requestCode+" resultCode:"+resultCode);
         switch (requestCode) {
         case DELIVERYDETAILS_TIMEOUT:
             Log.i("onActivityResult", "DELIVERYDETAILS_TIMEOUT");
             if (resultCode == RESULT_OK) {
                 getDeliveryDetails();
                 break;
             }
         case POSITIVEDIALOG_TIMEOUT:
             Log.i("onActivityResult", "POSITIVEDIALOG_TIMEOUT");
             //positiveDialog();
             break;
         case KEEPALIVE_TIMEOUT:
             Log.i("onActivityResult", "KEEPALIVE_TIMEOUT");
             new Timer().schedule(new TimerTask() {          
                 @Override
                 public void run() {
                     InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                     imm.hideSoftInputFromWindow(
                             naemployeeView.getWindowToken(), 0);
                 }
             }, 50);                 
             
             break;
         case VOICE_RECOGNITION_REQUEST_CODE:
                 if (resultCode == RESULT_OK) {
             // Fill the list view with the strings the recognizer thought it
             // could have heard
             ArrayList<String> matches = data
                     .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
             commentsEditText.setText(matches.get(0));
              } 
         }
 
         super.onActivityResult(requestCode, resultCode, data);
     }
     public void backButton(View view) {
         super.onBackPressed();
     }
 
     // class for connecting to internet and sending HTTP request to server
     class RequestTask extends AsyncTask<String, String, String>
     {
 
         @Override
         protected String doInBackground(String... uri) {
             HttpClient httpclient = LoginActivity.httpClient;
             HttpResponse response;
             String responseString = null;
             Log.i("WEBRECEIVE", "requestTaskType:" + requestTaskType);
             if (requestTaskType.equalsIgnoreCase("Delivery")) {
                 try {
                     // Scale the Image
                     
                     String NUXRRELSIGN = "";
 
                     ByteArrayOutputStream bs = new ByteArrayOutputStream();
                     Bitmap bitmap = sign.getImage();
                     Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                             200, 40, true);
                     // System.out.println("SCALED SIZE:"+bitmap.getByteCount()+" -> "+scaledBitmap.getByteCount());
                     for (int x = 0; x < scaledBitmap.getWidth(); x++) {
                         for (int y = 0; y < scaledBitmap.getHeight(); y++) {
                             String strColor = String.format("#%06X",
                                     0xFFFFFF & scaledBitmap.getPixel(x, y));
                             if (strColor.equals("#000000")
                                     || scaledBitmap.getPixel(x, y) == Color.TRANSPARENT) {
                                 // System.out.println("********"+x+" x "+y+" SETTING COLOR TO WHITE");
                                 scaledBitmap.setPixel(x, y, Color.WHITE);
                             }
                         }
                     }
                     scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bs);
                     imageInByte = bs.toByteArray();
 
                     // Post the Image to the Web Server
                     
                     StringBuilder urls = new StringBuilder();
                     urls.append(uri[0].trim());
                     if (uri[0].indexOf("?") > -1) {
                         if (!uri[0].trim().endsWith("?")) {
                             urls.append("&");
                         }
                     } else {
                         urls.append("?");
                     }
                     urls.append("userFallback=");
                     urls.append(LoginActivity.nauser);
 
                     URL url = new URL(urls.toString());
 
                     HttpClient httpClient = LoginActivity.httpClient;
                     
                     if (httpClient == null) {
                         Log.i(RequestTask.class.getName(),
                                 "MainActivity.httpClient was null so it is being reset");
                         LoginActivity.httpClient = new DefaultHttpClient();
                         httpclient = LoginActivity.httpClient;
                     }
                     
                     HttpContext localContext = new BasicHttpContext();  
                     MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);                      
                             
                     HttpPost httpPost = new HttpPost(urls.toString());    
                     entity.addPart("Signature", new ByteArrayBody(imageInByte, "temp.jpg"));  
                     httpPost.setEntity(entity);                      
 
                    /* HttpURLConnection conn = (HttpURLConnection) url
                             .openConnection();
                     // Set connection parameters.
                     conn.setDoInput(true);
                     conn.setDoOutput(true);
                     conn.setUseCaches(false);
 
                     // Set content type to PNG
                     conn.setRequestProperty("Content-Type", "image/jpg");
                     OutputStream outputStream = conn.getOutputStream();
                     OutputStream out = outputStream;
                     // Write out the bytes of the content string to the stream.
                     out.write(imageInByte);
                     out.flush();
                     out.close();
                     // Read response from the input stream.
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(conn.getInputStream()));
                     String temp;
                     while ((temp = in.readLine()) != null) {
                         responseString += temp + "\n";
                     }
                     temp = null;
                     in.close();*/
                     // System.out.println("Server response:\n'" + responseString
                     // + "'");
                     
                     // Get Server Response to the posted Image
                     
                     response = httpClient.execute(httpPost, localContext);  
                     BufferedReader reader = new BufferedReader(new InputStreamReader( response.getEntity().getContent(), "UTF-8"));  
                     responseString= reader.readLine();                      
                     int nuxrsignLoc = responseString.indexOf("NUXRSIGN:");
                     if (nuxrsignLoc > -1) {
                         NUXRACCPTSIGN = responseString
                                 .substring(nuxrsignLoc + 9)
                                 .replaceAll("\r", "").replaceAll("\n", "");
                     } else {
                         NUXRACCPTSIGN = responseString.replaceAll("\r", "")
                                 .replaceAll("\n", "");
                     }
 
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 try {
                     StringBuilder urls = new StringBuilder();
                     urls.append(uri[1].trim());
                     if (uri[1].indexOf("?") > -1) {
                         if (!uri[1].trim().endsWith("?")) {
                             urls.append("&");
                         }
                     } else {
                         urls.append("?");
                     }
                     urls.append("userFallback=");
                     urls.append(LoginActivity.nauser);
                     response = httpclient.execute(new HttpGet(urls.toString()
                             + "&NUXRACCPTSIGN=" + NUXRACCPTSIGN
                             + "&DECOMMENTS=" + DECOMMENTS));
 
                     StatusLine statusLine = response.getStatusLine();
                     if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                         ByteArrayOutputStream out = new ByteArrayOutputStream();
                         response.getEntity().writeTo(out);
                         out.close();
                         responseString = out.toString();
                     } else {
                         // Closes the connection.
                         response.getEntity().getContent().close();
                         throw new IOException(statusLine.getReasonPhrase());
                     }
                 } catch (ClientProtocolException e) {
                     // TODO Handle problems..
                 } catch (IOException e) {
                     // TODO Handle problems..
                 }
                 res = responseString;
                 Log.i("WEBRECEIVE", "requestTaskType:" + requestTaskType
                         + " response:" + res);
 
                 return responseString;
             } else if (requestTaskType.equalsIgnoreCase("EmployeeDeliveryList")) {
 
                 // Get List of Employees for the Signing Employee Dropdown
 
                 try {
                     StringBuilder urls = new StringBuilder();
                     urls.append(uri[0].trim());
                     if (uri[0].indexOf("?") > -1) {
                         if (!uri[0].trim().endsWith("?")) {
                             urls.append("&");
                         }
                     } else {
                         urls.append("?");
                     }
                     urls.append("userFallback=");
                     urls.append(LoginActivity.nauser);
                     response = httpclient.execute(new HttpGet(urls.toString()));
                     StatusLine statusLine = response.getStatusLine();
                     if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                         ByteArrayOutputStream out = new ByteArrayOutputStream();
                         response.getEntity().writeTo(out);
                         out.close();
                         employeeList = out.toString();
                     } else {
                         // Closes the connection.
                         response.getEntity().getContent().close();
                         throw new IOException(statusLine.getReasonPhrase());
                     }
                 } catch (ClientProtocolException e) {
                     // TODO Handle problems..
                     employeeList = "";
                 } catch (IOException e) {
                     // TODO Handle problems..
                     employeeList = "";
                 }
 
                 try {
                     response = httpclient.execute(new HttpGet(uri[1]
                             + "&userFallback=" + LoginActivity.nauser));
                     StatusLine statusLine = response.getStatusLine();
                     if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                         ByteArrayOutputStream out = new ByteArrayOutputStream();
                         response.getEntity().writeTo(out);
                         out.close();
                         responseString = out.toString();
                     } else {
                         // Closes the connection.
                         response.getEntity().getContent().close();
                         throw new IOException(statusLine.getReasonPhrase());
                     }
                 } catch (ClientProtocolException e) {
                     // TODO Handle problems..
                     e.printStackTrace();
                 } catch (IOException e) {
                     // TODO Handle problems..
                     e.printStackTrace();
                 }
 
                 return responseString;
             } else if (requestTaskType.equalsIgnoreCase("KeepAlive")) {
 
                 // Get List of Employees for the Signing Employee Dropdown
 
                 try {
                     response = httpclient.execute(new HttpGet(uri[0]));
                     StatusLine statusLine = response.getStatusLine();
                     if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                         ByteArrayOutputStream out = new ByteArrayOutputStream();
                         response.getEntity().writeTo(out);
                         out.close();
                         responseString = out.toString();
                     } else {
                         // Closes the connection.
                         response.getEntity().getContent().close();
                         throw new IOException(statusLine.getReasonPhrase());
                     }
                 } catch (ClientProtocolException e) {
                     // TODO Handle problems..
                     e.printStackTrace();
                 } catch (IOException e) {
                     // TODO Handle problems..
                     e.printStackTrace();
                 }
                 return responseString;
             }
             return responseString;
 
         }
     }
     
     public void getDeliveryDetails(){
         // check network connection
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             // fetch data
             status = "yes";
 
             // Get the URL from the properties
             URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();
 
             this.requestTaskType = "EmployeeDeliveryList";
             AsyncTask<String, String, String> resr1 = new RequestTask()
                     .execute(URL + "/EmployeeList", URL
                             + "/DeliveryDetails?NUXRPD=" + nuxrpd);
 
             try {
                 try {
                     res = null;
                     res = resr1.get().trim().toString();
                     if (res == null) {
                         noServerResponse();
                         return;
                     } else if (res.indexOf("Session timed out") > -1) {
                         startTimeout(this.DELIVERYDETAILS_TIMEOUT);
                         return;
                     }
 
                 } catch (NullPointerException e) {
                     noServerResponse();
                     return;
                 }
                 // code for JSON
 
                 String jsonString = resr1.get().trim().toString();
                 JSONArray jsonArray = new JSONArray(jsonString);
 
                 for (int i = 0; i < jsonArray.length(); i++) {
 
                     JSONObject jo = new JSONObject();
                     jo = jsonArray.getJSONObject(i);
                     System.out.println(i + ":" + jo.toString());
                     String nusenate = jo.getString("nusenate");
                     String cdcategory = jo.getString("cdcategory");
                     String decommodityf = jo.getString("decommodityf");
                     String cdlocat = jo.getString("cdlocat");
 
                     // 3/15/13 BH Coded below to use InvItem Objects to display
                     // the list.
                     InvItem invItem = new InvItem(nusenate, cdcategory,
                             "EXISTING", decommodityf, cdlocat);
                     invList.add(invItem);
                 }
 
                 // Display the pickup data
                 listview = (ListView) findViewById(R.id.listView1);
                 listview.setItemsCanFocus(false);
                 // listview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
 
                 // listview.setAdapter(adapter2);
                 invAdapter = new InvSelListViewAdapter(getApplicationContext(),
                         R.layout.invlist_sel_item, invList);
                 listview.setAdapter(invAdapter);
                 // set everything as checked
                 invAdapter.setAllSelected(true);
                 invAdapter.setNotifyOnChange(true);
 
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 Log.i("InterruptedException", e.getMessage());
             } catch (ExecutionException e) {
                 // TODO Auto-generated catch block
                 Log.i("ExecutionException", e.getMessage());
                 e.printStackTrace();
             } catch (JSONException e) {
                 // TODO Auto-generated catch block
                 Log.i("JSONException", e.getMessage());
                 e.printStackTrace();
             }
             status = "yes1";
         } else {
             // display error
             status = "no";
         }      
         // Signature from 'Accepted By'
 
         // Save the signature on server (Received By), comments, Name
         // currently hardcoding
         // Brian : Please assign values to following variables after saving the
         // signature and name
         NUXRACCPTSIGN = "1111";
         NADELIVERBY = "BH";
 
         NAACCEPTBY = "Abc,xyz";// note : we need to have comma in name (query is
                                // formated that way)
 
         // Get the results for the Employee List and now do the actual setting
         // of the Signing Employee
         // Dropdown.
         try {
             JSONArray jsonArray = new JSONArray(employeeList);
             for (int x = 0; x < jsonArray.length(); x++) {
                 JSONObject jo = new JSONObject();
                 jo = jsonArray.getJSONObject(x);
                 Employee currentEmployee = new Employee();
                 currentEmployee.setEmployeeData(jo.getInt("nuxrefem"),
                         jo.getString("naemployee"));
                 employeeHiddenList.add(currentEmployee);
                 employeeNameList.add(jo.getString("naemployee"));
             }
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         Collections.sort(employeeNameList);
 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_dropdown_item_1line, employeeNameList);
 
         naemployeeView.setAdapter(adapter);
         
     }
     
     public boolean keepAlive() {
         // check network connection
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             // fetch data
             status = "yes";
 
             AsyncTask<String, String, String> resr1;
             try {
                 // Get the URL from the properties
                 String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                         .toString();
                 this.requestTaskType = "KeepAlive";
                 resr1 = new RequestTask().execute(URL + "/KeepSessionAlive");
 
                 try {
                     res = null;
                     res = resr1.get().trim().toString();
                     if (res == null) {
                         noServerResponse();
                         return false;
                     } else if (res.indexOf("Session timed out") > -1) {
                         startTimeout(this.KEEPALIVE_TIMEOUT);
                         return false;
                     }
                     
                 } catch (NullPointerException e) {
                     noServerResponse();
                     return false;
                 }
 
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (ExecutionException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             status = "yes1";
         } else {
             // display error
             status = "no";
         }
         return true;
         
     }
 }
