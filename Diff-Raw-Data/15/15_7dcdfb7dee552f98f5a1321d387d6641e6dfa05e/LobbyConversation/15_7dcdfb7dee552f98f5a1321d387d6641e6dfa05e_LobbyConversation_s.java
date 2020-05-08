 /*
  * Copyright (c) 2009  Mel Nicholson.
  * All Rights Reserved.
  */
 
 package mel.server;
 
 import mel.common.User;
 import java.io.IOException;
 import mel.security.AccountManager;
 
 /**
  *
  * CLIENT-SIDE OPCODES
  *  inherited from basic
  *      C:text = Chat
  *      J:conType = Joined
  *      L:conName = Left (exited)
  *  local
  *      A:conName = Add Conversation
  * @author nicholson
  */
 public class LobbyConversation extends BasicServerConversation
 {
     // TODO show active conversations in refresh command
     private ServerConversationManager master;
 
     public LobbyConversation(ServerConversationManager master)
     {
         super("lobby");
         this.master = master;
         registerCommand('J', new JoinCommand());
         registerCommand('L', new LeaveCommand());
         registerCommand('N', new CreateSavedConversationCommand());
         registerCommand('U', new CreateUnsavedConversationCommand());
     }
 
     public class LeaveCommand extends AbstractCommand
     {
         public void execute(User user, String content)
         {       	
             // TODO implement leave
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
     
     public class JoinCommand extends AbstractCommand
     {
         public void execute(User user, String content)
         {
             ServerConversation c = null;
             try
             {
                 c = master.loadConversation(content);
             } catch (IOException ex)
             {
                 sendError(user, "Error Loading conversation: "+ex);
                 return;
             }
             if (c == null)
             {
                 sendError(user, "No such conversation");
             }
             String auth = c.requireAuth();
             if (auth != null)
             {
                 if (!AccountManager.getAccountManager().isAuthorized(user, auth))
                 {
                     sendError(user, "Permission Denied");
                     return;
                 }
             }
             c.join(user);
         }
     }
 
     public class CreateSavedConversationCommand extends AbstractCommand
     {
         /**
          * 
          * @param user
          * @param content Syntax: "GameName:RegisteredFactoryName"
          */
         public void execute(User user, String content)
         {
             createConversation(user, content, true);
         }
     }
     
     public class CreateUnsavedConversationCommand extends AbstractCommand
     {
         /**
          * 
          * @param user
          * @param content Syntax: "GameName:RegisteredFactoryName"
          */
         public void execute(User user, String content)
         {
             createConversation(user, content, false);
         }
     }
 
     @Override
     public String requireAuth()
     {
         return null;
     }
 
     private void createConversation(User user, String content, boolean saved)
     {
         String args[] = content.split(":", 2);
         if(args.length != 2)
         {
             sendError(user, "Syntax Error");
             return;
         }
 
         ConversationFactory f = master.getConversationFactory(args[1]);
         if(f==null)
         {
             sendError(user, "Unknown Conversation type");
             return;
         }
         String auth = f.requireAuth();
         if (!AccountManager.getAccountManager().isAuthorized(user, auth))
         {
             sendError(user, "Permission Denied");
             return;
         }
 
         try
         {
             master.createConversation(args[0], args[1], saved);
             sendAll(User.MODERATOR, 'A', args[0]+":"+args[1]);
         } catch (IOException ex)
         {
             sendError(user, ex.getMessage());
         }   
     }
     @Override
     public String getType() { return "Lobby"; }
 }
