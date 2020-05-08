 package Hanes.testProj;
 /*
  * CSH Drink App by Andrew Hanes
  */
 import java.io.*;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import Hanes.testProj.R;
 import android.util.Log;
 
 public class Connector {
 	/*
 	 * Class for connecting to the drink server
 	 */
 	BufferedReader br = null;
 	BufferedWriter bw = null;
 	Socket skt = null;
 	public Connector(String host, int port)
 	/*
 	 * Connects to a drink server
 	 */
 	{
 		try {
 			skt = new Socket(host,port);
 			this.br = new BufferedReader(new InputStreamReader(skt.getInputStream()));
 			this.bw = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
 			Log.d("Connected",this.recieve().toString());
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			Log.d("UnknownHost","Cant Connect to "+host);
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.d("IOException","IOException in Constructor");
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void close()
 	{
 		try {
 			br.close();
 			bw.close();
 			skt.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	public void send(String command)
 	/*
 	 * Sends a string to the drink server
 	 */
 	{
 		try {
 			bw.flush();
 			bw.write(command);
 			bw.flush();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch(NullPointerException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public ArrayList<String> recieve()
 	/*
 	 * Returns a string sent from the drink server
 	 */
 	{
 		ArrayList <String> temp = new ArrayList<String>();
 		try{
 			temp.add(br.readLine());
 			while(br.ready())
 			{
 				temp.add(br.readLine());
 			}
 		}catch(IOException e)
 		{
 			Log.d("IOException",e.toString());
 			e.printStackTrace();
 			return null;
 		}
 		catch(NullPointerException e)
 		{
 			Log.d("NullPointerException",e.toString());
 			e.printStackTrace();
 			if(br == null)
 				Log.d("NULL","br "+br);
 			if(bw == null)
 				Log.d("NULL","bw "+bw);
 			if(skt == null)
 				Log.d("NULL","skt "+skt);
 			//return new ArrayList<String>();
 		}
 		return temp;
 	}
 	public ArrayList<String> command(String s)
 	/*
 	 * Combines the recieve and send functions
 	 * Takes a string and returns the output from Drink
 	 */
 	{
 		this.send(s+'\n');
 		return this.recieve();
 	}
 }
