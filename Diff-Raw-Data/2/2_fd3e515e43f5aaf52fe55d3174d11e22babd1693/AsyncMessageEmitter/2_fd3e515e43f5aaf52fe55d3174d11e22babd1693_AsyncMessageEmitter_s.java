 /*
  * Copyright (C) 2011, Valentin Lorentz
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.openihs.seendroid;
 
 import java.net.UnknownHostException;
 
 import org.openihs.seendroid.lib.Connection;
 import org.openihs.seendroid.lib.MessageEmitter;
 import org.openihs.seendroid.lib.Query.ParserException;
 import org.w3c.dom.Document;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 public class AsyncMessageEmitter extends AsyncTask<Void, Integer, Document> {
 	private Activity activity;
 	private Connection connection;
 	private String message;
 	private ProgressDialog dialog;
 	private int replyTo;
 	private OnMessageSentListener onMessageSentListener;
 	
 	public AsyncMessageEmitter(Activity activity, Connection connection, String message) {
 		this(activity, connection, message, -1);
 	}
 	
 
 	public AsyncMessageEmitter(Activity activity, Connection connection, String message, int replyTo) {
 		super();
 		this.connection = connection;
 		this.message = message;
 		this.activity = activity;
 		this.replyTo = replyTo;
 		this.dialog = ProgressDialog.show(this.activity, "", this.activity.getString(R.string.postmessage_sending), true);
 	}
 
 	protected Document doInBackground(Void... arg0) {
     	try {
     		MessageEmitter emitter = new MessageEmitter(this.connection);
     		try {
 	    		if (this.replyTo == -1) {
 	    			Log.d("SeenDroid", "reply");
 	    			return emitter.publish(this.message);
 	    		}
 	    		else {
 	    			Log.d("SeenDroid", "post");
 	    			return emitter.publish(this.message, this.replyTo);
 	    		}
     		} catch (UnknownHostException e) {
 				Toast.makeText(this.activity, R.string.error_unknownhost, Toast.LENGTH_LONG).show();
 				return null;
     		}
     		
 		} catch (ParserException e) {
 			Utils.errorLog(this.activity, e, String.format("Writing message in reply to %d", this.replyTo));
 			e.printStackTrace();
 			return null;
 		}
     }
 
     protected void onProgressUpdate(Integer... progress) {
     }
     protected void onPostExecute(Document document) {
         this.dialog.dismiss();
        if (document.getDocumentElement() == null) {
             Toast.makeText(this.activity, R.string.postmessage_failed, Toast.LENGTH_LONG).show();
         }
         else {
 	        Toast.makeText(this.activity, R.string.postmessage_success, Toast.LENGTH_LONG).show();
 	        if (this.onMessageSentListener != null) {
 	        	this.onMessageSentListener.onMessageSent();
 	        }
         }
     }
     public void setOnMessageSentListener(OnMessageSentListener listener) {
     	this.onMessageSentListener = listener;
     }
 }
