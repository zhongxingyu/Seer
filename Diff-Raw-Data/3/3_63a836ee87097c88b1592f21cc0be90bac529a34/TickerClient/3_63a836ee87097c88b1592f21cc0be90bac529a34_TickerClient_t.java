 package de.fhro.inf.p3.uebung06;
 
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Created by felix on 11/7/13.
  */
 public class TickerClient implements Observer {
     // Der Name des TickerClients
     private String name;
 
     // Erzeugt einen neuen TickerClient
     public TickerClient(String name) {
         this.name = name;
     }
 
     // Die aktuelle Kursliste wird auf der Konsole ausgegeben.
     @Override
     public void update(Observable observable, Object o) {
        if (observable.getClass() != Ticker.class || o.getClass() != String.class)
            throw new IllegalArgumentException();

         Ticker ticker = (Ticker) observable;
         String wkn = (String) o;
 
         System.out.println(wkn + ": " + ticker.getKurs(wkn));
     }
 }
