 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rssninja.ninja;
 
 import com.rssninja.models.*;
 import com.rssninja.models.Link;
 import jadex.adapter.fipa.AgentIdentifier;
 import jadex.runtime.*;
 import jadex.adapter.fipa.SFipa;
 import jadex.model.IMBelief;
 import jadex.model.IMBeliefbase;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 /**
  *
  * @author rsantos
  */
 public class SendInfoToSensei extends Plan{
 
     @Override
     public void body() {
         System.out.println("[Ninja] Trying to send info to SENSEI");
         boolean info_updated = (Boolean) getBeliefbase().getBelief("info_updated").getFact();
         if(!info_updated){
             System.out.println("[Ninja] Couldn't send to Sensei, info_updated was false");
             return;
         }
 
         ArrayList<Link> links = new ArrayList<Link>();
         List<Object> linksObjs = Arrays.asList(getBeliefbase().getBeliefSet("new_links").getFacts());
         for (Object object : linksObjs) {
             links.add((Link)object);
         }
         if(links.isEmpty()){
             System.out.println("[Ninja] Couldn't send to Sensei, links was empty");
             return;
         }
 
 
         IMessageEvent ime = createMessageEvent("informSend");
         //Asignar contenido del mensaje
         ime.setContent(CreateLinksJSON(links));                
 
         //sacar agent identifier de sensei del beliefbase
        AgentIdentifier mySensei = new AgentIdentifier((String)getBeliefbase().getBelief("sensei").getFact());
         ime.getParameterSet(SFipa.RECEIVERS).addValue(mySensei);
         

         //Send message
         sendMessage(ime);
         
         //Reestablecer belief de info_updated
         getBeliefbase().getBelief("info_updated").setFact(false);
     }
 
     private String CreateLinksJSON(ArrayList<Link> linkList){
         StringBuilder json = new StringBuilder();
         json.append("{\"links\":[");
         for(Link l : linkList){
             json.append("\"");
                 json.append(l.getValue());
             json.append("\"");
         }
         json.append("]}");
         return json.toString();
     }
 
 }
