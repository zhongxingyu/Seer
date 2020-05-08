 /**
  * User: gj
  * Date: 08/01/2011
  * Time: 20:38
  *
  *
  *   Public Domain
  *
  */
 
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.SecureRandom;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class goBitches
 {
   private SecureRandom random;
 
   public goBitches()
   {
     try
     {
       random = SecureRandom.getInstance("SHA1PRNG");
     }
     catch (Exception e)
     {
     }
 
     if (null == random)
     {
       random = new SecureRandom();
       random.setSeed(System.currentTimeMillis() / 666);
     }
   }
 
   public String getTheBitch(String inurl, String agent)
   {
     String bitchesUrl;
 
    if (!inurl.matches("http://") || !inurl.matches("wrzuta.pl"))
     {
       return null;
     }
 
     try
     {
       String filename = inurl.split("/")[4];
       String victim = inurl.split("/")[2].replace(".wrzuta.pl", "");
 
       String requestUrl = "http://" + victim + ".wrzuta.pl/xml/plik/" + filename + "/wrzuta.pl/undefined/" + Math.abs(random.nextInt());
 
       URL url = new URL(requestUrl);
       URLConnection urlc = url.openConnection();
 
       urlc.setRequestProperty("User-Agent", agent);
       BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
 
       String line;
       String lines = "";
 
       // read all bloody lines
       while ((line = bufferedreader.readLine()) != null)
       {
         lines += line;
       }
 
       Matcher matcher = Pattern.compile("<fileId>(.*)</fileId>").matcher(lines);
 
       String slave = "";
 
       while (matcher.find())
       {
         slave = matcher.group().substring(17, matcher.group().length() - 12);
       }
 
       bitchesUrl = slave;
     }
     catch (Exception e)
     {
       System.err.println("buggered in getTheBitch sunshine, Reason: " + e.toString());
       e.printStackTrace();
       return null;
     }
 
     return bitchesUrl;
   }
 
 }
