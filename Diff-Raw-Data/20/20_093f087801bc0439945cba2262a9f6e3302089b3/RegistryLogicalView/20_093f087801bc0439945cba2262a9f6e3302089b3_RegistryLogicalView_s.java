 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.registry;
 
 import org.netbeans.spi.project.ui.LogicalViewProvider;
 
 import org.openide.nodes.Node;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public class RegistryLogicalView implements LogicalViewProvider {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient RegistryProject project;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new RegistryLogicalView object.
      *
      * @param  project  DOCUMENT ME!
      */
     public RegistryLogicalView(final RegistryProject project) {
         this.project = project;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Node findPath(final Node node, final Object object) {
         return null;
     }
 
     @Override
     public Node createLogicalView() {
        return new RegistryProjectNode(project);
     }
 }
