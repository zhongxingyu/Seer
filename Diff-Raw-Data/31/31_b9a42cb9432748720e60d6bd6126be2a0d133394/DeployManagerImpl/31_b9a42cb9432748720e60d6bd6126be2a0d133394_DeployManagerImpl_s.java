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
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.inject.Inject;
 
 import beans.cloudify.CloudifyRestClient;
 import models.ServerNode;
 
 import models.ServerNodeEvent;
 import models.Widget;
 import models.WidgetInstance;
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecuteResultHandler;
 import org.apache.commons.exec.ExecuteException;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import server.ApplicationContext;
 import server.DeployManager;
 import server.ProcExecutor;
 import server.exceptions.ServerException;
 import beans.api.ExecutorFactory;
 import beans.config.Conf;
 import utils.Utils;
 
 /**
  * This class deploys a recipe file vi cloudify non-interactive CLI. 
  * Each deploy forks a CLI process and stream the output.
  * 
  * @author Igor Goldenberg
  */
 public class DeployManagerImpl implements DeployManager
 {
 
     private static Logger logger = LoggerFactory.getLogger( DeployManagerImpl.class );
 
     @Inject
     private Conf conf;
     
     @Inject 
     private ExecutorFactory executorFactory;
 
     @Inject
     private CloudifyRestClient cloudifyRestClient;
 
 
     @Override
     public WidgetInstance uninstall( ServerNode serverNode ){
         WidgetInstance widgetInstance = serverNode.getWidgetInstance();
         String installName = widgetInstance.getInstallName();
         // TODO : maybe we should verify it is installed using the rest client?
         File script = widgetInstance.getRecipeType() == Recipe.Type.APPLICATION ? conf.cloudify.uninstallApplicationScript : conf.cloudify.uninstallServiceScript;
         CommandLine cmdLine = new CommandLine( script );
         cmdLine.addArgument( serverNode.getPublicIP() );
         cmdLine.addArgument( installName );
         logger.info( "executing command [{}]", cmdLine );
         execute(  cmdLine, serverNode );
         return widgetInstance;
     }
 
 
     /**
      *
      *
      * Decide if this is an application or a service
      *
      * if Application
      *          Use a rest API call to list all the
      *          http://IP:8100/service/applications
      *
      *if service
      *          Use a rest API to list all services on default
      *          http://IP:8100/service/applications/default/services
      *
      * view the results and decide if it is installed
      *
      * @param server
      * @param widget
      * @return
      */
     private boolean alreadyInstalled( ServerNode server, Widget widget, Recipe.Type recipeType ){
 
         logger.info( "checking if [{}] named [{}] is installed on [{}]", new Object[]{recipeType, widget.toInstallName(), server.getPublicIP() });
         try{
         switch ( recipeType ) {
 
             case APPLICATION:
             {
                 return cloudifyRestClient.listApplications( server.getPublicIP() ).response.containsKey( widget.toInstallName() );
             }
             case SERVICE:
             {
                 // check if application "default" is installed at all, and if so - check for services on it.
                 if ( cloudifyRestClient.listApplications( server.getPublicIP() ).response.containsKey( "default" )){
                     return cloudifyRestClient.listServices( server.getPublicIP(), "default" ).response.contains( widget.toInstallName() );
                 }else{
                     logger.info( "figured that application 'default' is not installed, and hence the service is not installed" );
                     return false;
                 }
             }
         }
         }catch(Exception e){
             logger.error( "unable to decide if [{}] named [{}] is already installed on [{}]", new Object[]{recipeType, widget.toInstallName(), server} );
         }
 
         return false; // todo
     }
 
     @Override
     public String getServicePublicIp( WidgetInstance widgetInstance )
     {
         try {
             ServerNode server = widgetInstance.getServerNode();
             Widget widget = widgetInstance.getWidget();
             Recipe.Type recipeType = widgetInstance.getRecipeType();
             if ( recipeType == Recipe.Type.SERVICE ) {
                String serviceIp = cloudifyRestClient.getPublicIp( server.getPublicIP(), "default", widget.toInstallName() ).cloudPublicIp;
                logger.info( "service IP is [{}]", serviceIp );
             } else if ( !StringUtils.isEmpty( widget.getConsoleUrlService() ) ) { // this is an application and we need to get ip for specific service
                 return cloudifyRestClient.getPublicIp( server.getPublicIP(), widget.toInstallName(), widget.getConsoleUrlService() ).cloudPublicIp;
             }
         } catch ( Exception e ) {
             logger.error( "unable to resolve public ip for widget instance [{}]", widgetInstance, e );
         }
         return null;
     }
 
 
     // todo - replace widget with widgetInstance - since we have server 1to1 widgetInstance, we can simply transfer server here.
     // todo - Widget should only be a template. We are installing a single instance.
 	public WidgetInstance fork(ServerNode server, Widget widget)
 	{
         File unzippedDir = Utils.downloadAndUnzip( widget.getRecipeURL(), widget.getApiKey() );
         File recipeDir = unzippedDir;
         if ( widget.getRecipeRootPath() != null ) {
             recipeDir = new File( unzippedDir, widget.getRecipeRootPath() );
         }
         logger.info( "Deploying an instance for recipe at : [{}] ", recipeDir );
 
         Recipe.Type recipeType = new Recipe( recipeDir ).getRecipeType();
 
         if ( alreadyInstalled( server, widget, recipeType ) ){
             logger.info( "[{}] [{}] is already installed", recipeType, widget.toInstallName()  );
             WidgetInstance widgetInstance = widget.addWidgetInstance( server, recipeDir );
             String publicIp = getServicePublicIp( widgetInstance );
             if ( !StringUtils.isEmpty( publicIp )){
                 widgetInstance.setServicePublicIp( publicIp );
                 widgetInstance.save(  );
             }
             server.createEvent(null, ServerNodeEvent.Type.DONE ).save(  );
 
             return widgetInstance;
         }else{
             logger.info( "Deploying: [ServerIP={}] [recipe={}] [type={}]", new Object[]{server.getPublicIP(), recipeDir, recipeType.name()} );
             String recipePath = FilenameUtils.separatorsToSystem( recipeDir.getPath() );
 
             CommandLine cmdLine = new CommandLine( conf.cloudify.deployScript );
             cmdLine.addArgument( server.getPublicIP() );
             cmdLine.addArgument( recipePath.replaceAll( "\\\\", "/" ) ); // support for windows.
             cmdLine.addArgument( recipeType.commandParam );
             cmdLine.addArgument( widget.toInstallName() );
 
             execute( cmdLine, server );
             return widget.addWidgetInstance( server, recipeDir );
         }
     }
 
     private void execute( CommandLine cmdLine, ServerNode server )
     {
         try {
             DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
             ProcExecutor executor = executorFactory.getDeployExecutor( server );
             logger.info( "executing command [{}]", cmdLine );
             executor.execute( cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler );
             logger.info( "The process instanceId: {}", executor.getId() );
         } catch ( ExecuteException e ) {
             logger.error( "Failed to execute process. Exit value: " + e.getExitValue(), e );
 
             throw new ServerException( "Failed to execute process. Exit value: " + e.getExitValue(), e );
         } catch ( IOException e ) {
             logger.error( "Failed to execute process", e );
 
             throw new ServerException( "Failed to execute process.", e );
         }
     }
 
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 
     public void setCloudifyRestClient( CloudifyRestClient cloudifyRestClient )
     {
         this.cloudifyRestClient = cloudifyRestClient;
     }
 }
