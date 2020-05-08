 
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class PatternTest {
 
   private static final Pattern MASTER = 
      Pattern.compile("(.*?) \\[([-A-Za-z ]+)\\] \\(([A-Z\\\\/ ]+)\\) - (.*)", Pattern.DOTALL);
   
   public static void main(String[] args) {
    doTest("MORRISTOWN BEARD SCHOOL (22), 70 WHIPPANY RD [Morris Twp] (STROKE\\CVA) - E2368,F22DUTY,E8002\nAT FRONT OF CAMPUS; POSS MALE WITH STROKE\n1st");
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
