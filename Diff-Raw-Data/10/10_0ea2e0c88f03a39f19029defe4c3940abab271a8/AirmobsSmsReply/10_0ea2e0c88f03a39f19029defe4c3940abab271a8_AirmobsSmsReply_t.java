 package edu.mit.media.airmobs;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 
import android.content.Context;
import android.net.Uri;
 import android.util.Log;
 
 /**
  * create a sms reply worker per customer connecting usually this will be 1:1 but
  * some cases one provider may relay messages to multiple customers.
  * so each customer will get its own SMS relay
  * @author eyalto
  *
  */
 public class AirmobsSmsReply extends Worker {
 	

 	public AirmobsSmsReply(Socket provider) {
 		s = provider;
 		try {
 			is = new DataInputStream(s.getInputStream());
 			mState = Worker.WORKER_STATE.IDLE;
 		} catch (IOException e) {
 			Log.e(LOG_TAG,"error constucting sms reply worker unable to get input stream");
 			mState = Worker.WORKER_STATE.FINISHED_ERROR;
 			mBoss.workerFinished(this, mState);
 		}
 	}
 	
 	public void setContext(Context ctx) { mContext = ctx; }
 	// worker thread loop method
 	@Override
 	void work() {
 		// worker thread loop 
 		while(is_working){
 			try {
 				String message = is.readUTF();
 				postReplyMessage(message);
 			} catch (IOException e) {
 				Log.e(LOG_TAG,"error reading UTF string message from input stream");
 				mState = Worker.WORKER_STATE.FINISHED_ERROR;	
 				this.stopWorking();
 			}
 		}
 	}
 
 	private void postReplyMessage(String message){
 		Log.i(LOG_TAG,"got reply messge ==> "+message);
 		Document msg;
 		try {
 			msg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(message.getBytes()));
 		} catch (Exception e) {
 			Log.e(LOG_TAG,"exception while parsing relay message - message = "+message+" ==> not sent");
 			return;
 		}
 		msg.getDocumentElement().normalize();
 		String msgto = msg.getElementsByTagName("msgto").item(0).getTextContent();
 		String msgfrom = msg.getElementsByTagName("msgfrom").item(0).getTextContent();
 		String text = msg.getElementsByTagName("msgtext").item(0).getTextContent();
 		//TODO: content provider insert - or - send to myself  
		//mContext.getContentResolver().insert(Uri.parse("content://sms/inbox));
 		
 	}
 	private Socket s;
 	private DataInputStream is;
 	private static final String LOG_TAG = "Airmobs::SmsRelayWorker"; 
	private Context mContext;
 }
