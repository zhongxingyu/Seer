 package com.ecnmelog.model;
 
 import com.ecnmelog.observer.Observer;
 import java.util.ArrayList;
 
 public abstract class ObservableModel {
 
     protected ArrayList<Observer> listObserver = new ArrayList<Observer>();
     
     //**************************************************
     //           IMPLÉMENTATION PATTERN OBSERVER
     //**************************************************
     public void addObserver(Observer obs) {
         this.listObserver.add(obs);
     }
     
     public void notifyAttenteObserver(AbstractAttenteBean attenteBean) {
         for(Observer obs : listObserver)
             obs.updateAttente(attenteBean);
     }
 
     public void notifyStockageObserver(AbstractStockageBean stockageBean) {
         for(Observer obs : listObserver)
             obs.updateStockage(stockageBean);
     }
     
     public void notifyInfo(String info) {
         for(Observer obs : listObserver)
             obs.displayInfo(info);
     }
     
     public void notifyWarning(String warning) {
         for(Observer obs : listObserver)
             obs.displayWarning(warning);
     }
     
     public void notifyError(String error) {
         for(Observer obs : listObserver)
             obs.displayError(error);
     }
 
     public void removeObserver() {
         listObserver = new ArrayList<Observer>();
     }
 }
