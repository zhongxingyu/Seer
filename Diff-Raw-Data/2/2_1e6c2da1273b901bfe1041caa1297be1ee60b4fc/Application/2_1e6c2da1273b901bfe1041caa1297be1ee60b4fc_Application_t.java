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
 import static utils.RestUtils.resultAsJson;
 import static utils.RestUtils.resultErrorAsJson;
 import models.ServerNode;
 import models.Widget;
 import models.WidgetInstance;
 
 import org.apache.commons.lang.NumberUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.Play;
 import play.Routes;
 import play.i18n.Messages;
 import play.libs.Json;
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
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Widget controller with the main functions like start(), stop(), getWidgetStatus().
  * 
  * @author Igor Goldenberg
  */
 public class Application extends Controller
 {
     private static Logger logger = LoggerFactory.getLogger( Application.class );
     // guy - todo - apiKey should be an encoded string that contains the userId and widgetId.
     //              we should be able to decode it, verify user's ownership on the widget and go from there.
 	public static Result start( String apiKey, String hpcsKey, String hpcsSecretKey )
 	{
 		try
 		{
 			logger.info("starting widget with [apiKey, hpcsKey, hpcsSecretKey] = [{},{},{}]", new Object[]{apiKey, hpcsKey, hpcsSecretKey} );
  			Widget widget = Widget.getWidget( apiKey );
             ServerNode serverNode = null;
            	if ( widget == null || !widget.isEnabled()) {
                 	new HeaderMessage().setError( Messages.get("widget.disabled.by.administrator") ).apply( response().getHeaders() );
 	                return badRequest(  );
             }
 			ApplicationContext.get().getEventMonitor().eventFired( new Events.PlayWidget( request().remoteAddress(), widget ));
 			WidgetInstance wi = null;
 			//TODO[adaml]: add proper input validation response
 			if ( !StringUtils.isEmpty( hpcsKey ) && !StringUtils.isEmpty( hpcsSecretKey ) ){
                 if ( !isValidInput(hpcsKey, hpcsSecretKey) ) {
                     new HeaderMessage().setError("invalid hpcs credentials").apply(response().getHeaders());
                     return badRequest();
                 }
 
                 serverNode = new ServerNode();
                 serverNode.setUserName( hpcsKey );
                 serverNode.setRemote(true);
                 serverNode.setApiKey( hpcsSecretKey );
                 serverNode.save();
                 ApplicationContext.get().getServerBootstrapper().bootstrapCloud( serverNode );
 				ApplicationContext.get().getWidgetServer().deploy(widget, serverNode);
 				return ok();
 
             }else{
 				wi = ApplicationContext.get().getWidgetServer().deploy(apiKey);
                 serverNode = wi.getServerNode();
             }
 
             Map<String,Object> result = new HashMap<String,Object>();
             result.put("status", new Widget.Status().setInstanceId(serverNode.getId().toString()));
 			return ok( Json.toJson(result));
 		}catch(ServerException ex)
 		{
 			return resultErrorAsJson(ex.getMessage());
 		}
 	}
 
 	private static boolean isValidInput(String hpcsKey, String hpcsSecretKey) {
 		return !StringUtils.isEmpty(hpcsKey) && !StringUtils.isEmpty(hpcsSecretKey)
 				&& hpcsKey.contains(":") && !hpcsKey.startsWith(":") && !hpcsKey.endsWith(":");
 	}
 	
 	public static Result stop( String apiKey, String instanceId )
 	{
 		
 		ServerNode serverNode = ServerNode.find.byId(Long.parseLong(instanceId));
 		if (serverNode.isRemote()) {
 			return notFound();
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
             if ( serverNode == null ){
                throw new ServerException( Messages.get("server.was.terminated") );
             }
 			Widget.Status wstatus = ApplicationContext.get().getWidgetServer().getWidgetStatus(serverNode);
 			return resultAsJson( wstatus );
 		}catch(ServerException ex)
 		{
 			return resultErrorAsJson(ex.getMessage());
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
                         routes.javascript.WidgetAdmin.checkPasswordStrength(),
                         routes.javascript.WidgetAdmin.postChangePassword(),
                         routes.javascript.WidgetAdmin.getPasswordMatch(),
                         routes.javascript.WidgetAdmin.deleteWidget()
 
                 )
         );
 
     }
 }
