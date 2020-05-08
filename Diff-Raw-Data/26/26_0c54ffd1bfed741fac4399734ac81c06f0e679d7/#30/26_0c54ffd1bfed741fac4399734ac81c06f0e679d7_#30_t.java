 package issue_30;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Issue_30 {
     public static void main(String[] args) {
		String day="",month="",year="",hour="",minute="",second="";
         Date now = new Date();
         DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
         String s = formatter.format(now);
        day+=s.charAt(0);
        day+=s.charAt(1);
        month+=s.charAt(3);
        month+=s.charAt(4);
        year+=s.charAt(6);
        year+=s.charAt(7);
        year+=s.charAt(8);
        year+=s.charAt(9);
        hour+=s.charAt(11);
        hour+=s.charAt(12);
        minute+=s.charAt(14);
        minute+=s.charAt(15);
        second+=s.charAt(17);
        second+=s.charAt(18);
        System.out.println("Current date is "+day+"/"+month+"/"+year+"\nCurrent time is "+hour+":"+minute+":"+second);
     }
}
