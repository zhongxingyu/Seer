 /*
  * Created on 18 Sep 2006
  */
 package uk.org.ponder.rsf.renderer.html;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import uk.org.ponder.rsf.renderer.ComponentRenderer;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.renderer.scr.CollectingSCR;
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.rsf.template.XMLLumpList;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.xml.XMLWriter;
 
 /**
  * A basic collector of &lt;head&gt; material for HTML pages. Will emit all
  * collected &lt;style&gt; and &lt;script&gt; tags, and leave the tag in an open
  * condition.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class HeadCollectingSCR implements CollectingSCR {
   public static final String NAME = "head-collect";
   private URLRewriteSCR urlRewriteSCR;
 
   public String getName() {
     return NAME;
   }
 
   public String[] getCollectingNames() {
     return new String[] { "style", "script" };
   }
 
   public void setURLRewriteSCR(URLRewriteSCR urlRewriteSCR) {
     this.urlRewriteSCR = urlRewriteSCR;
   }
 
   public int render(XMLLump lump, XMLLumpList collected, XMLWriter xmlw) {
     PrintOutputStream pos = xmlw.getInternalWriter();
     RenderUtil.dumpTillLump(lump.parent.lumps, lump.lumpindex,
         lump.open_end.lumpindex + 1, pos);
     Set used = new HashSet();
     for (int i = 0; i < collected.size(); ++i) {
       XMLLump collump = collected.lumpAt(i);
       String attr = URLRewriteSCR.getLinkAttribute(collump);
       if (attr != null) {
         String attrval = (String) collump.attributemap.get(attr);
         if (attrval != null) {
           String rewritten = urlRewriteSCR.resolveURL(collump.parent, attrval);
           int qpos = rewritten.indexOf('?');
           if (qpos != -1)
             rewritten = rewritten.substring(0, qpos);
           if (used.contains(rewritten))
             continue;
           else
             used.add(rewritten);
         }
       }
       // TODO: equivalent of TagRenderContext for SCRs
       urlRewriteSCR.render(collump, xmlw);
       RenderUtil.dumpTillLump(collump.parent.lumps,
           collump.open_end.lumpindex + 1, collump.close_tag.lumpindex + 1, pos);
     }
     return ComponentRenderer.NESTING_TAG;
   }
 
 }
