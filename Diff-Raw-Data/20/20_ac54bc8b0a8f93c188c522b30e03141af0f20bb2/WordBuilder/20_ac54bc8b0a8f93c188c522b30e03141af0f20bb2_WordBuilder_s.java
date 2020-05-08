 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wordbuilder;
 
 
 import database.DatabaseOperations;
 import java.io.File;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.TreeSet;
 
 /**
  *
  * @author Mohit
  */
 public class WordBuilder {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
         // TODO code application logic here
       //  Properties props = System.getProperties();
      //  props.setProperty("wordnet.database.dir", "wordnet/dict"); 
         System.setProperty("wordnet.database.dir","wordnet/dict");
        //System.setProperty("wordnet.database.dir", "C:/Program Files (x86)/WordNet/2.1/dict");
    //     String a = System.getProperty("user.dir");
      //   a+="/WordBuilder.jar/wordnet/dict/";
       //  System.setProperty("wordnet.database.dir", a);
         //ApiFetch.main(args);
         //ApiFetch.getMeaning(" ","help","q");
         //MainPage.main(args);
         //ApiFetch.getHypernym("");
         //ApiFetch.getHyponym("");
         // MainPage.main(args);
         // BaseIcon.set();
        // PopUp.main(args);
      //   Signup.main(args);
       //  System.out.println(getCurrentUser());
        // PopUp a =new PopUp();
         
         
      /*   Scanner sc = new Scanner(System.in);
         String input = sc.nextLine();
         //a.show(input);
         Notify.showNotification(input);
         MainPage.main(args);
         */
<<<<<<< HEAD
 


/*
 * Chandra's code....to be added later (removed for testing
        BackGround a = new BackGround();
  */

        //Database speed increased.....
        DatabaseOperations.init();

        Signin frame=new Signin();
         frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

=======
                 new Thread() 
                 {
                 @Override
                 public void run() {
                     try {
                         BackGround a = new BackGround();
                     } catch (Exception e) {
                     }
                 }
         
                 }
                         .start();
                 
                 
                 
         //Signin in = new Signin();
         //in.setVisible(true);
         ab = new MainPage();
        ab.setVisible(true);
        bi = new BaseIcon();
         Options op = new Options();
>>>>>>> word_cravip
      
        
     }
     
     
     public static String getCurrentUser()
     {
         return currentU;
     }
     public static void setCurrentUser(String user)
     {
         currentU = user;
     }
     public static MainPage getMainpage()
     {
         return ab;
     }
     
     private static String currentU;
     private static MainPage ab;
     private static BaseIcon bi ;
     
 }
