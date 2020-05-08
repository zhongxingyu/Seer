 package com.rssninja.ninja;
 
 import com.rssninja.utils.Database;
 import jadex.adapter.fipa.AgentIdentifier;
 import jadex.runtime.*;
 import jadex.adapter.fipa.SFipa;
 import jadex.model.IMBelief;
 import jadex.model.IMBeliefbase;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 /**
  *
  * @author unicorn
  */
 public class SearchInternetPlan extends Plan{
 
     private static String[] APPRENTICE_ARRAY={  "Delicious",
                                                 "LastFM",
                                                 "Twitpic",
                                                 "TwitterSearch",
                                                 "Wikipedia"};
     public SearchInternetPlan() {
         System.out.println("Buscando en internet");
     }
     
     @Override
     public void body(){
         IMessageEvent message = (IMessageEvent) getInitialEvent();
         String contentStr = (String) message.getContent();
 
         //Json format: {"search":"tag"}
         JSONObject content = (JSONObject)JSONValue.parse(contentStr);
         
         //Set SENSEI
        //AgentIdentifier sensei = new AgentIdentifier((String)content.get("sensei"));
        getBeliefbase().getBelief("sensei").setFact((String)content.get("sensei"));
 
         if(content.containsKey("search")){
            String tag = (String)content.get("search");
            registerTagBelief(tag);
            
            sendTagToAll(tag);
         }else{
             sendMessage(message.createReply("informSend","Could not understand: "+message.getContent()));
         }
     }
 
     private void registerTagBelief(String tag){
         getBeliefbase().getBelief("tag").setFact(tag);
         Database.INSTANCE.insertWord(tag);
     }
 
     private void sendTagToAll(String tag){
         //Create message
         IMessageEvent ime = createMessageEvent("search");
         ime.setContent(tag);
 
         //Add receivers to message
         for(String name:SearchInternetPlan.APPRENTICE_ARRAY){
             String path="com.rssninja.aprendiz.Aprendiz"+name;
             IGoal ca = createGoal("ams_create_agent");
             ca.getParameter("type").setValue(path);
             dispatchSubgoalAndWait(ca);
           
             AgentIdentifier createdAgent = (AgentIdentifier)ca.getParameter("agentidentifier").getValue();
             ime.getParameterSet(SFipa.RECEIVERS).addValue(createdAgent);
         }
 
         //Send message
         sendMessage(ime);
     }
 }
