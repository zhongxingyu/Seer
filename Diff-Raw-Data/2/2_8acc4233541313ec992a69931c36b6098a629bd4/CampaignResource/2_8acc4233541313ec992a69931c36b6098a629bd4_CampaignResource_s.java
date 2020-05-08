 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.fhg.fokus.service;
 
 import de.fhg.fokus.facades.*;
 import de.fhg.fokus.misc.Counter;
 import de.fhg.fokus.persistence.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.*;
 
 /**
  *
  * @author Hannes Gorges
  */
 @Stateless
 @Path("campaign")
 public class CampaignResource {
     
     @EJB
     private PublisheditemFacade publisheditemFacade;
     @EJB
     private MessageFacade messageFacade;
     @EJB
     private PublishchannelFacade publishchannelFacade;
     @EJB
     private LocationFacade locationFacade;
     @EJB
     private CampaigntopicsFacade campaigntopicsFacade;
     @EJB
     private UserdataFacade userdataFacade;
     @EJB
     private SampleSessionBean sampleSessionBean;
     @EJB    
     private CampaignFacade campaignFacade;
     @EJB
     private UserRoleFacade userroleFacade;
 
     /**
      * Returns all campaigns for the given user. It also returns the campaign
      * location object and the campaign topics. <br /> Address: GET
      * [server]/resources/campaign?sid=test_user
      *
      * @param sid valid session id
      * @return list of campaigns
      */
     @GET
     @Produces({"application/xml", "application/json"})
     public List<Campaign> getCampaigns(@DefaultValue("test_user") @QueryParam("sid") String sid) {
         if (sid.equals("test_user")) {//return test data
             return sampleSessionBean.makeSampleCampaignList();
         }
         //check sid
         List<Userdata> udList = userdataFacade.executeNamedQuery("Userdata.findByUserSIGN", "userSIGN", sid);
         if (udList.isEmpty()) {
             Campaign c = new Campaign();
             c.setTitle("The session id is not valid!");
             List<Campaign> cList = new ArrayList<>();
             cList.add(c);
             return cList;
         }
         
         Userdata ud = udList.get(0);
         return campaignFacade.getCampaignsForUser(ud);
         
     }
 
     /**
      * Create new campaign and returns the persisted object (with valid id).
      * Every campaign needs a location. Therefore a location object inside the
      * campaign is needed (id is enough). It is also possible to sends a list of
      * initial campaign topics (id is not needed) inside the campaign object. A
      * list of related publishChannels can also be inside the campaign
      * object.<br />
      *
      * Address: POST [server]/resources/campaign?sid=test_user
      *
      * @param campaign Campaign object
      * @param sid valid session id
      * @return
      */
     @POST
     @Consumes({"application/xml", "application/json"})
     @Produces({"application/xml", "application/json"})
     public Campaign createCampaign(Campaign campaign, @DefaultValue("test_user") @QueryParam("sid") String sid) {
         
         if (sid.equals("test_user")) {//return test data                
             return sampleSessionBean.makeSampleCampaign();
         }
 
         //check sid
         List<Userdata> udList = userdataFacade.executeNamedQuery("Userdata.findByUserSIGN", "userSIGN", sid);
         if (udList.isEmpty()) {
             Campaign c = new Campaign();
             c.setTitle("The session id is not valid!");
             return c;
         }
         Userdata ud = udList.get(0);
         
         
         Location l = locationFacade.find(campaign.getIdLocation().getIdLocation());
         campaign.setIdLocation(l);
         
         
         List<Campaigntopics> topicList = null;
         if (campaign.getCampaigntopicsList() != null) {
             topicList = campaign.getCampaigntopicsList();
             campaign.setCampaigntopicsList(null);
         }
         
         
         if (campaign.getPublishchannelList() != null) {
             List<Publishchannel> pcList = campaign.getPublishchannelList();
             campaign.setPublishchannelList(new ArrayList());
             for (Publishchannel pc : pcList) {
                 Publishchannel pcInDb = publishchannelFacade.find(pc.getIdPublishChannel());
                 campaign.addPublishchannel(pcInDb);
             }
         }
         campaign.setCreationdate(new Date());//today
 
         // chck hashTag
         if(!campaign.getHashTag().isEmpty() && !campaign.getHashTag().contains("#"))
             campaign.setHashTag("#" + campaign.getHashTag());
         
         campaignFacade.create(campaign);
         
         if (topicList != null) {
             for (Campaigntopics ct : topicList) {
                 ct.setCampaignidCampaign(campaign);
                 campaigntopicsFacade.create(ct);
                 campaign.addCampaigntopic(ct);
             }
         }
 
         //add userRole and update userData and campaign
         Userrole ur = new Userrole();
         ur.setIdCampaign(campaign);
         ur.setIdUserData(ud);
         ur.setUserRole("manager");
         userroleFacade.create(ur);
         
         ud.addUserrole(ur);
         campaign.addUserrole(ur);
         
         return campaign;
     }
 
     /**
      * Change the values of the campaign object of the database with the given
      * one. It don't change foreign keys (relations). Only the campaign manager
      * can change the campaign object.<br />
      *
      * Address: PUT [server]/resources/campaign/[campId]?sid=test_user
      *
      * @param campaign Campaign Object
      * @param sid session id
      * @param campaignId id from the campaign
      * @return the campaign object from database (changed or unchanged)
      */
     @PUT
     @Path("{id}")
     @Consumes({"application/xml", "application/json"})
     @Produces({"application/xml", "application/json"})
     public Campaign updateCampaign(Campaign campaign, @DefaultValue("test_user") @QueryParam("sid") String sid, @PathParam("id") Integer campaignId) {
         
         if (sid.equals("test_user")) {//return test data                
             return sampleSessionBean.makeSampleCampaign();
         }
 
         //check sid
         List<Userdata> udList = userdataFacade.executeNamedQuery("Userdata.findByUserSIGN", "userSIGN", sid);
         if (udList.isEmpty()) {
             Campaign c = new Campaign();
             c.setTitle("The session id is not valid!");
             return c;
         }
         Userdata ud = udList.get(0);
         
         Campaign dbCampaign = campaignFacade.find(campaignId); //get requested campaign
 
         if (dbCampaign == null) {
             Campaign c = new Campaign();
             c.setTitle("It exists no campaign with this id!");
             return c;
         }
         
         // chck hashTag
         if(!campaign.getHashTag().isEmpty() && !campaign.getHashTag().contains("#"))
             campaign.setHashTag("#" + campaign.getHashTag());
         
         if (userroleFacade.isCampaignManager(ud, campaign)) { //is the user the campaign manager? 
             dbCampaign.setActive(campaign.getActive());
             dbCampaign.setCreationdate(campaign.getCreationdate());
             dbCampaign.setEnddate(campaign.getEnddate());           
             dbCampaign.setHashTag(campaign.getHashTag());
             dbCampaign.setNotes(campaign.getNotes());
             dbCampaign.setStartdate(campaign.getStartdate());
             dbCampaign.setTitle(campaign.getTitle());
             dbCampaign.setUrl(campaign.getUrl());
             
             if (campaign.getIdLocation() != null) { //location is changed?
                 Location l = locationFacade.find(campaign.getIdLocation().getIdLocation());
                 dbCampaign.setIdLocation(l);
             }
             campaignFacade.edit(dbCampaign);
         } else {
             Campaign c = new Campaign();
             c.setTitle("You are not the campaign manager. You have no rights to edit this campaign.");
             return c;
         }        
         
         return dbCampaign;
     }
 
     /**
      * Deletes the campaign and some related objects if the user is the campaign
      * manager.<br />
      *
      * Address: DELETE [server]/resources/campaign/[campId]?sid=test_user
      *
      * @param sid
      * @param campaignId
      */
     @DELETE
     @Path("{id}")
     public void deleteCampaign(@DefaultValue("test_user") @QueryParam("sid") String sid, @PathParam("id") Integer campaignId) {
 
         //check sid
         List<Userdata> udList = userdataFacade.executeNamedQuery("Userdata.findByUserSIGN", "userSIGN", sid);
         if (udList == null) {
             return;
         }
         Userdata ud = udList.get(0);
         
         Campaign dbCampaign = campaignFacade.find(campaignId); //get requested campaign
 
         if (userroleFacade.isCampaignManager(ud, dbCampaign)) {//campaign manager
             
             for (Userrole ur : dbCampaign.getUserroleList()) {//update user data
                 ur.getIdUserData().removeUserrole(ur);                
             }
             campaignFacade.remove(dbCampaign);
         }
     }
 
     /**
      * Gives you all messages from a campaign. Only paticipants of this campaign
      * can see this object.<br />
      *
      * Address: GET
      * [server]/resources/campaign/[campId]/message?sid=test_user&from=25
      *
      * @param sid valid session id
      * @param campaignId id of the campaign
      * @param from What is the first message?
      *
      * @return list of messages
      */
     @GET
     @Path("{id}/message")
     @Produces({"application/xml", "application/json"})
     public List<Message> getCampaignMessages(@DefaultValue("test_user") @QueryParam("sid") String sid, @PathParam("id") Integer campaignId, @DefaultValue("0") @QueryParam("from") Integer from) {
         
         if (sid.equals("test_user")) {//return test data                
             return sampleSessionBean.makeSampleMessageList();
         }
         
         List<Message> mList = new ArrayList<>();
 
         //check sid
         List<Userdata> udList = userdataFacade.executeNamedQuery("Userdata.findByUserSIGN", "userSIGN", sid);
         if (udList.isEmpty()) {
             Message m = new Message();
             m.setTitle("The session id is not valid!");
             mList.add(m);
             return mList;
         }
         Userdata ud = udList.get(0);
         
         Campaign dbCampaign = campaignFacade.find(campaignId); //get requested campaign
 
         if (dbCampaign == null) {
             Message m = new Message();
             m.setTitle("It exists no campaign with this id!");
             mList.add(m);
             return mList;
         }
 
         if (userroleFacade.isCampaignPaticipant(ud, dbCampaign)) { //only paticipants of this campaign can see this object.
 
             List<Message> messages = messageFacade.getMessages(dbCampaign.getIdCampaign(), from);
             Userrole userRole = userroleFacade.getUserRole(ud, dbCampaign);
            userRole.setLastActive(new Date()); //now
             userroleFacade.edit(userRole);
             return messages;
        
         } else {
             Message m = new Message();
             m.setTitle("The user have no rights to get information about the messages of this campaign.");
             mList.add(m);
             return mList;
         }  
     }
 
     /**
      * Method creates a new message which belongs to the given user (identifies
      * by sid). Every message needs a campaign object, so the campaign id is
      * necessary. This message is only available in the padgets database and not
      * published to any Networks. The method returns a sample message, if the
      * sid is "test_user".<br /> The service needs a Social Network, where the
      * message should be published. If it is more then one network, it is
      * possible to hand over a list like facebook,twitter,blogger<br />
      *
      * Address: POST
      * [server]/resources/campaign/[campId]/message?sid=test_user&socialNetwork=[list
      * of networks
      *
      * @param message Message object with campaign object inside
      * @param sid valid session id
      * @param campaignId Id of the campaign, which belongs to the message
      * @param snList list of Social Networks
      * @return
      */
     @POST
     @Path("{id}/message")
     @Consumes({"application/xml", "application/json"})
     @Produces({"application/xml", "application/json"})
     public Message createMessage(Message message, @DefaultValue("test_user") @QueryParam("sid") String sid, @PathParam("id") Integer campaignId, @DefaultValue("nothing") @QueryParam("socialNetwork") String snList) {
         
         if (sid.equals("test_user")) {//return test data                
             return sampleSessionBean.makeSampleMessage();
         }
         
         Campaign c = campaignFacade.find(campaignId); //get requested campaign
 
         if (c == null) {
             Message m = new Message();
             m.setTitle("It exists no campaign with this id!");
             return m;
         }
         
         if (snList.equals("nothing")) {
             Message m = new Message();
             m.setTitle("This service needs a list of Social Network delivered through the auery param socialNetwork !");
             return m;
         }
 
         //check sid
         List<Userdata> udList = userdataFacade.executeNamedQuery("Userdata.findByUserSIGN", "userSIGN", sid);
         if (udList.isEmpty()) {
             Message m = new Message();
             m.setTitle("The session id is not valid!");
             return m;
         }
         Userdata ud = udList.get(0);
 
         if (!(userroleFacade.isCampaignPaticipant(ud, c))) { //is the user the campaign manager or helper?
             Message m = new Message();
             m.setTitle("You are not the campaign manager or a helper. You have no rights to edit this campaign.");
             return m;
         } 
 
         List<Publishchannel> publishchannelList = new ArrayList<>();
         for (Publishchannel pc : c.getPublishchannelList()) {
             if (snList.matches("(?s).*" + pc.getNetwork() + ".*")) {
                 publishchannelList.add(pc);
             }
         }
         if (publishchannelList.isEmpty()) {
             Message m = new Message();
             m.setTitle("No publish channels are matched with your input.");
             return m;
         }
         
         message.setIdUserData(ud);
         message.setIdCampaign(c);
         message.setCreateTime(new Date()); //now
 
         messageFacade.create(message);
 
         //update user entity and campaign
         ud.addMessage(message);
         c.addMessage(message);
 
         //Create PublishedItem objects
         for (Publishchannel pc : publishchannelList) {
             Publisheditem pi = new Publisheditem();
             pi.setIdMessage(message);
             pi.setIdPublishChannel(pc);
             pi.setIsPublished(Boolean.FALSE);
             publisheditemFacade.create(pi);
             message.addPublisheditem(pi);
         }
         System.out.println("");
 //       publishService.publishMessage(message.getIdMessage());
         return message;
     }
 
     /**
      * Add a publish channel to a campaign.<br />
      *
      * Address: POST
      * [server]/resources/campaign/[campId]/publishchannel?sid=test_user&channelId=[publishChannelId]
      *
      * @param sid valid session id
      * @param campaignId id of the campaign
      * @param channelId id of the publish channel
      * @return
      */
     @PUT
     @Path("{id}/publishchannel")
     @Produces({"application/xml", "application/json"})
     public Campaign addPublishChannel(@DefaultValue("test_user") @QueryParam("sid") String sid, @PathParam("id") Integer campaignId, @DefaultValue("-1") @QueryParam("channelId") Integer channelId) {
         if (sid.equals("test_user")) {//return test data                
             return sampleSessionBean.makeSampleCampaign();
         }
         
         Campaign c = campaignFacade.find(campaignId); //get requested campaign
 
         if (c == null) {
             c = new Campaign();
             c.setTitle("It exists no campaign with this id!");
             return c;
         }
 
         //check sid
         List<Userdata> udList = userdataFacade.executeNamedQuery("Userdata.findByUserSIGN", "userSIGN", sid);
         if (udList.isEmpty()) {
             c = new Campaign();
             c.setTitle("The session id is not valid!");
             
             return c;
         }
         Userdata ud = udList.get(0);
 
         if (!userroleFacade.isCampaignManager(ud, c)) { //is the user the campaign manager?
             c = new Campaign();
             c.setTitle("You are not the campaign manager. You have no rights to edit this campaign.");
             return c;
         } 
 
         Publishchannel pc = publishchannelFacade.find(channelId);
         if (pc == null) {
             c = new Campaign();
             c.setTitle("The id of the publish channel is not correct.");
             return c;
         }
         if (c.getPublishchannelList().contains(pc)) {
             c = new Campaign();
             c.setTitle("The campaign already contains this publish channel.");
             return c;
         }
         
         c.addPublishchannel(pc);
         pc.addCampaign(c);
         publishchannelFacade.edit(pc);
         campaignFacade.edit(c);
         userdataFacade.refresh(ud);
         
         return c;
         
     }
 
     /**
      * How many messages has a campaign?<br /> Failure codes: <br /> -1 - "It
      * exists no campaign with this id!"<br /> -2 - "The session id is not
      * valid!"<br /> -3 - "You are not the campaign manager. You have no rights
      * to edit this campaign."<br />
      *
      * @param sid
      * @param campaignId
      * @return
      */
     @GET
     @Path("{id}/messagecount")
     @Produces({"application/xml", "application/json"})
     public Counter getMessageCount(@DefaultValue("test_user") @QueryParam("sid") String sid, @PathParam("id") Integer campaignId) {
         if (sid.equals("test_user")) {//return test data                
             return sampleSessionBean.makeSampleCounter();
         }
         
         Campaign c = campaignFacade.find(campaignId); //get requested campaign
 
         Counter co = new Counter();
         if (c == null) {
             co.setCount(-1);
             return co;
         }
 
         //check sid
         List<Userdata> udList = userdataFacade.executeNamedQuery("Userdata.findByUserSIGN", "userSIGN", sid);
         if (udList.isEmpty()) {
             co.setCount(-2);
             return co;
         }
         Userdata ud = udList.get(0);
 
         if (!userroleFacade.isCampaignPaticipant(ud, c)) { //is the user a participant of the campaign?
 
             co.setCount(-3);
             return co;
         } 
         
         Long counter = messageFacade.countMessages(campaignId);
         co.setCount(counter);
         return co;
     }
 }
