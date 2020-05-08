 /*
  * Created on Jul 27, 2005
  */
 package uk.org.ponder.rsf.renderer.html;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.Map;
 
 import uk.org.ponder.rsf.components.UIAnchor;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBoundList;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIOutputMultiline;
 import uk.org.ponder.rsf.components.UIParameter;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UISelectChoice;
 import uk.org.ponder.rsf.components.UISelectLabel;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.renderer.ComponentRenderer;
 import uk.org.ponder.rsf.renderer.DecoratorManager;
 import uk.org.ponder.rsf.renderer.RenderSystem;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.renderer.StaticComponentRenderer;
 import uk.org.ponder.rsf.renderer.StaticRendererCollection;
 import uk.org.ponder.rsf.renderer.TagRenderContext;
 import uk.org.ponder.rsf.request.FossilizedConverter;
 import uk.org.ponder.rsf.request.SubmittedValueEntry;
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.rsf.template.XMLLumpList;
 import uk.org.ponder.rsf.uitype.UITypes;
 import uk.org.ponder.rsf.view.View;
 import uk.org.ponder.streamutil.StreamCopyUtil;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.stringutil.URLUtil;
 import uk.org.ponder.util.Constants;
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
   private DecoratorManager decoratormanager;
 
   public void setStaticRenderers(StaticRendererCollection scrc) {
     this.scrc = scrc;
   }
 
   public void setDecoratorManager(DecoratorManager decoratormanager) {
     this.decoratormanager = decoratormanager;
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
     else if (sve.oldvalue instanceof String) {
      if (Constants.NULL_STRING.equals(sve.newvalue)) {
         sve.newvalue = null;
       }
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
   public int renderComponent(UIComponent torendero, View view, XMLLump[] lumps,
       int lumpindex, PrintOutputStream pos, String IDStrategy) {
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
         if (scr == null) {
           Logger.log
               .info("Warning: unrecognised static component renderer reference with key "
                   + scrname + " at lump " + lump.toDebugString());
           scr = NullRewriteSCR.instance;
         }
         int tagtype = scr.render(lumps, lumpindex, xmlw);
         nextpos = tagtype == ComponentRenderer.LEAF_TAG ? outerclose.lumpindex + 1
             : outerendopen.lumpindex + 1;
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
       RenderUtil.adjustForID(attrcopy, IDStrategy, fullID);
       decoratormanager.decorate(torendero.decorators, uselump.getTag(),
           attrcopy);
 
       TagRenderContext rendercontext = new TagRenderContext(attrcopy, lumps,
           uselump, endopen, close, pos, xmlw);
       // ALWAYS dump the tag name, this can never be rewritten. (probably?!)
       pos.write(uselump.buffer, uselump.start, uselump.length);
       // TODO: Note that these are actually BOUND now. Create some kind of
       // defaultBoundRenderer.
       if (torendero instanceof UIBound) {
         UIBound torender = (UIBound) torendero;
         if (!torender.willinput) {
           if (torendero.getClass() == UIOutput.class) {
             String value = ((UIOutput) torendero).getValue();
             rewriteLeaf(value, rendercontext);
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
               renderUnchanged(rendercontext);
             }
             else {
               attrcopy.put("name", value);
               replaceAttributes(rendercontext);
             }
           }
         }
         // factor out component-invariant processing of UIBound.
         else { // Bound with willinput = true
           attrcopy.put("name", torender.submittingname);
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
             }
             else {
               attrcopy.remove("checked");
             }
             attrcopy.put("value", "true");
           }
           rewriteLeaf(body, rendercontext);
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
         attrcopy.put("name", select.selection.submittingname);
         attrcopy.put("id", select.selection.getFullID());
         boolean ishtmlselect = uselump.textEquals("<select ");
         if (select.selection instanceof UIBoundList && ishtmlselect) {
           attrcopy.put("multiple", "true");
         }
         XMLUtil.dumpAttributes(attrcopy, xmlw);
         if (ishtmlselect) {
           pos.print(">");
           String[] values = select.optionlist.getValue();
           String[] names = select.optionnames == null ? values
               : select.optionnames.getValue();
           for (int i = 0; i < names.length; ++i) {
             pos.print("<option value=\"");
             xmlw.write(values[i] == null? Constants.NULL_STRING: values[i]);
             if (select.selected.contains(values[i])) {
               pos.print("\" selected=\"true");
             }
             pos.print("\">");
             xmlw.write(names[i]);
             pos.print("</option>\n");
           }
           closeTag(pos, uselump);
         }
         else {
           dumpTemplateBody(rendercontext);
         }
 
         dumpBoundFields(select.selection, xmlw);
         dumpBoundFields(select.optionlist, xmlw);
         dumpBoundFields(select.optionnames, xmlw);
       }
       else if (torendero instanceof UISelectChoice) {
         UISelectChoice torender = (UISelectChoice) torendero;
         UISelect parent = (UISelect) view.getComponent(torender.parentFullID);
         String value = parent.optionlist.getValue()[torender.choiceindex];
         // currently only peers with "input type="radio"".
         attrcopy.put("name", parent.selection.submittingname);
         attrcopy.put("value", value);
         attrcopy.remove("checked");
         if (parent.selected.contains(value)) {
           attrcopy.put("checked", "true");
         }
         replaceAttributes(rendercontext);
       }
       else if (torendero instanceof UISelectLabel) {
         UISelectLabel torender = (UISelectLabel) torendero;
         UISelect parent = (UISelect) view.getComponent(torender.parentFullID);
         String value = parent.optionnames.getValue()[torender.choiceindex];
         replaceBody(value, rendercontext);
       }
       else if (torendero instanceof UILink) {
         UILink torender = (UILink) torendero;
         // TODO - imagine that an image link has been provided. this
         // both needs URL rewritten inside, and also NOT BEING REPLACED with
         // the supplied body text.
         String attrname = URLRewriteSCR.getLinkAttribute(uselump);
         if (attrname != null) {
           String target = torender.target.getValue();
           if (target == null || target.length() == 0) {
             throw new IllegalArgumentException("Empty URL in UILink at " + torender.getFullID());
           }
           URLRewriteSCR urlrewriter = (URLRewriteSCR) scrc.getSCR(URLRewriteSCR.NAME);
           if (!URLUtil.isAbsolute(target)) {
             String rewritten = urlrewriter.resolveURL(target);
             if (rewritten != null)
               target = rewritten;
           }
           attrcopy.put(attrname, target);
         }
         String value = torender.linktext == null ? null
             : torender.linktext.getValue();
         rewriteLeaf(value, rendercontext);
       }
 
       else if (torendero instanceof UICommand) {
         UICommand torender = (UICommand) torendero;
         String value = RenderUtil.makeURLAttributes(torender.parameters);
         // any desired "attributes" decoded for JUST THIS ACTION must be
         // secretly
         // bundled as this special attribute.
         attrcopy.put("name", FossilizedConverter.COMMAND_LINK_PARAMETERS
             + value);
         String text = torender.commandtext;
         boolean isbutton = lump.textEquals("<button ");
         if (text != null && !isbutton) {
           attrcopy.put("value", torender.commandtext);
           text = null;
         }
         rewriteLeaf(text, rendercontext);
         // RenderUtil.dumpHiddenField(SubmittedValueEntry.ACTION_METHOD,
         // torender.actionhandler, pos);
       }
       // Forms behave slightly oddly in the hierarchy - by the time they reach
       // the renderer, they have been "shunted out" of line with their children,
       // i.e. any "submitting" controls, if indeed they ever were there.
       else if (torendero instanceof UIForm) {
         UIForm torender = (UIForm) torendero;
         if (attrcopy.get("method") == null) { // forms DEFAULT to be post
           attrcopy.put("method", "post");
         }
         // form fixer guarantees that this URL is attribute free.
         attrcopy.put("action", torender.targetURL);
 
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
           renderUnchanged(rendercontext);
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
 
   private void renderUnchanged(TagRenderContext c) {
     RenderUtil.dumpTillLump(c.lumps, c.uselump.lumpindex + 1,
         c.close.lumpindex + 1, c.pos);
   }
 
   private void rewriteLeaf(String value, TagRenderContext c) {
     if (value != null && !UITypes.isPlaceholder(value))
       replaceBody(value, c);
     else
       replaceAttributes(c);
   }
 
   private void replaceBody(String value, TagRenderContext c) {
     XMLUtil.dumpAttributes(c.attrcopy, c.xmlw);
     c.pos.print(">");
     c.xmlw.write(value);
     closeTag(c.pos, c.uselump);
   }
 
   private void replaceAttributes(TagRenderContext c) {
     XMLUtil.dumpAttributes(c.attrcopy, c.xmlw);
 
     dumpTemplateBody(c);
   }
 
   private void dumpTemplateBody(TagRenderContext c) {
     if (c.endopen.lumpindex == c.close.lumpindex) {
       c.pos.print("/>");
     }
     else {
       c.pos.print(">");
       RenderUtil.dumpTillLump(c.lumps, c.endopen.lumpindex + 1,
           c.close.lumpindex + 1, c.pos);
     }
   }
 
 }
