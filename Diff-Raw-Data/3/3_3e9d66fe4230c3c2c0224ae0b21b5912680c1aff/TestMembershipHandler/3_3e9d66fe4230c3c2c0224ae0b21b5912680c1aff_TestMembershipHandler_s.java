 /*
  * Copyright (C) 2003-2008 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.services.tck.organization;
 
 import org.exoplatform.commons.utils.ListAccess;
 import org.exoplatform.commons.utils.PageList;
 import org.exoplatform.services.organization.Group;
 import org.exoplatform.services.organization.Membership;
 import org.exoplatform.services.organization.MembershipEventListener;
 import org.exoplatform.services.organization.MembershipEventListenerHandler;
 import org.exoplatform.services.organization.MembershipType;
 import org.exoplatform.services.organization.User;
 
 import java.util.List;
 
 /**
  * Created by The eXo Platform SAS.
  * 
  * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
  * @version $Id: TestMembershipImpl.java 111 2008-11-11 11:11:11Z $
  */
 public class TestMembershipHandler extends AbstractOrganizationServiceTest
 {
    /**
     * Find membership.
     */
    public void testFindMembership() throws Exception
    {
       createMembership(userName, groupName1, membershipType);
 
       Membership m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);
       assertNotNull(mHandler.findMembership(m.getId()));
 
       // try to find not existed membership. We are supposed to get Exception
       try
       {
          assertNull(mHandler.findMembership("not-existed-id"));
          fail("Exception should be thrown");
       }
       catch (Exception e)
       {
          
       }
    }
 
    /**
     * Find membership by user and group.
     */
    public void testFindMembershipByUserGroupAndType() throws Exception
    {
       Membership m = mHandler.findMembershipByUserGroupAndType("marry", "/platform/users", "member");
 
       assertNotNull(m);
       assertEquals(m.getGroupId(), "/platform/users");
       assertEquals(m.getMembershipType(), "member");
       assertEquals(m.getUserName(), "marry");
 
       // try to find not existed membership. We are supposed to get null instead of Exception
       try
       {
          assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/platform/users", "member"));
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
 
       try
       {
          assertNull(mHandler.findMembershipByUserGroupAndType("marry", "/" + groupName1, "member"));
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
 
       try
       {
          assertNull(mHandler.findMembershipByUserGroupAndType("marry", "/platform/users", membershipType));
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Find membership by group.
     */
    public void testFindMembershipsByGroup() throws Exception
    {
       Group g = gHandler.findGroupById("/platform/users");
       assertEquals(mHandler.findMembershipsByGroup(g).size(), 4);
 
       // try to find for non-existing group
       g = gHandler.createGroupInstance();
       g.setGroupName(groupName1);
       gHandler.addChild(null, g, false);
       assertEquals(g.getId(), gHandler.findGroupById("/" + groupName1).getId());
       g = gHandler.removeGroup(g, false);
       assertEquals(mHandler.findMembershipsByGroup(g).size(), 0);
 
    }
 
    /**
     * Find membership by group.
     */
    public void testFindAllMembershipsByGroup() throws Exception
    {
       Group g = gHandler.findGroupById("/platform/users");
       ListAccess<Membership> memberships = mHandler.findAllMembershipsByGroup(g);
       assertEquals(memberships.getSize(), 4);
 
       try
       {
          Membership[] m = memberships.load(0, 4);
          assertEquals(4, m.length);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
 
       try
       {
          Membership[] m = memberships.load(1, 2);
          assertEquals(2, m.length);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
 
       try
       {
          Membership[] m = memberships.load(1, 4);
          fail("Exception should be thrown");
       }
       catch (Exception e)
       {
       }
 
       // try to find for non-existing group
       g = gHandler.createGroupInstance();
       g.setGroupName(groupName1);
       gHandler.addChild(null, g, false);
       assertEquals(g.getId(), gHandler.findGroupById("/" + groupName1).getId());
       g = gHandler.removeGroup(g, false);
       assertEquals(mHandler.findMembershipsByGroup(g).size(), 0);
 
    }
 
    /**
     * Find all memberships by user.
     */
    public void testFindMembershipsByUser() throws Exception
    {
       assertEquals(mHandler.findMembershipsByUser("john").size(), 3);
       assertEquals(mHandler.findMembershipsByUser("not-existed-user").size(), 0);
    }
 
    /**
     * Find all membership by user and group.
     */
    public void testFindMembershipsByUserAndGroup() throws Exception
    {
       assertEquals(mHandler.findMembershipsByUserAndGroup("john", "/platform/users").size(), 1);
 
       // try to find not existed membership. We are supposed to get null instead of Exception
       try
       {
          assertEquals(mHandler.findMembershipsByUserAndGroup("non-existed-john", "/platform/users").size(), 0);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
 
       try
       {
          assertEquals(mHandler.findMembershipsByUserAndGroup("john", "/non-existed-group").size(), 0);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Link membership.
     */
    public void testLinkMembership() throws Exception
    {
       createUser(userName);
       createGroup(null, groupName1, "lable", "desc");
       createMembershipType(membershipType, "desc");
 
       // link membership
       mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1), mtHandler
                .findMembershipType(membershipType), true);
 
       Membership m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);
       assertNotNull(m);
 
       // try to create already existed membership. Exception should not be thrown
       try
       {
          mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1),
             mtHandler.findMembershipType(membershipType), true);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
 
       // we expect only 1 membership record
       assertEquals(1, mHandler.findMembershipsByUser(userName).size());
 
       // test deprecated memthod create membership
       mHandler.removeMembership(m.getId(), true);
       mHandler.createMembership(m, true);
       m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);
       assertNotNull(m);
 
       // try to link membership with not existed entries. We are supposed to get Exception
       Group group = createGroupInstance(null, "not-existed-group", "lable", "desc");
       try
       {
          mHandler.linkMembership(uHandler.findUserByName(userName), group,
                   mtHandler.findMembershipType(membershipType), true);
          fail("Exception  should be thrown");
       }
       catch (Exception e)
       {
       }
 
       User user = uHandler.createUserInstance("not-existed-user");
       try
       {
          mHandler.linkMembership(user, gHandler.findGroupById("/" + groupName1), mtHandler
                   .findMembershipType(membershipType), true);
          fail("Exception  should be thrown");
       }
       catch (Exception e)
       {
       }
 
       MembershipType mt = mtHandler.createMembershipTypeInstance();
       mt.setName("not-existed-mt");
       try
       {
          mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1), mt, true);
          fail("Exception  should be thrown");
       }
       catch (Exception e)
       {
       }
 
       try
       {
          mHandler.linkMembership(uHandler.findUserByName(userName), null, mtHandler.findMembershipType(membershipType),
                   true);
          fail("Exception  should be thrown");
       }
       catch (Exception e)
       {
       }
 
       try
       {
          mHandler.linkMembership(null, gHandler.findGroupById("/" + groupName1), mtHandler
                   .findMembershipType(membershipType), true);
          fail("Exception  should be thrown");
       }
       catch (Exception e)
       {
       }
 
       try
       {
          mHandler.linkMembership(uHandler.findUserByName(userName), gHandler.findGroupById("/" + groupName1), null,
                   true);
          fail("Exception  should be thrown");
       }
       catch (Exception e)
       {
       }
    }
 
    /**
     * Remove membership
     */
    public void testRemoveMembership() throws Exception
    {
 
       createMembership(userName, groupName1, membershipType);
       Membership m = mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType);
 
       assertNotNull(m);
 
       m = mHandler.removeMembership(m.getId(), true);
       assertEquals(m.getGroupId(), "/" + groupName1);
       assertEquals(m.getMembershipType(), membershipType);
       assertEquals(m.getUserName(), userName);
 
       assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));
 
       
       // try to remove not existed membership. We are supposed to get "null" instead of Exception
       try
       {
          assertNull(mHandler.removeMembership("not-existed-id", true));
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Remove membership by user.
     */
    public void testRemoveMembershipByUser() throws Exception
    {
       createMembership(userName, groupName1, membershipType);
 
       assertEquals(mHandler.removeMembershipByUser("user", true).size(), 1);
       assertNull(mHandler.findMembershipByUserGroupAndType("user", "/group", "type"));
 
       // try to remove memberships by not existed users. We are supposed to get empty list instead of Exception
       try
       {
          assertEquals(mHandler.removeMembershipByUser("not-existed-user", true).size(), 0);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Find group by membership.
     */
    public void testFindGroupByMembership() throws Exception
    {
       assertEquals(gHandler.findGroupByMembership("john", "manager").size(), 1);
 
       // try to find groups by not existed entries. We supposed to get empty list instead of Exception
       try
       {
          assertEquals(gHandler.findGroupByMembership("not-existed-john", "manager").size(), 0);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
 
 
    }
 
    /**
     * Find groups of user.
     */
    public void testFindGroupsOfUser() throws Exception
    {
       assertEquals(gHandler.findGroupsOfUser("john").size(), 3);
 
       // try to find groups by not existed entries. We supposed to get empty list instead of Exception
       try
       {
          assertEquals(gHandler.findGroupsOfUser("not-existed-james").size(), 0);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Find users by group.
     */
    public void testFindUsersByGroupId() throws Exception
    {
       ListAccess<User> usersListAccess = uHandler.findUsersByGroupId("/platform/users");
 
       assertEquals(usersListAccess.getSize(), 4);
 
       for (User u : usersListAccess.load(0, usersListAccess.getSize()))
       {
          User currentUrer = uHandler.findUserByName(u.getUserName());
          assertNotNull(currentUrer);
          
          assertEquals(currentUrer.getUserName(), u.getUserName());
          assertEquals(currentUrer.getFirstName(), u.getFirstName());
          assertEquals(currentUrer.getLastName(), u.getLastName());
          assertEquals(currentUrer.getEmail(), u.getEmail());
          assertEquals(currentUrer.getOrganizationId(), u.getOrganizationId());
          assertEquals(currentUrer.getPassword(), u.getPassword());
         assertEquals(currentUrer.getCreatedDate().toString(), u.getCreatedDate().toString());
       }
 
       // try to find users by not existed entries. We supposed to get empty list instead of Exception
       try
       {
          assertEquals(uHandler.findUsersByGroupId("/not-existed-group").getSize(), 0);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Find users by group.
     */
    public void testFindUsersByGroup() throws Exception
    {
       PageList<User> usersList = uHandler.findUsersByGroup("/platform/users");
       
       assertEquals(usersList.getAll().size(), 4);
 
       for (User u : usersList.getAll())
       {
          User currentUrer = uHandler.findUserByName(u.getUserName());
          assertNotNull(currentUrer);
 
          assertEquals(currentUrer.getUserName(), u.getUserName());
          assertEquals(currentUrer.getFirstName(), u.getFirstName());
          assertEquals(currentUrer.getLastName(), u.getLastName());
          assertEquals(currentUrer.getEmail(), u.getEmail());
          assertEquals(currentUrer.getOrganizationId(), u.getOrganizationId());
          assertEquals(currentUrer.getPassword(), u.getPassword());
         assertEquals(currentUrer.getCreatedDate().toString(), u.getCreatedDate().toString());
       }
 
       // try to find users by not existed entries. We supposed to get empty list instead of Exception
       try
       {
          assertEquals(uHandler.findUsersByGroup("/not-existed-group").getAll().size(), 0);
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Remove membership type.
     */
    public void testRemoveMembershipType() throws Exception
    {
       createMembership(userName, groupName1, membershipType);
 
       mtHandler.removeMembershipType("type", true);
       assertNull(mtHandler.findMembershipType("type"));
       assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));
    }
 
    /**
     * Remove group.
     */
    public void testRemoveGroup() throws Exception
    {
       createMembership(userName, groupName1, membershipType);
 
       gHandler.removeGroup(gHandler.findGroupById("/" + groupName1), true);
 
       assertNull(gHandler.findGroupById("/" + groupName1));
       assertNull(mHandler.findMembershipByUserGroupAndType(userName, "/" + groupName1, membershipType));
 
 
       // try to remove not existed groups. We are supposed to get Exception
       try
       {
          Group group = createGroupInstance(null, "not-existed-group", "lable", "desc");
 
          gHandler.removeGroup(group, true);
 
          fail("Exception should be thrown");
       }
       catch (Exception e)
       {
       }
 
       try
       {
          gHandler.removeGroup(null, true);
          fail("Exception should be thrown");
       }
       catch (Exception e)
       {
       }
    }
 
    /**
     * Test get listeners.
     */
    public void testGetListeners() throws Exception
    {
       if (mHandler instanceof MembershipEventListenerHandler)
       {
          List<MembershipEventListener> list = ((MembershipEventListenerHandler) mHandler).getMembershipListeners();
          try
          {
             list.clear();
             fail("We are not supposed to change list of listners");
          }
          catch (Exception e)
          {
          }
       }
    }
 }
