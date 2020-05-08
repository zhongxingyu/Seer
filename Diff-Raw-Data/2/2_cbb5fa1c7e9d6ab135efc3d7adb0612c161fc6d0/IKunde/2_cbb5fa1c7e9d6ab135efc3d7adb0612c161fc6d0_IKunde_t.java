 package de.fhro.inf.p3.uebung09;
 
 import java.util.List;
 
 /**
  * Created by felix on 11/27/13.
  */
 public interface IKunde {
     void addBestellung(Bestellung bestellung);
 
    int getId();
 
     String getName();
 
     Adresse getAdresse();
 
     List<Bestellung> getBestellungen();
 
     List<Bestellung> getBezahlteBestellungen();
 
     void setAdresse(Adresse adresse);
 }
