 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.renderkit;
 
 import org.icemobile.component.ITabSet;
 import org.icemobile.component.IMobiComponent;
 import org.icemobile.util.Utils;
 
 import javax.swing.text.html.HTML;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static org.icemobile.util.HTML.*;
 
 public class TabSetCoreRenderer extends BaseCoreRenderer {
     
     private static final Logger logger =
             Logger.getLogger(TabSetCoreRenderer.class.toString());
     
     public void encodeBegin(IMobiComponent component, IResponseWriter writer, boolean isJSP, boolean isTop)
             throws IOException {
         ITabSet tabset = (ITabSet)component;
         String clientId = tabset.getClientId();
         /* JSF comp writes script into 1st span */
        if (!isJSP)  {
            super.writeExternalScript(component, writer,
                    tabset.getJavascriptFileRequestPath(),
                    !tabset.isScriptLoaded());
             tabset.setScriptLoaded();
         }
         writer.startElement(DIV_ELEM, tabset);
         writer.writeAttribute(ID_ATTR, clientId);
         // apply default style class
         StringBuilder styleClass = new StringBuilder(ITabSet.TABSET_CONTAINER_CLASS);
         // apply orientation style for bottom or to tab placement
         String orientation = tabset.getOrientation();
         if (isTop){
             styleClass.append(" ").append(ITabSet.TABSET_CONTAINER_TOP_CLASS);
         }
         else {
             styleClass.append(" ").append(ITabSet.TABSET_CONTAINER_BOTTOM_CLASS);
         }
         // default to bottom placement
         // this attribute could probably be release with a component tree search
         // for the footer facet which would be a lot cleaner.
         boolean parentFooter = tabset.isParentFooter();
         if (!isTop && parentFooter) {
             styleClass.append(" ").append(ITabSet.TABSET_CONTAINER_BOTTOM_FOOTER_CLASS);
         }
         // this attribute could probably be release with a component tree search
         // for the footer facet which would be a lot cleaner.
         boolean parentHeader = tabset.isParentHeader();
         if (isTop && parentHeader) {
             styleClass.append(" ").append(ITabSet.TABSET_CONTAINER_TOP_HEADER_CLASS);
         }
         // user specified style class
         String userDefinedClass = tabset.getStyleClass();
         if (userDefinedClass != null && userDefinedClass.length() > 0) {
             styleClass.append(" ").append(userDefinedClass);
         }
         writer.writeAttribute("class", styleClass.toString());
         // write out any users specified style attributes.
         writer.writeAttribute(STYLE_ATTR, tabset.getStyle());
         /* now do tab headers */
     }
 
     public void writeStartOfTabContents( IMobiComponent tabset,IResponseWriter writer, String clientId) throws IOException{
         writer.startElement(DIV_ELEM, tabset);
         ITabSet its = (ITabSet)tabset;
         writer.writeAttribute(ID_ATTR, clientId + "_tabContent");
         writer.writeAttribute(CLASS_ATTR, ITabSet.TABSET_CONTENT_CLASS.toString());
         if (null != its.getHeight()){
             writer.writeAttribute(STYLE_ATTR,  "height:" + its.getHeight()+";");
         }
     }
 
     /**
      * used only by JSP at this time.
      * @param tabset
      * @param writer
      * @throws IOException
      */
     public void writeEndOfTabContents(IMobiComponent tabset,IResponseWriter writer) throws IOException{
         writer.endElement(DIV_ELEM);//end of wrapper
         writer.endElement(DIV_ELEM); //end of tabPane
     }
 
     public void encodeEndContents(IMobiComponent component, IResponseWriter writer)
             throws IOException{
         writer.endElement(DIV_ELEM);
     }
     public void encodeEnd(IMobiComponent component, IResponseWriter writer, boolean isJSP)
             throws IOException {
         /* what about hidden field? */
     //    writer.endElement(DIV_ELEM); //end of tabContent div
         ITabSet tabset = (ITabSet)component;
         writeHiddenInput(writer, component, String.valueOf(tabset.getIndex()));
         encodeScript(component, writer, isJSP);
         writer.endElement(DIV_ELEM);  //end of tabset div
     }
 
     public void encodeTabSetHeaders(IMobiComponent component, IResponseWriter writer)
             throws IOException {
         ITabSet tabset = (ITabSet) component;
         writer.startElement(DIV_ELEM, component);
         writer.writeAttribute(ID_ATTR, tabset.getClientId() + "_tabs");
         String baseTabsClass = ITabSet.TABSET_TABS_CLASS.toString();
         if (!tabset.isFixedPosition()){
             baseTabsClass += " mobi-tabset-tabs-nonfixed";
         }
         writer.writeAttribute("class", baseTabsClass);
         writer.startElement(UL_ELEM, component);
         writer.writeAttribute(ID_ATTR, tabset.getClientId()+"_dc");
     }
     public void encodeEndTabSetHeaders(IMobiComponent component, IResponseWriter writer)
             throws IOException{
         writer.endElement(UL_ELEM);
         writer.endElement(DIV_ELEM);
     }
 
     public void encodeScript(IMobiComponent component, IResponseWriter writer, boolean isJSP)
             throws IOException {
         ITabSet tabset = (ITabSet) component;
         int idIndex = tabset.getIndex();
         String clientId = tabset.getClientId();
         String height = tabset.getHeight();
         writer.startElement(SPAN_ELEM);
         writer.writeAttribute(CLASS_ATTR, "mobi-hidden");
         writer.writeAttribute(ID_ATTR, clientId + "_script");
         writer.startElement(SCRIPT_ELEM);
         writer.writeAttribute(TYPE_ATTR, "text/javascript");
         StringBuilder cfg = new StringBuilder("ice.mobi.tabsetController.initClient('" );
         cfg.append(clientId).append("',").append("{singleSubmit: ");
         boolean singleSubmit = tabset.isSingleSubmit();
         cfg.append(singleSubmit);
         cfg.append(", tIndex: ").append(idIndex);
         if (!isJSP){
             cfg.append(", hash: ").append(tabset.getHashVal());
         }
         cfg.append(", disabled: ").append(tabset.isDisabled());
         if (!this.isStringAttributeEmpty(height)){
             cfg.append(",height: '").append(height).append("'");
         } else  {
             cfg.append(", autoheight: ").append(tabset.isAutoHeight());
         }
         if (tabset.isAutoWidth()){
             cfg.append(", autoWidth: ").append(tabset.isAutoWidth());
         }
         cfg.append("}").append(");");
         writer.writeText(cfg.toString());
         writer.endElement(SCRIPT_ELEM);
         writer.endElement(SPAN_ELEM);
     }
 
 }
