 /*
 Dynamo CouchDB Connector is a plugin for browsing CouchDB views in LucidDB.
 Copyright (C) 2011 Dynamo Business Intelligence Corporation
 
 This program is free software; you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by the Free
 Software Foundation; either version 2 of the License, or (at your option)
 any later version approved by Dynamo Business Intelligence Corporation.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 
 package com.dynamobi.db.conn.couchdb;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 
 
 import net.sf.farrago.type.FarragoParameterMetaData;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import org.apache.commons.codec.binary.Base64;
 
 import java.sql.ParameterMetaData;
 
 import java.sql.SQLException;
 import java.sql.PreparedStatement;
 
 /**
  * Generic Udx class to build any custom functions to browse CouchDB views.
  * @author Kevin Secretan
  */
 public class CouchUdx {
 
   /**
    * Called by a custom LucidDB function for each view.
    * @param userName - CouchDB user name
    * @param pw - CouchDB password
    * @param url - CouchDB REST URL
    * @param view - CouchDB REST view -- concatenated on the end of URL with
    *               a slash prefix if necessary.
    * @param limit - Limit parameter passed to couchdb
    * @param resultInserter - Table for inserting results. Assumed to have the
    * necessary column names in the order we get them.
    */
   public static void query(
       String userName,
       String pw,
       String url,
       String view,
       String limit,
       PreparedStatement resultInserter) throws SQLException {
 
     String[] paramNames = null;
     ParameterMetaData pmd;
     pmd = resultInserter.getParameterMetaData();
     // Specialize so we can column names for our resultInserter
 
     FarragoParameterMetaData fpmd = (FarragoParameterMetaData) pmd;
     int paramCount = fpmd.getParameterCount();
     paramNames = new String[paramCount];
     for ( int i = 0 ; i < paramCount; i++ ){
       paramNames[i] = fpmd.getFieldName(i+1); // JDBC offset
     }
 
     JSONArray lst = getViewRows(userName, pw, url, view, limit);
     if (lst == null)
       return;
 
     Iterator iter = lst.iterator();
     while (iter.hasNext()) {
       JSONObject obj = (JSONObject) iter.next();
       JSONObject cols = (JSONObject) obj.get("value");
 
       for ( int c = 0 ; c < paramNames.length ; c++ ) {
         Object o = cols.get(paramNames[c]);
         if ( o != null ) {
           // TODO work on data types
           resultInserter.setString(c+1, o.toString());
         }
 
       }
       resultInserter.executeUpdate();
     }
   }
   
   /**
    * Helper function gets the rows from the foreign CouchDB server and returns
    * them as a JSONArray.
    */
   private static JSONArray getViewRows(
       String user, String pw, String url, String view, String limit)
     throws SQLException {
     try {
       String full_url = makeUrl(url, view);
       String sep = (view.indexOf("?") == -1) ? "?" : "&";
       if (limit != null && limit.length() > 0)
         full_url += sep + "limit=" + limit;
       URL u = new URL(full_url);
       HttpURLConnection uc = (HttpURLConnection) u.openConnection();
       if ( user != null && user.length() > 0 ) { 
     	  uc.setRequestProperty(
           "Authorization", "Basic " + buildAuthHeader(user, pw));
       }
       uc.connect();
       String s = readStringFromConnection(uc);
 
       JSONParser parser = new JSONParser();
       // View Object
       JSONObject o = (JSONObject) parser.parse(s);
       JSONArray a = (JSONArray) o.get("rows");
       return a;
     } 
     catch (MalformedURLException e) {
       throw new SQLException("Bad URL.");
     } catch (IOException e) {
       throw new SQLException("Could not read data from URL.");
     } catch (ParseException e) {
       throw new SQLException("Could not parse data from URL.");
     }
   }
   
   /**
    * Concatenates a URL and a view together for the full CouchDB view.
    * If the string "_view" is not in either of the two, it will be
    * added automatically before the view parameter. (This is to allow for
    * ease-of-use in view_def creation.)
    */
   private static String makeUrl(String url, String view) {
     String full_url = url;
     if (url.charAt(url.length() - 1) != '/' && view.charAt(0) != '/')
       full_url += "/";
     if (url.indexOf("_view") == -1 &&
        view.indexOf("_view") == -1) {
       full_url += "_view";
       if (view.charAt(0) != '/')
         full_url += "/";
     }
     full_url += view;
     return full_url;
   }
 
   /**
    * Builds a standard basic authentication http header with the user and pass.
    */
   private static String buildAuthHeader (String username, String password) {
     String login = username + ":" + password;
     String encodedLogin = new String(Base64.encodeBase64(login.getBytes()));
     return encodedLogin;
   }
   
   /**
    * Reads the data from a http call into a string.
    */
   private static String readStringFromConnection (HttpURLConnection uc) throws IOException {
     InputStreamReader in = new InputStreamReader(uc.getInputStream());
     BufferedReader buff = new BufferedReader(in);
     StringBuffer sb = new StringBuffer();
     String line = null;
     do {
       line = buff.readLine();
       if ( line != null ) sb.append(line);
     } while ( line != null );
     
     return sb.toString();
   }
 
   /**
    * Creates a CouchDB view
    */
   public static void makeView(
       String user,
       String pw,
       String url,
       String viewDef) throws SQLException {
     try {
       URL u = new URL(url);
       HttpURLConnection uc = (HttpURLConnection) u.openConnection();
       uc.setDoOutput(true);
       if ( user != null && user.length() > 0 ) { 
         uc.setRequestProperty(
             "Authorization", "Basic " + buildAuthHeader(user, pw));
       }
       uc.setRequestProperty("Content-Type", "application/json");
       uc.setRequestMethod("PUT");
       OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
       wr.write(viewDef);
       wr.close();
 
       String s = readStringFromConnection(uc);
     } catch (MalformedURLException e) {
       throw new SQLException("Bad URL.");
     } catch (IOException e) {
       throw new SQLException(e.getMessage());
     }
   }
   
 }
