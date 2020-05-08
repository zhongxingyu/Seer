 package ai.ilikeplaces.logic.Listeners.widgets;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.logic.validators.unit.ProfileUrl;
 import ai.ilikeplaces.servlets.Controller.Page;
 import ai.ilikeplaces.util.AbstractWidgetListener;
 import ai.ilikeplaces.util.EventType;
 import ai.ilikeplaces.util.Loggers;
 import ai.ilikeplaces.util.MarkupTag;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.event.NodePropertyTransport;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.w3c.dom.Element;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.html.HTMLDocument;
 
 /**
  * @author Ravindranath Akila
  */
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 public class Profile extends AbstractWidgetListener {
 // ------------------------------ FIELDS ------------------------------
 
     HumanId humanId = null;
     ProfileUrl url = null;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     /**
      * @param request__
      * @param appendToElement__
      * @param humanId
      */
     public Profile(final ItsNatServletRequest request__, final Element appendToElement__, final HumanId humanId) {
         super(
                 request__,
                 Page.ProfileWidget,
                 appendToElement__,
                 humanId
         );
     }
 
     @Override
     protected void init(final Object... initArgs) {
         this.humanId = (HumanId) initArgs[0];
        this.url = new ProfileUrl(null);
     }
 
     protected void registerEventListeners(final ItsNatHTMLDocument itsNatHTMLDocument_, final HTMLDocument hTMLDocument_) {
         itsNatHTMLDocument_.addEventListener((EventTarget) $$(Page.ProfileURL), EventType.BLUR.toString(), new EventListener() {
             final HumanId myhumanId = humanId;
             final ProfileUrl myurl = url;
 
             @Override
             public void handleEvent(final Event evt_) {
                 myurl.setObj($$(evt_).getAttribute(MarkupTag.TEXTAREA.value()));
                 if (myurl.validate() != 0) {
                     $$(Page.ProfileNotice).setTextContent(myurl.getViolationAsString());
                    myurl.setObj(null);//Moving this before calling getviolations will throw an exception
                 }
             }
         }, false, new NodePropertyTransport(MarkupTag.TEXTAREA.value()));
 
         itsNatHTMLDocument_.addEventListener((EventTarget) $$(Page.ProfileURLUpdate), EventType.CLICK.toString(), new EventListener() {
             final HumanId myhumanId = humanId;
             final ProfileUrl myurl = url;
 
             @Override
             public void handleEvent(final Event evt_) {
                 if (myurl.validate() == 0) {
                     Loggers.debug("myhumanId:" + myhumanId);
                     Loggers.debug("myurl.getObjectAsValid():" + myurl.getObjectAsValid());
 
                     DB.getHumanCRUDHumanLocal(true).doUHumansPublicURL(myhumanId, myurl.getObjectAsValid());
                     $$clear($$(Page.ProfileNotice));
                 } else {
                     $$(Page.ProfileNotice).setTextContent(myurl.getViolationAsString());
                    myurl.setObj(null);//Moving this before calling getviolations will throw an exception
                 }
             }
         }, false, new NodePropertyTransport(MarkupTag.TEXTAREA.value()));
     }
 }
