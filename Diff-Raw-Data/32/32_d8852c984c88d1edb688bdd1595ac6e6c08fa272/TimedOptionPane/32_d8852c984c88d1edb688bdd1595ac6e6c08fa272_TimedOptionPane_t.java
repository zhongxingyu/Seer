 package ted.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.StringTokenizer;
  
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 import javax.swing.WindowConstants;
  
 /**
  * A message dialog that closes after a timeout.
  * 
  * @author clap
  * @author roel
  * 
  * Inspired by http://forum.java.sun.com/thread.jspa?threadID=5148811&messageID=9556308
  * 
  */
 public class TimedOptionPane {
  
 	protected static int UPDATE_PERIOD = 1000;
  
 	/**
 	 * Show a dialogBox.
 	 * 
 	 * @param f
 	 *            the owner
 	 * @param message
 	 *            the message to display
 	 * @param timeout
 	 *            in milliseconds
 	 * @param title
 	 *            title of the dialog box
 	 * @param timeoutMessage
 	 *            text showing remaining seconds
 	 *            
 	 * @param messageType
 	 *            JOptionPAne messagetype
 	 * @param optionType
 	 *            JoptionPane optiontype
 	 * @return {@link JOptionPane#YES_OPTION}, when Yes is clicked, {@link JOptionPane#NO_OPTION}
 	 *         if NO is clicked, or {@link JOptionPane#CANCEL_OPTION} on timeout or window closed
 	 *         event.
 	 */
 	public final static int showTimedOptionPane(Frame f, String message, String title,
 			String timeoutMessage, final long timeout, int messageType, int optionType) 
 	{
 		final Message messageComponent = new Message(message, timeout,
 				timeoutMessage);
 		final JOptionPane pane = new JOptionPane(messageComponent, messageType,
 				optionType);
 		Object result = showTimedOptionPane(f, title, messageComponent, pane);
 		
 		String valueString = result.toString();
 		if (JOptionPane.UNINITIALIZED_VALUE.equals(valueString)) 
 		{
 			return JOptionPane.CLOSED_OPTION;
 		}
 		return ((Integer)result).intValue();		
 	}
 	
 	// Same as above, only with custom button options that need some conversion
 	public static int showTimedOptionPane(Frame f, String message, String title,
 			String timeoutMessage, final long timeout, int messageType, int optionType, Icon icon, Object[] options,
 			Object defaultOption) 
 	{
 		
 		final Message messageComponent = new Message(message, timeout,
 				timeoutMessage);
 		final JOptionPane pane = new JOptionPane(messageComponent, messageType,
 				optionType, icon, options, defaultOption);
 		String result = (String)showTimedOptionPane(f, title, messageComponent, pane);
 		int intResult = JOptionPane.CLOSED_OPTION;
 		// convert custom button to int result
 		for (int i = 0; i < options.length; i++)
 		{
 			if (result.equals(options[i]))
 			{
 				intResult = i;
 			}
 		}
 		return intResult;
 	}
 	
 	public static Object showTimedOptionPane(Frame f, String title, final Message messageComponent, final JOptionPane pane)
 	{
 		final JDialog dialog = new JDialog(f, title, true);
 		dialog.setResizable(false);
 
 		dialog.setContentPane(pane);
 		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		Timer timer = new Timer(UPDATE_PERIOD, new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				messageComponent.addEllapsedMilliseconds(UPDATE_PERIOD);
 				if (messageComponent.isOver()) 
 				{
 					dialog.dispose();
 				}
 			}
 		});
 		timer.start();
 		pane.addPropertyChangeListener(new PropertyChangeListener() 
 		{
 			public void propertyChange(PropertyChangeEvent e) 
 			{
 				String prop = e.getPropertyName();
 				if (dialog.isVisible() && (e.getSource() == pane)
 						&& (JOptionPane.VALUE_PROPERTY.equals(prop))) 
 				{
 					dialog.setVisible(false);
 				}
 			}
 		});
 		
 		dialog.pack();
 		dialog.setLocationRelativeTo(f);
 		dialog.setVisible(true);
 
 		return pane.getValue();
 	}
  
 	/**
 	 * Content of the {@link JOptionPane}.
 	 * 
 	 * @author clap
 	 * @author roel
 	 * 
 	 */
 	static class Message extends JPanel 
 	{
  
 		private final static String RS = "Auto-close in ";
 		JLabel remaining;
 		private long timeout;
 		private long ellapsed = 0;
  
 		/**
 		 * Build content panel.
 		 * 
 		 * @param message
 		 *            the message to show
 		 * @param milliseconds
 		 *            timeout in milliseconds
 		 * @param timeoutMessage
 		 *            text showing remaining seconds
 		 */
 		protected Message(String message, long milliseconds, String timeoutMessage) 
 		{
 			super(new BorderLayout());
 			//JLabel messageLabel;
 			JPanel messagePanel = new JPanel();
 			GridLayout gridLayout = new GridLayout(0,1);
 			messagePanel.setLayout(gridLayout);
 			
 			Font SMALL_FONT = new Font("Dialog",0,10);
 			this.timeout = milliseconds;
 			// tokenize message on "\n"
 			StringTokenizer tokenizer = new StringTokenizer(message, "\n");
 			while (tokenizer.hasMoreTokens())
 			{
 				String token = tokenizer.nextToken();
 				JLabel messageLabel = new JLabel(token);
 				messagePanel.add(messageLabel);			
 			}
 			
 			
 			this.add(messagePanel);
 			
 			this.remaining = new JLabel(formatRemainingSeconds(milliseconds));
 			this.remaining.setFont(SMALL_FONT);
 			add(this.remaining, BorderLayout.SOUTH);
 		}
  
 		private String formatRemainingSeconds(long ms) 
 		{
 			return RS + (ms / 1000) + "s.";
 		}
  
 		/**
 		 * 
 		 * @return true if the timeout has been reached.
 		 */
 		protected boolean isOver() 
 		{
 			return ellapsed >= timeout;
 		}
  
 		/**
 		 * Indicates that some milliseconds has occured.
 		 * 
 		 * @param milliseconds
 		 */
 		protected void addEllapsedMilliseconds(long milliseconds) 
 		{
 			ellapsed += milliseconds;
 			remaining.setText(formatRemainingSeconds(timeout - ellapsed));
 			repaint();
 		}
 	}
 
 	
 }
