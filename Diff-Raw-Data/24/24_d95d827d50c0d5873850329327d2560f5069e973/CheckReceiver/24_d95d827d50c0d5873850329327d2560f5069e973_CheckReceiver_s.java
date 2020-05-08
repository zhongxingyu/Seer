 package com.villainrom.otaupdater.receiver;
 
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.villainrom.otaupdater.R;
 import com.villainrom.otaupdater.activity.TitleActivity;
 import com.villainrom.otaupdater.utility.Update;
 
 /**
  * Check for updates
  * 
  * @author alankila
  */
 public class CheckReceiver extends BroadcastReceiver {	
 	private static final String TAG = CheckReceiver.class.getSimpleName();
 
 	/* so they {@hide} android.os.SystemProperties. Fuck them. */
 	private String getSystemProperty(String name) {
 		try {
 			Class<?> c = Class.forName("android.os.SystemProperties");
 			Method m = c.getMethod("get", String.class);
 			return (String) m.invoke(null, name);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.i(TAG, "Beginning check.");
 		
 		String urlPattern = context.getString(R.string.manifest_url);
 		String urlString = String.format(urlPattern, getSystemProperty("ro.build.version.release"));
 
 		List<Update> updates = new ArrayList<Update>();
 		
 		try {
 			URL url = new URL(urlString);
 			URLConnection c = url.openConnection();
 			c.setDoOutput(false);
 			InputStream is = c.getInputStream();
 			
 			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
 			Element docElement = doc.getDocumentElement();
 			if (docElement.getNodeName().equals("updates")) {
 				handleUpdates(updates, docElement);
 			} else {
 				throw new Exception("Unknown top-level element: " + docElement.getNodeName());
 			}
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Error fetching update", e);
 			
 			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 			Notification notification = new Notification(
 					R.drawable.icon,
 					"Update check failed",
 					System.currentTimeMillis()
 			);
 			notification.flags |= Notification.FLAG_AUTO_CANCEL;
 			notification.setLatestEventInfo(context, "Exception", e.getMessage(), PendingIntent.getActivity(context, 0, new Intent("com.villainrom.otaupdater.MAIN"), 0));
 			nm.notify(TitleActivity.NOTIFY_CHECK_FAILED_ID, notification);
 			return;
 		}
 
 		Log.i(TAG, "Found updates: " + updates.size());
 		
 		Intent foundUpdates = new Intent("com.villainrom.otaupdater.SELECT_UPDATE");
 		foundUpdates.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		foundUpdates.putExtra("update", updates.toArray(new Update[updates.size()]));
 		context.startActivity(foundUpdates);
 	}
 
 
 	private static void handleUpdates(List<Update> updates, Element rootElement) throws Exception {
 		NodeList nl = rootElement.getChildNodes();
 		for (int i = 0; i < nl.getLength(); i ++) {				
 			Node node = nl.item(i);
 			if (! (node instanceof Element)) {
 				continue;
 			}
 		
 			Element element = (Element) node;
 			if (element.getNodeName().equals("update")) {
 				Update update = new Update();
 				handleUpdate(update, element);
 				updates.add(update);
 			} else {
 				throw new Exception("Unknown child of updates: " + element.getNodeName());
 			}
 		}
 	}
 
 	private static void handleUpdate(Update update, Element updateElement) throws Exception {
 		NodeList nl = updateElement.getChildNodes();
 		for (int i = 0; i < nl.getLength(); i ++) {
 			Node node = nl.item(i);
 			if (! (node instanceof Element)) {
 				continue;
 			}
 
 			String attribute = node.getNodeName();
 			if ("name".equals(attribute)
 					|| "description".equals(attribute)
 					|| "url".equals(attribute)) {
				Update.class.getField(attribute).set(update, node.getNodeValue());
 				continue;
 			} else if ("dependencies".equals(attribute)) {
 				handleUpdateDependencies(update, (Element) node);
 			} else {
 				throw new Exception("Update file contains unhandled element: " + node.getNodeName());
 			}
 		}
 	}
 		
 	private static void handleUpdateDependencies(Update u, Element element) throws Exception {
 		NodeList nl = element.getElementsByTagName("update");
 		for (int i = 0; i < nl.getLength(); i ++) {
 			Node node = nl.item(i);
 			if (! (node instanceof Element)) {
 				continue;
 			}
 			
 			if ("update".equals(node.getNodeName())) {
				u.dependencyUpdateNames.add(node.getNodeValue());
 			} else {
 				throw new Exception("Dependencies contains unhandled element: " + node.getNodeName());
 			}
 		}
 	}
 }
