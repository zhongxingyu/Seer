 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVACAO, SA (PTIN), IBM Corp.,
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package si.stecce.societies.crowdtasking.api.RESTful.impl;
 
 import com.google.appengine.api.channel.ChannelMessage;
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.gson.Gson;
 import com.googlecode.objectify.cmd.Query;
 import si.stecce.societies.crowdtasking.api.RESTful.IRemoteControlAPI;
 import si.stecce.societies.crowdtasking.model.CTUser;
 import si.stecce.societies.crowdtasking.model.Channel;
 import si.stecce.societies.crowdtasking.model.Community;
 import si.stecce.societies.crowdtasking.model.Task;
 import si.stecce.societies.crowdtasking.model.dao.ChannelDAO;
 import si.stecce.societies.crowdtasking.model.dao.CommunityDAO;
 import si.stecce.societies.crowdtasking.model.dao.TaskDao;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Logger;
 
 /**
  * Remote control public display
  *
  * @author Simon Jureša
  */
 @Path("/remote/{querytype}")
 public class RemoteControlAPI implements IRemoteControlAPI {
    private static final String notCheckedInMessage = "To see this on a larger screen, please go to a SOCIETIES public display screen and update your location.";
    //    private static final String notCheckedInMessage = "You are not a member of any community with this collaborative space.";
     private static final Logger log = Logger.getLogger(RemoteControlAPI.class.getName());
 
     private boolean isTooLate(Date date) {
         Date now = new Date();
         long timeOut = UsersAPI.getApplicationSettings().getChekInTimeOut();
         log.info("timeout:" + timeOut);
         // automatic check-out after set period
         Date toLate = new Date(date.getTime() + timeOut);
         return (now.after(toLate));
     }
 
     @Override
     @GET
     @Produces({MediaType.TEXT_PLAIN})
     public Response get(@PathParam("querytype") String querytype,
                         @DefaultValue("") @QueryParam("page") String page,
                         @DefaultValue("") @QueryParam("taskId") String taskId,
                         @DefaultValue("") @QueryParam("channelNumber") String channelNumber,
                         @DefaultValue("") @QueryParam("communityId") String communityId,
                         @Context HttpServletRequest request) {
         CTUser user = UsersAPI.getLoggedInUser(request.getSession());
         if (user == null) {
             return Response.status(Status.UNAUTHORIZED).entity("Not authorized.").type("text/plain").build();
         }
 
         log.info("querytype: " + querytype);
 
         log.info("channelId: " + channelNumber);
         log.info("communityId: " + communityId);
         log.info("user: " + user.getUserName());
         log.info("checkin date: " + user.getCheckIn());
         log.info("checkin space id: " + user.getSpaceId());
         String message = null;
         Long spaceId = user.getSpaceId();
         Date checkedInDate = user.getCheckIn();
         if (spaceId == null || checkedInDate == null) {
             return Response.status(Status.UNAUTHORIZED).entity(notCheckedInMessage).type("text/plain").build();
         }
         if (isTooLate(checkedInDate)) {
             return Response.status(Status.GONE).entity("Last known location too old.").type("text/plain").build();
         }
 
         if ("takeControl".equalsIgnoreCase(querytype)) {
             if (!"".equalsIgnoreCase(channelNumber) && !"".equalsIgnoreCase(communityId)) {
                 // TODO preveri comunityID ga shrani in pošlji PDju naj se reloada
                 Channel channel = new Channel(new Long(channelNumber), user.getId(), new Long(communityId), user.getSpaceId(), new Date());
                 ChannelDAO.save(channel);
                 sendMessage(channelNumber, "takeControl:" + channelNumber);
                 message = "You took control of the public display.";
                 return Response.ok().entity(message).build();
             }
 
             Query<Community> comms = CommunityDAO.findCommunities(spaceId, user);
             String tc = "";
             if (comms.count() == 0) {
                 message = "You are not a member of any community with this collaborative space.";
             } else if (comms.count() == 1) {
                 Community comm = comms.first().get();
                 tc = comms.first().get().getName();
                 Channel channel = new Channel(new Long(channelNumber), user.getId(), comm.getId(), user.getSpaceId(), new Date());
                 channel = ChannelDAO.save(channel);
                 user.setChannelId(channel.getId());
                 UsersAPI.saveUser(user);
                 sendMessage(channelNumber, "takeControl:" + channelNumber);
                 message = "You took control of the public display.";
             } else if (comms.count() > 1) {
                 Community comm = comms.first().get();
                 ArrayList<Community> communities = new ArrayList<>();
                 for (Community community : comms) {
                     communities.add(community);
                 }
                 ArrayList response = new ArrayList();
                 response.add(CommunityAPI.getCommunityJSes(communities, user));
                 response.add(channelNumber);
                 Gson gson = new Gson();
                 return Response.status(Status.CONFLICT).entity(gson.toJson(response)).type("text/plain").build();
             }
 
             return Response.ok().entity(message).build();
         }
 
         if ("changeChannel".equalsIgnoreCase(querytype)) {
            message = "changeTo:/cs/" + SpaceAPI.getCollaborativeSpace(user.getSpaceId()).getUrlMapping() + "?p=" + page;
         }
         if ("showTask".equalsIgnoreCase(querytype)) {
             // check if user took control
             Channel channel = ChannelDAO.load(user.getChannelId());
 
             if (channel == null || isTooLate(channel.getCreated())) {
                 return Response.status(Status.GONE).entity("Take control of the public display first.").type("text/plain").build();
             }
             // check community
             Task task = TaskDao.getTaskById4User(new Long(taskId), user);
             if (task == null) {
                 return Response.status(Status.GONE).entity("Task is not in your community.").type("text/plain").build();
             }
             if (!isTaskInCommunity(task, channel.getCommunityId())) {
                 return Response.status(Status.GONE).entity("Task is not in the community shown on the public display.").type("text/plain").build();
             }
             message = "showTask:" + taskId;
             EventAPI.logShowTaskOnPd(new Long(taskId), user);
         }
         if ("hideTask".equalsIgnoreCase(querytype)) {
             message = "hideTask";
         }
         sendMessage(spaceId, message);
         return Response.ok().entity("Request sent.").build();
     }
 
     private boolean isTaskInCommunity(Task task, Long communityId) {
         for (Community community : task.getCommunities()) {
             if (community.getId().longValue() == communityId.longValue()) {
                 return true;
             }
         }
         return false;
     }
 
     private void sendMessage(Long spaceId, String message) {
         sendMessage(Long.toString(spaceId), message);
     }
 
     private void sendMessage(String channelId, String message) {
         ChannelService channelService = ChannelServiceFactory.getChannelService();
         channelService.sendMessage(new ChannelMessage(channelId, message));
     }
 }
