 package ai.ilikeplaces.logic.Listeners.widgets.privateevent;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.doc.OK;
 import ai.ilikeplaces.doc.WARNING;
 import ai.ilikeplaces.entities.HumansFriend;
 import ai.ilikeplaces.entities.HumansNetPeople;
 import ai.ilikeplaces.entities.PrivateEvent;
 import ai.ilikeplaces.logic.Listeners.JSCodeToSend;
 import ai.ilikeplaces.logic.Listeners.widgets.AlbumManager;
 import ai.ilikeplaces.logic.Listeners.widgets.Button;
 import ai.ilikeplaces.logic.Listeners.widgets.MemberHandler;
 import ai.ilikeplaces.logic.Listeners.widgets.WallWidgetPrivateEvent;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.mail.SendMail;
 import ai.ilikeplaces.logic.validators.unit.GeoCoord;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.servlets.Controller.Page;
 import ai.ilikeplaces.util.*;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Element;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.html.HTMLDocument;
 
 import java.util.List;
 
 import static ai.ilikeplaces.servlets.Controller.Page.*;
 
 
 /**
  * @author Ravindranath Akila
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @OK
 abstract public class PrivateEventDelete extends AbstractWidgetListener {
 
 
     final private Logger logger = LoggerFactory.getLogger(PrivateEventDelete.class.getName());
 
     private HumanId humanId = null;
 
     private Long privateEventId = null;
 
     @WARNING("Underlying widgets expect a fully refreshed PrivateEvent with respect to owners, visitors and viewers")
     Return<PrivateEvent> privateEventReturn;
 
     List<HumansNetPeople> possibilities;
 
     private static final String HAS_ADDED_YOU_AS_AN_OWNER_OF = " has added you as an Owner of ";
     private static final String HAS_REMOVED_YOU_AS_AN_OWNER_OF = " has removed you as an Owner of ";
     private static final String HAS_ADDED_YOU_AS_A_VISITOR_OF = " has added you as a Visitor of ";
     private static final String HAS_REMOVED_YOU_AS_A_VISITOR_OF = " has removed you as a Visitor of ";
     private static final String HAS_INVITED_YOU_TO = " has invited you to ";
     private static final String HAS_CANCELLED_YOUR_INVITATION_TO = " has cancelled your invitation to ";
     private static final String ARROW_RIGHT_GIF = "arrow-right.gif";
 
     /**
      * @param request__
      * @param appendToElement__
      * @param humanId__
      * @param privateEventId__
      * @param detailedMode__
      */
     public PrivateEventDelete(final ItsNatServletRequest request__, final Element appendToElement__, final String humanId__, final long privateEventId__, final boolean detailedMode__) {
         super(request__, Page.PrivateEventDelete, appendToElement__, humanId__, privateEventId__, detailedMode__);
     }
 
     /**
      *
      */
     @Override
     protected void init(final Object... initArgs) {
         this.humanId = new HumanId((String) initArgs[0]);
         this.privateEventId = (Long) initArgs[1];
 
         privateEventReturn = DB.getHumanCrudPrivateEventLocal(true).dirtyRPrivateEventAsAny(humanId.getObj(), privateEventId);
         if (privateEventReturn.returnStatus() == 0) {
             $$(privateEventDeleteName).setTextContent(privateEventReturn.returnValue().getPrivateEventName());
             $$(privateEventDeleteInfo).setTextContent(privateEventReturn.returnValue().getPrivateEventInfo());
            new Button(request, $$(privateEventDeleteLink), "Link to " + privateEventReturn.returnValue().getPrivateEventName(), false, privateEventReturn.returnValue()) {
                 PrivateEvent privateEvent = null;
 
                 @Override
                 protected void init(final Object... initArgs) {
                     privateEvent = (PrivateEvent) (((Object[]) initArgs[2])[0]);
                     SetLocationLink:
                     {
                         setLink:
                         {
                             $$(GenericButtonLink).setAttribute(MarkupTag.A.href(),
                                     new Parameter(Organize.getURL())
                                             .append(DocOrganizeCategory, DocOrganizeModeEvent, true)
                                             .append(DocOrganizeLocation, privateEventReturn.returnValue().getPrivateLocation().getPrivateLocationId())
                                             .append(DocOrganizeEvent, privateEvent.getPrivateEventId())
                                             .get()
                             );
                         }
                         setImage:
                         {
                             $$(GenericButtonImage).setAttribute(MarkupTag.IMG.src(), RBGet.globalConfig.getString(RBGet.url_CDN_STATIC) + ARROW_RIGHT_GIF);
                         }
                     }
                 }
             };
             if ((Boolean) initArgs[2]) {
                 new WallWidgetPrivateEvent(request, $$(Page.privateEventDeleteWall), humanId, privateEventReturn.returnValue().getPrivateEventId());
                 new AlbumManager(request, $$(Page.privateEventDeleteAlbum), humanId, privateEventReturn.returnValue());
 
                 final GeoCoord gc = new GeoCoord();
                 gc.setObj(privateEventReturn.returnValue().getPrivateLocation().getPrivateLocationLatitude() + "," + privateEventReturn.returnValue().getPrivateLocation().getPrivateLocationLongitude());
                 gc.validateThrow();
 
                 $$(privateEventDeleteLocationMap).setAttribute(MarkupTag.IMG.src(),
                         new Parameter("http://maps.google.com/maps/api/staticmap")
                                 .append("sensor", "false", true)
                                 .append("center", gc.toString())
                                 .append("size", "230x250")
                                 .append("format", "jpg")
                                 .append("markers", "color:0x7fe2ff|label:S|path=fillcolor:0xAA000033|color:0xFFFFFF00|"
                                         + gc.toString())
                                 .get());
             }
 
         } else {
             $$(privateEventDeleteNotice).setTextContent(privateEventReturn.returnMsg());
         }
 
     }//init
 
     @Override
     protected void registerEventListeners(final ItsNatHTMLDocument itsNatHTMLDocument__, final HTMLDocument hTMLDocument__) {
         delete:
         {
             itsNatHTMLDocument__.addEventListener((EventTarget) $$(privateEventDelete), EventType.CLICK.toString(), new EventListener() {
 
                 final HumanId myhumanId = humanId;
                 final Long myprivateEventId = privateEventId;
                 final Obj<Boolean> alertedUserWithConfirm = (Obj<Boolean>) new Obj<Boolean>().setObjAsValid(false);
 
                 @Override
                 public void handleEvent(final Event evt_) {
                     if (!alertedUserWithConfirm.getObj()) {
                         Loggers.USER.info(myhumanId.getObj() + " clicked delete for private event " + myprivateEventId);
                         alertedUserWithConfirm.setObjAsValid(true);
                         $$(evt_).setAttribute(MarkupTag.IMG.src(),
                                 $$(evt_).getAttribute(MarkupTag.IMG.src()).replace("delete.png", "confirm.png")
                         );
                         //$$(evt_).setTextContent("Confirm Delete!");
                     } else {
                         Loggers.USER.info(myhumanId.getObj() + " clicked delete after confirmation for private event " + myprivateEventId);
                         final Return<Boolean> r = DB.getHumanCrudPrivateEventLocal(true).dPrivateEvent(myhumanId, myprivateEventId);
                         if (r.returnStatus() == 0) {
                            Loggers.USER.info(humanId.getObj() + " clicked deleted private event " + r.returnValue());
                            remove(evt_.getTarget(), EventType.CLICK, this);
                             $$sendJS(
                                     JSCodeToSend.redirectPageWithURL("/page/_org")
                             );
                            clear($$(privateEventDeleteNotice));
                         } else {
                            $$(privateEventDelete).setTextContent(r.returnMsg());
                         }
                     }
 
 
                 }
 
             }, false);
         }
 
         final HumansNetPeople user = DB.getHumanCRUDHumanLocal(true).doDirtyRHumansNetPeople(humanId);
         this.possibilities = user.getHumansNetPeoples();
 
         AddRemoveOwners:
         {
             new MemberHandler<HumansFriend, List<HumansFriend>, Return<PrivateEvent>>(
                     request, $$(privateEventDeleteOwners),
                     user.getHumansNet(),
                     possibilities,
                     privateEventReturn.returnValue().getPrivateEventOwners(),
                     new Save<Return<PrivateEvent>>() {
 
                         final long myprivateEventId = privateEventReturn.returnValue().getPrivateEventId();
 
                         @Override
                         public Return<PrivateEvent> save(final HumanId humanId, final HumansFriend humansFriend) {
 
 
                             final Return<PrivateEvent> returnVal = DB.getHumanCrudPrivateEventLocal(true).uPrivateEventAddOwnerWithPrivateLocationCheck(humanId, myprivateEventId, humansFriend, privateEventReturn.returnValue().getPrivateLocation().getPrivateLocationId());
                             if (returnVal.returnStatus() == 0) {
                                 SendMail.getSendMailLocal().sendAsSimpleTextAsynchronously(humansFriend.getHumanId(),
                                         humanId.getObj(),
                                         humanId.getObj() + HAS_ADDED_YOU_AS_AN_OWNER_OF + returnVal.returnValue().getPrivateEventName());
                             }
                             return returnVal;
 
                         }
                     },
                     new Save<Return<PrivateEvent>>() {
 
                         final long myprivateEventId = privateEventReturn.returnValue().getPrivateEventId();
 
                         @Override
                         public Return<PrivateEvent> save(final HumanId humanId, final HumansFriend humansFriend) {
 
 
                             final Return<PrivateEvent> returnVal = DB.getHumanCrudPrivateEventLocal(true).uPrivateEventRemoveOwner(humanId, myprivateEventId, humansFriend);
                             if (returnVal.returnStatus() == 0) {
                                 SendMail.getSendMailLocal().sendAsSimpleTextAsynchronously(humansFriend.getHumanId(),
                                         humanId.getObj(),
                                         humanId.getObj() + HAS_REMOVED_YOU_AS_AN_OWNER_OF + returnVal.returnValue().getPrivateEventName());
                             }
                             return returnVal;
 
                         }
                     }
             ) {
             };
         }
         AddRemoveVisitors:
         {
             new MemberHandler<HumansFriend, List<HumansFriend>, Return<PrivateEvent>>(
                     request, $$(privateEventDeleteVisitors),
                     user.getHumansNet(),
                     possibilities,
                     privateEventReturn.returnValue().getPrivateEventViewers(),
                     new Save<Return<PrivateEvent>>() {
 
                         final long myprivateEventId = privateEventReturn.returnValue().getPrivateEventId();
 
                         @Override
                         public Return<PrivateEvent> save(final HumanId humanId, final HumansFriend humansFriend) {
 
                             final Return<PrivateEvent> returnVal = DB.getHumanCrudPrivateEventLocal(true).uPrivateEventAddVisitorWithPrivateLocationCheck(humanId, myprivateEventId, humansFriend, privateEventReturn.returnValue().getPrivateLocation().getPrivateLocationId());
                             if (returnVal.returnStatus() == 0) {
                                 SendMail.getSendMailLocal().sendAsSimpleTextAsynchronously(humansFriend.getHumanId(),
                                         humanId.getObj(),
                                         humanId.getObj() + HAS_ADDED_YOU_AS_A_VISITOR_OF + returnVal.returnValue().getPrivateEventName());
                             }
                             return returnVal;
 
                         }
                     },
                     new Save<Return<PrivateEvent>>() {
 
                         final long myprivateEventId = privateEventReturn.returnValue().getPrivateEventId();
 
                         @Override
                         public Return<PrivateEvent> save(final HumanId humanId, final HumansFriend humansFriend) {
                             final Return<PrivateEvent> returnVal = DB.getHumanCrudPrivateEventLocal(true).uPrivateEventRemoveVisitor(humanId, myprivateEventId, humansFriend);
                             if (returnVal.returnStatus() == 0) {
                                 SendMail.getSendMailLocal().sendAsSimpleTextAsynchronously(humansFriend.getHumanId(),
                                         humanId.getObj(),
                                         humanId.getObj() + HAS_REMOVED_YOU_AS_A_VISITOR_OF + returnVal.returnValue().getPrivateEventName());
                             }
                             return returnVal;
 
                         }
                     }
             ) {
             };
             AddRemoveInvitee:
             {
                 new MemberHandler<HumansFriend, List<HumansFriend>, Return<PrivateEvent>>(
                         request, $$(privateEventDeleteInvitees),
                         user.getHumansNet(),
                         possibilities,
                         privateEventReturn.returnValue().getPrivateEventInvites(),
                         new Save<Return<PrivateEvent>>() {
 
                             final long myprivateEventId = privateEventReturn.returnValue().getPrivateEventId();
 
                             @Override
                             public Return<PrivateEvent> save(final HumanId humanId, final HumansFriend humansFriend) {
 
                                 final Return<PrivateEvent> returnVal = DB.getHumanCrudPrivateEventLocal(true).uPrivateEventAddInviteWithPrivateLocationCheck(humanId, myprivateEventId, humansFriend, privateEventReturn.returnValue().getPrivateLocation().getPrivateLocationId());
                                 if (returnVal.returnStatus() == 0) {
                                     SendMail.getSendMailLocal().sendAsSimpleTextAsynchronously(humansFriend.getHumanId(),
                                             humanId.getObj(),
                                             humanId.getObj() + HAS_INVITED_YOU_TO + returnVal.returnValue().getPrivateEventName());
                                 }
                                 return returnVal;
 
 
                             }
                         },
                         new Save<Return<PrivateEvent>>() {
 
                             final long myprivateEventId = privateEventReturn.returnValue().getPrivateEventId();
 
                             @Override
                             public Return<PrivateEvent> save(final HumanId humanId, final HumansFriend humansFriend) {
                                 final Return<PrivateEvent> returnVal = DB.getHumanCrudPrivateEventLocal(true).uPrivateEventRemoveInvite(humanId, myprivateEventId, humansFriend);
                                 if (returnVal.returnStatus() == 0) {
                                     SendMail.getSendMailLocal().sendAsSimpleTextAsynchronously(humansFriend.getHumanId(),
                                             humanId.getObj(),
                                             humanId.getObj() + HAS_CANCELLED_YOUR_INVITATION_TO + returnVal.returnValue().getPrivateEventName());
                                 }
                                 return returnVal;
                             }
                         }
                 );
             }
         }
     }
 }
