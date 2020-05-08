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
 package controllers;
 
 import static utils.RestUtils.OK_STATUS;
 
 import akka.util.Duration;
 import models.ServerNode;
 import models.Widget;
 
 import org.apache.commons.lang.NumberUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.Play;
 import play.Routes;
 import play.cache.Cache;
 import play.i18n.Messages;
 import play.libs.Akka;
 import play.libs.F;
 import play.libs.Json;
 import play.libs.WS;
 import play.mvc.Controller;
 import play.mvc.Result;
 import server.ApplicationContext;
 import server.HeaderMessage;
 import server.exceptions.ServerException;
 import beans.events.Events;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.config.ServerConfig;
 import com.avaje.ebean.config.dbplatform.MySqlPlatform;
 import com.avaje.ebeaninternal.api.SpiEbeanServer;
 import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
 import utils.StringUtils;
 import utils.Utils;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Widget controller with the main functions like start(), stop(), getWidgetStatus().
  * 
  * @author Igor Goldenberg
  */
 public class Application extends Controller
 {
 
     private static Logger logger = LoggerFactory.getLogger(Application.class);
     // guy - todo - apiKey should be an encoded string that contains the userId and widgetId.
     //              we should be able to decode it, verify user's ownership on the widget and go from there.
    **
      * Start does 2 things:
      *  - gets a server node
      *           - if remote bootstrap we need to bootstrap one from scratch
      *           - otherwise we take one from the pool.
      *
      *  - installs the widget recipe
     * @param apiKey
     * @param hpcsKey
     * @param hpcsSecretKey
     * @param userId
     * @return
      */
 	public static Result start( String apiKey, String project, String key, String secretKey, String userId )
 	{
 		try
 		{
 			logger.info("starting widget with [apiKey, key, secretKey] = [{},{},{}]", new Object[]{apiKey, key, secretKey} );
  			Widget widget = Widget.getWidget( apiKey );
             ServerNode serverNode = null;
            	if ( widget == null || !widget.isEnabled()) {
                 	new HeaderMessage().setError( Messages.get("widget.disabled.by.administrator") ).apply( response().getHeaders() );
 	                return badRequest(  );
             }
 
 
             if ( !StringUtils.isEmptyOrSpaces( widget.getLoginVerificationUrl() ) ) {
                 try {
                     F.Promise<WS.Response> post = WS.url( widget.getLoginVerificationUrl().replace( "$userId", userId ) ).post( "content" );
                     WS.Response response = post.get( 5L, TimeUnit.SECONDS );
                     if ( response.getStatus() != 200 ) {
                         return badRequest( "userId not verified : " + response.toString() );
                     }
                 } catch ( Exception e ) {
                     logger.error( "error while validating userId [{}] on url [{}]", userId, widget.getLoginVerificationUrl() );
                 }
             }
 
 
              ApplicationContext.get().getEventMonitor().eventFired( new Events.PlayWidget( request().remoteAddress(), widget ) );
 
             //TODO[adaml]: add proper input validation response
             if ( !StringUtils.isEmpty( project ) && !StringUtils.isEmpty( key ) && !StringUtils.isEmpty( secretKey ) ){
                 if ( !isValidInput( key, secretKey ) ) {
                     new HeaderMessage().setError(Messages.get("invalid.hpcs.credentials")).apply(response().getHeaders());
                     return badRequest();
                 }
 
                 serverNode = new ServerNode();
                 serverNode.setUserName( key );
                 serverNode.setRemote(true);
                 serverNode.setApiKey( secretKey );
                 serverNode.save();
             }else{
                 serverNode = ApplicationContext.get().getServerPool().get(widget.getLifeExpectancy());
                 if (serverNode == null) {
                     // if the user clicked another play in the last 30 seconds, we allow ourselves to tell them to wait..
                     Long timeLeft = (Long) Cache.get(Controller.request().remoteAddress());
                     if (timeLeft != null) {
                         throw new ServerException(Messages.get("please.wait.x.sec", (timeLeft - System.currentTimeMillis()) / 1000));
                     }
                     ApplicationContext.get().getMailSender().sendPoolIsEmptyMail( ApplicationContext.get().getServerPool().getStats().toString() );
                     throw new ServerException(Messages.get("no.available.servers"));
                 }
             }
 
             // run the "bootstrap" and "deploy" in another thread.
             final ServerNode finalServerNode = serverNode;
             final Widget finalWidget = widget;
             final String remoteAddress = request().remoteAddress();
             Akka.system().scheduler().scheduleOnce(
                     Duration.create(0, TimeUnit.SECONDS),
                     new Runnable() {
                         @Override
                         public void run() {
                             if (finalServerNode.isRemote()) {
                                 logger.info("bootstrapping remote cloud");
                                 ApplicationContext.get().getServerBootstrapper().bootstrapCloud(finalServerNode);
                             }
                             logger.info("installing widget on cloud");
                             ApplicationContext.get().getWidgetServer().deploy(finalWidget, finalServerNode, remoteAddress );
                         }
                     });
 
             return statusToResult( new Widget.Status().setInstanceId(serverNode.getId().toString()).setRemote(serverNode.isRemote()) );
 		}catch(ServerException ex)
 		{
             return exceptionToStatus( ex );
 		}
 	}
 
     private static Result exceptionToStatus( Exception e ){
            Widget.Status status = new Widget.Status();
            status.setState(Widget.Status.State.STOPPED);
            status.setMessage(e.getMessage());
            return statusToResult(status);
        }
 
     public static Result downloadPemFile( String instanceId ){
         ServerNode serverNode = ServerNode.find.byId( Long.parseLong( instanceId ) );
         if ( serverNode != null && !StringUtils.isEmpty(serverNode.getPrivateKey()) ){
             response().setHeader("Content-Disposition", String.format("attachment; filename=privateKey_%s.pem", instanceId));
             return ok ( serverNode.getPrivateKey() );
         }
         return badRequest("instance stopped");
     }
 
     private static Result statusToResult( Widget.Status status ){
         Map<String,Object> result = new HashMap<String, Object>();
         result.put("status", status );
         logger.info("statusToResult > result: [{}]", result);
         return ok( Json.toJson( result ));
     }
 
 	private static boolean isValidInput(String hpcsKey, String hpcsSecretKey) {
 		return !StringUtils.isEmpty(hpcsKey) && !StringUtils.isEmpty(hpcsSecretKey)
 				&& hpcsKey.contains(":") && !hpcsKey.startsWith(":") && !hpcsKey.endsWith(":");
 	}
 
 	public static Result stop( String apiKey, String instanceId )
 	{
 		ServerNode serverNode = ServerNode.find.byId(Long.parseLong(instanceId));
         if ( serverNode != null ){
             Utils.deleteCachedOutput(serverNode);
         }
 		if (serverNode.isRemote()) {
 			return ok(); // lets assume
 		}else {
 			Widget widget = Widget.getWidget( apiKey );
 			if ( widget != null ){
 				ApplicationContext.get().getEventMonitor().eventFired( new Events.StopWidget( request().remoteAddress(), widget ) );
 			}
 			if ( instanceId != null ){
 				ApplicationContext.get().getWidgetServer().undeploy(instanceId);
 			}
 
 			return ok(OK_STATUS).as("application/json");
 		}
 	}
 
 
 	public static Result getWidgetStatus( String apiKey, String instanceId )
 	{
 		try
 		{
             if (!NumberUtils.isNumber( instanceId )){
                 return badRequest();
             }
             ServerNode serverNode = ServerNode.find.byId( Long.parseLong(instanceId) );
 
 			Widget.Status wstatus = ApplicationContext.get().getWidgetServer().getWidgetStatus(serverNode);
 			return statusToResult(wstatus);
 		}catch(ServerException ex)
 		{
 			return exceptionToStatus( ex );
 		}
 	}
 
     public static Result generateDDL(){
         if ( Play.isDev() ) {
             EbeanServer defaultServer = Ebean.getServer( "default" );
 
             ServerConfig config = new ServerConfig();
             config.setDebugSql( true );
 
             DdlGenerator ddlGenerator = new DdlGenerator( ( SpiEbeanServer ) defaultServer, new MySqlPlatform(), config );
             String createDdl = ddlGenerator.generateCreateDdl();
             String dropDdl = ddlGenerator.generateDropDdl();
             return ok( createDdl );
         }else{
             return forbidden(  );
         }
     }
 
     public static Result javascriptRoutes()
     {
         response().setContentType( "text/javascript" );
         return ok(
                 Routes.javascriptRouter( "jsRoutes",
 
                         // Routes for Projects
                         routes.javascript.WidgetAdmin.getAllWidgets(),
                         routes.javascript.WidgetAdmin.summary(),
                         routes.javascript.WidgetAdmin.postWidget(),
                         routes.javascript.WidgetAdmin.checkPasswordStrength(),
                         routes.javascript.WidgetAdmin.postChangePassword(),
                         routes.javascript.WidgetAdmin.getPasswordMatch(),
                         routes.javascript.WidgetAdmin.postWidgetDescription(),
                         routes.javascript.WidgetAdmin.deleteWidget(),
                         routes.javascript.WidgetAdmin.postRequireLogin(),
                         routes.javascript.Application.downloadPemFile(),
                         routes.javascript.WidgetAdmin.regenerateWidgetApiKey(),
                         routes.javascript.WidgetAdmin.enableWidget(),
                         routes.javascript.WidgetAdmin.disableWidget(),
                         routes.javascript.DemosController.listWidgetForDemoUser()
 
                 )
         );
 
     }
 }
