 /*
  *  McDesktopInfo - A Bukkit plugin + Windows Sidebar Gadget
  *  Copyright (C) 2012  Damarus
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.damarus.mcdesktopinfo.socket;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.util.HashMap;
 
 import de.damarus.mcdesktopinfo.McDesktopInfo;
 import de.damarus.mcdesktopinfo.PasswordSystem;
 import de.damarus.mcdesktopinfo.queries.Query;
 import de.damarus.mcdesktopinfo.queries.QueryEnum;
 
 public class ConnectionHandler implements Runnable {
 
     private Socket       socket;
 
     public ConnectionHandler(Socket socket) {
         this.socket = socket;
     }
 
     @Override
     public void run() {
         try {
             // Get in and out streams
             BufferedReader sIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream sOut = new DataOutputStream(socket.getOutputStream());
 
             String query = sIn.readLine();
 
             HashMap<String, String> params = new HashMap<String, String>();
 
             // Splitting into the important part
             query = query.substring(query.indexOf("?") + 1);
             if(query.contains(" HTTP/")) query = query.substring(0, query.indexOf(" HTTP/"));
 
             String[] paramsWithValue = query.split("[&]");
 
             for(int i = 0; i < paramsWithValue.length; i++) {
                 String[] param = paramsWithValue[i].split("[=]");
 
                 // Skip faulty parameters
                 if(param.length != 2) {
                     continue;
                 }
 
                 // Decode parameters and put them into map
                 params.put(URLDecoder.decode(param[0], "UTF-8"), URLDecoder.decode(param[1], "UTF-8"));
             }
 
             params.remove("rnd");
             params.put("gadgetIp", socket.getInetAddress().getHostAddress());
 
             // Form response to the given query (Everything is in first line)
             String response = get(params.get("action"), params);
 
             sOut.writeBytes(response); // Give the response back to the client
             sOut.flush(); // Make sure, that data is sent now
 
             socket.close(); // Transmission done...
         } catch (IOException e) {
             McDesktopInfo.log("Caught an exception while answering a query.");
             e.printStackTrace();
         }
     }
     
     public String get(String query, HashMap<String, String> params) {
        try {
            Query queryObj = QueryEnum.valueOf(QueryEnum.class, query.toUpperCase()).getQueryObj();
            
            if(queryObj.isDisabled()) return "";
            if(queryObj.isUserExecutable()) return queryObj.execute(params);
            if(queryObj.isAdminOnly()) return PasswordSystem.checkAdminPW(params.get("adminPw")) ? queryObj.execute(params) : "";
        } catch(IllegalArgumentException e) {
            McDesktopInfo.log("Received unknown query \"" + query + "\"");
         }
         
         return "";
     }
 }
