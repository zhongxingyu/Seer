 package models;
 
 import models.domain.IncogitoSession;
 
import java.io.UnsupportedEncodingException;
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
 
    private static IncogitoSession createSession(Map<String, Object> v) {
         IncogitoSession session = new IncogitoSession();

        try {
            session.title(toUTF8(v, "title"));
            session.talkAbstract(toUTF8(v, "bodyHtml"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

         return session;
     }

    private static String toUTF8(Map<String, Object> v, String key) throws UnsupportedEncodingException {
        return ((String)v.get(key)).getBytes("UTF-8").toString();
    }
 }
