 /*
  * Created on Oct 13, 2004
  */
 package uk.org.ponder.rsf.util;
 
 import uk.org.ponder.rsf.components.ComponentList;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIParameter;
 import uk.org.ponder.stringutil.CharWrap;
 import uk.org.ponder.util.AssertionException;
 
 /**
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  *  
  */
 public class RSFUtil {
   
   /** This method returns an enclosing Form instance, where one is present
    * in the tree. Note that this only makes sense for HTML-style forms, and
    * is a coding convenience. For alternative form models, client code must
    * keep records of the UIForm instance themselves.
    */
   public static UIForm findBasicForm(UIContainer tocheck) {
     while (tocheck != null) {
       if (tocheck instanceof UIForm) return (UIForm) tocheck;
       tocheck = tocheck.parent;
     }
     return null;
   }
   
   /** A convenience method that assumes the BasicFormModel (uses findBasicForm above).
    * Adds the supplied name/value pair to the nearest enclosing form control.
    */
   
   public static void addBasicFormParameter(UIContainer local, UIParameter toadd) {
     UIForm enclosing = findBasicForm(local);
     if (enclosing == null) {
       throw new AssertionException("Component " + local.getFullID() + " has no form parent!");
     }
     enclosing.parameters.add(toadd);
   }
   
   /** Determines whether the supplied component has a bound value, and hence
    * will be visible to fossilized bindings/RSVC processing. It must not only
    * be an instance of @see UIBound, but also have a non-null binding.
    */
   public static boolean isBound(UIComponent tocheck) {
     return tocheck instanceof UIBound? ((UIBound)tocheck).valuebinding != null : false;
   }
   // PROFILER hotspot: 1.5% of request render time.
   public static String getFullIDSegment(String ID, String localID) {
     return ID + SplitID.SEPARATOR + localID + SplitID.SEPARATOR;
   }
   
   /** @see UIComponent for the operation of this algorithm.
    */
   // PROFILER hotspot: 2.4% render request time
   public static String computeFullID(UIComponent tocompute) {
     StringBuffer togo = new StringBuffer();
     UIComponent move = tocompute;
     if (!(move instanceof UIBranchContainer)) {
       togo.insert(0, move.ID); // the tail part of an ID is always the component's
                              // leaf ID itself.
     }
     while (move.parent != null) { // ignore the top-level viewroot Branch 
       
       if (move instanceof UIBranchContainer) {
         // only insert naming section for concrete container. 
         //if (move.getClass() == UIBranchContainer.class) {
           UIBranchContainer movec = (UIBranchContainer)move;
           togo.insert(0, getFullIDSegment(movec.ID.toString(), movec.localID));
         //}
       }
       move = move.parent;
     }
     return togo.toString();
   }
   
  /** Returns the common ancestor path of s1 and s2 **/
  public static int commonPath(String s1, String s2) {
    int s1c = s1.lastIndexOf(':');
    int s2c = s2.lastIndexOf(':');
    int i = 0;
    while (true) {
      if (i >= s1c || i >= s2c) break;
      if (s1.charAt(i) != s2.charAt(i)) break;
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
   
 /** "Reduce" a component full ID by stripping out the local ID segments. Now disused. */
   public static String getReducedID(String fullID) {
     String[] components = fullID.split(":");
     CharWrap toappend = new CharWrap();
     for (int i = 0; i < components.length; ++ i) {
       if (i%3 != 2) {
         toappend.append(components[i]);
         // ID can either end on %0 for a container, or on %1 for a leaf. 
         // So if it is a container, we will retain the :.
         if (i != components.length - 1) {
           toappend.append(':');
         }
       }
     }
     return toappend.toString();
   }
 
 }
