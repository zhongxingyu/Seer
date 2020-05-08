 package pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer;
 
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.html.tree.ITreeStateListener;
 import org.apache.wicket.model.PropertyModel;
 import org.purl.wf4ever.rosrs.client.Folder;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.Thing;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import pl.psnc.dl.wf4ever.portal.model.RoTreeModel;
 import pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer.components.FilesPanel;
 import pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer.components.RoTree;
 import pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer.components.behaviours.FolderViewerAjaxInitialBahaviour;
 import pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer.components.behaviours.ITreeListener;
 import pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer.components.forms.FilesShiftForm;
 import pl.psnc.dl.wf4ever.portal.ui.behaviours.Loadable;
 import pl.psnc.dl.wf4ever.portal.ui.components.LoadingCircle;
 
 /**
  * Basic RO files and folders viewer
  * 
  * @author pejot
  * 
  */
 public class FoldersViewer extends Panel implements Loadable, ITreeStateListener, ITreeListener {
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(FoldersViewer.class);
 
     /** Makes wicket happy. */
     private static final long serialVersionUID = 1L;
 
     /** Tree component. */
     private RoTree roTree;
 
     /** Loading image. */
     private LoadingCircle loadingCircle;
 
     /** Research object. */
     private ResearchObject researchObject;
 
     /** Loading information. */
     private static final String LOADING_OBJECT = "Loading Research Object metadata.<br />Please wait...";
 
     /** The model of RO resource. */
     private RoTreeModel roTreeModel;
 
     private Folder selectedFolder;
     private PropertyModel<List<Thing>> foldersModel;
     /** Failes panel */
     private FilesPanel filesPanel;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param model
      *            tree model
      */
     public FoldersViewer(String id, ResearchObject researchObject) {
         super(id);
         //setting variables
         this.researchObject = researchObject;
         this.selectedFolder = null;
         this.foldersModel = new PropertyModel<List<Thing>>(this, "selectedFolder.resources");
         //building UI
 
         roTree = new RoTree("ro-tree", new PropertyModel<TreeModel>(this, "roTreeModel"));
         roTree.getTreeState().addTreeStateListener(this);
         roTree.addTreeListeners(this);
         loadingCircle = new LoadingCircle("ro-tree", LOADING_OBJECT);
         filesPanel = new FilesPanel("files-panel", foldersModel);
         add(loadingCircle);
         add(new FilesShiftForm("form"));
         //init in the background
         add(new FolderViewerAjaxInitialBahaviour(researchObject, this));
         add(filesPanel);
     }
 
 
     public Folder getSelctedFolder() {
         return selectedFolder;
     }
 
 
     @Override
     public void onLoaded(Object data) {
         setRoTreeModel((RoTreeModel) data);
         loadingCircle.replaceWith(roTree);
     }
 
 
     public RoTreeModel getRoTreeModel() {
         return roTreeModel;
     }
 
 
     public void setRoTreeModel(RoTreeModel roTreeModel) {
         this.roTreeModel = roTreeModel;
     }
 
 
     @Override
     public void nodeSelected(Object node) {
         //folder
         Thing object = (Thing) ((DefaultMutableTreeNode) node).getUserObject();
         if (object instanceof Folder) {
             this.selectedFolder = (Folder) object;
             if (!this.selectedFolder.isLoaded()) {
                 try {
                     this.selectedFolder.load(false);
                 } catch (ROSRSException e) {
                     LOG.error("Folfer " + object.getUri().toString() + " can not be loaded", e);
                     this.selectedFolder = null;
                 }
             }
         } else {
             this.selectedFolder = null;
         }
     }
 
 
     @Override
     public void nodeUnselected(Object node) {
     }
 
 
     @Override
     public void allNodesCollapsed() {
     }
 
 
     @Override
     public void allNodesExpanded() {
     }
 
 
     @Override
     public void nodeCollapsed(Object node) {
     }
 
 
     @Override
     public void nodeExpanded(Object node) {
     }
 
 
     @Override
    public void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
         target.add(filesPanel);
     }
 }
