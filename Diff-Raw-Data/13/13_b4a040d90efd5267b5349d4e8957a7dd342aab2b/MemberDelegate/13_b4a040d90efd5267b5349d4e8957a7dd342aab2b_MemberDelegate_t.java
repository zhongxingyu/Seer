 package net.jlj.board.delegate;
 
 import java.util.List;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 import org.apache.openjpa.persistence.NoResultException;
 
 import net.jlj.board.BoardApp;
 import net.jlj.board.dom.Board;
 import net.jlj.board.dom.Event;
 import net.jlj.board.dom.Link;
 import net.jlj.board.dom.Member;
 import net.jlj.board.dom.Message;
 
 public class MemberDelegate extends AbstractDelegate
 {
    private static Logger log = Logger.getLogger (MemberDelegate.class.getName ());
    
    public MemberDelegate (EntityManagerFactory emf)
    {
       super(emf);
    }
    
    @SuppressWarnings("unchecked")
    public List<Member> getAdmins ()
    {
       Query q = createNamedQuery("admins");
       List<Member> results = q.getResultList();
 
       return results;
    }
   
   public Member getUser (String name)
   {
      Member member = getUserByName (name.trim ());
      if (member == null && name.matches ("[\\w\\d][-_.\\w]+@([\\w-]+\\.)+[\\w-]+"))
      {
         member = getUserByEmail (name.trim());
      }
      return member;
   }
    /**
     * Find and return a single user with exact match.
     * @param user
     * @return
     */
   public Member getUserByName (String user)
    {
       Query q = createNamedQuery("user");
       q.setParameter(1, user);
       Member result = null;
       try
       {
          result = (Member) q.getSingleResult ();
       }
       catch (NoResultException   e)
       {
          // This is expected sometimes, but check for empty database
          if ("guest".equals (user))
          {
             log.debug ("Checking to see if the data base is empty...");
             List<Member> list = getMembers ("%");
             if (list.isEmpty ())
             {
                log.debug ("   ...OK, the data base is empty. Initialising now.");
                initialiseDatabase ();
                list = getMembers("%"); /* NB this will only return admin; guest and root are hidden */
                if (list.size() > 0) // prevent loop
                {
                   result = getUser (user);
                }
             }
          }
       }
 
       return result;
    }
    /**
     * Look for a user with the given email address.
     * @param user
     * @return
     */
    @SuppressWarnings ("unchecked")
    public Member getUserByEmail (String email)
    {
       Query q = createNamedQuery("user_by_email");
       q.setParameter(1, email);
       Member result = null;
       List<Member> list = q.getResultList ();
       if (list.size () > 0)
       {
          result = list.get (0); // email is unique, so at most one result
       }
       return result;
    }
    @SuppressWarnings("unchecked")
    public List<Member> getMembers (String filter)
    {
       Query q = createNamedQuery("all_users");
       q.setParameter(1, filter);
       List<Member> results = q.getResultList ();
       return results;
    }
    public Member getValidatedUser (String user, String password)
    {
       Member member = getUser (user);
       return member != null && member.checkPassword (password) ? member : null;
    }
    public boolean validateUser (String user, String password)
    {
       Member member = getUser (user);
       return member != null && member.checkPassword (password);
    }
    
    public int getNumberOfPosts (String user)
    {
       Query q = createNamedQuery("num_posts_for_user");
       q.setParameter (1, user);
       Number result = (Number) q.getSingleResult ();
       return result.intValue ();
    }
 
    public void addLink (Member member, Link link)
    {
       beginTx ();
       persist (link);
       member.addLink (link);
    }
    public void removeLink (Member member, Link link)
    {
       beginTx ();
       member.removeLink (link);
    }
    private static final String ROOT = "root";   /* Do not change this */
    private static final String GUEST = "guest";  /* Do not change this */
    private static final String ADMIN_NAME = "admin";   /* Do not change this */
    private static final String ROOT_PASSWORD  = "qCew4lAGe5nFzm1bKBtAOgGMfErzFj9reMw+nzsGRDU=";
    private static final String ADMIN_PASSWORD = "Zq2t2jHbIpNl3YQBrtyo1awZGLQpoTIvg03O8Mok9KQ="; 
    private static final String GUEST_PASSWORD = "yTQragNe4STclJsGoH7NbZ6fmyP0fEdSnIt4pEwkyCg=";
    
    public void initialiseDatabase ()
    {
       beginTx ();
       
       Member root = new Member ();
       root.setName (ROOT);
       root.setPassword (ROOT_PASSWORD);
       root.setFirst ("Board");
       root.setLast ("System");
       root.setDescription ("System user");
       root.setState ("unregistered"); /* root is never user from the UI, so is unregistered */
       root.setGroup ("system");       /* the only member of the system group */
       root.setEmail ("board.system@local");
       root.setCreatedBy ("root");
       root.setLocked (true);
       persist (root);
       
       Member admin = new Member ();
       admin.setName (ADMIN_NAME);
       admin.setPassword (ADMIN_PASSWORD);
       admin.setFirst ("Board");
       admin.setLast ("Admin");
       admin.setDescription ("Board administrator");
       admin.setState ("registered");
       admin.setGroup ("admin");
       admin.setCreatedBy ("root");
       admin.setEmail ("board.admin@local"); /* this will be changed after initial login */
       persist (admin);
       
       Member guest = new Member ();
       guest.setName ("guest");
       guest.setPassword (GUEST_PASSWORD);
       guest.setFirst ("Guest");
       guest.setLast ("User");
       guest.setDescription ("Guest user");
       guest.setState ("unregistered");
       guest.setGroup ("guest");
       guest.setLocked (true);
       guest.setCreatedBy ("root");
       guest.setEmail ("guest.user@local");
       persist (guest);
       
       /* Do this here for convenience */
       BoardDelegate bd = BoardApp.getComponent (BoardDelegate.class);
       if (bd.getBoard ("%") == null)
       {
          Board board = new Board ();
          board.setTitle ("main");
          board.setState ("active");
          board.setLocked (false);
          board.setDescription ("JBoard");
          board.setCreatedBy ("root");
          persist (board);
       }
       Message msg = new Message ("root", "Welcome to JBoard. Please [b]change your password[/b] right now! " +
       	                                "Once you have done that, create your Categories and Forums as required.");
       persist (msg);
       flush (); // ensure we get a valid oid
       Event event = new Event ();
       event.setCreatedBy ("root");
       event.setPrivateMessage (msg, admin);
       persist (event);
       commitAndKeepOpen ();
    }
 
 }
