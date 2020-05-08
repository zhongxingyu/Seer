 /*
  * Name: UpdateLog.java
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
  * | 20120422 | Gabriele Facchin    | + UpdateLog
  * |          |                     | + finalize
  * |          |                     | + scrivi
  * |          |                     | + scrivichiudi
  * +----------+---------------------|---------------------
  *
  */
 package com.safetyGame.back.access;
 import java.io.*;
 
 /**
  * Classe che gestisce l'update dei file di log di ogni utente
  * 
  * @author gfacchin
  * @version 0.1
  */
 
 public class UpdateLog extends IOException {
   private PrintWriter out;
   
   /**
    * Costruttore della classe UpdateLog
    * 
    * @param percorso percorso di creazione/apertura file dalla cartella log 
    * 
    */
   public UpdateLog(String percorso) throws IOException {
     out=new PrintWriter(new File("log/"+percorso));
   }
 
   /**
    * Distruttore della classe UpdateLog:
    * chiude lo stream quando l'oggetto viene distrutto.
    * 
    */  
   public void finalize(){out.close();}
   
   /**
    * Metodo che scrive una determinata frase all'interno dello stream aperto
    * 
    * @param s stringa da scrivere
    */  
   public void scrivi(String s){
     synchronized (out){
       out.println(s);
       out.flush();
     } 
   }
   
   /**
     * Metodo che scrive una stringa sullo stream aperto e chiude il file
     * 
     * @param s stringa da scrivere
     */
   public synchronized void scriviChiudi(String s){
       out.println(s);
       out.flush();
       out.close();
   }
 }
