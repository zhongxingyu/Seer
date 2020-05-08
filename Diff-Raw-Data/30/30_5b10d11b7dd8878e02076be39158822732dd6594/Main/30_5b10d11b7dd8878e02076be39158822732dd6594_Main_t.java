 package arranger;
 
 import gui.MainPanel;
 
 import java.util.*;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import logic.LogicManager;
 import logic.MidiAPI;
 import logic.Editor;
 import music.Clef;
 import music.ClefName;
 import music.Piece;
 import instructions.*;
 import gui.NewFileDialog;
 
 /*
  * Main handles delegations of tasks between
  */
 public class Main extends JFrame implements InstructionListener {
 
 	MidiAPI _api;
 	MainPanel _mainPanel;
 
 	Editor _editor;
 	LogicManager _logicManager;
 
 	Piece _piece;
 
 	public Main(){
 		super("Music Arranger");
 		_piece = new tests.LongMelodyPiece();
 		_logicManager = new LogicManager(_piece);
 		_editor = _logicManager.getEditor();
 		
 		ArrangerConstants.WINDOW_WIDTH = 1000;
 		ArrangerConstants.WINDOW_HEIGHT = 800;
 		
 		_mainPanel = new MainPanel(_piece);
 		_mainPanel.addInstructionListener(this);
 		this.add(_mainPanel);
 		
 		addMenuBar();
 		
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setSize(ArrangerConstants.WINDOW_WIDTH, ArrangerConstants.WINDOW_HEIGHT);
 		this.setVisible(true);
 	}
 
 	public void addMenuBar(){
 		JMenuBar menuBar = new JMenuBar();
 
 		//----------------FILE----------------
 		JMenu file = new JMenu("File");
 		file.setMnemonic(KeyEvent.VK_F);
 
 		// new file
 		//Instruction instrNew = new FileInstruction();
 		final JFrame frame = this;
 		JMenuItem menuItemNew = new JMenuItem("New");
 		menuItemNew.setMnemonic(KeyEvent.VK_N);
 		menuItemNew.setToolTipText("New song");
 		menuItemNew.addActionListener(
 			new ActionListener() {
 				private List<Clef> clefList = new ArrayList<Clef>();
 				
 				{
 					// TODO: THIS IS STILL ALL CONSTANTS
 					Clef trebleClef = new Clef(ClefName.GCLEF, -2);
 					Clef bassClef = new Clef(ClefName.FCLEF, 2);
 					clefList.add(trebleClef);
 					clefList.add(bassClef);
 				}
 				
 				public void actionPerformed(ActionEvent event) {
 					// prompt for new song data
 					NewFileDialog newFileDialog = new NewFileDialog(frame);
 					newFileDialog.setLocationRelativeTo(frame);
 					newFileDialog.pack();
 					newFileDialog.setVisible(true);
 					
 					if (!newFileDialog.success())
 						return;
 					
 					InstructionBlock myInstr = new InstructionBlock(this, new FileInstructionNew(newFileDialog.getClefs(), newFileDialog.getNumMeasures(),
 																		newFileDialog.getTimeNumer(), newFileDialog.getTimeDenom(), newFileDialog.getNumAccidentals(), newFileDialog.getIsMajor()));
 					
 					receiveInstruction(myInstr);
 				}
 			});
 
 		JMenuItem menuItemOpen = new JMenuItem("Open");
 		menuItemOpen.setMnemonic(KeyEvent.VK_O);
 		menuItemOpen.setToolTipText("Open XML file");
 		menuItemOpen.addActionListener(
 			new ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					JFileChooser chooser = new JFileChooser();
 					
 					int returnVal = chooser.showOpenDialog(frame);
 					if(returnVal == JFileChooser.APPROVE_OPTION) {
						InstructionBlock myInstr = new InstructionBlock(this, 
								new FileInstructionIO(FileInstructionType.OPEN, 
										chooser.getSelectedFile().getAbsolutePath()));
 						receiveInstruction(myInstr);
 					}
 				}
 			});
 		
 		JMenuItem menuItemSave = new JMenuItem("Save");
 		menuItemSave.setMnemonic(KeyEvent.VK_S);
 		menuItemSave.setToolTipText("Save current song");
 		menuItemSave.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						JFileChooser chooser = new JFileChooser();
						
						int returnVal = chooser.showSaveDialog(frame);
						if(returnVal == JFileChooser.APPROVE_OPTION) {
							InstructionBlock myInstr = new InstructionBlock(this, 
									new FileInstructionIO(FileInstructionType.SAVE, 
											chooser.getSelectedFile().getAbsolutePath()));
							receiveInstruction(myInstr);
						}
 					}
				});
 
 		JMenuItem menuItemPrint = new JMenuItem("Print");
 		menuItemPrint.setMnemonic(KeyEvent.VK_P);
 		menuItemPrint.setToolTipText("Print song");
 		menuItemPrint.addActionListener(
 			new ActionListener() {
 				InstructionBlock myInstr = new InstructionBlock(this, new FileInstructionPrint(_mainPanel.getScoreImage()));
 				public void actionPerformed(ActionEvent event) {
 					receiveInstruction(myInstr);
 				}
 			});
 		
 		JMenuItem menuItemExit = new JMenuItem("Exit");
 		menuItemExit.setMnemonic(KeyEvent.VK_X);
 		menuItemExit.setToolTipText("Exit application");
 		menuItemExit.addActionListener(
 			new ActionListener() {
 				InstructionBlock myInstr = new InstructionBlock(this, new FileInstructionIO(FileInstructionType.EXIT, ""));
 				public void actionPerformed(ActionEvent event) {
 					receiveInstruction(myInstr);
 				}
 			});
 
 		file.add(menuItemNew);
 		file.add(menuItemOpen);
 		file.add(menuItemSave);
 		file.add(menuItemPrint);
 		file.add(menuItemExit);
 		//----------------EDIT----------------
 		JMenu edit = new JMenu("Edit");
 		edit.setMnemonic(KeyEvent.VK_E);
 
 		JMenuItem menuItemUndo = new JMenuItem("Undo");
 		menuItemUndo.setMnemonic(KeyEvent.VK_U);
 		menuItemUndo.setToolTipText("Undo last action");
 		menuItemUndo.addActionListener(
 			new ActionListener() {
 				InstructionBlock myInstr = new InstructionBlock(this, new FileInstructionIO(FileInstructionType.EXIT, ""));
 				public void actionPerformed(ActionEvent event) {
 					receiveInstruction(myInstr);
 				}
 			});
 
 		JMenuItem menuItemRedo = new JMenuItem("Redo");
 		menuItemRedo.setMnemonic(KeyEvent.VK_R);
 		menuItemRedo.setToolTipText("Redo last action");
 		menuItemRedo.addActionListener(
 			new ActionListener() {
 				InstructionBlock myInstr = new InstructionBlock(this, new FileInstructionIO(FileInstructionType.EXIT, ""));
 				public void actionPerformed(ActionEvent event) {
 					receiveInstruction(myInstr);
 				}
 			});
 
 		edit.add(menuItemUndo);
 		edit.add(menuItemRedo);
 
 		menuBar.add(file);
 		menuBar.add(edit);
 		setJMenuBar(menuBar);
 	}
 
 	public void receiveInstruction(InstructionBlock instr) {
 		_logicManager.interpretInstrBlock(instr);
 		_mainPanel.updateScore();
 	}
 
 	public static void main(String[] args){
 		new Main();
 	}
 }
