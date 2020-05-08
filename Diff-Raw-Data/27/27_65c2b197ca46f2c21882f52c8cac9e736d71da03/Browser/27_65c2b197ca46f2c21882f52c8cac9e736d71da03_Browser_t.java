 package gui;
 
 import gui.util.FileList;
 import gui.util.FileTreePanel;
 import gui.util.RelativeLayoutPanel;
 import gui.util.FileTreePanel.FileTreeNode;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 
 import main.Overview;
 
 /**
  * Klasse fuer den FileBrowser
  */
 public class Browser extends RelativeLayoutPanel {
 	
 	private static final long serialVersionUID = 1L;
 	
 	/** JList die Musikdateien anzeigt */
 	FileList musicFileList;
 	
 	public Browser() {
 		loadFileView();
 		Overview.setBrowser(this);
 	}
 	
 	
 	/** erstellt den refresh button und fuegt ihn zum panel hinzu */
 	private void buildRefreshBttn(){
 		JButton refreshButton = new JButton(new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				reloadFileView();
 			}
 		});
 		refreshButton.setText("refresh");
 		this.add(refreshButton, 0f, 0f, false, 0.5f, 0.1f);
 	}
 	
 	
 	/**
 	 * startet das Laden des FileTreePanels.
 	 * dazu wird ein seperater thread erstellt der das uebernimmt,
 	 * da dies evtl viel Zeit in anspruch nimmt.
 	 */
 	private void loadFileView() {
 		// loading label als platzhalter
 		final JLabel loadingLabel = new JLabel("loading Filesystem...", JLabel.CENTER);
 		this.add(loadingLabel, 0.5f,0.5f,true,0.8f,0.8f);
 		buildRefreshBttn();
 		
 		// loader thread
 		Thread loader = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				// components erstellen
 				musicFileList = new FileList();
 				final FileTreePanel folderPane = new FileTreePanel();
 				final JScrollPane filePane = new JScrollPane(musicFileList);
 				// scrollbars der panes anzeigen
 				folderPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 				filePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 				// listener des trees
 				folderPane.setTreeSelectionListener(new TreeSelectionListener() {
 					
 					@Override
 					// listet dateien des gewaehlten ordners in der musicFileList auf
 					public void valueChanged(TreeSelectionEvent e) {
 						FileTreeNode node = (FileTreeNode)
 								folderPane.getTree().getLastSelectedPathComponent();
						if(node != null && !node.isTreeRoot()){
 							System.out.println(node.getFile());
 							if(node.getFile().exists()){
 								musicFileList.setListData(node.getFile().listFiles());
 							} else {
 								// wenn ausgewaehlter ordner nichtmehr existiert
 								folderPane.reloadTree();
 							}
 						}
 						
 					}
 				});
 				// platzhalter label ersetzen
 				remove(loadingLabel);
 				add(folderPane, 0f, 0.1f, false, 0.5f, 0.9f);
 				add(filePane, 0.5f, 0f, false, 0.5f, 1);
 				// frame erneuern durch kurzes verstecken
 				Overview.getMainFrame().setVisible(false);
 				Overview.getMainFrame().setVisible(true);
 			}
 		});
 		// loader thread starten
 		loader.start();
 	}
 	
 	
 	private void reloadFileView(){
 		for(Component comp :this.getComponents()){
 			this.remove(comp);
 		}
 		loadFileView();
 	}
 	
 	/** test */
 	public static void main(String[] args) {
 		JFrame myframe = new JFrame();
 		myframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		myframe.add(new Browser());
 		myframe.setSize(500, 400);
 		myframe.setVisible(true);
 		Overview.setMainFrame(myframe);
 	}
 	
 	
 }
