 /*
  * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
  *
  * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
  * In most instances, the license terms are contained in a file named license.txt.
  */
 package org.fabrican.extension.variable.provider;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.Properties;
 
 import org.apache.commons.jexl2.Expression;
 import org.apache.commons.jexl2.JexlContext;
 import org.apache.commons.jexl2.JexlEngine;
 import org.apache.commons.jexl2.MapContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.datasynapse.fabric.admin.info.ComponentInfo;
 import com.datasynapse.fabric.admin.info.FabricEngineInfo;
 import com.datasynapse.fabric.admin.info.StackInfo;
 import com.datasynapse.fabric.broker.userartifact.variable.AbstractDynamicVariableProvider;
 
 public class VariableProviderProxy extends AbstractDynamicVariableProvider {
 
     private String serverURL;
     private String primaryKey;
     private String secondaryKey;
     private JexlEngine jexl = new JexlEngine();
     
     
     public Properties getVariables(FabricEngineInfo engineInfo, StackInfo stackInfo, ComponentInfo componentInfo) {
         
         MapContext jc = new MapContext();
         jc.set("engineInfo", engineInfo);
         jc.set("stackInfo", stackInfo);
         jc.set("componentInfo", componentInfo);
         String p = evaluate(getPrimaryKey(), jc);
         String s = evaluate(getSecondaryKey(), jc);;
 
         if ( getServerURL() == null ){
             throw new IllegalArgumentException("serverURL is not set");
         }
         String u = getServerURL();
         try {
             u += "?primary=" + (p == null ? "" : URLEncoder.encode(p.trim(), "utf-8"));
            u += "&secondary=" + (p == null ? "" : URLEncoder.encode(p.trim(), "utf-8"));
         } catch (UnsupportedEncodingException e) {
             // we always have "UTF-8" but anyway
             throw new RuntimeException("utf-8 not supported", e);
         }
         InputStream is = null;
         try {
             URL url = new URL(u);
             URLConnection c = url.openConnection();
             is = c.getInputStream();
             JSONTokener tokener = new JSONTokener(new InputStreamReader(is, "utf-8"));
             JSONObject jObj = new JSONObject(tokener);
             Properties r = new Properties();
             Iterator<String> keys = jObj.keys();
             while ( keys.hasNext() ){
                 String k = keys.next();
                 r.put(k, jObj.getString(k));
             }
             return r;
         }catch (IOException ioe){
             throw new RuntimeException("failed to connection to web server", ioe);
         } catch (JSONException je) {
             throw new RuntimeException("failed to parse response from web server", je);
         }finally {
             try{
                 if ( is != null ){
                     is.close();
                 }                
             }catch (Exception e){
                 
             }
         }            
 
     }
 
     private String evaluate(String propExpr, JexlContext jc) {
         if (propExpr != null ){
             Expression e = jexl.createExpression( propExpr );
             return (String) e.evaluate(jc);
         } else { 
             return null;
         }
     }
 
     public void destroy() {
     }
 
     public void init() throws Exception {
     }
 
     public String getServerURL() {
         return serverURL;
     }
 
     public void setServerURL(String serverURL) {
         this.serverURL = serverURL;
     }
 
     public String getPrimaryKey() {
         return primaryKey;
     }
 
     public void setPrimaryKey(String primaryKey) {
         this.primaryKey = primaryKey;
     }
 
     public String getSecondaryKey() {
         return secondaryKey;
     }
 
     public void setSecondaryKey(String secondaryKey) {
         this.secondaryKey = secondaryKey;
     }
 
 }
