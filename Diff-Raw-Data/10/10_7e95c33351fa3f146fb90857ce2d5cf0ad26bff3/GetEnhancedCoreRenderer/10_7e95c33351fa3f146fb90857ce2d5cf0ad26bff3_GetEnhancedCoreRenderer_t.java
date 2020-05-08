 package org.icemobile.renderkit;
 
 import static org.icemobile.util.HTML.ANCHOR_ELEM;
 import static org.icemobile.util.HTML.CLASS_ATTR;
 import static org.icemobile.util.HTML.DISABLED_ATTR;
 import static org.icemobile.util.HTML.HREF_ATTR;
 import static org.icemobile.util.HTML.ID_ATTR;
 import static org.icemobile.util.HTML.ONCLICK_ATTR;
 import static org.icemobile.util.HTML.SPAN_ELEM;
 import static org.icemobile.util.HTML.STYLE_ATTR;
 
 import java.io.IOException;
 
 import org.icemobile.component.IGetEnhanced;
 import org.icemobile.util.ClientDescriptor;
 
 public class GetEnhancedCoreRenderer {
     
     public void encode(IGetEnhanced component, IResponseWriter writer)
             throws IOException {
         
         if( component.isIOSSmartBannerRendered() ){
             return;
         }
         
         String clientId = component.getClientId();
         ClientDescriptor client = component.getClient();
         
         if( !client.isICEmobileContainer() && !client.isSXRegistered()){
             writer.startElement(SPAN_ELEM, component);
             writer.writeAttribute(ID_ATTR, clientId);
             String styleClass = component.getStyleClass();
             if( styleClass != null ){
                 styleClass = IGetEnhanced.CSS_CLASS + " " + styleClass;
             }
             else{
                 styleClass = IGetEnhanced.CSS_CLASS;
             }
             writer.writeAttribute(CLASS_ATTR, styleClass);
             String style = component.getStyle();
             if( style != null ){
                 writer.writeAttribute(STYLE_ATTR, style);
             }
             boolean disabled = component.isDisabled();
             if( disabled ){
                 writer.writeAttribute(DISABLED_ATTR, "disabled");
             }
             
             String msg = IGetEnhanced.INFO_MSG; //default msg
             String link = null;
             if( client.isAndroidOS()){
                 link = IGetEnhanced.ANDROID_LINK; 
                 String androidMsg = component.getAndroidMsg();
                 if( androidMsg != null ){
                     msg = androidMsg;
                 }
             }
             else if( client.isIOS()){
                 link = IGetEnhanced.IOS_LINK; 
                 String iosMsg = component.getIosMsg();
                 if( iosMsg != null ){
                     msg = iosMsg;
                 }
             }
             else if( client.isBlackBerryOS()){
                 link = IGetEnhanced.BLACKBERRY_LINK; 
                 String blackBerryMsg = component.getBlackberryMsg();
                 if( blackBerryMsg != null ){
                     msg = blackBerryMsg;
                 }
             }
             else{
                 link = IGetEnhanced.ICEMOBILE_LINK;
             }
             writer.writeText(msg);
 
             if( client.isIOS() ){
                 writer.startElement(ANCHOR_ELEM, null);
                 writer.writeAttribute(HREF_ATTR,"#");
                 writer.writeAttribute(CLASS_ATTR, "mobi-button mobi-button-important");
                 writer.writeAttribute(ONCLICK_ATTR, component.getICEmobileRegisterSXScript());
                 writer.writeText("Enable ICEmobile SX");
                writer.endElement(ANCHOR_ELEM);
             }
             
             if( component.isIncludeLink() ){
                 writer.startElement(ANCHOR_ELEM);
                 writer.writeAttribute(HREF_ATTR, link);
                 writer.writeAttribute(CLASS_ATTR, "mobi-button mobi-button-important");
                 writer.writeText(IGetEnhanced.DOWNLOAD);
                 writer.endElement(ANCHOR_ELEM);
             }
             
             writer.endElement(SPAN_ELEM);
         }
     }
 
 }
