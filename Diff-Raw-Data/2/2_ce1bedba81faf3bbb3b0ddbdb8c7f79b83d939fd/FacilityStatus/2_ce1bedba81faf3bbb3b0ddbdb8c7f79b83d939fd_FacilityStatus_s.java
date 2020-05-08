 package au.edu.uq.cmm.paul.status;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import au.edu.uq.cmm.aclslib.server.Facility;
 
 public class FacilityStatus {
     private Facility facility;
     private List<FacilitySession> sessions = new ArrayList<FacilitySession>();
     
     public FacilityStatus(Facility facility) {
         super();
         this.facility = facility;
     }
     
     public Facility getFacility() {
         return facility;
     }
 
     public void addSession(FacilitySession session) {
         sessions.add(session);
     }
 
     public FacilitySession currentSession() {
         return sessions.get(sessions.size() - 1);
     }
     
     public boolean isInUse() {
        return sessions.size() > 0 && currentSession().getLoginTime() != 0L;
     }
 
     public FacilitySession getLoginDetails(long timestamp) {
         for (int i = sessions.size() - 1; i >= 0; i++) {
             FacilitySession session = sessions.get(i);
             if (session.getLoginTime() <= timestamp && 
                     (session.getLogoutTime() == 0L || 
                      session.getLogoutTime() >= timestamp)) {
                 return session;
             }
         }
         return null;
     }
 }
