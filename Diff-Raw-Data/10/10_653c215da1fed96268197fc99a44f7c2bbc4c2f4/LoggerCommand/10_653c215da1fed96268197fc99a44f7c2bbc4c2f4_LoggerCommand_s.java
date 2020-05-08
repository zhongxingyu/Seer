 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.cli;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.http.HttpMessage;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import ch.qos.logback.classic.Level;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.gson.Gson;
 import com.meltmedia.cadmium.core.LoggerConfig;
 import com.meltmedia.cadmium.core.LoggerServiceResponse;
 
 /**
  * <p>CLI Command that queries or updates the logging level of each node in the cluster.</p>
  * <p>The @Parameters of this class get wired up by JCommander.</p>
  * 
  * @see <a href="http://jcommander.org/">JCommander</a>
  * 
  * @author John McEntire
  *
  */
 @Parameters(commandDescription="This command will query or update the logging level.", separators="=")
 public class LoggerCommand extends AbstractAuthorizedOnly implements CliCommand {
   
   /**
    * A collection of strings that will be assigned the raw parameters that are not associated with an option.
    * 
    * @see <a href="http://jcommander.org/">JCommander</a>
    */
   @Parameter(description="[logger] [level] <site>")
   private List<String> params;
 
   @Override
   public String getCommandName() {return "logger";}
 
   @Override
   public void execute() throws Exception {
     String logger = null;
     String level = null;
     String site = null;
     if(params != null && params.size() >= 3) {
       logger = params.get(0);
       level = params.get(1);
       site = params.get(2);
     } else if (params != null && params.size() == 2) {
       String value = params.get(0);
       try {
         level = Level.toLevel(value, null).levelStr;
       } catch(Exception e) {
         logger = value;
       }
       site = params.get(1);
     } else if(params != null && params.size() == 1) {
       site = params.get(0);
     } else {
       System.err.println("A site is required!");
       System.exit(1);
     }
    DefaultHttpClient client = new DefaultHttpClient();
     HttpMessage method = null;
     if(level != null) {
      String uri = this.getSecureBaseUrl(site)+"/system/logger/"+(StringUtils.isNotBlank(logger)?logger+"/":ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME+"/")+(StringUtils.isNotBlank(level)?level:"DEBUG");
       System.out.println("Updating logger ["+(StringUtils.isNotBlank(logger)?logger:"ROOT") + "] to level ["+level+"] for site " + site);
       method = new HttpPost(uri);
     } else {
      String uri = this.getSecureBaseUrl(site)+"/system/logger/"+(StringUtils.isNotBlank(logger)?logger+"/":"");
       System.out.println("Getting levels for "+(StringUtils.isNotBlank(logger)?logger:"all") + " logger[s] on site " + site);
       method = new HttpGet(uri);
     }
     addAuthHeader(method);
     HttpResponse response = client.execute((HttpUriRequest) method);
     if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
       LoggerServiceResponse configs = new Gson().fromJson(EntityUtils.toString(response.getEntity()), LoggerServiceResponse.class);
       List<String> nodes = new ArrayList<String>();
       nodes.addAll(configs.getConfigs().keySet());
       Collections.sort(nodes);
       List<String> loggers = new ArrayList<String>();
       Map<String, String[]> loggerLevels = new HashMap<String, String[]>();
       for(String node: nodes) {
         for(LoggerConfig config : configs.getConfigs().get(node)){
           if(!loggers.contains(config.getName())) {
             loggers.add(config.getName());
             loggerLevels.put(config.getName(), new String[nodes.size()]);
             Arrays.fill(loggerLevels.get(config.getName()), "-");
           }
           loggerLevels.get(config.getName())[nodes.indexOf(node)] = config.getLevel();
         }
       }
       
       Collections.sort(loggers);
       if(loggers.remove(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)) {
         loggers.add(0, ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
       }
       
       System.out.println("Got " + loggers.size() + " logger[s] and " + nodes.size() + " node[s]");
       
       for(String loggerName : loggers) {
         System.out.println("Logger: "+loggerName);
         String levels[] = loggerLevels.get(loggerName);
         for(String node : nodes) {
           System.out.println("  " + node + ": "+levels[nodes.indexOf(node)]);
         }
       }
     } else {
       System.err.println("Request failed: " + response.getStatusLine());
       System.err.println("Raw response [" + EntityUtils.toString(response.getEntity()) + "]");
     }
   }
 
 }
