 package com.example.nxtdriver;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import android.util.Log;
 
 public class Threads {
 
 	MainActivity act;
 	ScheduledExecutorService sendNXT;
 	ScheduledExecutorService readNXT;
 	ScheduledExecutorService sendserver;
 	ScheduledExecutorService getserver;
 	ScheduledExecutorService background;
 	ScheduledExecutorService compile;
 	ScheduledExecutorService override;
 	String get;
 	String code;
 	compileThread cpt;
 	byte[][] motors = new byte[2][3];
	byte[] received = new byte[3];
 	boolean sending = false;
 
 	Threads(MainActivity act)
 	{
 		this.act = act;
 		sendNXT = Executors.newScheduledThreadPool(1);
 		readNXT = Executors.newScheduledThreadPool(1);
 		sendserver = Executors.newScheduledThreadPool(1);
 		getserver = Executors.newScheduledThreadPool(1);
 		background = Executors.newScheduledThreadPool(1);
 		compile = Executors.newScheduledThreadPool(1);
 		override = Executors.newScheduledThreadPool(1);
 	}
 
 	public class dotbot implements Runnable{
 		public void run(){
 			//getHTML htmlclass = new getHTML();
 			read read = new read();
 			postHTML post = new postHTML();			
 			readNXT.scheduleAtFixedRate(read, 0, 10, TimeUnit.MILLISECONDS);
 			sendserver.scheduleAtFixedRate(new getHTML(), 0, 1, TimeUnit.SECONDS);
 			getserver.scheduleAtFixedRate(post, 0, 1, TimeUnit.SECONDS);
 			//change_motor(new boolean[] {true, true, true}, new byte[] {0,0,0}, 1);
 		}
 	}
 
 	public class postHTML implements Runnable
 	{
 		String customMsg = "";
 		URL url;
 		HttpURLConnection connection = null;
 		String urlParameters = null;
 		String line;
 		StringBuffer response = new StringBuffer(); 
 
 		public postHTML() {
 			this("");
 		}
 
 		public postHTML(String msg) {  
 			try {
 				customMsg = "&msg="+ URLEncoder.encode(msg, "UTF-8");
 			}
 			catch (Exception e) {
 				Log.v("nxtdriverposthtmlerror", e.getMessage());
 				if(!msg.equals("")) customMsg = "&msg="+ msg;
 			}
 
 		}
 
 		public void run()
 		{
 			try {
 				urlParameters = "from=" + URLEncoder.encode("slave", "UTF-8") +
 						"&id=" + URLEncoder.encode(String.valueOf(act.id), "UTF-8") +
						"&status=" + URLEncoder.encode("ok", "UTF-8") +
						"&sensors=" + URLEncoder.encode(String.valueOf(received[2]), "UTF-8") +

 						customMsg;
 				//Create connection
 				connection = (HttpURLConnection)url.openConnection();
 				connection.setRequestMethod("POST");
 				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 				connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
 				connection.setRequestProperty("Content-Language", "en-US");  
 				connection.setUseCaches (false);
 				connection.setDoInput(true);
 				connection.setDoOutput(true);
 
 				//Send request
 				DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
 				wr.writeBytes (urlParameters);
 				wr.flush ();
 				wr.close ();
 
 				//Get Response
 				BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 				while((line = rd.readLine()) != null)
 				{
 					response.append(line);
 					response.append('\r');
 				}
 				rd.close();
 			} catch (Exception e) {
 				Log.v("nxtdriverposthtmlerror", e.getMessage());
 			} finally {
 				if(connection != null) {
 					connection.disconnect(); 
 				}
 			}
 		}	
 	}
 
 	public class read implements Runnable{
 		public void run(){
 			try {
 				if (act.mmInputStream.available()>0)
 				{
 					act.mmInputStream.read(received);
 					//Log.v("nxtdriverread", "distance: "+String.valueOf(received[2]));
 				}
 			} catch (Exception e) {
 				Log.v("nxtdriverreaderror", e.getMessage());
 			}
 		}
 	}
 
 	public class send implements Runnable{
 		public void run()
 		{
 			try 
 			{
 				Log.v("nxtdriversend", String.valueOf(motors[0][0])+ " "+String.valueOf(motors[0][1])+" "+String.valueOf(motors[0][2]));
 				act.mmOutputStream.write(motors[0]);
 			} catch (Exception e) {
 				Log.v("nxtdriversenderror",e.getMessage());
 			}
 		}
 	}
 
 	public class getHTML implements Runnable{
 
 		public void run() {
 			try{
 				URL url = new URL("http://discos.herokuapp.com/io?from=slave&id="+String.valueOf(act.id));
 				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 				conn.setRequestMethod("GET");
 				String line;
 				//Log.v("nxtdrivergethtml", "starting get");
 				get = "";
 				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 
 				while ((line = rd.readLine()) != null) {
 					get += line;
 				}
 				Log.v("nxtdrivergetmsg", get);
 				if(get.startsWith("active", 10) && cpt != null) compile.submit(cpt);
 				else if(get.startsWith("active", 10) && cpt == null); //some sort of error?
 				else if(get.startsWith("inactive", 10) && get.contains("main")) 
 				{
 					change_motor(new boolean[] {true, true, true}, new byte[] {0,0,0}, 1);
 					cpt = new compileThread(get);
 				}
 				else if(get.startsWith("inactive", 10) && !get.contains("main")) 
 				{
 					compile.shutdownNow(); 
 					change_motor(new boolean[] {true, true, true}, new byte[] {0,0,0}, 1);
 					cpt = null;
 				}
 				else if(get.startsWith("paused", 10)) 
 				{
 					change_motor(new boolean[] {true, true, true}, new byte[] {0,0,0}, 1);
 					compile.wait();
 				}
 				else if(get.startsWith("override", 10))
 				{
 					compile.wait();
 					override.submit(new compileThread(get));
 				}
 				else ;
 				rd.close();
 			} catch (Exception e) {
 				Log.v("nxtdrivergethtmlerror", e.getMessage());
 			}
 		}
 	}
 
 	public class compileThread implements Runnable{
 		String code;
 		Compiler compiler;
 		compileThread(String code)
 		{
 			Log.v("nxtdrivercompiler", "initiated");
 			Log.v("nxtdrivercompilercode", code);
 			this.code = code;
 			compiler = new Compiler(code, Threads.this);
 		}
 
 		@Override
 		public void run() {
 			compiler.compile();
 		}
 
 	}
 
 	public void change_motor(boolean[] m, byte[] s, int d)
 	{
 		Log.v("nxtdrivercompiler", "starting change motor");
 		Log.v("nxtdrivercompiler", String.valueOf(d));
 		if(!sending)
 		{
 			sendNXT.scheduleAtFixedRate(new send(), 0, 500, TimeUnit.MILLISECONDS);
 			sending = true;
 		}
 
 		for (int i = 0; i<m.length; i++)
 		{
 			if(m[i]) motors[0][i] = s[i];
 		}
 		try {
 			Thread.sleep(d);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
