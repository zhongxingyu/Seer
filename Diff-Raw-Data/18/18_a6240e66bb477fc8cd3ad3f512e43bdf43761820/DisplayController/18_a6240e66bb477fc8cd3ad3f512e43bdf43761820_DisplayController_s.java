 package controller;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JComponent;
 
 import utility.GraphicConstants;
 import view.*;
 
 import model.DisplayModel;
 
 
 /**
  * A class to represent a controller 
  * @author Hanna Materne
  *
  */
 public class DisplayController implements ActionListener, MouseListener{
 	
 	private ListView listView;
 	private TaskSettingView settingView;
 	private DisplayTaskPanel displayTaskPanel;
 	private EditTaskPanel editTaskPanel;
	
 	/**
	 * Create the controller. 
 	 * 
 	 * @param settingView
 	 * @param displayTaskPanel
 	 */
	public DisplayController(ListView listView, TaskSettingView settingView, DisplayTaskPanel displayTaskPanel){
		this.listView=listView;
		this.settingView=settingView;
		this.displayTaskPanel=displayTaskPanel;
 		displayTaskPanel.setController(this);
 		displayTaskPanel.addMouseListener(this);
		
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		System.out.println("entered");
 
 		if (e.getActionCommand().equals("editTask")) {
 			System.out.println("presset editbutton");
 			settingView.panelInScroll.removeAll();
 
 			this.editTaskPanel = new EditTaskPanel(
 					displayTaskPanel.getTaskModel());
 			EditController editController = new EditController(listView,
 					settingView, editTaskPanel);
 
 			settingView.panelInScroll.add(editTaskPanel, 0);
 			settingView.updateView();
 
 		}
 				
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 
 		if (e.getSource() instanceof DisplayTaskPanel) {
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		displayTaskPanel.setBackground(GraphicConstants.BUTTONPRESSEDFOREGROUND);
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 
 	}
 
 }
