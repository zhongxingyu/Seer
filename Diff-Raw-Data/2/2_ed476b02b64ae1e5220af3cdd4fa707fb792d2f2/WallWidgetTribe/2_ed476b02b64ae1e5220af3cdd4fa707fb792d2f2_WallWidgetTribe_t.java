 package ai.ilikeplaces.logic.Listeners.widgets;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.entities.*;
 import ai.ilikeplaces.logic.Listeners.JSCodeToSend;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.mail.SendMail;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.logic.validators.unit.VLong;
 import ai.ilikeplaces.logic.validators.unit.WallEntry;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.util.*;
 import ai.ilikeplaces.util.cache.SmartCache2;
 import ai.ilikeplaces.util.jpa.RefreshSpec;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.html.HTMLDocument;
 import org.xml.sax.SAXException;
 
 import javax.xml.transform.TransformerException;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Jun 17, 2010
  * Time: 12:17:36 AM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 public class WallWidgetTribe extends WallWidget<WallWidgetTribeCriteria> {
 // ------------------------------ FIELDS ------------------------------
 
     final static public SmartCache2<Long, Msg, String> LAST_WALL_ENTRY = new SmartCache2<Long, Msg, String>(
             new SmartCache2.RecoverWith<Long, Msg, String>() {
                 @Override
                 public Msg getValue(final Long whichWall, final String requester) {
                     final List<Msg> msgs = DB.getHumanCRUDTribeLocal(false).readWallLastEntries(new HumanId(requester), new Obj<Long>(whichWall), 1, new RefreshSpec()).returnValue();
                     final Msg returnVal;
                     if (msgs.isEmpty()) {
                         returnVal = null;
                     } else {
                         returnVal = msgs.get(0);
                     }
                     return returnVal;
                 }
             }
     );
 
     private static final String WALL_SUBIT_FROM_EMAIL = "ai/ilikeplaces/widgets/WallSubmitFromEmail.xhtml";
     public static final RefreshSpec REFRESH_SPEC = new RefreshSpec("wallMsgs", "wallMutes");
     private static final String TALK_AT_0 = "talk.at.0";
     private static final String WALL_ENTRY_CONSUMED_STATUES = "&wall_entry_consumed=true";
     private static final String WALL_ENTRY_CONSUMED = "wall_entry_consumed";
     private static final String TRUE = "true";
     private static final String NULL = "null";
     private static final String WALL_ENTRY = "wall_entry=";
     private static final String WALL_ENTRY_FROM_EMAIL_RECEIVED = "Wall entry from email received!";
     private static final String DERIVED_FROM_EMAIL = "DERIVED FROM EMAIL:{}";
     private static final String ENTERS_TEXT = " enters text:";
     private static final String CATEGORY = "category";
     private static final String LOCATION = "location";
     private static final String EVENT = "event";
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public WallWidgetTribe(final ItsNatServletRequest request__, final WallWidgetTribeCriteria wallWidgetTribeCriteria, final Element appendToElement__) {
         super(request__, wallWidgetTribeCriteria, appendToElement__);
     }
 
 // ------------------------ OVERRIDING METHODS ------------------------
 
     /**
      *
      */
     @Override
     protected void init(final WallWidgetTribeCriteria wallWidgetTribeCriteria) {
        final Tribe tribe = DB.getHumanCRUDTribeLocal(true).getTribe(criteria.getHumanId(), new VLong(criteria.getTribeId()), true).returnValueBadly();
 
         criteria.setWallId(tribe.getTribeWall().getWallId());
 
         fetchToEmail(tribe.getTribeId(),
                 tribe.getTribeId());
 
         final String wall_entry = request.getServletRequest().getParameter(WallWidget.PARAM_WALL_ENTRY);
         final String wall_entry_consumed = request.getServletRequest().getParameter(WALL_ENTRY_CONSUMED);
         Loggers.DEBUG.debug(WALL_ENTRY + (wall_entry != null ? wall_entry : NULL));
 
 
         final Return<Wall> aReturn = DB.getHumanCRUDTribeLocal(true).
                 readWall(criteria.getHumanId(), new VLong(criteria.getTribeId()), REFRESH_SPEC);
 
         UCHidingWallIfEmptyToShowInviteWidgetOnTop:
         {
             if (tribe.getTribeMembers().size() == 1) {
                 $$displayNone(WallWidgetIds.wallWidget);
             }
         }
 
         /**
          * If null, this means we have to check on if the wall entry parameter is available and update.
          * If not null, this means the wall entry has been consumed(we set it to true)
          */
         if ((wall_entry_consumed == null || !wall_entry_consumed.equals(TRUE)) && wall_entry != null) {//This will refresh or close the page after actions
             Loggers.DEBUG.debug(WALL_ENTRY_FROM_EMAIL_RECEIVED);
             final Return<Wall> r = DB.getHumanCRUDTribeLocal(true).addEntryToWall(criteria.getHumanId(),
                     criteria.getHumanId(),
                     new Obj<Long>(criteria.getTribeId()),
                     new WallEntry().setObjAsValid(wall_entry).getObj());//Use of class scoped wall entry is not possible as it isn't initialized yet
             Loggers.log(Loggers.LEVEL.DEBUG, DERIVED_FROM_EMAIL, r.returnMsg());
             final StringBuilder b = new StringBuilder("");
 
 
             for (final Msg msg : aReturn.returnValueBadly().getWallMsgs()) {//For the purpose of emailing the users the update
                 b.append(
                         new UserProperty(
                                 request,
                                 $$(WallWidgetIds.wallContent),
                                 ElementComposer.compose($$(MarkupTag.DIV)).$ElementSetText(msg.getMsgContent()).get(),
                                 new HumanId(msg.getMsgMetadata())) {
                         }.fetchToEmail
                 );
             }
             for (final HumansTribe hpe : tribe.getTribeMembers()) {
                 if (!hpe.getHumanId().equals(criteria.getHumanId().getObj())) {
                     SendMail.getSendMailLocal().sendAsHTMLAsynchronously(hpe.getHumanId(), tribe.getTribeName(), fetchToEmail + b.toString());
                     DB.getHumanCRUDHumansUnseenLocal(false).addEntry(hpe.getHumanId(), aReturn.returnValue().getWallId());
                 }
             }
 
             final boolean loadWallPageAfterAnEmailWallSubmit = false;
 
             if (loadWallPageAfterAnEmailWallSubmit) {
                 itsNatDocument_.addCodeToSend(JSCodeToSend.refreshPageWith(WALL_ENTRY_CONSUMED_STATUES));//
             } else {
                 itsNatDocument_.addCodeToSend(JSCodeToSend.ClosePageOrRefresh);//
             }
         } else {//Moves on with the wall without refresh
             for (final Msg msg : aReturn.returnValueBadly().getWallMsgs()) {
                 new UserProperty(request, $$(WallWidgetIds.wallContent), new HumanId(msg.getMsgMetadata())) {
                     protected void init(final Object... initArgs) {
                         $$(Controller.Page.user_property_content).setTextContent(msg.getMsgContent());
                     }
                 };//No use of fetching email content or initializing it
             }
         }
 
         DB.getHumanCRUDHumansUnseenLocal(false).removeEntry(criteria.getHumanId().getObjectAsValid(), aReturn.returnValue().getWallId());
 
 
         final HumansIdentity currUserAsVisitorHI = UserProperty.HUMANS_IDENTITY_CACHE.get((criteria.getHumanId().getHumanId()), "");
 
         super.setWallProfileName(currUserAsVisitorHI.getHuman().getDisplayName());
         super.setWallProfilePhoto(UserProperty.formatProfilePhotoUrl(currUserAsVisitorHI.getHumansIdentityProfilePhoto()));
         //Change property key please super.setWallTitle(MessageFormat.format(RBGet.gui().getString(TALK_AT_0), tribe.getTribeName()));
 
         $$displayWallAsMuted($$(WallWidgetIds.wallMute), aReturn.returnValueBadly().getWallMutes().contains(criteria.getHumanId()));
     }
 
     /**
      * @param args
      */
     protected void fetchToEmail(final Object... args) {
         try {
             final Document document = HTMLDocParser.getDocument(Controller.REAL_PATH + Controller.WEB_INF_PAGES + WALL_SUBIT_FROM_EMAIL);
 
             displayNone(($$(ORGANIZE_SECTION, document)));
 
             //$$(CATEGORY, document).setAttribute(MarkupTag.INPUT.value(), Integer.toString(Controller.Page.DocOrganizeModeEvent));
             //$$(LOCATION, document).setAttribute(MarkupTag.INPUT.value(), args[0].toString());
             //$$(EVENT, document).setAttribute(MarkupTag.INPUT.value(), args[1].toString());
 
 
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
 
         super.registerForClick($$(WallWidgetIds.wallSubmit),
                 new AIEventListener<WallWidgetTribeCriteria>(criteria) {
 
                     @Override
                     public void onFire(final Event evt_) {
                         Loggers.USER.info(criteria.getHumanId().getObj() + ENTERS_TEXT + wallAppend.getObj());
                         if (wallAppend.validate() == 0) {
                             if (!wallAppend.getObj().equals("")) {
                                 Loggers.USER.info(criteria.getHumanId().getObj() + ENTERS_TEXT + wallAppend.getObj());
 
 
                                 final Return<Wall> r = DB.getHumanCRUDTribeLocal(true).addEntryToWall(criteria.getHumanId(),
                                         criteria.getHumanId(),
                                         new VLong(criteria.getTribeId()),
                                         wallAppend.getObj());
 
 
                                 if (r.returnStatus() == 0) {
                                     $$(WallWidgetIds.wallAppend).setAttribute(MarkupTag.TEXTAREA.value(), "");
                                     wallAppend.setObj("");
 
                                     //LAST_WALL_ENTRY.MAP.put(criteria.getWallId(), null);
 
                                     clear($$(WallWidgetIds.wallContent));
                                     final Wall wall = (DB.getHumanCRUDTribeLocal(true).readWall(criteria.getHumanId(), new VLong(criteria.getTribeId()), REFRESH_SPEC).returnValueBadly());
                                     final StringBuilder b = new StringBuilder("");
                                     for (final Msg msg : wall.getWallMsgs()) {
                                         b.append(
                                                 new UserProperty(
                                                         request,
                                                         $$(WallWidgetIds.wallContent),
                                                         ElementComposer.compose($$(MarkupTag.DIV)).$ElementSetText(msg.getMsgContent()).get(),
                                                         new HumanId(msg.getMsgMetadata())) {
                                                 }.fetchToEmail
                                         );
                                     }
                                     final Return<Tribe> returnVal = DB.getHumanCRUDTribeLocal(true).getTribe(criteria.getHumanId(), new VLong(criteria.getTribeId()), true);
 
                                     if (returnVal.valid()) {
                                         final Tribe tribe = returnVal.returnValue();
                                         for (final HumansTribe hpe : tribe.getTribeMembers()) {
                                             if (!wall.getWallMutes().contains(hpe) && !hpe.getHumanId().equals(criteria.getHumanId().getObj())) {
                                                 SendMail.getSendMailLocal().sendAsHTMLAsynchronously(hpe.getHumanId(), tribe.getTribeName(), fetchToEmail + b.toString());
                                                 DB.getHumanCRUDHumansUnseenLocal(false).addEntry(hpe.getHumanId(), wall.getWallId());
                                             }
                                         }
                                     } else {
                                         $$(WallWidgetIds.wallNotice).setTextContent(r.returnMsg());
                                     }
                                 } else {
                                     $$(WallWidgetIds.wallNotice).setTextContent(r.returnMsg());
                                 }
                             }
                         }
                     }
                 });
 
 
         super.registerForClick($$(WallWidgetIds.wallMute),
 
                 new AIEventListener<WallWidgetTribeCriteria>(criteria) {
 
                     @Override
                     public void onFire(final Event evt_) {
                         if (DB.getHumanCRUDTribeLocal(true).readWall(criteria.getHumanId(), new VLong(criteria.getTribeId()), REFRESH_SPEC).returnValueBadly().getWallMutes().contains(criteria.getHumanId())) {
                             DB.getHumanCRUDTribeLocal(true).unmuteWall(criteria.getHumanId(), criteria.getHumanId(), new VLong(criteria.getTribeId()));
                             $$displayWallAsMuted(evt_, false);
                         } else {
                             DB.getHumanCRUDTribeLocal(true).muteWall(criteria.getHumanId(), criteria.getHumanId(), new VLong(criteria.getTribeId()));
                             $$displayWallAsMuted(evt_, true);
                         }
                     }
                 });
     }
 }
