 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rssninja.sensei;
 
 import com.rssninja.models.Keyword;
 import com.rssninja.models.Knowledge;
 import com.rssninja.models.Link;
 import com.rssninja.utils.Database;
 import jadex.adapter.fipa.AgentIdentifier;
 import jadex.runtime.IMessageEvent;
 import jadex.runtime.IParameter;
 import jadex.runtime.Plan;
 import java.util.ArrayList;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 /**
  *
  * @author zheroth
  */
 public class ManageInformation extends Plan {
 
     public ManageInformation(){
         
     }
 
     @Override
     public void body() {
         IMessageEvent message = (IMessageEvent) getInitialEvent();
         JSONObject json = (JSONObject) JSONValue.parse((String)message.getContent());
         IParameter param = (IParameter) message.getParameter("sender");
 
         AgentIdentifier agent = (AgentIdentifier)param.getValue();
         JSONArray linkJSONArray=(JSONArray)json.get("links");
         
         for(Object link:linkJSONArray){
             JSONObject jsonLink = (JSONObject)link;
 
             int id = Integer.parseInt((String)jsonLink.get("id"));
             String value = (String)jsonLink.get("value");
             int relevancia = Integer.parseInt((String)jsonLink.get("relevance"));
             String service = (String)jsonLink.get("service");
             int tag_id = Integer.parseInt((String)jsonLink.get("tag_id"));
             String tag_value = (String)jsonLink.get("tag_value");
 
             Link l = new Link(id, new Keyword(tag_id, tag_value), value, "");
             l.setRelevance(relevancia);
             l.setService(service);
            
            Database.INSTANCE.insertKnowledge(l, service, tag_value, relevancia);
 
             System.out.println("[Sensei] I received a link from "+agent.getName()+" : "+l);
         }        
     }
 }
