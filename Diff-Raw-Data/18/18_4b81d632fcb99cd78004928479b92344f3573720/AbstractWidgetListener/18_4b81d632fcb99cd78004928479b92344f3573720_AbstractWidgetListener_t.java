 package ai.ilikeplaces.util;
 
 import ai.ilikeplaces.doc.*;
 import ai.ilikeplaces.logic.Listeners.JSCodeToSend;
 import ai.ilikeplaces.logic.Listeners.widgets.WallWidgetHumansWall;
 import ai.ilikeplaces.logic.role.HumanUser;
 import ai.ilikeplaces.logic.role.HumanUserLocal;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.servlets.ServletLogin;
 import org.itsnat.core.ItsNatDocument;
 import org.itsnat.core.ItsNatServlet;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.event.NodePropertyTransport;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.itsnat.core.tmpl.ItsNatHTMLDocFragmentTemplate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.css.ElementCSSInlineStyle;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.html.HTMLDocument;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import static ai.ilikeplaces.servlets.Controller.Page;
 
 /**
  * @author Ravindranath Akila
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @DOCUMENTATION(
         LOGIC = @LOGIC({
                 @NOTE("Each widget itself knows its functionality." +
                         "Hence, each widget should register its own event listeners. " +
                         "But there will be several copies of the same widget in the same page. " +
                         "Therefore, the widget ids should be different. " +
                         "Each widget has to have its own class. "
                 ),
                 @NOTE("Each widget has it's own vaiable names." +
                         "However, since there can be multiple copies of the same widget in a page, a static long values is appended to the element ids." +
                         "This makes the element IDs unique within a page."
                 )}),
 
         FIXME = @FIXME(
                 issues = {"Nested widgets have issues registering themselves upon DOM events.",
                         "ItsNat html document fragment templates appear to be needed to only loaded once and then reused."}),
 
         WARNING = @WARNING("If you want your variables initialized by the time you reach registereventlisteners, " +
                 "do the assignment in init. " +
                 "NOT the implemented subclass constructer" +
                 "which runs as super, init, registereventlisteners and THEN the remainder of the constructer.")
 )
 public abstract class AbstractWidgetListener<T> {
 
     private static final String DISPLAY_BLOCK_ = "display:block";
     private static final String DISPLAY_NONE_ = "display:none";
     private static final String DISPLAY_BLOCK_WITH_SPACE = "display: block";
     private static final String DISPLAY_NONE_WITH_SPACE = "display: none";
     private static final String DISPLAY = "display";
     private static final String BLOCK = "block";
     private static final String NONE = "none";
     private static final String EMPTY = "";
     private static final String THE_ELEMENT_GIVEN_T0_REGISTER_FOR_NOTIFICATIONS_IS_NULL = "THE ELEMENT GIVEN T0 REGISTER FOR NOTIFICATIONS IS NULL!";
     private static final String NOTIFIER_ELEMENT_HAS_NOT_BEEN_INITIALIZED = "NOTIFIER ELEMENT HAS NOT BEEN INITIALIZED!";
     public static final String STR_FATAL_ERROR_IN_WIDGET_THIS_SHOULD_NOT_HAPPEN_DETAILS_AS_FOLLOWS = "FATAL ERROR IN WIDGET. THIS SHOULD NOT HAPPEN DETAILS AS FOLLOWS:";
     public static final String STR_PAGE = "\npage:";
     public static final String STR_APPEND_TO_ELEMENT = "\nappendToElement: ";
     public static final String STR_HUMAN_ID = "\nhumanId: ";
     private static final String STR_CSS_CLASS = "class";
     private static final String STR_SPACE = " ";
     protected final ItsNatDocument itsNatDocument_;
     private final HTMLDocument hTMLDocument_;
     private final ItsNatHTMLDocument itsNatHTMLDocument_;
     private final Document document_;
 
     /**
      * This will be null if generics are not used!
      */
     protected final T criteria;
 
     /**
      * As a widget may have many instances within a document, we register each
      * instance with unique ids. i.e. the existing id is appended with the
      * instance number. Remember, each instance has to have its own instance id,
      * and should not have a reference to THIS copy which will lead to bugs.
      */
     private static long InstanceCounter_ = 0;
     /**
      * As a widget may have many instances within a document, we register each
      * instance with unique ids. i.e. the existing id is appended with the
      * instance number
      */
     final protected long instanceId;
     final protected Page page;
     final private static String Id = "id";
     private boolean visible = true;
 
     /**
      * Per request refresh needs to be verified.
      */
     final protected ItsNatServletRequest request;
 
     private Element notifier = null;
 
 
     final static private String NPE_1 = "SORRY! THIS DOCUMENT APPEARS NOT TO HAVE BEEN REGISTERED. PLEASE VERIFY THIS FIRST.(CAUSE OF NPE COULD BE DIFFERENT)";
     private static final String SORRY_I_FIND_THAT_ELEMENT = "SORRY! I FIND THAT ELEMENT \"";
     private static final String CONTAINS_NULL_OR_NO_REFERENCE_IN_REGISTRY = "\" CONTAINS NULL OR NO REFERENCE IN REGISTRY!";
     private static final String SORRY_COULD_NOT_FETCH_THE_ELEMENT_NAMED = "SORRY! COULD NOT FETCH THE ELEMENT NAMED ";
     private static final String FROM_FILE = " FROM FILE.";
     private static final String THIS_COULD_HAPPEN_IF_THE_WRONG_PAGE_IS_REGISTERED_IN_THE_WIDGET_CONSTRUCTOR_WHEN_CALLING_SUPER = "\nTHIS COULD HAPPEN IF THE WRONG PAGE IS REGISTERED IN THE WIDGET CONSTRUCTOR WHEN CALLING SUPER OR IF THE ELEMENT WAS REGISTERED UNDER A WRONG ENTITY.";
     private static final String STYLE = "style";
     private static final String DISPLAY_BLOCK = "display:block;";
     private static final String DISPLAY_NONE = "display:none;";
     /**
      * Check for null before use. Null if not initialized
      */
     public String fetchToEmail = "";
 
 
     /**
      * Creating a constructor for implementing classes with variable arguments is strongly frowned upon.
      * The reason is that when widgets gets nested, things get quite hasty.
      * Instead, have a constructor for each type created.
      * Use varargs to transport just one object which transports the data.
      * Think of this constructors var args as a clean slate to do data transport extremely fast and memory efficiently.
      * The implementation of it is upto the extender.
      * <p/>
      * En example of a proper extend would be
      * <br/>
      * <br/>
      * <code>
      * &nbsp; &nbsp; public class CustomAbstractWidgetListener extends AbstractWidgetListener{<br/>
      * &nbsp; &nbsp; &nbsp; &nbsp; public CustomAbstractWidgetListener(final ItsNatServletRequest request__, final Element appendToElement__, final Bean dataTransferBean){<br/>
      * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; this.super(request__, Controller.Page.mypage, dataTransferBean);<br/>
      * &nbsp; &nbsp; &nbsp; &nbsp; }<br/>
      * &nbsp; &nbsp; }<br/>
      * </code>
      *
      * @param request__
      * @param page__
      * @param appendToElement__
      * @param initArgs
      */
     @WARNING(warning = "If you want your variables initialized by the time you reach registereventlisteners, do the asignmen in init, NOT the implemented subclass constructer" +
             "which runs as super, init, registereventlisteners and THEN the remainder of the constructer.")
     @Deprecated
     public AbstractWidgetListener(final ItsNatServletRequest request__, final Page page__, final Element appendToElement__, final Object... initArgs) {
         request = request__;
         instanceId = InstanceCounter_++;
         page = page__;
         criteria = null;
         this.itsNatDocument_ = request__.getItsNatDocument();

        itsNatDocument_.addEventListener(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                /*itsNatDocument_.addCodeToSend("\n" + "alert("
                        + "'"
                        + itsNatDocument_.getItsNatDocumentTemplate().getName()
                        + ","
                        + ((Element) evt.getCurrentTarget()).getAttribute(MarkupTag.GENERIC.id()).replaceAll("\\d*$", "")
                        + ","
                        + evt.getType()
                        + "'"
                        + ");" + "\n");*/

                itsNatDocument_.addCodeToSend("_gaq.push(['_trackEvent', '" + page.name() + "', '" + evt.getType() + "', '" + ((Element) evt.getCurrentTarget()).getAttribute(MarkupTag.GENERIC.id()).replaceAll("\\d*$", "") + "']);");
            }
        });

         this.itsNatHTMLDocument_ = (ItsNatHTMLDocument) itsNatDocument_;
         this.hTMLDocument_ = itsNatHTMLDocument_.getHTMLDocument();
         this.document_ = itsNatDocument_.getDocument();
         final ItsNatServlet itsNatServlet_ = itsNatDocument_.getItsNatDocumentTemplate().getItsNatServlet();
         final ItsNatHTMLDocFragmentTemplate inhdft_ = (ItsNatHTMLDocFragmentTemplate) itsNatServlet_.getItsNatDocFragmentTemplate(page__.toString());
 
 //        //@todo needs to be removed.
 //        try {
 //            if (!inhdft_.isOnLoadCacheStaticNodes()) {
 //                inhdft_.setOnLoadCacheStaticNodes(false);
 //                Loggers.DEBUG.debug("Set static for fragment to false");
 //            }
 //        } catch (final Exception e) {
 //            Loggers.EXCEPTION.error("Possible is setting static mode for fragment.");
 //        }
 
         LogNull.logThrow(inhdft_, NPE_1);//Do not remove unless performance degrade is evident.
         appendToElement__.appendChild(inhdft_.loadDocumentFragmentBody(itsNatDocument_));
 
         setWidgetElementIds(Controller.GlobalPageIdRegistry.get(page));
 
         try {
             init(initArgs);
             registerEventListeners(itsNatHTMLDocument_, hTMLDocument_);
         } catch (final Throwable throwable) {//allowing the website to proceed with the remaining portions of the UI
             Loggers.ERROR.error(STR_FATAL_ERROR_IN_WIDGET_THIS_SHOULD_NOT_HAPPEN_DETAILS_AS_FOLLOWS +
                     STR_PAGE + page__ +
                     STR_APPEND_TO_ELEMENT + (appendToElement__ != null ? appendToElement__.getAttribute(MarkupTag.GENERIC.id()) : null) +
                     STR_HUMAN_ID + getHumanIdFromRequest(request__), throwable);
         }
     }
 
     public static String getHumanIdFromRequest(final ItsNatServletRequest request_) {
         final Object attribute__ = request_.getItsNatSession().getAttribute(ServletLogin.HumanUser);
         SessionBoundBadRefWrapper<HumanUserLocal> sessionBoundBadRefWrapper =
                 attribute__ == null ?
                         null : (!((SessionBoundBadRefWrapper<HumanUserLocal>) attribute__).isAlive() ?
                         null : ((SessionBoundBadRefWrapper<HumanUserLocal>) attribute__));
 
         return sessionBoundBadRefWrapper != null ?
                 (sessionBoundBadRefWrapper.isAlive() ?
                         sessionBoundBadRefWrapper.boundInstance.getHumanUserId() : null) : null;
 
     }
 
     public static HumanUserLocal getHumanUserFromRequest(final ItsNatServletRequest request_) {
         final SessionBoundBadRefWrapper<HumanUserLocal> sessionBoundBadRefWrapper = (SessionBoundBadRefWrapper<HumanUserLocal>) request_.getItsNatSession().getAttribute(ServletLogin.HumanUser);
         return HumanUser.getHumanUserAsValid(sessionBoundBadRefWrapper);
 
     }
 
     /**
      * @param request__
      * @param page__
      * @param t
      * @param appendToElement__
      */
     public AbstractWidgetListener(final ItsNatServletRequest request__, final Page page__, final T t, final Element appendToElement__) {
         request = request__;
         instanceId = InstanceCounter_++;
         page = page__;
         criteria = t;
         this.itsNatDocument_ = request__.getItsNatDocument();
         this.itsNatHTMLDocument_ = (ItsNatHTMLDocument) itsNatDocument_;
         this.hTMLDocument_ = itsNatHTMLDocument_.getHTMLDocument();
         this.document_ = itsNatDocument_.getDocument();
         final ItsNatServlet itsNatServlet_ = itsNatDocument_.getItsNatDocumentTemplate().getItsNatServlet();
         final ItsNatHTMLDocFragmentTemplate inhdft_ = (ItsNatHTMLDocFragmentTemplate) itsNatServlet_.getItsNatDocFragmentTemplate(page__.toString());
 
 //        //@todo needs to be removed.
 //        try {
 //            if (!inhdft_.isOnLoadCacheStaticNodes()) {
 //                inhdft_.setOnLoadCacheStaticNodes(false);
 //                Loggers.DEBUG.debug("Set static for fragment to false");
 //            }
 //        } catch (final Exception e) {
 //            Loggers.EXCEPTION.error("Possible is setting static mode for fragment.");
 //        }
 
         LogNull.logThrow(inhdft_, NPE_1);//Do not remove unless performance degrade is evident.
         appendToElement__.appendChild(inhdft_.loadDocumentFragmentBody(itsNatDocument_));
 
         setWidgetElementIds(Controller.GlobalPageIdRegistry.get(page));
 
         try {
             init(t);
             init(new Object[]{t});//In case the invoker overrides. Reflection mandates getMethod methods should be public, hence we cant detect an override at runtime :'( boohooohoo
             registerEventListeners(itsNatHTMLDocument_, hTMLDocument_);
         } catch (final Throwable throwable) {//allowing the website to proceed with the remaining portions of the UI
             Loggers.ERROR.error(STR_FATAL_ERROR_IN_WIDGET_THIS_SHOULD_NOT_HAPPEN_DETAILS_AS_FOLLOWS +
                     STR_PAGE + page__ +
                     STR_APPEND_TO_ELEMENT + appendToElement__ != null ? appendToElement__.getAttribute(MarkupTag.GENERIC.id()) : null +
                     STR_HUMAN_ID + getHumanIdFromRequest(request__), throwable);
         }
     }
 
     /**
      * Use this only in conjunction with {@link #AbstractWidgetListener(org.itsnat.core.ItsNatServletRequest, ai.ilikeplaces.servlets.Controller.Page, org.w3c.dom.Element, Object...)}
      * NON GENERIC constructor
      *
      * @param initArgs
      */
     @WARNING(
             "Use this only in conjunction with {@link #AbstractWidgetListener(org.itsnat.core.ItsNatServletRequest, ai.ilikeplaces.servlets.Controller.Page, org.w3c.dom.Element, Object...)} " +
                     "NON GENERIC constructor"
     )
     protected void init(final Object... initArgs) {
     }
 
     /**
      * Use this only in conjunction with {@link #AbstractWidgetListener(org.itsnat.core.ItsNatServletRequest, ai.ilikeplaces.servlets.Controller.Page, Object, org.w3c.dom.Element)}
      * GENERIC constructor.
      *
      * @param t
      */
     @WARNING(
             "Use this only in conjunction with {@link #AbstractWidgetListener(org.itsnat.core.ItsNatServletRequest, ai.ilikeplaces.servlets.Controller.Page, Object, org.w3c.dom.Element)} " +
                     "GENERIC constructor"
     )
     protected void init(final T t) {
     }
 
     /**
      * Use ItsNatHTMLDocument variable stored in the AbstractListener class
      * Do not call this method anywhere, just implement it, as it will be
      * automatically called by the constructor
      *
      * @param itsNatHTMLDocument_
      * @param hTMLDocument_
      */
     protected abstract void registerEventListeners(final ItsNatHTMLDocument itsNatHTMLDocument_, final HTMLDocument hTMLDocument_);
 
 
     /**
      * To be called only during initialization. If otherwise, not tested for functionality!
      *
      * @param element       registered for listening
      * @param eventListener called when text changes
      */
     protected void registerForInputText(final Element element, final EventListener eventListener) {
 
         itsNatHTMLDocument_.addEventListener((EventTarget) element, EventType.CHANGE.toString(), eventListener, false, new NodePropertyTransport(MarkupTag.TEXTAREA.value()));
     }
 
     /**
      * To be called only during initialization. If otherwise, not tested for functionality!
      *
      * @param element       registered for listening
      * @param eventListener called when text changes
      */
     protected void registerForInputText(final WidgetIds element, final EventListener eventListener) {
         registerForInputText($$(element), eventListener);
     }
 
     /**
      * To be called only during initialization. If otherwise, not tested for functionality!
      * DOES NOT SUPPORT TEXT INPUTS. USE {@link #registerForInputText(org.w3c.dom.Element, org.w3c.dom.events.EventListener)} INSTEAD
      *
      * @param element       registered for listening
      * @param eventListener called when text changes
      */
     protected void registerForEvent(final Element element, final EventListener eventListener, final EventType eventType) {
 
         itsNatHTMLDocument_.addEventListener((EventTarget) element, eventType.toString(), eventListener, false);
     }
 
     /**
      * To be called only during initialization. If otherwise, not tested for functionality!
      * DOES NOT SUPPORT TEXT INPUTS. USE {@link #registerForInputText(org.w3c.dom.Element, org.w3c.dom.events.EventListener)} INSTEAD
      *
      * @param element       registered for listening
      * @param eventListener called when text changes
      */
     protected void registerForEvent(final WidgetIds element, final EventListener eventListener, final EventType eventType) {
         registerForEvent($$(element), eventListener, eventType);
     }
 
     /**
      * To be called only during initialization. If otherwise, not tested for functionality!
      *
      * @param element       registered for listening
      * @param eventListener called when text changes
      */
     protected void registerForClick(final Element element, final EventListener eventListener) {
 
         itsNatHTMLDocument_.addEventListener((EventTarget) element, EventType.CLICK.toString(), eventListener, false);
     }
 
     /**
      * To be called only during initialization. If otherwise, not tested for functionality!
      *
      * @param element       registered for listening
      * @param eventListener called when text changes
      */
     protected void registerForClick(final WidgetIds element, final EventListener eventListener) {
         registerForClick($$(element), eventListener);
     }
 
     /**
      * @param evt_ fired from client
      * @return text of given events' target
      */
     protected String getTargetInputText(final Event evt_) {
         return ((Element) evt_.getCurrentTarget()).getAttribute(MarkupTag.TEXTAREA.value());
     }
 
     protected void fetchToEmail(final Object... args) {
         throw ExceptionCache.METHOD_NOT_IMPLEMENTED;
     }
 
     /**
      * Id registry should be globally visible to callers
      *
      * @param key__
      * @return Element
      */
     @WARNING(warning = "You cannot actually use this method as it will definitely throw a NPE. Check if fetching a main xhtml template element works. Should." +
             "Nevertheless, having this is bug friendly as $ instead of $$ is an easy miss for widgets.")
     @Deprecated
     protected final Element $(final String key__) {
         final String elementId__ = Controller.GlobalHTMLIdRegistry.get(key__);
         if (elementId__ == null) {
             throw new java.lang.NullPointerException("SORRY! I FIND THAT ELEMENT \"" + key__ + "\" CONTAINS NULL OR NO REFERENCE IN REGISTRY!");
         }
         final Element returnVal = document_.getElementById(elementId__);
         return returnVal != null ? returnVal : (Element) LogNull.logThrow("SORRY! CANNOT FETCH THE ELEMENT " + key__ + ". YOU PROBABLY WERE SUPPOSED TO USE $$ INSTEAD OF $");
     }
 
     /**
      * Id list of this widget
      *
      * @param ids__
      */
     private final void setWidgetElementIds(final HashSet<String> ids__) {
         for (final String id_ : ids__) {
             document_.getElementById(id_).setAttribute(Id, id_ + instanceId);
         }
     }
 
     /**
      * Wrapper to getWidgetElementById
      *
      * @param key__
      * @return Element
      */
     final public Element $$(final String key__) {
         return getWidgetElementById(key__);
     }
 
     /**
      * Wrapper to getWidgetElementById
      *
      * @param key__
      * @return Element
      */
     final public Element $$(final WidgetIds key__) {
         return getWidgetElementById(key__.toString());
     }
 
     /**
      * Fetches the element disregarding widget behavior. i.e. ignores dynamic nature of ID's
      *
      * @param key__
      * @param document__
      * @return
      */
     final protected Element $$(final String key__, final Document document__) {
         return document__.getElementById(key__);
     }
 
     /**
      * Fetches the element disregarding widget behavior. i.e. ignores dynamic nature of ID's
      * Static version of {@link #$$(String, org.w3c.dom.Document)}
      *
      * @param key__
      * @param document__
      * @return
      */
     final static protected Element $$static(final String key__, final Document document__) {
         return document__.getElementById(key__);
     }
 
     /**
      * This method WILL NOT FETCH ELEMENTS OF OTHER WIDGETS since the {@link #instanceId instanceId} will be different
      *
      * @param key__
      * @return
      */
     final private Element getWidgetElementById(final String key__) {
         final String elementId__ = Controller.GlobalHTMLIdRegistry.get(key__);
         if (elementId__ == null) {
             throw new java.lang.NullPointerException("SORRY! I FIND THAT ELEMENT \"" + key__ + "\" CONTAINS NULL OR NO REFERENCE IN REGISTRY!");
         }
         final Element returnVal = document_.getElementById(elementId__ + instanceId);
         return returnVal != null ? returnVal : (Element) LogNull.logThrow("SORRY! COULD NOT FETCH THE ELEMENT NAMED " + key__ + " FROM PAGE:" + page.toString() +
                 "\nTHIS COULD HAPPEN IF THE WRONG PAGE IS REGISTERED IN THE WIDGET CONSTRUCTOR WHEN CALLING SUPER OR IF THE ELEMENT WAS REGISTERED UNDER A WRONG ENTITY..");
     }
 
     /**
      * @return HashSet<String> widgetElements
      */
     final Set<String> getWidgetElements() {
         return Controller.GlobalPageIdRegistry.get(page);
     }
 
     @Deprecated
     protected void toggleVisible(final String toggleLink) {
         final Set<String> widgetElements = getWidgetElements();
         if (visible) {
             for (String elementId__ : widgetElements) {
                 if (!elementId__.equals(toggleLink)) {
                     final String existingVal = $$(elementId__).getAttribute(STYLE);
                     if (existingVal.contains(AbstractWidgetListener.DISPLAY_BLOCK_)) {
                         $$(elementId__).setAttribute(STYLE, existingVal.replace(AbstractWidgetListener.DISPLAY_BLOCK_, AbstractWidgetListener.DISPLAY_NONE_));
                     } else {
                         $$(elementId__).setAttribute(STYLE, DISPLAY_NONE_ + existingVal);
                     }
                 }
             }
             visible = false;
         } else {
             for (String elementId__ : widgetElements) {
                 if (!elementId__.equals(toggleLink)) {
                     @FIXME(issue = "this is wrong. css can have necessary spaces. e.g. backgroound-image:0% 0% url(/path/image.png);")
                     final String existingVal = $$(elementId__).getAttribute(STYLE).replace(" ", EMPTY);
 
                     if (existingVal.contains(AbstractWidgetListener.DISPLAY_NONE_)) {
                         $$(elementId__).setAttribute(STYLE, existingVal.replace(AbstractWidgetListener.DISPLAY_NONE_, AbstractWidgetListener.DISPLAY_BLOCK_));
                     } else {
                         $$(elementId__).setAttribute(STYLE, DISPLAY_BLOCK_ + existingVal);
                     }
                 }
             }
             visible = true;
         }
     }
 
     @Deprecated
     @DOCUMENTATION(
             WARNING = @WARNING("Overrides existing values withing the style. " +
                     "Needed by fetchToEmail of WallWidgetHumansWall though since it fails on the other."),
             SEE = @SEE(
                     {AbstractWidgetListener.class, WallWidgetHumansWall.class}
             ),
             NOTE = @NOTE("ai.ilikeplaces.AbstractWidgetListener#$$displayBlock . " +
                     "Also note that this method is needed by " +
                     "ai.ilikeplaces.logic.Listeners.widgets.WallWidgetHumansWall#protected void fetchToEmail(Object... args)")
 
     )
     protected final void displayBlock(final Element element__) {
         element__.setAttribute(STYLE, DISPLAY_BLOCK);
     }
 
     @Deprecated
     @DOCUMENTATION(
             WARNING = @WARNING("Overrides existing values withing the style. " +
                     "Needed by fetchToEmail of WallWidgetHumansWall though since it fails on the other."),
             SEE = @SEE(
                     {AbstractWidgetListener.class, WallWidgetHumansWall.class}
             ),
             NOTE = @NOTE("ai.ilikeplaces.AbstractWidgetListener#$$displayNone . " +
                     "Also note that this method is needed by " +
                     "ai.ilikeplaces.logic.Listeners.widgets.WallWidgetHumansWall#protected void fetchToEmail(Object... args)")
 
     )
     protected final void displayNone(final Element element__) {
         element__.setAttribute(STYLE, DISPLAY_NONE);
     }
 
     @WARNING("Fails on ai.ilikeplaces.logic.Listeners.widgets.WallWidgetHumansWall#protected void fetchToEmail(Object... args) " +
             "since it is not a normal itsnat operation on the current document, but a raw created one.")
     protected final void $$displayBlock(final Element element__) {
         ((ElementCSSInlineStyle) element__).getStyle().setProperty(DISPLAY, BLOCK, EMPTY);
     }
 
     @WARNING("Fails on ai.ilikeplaces.logic.Listeners.widgets.WallWidgetHumansWall#protected void fetchToEmail(Object... args) " +
             "since it is not a normal itsnat operation on the current document, but a raw created one.")
     protected final void $$displayBlock(final WidgetIds element__) {
         $$displayBlock($$(element__));
     }
 
     @WARNING("Fails on ai.ilikeplaces.logic.Listeners.widgets.WallWidgetHumansWall#protected void fetchToEmail(Object... args) " +
             "since it is not a normal itsnat operation on the current document, but a raw created one.")
 
     protected final void $$displayNone(final Element element__) {
         ((ElementCSSInlineStyle) element__).getStyle().setProperty(DISPLAY, NONE, EMPTY);
     }
 
     @WARNING("Fails on ai.ilikeplaces.logic.Listeners.widgets.WallWidgetHumansWall#protected void fetchToEmail(Object... args) " +
             "since it is not a normal itsnat operation on the current document, but a raw created one.")
 
     protected final void $$displayNone(final WidgetIds element__) {
         $$displayNone($$(element__));
     }
 
     protected final void $$setStyle(final Element element__, final String property, final String value) {
         ((ElementCSSInlineStyle) element__).getStyle().setProperty(property, value, EMPTY);
     }
 
 
     protected final void $$setClass(final Element element__, final String classToSet, final boolean clearClasses) {
         final String existingClasses = element__.getAttribute(STR_CSS_CLASS);
         element__.setAttribute(STR_CSS_CLASS, existingClasses.isEmpty() ? STR_CSS_CLASS : (clearClasses ? classToSet : existingClasses + STR_SPACE + classToSet));
     }
 
     final protected Element $$(MarkupTagFace tagNameInAllCaps) {
         return document_.createElement(tagNameInAllCaps.toString());
     }
 
     final protected Element $$(MarkupTagFace tagNameInAllCaps__, final Document document__) {
         return document__.createElement(tagNameInAllCaps__.toString());
     }
 
     /**
      * @param eventTarget_
      * @param eventType_
      * @param eventListener_
      * @param useCapture     ... default false
      */
     final protected void remove(final EventTarget eventTarget_, final EventType eventType_, final EventListener eventListener_, final Boolean... useCapture) {
         itsNatDocument_.removeEventListener(eventTarget_, eventType_.toString(), eventListener_, useCapture.length == 0 ? false : useCapture[0]);
     }
 
     /**
      * @param event_
      * @param eventType_
      * @param eventListener_
      * @param useCapture     ... default false
      */
     final protected void $$remove(final Event event_, final EventType eventType_, final EventListener eventListener_, final Boolean... useCapture) {
         remove(event_.getTarget(), eventType_, eventListener_, useCapture);
     }
 
     /**
      * Use {@link AbstractWidgetListener#$$clear(org.w3c.dom.Element)} instead
      *
      * @param whoseChildrenRemoved
      * @return whoseChildrenRemoved
      */
     @Deprecated
     final protected Element clear(final Element whoseChildrenRemoved) {
         return $$clear(whoseChildrenRemoved);
     }
 
     /**
      * @param whoseChildrenRemoved
      * @return whoseChildrenRemoved
      */
     final protected Element $$clear(final Element whoseChildrenRemoved) {
         try {
             //whoseChildrenRemoved.normalize();//Clearing the air for text nodes
             while (whoseChildrenRemoved.hasChildNodes()) {
                 whoseChildrenRemoved.removeChild(whoseChildrenRemoved.getFirstChild());
             }
         } catch (final Exception e) {
             Loggers.EXCEPTION.error("SORRY! AN ERROR OCCURRED WHILE CLEARING CONTENT.", e);
         }
         return whoseChildrenRemoved;
 //        if (owner.hasChildNodes()) {
 //            final NodeList childNodes = element__.getChildNodes();
 //            for (int i = 0; i < childNodes.getLength(); i++) {
 //                element__.removeChild(childNodes.item(i));
 //            }
 //        }
     }
 
     /**
      * Note: suppresses NPE occured due to element not having any children.
      * Advice: use &nbsp;
      *
      * @param elementToBeSetTextOf
      * @param textToBeSet
      * @return elementToBeSetTextOf
      */
     static Element $$setText(final Element elementToBeSetTextOf, final String textToBeSet) {
         try {
             ElementComposer.$ElementSetText(elementToBeSetTextOf, textToBeSet);
         } catch (final NullPointerException npe) {
             Loggers.EXCEPTION.error("SORRY! THIS ELEMENT DOES NOT HAVE ANY CHILDREN", npe);
         }
         return elementToBeSetTextOf;
     }
 
     /**
      * A new line will be inserted at the start and end of the string.
      *
      * @param jsToBeSentToClient JS To Be Sent To Client
      */
     protected void $$sendJS(final String jsToBeSentToClient) {
         itsNatDocument_.addCodeToSend("\n" + jsToBeSentToClient + "\n");
     }
 
     /**
      * A new line will be inserted at the start and end of the string.
      * <p/>
      * If not trailing semicolon is found, it will be added
      *
      * @param jsStatementToBeSentToClient JS statement To Be Sent To Client
      */
     protected void $$sendJSStmt(final String jsStatementToBeSentToClient) {
         itsNatDocument_.addCodeToSend("\n" + jsStatementToBeSentToClient + (jsStatementToBeSentToClient.endsWith(";") ? "\n" : ";\n"));
     }
 
 
     /**
      * @param event Event of which the target is required
      * @return Target Element of this Event
      */
     protected Element $$(final Event event) {
         return (Element) event.getCurrentTarget();
     }
 
     final static protected Logger logger = LoggerFactory.getLogger(AbstractWidgetListener.class);
 
     void $$asyn(final Runnable r) {
         new Thread(r).start();
     }
 
 
     protected void $$printDocumentElementIds() {
         for (final String id : Controller.GlobalPageIdRegistry.get(page)) {
             Loggers.DEBUG.debug(page.toString() + ":ids:" + document_.getElementById(id));
         }
     }
 
     /**
      * @param itsNatServletRequest ItsNatServletRequest
      * @param uRLParameter         Parameter on URL which you want
      * @return the result returned from itsNatServletRequest.getServletRequest().getParameter()
      */
     public String $$(final ItsNatServletRequest itsNatServletRequest, final String uRLParameter) {
         return itsNatServletRequest.getServletRequest().getParameter(uRLParameter);
     }
 
 
     /**
      * @param elementOfWhichIdIsRequired
      * @return Id of element
      */
     final protected String $$getId(final Element elementOfWhichIdIsRequired) {
         return elementOfWhichIdIsRequired.getAttribute(MarkupTag.GENERIC.id());
     }
 
     /**
      * @param elementOfWhichIdIsRequired
      * @return Id of element
      */
     final protected String $$getId(final String elementOfWhichIdIsRequired) {
         return $$getId($$(elementOfWhichIdIsRequired));
     }
 
 
     /**
      * Use this method to register an element which will be used to notify the user.
      * The element sent in should support {@link Element#setTextContent(String)}, as in, should show text in it, as in
      * should not be something like an IMG element.
      *
      * @param elementToUseForUserNotifications
      *
      * @return the element sent in
      */
     final protected Element registerUserNotifier(final Element elementToUseForUserNotifications) {
         if (elementToUseForUserNotifications != null) {
             notifier = elementToUseForUserNotifications;
             return notifier;
         } else {
             throw new NullPointerException(THE_ELEMENT_GIVEN_T0_REGISTER_FOR_NOTIFICATIONS_IS_NULL);
         }
     }
 
     /**
      * Use this method to register an element which will be used to notify the user.
      * The element sent in should support {@link Element#setTextContent(String)}, as in, should show text in it, as in
      * should not be something like an IMG element.
      *
      * @param elementToUseForUserNotifications
      *
      * @return the element sent in
      */
     final protected Element registerUserNotifier(final WidgetIds elementToUseForUserNotifications) {
         return registerUserNotifier($$(elementToUseForUserNotifications));
     }
 
     /**
      * Use in conjunction with {@link #registerUserNotifier(org.w3c.dom.Element)}
      *
      * @param message Message to be displayed to the user
      * @return the notifier element
      */
     final protected Element notifyUser(final String message) {
         if (notifier == null) {
             throw new IllegalStateException(NOTIFIER_ELEMENT_HAS_NOT_BEEN_INITIALIZED);
         }
         notifier.setTextContent(message);
         $$sendJS(JSCodeToSend.clearContent(notifier.getAttribute(MarkupTag.GENERIC.id()), 10000));
         return notifier;
     }
 
     static public interface WidgetIds {
         public String toString();
     }
 
 }
