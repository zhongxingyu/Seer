 package haed.notification;
 
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.CacheControl;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.log4j.Logger;
 import org.atmosphere.cpr.AtmosphereResourceEvent;
 import org.atmosphere.cpr.AtmosphereResourceEventListener;
 import org.atmosphere.jersey.SuspendResponse;
 import org.atmosphere.jersey.SuspendResponse.SuspendResponseBuilder;
 
 @Path("/")
 public class NotificationAPI {
 	
 	private static final Logger logger = Logger.getLogger(NotificationAPI.class);
 	
 	public static final CacheControl cacheControl_cacheNever = new CacheControl();
 	
 	static {
 		cacheControl_cacheNever.setMaxAge(0);
 		cacheControl_cacheNever.setNoCache(true);
 	}
 	
 	@GET
 	@Path("/createChannel")
 	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
 	public Response createChannel() {
 		final String channelID = UUID.randomUUID().toString();
 		return Response.ok(channelID).cacheControl(cacheControl_cacheNever).build();
 	}
 	
 	@GET
 	@Path("/openChannel")
 	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
 	public SuspendResponse<String> openChannel(
 				final @QueryParam("channelID") String channelID, 
 				final @QueryParam("outputComments") @DefaultValue("false") Boolean outputComments)
 			throws Exception {
 		
 		final NotificationMgr notificationMgr = NotificationMgr.getInstance();
 		
 		if (channelID == null || channelID.isEmpty())
 			throw new Exception("channelID must not be null or empty");
 		
 		boolean sendPing = false;
 		
 		NotificationBroadcaster broadCaster = notificationMgr.getBroadcaster(channelID, false);
 		if (broadCaster == null) {
 			
 			// TODO @haed [haed]: maybe we should throw an exception, only createChannel should create a new channel (with some permission checks)
 			//  => open should only open a connection to a already existing channel
 			
 			broadCaster = notificationMgr.getBroadcaster(channelID, true);
 			
 			// NOTE: ping-notification and registration should be done only on initial call, 
 			// otherwise we got an endless recursion on long-polling
 			
 			// also register for ping and ping initial
 			sendPing = true;
 			NotificationMgr.getInstance().subscribe(channelID, createPingNotificationType(channelID));
 			
 		} else {
 			
			// check if channel is already connected (atmosphere bug?)
 			if (broadCaster.getAtmosphereResources().isEmpty() == false) {
 				
				// TODO @haed [haed]: inspect atmosphere client api for "synchronized" re-connect
 				logger.warn("channel already connected, channelID: " + channelID);
 				
 				// avoid duplicate channel with exception
				throw new WebApplicationException(Response.noContent().build());
 			}
 		}
 		
 		final NotificationBroadcaster _broadCaster = broadCaster;
 		final SuspendResponseBuilder<String> suspendResponseBuilder = new SuspendResponse.SuspendResponseBuilder<String>()
 			.period(1, TimeUnit.MINUTES)
 		  .addListener(new AtmosphereResourceEventListener() {
 				
 				public void onSuspend(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
 					
 					if (logger.isDebugEnabled())
 						logger.debug("channel with id '" + channelID + "' suspended");
 					
 //					// touch channel session to (keep alive)
 //					HttpSessionMgr.getInstance().getSession(channelID, false);
 					
 					_broadCaster.setResumed(false);
 					
 //					// process queued notifications: re-check if broadcaster was absent (because of all resources were disconnected)
 //					notificationMgr.processQueue(channelID, _broadCaster);
 				}
 				
 				public void onResume(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
 					
 					if (logger.isDebugEnabled())
 						logger.debug("channel with id '" + channelID + "' resumed");
 					
 					_broadCaster.setResumed(true);
 				}
 				
 				public void onDisconnect(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
 					
 					// NOTE: disconnect also happens in "connected"-environments, e.g. on suspend timeout (after 5 minutes)
 					
 					if (logger.isDebugEnabled())
 						logger.debug("channel with id '" + channelID + "' disconnected");
 					
 					// TODO [haed]: this is uncommented for testing, maybe a "buggy" destroy kills all subscriptions
 					// 		-> if this works, we do not need a scheduled destroy on suspend, we can use onDisconnect
 					//			 (but should should also wait until destroy channel, because of reconnects on shortly network failures)
 //					// destroy channel
 //					HttpSessionMgr.getInstance().destroySession(channelID);
 				}
 				
 				public void onBroadcast(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
 				}
 
 				public void onThrowable(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
 					logger.fatal("error in atmosphere", event.throwable());
         }
 			})
 			.broadcaster(broadCaster);
 		
 		// configure and disable caching
 		suspendResponseBuilder
 			.outputComments(outputComments)
 			.cacheControl(cacheControl_cacheNever);
 		
 		if (sendPing)
 			notificationMgr.sendNotification(createPingNotificationType(channelID), "ping");
 		
 	  return suspendResponseBuilder.build();
 	}
 	
 	@GET
 	@Path("/ping")
 	public Response ping(
 				final @QueryParam("channelID") String channelID)
 			throws Exception {
 		NotificationMgr.getInstance().sendNotification(createPingNotificationType(channelID), "ping");
 		return Response.ok().build();
 	}
 	
 	public static String createPingNotificationType(final String channelID) {
 		return "haed.notification.ping." + channelID;
 	}
 }
