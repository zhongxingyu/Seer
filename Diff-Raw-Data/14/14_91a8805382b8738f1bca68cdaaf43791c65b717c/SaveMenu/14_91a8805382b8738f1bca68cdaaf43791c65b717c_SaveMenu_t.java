 package com.bugfullabs.mapeditor;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 
 
 
 public class SaveMenu implements ActionListener {
 	
 	private JFrame saveMenu;
 	private JButton saveButton;
 	private JSpinner spinLevelId;
 	private JSpinner spinLevelPackId;
 	private JPanel mSpinnerLPIPanel;
 	private JPanel mSpinnerLIPanel;
 	private JPanel mButtonPanel;
 	private JFileChooser mSave;
 	
 	
 	SaveMenu(JFrame frame){
 		
 		saveMenu = new JFrame();
 		mSpinnerLPIPanel = new JPanel();
 		mSpinnerLIPanel = new JPanel();
 		mButtonPanel = new JPanel();
 		
 		mSave = new JFileChooser();
 
 		saveMenu.setSize(200, 150);
 		saveMenu.setTitle("Settings");
 		saveMenu.setVisible(true);
 
 		SpinnerModel modelLPI = new SpinnerNumberModel(BugfullabsMapEditor.mEditor.mEditorPanel.level.getLevelPack(), 1, 99, 1);
 	    spinLevelPackId = addLabeledSpinner(mSpinnerLPIPanel, "Level Pack id:", modelLPI);
 	    spinLevelPackId.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 		SpinnerModel modelLI = new SpinnerNumberModel(BugfullabsMapEditor.mEditor.mEditorPanel.level.getId(), 1, 15, 1);
 	    spinLevelId = addLabeledSpinner(mSpinnerLIPanel, "Level id:", modelLI);
 		spinLevelId.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		saveButton = new JButton("Save");
 		mButtonPanel.add(saveButton);
 		saveButton.addActionListener(this);
 		saveButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
         saveButton.setActionCommand("save");
         
 		saveMenu.setLayout(new BoxLayout(saveMenu.getContentPane(), BoxLayout.Y_AXIS));
         saveMenu.getContentPane().add(mSpinnerLPIPanel);
         saveMenu.getContentPane().add(mSpinnerLIPanel);
 	    saveMenu.getContentPane().add(saveButton);
 	    saveMenu.getRootPane().setDefaultButton(saveButton);
 
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 	    if ("save".equals(e.getActionCommand())) {
 	    	saveButton.setEnabled(false);
 	        mSave.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 	        mSave.setCurrentDirectory(new File("C:/Users/dlt/git/TheGame/TheGame/assets/levels"));
 			mSave.setDialogTitle("Wybierz folder zapisu");
 			int returnVal = mSave.showOpenDialog(null);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				BugfullabsMapEditor.mEditor.mEditorPanel.level.setId((Integer)spinLevelId.getValue());
 				System.out.println("saved to: " + mSave.getSelectedFile().toString() + "/level_" + BugfullabsMapEditor.mEditor.mEditorPanel.level.getLevelPack() + "_" + BugfullabsMapEditor.mEditor.mEditorPanel.level.getId() + ".xml");
 				XMLWriter.writeXML(new File(mSave.getSelectedFile().toString() + "/level_" + BugfullabsMapEditor.mEditor.mEditorPanel.level.getLevelPack() + "_" + BugfullabsMapEditor.mEditor.mEditorPanel.level.getId() + ".xml"), BugfullabsMapEditor.mEditor.mEditorPanel.level);
 				saveMenu.setVisible(false);
 		    	saveButton.setEnabled(true);
 			}
 	    }
 	} 
 	static protected JSpinner addLabeledSpinner(JPanel pPanel, String label, SpinnerModel model) {
 		JLabel l = new JLabel(label);
 		pPanel.add(l);
 		
 		JSpinner spinner = new JSpinner(model);
 		l.setLabelFor(spinner);
 		pPanel.add(spinner);
 		
 		return spinner;
 		}
 }
