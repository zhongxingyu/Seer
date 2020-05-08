 /*
  * Created on Jul 27, 2005
  */
 package uk.org.ponder.rsf.components;
 
 /**
  * This component peers with a simple navigational link. Operating this link is
  * expected to give rise to an idempotent ("render") request. The basic case
  * where the command is rendered using a piece of non-bound text is handled by
  * filling in the <code>linktext</code> field. For more complex command
  * contents including bound ones, leave <code>linktext</code> as null and add
  * rendering components as childen of the link in the template.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 public class UILink extends UIComponent {
   /**
    * A string representing the target of this link - e.g. in an HTML system, a
    * URL. In HTML in particular, the special prefix $context/ will be resolved
    * onto the context for the current webapp - that is, the directory
    * immediately above WEB-INF.
    * 
    * <p/> For an InternalLink this will be filled in by a fixup from the
    * ViewParameters member.
    */
   public UIBoundString target;
 
   /** A bound String representing any text to be rendered for this control * */
   public UIBoundString linktext;
 
   /** Construct a navigation link.
    * @param parent Container to which the link is to be added.
    * @param ID RSF ID of this link.
    * @param text Text to be rendered for this link.
    * @param target @see {@link #target}
    */
   public static UILink make(UIContainer parent, String ID, String text,
       String target) {
     UIBoundString linktext = null;
     if (text != null) {
       linktext = new UIOutput();
       linktext.setValue(text);
     }
     return make(parent, ID, linktext, target);
   }
 
   /** Construct a navigation link with a bound control (e.g. UIMessage) forming
    * the link text.
    */
   public static UILink make(UIContainer parent, String ID,
       UIBoundString linktext, String target) {
     UILink togo = new UILink();
     togo.ID = ID;
     togo.target = new UIOutput();
     if (target != null) {
       togo.target.setValue(target);
     }
     togo.linktext = linktext;
     parent.addComponent(togo);
     return togo;
   }
 
   /** Construct a navigation link which will leave nested markup unchanged
    * from the template.
    */
   public static UILink make(UIContainer parent, String ID, String target) {
     return make(parent, ID, (UIBoundString) null, target);
   }
 
 }
