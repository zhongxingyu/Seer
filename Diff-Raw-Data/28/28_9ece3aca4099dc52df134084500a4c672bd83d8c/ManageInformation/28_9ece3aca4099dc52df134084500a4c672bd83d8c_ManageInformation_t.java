 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rssninja.sensei;
 
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
 

        Object[] linkArrayObj= linkJSONArray.toArray();
        ArrayList<String> linkArray= new ArrayList<String>();
        for(Object object:linkArrayObj){
            linkArray.add((String)object);
        }
        System.out.println("[Sensei]- -Message recieved. Link Array length:"+linkArray.size());
        for(int i=0; i<linkArray.size();i++){
            System.out.println("[Sensei] I received a link from "+agent.getName()+" : "+linkArray.get(i));
         }
     }
 }
