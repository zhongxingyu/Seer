 package com.example.android.opengl;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Base64;
 import android.os.AsyncTask;
 import android.util.Log;
 import org.json.JSONTokener;
 
 public class DataFetcher extends AsyncTask<String, Integer, String> {
     private float[] m_vertexData;
 	@Override
 	protected String doInBackground(String... params) {
 
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse response;
 
         Log.d(MyGLRenderer.TAG, "Requesting chunk data");
 
         try {
            response = httpclient.execute(new HttpGet("http://larsendt.com:1234/?x=0&y=0&compression=yes"));
         } catch (IOException e) {
             Log.d(MyGLRenderer.TAG, "HttpResponse broke...");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return "";
         }
 
         StatusLine statusLine = response.getStatusLine();
         if(statusLine.getStatusCode() == HttpStatus.SC_OK) {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             try {
                 response.getEntity().writeTo(out);
             } catch(IOException e) {
                 Log.d(MyGLRenderer.TAG, "ByteArrayOutputStream broke when fetching chunk data");
                 e.printStackTrace();
                 return "";
             }
             String input_json = out.toString();
             //Log.d(MyGLRenderer.TAG, "response is" + input_json);
             Log.d(MyGLRenderer.TAG, "Got chunk data");
             return input_json;
         }
         else {
             try {
                 response.getEntity().getContent().close();
             } catch(IOException e) {
                 Log.d(MyGLRenderer.TAG, "Response broke");
                 e.printStackTrace();
             }
             Log.d(MyGLRenderer.TAG, statusLine.getReasonPhrase());
         }
 		Log.d(MyGLRenderer.TAG, "Broke somewhere.");
 		return null;
 	}
 
     @Override
     protected void onPostExecute(String json_string) {
         JSONObject obj;
         try {
             obj = (JSONObject) new JSONTokener(json_string).nextValue();
         } catch (JSONException e) {
             Log.e(MyGLRenderer.TAG, "Parsing json failed");
             e.printStackTrace();
             return;
         }
 
         Log.d(MyGLRenderer.TAG, "Created JSON object");
 
         String type;
         try {
             type = obj.getString("type");
         } catch(JSONException e) {
             Log.e(MyGLRenderer.TAG, "JSON object didn't have 'type'");
             e.printStackTrace();
             return;
         }
 
         Log.d(MyGLRenderer.TAG, "Type was: " + type);
         if(type.equals("chunk")) {
             String chunk_string;
             try {
                 chunk_string = obj.getString("vertex_data");
             } catch(JSONException e) {
                 Log.e(MyGLRenderer.TAG, "JSON object didn't have vertex data");
                 e.printStackTrace();
                 return;
             }
 
             byte[] decoded_data = Base64.decode(chunk_string, 0);
             Log.d(MyGLRenderer.TAG, "Decoded chunk data from Base64");
            boolean compression;
             try {
                compression = obj.getBoolean("compression");
             } catch(JSONException e) {
                 Log.d(MyGLRenderer.TAG, "JSON object didn't have compression flag, assuming no compression");
                 e.printStackTrace();
                compression = false;
                 Log.d(MyGLRenderer.TAG, "Assuming no compression (server didn't send compression flag");
             }
 
            if(compression) {
                 Log.d(MyGLRenderer.TAG, "Chunk data is compressed");
                 Inflater inf = new Inflater();
                 inf.setInput(decoded_data);
                 int inflated_size;
                 try {
                     inflated_size = obj.getInt("inflated_size");
                 } catch(JSONException e) {
                     Log.e(MyGLRenderer.TAG, "JSON object didn't have inflated size");
                     e.printStackTrace();
                     return;
                 }
 
                 byte[] decompressed_bytes = new byte[inflated_size];
                 int sz;
                 try {
                     sz = inf.inflate(decompressed_bytes);
                 } catch(DataFormatException e) {
                     Log.e(MyGLRenderer.TAG, "Failed to decompress vertex data");
                     e.printStackTrace();
                     return;
                 }
                 Log.d(MyGLRenderer.TAG, "Decompressed chunk data");
                 if(sz != inflated_size) {
                     Log.e(MyGLRenderer.TAG, "Byte array decompressed to different size than expected");
                 }
                 decoded_data = decompressed_bytes;
             }
             else {
                 Log.d(MyGLRenderer.TAG, "Chunk data is not compressed");
             }
 
             Log.d(MyGLRenderer.TAG, "Converting bytes to floats");
             ByteBuffer buf = ByteBuffer.wrap(decoded_data);
             buf.order(ByteOrder.BIG_ENDIAN);
             FloatBuffer vert_data = buf.asFloatBuffer();
             m_vertexData = new float[decoded_data.length/4];
 
             for(int i = 0; i < m_vertexData.length; i++) {
                 m_vertexData[i] = vert_data.get(i);
             }
         }
         else {
             Log.e(MyGLRenderer.TAG, "Got bad json data:" + json_string + "(type == " + type);
         }
 
         Log.d(MyGLRenderer.TAG, "Done");
     }
 
 	public float[] getVertexData() {
         return m_vertexData;
     }
 }
