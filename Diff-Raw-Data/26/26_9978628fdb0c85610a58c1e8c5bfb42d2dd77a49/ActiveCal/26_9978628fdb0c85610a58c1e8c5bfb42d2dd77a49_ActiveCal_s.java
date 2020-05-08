 /**
  * 
  */
 package br.rio.puc.inf.app.activecal;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import br.rio.puc.inf.app.activecal.screen.LoginScreen;
 import br.rio.puc.inf.app.activecal.screen.MenuScreen;
 import br.rio.puc.inf.lac.mobilis.cms.ContextConsumer;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Window;
 import android.widget.Toast;
 
 /**
  * @author victor.pantoja
  *
  */
 public class ActiveCal extends Activity implements Runnable, Observer
 {
 	public static class ActiveCalendarXML {
 		private String xml;
 
 		ActiveCalendarXML(String s) { xml = s; }
 
 		public Map<String,String>[] deXML()
 		{
 			class myMap extends HashMap<String, String> {
 				private static final long serialVersionUID = 8220430134800207887L;
 				myMap (int i){super(i);}
 			};
 			List<myMap> list = new ArrayList<myMap>(0);
 
 			try
 			{
 				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 				DocumentBuilder db = dbf.newDocumentBuilder();
 				InputSource is = new InputSource();
 				StringReader sr = new StringReader(xml);
 				is.setCharacterStream(sr);
 				Document doc = db.parse(is);	    	
 				Node n = doc.getChildNodes().item(0);
 				NodeList nl = n.getChildNodes();
 				for (int i = 0; i < nl.getLength(); i++)
 				{
 					NamedNodeMap map = nl.item(i).getAttributes();
 					myMap row = new myMap(0);
 
 					for (int j = 0; j < map.getLength(); j++)
 						row.put(map.item(j).getNodeName(), map.item(j).getNodeValue());
 
 					list.add(row);
 				}
 			}
 
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				Log.e(TAG, e.getMessage());
 			}
 
 			return list.toArray(new myMap[0]);
 		}
 	}
 
 	class activeHandler extends Handler {
 		private ActiveCal parent;
 
 		activeHandler (ActiveCal obj) {
 			parent = obj;
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 
 			switch (msg.what) {
 			case ASKLOGIN:
 				startActivityForResult(new Intent(getApplicationContext(), LoginScreen.class), 1);
 				break;
 
 			case TRYAUTH:
 				SharedPreferences pref = getSharedPreferences("ACTIVECAL", 0);
 				String login = pref.getString("login", "not_found");
 				String pass = pref.getString("pass", "not_found");
 
 				if (tryAuthenticate(login, pass))
 				{
 					Toast.makeText(getApplicationContext(), "Autenticado", Toast.LENGTH_SHORT).show();
 
 					ContextConsumer.startUp(getApplicationContext(), new int[] { 32123, 32124, 32145 }, "10.22.38.114" );
 					// ContextConsumer.startUp(getApplicationContext());
 					try {
 						consumer = ActiveCalContextConsumer.newConsumer(getApplicationContext(), handler, (ActiveCal)msg.obj);
 						consumer.addObserver(parent);
 						update(null, new Boolean(ContextConsumer.isActive()));
 						handler.sendMessageDelayed(Message.obtain(handler, WAITCONTEXT, msg.obj), DELAY);
 					}
 					catch (Exception e) {
 						Log.e(TAG, "error while trying to create consumer - " + e.getMessage(), e);
 					}
 					break;
 				}
 
 				Toast.makeText(getApplicationContext(), "Falhou!!!", Toast.LENGTH_SHORT).show();
 				handler.sendMessageDelayed(Message.obtain(handler, ASKLOGIN, msg.obj), DELAY);
 				break;
 
 			case RUN:
 				startActivity(new Intent(getApplicationContext(), MenuScreen.class));
 				break;
 
 			case WAITCONTEXT:
 				Toast.makeText(getApplicationContext(), "Aguardando Contexto", Toast.LENGTH_LONG).show();
 				break;
 
 			default:
 				finish();
 				break;
 			}
 		}
 	}
 
 	public static ActiveCalendarXML cal;
 	public String login, pass;
 	public static String authdata;
 	private activeHandler handler;
 	private ContextConsumer consumer;
 
 	public final static int DELAY = 100, ASKLOGIN = 1001, TRYAUTH = 1002, RUN = 1003, WAITCONTEXT = 1004;
 	private final static String TAG = "activecal";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		handler = new activeHandler(this);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.layout_splash);
 
 		Toast.makeText(this, "Carregando...", Toast.LENGTH_SHORT).show();
 
 		handler.postDelayed(this, DELAY);
 	}
 
 	@Override
 	protected void onActivityResult (int requestCode, int resultCode, Intent data) 
 	{
 		handler.sendMessage(Message.obtain(handler, TRYAUTH, this));
 	}
 
 	@Override
 	public void run()
 	{
 		SharedPreferences pref = getSharedPreferences("ACTIVECAL", 0);
 		SharedPreferences.Editor editor = pref.edit();
 
 		editor.putString("pass", "");
 		editor.commit();
 
 		handler.sendMessageDelayed(Message.obtain(handler, ASKLOGIN, this), DELAY);
 	}
 
 	private boolean tryAuthenticate (String login, String pass)
 	{
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost("https://www.google.com/accounts/ClientLogin");
 
 			/* Sets all post parameters */
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("Email", login));
 			nameValuePairs.add(new BasicNameValuePair("Passwd", pass));
 			nameValuePairs.add(new BasicNameValuePair("source", "br.rio.puc.inf.lac.activecal"));
 			nameValuePairs.add(new BasicNameValuePair("service", "cl"));
 
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse response = httpclient.execute(httppost);
 
 			String responseBody = EntityUtils.toString(response.getEntity());
 			Log.d(TAG, "authenticateUser - Status: " + response.getStatusLine().getStatusCode());
 
 			String[] lines = responseBody.split("\n");
 			for (String line : lines) {
 				if (!line.startsWith("Auth="))
 					continue;
 
 				authdata = line.replace("Auth=", "");
 				return true;
 			}
 		}
 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Log.e(TAG, e.getMessage());
 		}
 
 		return false;
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		boolean active = (Boolean) data;
 		if (active) {
 			try {
 				Log.d(TAG, "Active!");
 
 				/*Publicando o path no CMS. Necessario apenas uma vez*/
				String domain = "http://www.luizfelipe.eng.br";
 
 				String apkUrl = domain + "/MeetingContextProvider.apk";
 				String descriptionUrl = apkUrl.concat(".desc");
 				ContextConsumer.getCmsInterface().addContextProviderRepository(apkUrl, descriptionUrl);
 
 				String apkUrl2 = domain + "/DistanceContextProvider.apk";
 				String descriptionUrl2 = apkUrl2.concat(".desc");
 				ContextConsumer.getCmsInterface().addContextProviderRepository(apkUrl2, descriptionUrl2);
 
 
 				ContextConsumer.getCmsInterface().setProviderConfiguration("this.meeting.feed", "setAuth", authdata);				
 				ContextConsumer.getCmsInterface().setProviderConfiguration("this.meeting.feed", "refreshInterval", "30");
 				consumer.addContextInformationInterest("this.meeting.feed");
 
 				//TODO - Usar informacao de bateria para saber o tempo de atualizacao do CP de distancia
 			}
 			catch (Exception e) {
 				Log.e(TAG, "error while trying to add consumer - " + e.getMessage(), e);
 			}
 		}
 		else {
 			Log.e(TAG, "Not active");
 		}
 	}
 }
