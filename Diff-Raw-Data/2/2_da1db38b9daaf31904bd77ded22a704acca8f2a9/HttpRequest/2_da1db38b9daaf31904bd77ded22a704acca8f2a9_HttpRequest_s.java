 /*
     Copyright (C) 2010 Brian Dunigan
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.openstatic.http;
 
 import java.util.Hashtable;
 import java.net.Socket;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Enumeration;
 
 import org.json.*;
 
 public class HttpRequest
 {
     private Hashtable<String, String> headers;
     private Hashtable<String, String> cookies;
     private Hashtable<String, String> post_data;
     private Hashtable<String, String> get_data;
     private String raw_post;
     private String raw_get;
     private Socket socket;
     private OutputStream socket_output_stream;
     
     private String path;
     private String request_type;
     private String request_prefix;
     private String set_cookie;
     private String clientHostname;
     private String serverHostname;
     private PlaceboHttpServer myServer;
     private PlaceboSession mySession;
     
    private HttpRequest(String path, String request_type, Hashtable<String, String> headers, Hashtable<String, String> cookies, Hashtable<String, String> post_data, String raw_post, Hashtable<String, String> get_data, String raw_get, Socket socket, PlaceboHttpServer myServer)
     {
         this.path = path;
         this.request_prefix = null;
         this.request_type = request_type;
         this.headers = headers;
         this.cookies = cookies;
         this.post_data = post_data;
         this.raw_post = raw_post;
         this.get_data = get_data;
         this.raw_get = raw_get;
         this.socket = socket;
         this.myServer = myServer;
         this.mySession = null;
         this.set_cookie = null;
         this.clientHostname = this.socket.getInetAddress().getCanonicalHostName();
         this.serverHostname = this.socket.getLocalAddress().getCanonicalHostName();
         try
         {
             this.socket_output_stream = socket.getOutputStream();
         } catch (Exception e) {
             this.myServer.logln(this.clientHostname, "Failed Initialization of output stream");
         }
     }
     
     /** Return the server object that created this request object */
     public PlaceboHttpServer getPlaceboHttpServer()
     {
         return this.myServer;
     }
     
     /** Set the session object this request belongs to */
     protected void setSession(PlaceboSession session)
     {
         this.mySession = session;
     }
     
     /** Retrieve the session object this request belongs to */
     public PlaceboSession getPlaceboSession()
     {
         return this.mySession;
     }
     
     public void setRequestPrefix(String value)
     {
         this.request_prefix = value;
     }
     
     public void setCookie(String cookie)
     {
         this.set_cookie = cookie;
     }
     
     /** Return the full path of this request */
     public String getFullPath()
     {
         return this.path;
     }
     
     /** Get the localized path of this request */
     public String getPath()
     {
         String return_path = this.path;
         if (this.request_prefix != null)
             if (this.path.startsWith(this.request_prefix))
                 return_path = this.path.substring(this.request_prefix.length()-1);
         return return_path;
     }
     
     /** Return the entire path with parameters */
     public String getPathWithParams()
     {
         String return_path = this.path;
         if (!this.raw_get.equals(""))
             return_path += "?" + this.raw_get;
         return return_path;
     }
     
     /** Retrieve the hostname of the client side of this request */
     public String getClientHostname()
     {
         return this.clientHostname;
     }
     
     /** Get the hostname of the server */
     public String getServerHostname()
     {
         return this.serverHostname;
     }
     
     /** Retrieve the type of this request, either "GET" or "POST" */
     public String getRequestType()
     {
         return this.request_type;
     }
     
     /** Get the value of an http header in this request */ 
     public String getHttpHeader(String key)
     {
         return this.headers.get(key);
     }
     
     /** Get the value of a cookie sent in the header of this request */
     public String getCookie(String key)
     {
         return this.cookies.get(key);
     }
     
     /** Get the value of a variable posted by form */
     public String getPostValue(String key)
     {
         return this.post_data.get(key);
     }
     
     /** Returns a string containing the raw contents of the post */
     public String getRawPost()
     {
         return this.raw_post;
     }
     
     /** Returns everything after the question mark in the url */
     public String getRawGet()
     {
         return this.raw_get;
     }
     
     /** Return a JSONObject of a posted JSON Object */
     public JSONObject getJSONObjectPost() throws JSONException
     {
         return new JSONObject(this.raw_post);
     }
     
     /** a JSONArray of a posted JSON Array */
     public JSONArray getJSONArrayPost() throws JSONException
     {
         return new JSONArray(this.raw_post);
     }
     
     /** Return the value of a url parameter */
     public String getGetValue(String key)
     {
         return this.get_data.get(key);
     }
     
     /** Return an enumeration containing all the keys of the url parameters */
     public Enumeration<String> getGetKeys()
     {
         return this.get_data.keys();
     }
     
     /** Return an enumeration containing all the fields of a posted form */
     public Enumeration<String> getPostKeys()
     {
         return this.post_data.keys();
     }
     
     public void sendResponse(HttpResponse response)
     {
         InputStream data = response.getData();
         long data_length = response.getDataLength();
         socketWrite("HTTP/1.1 " + response.getResponseCode() + "\r\n");
         socketWrite("Server: OpenstaticPlacebo/1.0\r\n");
         socketWrite("Date: " + (new Date()).toString() + "\r\n");
         socketWrite("Content-Type: " + response.getContentType() + "\r\n");
         if (data_length != -1)
             socketWrite("Content-Length: " + String.valueOf(data_length) + "\r\n");
         if (this.set_cookie != null)
         {
             socketWrite("Set-Cookie: " + this.set_cookie + "\r\n");
         }
         socketWrite("\r\n");
         if (data != null)
         {
             try
             {
                 int inputByte;
                 if (this.myServer.isShowData())
                 {
                     this.myServer.getDebugStream().println("-------------Outbound Data-------------");
                 }
                 while ( (inputByte = data.read()) > -1 )
                 {
                     this.socket_output_stream.write(inputByte);
                     if (this.myServer.isShowData())
                     {
                         this.myServer.getDebugStream().print((char) inputByte);
                     }
                 }
                 data.close();
                 this.socket_output_stream.flush();
             } catch (Exception cs_exc) {
                 this.myServer.logln(this.clientHostname, "Flush Exception: " + cs_exc.toString() + " / " + cs_exc.getMessage());
             }
             if (this.myServer.isShowData())
             {
                 this.myServer.getDebugStream().println("");
                 this.myServer.getDebugStream().println("---------------------------------------");
             }
         }
         try
         {
             this.socket_output_stream.close();
             this.socket.close();
         } catch (Exception close_error) {
             this.myServer.logln(this.clientHostname, "Close Exception: " + close_error.toString() + " / " + close_error.getMessage());
         }
     }
 
     private void socketWrite(String out)
     {
         try
         {
             this.socket_output_stream.write(out.getBytes());
             this.myServer.logln(this.clientHostname, "<- " + out);
         } catch (Exception we) {
             this.myServer.logln(this.clientHostname, "<!!!- " + out);
         }
     }
 }
