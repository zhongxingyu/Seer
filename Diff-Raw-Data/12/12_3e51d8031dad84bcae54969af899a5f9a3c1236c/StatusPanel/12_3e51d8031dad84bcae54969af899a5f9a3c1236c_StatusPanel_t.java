 /**
  * 
  */
 package admin;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 /**
  * @author kevin
  * 
  */
 public class StatusPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private JLabel tabLabel;
 	private JLabel msgLabel;
 	private Timer t = null;
 	private Component[] errorComps = new Component[0];
 
 	private static int TIMER_TIME = 3500;
 
 	public StatusPanel() {
 		setLayout(new BorderLayout(5, 5));
 
 		tabLabel = new JLabel();
 		msgLabel = new JLabel();
 
 		add(tabLabel, BorderLayout.LINE_START);
 		add(msgLabel, BorderLayout.LINE_END);
 	}
 
 	/**
 	 * Set the tab(General,Contestant,user,..) label
 	 * 
 	 * @param txt
 	 *            The tab's name
 	 */
 	public void setTabLabel(String txt) {
 		tabLabel.setText(txt);
 	}
 
 	/**
 	 * Display message to user. For error messages see setErrorMsgLabel
 	 * 
 	 * @param txt
 	 *            text to display
 	 */
 	public void setMsgLabel(String txt) {
 		if (t == null || !t.isRunning()) {
 			resetColor();
 			msgLabel.setText(txt);
 		}
 	}
 
 	/**
 	 * Class used to run the error timer. This is used to force all other 
 	 * events in the GUI to execute before this is called. 
 	 * @author Kevin Brightwell
 	 */
 	private class RunTimer implements Runnable {
 		
 		private JPanel pane;
 		private String txt;
 		
 		public RunTimer(JPanel pane, String txt) {
 			this.pane = pane;
 			this.txt = txt;
 		}
 		
 		@Override
 		public void run() {
 			if (t != null && t.isRunning()) {
 				t.stop();
 			} else {
 				t = new Timer(TIMER_TIME, displayError);
 				t.setRepeats(false);
 			}
 
 			setMsgLabel(txt);
 			msgLabel.setForeground(Color.white);
 			pane.setBackground(Color.red);
 
 			for (Component c : errorComps)
 				if (c != null)
 					c.setBackground(Color.RED);
 
 			t.start();
 		}
 		
 	}
 	
 	/**
 	 * Display an error message. Makes panel red
 	 * 
 	 * @param txt
 	 *            Error text to display
 	 * @param c
 	 *            A component to make the background red(can pass null if none
 	 *            needed)
 	 */
 	public void setErrorMsgLabel(String txt, Component... comps) {
 		errorComps = comps;
 		
 		SwingUtilities.invokeLater(new RunTimer(this, txt));	
 	}
 
 	private void resetColor() {
 		for (Component c : errorComps)
 			if (c != null)
 				c.setBackground(Utils.getThemeBG());
 
 		msgLabel.setForeground(Utils.getThemeFG());
		msgLabel.setText("");
 		this.setBackground(Utils.getThemeBG());
 	}
 	
 	/**
 	 * Clears the panels error messages
 	 */
 	public void clearPanel() {
 		if (t != null)	
 			t.stop();
 		
 		resetColor();
 	}
 
 	Action displayError = new AbstractAction() {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			resetColor();
 
 		}
 
 	};
 }
