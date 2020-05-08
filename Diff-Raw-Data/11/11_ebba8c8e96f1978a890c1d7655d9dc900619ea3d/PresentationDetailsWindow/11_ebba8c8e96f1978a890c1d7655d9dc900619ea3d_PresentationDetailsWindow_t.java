 package ds.gui;
 
 import java.awt.GridLayout;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 
 import ds.controllers.ControllerGUI;
 import ds.entity.Presentations;
 
 public class PresentationDetailsWindow extends JFrame{
 	private ControllerGUI controllerGUI;
 	private JTextField title = new JTextField();
 	private JTextField titleEdit;
 	private JTextField description = new JTextField();
 	private JTextField descriptionEdit;
 	private Presentations selectedPresentation;
 	private JButton saveDetails;
	private GridLayout myGrid = new GridLayout(5,2);
 
 	public PresentationDetailsWindow(ControllerGUI controllerGUI, Presentations selectedPresentation) {
 		this.selectedPresentation = selectedPresentation;
 		this.controllerGUI = controllerGUI;
 		initWindow();
 		initComponents();
 	}
 	
 	private void initComponents() {
		titleEdit = new JTextField("title is: "+selectedPresentation.getPresentationTitle());
		titleEdit.setEditable(true);
		descriptionEdit = new JTextField(""+selectedPresentation.getPresentationId());
		descriptionEdit.setEditable(true);
 		saveDetails = new JButton("Save details");
 		
 		this.add(title);
 		this.add(titleEdit);
 		this.add(description);
 		this.add(descriptionEdit);
 		this.add(saveDetails);
 		this.pack();
 		this.validate();
 	}
 
 	private void initWindow() {
 		this.setTitle("Presentation details");
 		this.setSize(400, 200);
 		this.setVisible(true);	
 		this.setLayout(myGrid);
 	}	
 
 }
