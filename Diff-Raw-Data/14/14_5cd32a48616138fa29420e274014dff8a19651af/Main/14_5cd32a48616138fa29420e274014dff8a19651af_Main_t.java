 package chb.test;
 
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 public class Main {
 
     public static void main(String[] args) {
         SimpleEmail email = new SimpleEmail();
 
         try{
            String s = "\n\n\nABCDE\n\n";
            s = s.replace("\n", "");
            System.out.print(s);
        } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
