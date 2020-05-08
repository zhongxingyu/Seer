 package chb.test;
 
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 public class Main {
 
     public static void main(String[] args) {
         SimpleEmail email = new SimpleEmail();
 
         try{

        email.setHostName("smtp.163.com");
        email.addTo("bulktree@126.com","bulktree");
        email.setFrom("bulktree@163.com", "bulktree");
        email.setAuthentication("bulktree", "123456");
        email.setSubject("Hello, This is My First Email Application");
        email.setMsg("I am bulktree This is JavaMail Application");
        email.send();
        System.out.println("The SimpleEmail send sucessful!!!");
        } catch (EmailException e) {
             e.printStackTrace();
         }
     }
 }
