 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.librarysupport.project;
 
 import org.netbeans.spi.project.ui.LogicalViewProvider;
 
 import org.openide.nodes.Node;
 import org.openide.util.lookup.Lookups;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  1.5
  */
 public final class LibrarySupportLogicalView implements LogicalViewProvider {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient LibrarySupportProject project;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of LibrarySupportLogicalView.
      *
      * @param  project  DOCUMENT ME!
      */
     public LibrarySupportLogicalView(final LibrarySupportProject project) {
         this.project = project;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Node findPath(final Node node, final Object object) {
         return null;
     }
 
     @Override
     public Node createLogicalView() {
        final LibrarySupportProjectNode view = new LibrarySupportProjectNode(
                project);
        project.addLookup(Lookups.fixed(view));
         return view;
     }
 }
