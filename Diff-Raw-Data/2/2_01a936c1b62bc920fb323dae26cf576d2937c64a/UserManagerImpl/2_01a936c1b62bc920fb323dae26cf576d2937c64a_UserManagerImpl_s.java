 package org.mule.galaxy.impl.jcr;
 
 import static org.mule.galaxy.impl.jcr.JcrUtil.getStringOrNull;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.jcr.ItemExistsException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UserDetailsService;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.mule.galaxy.impl.jcr.onm.AbstractReflectionDao;
 import org.mule.galaxy.security.AccessControlManager;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.security.UserExistsException;
 import org.mule.galaxy.security.UserManager;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.dao.DataAccessException;
 import org.springmodules.jcr.JcrCallback;
 
 public class UserManagerImpl extends AbstractReflectionDao<User> 
     implements UserManager, UserDetailsService, ApplicationContextAware {
     
     private static final String USERNAME = "username";
     private static final String PASSWORD = "password";
     private static final String NAME = "name";
     private static final String CREATED = "created";
     private static final String EMAIL = "email";
     private static final String ENABLED = "enabled";
     private static final String ROLES = "roles";
 
     private AccessControlManager accessControlManager;
     private ApplicationContext applicationContext;
     private String activeUsersNodeId;
     
     public UserManagerImpl() throws Exception {
         super(User.class, "users", true);
     }
 
     public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException, DataAccessException {
         return (UserDetails) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node userNode = findUser(username, session);
                 if (userNode == null) {
                     throw new UsernameNotFoundException("Username was not found: " + username);
                 }
                 try {
                     User user = build(userNode, session);
                     return new UserDetailsWrapper(user, 
                                                   getAccessControlManager().getGrantedPermissions(user),
                                                   getStringOrNull(userNode, PASSWORD));
                 } catch (Exception e) {
                     if (e instanceof RepositoryException) {
                         throw (RepositoryException) e;
                     }
                     throw new RuntimeException(e);
                 }
             }
         });
     }
 
     public User getByUsername(final String username) {
         return (User) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node userNode = findUser(username, session);
                 if (userNode == null) {
                     throw new UsernameNotFoundException("Username was not found: " + username);
                 }
                 try {
                     return build(userNode, session);
                 } catch (Exception e) {
                     if (e instanceof RepositoryException) {
                         throw (RepositoryException) e;
                     }
                     throw new RuntimeException(e);
                 }
             }
         });
     }
 
     public boolean setPassword(final String username, final String oldPassword, final String newPassword) {
         return (Boolean) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = findUser(username, session);
                 if (node == null) {
                     return null;
                 }
                 
                 String pass = JcrUtil.getStringOrNull(node, PASSWORD);
                 
                 if (oldPassword != null && oldPassword.equals(pass)) {
                     node.setProperty(PASSWORD, newPassword);
                 } else {
                     return false;
                 }
                 
                 return true;
             }
         });
     }
 
     public void setPassword(final User user, final String password) {
         execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = findUser(user.getUsername(), session);
                 if (node == null) {
                     return null;
                 }
                 
                 node.setProperty(PASSWORD, password);
                 return null;
             }
         });
     }
 
     public User authenticate(final String username, final String password) {
         return (User) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = findUser(username, session);
                 if (node == null) {
                     return null;
                 }
                
                 String pass = getStringOrNull(node, PASSWORD);
                 if (password != null && password.equals(pass)) {
                     try {
                         return build(node, session);
                     } catch (Exception e) {
                         if (e instanceof RepositoryException) {
                             throw (RepositoryException) e;
                         }
                         throw new RuntimeException(e);
                     }
                 }
                 
                 return null;
                 
             }
         });
     }
 
     protected Node findUser(String username, Session session) throws RepositoryException {
         QueryManager qm = getQueryManager(session);
         Query q = qm.createQuery("/jcr:root/users/*[@enabled='true' and @username='" + username + "']", Query.XPATH);
         QueryResult qr = q.execute();
         
         NodeIterator nodes = qr.getNodes();
         if (!nodes.hasNext()) {
             return null;
         }
         
         return nodes.nextNode();
     }
 
     public void create(final User user, final String password) throws UserExistsException {
         Object result = execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node activeUsers = getNodeByUUID(activeUsersNodeId);
                 try {
                     activeUsers.addNode(user.getUsername());
                 } catch (ItemExistsException e) {
                     return new UserExistsException();
                 }
                 Node users = getObjectsNode(session);
                 
                 String id = generateNodeName(null);
                 Node node = users.addNode(id);
                 node.addMixin("mix:referenceable");
                 node.setProperty(PASSWORD, password);
                 node.setProperty(ENABLED, true);
                 
                 user.setId(id);
                 
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(new Date());
                 user.setCreated(cal);
                 
                 try {
                     persist(user, node, session);
                 } catch (Exception e) {
                     if (e instanceof RepositoryException) {
                         throw (RepositoryException) e;
                     }
                     throw new RuntimeException(e);
                 }
                 
                 session.save();
                 return null;
             }
         });
         
         if (result instanceof UserExistsException) {
             throw (UserExistsException) result;
         }
     }
 
     @Override
     protected void doDelete(String id, Session session) throws RepositoryException {
         Node userNode = findNode(id, session);
         
         if (userNode != null) {
             userNode.setProperty(ENABLED, false);
         }
         
         Node activeUsers = getNodeByUUID(activeUsersNodeId);
        Node activeUser = activeUsers.getNode(JcrUtil.getStringOrNull(activeUsers, USERNAME));
         
         activeUser.remove();
         
         session.save();
     }
 
     
     @SuppressWarnings("unchecked")
     @Override
     protected List<User> doListAll(Session session) throws RepositoryException {
         return (List<User>) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 Query q = qm.createQuery("/jcr:root/users/*[@enabled='true']", Query.XPATH);
                 QueryResult qr = q.execute();
                 
                 ArrayList<User> users = new ArrayList<User>();
                 for (NodeIterator nodes = qr.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
                     
                     try {
                         users.add(build(node, session));
                     } catch (Exception e) {
                         if (e instanceof RepositoryException) {
                             throw (RepositoryException) e;
                         } else if (e instanceof RuntimeException) {
                             throw (RuntimeException) e;
                         }
                         throw new RuntimeException(e);
                     }
                 }
 
                 return users;
             }
         });
     }
 
     protected void doCreateInitialNodes(Session session, Node objects) throws RepositoryException {
         Node activeUsers = JcrUtil.getOrCreate(getRootNode(), "activeUsers", "galaxy:noSiblings");
         activeUsersNodeId = activeUsers.getUUID();
         
         if (objects.getNodes().getSize() == 0) {
             String id = generateNodeName(null);
             Node node = objects.addNode(id);
             node.addMixin("mix:referenceable");
             node.setProperty(PASSWORD, "admin");
             node.setProperty(ENABLED, true);
             
             JcrUtil.setProperty(USERNAME, "admin", node);
             JcrUtil.setProperty(NAME, "Administrator", node);
             JcrUtil.setProperty(EMAIL, "", node);
             HashSet<String> roles = new HashSet<String>();
             roles.add(UserManager.ROLE_ADMINISTRATOR);
             roles.add(UserManager.ROLE_USER);
             JcrUtil.setProperty(ROLES, roles, node);
             
             Calendar cal = Calendar.getInstance();
             cal.setTime(new Date());
             JcrUtil.setProperty(CREATED, cal, node);
             
             activeUsers.addNode("admin");
         }
     }
 
     private AccessControlManager getAccessControlManager() {
         if (accessControlManager == null) {
             accessControlManager =(AccessControlManager) applicationContext.getBean("accessControlManager");
         }
         return accessControlManager;
     }
     
     public void setApplicationContext(ApplicationContext ctx) throws BeansException {
         this.applicationContext = ctx;
     }
 
 }
