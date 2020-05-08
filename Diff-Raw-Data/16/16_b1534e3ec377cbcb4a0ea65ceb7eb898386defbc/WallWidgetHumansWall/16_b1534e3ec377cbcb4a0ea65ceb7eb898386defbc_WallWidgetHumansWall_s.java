 package ai.ilikeplaces.logic.Listeners.widgets;
 
 import ai.ilikeplaces.doc.FIXME;
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.entities.HumansIdentity;
 import ai.ilikeplaces.entities.HumansNetPeople;
 import ai.ilikeplaces.entities.Msg;
 import ai.ilikeplaces.entities.Wall;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.mail.SendMail;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.util.*;
 import ai.ilikeplaces.util.cache.SmartCache;
 import ai.ilikeplaces.util.cache.SmartCache2;
 import ai.ilikeplaces.util.jpa.RefreshSpec;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.html.HTMLDocument;
 import org.xml.sax.SAXException;
 
 import javax.xml.transform.TransformerException;
 import java.io.IOException;
 import java.text.MessageFormat;
 
 /**
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Jun 17, 2010
  * Time: 12:17:36 AM
  */
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 public class WallWidgetHumansWall extends WallWidget {
 
     private static final String WALL_SUBIT_FROM_EMAIL = "ai/ilikeplaces/widgets/WallSubmitFromEmail.xhtml";
     private static final RefreshSpec REFRESH_SPEC = new RefreshSpec("wallMsgs", "wallMutes");
     private static final RefreshSpec REFRESH_SPEC_EMPTY = new RefreshSpec();
 
     final static public SmartCache<Pair<String, String>, Long> HUMANS_WALL_ID = new SmartCache<Pair<String, String>, Long>(new SmartCache.RecoverWith<Pair<String, String>, Long>() {
         @Override
         public Long getValue(final Pair<String, String> current_friend) {
             return DB.getHumanCrudWallLocal(false).readWallId(new HumanId(current_friend.getValue()), new Obj<String>(current_friend.getKey())).returnValueBadly();
         }
     });
 
     final static public SmartCache2<String, Msg, String> LAST_WALL_ENTRY = new SmartCache2<String, Msg, String>(
             new SmartCache2.RecoverWith<String, Msg, String>() {
 
                 @Override
                 public Msg getValue(final String whosWall, final String visitor) {
                     return DB.getHumanCrudWallLocal(false).readWallLastEntries(new HumanId(whosWall), new Obj<String>(visitor), 1, new RefreshSpec()).returnValue().get(0);//Guaranteed not to fail since wall has atleast one entry
                 }
             }
     );
 
     private static final String TALK_AT_DOWN_TOWN_ER_0_S = "talk.at.down.town.er.0.s";
 
 
     HumanId requestedProfile;
     HumanId currUserAsVisitor;
 
     public WallWidgetHumansWall(final ItsNatServletRequest request__, final Element appendToElement__, final HumanId requestedProfile, final HumanId currUserAsVisitor) {
         super(request__, appendToElement__, requestedProfile, currUserAsVisitor);
     }
 
     /**
      *
      */
     @Override
     protected void init(final Object... initArgs) {
         this.requestedProfile = ((HumanId) initArgs[0]).getSelfAsValid();
         this.currUserAsVisitor = ((HumanId) initArgs[1]).getSelfAsValid();
 
         final HumansIdentity currUserAsVisitorHI = UserProperty.HUMANS_IDENTITY_CACHE.get((currUserAsVisitor.getHumanId()),"");
         final HumansIdentity requestedProfileHI = UserProperty.HUMANS_IDENTITY_CACHE.get((requestedProfile.getHumanId()),"");
 
         super.setWallProfileName(currUserAsVisitorHI.getHuman().getDisplayName());
         super.setWallProfilePhoto(UserProperty.formatProfilePhotoUrl(currUserAsVisitorHI.getHumansIdentityProfilePhoto()));
         super.setWallTitle(MessageFormat.format(RBGet.gui().getString(TALK_AT_DOWN_TOWN_ER_0_S), requestedProfileHI.getHuman().getDisplayName()));
 
         fetchToEmail();
 
         final Wall wall = DB.getHumanCrudWallLocal(true).readWall(requestedProfile, new Obj<HumanId>(currUserAsVisitor), REFRESH_SPEC).returnValueBadly();
 
         for (final Msg msg : wall.getWallMsgs()) {
             new UserProperty(request, $$(Controller.Page.wallContent), new HumanId(msg.getMsgMetadata())) {
                 protected void init(final Object... initArgs) {
                     $$(Controller.Page.user_property_content).setTextContent(msg.getMsgContent());
                 }
             };
         }
 
         DB.getHumanCRUDHumansUnseenLocal(false).removeEntry(currUserAsVisitor.getObjectAsValid(), wall.getWallId());
 
         $$displayWallAsMuted($$(Controller.Page.wallMute), wall.getWallMutes().contains(currUserAsVisitor));
 
     }
 
 
     /**
      * @param args
      */
     protected void fetchToEmail(final Object... args) {
         try {
             final Document document = HTMLDocParser.getDocument(Controller.REAL_PATH + Controller.WEB_INF_PAGES + WALL_SUBIT_FROM_EMAIL);
 
             displayNone($$(ORGANIZE_SECTION, document));
             $$(ORGANIZE_SECTION, document).getParentNode().removeChild($$(ORGANIZE_SECTION, document));
 
             fetchToEmail = HTMLDocParser.convertNodeToHtml($$(WALL_SUBMIT_WIDGET, document));
 
         } catch (final TransformerException e) {
             Loggers.EXCEPTION.error("", e);
         } catch (final SAXException e) {
             Loggers.EXCEPTION.error("", e);
         } catch (final IOException e) {
             Loggers.EXCEPTION.error("", e);
         }
     }
 
     @Override
     protected void registerEventListeners(final ItsNatHTMLDocument itsNatHTMLDocument__, final HTMLDocument hTMLDocument__) {
 
 
         itsNatHTMLDocument__.addEventListener((EventTarget) $$(Controller.Page.wallSubmit), EventType.CLICK.toString(), new EventListener() {
 
             private HumanId myrequestedProfile = requestedProfile;
             private HumanId mycurrUserAsVisitor = currUserAsVisitor;
 
             @Override
             public void handleEvent(final Event evt_) {
 
                 if (wallAppend.validate() == 0) {
                     if (!wallAppend.getObj().equals("")) {
 
                         @FIXME("THIRD PARAMETER WRONG! SINCE 1 AND 3 PARAMETERS ARE SAME AND BACKEND USES 1, SO FAR NO ISSUE")
                         final Return<Wall> r = DB.getHumanCrudWallLocal(true).addEntryToWall(myrequestedProfile, mycurrUserAsVisitor, new Obj<HumanId>(mycurrUserAsVisitor), wallAppend.getObj());
 
 
                         if (r.returnStatus() == 0) {
                             $$(Controller.Page.wallAppend).setAttribute(MarkupTag.TEXTAREA.value(), "");
                             wallAppend.setObj("");
                             LAST_WALL_ENTRY.MAP.put(new String(myrequestedProfile.getHumanId()), null);
 
                             clear($$(Controller.Page.wallContent));
                             final Wall wall = DB.getHumanCrudWallLocal(true).readWall(requestedProfile, new Obj<HumanId>(currUserAsVisitor), REFRESH_SPEC).returnValue();
                             final StringBuilder b = new StringBuilder("");
                             for (final Msg msg : wall.getWallMsgs()) {
                                 b.append(new UserProperty(request,
                                         $$(Controller.Page.wallContent),
                                         ElementComposer.compose($$(MarkupTag.DIV)).$ElementSetText(msg.getMsgContent()).get(),
                                         new HumanId(msg.getMsgMetadata())) {
                                 }.fetchToEmail);
                             }
 
                             new Thread(new Runnable() {
 
                                 private HumanId mymyrequestedProfile = myrequestedProfile;
                                 private HumanId mycurrUserAsVisitor = currUserAsVisitor;
                                 private String myfetchToEmail = fetchToEmail;
                                 private StringBuilder myb = b;
                                 private Wall mywall = wall;
 
 
                                 @Override
                                 public void run() {
                                     final HumansNetPeople hnps = DB.getHumanCRUDHumanLocal(true).doDirtyRHumansNetPeople(mymyrequestedProfile);
 
                                     for (final HumansNetPeople hpe : hnps.getHumansNetPeoples()) {
                                        if (!wall.getWallMutes().contains(hpe)) {
                                             SendMail.getSendMailLocal().sendAsHTMLAsynchronously(hpe.getHumanId(), hnps.getDisplayName(), myfetchToEmail + myb.toString());
                                             DB.getHumanCRUDHumansUnseenLocal(false).addEntry(hpe.getHumanId(), mywall.getWallId());
                                         }
                                     }
 
                                 }
                             }).start();
 
 
                         } else {
                             $$(Controller.Page.wallNotice).setTextContent(r.returnMsg());
                         }
                     }
                 }
             }
         }, false);
 
         itsNatHTMLDocument__.addEventListener((EventTarget) $$(Controller.Page.wallMute), EventType.CLICK.toString(), new EventListener() {
 
             private HumanId myrequestedProfile = requestedProfile;
             private HumanId mycurrUserAsVisitor = currUserAsVisitor;
 
             @Override
             public void handleEvent(final Event evt_) {
 
                 if (DB.getHumanCrudWallLocal(true).readWall(myrequestedProfile, new Obj<HumanId>(currUserAsVisitor), REFRESH_SPEC_EMPTY).returnValueBadly().getWallMutes().contains(mycurrUserAsVisitor)) {
                     if (DB.getHumanCrudWallLocal(true).unmuteWall(myrequestedProfile, mycurrUserAsVisitor, new Obj<HumanId>(currUserAsVisitor)).returnStatus() == 0) {
                         $$displayWallAsMuted(evt_, false);
                     }
 
                 } else {
                     if (DB.getHumanCrudWallLocal(true).muteWall(myrequestedProfile, mycurrUserAsVisitor, new Obj<HumanId>(currUserAsVisitor)).returnStatus() == 0) {
                         $$displayWallAsMuted(evt_, true);
                     }
                 }
             }
         }, false);
     }
 }
