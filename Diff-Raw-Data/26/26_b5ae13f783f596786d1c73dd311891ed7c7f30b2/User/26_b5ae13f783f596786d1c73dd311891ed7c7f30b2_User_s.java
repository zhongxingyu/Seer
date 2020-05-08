 package in.haeg.csbot;
 
 import in.haeg.datastructures.SelectedList;
 
 import java.util.List;
 
 import org.schwering.irc.lib.IRCUser;
 
 public class User {
     private SelectedList<String> m_Nicknames;
     private String               m_Username;
     private String               m_Host;
     private int                  m_Karma = 0;
 
     public User(String a_Nickname) {
         m_Nicknames = new SelectedList<String>();
         m_Nicknames.add(a_Nickname);
         m_Nicknames.setSelected(a_Nickname);
     }
 
     public User(String a_Nickname, String a_Username, String a_Host) {
         m_Nicknames = new SelectedList<String>();
         m_Nicknames.add(a_Nickname);
         m_Nicknames.setSelected(a_Nickname);
         m_Username = a_Username;
         m_Host = a_Host;
     }
 
     public User(IRCUser a_IRCUser) {
         m_Nicknames = new SelectedList<String>();
         m_Nicknames.add(a_IRCUser.getNick());
         m_Nicknames.setSelected(a_IRCUser.getNick());
         m_Username = a_IRCUser.getUsername();
         m_Host = a_IRCUser.getHost();
     }
 
     public boolean hasNick(String a_Nick) {
         for (String nickname : m_Nicknames) {
             if (nickname.equals(a_Nick)) {
                 return true;
             }
         }
         return false;
     }
 
     public void addNick(String a_Nick) {
         m_Nicknames.add(a_Nick);
     }
 
     public void addLatestNick(String a_Nick) {
         m_Nicknames.add(a_Nick);
         m_Nicknames.setSelected(a_Nick);
     }
 
     public String getUsername() {
         return m_Username;
     }
 
     public String getHost() {
         return m_Host;
     }
 
     public String getNickname() {
         return m_Nicknames.getSelected();
     }
 
     public List<String> getNicknames() {
         return m_Nicknames;
     }
 
     public void setNicknames(SelectedList<String> a_Nicknames) {
         m_Nicknames = a_Nicknames;
     }
 
     public void setUsername(String a_Username) {
         m_Username = a_Username;
     }
 
     public void setHost(String a_Host) {
         m_Host = a_Host;
     }
 
     /* Karma */
     public Integer getKarma() {
         return m_Karma;
     }
 
     public void resetKarma() {
         m_Karma = 0;
     }
 
     public void incrementKarma() {
         m_Karma++;
     }
 
     public void decrementKarma() {
         m_Karma--;
     }
 
 }
