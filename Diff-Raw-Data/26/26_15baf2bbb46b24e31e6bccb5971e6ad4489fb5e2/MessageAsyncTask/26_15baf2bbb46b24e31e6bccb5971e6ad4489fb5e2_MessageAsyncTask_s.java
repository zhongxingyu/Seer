 package services;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.widget.EditText;
 
 import com.trueffelscout.trueffelscoutapp.R;
 
 public class MessageAsyncTask extends AsyncTask<String, Void, String> {
 	
 	private Activity activity;
 	private ProgressDialog dialog;
 	private String server_msg="";
 	private int field;
 	
 	public MessageAsyncTask(Activity activity){
 		this.activity=activity;		
 	}
 	
 	@Override
 	protected void onPreExecute(){
 		this.dialog = new ProgressDialog(this.activity);
 		dialog.show();
 		dialog.setMessage(activity.getResources().getString(R.string.send_message));
 		
 	}
 	@Override
 	protected void onProgressUpdate(Void... v){
 		super.onProgressUpdate(v);
 		dialog.dismiss();
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.activity);
 			alertDialogBuilder
 				.setMessage(this.server_msg)
 				.setCancelable(false)
 				.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,int id) {
 						dialog.dismiss();
 						switch(field){
 						case 1: 
 							EditText name = (EditText)activity.findViewById(R.id.msg_name);
 							name.requestFocus();
 							break;
 						case 2: 
 							EditText phone = (EditText)activity.findViewById(R.id.msg_phone);
 							phone.requestFocus();
 							break;
 						case 10:
 							activity.finish();
 						}
 					}
 			});
 				// create alert dialog
 			AlertDialog alertDialog = alertDialogBuilder.create();
 				// show it
 			alertDialog.show();
 	}
 	
 	@Override
 	protected String doInBackground(String... params) {
 		URL url;
 		
 		if(!hasInternetConnection()){
 			this.server_msg="Keine Netzwerkverbindung!";
 			publishProgress();
 		}else{
 			try{
 				params[1].replace(System.getProperty("line.separator"), "");
 		    	String feed = "http://www.trueffelscout.de/mobile/save_mobile_message.php?key=biri&name="+params[1]+"&phone="+params[2]+"&mail="+params[3]+"&message="+params[4]+"&language="+params[5];
 		    	url = new URL(feed);
 		    	
 		    	URLConnection connection = url.openConnection();
 		    	HttpURLConnection httpconn = (HttpURLConnection) connection;
 		    	
 		    	int responseCode = httpconn.getResponseCode();
 		    	
 		    	if(responseCode==HttpURLConnection.HTTP_OK){
 		    		InputStream is_xml = httpconn.getInputStream();
 		    		
 		    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		    		DocumentBuilder db = dbf.newDocumentBuilder();
 		    		
 		    		Document dom = db.parse(is_xml);
 		    		Element doc_elem = dom.getDocumentElement();
 		    		Element msg_node = (Element)doc_elem.getElementsByTagName("message").item(0);
 		    		this.server_msg=msg_node.getTextContent();
 		    		Element field_node = (Element)doc_elem.getElementsByTagName("field").item(0);
 		    		this.field=Integer.parseInt(field_node.getTextContent());
 		    		publishProgress();
 		    	}
 			}catch (MalformedURLException e) {
 				e.printStackTrace();
 				return null;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			} catch (ParserConfigurationException e) {
 					e.printStackTrace();
 					return null;
 			} catch (SAXException e) {
 					e.printStackTrace();
 					return null;
 			}
 		}
 		return server_msg;
 	}
 	
 	@Override
 	public void onPostExecute(String msg){
 		if(msg!=null&&hasInternetConnection()){
 			dialog.dismiss();
 		}else{
 			dialog.setMessage("Verbindungsfehler!");
 			dialog.dismiss();
 		}
 	}
 	
 	public boolean hasInternetConnection(){
 		ConnectivityManager conMgr =  (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo i = conMgr.getActiveNetworkInfo();
 	    if (i == null)
 		    return false;
 		if (!i.isConnected())
 		    return false;
 		if (!i.isAvailable())
 		    return false;
 	    return true;
 	}
 	
 
 }
