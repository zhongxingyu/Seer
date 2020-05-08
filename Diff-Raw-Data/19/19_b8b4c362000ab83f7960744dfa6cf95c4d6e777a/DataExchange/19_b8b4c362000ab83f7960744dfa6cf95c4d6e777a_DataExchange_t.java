 package com.RoboMobo;
 
 import android.os.Handler;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Zubrolet
  * Date: 06.08.13
  * Time: 15:58
  * To change this template use File | Settings | File Templates.
  */
 public class DataExchange extends Thread
 {
     public Handler handler;
     public InputStream in;
     public OutputStream out;
     JSONObject receive;
 
     public DataExchange(Handler _handler)
     {
         handler = _handler;
         try
         {
             in = RMR.btSocket.getInputStream();
             out = RMR.btSocket.getOutputStream();
         } catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     @Override
     public void run()
     {
         byte[] buffer = new byte[1024];
         int bytes;
        if(RMR.state == RMR.GameState.Client)
        {
            try
            {
                out.write(new byte[] {0});
            }
            catch (IOException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
         while (true)
         {
             try
             {
                 bytes = in.read(buffer);
                 receive = new JSONObject(new String(buffer, 0, bytes));
                 handler.obtainMessage(RMR.INCOMING_MESSAGE, receive).sendToTarget();
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
             catch (JSONException e)
             {
                 e.printStackTrace();
             }
         }
     }
 
     public void write(JSONObject msg)
     {
         try
         {
             out.write(msg.toString().getBytes());
         } catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 }
