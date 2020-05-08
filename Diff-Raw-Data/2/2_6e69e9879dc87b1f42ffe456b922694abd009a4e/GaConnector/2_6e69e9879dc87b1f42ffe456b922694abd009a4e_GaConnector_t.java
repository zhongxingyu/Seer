 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.connector;
 
 import com.gooddata.exception.InvalidCommandException;
 import com.gooddata.util.CSVWriter;
 import com.gooddata.connector.backend.ConnectorBackend;
 import com.gooddata.exception.InternalErrorException;
 import com.gooddata.exception.InvalidArgumentException;
 import com.gooddata.exception.ProcessingException;
 import com.gooddata.google.analytics.FeedDumper;
 import com.gooddata.google.analytics.GaQuery;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.processor.CliParams;
 import com.gooddata.processor.Command;
 import com.gooddata.processor.ProcessingContext;
 import com.gooddata.util.FileUtil;
 import com.google.gdata.client.ClientLoginAccountType;
 import com.google.gdata.client.analytics.AnalyticsService;
 import com.google.gdata.data.analytics.DataFeed;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 /**
  * GoodData Google Analytics Connector
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class GaConnector extends AbstractConnector implements Connector {
 
     public static final String GA_DATE = "ga:date";
     
     private static Logger l = Logger.getLogger(GaConnector.class);
 
     private static final String APP_NAME = "gdc-ga-client";
 
     private String googleAnalyticsUsername;
     private String googleAnalyticsPassword;
     private String googleAnalyticsToken;
     private GaQuery googleAnalyticsQuery;
 
     /**
      * Creates a new Google Analytics Connector
      * @param connectorBackend connector backend
      */
     protected GaConnector(ConnectorBackend connectorBackend) {
         super(connectorBackend);
     }
 
      /**
       * Creates a new Google Analytics Connector
       * @param connectorBackend connector backend
       * @return a new instance of the GA connector
       *
      */
     public static GaConnector createConnector(ConnectorBackend connectorBackend) {
         return new GaConnector(connectorBackend);
     }
 
     /**
      * Saves a template of the config file
      * @param name the new config file name 
      * @param configFileName the new config file name
      * @param gQuery the Google Analytics query
      * @throws com.gooddata.exception.InvalidArgumentException if there is a problem with arguments
      * @throws IOException if there is a problem with writing the config file
      */
     public static void saveConfigTemplate(String name, String configFileName, GaQuery gQuery)
             throws IOException {
         l.debug("Saving GA config template.");
         String dims = gQuery.getDimensions();
         String mtrs = gQuery.getMetrics();
         SourceSchema s = SourceSchema.createSchema(name);
         if(dims != null && dims.length() > 0) {
             String[] dimensions = dims.split("\\|");
             for(String dim : dimensions) {
                 // remove the "ga:"
                 if(dim != null && dim.length() > 3) {
                     String d= dim.substring(3);
                     if(GA_DATE.equals(dim)) {
                         SourceColumn sc = new SourceColumn(d,SourceColumn.LDM_TYPE_DATE, d);
                         sc.setFormat("yyyy-MM-dd");
                         s.addColumn(sc);
                     }
                     else {
                         SourceColumn sc = new SourceColumn(d,SourceColumn.LDM_TYPE_ATTRIBUTE, d);
                         s.addColumn(sc);
                     }
                 }
                 else {
                     l.debug("Invalid dimension name '" + dim + "'");
                     throw new InvalidArgumentException("Invalid dimension name '" + dim + "'");
                 }
             }
         }
         else {
             l.debug("Please specify Google Analytics dimensions separated by comma.");
             throw new InvalidArgumentException("Please specify Google Analytics dimensions separated by comma.");            
         }
         if(mtrs != null && mtrs.length() > 0) {
             String[] metrics = mtrs.split("\\|");
             for(String mtr : metrics) {
                 // remove the "ga:"
                 if(mtr != null && mtr.length() > 3) {
                     String m= mtr.substring(3);
                     SourceColumn sc = new SourceColumn(m,SourceColumn.LDM_TYPE_FACT, m);
                     s.addColumn(sc);
                 }
                 else {
                     l.debug("Invalid dimension name '" + mtr + "'");
                     throw new InvalidArgumentException("Invalid metric name '" + mtr + "'");
                 }
             }
         }
         else {
             l.debug("Please specify Google Analytics metrics separated by comma.");
             throw new InvalidArgumentException("Please specify Google Analytics metrics separated by comma.");
         }
         s.writeConfig(new File(configFileName));
         l.debug("Saved GA config template.");
     }
 
     /**
      * {@inheritDoc}
      */
     public void extract() throws IOException {
         try {
             AnalyticsService as = new AnalyticsService(APP_NAME);
             if(googleAnalyticsToken != null && googleAnalyticsToken.length() > 0) {
                as.setAuthSubToken(googleAnalyticsToken);
             } else if(googleAnalyticsUsername != null && googleAnalyticsUsername.length() > 0 &&
                   googleAnalyticsPassword != null && googleAnalyticsPassword.length() > 0) {
                 as.setUserCredentials(googleAnalyticsUsername, googleAnalyticsPassword, ClientLoginAccountType.GOOGLE);
             }
             else {
                 throw new InvalidCommandException("The LoadGoogleAnalytics commend requires either GA token or " +
                     "username and password!");
             }
             File dataFile = FileUtil.getTempFile();
             GaQuery gaq = getGoogleAnalyticsQuery();
             gaq.setMaxResults(5000);
             int cnt = 1;
 
             CSVWriter cw = FileUtil.createUtf8CsvWriter(dataFile);
             
             for(int startIndex = 1; cnt > 0; startIndex += cnt + 1) {
                 gaq.setStartIndex(startIndex);
                 DataFeed feed = as.getFeed(gaq.getUrl(), DataFeed.class);
                 l.debug("Retrieving GA data from index="+startIndex);
                 cnt = FeedDumper.dump(cw, feed);
                 l.debug("Retrieved "+cnt+" entries.");
             }
             cw.flush();
             cw.close();
             getConnectorBackend().extract(dataFile,false);
             FileUtil.recursiveDelete(dataFile);
         }
         catch (AuthenticationException e) {
             throw new InternalErrorException(e);
         } catch (ServiceException e) {
             throw new InternalErrorException(e);
         }
     }
 
     /**
      * Google Analytics username getter
      * @return Google Analytics username
      */
     public String getGoogleAnalyticsUsername() {
         return googleAnalyticsUsername;
     }
 
     /**
      * Google Analytics username setter
      * @param googleAnalyticsUsername Google Analytics username
      */
     public void setGoogleAnalyticsUsername(String googleAnalyticsUsername) {
         this.googleAnalyticsUsername = googleAnalyticsUsername;
     }
 
     /**
      * Google Analytics password getter
      * @return Google Analytics password
      */
     public String getGoogleAnalyticsPassword() {
         return googleAnalyticsPassword;
     }
 
     /**
      * Google Analytics password setter
      * @param googleAnalyticsPassword Google Analytics password
      */
     public void setGoogleAnalyticsPassword(String googleAnalyticsPassword) {
         this.googleAnalyticsPassword = googleAnalyticsPassword;
     }
 
     /**
      * Google Analytics query getter
      * @return Google Analytics query
      */
     public GaQuery getGoogleAnalyticsQuery() {
         return googleAnalyticsQuery;
     }
 
     /**
      * Google Analytics query setter
      * @param googleAnalyticsQuery Google Analytics query
      */
     public void setGoogleAnalyticsQuery(GaQuery googleAnalyticsQuery) {
         this.googleAnalyticsQuery = googleAnalyticsQuery;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         l.debug("Processing command "+c.getCommand());
         try {
             if(c.match("GenerateGoogleAnalyticsConfig")) {
                 generateGAConfig(c, cli, ctx);
             }
             else if(c.match("LoadGoogleAnalytics")) {
                 loadGA(c, cli, ctx);
             }
             else {
                 l.debug("No match passing the command "+c.getCommand()+" further.");
                 return super.processCommand(c, cli, ctx);
             }
         }
         catch (IOException e) {
             throw new ProcessingException(e);
         }
         l.debug("Processed command "+c.getCommand());
         return true;
     }
 
     /**
      * Loads new GA data command processor
      * @param c command
      * @param p command line arguments
      * @param ctx current processing context
      * @throws IOException in case of IO issues
      */
     private void loadGA(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         GaQuery gq;
         try {
             gq = new GaQuery();
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(e.getMessage());
         }
         String configFile = c.getParamMandatory("configFile");
         String usr = c.getParam("username");
         String psw = c.getParam("password");
         String token = c.getParam("token");
         String id = c.getParamMandatory("profileId");
         File conf = FileUtil.getFile(configFile);
         initSchema(conf.getAbsolutePath());
         gq.setIds(id);
         if(token != null && token.length() > 0) {
             setGoogleAnalyticsToken(token);
         } else if(googleAnalyticsUsername != null && googleAnalyticsUsername.length() > 0 && 
                   googleAnalyticsPassword != null && googleAnalyticsPassword.length() > 0) {
             setGoogleAnalyticsUsername(usr);
             setGoogleAnalyticsPassword(psw);
         }
         else {
             throw new InvalidCommandException("The LoadGoogleAnalytics commend requires either GA token or " +
                     "username and password!");            
         }
         setGoogleAnalyticsQuery(gq);
         gq.setDimensions(c.getParamMandatory("dimensions").replace("|",","));
         gq.setMetrics(c.getParamMandatory("metrics").replace("|",","));
         gq.setStartDate(c.getParamMandatory("startDate"));
         gq.setEndDate(c.getParamMandatory("endDate"));
         if(c.checkParam("filters"))
             gq.setFilters(c.getParam("filters"));
         // sets the current connector
         ctx.setConnector(this);
         setProjectId(ctx);
         l.info("Google Analytics Connector successfully loaded.");
     }
 
     /**
      * Generate GA config command processor
      * @param c command
      * @param p command line arguments
      * @param ctx current processing context
      * @throws IOException in case of IO issues
      */
     private void generateGAConfig(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String configFile = c.getParamMandatory("configFile");
         String name = c.getParamMandatory("name");
         String dimensions = c.getParamMandatory("dimensions");
         String metrics = c.getParamMandatory("metrics");
         GaQuery gq;
         try {
             gq = new GaQuery();
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(e.getMessage());
         }
         gq.setDimensions(dimensions);
         gq.setMetrics(metrics);
         GaConnector.saveConfigTemplate(name, configFile, gq);
         l.info("Google Analytics Connector configuration successfully generated. See config file: "+configFile);
     }
 
     public String getGoogleAnalyticsToken() {
         return googleAnalyticsToken;
     }
 
     public void setGoogleAnalyticsToken(String googleAnalyticsToken) {
         this.googleAnalyticsToken = googleAnalyticsToken;
     }
 }
