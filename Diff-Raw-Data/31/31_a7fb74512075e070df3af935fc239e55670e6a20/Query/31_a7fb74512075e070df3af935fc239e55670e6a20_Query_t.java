 package pascaldimassimo;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Query
 {
     private final String userID;
 
     private final String queryString;
 
     private final Date   timestamp;
 
     public Query(final String log)
     {
         String[] parts = log.split("\\t");
        if (parts.length < 3)
        {
            throw new IllegalArgumentException("Invalid query log");
        }
         this.userID = parts[0];
         this.queryString = parts[1];
         try
         {
             this.timestamp = parseTimestamp(parts[2]);
         }
         catch (ParseException e)
         {
             throw new IllegalArgumentException(e);
         }
     }
 
     private Date parseTimestamp(String timestamp) throws ParseException
     {
         String pattern = "yyyy-MM-dd HH:mm:ss";
         SimpleDateFormat format = new SimpleDateFormat(pattern);
         return format.parse(timestamp);
     }
 
     public String getUserID()
     {
         return userID;
     }
 
     public String getQueryString()
     {
         return queryString;
     }
 
     public Date getTimestamp()
     {
         return timestamp;
     }
 
     @Override
     public String toString()
     {
         return String.format("Query [userID=%s, queryString=%s, timestamp=%s]", userID, queryString, timestamp);
     }
 
    /**
     * TEST CODE!
     * 
     * @param args
     * @throws Exception
     */
     public static void main(String[] args) throws Exception
     {
         File file = new File("/Users/pascal/data/AOL-user-ct-collection/user-ct-test-collection-01.txt");
         BufferedReader br = new BufferedReader(new FileReader(file));
         String line;
        boolean skipHeader = true;
         while ((line = br.readLine()) != null)
         {
            if (skipHeader)
             {
                skipHeader = false;
                continue;
             }
            Query query = new Query(line);
            System.out.println(query);
         }
     }
 }
