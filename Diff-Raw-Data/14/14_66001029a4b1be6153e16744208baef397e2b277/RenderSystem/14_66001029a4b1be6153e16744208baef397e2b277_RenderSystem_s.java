 /*
  * Created on Jul 27, 2005
  */
 package uk.org.ponder.rsf.renderer;
 
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.renderer.decorator.DecoratorManager;
 import uk.org.ponder.rsf.renderer.scr.StaticRendererCollection;
 import uk.org.ponder.rsf.request.RenderSystemDecoder;
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.rsf.template.XMLLumpMMap;
 import uk.org.ponder.rsf.view.View;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 
 /**
  * A Render System encapsulates all operations that are specific to a
  * particular rendering technology, e.g. HTML.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public interface RenderSystem extends RenderSystemDecoder {
   /** Invoked by the IKAT renderer in order to perform a template rewrite based
    * on the System XML dialect. 
    * @param torender The UIComponent which IKAT determines is to be 
    * corresponded with the specified tag. The ID of this component will match
    * (at least in prefix) the rsf:id of tag specified in lumpindex. The 
    * component may be <code>null</code> where the tag is to be target of a 
    * static rewrite rule, in which case the System will invoke the relevant SCR.
    * @param view The view currently being rendered - necessary to resolve
    * any inter-component references.
    * @param lump The lump (head lump holding open tag) where
    * the tag appears within the template requiring rewrite.
    * @param pos The output stream where the transformed template data is to be
    * written.
    * @param idassigner An {@link IDAssigner} object to be used for assigning 
    * a value to outgoing XML <code>id</code> attributes.
    * @return The render index that the renderer has moved along to.
    */
   public int renderComponent(UIComponent torender, View view, XMLLump lump, 
       PrintOutputStream pos, IDAssigner idassigner, XMLLumpMMap collected);
 
 
   public void setStaticRenderers(StaticRendererCollection scrc);
 
   public void setDecoratorManager(DecoratorManager decoratormanager);
 }
