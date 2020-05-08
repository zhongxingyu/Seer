 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.jpa.entity.user;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 import org.openide.util.NbBundle;
 
 import java.io.Serializable;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import de.cismet.cids.jpa.entity.common.CommonEntity;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 @Entity
 @Table(name = "cs_usr")
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 public class User extends CommonEntity implements Serializable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final User NO_USER;
 
     static {
         NO_USER = new User();
         NO_USER.setId(-1);
         NO_USER.setLoginname(NbBundle.getMessage(User.class, "User.<clinit>.NO_USER.loginName")); // NOI18N
         NO_USER.setAdmin(false);
     }
 
     //~ Instance fields --------------------------------------------------------
 
     @Id
     @SequenceGenerator(
         name = "cs_usr_sequence",
         sequenceName = "cs_usr_sequence",
         allocationSize = 1
     )
     @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
         generator = "cs_usr_sequence"
     )
     @Column(name = "id")
     private Integer id;
 
     @Column(name = "login_name")
     private String loginname;
 
     @Column(name = "administrator")
     private boolean admin;
 
     @Column(name = "password")
     private String password;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "last_pwd_change")
     private Date lastPwdChange;
 
     @ManyToMany(fetch = FetchType.EAGER)
     @JoinTable(
         name = "cs_ug_membership",
         joinColumns = { @JoinColumn(name = "usr_id") },
         inverseJoinColumns = { @JoinColumn(name = "ug_id") }
     )
    @OrderBy(value = "priority")
     @Fetch(FetchMode.SUBSELECT)
     @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     private Set<UserGroup> userGroups;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new User object.
      */
     public User() {
         admin = false;
         userGroups = new HashSet<UserGroup>();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getLoginname() {
         return loginname;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  loginname  DOCUMENT ME!
      */
     public void setLoginname(final String loginname) {
         this.loginname = loginname;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isAdmin() {
         return admin;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  admin  DOCUMENT ME!
      */
     public void setAdmin(final boolean admin) {
         this.admin = admin;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  password  DOCUMENT ME!
      */
     public void setPassword(final String password) {
         this.password = password;
         this.setLastPwdChange(new Date(System.currentTimeMillis()));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Date getLastPwdChange() {
         return lastPwdChange;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  lastPwdChange  DOCUMENT ME!
      */
     public void setLastPwdChange(final Date lastPwdChange) {
         this.lastPwdChange = lastPwdChange;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Set<UserGroup> getUserGroups() {
         return userGroups;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  userGroups  DOCUMENT ME!
      */
     public void setUserGroups(final Set<UserGroup> userGroups) {
         this.userGroups = userGroups;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return getLoginname() + "(" + getId() + ")"; // NOI18N
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Integer getId() {
         return id;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setId(final Integer id) {
         this.id = id;
     }
 }
