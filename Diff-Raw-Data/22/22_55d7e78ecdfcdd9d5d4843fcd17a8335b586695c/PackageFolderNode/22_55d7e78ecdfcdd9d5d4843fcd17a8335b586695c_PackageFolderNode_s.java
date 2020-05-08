 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.librarysupport.project.nodes.liblocal;
 
 import org.apache.log4j.Logger;
 
 import org.openide.ErrorManager;
 import org.openide.actions.DeleteAction;
 import org.openide.actions.FindAction;
 import org.openide.filesystems.FileLock;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataFolder;
 import org.openide.nodes.FilterNode;
 import org.openide.util.ImageUtilities;
 import org.openide.util.Lookup;
 import org.openide.util.actions.CallableSystemAction;
 import org.openide.util.datatransfer.PasteType;
 import org.openide.util.lookup.Lookups;
 import org.openide.util.lookup.ProxyLookup;
 
 import java.awt.Image;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.Action;
 
 import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
 import de.cismet.cids.abf.librarysupport.project.nodes.cookies.PackageContextCookieImpl;
 import de.cismet.cids.abf.librarysupport.project.nodes.wizard.AddFilesWizardAction;
 import de.cismet.cids.abf.librarysupport.project.nodes.wizard.NewWizardAction;
 import de.cismet.cids.abf.librarysupport.project.nodes.wizard.RenamePackageWizardAction1;
 import de.cismet.cids.abf.utilities.DnDUtils;
 import de.cismet.cids.abf.utilities.DnDUtils.UnifiedDnDFilePasteType;
 import de.cismet.cids.abf.utilities.DnDUtils.UnifiedFilePasteType;
 import de.cismet.cids.abf.utilities.ModificationStore;
 import de.cismet.cids.abf.utilities.data.DataSystemUtil;
 import de.cismet.cids.abf.utilities.files.PackageUtils;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  1.10
  */
 public final class PackageFolderNode extends FilterNode implements Observer {
 
     //~ Static fields/initializers ---------------------------------------------
 
    private static final transient Logger LOG = Logger.getLogger(
            PackageFolderNode.class);
 
     private static final Image IMG_PACKAGE;
     private static final Image IMG_PACKAGE_EMTPY;
 
     static {
         IMG_PACKAGE = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER + "package_16.png");            // NOI18N
         IMG_PACKAGE_EMTPY = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER + "packageEmpty_16.gif"); // NOI18N
     }
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient FileObject folder;
     private final transient FileObject root;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new PackageFolderNode object.
      *
      * @param  project  DOCUMENT ME!
      * @param  folder   DOCUMENT ME!
      * @param  root     DOCUMENT ME!
      */
     public PackageFolderNode(final LibrarySupportProject project,
             final DataFolder folder, final DataFolder root) {
         super(folder.getNodeDelegate(),
             folder.createNodeChildren(DataSystemUtil.NO_FOLDERS_FILTER),
             new ProxyLookup(
                 new Lookup[] {
                     Lookups.singleton(project),
                     folder.getNodeDelegate().getLookup(),
                     Lookups.singleton(
                         new PackageContextCookieImpl(
                             root.getPrimaryFile(),
                             folder.getPrimaryFile())),
                 }));
         this.folder = folder.getPrimaryFile();
         this.root = root.getPrimaryFile();
         setDisplayName(PackageUtils.toPackage(this.root, this.folder));
         ModificationStore.getInstance().addObserver(this);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Image getOpenedIcon(final int i) {
         return getIcon(i);
     }
 
     @Override
     public Image getIcon(final int i) {
         Image image;
         final Enumeration children = folder.getData(false);
         if ((children != null) && children.hasMoreElements()) {
             image = IMG_PACKAGE;
         } else {
             image = IMG_PACKAGE_EMTPY;
         }
         if (ModificationStore.getInstance().wasModified(
                         FileUtil.toFile(folder).getAbsolutePath(),
                         ModificationStore.MOD_CHANGED)) {
             final Image badge = ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER + "blueDot_7.gif"); // NOI18N
             image = ImageUtilities.mergeImages(image, badge, 10, 10);
         }
         return image;
     }
 
     @Override
     public boolean canDestroy() {
         return true;
     }
 
     @Override
     public boolean canRename() {
         return false;
     }
 
     @Override
     public boolean canCopy() {
         return false;
     }
 
     @Override
     public boolean canCut() {
         return false;
     }
 
     @Override
     public void destroy() throws IOException {
         FileLock lock = null;
         try {
             if (folder.getFolders(false).hasMoreElements()) {
                 final Enumeration<? extends FileObject> e = folder.getData(
                         false);
                 while (e.hasMoreElements()) {
                     final FileObject fo = e.nextElement();
                     try {
                         lock = fo.lock();
                         fo.delete(lock);
                     } catch (final IOException ex) {
                         LOG.error("delete failed: " // NOI18N
                                     + fo.getNameExt(), ex);
                         ErrorManager.getDefault()
                                 .annotate(
                                     ex,
                                     org.openide.util.NbBundle.getMessage(
                                         PackageFolderNode.class,
                                         "PackageFolderNode.destroy().ErrorManager.message")
                                     + fo.getNameExt()); // NOI18N
                     }
                 }
             } else {
                 if (!root.equals(folder)) {
                     lock = folder.lock();
                     folder.delete(lock);
                 }
             }
         } catch (final IOException ex) {
             LOG.error("delete failed: " + folder.getName(), ex); // NOI18N
             // TODO: replace using appropriate user dialog interface
             ErrorManager.getDefault().notify(ex);
         } finally {
             if ((lock != null) && lock.isValid()) {
                 lock.releaseLock();
             }
         }
     }
 
     @Override
     public PasteType getDropType(final Transferable t, final int action,
             final int index) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("see what flavor is supported");               // NOI18N
         }
         if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("javaFileListFlavor is supported");        // NOI18N
             }
             try {
                 final Object obj = t.getTransferData(DataFlavor.javaFileListFlavor);
                 if (obj != null) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("transferdata already present");   // NOI18N
                     }
                     if (!DnDUtils.acceptFiles((List)obj, FileUtil.toFile(folder), action)) {
                         return super.getDropType(t, action, index);
                     }
                 }
                 return new UnifiedDnDFilePasteType(t, action, folder);
             } catch (final UnsupportedFlavorException ex) {
                 LOG.warn("javaFileListFlavor not supported despite " // NOI18N
                             + "transferable's \"tell\"", ex);        // NOI18N
             } catch (final IOException ex) {
                 LOG.warn("could not get transferdata before "        // NOI18N
                             + "paste action", ex);                   // NOI18N
             }
         } else if (t.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("uriListFlavor is supported");             // NOI18N
             }
             final List<File> fileList = DnDUtils.getFileListFromURIList(t);
             if (fileList != null) {
                 if (DnDUtils.acceptFiles(fileList, FileUtil.toFile(folder), action)) {
                     return new UnifiedDnDFilePasteType(t, action, folder);
                 }
             } else {
                 LOG.warn("cannot retrieve transfer data");           // NOI18N
             }
         } else {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("no known flavor supported");              // NOI18N
             }
         }
         return super.getDropType(t, action, index);
     }
 
     @Override
     public PasteType[] getPasteTypes(final Transferable t) {
         PasteType[] pt = super.getPasteTypes(t);
         pt = Arrays.copyOf(pt, pt.length + 1);
         pt[pt.length - 1] = new UnifiedFilePasteType(t, folder);
         return pt;
     }
 
     @Override
     public Action[] getActions(final boolean context) {
         final Action[] superActions = super.getActions(context);
         final ArrayList<Action> actions = new ArrayList<Action>(superActions.length + 7);
         // we should use the NewType mechanism instead of that wizard
         actions.add(CallableSystemAction.get(NewWizardAction.class));
         actions.add(CallableSystemAction.get(AddFilesWizardAction.class));
         boolean skip = false;
         for (final Action action : superActions) {
             // we want to skip RSMDataObjectAction as it is the rename action
             // here but it is not available in the public api. we have to assume
             // that the action will occur after the delete action
             if (action instanceof DeleteAction) {
                 skip = true;
                 actions.add(action);
                 actions.add(CallableSystemAction.get(RenamePackageWizardAction1.class));
             } else if (action instanceof FindAction) {
                 // as we were searching for the New... Action and is not
                 // available in the public api, we assume that occured before
                 // the find action and remove it from the actions list by
                 // by replacing it with the find action
                 actions.set(actions.size() - 1, action);
             } else if (skip) {
                 skip = false;
                 continue;
             } else {
                 actions.add(action);
             }
         }
         return actions.toArray(new Action[actions.size()]);
     }
 
     @Override
     public void update(final Observable observable, final Object object) {
         if (observable instanceof ModificationStore) {
             fireIconChange();
         }
     }
 }
