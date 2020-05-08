 package com.telstra.jitterclient;
 
 import java.net.InetAddress; // API level 1
 import java.net.Socket; // API level 1
 import java.net.DatagramSocket; // API level 1
 import java.net.DatagramPacket; // API level 1
 import java.net.UnknownHostException; // API level 1
 import java.io.OutputStream;
 import java.io.IOException;
 import java.util.Arrays; // API level 1
 import android.app.Activity; // API level 1
 import android.os.Bundle; // API level 1
 import android.util.Log; // API level 1
 import android.widget.Button; // API level 1
 import android.view.View;
 import android.widget.EditText; // API level 1
 import android.widget.RadioButton; // API level 1
 
 public class JitterTestActivity extends Activity
   implements View.OnClickListener
 {
   private static final String TAG = "JitterTestActivity";
 
   //public static int SERVER_ADDR[] = {69, 5, 7, 210}; // www.aes.id.au
   public static int SERVER_ADDR[] = {192, 168, 1, 12}; // the Pi
   public static int SERVER_PORT = 7000;
   public static int PACKET_SIZE = 50;
   public static int MAX_COUNT = 100;
   public static int SLEEP_INTERVAL = 20; // in ms
 
   //public static final int MENU_ITEM_START = Menu.FIRST;
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
       Button b;
       RadioButton rb;
       EditText et;
 
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
       b = (Button) findViewById(R.id.button_start);
       b.setOnClickListener(this);
       et = (EditText) findViewById(R.id.text_ipaddr_1);
       et.setText("" + SERVER_ADDR[0]);
       et = (EditText) findViewById(R.id.text_ipaddr_2);
       et.setText("" + SERVER_ADDR[1]);
       et = (EditText) findViewById(R.id.text_ipaddr_3);
       et.setText("" + SERVER_ADDR[2]);
       et = (EditText) findViewById(R.id.text_ipaddr_4);
       et.setText("" + SERVER_ADDR[3]);
       et = (EditText) findViewById(R.id.text_port);
       et.setText("" + SERVER_PORT);
       et = (EditText) findViewById(R.id.text_size);
       et.setText("" + PACKET_SIZE);
       et = (EditText) findViewById(R.id.text_count);
       et.setText("" + MAX_COUNT);
       rb = (RadioButton) findViewById(R.id.radio_tcp);
       rb.setChecked(true);
      et = (EditText) findViewById(R.id.text_interval);
      et.setText("" + SLEEP_INTERVAL);
   }
 
   public void onClick(View v)
   {
     EditText et;
     RadioButton rb;
     byte ipaddr[];
    int port, packetSize, maxCount, sleepInterval;
     boolean tcp_mode;
 
     try
     {
       ipaddr = new byte[4];
       et = (EditText) findViewById(R.id.text_ipaddr_1);
       ipaddr[0] = (byte) Integer.parseInt(et.getText().toString());
       et = (EditText) findViewById(R.id.text_ipaddr_2);
       ipaddr[1] = (byte) Integer.parseInt(et.getText().toString());
       et = (EditText) findViewById(R.id.text_ipaddr_3);
       ipaddr[2] = (byte) Integer.parseInt(et.getText().toString());
       et = (EditText) findViewById(R.id.text_ipaddr_4);
       ipaddr[3] = (byte) Integer.parseInt(et.getText().toString());
 
       et = (EditText) findViewById(R.id.text_port);
       port = Integer.parseInt(et.getText().toString());
 
       et = (EditText) findViewById(R.id.text_size);
       packetSize = Integer.parseInt(et.getText().toString());
 
       et = (EditText) findViewById(R.id.text_count);
       maxCount = Integer.parseInt(et.getText().toString());
 
       rb = (RadioButton) findViewById(R.id.radio_tcp);
       tcp_mode = rb.isChecked();
 
      et = (EditText) findViewById(R.id.text_interval);
      sleepInterval = Integer.parseInt(et.getText().toString());

      startTest(ipaddr, port, packetSize, maxCount, tcp_mode, sleepInterval);
     } catch (NumberFormatException nfe)
     {
     }
   }
 
   public void startTest(byte[] ipAddr, int port, int packetSize, int maxCount,
                  boolean tcp_mode, int sleepInterval)
   {
     //byte ipAddress[] = {127, 0, 0, 1}; // localhost
     //byte ipAddress[] = {(byte)141, (byte)168, 32, 18}; // neper.cto.in.telstra.com.au
     //byte ipAddress[] = {(byte)10, (byte)0, (byte)0, (byte)88}; 
     //byte ipAddress[] = {(byte)69, (byte)5, (byte)7, (byte)210}; // www.aes.id.au
     byte buffShort[], buffLong[], buffMessage[];
     String message = "Hello:", msg;
     int lenMessage, count;
     long millisBase, millisNow;
     Socket sock = null;
     OutputStream sockOut = null;
     DatagramSocket udp_sock = null;
     DatagramPacket udp_pack = null;
 
     millisBase = System.currentTimeMillis();
     buffShort = new byte[2];
    buffLong = new byte[packetSize];
     Log.v(TAG, "Sending message '" + message + "':");
     try
     {
       InetAddress addrServer = InetAddress.getByAddress(ipAddr);
       Log.v(TAG, "Connecting to: " + addrServer.toString());
       if (tcp_mode)
       {
         sock = new Socket(addrServer, port);
         sock.setTcpNoDelay(true);
         Log.v(TAG, "Buffer size was = " + sock.getSendBufferSize());
         //sock.setSendBufferSize(PACKET_SIZE * 2 - 1);
         sock.setSendBufferSize(8192);
         sockOut = sock.getOutputStream();
       } else
       {
         udp_sock = new DatagramSocket();
         Log.v(TAG, "UDP Buffer size was = " + udp_sock.getSendBufferSize());
       }
       for (count = 0; count < maxCount; count++)
       {
         Log.v(TAG, "Sending message #" + count);
         // Prepare message to send
         millisNow = System.currentTimeMillis();
         msg = message + count + ":" + (millisNow - millisBase);
         buffMessage = msg.getBytes();
         lenMessage = buffMessage.length;
         Arrays.fill(buffLong, (byte)0);
         if (buffLong.length - 2 < lenMessage)
         {
                 lenMessage = buffLong.length - 2;
         }
         System.arraycopy(buffMessage, 0, buffLong, 2, lenMessage);
        buffShort[0] = (byte)((packetSize - 2) % 256);
        buffShort[1] = (byte)((packetSize - 2) / 256);
         System.arraycopy(buffShort, 0, buffLong, 0, 2);
         // Send it!
         if (tcp_mode)
         {
           sockOut.write(buffLong);
           sockOut.flush();
         } else
         {
           udp_pack = new DatagramPacket(buffLong, buffLong.length, addrServer,
             port);
           udp_sock.send(udp_pack);
         }
         try
         {
          Thread.sleep(sleepInterval);
         } catch (InterruptedException ie)
         {
           // just ignore
         }
       }
       if (tcp_mode)
       {
         sockOut.close();
       } else
       {
         udp_sock.close();
       }
     } catch (UnknownHostException uhe)
     {
       uhe.printStackTrace();
     } catch (IOException ioe)
     {
       ioe.printStackTrace();
     }
   }
 }
