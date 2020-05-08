 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package beans;
 
 import static server.Config.WIDGET_STOP_TIMEOUT;
 
 import java.io.*;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Pattern;
 
 import beans.config.Conf;
 import controllers.WidgetAdmin;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.cache.Cache;
 import play.i18n.Messages;
 import play.mvc.Controller;
 
 import models.ServerNode;
 import models.Widget;
 import models.Widget.Status;
 import models.WidgetInstance;
 import server.*;
 import server.exceptions.ServerException;
 import utils.CollectionUtils;
 import utils.Utils;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 /**
  * This class provides ability to deploy/undeploy new widget by apiKey.
  * Before that the user must create an account by WidgetAdmin and register a new widget.
  * 
  * @author Igor Goldenberg
  * @see ServerPoolImpl
  * @see WidgetAdmin
  */
 public class WidgetServerImpl implements WidgetServer
 {
     private static Logger logger = LoggerFactory.getLogger( WidgetServerImpl.class );
     @Inject
     private ServerPool serverPool;
 
     @Inject
     private MailSender mailSender;
 
     @Inject
     private Conf conf;
 
     @Inject
     private DeployManager deployManager;
 
     private static Map<Recipe.Type, Pattern> installationFinishedRegexMap = null;
 
     static {
         for ( Recipe.Type type  : Recipe.Type.values() ) {
             String pattern = type + " .* installed successfully";
             installationFinishedRegexMap.put(type, Pattern.compile( pattern, Pattern.CASE_INSENSITIVE) );
         }
     }
 
     private List<String> filterOutputLines = new LinkedList<String>(  );
     private List<String> filterOutputStrings = new LinkedList<String>(  );
 
     @PostConstruct
     public void init(){
         Utils.addAllTrimmed( filterOutputLines,  StringUtils.split( conf.cloudify.removeOutputLines, "|" ));
         Utils.addAllTrimmed( filterOutputStrings,  StringUtils.split( conf.cloudify.removeOutputString, "|" ));
     }
 	/**
 	 * Deploy a widget instance.
 	 * @param apiKey 
 	 */
 	public WidgetInstance deploy( String apiKey )
 	{
 		// don't allow for 30 seconds to start the widget again
 		Long timeLeft = (Long)Cache.get(Controller.request().remoteAddress()); // todo : move this block of code to controller. too play oriented.
 		if ( timeLeft != null )
         {
 			throw new ServerException( Messages.get( "please.wait.x.sec", (timeLeft - System.currentTimeMillis()) / 1000) );
         }
 
 		Widget widget = Widget.getWidget( apiKey ); // todo : get user to here somehow and use that signature.
 		if ( !widget.isEnabled() )
         {
 			throw new ServerException( Messages.get( "widget.disabled.by.administrator" ) );
         }
 		
 
 		ServerNode server = serverPool.get( widget.getLifeExpectancy() );
 		if ( server == null ){
 			mailSender.sendPoolIsEmptyMail();
 			throw new ServerException(Messages.get("no.available.servers"));
         }
 		return deploy(widget, server);
 	}
 	
 	
 	public WidgetInstance deploy( Widget widget, ServerNode server )
 	{
 		File unzippedDir = Utils.downloadAndUnzip( widget.getRecipeURL(), widget.getApiKey() );
         File recipeDir = unzippedDir;
         if ( widget.getRecipeRootPath() != null  ){
             recipeDir = new File( unzippedDir, widget.getRecipeRootPath() );
         }
         logger.info("Deploying an instance for recipe at : [{}] ", recipeDir );
 		widget.countLaunch();
 		deployManager.fork(server, recipeDir);
 		return widget.addWidgetInstance( server, recipeDir );
 	}
 	
 	public void undeploy( String instanceId )
 	{
 		// keep the user for 30 seconds by IP, to avoid immediate widget start after stop
 		Cache.set(Controller.request().remoteAddress(), new Long(System.currentTimeMillis() + WIDGET_STOP_TIMEOUT*1000), WIDGET_STOP_TIMEOUT ); 
 		
 		serverPool.destroy(instanceId);
 	}
 
 
     @Override
     public Status getWidgetStatus(ServerNode server) {
         Status result = new Status();
         List<String> output = new LinkedList<String>();
         result.setOutput(output);
 
         if (server == null) {
             result.setState(Status.State.STOPPED);
             output.add(Messages.get("server.was.terminated"));
             return result;
         }
 
         WidgetInstance widgetInstance = WidgetInstance.findByInstanceId(server.getNodeId());
         if (widgetInstance != null ){
             widgetInstance.getWidget();
             widgetInstance.getLink();
             String last = CollectionUtils.last(output);
             Pattern pattern = installationFinishedRegexMap.get(widgetInstance.getRecipeType());
 
             if (pattern.matcher(last).matches()){
                 result.setLink( widgetInstance.getLink() );
             }
         }
 
         result.setState(Status.State.RUNNING);
         if (!StringUtils.isEmpty(server.getPublicIP())) {
             result.setPublicIp(server.getPublicIP());
         }
 
         String cachedOutput = Utils.getCachedOutput(server.getNodeId());
 
         output.addAll(Utils.formatOutput(cachedOutput, server.getPrivateIP() + "]", filterOutputLines, filterOutputStrings));
 
         // server is remote we don't count time
         if (!server.isRemote() && server.getExpirationTime() != null) {
             long elapsedTime = server.getExpirationTime() - System.currentTimeMillis();
             result.setTimeleft((int) TimeUnit.MILLISECONDS.toMinutes(elapsedTime));
         }
         return result;
     }
 
     public void setServerPool(ServerPool serverPool) {
         this.serverPool = serverPool;
     }
 
     public void setDeployManager(DeployManager deployManager) {
         this.deployManager = deployManager;
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 }
