 /**
  * RapidContext HTTP plug-in <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2009 Per Cederberg & Dynabyte AB.
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.plugin.http;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import org.rapidcontext.core.proc.AddOnProcedure;
 import org.rapidcontext.core.proc.Bindings;
 import org.rapidcontext.core.proc.CallContext;
 import org.rapidcontext.core.proc.ProcedureException;
 
 /**
  * An HTTP POST procedure. This procedure provides simplified access
  * to HTTP data sending and retrieval.
  *
  * @author   Per Cederberg, Dynabyte AB
  * @version  1.0
  */
 public class HttpPostProcedure extends AddOnProcedure {
 
     /**
      * The binding name for the HTTP connection.
      */
     public static final String BINDING_CONNECTION = "connection";
 
     /**
      * The binding name for the HTTP URL.
      */
     public static final String BINDING_URL = "url";
 
     /**
      * The binding name for the additional HTTP headers.
      */
     public static final String BINDING_HEADER = "header";
 
     /**
      * The binding name for the HTTP POST data (name and value pairs).
      */
     public static final String BINDING_DATA = "data";
 
     /**
      * Creates a new HTTP POST procedure.
      *
      * @throws ProcedureException if the initialization failed
      */
     public HttpPostProcedure() throws ProcedureException {
         defaults.set(BINDING_CONNECTION, Bindings.CONNECTION, null,
                      "The HTTP connection pool name, set to null for none.");
         defaults.set(BINDING_URL, Bindings.DATA, "",
                      "The HTTP URL to send the data to. May be relative to " +
                      "the connection pool URL.");
         defaults.set(BINDING_HEADER, Bindings.DATA, "",
                      "The additional HTTP headers or blank for none.");
         defaults.set(BINDING_DATA, Bindings.DATA, "",
                      "The HTTP POST data to send or blank for none.");
         defaults.seal();
     }
 
     /**
      * Executes a call of this procedure in the specified context
      * and with the specified call bindings. The semantics of what
      * the procedure actually does, is up to each implementation.
      * Note that the call bindings are normally inherited from the
      * procedure bindings with arguments bound to their call values.
      *
      * @param cx             the procedure call context
      * @param bindings       the call bindings to use
      *
      * @return the result of the call, or
      *         null if the call produced no result
      *
      * @throws ProcedureException if the call execution caused an
      *             error
      */
     public Object call(CallContext cx, Bindings bindings)
         throws ProcedureException {
 
         String  str;
         Object  obj;
 
         obj = bindings.getValue(BINDING_CONNECTION, null);
         if (obj != null && !HttpChannel.class.isInstance(obj)) {
             str = "connection not of HTTP type: " + obj.getClass().getName();
             throw new ProcedureException(str);
         }
         return execCall(cx, (HttpChannel) obj, bindings);
     }
 
     /**
      * Executes a call of this procedure in the specified context
      * and with the specified call bindings.
      *
      * @param cx             the procedure call context
      * @param con            the HTTP connection or null for none
      * @param bindings       the call bindings to use
      *
      * @return the result of the call, or
      *         null if the call produced no result
      *
      * @throws ProcedureException if the call execution caused an
      *             error
      */
     static Object execCall(CallContext cx, HttpChannel con, Bindings bindings)
         throws ProcedureException {
 
         URL             url;
         LinkedHashMap   headers;
         String          str;
         Object          obj;
 
         obj = bindings.getValue(BINDING_URL, null);
         try {
             if (con != null && obj != null) {
                 url = new URL(con.getUrl(), obj.toString());
             } else if (con != null) {
                 url = con.getUrl();
             } else {
                 url = new URL(bindings.getValue(BINDING_URL).toString());
             }
         } catch (MalformedURLException e) {
             throw new ProcedureException("malformed URL: " + obj);
         }
         headers = new LinkedHashMap();
         if (con != null) {
             str = replaceArguments(con.getHeaders(), bindings);
             parseHeaders(headers, str);
         }
         str = (String) bindings.getValue(BINDING_HEADER, "");
         parseHeaders(headers, replaceArguments(str, bindings));
         str = (String) bindings.getValue(BINDING_DATA);
         str = replaceArguments(str, bindings);
         return sendReceive(url, headers, str);
     }
 
     /**
      * Parses a string with HTTP headers into a result value map.
      *
      * @param map            the result name and value map
      * @param data           the unparsed header string
      */
     private static void parseHeaders(LinkedHashMap map, String data) {
        for (String line : data.split("[\\n\\r]+")) {
            String[] parts = line.split("\\s*:\\s*", 2);
            if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1].trim());
            }
         }
     }
 
     /**
      * Replaces any parameters with the corresponding argument value
      * from the bindings.
      *
      * @param data           the data string to process
      * @param bindings       the bindings to use
      *
      * @return the processed data string
      *
      * @throws ProcedureException if some parameter couldn't be found
      */
     private static String replaceArguments(String data, Bindings bindings)
         throws ProcedureException {
 
         String[]  names = bindings.getNames();
         Object    value;
 
         for (int i = 0; i < names.length; i++) {
             if (bindings.getType(names[i]) == Bindings.ARGUMENT) {
                 value = bindings.getValue(names[i], null);
                 if (value == null) {
                     value = "";
                 }
                 data = data.replaceAll("\\:" + names[i], value.toString());
             }
         }
         return data;
     }
 
     /**
      * Sends an HTTP POST request and returns the response as a string.
      *
      * @param url            the URL to use
      * @param headers        the HTTP headers
      * @param data           the HTTP POST payload
      *
      * @return the response text
      *
      * @throws ProcedureException if the request sending caused an
      *             exception
      */
     private static String sendReceive(URL url, LinkedHashMap headers, String data)
         throws ProcedureException {
 
         HttpURLConnection  con;
         DataOutputStream   os;
         BufferedReader     is;
         StringBuffer       response = new StringBuffer();
         Iterator           iter;
         String             str;
 
         try {
             con = (HttpURLConnection) url.openConnection();
         } catch (IOException e) {
             throw new ProcedureException("failed to open URL: " +
                                          e.getMessage());
         }
         try {
             con.setRequestMethod("POST");
         } catch (ProtocolException e) {
             throw new ProcedureException("invalid method: " + e.getMessage());
         }
         con.setDoInput(true);
         con.setDoOutput(true);
         con.setUseCaches(false);
         con.setInstanceFollowRedirects(false);
         con.setConnectTimeout(10000);
         iter = headers.keySet().iterator();
         while (iter.hasNext()) {
             str = (String) iter.next();
             con.setRequestProperty(str, (String) headers.get(str));
         }
         con.setRequestProperty("Content-Length",
                                Integer.toString(data.getBytes().length));
         try {
             con.connect();
             os = new DataOutputStream(con.getOutputStream());
             os.writeBytes(data);
             os.flush();
             os.close();
             is = new BufferedReader(new InputStreamReader(con.getInputStream()));
             while ((str = is.readLine()) != null) {
                 response.append(str);
             }
             is.close();
         } catch (IOException e) {
             throw new ProcedureException(e.getMessage());
         } finally {
             con.disconnect();
         }
         return response.toString();
     }
 }
