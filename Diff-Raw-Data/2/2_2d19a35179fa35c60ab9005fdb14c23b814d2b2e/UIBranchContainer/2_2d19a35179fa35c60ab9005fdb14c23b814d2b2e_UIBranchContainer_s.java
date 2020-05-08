 /*
  * Created on Aug 8, 2005
  */
 package uk.org.ponder.rsf.components;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import uk.org.ponder.rsf.util.RSFUtil;
 import uk.org.ponder.rsf.util.SplitID;
 import uk.org.ponder.stringutil.CharWrap;
 
 /**
  * UIBranchContainer represents a "branch point" in the IKAT rendering process,
  * rather than simply just a level of component containment.
  * <p>
  * UIBranchContainer has responsibility for managing naming of child components,
  * as well as separate and parallel responsibility for forms. The key to the
  * child map is the ID prefix - if the ID has no suffix, the value is the single
  * component with that ID at this level. If the ID has a suffix, indicating a
  * repetitive domain, the value is an ordered list of components provided by the
  * producer which will drive the rendering at this recursion level.
  * <p>
  * It is assumed that an ID prefix is globally unique within the tree, not just
  * within its own recursion level - i.e. IKAT resolution takes place over ALL
  * components sharing a prefix throughout the template. This is "safe" since
  * "execution" will always return to the call site once the base (XML) nesting
  * level at the target is reached again.
  * <p>
  * "Leaf" rendering classes <it>may</it> be derived from UISimpleContainer -
  * only concrete instances of UIBranchContainer will be considered as
  * representatives of pure branch points. By the time fixups have concluded, all
  * non-branching containers (e.g. UIForms) MUST have been removed from non-leaf
  * positions in the component hierarchy.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 public class UIBranchContainer extends UIContainer {
   /**
    * The localID allows clients to distinguish between multiple instantiations
    * of the "same" (by rsf:id) component within the same scope. It forms part of
    * the global path constructed by getFullID() which uniquely identifies the
    * component.
    */
   public String localID = "";
   // This is a map to either the single component with a given ID prefix, or a
   // list in the case of a repetitive domain (non-null suffix)
   private Map childmap = new HashMap();
 
   // this is created by the first call to flatChildren() which is assumed to
   // occur during the render phase. Implicit model that component tree is
   // i) constructed, ii) rendered, iii) discarded.
   // It is worth caching this since it is iterated over up to 4n times during
   // rendering, for each HTMLLump headlump that matches the requested call
   // in the 4 scopes.
   private transient UIComponent[] flatchildren;
 
   /**
    * Constructs a "repeating" BranchContainer, uniquely identified by the
    * "localID" passed as the 3rd argument. Suitable, for example, for creating a
    * table row.
    * 
    * @param parent The parent container to which the returned branch should be
    *          added.
    * @param ID The RSF ID for the branch (must contain a colon character)
    * @param localID The local ID identifying this branch instance (must be
    *          unique for each branch with the same ID in this branch)
    */
   public static UIBranchContainer make(UIContainer parent, String ID,
       String localID) {
     if (ID.indexOf(':') == -1) {
       throw new IllegalArgumentException(
           "Branch container ID must contain a colon character :");
     }
     UIBranchContainer togo = new UIBranchContainer();
     togo.ID = ID;
     togo.localID = localID;
     parent.addComponent(togo);
     return togo;
   }
 
   /**
    * Constructs a simple BranchContainer, used to group components or to cause a
    * rendering switch. Suitable where there will be just one branch with this ID
    * within its container. Where BranchContainers are created in a loop, supply
    * a localID by using the {@link #make(UIContainer, String, String)}
    * constructor.
    * 
    * @see #make(UIContainer, String, String)
    */
   public static UIBranchContainer make(UIContainer parent, String ID) {
     return make(parent, ID, "");
   }
 
   /**
    * Return the single component with the given ID. This should be an ID without
    * colon designating a leaf child.
    */
   public UIComponent getComponent(String id) {
     if (childmap == null)
       return null;
     Object togo = childmap.get(id);
     if (togo != null && !(togo instanceof UIComponent)) {
       throw new IllegalArgumentException(
           "Error in view tree: component with id " + id
               + " was expected to be a leaf component but was a branch."
               + "\n (did you forget to use a colon in the view template?)");
     }
     return (UIComponent) togo;
   }
 
   /**
    * Return all child components with the given prefix. This should be an ID
    * containing colon designating a child container.
    */
   public List getComponents(String id) {
     Object togo = childmap.get(id);
     if (togo != null && !(togo instanceof List)) {
       throw new IllegalArgumentException(
           "Error in view tree: component with id " + id
               + " was expected to be a branch container but was a leaf."
               + "\n (did you forget to use a colon in the component ID?)");
     }
     return (List) togo;
   }
 
   public String debugChildren() {
     CharWrap togo = new CharWrap();
     togo.append("Child IDs: (");
     UIComponent[] children = flatChildren();
     for (int i = 0; i < children.length; ++i) {
       if (i != 0) {
         togo.append(", ");
       }
       togo.append(children[i].ID);
     }
     togo.append(")");
     return togo.toString();
   }
 
   /**
    * Returns a flattened array of all children of this container. Note that this
    * method will trigger the creation of a cached internal array on its first
    * use, which cannot be recreated. It is essential therefore that it only be
    * used once ALL modifications to the component tree have concluded (i.e. once
    * rendering starts).
    */
   public UIComponent[] flatChildren() {
     if (flatchildren == null) {
       ComponentList children = flattenChildren();
       flatchildren = (UIComponent[]) children.toArray(new UIComponent[children
           .size()]);
     }
     return flatchildren;
   }
 
   /**
    * Returns a list of all CURRENT children of this container. This method is
    * safe to use at any time.
    */
   // There are now two calls to this in the codebase, firstly from ViewProcessor
   // and then from BasicFormFixer. The VP call is necessary since it needs to
   // fossilize
   // the list up front, but if another call arises as in BFF we ought to write a
   // multi-iterator.
   public ComponentList flattenChildren() {
     ComponentList children = new ComponentList();
     for (Iterator childit = childmap.values().iterator(); childit.hasNext();) {
       Object child = childit.next();
       if (child instanceof UIComponent) {
         children.add(child);
       }
       else if (child instanceof List) {
         children.addAll((List) child);
       }
     }
     return children;
   }
 
   /** Add a component as a new child of this container */
 
   public void addComponent(UIComponent toadd) {
     toadd.parent = this;
 
     SplitID split = new SplitID(toadd.ID);
     String childkey = split.prefix;
     if (toadd.ID != null && split.suffix == null) {
       childmap.put(childkey, toadd);
     }
     else {
       List children = (List) childmap.get(childkey);
       if (children == null) {
         children = new ArrayList();
         childmap.put(childkey, children);
       }
      else if (toadd instanceof UIBranchContainer) {
         UIBranchContainer addbranch = (UIBranchContainer) toadd;
         if (addbranch.localID == "") {
           throw new IllegalArgumentException(
               "Error in component tree: duplicate branch added with full ID " + addbranch.getFullID() 
               +" - make sure to use the 3-argument UIBranchContainer.make() method when creating branch containers in a loop");
  // We can't adjust this here in general since when UIBranchContainers are added to a form,
  // their IDs will 
  //         addbranch.localID = Integer.toString(children.size());
         }
       }
       children.add(toadd);
     }
   }
 
   public void remove(UIComponent tomove) {
     SplitID split = new SplitID(tomove.ID);
     String childkey = split.prefix;
     if (split.suffix == null) {
       Object tomovetest = childmap.remove(childkey);
       if (tomove != tomovetest) {
         RSFUtil.failRemove(tomove);
       }
     }
     else {
       List children = (List) childmap.get(childkey);
       if (children == null) {
         RSFUtil.failRemove(tomove);
       }
       boolean removed = children.remove(tomove);
       if (!removed)
         RSFUtil.failRemove(tomove);
     }
     tomove.updateFullID(null); // remove cached ID
   }
 
 }
