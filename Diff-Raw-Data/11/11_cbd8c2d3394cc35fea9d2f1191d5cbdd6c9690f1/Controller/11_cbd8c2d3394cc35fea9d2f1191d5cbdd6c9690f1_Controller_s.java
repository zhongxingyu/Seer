 package ai.ilikeplaces.servlets;
 
 import ai.ilikeplaces.doc.*;
 import ai.ilikeplaces.logic.Listeners.*;
 import ai.ilikeplaces.logic.Listeners.templates.TemplateGeneric;
 import ai.ilikeplaces.logic.Listeners.widgets.ListenerShare;
 import ai.ilikeplaces.logic.Listeners.widgets.WallWidget;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.util.Loggers;
 import org.itsnat.core.*;
 import org.itsnat.core.event.ItsNatServletRequestListener;
 import org.itsnat.core.http.HttpServletWrapper;
 import org.itsnat.core.http.ItsNatHttpServlet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * @author Ravindranath Akila
  */
 
 @TODO(task = "Code to disable url calls with itsnat_doc_name=### type urls if possible")
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 final public class
         Controller extends HttpServletWrapper {
 // ------------------------------ FIELDS ------------------------------
 
     public static final String LOCATION_HUB = "/page/Earth_of_Earth?WOEID=1";
     public static final String WEB_INF_PAGES = "WEB-INF/pages/";
 
     /**
      * This Map is static as Id's in html documents should be universally identical, i.e. as htmldocname_elementId
      */
     public final static Map<String, String> GlobalHTMLIdRegistry = new IdentityHashMap<String, String>();
 
     /**
      * Retrievable list of all element Ids by Page
      */
     public final static Map<PageFace, HashSet<String>> GlobalPageIdRegistry = new IdentityHashMap<PageFace, HashSet<String>>();
     public static String REAL_PATH;//Weak implementation but suffices
 
     private final static Map<PageFace, String> PrettyURLMap_ = new IdentityHashMap<PageFace, String>();//Please read javadoc before making any changes to this implementation
     final static private Logger staticLogger = LoggerFactory.getLogger(Controller.class.getName());
     private static final String ITSNAT_DOC_NAME = "itsnat_doc_name";
 
     final PageFace locationMain = Page.LocationMain;
     final PageFace aarrr = Page.Aarrr;
 //    final PageFace photoCRUD = Page.PhotoCRUD;
     final PageFace photo$Description = Page.Photo$Description;
     final PageFace signInOn = Page.SignInOn;
 
     final PageFace privateLocationCreate = Page.PrivateLocationCreate;
     final PageFace privateLocationView = Page.PrivateLocationView;
     final PageFace privateLocationDelete = Page.PrivateLocationDelete;
 
     final PageFace privateEventCreate = Page.PrivateEventCreate;
     final PageFace privateEventView = Page.PrivateEventView;
     final PageFace privateEventDelete = Page.PrivateEventDelete;
 
     final PageFace wOIEDGrabber = Page.WOEIDGrabber;
 
     final PageFace downTownHeatMap = Page.DownTownHeatMap;
 
     final PageFace skeleton = Page.Skeleton;
     final PageFace organize = Page.Organize;
     final PageFace findFriend = Page.Friends;
     final PageFace book = Page.Bookings;
     final PageFace profile = Page.Profile;
     final PageFace i = Page.I;
     final PageFace activate = Page.Activate;
     final PageFace share = Page.Share;
     final PageFace geoBusiness = Page.GeoBusiness;
     final PageFace photos = Page.Photos;
 
     final PageFace findFriendWidget = Page.FindFriend;
     final PageFace friendAdd = Page.FriendAdd;
     final PageFace friendDelete = Page.FriendDelete;
     final PageFace friendList = Page.FriendList;
 
     final PageFace genericButton = Page.GenericButton;
 
     final PageFace templateGeneric = Page.TemplateGeneric;
 
     final PageFace displayName = Page.DisplayName;
 
     final PageFace wallHanlder = Page.WallHandler;
 
     final PageFace passwordChange = Page.PasswordChange;
     final PageFace forgotPasswordChange = Page.ForgotPasswordChange;
     final PageFace profileWidget = Page.ProfileWidget;
 
     final PageFace album = Page.Album;
 
     final PageFace userProperty = Page.UserProperty;
     final PageFace userPropertySidebar = Page.UserPropertySidebar;
 
 // -------------------------- ENUMERATIONS --------------------------
 
     @NOTE(note = "Inner Enums are static. Therefore, the lists shall be populated only once.")
     public enum Page implements PageFace {
         Album("ai/ilikeplaces/widgets/Album.xhtml",
               Controller.Page.AlbumNotice,
               Controller.Page.AlbumPivateEventId,
               Controller.Page.AlbumOwner,
               Controller.Page.AlbumForward,
               Controller.Page.AlbumPhotos
         ) {
             @Override
             public String toString() {
                 return DocAlbum;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
         UserProperty("ai/ilikeplaces/widgets/UserProperty.xhtml",
                      Controller.Page.user_property_profile_photo,
                      Controller.Page.user_property_name,
                      Controller.Page.user_property_widget,
                      Controller.Page.user_property_content
 
         ) {
             @Override
             public String toString() {
                 return DocUserProperty;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
 
         UserPropertySidebar("ai/ilikeplaces/widgets/UserProperty_sidebar.xhtml",
                             Controller.Page.user_property_sidebar_profile_photo,
                             Controller.Page.user_property_sidebar_name,
                             Controller.Page.user_property_sidebar_widget,
                             Controller.Page.user_property_sidebar_content
 
         ) {
             @Override
             public String toString() {
                 return DocUserPropertySidebar;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
         },
 
 
         ProfileWidget("ai/ilikeplaces/widgets/profile.xhtml",
                       Controller.Page.ProfileNotice,
                       Controller.Page.ProfileURLChange,
                       Controller.Page.ProfileURL,
                       Controller.Page.ProfileURLUpdate
         ) {
             @Override
             public String toString() {
                 return DocProfileWidget;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }},
         ForgotPasswordChange("ai/ilikeplaces/widgets/password.xhtml",
                              Controller.Page.ProfileForgotPasswordWidget,
                              Controller.Page.ProfileForgotPasswordNotice,
                              Controller.Page.ProfileForgotPasswordEmailAddress,
                              Controller.Page.ProfileForgotPasswordCodeMail,
                              Controller.Page.ProfileForgotPasswordEmailedCode,
                              Controller.Page.ProfileForgotPasswordNew,
                              Controller.Page.ProfileForgotPasswordNewConfirm,
                              Controller.Page.ProfileForgotPasswordSave
         ) {
             @Override
             public String toString() {
                 return DocForgotPasswordChange;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }},
         PasswordChange("ai/ilikeplaces/widgets/password.xhtml",
                        Controller.Page.ProfilePasswordWidget,
                        Controller.Page.ProfilePasswordNotice,
                        Controller.Page.ProfilePasswordCurrent,
                        Controller.Page.ProfilePasswordNewConfirm,
                        Controller.Page.ProfilePasswordNew,
                        Controller.Page.ProfilePasswordSave
         ) {
             @Override
             public String toString() {
                 return DocPasswordChange;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }},
         WallHandler("ai/ilikeplaces/widgets/wall.xhtml",
                     Controller.Page.wallContent,
                     Controller.Page.wallAppend,
                     Controller.Page.wallSubmit,
                     Controller.Page.wallNotice,
                     Controller.Page.wallMute
         ) {
             @Override
             public String toString() {
                 return DocWall;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }},
         DisplayName("ai/ilikeplaces/widgets/DisplayName.xhtml",
                     Controller.Page.DisplayNameDisplay) {
             @Override
             public String toString() {
                 return DocDisplayName;
             }
 
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }},
         PrivateEventCreate("ai/ilikeplaces/widgets/privateevent/private_event_create.xhtml",
                            Controller.Page.privateEventCreateName,
                            Controller.Page.privateEventCreateInfo,
                            Controller.Page.privateEventCreateSave,
                            Controller.Page.privateEventCreateNotice
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateEventCreate;
             }},
 
         PrivateEventView("ai/ilikeplaces/widgets/privateevent/private_event_view.xhtml",
                          Controller.Page.privateEventViewNotice,
                          Controller.Page.privateEventViewName,
                          Controller.Page.privateEventViewInfo,
                          Controller.Page.privateEventViewOwners,
                          Controller.Page.privateEventViewVisitors,
                          Controller.Page.privateEventViewInvites,
                          Controller.Page.privateEventViewLink,
                          Controller.Page.privateEventViewWall,
                          Controller.Page.privateEventViewAlbum,
                          Controller.Page.privateEventViewLocationMap
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateEventView;
             }},
 
 
         PrivateEventDelete("ai/ilikeplaces/widgets/privateevent/private_event_delete.xhtml",
                            Controller.Page.privateEventDeleteName,
                            Controller.Page.privateEventDeleteInfo,
                            Controller.Page.privateEventDeleteNotice,
                            Controller.Page.privateEventDelete,
                            Controller.Page.privateEventDeleteLink,
                            Controller.Page.privateEventDeleteOwners,
                            Controller.Page.privateEventDeleteVisitors,
                            Controller.Page.privateEventDeleteInvitees,
                            Controller.Page.privateEventDeleteWall,
                            Controller.Page.privateEventDeleteAlbum,
                            Controller.Page.privateEventDeleteLocationMap
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateEventDelete;
             }},
 
 
         WOEIDGrabber("ai/ilikeplaces/widgets/WOEIDGrabber.xhtml",
                      Controller.Page.WOEIDGrabberWOEID
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocWOEIDGrabber;
             }},
 
 
         DownTownHeatMap("ai/ilikeplaces/widgets/DownTownHeatMap.xhtml",
                         Controller.Page.DownTownHeatMapWOEID,
                         Controller.Page.DownTownHeatMapBB
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocDownTownHeatMap;
             }},
 
         FindFriend("ai/ilikeplaces/widgets/friend/friend_find.xhtml",
                    Controller.Page.friendFindSearchTextInput,
                    Controller.Page.friendFindSearchButtonInput,
                    Controller.Page.friendFindSearchResults,
                    Controller.Page.friendFindSearchInvites
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocFindFriend;
             }},
 
         FriendAdd("ai/ilikeplaces/widgets/friend/friend_add.xhtml",
                   Controller.Page.friendAddAddButton,
                   Controller.Page.friendAddDisplayNameLabel
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocFriendAdd;
             }},
 
         FriendDelete("ai/ilikeplaces/widgets/friend/friend_delete.xhtml",
                      Controller.Page.friendDeleteAddButton,
                      Controller.Page.friendDeleteDisplayNameLabel
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocFriendDelete;
             }},
 
         FriendList("ai/ilikeplaces/widgets/friend/friend_list.xhtml",
                    Controller.Page.FriendListList
 
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocFriendList;
             }},
 
         GenericButton("ai/ilikeplaces/widgets/button.xhtml",
                       Controller.Page.GenericButtonLink,
                       Controller.Page.GenericButtonText,
                       Controller.Page.GenericButtonImage,
                       Controller.Page.GenericButtonWidth
 
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocGenericButton;
             }},
 
         Organize(null
         ) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_org";
             }
 
             @Override
             public String toString() {
                 return DocOrganize;
             }},
 
         Activate(null
         ) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_activate";
             }
 
             @Override
             public String toString() {
                 return DocActivate;
             }},
 
         Share(null
         ) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_share";
             }
 
             @Override
             public String toString() {
                 return DocShare;
             }},
 
         GeoBusiness(null) {
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_geobusiness";
             }
 
             @Override
             public String toString() {
                 return DocGeoBusiness;
             }},
 
         TemplateGeneric(null) {
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_public";
             }
 
             @Override
             public String toString() {
                 return DocPublic;
             }},
 
         Profile(null,
                 Controller.Page.ProfilePhotoChange
         ) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_profile";
             }
 
             @Override
             public String toString() {
                 return DocProfile;
             }},
 
         I(null) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_i";
             }
 
             @Override
             public String toString() {
                 return DocI;
             }},
 
         Photos(null) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_me";
             }
 
             @Override
             public String toString() {
                 return DocPhotos;
             }},
 
         Friends(null
         ) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_friends";
             }
 
             @Override
             public String toString() {
                 return DocFriends;
             }},
         Bookings(null
         ) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/_book";
             }
 
             @Override
             public String toString() {
                 return DocBook;
             }},
 
         Skeleton("ai/ilikeplaces/Skeleton.xhtml",
                  Controller.Page.skeletonTitle,
                  Controller.Page.SkeletonCPageTitle,
                  Controller.Page.SkeletonCPageIntro,
                  Controller.Page.SkeletonCPageNotice,
                  Controller.Page.Skeleton_center,
                  Controller.Page.Skeleton_center_content,
                  Controller.Page.Skeleton_center_skeleton,
                  Controller.Page.Skeleton_file_list,
                  Controller.Page.Skeleton_left_column,
                  Controller.Page.Skeleton_login_widget,
                  Controller.Page.Skeleton_notice,
                  Controller.Page.Skeleton_notice_sh,
                  Controller.Page.Skeleton_othersidebar,
                  Controller.Page.Skeleton_profile_photo,
                  Controller.Page.Skeleton_othersidebar_identity,
                  Controller.Page.Skeleton_othersidebar_organizer_link,
                  Controller.Page.Skeleton_othersidebar_photo_manager_link,
                  Controller.Page.Skeleton_othersidebar_places_link,
                  Controller.Page.Skeleton_othersidebar_profile_link,
                  Controller.Page.Skeleton_othersidebar_upload_file_sh,
                  Controller.Page.Skeleton_right_column,
                  Controller.Page.Skeleton_sidebar,
                  Controller.Page.Skeleton_othersidebar_wall_link
 
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocSkeleton;
             }},
         PrivateLocationCreate("ai/ilikeplaces/widgets/privatelocation/private_location_create.xhtml",
                               Controller.Page.privateLocationCreateName,
                               Controller.Page.privateLocationCreateInfo,
                               Controller.Page.privateLocationCreateWOEID,
                               Controller.Page.privateLocationCreateWOEIDGrabber,
                               Controller.Page.privateLocationCreateSave,
                               Controller.Page.PrivateLocationCreateCNotice,
                               Controller.Page.PrivateLocaionCreateCTitle,
                               Controller.Page.PrivateLocaionCreateCIntro
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateLocationCreate;
             }},
 
         PrivateLocationView("ai/ilikeplaces/widgets/privatelocation/private_location_view.xhtml",
                             Controller.Page.privateLocationViewNotice,
                             Controller.Page.privateLocationViewName,
                             Controller.Page.privateLocationViewInfo,
                             Controller.Page.privateLocationViewOwners,
                             Controller.Page.privateLocationViewVisitors,
                             Controller.Page.privateLocationViewLink,
                             Controller.Page.privateLocationViewEventList
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateLocationView;
             }},
 
 
         PrivateLocationDelete("ai/ilikeplaces/widgets/privatelocation/private_location_delete.xhtml",
                               Controller.Page.privateLocationDeleteName,
                               Controller.Page.privateLocationDeleteInfo,
                               Controller.Page.privateLocationDeleteNotice,
                               Controller.Page.privateLocationDelete,
                               Controller.Page.privateLocationDeleteOwners,
                               Controller.Page.privateLocationDeleteVisitors,
                               Controller.Page.privateLocationDeleteEventList
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return DocPrivateLocationDelete;
             }},
 
         home(
                 null) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "";
             }
 
             @Override
             public String toString() {
                 return "";
             }
         },
         LocationMain(
                 "ai/ilikeplaces/Main.xhtml",
                 Controller.Page.body,
                 Controller.Page.mainTitle,
                 Controller.Page.mainMetaDesc,
                 Controller.Page.Main_othersidebar_identity,
                 Controller.Page.Main_location_photo,
                 Controller.Page.Main_profile_photo,
                 Controller.Page.Main_othersidebar_profile_link,
                 Controller.Page.Main_othersidebar_upload_file_sh,
                 Controller.Page.Main_notice_sh,
                 Controller.Page.Main_hotels_link,
                 Controller.Page.Main_center_main,
                 Controller.Page.Main_notice,
                 Controller.Page.Main_center_main_location_title,
                 Controller.Page.Main_center_content,
                 Controller.Page.Main_yox,
                 Controller.Page.Main_left_column,
                 Controller.Page.Main_right_column,
                 Controller.Page.Main_sidebar,
                 Controller.Page.Main_login_widget,
                 Controller.Page.Main_location_backlink,
                 Controller.Page.Main_location_list_header,
                 Controller.Page.Main_location_list,
                 Controller.Page.Main_flickr) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/main";
             }
 
             @Override
             public String toString() {
                 return DocLocation;
             }
         },
         Aarrr(
                 "ai/ilikeplaces/AARRR.xhtml",
                 Controller.Page.AarrrDownTownHeatMap,
                 Controller.Page.AarrrWOEID
         ) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/";
             }
 
             @Override
             public String toString() {
                 return DocAarrr;
             }
         },
         Photo$Description(
                 "ai/ilikeplaces/widgets/Photo-Description.xhtml",
                 Controller.Page.pd,
                 Controller.Page.close,
                 Controller.Page.pd_photo_permalink,
                 Controller.Page.pd_photo,
                 Controller.Page.pd_photo_description,
                 Controller.Page.pd_photo_delete
         ) {
             @Override
             public String getURL() {
                 throw new IllegalAccessError("SORRY! THIS IS A TEMPLATE WITH NO SPECIFIC PAGE OF WHICH YOU WANT THE URL.");
             }
 
             @Override
             public String toString() {
                 return "Photo-Description";
             }
         },
         PhotoUpload(
                 "ai/ilikeplaces/widgets/PhotoUpload.xhtml") {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/main";
             }
 
             @Override
             public String toString() {
                 return "PhotoUpload";
             }
         },
         signup(
                 null) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "signup";
             }
 
             @Override
             public String toString() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "signup";
             }
         },
         login(
                 null) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "login";
             }
 
             @Override
             public String toString() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "login";
             }
         },
         SignInOn(
                 "ai/ilikeplaces/widgets/SignInOn.xhtml",
                 Controller.Page.signinon_login,
                 Controller.Page.signinon_signup,
                 Controller.Page.signinon_logon
         ) {
             @Override
             public String getURL() {
                 return RBGet.getGlobalConfigKey("AppRoot") + "page/main";
             }
 
             @Override
             public String toString() {
                 return "DocSignInOn";
             }
         };
 
         /*Album Page*/
         final static public String DocAlbum = "DocAlbum";
         /*Album IDs*/
         final static public String AlbumNotice = "AlbumNotice";
         final static public String AlbumPivateEventId = "AlbumPivateEventId";
         final static public String AlbumOwner = "AlbumOwner";
         final static public String AlbumForward = "AlbumForward";
         final static public String AlbumPhotos = "AlbumPhotos";
 
         /*ProfileWidget Page*/
         final static public String DocUserProperty = "DocUserProperty";
         /*ProfileWidget IDs*/
         final static public String user_property_profile_photo = "user_property_profile_photo";
         final static public String user_property_name = "user_property_name";
         final static public String user_property_widget = "user_property_widget";
         final static public String user_property_content = "user_property_content";
 
         /*UserPropertySidebar Page*/
         final static public String DocUserPropertySidebar = "DocUserPropertySidebar";
         /*UserPropertySidebar IDs*/
         final static public String user_property_sidebar_profile_photo = "user_property_sidebar_profile_photo";
         final static public String user_property_sidebar_name = "user_property_sidebar_name";
         final static public String user_property_sidebar_widget = "user_property_sidebar_widget";
         final static public String user_property_sidebar_content = "user_property_sidebar_content";
 
 
         /*ProfileWidget Page*/
         final static public String DocProfileWidget = "DocProfileWidget";
         /*ProfileWidget IDs*/
         final static public String ProfileNotice = "ProfileNotice";
         final static public String ProfileURLChange = "ProfileURLChange";
         final static public String ProfileURL = "ProfileURL";
         final static public String ProfileURLUpdate = "ProfileURLUpdate";
 
         /*Forgot Password Page*/
         final static public String DocForgotPasswordChange = "DocForgotPasswordChange";
         /*Forgot Password IDs*/
         final static public String ProfileForgotPasswordWidget = "ProfileForgotPasswordWidget";
         final static public String ProfileForgotPasswordNotice = "ProfileForgotPasswordNotice";
         final static public String ProfileForgotPasswordEmailAddress = "ProfileForgotPasswordEmailAddress";
         final static public String ProfileForgotPasswordCodeMail = "ProfileForgotPasswordCodeMail";
         final static public String ProfileForgotPasswordEmailedCode = "ProfileForgotPasswordEmailedCode";
         final static public String ProfileForgotPasswordNew = "ProfileForgotPasswordNew";
         final static public String ProfileForgotPasswordNewConfirm = "ProfileForgotPasswordNewConfirm";
         final static public String ProfileForgotPasswordSave = "ProfileForgotPasswordSave";
 
 
         /*Password Page*/
         final static public String DocPasswordChange = "DocPasswordChange";
         /*Password IDs*/
         final static public String ProfilePasswordWidget = "ProfilePasswordWidget";
         final static public String ProfilePasswordNotice = "ProfilePasswordNotice";
         final static public String ProfilePasswordCurrent = "ProfilePasswordCurrent";
         final static public String ProfilePasswordNewConfirm = "ProfilePasswordNewConfirm";
         final static public String ProfilePasswordNew = "ProfilePasswordNew";
         final static public String ProfilePasswordSave = "ProfilePasswordSave";
 
         /*WallHandler Page*/
         final static public String DocWall = "DocWall";
         /*WallHandler IDs*/
         final static public String wallContent = "wallContent";
         final static public String wallAppend = "wallAppend";
         final static public String wallSubmit = "wallSubmit";
         final static public String wallNotice = "wallNotice";
         final static public String wallMute = "wallMute";
 
         /*DisplayName Page*/
         final static public String DocDisplayName = "DocDisplayName";
         /*DisplayName IDs*/
         final static public String DisplayNameDisplay = "DisplayNameDisplay";
 
         /*Private Event Page*/
         final static public String DocPrivateEventView = "PrivateEventView";
         /*Private Event Create IDs*/
         final static public String privateEventViewNotice = "privateEventViewNotice";
         final static public String privateEventViewName = "privateEventViewName";
         final static public String privateEventViewInfo = "privateEventViewInfo";
         final static public String privateEventViewOwners = "privateEventViewOwners";
         final static public String privateEventViewVisitors = "privateEventViewVisitor";
         final static public String privateEventViewInvites = "privateEventViewInvites";
         final static public String privateEventViewLink = "privateEventViewLink";
         final static public String privateEventViewWall = "privateEventViewWall";
         final static public String privateEventViewAlbum = "privateEventViewAlbum";
         final static public String privateEventViewLocationMap = "privateEventViewLocationMap";
 
         /*Private Event Page*/
         final static public String DocPrivateEventCreate = "PrivateEventCreate";
         /*Private Event Create IDs*/
         final static public String privateEventCreateName = "privateEventCreateName";
         final static public String privateEventCreateInfo = "privateEventCreateInfo";
         final static public String privateEventCreateSave = "privateEventCreateSave";
         final static public String privateEventCreateNotice = "privateEventCreateNotice";
 
         /*Private Event Page*/
         final static public String DocPrivateEventDelete = "PrivateEventDelete";
         /*Private Event Create IDs*/
         final static public String privateEventDeleteName = "privateEventDeleteName";
         final static public String privateEventDeleteInfo = "privateEventDeleteInfo";
         final static public String privateEventDeleteNotice = "privateEventDeleteNotice";
         final static public String privateEventDelete = "privateEventDelete";
         final static public String privateEventDeleteLink = "privateEventDeleteLink";
         final static public String privateEventDeleteOwners = "privateEventDeleteOwners";
         final static public String privateEventDeleteVisitors = "privateEventDeleteVisitors";
         final static public String privateEventDeleteInvitees = "privateEventDeleteInvitees";
         final static public String privateEventDeleteWall = "privateEventDeleteWall";
         final static public String privateEventDeleteAlbum = "privateEventDeleteAlbum";
         final static public String privateEventDeleteLocationMap = "privateEventDeleteLocationMap";
 
         /*WOIEDGrabber Page*/
         final static public String DocWOEIDGrabber = "DocWOEIDGrabber";
         /*WOIEDGrabber IDs*/
         final static public String WOEIDGrabberWOEID = "WOEIDGrabberWOEID";
 
         /*DownTownHeatMap Page*/
         final static public String DocDownTownHeatMap = "DocDownTownHeatMap";
         /*DownTownHeatMap IDs*/
         final static public String DownTownHeatMapWOEID = "DownTownHeatMapWOEID";
         final static public String DownTownHeatMapBB = "DownTownHeatMapBB";
 
         /*FindFriend Page*/
         final static public String DocFindFriend = "DocFindFriend";
         /*FindFriend IDs*/
         final static public String friendFindSearchTextInput = "friendFindSearchTextInput";
         final static public String friendFindSearchButtonInput = "friendFindSearchButtonInput";
         final static public String friendFindSearchResults = "friendFindSearchResults";
         final static public String friendFindSearchInvites = "friendFindSearchInvites";
 
         /*AddFriend Page*/
         final static public String DocFriendAdd = "DocFriendAdd";
         /*AddFriend IDs*/
         final static public String friendAddDisplayNameLabel = "friendAddDisplayNameLabel";
         final static public String friendAddAddButton = "friendAddAddButton";
 
         /*DeleteFriend Page*/
         final static public String DocFriendDelete = "DocFriendDelete";
         /*DeleteFriend IDs*/
         final static public String friendDeleteDisplayNameLabel = "friendDeleteDisplayNameLabel";
         final static public String friendDeleteAddButton = "friendDeleteDeleteButton";
 
         /*FriendList Page*/
         final static public String DocFriendList = "DocFriendList";
         /*FriendList IDs*/
         final static public String FriendListList = "FriendListList";
 
         /*FriendList Page*/
         final static public String DocGenericButton = "DocGenericButton";
         /*FriendList IDs*/
         final static public String GenericButtonLink = "GenericButtonLink";
         final static public String GenericButtonText = "GenericButtonText";
         final static public String GenericButtonImage = "GenericButtonImage";
         final static public String GenericButtonWidth = "GenericButtonWidth";
 
         /*Profile Page*/
         final static public String DocProfile = "DocProfile";
         /*Profile IDs*/
         final static public String ProfilePhotoChange = "ProfilePhotoChange";
 
         /*I Page*/
         final static public String DocI = "DocI";
 
         /*Photos Page*/
         final static public String DocPhotos = "DocPhotos";
 
         /*Actiavte Page*/
         final static public String DocActivate = "DocActivate";
 
         /*Share Page*/
         final static public String DocShare = "DocShare";
 
         /*GeoBusiness Page*/
         final static public String DocGeoBusiness = "DocGeoBusiness";
 
         /*TemplateGeneric Pages*/
         final static public String DocPublic = "DocPublic";
 
         /*Organize Page*/
         final static public String DocOrganize = "DocOrganize";
         /*Organize Attributes*/
         final static public String DocOrganizeCategory = "category";
         final static public int DocOrganizeModeOrganize = 0;
         final static public String DocOrganizeLocation = "location";
         final static public int DocOrganizeModeLocation = 1;
         final static public String DocOrganizeEvent = "event";
         final static public int DocOrganizeModePrivateLocation = 2;
         final static public String DocOrganizeAlbum = "album";
         final static public int DocOrganizeModeEvent = 3;
 
         /*FriendFind Page*/
         final static public String DocFriends = "DocFriends";
 
         /*Book Page*/
         final static public String DocBook = "DocBook";
 
         /*Skeleton Page*/
         final static public String DocSkeleton = "DocPrivateLocationCreate";
         /*Skeleton Create IDs*/
         final static public String skeletonTitle = "skeletonTitle";
         final static public String Skeleton_login_widget = "Skeleton_login_widget";
         final static public String Skeleton_othersidebar = "Skeleton_othersidebar";
         final static public String Skeleton_profile_photo = "Skeleton_profile_photo";
         final static public String Skeleton_othersidebar_identity = "Skeleton_othersidebar_identity";
         final static public String Skeleton_othersidebar_places_link = "Skeleton_othersidebar_places_link";
         final static public String Skeleton_othersidebar_profile_link = "Skeleton_othersidebar_profile_link";
         final static public String Skeleton_othersidebar_photo_manager_link = "Skeleton_othersidebar_photo_manager_link";
         final static public String Skeleton_othersidebar_organizer_link = "Skeleton_othersidebar_organizer_link";
         final static public String Skeleton_othersidebar_upload_file_sh = "Skeleton_othersidebar_upload_file_sh";
         final static public String Skeleton_file_list = "Skeleton_file_list";
         final static public String Skeleton_center = "Skeleton_center";
         final static public String Skeleton_center_skeleton = "Skeleton_center_skeleton";
         final static public String Skeleton_notice_sh = "Skeleton_notice_sh";
         final static public String Skeleton_notice = "Skeleton_notice";
         final static public String Skeleton_center_content = "Skeleton_center_content";
         final static public String Skeleton_left_column = "Skeleton_left_column";
         final static public String Skeleton_right_column = "Skeleton_right_column";
         final static public String Skeleton_sidebar = "Skeleton_sidebar";
         final static public String Skeleton_othersidebar_wall_link = "Skeleton_othersidebar_wall_link";
 
         /*Private Location Page*/
         final static public String DocPrivateLocationView = "PrivateLocationView";
         /*Private Location Create IDs*/
         final static public String privateLocationViewNotice = "privateLocationViewNotice";
         final static public String privateLocationViewName = "privateLocationViewName";
         final static public String privateLocationViewInfo = "privateLocationViewInfo";
         final static public String privateLocationViewOwners = "privateLocationViewOwners";
         final static public String privateLocationViewVisitors = "privateLocationViewVisitor";
         final static public String privateLocationViewLink = "privateLocationViewLink";
         final static public String privateLocationViewEventList = "privateLocationViewEventList";
 
         /*Private Location Page*/
         final static public String DocPrivateLocationCreate = "PrivateLocationCreate";
         /*Private Location Create IDs*/
         final static public String privateLocationCreateName = "privateLocationCreateName";
         final static public String privateLocationCreateInfo = "privateLocationCreateInfo";
         final static public String privateLocationCreateWOEID = "privateLocationCreateWOEID";
         final static public String privateLocationCreateWOEIDGrabber = "privateLocationCreateWOEIDGrabber";
         final static public String privateLocationCreateSave = "privateLocationCreateSave";
         final static public String privateLocationDeleteOwners = "privateLocationDeleteOwners";
         final static public String privateLocationDeleteVisitors = "privateLocationDeleteVisitors";
         final static public String privateLocationDeleteEventList = "privateLocationDeleteEventList";
 
         /*Private Location Page*/
         final static public String DocPrivateLocationDelete = "PrivateLocationDelete";
         /*Private Location Create IDs*/
         final static public String privateLocationDeleteName = "privateLocationDeleteName";
         final static public String privateLocationDeleteInfo = "privateLocationDeleteInfo";
         final static public String privateLocationDeleteNotice = "privateLocationDeleteNotice";
         final static public String privateLocationDelete = "privateLocationDelete";
 
         /*Photo Descrition Specific IDs*/
         final static public String pd = "pd";
         final static public String close = "close";
         final static public String pd_photo_permalink = "pd_photo_permalink";
         final static public String pd_photo = "pd_photo";
         final static public String pd_photo_description = "pd_photo_description";
         final static public String pd_photo_delete = "pd_photo_delete";
 
 
         /*Aarrr Page*/
         final static public String DocAarrr = "Aarrr";
         /*Aarrr Specific IDs*/
         final static public String AarrrDownTownHeatMap = "AarrrDownTownHeatMap";
         final static public String AarrrWOEID = "AarrrWOEID";
 
 
         /*DocLocation Page*/
         final static public String DocLocation = "DocLocation";
 
         /*Main Specific IDs*/
         final static public String body = "body";
         final static public String mainTitle = "mainTitle";
         final static public String mainMetaDesc = "mainMetaDesc";
         final static public String Main_othersidebar_identity = "Main_othersidebar_identity";
         final static public String Main_location_photo = "Main_location_photo";
         final static public String Main_profile_photo = "Main_profile_photo";
         final static public String Main_othersidebar_profile_link = "Main_othersidebar_profile_link";
         final static public String Main_othersidebar_upload_file_sh = "Main_othersidebar_upload_file_sh";
         final static public String Main_loading_hotels_link = "Main_loading_hotels_link";
         final static public String Main_hotels_link = "Main_hotels_link";
         final static public String Main_center_main = "Main_center_main";
         final static public String Main_notice = "Main_notice";
         final static public String Main_notice_sh = "Main_notice_sh";
         final static public String Main_center_main_location_title = "Main_center_main_location_title";
         final static public String Main_center_content = "Main_center_content";
         final static public String Main_yox = "Main_yox";
         final static public String Main_left_column = "Main_left_column";
         final static public String Main_right_column = "Main_right_column";
         final static public String Main_sidebar = "Main_sidebar";
         final static public String Main_login_widget = "Main_login_widget";
         final static public String Main_location_backlink = "Main_location_backlink";
         final static public String Main_location_list_header = "Main_location_list_header";
         final static public String Main_location_list = "Main_location_list";
         final static public String Main_flickr = "Main_flickr";
 
         /*PhotoCRUD Specific IDs*/
         final static public String pc_photo_title = "pc_photo_title";
         final static public String pc_close = "pc_close";
         final static public String pc = "pc";
         final static public String pc_photo = "pc_photo";
         final static public String pc_photo_permalink = "pc_photo_permalink";
         final static public String pc_photo_name = "pc_photo_name";
         final static public String pc_update_name = "pc_update_name";
         final static public String pc_photo_description = "pc_photo_description";
         final static public String pc_delete = "pc_delete";
         final static public String pc_update_description = "pc_update_description";
 
         /*SignInOn Page*/
         final static public String DocSignInOn = "SignInOn";
         /*SignInOn Specific IDs*/
         final static public String signinon_login = "signinon_login";
         final static public String signinon_signup = "signinon_signup";
         final static public String signinon_logon = "signinon_logon";
 
 
         /*Common IDs that should be present in any page*/
         final static public String CPageTitle = "PageTitle";
         final static public String CPageIntro = "PageIntro";
         final static public String CPageNotice = "PageNotice";
 
         final static public String SkeletonCPageTitle = CPageTitle;
         final static public String MainCPageTitle = CPageTitle;//NOT IMPLEMENTED YET, UPDATE HTML AND CONSTRUCTOR PLS
         final static public String PrivateLocaionCreateCTitle = "PrivateLocationCreateTitle";
 
         final static public String SkeletonCPageIntro = CPageIntro;
         final static public String MainCPageIntro = CPageIntro;//NOT IMPLEMENTED YET, UPDATE HTML AND CONSTRUCTOR PLS
         final static public String PrivateLocaionCreateCIntro = "PrivateLocationCreateIntro";
 
         final static public String SkeletonCPageNotice = CPageNotice;
         final static public String MainCPageNotice = CPageNotice;//NOT IMPLEMENTED YET, UPDATE HTML AND CONSTRUCTOR PLS
         final static public String PrivateLocationCreateCNotice = "PrivateLocationCreateNotice";
 /*End of common definitions*/
 
 
         private Page(final String path__, final String... ids__) {
             try {
                 Loggers.DEBUG.debug("HELLO, ENUM VAL:" + this.name());
                 Loggers.DEBUG.debug("HELLO, ENUM PATH:" + path__);
                 Loggers.DEBUG.debug("HELLO, ENUM IDS:" + Arrays.toString(ids__));
                 Loggers.DEBUG.debug("HELLO, ADDING ELEMENTS TO PRETTYURL MAP");
                 PrettyURLMap_.put(this, path__);
                 Loggers.DEBUG.debug("HELLO, ADDING ELEMENTS TO ALLPAGEELEMENTS");
                 PutAllPageElementIds(ids__);
                 Loggers.DEBUG.debug("HELLO, ADDING ELEMENTS TO ALLPAGEELEMENTS BY PAGE");
                 PutAllPageElementIdsByPage(this, ids__);
             } catch (
                     @WARNING(warning = "Don't remove this catch. You'll not see any duplicate id exceptions etc. anywhere if you do so.")
                     final Exception e) {
                 Loggers.EXCEPTION.error("SORRY! SOMETHING WENT WRONG DURING INITIALIZATION. THIS SHOULD EXPTECTED TO BE FATAL.", e);
             }
         }
 
         abstract public String toString();
 
         abstract public String getURL();
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface Servlet ---------------------
 
     /**
      * @param serveletConfig__
      * @throws ServletException
      */
     @Override
     public void init(final ServletConfig serveletConfig__) throws ServletException {
         super.init(serveletConfig__);
 
         final ItsNatHttpServlet inhs__ = getItsNatHttpServlet();
 
         final ItsNatServletConfig itsNatServletConfig = inhs__.getItsNatServletConfig();
 
         itsNatServletConfig.setDebugMode(true);
         itsNatServletConfig.setClientErrorMode(ClientErrorMode.SHOW_SERVER_AND_CLIENT_ERRORS);
         itsNatServletConfig.setLoadScriptInline(true);
         itsNatServletConfig.setUseGZip(UseGZip.MARKUP);
         //itsNatServletConfig.setDefaultSyncMode(SyncMode.SYNC);
         itsNatServletConfig.setAutoCleanEventListeners(true);
         itsNatServletConfig.setDefaultEncoding("UTF-8");
         itsNatServletConfig.setReferrerEnabled(true);
 
         setDefaultLocale:
         {
             Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0001"));
             Locale.setDefault(new Locale("en", "US"));
             Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0002"), Locale.getDefault().toString());
         }
 
         Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0003"), Locale.getDefault().toString());
 
 
         /*Add a listner to convert pretty urls to proper urls*/
         inhs__.addItsNatServletRequestListener(new ItsNatServletRequestListener() {
             @Override
             public void processRequest(final ItsNatServletRequest request__, final ItsNatServletResponse response__) {
                 final ItsNatDocument itsNatDocument__ = request__.getItsNatDocument();
                 /*if(itsNatDocument != null && ((HttpServletRequest) request__.getServletRequest()).getPathInfo().contains("itsnat_doc_name")){
                 throw new java.lang.RuntimeException("INVALID URL");//This code does not seem to work, please verify.
                 }*/
                 if (itsNatDocument__ == null && request__.getServletRequest().getAttribute(ITSNAT_DOC_NAME) == null) {
                     final HttpServletRequest httpServletRequest = (HttpServletRequest) request__.getServletRequest();
                     pathResolver(request__, response__);
                     parameterResolver(request__);
                     request__.getItsNatServlet().processRequest(httpServletRequest, response__.getServletResponse());
                 }
             }
         });
 
         final String realPath__ = getServletContext().getRealPath("/");
         REAL_PATH = realPath__;
         /*final String pathPrefix__ = realPath__ + "WEB-INF/pages/";*/
 
         final String pathPrefix__ = RBGet.getGlobalConfigKey("PAGEFILES") != null ? RBGet.getGlobalConfigKey("PAGEFILES") : realPath__ + WEB_INF_PAGES;
 
         registerDocumentTemplates:
         {
             inhs__.registerItsNatDocumentTemplate(aarrr.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(aarrr)).addItsNatServletRequestListener(new ListenerAarrr());
 
             inhs__.registerItsNatDocumentTemplate(locationMain.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(locationMain)).addItsNatServletRequestListener(new ListenerMain());
 
             inhs__.registerItsNatDocumentTemplate(photos.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerPhoto());
 
             inhs__.registerItsNatDocumentTemplate(organize.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerOrganize());
 
             inhs__.registerItsNatDocumentTemplate(findFriend.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerFriends());
 
             inhs__.registerItsNatDocumentTemplate(book.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerBookings());
 
             inhs__.registerItsNatDocumentTemplate(profile.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerProfile());
 
             inhs__.registerItsNatDocumentTemplate(i.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerI());
 
             inhs__.registerItsNatDocumentTemplate(activate.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerActivate());
 
             inhs__.registerItsNatDocumentTemplate(share.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(skeleton)).addItsNatServletRequestListener(new ListenerShare());
 
             inhs__.registerItsNatDocumentTemplate(geoBusiness.toString(), "text/html", new TemplateSourceGeoBusiness()).addItsNatServletRequestListener(new ListenerGeoBusiness());
 
             inhs__.registerItsNatDocumentTemplate(templateGeneric.toString(), "text/html", new TemplateGeneric(true));
         }
 
         registerDocumentFragmentTemplatesAKAWidgets:
         {
 //            inhs__.registerItsNatDocFragmentTemplate(photoCRUD.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(photoCRUD));
 
             inhs__.registerItsNatDocFragmentTemplate(photo$Description.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(photo$Description));
 
             inhs__.registerItsNatDocFragmentTemplate(signInOn.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(signInOn));
 
             inhs__.registerItsNatDocFragmentTemplate(privateLocationCreate.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateLocationCreate));
 
             inhs__.registerItsNatDocFragmentTemplate(privateLocationView.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateLocationView));
 
             inhs__.registerItsNatDocFragmentTemplate(privateLocationDelete.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateLocationDelete));
 
             inhs__.registerItsNatDocFragmentTemplate(privateEventCreate.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateEventCreate));
 
             inhs__.registerItsNatDocFragmentTemplate(privateEventView.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateEventView));
 
             inhs__.registerItsNatDocFragmentTemplate(privateEventDelete.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(privateEventDelete));
 
             inhs__.registerItsNatDocFragmentTemplate(wOIEDGrabber.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(wOIEDGrabber));
 
             inhs__.registerItsNatDocFragmentTemplate(downTownHeatMap.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(downTownHeatMap));
 
             inhs__.registerItsNatDocFragmentTemplate(findFriendWidget.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(findFriendWidget));
 
             inhs__.registerItsNatDocFragmentTemplate(friendAdd.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(friendAdd));
 
             inhs__.registerItsNatDocFragmentTemplate(friendDelete.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(friendDelete));
 
             inhs__.registerItsNatDocFragmentTemplate(friendList.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(friendList));
 
             inhs__.registerItsNatDocFragmentTemplate(genericButton.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(genericButton));
 
             inhs__.registerItsNatDocFragmentTemplate(displayName.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(displayName));
 
             inhs__.registerItsNatDocFragmentTemplate(wallHanlder.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(wallHanlder));
 
             inhs__.registerItsNatDocFragmentTemplate(passwordChange.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(passwordChange));
 
             inhs__.registerItsNatDocFragmentTemplate(forgotPasswordChange.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(forgotPasswordChange));
 
             inhs__.registerItsNatDocFragmentTemplate(profileWidget.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(profileWidget));
 
             inhs__.registerItsNatDocFragmentTemplate(album.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(album));
 
             inhs__.registerItsNatDocFragmentTemplate(userProperty.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(userProperty));
 
             inhs__.registerItsNatDocFragmentTemplate(userPropertySidebar.toString(), "text/html", pathPrefix__ + PrettyURLMap_.get(userPropertySidebar));
         }
     }
 
 // ------------------------ CANONICAL METHODS ------------------------
 
 
 // -------------------------- STATIC METHODS --------------------------
 
     private static void PutAllPageElementIdsByPage(final PageFace page__, final String... ids__) {
         final HashSet<String> ids_ = new HashSet<String>();
         ids_.addAll(Arrays.asList(ids__));
         GlobalPageIdRegistry.put(page__, ids_);
     }
 
     /**
      * Some nested widgets have no access to the request. instead of passing the request along the widget chain,
      * we store it in the users session.
      * <p/>
      * While this implementation may consume a bit more memory(could be an issue), it allows the widget to set the
      * value to null on a per request basis.
      *
      * @param request__
      */
     private static void parameterResolver(final ItsNatServletRequest request__) {
         wallEntryRelated:
         {
             final String wall_entry = request__.getServletRequest().getParameter(WallWidget.PARAM_WALL_ENTRY);
             if (wall_entry != null) {
                 request__.getItsNatSession().setAttribute(WallWidget.PARAM_WALL_ENTRY, wall_entry);
             }
         }
 
         others:
         {
         }
     }
 
     /**
      * We have a URL of type say www.ilikeplaces.com/page/Egypt
      * where we are supposed to receive the /Egypt part. Most requests WILL be
      * <p/>
      * location requests so we go optimistic on that first.
      *
      * @param request__
      * @param response__
      */
     private static void pathResolver(final ItsNatServletRequest request__, final ItsNatServletResponse response__) {
         final String pathInfo = ((HttpServletRequest) request__.getServletRequest()).getPathInfo();
         final String URL__ = pathInfo == null ? "" : ((HttpServletRequest) request__.getServletRequest()).getPathInfo().substring(1);//Removes preceding slash
         if (isHomePage(URL__)) {
             Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0012"));
             Loggers.INFO.info(((HttpServletRequest) request__.getServletRequest()).getRequestURL().toString()
                     + (((HttpServletRequest) request__.getServletRequest()).getQueryString() != null
                        ? ((HttpServletRequest) request__.getServletRequest()).getQueryString()
                        : ""));
             request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocAarrr);/*Framework specific*/
 
 //            final ItsNatHttpSession itsNatHttpSession = (ItsNatHttpSession) request__.getItsNatSession();
 //            final Object attribute__ = itsNatHttpSession.getAttribute(ServletLogin.HumanUser);
 //            final SessionBoundBadRefWrapper<HumanUserLocal> sessionBoundBadRefWrapper = attribute__ == null ? null : (SessionBoundBadRefWrapper<HumanUserLocal>) attribute__;
 //
 //            if (sessionBoundBadRefWrapper != null && sessionBoundBadRefWrapper.boundInstance.getHumanUserId() != null) {
 //                try {
 //                    ((HttpServletResponse) response__.getServletResponse()).sendRedirect(LOCATION_HUB);
 //                } catch (final IOException e) {
 //                    Loggers.EXCEPTION.error("", e);
 //                }
 //            }
         } else {
             if (isNonLocationPage(URL__)) {/*i.e. starts with underscore*/
                 final HttpSession httpSession = ((HttpServletRequest) request__.getServletRequest()).getSession(false);
                 if (isSignOut(URL__)) {//This can never happen, as there is a filter in place for this
                     if (httpSession != null) {
                         try {
                             ((HttpServletRequest) request__.getServletRequest()).getSession(false).invalidate();
                         } finally {
                             request__.getServletRequest().setAttribute("location", "");
                             request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocAarrr);/*Framework specific*/
                             try {
                                 ((HttpServletResponse) response__.getServletResponse()).sendRedirect(LOCATION_HUB);
                             } catch (final IOException e) {
                                 Loggers.EXCEPTION.error(Loggers.EMBED, e);
                             }
                         }
                     }
                 } else if (isPhotoPage(URL__)) {
                     request__.getServletRequest().setAttribute(RBGet.globalConfig.getString("HttpSessionAttr.location"), getPhotoLocation(URL__));
                     request__.getServletRequest().setAttribute("photoURL", getPhotoURL(URL__));
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, "photo");/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0005") + getPhotoLocation(URL__));
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0006") + getPhotoURL(URL__));
                 } else if (isHumanPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocPhotos);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0007"));
                 } else if (isOrganizePage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocOrganize);/*Framework specific*/
                     request__.getServletRequest().setAttribute(Page.DocOrganizeCategory, request__.getServletRequest().getParameter(Page.DocOrganizeCategory));
                     request__.getServletRequest().setAttribute(Page.DocOrganizeLocation, request__.getServletRequest().getParameter(Page.DocOrganizeLocation));
                     request__.getServletRequest().setAttribute(Page.DocOrganizeEvent, request__.getServletRequest().getParameter(Page.DocOrganizeEvent));
                     request__.getServletRequest().setAttribute(Page.DocOrganizeAlbum, request__.getServletRequest().getParameter(Page.DocOrganizeAlbum));
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0013"));
                 } else if (isFriendsPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocFriends);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0014"));
                 } else if (isBookingsPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocBook);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0015"));
                 } else if (isProfilePage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocProfile);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0016"));
                 } else if (isIPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocI);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0017"));
                 } else if (isActivatePage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocActivate);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0018"));
                 } else if (isSharePage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocShare);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0019"));
                 } else if (isGeoBusinessPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocGeoBusiness);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0020"));
                 } else if (isTemplateGenericPage(URL__)) {
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocPublic);/*Framework specific*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0021"));
                 } else {/*Divert to home page*/
                     Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0008"));
                     request__.getServletRequest().setAttribute("location", "");
                     request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocAarrr);/*Framework specific*/
                 }
             } else if (isCorrectLocationFormat(URL__)) {
                 request__.getServletRequest().setAttribute("location", URL__);
                 request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Controller.Page.DocLocation);/*Framework specific*/
                 Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0009") + URL__);
             } else {/*Divert to home page*/
                 Loggers.INFO.info(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0012"));
                 request__.getServletRequest().setAttribute("location", "");
                 request__.getServletRequest().setAttribute(ITSNAT_DOC_NAME, Page.DocAarrr);/*Framework specific*/
             }
         }
     }
 
     /**
      * Check if the place contains any odd/special characters that are not valid
      * Also check if the url is of itsnat?docblablabla format with "?"
      * Home page will not have any parameters. e.g. ilikeplaces.com/ilikeplaces/page/[nothing here]
      *
      * @param URL__
      * @return boolean
      */
     static private boolean isHomePage(final String URL__) {
         return URL__.length() == 0;
     }
 
     static private boolean isNonLocationPage(final String URL_) {
         return (URL_.startsWith("_") || URL_.startsWith("#"));
     }
 
     static private boolean isSignOut(final String URL_) {
         return (URL_.equals("_so"));
     }
 
     static private boolean isPhotoPage(final String URL_) {
         return (URL_.startsWith("_photo_") && URL_.split("_").length == 4);
     }
 
     static private String getPhotoLocation(final String URL_) {
         return URL_.replace("_photo_", "").split("_")[0];
     }
 
     static private String getPhotoURL(final String URL_) {
         return URL_.replace("_photo_", "").split("_")[1];
     }
 
     static private boolean isHumanPage(final String URL_) {
         return (URL_.startsWith("_me"));
     }
 
     static private boolean isOrganizePage(final String URL_) {
         return (URL_.startsWith("_org"));
     }
 
     static private boolean isFriendsPage(final String URL_) {
         return (URL_.startsWith("_friends"));
     }
 
     static private boolean isBookingsPage(final String URL_) {
         return (URL_.startsWith("_book"));
     }
 
     static private boolean isProfilePage(final String URL_) {
         return (URL_.startsWith("_profile"));
     }
 
     static private boolean isIPage(final String URL_) {
         return (URL_.startsWith("_i"));
     }
 
     static private boolean isActivatePage(final String URL_) {
         return (URL_.startsWith("_activate"));
     }
 
     static private boolean isSharePage(final String URL_) {
         return (URL_.startsWith("_share"));
     }
 
     static private boolean isGeoBusinessPage(final String URL_) {
         return (URL_.startsWith("_geobusiness"));
     }
 
     static private boolean isTemplateGenericPage(final String URL_) {
         return (URL_.startsWith("_public"));
     }
 
     /**
      * Check if the place contains any odd/special characters that are not valid
      * Also check if the url is of itsnat?docblablabla format with "?"
      *
      * @param URL__
      * @return boolean
      */
     @FIXME(issue = "Location should not contain underscores")
     static private boolean isCorrectLocationFormat(final String URL__) {
         /*"_" (underscore) check first is vital as the photo and me urls might have "/"*/
         return !(URL__.startsWith("_") || URL__.contains(","));
     }
 
     /**
      * Register all your document keys before using. Accepts variable argument length.
      * Usage: putAllPageElementIds("id1","id2","id3");
      *
      * @param ids__
      */
     public final static void PutAllPageElementIds(final String... ids__) {
         for (final String id_ : ids__) {
             if (!GlobalHTMLIdRegistry.containsKey(id_)) {
                 /**
                  * Do not verify if element exists in "document" here as elements
                  * can be dynamically created
                  */
                 GlobalHTMLIdRegistry.put(id_, id_);
             } else {
                 throw new SecurityException(RBGet.logMsgs.getString("ai.ilikeplaces.servlets.Controller.0011" + id_));
             }
         }
     }
 }
