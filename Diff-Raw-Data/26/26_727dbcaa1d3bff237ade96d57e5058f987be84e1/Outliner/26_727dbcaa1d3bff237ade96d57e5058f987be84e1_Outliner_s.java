 package model;
 
 import java.util.Observable;
 
 public class Outliner extends Observable
 {
     private Session session;
     
     public Outliner(Session session)
     {
         this.session = session;
     }
     
     private Component latestRoot;
     
     public void refreshOutline()
     {
         latestRoot = session.getCurrentBuffer().generateTree();
         
         setChanged();
         notifyObservers();
     }
    
     public Component getLatestRoot()
     {
         return latestRoot;
     }
     
     public void init()
     {
         if(session.hasABuffer())
         {
             latestRoot = session.getABuffer().generateTree();
         }
         
         setChanged();
         notifyObservers();
     }
 }
