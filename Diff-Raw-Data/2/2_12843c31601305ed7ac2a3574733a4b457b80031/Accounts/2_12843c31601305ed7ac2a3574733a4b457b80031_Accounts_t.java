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
 public class Accounts extends Controller {
     
     public static void create(String name,String currency,List<String> emails) {
         
         //Retrieve user
         User creator=User.findById(Long.parseLong(session.get("uuid")));
         //Create Account
         Account newAccount=new Account(null, name, currency);
         newAccount.create();
         //Create Creator participant
         ParticipantAccount creatorParticipant=ParticipantsAccounts.create(creator, newAccount, ParticipantStatus.CONFIRMED, ParticipantRole.ADMIN);
         
         //Associate Account and participant
         newAccount.creator=creatorParticipant;
         newAccount.listParticipants.add(creatorParticipant);
             //persist
             newAccount.save();
         
         //Update bidirectional link
         creator.listParticipantAccount.add(creatorParticipant);
         creator.save();
         
         User userTemp;
         //Iterate over the emails, and create user if they don't exist
         for (Iterator iterator = emails.iterator(); iterator.hasNext();) {
             String email = (String) iterator.next();
             
             userTemp=User.find("byEmail", email).first();
             //if user doesn't exit, whe create it with is email
             if(userTemp==null) {
                 userTemp=Users.createWithEmail(email);
             }
             
             
             ParticipantAccount newParticipantAccount=ParticipantsAccounts.create(userTemp, newAccount, ParticipantStatus.PENDING, ParticipantRole.USER);
             //update the bidirectionnals link
             userTemp.listParticipantAccount.add(newParticipantAccount);
             userTemp.save();
             
             newAccount.listParticipants.add(newParticipantAccount);
             newAccount.save();
         }
         
         renderJSON(new JSONSerializer().include("listParticipants").exclude("*.class").serialize(newAccount));
        
     }
     
     public static void delete(Long accountId) {
         //NB: deprecated if we set up an other way to be ADMIN
         
         //Retrieve user
         User creator=User.findById(Long.parseLong(session.get("uuid")));
         
         //Retrieve account
         Account accountToDelete=Account.findById(accountId);
         if(accountToDelete!=null) {
             if(accountToDelete.creator.user.equals(creator)) {
                 for (Iterator iterator = accountToDelete.listParticipants.iterator(); iterator.hasNext();) {
                     ParticipantAccount participantAccount = (ParticipantAccount) iterator.next();
                     User userTemp=User.findById(participantAccount.user.id);
                     userTemp.listParticipantAccount.remove(participantAccount);
                     userTemp.save();
                 }
                 accountToDelete.delete();
                 renderJSON("{\"success\":\"Account delete\"}");
             }
             else {
                 renderJSON("{\"error\":\"Not allowed to delete an account if you're not ADMIN\"}");
             }
         }
         else {
             renderJSON("{\"error\":\"This account doesn't exist\"}");
         }
         
     }
     
     public static void addExpense(Long accountId,String description,Float amount) {
         if(description==null) {
             renderJSON("{\"error\":\"The description is not correct\"}");
         }
         if(amount==null) {
            renderJSON("{\"error\":\"The amount is not correct\"}");
         }
         //Retrieve user
         User owner=User.findById(Long.parseLong(session.get("uuid")));
         //Retrieve account
         Account account=Account.findById(accountId);
         if(account==null) {
             renderJSON("{\"error\":\"This account doesn't exist\"}");
         }
         //Retrieve ParticipantAccount
         ParticipantAccount participantAccount=ParticipantAccount.find("SELECT p FROM ParticipantAccount p WHERE p.user.id=? AND p.account.id=?",owner.id,account.id).first();
         if(participantAccount==null) {
             renderJSON("{\"error\":\"You don't participate to this account\"}");
         }
         
         Expense expense=new Expense(participantAccount, account, amount, description);
         expense.create();
         
         account.listExpenses.add(expense);
         account.save();
         
         participantAccount.listExpenses.add(expense);
         participantAccount.save();
         
         renderJSON("{\"success\":\"The expense was successfully registered\"}");
     }
     
     public static void getAllParticipantsOfAccount(Long accountId) {
         //Retrieve user
         User owner=User.findById(Long.parseLong(session.get("uuid")));
         //Retrieve account
         Account account=Account.findById(accountId);
         if(account==null) {
             renderJSON("{\"error\":\"This account doesn't exist\"}");
         }
         //Retrieve ParticipantAccount
         ParticipantAccount participantAccount=ParticipantAccount.find("SELECT p FROM ParticipantAccount p WHERE p.user.id=? AND p.account.id=?",owner.id,account.id).first();
         if(participantAccount==null) {
             renderJSON("{\"error\":\"You don't participate to this account\"}");
         }
         renderJSON(new JSONSerializer().exclude("*.class","account").rootName("listOfParticipants").serialize(account.listParticipants));
     }
     
     public static void getAllExpensesOfAccount(Long accountId) {
         //Retrieve user
         User owner=User.findById(Long.parseLong(session.get("uuid")));
         //Retrieve account
         Account account=Account.findById(accountId);
         if(account==null) {
             renderJSON("{\"error\":\"This account doesn't exist\"}");
         }
         //Retrieve ParticipantAccount
         ParticipantAccount participantAccount=ParticipantAccount.find("SELECT p FROM ParticipantAccount p WHERE p.user.id=? AND p.account.id=?",owner.id,account.id).first();
         if(participantAccount==null) {
             renderJSON("{\"error\":\"You don't participate to this account\"}");
         }
         renderJSON(new JSONSerializer().exclude("*.class","account","owner.account").rootName("listOfExpenses").serialize(account.listExpenses));
     }
     
 }
