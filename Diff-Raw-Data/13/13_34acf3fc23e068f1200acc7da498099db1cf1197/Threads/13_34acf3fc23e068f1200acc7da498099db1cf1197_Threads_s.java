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
 	ScheduledExecutorService server;
 	ScheduledExecutorService background;
 	ScheduledExecutorService compile;
 	ScheduledExecutorService override;
 	String get;
 	String code;
	Compiler compiler;
 	byte[][] motors = new byte[2][3];
 	byte[] received = new byte[3];
 	boolean sending = false;
 
 	Threads(MainActivity act)
 	{
 		this.act = act;
 		sendNXT = Executors.newScheduledThreadPool(1);
 		readNXT = Executors.newScheduledThreadPool(1);
 		server = Executors.newScheduledThreadPool(1);
 		background = Executors.newScheduledThreadPool(1);
 		compile = Executors.newScheduledThreadPool(1);
 		override = Executors.newScheduledThreadPool(1);
 	}
 
 	public class dotbot implements Runnable{
 		public void run(){
 			getHTML htmlclass = new getHTML();
 			read read = new read();
 			postHTML post = new postHTML();			
 			readNXT.scheduleAtFixedRate(read, 0, 10, TimeUnit.MILLISECONDS);
 			server.scheduleAtFixedRate(htmlclass, 0, 1, TimeUnit.SECONDS);
 			server.scheduleAtFixedRate(post, 0, 1, TimeUnit.SECONDS);
 			change_motor(new boolean[] {true, true, true}, new byte[] {0,0,0}, 1);
 		}
 	}
 
 	public class postHTML implements Runnable
 	{
 		URL url;
 		HttpURLConnection connection = null;
 		String urlParameters = null;
 		String line;
 		StringBuffer response = new StringBuffer(); 
 		
 		postHTML()
 		{
 
 		}
 		public void run()
 		{
 			try {
 				urlParameters = "from=" + URLEncoder.encode("slave", "UTF-8") +
 						"&id=" + URLEncoder.encode(String.valueOf(act.id), "UTF-8") +
 						"&status=" + URLEncoder.encode("ok", "UTF-8") +
 						"&sensors=" + URLEncoder.encode(String.valueOf(received[2]), "UTF-8") +
 						"&msg=" + URLEncoder.encode("ok", "UTF-8");
 			
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
 				Log.v("nxtdriver", "error in postHTML run: "+e.getMessage());
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
 					Log.v("nxtdriverread", "distance: "+String.valueOf(received[2]));
 				}
 			} catch (Exception e) {
 				Log.v("nxtdriver", "error in read run: "+e.getMessage());
 			}
 		}
 	}
 
 	public class send implements Runnable{
 		public void run()
 		{
 			try 
 			{
 				act.mmOutputStream.write(motors[0]);
 			} catch (Exception e) {
 				Log.v("nxtdriver","error in send run: "+e.getMessage());
 			}
 		}
 	}
 
 	public class getHTML implements Runnable{
 		URL url ;
 		HttpURLConnection conn;
 		BufferedReader rd;
 		String line;
 
 		public getHTML(){
 			try {
 				url = new URL("http://discos.herokuapp.com/io?from=slave&id="+String.valueOf(act.id));
 				conn = (HttpURLConnection) url.openConnection();
 				conn.setRequestMethod("GET");
 				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			} catch (Exception e) {
 				Log.v("nxtdriver", "error in getHTML init: "+ e.getMessage());
 			}
 		}
 
 		public void run() {
 			get = "";
 			try {
 				while ((line = rd.readLine()) != null) {
 					get += line;
 				}
				if(get.startsWith("active", 10)) compile.submit(new compileThread(code));
				else if(get.startsWith("inactive", 10)) code = get;
 				else if(get.startsWith("paused", 10)) compile.wait();
 				else if(get.startsWith("override", 10))
 				{
 					compile.wait();
 					override.submit(new compileThread(get));
 				}
 			} catch (Exception e) {
 				Log.v("nxtdriver", "error in getHTML run: " + e.getMessage());
 			}
 		}
 	}
 
 	public class compileThread implements Runnable{
 		String code;
 		Compiler compiler;
 		compileThread(String code)
 		{
 			this.code = code;
 		}
 		
 		@Override
 		public void run() {
			compiler = new Compiler(code, Threads.this);
 			compiler.compile();
 		}
 
 	}
 
 	public void change_motor(boolean[] m, byte[] s, int d)
 	{
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
