 // Copyright (c) 2012 Tim Niblett. All Rights Reserved.
 //
 // File:        MicroPersonaUserDAO.java  (12-Oct-2012)
 // Author:      tim
 //
 // Copyright in the whole and every part of this source file belongs to
 // Tim Niblett (the Author) and may not be used, sold, licenced, 
 // transferred, copied or reproduced in whole or in part in 
 // any manner or form or in or on any media to any person other than 
 // in accordance with the terms of The Author's agreement
 // or otherwise without the prior written consent of The Author.  All
 // information contained in this source file is confidential information
 // belonging to The Author and as such may not be disclosed other
 // than in accordance with the terms of The Author's agreement, or
 // otherwise, without the prior written consent of The Author.  As
 // confidential information this source file must be kept fully and
 // effectively secure at all times.
 //
 
 
 package com.cilogi.shiro.microdemo;
 
 import com.cilogi.shiro.persona.DefaultPersonaUserDAO;
 import com.google.common.collect.Sets;
 import org.apache.shiro.authc.SimpleAccount;
 import org.apache.shiro.realm.text.IniRealm;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 public class MicroPersonaUserDAO extends DefaultPersonaUserDAO {
     static final Logger LOG = Logger.getLogger(MicroPersonaUserDAO.class.getName());
 
     private final MyIniRealm iniRealm;
 
     public MicroPersonaUserDAO() {
         this.iniRealm = new MyIniRealm("classpath:shiro.ini");
     }
 
     public Set<String> userRoles(String principal) {
         Set<String> set =  Sets.newHashSet("user");
         set.addAll(iniRealm.getRoles(principal));
         return set;
     }
 
     public Set<String> userPermissions(String principal) {
         Set<String> set = Sets.newHashSet();
        set.addAll(iniRealm.getRoles(principal));
         return set;
     }
 
     private static class MyIniRealm extends IniRealm {
         MyIniRealm(String iniLocation) {
             super(iniLocation);
         }
         public SimpleAccount getUser(String name) {
             return super.getUser(name);
         }
         public Collection<String> getStringPermissions(String name) {
             SimpleAccount account = getUser(name);
            return (account == null) ? Sets.<String>newHashSet() : account.getStringPermissions();
         }
         public Collection<String> getRoles(String name) {
             SimpleAccount account = getUser(name);
            return (account == null) ? Sets.<String>newHashSet() : account.getRoles();
         }
     }
 }
