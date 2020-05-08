 package com.mymed.model.data.session;
 
 import com.mymed.model.data.AbstractMBean;
 
 public class MSessionBean extends AbstractMBean {
   /**
    * Used for the calculation of the hash code
    */
   private static final int PRIME = 31;
 
   /** SESSION_ID */
   private String id;
   /** USER_ID */
   private String user;
   /** APPLICATION_LIST_ID */
   private String currentApplications;
   private long timeout;
   private boolean isP2P;
   private String ip;
   private int port;
   private String accessToken;
   private boolean isExpired = false;
 
   @Override
   public String toString() {
     return "Session:\n" + super.toString();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
     int result = 1;
 
     result = PRIME * result + (id == null ? 0 : id.hashCode());
     result = PRIME * result + (ip == null ? 0 : ip.hashCode());
     result = PRIME * result + (isP2P ? 1231 : 1237);
     result = PRIME * result + port;
     result = PRIME * result + (user == null ? 0 : user.hashCode());
 
     return result;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object object) {
 
     boolean equal = false;
 
     if (this == object) {
       equal = true;
     } else if (object instanceof MSessionBean) {
       final MSessionBean comparable = (MSessionBean) object;
 
       /*
        * We compare only a subsets of the field to check that two MUserBean
        * objects are the same. These should be values that are set for sure, and
        * not null.
        */
       equal = true;
 
       if (id == null && comparable.getId() != null) {
         equal &= false;
      } else {
         equal &= id.equals(comparable.getId());
       }
 
       if (ip == null && comparable.getIp() != null) {
         equal &= false;
      } else {
         equal &= ip.equals(comparable.getIp());
       }
 
       equal &= isP2P == comparable.isP2P();
       equal &= port == comparable.getPort();
       equal &= isExpired == comparable.isExpired();
 
       if (accessToken == null && comparable.getAccessToken() != null) {
         equal &= false;
      } else {
         equal &= accessToken.equals(comparable.getAccessToken());
       }
 
       if (user == null && comparable.getUser() != null) {
         equal &= false;
      } else {
         equal &= user.equals(comparable.getUser());
       }
     }
 
     return equal;
   }

   /* --------------------------------------------------------- */
   /* GETTER AND SETTER */
   /* --------------------------------------------------------- */
   /**
    * @return the id of the session
    */
   public String getId() {
     return id;
   }
 
   /**
    * Set the id of the session
    * 
    * @param id
    */
   public void setId(final String id) {
     this.id = id;
   }
 
   /**
    * @return the ID of the user associated with the session
    */
   public String getUser() {
     return user;
   }
 
   /**
    * Set the ID of the user to associate with this session
    * 
    * @param user
    */
   public void setUser(final String user) {
     this.user = user;
   }
 
   /**
    * @return the ID of the applications list associated with this session
    */
   public String getCurrentApplications() {
     return currentApplications;
   }
 
   /**
    * Set the ID of the applications list to associate with this session
    * 
    * @param currentApplications
    */
   public void setCurrentApplications(final String currentApplications) {
     this.currentApplications = currentApplications;
   }
 
   public long getTimeout() {
     return timeout;
   }
 
   public void setTimeout(final long timestamp) {
     timeout = timestamp;
   }
 
   /**
    * @return true if the session is on a P2P protocol
    */
   public boolean isP2P() {
     return isP2P;
   }
 
   /**
    * Set whatever the session is on a P2P protocol
    * 
    * @param isP2P
    */
   public void setP2P(final boolean isP2P) {
     this.isP2P = isP2P;
   }
 
   /**
    * @return the IP bound to this session
    */
   public String getIp() {
     return ip;
   }
 
   /**
    * Set the IP of this session
    * 
    * @param ip
    */
   public void setIp(final String ip) {
     this.ip = ip;
   }
 
   /**
    * @return the port of the session
    */
   public int getPort() {
     return port;
   }
 
   /**
    * Set the port of the session
    * 
    * @param port
    */
   public void setPort(final int port) {
     this.port = port;
   }
 
   public String getAccessToken() {
     return accessToken;
   }
 
   public void setAccessToken(final String accessToken) {
     this.accessToken = accessToken;
   }
 
   public boolean isExpired() {
     return isExpired;
   }
 
   public void setExpired(final boolean isExpired) {
     this.isExpired = isExpired;
   }
 
 }
