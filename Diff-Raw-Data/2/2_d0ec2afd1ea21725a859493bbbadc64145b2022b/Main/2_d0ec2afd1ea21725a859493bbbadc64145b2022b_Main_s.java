 package namnam.twitterer;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import namnam.parser.NamNamParser;
import namnam.parser.NamNamParserIN;
 import winterwell.jtwitter.Twitter;
 
 /*
  * @author fake
  */
 public class Main {
 
     private static NamNamParser myParser;
     private static DecimalFormat nf;
 
     private static final boolean doTwitter = false;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         myParser = new NamNamParserIN();
         nf = new DecimalFormat();
         nf.setMinimumFractionDigits(2);
         DecimalFormatSymbols foo = new DecimalFormatSymbols();
         foo.setDecimalSeparator(',');
         nf.setDecimalFormatSymbols(foo);
 
         Twitter twitter = new Twitter("hsin_mensa","toppas123");
 
         try {
             Map<Date,Map<String,Integer[]>> result = myParser.getCurrentMenues();
 
             Calendar now = Calendar.getInstance();
             now.set(Calendar.HOUR_OF_DAY, 0);
             now.set(Calendar.MINUTE, 0);
             now.set(Calendar.SECOND, 0);
             now.set(Calendar.MILLISECOND, 0);
 
             Map<String,Integer[]> heid = result.get(now.getTime());
             Iterator<String> hIt = heid.keySet().iterator();
             while(hIt.hasNext()) {
                 String desc = hIt.next();
                 Integer[] preis = heid.get(desc);
                 // 0 = stud
                 // 1 = bed
 
                 String up = ", " + nf.format(preis[0]/100.0) +
                         " bzw. " + nf.format(preis[1]/100.0) + " EUR" ;
 
                 if((up.length() + desc.length()) > 140) {
                     int toomuch = 140 - (up.length() + desc.length());
                     up = desc.substring(0,toomuch-3) + "..."+ up;
                 } else {
                     up = desc + up;
                 }
 
                 if(doTwitter)
                     twitter.updateStatus(new String(up.getBytes("ISO-8859-15"),"ISO-8859-15"));
                 
                 System.out.println(new String(up.getBytes("ISO-8859-15"),"ISO-8859-15"));
             }
         } catch (Exception ex) {
             System.err.println("Error twittering menues for today: " + ex);
             if(doTwitter)
                 twitter.updateStatus("Sorry, es gibt ein Problem beim Abruf des Menues!");
         }
 
     }
 
 }
