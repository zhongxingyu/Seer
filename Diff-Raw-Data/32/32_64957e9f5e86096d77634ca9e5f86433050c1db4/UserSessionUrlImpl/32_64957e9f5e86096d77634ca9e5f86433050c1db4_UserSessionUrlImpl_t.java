 package org.jasig.portlet.blackboardvcportlet.dao.impl;
 
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Immutable;
 import org.hibernate.annotations.NaturalId;
 import org.hibernate.annotations.NaturalIdCache;
 import org.jasig.portlet.blackboardvcportlet.data.Session;
 import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
 import org.jasig.portlet.blackboardvcportlet.data.UserSessionUrl;
 
 @Entity
 @Table(name = "VC2_USER_SESSION_URL")
 @SequenceGenerator(
         name="VC2_USER_SESSION_URL_GEN",
         sequenceName="VC2_USER_SESSION_URL_SEQ",
         allocationSize=10
     )
 @TableGenerator(
         name="VC2_USER_SESSION_URL_GEN",
         pkColumnValue="VC2_USER_SESSION_URL",
         allocationSize=10
     )
 @Immutable
 @NaturalIdCache
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class UserSessionUrlImpl implements UserSessionUrl {
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue(generator = "VC2_USER_SESSION_URL_GEN")
     @Column(name = "URL_ID")
     private final long urlId;
     
     @NaturalId
    @ManyToOne(targetEntity = SessionImpl.class, optional = false)
     @JoinColumn(name = "SESSION_ID", nullable = false)
     private final Session session;
 
     @NaturalId
    @ManyToOne(targetEntity = ConferenceUserImpl.class, optional = false )
     @JoinColumn(name = "USER_ID", nullable = false)
     private final ConferenceUser user;
     
     @Column(name = "URL", nullable = false)
     private final String url;
     
     /**
      * Needed by hibernate
      */
     @SuppressWarnings("unused")
     private UserSessionUrlImpl() {
         this.urlId = -1;
         this.session = null;
         this.user = null;
         this.url = null;
     }
 
     UserSessionUrlImpl(Session session, ConferenceUser creator, String url) {
         this.urlId = -1;
         
         this.session = session;
         this.user = creator;
         this.url = url;
     }
     
     @Override
     public long getUrlId() {
         return urlId;
     }
 
     @Override
     public Session getSession() {
         return session;
     }
 
     @Override
     public ConferenceUser getUser() {
         return user;
     }
 
     @Override
     public String getUrl() {
         return url;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((session == null) ? 0 : session.hashCode());
         result = prime * result + ((user == null) ? 0 : user.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         UserSessionUrlImpl other = (UserSessionUrlImpl) obj;
         if (session == null) {
             if (other.session != null)
                 return false;
         }
         else if (!session.equals(other.session))
             return false;
         if (user == null) {
             if (other.user != null)
                 return false;
         }
         else if (!user.equals(other.user))
             return false;
         return true;
     }
 
     @Override
     public String toString() {
         return "UserSessionUrlImpl [urlId=" + urlId + ", url=" + url + ", user=" + user + ", session=" + session + "]";
     }
 }
