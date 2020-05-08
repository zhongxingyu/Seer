 package com.rfcode.zm.examples;
 
 /*
  * #%L
  * ZoneManagerApiExamples
  * %%
  * Copyright (C) 2012 RF Code, Inc.
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
 * 
  *      http://www.apache.org/licenses/LICENSE-2.0
 * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.ParameterException;
 
 import java.net.URI;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.AuthCache;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.BasicAuthCache;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 public class Main {
 
     protected static enum Format { JSON, TXT, XML};
 
     @Parameter(names = {"--host"}, description="[Host/IP Address]:port")
     private String host = "localhost:6580";
 
     @Parameter(names = {"-f", "--format"}, description="Format for results (json, txt, xml)")
     protected Format format = Format.JSON;
 
     @Parameter(names = {"-u", "--user"}, description="Specify the user name and password to use for authentication")
     private String credentials;
 
     private String domain;
     private int port;
 
     private DefaultHttpClient client;
     private HttpContext context;
 
     private final Map<String, Runnable> _cmds = new HashMap<String, Runnable>();
 
     public static void main(String[] args) {
         new Main(args);
     }
 
     public Main(String[] args) {
         JCommander jc = new JCommander();
 
         _cmds.put("help", new Help(jc));
 
         _cmds.put("loclist", new LocList());
         _cmds.put("readerlist", new ReaderList());
         _cmds.put("taglistbyattrib", new TagListByAttrib());
         _cmds.put("tagprint", new TagPrint());
         _cmds.put("tagprintbyattrib", new TagPrintByAttrib());
         _cmds.put("tagupdates", new TagUpdates());
 
         for (Map.Entry<String, Runnable> entry : _cmds.entrySet()) {
             jc.addCommand(entry.getKey(), entry.getValue());
         }
 
         try {
             jc.parse(args);
 
             String name = jc.getParsedCommand();
             if (name != null) {
                 Runnable cmd = _cmds.get(name);
                 cmd.run();
             }
             else {
                 showUsageAndExit(jc, null);
             }
         }
         catch (Exception e) {
             showUsageAndExit(jc, e.getMessage());
         }
     }
 
     private void showUsageAndExit(JCommander jc, String msg) {
         StringBuilder usage = new StringBuilder();
         jc.usage(usage);
         System.err.println(usage);
         if (msg != null) {
             System.err.println(msg);
         }
         System.exit(0);
     }
 
     private static class Help implements Runnable {
 
         @Parameter(description="Command name")
         private List<String> _parameters = new ArrayList<String>();
 
         private final JCommander _jc;
 
         public Help(JCommander jc) {
             _jc = jc;
         }
 
         @Override()
         public void run() {
            String name = null;
            if (!_parameters.isEmpty()) {
                name = _parameters.get(0);
             }

            _jc.usage(name);
         }
     }
 }
 
