 package de.fu_berlin.inf.dpp.stf.sarosswtbot;
 
 import java.rmi.AlreadyBoundException;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.List;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.log4j.Logger;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
 import org.eclipse.swtbot.swt.finder.SWTBot;
 import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 
 import de.fu_berlin.inf.dpp.stf.conditions.SarosConditions;
 import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
 import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;
 
 /**
  * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
  * exports {@link ISarosState} via RMI. You should not use this within tests.
  * Have a look at {@link Musician} if you want to write tests.
  */
 public class SarosRmiSWTWorkbenchBot extends RmiSWTWorkbenchBot implements
     ISarosRmiSWTWorkbenchBot {
     private static final transient Logger log = Logger
         .getLogger(SarosRmiSWTWorkbenchBot.class);
 
     public final static transient String TEMPDIR = System
         .getProperty("java.io.tmpdir");
 
     private static transient SarosRmiSWTWorkbenchBot self;
 
     /** RMI exported Saros state object */
     private ISarosState state;
 
     /** SarosRmiSWTWorkbenchBot is a singleton */
     public static SarosRmiSWTWorkbenchBot getInstance() {
         if (delegate != null && self != null)
             return self;
 
         SarosSWTWorkbenchBot swtwbb = new SarosSWTWorkbenchBot();
         self = new SarosRmiSWTWorkbenchBot(swtwbb);
         return self;
     }
 
     /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
     protected SarosRmiSWTWorkbenchBot(SarosSWTWorkbenchBot bot) {
         super(bot);
     }
 
     /**
      * Export given state object by given name on our local RMI Registry.
      */
     public void exportState(SarosState state, String exportName) {
         try {
             this.state = (ISarosState) UnicastRemoteObject.exportObject(state,
                 0);
             addShutdownHook(exportName);
             registry.bind(exportName, this.state);
         } catch (RemoteException e) {
             log.error("Could not export stat object.", e);
         } catch (AlreadyBoundException e) {
             log.error(
                 "Could not bind stat object, because it is bound already.", e);
         }
     }
 
     /***************** confirm ****************/
 
     /*************** Saros-specific-highlevel RMI exported Methods ******************/
 
     public void confirmCreateNewUserAccountWindow(String server,
         String username, String password) throws RemoteException {
         try {
             activateShellWithText("Create New User Account");
             setTextWithLabel("Jabber Server", server);
             setTextWithLabel("Username", username);
             setTextWithLabel("Password", password);
             setTextWithLabel("Repeat Password", password);
             clickButton(SarosConstant.BUTTON_FINISH);
         } catch (WidgetNotFoundException e) {
             log.error("widget not found while accountBySarosMenu", e);
         }
     }
 
     /**
      * First step: invitee acknowledge session to given inviter
      * 
      * This method captures two screenshots as side effect.
      */
     public void confirmSessionInvitationWindowStep1(String inviter)
         throws RemoteException {
         activateShellWithText(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
         if (!isTextWithLabelEqualWithText(SarosConstant.TEXT_LABEL_INVITER,
             inviter))
             log.warn("inviter does not match: " + inviter);
         captureScreenshot(TEMPDIR + "/acknowledge_project1.png");
         waitUntilButtonEnabled(SarosConstant.BUTTON_NEXT);
         clickButton(SarosConstant.BUTTON_NEXT);
         captureScreenshot(TEMPDIR + "/acknowledge_project2.png");
         waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
     }
 
     /**
      * Second step: invitee acknowledge a new project
      * 
      * This method captures two screenshots as side effect.
      */
     public void confirmSessionInvitationWindowStep2UsingNewproject(
         String projectName) throws RemoteException {
         clickRadio(SarosConstant.RADIO_LABEL_CREATE_NEW_PROJECT);
         captureScreenshot(TEMPDIR + "/acknowledge_project3.png");
         clickButton(SarosConstant.BUTTON_FINISH);
         captureScreenshot(TEMPDIR + "/acknowledge_project4.png");
         waitUntilShellCloses(getShellWithText(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
     }
 
     public void confirmSessionInvitationWindowStep2UsingExistProject(
         String projectName) throws RemoteException {
         clickRadio("Use existing project");
         delegate.sleep(sleepTime);
         clickButton("Browse");
         confirmWindowWithTree("Folder Selection", SarosConstant.BUTTON_OK,
             projectName);
         delegate.sleep(sleepTime);
         clickButton(SarosConstant.BUTTON_FINISH);
         delegate.sleep(sleepTime);
         confirmWindow("Warning: Local changes will be deleted",
             SarosConstant.BUTTON_YES);
         waitUntilShellCloses(getShellWithText(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
     }
 
     public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
         String projectName) throws RemoteException {
         clickRadio("Use existing project");
         delegate.sleep(sleepTime);
         clickCheckBox("Create copy for working distributed. New project name:");
         delegate.sleep(sleepTime);
         clickButton(SarosConstant.BUTTON_FINISH);
         waitUntilShellCloses(getShellWithText(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
     }
 
     /**
      * Fill up the configuration wizard with title "Saros Configuration".
      */
 
     public void confirmSarosConfigurationWindow(String xmppServer, String jid,
         String password) throws RemoteException {
         activateShellWithText(SarosConstant.SAROS_CONFI_SHELL_TITLE);
         setTextWithLabel(SarosConstant.TEXT_LABEL_JABBER_SERVER, xmppServer);
         delegate.sleep(sleepTime);
         setTextWithLabel(SarosConstant.TEXT_LABEL_USER_NAME, jid);
         delegate.sleep(sleepTime);
         setTextWithLabel(SarosConstant.TEXT_LABEL_PASSWORD, password);
         delegate.sleep(sleepTime);
 
         while (delegate.button("Next >").isEnabled()) {
             delegate.button("Next >").click();
             log.debug("click Next > Button.");
             delegate.sleep(sleepTime);
         }
 
         if (isButtonEnabled(SarosConstant.BUTTON_FINISH)) {
             clickButton(SarosConstant.BUTTON_FINISH);
             return;
         } else {
             System.out.println("can't click finish button");
         }
         throw new NotImplementedException(
             "only set text fields and click Finish is implemented.");
     }
 
     // public void addNewContact(String name) throws RemoteException {
     // if (!isRosterViewOpen())
     // addSarosSessionView();
     // clickToolbarButtonWithTooltipInViewWithTitle(
     // SarosConstant.VIEW_TITLE_ROSTER,
     // SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT);
     // activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);
     // setTextWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID, name);
     // waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
     // clickButton(SarosConstant.BUTTON_FINISH);
     // delegate.sleep(sleepTime);
     //
     // // // server respond with failure code 503, service unavailable, add
     // // // contact anyway
     // // try {
     // // delegate.shell("Contact look-up failed").activate();
     // // delegate.button("Yes").click();
     // // } catch (WidgetNotFoundException e) {
     // // // ignore, server responds
     // // }
     // }
 
     /****************** click widget *********************/
 
     public void clickSendAFileToSelectedUserInSPSView(String inviteeJID)
         throws RemoteException {
         selectTableItemWithLabelInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, inviteeJID);
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.TOOL_TIP_TEXT_SEND_FILE_TO_SELECTED_USER);
     }
 
     public void clickStartAVoIPSessionInSPSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.TOOL_TIP_TEXT_START_VOIP_SESSION);
     }
 
     public void clickNoInconsistenciesInSPSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.TOOL_TIP_TEXT_NO_INCONSISTENCIES);
     }
 
     public void clickRemoveAllRriverRolesInSPSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.TOOL_TIP_TEXT_REMOVE_ALL_DRIVER_ROLES);
     }
 
     public void clickEnableDisableFollowModeInSPSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.TOOL_TIP_TEXT_ENABLE_DISABLE_FOLLOW_MODE);
     }
 
     public void clickLeaveTheSessionInSPSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.TOOL_TIP_TEXT_LEAVE_THE_SESSION);
     }
 
     public void clickCMJumpToPositionOfSelectedUserInSPSView(
         String participantJID, String sufix) throws RemoteException {
         if (!isSharedSessionViewOpen())
             openSarosSessionView();
         clickContextMenuOfTableInView(BotConfiguration.NAME_SESSION_VIEW,
             participantJID + sufix,
             SarosConstant.CONTEXT_MENU_JUMP_TO_POSITION_SELECTED_USER);
     }
 
     public void clickCMStopFollowingThisUserInSPSView(String participantJID,
         String sufix) throws RemoteException {
         if (!isSharedSessionViewOpen())
             openSarosSessionView();
         clickContextMenuOfTableInView(BotConfiguration.NAME_SESSION_VIEW,
             participantJID + sufix,
             SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
     }
 
     public void clickCMgiveExclusiveDriverRoleInSPSView(String inviteeJID)
         throws RemoteException {
         if (!isSharedSessionViewOpen())
             openSarosSessionView();
         clickContextMenuOfTableInView(BotConfiguration.NAME_SESSION_VIEW,
             inviteeJID, SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
     }
 
     // public void clickCMgiveDriverRoleInSPSView(String inviteeJID)
     // throws RemoteException {
     //
     // }
 
     public void clickCMRemoveDriverRoleInSPSView(String inviteeJID)
         throws RemoteException {
         if (!isSharedSessionViewOpen())
             openSarosSessionView();
         clickContextMenuOfTableInView(BotConfiguration.NAME_SESSION_VIEW,
             inviteeJID, SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
     }
 
     /**
      * Roster must be open
      */
     public void clickTBConnectInSPSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(SarosConstant.VIEW_TITLE_ROSTER,
             SarosConstant.TOOL_TIP_TEXT_CONNECT);
     }
 
     /**
      * Roster must be open
      */
     public boolean clickTBDisconnectInSPSView() throws RemoteException {
         return clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_ROSTER,
             SarosConstant.TOOL_TIP_TEXT_DISCONNECT) != null;
 
     }
 
     /**
      * This method captures two screenshots as side effect.
      */
     public void clickCMShareProjectInPEView(String projectName,
         String nameOfContextMenu) throws RemoteException {
         clickContextMenuOfTreeInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
             nameOfContextMenu, projectName);
 
     }
 
     public void clickShareYourScreenWithSelectedUserInSPSView()
         throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.TOOL_TIP_TEXT_SHARE_SCREEN_WITH_USER);
     }
 
     public void clickStopSessionWithUserInSPSView(String name)
         throws RemoteException {
         // selectTableItemWithLabelInViewWithTitle(
         // SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, name);
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.TOOL_TIP_TEXT_STOP_SESSION_WITH_USER + " " + name);
     }
 
     public void clickChangeModeOfImageSourceInRSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
             SarosConstant.TOOL_TIP_TEXT_CHANGE_MODE_IMAGE_SOURCE);
     }
 
     public void clickStopRunningSessionInRSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
             SarosConstant.TOOL_TIP_TEXT_STOP_RUNNING_SESSION);
     }
 
     public void clickResumeInRSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
             SarosConstant.TOOL_TIP_TEXT_RESUME);
     }
 
     public void clickPauseInRSView() throws RemoteException {
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
             SarosConstant.TOOL_TIP_TEXT_PAUSE);
     }
 
     // public void ackContactAdded(String name) {
     // try {
     // delegate.shell("Request of subscription received").activate();
     // delegate.sleep(750);
     // delegate.button("OK").click();
     // delegate.sleep(750);
     // } catch (WidgetNotFoundException e) {
     // // ignore
     // }
     // }
 
     /************ open **************/
 
     public void openRosterView() throws RemoteException {
         openViewWithName(SarosConstant.VIEW_TITLE_ROSTER,
             SarosConstant.CATEGORY_SAROS, SarosConstant.NODE_ROSTER);
     }
 
     public void openSarosSessionView() throws RemoteException {
         openViewWithName(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.CATEGORY_SAROS, SarosConstant.NODE_SAROS_SESSION);
     }
 
     public void openChatView() throws RemoteException {
         openViewWithName(SarosConstant.VIEW_TITLE_CHAT_VIEW,
             SarosConstant.CATEGORY_SAROS, SarosConstant.NODE_CHAT_VIEW);
     }
 
     public void openRemoteScreenView() throws RemoteException {
         openViewWithName(SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
             SarosConstant.CATEGORY_SAROS, SarosConstant.NODE_REMOTE_SCREEN);
     }
 
     // public void addToSharedProject(String invitee) throws RemoteException {
     // activeViewWithTitle("Shared Project Session");
     // delegate.viewByTitle("Shared Project Session")
     // .toolbarButton("Open invitation interface").click();
     // selectCheckBoxWithText(invitee);
     // }
 
     // public boolean isConfigShellPoppedUp() throws RemoteException {
     // try {
     // delegate.shell("Saros Configuration");
     // return true;
     // } catch (WidgetNotFoundException e) {
     // // ignore
     // }
     // return false;
     // }
 
     /*************** is... ******************/
 
     public boolean isInFollowMode(String participantJID, String sufix)
         throws RemoteException {
         if (!isSharedSessionViewOpen())
             openSarosSessionView();
         activateViewWithTitle(BotConfiguration.NAME_SESSION_VIEW);
 
         return isContextMenuOfTableItemInViewExist(
             BotConfiguration.NAME_SESSION_VIEW, participantJID + sufix,
             SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
 
     }
 
     public boolean isConnectedByXmppGuiCheck() throws RemoteException {
         try {
             SWTBotToolbarButton toolbarButton = getToolbarButtonWithTooltipInView(
                 SarosConstant.VIEW_TITLE_ROSTER,
                 SarosConstant.TOOL_TIP_TEXT_DISCONNECT);
             return (toolbarButton != null && toolbarButton.isVisible());
         } catch (WidgetNotFoundException e) {
             return false;
         }
     }
 
     public boolean hasContactWith(String contact) throws RemoteException {
         if (!isRosterViewOpen())
            openRosterView();
         if (!isConnectedByXmppGuiCheck())
             clickTBConnectInSPSView();
         activateViewWithTitle(SarosConstant.VIEW_TITLE_ROSTER);
         SWTBotTreeItem contact_added = selectTreeWithLabelsInView(
             SarosConstant.VIEW_TITLE_ROSTER, "Buddies", contact);
         return contact_added != null && contact_added.getText().equals(contact);
         // try {
         // SWTBotTree tree = delegate.viewByTitle("Roster").bot().tree();
         // if (tree != null) {
         // SWTBotTreeItem buddy = tree.getTreeItem("Buddies");
         // SWTBotTreeItem contact_added = buddy.getNode(contact).select();
         // delegate.sleep(1000);
         // return contact_added != null
         // && contact_added.getText().equals(contact);
         // }
         // } catch (WidgetNotFoundException e) {
         // log.warn("Contact not found: " + contact, e);
         // }
         // return false;
     }
 
     /**
      * "Shared Project Session" View must be open
      */
     public boolean isInSession() {
         try {
             activateViewWithTitle("Shared Project Session");
             return delegate.viewByTitle("Shared Project Session")
                 .toolbarButton("Leave the session").isEnabled();
         } catch (RemoteException e) {
             return false;
         }
     }
 
     public boolean isContactOnline(String contact) {
         throw new NotImplementedException(
             "Can not be implemented, because no information is visible by swtbot. Enhance information with a tooltip or toher stuff.");
     }
 
     /**
      * Returns true if the given jid was found in Shared Project Session View.
      */
     public boolean isInSharedProject(String jid) {
         SWTBotView sessionView = delegate.viewByTitle("Shared Project Session");
         SWTBot bot = sessionView.bot();
 
         try {
             SWTBotTable table = bot.table();
             SWTBotTableItem item = table.getTableItem(jid);
             return item != null;
         } catch (WidgetNotFoundException e) {
             return false;
         }
     }
 
     public boolean isRosterViewOpen() throws RemoteException {
         return isViewOpen(SarosConstant.VIEW_TITLE_ROSTER);
     }
 
     public boolean isChatViewOpen() throws RemoteException {
         return isViewOpen(SarosConstant.VIEW_TITLE_CHAT_VIEW);
     }
 
     public boolean isRemoteScreenViewOpen() throws RemoteException {
         return isViewOpen(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
     }
 
     public boolean isSharedSessionViewOpen() throws RemoteException {
         return isViewOpen(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
     }
 
     /*********************** delete ************************/
     /**
      * Remove given contact from Roster, if contact was added before.
      */
     public void deleteContact(String contact) throws RemoteException {
         if (!hasContactWith(contact))
             return;
         try {
             clickContextMenuOfTreeInView(SarosConstant.VIEW_TITLE_ROSTER,
                 SarosConstant.CONTEXT_MENU_DELETE, SarosConstant.BUDDIES,
                 contact);
             // SWTBotTree tree = delegate.viewByTitle("Roster").bot().tree();
             // if (tree != null) {
             // SWTBotTreeItem buddy = tree.getTreeItem("Buddies");
             // SWTBotTreeItem item = buddy.getNode(contact).select();
             // // remove by context menu
             // delegate.sleep(750);
             // item.contextMenu("Delete").click();
             // delegate.sleep(750);
             // confirm delete
             waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
             confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
                SarosConstant.BUTTON_OK);
             // activateShellByText(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
             // delegate.sleep(sleepTime);
             // clickButton(SarosConstant.BUTTON_YES);
             // delegate.button("Yes").click();
 
             // send backspace
             // item.pressShortcut(0, '\b'); // 0 == don't add keystroke
 
             delegate.sleep(sleepTime);
             // }
         } catch (WidgetNotFoundException e) {
             log.info("Contact not found: " + contact, e);
         }
     }
 
     // /**
     // * Create a {@link ISarosSession} using context menu off the given project
     // * on package explorer view.
     // */
     // public void clickProjectContextMenu(String projectName,
     // String nameOfContextMenu) throws RemoteException {
     // SWTBotView view = delegate.viewByTitle("Package Explorer");
     // SWTBotTree tree = view.bot().tree().select(projectName);
     // SWTBotTreeItem item = tree.getTreeItem(projectName).select();
     // SWTBotMenu menu = item.contextMenu(nameOfContextMenu);
     // menu.click();
     // }
 
     /***************** share *******************/
     public void shareProjectSequential(String projectName,
         String nameOfContextMenu, List<String> invitees) throws RemoteException {
         clickCMShareProjectInPEView(projectName, nameOfContextMenu);
         for (String toInvite : invitees)
             inviteUserToProject(toInvite);
     }
 
     /**
      * This method captures two screenshots as side effect.
      */
     public void shareProjectParallel(String projectName, List<String> invitees)
         throws RemoteException {
         clickContextMenuOfTreeInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
             SarosConstant.SHARE_PROJECT, projectName);
         // clickProjectContextMenu(projectName, "Share project...");
         captureScreenshot(TEMPDIR + "/shareProjectStepParallel1.png");
         selectCheckBoxWithList(invitees);
         captureScreenshot(TEMPDIR + "/shareProjectStepParallel2.png");
         // delegate.button("Finish").click();
         clickButton(SarosConstant.BUTTON_FINISH);
     }
 
     /*********** not exported Helper Methods *****************/
 
     // protected SWTBotToolbarButton getXmppDisconnectButton() {
     // for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle("Roster")
     // .getToolbarButtons()) {
     // if (toolbarButton.getToolTipText().matches("Disconnect.*")) {
     // return toolbarButton;
     // }
     //
     // }
     //
     // return null;
     // }
 
     /************** waitUntil ****************/
 
     public void waitUntilSessionCloses() throws RemoteException {
         log.info("wait begin " + System.currentTimeMillis());
         waitUntil(SarosConditions.isSessionClosed(state));
         log.info("wait end " + System.currentTimeMillis());
     }
 
     public void waitUntilSessionCloses(ISarosState state)
         throws RemoteException {
         waitUntil(SarosConditions.isSessionClosed(state));
         delegate.sleep(sleepTime);
 
     }
 
     public void followUser(String participantJID, String sufix)
         throws RemoteException {
         if (!isSharedSessionViewOpen())
             openSarosSessionView();
         clickContextMenuOfTableInView(BotConfiguration.NAME_SESSION_VIEW,
             participantJID + sufix, SarosConstant.CONTEXT_MENU_FOLLOW_THIS_USER);
         // SWTBotTableItem item = selectTableItemWithLabelInViewWithTitle(
         // SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, participantJID
         // + sufix);
         // waitUntilContextMenuOfTableItemEnabled(item,
         // SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
     }
 
     /**
      * "Shared Project Session" View must be open
      */
     public void leaveSession() throws RemoteException {
         activateViewWithTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
         clickToolbarButtonWithTooltipInView(
             SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
             SarosConstant.CONTEXT_MENU_LEAVE_THE_SESSION);
         delegate.sleep(sleepTime);
         // delegate.viewByTitle("Shared Project Session").toolbarButton(
         // "Leave the session").click();
     }
 
     public void inviteUserToProject(String jid) throws RemoteException {
         SWTBotView view = delegate.viewByTitle("Shared Project Session");
         view.setFocus();
         delegate.sleep(750);
         view.toolbarButton("Open invitation interface").click();
         selectCheckBoxWithText(jid);
         delegate.sleep(750);
         delegate.button("Finish").click();
     }
 
     public void addContact(String plainJID) throws RemoteException {
         openRosterView();
 
         activateViewWithTitle(SarosConstant.VIEW_TITLE_ROSTER);
         clickToolbarButtonWithTooltipInView(SarosConstant.VIEW_TITLE_ROSTER,
             SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT);
         waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_CONTACT);
         // activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);
         setTextWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID, plainJID);
         waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
         clickButton(SarosConstant.BUTTON_FINISH);
     }
 }
