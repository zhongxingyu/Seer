 package com.nuecho.grammarserver.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 final class ServerSession implements Session
 {
     private String mAuthToken;
     private GrammarServer mServer;
     private String mSessionId;
 
     ServerSession(GrammarServer server, String authToken) throws GrammarServerException
     {
         mServer = server;
         mAuthToken = authToken;
         initializeSession();
     }
 
     ServerSession(GrammarServer server, String username, String password) throws GrammarServerException
     {
         this(server, (new Base64()).encode((username + ":" + password).getBytes()));
     }
 
     private void initializeSession() throws GrammarServerException
     {
         String url = mServer.getUrl() + "/session";
         String answer = sendHttpRequest(url, "POST", true, null, "responseFormat=json");
         JSONObject session = (JSONObject) JSONValue.parse(answer);
 
         mSessionId = (String) ((JSONObject) session.get("session")).get("id");
     }
 
     public String getSessionId()
     {
         return mSessionId;
     }
 
     public void disconnect()
     {
         try
         {
             String url = mServer.getUrl() + "/session/" + mSessionId;
             sendHttpRequest(url, "DELETE", true, null, null);
         }
         catch (GrammarServerException exception)
         {
             // do nothing
         }
     }
 
     public InstantiatedGrammar instantiate(String grammarPath, String jsonContext) throws GrammarServerException
     {
         try
         {
             Object context = new JSONParser().parse(jsonContext);
            // just make sure that we have a valid JSON context
             assert context instanceof JSONObject;
            return instantiateGrammar(grammarPath, jsonContext);
        
         }
         catch (ParseException exception)
         {
             throw new GrammarServerException("Invalid JSON context", exception);
         }
     }
 
     public InstantiatedGrammar instantiate(String grammarPath, JSONObject context) throws GrammarServerException
     {
        assert context != null;
        return instantiateGrammar(grammarPath, context.toJSONString());
    }
 
    @SuppressWarnings("unchecked")
    private InstantiatedGrammar instantiateGrammar(String grammarPath, String jsonContext)
            throws GrammarServerException
    {
        String url = mServer.getUrl() + "/grammar/" + mSessionId + "/" + grammarPath;
         Map data = new HashMap();
         data.put("responseFormat", "json");
         data.put("context", jsonContext);
         String response = sendHttpRequest(url, "POST", true, data, null);
 
         JSONObject grammarData = (JSONObject) JSONValue.parse(response);
         return new Grammar((JSONObject) grammarData.get("grammar"));
     }
 
     public InstantiatedGrammar load(String grammarPath) throws GrammarServerException
     {
         return instantiate(grammarPath, new JSONObject());
     }
 
     public void upload(String grammarPath, String content) throws GrammarServerException
     {
         String url = mServer.getUrl() + "/grammar/" + grammarPath;
         sendHttpRequest(url, "PUT", true, null, content);
     }
 
     @SuppressWarnings("unchecked")
     public String sendHttpRequest(String url, String method, boolean needsAuthorization, Map data, String text)
             throws GrammarServerException
     {
         HttpURLConnection connection;
         try
         {
             connection = (HttpURLConnection) (new java.net.URL(url)).openConnection();
             connection.setRequestMethod(method);
             if (needsAuthorization)
             {
                 connection.setRequestProperty("Authorization", "Basic " + mAuthToken);
             }
             if (data != null || text != null)
             {
                 String content = "";
                 if (text != null)
                 {
                     content = text;
                 }
                 else
                 {
                     StringBuffer buffer = new StringBuffer();
                     for (Iterator iterator = data.keySet().iterator(); iterator.hasNext();)
                     {
                         String key = (String) iterator.next();
                         
                         buffer.append("&").append(key).append("=").append(data.get(key).toString());
                     }
                     content = buffer.substring(1);
                 }
                 connection.setDoOutput(true);
                 OutputStreamWriter writer = new java.io.OutputStreamWriter(connection.getOutputStream());
                 writer.write(content);
                 writer.close();
             }
             connection.setConnectTimeout(5000);
             if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300)
             {
                 String response = readStreamAsString(connection.getInputStream());
                 connection.disconnect();
                 return response;
             }
             else
             {
                 connection.disconnect();
                 throw new GrammarServerException("com.nuecho.grammarserver.exception:" + connection.getResponseMessage());
             }
         }
         catch (MalformedURLException exception)
         {
             throw new GrammarServerException("Invalid URL", exception);
         }
         catch (IOException exception)
         {
             throw new GrammarServerException("Communication error with grammarserver", exception);
         }
     }
 
     private String readStreamAsString(InputStream inputStream) throws IOException
     {
         StringBuffer buffer = new java.lang.StringBuffer();
         BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
 
         while (reader.ready())
         {
             buffer.append(reader.readLine()).append('\n');
         }
         return "" + buffer.toString();
     }
 
     private class Grammar implements InstantiatedGrammar
     {
         private JSONObject mData;
 
         public Grammar(JSONObject data)
         {
             mData = data;
         }
 
         public String getContent() throws GrammarServerException
         {
             return getContent(null);
         }
 
         public String getContent(String format) throws GrammarServerException
         {
             return sendHttpRequest(getUrl(format), "GET", false, null, null);
         }
 
         @SuppressWarnings("unchecked")
         public Object interpret(String sentence) throws GrammarServerException
         {
             assert (sentence != null);
             String url = mServer.getUrl() + "/interpretation/" + mSessionId + "/" + mData.get("id");
             JSONObject data = new JSONObject();
             data.put("sentence", sentence);
             data.put("responseFormat", "json");
             String response = sendHttpRequest(url, "POST", true, data, null);
 
             JSONObject jsonResponse = (JSONObject) JSONValue.parse(response);
             return jsonResponse.get("interpretation");
         }
 
         public String getUrl()
         {
             return getUrl(null);
         }
 
         public String getUrl(String format)
         {
             assert (format == null || format.equals("abnf") || format.equals("grxml") || format.equals("gsl"));
             String url = mServer.getUrl() + "/grammar/" + mSessionId + "/" + mData.get("id");
             if (format != null)
             {
                 url += "." + format;
             }
             return url;
         }
     }
 }
