 package ai.ilikeplaces.logic.Listeners;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.doc.WARNING;
 import ai.ilikeplaces.entities.Human;
 import ai.ilikeplaces.logic.Listeners.widgets.*;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.util.ElementComposer;
 import ai.ilikeplaces.util.Loggers;
 import ai.ilikeplaces.util.MarkupTag;
 import ai.ilikeplaces.util.SmartLogger;
 import org.itsnat.core.ItsNatDocument;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.ItsNatServletResponse;
 import org.itsnat.core.event.ItsNatServletRequestListener;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.w3c.dom.html.HTMLDocument;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 import static ai.ilikeplaces.servlets.Controller.Page.*;
 import static ai.ilikeplaces.util.Loggers.EXCEPTION;
 
 /**
  * @author Ravindranath Akila
  */
 @WARNING(warning = "Remember, this shows profiles of other users to the current user. Might impose serious privacy issues if " +
         "not handled with utmost care")
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 public class ListenerI implements ItsNatServletRequestListener {
     public static final String USER_PROFILE = "up";
     private static final String NOT_YET_YOUR_FRIEND = "Not yet your friend";
     private static final String VIEW_FRIEND_SUCCESSFUL = "View Friend Successful.";
     private static final String NOT_FRIEND = "Not Friend.";
     private static final String SORRY_I_ENCOUNTERED_AN_ERROR_IN_REDIRECTING_THE_USER_FROM_I_PAGE = "SORRY! I ENCOUNTERED AN ERROR IN REDIRECTING THE USER FROM 'I' PAGE {}";
 
     /**
      * @param request__
      * @param response__
      */
     @WARNING(warning = "Remember, this shows profiles of other users to the current user. Might impose serious privacy issues if " +
             "not handled with utmost care")
     @Override
     public void processRequest(final ItsNatServletRequest request__, final ItsNatServletResponse response__) {
 
         new AbstractSkeletonListener(request__, response__) {
 
             private void redirectToSomeOtherPage(final ItsNatServletResponse response) {
                 try {
                     ((HttpServletResponse) response.getServletResponse()).sendRedirect(Controller.Page.Organize.getURL());
                 } catch (final IOException e) {
                     Loggers.EXCEPTION.error(SORRY_I_ENCOUNTERED_AN_ERROR_IN_REDIRECTING_THE_USER_FROM_I_PAGE, e);
                 }
             }
 
             /**
              * Intialize your document here by appending fragments
              */
             @Override
             @SuppressWarnings("unchecked")
             protected final void init(final ItsNatHTMLDocument itsNatHTMLDocument__, final HTMLDocument hTMLDocument__, final ItsNatDocument itsNatDocument__, final Object... initArgs) {
                 //itsNatDocument.addCodeToSend(JSCodeToSend.FnEventMonitor);
 
                 SmartLogger.g().appendToLogMSG("Returning I Page");
 
 
                 final String requestedProfile = DB.getHumanCRUDHumanLocal(true).doDirtyProfileFromURL(request__.getServletRequest().getParameter(USER_PROFILE)).returnValueBadly();
 
 
                 SmartLogger.g().appendToLogMSG("Requested Profile:" + requestedProfile);
 
                 if (getUsername() == null) {
                     SmartLogger.g().complete(Loggers.LEVEL.DEBUG, "No Login." + Loggers.DONE);
                     redirectToSomeOtherPage(response__);
                 } else {//User is logged on, now other things
                     if (requestedProfile == null) {//This user isn't alive
                         SmartLogger.g().complete(Loggers.LEVEL.DEBUG, "No Such Live User." + Loggers.DONE);
                         redirectToSomeOtherPage(response__);
                     } else {//This user should be a friend
                         //Be careful who checks who here. this user should have been added by the profile we are visiting as friend.(asymetric friend addition)
                         if (DB.getHumanCRUDHumanLocal(true).doNTxIsHumansNetPeople(new HumanId(requestedProfile), new HumanId(getUsernameAsValid())).returnValue()
                                 || getUsernameAsValid().equals(requestedProfile)) {//Show page if it is I am a friend of hers or if this is my profile
 
                             layoutNeededForAllPages:
                             {
                                 setLoginWidget:
                                 {
                                     setLoginWidget((ItsNatServletRequest) initArgs[0], SignInOnCriteria.SignInOnDisplayComponent.TALKS);
                                 }
                                 //Setting her details for identity
                                 signOnDisplayLink:
                                 {
                                     $(Skeleton_othersidebar_identity).setTextContent(DB.getHumanCRUDHumanLocal(true).doDirtyRHuman(requestedProfile).getDisplayName());
                                 }
                                 //Setting her details for identity
                                 setProfileLink:
                                 {
                                     //setProfileDataLink();//We wii do this ourselves. Look below
                                 }
                                 //Setting her details for identity
                                 setProfilePhotoLink:
                                 {
                                     /**
                                      * TODO check for db failure
                                      */
                                     final String url = DB.getHumanCRUDHumanLocal(true).doDirtyRHumansProfilePhoto(new HumanId(requestedProfile)).returnValueBadly();
                                     $(Skeleton_profile_photo).setAttribute(MarkupTag.IMG.src(),
                                             ai.ilikeplaces.logic.Listeners.widgets.UserProperty.formatProfilePhotoUrlStatic(url));
                                 }
                                 //My friends, not hers
                                 setSidebarFriends:
                                 {
                                     setSideBarFriends((ItsNatServletRequest) initArgs[0]);
                                 }
 
                                 setAddAsFriendIfNotFriend:
                                 {
                                     try {
                                         final Human me = DB.getHumanCRUDHumanLocal(true).doDirtyRHuman(getUsernameAsValid());
                                         if (/*Self*/!me.getHumanId().equals(requestedProfile)
                                                 /*Other*/ && me.notFriend(requestedProfile)) {
                                             new UserProperty(request__, $(Skeleton_center_content), new HumanId(requestedProfile)) {
                                                 protected void init(final Object... initArgs) {
                                                     $$(Controller.Page.user_property_content).appendChild(
                                                             ElementComposer.compose($$(MarkupTag.DIV))
                                                                     .$ElementSetText(NOT_YET_YOUR_FRIEND).get());
                                                     new FriendAdd(request__, $$(Controller.Page.user_property_content), new HumanId(requestedProfile).getSelfAsValid(), new HumanId(getUsernameAsValid()).getSelfAsValid()) {
                                                     };
                                                 }
                                             };
 
                                             setTitle:
                                             {
                                                 super.setTitle(me.getDisplayName());
                                             }
                                         } else {
 
                                         }
                                     } catch (final Throwable t) {
                                         EXCEPTION.error("{}", t);
 
                                     }
                                 }
                                 //Setting her details for identity
                                 setWall:
                                 {
                                     try {
                                         new WallWidgetHumansWall(request__, $(Skeleton_center_content), new HumanId(requestedProfile), new HumanId(getUsernameAsValid()));
 
                                     } catch (final Throwable t) {
                                         EXCEPTION.error("{}", t);
 
                                     }
                                 }
                                 UCSetFriendAddWidget:
                                 {
                                     new AdaptableSignup(
                                             request__,
                                             new AdaptableSignupCriteria()
                                                     .setHumanId(new HumanId(getUsernameAsValid()))
                                                     .setWidgetTitle("Add Followers")
                                                     .setAdaptableSignupCallback(new AdaptableSignupCallback() {
                                                         @Override
                                                         public String afterInvite(final HumanId invitee) {
                                                             return ai.ilikeplaces.logic.Listeners.widgets.UserProperty.HUMANS_IDENTITY_CACHE
                                                                    .get(invitee.getHumanId(), invitee.getHumanId()).getHuman().getDisplayName() + " is now following you!";
                                                         }
 
                                                         @Override
                                                         public String jsToSend(HumanId invitee) {
                                                             return JSCodeToSend.refreshPageIn(5);
                                                         }
                                                     })
 
                                             , $(Skeleton_center_content));
                                 }
                             }
                             SmartLogger.g().complete(Loggers.LEVEL.DEBUG, VIEW_FRIEND_SUCCESSFUL + Loggers.DONE);
                         } else {
                             SmartLogger.g().complete(Loggers.LEVEL.DEBUG, NOT_FRIEND + Loggers.DONE);
                             redirectToSomeOtherPage(response__);
                         }
                     }
                 }
 
             }
 
             /**
              * Use ItsNatHTMLDocument variable stored in the AbstractListener class
              */
             @Override
             protected void registerEventListeners(
                     final ItsNatHTMLDocument itsNatHTMLDocument__,
                     final HTMLDocument hTMLDocument__,
                     final ItsNatDocument itsNatDocument__) {
             }
         };
     }
 }
