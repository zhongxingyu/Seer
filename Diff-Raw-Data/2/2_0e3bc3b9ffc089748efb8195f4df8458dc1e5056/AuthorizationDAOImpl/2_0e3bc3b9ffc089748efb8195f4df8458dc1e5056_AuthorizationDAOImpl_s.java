 package gov.nih.nci.security.dao;
 
 import gov.nih.nci.security.authorization.domainobjects.Application;
 import gov.nih.nci.security.authorization.domainobjects.ApplicationContext;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.Privilege;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.authorization.domainobjects.UserGroupRoleProtectionGroup;
 import gov.nih.nci.security.authorization.jaas.AccessPermission;
 import gov.nih.nci.security.dao.hibernate.ProtectionGroupProtectionElement;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 
 import java.security.Principal;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.security.auth.Subject;
 
 import java.sql.*;
 
 import net.sf.hibernate.Criteria;
 import net.sf.hibernate.HibernateException;
 import net.sf.hibernate.Session;
 import net.sf.hibernate.SessionFactory;
 import net.sf.hibernate.Transaction;
 import net.sf.hibernate.expression.Example;
 import net.sf.hibernate.expression.Expression;
 
 import org.apache.log4j.Logger;
 
 /**
  * @version 1.0
  * @created 03-Dec-2004 1:17:47 AM
  */
 public class AuthorizationDAOImpl implements AuthorizationDAO {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.UserProvisioningManager#getRoleById(java.lang.String)
 	 */
 	public Role getRoleById(String roleId) throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		return (Role)this.getObjectByPrimaryKey(Role.class,new Long(roleId));
 	}
 
 	static final Logger log = Logger.getLogger(AuthorizationDAOImpl.class
 			.getName());
 
 	private SessionFactory sf = null;
 
 	private Application application = null;
 
 	public AuthorizationDAOImpl(SessionFactory sf, String applicationContextName) {
 		this.sf = sf;
 		try {
 			System.out.println("The context Name passed:"
 					+ applicationContextName);
 			this.application = this
 					.getApplicationByName(applicationContextName);
 			//this.application=
 			// (Application)this.getObjectByPrimaryKey(Application.class,new
 			// Long("1"));
 			System.out.println("The Application:"
 					+ application.getApplicationId() + ":"
 					+ application.getApplicationDescription());
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void finalize() throws Throwable {
 		super.finalize();
 	}
 
 	public void setHibernateSessionFactory(SessionFactory sf) {
 		this.sf = sf;
 	}
 
 	public void addUserToGroup(String groupId, String userId)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 
 			User user = (User) this.getObjectByPrimaryKey(s, User.class, new Long(
 					userId));
 			Set user_groups = user.getGroups();
 			Group group = getGroup(new Long(groupId));
 
 			if (!user_groups.contains(group)) {
 				user_groups.add(group);
 				user.setGroups(user_groups);
 				s.update(user);
 			}
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignGroupRoleToProtectionGroup(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 public void assignGroupRoleToProtectionGroup(String protectionGroupId,
 			String groupId, String[] rolesId) throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 
 			ProtectionGroup pgroup = (ProtectionGroup) this.getObjectByPrimaryKey(s, ProtectionGroup.class,
 					new Long(protectionGroupId));
 			
 			Group group = (Group) this.getObjectByPrimaryKey(s, Group.class,
 					new Long(groupId));
 			
 			for (int i = 0; i < rolesId.length; i++) {
 				UserGroupRoleProtectionGroup intersection = new UserGroupRoleProtectionGroup();
 
 				intersection.setGroup(group);
 				intersection.setProtectionGroup(pgroup);
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				
 				Criteria criteria = s.createCriteria(UserGroupRoleProtectionGroup.class);
 				criteria.add(Expression.eq("protectionGroup",pgroup));
 				criteria.add(Expression.eq("group",group));
 				criteria.add(Expression.eq("role",role));
 				
 				List list = criteria.list();
 				
 				if(list.size()==0){
 					intersection.setRole(role);
 					intersection.setUpdateDate( new Date() );				
 					s.save(intersection);
 				}
 
 			}
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 	}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignPrivilegesToRole(java.lang.String[],
 	 *      java.lang.String)
 	 */
 
 	public void assignPrivilegesToRole(String roleId, String[] privilegeIds)
 			throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 
 			Role role = (Role) this.getObjectByPrimaryKey(s, Role.class, new Long(
 					roleId));
 
 			Set currPriv = role.getPrivileges();
 
 			for (int k = 0; k < privilegeIds.length; k++) {
 				log.debug("The new list:" + privilegeIds[k]);
 				Privilege pr = (Privilege) this.getObjectByPrimaryKey(
 						s, Privilege.class, new Long(privilegeIds[k]));
 				if (pr != null && !currPriv.contains(pr)) {
 					currPriv.add(pr);
 				}
 			}
 
 			s.update(role);
 			t.commit();
 
 			//log.debug( "Privilege ID is: " +
 			// privilege.getId().doubleValue() );
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignProtectionElements(java.lang.String,
 	 *      java.lang.String[], java.lang.String[])
 	 */
 	public void assignProtectionElements(String protectionGroupName,
 			String protectionElementObjectId,
 			String protectionElementAttributeName)
 			throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 
 			ProtectionGroup protectionGroup = getProtectionGroup( protectionGroupName );
 			ProtectionElement protectionElement = getProtectionElement( protectionElementObjectId );
 			
 			Criteria criteria = s.createCriteria(ProtectionGroupProtectionElement.class);
 			criteria.add(Expression.eq("protectionGroup",protectionGroup));
 			criteria.add(Expression.eq("protectionElement",protectionElement));
 			
 			List list = criteria.list();
           
 			 if(list.size()==0){
 			 	ProtectionGroupProtectionElement pgpe = new ProtectionGroupProtectionElement();
 			 	pgpe.setProtectionElement(protectionElement);
 			 	pgpe.setProtectionGroup(protectionGroup);
 			 	pgpe.setUpdateDate(new Date());
 			 	
 			 	s.save(pgpe);
 			 }
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignProtectionElements(java.lang.String,
 	 *      java.lang.String[])
 	 */
 	public void assignProtectionElements(String protectionGroupName,
 			String protectionElementObjectId) throws CSTransactionException {
 		// TODO Auto-generated method stub
 		
 		 this.assignProtectionElements(protectionGroupName,protectionElementObjectId,null);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignUserRoleToProtectionGroup(java.lang.String,
 	 *      java.lang.String[], java.lang.String)
 	 */
 	public void assignUserRoleToProtectionGroup(String userId,
 			String[] rolesId, String protectionGroupId)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 
 			ProtectionGroup pgroup = (ProtectionGroup) this.getObjectByPrimaryKey(s, ProtectionGroup.class,
 					new Long(protectionGroupId));
 			
 			User user = (User) this.getObjectByPrimaryKey(s, User.class,
 					new Long(userId));
 			
 			for (int i = 0; i < rolesId.length; i++) {
 				UserGroupRoleProtectionGroup intersection = new UserGroupRoleProtectionGroup();
 
 				intersection.setUser(user);
 				intersection.setProtectionGroup(pgroup);
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				
 				Criteria criteria = s.createCriteria(UserGroupRoleProtectionGroup.class);
 				criteria.add(Expression.eq("protectionGroup",pgroup));
 				criteria.add(Expression.eq("user",user));
 				criteria.add(Expression.eq("role",role));
 				
 				List list = criteria.list();
 				
 				if(list.size()==0){
 					intersection.setRole(role);
 					intersection.setUpdateDate( new Date() );				
 					s.save(intersection);
 				}
 
 			}
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#checkPermission(gov.nih.nci.security.authorization.jaas.AccessPermission,
 	 *      java.lang.String)
 	 */
 	public boolean checkPermission(AccessPermission permission, String userName) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#checkPermission(gov.nih.nci.security.authorization.jaas.AccessPermission,
 	 *      javax.security.auth.Subject)
 	 */
 	public boolean checkPermission(AccessPermission permission, Subject subject) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#checkPermission(gov.nih.nci.security.authorization.jaas.AccessPermission,
 	 *      gov.nih.nci.security.authorization.domainobjects.User)
 	 */
 	public boolean checkPermission(AccessPermission permission, User user) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#checkPermission(java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public boolean checkPermission(String userName, String objectId,
 			String attributeId, String privilegeName) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#checkPermission(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 	public boolean checkPermission(String userName, String objectId,
 			String privilegeName) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#createGroup(gov.nih.nci.security.authorization.domainobjects.Group)
 	 */
 	public void createGroup(Group group) throws CSTransactionException {
 		// TODO Auto-generated method stub
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			group.setApplication(application);
 			group.setUpdateDate(new Date());
 			s.save(group);
 			t.commit();
 			log.debug("Group ID is: " + group.getGroupId());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 	}
 
 	public void createPrivilege(Privilege privilege)
 			throws CSTransactionException {
 		// TODO Auto-generated method stub
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			privilege.setUpdateDate(new Date());
 			s.save(privilege);
 			t.commit();
 
 			log.debug("Privilege ID is: " + privilege.getId().doubleValue());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#createProtectionElement(gov.nih.nci.security.authorization.domainobjects.ProtectionElement)
 	 */
 	public void createProtectionElement(ProtectionElement protectionElement)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			protectionElement.setApplication(application);
 			protectionElement.setUpdateDate(new Date());
 			s.save(protectionElement);
 			t.commit();
 			log.debug("Protection element ID is: "
 					+ protectionElement.getProtectionElementId());
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException(
 					"Protection Element could not be created:", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#createProtectionGroup(gov.nih.nci.security.authorization.domainobjects.ProtectionGroup)
 	 */
 	public void createProtectionGroup(ProtectionGroup protectionGroup)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			protectionGroup.setApplication(application);
 			protectionGroup.setUpdateDate(new Date());
 			s.save(protectionGroup);
 			t.commit();
 			log.debug("Protection group ID is: "
 					+ protectionGroup.getProtectionGroupId());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException(
 					"Protection group could not be created:", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#createRole(gov.nih.nci.security.authorization.domainobjects.Role)
 	 */
 	public void createRole(Role role) throws CSTransactionException {
 		// TODO Auto-generated method stub
 
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			role.setApplication(application);
 			role.setUpdateDate(new Date());
 			s.save(role);
 			t.commit();
 			log.debug("Role ID is: " + role.getId().doubleValue());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#deAssignProtectionElements(java.lang.String,
 	 *      java.lang.String[], java.lang.String[])
 	 */
 	     /**
 	      * Don't implement this method 'cause from authorization manager
 	      * no body will pass an array 
 	      */
 	public void deAssignProtectionElements(String protectionGroupName,
 			String[] protectionElementObjectNames,
 			String[] protectionElementAttributeNames)
 			throws CSTransactionException {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#deAssignProtectionElements(java.lang.String[],
 	 *      java.lang.String)
 	 */
 	/**
 	 * @param protectionGroupName
 	 * @param protectionElementObjectId
 	 *  
 	 */
 	public void deAssignProtectionElements(String protectionGroupName,
 			String protectionElementObjectId) throws CSTransactionException {
 	
 		  try{
 			ProtectionGroup protectionGroup = this.getProtectionGroup(protectionGroupName);
 			ProtectionElement protectionElement = this.getProtectionElement(protectionElementObjectId);
 			
 			String pgId = protectionGroup.getProtectionGroupId().toString();
 			String[] peIds = {protectionElement.getProtectionElementId().toString()};
 			
 			this.removeProtectionElementsFromProtectionGroup(pgId,peIds);
 		  }catch(Exception ex){
 		  	throw new CSTransactionException("Deassignement failed",ex);
 		  }
 			
 			
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getApplicationContext()
 	 */
 	public ApplicationContext getApplicationContext() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getGroup(java.lang.Long)
 	 */
 	public Group getGroup( Long groupId) throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		return (Group) this.getObjectByPrimaryKey( Group.class, groupId);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getGroup(java.lang.String)
 	 */
 	public Group getGroup(String groupName) throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		Session s = null;
 		Group grp = null;
 		try {
 			Group search = new Group();
 			search.setGroupName(groupName);
 			search.setApplication(application);
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = sf.openSession();
 			List list = s.createCriteria(Group.class).add(
 					Example.create(search)).list();
 			//p = (Privilege)s.load(Privilege.class,new Long(privilegeId));
 
 			if (list.size() == 0) {
 				throw new CSObjectNotFoundException("Group not found");
 			}
 			grp = (Group) list.get(0);
 
 		} catch (Exception ex) {
 			log.fatal("Unable to find Group", ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		return grp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getObjects(gov.nih.nci.security.dao.SearchCriteria)
 	 */
 	public Set getObjects(SearchCriteria searchCriteria) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getPrincipals(java.lang.String)
 	 */
 	public Principal[] getPrincipals(String userName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getPrivilege(java.lang.String)
 	 */
 	public Privilege getPrivilege(String privilegeId)
 			throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		/**
 		 * Session s = null; Privilege pr = null; try { s = sf.openSession();
 		 * Query query = s.createQuery("select p from Privilege as p where
 		 * p.name =:name"); query.setString("name",privilegeId); for (Iterator
 		 * it = query.iterate(); it.hasNext();) { Privilege pr1 = (Privilege)
 		 * it.next(); System.out.println("Privilege: " + pr1.getName() );
 		 * pr=pr1; } } catch (Exception ex) { log.fatal("Unable to find Group",
 		 * ex); } finally { try { s.close(); } catch (Exception ex2) { } }
 		 * return pr;
 		 */
 
 		return (Privilege) this.getObjectByPrimaryKey(Privilege.class,
 				new Long(privilegeId));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionElement(java.lang.Long)
 	 */
 	public ProtectionElement getProtectionElement(Long protectionElementId)
 			throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		return (ProtectionElement) this.getObjectByPrimaryKey(
 				ProtectionElement.class, protectionElementId);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionElement(java.lang.String)
 	 */
 	public ProtectionElement getProtectionElement(String objectId)
 			throws CSObjectNotFoundException {
 		Session s = null;
 		ProtectionElement pe = null;
 		try {
 			ProtectionElement search = new ProtectionElement();
 			search.setObjectId(objectId);
 			search.setApplication(application);
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = sf.openSession();
 			List list = s.createCriteria(ProtectionElement.class).add(
 					Example.create(search)).list();
 
 			if (list.size() == 0) {
 				throw new CSObjectNotFoundException(
 						"Protection Element not found");
 			}
 			pe = (ProtectionElement) list.get(0);
 
 		} catch (Exception ex) {
 			log.fatal("Unable to find Protection Element", ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		return pe;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionGroup(java.lang.Long)
 	 */
 	public ProtectionGroup getProtectionGroup(Long protectionGroupId)
 			throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		return (ProtectionGroup) this.getObjectByPrimaryKey(
 				ProtectionGroup.class, protectionGroupId);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionGroup(java.lang.String)
 	 */
 	public ProtectionGroup getProtectionGroup(String protectionGroupName)
 			throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		Session s = null;
 		ProtectionGroup pgrp = null;
 		try {
 			ProtectionGroup search = new ProtectionGroup();
 			search.setProtectionGroupName(protectionGroupName);
 			search.setApplication(application);
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = sf.openSession();
 			List list = s.createCriteria(ProtectionGroup.class).add(
 					Example.create(search)).list();
 
 			if (list.size() == 0) {
 				throw new CSObjectNotFoundException(
 						"Protection Group not found");
 			}
 			pgrp = (ProtectionGroup) list.get(0);
 
 		} catch (Exception ex) {
 			log.fatal("Unable to find Protection group", ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		return pgrp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getRole(java.lang.Long)
 	 */
 	public Role getRole(Long roleId) throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		Role r = (Role) this.getObjectByPrimaryKey(Role.class, roleId);
 		//r.setPrivileges(this.getPrivileges(roleId.toString()));
 		return r;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getRole(java.lang.String)
 	 */
 	public Role getRole(String roleName) throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		Session s = null;
 		Role role = null;
 		try {
 			Role search = new Role();
 			search.setName(roleName);
 			search.setApplication(application);
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = sf.openSession();
 			List list = s.createCriteria(Role.class).add(
 					Example.create(search)).list();
 
 			if (list.size() == 0) {
 				throw new CSObjectNotFoundException(
 						"Role not found");
 			}
 			role = (Role) list.get(0);
 
 		} catch (Exception ex) {
 			log.fatal("Unable to find Protection Element", ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		return role;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getUser(java.lang.String)
 	 */
 	public User getUser(String loginName) {
 		Session s = null;
 		User user = null;
 		try {
 			User search = new User();
 			search.setLoginName(loginName);
 
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = sf.openSession();
 			List list = s.createCriteria(User.class)
 					.add(Example.create(search)).list();
 			//p = (Privilege)s.load(Privilege.class,new Long(privilegeId));
 
 			if (list.size() != 0) {
 				user = (User) list.get(0);
 			}
 
 			Collection groups = user.getGroups();
 			Iterator it = groups.iterator();
 			while (it.hasNext()) {
 				Group grp = (Group) it.next();
 				System.out.println("The group Id:" + grp.getGroupId());
 			}
 
 		} catch (Exception ex) {
 			log.fatal("Unable to find Group", ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		return user;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#modifyGroup(gov.nih.nci.security.authorization.domainobjects.Group)
 	 */
 	public void modifyGroup(Group group) throws CSTransactionException {
 		// TODO Auto-generated method stub
 		Session s = null;
 		Transaction t = null;
 		try {
 			log.debug("About to be Modified");
 			s = sf.openSession();
 			t = s.beginTransaction();
 			group.setUpdateDate(new Date());
 			s.update(group);
 			log.debug("Modified");
 			t.commit();
 
 			//log.debug( "Privilege ID is: " +
 			// privilege.getId().doubleValue() );
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException(
 					"Group Object could not be modified:", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#modifyPrivilege(gov.nih.nci.security.authorization.domainobjects.Privilege)
 	 */
 	public void modifyPrivilege(Privilege privilege)
 			throws CSTransactionException {
 		// TODO Auto-generated method stub
 		Session s = null;
 		Transaction t = null;
 		try {
 			log.debug("About to be Modified");
 			s = sf.openSession();
 			t = s.beginTransaction();
 			privilege.setUpdateDate(new Date());
 			s.update(privilege);
 			log.debug("Modified");
 			t.commit();
 
 			//log.debug( "Privilege ID is: " +
 			// privilege.getId().doubleValue() );
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#modifyProtectionGroup(gov.nih.nci.security.authorization.domainobjects.ProtectionGroup)
 	 */
 	public void modifyProtectionGroup(ProtectionGroup protectionGroup)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 			protectionGroup.setUpdateDate(new Date());
 			s.update(protectionGroup);
 			log.debug("Modified");
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException(
 					"Protection group could not be modified", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#modifyRole(gov.nih.nci.security.authorization.domainobjects.Role)
 	 */
 	public void modifyRole(Role role) throws CSTransactionException {
 		// TODO Auto-generated method stub
 		Session s = null;
 		Transaction t = null;
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 			s.update(role);
 			log.debug("Modified");
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeGroup(java.lang.String)
 	 */
 	public void removeGroup(String groupId) throws CSTransactionException {
 		// TODO Auto-generated method stub
 
 		Group group = new Group();
 		group.setGroupId(new Long(groupId));
 		group.setGroupName("XX");
 		group.setGroupDesc("XX");
 		group.setUpdateDate(new Date());
 		removeObject(group);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeGroupFromProtectionGroup(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void removeGroupFromProtectionGroup(String protectionGroupId,
 			String groupId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		Connection cn = null;
 		//log.debug("Running create test...");
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 			cn = s.connection();
 			String sql = "delete from user_group_role_protection_group where protection_group_id=? and group_id=?";
 			PreparedStatement pstmt = cn.prepareStatement(sql);
 			Long pg_id = new Long(protectionGroupId);
 			Long g_id = new Long(groupId);
 			pstmt.setLong(1,pg_id.longValue());
 			pstmt.setLong(2,g_id.longValue());
 			
 			int i = pstmt.executeUpdate();
 			
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				cn.close();
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeGroupRoleFromProtectionGroup(java.lang.String,
 	 *      java.lang.String, java.lang.String[])
 	 */
 	public void removeGroupRoleFromProtectionGroup(String protectionGroupId,
 			String groupId, String[] rolesId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 
 			ProtectionGroup pgroup = (ProtectionGroup) this.getObjectByPrimaryKey(s, ProtectionGroup.class,
 					new Long(protectionGroupId));
 			
 			Group group = (Group) this.getObjectByPrimaryKey(s, Group.class,
 					new Long(groupId));
 			
 			for (int i = 0; i < rolesId.length; i++) {
 				UserGroupRoleProtectionGroup intersection = new UserGroupRoleProtectionGroup();
 
 				intersection.setGroup(group);
 				intersection.setProtectionGroup(pgroup);
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				
 				Criteria criteria = s.createCriteria(UserGroupRoleProtectionGroup.class);
 				criteria.add(Expression.eq("protectionGroup",pgroup));
 				criteria.add(Expression.eq("group",group));
 				criteria.add(Expression.eq("role",role));
 				
 				List list = criteria.list();
 				
 				if(list.size()==0){
 					intersection.setRole(role);
 					intersection.setUpdateDate( new Date() );				
 					s.delete(intersection);
 				}
 
 			}
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removePrivilege(java.lang.String)
 	 */
 	public void removePrivilege(String privilegeId)
 			throws CSTransactionException {
 
 		Privilege p = new Privilege();
 		p.setId(new Long(privilegeId));
 		p.setDesc("XX");
 		p.setName("XX");
 		p.setUpdateDate(new Date());
 		removeObject(p);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeProtectionElement(gov.nih.nci.security.authorization.domainobjects.ProtectionElement)
 	 */
 	public void removeProtectionElement(String protectionElementId)
 			throws CSTransactionException {
 		// TODO Auto-generated method stub
 		ProtectionElement protectionElement = new ProtectionElement();
 		protectionElement.setProtectionElementId(new Long(protectionElementId));
 		protectionElement.setProtectionElementName("XX");
 		protectionElement.setObjectId("XX");
 		protectionElement.setAttribute("XX");
 		protectionElement.setUpdateDate(new Date());
 		removeObject(protectionElement);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeProtectionGroup(java.lang.String)
 	 */
 	public void removeProtectionGroup(String protectionGroupId)
 			throws CSTransactionException {
 		ProtectionGroup protectionGroup = new ProtectionGroup();
 		protectionGroup.setProtectionGroupId(new Long(protectionGroupId));
 		protectionGroup.setProtectionGroupName("XX");
 		protectionGroup.setProtectionGroupDescription("XX");
 		protectionGroup.setUpdateDate(new Date());
 		removeObject(protectionGroup);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeRole(java.lang.String)
 	 */
 	public void removeRole(String roleId) throws CSTransactionException {
 		// TODO Auto-generated method stub
 		Role r = new Role();
 		r.setId(new Long(roleId));
 		r.setName("XX");
 		r.setDesc("XX");
 		r.setUpdateDate(new Date());
 		this.removeObject(r);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeUserFromGroup(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void removeUserFromGroup(String groupId, String userId)
 			throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			User user = (User) this.getObjectByPrimaryKey(s, User.class, new Long(
 					userId));
 			Group group = (Group) this.getObjectByPrimaryKey(s, Group.class,
 					new Long(groupId));
 			Set groups = user.getGroups();
 			if (groups.contains(group)) {
 				groups.remove(group);
 				user.setGroups(groups);
 				s.update(user);
 			}
 
 			t.commit();
 
 			//log.debug( "Privilege ID is: " +
 			// privilege.getId().doubleValue() );
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeUserFromProtectionGroup(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void removeUserFromProtectionGroup(String protectionGroupId,
 			String userId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		Connection cn = null;
 		//log.debug("Running create test...");
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 			cn = s.connection();
 			String sql = "delete from user_group_role_protection_group where protection_group_id=? and user_id=?";
 			PreparedStatement pstmt = cn.prepareStatement(sql);
 			Long pg_id = new Long(protectionGroupId);
 			Long u_id = new Long(userId);
 			pstmt.setLong(1,pg_id.longValue());
 			pstmt.setLong(2,u_id.longValue());
 			
 			int i = pstmt.executeUpdate();
 			
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				cn.close();
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeUserRoleFromProtectionGroup(java.lang.String,
 	 *      java.lang.String, java.lang.String[])
 	 */
 	public void removeUserRoleFromProtectionGroup(String protectionGroupId, String userId, String[] rolesId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 
 			ProtectionGroup pgroup = (ProtectionGroup) this.getObjectByPrimaryKey(s, ProtectionGroup.class,
 					new Long(protectionGroupId));
 			
 			User user = (User) this.getObjectByPrimaryKey(s, User.class,
 					new Long(userId));
 			
 			for (int i = 0; i < rolesId.length; i++) {
 				UserGroupRoleProtectionGroup intersection = new UserGroupRoleProtectionGroup();
 
 				intersection.setUser(user);
 				intersection.setProtectionGroup(pgroup);
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				
 				Criteria criteria = s.createCriteria(UserGroupRoleProtectionGroup.class);
 				criteria.add(Expression.eq("protectionGroup",pgroup));
 				criteria.add(Expression.eq("user",user));
 				criteria.add(Expression.eq("role",role));
 				
 				List list = criteria.list();
 				
 				if(list.size()==0){
 					intersection.setRole(role);
 					intersection.setUpdateDate( new Date() );				
 					s.delete(intersection);
 				}
 
 			}
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#setOwnerForProtectionElement(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 	public void setOwnerForProtectionElement(String userName,
 			String protectionElementObjectName,
 			String protectionElementAttributeName)
 			throws CSTransactionException {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#setOwnerForProtectionElement(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void setOwnerForProtectionElement(
 			String protectionElementObjectName, String userName)
 			throws CSTransactionException {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Set getPrivileges(String roleId) throws CSObjectNotFoundException {
 		Session s = null;
 		System.out.println("The role: getting there");
 		//ArrayList result = new ArrayList();
 		Set result = new HashSet();
 		try {
 			s = sf.openSession();
 			Role role = (Role) this.getObjectByPrimaryKey(Role.class, new Long(
 					roleId));
 			System.out.println("The role:" + role.getName());
 			result = role.getPrivileges();
 			System.out.println("The result size:" + result.size());
 
 		} catch (Exception ex) {
 			log.error(ex);
 			throw new CSObjectNotFoundException("No Set found", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		return result;
 	}
 
 	public void createUser(User user) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			user.setUpdateDate(new Date());
 			s.save(user);
 			t.commit();
 			log.debug("User ID is: " + user.getUserId());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Could not create the user", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	public void assignProtectionElements(String protectionGroupId,
 			String[] protectionElementIds) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			//System.out.println("The original user Id:"+userId);
 
 			ProtectionGroup protectionGroup = (ProtectionGroup) this
 					.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 							protectionGroupId));
 			
 			
 			
 			for (int i = 0; i < protectionElementIds.length; i++) {
 				ProtectionGroupProtectionElement intersection = new
 				ProtectionGroupProtectionElement();
 				ProtectionElement protectionElement = (ProtectionElement) this
 						.getObjectByPrimaryKey(s, ProtectionElement.class,
 								new Long(protectionElementIds[i]));
 				
 				Criteria criteria = s.createCriteria(ProtectionGroupProtectionElement.class);
 				criteria.add(Expression.eq("protectionGroup",protectionGroup));
 				criteria.add(Expression.eq("protectionElement",protectionElement));	
 				List list = criteria.list();
 				if(list.size()==0){
 					intersection.setProtectionGroup( protectionGroup );
 					intersection.setProtectionElement( protectionElement );
 					intersection.setUpdateDate( new Date() );
 					s.save( intersection );
 				}
 
 			}
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	public void removeProtectionElementsFromProtectionGroup(
 			String protectionGroupId, String[] protectionElementIds)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		//log.debug("Running create test...");
 		try {
 			s = sf.openSession();
 			t = s.beginTransaction();
 			//System.out.println("The original user Id:"+userId);
 
 			ProtectionGroup protectionGroup = (ProtectionGroup) this
 					.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 							protectionGroupId));
 			
 			
 			
 			for (int i = 0; i < protectionElementIds.length; i++) {
 				ProtectionGroupProtectionElement intersection = new
 				ProtectionGroupProtectionElement();
 				ProtectionElement protectionElement = (ProtectionElement) this
 						.getObjectByPrimaryKey(s, ProtectionElement.class,
 								new Long(protectionElementIds[i]));
 				
 				
 					intersection.setProtectionGroup( protectionGroup );
 					intersection.setProtectionElement( protectionElement );
 					intersection.setUpdateDate( new Date() );
 				    this.removeObject(intersection);
 				
 
 			}
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 	}
 
 	private void assignProtectionElement(ProtectionGroup pg, String peId) {
 
 	}
 	
 	private Object getObjectByPrimaryKey( Session s, Class objectType, Long primaryKey) throws HibernateException, CSObjectNotFoundException {
 		
			s = sf.openSession();

 			Object obj = s.load(objectType, primaryKey);
 
 			if (obj == null) {
 				throw new CSObjectNotFoundException(objectType.getName()
 						+ " not found");
 			}
 			
 			return obj;
 
 	}
 	
 	private Object getObjectByPrimaryKey(Class objectType, Long primaryKey)
 		throws CSObjectNotFoundException {
 		Object oj = null;
 		
 		Session s = null;
 		
 		try {
 			
 			s = sf.openSession();	
 			oj = getObjectByPrimaryKey( s, objectType, primaryKey );
 			
 		} catch (Exception ex) {
 			log.error(ex);
 			throw new CSObjectNotFoundException(objectType.getName()
 					+ " not found", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		
 		return oj;
 	}
 
 
 	private void removeObject(Object oj) throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		try {
 
 			s = sf.openSession();
 			t = s.beginTransaction();
 
 			s.delete(oj);
 
 			t.commit();
 
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 			throw new CSTransactionException("Bad", ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 
 	}
 
 	private Application getApplicationByName(String contextName)
 			throws CSObjectNotFoundException {
 		Session s = null;
 		Application app = null;
 		try {
 			Application search = new Application();
 			search.setApplicationName(contextName);
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = sf.openSession();
 			List list = s.createCriteria(Application.class).add(
 					Example.create(search)).list();
 			//p = (Privilege)s.load(Privilege.class,new Long(privilegeId));
 			log.debug("Somwthing");
 			if (list.size() == 0) {
 				System.out.println("Could not find the Application");
 				throw new CSObjectNotFoundException("Not found");
 			}
 			app = (Application) list.get(0);
 			System.out.println("Found the Application");
 
 		} catch (Exception ex) {
 			log.fatal("Unable to find application context", ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 			}
 		}
 		return app;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.AuthorizationManager#initialize(java.lang.String)
 	 */
 	public void initialize(String applicationContextName) {
 		//do nothing...
 
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionGroupRoleContext()
 	 */
 	public Set getProtectionGroupRoleContext(String userId) throws CSObjectNotFoundException {
 		// TODO Auto-generated method stub
 		return null;
     }
 }
