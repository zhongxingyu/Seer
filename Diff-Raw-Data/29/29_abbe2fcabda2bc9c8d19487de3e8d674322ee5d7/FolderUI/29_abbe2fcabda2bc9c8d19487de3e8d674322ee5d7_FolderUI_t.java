 package com.ceng316.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.nio.file.Paths;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.GroupLayout.Alignment;
 
 
 import javax.swing.JLabel;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 import com.ceng316.controller.MultiReaderController;
 import com.ceng316.model.MetaReader.RETCODES;
 import javax.swing.JCheckBox;
 
 @SuppressWarnings("serial")
 public class FolderUI extends JPanel implements ActionListener{
 	private FileData fileData;
 	private JPanel folderPanel;
 	private JTextField txtFolderPath;
 	private JButton btnChooseFolder;
 	private JButton btnGetMetadata;
 	private JComboBox<String> fileNames;
 	private MultiReaderController multiReader;
 	private JLabel lblFilesInfo;
 	private JLabel lblChooseFile;
 	private JButton btnSaveMetadata;
 	private JCheckBox chckbxSaveAll;
 	
 	public FolderUI() {
 		
 		folderPanel = new JPanel();
 		fileData = new FileData();
 		
 		txtFolderPath = new JTextField();
 		txtFolderPath.addActionListener(this);
 
 		btnChooseFolder = new JButton("Choose Folder");		
 		btnChooseFolder.addActionListener(this);
 
 		btnGetMetadata = new JButton("Get Metadata");
 		btnGetMetadata.addActionListener(this);
 		
 		fileNames = new JComboBox<String>();
 		fileNames.addActionListener(this);
 		
 		lblFilesInfo = new JLabel("");
 		lblFilesInfo.setVisible(false);
 		
 		lblChooseFile = new JLabel("Choose File");
 		
 		btnSaveMetadata = new JButton("Save Metadata");
 		btnSaveMetadata.addActionListener(this);
 		
 		chckbxSaveAll = new JCheckBox("Save ALL");
 		
 		GroupLayout gl_filePanel = new GroupLayout(folderPanel);
 		gl_filePanel.setHorizontalGroup(
 			gl_filePanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_filePanel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_filePanel.createParallelGroup(Alignment.LEADING, false)
 						.addGroup(gl_filePanel.createSequentialGroup()
 							.addComponent(btnGetMetadata, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblFilesInfo)
 							.addGap(16))
 						.addComponent(lblChooseFile)
 						.addComponent(fileData, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
 						.addGroup(gl_filePanel.createSequentialGroup()
 							.addGroup(gl_filePanel.createParallelGroup(Alignment.TRAILING, false)
 								.addComponent(fileNames, Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, 380)
 								.addComponent(txtFolderPath, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
 							.addGroup(gl_filePanel.createParallelGroup(Alignment.LEADING)
 								.addGroup(gl_filePanel.createSequentialGroup()
 									.addGap(18)
 									.addComponent(btnChooseFolder, GroupLayout.DEFAULT_SIZE, 140, GroupLayout.PREFERRED_SIZE))
 								.addGroup(gl_filePanel.createSequentialGroup()
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addGroup(gl_filePanel.createParallelGroup(Alignment.LEADING)
 										.addComponent(chckbxSaveAll)
 										.addComponent(btnSaveMetadata, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE))))))
 					.addContainerGap(62, Short.MAX_VALUE))
 		);
 		gl_filePanel.setVerticalGroup(
 			gl_filePanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_filePanel.createSequentialGroup()
 					.addGap(37)
 					.addGroup(gl_filePanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(txtFolderPath, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
 						.addComponent(btnChooseFolder, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
 					.addGap(37)
 					.addGroup(gl_filePanel.createParallelGroup(Alignment.TRAILING)
 						.addGroup(gl_filePanel.createSequentialGroup()
 							.addGroup(gl_filePanel.createParallelGroup(Alignment.BASELINE)
 								.addComponent(btnGetMetadata, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblFilesInfo))
 							.addGap(18)
 							.addComponent(lblChooseFile))
 						.addComponent(chckbxSaveAll))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_filePanel.createParallelGroup(Alignment.LEADING, false)
 						.addComponent(btnSaveMetadata, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(fileNames, GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
 					.addGap(18)
 					.addComponent(fileData, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		folderPanel.setLayout(gl_filePanel);
 
 	}
 
 
 	public JPanel getFolderPanel(){
 		return this.folderPanel;
 	}
 
 	public void actionPerformed(ActionEvent e) {		
 		
 		if(e.getSource() == btnChooseFolder){
 			JFileChooser chooser = new JFileChooser();
 			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 							
 			int returnVal = chooser.showOpenDialog(null);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				txtFolderPath.setText(chooser.getSelectedFile().getAbsolutePath());
 			}
 		}
 		
 		if(e.getSource() == btnGetMetadata){
 			lblFilesInfo.setVisible(false);
 			fileNames.removeAllItems();	
 			multiReader  = new MultiReaderController(this);
 			
 			if (!txtFolderPath.getText().isEmpty()){
 				multiReader.analyseMultiFiles(Paths.get(txtFolderPath.getText()));
 				
 				if(multiReader.getRetCode() == RETCODES.SUCCESS){
 					try{
 						fileNames.setSelectedIndex(0);
 						lblFilesInfo.setVisible(true);
 						lblFilesInfo.setText("Total number of files is " + fileNames.getItemCount());
 					}catch (IllegalArgumentException ie) {
 						setMsgBox("There is no supported file in the selected directory.");
 					}
 				}
 			}else{
 				fileData.clearText();
 				setMsgBox("Choose a folder directory");
 			}
 		}
 		
 		if(e.getSource() == fileNames){
 			fileData.clearText();
 			multiReader.fillFileData(getFileData(), fileNames.getSelectedIndex());
 		}
 		
 		if (e.getSource() == btnSaveMetadata){
			if(multiReader != null && fileNames.getItemCount() > 0){
 				JFileChooser chooser = new JFileChooser();
 				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 								
 				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 					if(chckbxSaveAll.getSelectedObjects() != null)
 						multiReader.saveAllMetadata(chooser.getSelectedFile().getAbsolutePath());
 					else
 						multiReader.saveMetadata(chooser.getSelectedFile().getAbsolutePath(), fileNames.getSelectedIndex());
 				}
 			}
 		}
 		
 	}
 
 	public void addFileNames(String file, int index) {
 		fileNames.insertItemAt(file, index);
 	}
 	
 	public FileData getFileData() {
 		return fileData;
 	}
 	
 	public void setMsgBox(String errorMsg){
 		fileData.clearText();
 		lblFilesInfo.setVisible(false);
		
 		JOptionPane.showMessageDialog(folderPanel, errorMsg, null, JOptionPane.INFORMATION_MESSAGE);
 	}
 }
