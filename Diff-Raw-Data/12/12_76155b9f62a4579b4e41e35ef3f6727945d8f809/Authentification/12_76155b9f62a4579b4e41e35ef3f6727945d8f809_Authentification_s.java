 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package serveur;
 
 import données.Joueur;
 import java.io.*;
 import java.net.Socket;
 import java.util.Scanner;
 
 /**
  *
  * @author Watre
  */
 public class Authentification implements Runnable {
 
     private Socket socket;
     //private PrintWriter out;
     private ObjectOutputStream out;
     //private BufferedReader in;
     private ObjectInputStream in;
     private String ip, login;
     private Thread t2;
 
     public Authentification(Socket s) {
         socket = s;
         ip = socket.getInetAddress().toString();
     }
 
     @Override
     public void run() {
 
         try {
             /*
              * in = new BufferedReader(new
              * InputStreamReader(socket.getInputStream())); out = new PrintWriter(socket.getOutputStream());
              */
 
             OutputStream os = socket.getOutputStream();
             out = new ObjectOutputStream(os);
 
             InputStream is = socket.getInputStream();
             in = new ObjectInputStream(is);
 
             /*
              * InputStreamReader isr=new InputStreamReader(ois); in=new BufferedReader(isr);
              */
 
 
             //String loginPass = in.readLine();
             String loginPass = in.readUTF();
 
             login = loginPass.split(" ")[0];
 
             if (!isValid(loginPass)) {
                out.writeUTF("erreur");
                 out.flush();
            } else {
                out.writeUTF("pret");
                 out.flush();
 
                 //Création du joueur
                 //Messager m=new Messager(in, out, login);
                 Joueur j = new Joueur(login, in, out);
                 //System.out.println(j.getLogin());
 
                 //Ajout du joueur à la liste des joueurs connectés
                 Serveur.addJoueur(j);
                 //System.out.println(Serveur.getJoueurs().size());
 
                 //Envoi du numéro du salon
                 /*out.writeInt(0);
                 out.flush();
 
                 /*
                  * FileOutputStream fos=new FileOutputStream("titus.txt");
                  * ObjectOutputStream oos= new ObjectOutputStream(socket.getOutputStream());
                  */
 
                 //Ajout du joueur dans le MétaHub
                 Salon hub = (Salon) Serveur.getSalons().get(0);
                 hub.addJoueur(j.getLogin());
             }
 
 
 
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static boolean isValid(String loginPass) {
 
         boolean connexion = false;
         try {
             Scanner sc = new Scanner(new File("comptes.txt"));
 
             while (sc.hasNext()) {
                 if (sc.nextLine().equals(loginPass)) {
                     connexion = true;
                     break;
                 }
             }
 
         } catch (FileNotFoundException e) {
             System.err.println("Le fichier n'existe pas !");
         }
         return connexion;
     }
 }
