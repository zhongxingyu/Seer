 /*
  * Created on Oct 13, 2004
  */
 package uk.org.ponder.rsf.util;
 
 import uk.org.ponder.rsf.components.ComponentList;
 import uk.org.ponder.rsf.components.ELReference;
 import uk.org.ponder.rsf.components.ParameterList;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIParameter;
 import uk.org.ponder.rsf.components.UIParameterHolder;
 import uk.org.ponder.rsf.view.ViewRoot;
 import uk.org.ponder.util.AssertionException;
 
 /**
  * Low-level utilities for working on RSF component trees.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class RSFUtil {
   /**
    * This method returns an enclosing Form instance, where one is present in the
    * tree. Note that this only makes sense for HTML-style forms, and is a coding
    * convenience. For alternative form models, client code must keep records of
    * the UIForm instance themselves.
    */
   public static UIForm findBasicForm(UIContainer tocheck) {
     while (tocheck != null) {
       if (tocheck instanceof UIForm)
         return (UIForm) tocheck;
       tocheck = tocheck.parent;
     }
     return null;
   }
 
   /**
    * This method returns an enclosing ViewRoot instance, where one is present in
    * the tree (it should be, in every well-formed tree)
    */
   public static ViewRoot findViewRoot(UIComponent tofind) {
     while (tofind != null) {
       if (tofind instanceof ViewRoot)
         return (ViewRoot) tofind;
       tofind = tofind.parent;
     }
     return null;
   }
 
   /**
    * A convenience method that assumes the BasicFormModel (uses findBasicForm
    * above). Adds the supplied name/value pair to the nearest enclosing form
    * control.
    */
 
   public static void addBasicFormParameter(UIContainer local, UIParameter toadd) {
     UIForm enclosing = findBasicForm(local);
     if (enclosing == null) {
       throw new AssertionException("Component " + local.getFullID()
           + " has no form parent!");
     }
     enclosing.parameters.add(toadd);
   }
 
   /** Adds a "deferred resulting view" EL binding to the supplied UIComponent. 
    * This will transfer a value from an EL path in the request context to a
    * particular path withing the outgoing ViewParameters state for the coming 
    * action cycle, assuming that it completes without error.
    */
  
   public static void addResultingViewBinding(UIParameterHolder holder, String viewParamsPath, String requestPath) {
     if (holder.parameters == null) {
       holder.parameters = new ParameterList();
     }
    ELReference vpp = new ELReference(viewParamsPath);
    holder.parameters.add(new UIELBinding("ARIResult.resultingView."+vpp.value, 
         new ELReference(requestPath)));
   }
   
   /**
    * Adds a binding to the supplied parameter list that will assign the EL
    * expression <code>source</code> to <code>transit</code> and then
    * <code>transit</code> to <code>dest</code>, a classic usage of
    * "whole-object validation through transit".
    */
   public static void addTransitBinding(ParameterList paramlist, String source,
       String transit, String dest) {
     paramlist.add(new UIELBinding(transit, new ELReference(source)));
     paramlist.add(new UIELBinding(dest, new ELReference(transit)));
   }
 
   /**
    * Determines whether the supplied component has a bound value, and hence will
    * be visible to fossilized bindings/RSVC processing. It must not only be an
    * instance of {@link UIBound}, but also have a non-null binding.
    */
   public static boolean isBound(UIComponent tocheck) {
     return tocheck instanceof UIBound ? ((UIBound) tocheck).valuebinding != null
         : false;
   }
 
   // PROFILER hotspot: 1.5% of request render time.
   public static String getFullIDSegment(String ID, String localID) {
     return SplitID.getPrefix(ID) + SplitID.SEPARATOR + localID
         + SplitID.SEPARATOR;
   }
 
   /**
    * See {@link UIComponent} for the operation of this algorithm.
    */
   // PROFILER hotspot: 2.4% render request time
   public static String computeFullID(UIComponent tocompute) {
     StringBuffer togo = new StringBuffer();
     UIContainer move = null;
     if (!(tocompute instanceof UIBranchContainer)) {
       // the tail part of an ID is always the component's leaf ID itself.
       togo.insert(0, tocompute.ID);
       move = tocompute.parent;
     }
     else {
       move = (UIContainer) tocompute;
     }
     while (move.parent != null) { // ignore the top-level viewroot Branch
       if (!move.noID) {
         togo.insert(0, getFullIDSegment(move.ID, move.localID));
       }
       move = move.parent;
     }
     return togo.toString();
   }
 
   public static String reportPath(UIComponent branch) {
     String path = branch.getFullID();
     return path.equals("") ? "component tree root"
         : "full path " + path;
   }
 
   /** Returns the common ancestor path of s1 and s2 * */
   public static int commonPath(String s1, String s2) {
     int s1c = s1.lastIndexOf(':');
     int s2c = s2.lastIndexOf(':');
     int i = 0;
     while (true) {
       if (i > s1c || i > s2c)
         break;
       if (s1.charAt(i) != s2.charAt(i))
         break;
       ++i;
     }
     return i;
   }
 
   public static ComponentList getRootPath(UIComponent leaf) {
     ComponentList togo = new ComponentList();
     while (leaf != null) {
       togo.add(0, leaf);
       leaf = leaf.parent;
     }
     return togo;
   }
 
   public static void failRemove(UIComponent failed) {
     throw new IllegalArgumentException("Tried to remove " + failed.getFullID()
         + " which is not a child of this container");
   }
 
 }
