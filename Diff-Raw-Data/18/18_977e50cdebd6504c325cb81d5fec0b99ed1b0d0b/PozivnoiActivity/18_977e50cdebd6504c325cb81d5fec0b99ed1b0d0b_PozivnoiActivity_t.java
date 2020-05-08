 package ru.peppers;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import model.Driver;
 import model.Message;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class PozivnoiActivity extends Activity {
     private static final String URL_APK = "http://github.com/Icesman/taxi_project/blob/master/bin/TaxiProject.apk";
    private static final String URL_MANIFEST = "https://raw.github.com/Icesman/taxi_project/master/AndroidManifest.xml";
     private static final String MY_TAG = "My_tag";
     protected static final String PREFS_NAME = "MyNamePrefs1";
     protected Integer index;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         boolean update = false;
         TaxiApplication.setDriver(new Driver(0, 0, 0, "", ""));
         Log.d("My_tag", "INIT DRIVER");
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 
         SharedPreferences.Editor editor = settings.edit();
         try {
             if (settings.getInt("version", 0) != 0) //    
                 if (settings.getInt("version", 0) != this.getPackageManager().getPackageInfo(
                         this.getPackageName(), 0).versionCode) { // 
                     // 
                     // 
                     // 
                     // 
                     // 
                     // 
                     // 
                     // 
                     editor.putInt("version", this.getPackageManager()
                             .getPackageInfo(this.getPackageName(), 0).versionCode);
                     //         ?
                 } else {
                     //  
                     Log.d("My_tag", "Newest version");
                     // List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                     // 1);
                     // nameValuePairs.add(new BasicNameValuePair("action",
                     // "getversion"));
                     // Document doc = PhpData.postData(PozivnoiActivity.this,
                     // nameValuePairs);
                     // if (doc != null) {
                     try {
                         //    
                         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                         DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(URL_MANIFEST);
                         //doc.getDocumentElement().normalize();
                         //NamedNodeMap idNode = doc.getElementsByTagName("manifest").item(0).getAttributes();
                         //Log.d("My_tag", idNode.toString());
                        Log.d("My_tag", doc.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("android:versionCode").getTextContent());
                        Log.d("My_tag", String.valueOf(settings.getInt("version", 0)));
                         //index = Integer.valueOf(idNode.getTextContent());
                         if (settings.getInt("version", 0) < 99) {
                             update = true;
                             //    
                             initDialog();
                             //       
                             //   
                         }
 
                    }catch (SAXParseException e) {
                        Log.d("My_tag", String.valueOf(e.getColumnNumber()));
                       // new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                         //       .setMessage(e.toString()).setNeutralButton("", null).show();
                     } catch (Exception e) {
                         Log.d("My_tag", e.toString());
                        // new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                          //       .setMessage(e.toString()).setNeutralButton("", null).show();
                     }
                 }
             else {
                 //  
                 Log.d("My_tag", "First version");
                 editor.putInt("version",
                         this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode);
                 //         
                 // ?
             }
         } catch (NameNotFoundException e) {
             new AlertDialog.Builder(PozivnoiActivity.this).setTitle("").setMessage(e.toString())
                     .setNeutralButton("", null).show();
         }
         editor.commit();
         if (!update) {
             init(settings);
         }
     }
 
     private void init(SharedPreferences settings) {
         boolean isFirstTime = settings.getBoolean("isFirstTime", false);
         if (!isFirstTime) {
             initRegistration();
             return;
         }
         String pozivnoi = settings.getString("pozivnoidata", "");
 
         if (pozivnoi == "") {
             setContentView(R.layout.login);
             EditText pozivnoiEditText = (EditText) findViewById(R.id.pozivnoiEditText);
 
             InputFilter filter = new InputFilter() {
                 public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
                         int dend) {
                     for (int i = start; i < end; i++) {
                         if (!Character.isDigit(source.charAt(i))) {
                             return "";
                         }
                     }
                     return null;
                 }
             };
             pozivnoiEditText.setFilters(new InputFilter[] { filter });
 
             pozivnoiEditText.setOnKeyListener(new EditText.OnKeyListener() {
 
                 @Override
                 public boolean onKey(View v, int keyCode, KeyEvent event) {
 
                     EditText pozivnoiEditText = (EditText) v;
 
                     if (keyCode == EditorInfo.IME_ACTION_SEARCH || keyCode == EditorInfo.IME_ACTION_DONE
                             || event.getAction() == KeyEvent.ACTION_DOWN
                             && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 
                         if (!event.isShiftPressed() && pozivnoiEditText.getText().toString().length() != 0)
                             return loginWithPozivnoi(pozivnoiEditText.getText().toString());
                         else
                             new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                                     .setMessage("    .")
                                     .setNeutralButton("", null).show();
 
                     }
                     return false; // pass on to other listeners.
                 }
 
             });
 
             Button passwordButton = (Button) findViewById(R.id.pozivnoiButton);
             passwordButton.setOnClickListener(new Button.OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     EditText pozivnoiEditText = (EditText) findViewById(R.id.pozivnoiEditText);
                     if (pozivnoiEditText.getText().toString().length() != 0)
                         loginWithPozivnoi(pozivnoiEditText.getText().toString());
                     else
                         new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                                 .setMessage("    .")
                                 .setNeutralButton("", null).show();
                 }
 
             });
         } else {
             loginWithPozivnoi(pozivnoi);
         }
     }
 
     private void initRegistration() {
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("");
         builder.setMessage("      ");
         builder.setCancelable(false);
 
         final EditText input = new EditText(this);
 
         InputFilter filter = new InputFilter() {
             public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
                     int dend) {
                 for (int i = start; i < end; i++) {
                     if (!Character.isDigit(source.charAt(i))) {
                         return "";
                     }
                 }
                 return null;
             }
         };
         input.setFilters(new InputFilter[] { filter });
 
         input.setOnKeyListener(new EditText.OnKeyListener() {
 
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) {
 
                 EditText input = (EditText) v;
 
                 if (keyCode == EditorInfo.IME_ACTION_SEARCH || keyCode == EditorInfo.IME_ACTION_DONE
                         || event.getAction() == KeyEvent.ACTION_DOWN
                         && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 
                     if (!event.isShiftPressed() && input.getText().toString().length() != 0) {
                         getRequest(input.getText().toString());
                         return true;
                     } else
                         new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                                 .setMessage("    .")
                                 .setNeutralButton("", null).show();
 
                 }
                 return false; // pass on to other listeners.
             }
 
         });
 
         builder.setView(input);
         builder.setPositiveButton("", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 dialog.dismiss();
                 String pozivnoi = input.getText().toString();
                 getRequest(pozivnoi);
             }
 
         });
         builder.setNegativeButton("", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 finish();
             }
 
         });
         AlertDialog alert = builder.create();
         alert.show();
 
     }
 
     static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
     static Random rnd = new Random();
 
     String randomString(int len) {
         StringBuilder sb = new StringBuilder(len);
         for (int i = 0; i < len; i++)
             sb.append(AB.charAt(rnd.nextInt(AB.length())));
         return sb.toString();
     }
 
     private void getRequest(String pozivnoi) {
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         String symbols = randomString(24);
 
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
         nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
         nameValuePairs.add(new BasicNameValuePair("object", "app"));
         nameValuePairs.add(new BasicNameValuePair("action", "authrequest"));
         nameValuePairs.add(new BasicNameValuePair("devserial", symbols));
         nameValuePairs.add(new BasicNameValuePair("drvnumber", pozivnoi));
         // TODO:  
         Document doc = PhpData.postData(PozivnoiActivity.this, nameValuePairs,
                 "https://www.abs-taxi.ru/fcgi-bin/office/cman.fcgi");
         if (doc != null) {
 
             Node responseNode = doc.getElementsByTagName("response").item(0);
             Node errorNode = doc.getElementsByTagName("message").item(0);
 
             if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                 new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                         .setMessage(errorNode.getTextContent())
                         .setNeutralButton("", new OnClickListener() {
                             @Override
                             public void onClick(DialogInterface arg0, int arg1) {
                                 initRegistration();
                             }
                         }).show();
             else {
                 // TODO:   
                 SharedPreferences.Editor editor = settings.edit();
                 editor.putBoolean("isFirstTime", true);
                 editor.putString("pozivnoidata", pozivnoi);
                 editor.putString("password", doc.getElementsByTagName("password").item(0).getTextContent());
                 editor.putString("login", doc.getElementsByTagName("login").item(0).getTextContent());
                 editor.commit();
 
                 init(settings);
             }
         }
     }
 
     private void initDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("");
         builder.setMessage("  ?");
         builder.setCancelable(false);
         builder.setPositiveButton("", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 dialog.dismiss();
                 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_APK));
                 startActivity(intent);
                 finish();
             }
         });
         builder.setNegativeButton("", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
                 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                 init(settings);
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     private boolean loginWithPozivnoi(String pozivnoi) {
         {
             SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
             // TODO: 
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
             nameValuePairs.add(new BasicNameValuePair("action", "login"));
             nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
             nameValuePairs.add(new BasicNameValuePair("object", "session"));
             nameValuePairs.add(new BasicNameValuePair("login", settings.getString("login", "")));
             nameValuePairs.add(new BasicNameValuePair("password", settings.getString("password", "")));
 
             Document doc = PhpData.postData(PozivnoiActivity.this, nameValuePairs,
                     "https://www.abs-taxi.ru/fcgi-bin/office/cman.fcgi");
             if (doc != null) {
                 Node responseNode = doc.getElementsByTagName("response").item(0);
                 Node errorNode = doc.getElementsByTagName("message").item(0);
 
                 if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                     new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                             .setMessage(errorNode.getTextContent())
                             .setNeutralButton("", new OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface arg0, int arg1) {
                                     initRegistration();
                                 }
                             }).show();
                 else {
                     // save pozivnoi if all ok
 
                     // SharedPreferences.Editor editor = settings.edit();
                     // editor.putString("pozivnoidata", pozivnoi);
                     // editor.commit();
 
                     PhpData.sessionid = doc.getElementsByTagName("sessionid").item(0).getTextContent();
                     Log.d("My_tag", doc.getElementsByTagName("sessionid").item(0).getTextContent());
                     initMessages(doc);
                 }
             }
             // } else {
 
             // EditText pozivnoiEditText = (EditText)
             // findViewById(R.id.pozivnoiEditText);
             // // may cause bugs
             //
             // InputFilter filter = new InputFilter() {
             // public CharSequence filter(CharSequence source, int start, int
             // end, Spanned dest,
             // int dstart, int dend) {
             // for (int i = start; i < end; i++) {
             // if (!Character.isDigit(source.charAt(i))) {
             // return "";
             // }
             // }
             // return null;
             // }
             // };
             // pozivnoiEditText.setFilters(new InputFilter[] { filter });
             //
             // pozivnoiEditText.setOnKeyListener(new EditText.OnKeyListener() {
             //
             // @Override
             // public boolean onKey(View v, int keyCode, KeyEvent event) {
             //
             // EditText pozivnoiEditText = (EditText) v;
             //
             // if (keyCode == EditorInfo.IME_ACTION_SEARCH || keyCode ==
             // EditorInfo.IME_ACTION_DONE
             // || event.getAction() == KeyEvent.ACTION_DOWN
             // && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
             //
             // if (!event.isShiftPressed()
             // && pozivnoiEditText.getText().toString().length() != 0)
             // return loginWithPozivnoi(pozivnoiEditText.getText().toString());
             // else
             // new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
             // .setMessage("    .")
             // .setNeutralButton("", null).show();
             //
             // }
             // return false; // pass on to other listeners.
             // }
             //
             // });
             // }
             return true;
         }
     }
 
     private void initMessages(Document doc) {
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
         nameValuePairs.add(new BasicNameValuePair("action", "list"));
         nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
         nameValuePairs.add(new BasicNameValuePair("object", "message"));
 
         doc = PhpData.postData(PozivnoiActivity.this, nameValuePairs,
                 "https://www.abs-taxi.ru/fcgi-bin/office/cman.fcgi");
         if (doc != null) {
             Node responseNode = doc.getElementsByTagName("response").item(0);
             Node errorNode = doc.getElementsByTagName("message").item(0);
 
             if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                 new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                         .setMessage(errorNode.getTextContent()).setNeutralButton("", null).show();
             else {
                 try {
                     getMessages(doc);
                 } catch (ParseException e) {
                     new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                             .setMessage("    - " + e.toString())
                             .setNeutralButton("", null).show();
                 }
             }
         }
     }
 
     private void getMessages(Document doc) throws ParseException {
         NodeList nodeList = doc.getElementsByTagName("item");
         final ArrayList<Message> unreaded = new ArrayList<Message>();
         for (int i = 0; i < nodeList.getLength(); i++) {
 
             Element item = (Element) nodeList.item(i);
             boolean isRead = true;
             if (item.getElementsByTagName("readdate").item(0) == null)
                 isRead = false;
             int index = Integer.valueOf(item.getElementsByTagName("messageid").item(0).getTextContent());
             SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
             Date date = format.parse(item.getElementsByTagName("postdate").item(0).getTextContent());
             String text = item.getElementsByTagName("message").item(0).getTextContent();
 
             Message message = new Message(text, date, isRead, index);
             if (!isRead) {
                 unreaded.add(message);
             }
         }
         if (unreaded.size() == 0) {
             SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 
             Intent intent;
             boolean isPassword = settings.getBoolean("isPassword", false);
             if (!isPassword)
                 intent = new Intent(PozivnoiActivity.this, MainListActivity.class);
             else
                 intent = new Intent(PozivnoiActivity.this, PasswordActivity.class);
 
             startActivity(intent);
             startService(new Intent(this, PhpService.class));
             finish();
         } else {
             AlertDialog.Builder builder = new AlertDialog.Builder(PozivnoiActivity.this);
             builder.setTitle(unreaded.get(0).getDate().toGMTString());
             builder.setMessage(unreaded.get(0).getText());
             builder.setNeutralButton("", onMessageClick(unreaded));
             AlertDialog alert = builder.create();
             alert.show();
         }
     }
 
     private OnClickListener onMessageClick(final ArrayList<Message> unreaded) {
         return new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 unreaded.remove(0);
                 if (unreaded.size() != 0) {
                     ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                     nameValuePairs.add(new BasicNameValuePair("action", "markread"));
                     nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
                     nameValuePairs.add(new BasicNameValuePair("object", "message"));
                     nameValuePairs.add(new BasicNameValuePair("messageid", String.valueOf(unreaded.get(0)
                             .get_index())));
 
                     Document doc = PhpData.postData(PozivnoiActivity.this, nameValuePairs,
                             "https://www.abs-taxi.ru/fcgi-bin/office/cman.fcgi");
                     if (doc != null) {
                         Node responseNode = doc.getElementsByTagName("response").item(0);
                         Node errorNode = doc.getElementsByTagName("message").item(0);
 
                         if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                             new AlertDialog.Builder(PozivnoiActivity.this).setTitle("")
                                     .setMessage(errorNode.getTextContent()).setNeutralButton("", null)
                                     .show();
                         else {
                             AlertDialog.Builder builder = new AlertDialog.Builder(PozivnoiActivity.this);
                             builder.setTitle(unreaded.get(0).getDate().toGMTString());
                             builder.setMessage(unreaded.get(0).getText());
                             builder.setNeutralButton("", onMessageClick(unreaded));
                             AlertDialog alert = builder.create();
                             alert.show();
                         }
                     }
                 } else {
                     SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                     // if password type password then check password then
                     // loginWithPozivnoi
                     Intent intent;
                     boolean isPassword = settings.getBoolean("isPassword", false);
                     if (!isPassword)
                         intent = new Intent(PozivnoiActivity.this, MainListActivity.class);
                     else
                         intent = new Intent(PozivnoiActivity.this, PasswordActivity.class);
                     Bundle bundle = new Bundle();
                     bundle.putInt("id", index);
                     intent.putExtras(bundle);
                     // TaxiApplication.setDriverId(index);
                     startActivity(intent);
                     startService(new Intent(PozivnoiActivity.this, PhpService.class));
                     finish();
                 }
             }
 
         };
     }
 }
