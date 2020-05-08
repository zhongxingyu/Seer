 package com.denisneuling.binarytree.gui.component;
 
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
import com.denisneuling.binarytree.gui.MainFrame;

 @Component
 public class PopupMenu extends JPopupMenu implements ActionListener,InitializingBean{
 	private static final long serialVersionUID = -5478903794243320305L;
 	protected Logger log = Logger.getLogger(this.getClass());
 	
 	@Autowired
	private MainFrame mainFrame;
	
	@Autowired
 	private BinaryTreeEditorPane binaryTreeEditorPane;
 	
 	private JMenuItem add;
 	private JMenuItem edit;
 	private JMenuItem remove;
 	
 	public PopupMenu(){
		add = new JMenuItem("Add Node");
		edit = new JMenuItem("Edit Node");
		remove = new JMenuItem("Remove Node");
 		
 		this.add(add);
 		this.add(edit);
 		this.add(remove);
 		
 		this.pack();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if(add.equals(arg0.getSource())){
 			log.info("Add");
 		}else if(edit.equals(arg0.getSource())){
 			log.info("Edit");
 		}else if(remove.equals(arg0.getSource())){
 			log.info("Remove");
 		}
 	}
 	
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		add.addActionListener(this);
 		edit.addActionListener(this);
 		remove.addActionListener(this);
 		
 		setInvoker(binaryTreeEditorPane);
 	}
 	
 	public void relocate(Point point){
		Point mainFrameLocation = mainFrame.getLocation();
		this.setLocation((int)(mainFrameLocation.getX() + point.getX()) - 30, (int)(mainFrameLocation.getY() + point.getY()) + 40);
 	}
 }
