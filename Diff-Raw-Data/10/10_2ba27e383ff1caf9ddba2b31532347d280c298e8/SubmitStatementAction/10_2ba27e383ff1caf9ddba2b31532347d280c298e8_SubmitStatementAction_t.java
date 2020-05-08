 package com.rusticisoftware.activitytracker;
 
 import com.rusticisoftware.tincan.*;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 
 public class SubmitStatementAction {
 
     private String actor;
     private String verb;
     private String activity;
 
     private static final ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
 
     public SubmitStatementAction(String actor, String verb, String activity) {
         this.actor = actor;
         this.verb = verb;
         this.activity = activity;
     }
 
     public void process() throws Exception {
 
         RemoteLRS lrs = new RemoteLRS();
 
         lrs.setEndpoint(AppConfig.lrsUrl);
         lrs.setUsername(AppConfig.lrsId);
         lrs.setPassword(AppConfig.lrsPassword);
         lrs.setVersion(TCAPIVersion.V095);
 
         Statement st = new Statement();
         Agent agent = new Agent();
         agent.setMbox("mailto:" + this.actor);


        // verb comes through as desc|id
        String[] verbPair = this.verb.split("\\|");

        Verb verb = new Verb(verbPair[1]);
        LanguageMap lm  = new LanguageMap();
        lm.put("en-US", verbPair[0]);
        verb.setDisplay(lm);
 
         com.rusticisoftware.tincan.Activity activity;
         if (this.activity.toLowerCase().startsWith("http")) {
             activity = new com.rusticisoftware.tincan.Activity(this.activity);
             ActivityDefinition activityDefinition = new ActivityDefinition();
             LanguageMap map = new LanguageMap();
 
             List<LinkedHashMap> activityOptions = mapper.readValue(AppConfig.activityDataFile, List.class);
             String description = "";
             for(LinkedHashMap activityOption : activityOptions) {
                 if (activityOption.get("id").equals(this.activity)) {
                     description = activityOption.get("description").toString();
                     break;
                 }
             }
 
             map.put("en-US", description);
             activityDefinition.setDescription(map);
             activityDefinition.setName(map);
             activity.setDefinition(activityDefinition);
         } else {
             activity = new com.rusticisoftware.tincan.Activity(AppConfig.newActivityIdPrefix + this.activity.replaceAll(" ", "-"));
             ActivityDefinition activityDefinition = new ActivityDefinition();
             LanguageMap map = new LanguageMap();
             map.put("en-US", this.activity);
             activityDefinition.setDescription(map);
             activityDefinition.setName(map);
             activity.setDefinition(activityDefinition);
             List<ActivityOption> activityOptions = mapper.readValue(AppConfig.activityDataFile, List.class);
             activityOptions.add(new ActivityOption(activity.getId().toString(), this.activity, this.activity));
             mapper.writeValue(AppConfig.activityDataFile, activityOptions);
         }
 
         //st.stamp();
         st.setActor(agent);
         st.setVerb(verb);
         st.setObject(activity);
 
         lrs.saveStatement(st);
     }
 }
