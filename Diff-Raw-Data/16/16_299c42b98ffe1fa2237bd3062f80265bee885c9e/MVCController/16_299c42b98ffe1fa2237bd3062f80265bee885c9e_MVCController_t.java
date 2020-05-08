 package com.gravical.bwell.controller;
 
 import com.gravical.bwell.db.PopulateSampleData;
 import com.gravical.bwell.models.Sessions;
 import com.gravical.bwell.models.Users;
 //import com.gravical.bwell.models.Roles;
 //import com.gravical.bwell.models.Users;
 import java.awt.event.*;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 /**
  * MVCController is the Controller for this MVC-style java application. This
  * class holds Event Handlers for all GUI elements.
  * 
 *
  */
 public class MVCController {
 
     public static Users loggedInUser;
     
     /**
      * @return the users
      */
     public static ArrayList getUsers() {
         return users;
     }
 
     /**
      * @param aUsers the users to set
      */
     public static void setUsers(ArrayList aUsers) {
         users = aUsers;
     }
 
     /**
      * @return the roles
      */
     public static ArrayList getRoles() {
         return roles;
     }
 
     /**
      * @param aRoles the roles to set
      */
     public static void setRoles(ArrayList aRoles) {
         roles = aRoles;
     }
 //  private views.Frame frame = new views.Frame();
     private JFrame frame = new JFrame();
     private com.gravical.bwell.views.LoginPanel loginPanel = new com.gravical.bwell.views.LoginPanel();
     private com.gravical.bwell.views.MainMenuPanel mainMenuPanel = new com.gravical.bwell.views.MainMenuPanel();
     private com.gravical.bwell.views.UserAdministrationPanel userAdministrationPanel = new com.gravical.bwell.views.UserAdministrationPanel();
     private com.gravical.bwell.views.SupervisePanel supervisePanel = new com.gravical.bwell.views.SupervisePanel();
     private com.gravical.bwell.views.SuperviseEncounterPanel superviseEncounterPanel = new com.gravical.bwell.views.SuperviseEncounterPanel();
     private com.gravical.bwell.views.SettingsPanel settingsPanel = new com.gravical.bwell.views.SettingsPanel();
     private com.gravical.bwell.views.ReviewEncountersPanel reviewEncountersPanel = new com.gravical.bwell.views.ReviewEncountersPanel();
     private com.gravical.bwell.views.UserProfilePanel userProfilePanel = new com.gravical.bwell.views.UserProfilePanel();
     private com.gravical.bwell.views.StartSessionPanel startSessionPanel = new com.gravical.bwell.views.StartSessionPanel();
     private com.gravical.bwell.views.ReviewSessionPanel reviewSessionPanel = new com.gravical.bwell.views.ReviewSessionPanel();
     private com.gravical.bwell.views.AddClientPanel addClientPanel = new com.gravical.bwell.views.AddClientPanel();
     private com.gravical.bwell.views.InRoomSessionPanel inRoomSessionPanel = new com.gravical.bwell.views.InRoomSessionPanel();
     private com.gravical.bwell.views.RemoteSessionPanel remoteSessionPanel = new com.gravical.bwell.views.RemoteSessionPanel();
     private com.gravical.bwell.views.AdminMenuPanel adminMenuPanel = new com.gravical.bwell.views.AdminMenuPanel();
     private com.gravical.bwell.views.AdminRolesPanel adminRolesPanel = new com.gravical.bwell.views.AdminRolesPanel();
 //    private com.gravical.bwell.views.DeleteMePanel startSessionPanel = new com.gravical.bwell.views.DeleteMePanel();
     private PopulateSampleData populateSampleData;
     private static ArrayList users;
     private static ArrayList roles;
 
     {
         populateSampleData = new PopulateSampleData();
         this.setRoles(PopulateSampleData.getRoles());
         this.setUsers(PopulateSampleData.getUsers());
         System.out.println("MVCController users.size() == " + users.size());
     }
 
 //  private models.Data data = new models.Data();
     public void init() {
         // TODO: Fix hardcoded user and implement security system
         loggedInUser = new Users(1, "us", "er1", 1, "user1", "password");
 
 
         frame.setSize(800, 600);
         frame.setContentPane(loginPanel);
         //frame.add(loginPanel);
         frame.setVisible(true);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
         // Listens for user pressing any Logout button
         ActionListener logoutActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 // TODO: Write logout code
                 changeContentPane(loginPanel);
             }
         };
 
         // Listen for user request to go to Home, aka Main Menu
         ActionListener homeActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(mainMenuPanel);
             }
         };
 
         // Listen for user request to change Media settings
         ActionListener settingsActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(settingsPanel);
             }
         };
 
         // Listen for user request to go to admin menu
         ActionListener adminMenuActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(adminMenuPanel);
             }
         };
 
         // Listen for user request to edit roles (admin only)
         ActionListener adminRolesActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(adminRolesPanel);
             }
         };
 
         // Listen for user request to edit roles (admin only)
         ActionListener executeRolesQueryActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
 
                 System.out.println("ExecuteRolesQueryActionListener");
                 adminRolesPanel.loadRoles();
             }
         };
 
         // Listen for user request to update roles (admin only)
         ActionListener updateQueryActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
 
                 System.out.println("updateQueryActionListener update!");
                 adminRolesPanel.saveChanges();
             }
         };
 
 
 //showRolesButtonActionListener    
 
         // Listen for user request to visit User Profile screen
         ActionListener userProfileActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(userProfilePanel);
             }
         };
 
         // Bring up the screen to add a new client.
         ActionListener addClientActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(inRoomSessionPanel);
             }
         };
 
         // Review a previous encounter
         ActionListener reviewEncounterActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(reviewSessionPanel);
             }
         };
 
 
         // Start a remote session.  Multipoint not yet supported
         ActionListener startRemoteSessionActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(remoteSessionPanel);
             }
         };
 
 
         // Start an in-room session.  Probably will be recorded.
         ActionListener startInRoomSessionActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 int rowSelected = 0;
                 rowSelected = startSessionPanel.clientsTable.getSelectedRow();
                 Users user = (Users) startSessionPanel.usersList.get(rowSelected);
                 int userIdSelected = user.getUserId();
                 System.out.println("startInRoomSessionActionListener detected userIdSelected =" + userIdSelected);
                 inRoomSessionPanel.cameraTwoLabel.setText(user.getFirstName() + " " + user.getLastName());
                 inRoomSessionPanel.client = user;
                 inRoomSessionPanel.startInRoomSession();
                 changeContentPane(inRoomSessionPanel);
             }
         };
 
         // Start an in-room session.  Probably will be recorded.
         ActionListener remoteSessionActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 System.out.println("startRemoteSessionActionListener");
                 changeContentPane(remoteSessionPanel);
             }
         };
 
 
 
 
         // this will be called by a successful login attempt.
         ActionListener successfulLoginActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 System.out.println("successfulLoginActionListener");
                 changeContentPane(mainMenuPanel);
             }
         };
 
         // this will be called by an admin user request to edit users
         ActionListener usersAdministrationActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(userAdministrationPanel);
             }
         };
 
         // this will be called by a user wishing to review bwellSession
         ActionListener reviewSessionActionListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                //TODO: Big performance gain in moving this to only hit DB on actual Panel load
                reviewEncountersPanel.loadBwellSessions();     
                 changeContentPane(reviewEncountersPanel);
             }
         };
 
 
 
         loginPanel.GotoMainMenuActionListener(successfulLoginActionListener);
 
         mainMenuPanel.LogoutButtonActionListener(logoutActionListener);
 
         mainMenuPanel.AdminButtonActionListener(adminMenuActionListener);
 
         mainMenuPanel.SuperviseButtonActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(supervisePanel);
             }
         });
 
         mainMenuPanel.ReviewOldSessionsButtonActionListener(reviewSessionActionListener);
 
         mainMenuPanel.SessionButtonActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(startSessionPanel);
             }
         });
 
         adminMenuPanel.AdminUsersButtonActionListener(usersAdministrationActionListener);
         adminMenuPanel.editRolesButtonActionListener(adminRolesActionListener);
 
         settingsPanel.HomeButtonActionListener(homeActionListener);
 
         startSessionPanel.HomeButtonActionListener(homeActionListener);
         startSessionPanel.startInRoomSessionButtonActionListener(startInRoomSessionActionListener);
         startSessionPanel.startRemoteSessionButtonActionListener(startRemoteSessionActionListener);
 
         this.inRoomSessionPanel.HomeButtonActionListener(homeActionListener);
         this.remoteSessionPanel.HomeButtonActionListener(homeActionListener);
 
         userAdministrationPanel.HomeButtonActionListener(homeActionListener);
         userAdministrationPanel.SettingsButtonActionListener(settingsActionListener);
         userAdministrationPanel.LogoutButtonActionListener(logoutActionListener);
         userAdministrationPanel.UserProfileButtonActionListener(userProfileActionListener);
 //        startSessionPanel.HomeButtonActionListener(homeActionListener);
 //        startSessionPanel.SettingsButtonActionListener(settingsActionListener);
 //        startSessionPanel.LogoutButtonActionListener(logoutActionListener);
 //        startSessionPanel.UserProfileButtonActionListener(userProfileActionListener);
 
 //        startSessionPanel.AddPatientButtonActionListener(addClientActionListener);
 //        startSessionPanel.StartSessionButtonActionListener(startSessionActionListener);
 //        startSessionPanel.StartInRoomSessionButtonActionListener(startInRoomSessionActionListener);
 
 //        startSessionPanel.HomeButtonActionListener(homeActionListener);
         adminRolesPanel.adminMenuButtonActionListener(adminMenuActionListener);
         adminRolesPanel.mainMenuButtonActionListener(homeActionListener);
         adminRolesPanel.executeRolesQueryActionListener(executeRolesQueryActionListener);
         adminRolesPanel.updateQueryActionListener(updateQueryActionListener);
 
 
         addClientPanel.HomeButtonActionListener(homeActionListener);
 
 
 //        liveSessionPanel.HomeButtonActionListener(homeActionListener);
 
         supervisePanel.HomeButtonActionListener(homeActionListener);
         supervisePanel.UserProfileButtonActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(userProfilePanel);
             }
         });
 
         supervisePanel.SettingsButtonActionListener(settingsActionListener);
 
         supervisePanel.LogoutButtonActionListener(logoutActionListener);
 
         supervisePanel.SuperviseEncounterButtonActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 changeContentPane(superviseEncounterPanel);
             }
         });
 
         supervisePanel.ForceCloseButtonActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 throw new UnsupportedOperationException("Force Close Not supported yet.");
             }
         });
 
         supervisePanel.CancelButtonActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 throw new UnsupportedOperationException("Cancel Not supported yet.");
             }
         });
 
         reviewEncountersPanel.HomeButtonActionListener(homeActionListener);
         reviewEncountersPanel.LogoutActionListener(logoutActionListener);
         reviewEncountersPanel.SettingsActionListener(settingsActionListener);
         reviewEncountersPanel.CancelButtonActionListener(homeActionListener);
         reviewEncountersPanel.UserProfileActionListener(userProfileActionListener);
         reviewEncountersPanel.ReviewEncounterActionListener(reviewEncounterActionListener);
         reviewSessionPanel.ReviewMoreSessionsActionListener(reviewEncounterActionListener);
         settingsPanel.ApplySettingsButtonActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 throw new UnsupportedOperationException("Apply Settings Not supported yet.");
             }
         });
 
         settingsPanel.CancelButtonActionListener(homeActionListener);
         userProfilePanel.HomeButtonActionListener(homeActionListener);
         superviseEncounterPanel.HomeButtonActionListener(homeActionListener);
         reviewSessionPanel.HomeButtonActionListener(homeActionListener);
     }
 
     private void changeContentPane(JPanel newPanel) {
         frame.setSize(800, 601);
         frame.setSize(800, 600);
         //frame.remove(mainMenuPanel);
         frame.add(newPanel);
         frame.setContentPane(newPanel);
     }
 
     @SuppressWarnings("CallToThreadDumpStack")
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             new MVCController().init();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * @return the populateSampleData
      */
     public PopulateSampleData getPopulateSampleData() {
         return populateSampleData;
     }
 }
