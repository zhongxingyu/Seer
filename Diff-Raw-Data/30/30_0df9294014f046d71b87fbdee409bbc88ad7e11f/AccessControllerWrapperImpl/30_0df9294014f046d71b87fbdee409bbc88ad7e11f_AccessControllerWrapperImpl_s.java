 /*
 jGuard is a security framework based on top of jaas (java authentication and authorization security).
 it is written for web applications, to resolve simply, access control problems.
 version $Name$
 http://sourceforge.net/projects/jguard/
 
 Copyright (C) 2004  Charles GAY
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 
 jGuard project home page:
 http://sourceforge.net/projects/jguard/
 
 */
 package net.sf.jguard.core.authorization.policy;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.security.auth.Subject;
 import java.security.*;
 
 
 /**
  * Facade class used to bound control check to either the legacy {@link java.security.AccessController} bound to the global JVM security,
  * or to the {@link LocalAccessController} 'isolated'.
  *
  * @author <a href="mailto:diabolo512@users.sourceforge.net">Charles Gay</a>
  * @author <a href="mailto:vberetti@users.sourceforge.net">Vincent Beretti</a>
  * @see java.security.AccessControlContext
  * @see java.security.AccessController
  * @see java.security.ProtectionDomain
  */
 @Singleton
 public class AccessControllerWrapperImpl implements AccessControllerWrapper {
     private static final Logger logger = LoggerFactory.getLogger(AccessControllerWrapperImpl.class.getName());
     private static LocalAccessController accessController = null;
 
 
     /**
      * constructor used to check access control against a specified Policy or
      * the global one (JVM scope) if Policy is null.
      * in this case, the localAccessController is null and check is done further with
      * the static method checkPermission of the AccessController class from JDK.
      *
      * @param policy
      */
     @Inject
     public AccessControllerWrapperImpl(@Nullable Policy policy) {
         if (policy != null) {
             accessController = new LocalAccessController(policy);
         }
     }
 
 
     public boolean hasPermission(Subject subj, final Permission p) {
         try {
             checkPermission(subj, p);
         } catch (Exception ex) {
             return false;
         }
 
         return true;
     }
 
     /**
      * check if the {@link Subject} has got the permission.
      *
      * @param subj user which try to enforce the permission
      * @param p    permission to check
      * @throws PrivilegedActionException
      * @throws AccessControlException    when access is denied
      */
     public void checkPermission(Subject subj, final Permission p) throws AccessControlException, PrivilegedActionException {
 
         if (subj == null && logger.isDebugEnabled()) {
             logger.debug(" subject is null");
         }
         try {
             Subject.doAs(subj, new PrivilegedExceptionAction() {
                 public Object run() {
                     if (accessController == null) {
                         AccessController.checkPermission(p);
                     } else {
                         accessController.checkPermission(p);
                     }
                     // the 'null' tells the SecurityManager to consider this resource access
                     //in an isolated context, ignoring the permissions of code currently
                     //on the execution stack.
                     return null;
                 }
             });
 
         } catch (AccessControlException ace) {
             if (logger.isDebugEnabled()) {
                 logger.debug("AccessControlException ", ace);
             }
             throw ace;
         } catch (PrivilegedActionException pae) {
             if (logger.isDebugEnabled()) {
                 logger.debug("PrivilegedActionException ", pae);
             }
             throw pae;
         }
 
         if (logger.isDebugEnabled()) {
             logger.debug("user has got the permission ", p);
         }
     }
 
 
 }
