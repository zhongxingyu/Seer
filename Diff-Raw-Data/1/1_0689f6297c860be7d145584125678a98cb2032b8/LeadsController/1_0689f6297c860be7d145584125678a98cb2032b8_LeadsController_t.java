 package controllers;
 
 import controllers.compositions.AdminUserCheck;
 import controllers.compositions.UserCheck;
 import models.Lead;
 import models.ServerNode;
 import models.User;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.With;
 import server.ApplicationContext;
 import tyrex.services.UUID;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: guym
  * Date: 8/20/13
  * Time: 12:12 PM
  */
 public class LeadsController extends Controller {
 
     private static Logger logger = LoggerFactory.getLogger(LeadsController.class);
 
     @With( UserCheck.class )
     public static Result postLead( String userId , String authToken ){
         User user = ( User) ctx().args.get("user");
         JsonNode postLeadBody = request().body().asJson();
         logger.info("postLeadBody = " + postLeadBody );
         String email = (String) postLeadBody.get("email").asText();
 
         Lead lead = Lead.findByOwnerAndEmail( user, email );
         if ( lead == null ){
             lead = new Lead();
             lead.email = email;
             lead.owner = user;
             lead.uuid = UUID.create();
             lead.confirmationCode = UUID.create().replaceAll("-", "");
             lead.validated = false;
             lead.extra = postLeadBody.toString();
             lead.save();
         }else{
             logger.info("lead [{}] already exists for user [{}], sending email again", email, user.toDebugString() );
         }
 
         logger.info("sending registration mail to [{}]", lead.toDebugString() );
         ApplicationContext.get().getMailSender().sendRegistrationMail( lead );
 
          return ok(Json.toJson(lead));
     }
 
     /**
      *
      *
      * This is a REST API call used by users to extend leads' widget timeout.
      *
      * Leads provide their email and get more trial time in return.
      *
      * Currently, we allow user A to extend time on widgets defined by user B.
      * We also allow to assign a single hacker to multiple widgets.
      * This is a potential security threat as a hacker can "snatch" widgets and keep assigning a dummy lead
      * to each widget, thus denying it from other leads.
      *
      * Currently we will do with a permissions definition for users that can assign leads to widgets.
      * This reduces the problems to users with this permission.
      * A wider resolution is required.
      *
      *
      * @param userId - the user's primary key
      * @param authToken - the user's authentication token
      * @param leadId - the lead's primary key
      * @param instanceId = the server node primary key
      * @return - success or error
      */
     @With( UserCheck.class )
     public static Result assignLeadToWidgetInstance( String userId, String authToken, Long leadId, Long instanceId ){
          User user = (User) ctx().args.get("user");
 
         logger.info(user.getPermissions().toString());
         if ( !user.getPermissions().isCanAssignLeads() ){
           logger.error("user {} tried to assign lead {} to instanceId {} but failed due to permissions", new Object[]{user.toDebugString(), leadId, instanceId});
            return forbidden("You need permission to assign leads");
         }
         ServerNode serverNode = ServerNode.find.byId( instanceId );
 
         Lead lead = Lead.find.byId( leadId );
 
 
         if ( lead == null || !lead.owner.getId().equals( user.getId() )){ // whether lead does not exist
             return notFound("no lead with id " + leadId );
         }
 
         // lets verify serverNode exists and is not assigned to another lead already.
         // if it is assigned to another lead, we want to give the same message as if the server node
         // does not exist.
         if ( serverNode == null || ( serverNode.getLead() != null && !serverNode.getLead().getId().equals(lead.getId()))){ // whether serverNode already assigned or does not exist
              return notFound("instanceId " + instanceId + " does not exist");
         }else if ( lead.getServerId() != null && !lead.getServerId().equals(serverNode.getId()) ){ // whether lead already assigned
             return notFound("invalid params");
         } else if ( serverNode.getLead() == null && lead.getServerId() == null ){
 
             serverNode.setLead( lead );
             serverNode.save();
 
             return ok();
         }else if ( serverNode.getLead() != null && serverNode.getLead().getId().equals( lead.getId() )){
             return ok();
         }
 
 
         return ok();
 
     }
 
     @With( UserCheck.class )
     public static Result confirmEmail( String userId, String authToken, Long leadId, String confirmationCode ){
         User user = (User) ctx().args.get("user");
         Lead lead = Lead.findByOwnerIdAndConfirmationCode( user, leadId, confirmationCode);
 
         if ( lead == null ){
             return notFound("no such lead");
         }else{
             lead.validated = true;
             lead.save();
             return ok();
         }
     }
 
 
     @With( UserCheck.class )
     public static Result getLead(  String userId, String authToken, String email  ){
         User user =  (User) ctx().args.get("user");
         Lead lead = Lead.findByOwnerAndEmail( user, email );
         return ok( Json.toJson(lead));
     }
 
     @With( AdminUserCheck.class )
     public static Result getAdminLeads( String userId, String authToken ){
         List<Lead> leads = Lead.find.all();
         ObjectMapper mapper = new ObjectMapper();
         mapper.getSerializationConfig().addMixInAnnotations( Lead.class , LeadUserMixin.class );
         return ok( mapper.valueToTree( leads ) );
     }
 
 
     public static class LeadUserMixin{
         @JsonProperty
         public User owner;
     }
     @With( UserCheck.class )
     public static Result getLeads( String userId, String authToken ){
         User user = ( User ) ctx().args.get("user");
         List<Lead> leads = Lead.findAllByOwner( user );
         return ok( Json.toJson( leads ) );
     }
 }
