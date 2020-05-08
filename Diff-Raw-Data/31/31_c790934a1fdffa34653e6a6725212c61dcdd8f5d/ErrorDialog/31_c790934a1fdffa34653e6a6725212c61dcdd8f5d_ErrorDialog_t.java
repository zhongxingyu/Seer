 package com.denisneuling.connectfour.gui.dialog;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.stereotype.Component;
 
 @Component
/**
 * <p>ErrorDialog class.</p>
 *
 * @author Denis Neuling (denisneuling@gmail.com)
 * 
 */
 public class ErrorDialog extends BaseDialog implements ActionListener, InitializingBean {
 	private static final long serialVersionUID = -8042741582297828053L;
 	protected Logger log = Logger.getLogger(this.getClass());
 
 	private MigLayout layout;
 	private JLabel label;
 	private JButton okButton;
 	
 	private static final String NEWGAMEERROR = 
 			"<html><h2>Error</h2><p>" +
 			"Length of Y axis has to be between 5 and 8 (ixcl.).<br>" +
 			"Length of X axis has to be between 6 and 10 (ixcl.).<br>" +
 			"</html>";
 
	/**
	 * <p>Constructor for ErrorDialog.</p>
	 */
 	public ErrorDialog() {
 		this.setSize(new Dimension(350, 150));
 		this.setResizable(false);
 
 		layout = new MigLayout("fillx", "[right]rel[grow,fill]", "[]10[]");
 		this.setLayout(layout);
 
 		label = new JLabel();
 		this.add(label, "");
 
 		// TODO find workaround for better line feed
 		this.add(new JLabel(), "");
 
 		JPanel buttonPanel = new JPanel(new MigLayout("fillx,insets 0"));
 
 		// Ok button
 		okButton = new JButton("Ok");
 		okButton.setMnemonic('O');
 		okButton.addActionListener(this);
 		buttonPanel.add(okButton, "split,right,width 100!");
 
 		this.add(buttonPanel, "span");
 	}
 
	/**
	 * <p>showError.</p>
	 *
	 * @param errorMessage a {@link java.lang.String} object.
	 */
 	public void showError(String errorMessage) {
 		relocate();
 
 		label.setText(errorMessage);
 
 		this.repaint();
 		this.setVisible(true);
 	}
 
	/** {@inheritDoc} */
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		this.setVisible(false);
 	}
 
	/** {@inheritDoc} */
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		this.addWindowListener(this);
 	}
 
	/**
	 * <p>showNewGameError.</p>
	 */
 	public void showNewGameError() {
 		showError(NEWGAMEERROR);
 	}
 }
