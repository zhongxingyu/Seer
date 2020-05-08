 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rssninja.sensei;
 
 import com.rssninja.utils.Database;
 import jadex.adapter.fipa.AgentIdentifier;
 import jadex.adapter.fipa.SFipa;
 import jadex.runtime.IGoal;
 import jadex.runtime.IMessageEvent;
 import jadex.runtime.Plan;
 import java.util.HashMap;
 import java.util.List;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 /**
  *
  * @author zheroth
  */
 public class CreateNinjas extends Plan {
 
     @Override
     public void body() {
         IMessageEvent message = (IMessageEvent) getInitialEvent();
         String contentStr = (String) message.getContent();
 
         //Json format: {"search":"tag"}
         JSONObject content = (JSONObject)JSONValue.parse(contentStr);
         if(content.containsKey("search")){
            String tag = (String)content.get("search");
            CreateAndSendToNinja(tag);
         }else{
            sendMessage(message.createReply("informSend","Could not understand: "+message.getContent()));
         }
     }
 
     private void CreateAndSendToNinja(String tag){
 
         //Update database with tag
         Database.INSTANCE.insertKeyword(tag);
 
         //Create message
         HashMap<String,String> objectToSend=null;
 
         IMessageEvent ime = createMessageEvent("send_request");
 
         objectToSend=new HashMap<String,String>();
         objectToSend.put("search", tag);
         objectToSend.put("sensei",this.getAgentIdentifier().getName());
 
         
         String message = JSONObject.toJSONString(objectToSend);
 
         ime.setContent(message);
 
         //Add receivers to message
         String path="com.rssninja.ninja.Ninja";
         IGoal ca = createGoal("ams_create_agent");
         ca.getParameter("type").setValue(path);
         dispatchSubgoalAndWait(ca);
 
         AgentIdentifier createdAgent = (AgentIdentifier)ca.getParameter("agentidentifier").getValue();
         ime.getParameterSet(SFipa.RECEIVERS).addValue(createdAgent);
 
         //Send message
         sendMessage(ime);
 
        //Update my belief base
        this.getBeliefbase().getBeliefSet("tags").addFact(tag);
     }
 }
