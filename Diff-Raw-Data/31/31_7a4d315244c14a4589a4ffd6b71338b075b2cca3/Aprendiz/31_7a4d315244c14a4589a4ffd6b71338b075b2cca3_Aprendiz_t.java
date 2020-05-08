 package com.rssninja;
 //import java.util.*;

 import jadex.runtime.*;
 import org.json.simple.*;
 import jadex.adapter.fipa.SFipa;
 

 public class Aprendiz extends Plan {
 
     public Aprendiz() {
         System.out.println("Aprendiz creado");
     }
 
     @Override
    public void body(){
         IMessageEvent message = (IMessageEvent) getInitialEvent();
         String content = (String) message.getContent();
         System.out.println(content);
 
        JSONObject obj=null;
        try{
            obj = (JSONObject)JSONValue.parse(content);
            System.out.println("======parsed input======");
            System.out.println(obj.get("service"));
            System.out.println(obj.get("tag"));
            System.out.println();
 
            sendMessage(message.createReply("inform", "OK"));
            
        }catch(Exception e){
            sendMessage(message.createReply("inform","Could not understand: "+content));
        }
     }
    
 }
