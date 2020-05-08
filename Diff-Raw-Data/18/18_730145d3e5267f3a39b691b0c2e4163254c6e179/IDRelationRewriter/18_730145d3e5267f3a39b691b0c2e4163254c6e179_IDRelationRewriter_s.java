 /*
  * Created on 12 May 2008
  */
 package uk.org.ponder.rsf.renderer.html;
 
 import java.util.Map;
 
 import uk.org.ponder.htmlutil.HTMLConstants;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.renderer.TagRenderContext;
 import uk.org.ponder.rsf.template.TemplateParseInterceptor;
 import uk.org.ponder.rsf.template.XMLLump;
 
 public class IDRelationRewriter implements TemplateParseInterceptor {
   public static final String NAME = "rewrite-idrel";
 
   public String getName() {
     return NAME;
   }
 
   private static String getRelationAttr(XMLLump lump) {
     for (int i = 0; i < HTMLConstants.idRelation.length; ++i) {
       String[] tags = HTMLConstants.idRelation[i];
       String tag = tags[0];
       for (int j = 1; j < tags.length; ++j) {
         if (lump.textEquals(tags[j]))
           return tag;
       }
     }
     return null;
   }
 
   public void rewrite(Map rewritemap, TagRenderContext trc, UIContainer context) {
     String attr = getRelationAttr(trc.uselump);
     String val = (String) trc.attrcopy.get(attr);
     if (val != null) {
       String rewritten = (String) rewritemap.get(
           RenderUtil.getRewriteKey(trc.uselump.parent, context, val));
       if (rewritten != null) {
         trc.attrcopy.put(attr, rewritten);
       }
     }
 
   }
 
   public void adjustAttributes(String tag, Map attributes) {
     // cheap but overeager test stuffs in a null rewrite SCR just to make sure
     // the parser will pause, the rewriter is actually applied uniformly to
     // every rsf:id tag.
     if ((attributes.containsKey("for") || attributes.containsKey("headers"))
         && !attributes.containsKey(XMLLump.ID_ATTRIBUTE)) {
      attributes.put(XMLLump.ID_ATTRIBUTE, "null");
     }
   }
 
 }
