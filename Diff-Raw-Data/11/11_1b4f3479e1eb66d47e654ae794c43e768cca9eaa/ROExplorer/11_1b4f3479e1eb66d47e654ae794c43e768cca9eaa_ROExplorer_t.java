 package pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.form.IFormSubmitListener;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.html.tree.ITreeStateListener;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.purl.wf4ever.rosrs.client.Folder;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.Resource;
 import org.purl.wf4ever.rosrs.client.Thing;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import pl.psnc.dl.wf4ever.portal.model.RoTreeModel;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.behaviours.IAjaxLinkListener;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.behaviours.ITreeListener;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.behaviours.ROExplorerAjaxInitialBehaviour;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.components.FilesPanel;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.components.ROButtonsBar;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.components.ROStatusBar;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.components.ResourceButtonsBar;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.components.ResourceStatusBar;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.components.RoTree;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.forms.FilesShiftForm;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.models.TreeNodeContentModel;
 import pl.psnc.dl.wf4ever.portal.ui.behaviours.Loadable;
 import pl.psnc.dl.wf4ever.portal.ui.components.LoadingCircle;
 
 /**
  * Basic RO files and folders viewer.
  * 
  * @author pejot
  * 
  */
 public class ROExplorer extends Panel implements Loadable, ITreeStateListener, ITreeListener, IFormSubmitListener,
         IAjaxLinkListener {
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(ROExplorer.class);
     /** Makes wicket happy. */
     private static final long serialVersionUID = 1L;
 
     /** Failes panel. */
     private FilesPanel filesPanel;
     /** Tree component. */
     private RoTree roTree;
     /** Hidden form. */
     private FilesShiftForm filesShiftForm;
     /** Loading image. */
     private LoadingCircle loadingCircle;
     /** Info panel. */
     private ResourceStatusBar itemInfoPanel;
     /** Buttons panel. */
     private ResourceButtonsBar buttonsBar;
     /** Status bar of processed research object. */
     private ROStatusBar roStatusBar;
     /** RO buttons bar. */
     private ROButtonsBar roButtonsBar;
     /** Research object. */
     private ResearchObject researchObject;
     /** The model of RO resource. */
     private RoTreeModel roTreeModel;
     /** Folders model. */
     private TreeNodeContentModel foldersModel;
     /** Listeners for the selected resource. */
     private List<IAjaxLinkListener> listeners = new ArrayList<>();
 
     /** Loading information. */
     private static final String LOADING_OBJECT = "Loading Research Object metadata.<br />Please wait...";
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param researchObject
      *            Research Object
      * @param itemModel
      */
     public ROExplorer(String id, ResearchObject researchObject, IModel<Thing> itemModel) {
         // Last clicked item (file or Folder). 
         super(id, itemModel);
         //setting variables
         this.researchObject = researchObject;
 
         this.foldersModel = new TreeNodeContentModel(new PropertyModel<Thing>(this, "currentlySelectedItem"),
                 researchObject);
         //building UI
         roTree = new RoTree("ro-tree", new PropertyModel<TreeModel>(this, "roTreeModel"));
         roTree.getTreeState().addTreeStateListener(this);
         loadingCircle = new LoadingCircle("ro-tree", LOADING_OBJECT);
         filesPanel = new FilesPanel("files-panel", foldersModel);
         add(loadingCircle);
         filesShiftForm = new FilesShiftForm("form", this);
         itemInfoPanel = new ResourceStatusBar("selected-item-info-panel", new CompoundPropertyModel<Thing>(
                 new PropertyModel<Thing>(this, "currentlySelectedItem")));
         add(itemInfoPanel);
         buttonsBar = new ResourceButtonsBar("buttons-panel");
         add(buttonsBar);
         roStatusBar = new ROStatusBar("research-object-info-panel", new CompoundPropertyModel<Thing>(
                 new PropertyModel<Thing>(this, "researchObject")));
         add(roStatusBar);
         roButtonsBar = new ROButtonsBar("ro-button-bar", researchObject.getUri());
         add(roButtonsBar);
         //background initialziation
         add(new ROExplorerAjaxInitialBehaviour(researchObject, this));
         add(filesPanel);
         add(filesShiftForm);
         //registry listeners
         roTree.addTreeListeners(this);
         filesShiftForm.addOnSubmitListener(this);
         filesPanel.addLinkListeners(this);
     }
 
 
     public Thing getResearchObject() {
         return researchObject;
     }
 
 
     public Thing getCurrentlySelectedItem() {
         return this.getModel().getObject();
     }
 
 
     /**
      * Set currently selected item.
      * 
      * @param item
      *            currently selcted item.
      */
     public void setCurrentlySelectedItem(Thing item) {
         this.getModel().setObject(item);
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
         //folder or RO
         Thing object = (Thing) ((DefaultMutableTreeNode) node).getUserObject();
         setCurrentlySelectedItem(object);
        filesPanel.unselect();
         if (object instanceof Folder) {
             Folder folder = (Folder) object;
             if (!folder.isLoaded()) {
                 try {
                     folder.load(false);
                 } catch (ROSRSException e) {
                     LOG.error("Folfer " + object.getUri().toString() + " can not be loaded", e);
                     folder = null;
                 }
             }
         }
         switchButtonBar();
     }
 
 
     @Override
     public void nodeUnselected(Object node) {
         setCurrentlySelectedItem(null);
         switchButtonBar();
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
         target.add(itemInfoPanel);
         target.add(buttonsBar);
     }
 
 
     @Override
     public void onFormSubmitted() {
         URI folderUri = filesShiftForm.getFolderUri();
         URI fileUri = filesShiftForm.getFileUri();
         Resource resource = researchObject.getResource(fileUri);
         Folder folder = researchObject.getFolder(folderUri);
         if (folderUri == null || fileUri == null) {
             return;
         }
         if (folder != null) {
             try {
                 Iterator<URI> iter = researchObject.getFolders().keySet().iterator();
                 while (iter.hasNext()) {
                     Folder tmpFolder = researchObject.getFolder(iter.next());
                     if (tmpFolder.getFolderEntries().get(fileUri) != null) {
                         tmpFolder.getFolderEntries().get(fileUri).delete();
                     }
                 }
                 folder.addEntry(resource, resource.getName());
             } catch (ROSRSException | ROException e) {
                 LOG.error("Can not move resource: " + resource.toString() + " to folder: " + folder.toString(), e);
             }
         } else {
             //just remove it from folder, it's already in research object 
         }
     }
 
 
     @Override
     public void onAjaxLinkClicked(AjaxRequestTarget target) {
         if (getSelectedFile() != null) {
             setCurrentlySelectedItem(getSelectedFile());
         } else {
             if (roTree.getTreeState().getSelectedNodes().isEmpty()) {
                 setCurrentlySelectedItem(null);
             } else {
                 setCurrentlySelectedItem((Thing) ((DefaultMutableTreeNode) roTree.getTreeState().getSelectedNodes()
                         .iterator().next()).getUserObject());
 
             }
 
         }
         switchButtonBar();
         target.add(itemInfoPanel);
         target.add(buttonsBar);
         for (IAjaxLinkListener listener : listeners) {
             listener.onAjaxLinkClicked(target);
         }
     }
 
 
     /**
      * Switch between resource and folders buttons bar.
      */
     private void switchButtonBar() {
         if (getCurrentlySelectedItem() == null) {
             //nothing to show
             buttonsBar.hideFoldersButtonContainer();
             buttonsBar.hideResourceButtonsContainer();
         } else if (getCurrentlySelectedItem() instanceof Folder) {
             //folders bar
             buttonsBar.showFoldersButtonsContainer(getCurrentlySelectedItem());
             buttonsBar.hideResourceButtonsContainer();
         } else if (getCurrentlySelectedItem() instanceof Resource) {
             //files bar
             buttonsBar.hideFoldersButtonContainer();
             buttonsBar.showResourceButtonsContainer(getSelectedFile());
         } else if (getCurrentlySelectedItem() instanceof ResearchObject) {
             buttonsBar.showFoldersButtonsContainer(researchObject);
         }
     }
 
 
     @SuppressWarnings("unchecked")
     public IModel<Thing> getModel() {
         return (IModel<Thing>) getDefaultModel();
     }
 
 
     public Resource getSelectedFile() {
         return filesPanel.getSelectedFile();
     }
 
 
     /**
      * Add new link listener.
      * 
      * @param listener
      *            link listener
      */
     public void addLinkListener(IAjaxLinkListener listener) {
         listeners.add(listener);
     }
 
 }
