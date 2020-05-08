 import java.io.InputStream;
 import java.net.URL;
 import java.util.Scanner;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 /**
  *
  * @author flo
  * Programm sucht nach neuen Ticketangeboten im entsprechenden
  * Thread im FA-Board
  */
 public class BVBTickets extends JFrame {
 
     public static String actPost = ""; //letzter Post
     public static int zaehler = 0; //Anzahl der Durchläufe
 
     public static void main(String args[]) {
         InputStream sourceCode = null;
         int time = 15000;
         //Erster Parameter gibt die URL des entsprechenden
         //Threads an
        if (args.length != 1) {
             System.err.println("Funktionsweise: java BVBTickets URLDesEntsprechendenThreads [ZeitInSecZwischenZweiAnfragen]");
         } else {
             try {
                 if(args.length == 2){
                     time = Integer.parseInt(args[1])*1000;
                 }
                 while (true) {
                     URL threadURL = new URL(args[0]);
                     sourceCode = threadURL.openStream();
                     getTickets(sourceCode);
                     Thread.sleep(time);
                 }
             } catch (Exception ex) {
                 //System.out.println(ex.getMessage());
                 System.err.println("Bitte gib eine gueltige URL an.");
                 // System.exit(-1);
             } finally {
                 /*if (sourceCode != null) {
                 try {
                 sourceCode.close();
                 } catch (IOException e) {
                 }
                 }*/
             }
         }
     }
 
     private static void getTickets(InputStream sourceCode) throws InterruptedException {
         
 
         Scanner sc = new Scanner(sourceCode);
         String s;
         String x = "";
 
         java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
 
         try {
             while ((s = sc.nextLine()) != null) {
                 if (s.matches("^(.*)\"threadline\"(.*)")) {
                     x += s;
                     s = sc.nextLine();
                     while (!(s.matches("^(.*)<img src=\"images/bvb-design/posticon(.*)"))) {
                         x += s;
                         s = sc.nextLine();
                     }
                 }
             }
 
 
         } catch (Exception e) {
             //System.out.println(e.getMessage());
         } finally {
             //System.out.println(x);
             String[] posts = x.split("threadline");
 
             int start = posts[posts.length - 1].indexOf(" />") + 4;
             int end = posts[posts.length - 1].indexOf("</td>");
             String post = posts[posts.length - 1].substring(start, end);
             //System.out.println(post);
             zaehler++; //Zähler wird um eins erhöht.
             if (!(post.equals(actPost))) {
                 if (actPost.equals("")) {
                     actPost = post;
                     System.out.println("Keine neuen Posts. Zaehler: "+ zaehler);
                     
                 } else {
                     //Einer hat Tickets reingestellt!!
                     java.util.Date now = new java.util.Date();
                     System.out.println("Neuer Post. Zaehler: "+ zaehler +" Uhrzeit: "+ sdf.format(now));
                     JFrame frame = new JFrame("Neuer Ticket-Post!!");
                     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                     JLabel label = new JLabel("Hello World");
                     frame.getContentPane().add(label);
                     frame.setSize(400, 300);
                     frame.setAlwaysOnTop(true);
 
 
                     //frame.pack();
                     frame.setLocationRelativeTo(null);
                     frame.setVisible(true);
                     actPost = post;
                 }
 
             }else{
                 System.out.println("Keine neuen Posts. Zaehler: "+ zaehler);
             }
 
 
 
         }
 
     }
 }
