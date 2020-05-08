 package com.gf.istock;
 
 import java.io.BufferedReader;  
 import java.io.IOException;  
 import java.io.InputStreamReader;  
 import java.io.PrintStream; 
 import java.io.OutputStream;
 import java.net.ServerSocket;  
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 /** 
  * @project istock 
  * @author yangshengcheng  
  * @verson 0.0.1 
  * @date   20130330 
  * @description  Stock Quotes Simulator 
  */ 
 
 public class issuance implements Runnable
 {
 	public String id;
 	public String hostname;
 	public int port;
 	public String[] content;
 	
 	public issuance(HashMap<String,String> hs,List<stock> updateList)
 	{	this.id = hs.get("id");
 		this.hostname = hs.get("hostname");
 		this.port = Integer.parseInt(hs.get("port"));
 		this.content = this.Hash2Str(updateList);
 		
 	}
 	
 	public String[] Hash2Str(List<stock> updateList)
 	{
 		String[] temp = new String[updateList.size()];
 		int i = 0;
 		for(stock st:updateList )
 		{
			st.updateQt();
 			temp[i] = "{\"id\":\""+st.id+"\","+"\"buy_1\":\"" + st.Qt.buy_1 + "\"," + "\"buy_1_amount\":\"" +st.Qt.buy_1_amount+"\","+ "\"sell_1\": \""+st.Qt.sell_1 + "\"," + "\"sell_1_amount\":\""+ st.Qt.sell_1_amount + "\"}" ;
 			i++;
 		}
 		
 		return temp;		
 	}
 	
 	
     public void run()
     {  
 		Logger logger = logSingleton.get_logger();
 		globalVal global = globalVal.getInstance();
 		logger.info(this.id+":"+this.hostname +":"+ this.port +this.content[0]);
 		
 		Socket sock = null;
 		OutputStream out = null;
 	    //PrintStream out = null;  
 //	    BufferedReader in = null; 
 		
 		try
 		{
 		//publish info
 			sock = new Socket(this.hostname, this.port);
             out = sock.getOutputStream();  
             //in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             String jsonString = "[";
             for(int k=0;k<this.content.length;k++)
             {      
             	if(k < this.content.length - 1)
             	{
             		jsonString = jsonString + this.content[k] + ",";
             	}
             	else
             	{
             		jsonString = jsonString + this.content[k] + "]";
             	}
             }
             out.write(jsonString.getBytes("utf-8"));
             out.flush();
             
 		}
 		catch(IOException e)
 		{
 			//check this client's max retry 
 			String k = this.id+"#"+this.hostname+"#"+this.port;
 			if(global.getUserRetry(k) == 0)
 			{
 				global.userRetryAdd(k);
 				logger.warn("add a fail user "+ k);
 			}
 			else if(global.getUserRetry(k) < 5)
 			{
 				global.userRetryAdd(k);
 				logger.warn(k + "retry add, now "+ global.getUserRetry(k));
 			}
 			else
 			{
 				List<HashMap<String,String>> ls = new ArrayList<HashMap<String,String>>();
 				HashMap<String,String> temp = new HashMap<String,String>();
 				temp.put("id", this.id);
 				temp.put("hostname", this.hostname);
 				temp.put("port", Integer.toString(this.port));
 				ls.add(temp);
 				
 				global.deluser(ls);
 				global.userRetryReset(k);
 				logger.warn(k + " reach max retry ,delete from user list!");
 			}
 			
 			e.printStackTrace();
 		}
 		finally
 		{
             try 
             {   
                 if(!sock.isClosed())
                 {  
                 	sock.close();  
                 }  
             } 
             catch 
             (IOException e1) 
             {  
                 e1.printStackTrace();  
             }  
 		}
 		
 		
 		
     }
 }
