 package org.bio5.irods.imagej.views;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTree;
 import javax.swing.JViewport;
 import javax.swing.border.LineBorder;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 import org.bio5.irods.imagej.bean.IrodsImageJ;
 import org.bio5.irods.imagej.fileoperations.FileOperations;
 import org.bio5.irods.imagej.fileoperations.GetFileFromIrods;
 import org.bio5.irods.imagej.listeners.MyTreeModelListener;
 import org.bio5.irods.imagej.utilities.Constants;
 import org.bio5.irods.imagej.utilities.IrodsTransferStatusCallbackListener;
 import org.bio5.irods.imagej.utilities.IrodsUtilities;
 //import org.eclipse.wb.swing.FocusTraversalOnArray;
 import org.irods.jargon.core.connection.IRODSAccount;
 import org.irods.jargon.core.exception.JargonException;
 import org.irods.jargon.core.exception.OverwriteException;
 import org.irods.jargon.core.pub.DataTransferOperations;
 import org.irods.jargon.core.pub.IRODSFileSystem;
 import org.irods.jargon.core.pub.UserAO;
 import org.irods.jargon.core.pub.io.IRODSFile;
 import org.irods.jargon.core.pub.io.IRODSFileFactory;
 
 public class DirectoryContentsPane extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 722996165620904921L;
 	private String irodsZone;
 	public IRODSFileSystem irodsFileSystem;
 	public UserAO  userAccount;
 	private DataTransferOperations dataTransferOperationsAO;
 	final IRODSFileFactory iRODSFileFactory;
 	private String selectedNodeInTreeForDoubleClick;
 	private String selectedNodeInTreeForSingleClick;
 	private GetFileFromIrods getFile;
 	private JProgressBar progressBar;
 	private JFileChooser chooser;
 
 	private DefaultTreeModel treeModel;
 	private JTree userDirectoryTree;
 	private DefaultMutableTreeNode rootNode;
 
 	private JViewport viewport;
 	private JScrollPane scrollPane;
 	private IRODSAccount irodsAccount;
 	private IrodsTransferStatusCallbackListener irodsTransferStatusCallbackListener;
 
 	/*Logger instantiation*/
 	static Logger log = Logger.getLogger(
 			DirectoryContentsPane.class.getName());
 
 	/**
 	 * Create the panel.
 	 * @throws JargonException 
 	 */
 
 	public DirectoryContentsPane(List<String> ContentsInHome,final IRODSFile irodsAccountFile, final IrodsImageJ irodsImagej) throws JargonException {
 		log.info("Local path before refactoring: " +Constants.IMAGEJ_LOCAL_WORKING_DIRECTORY);
 		//Constants.IMAGEJ_LOCAL_WORKING_DIRECTORY.replaceAll(IrodsUtilities.pathSeperator(), "//");
 		log.info("Local directory to store ImageJ files: " +Constants.IMAGEJ_LOCAL_WORKING_DIRECTORY);
 		irodsAccount= irodsImagej.getIrodsAccount();
 		iRODSFileFactory= FileOperations.getIrodsAccountFileFactory(irodsImagej.getIrodsAccount());
 		String irodsZone =irodsAccount.getZone();
 		System.out.println("irodsZone" +irodsZone);
 		rootNode = new DefaultMutableTreeNode(Constants.HOME);
		
 		/*Setting iRODS file system*/
 		irodsFileSystem= IRODSFileSystem.instance();
 		irodsImagej.setIrodsFileSystem(irodsFileSystem);
 
 		userAccount = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(irodsAccount);
 		dataTransferOperationsAO =  irodsFileSystem
 				.getIRODSAccessObjectFactory().getDataTransferOperations(
 						irodsAccount);
 
 		scrollPane = new javax.swing.JScrollPane();
 		viewport = scrollPane.getViewport(); 
 		final JLabel jTextField_sourceFile = new JLabel("Local file");
 		final JLabel jTextField_destinationPath = new JLabel("Destination");
 		final JButton jButton_saveToIrodsServer = new JButton("Save to iRODS Server");
 
 		final File localFiles = (File) irodsAccountFile;
 		parseDirectoryContents(iRODSFileFactory, localFiles, rootNode, irodsAccount);
		treeModel = new DefaultTreeModel(rootNode,true);
		treeModel.addTreeModelListener(new MyTreeModelListener());
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 
 		GroupLayout groupLayout = new GroupLayout(this);
 		groupLayout.setHorizontalGroup(
 				groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 402, GroupLayout.PREFERRED_SIZE)
 						.addGap(18)
 						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
 						.addGap(18))
 				);
 		groupLayout.setVerticalGroup(
 				groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 										.addGap(44)
 										.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
 										.addGroup(groupLayout.createSequentialGroup()
 												.addContainerGap()
 												.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)))
 												.addContainerGap())
 				);
 
 		JPanel panel = new JPanel();
 		tabbedPane.addTab("File Operations", null, panel, null);
 
 		progressBar = new JProgressBar();
 		progressBar.setStringPainted(true);
 		progressBar.setToolTipText("Progress of action");
 		JButton jButton_selectLocalFile = new JButton("Select local file");
 
 
 		jButton_selectLocalFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				chooser = new JFileChooser();
 				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 				int option = chooser.showOpenDialog(DirectoryContentsPane.this);
 				if (option == JFileChooser.APPROVE_OPTION) {
 					jTextField_sourceFile.setText(((chooser.getSelectedFile()!=null)?
 							chooser.getSelectedFile().getAbsolutePath():"nothing is selected"));
 				}
 				else {
 					jTextField_sourceFile.setName("File selection canceled !");
 				}
 			}
 		});
 
 
 		jTextField_sourceFile.setEnabled(false);
 		JButton jButton_selectDestination = new JButton("Select Destination");
 		jButton_selectDestination.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent destinationButtonActionEvent) {
 
 				String destinationFolderPath = selectedNodeInTreeForSingleClick;
 				/*if(selectedNodeInTreeForSingleClick.contains("."))
 				{
 					log.info("Destination before Splitting: " +selectedNodeInTreeForSingleClick);
 					File destinationFile =new File(selectedNodeInTreeForSingleClick);
 					destinationFolderPath =destinationFile.getParent();
 					log.info("Destination after Splitting: " +destinationFolderPath);
 				}*/
 				jTextField_destinationPath.setText(destinationFolderPath);
 				jButton_saveToIrodsServer.setEnabled(true);
 			}
 		});
 
 		jTextField_destinationPath.setEnabled(false);
 
 
 		jButton_saveToIrodsServer.setEnabled(false);
 		jButton_saveToIrodsServer.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					log.info("Save to iRODS Server - Button Clicked");
 					String sourceFilePath=null;
 					String destinationFilePath=null;
 					String targetResourceName = "";
 					targetResourceName = irodsImagej.getIrodsAccount().getDefaultStorageResource();
 					File sourceLocalfile = null;
 					IRODSFile destinaitonIrodsFile = null;
 					if(chooser.getSelectedFile().getAbsolutePath()!=null && chooser.getSelectedFile().getAbsolutePath()!=""){
 						sourceFilePath=chooser.getSelectedFile().getAbsolutePath();
 						sourceLocalfile=new File(sourceFilePath);
 						if(selectedNodeInTreeForSingleClick!=null && selectedNodeInTreeForSingleClick!=""){
 							System.out.println("destination path || selectedNodeInTreeForSingleClick" +selectedNodeInTreeForSingleClick);
 							destinationFilePath=IrodsUtilities.pathSeperator() +irodsAccount.getZone()+IrodsUtilities.pathSeperator()+jTextField_destinationPath.getText();
 							destinaitonIrodsFile = iRODSFileFactory.instanceIRODSFile(destinationFilePath);
 							System.out.println("sourceLocalfile absolute path: " +sourceLocalfile.getAbsolutePath() +"\n"  +"destinaitonIrodsFile absolutepath: " +destinaitonIrodsFile.getAbsoluteFile());
 							try{
 								dataTransferOperationsAO.putOperation(sourceLocalfile.getAbsolutePath(),destinaitonIrodsFile.getAbsolutePath(),targetResourceName,irodsTransferStatusCallbackListener,null);
 								JOptionPane.showMessageDialog(null, "File Transfer done successfully");
 
 								DefaultMutableTreeNode parentNode = null;
 								
 								/*Destination selection - Fetching parent path if leaf node is selected*/
 								parentNode = (DefaultMutableTreeNode)
 										userDirectoryTree.getLastSelectedPathComponent();
 								 if (parentNode.isLeaf()) {
 									 parentNode= (DefaultMutableTreeNode) parentNode.getParent();
 								    } 
 								 
 							/*	String filePath= IrodsUtilities.createFilePathFromTreePath(parentPath);
 								if(filePath.contains(".")){
 									parentPath=parentPath.getParentPath();
 								}
 								System.out.println("selection path " +parentPath);
 								if (parentPath == null) {
 									parentNode = rootNode;
 								} else  {
 									parentNode = (DefaultMutableTreeNode)
 											(parentPath.getLastPathComponent());
 								}*/
 
 								addObject(parentNode, sourceLocalfile.getName(), true);
 							}
 							catch(OverwriteException overwriteException){
 								log.error(overwriteException.getMessage());
 								JOptionPane.showMessageDialog(null, "Selected file already exists in destination path !");
 							}
 						}
 					}
 					else{
 						JOptionPane.showMessageDialog(null, "Source is empty!");
 						log.error("Source is empty!");
 					}
 
 				} catch (JargonException jargonException) {
 					log.error(jargonException.getMessage());
 					jargonException.printStackTrace();
 				}
 			}
 		});
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 				gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 								.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel.createSequentialGroup()
 										.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 												.addComponent(jButton_selectLocalFile)
 												.addComponent(jButton_selectDestination))
 												.addGap(27)
 												.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 														.addComponent(jTextField_destinationPath)
 														.addComponent(jTextField_sourceFile)))
 														.addComponent(jButton_saveToIrodsServer))
 														.addContainerGap(83, Short.MAX_VALUE))
 				);
 		gl_panel.setVerticalGroup(
 				gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addGap(59)
 						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 								.addComponent(jButton_selectLocalFile)
 								.addComponent(jTextField_sourceFile))
 								.addGap(18)
 								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 										.addComponent(jButton_selectDestination)
 										.addComponent(jTextField_destinationPath))
 										.addGap(18)
 										.addComponent(jButton_saveToIrodsServer)
 										.addContainerGap(134, Short.MAX_VALUE))
 				);
 		panel.setLayout(gl_panel);
 
 		JPanel panel_1 = new JPanel();
 		tabbedPane.addTab("File Information", null, panel_1, null);
 
 		userDirectoryTree= new JTree(treeModel);
 		userDirectoryTree.setToolTipText("Directory list");
 		userDirectoryTree.setVisibleRowCount(100);
 		userDirectoryTree.setBorder(new LineBorder(new Color(0, 0, 0)));
 
 
 		scrollPane.setViewportView(userDirectoryTree);
 		userDirectoryTree.setModel(treeModel);
 		userDirectoryTree.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent mouseEvent) {
 
 				if(mouseEvent.getClickCount()==2){
 					/*resetting progress bar to 0 if a double click is detected*/
 					progressBar.setValue(0);
 					log.info("tree path after double click" +selectedNodeInTreeForDoubleClick);
 					System.out.println("double click selection: " +selectedNodeInTreeForDoubleClick);
 
 					selectedNodeInTreeForDoubleClick=IrodsUtilities.getJtreeSelection(mouseEvent,userDirectoryTree );
 					//getImageFile(iRODSFileFactory, selectedNodeInTreeForDoubleClick,irodsAccount);
 					getFile= new GetFileFromIrods(iRODSFileFactory, selectedNodeInTreeForDoubleClick, irodsImagej, progressBar);
 					getFile.execute();
 				}
 				else if(mouseEvent.getClickCount()==1){
 					//resetting progress bar to 0 if a single click is detected
 					progressBar.setValue(0);
 					selectedNodeInTreeForSingleClick=IrodsUtilities.getJtreeSelectionForSingleClick(mouseEvent,userDirectoryTree);
 					//log.info("Single click selection: " +selectedNodeInTreeForSingleClick);
 				}
 			}
 		});
 
 		userDirectoryTree.setShowsRootHandles(true);
 		userDirectoryTree.setEditable(true);
 		userDirectoryTree.setVisible(true);
 		viewport.add(userDirectoryTree);
 		setLayout(groupLayout);
 
 	}
 
 
 	public void parseDirectoryContents(final IRODSFileFactory iRODSFileFactory,final File irodsAccountFile, DefaultMutableTreeNode node, final IRODSAccount irodsAccount)
 	{
 
 		if(!irodsAccountFile.isDirectory()){
 			System.out.println("File name" +irodsAccountFile.getName() +":" +irodsAccountFile.getAbsolutePath());
 			DefaultMutableTreeNode child = new DefaultMutableTreeNode(irodsAccountFile.getName(),false);
 			node.add(child);
 		}
 
 		if(irodsAccountFile.isDirectory()){
 			System.out.println("Direc name" + irodsAccountFile.getName());
			log.info("Direc name" + irodsAccountFile.getName());
 			DefaultMutableTreeNode child = new DefaultMutableTreeNode(irodsAccountFile.getName(),true);
 			node.add(child);
 			File[] direcFiles=irodsAccountFile.listFiles();
 			for(int i=0;i<direcFiles.length;i++){
 				System.out.println("File number" +i +"\n depth:" +direcFiles.length);
				log.info("File number" +i +"\n depth:" +direcFiles.length);
 				parseDirectoryContents(iRODSFileFactory, direcFiles[i], child, irodsAccount);
 			}
 		}
 
 		viewport.removeAll();
 		viewport.setVisible(true);
 		viewport.repaint();
 		viewport.revalidate();
 		add(scrollPane,BorderLayout.CENTER);
 		setVisible(true);
 		revalidate();
 		repaint();
 	}
 
 /*	public DefaultMutableTreeNode parseDirectoryContentsForNodeUpdate(File contentsOfIrodsUpdatedDirectory, DefaultMutableTreeNode node)
 	{
 
 		if(!contentsOfIrodsUpdatedDirectory.isDirectory()){
 			System.out.println("File name" +contentsOfIrodsUpdatedDirectory.getName() +":" +contentsOfIrodsUpdatedDirectory.getAbsolutePath());
 			DefaultMutableTreeNode child = new DefaultMutableTreeNode(contentsOfIrodsUpdatedDirectory.getName(),false);
 			node.add(child);
 		}
 
 		if(contentsOfIrodsUpdatedDirectory.isDirectory()){
 			System.out.println("Direc name" + contentsOfIrodsUpdatedDirectory.getName());
 			DefaultMutableTreeNode child = new DefaultMutableTreeNode(contentsOfIrodsUpdatedDirectory.getName(),true);
 			node.add(child);
 			File[] direcFiles=contentsOfIrodsUpdatedDirectory.listFiles();
 			for(int i=0;i<direcFiles.length;i++){
 				System.out.println("File number" +i +"\n depth:" +direcFiles.length);
 				parseDirectoryContentsForNodeUpdate(direcFiles[i], child);
 			}
 		}
 		return node;
 	}*/
 
 	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
 			Object child, 
 			boolean shouldBeVisible) {
 		DefaultMutableTreeNode childNode =null;
 		try{
 			childNode = 
 					new DefaultMutableTreeNode(child, false);
 
 			if (parent == null) {
 				parent = rootNode;
 			}
 
 			treeModel.insertNodeInto(childNode, parent, 
 					parent.getChildCount());
 
 			if (shouldBeVisible) {
 				userDirectoryTree.scrollPathToVisible(new TreePath(childNode.getPath()));
 			}
 		}
 		catch(IllegalStateException illegalStateException){
 			log.error(illegalStateException.getMessage());
 			JOptionPane.showMessageDialog(null, "node does not allow children");
 		}
 		return childNode;
 	}
 }
