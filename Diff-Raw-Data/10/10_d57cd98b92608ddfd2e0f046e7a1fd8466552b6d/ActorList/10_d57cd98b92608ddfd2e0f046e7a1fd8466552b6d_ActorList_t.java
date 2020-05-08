 package org.illithid.cccp;
 
 import java.util.ArrayList;
 
 import org.illithid.cccp.bestiary.Actor;
 
 public class ActorList {
     ArrayList<Actor>          actors           = new ArrayList<Actor>();
 
     private static final long serialVersionUID = -574013274149584937L;
 
     public Actor[] getAll() {
         Actor[] a = new Actor[actors.size()];
        for(int i = 0; i<actors.size();i++)
             a[i] = actors.get(i);
         return a;
     }
 
     public void add(Actor a) {
         actors.add(a);
 
     }
 
     public void act() {
         for(Actor a : actors)
             a.act();
     }
 
 }
