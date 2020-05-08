 package issue_30;

 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Issue_30 {
     public static void main(String[] args) {
         Date now = new Date();
        System.out.println(now);
         DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
         String s = formatter.format(now);
        System.out.println(s);
     }
}
