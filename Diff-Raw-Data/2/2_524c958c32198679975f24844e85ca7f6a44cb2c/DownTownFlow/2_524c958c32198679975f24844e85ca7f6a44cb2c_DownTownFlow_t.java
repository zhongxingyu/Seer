 package ai.ilikeplaces.logic.Listeners.widgets;
 
 import ai.ilikeplaces.doc.FIXME;
 import ai.ilikeplaces.entities.*;
 import ai.ilikeplaces.logic.Listeners.JSCodeToSend;
 import ai.ilikeplaces.logic.Listeners.widgets.privateevent.PrivateEventViewSidebar;
 import ai.ilikeplaces.logic.Listeners.widgets.privateevent.PrivateEventViewSidebarCriteria;
 import ai.ilikeplaces.logic.contactimports.ImportedContact;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.validators.unit.Email;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.servlets.filters.ProfileRedirect;
 import ai.ilikeplaces.util.*;
 import ai.ilikeplaces.util.cache.SmartCache;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.w3c.dom.Element;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.html.HTMLDocument;
 
 import java.text.MessageFormat;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * This is still not fool proof. Autoplay should play upon:
  * <p/>
  * Failure of proper entity read
  * Network delays on client (when loading images)
  * <p/>
  * Created by IntelliJ IDEA.
  * User: Ravindranath Akila
  * Date: 10/16/11
  * Time: 2:28 PM
  */
 public class DownTownFlow extends AbstractWidgetListener<DownTownFlowCriteria> {
 // ------------------------------ FIELDS ------------------------------
 
 
 // ------------------------------ FIELDS STATIC --------------------------
 
     private static final String ENTER_A_VALID_EMAIL = "enter.a.valid.email";
     private static final String INVITED_ADDED_0 = "invited.added.0";
     private static final String COULD_NOT_INVITE_AND_ADD_TRY_AGAIN = "could.not.invite.and.add.try.again";
     private static final String MESSAGE_ADDING_SELF_AS_FRIEND = "message.adding.self.as.friend";
     private static final String IS_ALREADY_YOUR_FRIEND = "0.is.already.your.friend";
     private static final String READ_MORE = "read.more";
 
     @FIXME("We need to move this cache to EJB's, stateless hopefully.")
     public static final SmartCache.RecoverWith<String, Object> STATIC_VARIABLE_RECOVER_WITH_BE_FRIENDS = new SmartCache.RecoverWith<String, Object>() {
         @Override
         public Object getValue(final String s) {
            return DB.getHumanCRUDHumanLocal(true).doDirtyRHumansBefriends(new HumanId(s.split("BE_")[1]).getSelfAsValid()).returnValueBadly();
         }
     };
 
     @FIXME("We need to move this cache to EJB's, stateless hopefully.")
     public static final SmartCache.RecoverWith<String, Object> STATIC_VARIABLE_RECOVER_WITH_FRIENDS = new SmartCache.RecoverWith<String, Object>() {
         @Override
         public Object getValue(final String s) {
             return DB.getHumanCRUDHumanLocal(true).doDirtyRHumansNetPeople(new HumanId(s).getSelfAsValid()).getHumansNetPeoples();
         }
     };
 
 // ------------------------------ FIELDS (NON-STATIC)--------------------
 
 
     private String str;
 
     // --------------------------- CONSTRUCTORS ---------------------------
     public DownTownFlow(final ItsNatServletRequest request__, final DownTownFlowCriteria downTownFlowCriteria__, final Element appendToElement__) {
 
         super(request__, Controller.Page.DownTownFlow, downTownFlowCriteria__, appendToElement__);
 
         switch (downTownFlowCriteria__.getDownTownFlowDisplayComponent()) {
             case TALKS: {
                 $$displayBlock($$(Controller.Page.DownTownFlowTalks));
                 break;
             }
 
             case MOMENTS: {
                 $$displayBlock($$(Controller.Page.DownTownFlowMoments));
                 break;
             }
             case TRIBES: {
                 $$displayBlock($$(Controller.Page.DownTownFlowTribes));
                 break;
             }
             default: {
             }
         }
     }
 
     // ------------------------ OVERRIDING METHODS ------------------------
     @Override
     protected void init(final DownTownFlowCriteria downTownFlowCriteria) {
 
         final String currentUser = downTownFlowCriteria.getHumanId().getObj();
         final List<HumansNetPeople> beFriends = (List<HumansNetPeople>) downTownFlowCriteria.getHumanUserLocal().cache("BE_" + currentUser, STATIC_VARIABLE_RECOVER_WITH_BE_FRIENDS);
         //new People(request,new PeopleCriteria().setPeople((List<HumanIdFace>)(List<?>)beFriends),$(Controller.Page.Skeleton_left_column));
 
 
         switch (downTownFlowCriteria.getDownTownFlowDisplayComponent()) {
             case TALKS: {
                 UCDownTownFlowFriends:
                 {
                     final List<Wall> notifiedWalls = DB.getHumanCRUDHumansUnseenLocal(false).readEntries(downTownFlowCriteria.getHumanId().getHumanId());
 
                     final Set<Long> notifiedWallLongs = new HashSet<Long>(notifiedWalls.size());
                     for (final Wall wall : notifiedWalls) {
                         notifiedWallLongs.add(wall.getWallId());
                     }
                     for (final HumansNetPeople friend : beFriends) {
                         new UserPropertySidebar(request, $$(Controller.Page.DownTownFlowTalksFriends), new HumanId(friend.getHumanId())) {
                             protected void init(final Object... initArgs) {
 
                                 final Long friendWallId = WallWidgetHumansWall.HUMANS_WALL_ID.get(new Pair<String, String>(new String(currentUser), new String(friend.getHumanId())));
 
                                 final Msg lastWallEntry = WallWidgetHumansWall.LAST_WALL_ENTRY.get(new String(friend.getHumanId()), new String(currentUser));
 
                                 final Element appendToElement__ = $$(UserPropertySidebarIds.user_property_sidebar_content);
 
                                 new UserPropertySidebar(request, appendToElement__, new HumanId(lastWallEntry.getMsgMetadata())) {
                                     final Msg mylastWallEntry = lastWallEntry;
                                     private String href;
 
                                     protected void init(final Object... initArgs) {
 
                                         $$displayBlock($$(UserPropertySidebarIds.user_property_sidebar_talk));
                                         $$displayNone($$(UserPropertySidebarIds.user_property_sidebar_name_section));
 
                                         href = ProfileRedirect.PROFILE_URL + HUMANS_IDENTITY_SIDEBAR_CACHE.get(friend.getHumanId(), "").getUrl().getUrl();
 
                                         String msgContent = lastWallEntry.getMsgContent();
 
                                         TrimMessageContentForReadabilityOnSidebar:
                                         {
                                             final int length = msgContent.length();
                                             if (40 < length) {
                                                 msgContent = msgContent.substring(0, 40) + RBGet.gui().getString(READ_MORE);
                                             }
                                         }
 
                                         Element commentHref = ElementComposer.compose($$(MarkupTag.A)).$ElementSetText(msgContent).$ElementSetHref(href).get();
                                         $$(UserPropertySidebarIds.user_property_sidebar_content).appendChild(commentHref);
                                         if (notifiedWallLongs.contains(friendWallId)) {
                                             new Notification(request, new NotificationCriteria("!!!"), commentHref);
                                         }
                                     }
 
                                     @Override
                                     protected void registerEventListeners(ItsNatHTMLDocument itsNatHTMLDocument_, HTMLDocument hTMLDocument_) {
 
                                         itsNatHTMLDocument_.addEventListener((EventTarget) $$(UserPropertySidebarIds.user_property_sidebar_engage), EventType.CLICK.toString(), new EventListener() {
                                             @Override
                                             public void handleEvent(final Event evt_) {
 
                                                 $$sendJS(JSCodeToSend.redirectPageWithURL(href));
                                             }
                                         }, false);
                                     }
                                 };
                             }
                         };
                     }
                 }
                 break;
             }
 
             case MOMENTS: {
                 UCDownTownFlowMoments:
                 {
                     for (final PrivateEvent privateEvent : DB.getHumanCrudPrivateEventLocal(false).doDirtyRPrivateEventsOfHuman(downTownFlowCriteria.getHumanId()).returnValue()) {
                         new PrivateEventViewSidebar(
                                 request,
                                 new PrivateEventViewSidebarCriteria().setPrivateEventId__(privateEvent.getPrivateEventId()).setHumanId__(downTownFlowCriteria.getHumanId().getHumanId()),
                                 $$(Controller.Page.DownTownFlowMomentsMoments));
                     }
                 }
                 break;
             }
 
             case TRIBES: {
                 UCDownTownFlowTRIBES:
                 {
                     for (final Tribe tribe : DB.getHumanCRUDTribeLocal(false).getHumansTribes(downTownFlowCriteria.getHumanId())) {
                         new TribeSidebar(
                                 request,
                                 new TribeSidebarCriteria().setTribeId(tribe.getTribeId()).setHumanId(downTownFlowCriteria.getHumanId().getHumanId()),
                                 $$(Controller.Page.DownTownFlowTribesTribes));
                     }
                 }
                 break;
             }
             default: {
             }
         }
     }
 
     @Override
     protected void registerEventListeners(final ItsNatHTMLDocument itsNatHTMLDocument_, final HTMLDocument hTMLDocument_) {
 
         super.registerForInputText($$(Controller.Page.DownTownFlowInviteEmail),
                 new AIEventListener<DownTownFlowCriteria>(criteria) {
                     /**
                      * Override this method and avoid {@link #handleEvent(org.w3c.dom.events.Event)} to make debug logging transparent
                      *
                      * @param evt fired from client
                      */
                     @Override
                     protected void onFire(Event evt) {
 
                         final Email email = new Email($$(evt).getAttribute(MarkupTag.INPUT.value()));
                         if (email.valid()) {
                             criteria.getInviteData().setEmail(email);
                         } else {
                             $$(Controller.Page.DownTownFlowInviteNoti).setTextContent(RBGet.gui().getString(ENTER_A_VALID_EMAIL));
                         }
                     }
                 }
         );
 
         super.registerForClick($$(Controller.Page.DownTownFlowInviteClick),
                 new AIEventListener<DownTownFlowCriteria>(criteria) {
                     /**
                      * Override this method and avoid {@link #handleEvent(org.w3c.dom.events.Event)} to make debug logging transparent
                      *
                      * @param evt fired from client
                      */
                     @Override
                     protected void onFire(Event evt) {
 
                         final Email email = criteria.getInviteData().getEmail();
 
                         if (email.valid()) {
                             if (!DB.getHumanCRUDHumanLocal(false).doDirtyCheckHuman(email.getObj()).returnValue()) {
                                 final Return<Boolean> returnVal = ai.ilikeplaces.logic.Listeners.widgets.Bate.sendInviteToOfflineInvite(
                                         UserProperty.HUMANS_IDENTITY_CACHE.get(criteria.getHumanId().getHumanId(), "").getHuman().getDisplayName(),
                                         new ImportedContact().setEmail(email.getObj()).setFullName(""));
                             }
 
                             if (!DB.getHumanCRUDHumanLocal(false).doDirtyIsHumansNetPeople(criteria.getHumanId(), new HumanId(email.getObj())).returnValue()) {
                                 if (!criteria.getHumanId().getHumanId().equals(email.getObj())) {
                                     Return<Boolean> r = DB.getHumanCRUDHumanLocal(true).doNTxAddHumansNetPeople(criteria.getHumanId(), new HumanId(email.getObj()));
                                     if (r.valid()) {
                                         $$(Controller.Page.DownTownFlowInviteNoti).setTextContent(MessageFormat.format(RBGet.gui().getString(INVITED_ADDED_0), email));
                                         $$(Controller.Page.DownTownFlowInviteEmail).setAttribute(MarkupTag.INPUT.value(), "");
                                     } else {
                                         $$(Controller.Page.DownTownFlowInviteNoti).setTextContent(RBGet.gui().getString(COULD_NOT_INVITE_AND_ADD_TRY_AGAIN));
                                     }
                                 } else {
                                     $$(Controller.Page.DownTownFlowInviteNoti).setTextContent(RBGet.gui().getString(MESSAGE_ADDING_SELF_AS_FRIEND));
                                 }
                             } else {
                                 $$(Controller.Page.DownTownFlowInviteNoti).setTextContent(
                                         MessageFormat.format(RBGet.gui().getString(IS_ALREADY_YOUR_FRIEND), UserProperty.HUMANS_IDENTITY_CACHE.get(email.getObj(), "").getHuman().getDisplayName()));
                             }
                         }
                     }
                 });
     }
 }
