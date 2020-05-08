 package ai.ilikeplaces.logic.Listeners.widgets;
 
 import ai.ilikeplaces.doc.DOCUMENTATION;
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.doc.WARNING;
 import ai.ilikeplaces.entities.*;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.mail.SendMail;
 import ai.ilikeplaces.logic.validators.unit.Email;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.servlets.Controller.Page;
 import ai.ilikeplaces.util.*;
 import ai.ilikeplaces.util.jpa.RefreshSpec;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Element;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.html.HTMLDocument;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * @author Ravindranath Akila
  */
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @DOCUMENTATION(
         WARNING = @WARNING("As of 2011-02-04 the private photo widget, if appended dynamically, will thow a javascript error on client side." +
                 "This issue was not fixed despite much effort.")
 )
 public class AlbumManager extends AbstractWidgetListener {
 
 
     private static final String ALBUM__PHOTOS = "ALBUM_PHOTOS";
     private static final String BUTTONTEXT_EMAIL_FORWARDED = "buttontext.email.forwarded";
     private static final String BUTTONTEXT_CLICK_TO_CONFIRM = "buttontext.click.to.confirm";
     private static final String SLASH = "/";
     final private Logger logger = LoggerFactory.getLogger(AlbumManager.class.getName());
     private HumanId humanId = null;
     private PrivateEvent privateEvent;
     //private Return<PrivateEvent> privateEventReturn = null;
 
     final static private RefreshSpec REFRESH_SPEC_INIT = new RefreshSpec("albumPhotos");
     final static private RefreshSpec REFRESH_SPEC_REGISTER = new RefreshSpec("albumPhotos");
     private HumansIdentity humansIdentity;
     private Return<Album> albumReturn;
 
     List<HumansIdentity> wallProspects;
 
     public AlbumManager(final ItsNatServletRequest request__, final Element appendToElement__, final HumanId humanId__, final PrivateEvent privateEvent) {
         super(request__, Page.Album, appendToElement__, humanId__, privateEvent);
     }
 
     /**
      *
      */
     @Override
     protected void init(final Object... initArgs) {
         humanId = ((HumanId) initArgs[0]).getSelfAsValid();
         privateEvent = (PrivateEvent) initArgs[1];
         humansIdentity = DB.getHumanCRUDHumanLocal(true).doDirtyRHumansIdentity(humanId).returnValue();
         //privateEventReturn = DB.getHumanCrudPrivateEventLocal(true).dirtyRPrivateEventAsAny(humanId.getObj(), privateEvent.getPrivateEventId());
 
         final List<Email> emails = new ArrayList<Email>(privateEvent.getPrivateEventOwners().size() + privateEvent.getPrivateEventViewers().size() + privateEvent.getPrivateEventInvites().size());
         for (final HumansPrivateEvent humansNetPeople : privateEvent.getPrivateEventOwners()) {
             emails.add(new Email(humansNetPeople.getEmail()));
         }
         for (final HumansPrivateEvent humansNetPeople : privateEvent.getPrivateEventViewers()) {
             emails.add(new Email(humansNetPeople.getEmail()));
         }
         for (final HumansPrivateEvent humansNetPeople : privateEvent.getPrivateEventInvites()) {
             emails.add(new Email(humansNetPeople.getEmail()));
         }
 
         wallProspects = DB.getHumanCRUDHumanLocal(true).doDirtyRHumansIdentitiesByEmails(emails);
 
 
         setAlbumPhotos:
         {
             albumReturn = DB.getHumanCrudPrivateEventLocal(true).rPrivateEventReadAlbum(humanId, privateEvent.getPrivateEventId(), REFRESH_SPEC_INIT);
             if (albumReturn.returnStatus() == 0) {
                 final Album album = this.albumReturn.returnValue();
 
                 int photoSequenceNumber = 1;
 
                 for (final PrivatePhoto privatePhoto__ : album.getAlbumPhotos()) {
                     new Photo$Description(request, $$(Controller.Page.AlbumPhotos), photoSequenceNumber++, wallProspects) {
 
                         @Override
                         protected void init(final Object... initArgs) {
                             final Integer photoSequenceNumber = (Integer) initArgs[0];
                             List<HumansIdentity> mywallProspects = (List<HumansIdentity>) initArgs[1];
                             final String imageURL = RBGet.globalConfig.getString(ALBUM__PHOTOS) + privatePhoto__.getPrivatePhotoURLPath();
                             $$(Controller.Page.pd_photo_permalink).setAttribute(MarkupTag.A.href(), imageURL);
 
                             $$(Controller.Page.pd_photo).setAttribute(MarkupTag.IMG.title(), imageURL);
 
                             /* final String photoThumbURL = imageURL.substring(0, imageURL.lastIndexOf(SLASH) + 1) + CDNAlbum.THUMBNAIL + imageURL.substring(imageURL.lastIndexOf(SLASH) + 1, photoURL.length());
                            $$(Controller.Page.pd_photo).setAttribute(MarkupTag.IMG.src(), photoThumbURL);*/
 
                             $$(Controller.Page.pd_photo_sequence_number).setTextContent(photoSequenceNumber.toString());
 
                             displayNone($$(Controller.Page.pd_photo_delete));
 
                             new WallWidgetPrivatePhoto(request, $$(Controller.Page.pd_photo_wall), humanId, privatePhoto__.getPrivatePhotoId(), mywallProspects);
                         }
 
 
                         @Override
                         protected void registerEventListeners(final ItsNatHTMLDocument itsNatHTMLDocument_, final HTMLDocument hTMLDocument_) {
 
                             itsNatHTMLDocument_.addEventListener((EventTarget) $$(Controller.Page.pd_photo), EventType.ONMOUSEOVER.toString(), new EventListener() {
 
                                 boolean imageLoaded = false;
 
                                 @Override
                                 public void handleEvent(final Event evt_) {
                                     if (!imageLoaded) {
 
                                         $$(evt_).setAttribute(MarkupTag.IMG.src(), $$(evt_).getAttribute(MarkupTag.DIV.title()));
 
                                         imageLoaded = true;//safety measure 1
                                     }
                                     remove(evt_.getTarget(), EventType.ONMOUSEOVER, this); //safety measure 2
                                 }
 
                                 @Override
                                 public void finalize() throws Throwable {
                                     Loggers.finalized(this.getClass().getName());
                                     super.finalize();
                                 }
                             }, false);
                         }
                     };
                 }
             } else {
                 $$(Controller.Page.AlbumNotice).setTextContent(albumReturn.returnMsg());
             }
         }
 
 
         setAlbumOwnerMode:
         {
             final Return<Boolean> ro = DB.getHumanCrudPrivateEventLocal(true).dirtyRPrivateEventIsOwner(humanId, privateEvent.getPrivateEventId());
             if (ro.returnStatus() == 0) {
                 if (ro.returnValue()) {//owner
                    $$(Controller.Page.AlbumPivateEventId).setAttribute(MarkupTag.INPUT.value(), privateEvent.toString());
                     displayBlock($$(Controller.Page.AlbumOwner));
                     //show hide operations
                 }
             } else {
                 $$(Controller.Page.AlbumNotice).setTextContent(ro.returnMsg());
             }
         }
 
 
     }
 
     @Override
     protected void registerEventListeners(final ItsNatHTMLDocument itsNatHTMLDocument__, final HTMLDocument hTMLDocument__) {
 
         itsNatHTMLDocument__.addEventListener((EventTarget) $$(Controller.Page.AlbumForward), EventType.CLICK.toString(), new EventListener() {
 
             final HumanId myhumanId = humanId;
             final Long myprivateEventId = privateEvent.getPrivateEventId();
             boolean confirmed = false;
             final HumansIdentity myhumansIdentity = humansIdentity;
             //final Return<PrivateEvent> myprivateEventReturn = privateEventReturn;
             final PrivateEvent myprivateEvent = privateEvent;
             final Return<Album> myalbumReturn = albumReturn;
 
             @Override
             public void handleEvent(final Event evt_) {
                 if (confirmed) {
 //                    if (myprivateEventReturn.returnStatus() == 0) {
                     final PrivateEvent pe = myprivateEvent;// myprivateEventReturn.returnValue();
                     final Album album = myalbumReturn.returnValue();
                     Collections.sort(album.getAlbumPhotos());
 
                     final StringBuilder b = new StringBuilder("");
                     for (final PrivatePhoto pp : album.getAlbumPhotos()) {
                         b.append(
                                 UserProperty.fetchToEmailStatically(
                                         myhumansIdentity.getHuman().getDisplayName(),
                                         UserProperty.formatProfileUrl(myhumansIdentity.getUrl().getUrl(), true),
                                         UserProperty.formatProfilePhotoUrl(myhumansIdentity.getHumansIdentityProfilePhoto()),
                                         ElementComposer.compose($$(MarkupTag.IMG)).$ElementSetAttribute(MarkupTag.IMG.src(), RBGet.globalConfig.getString(ALBUM__PHOTOS) + pp.getPrivatePhotoURLPath()).get()
                                 )
                         );
                     }
 
 
                     for (final HumansPrivateEvent member : pe.getPrivateEventInvites()) {
                         SendMail.getSendMailLocal().sendAsHTMLAsynchronously(member.getHumanId(), pe.getPrivateEventName(), b.toString());
                     }
                     for (final HumansPrivateEvent member : pe.getPrivateEventInvites()) {
                         SendMail.getSendMailLocal().sendAsHTMLAsynchronously(member.getHumanId(), pe.getPrivateEventName(), b.toString());
                     }
                     for (final HumansPrivateEvent member : pe.getPrivateEventInvites()) {
                         SendMail.getSendMailLocal().sendAsHTMLAsynchronously(member.getHumanId(), pe.getPrivateEventName(), b.toString());
                     }
 
 //                    } else {
 //                        $$(Controller.Page.AlbumNotice).setTextContent(myprivateEventReturn.returnMsg());
 //                    }
 
                     remove(evt_.getTarget(), EventType.CLICK, this, false);
                     confirmed = false;
                     $$(evt_).setTextContent(RBGet.gui().getString(BUTTONTEXT_EMAIL_FORWARDED));
                 } else {
                     confirmed = true;
                     $$(evt_).setTextContent(RBGet.gui().getString(BUTTONTEXT_CLICK_TO_CONFIRM));
                 }
             }
 
             @Override
             public void finalize() throws Throwable {
                 Loggers.finalized(this.getClass().getName());
                 super.finalize();
             }
         }, false);
     }
 
     @Override
     public void finalize() throws Throwable {
         Loggers.finalized(this.getClass().getName());
         super.finalize();
     }
 }
