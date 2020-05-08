 /*
 jGuard is a security framework based on top of jaas (java authentication and authorization security).
 it is written for web applications, to resolve simply, access control problems.
 version $Name$
 http://sourceforge.net/projects/jguard/
 
 Copyright (C) 2004  Charles Lescot
 
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
 package net.sf.jguard.core.authorization.permissions;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URL;
 import java.security.*;
 import java.security.cert.Certificate;
 import java.util.Enumeration;
 
 /**
  * Audit permissions checks.
  *
  * @author <a href="mailto:diabolo512@users.sourceforge.net">Charles Lescot</a>
  * @since 2.0
  */
 public class AuditPermissionCollection extends PermissionCollection {
 
     private PermissionCollection pm;
     private ProtectionDomain protectionDomain;
     private CodeSource cs;
     private static Logger logger = LoggerFactory.getLogger(AuditPermissionCollection.class.getName());
     private boolean protectionDomainMode;
 
     /**
      * constructor used to track permissions check against user identified by a {@link ProtectionDomain}.
      *
      * @param permissionCollection
      * @param protectionDomain
      */
     public AuditPermissionCollection(PermissionCollection permissionCollection, ProtectionDomain protectionDomain) {
         this.pm = permissionCollection;
         this.protectionDomain = protectionDomain;
         protectionDomainMode = true;
     }
 
     /**
      * constructor used to track permissions check against jars identified by a {@link CodeSource}.
      *
      * @param permissionCollection
      * @param codeSource
      */
     public AuditPermissionCollection(PermissionCollection permissionCollection, CodeSource codeSource) {
         this.pm = permissionCollection;
         this.cs = codeSource;
         protectionDomainMode = false;
     }
 
     public void add(java.security.Permission permission) {
         pm.add(permission);
     }
 
     public boolean implies(java.security.Permission permission) {
         boolean result = pm.implies(permission);
         if (protectionDomainMode) {
             //we audit user permissions check
             //logger.log(SecurityLevel.JGUARD_SECURITY, "permission check result={1}", new Object[]{Boolean.valueOf(result).toString()});
             //Subject subject = ProtectionDomainUtils.getSubject(pDomain);
             logPermissionCollection(pm, protectionDomain);
             logger.debug(" ProtectionDomain permission check {} result={}", permission.toString(), result);
         } else if (cs != null) {
             logPermissionCollection(pm, cs);
             logger.debug(" CodeSource permission check {} result={}", permission.toString(), result);
         }
         return result;
 
     }
 
     public final Enumeration<java.security.Permission> elements() {
         return pm.elements();
     }
 
     private void logPermissionCollection(PermissionCollection pm, ProtectionDomain protectionDomain) {
         logger.debug("log protectionDomain");
         logProtectionDomain(protectionDomain);
         logger.debug("log permissionCollection");
         logPermissionCollection(pm);
     }
 
     private void logPermissionCollection(PermissionCollection pm, CodeSource codeSource) {
         logCodeSource(codeSource);
         logPermissionCollection(pm);
     }
 
 
     private void logCodeSource(CodeSource codeSource) {
         Certificate[] certs = codeSource.getCertificates();
         for (Certificate certificate : certs) {
             logger.debug("certificate={}", certificate);
         }
         URL location = codeSource.getLocation();
         logger.debug("codeSource location={}", location.toString());
         CodeSigner[] codeSigners = codeSource.getCodeSigners();
         for (CodeSigner codeSigner : codeSigners) {
             logger.debug("codeSigner={}", codeSigner);
         }
     }
 
 
     private void logProtectionDomain(ProtectionDomain protectionDomain) {
         PermissionCollection permissionCollection = protectionDomain.getPermissions();
         if (permissionCollection != null) {
             logPermissionCollection(permissionCollection);
         }
         Principal[] principals = protectionDomain.getPrincipals();
         if (principals != null && principals.length > 0)
             for (Principal principal : principals) {
                 logger.debug(principal.toString());
             }
 
     }
 
     private void logPermissionCollection(PermissionCollection permissionCollection) {
         Enumeration enumPerm = permissionCollection.elements();
         logger.debug("@@ user has got ");
         while (enumPerm.hasMoreElements()) {
            java.security.Permission perm = (java.security.Permission) enumPerm.nextElement();
             logger.debug("{}[" + "name={{}},actions={{}}]", new Object[]{perm.getClass().getSimpleName(), perm.getName(), perm.getActions()});
         }
     }
 
 }
