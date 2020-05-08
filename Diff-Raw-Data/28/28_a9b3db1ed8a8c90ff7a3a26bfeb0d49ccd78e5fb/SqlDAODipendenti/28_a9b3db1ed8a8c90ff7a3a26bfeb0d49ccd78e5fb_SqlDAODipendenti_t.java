 /*
  * Name: SqlDAODipendenti.java
  * Package: com.safetygame.back.access
  * Author: Gabriele Facchin
  * Date: {Data di approvazione del file}
  * Version: 0.1
  * Copyright: see COPYRIGHT
  * 
  * Changes:
  * +----------+---------------------+---------------------
  * |   Date   | Programmer          | Changes
  * +----------+---------------------+---------------------
  * | 20120527 | Gabriele Facchin    | + SqlDAODipendenti
  * |          |                     | + getInfoD
  * |          |                     | + getInfoA
  * |          |                     | + resetPassD                
  * |          |                     | + resetPassA
  * |          |                     | + passD
  * |          |                     | + passA
  * |          |                     | + mailD
  * |          |                     | + elencoDipendenti
  * |          |                     | + aggiungiDipendente
  * |          |                     | + cancellaDipendente
  * |          |                     | + modNome
  * |          |                     | + modCognome
  * |          |                     | + modCodFis
  * |          |                     | + modUsername
  * |          |                     | + modImpiego
  * |          |                     | + setTrofei
  * |          |                     | + generaPassCasuale
  * |          |                     | + resetA
  * |          |                     | + resetD
  * +----------+---------------------|---------------------
  *
  */
 
 package com.safetyGame.back.access;
 import com.safetyGame.back.condivisi.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Classe che gestisce i dipendenti, implementa i metodi pubblici dell'interfaccia
  * 
  * @author gfacchin
  * @version 0.1
  */
 
 public class SqlDAODipendenti implements DAODipendenti{
   private Indirizzo serverAzienda;
   
   /**
    * Costruttore della classe SqlDAODipendenti
    * 
    */
   public SqlDAODipendenti(Indirizzo azienda){
     serverAzienda=azienda;
   }
   
   /**
    * Metodo che prende le informazioni di un Dipendente dal database
    * 
    * @param l Oggetto Login da cui si prendono le informazioni
    * @return l'oggetto Dipendente istanziato se presente nel db, altrimenti null
    * 
    */     
   public Dipendente getInfoD(Login l){
     String username = l.getUsername();
     String password = l.getPassword();
     ResultSet rs = serverAzienda.selezione("Dipendente as dip INNER JOIN Ruolo as r ON dip.ruolo=r.ruolo ","*", "nickname="+username,""); 
     String nome,cognome,codfis,email,ruolo,passmod;
     int ID,trofeo;
     try{
       ID = rs.getInt("ID");
       nome = rs.getString("nome");
       cognome = rs.getString("cognome");
       codfis = rs.getString("codice_fiscale");
       passmod = rs.getString("passmod");
       email = rs.getString("email");
       ruolo = rs.getString("ruolo");
       trofeo = rs.getInt("trofeo");
     }
     catch (SQLException e){return null;} 
    rs = serverAzienda.selezione("Storico","punteggio","IDDipendente="+ID,"");
    int punti=0;
    boolean finito = false;
    while(!finito){
      try{
        punti+= rs.getInt("punteggio");
        rs.next();
      }
      catch (SQLException e){finito = true;}
    }
    return new Dipendente(ID,codfis, nome,cognome,email,username,password,ruolo,punti, passmod, trofeo);
   }
   
   /**
    * Metodo che prende le informazioni di un Amministratore dal database
    * 
    * @param l Oggetto Login da cui si prendono le informazioni
    * @return l'oggetto Dipendente (amministratore) istanziato se presente nel db, altrimenti null
    * 
    */   
   public Dipendente getInfoA(Login l){
     String username = l.getUsername();
     String password = l.getPassword();
     ResultSet rs = serverAzienda.selezione("Amministratore","*", "nickname="+username,""); 
     int ID;
     String email,codfis,data,passmod;
     boolean amm;
     try{
       ID = rs.getInt("ID");
       email = rs.getString("email");
       data = rs.getString("datapass");
       passmod = rs.getString("passmod");
       codfis = rs.getString("codice_fiscale");
       amm=rs.getBoolean("tipo_amministratore");
     }
     catch (SQLException e){return null;}
    //return new Dipendente(amm, data, passmod, email, username, password, codfis, ID);
    return new Dipendente();
   }
   
   /**
    * Metodo che resetta il campo password modificata di un Dipendente
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean resetPassD(Dipendente d){
     return serverAzienda.modificaRiga("Dipendente", "passmod=''","ID="+d.getId());
   }
   
   /**
    * Metodo che resetta il campo password modificata di un Amministratore
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean resetPassA(Dipendente d){
     return serverAzienda.modificaRiga("Amministratore", "passmod=''","ID="+d.getId());
   }
   
   /**
    * Metodo che setta il campo password di un Dipendente
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param pass la stringa della nuova password del Dipendente
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean passD(Dipendente d, String pass){
     return serverAzienda.modificaRiga("Dipendente", "password="+pass,"ID="+d.getId());
   }
 
   /**
    * Metodo che setta il campo password (e il campo data pass) di un Amministratore
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param pass la stringa della nuova password dell'Amministratore
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean passA(Dipendente d, String pass){
     DataOra data=new DataOra();
     boolean b = serverAzienda.modificaRiga("Amministratore", "password="+pass,"ID="+d.getId());
     if (b){
       b=false;
       while (!b){
         b=serverAzienda.modificaRiga("Amministratore", "datapass="+data.toString(),"ID="+d.getId());
       }
     }
     return b;
   }
   
   /**
    * Metodo che setta il campo mail di un Dipendente
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param mail la stringa della nuova email del Dipendente
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean mailD(Dipendente d, String mail){
     return serverAzienda.modificaRiga("Dipendente", "email="+mail,"ID="+d.getId());
   }
 
   /**
    * Metodo che ritorna l'elenco dei Dipendenti dell'Azienda
    * 
    * @return un ArrayList che contiene tutti i dipendenti
    * 
    */   
   public ArrayList<Dipendente> elencoDipendenti(){
     ResultSet rs=serverAzienda.selezione("Dipendenti","*","","");
     ArrayList<Dipendente> d = new ArrayList<Dipendente>();
     boolean trovato = false;
     String nome,cognome,codfis,email,ruolo,username,password,passmod;
     int ID,trofeo;
     while(!trovato){
       try{
         username = rs.getString("nickname");
         password = rs.getString("password");
        Dipendente temp=getInfoD(new Login(username,password));
         d.add(temp);      
         rs.next();
       }
       catch(SQLException e){trovato=true;}  
     }
     if (d.size()==0){d=null;}
     return d;
   }
 
   /**
    * Metodo che aggiunge un Dipendente al database
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean aggiungiDipendente(Dipendente d){
     //String nome, String cognome, String codfis, String mail, String impiego
     String param[]=new String[8];
     param[1]=d.getNome();
     param[2]=d.getCognome();
     param[3]=d.getCodFiscale();
     param[4]=d.getEmail();
     param[5]=d.getNickname();
     param[6]=d.getPassword();
     param[7]=d.getRuolo();
     param[8]=""+d.getPunteggio().getPunti();
     return serverAzienda.inserisciRiga("Dipendente","nome, cognome, codice fiscale, email, nickname, password, trofei",param);
   }
   
   /**
    * Metodo che cancella un Dipendente al database
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean cancellaDipendente(Dipendente d){
     return serverAzienda.cancellaRiga("Dipendente","ID="+d.getId()+" AND username="+d.getNickname()+" AND password ="+d.getPassword());
   }
   
   /**
    * Metodo che modifica il nome di un Dipendente al database
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param nome stringa contenente il nuovo nome
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean modNome(Dipendente d, String nome){
     return serverAzienda.modificaRiga("Dipendente","nome="+nome,"ID="+d.getId());
   }
   
   /**
    * Metodo che modifica il cognome di un Dipendente al database
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param cognome stringa contenente il nuovo cognome
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean modCognome(Dipendente d, String cognome){
     return serverAzienda.modificaRiga("Dipendente","cognome="+cognome,"ID="+d.getId());
   }
   
   /**
    * Metodo che modifica il codice fiscale di un Dipendente al database
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param codfis stringa contenente il nuovo codice fiscale
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean modCodFis(Dipendente d, String codfis){
     return serverAzienda.modificaRiga("Dipendente","codice fiscale="+codfis,"ID="+d.getId());
   }
 
   /**
    * Metodo che modifica lo username di un Dipendente al database
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param username stringa contenente il nuovo username
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean modUsername(Dipendente d, String username){
     return serverAzienda.modificaRiga("Dipendente","nickname="+username,"ID="+d.getId());
   }
 
   /**
    * Metodo che modifica l'impiego di un Dipendente al database
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param impiego stringa contenente il nuovo impiego
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean modImpiego(Dipendente d, String impiego){
     return serverAzienda.modificaRiga("Dipendente","ruolo="+impiego,"ID="+d.getId());
   }
   
   /**
    * Metodo che modifica i trofei di un Dipendente al database
    * 
    * @param d Oggetto Dipendente da cui si prendono le informazioni
    * @param n intero contenente il nuovo ammontare trofei
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean setTrofei(Dipendente d, int n){
     return serverAzienda.modificaRiga("Dipendente","trofei="+n,"ID="+d.getId());
   }
   
   /**
    * Metodo che resetta la password (casuale) di un Dipendente
    * 
    * @param r Oggetto Recupero da cui si prendono le informazioni
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean resetD(Recupero r){
     return serverAzienda.modificaRiga("Dipendente","passmod="+generaPassCasuale(),"email="+r.getEmail()+" AND codice fiscale= "+r.getCodFiscale());
   }
   
   /**
    * Metodo che resetta la password (casuale) di un Amministratore
    * 
    * @param r Oggetto Recupero da cui si prendono le informazioni
    * @return boolean che indica se l'operazione � andata o meno a buon fine
    * 
    */   
   public boolean resetA(Recupero r){
     return serverAzienda.modificaRiga("Amministratore","passmod="+generaPassCasuale(),"email="+r.getEmail()+" AND codice fiscale= "+r.getCodFiscale());
   }
 
   /**
    * Metodo che genera in modo casuale una password
    * 
    * @return stringa che contiene la nuova password generata casualmente
    * 
    */   
   private String generaPassCasuale(){
     String lettere[] = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"}; //toLowerCase
     String numeri[] = {"1","2","3","4","5","6","7","8","9","0"};
     String caratteri[] = {"@","#","*","+","?","^","%","&","/","$","!","+","-"};
     String pass="";
     Random rand= new Random();
     while (pass.length()<17){
       int scelta = rand.nextInt(3);
       if (scelta==0){
         int maiusc=rand.nextInt(2);
         int lettera=rand.nextInt(lettere.length);
         if (maiusc==0)
           pass+=lettere[lettera].toLowerCase();
         else
           pass+=lettere[lettera];
       }
       else{
         if (scelta==1){
           int numero=rand.nextInt(numeri.length);
           pass+=numeri[numero];
         }
         else{ //scelta == 2
           int carattere=rand.nextInt(caratteri.length);
           pass+=caratteri[carattere];
         }
       }
     }
     return pass;
   }
 }
