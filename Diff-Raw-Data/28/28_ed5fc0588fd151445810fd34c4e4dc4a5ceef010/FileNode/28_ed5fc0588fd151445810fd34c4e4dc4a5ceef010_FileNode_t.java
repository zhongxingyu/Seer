 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.demo.filebrowser;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Arrays;
 import java.util.List;
 import org.demo.fileservice.Folder;
 import org.openide.nodes.AbstractNode;
 import org.openide.nodes.ChildFactory;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.Lookups;
 
 /**
  *
  * @author edwin
  */
 public class FileNode extends AbstractNode {
 
     public FileNode(Children children, Lookup lookup) {
         super(children, lookup);
     }
 
     public FileNode(Children children) {
         super(children);
     }
 
     private static final class CustomFileFilter implements FileFilter {
 
         @Override
         public boolean accept(File pathname) {
             return pathname.isFile();
         }
     };
 
     public static class FileNodeChildren extends ChildFactory<File> {
 
         private List<File> src = null;
 
         public FileNodeChildren(Folder src) {
            if (src != null) {
                this.src = Arrays.asList(src.listFiles(new CustomFileFilter()));
            }
         }
 
         @Override
         protected boolean createKeys(List<File> toPopulate) {
             System.out.println("------------------");
            if (src != null) {
                toPopulate.addAll(src);
            }
             System.out.println(this.src);
             return true;
         }
 
         @Override
         protected Node createNodeForKey(File key) {
            Node node = new FileNode(Children.LEAF, Lookups.singleton(key));
             node.setDisplayName(key.getName());
             node.setShortDescription(key.getName());

             return node;
         }
     }
 }
