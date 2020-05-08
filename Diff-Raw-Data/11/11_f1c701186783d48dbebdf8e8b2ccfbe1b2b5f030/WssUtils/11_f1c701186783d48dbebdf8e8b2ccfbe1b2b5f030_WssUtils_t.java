 /*
  * Copyright (C) 2012 White Source Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.whitesource.bamboo.agent;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
import org.whitesource.agent.client.WhitesourceService;
 
 /**
  * Class to hold common utility helper methods.
  * 
  * @author Edo.Shor
  */
 public final class WssUtils
 {
     /* --- Static members --- */
 
     private static final Pattern PARAM_LIST_SPLIT_PATTERN = Pattern.compile(",|$", Pattern.MULTILINE);
     private static final Pattern KEY_VALUE_SPLIT_PATTERN = Pattern.compile("=");
 
     /* --- Public methods --- */
 
     public static WhitesourceService createServiceClient()
     {
         // @todo: the service URL should likely be configurable (see e.g. the Teamcity agent)!
         WhitesourceService service = new WhitesourceService(Constants.AGENT_TYPE, Constants.AGENT_VERSION,
                 Constants.DEFAULT_SERVICE_URL);
 
         // Reuse hosting application proxy settings, if any (see https://confluence.atlassian.com/x/nAFgDQ for the
         // rationale).
         final String httpProxyHost = System.getProperty("http.proxyHost");
         if (httpProxyHost != null)
         {
             final int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort", "80"));
             final String proxyUser = System.getProperty("http.proxyUser");
             final String proxyPassword = System.getProperty("http.proxyPassword");
             service.getClient().setProxy("http://" + httpProxyHost, proxyPort, proxyUser, proxyPassword);
         }
         else
         {
             final String httpsProxyHost = System.getProperty("https.proxyHost");
             if (httpsProxyHost != null)
             {
                 final int proxyPort = Integer.parseInt(System.getProperty("https.proxyPort", "443"));
                 final String proxyUser = System.getProperty("http.proxyUser");
                 final String proxyPassword = System.getProperty("http.proxyPassword");
                 service.getClient().setProxy("https://" + httpsProxyHost, proxyPort, proxyUser, proxyPassword);
             }
         }
 
         return service;
     }
 
     public static String logMsg(String component, String msg)
     {
         return "[whitesource]::" + component + ": " + msg;
     }
 
     public static List<String> splitParameters(String paramList)
     {
         List<String> params = new ArrayList<String>();
 
         if (paramList != null)
         {
             String[] split = PARAM_LIST_SPLIT_PATTERN.split(paramList);
             if (split != null)
             {
                 for (String param : split)
                 {
                     if (!(param == null || param.trim().length() == 0))
                     {
                         params.add(param.trim());
                     }
                 }
             }
         }
 
         return params;
     }
 
     public static Map<String, String> splitParametersMap(String paramList)
     {
         Map<String, String> params = new HashMap<String, String>();
 
         List<String> kvps = splitParameters(paramList);
         if (kvps != null)
         {
             for (String kvp : kvps)
             {
                 String[] split = KEY_VALUE_SPLIT_PATTERN.split(kvp);
                 if (split.length == 2)
                 {
                     params.put(split[0], split[1]);
                 }
             }
         }
 
         return params;
     }
 }
