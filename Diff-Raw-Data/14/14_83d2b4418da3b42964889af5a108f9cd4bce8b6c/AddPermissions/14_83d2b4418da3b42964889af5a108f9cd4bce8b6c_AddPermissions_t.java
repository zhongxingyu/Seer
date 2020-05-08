 /*
  * Copyright (c) 2006 Oliver Stewart.  All Rights Reserved.
  *
  * This file is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2, or (at your option)
  * any later version.
  *
  * This file is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 package com.trailmagic.image.util;
 
 import com.trailmagic.image.Image;
 import com.trailmagic.image.ImageFactory;
 import com.trailmagic.image.ImageFrame;
 import com.trailmagic.image.ImageGroup;
 import com.trailmagic.image.ImageGroupFactory;
 import com.trailmagic.image.ImageManifestation;
 import com.trailmagic.image.security.ImageSecurityFactory;
 import com.trailmagic.user.User;
 import com.trailmagic.user.UserFactory;
 import com.trailmagic.user.UserLoginModule;
 import com.trailmagic.user.Group;
 import com.trailmagic.user.GroupFactory;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.acegisecurity.acl.basic.SimpleAclEntry;
 import org.apache.log4j.Logger;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.orm.hibernate3.SessionFactoryUtils;
 
 public class AddPermissions {
     private ImageFactory m_imageFactory;
     private ImageGroupFactory m_imageGroupFactory;
     private ImageSecurityFactory m_imageSecurityFactory;
     private UserFactory m_userFactory;
     private GroupFactory m_groupFactory;
     private SessionFactory m_sessionFactory;
     private HibernateTemplate m_hibernateTemplate;
 
     private static Logger s_log = Logger.getLogger(AddPermissions.class);
 
     private static final String ADD_PERMISSIONS_BEAN = "addPermissions";
     private static final String EVERYONE_GROUP_NAME = "everyone";
 
     public ImageGroupFactory getImageGroupFactory() {
         return m_imageGroupFactory;
     }
 
     public void setImageGroupFactory(ImageGroupFactory factory) {
         m_imageGroupFactory = factory;
     }
 
     public ImageSecurityFactory getImageSecurityFactory() {
         return m_imageSecurityFactory;
     }
 
     public void setImageSecurityFactory(ImageSecurityFactory factory) {
         m_imageSecurityFactory = factory;
     }
 
     public void setImageFactory(ImageFactory factory) {
         m_imageFactory = factory;
     }
 
     public UserFactory getUserFactory() {
         return m_userFactory;
     }
 
     public void setUserFactory(UserFactory factory) {
         m_userFactory = factory;
     }
 
     public SessionFactory getSessionFactory() {
         return m_sessionFactory;
     }
 
     public void setSessionFactory(SessionFactory sf) {
         m_sessionFactory = sf;
     }
 
     public HibernateTemplate getHibernateTemplate() {
         return m_hibernateTemplate;
     }
 
     public void setHibernateTemplate(HibernateTemplate template) {
         m_hibernateTemplate = template;
     }
 
     public void setGroupFactory(GroupFactory factory) {
         m_groupFactory = factory;
     }
 
     public void makeEverythingPublic(final String ownerName) {
         m_hibernateTemplate.execute(new HibernateCallback() {
                 public Object doInHibernate(Session session) {
                     User owner = m_userFactory.getByScreenName(ownerName);
 
                     Collection<ImageGroup> imageGroups =
                         m_imageGroupFactory
                         .getAlbumsByOwnerScreenName(ownerName);
                     imageGroups.addAll(m_imageGroupFactory
                                        .getRollsByOwnerScreenName(ownerName));
 
                     for (ImageGroup group : imageGroups) {
                         makeGroupPublic(group);
                     }
                     return null;
                 }
             });
     }
 
     // assumes within a session
     private void makeGroupPublic(ImageGroup group) {
         m_imageSecurityFactory.makePublic(group);
         s_log.info("Added public permission for group: "
                    + group.getName());
 
         Collection<ImageFrame> frames = group.getFrames();
 
         for (ImageFrame frame : frames) {
             m_imageSecurityFactory.makePublic(frame);
             s_log.info("Added public permission for frame: "
                        + frame.getPosition() + " of group "
                        + group.getName());
 
             Image image = frame.getImage();
             m_imageSecurityFactory.makePublic(image);
             s_log.info("Added public permission for image: "
                        + image.getDisplayName());
 
             for (ImageManifestation mf : image.getManifestations()) {
                 m_imageSecurityFactory.makePublic(mf);
                 s_log.info("Added public permission for "
                            + "manifestation: "
                            + mf.getHeight() + "x" + mf.getWidth());
             }
         }
     }
 
     public void makeImageGroupsPublic(String ownerName, String type,
                                       Collection<String> groupNames) {
         Session session =
             SessionFactoryUtils.getSession(m_sessionFactory, false);
         Transaction transaction = null;
 
         try {
             transaction = session.beginTransaction();
 
             User owner = m_userFactory.getByScreenName(ownerName);
             for (String groupName : groupNames) {
                 ImageGroup group;
                 if ("roll".equals(type)) {
                     group =
                         m_imageGroupFactory.getRollByOwnerAndName(owner,
                                                                   groupName);
		    if (group == null) {
			s_log.error("No roll found with name " + groupName
				    + " owned by " + owner);
		    }
                 } else if ("album".equals(type)) {
                     group =
                         m_imageGroupFactory.getAlbumByOwnerAndName(owner,
                                                                    groupName);
			s_log.error("No album found with name " + groupName
				    + " owned by " + owner);
                 } else {
                     throw new IllegalArgumentException("invalid type");
                 }
                 makeGroupPublic(group);
             }
             transaction.commit();
             SessionFactoryUtils.releaseSession(session,
                                                m_sessionFactory);
         } catch (Exception e) {
             try {
                 if (transaction != null) {
                     transaction.rollback();
                     s_log.error("Exception adding acls; "
                                 + "transaction rolled back",
                                 e);
                 }
             } catch (HibernateException e1) {
                 s_log.error("Couldn't roll back transaction", e1);
             }
         }
     }
 
     public void addAllOwnerPermissions(final String ownerName) {
         m_hibernateTemplate.execute(new HibernateCallback() {
                 public Object doInHibernate(Session session) {
                     List<ImageGroup> groups =
                         m_imageGroupFactory
                         .getRollsByOwnerScreenName(ownerName);
                     groups.addAll(m_imageGroupFactory
                                   .getAlbumsByOwnerScreenName(ownerName));
                     for (ImageGroup group : groups) {
                         m_imageSecurityFactory.addOwnerAcl(group);
                         for (ImageFrame frame : group.getFrames()) {
                             m_imageSecurityFactory.addOwnerAcl(frame);
                             Image image = frame.getImage();
                             m_imageSecurityFactory.addOwnerAcl(image);
                             for (ImageManifestation mf
                                      : image.getManifestations()) {
                                 m_imageSecurityFactory.addOwnerAcl(mf);
                             }
                         }
                     }
                     return null;
                 }
             });
     }
 
     public void fixFramePermissions() {
         m_hibernateTemplate.execute(new HibernateCallback() {
                 public Object doInHibernate(Session session) {
                     List<ImageGroup> groups =
                         m_imageGroupFactory.getAll();
 
                     for (ImageGroup group : groups) {
                         for (ImageFrame frame : group.getFrames()) {
                             if (m_imageSecurityFactory.isPublic(frame
                                                                 .getImage())) {
                                 s_log.info("Making frame public: " + frame);
                                 m_imageSecurityFactory.makePublic(frame);
                             } else {
                                 s_log.info("Making frame private: " + frame);
                                 m_imageSecurityFactory.makePrivate(frame);
                             }
                         }
                     }
                     return null;
                 }
             });
     }
 
     public void makeAdminReadable(final String username,
                                   final Collection<Long> imageIds) {
         m_hibernateTemplate.execute(new HibernateCallback() {
                 public Object doInHibernate(Session session) {
                     User user = m_userFactory.getByScreenName(username);
                     for (Long id : imageIds) {
                         Image image = m_imageFactory.getById(id);
                         ImageGroup roll =
                             m_imageGroupFactory.getRollForImage(image);
                         m_imageSecurityFactory
                             .addPermission(image, roll, user,
                                            SimpleAclEntry.READ
                                            | SimpleAclEntry.ADMINISTRATION);
                     }
                     return null;
                 }
             });
     }
 
     public void makeReadable(final String username,
                              final Collection<Long> imageIds) {
         m_hibernateTemplate.execute(new HibernateCallback() {
                 public Object doInHibernate(Session session) {
                     User user = m_userFactory.getByScreenName(username);
                     for (Long id : imageIds) {
                         Image image = m_imageFactory.getById(id);
                         ImageGroup roll =
                             m_imageGroupFactory.getRollForImage(image);
                         m_imageSecurityFactory
                             .addPermission(image, roll, user,
                                            SimpleAclEntry.READ);
                     }
                     return null;
                 }
             });
     }
 
     public void addUserAccount(final String username,
                                final String password,
                                final String firstname,
                                final String lastname,
                                final String primaryEmail) {
         m_hibernateTemplate.execute(new HibernateCallback() {
                 public Object doInHibernate(Session session) {
                     try {
                         s_log.info("Adding account: " + username);
                         User user = new User();
                         user.setScreenName(username);
                         user.setFirstName(firstname);
                         user.setLastName(lastname);
                         user.setPrimaryEmail(primaryEmail);
                         user.setPassword(UserLoginModule
                                          .encodePassword(password.toCharArray()));
                         session.saveOrUpdate(user);
                         Group everyoneGroup =
                             m_groupFactory.getByName(EVERYONE_GROUP_NAME);
                         everyoneGroup.addUser(user);
                     } catch (Exception e) {
                         s_log.error("Error adding account", e);
                     }
                     return null;
                 }
             });
     }
 
     public void addAccounts(final String filename) {
         try {
             final BufferedReader in =
                 new BufferedReader(new FileReader(filename));
             for (String input = in.readLine();
                  input != null;
                  input = in.readLine()) {
                 StringTokenizer st = new StringTokenizer(input,",");
                 try {
                     String sname = st.nextToken();
                     addUserAccount(sname,
                                    st.nextToken(),
                                    st.nextToken(),
                                    st.nextToken(),
                                    st.nextToken());
                     s_log.info("Added account: " + sname);
                 } catch (NoSuchElementException e) {
                     s_log.error("Invalid row:" + input);
                 }
             }
             s_log.info("Finished reading input.");
         } catch (IOException e) {
             s_log.error("Error processing input", e);
         }
     }
 
     private static void printUsage() {
         System.err.println("Usage: AddPermissions <command> <owner> [<roll-name>]");
     }
 
     public static final void main(String[] args) {
         ClassPathXmlApplicationContext appContext =
             new ClassPathXmlApplicationContext(new String[]
                 {"applicationContext-global.xml",
                  "applicationContext-user.xml",
                  "applicationContext-imagestore.xml",
                  "applicationContext-imagestore-authorization.xml",
                  "applicationContext-standalone.xml"});
 
         AddPermissions ap =
             (AddPermissions) appContext.getBean(ADD_PERMISSIONS_BEAN);
 
         String command = args[0];
         if ("fixFramePermissions".equals(command)) {
             ap.fixFramePermissions();
         } else if ("addAllOwnerPermissions".equals(command)) {
             ap.addAllOwnerPermissions(args[1]);
         } else if ("addUserAccount".equals(command)) {
             ap.addUserAccount(args[1], args[2], args[3], args[4], args[5]);
         } else if ("makeAdminReadable".equals(command)) {
             ArrayList<Long> imageIds = new ArrayList<Long>();
             for (int i = 2; i < args.length; i++) {
                 imageIds.add(new Long(args[i]));
             }
             ap.makeAdminReadable(args[1], imageIds);
         } else if ("makeReadableBy".equals(command)) {
             ArrayList<Long> imageIds = new ArrayList<Long>();
             for (int i = 2; i < args.length; i++) {
                 imageIds.add(new Long(args[i]));
             }
             ap.makeReadable(args[1], imageIds);
         } else if ("addAccounts".equals(command)) {
             ap.addAccounts(args[1]);
         } else if ("makeImageGroupsPublic".equals(command)) {
             ArrayList<String> groups = new ArrayList<String>();
            for (int i = 3; i < args.length; i++) {
                 groups.add(args[i]);
             }
             // username, type, groups...
             ap.makeImageGroupsPublic(args[1], args[2], groups);
         } else if ("makeEverythingPublic".equals(command)) {
             ap.makeEverythingPublic(args[1]);
         } else {
             printUsage();
             System.exit(1);
         }
     }
}
