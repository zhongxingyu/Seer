 /*
  * Created on 18 Sep 2006
  */
 package uk.org.ponder.rsf.renderer.html;
 
 import java.util.Map;
 
 import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
 import uk.org.ponder.rsf.template.ContentTypedTPI;
 import uk.org.ponder.rsf.template.XMLLump;
 
 public class HeadInferringTPI implements ContentTypedTPI {
 
   public String[] getInterceptedContentTypes() {
     return new String[] { ContentTypeInfoRegistry.HTML,
         ContentTypeInfoRegistry.HTML_FRAGMENT };
   }
 
   public void adjustAttributes(String tag, Map attributes) {
    if (tag.equals("head") && attributes.get(XMLLump.ID_ATTRIBUTE) == null) {
       attributes.put(XMLLump.ID_ATTRIBUTE, XMLLump.SCR_PREFIX
           + HeadCollectingSCR.NAME);
     }
   }
 }
