 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane.server;
 
 import com.janrain.backplane.common.AuthException;
 import com.janrain.backplane.common.BackplaneServerException;
 import com.janrain.backplane.common.HmacHashUtils;
 import com.janrain.backplane.config.BackplaneConfig;
 import com.janrain.backplane.dao.DaoException;
import com.janrain.backplane.server.redisdao.BP1MessageDao;
import com.janrain.backplane.common.DateTimeUtils;
import com.janrain.commons.supersimpledb.SimpleDBException;
import com.janrain.util.ServletUtil;
import com.janrain.utils.AnalyticsLogger;
 import com.janrain.backplane.server1.dao.BP1DAOs;
 import com.janrain.backplane.server1.model.BusConfig1;
 import com.janrain.backplane.server1.model.BusConfig1Fields;
 import com.janrain.backplane.server1.model.BusUser;
 import com.janrain.backplane.server1.model.BusUserFields;
 import com.janrain.util.RandomUtils;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Histogram;
 import com.yammer.metrics.core.MetricName;
 import com.yammer.metrics.core.TimerContext;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Backplane API implementation.
  *
  * @author Johnny Bufu
  */
 @Controller
 @RequestMapping(value="/*")
 @SuppressWarnings({"UnusedDeclaration"})
 public class Backplane1Controller {
 
     // - PUBLIC
 
     @RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.HEAD })
     public ModelAndView greetings(HttpServletRequest request, HttpServletResponse response) {
         if (RequestMethod.HEAD.toString().equals(request.getMethod())) {
             response.setContentLength(0);
         }
         return new ModelAndView("welcome");
     }
 
     @RequestMapping(value = "/{version}/bus/{bus}", method = RequestMethod.GET)
     public @ResponseBody List<HashMap<String,Object>> getBusMessages(
             @PathVariable String version,
             @RequestHeader(value = "Authorization", required = false) String basicAuth,
             @PathVariable String bus,
             @RequestParam(value = "since", defaultValue = "") String since,
             @RequestParam(value = "sticky", required = false) String sticky )
             throws AuthException, SimpleDBException, BackplaneServerException, DaoException {
 
         final TimerContext context = getBusMessagesTime.time();
 
         try {
 
             checkAuth(basicAuth, bus, BusConfig1Fields.GETALL_USERS());
 
             List<BackplaneMessage> messages =
                com.janrain.backplane.server.redisdao.BP1DAOs.getMessageDao().getMessagesByBus(bus, since, sticky);
 
             List<HashMap<String,Object>> frames = new ArrayList<HashMap<String, Object>>();
             for (BackplaneMessage message : messages) {
                 frames.add(message.asFrame(version));
             }
             return frames;
 
         } finally {
             context.stop();
         }
 
     }
 
     @RequestMapping(value = "/{version}/bus/{bus}/channel/{channel}", method = RequestMethod.GET)
     public ResponseEntity<String> getChannel(
             HttpServletRequest request, HttpServletResponse response,
             @PathVariable String version,
             @PathVariable String bus,
             @PathVariable String channel,
             @RequestHeader(value = "Referer", required = false) String referer,
             @RequestParam(required = false) String callback,
             @RequestParam(value = "since", required = false) String since,
             @RequestParam(value = "sticky", required = false) String sticky)
             throws SimpleDBException, AuthException, BackplaneServerException {
 
         logger.debug("request started");
 
         try {
             boolean newChannel = NEW_CHANNEL_LAST_PATH.equals(channel);
             String resp;
             List<BackplaneMessage> messages = new ArrayList<BackplaneMessage>();
 
             if (newChannel) {
                 resp = newChannel();
                 aniLogNewChannel(request, referer, version, bus, resp.substring(1, resp.length()-1));
             } else {
                 messages = getChannelMessages(bus, channel, since, sticky);
                 resp = messagesToFrames(messages, version);
                 aniLogPollMessages(request, referer, version, bus, channel, messages);
             }
 
             return new ResponseEntity<String>(
                 resp,
                 new HttpHeaders() {{
                     add("Content-Type", "application/json");
                 }},
                 HttpStatus.OK);
 
         } finally {
             logger.debug("request ended");
         }
 
     }
 
     @RequestMapping(value = "/{version}/bus/{bus}/channel/{channel}", method = RequestMethod.POST)
     public @ResponseBody String postToChannel(
             HttpServletRequest request, HttpServletResponse response,
             @PathVariable String version,
             @RequestHeader(value = "Authorization", required = false) String basicAuth,
             @RequestBody List<Map<String,Object>> messages,
             @PathVariable String bus,
             @PathVariable String channel) throws AuthException, SimpleDBException, BackplaneServerException, DaoException {
 
         BusUser user = checkAuth(basicAuth, bus, BusConfig1Fields.POST_USERS());
 
         final TimerContext context = postMessagesTime.time();
 
         try {

            BP1MessageDao backplaneMessageDAO = com.janrain.backplane.server.redisdao.BP1DAOs.getMessageDao();

             //Block post if the caller has exceeded the message post limit
            if (backplaneMessageDAO.getMessageCount(bus, channel) >= BackplaneConfig.getDefaultMaxMessageLimit()) {
                 logger.warn("Channel " + bus + ":" + channel + " has reached the maximum of " +
                         BackplaneConfig.getDefaultMaxMessageLimit() + " messages");
                 throw new BackplaneServerException("Message limit exceeded for this channel");
             }
 
             BusConfig1 busConfig = BP1DAOs.busDao().get(bus).getOrElse(null);
 
             // For analytics.
             String channelId = "https://" + request.getServerName() + "/" + version + "/bus/" + bus + "/channel/" + channel;
             String clientId = user.id();
 
             for(Map<String,Object> messageData : messages) {
                BackplaneMessage message = new BackplaneMessage(bus, channel,
                         busConfig.retentionTimeSeconds(),
                         busConfig.retentionTimeStickySeconds(),
                         messageData);
                backplaneMessageDAO.persist(message);
                 aniLogNewMessage(version, bus, channelId, clientId);
             }
 
             return "";
 
         } finally {
             context.stop();
         }
     }
 
     /**
      * Handle auth errors
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final AuthException e, HttpServletResponse response) {
         logger.error("Backplane authentication error: " + e.getMessage(), BackplaneConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, e.getMessage());
         }};
     }
 
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final BackplaneServerException bse, HttpServletResponse response) {
         logger.error("Backplane server error: " + bse.getMessage(), BackplaneConfig.getDebugException(bse));
         response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, BackplaneConfig.isDebugMode() ? bse.getMessage() : "Service unavailable");
         }};
     }
 
     /**
      * Handle all other errors
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final Exception e, HttpServletRequest request, HttpServletResponse response) {
     	String path = request.getPathInfo();
         logger.error("Error handling backplane request for " + path + ": " + e.getMessage(), BackplaneConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, BackplaneConfig.isDebugMode() ? e.getMessage() : "Error processing request.");
         }};
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(Backplane1Controller.class);
 
     private static final String NEW_CHANNEL_LAST_PATH = "new";
     private static final String ERR_MSG_FIELD = "ERR_MSG";
     private static final int CHANNEL_NAME_LENGTH = 32;
 
     private final com.yammer.metrics.core.Timer getBusMessagesTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "get_bus_messages_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private final com.yammer.metrics.core.Timer getChannelMessagesTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "get_channel_messages_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private final com.yammer.metrics.core.Timer getNewChannelTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "get_new_channel_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private final com.yammer.metrics.core.Timer postMessagesTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "post_messages_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private final Histogram payLoadSizesOnGets = Metrics.newHistogram(new MetricName("v1", this.getClass().getName().replace(".","_"), "payload_sizes_gets"));
 
     @Inject
     private AnalyticsLogger anilogger;
 
     private BusUser checkAuth(String basicAuth, String bus, BusConfig1Fields.EnumVal permissionField) throws AuthException, BackplaneServerException, DaoException {
         // authN
         String userPass = null;
         if ( basicAuth == null || ! basicAuth.startsWith("Basic ") || basicAuth.length() < 7) {
             authError("Invalid Authorization header: " + basicAuth);
         } else {
             try {
                 userPass = new String(Base64.decodeBase64(basicAuth.substring(6).getBytes("utf-8")));
             } catch (UnsupportedEncodingException e) {
                 authError("Cannot check authentication, unsupported encoding: utf-8"); // shouldn't happen
             }
         }
 
         @SuppressWarnings({"ConstantConditions"})
         int delim = userPass.indexOf(":");
         if (delim == -1) {
             authError("Invalid Basic auth token: " + userPass);
         }
         String user = userPass.substring(0, delim);
         String pass = userPass.substring(delim + 1);
 
         BusUser userEntry = BP1DAOs.userDao().get(user).getOrElse(null);
 
         if (userEntry == null) {
             authError("User not found: " + user);
         } else if ( ! HmacHashUtils.checkHmacHash(pass, userEntry.get(BusUserFields.PWDHASH()).get()) ) {
             authError("Incorrect password for user " + user);
         }
 
         // authZ
         BusConfig1 busConfig = BP1DAOs.busDao().get(bus).getOrElse(null);
 
         if (busConfig == null) {
             authError("Bus configuration not found for " + bus);
         } else if (!busConfig.isAllowed(user, permissionField)) {
             authError("User " + user + " not among the uses in " + permissionField + " on bus " + bus);
         }
 
         return userEntry;
     }
 
     private void authError(String errMsg) throws AuthException {
         logger.error(errMsg);
         try {
             throw new AuthException("Access denied. " + (BackplaneConfig.isDebugMode() ? errMsg : ""));
         } catch (Exception e) {
             throw new AuthException("Access denied.");
         }
     }
 
     private String newChannel() {
     	final TimerContext context = getNewChannelTime.time();
         String newChannel = "\"" + RandomUtils.randomString(CHANNEL_NAME_LENGTH) +"\"";
         context.stop();
     	return newChannel;
     }
 
     private List<BackplaneMessage> getChannelMessages(final String bus, final String channel, final String since, final String sticky) throws SimpleDBException, BackplaneServerException {
 
         final TimerContext context = getChannelMessagesTime.time();
 
         try {
             return com.janrain.backplane.server.redisdao.BP1DAOs.getMessageDao().getMessagesByChannel(bus, channel, since, sticky);
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (BackplaneServerException bse) {
             throw bse;
         } catch (Exception e) {
             throw new BackplaneServerException(e.getMessage(), e);
         } finally {
             context.stop();
         }
     }
 
     private String messagesToFrames(List<BackplaneMessage> messages, final String version) throws BackplaneServerException {
 
         try {
             List<Map<String,Object>> frames = new ArrayList<Map<String, Object>>();
 
             for (BackplaneMessage message : messages) {
                 frames.add(message.asFrame(version));
             }
             ObjectMapper mapper = new ObjectMapper();
             try {
                 String payload = mapper.writeValueAsString(frames);
                 payLoadSizesOnGets.update(payload.length());
                 return payload;
             } catch (IOException e) {
                 String errMsg = "Error converting frames to JSON: " + e.getMessage();
                 logger.error(errMsg, BackplaneConfig.getDebugException(e));
                 throw new BackplaneServerException(errMsg, e);
             }
         } catch (BackplaneServerException bse) {
             throw bse;
         } catch (Exception e) {
             throw new BackplaneServerException(e.getMessage(), e);
         }
     }
 
     private void aniLogNewChannel(HttpServletRequest request, String referer, String version, String bus, String channel) {
         if (!anilogger.isEnabled()) {
             return;
         }
 
         String channelId = "https://" + request.getServerName() + "/" + version + "/bus/" + bus + "/channel/" + channel;
         String siteHost = (referer != null) ? ServletUtil.getHostFromUrl(referer) : null;
         Map<String,Object> aniEvent = new HashMap<String,Object>();
         aniEvent.put("channel_id", channelId);
         aniEvent.put("bus", bus);
         aniEvent.put("version", version);
         aniEvent.put("site_host", siteHost);
 
         aniLog("new_channel", aniEvent);
     }
 
     private void aniLogPollMessages(HttpServletRequest request, String referer, String version, String bus, String channel, List<BackplaneMessage> messages) {
         if (!anilogger.isEnabled()) {
             return;
         }
 
         String channelId = "https://" + request.getServerName() + "/" + version + "/bus/" + bus + "/channel/" + channel;
         String siteHost = (referer != null) ? ServletUtil.getHostFromUrl(referer) : null;
 
         Map<String,Object> aniEvent = new HashMap<String,Object>();
         aniEvent.put("channel_id", channelId);
         aniEvent.put("bus", bus);
         aniEvent.put("version", version);
         aniEvent.put("site_host", siteHost);
         List<String> messageIds = new ArrayList<String>();
         for (BackplaneMessage message : messages) {
             messageIds.add(message.getIdValue());
         }
         aniEvent.put("message_ids", messageIds);
 
         aniLog("poll_messages", aniEvent);
     }
 
     private void aniLogNewMessage(String version, String bus, String channelId, String clientId) {
         if (!anilogger.isEnabled()) {
             return;
         }
 
         Map<String,Object> aniEvent = new HashMap<String,Object>();
         aniEvent.put("channel_id", channelId);
         aniEvent.put("bus", bus);
         aniEvent.put("version", version);
         aniEvent.put("client_id", clientId);
 
         aniLog("new_message", aniEvent);
     }
 
     private void aniLog(String eventName, Map<String,Object> eventData) {
         ObjectMapper mapper = new ObjectMapper();
         String time = DateTimeUtils.ISO8601.get().format(new Date(System.currentTimeMillis()));
         eventData.put("time", time);
         try {
             anilogger.log(eventName, mapper.writeValueAsString(eventData));
         } catch (Exception e) {
             String errMsg = "Error sending analytics event: " + e.getMessage();
             logger.error(errMsg, BackplaneConfig.getDebugException(e));
         }
     }
 }
