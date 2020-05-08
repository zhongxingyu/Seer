 package podsalinan;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Vector;
 
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JSpinner;
 import javax.swing.JTree;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 /**
  * @author bugman
  *
  */
 public class CommandPass implements ActionListener {
 	private JTree tree;
 	private Podsalinan bgdownloader;
 	private Vector<ProgSettings> progSettings;
 	@SuppressWarnings("unused")
 	private boolean programExiting;
 
 	public CommandPass(boolean programExiting, Vector<ProgSettings> progSettings) {
 		this.programExiting=programExiting;
 		this.progSettings=progSettings;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		String command = event.getActionCommand();
 		
 		if (command.compareTo("quit")==0){
 			programExiting=true;
 			bgdownloader.saveSettings();
 			System.exit(0);
 		}
 		if (command.compareTo("addURL")==0){
 			addURL(false);
 		}
 		if (command.compareTo("addRSS")==0){
 			addURL(true);
 		}
 		if (command.compareTo("setDownloadFolder")==0){
 			// Changing Podcast Download Folder 
 			Podcast podcast = getSelectedPodcast();
 			String newDirectory = changeDirectory();
 			if (newDirectory!=null){
 				podcast.setLocalStore(newDirectory);
 			}
 		}
 		if (command.compareTo("removeFeed")==0){
 			// Removing Podcast from the system
 			Podcast podcast = getSelectedPodcast();
 		    podcast.remove=true;
 		    	
 		    // Following 2 lines remove the podcast from the tree.
 		    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 		    model.removeNodeFromParent((DefaultMutableTreeNode)tree.getLastSelectedPathComponent());
 		    bgdownloader.getTreePane().changeDownloadList("Downloads");
 		}
 		
 		if (command.compareTo("updateInterval")==0){
 			dialogSpinner("updateTimer", "Update Value", "Mins ", 10, 360, 10);
 		}
 		if (command.compareTo("setURLFolder")==0){
 			URLDownloadList urlDownloads = bgdownloader.getDownloadList();
 			
 			String newDirectory = changeDirectory();
 			if (newDirectory!=null){
 				urlDownloads.setDirectory(newDirectory);
				boolean found=false;
				for (int psc=0; psc < progSettings.size(); psc++){
					if (progSettings.get(psc).setting.equals("urlDirectory")){
						progSettings.get(psc).value=newDirectory;
						found=true;
					}
				}
				if (!found){
					ProgSettings newSetting = new ProgSettings("urlDirectory",newDirectory);
					progSettings.add(newSetting);
				}
 			}		
 		}
 		if (command.compareTo("deleteURL")==0){
 			bgdownloader.getDownloadList().removeDownload();
 		}
 		if (command.compareTo("numPodcasts")==0){
 			dialogSpinner("maxPodcastDownloaders", "Maximum Podcast Downloaders", "", 1,30,1);
 		}
 		if (command.compareTo("numDownloads")==0){
 			dialogSpinner("maxDownloaders", "Maximum URL Downloaders", "",1,30,1);
 		}
 	}
 
 	/**
 	 * This creates a document
 	 * @param valToChange
 	 */
 	public void dialogSpinner(String valToChange, String dialogTitle, String dialogMessage, int start, int end, int interval){
 		boolean valFound=false;
 		int value;
 		
 		int psc=0;
 		while ((!valFound)&&(psc < progSettings.size())){
 			if (progSettings.get(psc).setting.equals(valToChange)){
 				valFound=true;
 			} else
 				psc++;
 		}
 		
 		if (valFound)
 			value=Integer.parseInt(progSettings.get(psc).value);
 		else
 			value=10;
 		// Following if statement added if error in database
 		if (value==0)
 			value=start;
 		
 		JSpinner spinner = new JSpinner(new SpinnerNumberModel(value,start,end,interval));
 		Object[] message = {dialogMessage, spinner};
 		JOptionPane updateValWindow= new JOptionPane(message,
 													JOptionPane.PLAIN_MESSAGE,
 													JOptionPane.OK_CANCEL_OPTION,
 													null,null);
 		JDialog dialog = updateValWindow.createDialog(bgdownloader, dialogTitle);
 		dialog.setVisible(true);
 		if (valFound)
 			progSettings.get(psc).value=spinner.getValue().toString();
 		else {
 			ProgSettings newSetting = new ProgSettings(valToChange,
 														spinner.getValue().toString());
 			progSettings.add(newSetting);
 		}
 	}
 	
 	public Podcast getSelectedPodcast(){
 		// Grab the selected Node, so we can change it's download folder.
 		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
 		if (selectedNode!=null){
 			// Make an Object out of the selected node.
 			Object nodeInfo = selectedNode.getUserObject();
 			// Read below. if the node is a Leaf, not a branch, and it's also an RssFeed then we want to delete it. 
 		    if (selectedNode.isLeaf() && (nodeInfo instanceof Podcast)){
 		    	// Grab the object so we can delete the file and the entry
 		    	// in the podcast databast
 		    	Podcast podcast = (Podcast) selectedNode.getUserObject();
 		    	return podcast;
 		    }
 		}
 		return null;
 	}
 	
 	/**
 	 * This function creates and shows a directory dialog window for the user to
 	 * select a directory.
 	 * @return String - returns the new directory.
 	 */
 	public String changeDirectory(){
     	// Create and Show a Directory Dialog for the user to choose the feed
     	// directory.
     	JFileChooser browseWindow= new JFileChooser();
     	// Sets what is able to be selected. in this case, a folder.
     	browseWindow.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
     	// bwr is browser window result
     	int bwr = browseWindow.showOpenDialog(bgdownloader);
     	
     	if (bwr==JFileChooser.APPROVE_OPTION){
 			// Don't need to test if chosen Dir is a folder, as it has already
     		// been tested in the if statement.
     		File chosenDir = browseWindow.getSelectedFile();
     		
     		return chosenDir.toString();
     			
     	} else
     		return null;
 	}
 	
 	/**
 	 * This combines the two addURL functions for Add Download URL and Add Podcast
 	 * @param podcast - false : url is just a file to download
 	 * 				  - true  : url is podcast
 	 */
 	public void addURL(boolean podcast){
 		String url = (String)JOptionPane.showInputDialog(
                 bgdownloader,
                 "Please enter the URL to download",
                 "Add URL",
                 JOptionPane.PLAIN_MESSAGE,
                 null,
                 null,
                 null);
 		if (url!=null)
 			if (!url.isEmpty())
 				if (podcast)
 					bgdownloader.addRssFeed(url);
 				else 
 					bgdownloader.addDownload(url);
 	}
 
 	/**
 	 * setTree - grab a copy of the Tree in the tree pane, so we can work with it
 	 * @param mainTree
 	 */
 	public void setTree(JTree mainTree){
 		tree = mainTree;
 	}
 	
 	public void setParent(Podsalinan parent){
 		bgdownloader = parent;
 	}
 }
