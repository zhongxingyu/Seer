 package controllers;
 
 import models.Forum;
 import models.ForumThread;
 import models.User;
 import play.mvc.Controller;
 
 public class SamsaraController extends Controller
 {
   // Auth
 
   protected static void setUsername(String email)
   {
     session("email", email);
   }
 
   protected static String getUsername()
   {
     return session("email");
   }
 
   public static User getSessionUser()
   {
     String username = getUsername();
     if (username != null && User.exists(username))
     {
       return User.get(username);
     }
     return null;
   }
 
   // Access rights
 
   public static boolean isSessionLoggedIn()
   {
     return getSessionUser() != null;
   }
 
   public static boolean isSessionCrew()
   {
     User u = getSessionUser();
     return u != null && u.isCrew;
   }
 
   public static boolean isSessionCurator()
   {
     User u = getSessionUser();
     return u != null && u.isCurator;
   }
 
   public static boolean isSessionAdmin()
   {
     User u = getSessionUser();
     return u != null && u.isAdmin;
   }
 
   // News rights
 
   public static boolean canPostNews()
   {
     User u = getSessionUser();
     return u != null && (u.isCurator || u.isAdmin);
   }
 
   // Forum rights
 
   public static boolean canCreateForum()
   {
     User u = getSessionUser();
     return u != null && u.isAdmin;
   }
   
   public static boolean isForumOwner(Forum forum)
   {
     User u = getSessionUser();
     return u != null && u.userId == forum.owner.userId;
   }
 
   public static boolean canSeeForum(Forum forum)
   {
     User u = getSessionUser();
     if (forum.isCrewOnly)
     {
       return u != null && u.isCrew;
     }
     return true;
   }
 
   public static boolean canSeeScreenedPosts(ForumThread thread)
   {
     User u = getSessionUser();
     if (u == null)
     {
       return false;
     }
     // Mods can see screened posts, unless mod posts are also screened.
     if (!thread.moderatorPostsScreened && thread.parent.moderators.contains(u))
     {
       return true;
     }
     // Owner can always see screened posts.
     if (thread.parent.owner.userId == u.userId)
     {
       return true;
     }
     return false;
   }
 
   public static boolean canStartThread(Forum forum)
   {
     User u = getSessionUser();
     if (u == null)
     {
       return false;
     }
     if (forum.anyoneCanStartThreads)
     {
       return true;
     }
     if (forum.moderatorsCanStartThreads && forum.moderators.contains(u))
     {
       return true;
     }
     // Owner can always see screened posts.
     if (forum.owner.userId == u.userId)
     {
       return true;
     }
     return false; //u.isAdmin;
   }
 
   public static boolean canPostReply(ForumThread thread)
   {
     User u = getSessionUser();
     if (u == null)
     {
       return thread.unregisteredCanPost;
     }
     Forum forum = thread.parent;
     if (u.isCrew || !forum.isCrewOnly)
     {
       if (thread.nonModeratorsCanPost || forum.moderators.contains(u))
       {
         return true;
       }
     }
     // Owner can always see screened posts.
     if (forum.owner.userId == u.userId)
     {
       return true;
     }
     return false; //u.isAdmin;
   }
 
   // Form Helpers
 
   public static String nullForEmptyString(String value)
   {
     return (value == null || value.length() > 0) ? value : null;
   }

  public static String shortString(String value)
  {
    if (value.length() > 128)
    {
      return value.substring(0, 96) + "...";
    }
    return value;
  }
 }
