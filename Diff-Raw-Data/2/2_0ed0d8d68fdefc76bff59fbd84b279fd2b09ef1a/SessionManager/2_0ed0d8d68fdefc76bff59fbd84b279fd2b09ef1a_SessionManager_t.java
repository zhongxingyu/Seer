 package edu.utd.robocode;
 
 import java.util.HashMap;
 
 public class SessionManager
 {
     private static HashMap<String, RoboSession> sessions = new HashMap<String, RoboSession>();
 
     public synchronized static boolean createSession(String name,
             Integer capacity)
     {
         // Session exists.
         if (sessions.containsKey(name))
         {
             return false;
         }
         RoboSession session = new RoboSession();
         session.setSessionStatus("Waiting");
         session.setSessionCapacity(capacity);
         session.setSessionName(name);
         sessions.put(name, session);
         return true;
     }
 
     public static RoboSession getSession(String sessionName)
     {
         if (isSessionExisted(sessionName))
         {
             return sessions.get(sessionName);
         }
         return null;
     }
 
     public synchronized static boolean isSessionExisted(String sessionName)
     {
         return sessions.containsKey(sessionName);
     }
 
     public synchronized static boolean isReady(String sessionName)
     {
         if (isSessionExisted(sessionName))
         {
             RoboSession session = sessions.get(sessionName);
             return session.getRobotList().size() == session
                     .getSessionCapacity();
         }
         return false;
     }
 
     public synchronized static boolean runSession(String sessionName)
     {
         if (isSessionExisted(sessionName))
         {
             RoboSession session = sessions.get(sessionName);
             session.setSessionStatus("Playing");
             BattleObserver listener = new BattleObserver(session);
             RobocodeRunner runner = new RobocodeRunner(listener);
             runner.run(session.getRobotList().toArray(new String[0]));
             return true;
         }
         return false;
     }
 
     public synchronized static boolean addRobotToSession(String robotName,
             String sessionName)
     {
         if (isSessionExisted(sessionName))
         {
             RoboSession session = sessions.get(sessionName);
             session.addRobot(robotName);
             return true;
         }
         return false;
     }
 
     public synchronized static boolean destroySession(String sessionName)
     {
         if (isSessionExisted(sessionName))
         {
             RoboSession session = sessions.get(sessionName);
            if (!session.isEnded())
             {
                 // not ended session, should not be destroyed.
                 return false;
             }
             session = null;
             sessions.remove(sessionName);
             return true;
         }
         return false;
     }
 }
