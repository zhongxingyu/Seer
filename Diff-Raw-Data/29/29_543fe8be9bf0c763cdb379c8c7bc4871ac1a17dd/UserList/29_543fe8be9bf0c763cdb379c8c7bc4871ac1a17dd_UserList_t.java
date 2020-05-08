 package common;
 
 import java.util.Comparator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * List of users in sorted order by username
  *
  * @author evan
  *
  */
 public class UserList extends TreeMap<String, User> {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * creates userlist from a csv payload
      *
      * @param csv
      * @return
      */
     public static UserList fromString(String csv) {
         String[] values = csv.split(",");
         assert values.length % 2 == 0;
 
         UserList out = new UserList();
         for (int i = 0; i < values.length; i++) {
             out.put(values[i], new User(values[i], UserState.fromCode(values[++i]), null, 0));
         }
         return out;
     }
 
     public UserList() {
         super();
     }
 
     public UserList(Comparator<? super String> comparator) {
         super(comparator);
     }
 
     public UserList(Map<? extends String, ? extends User> m) {
         super(m);
     }
 
     public UserList(SortedMap<String, ? extends User> s) {
         super(s);
     }
 
     /**
      * @param n
      * @param ip
      * @return true if there are n or more users online with the input ip address
      */
     public boolean moreThanNOnlineWithSameIp(int n, String ip) {
         int count = 0;
         for (User u : values()) {
             if (u.getIp().equals(ip)) {
                 count++;
             }
         }
         return count >= n;
     }
 
     /**
      * @return formatted representation for client console
      */
     public String toFormattedString() {
         StringBuilder s = new StringBuilder();
         for (User u : values()) {
            s.append(u.getUsername()).append(" ").append(u.getState().name()).append('\n');
         }
         s.append("EOL");
         return s.toString();
     }
 
     @Override
     public String toString() {
         //convert to payload representation
         StringBuilder s = new StringBuilder();
         boolean fst = true;
         for (User u : values()) {
             if (!fst || (fst = false))
                 s.append(",");
             s.append(u.getUsername()).append(",").append(u.getState().toString());
         }
         return s.toString();
     }
 }
