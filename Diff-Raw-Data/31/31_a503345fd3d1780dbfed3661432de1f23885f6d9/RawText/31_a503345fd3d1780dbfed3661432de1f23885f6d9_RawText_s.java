 package com.wideplay.warp.components.core;
 
 import com.google.inject.Injector;
 import com.wideplay.warp.annotations.Component;
 import com.wideplay.warp.module.componentry.Renderable;
 import com.wideplay.warp.module.pages.PageClassReflection;
 import com.wideplay.warp.rendering.ComponentHandler;
 import com.wideplay.warp.rendering.HtmlWriter;
 import com.wideplay.warp.rendering.PageRenderException;
 import com.wideplay.warp.util.Token;
 import ognl.Ognl;
 import ognl.OgnlException;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * On: 17/03/2007
  *
  * A special case component that renders hanging bits of text in the template.
  *
  * @author Dhanji R. Prasanna
  * @since 1.0
  */
 @Component
 public class RawText implements Renderable, AttributesInjectable {
     private Map<String, Object> attributeNameValuePairs;
 
     public static final String WARP_RAW_TEXT_PROP_TOKENS = "warpRawTextTokens";
     public static final String WARP_RAW_TEXT_PROP_TAG = "warpRawTextTag";
     public static final String WARP_RAW_TEXT_PROP_ATTRS = "warpRawTextAttributes";
 
     public void render(HtmlWriter writer, List<? extends ComponentHandler> nestedComponents, Injector injector, PageClassReflection reflection, Object page) {
         Object[] attributes = null;
         String warpRawTextTag = null;
         List<Token> tokens = null;
         if (null != attributeNameValuePairs) {
             attributes = (Object[]) attributeNameValuePairs.get(WARP_RAW_TEXT_PROP_ATTRS);
             warpRawTextTag = (String) attributeNameValuePairs.get(WARP_RAW_TEXT_PROP_TAG);
             tokens = (List<Token>) attributeNameValuePairs.get(WARP_RAW_TEXT_PROP_TOKENS);
         }
 
         //self closed tags if there is no text to print
         if (nestedComponents.isEmpty() && null != warpRawTextTag) {
             writer.selfClosedElementWithAttrs(warpRawTextTag, attributes);
             return;
         }
 
         //print around tags if necessary
         if (null != warpRawTextTag)
             writer.elementWithAttrs(warpRawTextTag, attributes);
 
         //iterate and write tokens to output, parsing exprs as necessary
         if (null != tokens) {
             for (Token token : tokens) {
                 if (token.isExpression()) {
                     try {
                        writer.writeRaw(Ognl.getValue(token.getToken(), page).toString());
                     } catch (OgnlException e) {
                         throw new PageRenderException("Error while evaluating expression: " + token.getToken(), e);
                     }
                 } else
                     writer.writeRaw(token.getToken());
             }
         }
 
 
         //render children now (not sure exactly how the order works?!) --seems to work ok
         ComponentSupport.renderMultiple(writer, nestedComponents, injector, reflection,
                 page);
 
         //close it up
         if (null != warpRawTextTag)
             writer.end(warpRawTextTag);
     }
 
 
     public void setAttributeNameValuePairs(Map<String, Object> attributeNameValuePairs) {
         this.attributeNameValuePairs = attributeNameValuePairs;
     }
 }
