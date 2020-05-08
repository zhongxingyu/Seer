 package com.denisneuling.connectfour.gui.dialog;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.denisneuling.connectfour.gui.MainFrame;
 import com.denisneuling.connectfour.gui.components.ConnectFourGrid;
 import com.denisneuling.connectfour.gui.components.MenuPanel;
 import com.denisneuling.connectfour.gui.components.listener.TileActionListener;
 
 /**
  * <p>NewDialog class.</p>
  *
  * @author ska
  * @version $Id: $Id
  */
 @Component
 public class GameDialog extends JDialog implements WindowListener, ActionListener, InitializingBean {
 	private static final long serialVersionUID = 4621566363518596409L;
 	protected Logger log = Logger.getLogger(this.getClass());
 	
 	@Autowired
 	private MenuPanel menuPanel;
 	
 	@Autowired
 	private MainFrame mainFrame;
 	
 	@Autowired
 	private ConnectFourGrid connectFourGrid;
 	
 	@Autowired
 	private TileActionListener tileActionListener;
 	
 	private MigLayout layout;
 	
 	private JTextField xAxis;
 	private JTextField yAxis;
 	
 	private JButton okButton;
 	private JButton cancelButton;
 	
 	/**
 	 * <p>Constructor for GameDialog.</p>
 	 */
 	public GameDialog(){
 		this.setTitle("New Game");
 		this.setSize(new Dimension(350,125));
 		this.setResizable(false);
 		
 		layout = new MigLayout("fillx", "[right]rel[grow,fill]", "[]10[]");
 		this.setLayout(layout);
 		
 		this.add(new JLabel("Length of Y Axis:"),   "");
 		
 		yAxis = new JTextField("5");
 		yAxis.setToolTipText("Has to be numeric");
 		xAxis = new JTextField("6");
 		xAxis.setToolTipText("Has to be numeric");
 		
 		this.add(yAxis,          "wrap");
 		this.add(new JLabel("Length of X Axis:"), "");
 		this.add(xAxis,          "wrap");
 		
 		// TODO find workaround for better line feed
 		this.add(new JLabel(), "");
 		
 		JPanel buttonPanel = new JPanel(new MigLayout("fillx,insets 0"));
 
 		//Ok button
 		okButton = new JButton("Ok");
 		okButton.setMnemonic('O');
 		okButton.addActionListener(this);
 		buttonPanel.add(okButton, "split,right,width 100!");
 
 		//Cancel button
 		cancelButton = new JButton("Cancel");
 		cancelButton.setMnemonic('C');
 		cancelButton.addActionListener(this);
 		buttonPanel.add(cancelButton, "width 100!");
 		
 		this.add(buttonPanel, "");
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public void windowActivated(WindowEvent e) {
 		log.debug("Window activated");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void windowClosed(WindowEvent e) {
 		log.debug("Window closed");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void windowClosing(WindowEvent e) {
 		menuPanel.enableItems();
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 		log.debug("Window deactivated");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 		log.debug("Window deiconified");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void windowIconified(WindowEvent e) {
 		log.debug("Window iconified");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void windowOpened(WindowEvent e) {
 		log.debug("Window opened");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		this.addWindowListener(this);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		log.info(arg0.toString());
 		
 		if(okButton.equals(arg0.getSource())){
 			int intx, inty;
 			try{
 				String y = yAxis.getText();
 				inty = Integer.parseInt(y);
 			}catch(Exception e){
 				return;
 			}
 			
 			try{
 				String x = xAxis.getText();
 				intx = Integer.parseInt(x);
 			}catch(Exception e){
 				return;
 			}
 			
 			connectFourGrid.renew(intx,inty);
 			
 			tileActionListener.enable();
 			
 			this.setVisible(false);
 			menuPanel.enableItems();
 			
 		}else if(cancelButton.equals(arg0.getSource())){
 			this.setVisible(false);
 			menuPanel.enableItems();
 		}
 	}
 }
