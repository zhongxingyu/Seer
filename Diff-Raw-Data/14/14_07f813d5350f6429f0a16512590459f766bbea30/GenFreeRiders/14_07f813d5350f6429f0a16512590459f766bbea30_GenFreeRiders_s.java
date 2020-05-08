 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.donation.UserAcctManager;
 
 /**
  * This class converts the input file freeriders.txt to the freeriders.xml
  * resource file.  The class exists in the test project, but must be run in
  * the live cadpage workspace
  */
 public class GenFreeRiders {
   
   private static final File IN_FILE = new File("freeriders.txt");
   private static final File OUT_FILE = new File("./res/values/freeriders.xml");
   
   
 
   public static void main(String[] args) throws IOException {
     
     // Open input and output files
     BufferedReader ir = new BufferedReader(new InputStreamReader(new FileInputStream(IN_FILE)));
     PrintStream ps = new PrintStream(new FileOutputStream(OUT_FILE));
     
     // Write appropriate output XML headings
     ps.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
     ps.println("<resources>");
     
     String listName = null;
     List<String> userList = new ArrayList<String>(); 
     while (true) {
       String line = ir.readLine();
       if (line == null) break;
       int pt = line.indexOf("//");
       if (pt >= 0) line = line.substring(0,pt);
       line = line.trim();
       if (line.length() == 0) continue;
       if (line.startsWith("$")) {
         writeList(listName, userList, ps);
         listName = line.substring(1).trim();
         userList.clear();
       }
       
       else {
         userList.add(cvtUser(line));
       }
     }
     
     writeList(listName, userList, ps);
     
     ps.println("</resources>");
     ir.close();
     ps.close();
     
     System.out.println("freeriders.xml has been generated");
   }
 
   /**
    * Convert user specification line to an MD5 has XML item
    * @param line
    */
   private static final Pattern OPEN_BRACKET = Pattern.compile("[<({\\[]");
   private static String cvtUser(String line) {
     try {
       // parse name and user account from line
       String user = line;
       String name = null;
       Matcher match = OPEN_BRACKET.matcher(line);
       if (match.find()) {
         int pt1 = match.start();
         char delim = line.charAt(pt1);
         delim = (delim == '(' ? ')' : delim == '<' ? '>' : delim == '{' ? '}' : ']' );
         int pt2 = line.indexOf(delim,pt1+1);
         if (pt2 < 0) throw new RuntimeException("Missing closing " + delim);
         user = line.substring(pt1+1,pt2).trim();
         name = line.substring(0,pt1).trim();
       }
       
       // Final validity check
      if (user.length() == 0 || user.contains(" ") || ! user.contains("@")) {
         throw new RuntimeException("Malformed user account");
       }
       
       // Looks good
       String result = "<item>" + UserAcctManager.calcHash(user) + "</item>";
       if (name != null && name.length() > 0) {
         result += "  " + "<!-- " + name + " -->";
       }
       return result;
     } catch (RuntimeException ex) {
       throw new RuntimeException(ex.getMessage() + " in line: " + line, ex);
     }
   }
 
   /**
    * Write accumulated user list to output file
    * @param listName name of user list
    * @param userList list of users
    * @param ps output stream
    */
   private static void writeList(String listName, List<String> userList,
                                 PrintStream ps) {
     if (listName == null) {
       if (userList.size() == 0) return;
       throw new RuntimeException("Missing initial list name");
     }
     ps.println("  <string-array name=\"" + listName + "\">");
     
     String[] users = userList.toArray(new String[userList.size()]);
     Arrays.sort(users);
     for (String user : users) {
       ps.println("    " + user);
     }
     
     ps.println("  </string-array>");
   }
 }
