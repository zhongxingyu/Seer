 package com.smallcultfollowing.lathos;
 
 import java.io.IOException;
 
 public class Linked
     implements Page
 {
     private final Object linkedTo;
     private final Object displayAs;
 
     public Linked(Object linkedTo, Object displayAs)
     {
         super();
         this.linkedTo = linkedTo;
         this.displayAs = displayAs;
     }
 
     @Override
     public void renderSummary(Output out, Link link) throws IOException
     {
         Link link2 = new RelativeLink(link, "linkedTo");
         out.a(link2);
         out.obj(null, displayAs);
         out._a(link2);
     }
 
     @Override
     public Object derefPage(LathosServer server, String link) throws InvalidDeref
     {
        if(link.equals("linkedTo")) return linkedTo;
        throw InvalidDeref.instance;
     }
 
 }
