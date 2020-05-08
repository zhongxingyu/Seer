 
 package gui.panels.subcontrolpanels;
 
 import engine.agent.Agent;
 import gui.panels.ControlPanel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.Timer;
 
 /**
  * TracePanel class represents a JPanel that is used for displaying the messaging between
  * backend agents.
  */
 @SuppressWarnings("serial")
 public class TracePanel extends JPanel implements ActionListener
 {
 	/** The ControlPanel this panel is linked to */
 	ControlPanel parent;
 
 	/** Constant used as threshold for number of messages to display. */
 	private final int MESSAGELIMIT = 5000;
 
 	/** ArrayList that contains TracePanelMessages */
 	private java.util.Queue<TracePanelMessage> tracePanelMessages = new ArrayBlockingQueue<TracePanelMessage>(
 		MESSAGELIMIT, true);
 
 	// Gui stuff
 	/** The text area that displays the messages */
 	private JTextArea messageArea;
 
 	/** The scroll pane to connect to the text area */
 	private JScrollPane scrollPane;
 
 	/**
 	 * Used for refreshing the trace panel and controlling the amount of messages to
 	 * display.
 	 */
 	private Timer t = new Timer(500, this);
 
 	/**
 	 * Constructor for creating a TracePanel object. Control panel can
 	 * be sent as a parameter so this class's parent will be set
 	 * correctly.
 	 * @param cp
 	 *        the ControlPanel this panel is linked to
 	 * @param initialText
 	 *        Any text that the panel should start with
 	 */
 	public TracePanel(ControlPanel cp, String initialText)
 	{
 		this(initialText);
 
 		parent = cp;
 	}
 
 	/**
 	 * Constructor for this class's main function.
 	 * @param initialText
 	 */
 	public TracePanel(String initialText)
 	{
 		this.setBackground(Color.black);
 		this.setForeground(Color.black);
 		this.setMaximumSize(new Dimension(370, 80));
 		this.setMinimumSize(new Dimension(370, 80));
 		this.setPreferredSize(new Dimension(370, 80));
 
 		// set up message area
 		this.messageArea = new JTextArea("Output goes here", 5, 200);
		this.messageArea.setBackground(Color.gray);
 		this.messageArea.setForeground(Color.white);
 		this.messageArea.setWrapStyleWord(true);
 		this.messageArea.setLineWrap(true);
 		this.messageArea.setEditable(false);
 		this.scrollPane = new JScrollPane(this.messageArea);
 
 		// change the layout
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		add(this.scrollPane);
 		t.start();
 	}
 
 	/**
 	 * Agents call this method to direct their console out put to the TracePanel
 	 * @param message
 	 *        The message to display on the tracepanel
 	 * @param agent
 	 *        The message sender
 	 */
 	public void print(String message, Agent agent)
 	{
 		addPanelMessage(message, agent);
 	}
 
 	/**
 	 * Adds message to the tracePanelMessages queue to display
 	 * @param message
 	 * @param agent
 	 */
 	private void addPanelMessage(String message, Agent agent)
 	{
 		TracePanelMessage panelMessage = new TracePanelMessage(message, agent);
 		if (tracePanelMessages.size() == MESSAGELIMIT)
 		{
 			tracePanelMessages.remove();
 		}
 
 		this.tracePanelMessages.offer(panelMessage);
 	}
 
 	/**
 	 * Called on timer fire
 	 */
 	public void actionPerformed(ActionEvent ae)
 	{
 		this.update();
 	}
 
 	/**
 	 * Updates the trace panel
 	 */
 	public void update()
 	{
 		StringBuilder updatedText = new StringBuilder();
 
 		synchronized (tracePanelMessages)
 		{
 			for (TracePanelMessage m : tracePanelMessages)
 			{
 				updatedText.append(m.returnMessage());
 			}
 		}
 
 		this.messageArea.setText(updatedText.toString());
 		this.scrollPane.getVerticalScrollBar().setValue(this.scrollPane.getVerticalScrollBar().getMaximum());
 	}
 
 	/**
 	 * Returns the parent panel
 	 * @return the parent panel
 	 */
 	public ControlPanel getGuiParent()
 	{
 		return parent;
 	}
 }
