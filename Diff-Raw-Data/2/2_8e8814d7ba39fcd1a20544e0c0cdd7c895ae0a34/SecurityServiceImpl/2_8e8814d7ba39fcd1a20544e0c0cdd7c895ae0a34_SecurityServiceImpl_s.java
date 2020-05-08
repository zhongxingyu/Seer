 /*
  *   Copyright 2010, Maarten Billemont
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.lyndir.lhunath.snaplog.model.service.impl;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.AbstractIterator;
 import com.google.common.collect.Iterators;
 import com.google.inject.Inject;
 import com.lyndir.lhunath.lib.system.collection.Iterators2;
 import com.lyndir.lhunath.lib.system.collection.Pair;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.lib.system.logging.exception.InternalInconsistencyException;
 import com.lyndir.lhunath.lib.system.util.ObjectUtils;
 import com.lyndir.lhunath.snaplog.data.object.security.Permission;
 import com.lyndir.lhunath.snaplog.data.object.security.SecureObject;
 import com.lyndir.lhunath.snaplog.data.object.security.SecurityToken;
 import com.lyndir.lhunath.snaplog.data.object.user.User;
 import com.lyndir.lhunath.snaplog.data.service.SecurityDAO;
 import com.lyndir.lhunath.snaplog.error.IllegalOperationException;
 import com.lyndir.lhunath.snaplog.error.PermissionDeniedException;
 import com.lyndir.lhunath.snaplog.model.ServiceModule;
 import com.lyndir.lhunath.snaplog.model.service.SecurityService;
 import java.util.Iterator;
 import java.util.ListIterator;
 
 
 /**
  * <h2>{@link SecurityServiceImpl}<br> <sub>[in short] (TODO).</sub></h2>
  *
  * <p> <i>Mar 14, 2010</i> </p>
  *
  * @author lhunath
  */
 public class SecurityServiceImpl implements SecurityService {
 
     static final Logger logger = Logger.get( SecurityServiceImpl.class );
     private final SecurityDAO securityDAO;
 
     /**
      * @param securityDAO See {@link ServiceModule}.
      */
     @Inject
     public SecurityServiceImpl(final SecurityDAO securityDAO) {
 
         this.securityDAO = securityDAO;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean hasAccess(final Permission permission, final SecurityToken token, final SecureObject<?> o) {
 
         try {
             assertAccess( permission, token, o );
             return true;
         }
 
         catch (PermissionDeniedException ignored) {
             return false;
         }
     }
 
     @Override
     public <T extends SecureObject<?>> Iterator<T> filterAccess(final Permission permission, final SecurityToken token,
                                                                 final Iterator<T> source) {
 
         return Iterators.filter( source, new Predicate<T>() {
             @Override
             public boolean apply(final T input) {
 
                 return hasAccess( permission, token, input );
             }
         } );
     }
 
     @Override
     public <T extends SecureObject<?>> ListIterator<T> filterAccess(final Permission permission, final SecurityToken token,
                                                                     final ListIterator<T> source) {
 
         return Iterators2.filter( source, new Predicate<T>() {
             @Override
             public boolean apply(final T input) {
 
                 return hasAccess( permission, token, input );
             }
         } );
     }
 
     @Override
     public <S extends SecureObject<?>> S assertAccess(final Permission permission, final SecurityToken token, final S o)
             throws PermissionDeniedException {
 
         checkNotNull( token, "Given security token must not be null." );
 
         // Automatically grant permission when no object is given or required permission is NONE.
         if (o == null || permission == Permission.NONE) {
             logger.dbg( "Permission Granted: No permission necessary for: %s@%s", //
                         permission, o );
             return o;
         }
 
         // Automatically grant permission to INTERNAL_USE token.
         if (token.isInternalUseOnly()) {
             logger.dbg( "Permission Granted: INTERNAL_USE token for: %s@%s", //
                         permission, o );
             return o;
         }
 
         // Determine what permission level to grant on the object for the token.
         Permission tokenPermission;
         if (ObjectUtils.equal( o.getOwner(), token.getActor() ))
             tokenPermission = Permission.ADMINISTER;
         else
             tokenPermission = o.getACL().getUserPermission( token.getActor() );
 
         // If INHERIT, recurse.
         if (tokenPermission == Permission.INHERIT) {
             if (o.getParent() == null) {
                 logger.dbg( "Permission Denied: Can't inherit permissions, no parent set for: %s@%s", //
                             permission, o );
                 throw new PermissionDeniedException( permission, o, "Had to inherit permission but no parent set" );
             }
 
             logger.dbg( "Inheriting permission for: %s@%s", //
                         permission, o );
             assertAccess( permission, token, o.getParent() );
             return o;
         }
 
         // Else, check if granted permission provides required permission.
         if (!isPermissionProvided( tokenPermission, permission )) {
             logger.dbg( "Permission Denied: Token authorizes %s (ACL default? %s), insufficient for: %s@%s", //
                         tokenPermission, o.getACL().isUserPermissionDefault( token.getActor() ), permission, o );
             throw new PermissionDeniedException( permission, o, "Security Token %s grants permissions %s ", token, tokenPermission );
         }
 
         // No permission denied thrown, grant permission.
         logger.dbg( "Permission Granted: Token authorization %s matches for: %s@%s", //
                     tokenPermission, permission, o );
         return o;
     }
 
     private static boolean isPermissionProvided(final Permission givenPermission, final Permission requestedPermission) {
 
         if (givenPermission == requestedPermission)
             return true;
         if (givenPermission == null || requestedPermission == null)
             return false;
 
         for (final Permission inheritedGivenPermission : givenPermission.getProvided())
             if (isPermissionProvided( inheritedGivenPermission, requestedPermission ))
                 return true;
 
         return false;
     }
 
     @Override
     public Permission getDefaultPermission(final SecurityToken token, final SecureObject<?> o)
             throws PermissionDeniedException {
 
         checkNotNull( o, "Given secure object must not be null." );
         assertAccess( Permission.ADMINISTER, token, o );
 
         return o.getACL().getDefaultPermission();
     }
 
     @Override
     public Permission getEffectivePermissions(final SecurityToken token, final User user, final SecureObject<?> o)
             throws PermissionDeniedException {
 
         checkNotNull( o, "Given secure object must not be null." );
         assertAccess( Permission.ADMINISTER, token, o );
 
         Permission permission = o.getACL().getUserPermission( user );
         if (permission == Permission.INHERIT) {
             SecureObject<?> parent = checkNotNull( o.getParent(), "Secure object's default permission is INHERIT but has no parent." );
 
             return getEffectivePermissions( token, user, parent );
         }
 
         return permission;
     }
 
     @Override
     public Iterator<Pair<User, Permission>> iterateUserPermissions(final SecurityToken token, final SecureObject<?> o)
             throws PermissionDeniedException {
 
         checkNotNull( o, "Given secure object must not be null." );
         assertAccess( Permission.ADMINISTER, token, o );
 
         return Iterators.unmodifiableIterator( new AbstractIterator<Pair<User, Permission>>() {
             public Iterator<User> permittedUsers;
 
             {
                 permittedUsers = o.getACL().getPermittedUsers().iterator();
             }
 
             @Override
             protected Pair<User, Permission> computeNext() {
 
                 try {
                     if (permittedUsers.hasNext()) {
                         User user = permittedUsers.next();
                        return new Pair<User, Permission>( user, getEffectivePermissions( token, user, o ) );
                     }
                 }
                 catch (PermissionDeniedException e) {
                     throw new InternalInconsistencyException( "While evaluating user permissions", e );
                 }
 
                 return endOfData();
             }
         } );
     }
 
     @Override
     public int countPermittedUsers(final SecurityToken token, final SecureObject<?> o)
             throws PermissionDeniedException {
 
         checkNotNull( o, "Given secure object must not be null." );
 
         assertAccess( Permission.ADMINISTER, token, o );
         return o.getACL().getPermittedUsers().size();
     }
 
     @Override
     public void setDefaultPermission(final SecurityToken token, final SecureObject<?> o, final Permission permission)
             throws PermissionDeniedException {
 
         checkNotNull( o, "Given secure object must not be null." );
 
         assertAccess( Permission.ADMINISTER, token, o );
         o.getACL().setDefaultPermission( permission );
         securityDAO.update( o );
     }
 
     @Override
     public void setUserPermission(final SecurityToken token, final SecureObject<?> o, final User user, final Permission permission)
             throws PermissionDeniedException, IllegalOperationException {
 
         checkNotNull( o, "Given secure object must not be null." );
         checkNotNull( user, "Given user must not be null." );
 
         if (ObjectUtils.equal( o.getOwner(), user ))
             throw new IllegalOperationException( "Given user must not be the object's owner." );
 
         assertAccess( Permission.ADMINISTER, token, o );
         o.getACL().setUserPermission( user, permission );
         securityDAO.update( o );
     }
 }
