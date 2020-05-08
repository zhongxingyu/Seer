 /*
  * Created on 24-May-2006
  */
 package uk.org.ponder.rsf.renderer.scr;
 
 import uk.org.ponder.rsf.renderer.ComponentRenderer;
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.xml.XMLWriter;
 
 /** Default null SCR used on discovering an unrecognised SCR tag.
  * Minimal behaviour of writing the existing open tag, treating it as a 
  * trunk tag.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  *
  */
 
 public class IgnoreRewriteSCR implements BasicSCR {
   
   public static final BasicSCR instance = new IgnoreRewriteSCR();
   
   public String getName() {
     return "ignore";
   }
 
   public int render(XMLLump lump, XMLWriter xmlw) {
     return ComponentRenderer.LEAF_TAG;
   }
 
 }
