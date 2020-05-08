 /*
  * To change this template, choose Tools | Templates and open the template in the editor.
  */
 package com.blazebit.security.web.bean;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.blazebit.security.ActionFactory;
 import com.blazebit.security.EntityResourceFactory;
 import com.blazebit.security.Permission;
 import com.blazebit.security.PermissionDataAccess;
 import com.blazebit.security.PermissionFactory;
 import com.blazebit.security.PermissionManager;
 import com.blazebit.security.PermissionService;
 import com.blazebit.security.ResourceFactory;
 import com.blazebit.security.Role;
 import com.blazebit.security.Subject;
 import com.blazebit.security.impl.model.AbstractDataPermission;
 import com.blazebit.security.impl.model.EntityField;
 import com.blazebit.security.impl.service.PermissionHandlingImpl;
 import com.blazebit.security.impl.service.resource.EntityFieldResourceHandlingUtils;
 import com.blazebit.security.impl.utils.ActionUtils;
 import com.blazebit.security.metamodel.ResourceMetamodel;
 
 /**
  * 
  * @author cuszk
  */
 @ViewScoped
 @ManagedBean(name = "permissionHandlingBaseBean")
 @Named
 public class PermissionHandlingBaseBean extends TreeHandlingBaseBean {
 
     @Inject
     protected PermissionFactory permissionFactory;
     @Inject
     protected UserSession userSession;
     @Inject
     protected PermissionDataAccess permissionDataAccess;
     @Inject
     protected PermissionManager permissionManager;
     @Inject
     protected EntityResourceFactory entityFieldFactory;
     @Inject
     protected ResourceFactory resourceFactory;
     @Inject
     protected ActionFactory actionFactory;
     @Inject
     protected PermissionService permissionService;
     @Inject
     protected ActionUtils actionUtils;
     @Inject
     protected ResourceMetamodel resourceMetamodel;
     @Inject
     protected PermissionHandlingImpl permissionHandling;
 
     @Inject
     protected EntityFieldResourceHandlingUtils resourceUtils;
 
     private List<Permission> notGranted = new ArrayList<Permission>();
     private List<Permission> notRevoked = new ArrayList<Permission>();
 
     /**
      * separates permissions and data permissions
      * 
      * @param permissions
      * @return
      */
     public List<List<Permission>> filterPermissions(Collection<Permission> permissions) {
         List<List<Permission>> ret = new ArrayList<List<Permission>>();
         List<Permission> entities = new ArrayList<Permission>();
         List<Permission> objects = new ArrayList<Permission>();
         for (Permission p : permissions) {
             if (p.getClass().isAssignableFrom(AbstractDataPermission.class)) {
                 objects.add(p);
             } else {
                 entities.add(p);
             }
         }
         ret.add(entities);
         ret.add(objects);
         return ret;
     }
 
     protected Set<Permission> concat(Collection<Permission> current, Collection<Permission> added) {
         Set<Permission> ret = new HashSet<Permission>();
         ret.addAll(current);
         ret.addAll(added);
         return ret;
     }
 
     protected List<Set<Permission>> executeRevokeAndGrant(Role role, Collection<Permission> current, Set<Permission> selected, Set<Permission> prevRevoked, Set<Permission> prevReplaced) {
         return executeRevokeAndGrant(role, current, selected, prevRevoked, prevReplaced, false);
     }
 
     protected List<Set<Permission>> executeRevokeAndGrant(Role role, Collection<Permission> current, Set<Permission> selected, Set<Permission> prevRevoked, Set<Permission> prevReplaced, boolean simulate) {
         Set<Permission> revoked = new HashSet<Permission>();
         // add back previous revoked permisions
         for (Permission permission : prevRevoked) {
             if (!permissionHandling.implies(selected, permission)) {
                 revoked.add(permission);
             }
         }
         // add back previous replaced permisssion if no overriding permission exists in the current selected ones
         for (Permission permission : prevReplaced) {
             if (!permissionHandling.implies(selected, permission)) {
                 selected.add(permission);
             }
         }
 
         revoked.addAll(permissionHandling.getRevokableFromSelected(current, concat(current, selected)).get(0));
         Set<Permission> granted = permissionHandling.getGrantable(permissionHandling.removeAll(current, revoked), selected).get(0);
         return performOperations(role, current, revoked, granted, simulate);
 
     }
 
     protected List<Set<Permission>> executeRevokeAndGrant(Subject subject, Collection<Permission> current, Set<Permission> selected, Set<Permission> prevRevoked, Set<Permission> prevReplaced) {
         return executeRevokeAndGrant(subject, current, selected, prevRevoked, prevReplaced, false);
     }
 
     protected List<Set<Permission>> executeRevokeAndGrant(Subject subject, Collection<Permission> current, Set<Permission> selected, Set<Permission> prevRevoked, Set<Permission> prevReplaced, boolean simulate) {
         Set<Permission> revoked = new HashSet<Permission>();
         // add back previous revoked permisions
         for (Permission permission : prevRevoked) {
             if (!permissionHandling.implies(selected, permission)) {
                 revoked.add(permission);
             }
         }
         // add back previous replaced permisssion if no overriding permission exists in the current selected ones
         for (Permission permission : prevReplaced) {
             if (!permissionHandling.implies(selected, permission)) {
                 selected.add(permission);
             }
         }
 
         revoked.addAll(permissionHandling.getRevokableFromSelected(current, concat(current, selected)).get(0));
         Set<Permission> granted = permissionHandling.getGrantable(permissionHandling.removeAll(current, revoked), selected).get(0);
         return performOperations(subject, current, revoked, granted, simulate);
 
     }
 
     protected List<Set<Permission>> performOperations(Subject subject, Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted) {
         return performOperations(subject, current, revoked, granted, false);
     }
 
     protected List<Set<Permission>> performOperations(Subject subject, Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted, boolean simulate) {
         List<Set<Permission>> permissions = permissionHandling.getRevokedAndGrantedAfterMerge(current, revoked, granted);
         Set<Permission> finalRevoked = permissions.get(0);
         Set<Permission> finalGranted = permissions.get(1);
         return revokeAndGrant(subject, finalRevoked, finalGranted, simulate);
 
     }
 
     protected List<Set<Permission>> revokeAndGrant(Subject subject, Set<Permission> finalRevoked, Set<Permission> finalGranted, boolean simulate) {
        if (!simulate) {
             permissionService.revokeAndGrant(userSession.getUser(), subject, finalRevoked, finalGranted);
         }
 
         List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
         ret.add(finalRevoked);
         ret.add(finalGranted);
         return ret;
     }
 
     protected List<Set<Permission>> performOperations(Role role, Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted) {
         return performOperations(role, current, revoked, granted, false);
     }
 
     protected List<Set<Permission>> performOperations(Role role, Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted, boolean simulate) {
         List<Set<Permission>> permissions = permissionHandling.getRevokedAndGrantedAfterMerge(current, revoked, granted);
         Set<Permission> finalRevoked = permissions.get(0);
         Set<Permission> finalGranted = permissions.get(1);
         return revokeAndGrant(role, finalRevoked, finalGranted, simulate);
     }
 
     protected List<Set<Permission>> revokeAndGrant(Role role, Set<Permission> finalRevoked, Set<Permission> finalGranted, boolean simulate) {
         if (!simulate) {
             permissionService.revokeAndGrant(userSession.getUser(), role, finalRevoked, finalGranted);
         }
         List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
         ret.add(finalRevoked);
         ret.add(finalGranted);
         return ret;
     }
 
     public List<Permission> getNotGranted() {
         return this.notGranted;
     }
 
     public void setNotGranted(Set<Permission> notGranted) {
         List<Permission> ret = new ArrayList<Permission>(notGranted);
         Collections.sort(ret, new Comparator<Permission>() {
 
             @Override
             public int compare(Permission o1, Permission o2) {
                 return ((EntityField) o1.getResource()).getEntity().compareToIgnoreCase(((EntityField) o1.getResource()).getEntity());
             }
 
         });
         this.notGranted = ret;
 
     }
 
     public List<Permission> getNotRevoked() {
         return notRevoked;
     }
 
     public void setNotRevoked(Set<Permission> notRevoked) {
         List<Permission> ret = new ArrayList<Permission>(notRevoked);
         Collections.sort(ret, new Comparator<Permission>() {
 
             @Override
             public int compare(Permission o1, Permission o2) {
                 return ((EntityField) o1.getResource()).getEntity().compareToIgnoreCase(((EntityField) o1.getResource()).getEntity());
             }
 
         });
         this.notRevoked = ret;
     }
 
 }
