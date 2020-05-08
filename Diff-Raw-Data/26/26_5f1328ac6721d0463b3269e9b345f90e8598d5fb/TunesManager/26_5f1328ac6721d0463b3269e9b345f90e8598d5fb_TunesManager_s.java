 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.q1cc.cfs.tusync;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.JOptionPane;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import net.q1cc.cfs.tusync.struct.*;
 import xmlwise.Plist;
 import xmlwise.XmlParseException;
 
 /**
  *
  * @author cfstras
  */
 public class TunesManager {
 
     private Main main;
     public ArrayList<Playlist> playlists;
     public HashMap<Integer,Title> titles;
     
     TunesModel libModel;
 
     public TunesManager() {
         main = Main.instance();
         playlists = new ArrayList<Playlist>(32);
         titles = new HashMap<Integer, Title>(256);
         libModel = new TunesModel();
     }
 
     public void loadLibrary() {
         new Thread() {
             @Override
             public void run() {
                 try {
                     Main.instance().tunesManager.loadLibraryE();
                 } catch (TunesParseException e) {
                     System.out.println("Parse Error: " + e);
                 } catch (XmlParseException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 } finally {
                     main.gui.progressBar.setIndeterminate(false);
                     main.gui.progressBar.setString("");
                     main.gui.progressBar.setValue(0);
                 }
             }
         }.start();
     }
 
     public void loadLibraryE() throws XmlParseException, IOException, TunesParseException {
         
         main.gui.progressBar.setIndeterminate(true);
 
         String path = main.props.getProperty("lib.basepath");
         if (path == null) {
             JOptionPane.showMessageDialog(main.gui.frame, "Please select the path to your iTunes library first.");
             //TODO open dialog
             return;
         }
         File tunesFolder = new File(path);
         File xmlFile = new File(tunesFolder.getAbsolutePath() + System.getProperty("file.separator") + "iTunes Music Library.xml");
         if (!xmlFile.exists()) {
             JOptionPane.showMessageDialog(main.gui.frame, "Sorry, but i couldn't find your Library XML file.");
             //TODO find reason why xml was not found
             return;
         }
         Map<String, Object> lib = Plist.load(xmlFile);
 
         for (Map.Entry<String, Object> e : lib.entrySet()) {
             System.out.println(e.getKey() + ": " + e.getValue().getClass());
         }
 
         loadTracks(lib);
         loadPlaylists(lib);
         main.gui.tree.setModel(libModel);
         libModel.fireUpdate();
         main.gui.tree.expandRow(1);
         //TODO serialize
         
         System.out.println("Fin.");
     }
     
     void loadTracks(Map<String, Object> lib) throws TunesParseException {
         Object tr = lib.remove("Tracks");
         HashMap<String, HashMap> tracks = null;
         if (tr instanceof HashMap) {
             tracks = (HashMap<String, HashMap>) tr;
         }
 
         if (tracks == null) {
             throw new TunesParseException("no tracks");
         }
         
         int numTracks = tracks.size();
         int tracksPerProgress = numTracks/100;
         int num=0;
         int progress = 0;
         main.gui.progressBar.setMinimum(0);
         main.gui.progressBar.setMaximum(200);
         main.gui.progressBar.setValue(0);
         main.gui.progressBar.setString("reading tracks");
         main.gui.progressBar.setIndeterminate(false);
         
         Iterator<HashMap> it = tracks.values().iterator();
         while(it.hasNext()) {
             HashMap<String,Object> obj = it.next();
             it.remove();
             if(!obj.containsKey("Track ID")) {
                 continue;
             }
             int id = (Integer) obj.get("Track ID");
             Title title = new Title(id);
             
             for(Entry<String,Object> e : obj.entrySet()) {
                 int ind = Title.getAttInd(e.getKey());
                 if(ind==-1) {
                     System.out.println("Error: no row named "+e.getKey());
                     continue;
                 }
                 title.attribs[ind] = e.getValue().toString(); //TODO is this more efficient if we save objects instead of strings?
             }
             titles.put(id, title);
             num++;
             if(num%tracksPerProgress==0 || num==numTracks) {
                 main.gui.progressBar.setValue(++progress);
                 main.gui.progressBar.setString("reading tracks: "+num+" / "+numTracks);
             }
             
         }
         //tracks loaded.
         
     }
     
     void loadPlaylists(Map<String, Object> lib) throws TunesParseException {
         //playlists!
         Object pl = lib.remove("Playlists");
         ArrayList<HashMap> lists = null;
         if (pl instanceof ArrayList) {
             lists = (ArrayList<HashMap>) pl;
         }
 
         if (lists == null) {
             throw new TunesParseException("no playlists");
         }
         
         int numLists = lists.size();
         int listsPerProgress = numLists/100;
         int num=0;
         int progress = 100;
         main.gui.progressBar.setMinimum(0);
         main.gui.progressBar.setMaximum(200);
         main.gui.progressBar.setValue(100);
         main.gui.progressBar.setString("reading playlists");
         main.gui.progressBar.setIndeterminate(false);
         
         Iterator<HashMap> it = lists.iterator();
         while(it.hasNext()) {
             HashMap list = it.next();
             it.remove();
             Playlist playlist = new Playlist(list.get("Name").toString(), (Integer)list.get("Playlist ID"));
             ArrayList<HashMap> entries = (ArrayList) list.get("Playlist Items");
             if(entries == null) {
                 //no entries.
                 continue;
             }
             Iterator<HashMap> listIt = entries.iterator();
             while(listIt.hasNext()) {
                 HashMap<String,Integer> entry = listIt.next();
                 playlist.addTitle(titles.get((Integer)entry.get("Track ID")));
             }
             playlists.add(playlist);
             System.out.println("Playlist "+playlist.title+": "+playlist.tracks.size()+" tracks.");
             num++;
             if (num % listsPerProgress == 0 || num == numLists) {
                 main.gui.progressBar.setValue(++progress);
                main.gui.progressBar.setString("reading tracks: " + num + " / " + numLists);
             }
         }
        
         //playlists read. yay.
     }
 
     private class TunesModel implements TreeModel {
         
         public String root = "Music";
         String playlistsNode = "Playlists";
         public ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>(2);
         
         public TunesModel() {
             
         }
 
         @Override
         public Object getRoot() {
             return root;
         }
 
         @Override
         public Object getChild(Object parent, int index) {
             if(parent == root) {
                 switch (index) {
                     case 0:
                         return "Playlists";
                     case 1:
                         return ""; //TODO tracks
                     default:
                         return null;
                 }
             } else if(parent == playlistsNode) {
                 return TunesManager.this.playlists.get(index);
             } else if(playlists.contains(parent)) {
                 //return one title
                 return null;
             }
             return null;
         }
 
         @Override
         public int getChildCount(Object parent) {
             if(parent == root) {
                 return 1;
             } else if(parent == playlistsNode) {
                 return TunesManager.this.playlists.size();
             } else if(TunesManager.this.playlists.contains(parent)){
                 return 0;
                 //return ((Playlist)parent).tracks.size();
             }
             return 0;
         }
 
         @Override
         public boolean isLeaf(Object node) {
             if(node == root || node == playlistsNode) {
                 return false;
             } else {
                 return true;
             }
         }
 
         @Override
         public void valueForPathChanged(TreePath path, Object newValue) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public int getIndexOfChild(Object parent, Object child) {
             //TODO
             return 0;
         }
 
         @Override
         public void addTreeModelListener(TreeModelListener l) {
             listeners.add(l);
         }
 
         @Override
         public void removeTreeModelListener(TreeModelListener l) {
             listeners.remove(l);
         }
         
         public void fireUpdate() {
             TreeModelEvent t = new TreeModelEvent(root, new TreePath(root));
             for(TreeModelListener l : listeners) {
                 l.treeStructureChanged(t);
             }
         }
         
     }
 }
