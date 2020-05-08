 package model;
 
import java.util.ArrayList;
 import java.util.Observable;
 
 public class Outliner extends Observable
 {
     private Session session;
     
     public Outliner(Session session)
     {
         this.session = session;
        resourcelist = new ArrayList<String>();
     }
     
     private Component latestRoot;
    private ArrayList<String> resourcelist;
     
     public void refreshOutline()
     {
         latestRoot = session.getCurrentBuffer().generateTree();
        latestRoot.links(resourcelist);
         
         setChanged();
         notifyObservers();
     }
    public ArrayList<String> getResourceList()
    {
        return resourcelist;
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
