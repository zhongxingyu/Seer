 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.util;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import evopaint.pixel.rulebased.ExampleRuleSetCollectionFactory;
 import evopaint.pixel.rulebased.RuleSet;
 import evopaint.pixel.rulebased.RuleSetCollection;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.InvalidClassException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.List;
 import javax.swing.JFileChooser;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 /**
  *
  * @author tam
  */
 public class FileHandler implements TreeModelListener {
 
     private File homeDir;
     private File collectionsDir;
     private XStream xStream;
 
     public File getHomeDir() {
         return homeDir;
     }
 
     public File getCollectionsDir() {
         return collectionsDir;
     }
     
     public synchronized void checkFiles() {
         File userDir = new JFileChooser().getFileSystemView().getDefaultDirectory();
         homeDir = new File(userDir, ".evopaint");
         if (homeDir.exists() == false) {
             homeDir.mkdir();
         }
         collectionsDir = new File(homeDir, "collections");
         if (collectionsDir.exists() == false) {
             collectionsDir.mkdir();
             createExampleCollections(collectionsDir);
         }
     }
 
     public synchronized DefaultTreeModel readCollections() {
         File [] collectionDirs = collectionsDir.listFiles();
         DefaultMutableTreeNode root = new DefaultMutableTreeNode();
         DefaultTreeModel model = new DefaultTreeModel(root, true);
 
         try {
             for (File collectionDir : collectionDirs) {
                 if (false == collectionDir.isDirectory()) {
                     collectionDir.delete();
                 }
                 File metadataFile = new File(collectionDir, "metadata.xml");
                 Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(metadataFile), "UTF8"));
                 RuleSetCollection ruleSetCollection = (RuleSetCollection)xStream.fromXML(reader);
                 reader.close();
                 CollectionNode collectionNode = new CollectionNode(ruleSetCollection);
                 for (File file : collectionDir.listFiles()) {
                     if (file.getName().equals("metadata.xml")) {
                         continue;
                     }
                     reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
                     RuleSet ruleSet = (RuleSet)xStream.fromXML(reader);
                     reader.close();
                     RuleSetNode ruleSetNode = new RuleSetNode(ruleSet);
                     collectionNode.insert(ruleSetNode, collectionNode.getChildCount());
                 }
                 model.insertNodeInto(collectionNode, root, root.getChildCount());
             }
         } catch (FileNotFoundException ex) {
             ExceptionHandler.handle(ex);
         } catch (InvalidClassException ex) {
             ExceptionHandler.handle(ex);
         } catch (IOException ex) {
             ExceptionHandler.handle(ex);
         }
         model.addTreeModelListener(this);
         return model;
     }
 
     /*
     public synchronized void renameCollection(RuleSetCollection collection, String newName) {
         if (newName == null || collection.getName().equals(newName)) {
             return;
         }
 
         File collectionFile = new File(collectionsDir, makeDirectoryName(collection.getName()));
         collectionFile.renameTo(new File(collectionsDir, makeFileName(newName)));
     }
 
     public synchronized void writeCollection(CollectionNode collectionNode) {
         try {
             RuleSetCollection collection = (RuleSetCollection)collectionNode.getUserObject();
 
             File collectionDir = new File(collectionsDir, makeDirectoryName(collection.getName()));
             if (false == collectionDir.exists()) {
                 collectionDir.mkdir();
             }
 
             File metaDataFile = new File(collectionDir, "metadata.xml");
 
             Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaDataFile), "UTF8"));
             writer.write(xStream.toXML(collection));
             writer.close();
 
             Enumeration enumerationA = collectionNode.children();
             if (enumerationA != null) {
                 while (enumerationA.hasMoreElements()) {
                     RuleSetNode ruleSetNode = (RuleSetNode)enumerationA.nextElement();
                     RuleSet ruleSet = (RuleSet)ruleSetNode.getUserObject();
 
                     File ruleSetFile = new File(collectionDir, makeFileName(ruleSet.getName()));
                     writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruleSetFile), "UTF8"));
                     writer.write(xStream.toXML(ruleSet));
                     writer.close();
                 }
             }
         } catch (UnsupportedEncodingException ex) {
             ExceptionHandler.handle(ex);
         } catch (FileNotFoundException ex) {
             ExceptionHandler.handle(ex);
         } catch (IOException ex) {
             ExceptionHandler.handle(ex);
         }
     }
      
 
     public synchronized void deleteCollection(CollectionNode collectionNode) {
         RuleSetCollection collection = (RuleSetCollection)collectionNode.getUserObject();
         File collectionFile = new File(collectionsDir, makeDirectoryName(collection.getName()));
         collectionFile.delete();
     }
 */
     
     private synchronized void createExampleCollections(File dir) {
         try {
             URL examplesURL = getClass().getResource("/evopaint/examples");
             File examplesDir = new File(examplesURL.getFile());
             for (File exampleCollectionDir : examplesDir.listFiles()) {
                 assert(exampleCollectionDir.isDirectory());
                 File realCollectionDir = new File(collectionsDir, exampleCollectionDir.getName());
                 assert (false == realCollectionDir.exists());
                 realCollectionDir.mkdir();
                 File [] exampleRuleSetFiles = exampleCollectionDir.listFiles();
                 for (File exampleRuleSetFile : exampleRuleSetFiles) {
                     FileInputStream in = new FileInputStream(exampleRuleSetFile);
                     File realRuleSetFile = new File(realCollectionDir, exampleRuleSetFile.getName());
                     FileOutputStream out = new FileOutputStream(realRuleSetFile);
                     byte [] buffer = new byte[1024];
                     int readBytes;
                     while ((readBytes = in.read(buffer)) > 0) {
                         out.write(buffer, 0, readBytes);
                     }
                     in.close();
                     out.close();
                 }
             }
         } catch (FileNotFoundException ex) {
             ExceptionHandler.handle(ex);
         } catch (IOException ex) {
             ExceptionHandler.handle(ex);
         }
     }
 
     private String makeDirectoryName(String title) {
         return title.replace(" ", "_").toLowerCase();
     }
 
     private String makeFileName(String title) {
         return title.replace(" ", "_").toLowerCase() + ".epr";
     }
 
     public void treeNodesChanged(TreeModelEvent e) {
         DefaultMutableTreeNode parentNode =
                 (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
         DefaultMutableTreeNode changedNode = (DefaultMutableTreeNode)
                 parentNode.getChildAt(e.getChildIndices()[0]);
 
         Writer writer = null;
         try {
             if (changedNode instanceof RuleSetNode) {
                 RuleSetNode ruleSetNode = (RuleSetNode)changedNode;
                 CollectionNode collectionNode = (CollectionNode)parentNode;
                 assert(collectionNode != null);
                 File ruleSetFile = new File(collectionsDir,
                        makeDirectoryName(collectionNode.getName()) + File.separator +
                         makeFileName(ruleSetNode.getName()));
                 writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruleSetFile), "UTF8"));
                 writer.write(xStream.toXML((RuleSet)ruleSetNode.getUserObject()));
                 writer.close();
                 return;
             }
 
             if (changedNode instanceof CollectionNode) {
                 //System.out.println("collection directory changed");
                 CollectionNode collectionNode = (CollectionNode)changedNode;
                 assert(collectionNode != null);
                 File [] collectionDirs = collectionsDir.listFiles();
 
                 for (int i = 0; i < collectionDirs.length; i++) {
                     boolean found = false;
                     Enumeration enumeration = parentNode.children();
                     while (enumeration.hasMoreElements()) {
                         String name = makeDirectoryName(((CollectionNode)enumeration.nextElement()).getName());
                         if (collectionDirs[i].getName().equals(name)) {
                             found = true;
                             break;
                         }
                     }
                     if (found == false) {
                         if (false == collectionDirs[i].renameTo(new File(collectionsDir,
                                 makeDirectoryName(collectionNode.getName())))) {
                             System.out.println("Failed to rename collection directory: " + collectionDirs[i].getAbsolutePath());
                         } else {
                             File metaDataFile = new File(collectionsDir,
                                     makeDirectoryName(collectionNode.getName()) +
                                     "/metadata.xml");
                             writer = new BufferedWriter(
                                     new OutputStreamWriter(new FileOutputStream(metaDataFile), "UTF8"));
                             writer.write(xStream.toXML(
                                     (RuleSetCollection) collectionNode.getUserObject()));
                             writer.close();
                         }
                         break;
                     }
                 }
                 return;
             }
         } catch (UnsupportedEncodingException ex) {
             ExceptionHandler.handle(ex);
         } catch (FileNotFoundException ex) {
             ExceptionHandler.handle(ex);
         } catch (IOException ex) {
             ExceptionHandler.handle(ex);
         } finally {
             try {
                 writer.close();
             } catch (IOException ex) {
                 ExceptionHandler.handle(ex);
             }
         }
         assert (false);
     }
 
     public void treeNodesInserted(TreeModelEvent e) {
         DefaultMutableTreeNode parentNode =
                 (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
         DefaultMutableTreeNode addedNode =
                 (DefaultMutableTreeNode)e.getChildren()[0];
         //System.out.println("file handler: detected node insertion");
         Writer writer = null;
         try {
             if (addedNode instanceof CollectionNode) {
                 File collectionDir = new File(collectionsDir,
                         makeDirectoryName(((CollectionNode)addedNode).getName()));
                 if (false == collectionDir.exists()) {
                     collectionDir.mkdir();
                 }
 
                 File metaDataFile = new File(collectionDir, "metadata.xml");
                 writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaDataFile), "UTF8"));
                 writer.write(xStream.toXML((RuleSetCollection)addedNode.getUserObject()));
                 writer.close();
                 return;
             }
 
             if (addedNode instanceof RuleSetNode) {
                 CollectionNode collectionNode = (CollectionNode)parentNode;
                 assert(collectionNode != null);
                 File ruleSetFile = new File(collectionsDir,
                     makeDirectoryName(collectionNode.getName() +
                    File.separator +
                     makeFileName(((RuleSetNode)addedNode).getName())));
                 writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruleSetFile), "UTF8"));
                 writer.write(xStream.toXML((RuleSet)addedNode.getUserObject()));
                 writer.close();
                 return;
             }
         } catch (UnsupportedEncodingException ex) {
             ExceptionHandler.handle(ex);
         } catch (FileNotFoundException ex) {
             ExceptionHandler.handle(ex);
         } catch (IOException ex) {
             ExceptionHandler.handle(ex);
         } finally {
             try {
                 writer.close();
             } catch (IOException ex) {
                 ExceptionHandler.handle(ex);
             }
         }
 
         assert(false);
     }
 
     public void treeNodesRemoved(TreeModelEvent e) {
         DefaultMutableTreeNode parentNode =
                 (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
         DefaultMutableTreeNode removedNode =
                 (DefaultMutableTreeNode)e.getChildren()[0];
         //System.out.println("file handler: detected node deletion");
         
         if (removedNode instanceof CollectionNode) {
             String dirName = makeDirectoryName(((CollectionNode)removedNode).getName());
             File collectionDir = new File(collectionsDir, dirName);
             collectionDir.delete();
         }
 
         if (removedNode instanceof RuleSetNode) {
             CollectionNode collectionNode = (CollectionNode)parentNode;
             assert(collectionNode != null);
             File ruleSetFile = new File(collectionsDir,
                     makeDirectoryName(collectionNode.getName() +
                    File.separator +
                     makeFileName(((RuleSetNode)removedNode).getName())));
             if (false == ruleSetFile.delete()) {
                 System.out.println("Failed to delete rule set file: " + ruleSetFile.getAbsolutePath());
             }
             return;
         }
 
         assert(false);
     }
 
     public void treeStructureChanged(TreeModelEvent e) {
         System.out.println("file handler: detected node structure change this was not expected, exiting.");
         System.exit(1);
     }
 
     public FileHandler() {
         this.xStream = new XStream(new DomDriver());
         checkFiles();
     }
 }
