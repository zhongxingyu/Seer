 package edu.gatech.islab.chat.main;
 
 import java.util.HashMap;
 import java.util.List;
 
 import edu.gatech.islab.chat.enums.AccountType;
 import edu.gatech.islab.chat.enums.Operation;
 import edu.gatech.islab.chat.session.Session;
 import edu.gatech.islab.chat.session.UChatSession;
 import edu.gatech.islab.chat.session.XMPPSession;
 import edu.gatech.islab.chat.user.User;
 
 public class Main {
     
     private static final String SUCCESS = "Success";
     private static final String HACKFAIL = "Trying to hack? You have failed.";
     private final String ucUsername = AccountType.UCHAT + "Username";
     private final String ucSessionId = AccountType.UCHAT + "SessionId";
 
     private HashMap<String, User> userMap;
     private MainHelper helper;
 
     public Main() {
         userMap = new HashMap<String, User>();
         helper = new MainHelper(userMap);
     }
 
     public Object[] doOperation(HashMap<String, Object> args) {
 
         String username = (String) args.get("Username");
         String uchatUsername = (String) args.get(ucUsername);
         Operation operation = (Operation) args.get("Operation");
         AccountType accountType = (AccountType) args.get("AccountType");
         User user = null;
         Session session = null;
         Object[] retArray = null;
 
         if(operation == null || 
            operation == Operation.NULL ||
            accountType == null || 
            accountType == AccountType.NULL ||
            username == null) {
 
            return new Object[]{HACKFAIL};
         }
 
         if(accountType != AccountType.UCHAT) {
             if(!validateUChatSession(args)) {
                return new Object[]{HACKFAIL};
             } 
         } else {
             uchatUsername = username;
         }
 
         user = helper.getUser(username, uchatUsername, accountType, operation);
 
         if(user != null) {
             session = helper.getSession(user, accountType);
         } 
 
         if(user == null || session == null) {
            return new Object[]{HACKFAIL};
         } 
 
         if((operation != Operation.LOGIN  && operation != Operation.REGISTER) &&
            (!session.getSessionId().equals(args.get("SessionId")))) {
             
            return new Object[]{HACKFAIL};
         }
 
         switch(operation) {
         case REGISTER:
         case LOGIN:
             String password = (String)args.get("Password");
             String retMessage = "Login Failed\n" + HACKFAIL;
             boolean opSuccess = false;
 
             if(operation == Operation.REGISTER) {
                 if(user.getAccountType() != AccountType.UCHAT) {
                     retArray = new Object[]{HACKFAIL};
                 } else {
                    retMessage = ((UChatSession)session).
                         createUser(user.getLogin(), password);
                    if(retMessage.equals(SUCCESS)) {
                        opSuccess = true;
                    } else {
                        opSuccess = false;
                    }
                 }
             } else {
                 opSuccess = session.login(user.getLogin(), password);
             }
 
             if(opSuccess) {
                 helper.setSessionId(session);
                 retArray = new Object[]{SUCCESS, session.getSessionId()};
                 helper.addToUserMap(uchatUsername, accountType, user);
             } else {
                 retArray = new Object[]{retMessage};
             }
             break;
 
         case VALIDATE:
             retArray = new Object[]{SUCCESS};
             break;
 
         case SENDMESSAGE:
             if(session instanceof XMPPSession) {
                 String message = (String)args.get("Message");
                 String recipient = (String)args.get("Recipient");
                 if(message != null && recipient != null) {
                     ((XMPPSession)session).sendMessage
                         (message, new User(recipient, recipient, accountType));
                     retArray = new Object[]{SUCCESS};
                 }
             } else {
                 retArray = new Object[]{HACKFAIL};
             }
             break;
 
         case GETMESSAGES:
             if(session instanceof XMPPSession) {
                 retArray = new Object[]{((XMPPSession)session).getMessages()};
             } else {
                 retArray = new Object[]{HACKFAIL};
             }
             break;
 
         case GETFRIENDS:
             if(session instanceof XMPPSession) {
                 List<User> friends = ((XMPPSession)session).getFriendList();
                 retArray = new Object[]{friends};
             }
             break;
 
         case DISCONNECT:
             if(helper.removeUserSessions(uchatUsername, accountType)) {
                 retArray = new Object[]{SUCCESS};
             } else {
                 retArray = new Object[]{"Failed to close session"};
             }
             
             break;
         default:
             break;
         }
         return retArray;
     }
 
     private boolean validateUChatSession(HashMap<String, Object> args) {
         HashMap<String, Object> validateArgs = new HashMap<String, Object>();
         validateArgs.put("Username", args.get(ucUsername));
         validateArgs.put("SessionId", args.get(ucSessionId));
         validateArgs.put("Operation", Operation.VALIDATE);
         validateArgs.put("AccountType", AccountType.UCHAT);
         Object[] validate = this.doOperation(validateArgs);
         if(validate != null && validate.length > 0) {
             if(validate[0].toString().equals(SUCCESS)) {
                 return true;
             }
         }
         return false;
 
     }
 
 }
