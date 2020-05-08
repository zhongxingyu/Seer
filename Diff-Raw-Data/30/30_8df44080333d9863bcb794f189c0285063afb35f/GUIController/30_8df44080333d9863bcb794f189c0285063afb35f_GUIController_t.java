 package org.nebulostore.gui;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.tree.ExpandVetoException;
 
 import com.google.inject.Inject;
 
 import org.apache.log4j.Logger;
 import org.nebulostore.appcore.addressing.AppKey;
 import org.nebulostore.appcore.addressing.NebuloAddress;
 import org.nebulostore.appcore.addressing.ObjectId;
 import org.nebulostore.appcore.exceptions.NebuloException;
 import org.nebulostore.appcore.model.EncryptedObject;
 import org.nebulostore.appcore.model.NebuloElement;
 import org.nebulostore.appcore.model.NebuloFile;
 import org.nebulostore.appcore.model.NebuloList;
 import org.nebulostore.appcore.model.NebuloList.ListIterator;
 import org.nebulostore.appcore.model.NebuloObject;
 import org.nebulostore.appcore.model.NebuloObjectFactory;
 import org.nebulostore.peers.Peer;
 
 /**
  * Graphical User Interface logic.
  * @author Jadwiga Kanska
  */
 public class GUIController extends Peer {
   private static Logger logger_ = Logger.getLogger(GUIController.class);
 
   private static final String DEFAULT_OBJECT_ID = "123";
 
   private NebuloObjectFactory objectFactory_;
 
   private GUIView view_;
 
   private String infoMessage_;
 
   private NebuloAddress rootAddress_;
   private NebuloList currentParentList_;
   private boolean hasFileExisted_;
 
   public GUIController() {
     objectFactory_ = null;
   }
 
   @Inject
   public void setDependencies(NebuloObjectFactory objectFactory) {
     objectFactory_ = objectFactory;
   }
 
   protected void runPeer() {
     initPeer();
     runBroker();
     startPeer();
     putKey(appKey_);
 
     try {
       initializeRootAddress();
       if (appKey_.equals(rootAddress_.getAppKey()))
         createInitialList();
 
       EventQueue.invokeLater(new Runnable() {
         public void run() {
 
           view_ = new GUIView(new NebuloElement(rootAddress_), appKey_);
 
           view_.addNodeSelectionListener(new NodeSelectionListener());
           view_.addNodeExpansionListener(new NodeExpansionListener());
           view_.addSaveFileButtonListener(new SaveFileButtonListener());
           view_.addSaveElementButtonListener(new SaveElementButtonListener());
           view_.addSaveUpdateButtonListener(new SaveUpdateButtonListener());
           view_.addGetRootButtonListener(new GetTreeRootListener());
           view_.addNewListButtonListener(new NewListButtonListener());
           view_.addBrowseFileButtonListener(new BrowseFileButtonListener());
           view_.addBrowseFolderButtonListener(new BrowseFolderButtonListener());
           view_.addNewViewButtonListener(new NewViewButtonListener());
           view_.addWindowListener(new CloseListener());
 
           view_.expandRoot();
 
           view_.setVisible(true);
 
         }
       });
     } catch (UnsupportedEncodingException exception) {
       printExceptionAndFinish(exception);
     } catch (NebuloException exception) {
       printExceptionAndFinish(exception);
     }
 
   }
 
   private void printExceptionAndFinish(Exception exception) {
     logger_.error("Got exception while setting up GUI.", exception);
     quitNebuloStore();
     finishPeer();
   }
 
   private void initializeRootAddress() {
     BigInteger objectId = new BigInteger(DEFAULT_OBJECT_ID);
    rootAddress_ = new NebuloAddress(appKey_, new ObjectId(objectId));
   }
 
   private void createInitialList() throws NebuloException, UnsupportedEncodingException {
 
     // Create all needed addresses.
     AppKey appKey = rootAddress_.getAppKey();
     BigInteger objectId = rootAddress_.getObjectId().getKey();
     objectId = objectId.add(BigInteger.ONE);
    NebuloAddress firstChildAddress = new NebuloAddress(appKey_, new ObjectId(objectId));
     objectId = objectId.add(BigInteger.ONE);
    NebuloAddress secondChildAddress = new NebuloAddress(appKey_, new ObjectId(objectId));
     objectId = objectId.add(BigInteger.ONE);
    NebuloAddress grandChildAddress = new NebuloAddress(appKey_, new ObjectId(objectId));
     objectId = objectId.add(BigInteger.ONE);
    NebuloAddress fileAddress = new NebuloAddress(appKey_, new ObjectId(objectId));
 
     int exampleNumberOfChildren = 4;
 
     // Create first level (root and its children).
     NebuloList rootList = objectFactory_.createNewNebuloList(rootAddress_);
     for (int i = 0; i < exampleNumberOfChildren; ++i) {
       String content = "Content of child (NebuloElement) nr " +
           i + ".";
       rootList.append(new NebuloElement(new EncryptedObject(content.getBytes())));
     }
 
     // Create second level (root's children and grandchildren).
     NebuloList firstChildList = objectFactory_.createNewNebuloList(firstChildAddress);
     for (int i = 0; i < exampleNumberOfChildren; ++i) {
       String content = "Content of grandchild (NebuloElement) nr 1" +
           i + ".";
       firstChildList.append(new NebuloElement(new EncryptedObject(content.getBytes())));
     }
     NebuloFile file = objectFactory_.createNewNebuloFile(fileAddress);
     file.write("Example file.".getBytes("UTF-8"), 0);
     firstChildList.append(new NebuloElement(fileAddress));
 
     rootList.append(new NebuloElement(firstChildAddress));
 
     NebuloList secondChildList = objectFactory_.createNewNebuloList(secondChildAddress);
     for (int i = 0; i < exampleNumberOfChildren; ++i) {
       String content = "Content of grandchild (NebuloElement) nr 2" +
           i + ".";
       secondChildList.append(new NebuloElement(new EncryptedObject(content.getBytes())));
     }
 
     rootList.append(new NebuloElement(secondChildAddress));
 
     // Create third level (root's grandgrandchildren).
     NebuloList grandChildList = objectFactory_.createNewNebuloList(grandChildAddress);
     for (int i = 0; i < exampleNumberOfChildren; ++i) {
       String content = "Content of grandgrandchild (NebuloElement) nr " +
           i + ".";
       grandChildList.append(new NebuloElement(new EncryptedObject(content.getBytes())));
     }
 
     secondChildList.append(new NebuloElement(grandChildAddress));
 
     rootList.sync();
     firstChildList.sync();
     secondChildList.sync();
     grandChildList.sync();
 
     return;
 
   }
 
   /**
    * Class listening for a change of root address.
    */
   protected class GetTreeRootListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent event) {
       clearInfoMessage();
 
       String addressString = view_.getInputRootAddress();
       NebuloAddress rootAddress = parseNebuloAddress(addressString);
       if (rootAddress == null)
         return;
 
       try {
         NebuloObject link = fetchNebuloObject(rootAddress);
 
         if (link.getClass().equals(NebuloList.class)) {
           view_.resetListTree(new NebuloElement(link), true);
           view_.expandRoot();
         } else {
           view_.resetListTree(new NebuloElement(link), false);
           String content = readFileContent(rootAddress);
           view_.setAddressAndFileContent(rootAddress, content);
         }
 
       } catch (NebuloException exception) {
         showErrorMessage("Couldn't fetch root.\tGot exception:\n" +
             exception.getMessage());
         return;
       }
 
       view_.showMessage(infoMessage_);
     }
   }
 
   /**
    * Class listening for tree node selection.
    */
   protected class NodeSelectionListener implements TreeSelectionListener {
 
     @Override
     public void valueChanged(TreeSelectionEvent event) {
       clearInfoMessage();
 
       NebuloElement selectedElement = view_.getSelectedElement();
       if (selectedElement == null)
         return;
 
       if (selectedElement.isLink()) {
 
         try {
           NebuloAddress address = selectedElement.getAddress();
           NebuloObject link = fetchNebuloObject(address);
 
           if (link.getClass().equals(NebuloFile.class)) {
             String content = readFileContent(address);
             view_.setAddressAndFileContent(address, content);
           }
 
           if (link.getClass().equals(NebuloList.class)) {
             view_.setAddressAndFileContent(address, "");
           }
 
         } catch (NebuloException exception) {
           showErrorMessage("Problem while fetching selected object.\n" +
               exception.getMessage());
           return;
         }
 
       } else {
         view_.setAddressAndFileContent(null, new String(selectedElement.getData()
             .getEncryptedData()));
       }
 
       view_.showMessage(infoMessage_);
     }
   }
 
   /**
    * Class listening for tree node (list) expansion.
    */
   protected class NodeExpansionListener implements TreeWillExpandListener {
 
     @Override
     public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
       clearInfoMessage();
 
       view_.setSelectionAtComponent(event.getPath());
 
       NebuloElement selectedElement = view_.getSelectedElement();
       assert selectedElement.isLink();
       NebuloAddress link = selectedElement.getAddress();
 
       try {
         NebuloObject object = fetchNebuloObject(link);
         assert object.getClass().equals(NebuloList.class);
         NebuloList list = (NebuloList) object;
 
         if (list.iterator().hasNext()) {
           view_.removeAllChildrenFromSelectedNode();
           addExpandedNodeChildren(list);
         } else {
           appendInfoMessage("Selected list is empty.");
           view_.showMessage(infoMessage_);
           throw new ExpandVetoException(event);
         }
 
       } catch (NebuloException exception) {
         showErrorMessage("Exception while fetching object: " +
             exception.getMessage());
         throw new ExpandVetoException(event);
       }
 
       view_.showMessage(infoMessage_);
     }
 
     @Override
     public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
     }
 
   }
 
   /**
    * Class listening for saving of a file request.
    */
   protected class SaveFileButtonListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent event) {
       clearInfoMessage();
 
       String addressString = view_.getInputCurrentAddress();
       NebuloAddress fileAddress = parseNebuloAddress(addressString);
       if (fileAddress == null)
         return;
 
       try {
         if (isList(fileAddress)) {
           showErrorMessage("Given address is list address.");
           return;
         }
 
         String contentToSave = view_.getFileContent();
         setCurrentParentList();
         NebuloFile file = writeNebuloFileWithString(fileAddress, contentToSave);
 
         if (!hasFileExisted_) {
           view_.updateParentListView(new NebuloElement(file), false);
         }
 
       } catch (NebuloException exception) {
         showErrorMessage("Got exception while saving file's content:\t" +
             exception.getMessage());
         return;
       } catch (UnsupportedEncodingException exception) {
         showErrorMessage("Encoding exception while saving file's content: " +
             exception.getMessage());
         return;
       }
 
       view_.showMessage(infoMessage_);
     }
 
   }
 
   /**
    * Class listening for saving of a list element request.
    */
   protected class SaveElementButtonListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent event) {
       clearInfoMessage();
 
       try {
         String contentToSave = view_.getFileContent();
         NebuloElement element = new NebuloElement(new EncryptedObject(contentToSave.getBytes()));
 
         setCurrentParentList();
         appendLinkToList(element);
 
         view_.updateParentListView(element, false);
 
       } catch (NebuloException exception) {
         showErrorMessage("Got exception while saving element:\n" +
             exception.getMessage());
         return;
       }
 
       view_.showMessage(infoMessage_);
     }
 
   }
 
   /**
    * Class listening for creating new list request.
    */
   protected class NewListButtonListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent event) {
       clearInfoMessage();
 
       try {
 
         NebuloList newList = objectFactory_.createNewNebuloList();
         NebuloAddress listAddress = newList.getAddress();
         NebuloElement element = new NebuloElement(listAddress);
 
         setCurrentParentList();
         appendLinkToList(element);
 
         view_.updateParentListView(element, true);
 
       } catch (NebuloException exception) {
         showErrorMessage("Got exception while appending newly created list.");
         return;
       }
 
       view_.showMessage(infoMessage_);
     }
 
   }
 
   /**
    * Class listening for browsing files on disc action and implementing uploading file logic.
    */
   protected class BrowseFileButtonListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent event) {
       clearInfoMessage();
 
       File file = view_.getFolderFromFileChooserDialog();
 
       if (file != null) {
         assert file.isFile();
 
         String addressString = view_.getInputCurrentAddress();
         NebuloAddress address = parseNebuloAddress(addressString);
         if (address == null)
           return;
 
         try {
           setCurrentParentList();
           NebuloFile nebuloFile = uploadFile(file, address);
 
           if (!hasFileExisted_) {
             view_.updateParentListView(new NebuloElement(nebuloFile), false);
           }
 
         } catch (IOException exception) {
           showErrorMessage("Got IOException while uploading file: " +
               exception.getMessage());
           return;
         } catch (NebuloException exception) {
           showErrorMessage("Got NebuloException while uploading file: " +
               exception.getMessage());
           return;
         }
       }
 
       view_.showMessage(infoMessage_);
     }
 
   }
 
   /**
    * Class listening for browsing folders on disc action and implementing uploading folder logic.
    */
   protected class BrowseFolderButtonListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent event) {
       clearInfoMessage();
 
       File folder = view_.getFileFromFileChooserDialog();
 
       if (folder != null) {
         assert folder.isDirectory();
 
         try {
           setCurrentParentList();
           uploadFolder(folder);
 
           currentParentList_.sync();
         } catch (IOException exception) {
           showErrorMessage("Got IOException while uploading directory: " +
               exception.getMessage());
           return;
         } catch (NebuloException exception) {
           showErrorMessage("Got NebuloException while uploading directory: " +
               exception.getMessage());
           return;
         }
       }
 
       view_.showMessage(infoMessage_);
 
     }
 
   }
 
   /**
    * Class listening for refreshing view request.
    */
   protected class NewViewButtonListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent event) {
       view_.refreshView();
     }
 
   }
 
   /**
    * Class listening for saving element or file update.
    */
   protected class SaveUpdateButtonListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent event) {
       clearInfoMessage();
 
       NebuloElement element = view_.getSelectedElement();
 
       if (element.isLink()) {
         try {
           NebuloObject link = fetchNebuloObject(element.getAddress());
           setCurrentParentList();
 
           if (link.getClass().equals(NebuloFile.class)) {
 
             String contentToSave = view_.getFileContent();
             writeNebuloFileWithString(link.getAddress(), contentToSave);
 
           } else {
             showErrorMessage("You cannot override list; it has no content.");
             return;
           }
         } catch (NebuloException exception) {
           showErrorMessage("Got NebuloException while saving update: " +
               exception.getMessage());
           return;
         } catch (UnsupportedEncodingException exception) {
           showErrorMessage("Got UnsupportedEncodingException while saving update: " +
               exception.getMessage());
           return;
         }
 
       } else {
         showErrorMessage("You cannot override list element.");
         return;
       }
 
       view_.showMessage(infoMessage_);
     }
   }
 
   /**
    * Class listening for window closing operation. It finishes Peer.
    */
   protected class CloseListener implements WindowListener {
     @Override
     public void windowActivated(WindowEvent event) {
     }
 
     @Override
     public void windowClosed(WindowEvent event) {
     }
 
     @Override
     public void windowClosing(WindowEvent event) {
       view_.dispose();
       quitNebuloStore();
       finishPeer();
     }
 
     @Override
     public void windowDeactivated(WindowEvent event) {
     }
 
     @Override
     public void windowDeiconified(WindowEvent event) {
     }
 
     @Override
     public void windowIconified(WindowEvent event) {
     }
 
     @Override
     public void windowOpened(WindowEvent event) {
     }
   }
 
   private void clearInfoMessage() {
     infoMessage_ = "";
   }
 
   private void appendInfoMessage(String info) {
     infoMessage_ += "[INFO]\t" +
         info + "\n";
   }
 
   private NebuloAddress parseNebuloAddress(String addressString) {
     String errorMessage = "Exception while parsing root address." +
         "The address should be in 'applicationKey, objectID' format.";
 
     try {
       String[] tokens = addressString.replaceAll(" ", "").split(":");
 
       NebuloAddress address = new NebuloAddress(new AppKey(new BigInteger(tokens[0])),
           new ObjectId(new BigInteger(tokens[1])));
 
       return address;
 
     } catch (NumberFormatException exception) {
       showErrorMessage(errorMessage);
       return null;
     } catch (IllegalArgumentException exception) {
       showErrorMessage(errorMessage);
       return null;
     } catch (ArrayIndexOutOfBoundsException exception) {
       showErrorMessage(errorMessage);
       return null;
     }
 
   }
 
   private String formatAddress(NebuloAddress address) {
     return "[" +
         address.getAppKey().toString() + " : " + address.getObjectId().toString() + "]";
   }
 
   private void showErrorMessage(String message) {
     view_.refreshView();
     if (infoMessage_.isEmpty()) {
       view_.showMessage("[ERROR]\t" +
           message);
     } else {
       view_.showMessage(infoMessage_ +
           "\n" + "[ERROR]\t" + message);
     }
   }
 
   private NebuloObject fetchNebuloObject(NebuloAddress address) throws NebuloException {
     NebuloObject object = objectFactory_.fetchExistingNebuloObject(address);
     appendInfoMessage("Succesfully fetched object with address: " +
         formatAddress(address) + ".");
     return object;
   }
 
   private void setCurrentParentList() throws NebuloException {
     NebuloElement currentList = view_.getCurrentList();
     currentParentList_ = fetchNebuloList(currentList.getAddress());
   }
 
   private NebuloList fetchNebuloList(NebuloAddress address) throws NebuloException {
     NebuloList list = (NebuloList) objectFactory_.fetchExistingNebuloObject(address);
     appendInfoMessage("Succesfully fetched list with address: " +
         formatAddress(address) + ".");
     return (NebuloList) list;
   }
 
   private String readFileContent(NebuloAddress address) throws NebuloException {
     NebuloFile file = fetchNebuloFile(address);
 
     byte[] data;
     int size = file.getSize();
     data = file.read(0, size);
     appendInfoMessage("Successfully received content of file with address: " +
         formatAddress(address) + ".");
 
     return new String(data);
   }
 
   private NebuloFile fetchNebuloFile(NebuloAddress address) throws NebuloException {
     NebuloFile file = (NebuloFile) objectFactory_.fetchExistingNebuloObject(address);
     appendInfoMessage("Succesfully fetched file with address: " +
         formatAddress(address) + ".");
     return file;
   }
 
   private NebuloFile writeNebuloFileWithString(NebuloAddress address, String content)
     throws UnsupportedEncodingException, NebuloException {
     return writeNebuloFileWithByteArray(address, content.getBytes("UTF-8"));
   }
 
   private NebuloFile writeNebuloFileWithByteArray(NebuloAddress address, byte[] content)
     throws NebuloException {
     NebuloFile file = fetchOrCreateNebuloFile(address);
 
     int bytesWritten = file.write(content, 0);
     appendInfoMessage("Successfully written " +
         String.valueOf(bytesWritten) + " bytes to file with address: " + formatAddress(address) +
         ".");
 
     if (!hasFileExisted_)
       appendLinkToList(address);
 
     return file;
   }
 
   private void appendLinkToList(NebuloAddress address) throws NebuloException {
     appendLinkToList(new NebuloElement(address));
   }
 
   private void appendLinkToList(NebuloElement element) throws NebuloException {
     currentParentList_.append(element);
     if (element.isLink()) {
       appendInfoMessage("Successfully appended object with address: " +
           formatAddress(element.getAddress()) + " to list with address: " +
           formatAddress(currentParentList_.getAddress()) + ".");
     } else {
       appendInfoMessage("Successfully appended element to list with address: " +
           formatAddress(currentParentList_.getAddress()) + ".");
     }
     currentParentList_.sync();
     appendInfoMessage("Successfully synced list with address: " +
         formatAddress(currentParentList_.getAddress()) + ".");
     return;
   }
 
   private NebuloFile fetchOrCreateNebuloFile(NebuloAddress nebuloAddress) throws NebuloException {
     NebuloFile file = null;
 
     try {
       file = (NebuloFile) objectFactory_.fetchExistingNebuloObject(nebuloAddress);
       hasFileExisted_ = true;
       appendInfoMessage("Fetched exisiting file with address " +
           formatAddress(file.getAddress()) + ".");
     } catch (NebuloException exception) {
       file = objectFactory_.createNewNebuloFile(nebuloAddress);
       hasFileExisted_ = false;
       appendInfoMessage("Created new file with address " +
           formatAddress(file.getAddress()) + ".");
     }
 
     return file;
   }
 
   private NebuloFile uploadFile(File file, NebuloAddress address) throws IOException,
     NebuloException {
 
     FileInputStream fin = new FileInputStream(file);
     int contentSize = (int) file.length();
     byte[] fileContent = new byte[contentSize];
     fin.read(fileContent);
     fin.close();
 
     return writeNebuloFileWithByteArray(address, fileContent);
   }
 
   private void uploadFolder(File directoryPath) throws NebuloException, IOException {
     NebuloList oldParentList = currentParentList_;
     currentParentList_ = objectFactory_.createNewNebuloList();
 
     String[] elements = directoryPath.list();
     for (String elementPath : elements) {
       File abstractFile = new File(directoryPath.getPath() +
           "/" + elementPath);
 
       if (abstractFile.isFile()) {
         NebuloFile newFile = objectFactory_.createNewNebuloFile();
         uploadFile(abstractFile, newFile.getAddress());
       } else {
         uploadFolder(abstractFile);
       }
 
     }
 
     currentParentList_.sync();
     oldParentList.append(new NebuloElement(currentParentList_.getAddress()));
 
     currentParentList_ = oldParentList;
   }
 
   private boolean isList(NebuloAddress address) {
     try {
       NebuloObject object = objectFactory_.fetchExistingNebuloObject(address);
       return object.getClass().equals(NebuloList.class);
     } catch (NebuloException exception) {
       return false;
     }
   }
 
   private void addExpandedNodeChildren(NebuloList list) throws NebuloException {
     ListIterator iter = list.iterator();
     while (iter.hasNext()) {
       NebuloElement elem = iter.next();
       boolean isList = elem.isLink() &&
           isList(elem.getAddress());
       view_.addChildToSelectedNode(elem, isList);
     }
   }
 
 };;
