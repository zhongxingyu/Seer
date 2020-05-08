 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.client;
 
import org.netbeans.spi.project.ui.support.CommonProjectActions;

 import org.openide.nodes.FilterNode;
 import org.openide.nodes.Node;
 import org.openide.util.ImageUtilities;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.Lookups;
 import org.openide.util.lookup.ProxyLookup;
 
 import java.awt.Image;
 
import javax.swing.Action;

 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public final class ClientProjectNode extends FilterNode {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Image icon;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ClientProjectNode object.
      *
      * @param  node     DOCUMENT ME!
      * @param  project  DOCUMENT ME!
      */
     public ClientProjectNode(final Node node, final ClientProject project) {
         super(
             node,
             new FilterNode.Children(node),
             // The projects system wants the project in the Node's lookup.
             // NewAction and friends want the original Node's lookup.
             // Make a merge of both
             new ProxyLookup(
                 new Lookup[] {
                     Lookups.singleton(project),
                     node.getLookup()
                 }));
         icon = ImageUtilities.loadImage(ClientProject.IMAGE_FOLDER + "client.png"); // NOI18N
         setDisplayName(project.getProjectDirectory().getName() + " [cidsClient]");  // NOI18N
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Image getIcon(final int type) {
         return icon;
     }
 
     @Override
     public Image getOpenedIcon(final int type) {
         return icon;
     }

    @Override
    public Action[] getActions(final boolean context) {
        return new Action[] {
                CommonProjectActions.customizeProjectAction(),
                null,
                CommonProjectActions.closeProjectAction()
            };
    }
 }
