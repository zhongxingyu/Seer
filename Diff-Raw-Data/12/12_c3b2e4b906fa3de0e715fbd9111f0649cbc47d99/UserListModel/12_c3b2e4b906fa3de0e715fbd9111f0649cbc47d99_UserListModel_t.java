 package SeljeIRC;
 
 import javax.swing.DefaultListModel;
 
 /**
  * Model for the Channel's user list
  * @author Lars Erik Pedersen
  * @version 0.1
  * @since 0.1
  */
 public class UserListModel extends DefaultListModel {
     
     /**
      * Adds a regular user to the user list
      * @author Lars Erik Pedersen
      * @since 0.1
      * @param s Nick which shall be added to the user list
      */
     public void addUserToList(String s)   {
         User newUser = new User(s, false, false);
         if (size() == 0)   {                                // Empty list
             super.addElement(newUser);                      // Add the user first
         }
         else   {
             insert(newUser);                                // Insert user
         }
         fireContentsChanged(this, 0, size());
     }
     
     /**
      * Removes a user from the list
      * @author Lars Erik Pedersen
      * @since 0.1
      * @param s Nick that shall be removed
      */
     public void removeUser(String s)   {
         User tmp = getUser(s);              // Gets the user object with given nick
         super.removeElement(tmp);           // Remove it
         fireContentsChanged(this, 0, size());
     }
     
     /**
      * Ops or deops a user, and re-arrange the user list
      * @author Lars Erik Pedersen
      * @since 0.1
      * @param s Nick to be oped/deoped
      * @param o op or deop
      */
     public void op(String s, boolean o)   {                 // Will allways be called after the complete userlist is initialized
         User tmp = getUser(s);                              // Fetch user with nickname s
         if (tmp != null)   {                                // User is in list
             tmp.setOp(o);                                   // Set the op mode7
             removeElement(tmp);                             // Delete it from list
             insert(tmp);                                    // Insert it on its new place
             fireContentsChanged(this, 0, size());
         }
        else System.out.println("User " + s + " not in list");
         
     }
     
     /**
      * Voices or devoices a user, and re-arrange the user list.
      * @author Lars Erik Pedersen
      * @since 0.1
      * @param s Nick to be voiced/devoiced
      * @param v voice or devoice
      */
     public void voice(String s, boolean v)   {              // As above
         User tmp = getUser(s);
         if (tmp != null)   {
             tmp.setVoice(v);
             removeElement(tmp);
             insert(tmp);
             fireContentsChanged(this, 0, size());
         }
        else System.out.println("User " + s + " not in list");
     }
     
     /**
      * Inserts a user in the user list, and sorts it ascending
      * @author Lars Erik Pedersen
      * @since 0.1
      * @param n User to insert
      */
     private void insert(User n)   {
         int i;
         for (i = 0; i < size(); i++)   {            // Iterate through all users
             User userInList = (User)elementAt(i);       // Pick user
             if (userInList.compareTo(n) <= 0)   {       // Continue until current user is "bigger" than the new user
                 continue;
             }
             insertElementAt(n, i);                      // InserT the new user where the current user was
             return;                                     // No need for more searching after user is added
         }
         super.addElement(n);                            // New user was the "biggest" insert at the bottom
     }
     
     /**
      * Get the user object from a given nickname
      * @param s Nickname to be searched for
      * @return The User object with nickname s, or null if not found
      * @author Lars Erik Pedersen
      * @since 0.1
      */
     private User getUser(String s)   {
         User tmp = new User(s, false, false);       // Create a User object with give nick, to fetch from list
         int i = indexOf(tmp);                       // Gets the index of that user
         if (i != -1 ) return (User)elementAt(i);    // The user is in the list
         else return null;                           // The user is not in the list
     }
 
     /**
      * Inner class. Defines a User within the channel
      * @author Lars Erik Pedersen
      * @since 0.1
      * @version 0.1
      */
     class User implements Comparable<User>   {
         String nick;
         boolean op;
         boolean voice;
         
         /**
          * Constructor, creates a user with nickname av channel modes
          * @author Lars Erik Pedersen
          * @since 0.1
          * @param n Nickname
          * @param o Operator
          * @param v Voiced
          */
         public User(String n, boolean o, boolean v)   {
             nick = n;
             op = o;
             voice = v;
         }
         
         /**
          * Sets operator mode
          * @author Lars Erik Pedersen
          * @since 0.1
          * @param o True = Op, False = deop
          */
         public void setOp(boolean o)   {
             op = o;
         }
         
         /**
          * Set voic mode
          * @author Lars Erik Pedersen
          * @since 0.1
          * @param v True = Voice, Flase = devoice
          */
         public void setVoice(boolean v)   {
             voice = v;
         }
         
         /**
          * Set the users nick
          * @author Lars Erik Pedersen
          * @since 0.1
          * @param n 
          */
         public void setNick(String n)   {
             nick = n;
         }
         
         /**
          * Compares a User to another User. Used for sorting in the channels user list. Op shall be listed alphabeticly on the top of the list,
          * followed by voiced users, then a alphabetic list of normal users.
          * @author Lars Erik Pedersen
          * @param u The User to compare this User object with
          * @return A numeric value. Negative means that this object is "smaller" the the parameter. Positive means that this object is "larger" than the parameter
          * @since 0.1
          */
         @Override
         public int compareTo(User u) {
             if (op && !u.op)
                 return -1;
             if (!op && u.op)
                 return 1;
             if (voice && !u.voice)
                 return -1;
             if (!voice && u.voice)
                 return 1;
             return nick.compareToIgnoreCase(u.nick);
         }
         
         /**
          * Checks if to Users are equal. Uses the nickname, because that will be uniqe for every User.
          * @author Lars Erik Pedersen
          * @param o Either a User or a String.
          * @return True if the Users are equal, false if they're not.
          * @since 0.1
          */
         @Override
         public boolean equals(Object o)   {
             if (o instanceof User)   {                  // Compared with another User object
                 return nick.equals(((User)o).nick);
             }
             else if (o instanceof String) {             // Compare with a String
                 return nick.equals((String)o);
             }  
             else return false;                          // Compared with an illegal object
         }
         
         /**
          * Returns the literal User
          * @author Lars Erik Pedersen
          * @since 0.1
          * @return Username with op or voice prefixed
          */
         @Override
         public String toString()   {
             StringBuilder s = new StringBuilder();
             s.append( (op) ? "@" : "" );
             s.append( (voice && !op) ? "+" : "" );
             s.append(nick);
             return s.toString();
         }
     }
 }
