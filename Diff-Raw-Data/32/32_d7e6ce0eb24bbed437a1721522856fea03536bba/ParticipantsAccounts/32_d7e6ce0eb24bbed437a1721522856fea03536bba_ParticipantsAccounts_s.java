 package controllers;
 
 import play.*;
 import play.libs.WS;
 import play.mvc.*;
 import utils.Constants.ParticipantRole;
 import utils.Constants.ParticipantStatus;
 import utils.Secure;
 
 import java.util.*;
 
 import com.google.gson.JsonObject;
 
 import flexjson.JSONSerializer;
 
 import models.*;
 
 @With(Secure.class)
 public class ParticipantsAccounts extends Controller {
     
     public static ParticipantAccount create(User user,Account account,ParticipantStatus status,ParticipantRole role) {
         ParticipantAccount participantAccount=new ParticipantAccount(user, account, status, role);
         participantAccount.create();
         return participantAccount;
     }
     
     public static void confirmParticipation(Long participantAccountId) {
         //Retrieve user
         User user=User.findById(Long.parseLong(session.get("uuid")));
         
         //Retrieve participantAccount id
         ParticipantAccount participantAccountToConfirm=ParticipantAccount.findById(participantAccountId);
         
        if(participantAccountToConfirm.user.equals(user) && participantAccountToConfirm.status!=ParticipantStatus.CONFIRMED) {
            participantAccountToConfirm.status=ParticipantStatus.CONFIRMED;
            participantAccountToConfirm.save();
            renderJSON(new JSONSerializer().exclude("*.class","user","account").serialize(participantAccountToConfirm));
         }
         else {
            //TODO render error: not allowed to modify 
         }
        
        renderJSON(new JSONSerializer().exclude("*.class","user","account").serialize(participantAccountToConfirm));
     }
 }
