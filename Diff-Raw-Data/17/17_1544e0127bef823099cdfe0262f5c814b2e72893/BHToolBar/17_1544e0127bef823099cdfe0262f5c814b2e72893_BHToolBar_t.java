 package org.bh.gui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.*;
 
 import org.bh.platform.PlatformKey;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * 
  * BHToolBar offers quick access to functions
  *
  * <p>
  * This class offers the user quick access to important and frequently used functions.
  * The icons on the buttons are self explanatory and facilitate the work
  *
  * @author Tietze.Patrick
  * @version 0.1, 2009/12/03
  *
  */
 
 
 public class BHToolBar extends JToolBar implements ActionListener, MouseListener{
     
     //HelpSet and HelpBroker are necessary for user help
     //javax.help.HelpSet helpSet = null;
     //javax.help.HelpBroker helpBroker = null;
 
     //JileChooser allows file loading
     //JFileChooser fc;
 	
 	private boolean shown = false;
 	private boolean fixed = true;
 	private int width;
 	private int height;
 	public BHButton Bopen, Bsave, Bproject, Bscenario, Bperiod, Bdelete; 
 	public JButton BshowHide;
 	JPanel buttonPanel;
 	
 	CellConstraints cons;
 	
     
     public BHToolBar(int width, int height) {
     	this.width = width;
     	this.height = height;
     	addMouseListener(this);
     	
 		//paint background
 		setOpaque(true);
 		
 		//don't allow to relocate the bar
 		setFloatable(false);
 		
 		setSize(width, height);
 		
 		String rowDef = "p";
 		String colDef = "600px,fill:0px:grow,fill:30px";
 		setLayout(new FormLayout(colDef, rowDef));
 		cons = new CellConstraints();
 		
 		createToolBar();
 		
     }
     
     public void hideToolBar(){
    	
     	buttonPanel.setVisible(false);
     	
     	shown = false;
     }
     
     public void showToolBar(){
    	
     	buttonPanel.setVisible(true);
     	
     	shown = true;
     }
     
     public void createToolBar(){
     	
     	buttonPanel = new JPanel();
     	buttonPanel.setLayout(new FlowLayout());
     	buttonPanel.setOpaque(false);
    	buttonPanel.addMouseListener(this);
     	
     	Bopen = new BHToolButton(PlatformKey.TOOLBAROPEN, "Bopen");
    	Bopen.addMouseListener(this);
 		Bsave = new BHToolButton(PlatformKey.TOOLBARSAVE, "Bsave");
		Bsave.addMouseListener(this);
 		Bproject = new BHToolButton(PlatformKey.TOOLBARADDPRO, "BnewProject");
		Bproject.addMouseListener(this);
 		Bscenario = new BHToolButton(PlatformKey.TOOLBARADDS, "BnewScenario");
		Bscenario.addMouseListener(this);
 		Bperiod = new BHToolButton(PlatformKey.TOOLBARADDPER, "BnewPeriod");
		Bperiod.addMouseListener(this);
 		Bdelete = new BHToolButton(PlatformKey.TOOLBARREMOVE, "Bdelete");
		Bdelete.addMouseListener(this);
 
 		BshowHide = new JButton("");
 		BshowHide.setIcon(new ImageIcon(BHToolBar.class.getResource("/org/bh/images/buttons/Bshow.png"), ""));
		BshowHide.addActionListener(this);
		
 		
 		buttonPanel.add(Bopen);
 		buttonPanel.add(Bsave);
 		buttonPanel.add(Bproject);
 		buttonPanel.add(Bscenario);
 		buttonPanel.add(Bperiod);
 		buttonPanel.add(Bdelete);
 		
 		add(buttonPanel,  cons.xywh(1, 1, 1, 1));
 		add(BshowHide, cons.xywh(3, 1, 1, 1));
 		
 		shown = true;
     }
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if(shown){
 			BshowHide.setIcon(new ImageIcon(BHToolBar.class.getResource("/org/bh/images/buttons/Bhide.png"), ""));
 			fixed = false;
 			
 		}else {
 			BshowHide.setIcon(new ImageIcon(BHToolBar.class.getResource("/org/bh/images/buttons/Bshow.png"), ""));
 			fixed = true;
 			showToolBar();
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		if(!shown)
 			showToolBar();
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		if(!fixed)
 			hideToolBar();
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 }
 
