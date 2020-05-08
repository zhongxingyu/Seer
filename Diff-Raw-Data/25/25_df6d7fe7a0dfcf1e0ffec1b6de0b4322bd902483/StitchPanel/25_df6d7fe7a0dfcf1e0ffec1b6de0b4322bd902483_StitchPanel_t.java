 package com.my.splitter.swing;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.nio.file.Path;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.EtchedBorder;
 
 import com.my.splitter.file.FileUtils;
 import com.my.splitter.swing.model.PathObject;
 
 
 /**
  * Stitcher GUI panel
  * @author mgruzman
  *
  */
 @SuppressWarnings("serial")
 public class StitchPanel extends JPanel {
 	private JTextField textField;
 	private final JFileChooser fc = new JFileChooser();
 	private final DefaultListModel<PathObject> listModel = new DefaultListModel<PathObject>();
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private final JList fileList = new JList(listModel);
 	private final JButton btnClear = new JButton("Clear");
 	private final JButton removeBtn = new JButton("Remove");
 	private final JButton btnStitch = new JButton("Stitch");
 	
 
 
 	/**
 	 * Create the panel.
 	 */
 	public StitchPanel() {
 		setBounds(10, 26, 545, 273);
 		setLayout(null);
 		
 		textField = new JTextField();
 		textField.setBounds(10, 6, 425, 20);
 		textField.setColumns(10);
 		add(textField);
 		
 		JButton button = new JButton("Browse");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(fc.showDialog(StitchPanel.this, "Select") == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fc.getSelectedFile();
					textField.setText(selectedFile.getPath());
					populateFileList(selectedFile);
 				}
 			}
 		});
 		button.setBounds(445, 5, 90, 23);
 		add(button);
 		
 		JPanel fileInfoPanel = new JPanel();
 		fileInfoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		fileInfoPanel.setBounds(10, 37, 525, 225);
 		fileInfoPanel.setLayout(null);
 		add(fileInfoPanel);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(10, 36, 200, 178);
 		fileInfoPanel.add(scrollPane);
 		
 		listModel.addListDataListener(new ChunckListListener(listModel, btnClear, btnStitch));
 		fileList.addListSelectionListener(new FileListSelectionListener(removeBtn));
 		scrollPane.setViewportView(fileList);
 		
 		JLabel lblPartsList = new JLabel("Parts list");
 		lblPartsList.setBounds(28, 11, 65, 14);
 		fileInfoPanel.add(lblPartsList);
 		
 		btnStitch.setEnabled(false);
 		btnStitch.setBounds(426, 191, 89, 23);
 		fileInfoPanel.add(btnStitch);
 		
 		JButton btnAdd = new JButton("Add");
 		btnAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(fc.showDialog(StitchPanel.this, "Add") == JFileChooser.APPROVE_OPTION) {
 					int index = fileList.getSelectedIndex();
 					if(index < 0) { index = listModel.getSize(); } else { index += 1; }
 					listModel.insertElementAt(new PathObject(fc.getSelectedFile().toPath()), index);
 				}
 			}
 		});
 		btnAdd.setBounds(220, 36, 89, 23);
 		fileInfoPanel.add(btnAdd);
 		
 		removeBtn.setEnabled(false);
 		removeBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int index = fileList.getSelectedIndex();
 				if(index >= 0) {
 					listModel.removeElementAt(index);
					resetPanel();
 				}
 			}
 		});
 		removeBtn.setBounds(220, 70, 89, 23);
 		fileInfoPanel.add(removeBtn);
 		
 		btnClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				resetPanel();
 				listModel.clear();
 			}
 		});
 		btnClear.setBounds(220, 104, 89, 23);
 		btnClear.setEnabled(false);
 		fileInfoPanel.add(btnClear);
 		
 
 	}
 
 
	private void resetPanel() {
		textField.setText("");
	}


 	private void populateFileList(File selectedFile) {
 		List<Path> list = FileUtils.guessFileList(selectedFile.toPath());
		listModel.clear();
 		for(Path p : list) {
 			listModel.insertElementAt(new PathObject(p), listModel.getSize()); 
 		}
 	}
 }
