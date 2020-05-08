 package org.vamdc.validator.gui;
 
 import java.awt.Frame;
 import java.awt.Rectangle;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JDialog;
 
 import org.vamdc.validator.Setting;
 
 /**
  * JDialog that can remember it's position on screen
  */
 public class PositionMemoryDialog extends JDialog{
 
 	private static final long serialVersionUID = 862006088829415251L;
 
 	private Setting dimensionOption;
 	
 	public PositionMemoryDialog(String name, Frame owner, Setting dialogPosition){
		super(owner,name);
 		this.dimensionOption = dialogPosition;
 	}
 	
 	public void saveDimensions(){
 		Rectangle pos = this.getBounds();
 		dimensionOption.saveObject(pos);
 	}
 	
 	public void loadDimensions(){
 		Object pos = dimensionOption.loadObject();
 		if (pos instanceof Rectangle){
 			this.setBounds((Rectangle) pos);
 		}else{
 			this.pack();
 		}
 	}
 	
 	protected void initCloseEvent() {
 		this.addWindowListener(
 				new WindowAdapter(){
 					@Override
 					public void windowClosed(WindowEvent e){Setting.GUILogConsole.saveValue(false);}
 					@Override
 					public void windowClosing(WindowEvent e){
 						Setting.GUILogConsole.saveValue(false);
 						saveDimensions();
 					}
 					@Override
 					public void windowLostFocus(WindowEvent e) {
 						saveDimensions();
 					}
 				}
 				);
 	}
 	
 	
 }
