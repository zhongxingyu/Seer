 package com.fteams.pinger.base;
 
 import com.fteams.pinger.PropertyLoader;
 import com.fteams.pinger.ui.MainFrame;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.*;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Francis
  * Date: 13/02/06
  * Time: 2:00
  * To change this template use File | Settings | File Templates.
  */
 public abstract class Pinger extends Thread{
 
     protected MainFrame userInterface;
     protected PropertyLoader loader;
     protected boolean refreshing = false;
     protected List<String> availableItems = new LinkedList<String>();
     
     public abstract String getBaseURL();
     public abstract String getSearchString();
     public abstract void loadProperties(PropertyLoader loader);
     public abstract boolean refreshConfig(PropertyLoader loader);
     public List<String> getAvailableItems()
     {
         return availableItems;
     }
     public List<String> getUnavailableItems()
     {
         List<String> unavailableItems = loader.getFigureCodeList();
         unavailableItems.removeAll(getAvailableItems());
         return unavailableItems;
     }
     public void setUserInterface(MainFrame mainFrame)
     {
         this.userInterface = mainFrame;
     }
 
     public void run()
     {
         if (userInterface != null )
         {
             userInterface.updateConsole("Initializing the data:");
             userInterface.updateConsole(loader.toString());
         }
         while (true)
         {
             try {
                 // ping and parse
                 int size_before = loader.getFigureCodeList().size();
                 pingAndParse();
                 if (size_before != loader.getFigureCodeList().size())
                 {
                     announceStatus();
                 }
                 Thread.sleep(loader.getPingTimer());
 
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 
     private void announceStatus() {
         if (availableItems.size() != 0)
         {
             if (userInterface != null )
             {
                 userInterface.updateConsole("INFO: Updates Available for " + availableItems.size() + " elements.");
             }
             for (String code: availableItems)
             {
                 if (userInterface != null )
                 {
                     userInterface.updateConsole("> "+getBaseURL()+code);
                 }
             }
 
             if (userInterface != null )
             {
                 userInterface.updateConsole("INFO: Unchanged Items: " + loader.getFigureCodeList().size() + " elements.");
             }
             for (String code: loader.getFigureCodeList())
             {
                 if (userInterface != null )
                 {
                     userInterface.updateConsole("> "+getBaseURL()+code);
                 }
             }
         }
     }
 
     protected void pingAndParse() throws IOException {
         for (String code : loader.getFigureCodeList())
         {
             URL url = new URL(getBaseURL() + code);
             HttpURLConnection urlconn = null;
 
             // load user authentication information > if specified in the config
             if (loader.getProxyInformation().user_auth)
             {
                 Authenticator authenticator = new Authenticator(){
                     public PasswordAuthentication getPasswordAuthentication()
                     {
                         return new PasswordAuthentication(loader.getProxyInformation().p_username, loader.getProxyInformation().p_password.toCharArray());
                     }
 
                 };
                 Authenticator.setDefault(authenticator);
             }
             // load proxy config if specified
             if (loader.getProxyInformation().proxy_enabled)
             {
                 Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(loader.getProxyInformation().host, Integer.parseInt(loader.getProxyInformation().port)));
                 try {
                     urlconn = (HttpURLConnection) url.openConnection(proxy);
                 }catch (UnknownHostException e)
                 {
                     if (userInterface != null )
                     {
                        userInterface.updateConsole("Host Unreachable: verify user data - (username, password) - Tools > Setting > Proxy ");
                     }
                     // connection error while reaching the site, prevent additional calls by returning.
                     return;
                 }
             }
             else
             {
                 try{
                     urlconn = (HttpURLConnection) url.openConnection();
                 }catch (UnknownHostException e)
                 {
                     if (userInterface != null )
                     {
                        userInterface.updateConsole("Host Unreachable: are you behind a proxy? if so, set the proxy configuration in the Tools > Setting > Proxy tab");
                     }
                     // connection error while reaching the site, prevent additional calls by returning.
                     return;
                 }
             }
             urlconn.setRequestMethod("GET");
             urlconn.setInstanceFollowRedirects(false);
             try {
                 urlconn.connect();
             }catch (UnknownHostException e)
             {
                 if (userInterface != null )
                 {
                    userInterface.updateConsole("Host Unreachable: are you behind a proxy? if so, set the proxy configuration in the Tools > Setting > Proxy tab");
                 }
                // connection error while reaching the site, prevent additional calls by returning.
                 return;
             }
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
 
             while (true){
                 String line = reader.readLine();
                 if ( line == null ){
                     break;
                 }
                 if (line.contains(getSearchString()))
                 {
                     if (userInterface != null )
                     {
                         userInterface.updateConsole("WARNING: \n> Item: "+code + "(url: "+ loader.getBaseUrl() + code + ") is available!");
                     }
                     availableItems.add(code);
                 }
             }
             reader.close();
             urlconn.disconnect();
         }
         loader.getFigureCodeList().removeAll(availableItems);
     }
 }
