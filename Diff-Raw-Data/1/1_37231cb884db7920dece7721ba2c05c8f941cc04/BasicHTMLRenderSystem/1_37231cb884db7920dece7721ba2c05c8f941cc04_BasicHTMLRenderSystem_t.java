 /*
  * Created on Jul 27, 2005
  */
 package uk.org.ponder.rsf.renderer.html;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.Map;
 
 import uk.org.ponder.rsf.components.ParameterList;
 import uk.org.ponder.rsf.components.UIAnchor;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBoundList;
 import uk.org.ponder.rsf.components.UIBoundString;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIOutputMultiline;
 import uk.org.ponder.rsf.components.UIParameter;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.renderer.ComponentRenderer;
 import uk.org.ponder.rsf.renderer.RenderSystem;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.renderer.StaticComponentRenderer;
 import uk.org.ponder.rsf.renderer.StaticRendererCollection;
 import uk.org.ponder.rsf.request.FossilizedConverter;
 import uk.org.ponder.rsf.request.SubmittedValueEntry;
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.rsf.template.XMLLumpList;
 import uk.org.ponder.rsf.uitype.UITypes;
 import uk.org.ponder.rsf.viewstate.ViewParamUtil;
 import uk.org.ponder.streamutil.StreamCopyUtil;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.stringutil.StringSet;
 import uk.org.ponder.stringutil.URLUtil;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.xml.XMLUtil;
 import uk.org.ponder.xml.XMLWriter;
 
 /**
  * The implementation of the standard XHTML rendering System. This class is due
  * for basic refactoring since it contains logic that belongs in a) a "base
  * System-independent" lookup bean, and b) in a number of individual
  * ComponentRenderer objects.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class BasicHTMLRenderSystem implements RenderSystem {
   private StaticRendererCollection scrc;
 
   public void setStaticRenderers(StaticRendererCollection scrc) {
     this.scrc = scrc;
   }
 
   // two methods for the RenderSystemDecoder interface
   public void normalizeRequestMap(Map requestparams) {
     String key = RenderUtil.findCommandParams(requestparams);
     if (key != null) {
       String params = key.substring(FossilizedConverter.COMMAND_LINK_PARAMETERS
           .length());
       RenderUtil.unpackCommandLink(params, requestparams);
       requestparams.remove(key);
     }
   }
 
   public void fixupUIType(SubmittedValueEntry sve) {
     if (sve.oldvalue instanceof Boolean) {
       if (sve.newvalue == null)
         sve.newvalue = Boolean.FALSE;
     }
     else if (sve.oldvalue instanceof String[]) {
       if (sve.newvalue == null)
         sve.newvalue = new String[] {};
     }
   }
 
   private void closeTag(PrintOutputStream pos, XMLLump uselump) {
     pos.print("</");
     pos.write(uselump.buffer, uselump.start + 1, uselump.length - 2);
     pos.print(">");
   }
 
   private void dumpBoundFields(UIBound torender, XMLWriter xmlw) {
     if (torender != null) {
       if (torender.fossilizedbinding != null) {
         RenderUtil.dumpHiddenField(torender.fossilizedbinding.name,
             torender.fossilizedbinding.value, xmlw);
       }
       if (torender.fossilizedshaper != null) {
         RenderUtil.dumpHiddenField(torender.fossilizedshaper.name,
             torender.fossilizedshaper.value, xmlw);
       }
     }
   }
 
   // No, this method will not stay like this forever! We plan on an architecture
   // with renderer-per-component "class" as before, plus interceptors.
   // Although a lot of the parameterisation now lies in the allowable tag
   // set at target.
   public int renderComponent(UIComponent torendero, XMLLump[] lumps,
       int lumpindex, PrintOutputStream pos) {
     XMLWriter xmlw = new XMLWriter(pos);
     XMLLump lump = lumps[lumpindex];
     int nextpos = -1;
     XMLLump outerendopen = lump.open_end;
     XMLLump outerclose = lump.close_tag;
 
     nextpos = outerclose.lumpindex + 1;
 
     XMLLumpList payloadlist = lump.downmap == null ? null
         : lump.downmap.hasID(XMLLump.PAYLOAD_COMPONENT) ? lump.downmap
             .headsForID(XMLLump.PAYLOAD_COMPONENT)
             : null;
     XMLLump payload = payloadlist == null ? null
         : payloadlist.lumpAt(0);
 
     // if there is no peer component, it might still be a static resource holder
     // that needs URLs rewriting.
     // we assume there is no payload component here, since there is no producer
     // ID that might govern selection. So we use "outer" indices.
     if (torendero == null) {
       if (lump.rsfID.startsWith(XMLLump.SCR_PREFIX)) {
         String scrname = lump.rsfID.substring(XMLLump.SCR_PREFIX.length());
         StaticComponentRenderer scr = scrc.getSCR(scrname);
         if (scr != null) {
           int tagtype = scr.render(lumps, lumpindex, xmlw);
           nextpos = tagtype == ComponentRenderer.LEAF_TAG ? outerclose.lumpindex + 1
               : outerendopen.lumpindex + 1;
         }
       }
 
       if (lump.textEquals("<form ")) {
         Logger.log.warn("Warning: skipping form tag with rsf:id " + lump.rsfID
             + " and all children at " + lump.toDebugString()
             + " since no peer component");
       }
     }
     else {
       // else there IS a component and we are going to render it. First make
       // sure we render any preamble.
       XMLLump endopen = outerendopen;
       XMLLump close = outerclose;
       XMLLump uselump = lump;
       if (payload != null) {
         endopen = payload.open_end;
         close = payload.close_tag;
         uselump = payload;
         RenderUtil.dumpTillLump(lumps, lumpindex, payload.lumpindex, pos);
         lumpindex = payload.lumpindex;
       }
 
       String fullID = torendero.getFullID();
       HashMap attrcopy = new HashMap();
       attrcopy.putAll(uselump.attributemap);
       attrcopy.put("id", fullID);
       attrcopy.remove(XMLLump.ID_ATTRIBUTE);
       // ALWAYS dump the tag name, this can never be rewritten. (probably?!)
       pos.write(uselump.buffer, uselump.start, uselump.length);
       // TODO: Note that these are actually BOUND now. Create some kind of
       // defaultBoundRenderer.
       if (torendero instanceof UIBound) {
         UIBound torender = (UIBound) torendero;
         if (!torender.willinput) {
           if (torendero.getClass() == UIOutput.class) {
             String value = ((UIOutput) torendero).getValue();
             if (UITypes.isPlaceholder(value)) {
               RenderUtil.dumpTillLump(lumps, lumpindex + 1,
                   close.lumpindex + 1, pos);
             }
             else {
               XMLUtil.dumpAttributes(attrcopy, xmlw);
               pos.print(">");
               xmlw.write(value);
               closeTag(pos, uselump);
             }
           }
           else if (torendero.getClass() == UIOutputMultiline.class) {
             StringList value = ((UIOutputMultiline) torendero).getValue();
             if (value == null) {
               RenderUtil.dumpTillLump(lumps, lumpindex + 1,
                   close.lumpindex + 1, pos);
             }
             else {
               XMLUtil.dumpAttributes(attrcopy, xmlw);
               pos.print(">");
               for (int i = 0; i < value.size(); ++i) {
                 if (i != 0) {
                   pos.print("<br/>");
                 }
                 xmlw.write(value.stringAt(i));
               }
               closeTag(pos, uselump);
             }
           }
           else if (torender.getClass() == UIAnchor.class) {
             String value = ((UIAnchor) torendero).getValue();
             if (UITypes.isPlaceholder(value)) {
               RenderUtil.dumpTillLump(lumps, lumpindex + 1,
                   close.lumpindex + 1, pos);
             }
             else {
               attrcopy.put("name", value);
               XMLUtil.dumpAttributes(attrcopy, xmlw);
               if (endopen.lumpindex == close.lumpindex) {
                 pos.print("/>");
               }
               else {
                 pos.print(">");
                 RenderUtil.dumpTillLump(lumps, endopen.lumpindex + 1,
                     close.lumpindex + 1, pos);
               }
             }
           }
         }
         // factor out component-invariant processing of UIBound.
         else { // Bound with willinput = true
           attrcopy.put("name", fullID);
           // attrcopy.put("id", fullID);
           String value = "";
           String body = null;
           if (torendero instanceof UIInput) {
             value = ((UIInput) torender).getValue();
             if (uselump.textEquals("<textarea ")) {
               body = value;
             }
             else {
               attrcopy.put("value", value);
             }
           }
           else if (torendero instanceof UIBoundBoolean) {
             if (((UIBoundBoolean) torender).getValue()) {
               attrcopy.put("checked", "yes");
               // this "value" is thrown away for checkboxes.
               value = "true";
             }
             else {
              attrcopy.remove("checked");
               value = "false";
             }
             // eh? What is the "value" attribute for one of these?
             attrcopy.put("value", "true");
           }
 
           XMLUtil.dumpAttributes(attrcopy, xmlw);
           pos.print(">");
           if (body != null) {
             xmlw.write(body);
             pos.write(close.buffer, close.start, close.length);
           }
           else {
             RenderUtil.dumpTillLump(lumps, endopen.lumpindex + 1,
                 close.lumpindex + 1, pos);
           }
 
           // unify hidden field processing? ANY parameter children found must
           // be dumped as hidden fields.
         }
         // dump any fossilized binding for this component.
         dumpBoundFields(torender, xmlw);
       } // end if UIBound
 
       else if (torendero instanceof UISelect) {
         UISelect select = (UISelect) torendero;
         // The HTML submitted value from a <select> actually corresponds
         // with the selection member, not the top-level component.
         attrcopy.put("name", select.selection.getFullID());
         attrcopy.put("id", select.selection.getFullID());
         StringSet selected = new StringSet();
         if (select.selection instanceof UIBoundList) {
           selected.addAll(((UIBoundList) select.selection).getValue());
           attrcopy.put("multiple", "true");
         }
         else if (select.selection instanceof UIBoundString) {
           selected.add(((UIBoundString) select.selection).getValue());
         }
         XMLUtil.dumpAttributes(attrcopy, xmlw);
         pos.print(">");
         String[] values = select.optionlist.getValue();
         String[] names = select.optionnames == null ? values
             : select.optionnames.getValue();
         for (int i = 0; i < names.length; ++i) {
           pos.print("<option value=\"");
           xmlw.write(values[i]);
           if (selected.contains(values[i])) {
             pos.print("\" selected=\"true");
           }
           pos.print("\">");
           xmlw.write(names[i]);
           pos.print("</option>\n");
         }
         closeTag(pos, uselump);
         dumpBoundFields(select.selection, xmlw);
         dumpBoundFields(select.optionlist, xmlw);
         dumpBoundFields(select.optionnames, xmlw);
       }
       else if (torendero instanceof UILink) {
         UILink torender = (UILink) torendero;
         String attrname = URLRewriteSCR.getLinkAttribute(uselump);
         if (attrname != null) {
           attrcopy.put(attrname, torender.target.getValue());
         }
         XMLUtil.dumpAttributes(attrcopy, xmlw);
         pos.print(">");
         String value = torender.linktext == null ? null
             : torender.linktext.getValue();
         if (value != null && !UITypes.isPlaceholder(value)) {
           xmlw.write(value);
           closeTag(pos, uselump);
         }
         else {
           RenderUtil.dumpTillLump(lumps, endopen.lumpindex + 1,
               close.lumpindex + 1, pos);
         }
       }
 
       else if (torendero instanceof UICommand) {
         UICommand torender = (UICommand) torendero;
         String value = RenderUtil.makeURLAttributes(torender.parameters);
         // any desired "attributes" decoded for JUST THIS ACTION must be
         // secretly
         // bundled as this special attribute.
         attrcopy.put("name", FossilizedConverter.COMMAND_LINK_PARAMETERS
             + value);
         if (lump.textEquals("<input ") && torender.commandtext != null) {
           attrcopy.put("value", torender.commandtext);
         }
 
         XMLUtil.dumpAttributes(attrcopy, xmlw);
         if (endopen.lumpindex == close.lumpindex) {
           pos.print("/>");
         }
         else {
           pos.print(">");
           if (torender.commandtext != null && lump.textEquals("<button ")) {
             xmlw.write(torender.commandtext);
             closeTag(pos, uselump);
           }
           else {
             RenderUtil.dumpTillLump(lumps, endopen.lumpindex + 1,
                 close.lumpindex + 1, pos);
           }
         }
         // RenderUtil.dumpHiddenField(SubmittedValueEntry.ACTION_METHOD,
         // torender.actionhandler, pos);
       }
       // Forms behave slightly oddly in the hierarchy - by the time they reach
       // the renderer, they have been "shunted out" of line with their children,
       // i.e. any "submitting" controls, if indeed they ever were there.
       else if (torendero instanceof UIForm) {
         UIForm torender = (UIForm) torendero;
         int qpos = torender.postURL.indexOf('?');
         // Ensure that any attributes on this postURL
         if (qpos == -1) {
           attrcopy.put("action", torender.postURL);
         }
         else {
           attrcopy.put("action", torender.postURL.substring(0, qpos));
           String attrs = torender.postURL.substring(qpos + 1);
           Map attrmap = URLUtil.paramsToMap(attrs, new HashMap());
           ParameterList urlparams = ViewParamUtil.mapToParamList(attrmap);
           torender.parameters.addAll(urlparams);
         }
         XMLUtil.dumpAttributes(attrcopy, xmlw);
         pos.println(">");
         for (int i = 0; i < torender.parameters.size(); ++i) {
           UIParameter param = torender.parameters.parameterAt(i);
           RenderUtil.dumpHiddenField(param.name, param.value, xmlw);
         }
         // override "nextpos" - form is expected to contain numerous nested
         // Components.
         // this is the only ANOMALY!! Forms together with payload cannot work.
         // the fact we are at the wrong recursion level will "come out in the
         // wash"
         // since we must return to the base recursion level before we exit this
         // domain.
         // Assuming there are no paths *IN* through forms that do not also lead
         // *OUT* there will be no problem. Check what this *MEANS* tomorrow.
         nextpos = endopen.lumpindex + 1;
       }
       else if (torendero instanceof UIVerbatim) {
         UIVerbatim torender = (UIVerbatim) torendero;
         String rendered = null;
         // inefficient implementation for now, upgrade when we write bulk POS
         // utils.
         if (torender.markup instanceof InputStream) {
           rendered = StreamCopyUtil
               .streamToString((InputStream) torender.markup);
         }
         else if (torender.markup instanceof Reader) {
           rendered = StreamCopyUtil.readerToString((Reader) torender.markup);
         }
         else if (torender.markup != null) {
           rendered = torender.markup.toString();
         }
         if (rendered == null) {
           RenderUtil.dumpTillLump(lumps, lumpindex + 1, close.lumpindex + 1,
               pos);
         }
         else {
           XMLUtil.dumpAttributes(attrcopy, xmlw);
           pos.print(">");
           pos.print(rendered);
           closeTag(pos, uselump);
         }
       }
       // if there is a payload, dump the postamble.
       if (payload != null) {
         RenderUtil.dumpTillLump(lumps, close.lumpindex + 1,
             outerclose.lumpindex + 1, pos);
       }
 
     }
 
     return nextpos;
   }
 
 }
