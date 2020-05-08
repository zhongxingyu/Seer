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
 
 @Component
 public class PopupMenu extends JPopupMenu implements ActionListener,InitializingBean{
 	private static final long serialVersionUID = -5478903794243320305L;
 	protected Logger log = Logger.getLogger(this.getClass());
 	
 	@Autowired
 	private BinaryTreeEditorPane binaryTreeEditorPane;
 	
 	private JMenuItem add;
 	private JMenuItem edit;
 	private JMenuItem remove;
 	
 	public PopupMenu(){
		add = new JMenuItem("Add");
		edit = new JMenuItem("Edit");
		remove = new JMenuItem("Remove");
 		
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
		this.setLocation((int)point.getX() - 30, (int)point.getY() + 60);
 	}
 }
