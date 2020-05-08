 /*
  * This file is part of OwlSoft Owl.
  *
  *  OwlSoft Owl is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  alphaTab is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with alphaTab.  If not, see <http://www.gnu.org/licenses/>.
  */
 package at.owlsoft.owl.business;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.naming.directory.InitialDirContext;
 
 import at.owlsoft.owl.model.IDefaultRoles;
 import at.owlsoft.owl.model.NoPermissionException;
 import at.owlsoft.owl.model.SearchField;
 import at.owlsoft.owl.model.SearchFieldType;
 import at.owlsoft.owl.model.user.AccountMode;
 import at.owlsoft.owl.model.user.IRole;
 import at.owlsoft.owl.model.user.ISystemUser;
 import at.owlsoft.owl.model.user.Role;
 import at.owlsoft.owl.model.user.SystemUser;
 
 /**
  * 
  */
 public class AuthenticationController extends ControllerBase
 {
     private SystemUser          _currentUser;
     private IOwlSecurityManager _securityManager;
 
     public SystemUser getCurrentUser()
     {
         return _currentUser;
     }
 
     public AuthenticationController(OwlApplicationContext context)
     {
         super(context);
         _securityManager = new DefaultSecurityManager();
     }
 
     public IOwlSecurityManager getSecurityManager()
     {
         return _securityManager;
     }
 
     public void setSecurityManager(IOwlSecurityManager securityManager)
     {
         _securityManager = securityManager;
     }
 
     public void checkAccess(String role) throws NoPermissionException
     {
         _securityManager.checkAccess(role, _currentUser);
     }
 
     public ISystemUser login(String userName, String password)
     {
         String principal = getRdn(userName);
         try
         {
 
             // TODO Put properties into jndi.properties file - didn't work at
             // first
             Properties env = new Properties();
             env.put(Context.INITIAL_CONTEXT_FACTORY,
                     "com.sun.jndi.ldap.LdapCtxFactory");
             env.put(Context.PROVIDER_URL, "ldaps://ldap.fhv.at:636");
             env.put(Context.SECURITY_AUTHENTICATION, "simple");
             env.put(Context.SECURITY_PRINCIPAL, principal);
             env.put(Context.SECURITY_CREDENTIALS, password);
 
             InitialDirContext ctx = new InitialDirContext(env);
             ctx.close();
             // TODO dirty fix
             List<SearchField> search = new ArrayList<SearchField>();
             search.add(new SearchField("_username", userName,
                     SearchFieldType.Equals));
             List<SystemUser> users = getContext()
                     .getSystemUserSearchController().search(search);
             if (users.isEmpty())
             {
                 Random random = new Random();
                 _currentUser = new SystemUser(99 + random.nextInt(),
                         99 + random.nextInt(), userName, "pw", "", userName,
                         userName, null, AccountMode.Ldap);
 
                 _currentUser.addRole(new Role(IDefaultRoles.ADMIN_CONFIG,
                         IDefaultRoles.ADMIN_CONFIG));
 
                 _currentUser.addRole(new Role(IDefaultRoles.RENTAL_CREATE,
                         IDefaultRoles.RENTAL_CREATE));
                 _currentUser.addRole(new Role(IDefaultRoles.RENTAL_SHOW,
                         IDefaultRoles.RENTAL_SHOW));
                 _currentUser.addRole(new Role(IDefaultRoles.RENTAL_EXTEND,
                         IDefaultRoles.RENTAL_EXTEND));
 
                 _currentUser.addRole(new Role(IDefaultRoles.RESERVATION_CREATE,
                         IDefaultRoles.RESERVATION_CREATE));
             }
             else
             {
                 _currentUser = users.get(0);

                // FIXME only for testing !
                _currentUser.addRole(new Role(IDefaultRoles.ADMIN_CONFIG,
                        IDefaultRoles.ADMIN_CONFIG));

                _currentUser.addRole(new Role(IDefaultRoles.RENTAL_CREATE,
                        IDefaultRoles.RENTAL_CREATE));
                _currentUser.addRole(new Role(IDefaultRoles.RENTAL_SHOW,
                        IDefaultRoles.RENTAL_SHOW));
                _currentUser.addRole(new Role(IDefaultRoles.RENTAL_EXTEND,
                        IDefaultRoles.RENTAL_EXTEND));

                _currentUser.addRole(new Role(IDefaultRoles.RESERVATION_CREATE,
                        IDefaultRoles.RESERVATION_CREATE));
             }
 
             return _currentUser;
         }
         catch (NamingException e)
         {
             // Authentication failed
             return null;
         }
     }
 
     public List<? extends IRole> getRolesForCurrentUser()
     {
         ISystemUser user = _currentUser;
 
         if (user != null)
         {
             return user.getRoles();
         }
 
         return new ArrayList<IRole>();
 
     }
 
     private String getRdn(String userName)
     {
         // DN for user name check
         return "uid=" + userName + ",ou=fhv,ou=People,dc=uclv,dc=net";
     }
 }
