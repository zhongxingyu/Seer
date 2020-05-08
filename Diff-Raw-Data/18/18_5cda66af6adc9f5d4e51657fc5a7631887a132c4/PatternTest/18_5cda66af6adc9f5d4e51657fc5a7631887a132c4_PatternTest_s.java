 
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class PatternTest {
 
   private static final Pattern MASTER = 
      Pattern.compile("^P, *([A-Z]*[0-9]+)");
   
   public static void main(String[] args) {
    doTest("P,3627 E61 SQ60 816 ONE VEHICLE INTO GUARD RAIL. UNKNOWN INJURIES. UNKNOWN ENTRAPMENT.");
   }
   
   private static void doTest(String test) {
     Matcher match = MASTER.matcher(test);
     System.out.print(test);
     if (! match.find()) {
       System.out.print(" no match");
     } else {
       for (int g = 0; g<=match.groupCount(); g++) {
         System.out.print(" G" + g + ":" + match.group(g));
       }
     }
     System.out.println();
   }
 
 }
