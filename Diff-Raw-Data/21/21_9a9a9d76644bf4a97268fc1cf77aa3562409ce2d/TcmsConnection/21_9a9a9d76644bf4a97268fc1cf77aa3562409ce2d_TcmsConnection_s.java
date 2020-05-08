 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.nitrate;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import redstone.xmlrpc.XmlRpcClient;
 import redstone.xmlrpc.XmlRpcException;
 import redstone.xmlrpc.XmlRpcFault;
 import redstone.xmlrpc.XmlRpcStruct;
 import sun.misc.BASE64Encoder;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsConnection {
 
     private transient XmlRpcClient client;
     private String session;
     private URL url;
     private TcmsAccessCredentials credentials;
 
    public boolean testTcmsConnection() {
        try {
             HttpURLConnection connection = null;
             BufferedReader rd = null;
             StringBuilder sb = null;
             String line = null;
             boolean result = false;
 
             //Set up the initial connection
             connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("GET");
             connection.setDoOutput(true);
             connection.setReadTimeout(100000);
 
             ///Set basic auth
             if (!credentials.isEmpty()) {
                // FIXME !!!
                 String debug = basicAuthString(credentials.getUsername(), credentials.getPassword());
                 connection.setRequestProperty("Authorization", basicAuthString(credentials.getUsername(), credentials.getPassword()));
             }
 
             connection.connect();
             //read the result from the server
             rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             sb = new StringBuilder();
             while ((line = rd.readLine()) != null) {
                 sb.append(line + '\n');
             }
             if (sb.lastIndexOf("XML-RPC Service") > 0) {
                 result = true;
             }
             //close the connection, set all objects to null
             connection.disconnect();
 
 
             rd = null;
             sb = null;
             connection = null;
             return result;
        } catch (IOException ex) {
            Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }


     }
 
     public TcmsConnection(String server_url) throws MalformedURLException {
         this(new URL(server_url));
     }
 
     public TcmsConnection(URL server_url) {
         client = new XmlRpcClient(server_url, false);
         url = server_url;
         credentials = new TcmsAccessCredentials();
 
     }
 
     public void setSession(String session) {
         client.setRequestProperty("Cookie", "sessionid=".concat(session));
         this.session = session;
     }
 
     private String basicAuthString(String username, String password) {
         BASE64Encoder enc = new sun.misc.BASE64Encoder();
         String userpassword = username + ":" + password;
         String encodedAuthorization = enc.encode(userpassword.getBytes());
         return "Basic " + encodedAuthorization;
     }
 
     public void setUsernameAndPassword(String username, String password) {
         credentials.setUsername(username);
         credentials.setPassword(password);
         client.setRequestProperty("Authorization", basicAuthString(username, password));
     }
 
     public static <T extends Object> T rpcStructToFields(XmlRpcStruct data, Class<T> c) {
         Object object = null;
         try {
             object = c.newInstance();
 
 
             Field[] fields = c.getFields();
             for (Field field : fields) {
                 String name = getName(field);
                 if (data.containsKey(name)) {
                     Object value = data.get(name);
                     // fix string/integer abiguity
                     if((value instanceof String) && (field.getType() == Integer.class)){
                         String s = (String) value;
                         if(s.trim().length() == 0){
                             value = null;
                         }else{
                             value = Integer.getInteger((String)value);
                         }
                     }
                     field.set(object, value); //object.field=value;
                 }
             }
         } catch (InstantiationException ex) {
             Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
         }
         return (T) object;
     }
 
     public static Hashtable<String, Object> fieldsToHashtable(Object object) {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
 
         Field[] fields = object.getClass().getFields();
         for (Field field : fields) {
             String name = getName(field);
             Object value = null;
             try {
                 value = field.get(object);
             } catch (IllegalArgumentException ex) {
                 Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             }
             if (value != null) {
                 data.put(name, value);
             }
         }
         return data;
     }
 
     static String commandToString(TcmsCommand aThis) throws IllegalAccessException {
         String out = "";
         Collection c = (Collection) commandToParams(aThis);
         for (Object o : c) {
             if (o instanceof Hashtable) {
                 Hashtable<String, Object> ht = (Hashtable<String, Object>) o;
                 for (String key : ht.keySet()) {
                     out = out + "," + key + ":" + ht.get(key).toString();
                 }
             } else {
                 out = out + "," + o.toString();
             }
         }
         return out;
     }
 
     static String getName(Field field) {
         String s = field.getName();
         if (s.contains("caseVar")) {
             s = "case";
         }
         return s;
     }
 
     static List<Object> fieldsToCollection(Object object) {
         ArrayList data = new ArrayList();
 
         Field[] fields = object.getClass().getFields();
         for (Field field : fields) {
             String name = getName(field);
             Object value = null;
             try {
                 value = field.get(object);
             } catch (IllegalArgumentException ex) {
                 Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             if (TcmsArrayCommand.class.isInstance(value)) {
                 value = fieldsToCollection(value);
             } else if (TcmsHashCommand.class.isInstance(value)) {
                 value = fieldsToHashtable(value);
             }
 
             if (value != null) {
                 data.add(value);
             }
         }
         return data;
     }
 
     public static List commandToParams(TcmsCommand cmd) {
         Field[] fields = cmd.getClass().getFields();
 
         if (TcmsArrayCommand.class.isInstance(cmd)) {
             return fieldsToCollection(cmd);
         } else if (TcmsHashCommand.class.isInstance(cmd)) {
             Hashtable<String, Object> data = fieldsToHashtable(cmd);
             Vector params = new Vector();
             params.add(data);
             return params;
         }
         return null;
     }
 
     public Object invoke(TcmsCommand cmd) throws XmlRpcFault {
         try {
             List params = commandToParams(cmd);
             Object o = client.invoke(cmd.name(), params);
             return o;
         } catch (XmlRpcException ex) {
             Logger.getLogger(TcmsConnection.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 }
