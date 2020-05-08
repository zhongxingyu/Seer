 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.webcommon;
 
 import org.bedework.appcommon.AdminConfig;
 import org.bedework.appcommon.ClientError;
 import org.bedework.appcommon.client.Client;
 import org.bedework.calfacade.BwAuthUser;
 import org.bedework.calfacade.BwGroup;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.exc.CalFacadeAccessException;
 import org.bedework.calfacade.svc.BwAdminGroup;
 import org.bedework.calfacade.svc.UserAuth;
 import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
 import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
 import org.bedework.util.struts.Request;
 
 import org.apache.log4j.Logger;
 
 import java.util.Collection;
 
 /**
  * @author Mike Douglass
  *
  */
 public class AdminUtil implements ForwardDefs {
   /** Called just before action.
    *
    * @param request
    * @return int foward index
    * @throws Throwable
    */
   public static int actionSetup(final BwRequest request) throws Throwable {
     boolean debug = getLogger().isDebugEnabled();
 
     BwActionFormBase form = (BwActionFormBase)request.getForm();
     Client cl = request.getClient();
 
     BwAuthUser au = cl.getAuthUser(form.getCurrentUser());
 
     if (au == null) {
      if (!cl.isSuperUser()) {
        return forwardNoAccess;
      }

      au = BwAuthUser.makeAuthUser(cl.getCurrentPrincipalHref(),
                                   UserAuth.publicEventUser);
      cl.updateAuthUser(au);
     }
 
     // Refresh current auth user prefs.
     BwAuthUserPrefs prefs = au.getPrefs();
 
     ((BwSessionImpl)request.getSess()).setCurAuthUserPrefs(prefs);
     form.setCurAuthUserPrefs(prefs);
     if (form.getNewSession()) {
       // First time through here for this session. svci is still set up for the
       // authenticated user. Set access rights.
 
       int rights = au.getUsertype();
 
       form.assignCurUserPublicEvents((rights & UserAuth.publicEventUser) != 0);
       form.assignCurUserContentAdminUser((rights & UserAuth.contentAdminUser) != 0);
 
       form.assignAuthorisedUser(rights != UserAuth.noPrivileges);
     }
 
     if (debug) {
       logIt("form.getGroupSet()=" + cl.getGroupSet());
     }
 
     if (debug) {
       logIt("-------- isSuperUser: " + form.getCurUserSuperUser());
     }
 
     int temp = checkGroup(request, true);
 
     if (temp != forwardNoAction) {
       if (debug) {
         logIt("form.getGroupSet()=" + cl.getGroupSet());
       }
       return temp;
     }
 
     if (!form.getAuthorisedUser()) {
       return forwardNoAccess;
     }
 
     return forwardNoAction;
   }
 
   /** Return no action if group is chosen else return a forward index.
    *
    * @param request   for pars
    * @param initCheck true if this is a check to see if we're initialised,
    *                  otherwise this is an explicit request to change group.
    * @return int   forward index
    * @throws Throwable
    */
   public static int checkGroup(final BwRequest request,
                                final boolean initCheck) throws Throwable {
     final BwActionFormBase form = (BwActionFormBase)request.getForm();
 
     final Client cl = request.getClient();
 
     if (cl.getGroupSet()) {
       return forwardNoAction;
     }
 
     try {
       if (cl.getChoosingGroup()) {
         /** This should be the response to presenting a list of groups.
             We handle it here rather than in a separate action to ensure our
             client is not trying to bypass the group setting.
          */
 
         final String reqpar = request.getReqPar("adminGroupName");
         if (reqpar == null) {
           // Make them do it again.
 
           form.assignCalSuites(cl.getContextCalSuites());
 //          request.setSessionAttr(BwRequest.bwAdminGroupsInfoName,
 //                                 request.getClient().getAdminGroups());
 
           return forwardChooseGroup;
         }
 
         final BwAdminGroup adg = (BwAdminGroup)cl.findGroup(reqpar);
         if (adg == null) {
           if (getLogger().isDebugEnabled()) {
             logIt("No user admin group with name " + reqpar);
           }
 
           form.assignCalSuites(cl.getContextCalSuites());
 //          request.setSessionAttr(BwRequest.bwAdminGroupsInfoName,
 //                                 request.getClient().getAdminGroups());
           // We require a group
           return forwardChooseGroup;
         }
 
         return setGroup(request, adg);
       }
 
       /** If the user is in no group or in one group we just go with that,
           otherwise we ask them to select the group
        */
 
       final Collection<BwGroup> adgs;
 
       final BwPrincipal p = cl.getAuthPrincipal();
       if (p == null) {
         return forwardNoAccess;
       }
 
       if (initCheck || !form.getCurUserSuperUser()) {
         // Always restrict to groups of which we are a member
         adgs = cl.getGroups(p);
       } else {
         adgs = cl.getAllGroups(false);
       }
 
       if (adgs.isEmpty()) {
         /** If we require that all users be in a group we return to an error
             page. The only exception will be superUser.
          */
 
         final boolean noGroupAllowed =
           ((AdminConfig)form.getConfig()).getNoGroupAllowed();
         if (cl.isSuperUser() || noGroupAllowed) {
           cl.setAdminGroupName(null);
           cl.setGroupSet(true);
           return forwardNoAction;
         }
 
         return forwardNoGroupAssigned;
       }
 
       cl.setOneGroup(false);
       if (adgs.size() == 1) {
         cl.setOneGroup(true);
         return setGroup(request,
                         (BwAdminGroup)adgs.iterator().next());
       }
 
       /** Go ahead and present the possible groups
        */
       request.setSessionAttr(BwRequest.bwUserAdminGroupsInfoName,
                              adgs);
 
       form.assignCalSuites(cl.getContextCalSuites());
       cl.setChoosingGroup(true); // reset
 
       return forwardChooseGroup;
     } catch (final Throwable t) {
       form.getErr().emit(t);
       return forwardError;
     }
   }
 
   private static int setGroup(final BwRequest request,
                               final BwAdminGroup adg) throws Throwable {
     BwActionFormBase form = request.getBwForm();
     Client cl = request.getClient();
 
     boolean debug = getLogger().isDebugEnabled();
 
     cl.getMembers(adg);
 
     if (debug) {
       logIt("Set admin group to " + adg);
     }
 
     cl.setAdminGroupName(adg.getAccount());
     form.assignAdminGroupName(adg.getAccount());
     cl.setGroupSet(true);
 
     //int access = getAccess(request, getMessages());
 
     BwPrincipal p = cl.getPrincipal(adg.getOwnerHref());
 
     if ((p == null) ||
         !((BwAbstractAction)request.getAction()).checkSvci(request,
                                                            request.getSess(),
                                                            p.getAccount(),
                                                            isMember(adg, form))) {
       return forwardNoAccess;
     }
 
     return forwardNoAction;
   }
 
   private static boolean isMember(final BwAdminGroup ag,
                                   final BwActionFormBase form) throws Throwable {
     return ag.isMember(String.valueOf(form.getCurrentUser()), false);
   }
 
   /** For an administrative user this is how we determine the calendar suite
    * they are administering. We see if there is a suite associated with the
    * administrative group. If so, return.
    *
    * <p>If not we call ourself for each parent group.
    *
    * <p>If none is found we return null.
    *
    * @param request
    * @param cl
    * @return calendar suite wrapper
    * @throws Throwable
    */
   public static BwCalSuiteWrapper findCalSuite(final Request request,
                                                final Client cl) throws Throwable {
     final String groupName = cl.getAdminGroupName();
     if (groupName == null) {
       return null;
     }
 
     BwAdminGroup adg = (BwAdminGroup)cl.findGroup(groupName);
 
     return findCalSuite(request, cl, adg);
   }
 
   /** For an administrative user this is how we determine the calendar suite
    * they are administering. We see if there is a suite associated with the
    * administrative group. If so, return.
    *
    * <p>If not we call ourself for each parent group.
    *
    * <p>If none is found we return null.
    *
    * @param request
    * @param cl
    * @param adg
    * @return calendar suite wrapper
    * @throws Throwable
    */
   private static BwCalSuiteWrapper findCalSuite(final Request request,
                                                 final Client cl,
                                                 final BwAdminGroup adg) throws Throwable {
     if (adg == null) {
       return null;
     }
 
     /* At this point we still require at least authenticated read access to
      * the target calendar suite
      */
 
     try {
       BwCalSuiteWrapper cs = cl.getCalSuite(adg);
       if (cs != null) {
         return cs;
       }
 
       for (BwGroup parent: cl.findGroupParents(adg)) {
         cs = findCalSuite(request, cl, (BwAdminGroup)parent);
         if (cs != null) {
           return cs;
         }
       }
     } catch (CalFacadeAccessException cfe) {
       // Access is set incorrectly
       request.getErr().emit(ClientError.noCalsuiteAccess, adg.getPrincipalRef());
     }
 
     return null;
   }
 
   /**
    * @param msg
    */
   public static void debugOut(final String msg) {
     getLogger().debug(msg);
   }
 
   /**
    * @param msg
    */
   public static void logIt(final String msg) {
     getLogger().info(msg);
   }
 
   /**
    * @return Logger
    */
   public static Logger getLogger() {
     return Logger.getLogger(AdminUtil.class);
   }
 
   /** Info message
    *
    * @param msg
    */
   public static void info(final String msg) {
     getLogger().info(msg);
   }
 
   /** Warning message
    *
    * @param msg
    */
   public void warn(final String msg) {
     getLogger().warn(msg);
   }
 
   /**
    * @param msg
    */
   public void debugMsg(final String msg) {
     getLogger().debug(msg);
   }
 
   /**
    * @param t
    */
   public void error(final Throwable t) {
     getLogger().error(this, t);
   }
 
 }
