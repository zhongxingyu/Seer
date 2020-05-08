 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske druĹľbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAĂ‡Ă�O, SA (PTIN), IBM Corp., 
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
 package si.stecce.societies.crowdtasking.controller;
 
 import com.google.appengine.api.channel.ChannelMessage;
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Ref;
 import si.stecce.societies.crowdtasking.api.RESTful.impl.SpaceAPI;
 import si.stecce.societies.crowdtasking.model.CTUser;
 import si.stecce.societies.crowdtasking.model.Channel;
 import si.stecce.societies.crowdtasking.model.CollaborativeSpace;
 import si.stecce.societies.crowdtasking.model.Community;
 import si.stecce.societies.crowdtasking.model.dao.ChannelDAO;
 import si.stecce.societies.crowdtasking.model.dao.CollaborativeSpaceDAO;
 import si.stecce.societies.crowdtasking.model.dao.CommunityDAO;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.CharBuffer;
 import java.util.Random;
 import java.util.logging.Logger;
 
 /**
  * Servlet for managing collaborative spaces
  *
  * @author Simon Jureša
  */
 @SuppressWarnings("serial")
 public class PublicDisplayController extends HttpServlet {
     private static final Logger log = Logger.getLogger(PublicDisplayController.class.getName());
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         String channelId = request.getParameter("id");
         if (channelId == null || "".equalsIgnoreCase(channelId)) {
             sendFirstScreen(response);
         } else {
            showPublicDisplay(Long.valueOf(channelId), request, response);
         }
     }
 
    private void showPublicDisplay(Long channelNumber, HttpServletRequest request, HttpServletResponse response) throws IOException {
         Channel channel = ChannelDAO.loadChannelByNumber(channelNumber);
         if (channel == null) {
             sendFirstScreen(response);
             return;
         }
         CollaborativeSpace space = CollaborativeSpaceDAO.load(channel.getSpaceId());
         Community community = CommunityDAO.loadCommunity(channel.getCommunityId());
         String spaceId = String.valueOf(space.getId());
 
 /*  Moved to SessionFilter. Why?
         // login user
         request.getSession().setAttribute("loggedIn", "true");
         request.getSession().setAttribute("CTUserId", channel.getUserId());
         System.out.println("user logged in");
 */
 
         ChannelService channelService = ChannelServiceFactory.getChannelService();
         String token = channelService.createChannel(spaceId);
         System.out.println("Channel created with id: " + spaceId);
         log.info("token created:" + token);
 
        FileReader reader = new FileReader("WEB-INF/html/publicDisplay.html");
         CharBuffer buffer = CharBuffer.allocate(16384);
         reader.read(buffer);
 
         String index = new String(buffer.array());
         index = index.replaceAll("\\{\\{ pdHeader \\}\\}", community.getName() + " - " + space.getName());
         index = index.replaceAll("\\{\\{ token \\}\\}", token);
         index = index.replaceAll("\\{\\{ communityId \\}\\}", community.getId().toString());
         index = index.replaceAll("\\{\\{ spaceId \\}\\}", spaceId.toString());
 
         response.setContentType("text/html");
         response.getWriter().write(index);
         reader.close();
 
         ChannelDAO.delete(channelNumber);
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         String message = req.getParameter("u");
         System.out.println("message:" + message);
     }
 
     private void sendFirstScreen(HttpServletResponse response) throws IOException {
         Random randomGenerator = new Random();
         String channelNumber = Integer.toString(randomGenerator.nextInt(100000));
         ChannelService channelService = ChannelServiceFactory.getChannelService();
         String token = channelService.createChannel(channelNumber);
         log.info("token created:" + token);
 
         FileReader reader = new FileReader("WEB-INF/html/getDisplay.html");
         CharBuffer buffer = CharBuffer.allocate(16384);
         reader.read(buffer);
 
         String index = new String(buffer.array());
         index = index.replaceAll("\\{\\{ channelNumber \\}\\}", channelNumber);
         index = index.replaceAll("\\{\\{ token \\}\\}", token);
 
         response.setContentType("text/html");
         response.getWriter().write(index);
         reader.close();
     }
 
     private boolean isMemeber(CTUser user, Community community) {
         if (community.getMembers() == null) {
             return false;
         }
         for (Ref<CTUser> member : community.getMembers()) {
             if (member.equals(Ref.create(Key.create(CTUser.class, user.getId())))) {
                 return true;
             }
         }
         return false;
     }
 
     private void sendMessage(Long spaceId, String message) {
         ChannelService channelService = ChannelServiceFactory.getChannelService();
         channelService.sendMessage(new ChannelMessage(Long.toString(spaceId), message));
     }
 
     private void mapSpaceToUrl(Long spaceId, String url) {
         CollaborativeSpace space = SpaceAPI.getCollaborativeSpace(spaceId);
         space.setUrlMapping(url);
         CollaborativeSpaceDAO.save(space);
     }
 }
