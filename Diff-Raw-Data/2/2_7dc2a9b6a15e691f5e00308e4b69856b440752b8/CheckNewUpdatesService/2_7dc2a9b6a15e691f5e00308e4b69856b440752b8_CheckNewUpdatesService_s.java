 package com.focusings.focusingsworld5.notificationManagement;
 
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import com.focusings.focusingsworld5.MainActivity;
 import com.focusings.focusingsworld5.ImageAndTextList.ImageAndText;
 import com.focusings.focusingsworld5.YoutubeParser.AsyncResponse;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.TaskStackBuilder;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.support.v4.app.NotificationCompat;
 
 public class CheckNewUpdatesService extends IntentService {
 
 	public static AsyncNotificationResponse delegate=null;
 	
 	public CheckNewUpdatesService() {
 		super("CheckNewUpdatesService");
 	}
 	
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		new CheckNewUpdatesTask().execute();
 	}
 	
 	private class CheckNewUpdatesTask extends AsyncTask<String, Void, List<Update>> {
 		@Override
 		protected List<Update> doInBackground(String... strings) {
 			
 			List<Update> lu= new LinkedList<Update>();
 			try{
 			
 				//Check if there is any update in any of the channels
 				int numberOfTabs=Integer.parseInt(MainActivity.properties.getProperty("number_of_tabs"));
 				for (int i=0;i<numberOfTabs;i++){				
 					URL url = new URL(MainActivity.properties.getProperty("Youtube_URL_part_1")+MainActivity.properties.getProperty("tab_"+(i+1)+"_channel_name")+MainActivity.properties.getProperty("Youtube_URL_part_2"));
 					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 					DocumentBuilder db = dbf.newDocumentBuilder();
 					Document doc = db.parse(new InputSource(url.openStream()));
 					doc.getDocumentElement().normalize();
 			
 			        NodeList nodeList = doc.getElementsByTagName("entry");
 			
 			        //Go to the first item
 			        Node node = nodeList.item(0);
 			            
 		            NodeList nodeListEntry=node.getChildNodes();
 		            
 		            //I get the node that has the id
 		            Node firstNode=nodeListEntry.item(0);
 		            String lastId=firstNode.getTextContent();
 		            
 		            boolean foundTitle=false;
 		            //If lastId is not the last one, then I look for the title of the video and send a notification
		            if (lastId!=null && MainActivity.lastUpdatePerChannel[i]!=null && lastId.equals(MainActivity.lastUpdatePerChannel[i])){
 		            	//Send notification
 		            	
 		            	//First, I look for the new title
 		            	for (int j=0;j<nodeListEntry.getLength() && !foundTitle;j++){
 			            	Node currentNode=nodeListEntry.item(j);
 			            	if (currentNode.getNodeName().equals("title")){
 			            		foundTitle=true;
 			            		Update u=new Update(currentNode.getTextContent(),
 			            				MainActivity.properties.getProperty("tab_"+(i+1)+"_channel_name")
 			            			);
 			            		lu.add(u);
 			            	}
 			            }
 		            }
 				}
 			}catch (Exception e) {
 		        System.out.println("XML Pasing Exception = " + e);
 		    }
 			//If there is any update, then call sendNotification
 			return lu;
 		}
 		
 		@Override
 		protected void onPostExecute(List<Update> updates) {
 			if (updates!=null && updates.size()>0){
 				//Send notification
 				delegate.sendNotification(updates);
 			}
 		}
 	}
 }
