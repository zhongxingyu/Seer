 package edu.ucsb.cs56.projects.games.cs56_games_maze;
 
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.border.*;
 import java.awt.event.*;
 import java.beans.*;
 
 
 /**
    A JPanel that has labels and fields for all settings, as well as funcitonality to writeback as necessary.
 
    @author Evan West
    @version CS56 S13 UCSB
  */
 public class MazeSettingsPanel extends JPanel{
 
     public GridLayout layout;
     public MazeSettings settings;
    public MazeGui gm;
 
     private JTextField genChainLengthField;
     private JTextField genChainLengthFluxField;
     private JTextField stepGenDistanceField;
     private JTextField rowsField;
     private JTextField colsField;
     private JTextField cellWidthField;
     private JTextField startRowField;
     private JTextField startColField;
     private JTextField endRowField;
     private JTextField endColField;
     private JTextField progRevealRadiusField;
 
     private JCheckBox progDrawCB;
     private JTextField progDrawSpeedField;
 
     private JButton okButton;
     private JButton cancelButton;
 
     private PlainDocument doc;
     private IntegerDocumentFilter intDocumentFilter;
     private Border padding;
 
     /** Constructor for the JPanel
 @param settings Settings object that will be read and written back to as necessary.
      */
    public MazeSettingsPanel(MazeSettings settings, MazeGui gm){
 	super();
 	this.settings=settings;
	this.gm = gm;
 	this.layout = new GridLayout(0,2);
 	this.layout.setVgap(10);
 	this.setLayout(this.layout);
 
 	padding = BorderFactory.createEmptyBorder(20,20,15,10);
 	this.setBorder(padding);
 
 	initFields();
 	updateFieldValues();
     }
  
     /** Constructs and initializes all fields and values (empty)
      */
     private void initFields(){
 	intDocumentFilter = new IntegerDocumentFilter();
 	JLabel label;
 
 	label=new JLabel("Gen Chain Length");
 
 	genChainLengthField = new JTextField(5);
 	doc = (PlainDocument)genChainLengthField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(genChainLengthField);
 
 	label=new JLabel("Gen Chain Length Flux");
 
 	genChainLengthFluxField = new JTextField(5);
 	doc = (PlainDocument)genChainLengthFluxField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(genChainLengthFluxField);
 
 	label=new JLabel("Step Gen Distance");
 
 	stepGenDistanceField = new JTextField(5);
 	doc = (PlainDocument)stepGenDistanceField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(stepGenDistanceField);
 
 	label=new JLabel("Rows");
 
 	rowsField = new JTextField(5);
 	label.setToolTipText("Number of total rows in maze");
 	doc = (PlainDocument)rowsField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(rowsField);
 
 	label=new JLabel("Columns");
 
 	colsField = new JTextField(5);
 	label.setToolTipText("Number of total columns in maze.");
 	doc = (PlainDocument)colsField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(colsField);
 
 	label=new JLabel("Start Row");
 
 	startRowField = new JTextField(5);
 	label.setToolTipText("Row of starting position (0,0 is top left)");
 	doc = (PlainDocument)startRowField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(startRowField);
 
 	label=new JLabel("Start Column");
 
 	startColField = new JTextField(5);
 	label.setToolTipText("Column of starting position (0,0 is top left)");
 	doc = (PlainDocument)startColField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(startColField);
 
 	label=new JLabel("End Row");
 
 	endRowField = new JTextField(5);
 	label.setToolTipText("Row of ending (goal) position (0,0 is top left)");
 	doc = (PlainDocument)endRowField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(endRowField);
 
 	label=new JLabel("End Column");
 
 	endColField = new JTextField(5);
 	label.setToolTipText("Column of ending (goal) position (0,0 is top left)");
 	doc = (PlainDocument)endColField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(endColField);
 
 	label=new JLabel("Cell Width");
 
 	cellWidthField = new JTextField(5);
 	label.setToolTipText("Width of a single cell in pixels");
 	doc = (PlainDocument)cellWidthField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(cellWidthField);
 
 	label=new JLabel("Progressive Reveal Radius");
 
 	progRevealRadiusField = new JTextField(5);
 	doc = (PlainDocument)progRevealRadiusField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(progRevealRadiusField);
 
 	label=new JLabel("Progressive Draw");
 
 	progDrawCB = new JCheckBox();
 	label.setToolTipText("Whether maze should be drawn progressively or not (immediately)");
 	this.add(label);
 	this.add(progDrawCB);
 
 	label=new JLabel("Progressive Draw Speed");
 
 	progDrawSpeedField = new JTextField(5);
 	label.setToolTipText("Size of chunks in which maze should be drawn progressively (larger is faster)");
 	doc = (PlainDocument)progDrawSpeedField.getDocument();
 	doc.setDocumentFilter(intDocumentFilter);
 	this.add(label);
 	this.add(progDrawSpeedField);
 
 
 
 
 	okButton = new JButton("OK");
 	okButton.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    writeback();
 		    JDialog parentDialog = (JDialog)(getRootPane().getParent());
 		    parentDialog.setVisible(false);
		    gm.newMaze();
 		}
 	    });
 	this.add(okButton);
 
 	cancelButton = new JButton("Cancel");
 	cancelButton.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    JDialog parentDialog = (JDialog)(getRootPane().getParent());
 		    parentDialog.setVisible(false);
 		}
 	    });
 	this.add(cancelButton);
     }
 
     /** writes changes in UI elements back to settings file
      */
     public void writeback(){
 	this.settings.genChainLength = Integer.parseInt(genChainLengthField.getText());
 	this.settings.genChainLengthFlux = Integer.parseInt(genChainLengthFluxField.getText());
 	this.settings.stepGenDistance = Integer.parseInt(stepGenDistanceField.getText());
 	this.settings.rows = Integer.parseInt(rowsField.getText());
 	this.settings.cols = Integer.parseInt(colsField.getText());
 	this.settings.cellWidth = Integer.parseInt(cellWidthField.getText());
 	this.settings.startRow = Integer.parseInt(startRowField.getText());
 	this.settings.startCol = Integer.parseInt(startColField.getText());
 	this.settings.endRow = Integer.parseInt(endRowField.getText());
 	this.settings.endCol = Integer.parseInt(endColField.getText());
 	this.settings.progRevealRadius = Integer.parseInt(progRevealRadiusField.getText());
 	this.settings.progDraw = progDrawCB.isSelected();
 	this.settings.progDrawSpeed = Integer.parseInt(progDrawSpeedField.getText());
     }
 
     /** Sets values of fields on the panel to their current values form settings file.
      */
     private void updateFieldValues(){
 	genChainLengthField.setText(String.valueOf(settings.genChainLength));
 	genChainLengthFluxField.setText(String.valueOf(settings.genChainLengthFlux));
 	stepGenDistanceField.setText(String.valueOf(settings.stepGenDistance));
 	rowsField.setText(String.valueOf(settings.rows));
 	colsField.setText(String.valueOf(settings.cols));
 	cellWidthField.setText(String.valueOf(settings.cellWidth));
 	startRowField.setText(String.valueOf(settings.startRow));
 	startColField.setText(String.valueOf(settings.startCol));
 	endRowField.setText(String.valueOf(settings.endRow));
 	endColField.setText(String.valueOf(settings.endCol));
 	progRevealRadiusField.setText(String.valueOf(settings.progRevealRadius));
 	progDrawCB.setSelected(settings.progDraw);
 	progDrawSpeedField.setText(String.valueOf(settings.progDrawSpeed));
     }
 
     public void paintComponent(Graphics g){
 	updateFieldValues();
 	super.paintComponent(g);
     }
 }
