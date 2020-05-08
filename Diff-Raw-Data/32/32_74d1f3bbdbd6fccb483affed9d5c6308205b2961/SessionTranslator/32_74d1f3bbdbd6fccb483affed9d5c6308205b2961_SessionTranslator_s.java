 package models;
 
 import models.domain.IncogitoSession;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: Knut Haugen <knuthaug@gmail.com>
  * 2011-09-24
  */
 public class SessionTranslator {
 
     public static List<IncogitoSession> translateSessions(List<HashMap<String, Object>> sessions) {
         List<IncogitoSession> sessionObjects = new ArrayList<IncogitoSession>();
 
         for(Map<String, Object> v : sessions) {
             sessionObjects.add(createSession(v));
         }
 
         return sessionObjects;
     }
 
    private static IncogitoSession createSession(Map<String,Object> v) {
         IncogitoSession session = new IncogitoSession();
        session.title((String) v.get("title"));
        session.talkAbstract((String) v.get("bodyHtml"));
         return session;
     }
 }
