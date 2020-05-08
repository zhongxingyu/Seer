 package fr.adhoc.leboncoin.app;
 import java.sql.*;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.util.*;
import java.utils.DbUtils;
 import fr.adhoc.leboncoin.dao.impl.UtilisateurDaoImpl;

 
 public class LeboncoinTest {
 
 
     private static DbUtils myDbUtils;
 
 
 	public LeboncoinTest() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		// TODO Auto-generated method stub
 		Statement stmt;
 		ResultSet rslt = null;
         myDbUtils = new DbUtils();
 		
         stmt = myDbUtils.getStatement();//Cree un stmt pour la bd correspondant a la connexion conn
 
         
         String str="";
         Scanner user_input = new Scanner( System.in );
  /*      
 //Ajout d'une nouvelle offre
         System.out.println("Ajout d'une offre");
         //user_input = new Scanner( System.in );
         
         // Fixe la date
         Date date = new Date();
         SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
         ft.format(date);
         // Fixe le statut
         String statut = "Attente";
         //Fixe le nom
         System.out.print("Votre nom : ");//Pour l'instant on ne teste pas si il appartient a la BD
         String nom = user_input.next( );
 
         // Fixele produit
         System.out.print("Quel produit voulez vous acheter ? ");//Pour l'instant on ne teste pas si il appartient a la BD
         String produit = user_input.next( );
 
         // Fixe le montant
         System.out.print("A combien ? ");
         double montant = user_input.nextDouble( );
         
        //O_ID est auto-increment
         
         // Recherche l'U_ID du nom entre   
         rslt = stmt.executeQuery("SELECT * FROM Utilisateur WHERE nom='"+nom+"'");
         int Acht_ID=0;
         while  (rslt.next()){
         	Acht_ID = rslt.getInt("U_ID");
         }
         // Recherche leP_ID du produit
         rslt = stmt.executeQuery("SELECT * FROM Produit WHERE nom='"+produit+"'");
         int Produit_ID=0;
         while  (rslt.next()){
         	Produit_ID = rslt.getInt("P_ID");
         }
           
         str = "INSERT INTO Offre (MONTANT,DATE,STATUT,A_ID,P_ID) VALUES ("+ 
         								montant + ",'" + 
         								ft.format(date) + "','" + 
         								statut + "'," + 
         								Acht_ID + "," + 
         								(Produit_ID) + ")";
         stmt.execute(str);
     
 */
 //Ajout d'un nouvel utilisateur
         System.out.println("Ajout d'un utilisateur");
         //user_input = new Scanner( System.in );
         //Fixe le nom
         System.out.print("Votre nom : ");//Pour l'instant on ne teste pas si il appartient a la BD
         String U_nom = user_input.next( );
         
         //Fixe le mail
         System.out.print("Votre mail : ");//Pour l'instant on ne teste pas si il appartient a la BD
         String mail = user_input.next( );
 
         UtilisateurDao myUtDao = new UtilisateurDaoImpl();
         
         myUtDao.create(U_nom,mail);
 /*
         // Fixe la note
         float note = (float) U_nom.charAt(0) - (float) 'A' +1;
         
         // U_ID est auto-increment
         
         str = "INSERT INTO Utilisateur (NOM,MAIL,NOTE) VALUES ('" + 
         								U_nom + "','" + 
         								mail + "'," + 
         								note + ")";
         stmt.execute(str);
 
 */       
 /*        
 //Ajout d'un nouveau produit
         System.out.println("Ajout d'un produit");
         //user_input = new Scanner( System.in );
         
         //Fixe le nom
         System.out.print("Votre nom : ");//Pour l'instant on ne teste pas si il appartient a la BD
         String V_nom = user_input.next( );
         
         //Fixe le nom
         System.out.print("Nom du produit : ");
         String P_nom = user_input.next( );
         
         //Fixe la description
         System.out.print("Description du produit : ");
         user_input.nextLine( );
         String description = user_input.nextLine( );
 
         System.out.print("A combien ? ");
         double prixDepart = user_input.nextDouble( );
      
         // Le P_ID est auto-incrment
         
         // Recherche l'U_ID du nom entre   
         rslt = stmt.executeQuery("SELECT * FROM Utilisateur WHERE nom='"+V_nom+"'");
         int Vend_ID=0;
         while  (rslt.next()){
         	Vend_ID = rslt.getInt("U_ID");
         }
         
         
         str = "INSERT INTO Produit (NOM,PRIXDEPART,DESCRIPTION,V_ID) VALUES ('" + 
         								P_nom + "'," + 
         								prixDepart + ",'" + 
         								description + "'," + 
         								Vend_ID + ")";
         stmt.execute(str);  
 
 */
  
 
 
         
  //Affichage des Utilisateurs :
         rslt = stmt.executeQuery("SELECT * FROM Utilisateur");
     	System.out.println("ID\tnom\t\tmail\t\t\tnote");
     	for(int i=0;i<60;i++){
     		System.out.print("-");
     	}
     	System.out.println();
     	while  (rslt.next()){
     			
         	System.out.print(rslt.getInt("U_ID") + "\t");
         	System.out.print(rslt.getString("nom"));
         	
         	for (int i = 2-(rslt.getString("nom").length()/8);i>0 ;i--){
         		System.out.print("\t");
         	}
 
         	System.out.print(rslt.getString("mail"));
         	
         	for (int i = 3-(rslt.getString("mail").length()/8);i>0 ;i--){
         			System.out.print("\t");
         	}
         	System.out.print(rslt.getFloat("note")+"\n");
         }
         
     	
     	System.out.println();
 /*        
 //Affichage des Offres :
         rslt = stmt.executeQuery("SELECT * FROM Offre");
     	System.out.println("ID\tmontant\tdate\t\tstatut\t\tA_ID\tP_ID");
     	for(int i=0;i<80;i++){
     		System.out.print("-");
     	}
     	System.out.println();
 
     	while  (rslt.next()){
     			
         	System.out.print(rslt.getInt("O_ID") + "\t");
         	System.out.print(rslt.getDouble("montant") + "\t");
         	System.out.print(rslt.getString("date") + "\t");
         	System.out.print(rslt.getString("statut") + "\t");
         	for (int i = 1-(rslt.getString("statut").length()/8);i>0 ;i--){
     			System.out.print("\t");
         	}
         	System.out.print(rslt.getString("A_ID") + "\t");
         	System.out.print(rslt.getString("P_ID") + "\n");
 
         } 
     	
      	System.out.println();
      	
 //Affichage des Produits :
         rslt = stmt.executeQuery("SELECT * FROM Produit");
     	System.out.println("ID\tnom\t\tprixDepart\tdescription\t\t\t\t\tV_ID");
     	for(int i=0;i<100;i++){
     		System.out.print("-");
     	}
     	System.out.println();
     	while  (rslt.next()){
     			
         	System.out.print(rslt.getInt("P_ID") + "\t");
         	System.out.print(rslt.getString("nom"));
         	
         	for (int i = 2-(rslt.getString("nom").length()/8);i>0 ;i--){
         		System.out.print("\t");
         	}
 
         	System.out.print(rslt.getString("prixDepart") + "\t\t");
         	
         	System.out.print(rslt.getString("description"));
         	
         	for (int i = 6-(rslt.getString("description").length()/8);i>0 ;i--){
         			System.out.print("\t");
         	}
         	System.out.print(rslt.getInt("V_ID")+"\n");
         }
         
     	
     */	
     	
     	
         
         myDbUtils.getConnection().close();
 	}
 
 }
