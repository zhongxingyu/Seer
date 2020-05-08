 /*
  * Created on May 11, 2006
  */
 package uk.org.ponder.rsf.renderer;
 
 import java.util.Map;
 
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.xml.XMLWriter;
 
 /** The context necessary for a ComponentRenderer to do its work */
 
 public class TagRenderContext {
  public boolean isEmpty() {
    return endopen.lumpindex == close.lumpindex;
  }
   public Map attrcopy;
   public XMLLump uselump;
   public XMLLump endopen;
   public XMLLump close;
   public PrintOutputStream pos;
   public XMLWriter xmlw;
 
   public int nextpos;
   
   public TagRenderContext(Map attrcopy, XMLLump uselump,
       XMLLump endopen, XMLLump close, PrintOutputStream pos, XMLWriter xmlw, int nextpos) {
     this.attrcopy = attrcopy;
     this.uselump = uselump;
     this.endopen = endopen;
     this.close = close;
     this.pos = pos;
     this.xmlw = xmlw;
     this.nextpos = nextpos;
   }
 
 }
