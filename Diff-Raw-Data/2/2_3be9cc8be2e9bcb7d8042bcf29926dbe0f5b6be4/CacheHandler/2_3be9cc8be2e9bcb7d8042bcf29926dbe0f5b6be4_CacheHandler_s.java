 /*
  * Copyright (C) 2011 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.services.organization.ldap;
 
 import org.exoplatform.services.cache.CacheService;
 import org.exoplatform.services.cache.ExoCache;
 import org.exoplatform.services.organization.Group;
 import org.exoplatform.services.organization.Membership;
 import org.exoplatform.services.organization.MembershipType;
 import org.exoplatform.services.organization.User;
 
 import java.io.Serializable;
 
 /**
  * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
  * @version $Id: CacheHandler.java 34360 2009-07-22 23:58:59Z tolusha $
  */
 public class CacheHandler
 {
    public static final String MEMBERSHIPTYPE_PREFIX = "mt=";
 
    public static final String GROUP_PREFIX = "g=";
 
    public static final String USER_PREFIX = "u=";
 
    /**
     * Cache for Users.
     */
    private final ExoCache<Serializable, User> userCache;
 
    /**
     * Cache for MembershipTypes.
     */
    private final ExoCache<Serializable, MembershipType> membershipTypeCache;
 
    /**
     * Cache for Memberships.
     */
    private final ExoCache<Serializable, Membership> membershipCache;
 
    /**
     * Cache for Groups.
     */
    private final ExoCache<Serializable, Group> groupCache;
 
    /**
     * Constructor CacheHandler. 
     * 
     * @param cservice
     *          The cache handler
     */
    public CacheHandler(CacheService cservice)
    {
       this.userCache = cservice.getCacheInstance(this.getClass().getName() + "userCache");
       this.membershipTypeCache = cservice.getCacheInstance(this.getClass().getName() + "membershipTypeCache");
       this.groupCache = cservice.getCacheInstance(this.getClass().getName() + "groupCache");
       this.membershipCache = cservice.getCacheInstance(this.getClass().getName() + "membershipCache");
    }
 
    public void put(Serializable key, Object value, CacheType cacheType)
    {
       if (cacheType == CacheType.USER)
       {
          userCache.put(key, (User)value);
       }
       else if (cacheType == CacheType.GROUP)
       {
          groupCache.put(key, (Group)value);
       }
       else if (cacheType == CacheType.MEMBERSHIP)
       {
          membershipCache.put(key, (Membership)value);
       }
       else if (cacheType == CacheType.MEMBERSHIPTYPE)
       {
          membershipTypeCache.put(key, (MembershipType)value);
       }
    }
 
    public Object get(Serializable key, CacheType cacheType)
    {
       if (cacheType == CacheType.USER)
       {
          return userCache.get(key);
       }
       else if (cacheType == CacheType.GROUP)
       {
          return groupCache.get(key);
       }
       else if (cacheType == CacheType.MEMBERSHIP)
       {
          return membershipCache.get(key);
       }
       else if (cacheType == CacheType.MEMBERSHIPTYPE)
       {
          return membershipTypeCache.get(key);
       }
 
       return null;
    }
 
    public void remove(Serializable key, CacheType cacheType)
    {
       if (cacheType == CacheType.USER)
       {
          userCache.remove(key);
       }
       else if (cacheType == CacheType.GROUP)
       {
          groupCache.remove(key);
       }
       else if (cacheType == CacheType.MEMBERSHIP)
       {
          try
          {
             String tKey = ((String)key).toUpperCase();
             for (Membership m : membershipCache.getCachedObjects())
             {
                String mkey = getMembershipKey(m);
                if (mkey.toUpperCase().indexOf(tKey) >= 0)
                {
                   membershipCache.remove(mkey);
                }
             }
          }
          catch (Exception e)
          {
          }
       }
       else if (cacheType == CacheType.MEMBERSHIPTYPE)
       {
          membershipTypeCache.remove(key);
       }
    }
 
    public String getMembershipKey(Membership m)
    {
       StringBuilder key = new StringBuilder();
       key.append(GROUP_PREFIX + m.getGroupId());
       key.append(MEMBERSHIPTYPE_PREFIX + m.getMembershipType());
       key.append(USER_PREFIX + m.getUserName());
 
       return key.toString();
    }
 
    public String getMembershipKey(String username, String groupId, String type)
    {
       StringBuilder key = new StringBuilder();
       key.append(GROUP_PREFIX + groupId);
       key.append(MEMBERSHIPTYPE_PREFIX + type);
       key.append(USER_PREFIX + username);
 
       return key.toString();
    }
 
   public static enum CacheType
    {
       USER, GROUP, MEMBERSHIP, MEMBERSHIPTYPE
    }
 }
