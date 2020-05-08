 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008,2009 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.dataentry;
 
 import org.wwscc.dataentry.tables.RunsTable;
 import java.awt.AWTKeyStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.KeyboardFocusManager;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.ButtonModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import net.miginfocom.swing.MigLayout;
 import org.wwscc.bwtimer.TimeStorage;
 import org.wwscc.bwtimer.TimerModel;
 import org.wwscc.dialogs.SimpleFinderDialog;
 import org.wwscc.storage.Database;
 import org.wwscc.storage.Run;
 import org.wwscc.timercomm.SerialDataInterface;
 import org.wwscc.timercomm.TimerClient;
 import org.wwscc.util.IntTextField;
 import org.wwscc.util.MT;
 import org.wwscc.util.MessageListener;
 import org.wwscc.util.Messenger;
 import org.wwscc.util.NF;
 import org.wwscc.util.TimeTextField;
 
 
 /**
  * Implements the time entry panel used in the data entry GUI
  */
 @SuppressWarnings("serial")
 public class TimeEntry extends JPanel implements ActionListener, ListSelectionListener, ListDataListener, MessageListener, KeyListener
 {
 	private static final Logger log = Logger.getLogger(TimeEntry.class.getCanonicalName());
 
 	/**
 	 * The timer input mode
 	 */
 	public enum Mode { 
 		/** manual input only  */    OFF,
 		/** serial line timers */    BASIC_SERIAL, 
 		/** bwtimer serial line */   BWTIMER_SERIAL, 
 		/** bwtimer over network */  BWTIMER_NETWORK, 
 		/** protimer over network */ PROTIMER_NETWORK 
 	};
 
 	Mode mode;
 	TimerClient tclient;
 	String commPort;
 	JList<Run> timeList;
 	TimeStorage activeModel;
 	TimeStorage defaultModel;
 	TimeStorage course2Model;
 
 	JLabel reactionLabel;
 	JLabel sixtyLabel;
 	TimeTextField reaction;
 	TimeTextField sixty;
 	TimeTextField time;
 	IntTextField cones;
 	IntTextField gates;
 	TimeTextField segVal[];
 	JLabel segLabel[];
 	JComboBox<String> status;
 	JTextArea error;
 
 	JButton del;
 	JButton enter;
 
 	JLabel connectionStatus;
 	ModeButtonGroup modeGroup;
 	
 	/**
 	 * Special focus listener for cones and gates entry
 	 */
 	final static class SelectAllFocusListener implements FocusListener
 	{
 		@Override
 		public void focusGained(FocusEvent e) {
 			JTextField tf = (JTextField)e.getComponent();
 			tf.selectAll();
 		}
 
 		@Override
 		public void focusLost(FocusEvent e) {
 			JTextField tf = (JTextField)e.getComponent();
 			tf.select(0,0);
 		}
 	}
 	
 	/**
 	 * Create a new timer entry widget
 	 * @throws IOException 
 	 */
 	public TimeEntry() throws IOException
 	{
 		super();
 		Messenger.register(MT.TIMER_TAKES_FOCUS, this);
 		Messenger.register(MT.TIMER_SERVICE_CONNECTION, this);
 		Messenger.register(MT.OBJECT_DCLICKED, this);
 		Messenger.register(MT.EVENT_CHANGED, this);
 		Messenger.register(MT.COURSE_CHANGED, this);
 		Messenger.register(MT.TIME_ENTER_REQUEST, this);
 
 		connectionStatus = new JLabel("");
 		modeGroup = new ModeButtonGroup();
 		
 		mode = Mode.OFF;
 		tclient = null;
 		commPort = null;
 		defaultModel = new SimpleTimeListModel(0);
 		course2Model = defaultModel;
 		activeModel = defaultModel;
 		activeModel.addListDataListener(this);
 
 		timeList = new JList<Run>(activeModel);
 		timeList.addListSelectionListener(this);
 		timeList.setCellRenderer(new RunListRenderer());
 		timeList.setFixedCellHeight(26);
 
 		reactionLabel = new JLabel("Reac");
 		sixtyLabel = new JLabel("Sixty");
 
 		reaction = new TimeTextField("", 6);
 		sixty = new TimeTextField("", 6);
 		segVal = new TimeTextField[Run.SEGMENTS];
 		segLabel = new JLabel[Run.SEGMENTS];
 		for (int ii = 0; ii < Run.SEGMENTS; ii++)
 		{
 			segVal[ii] = new TimeTextField("", 6);
 			segLabel[ii] = new JLabel("Seg" + (ii+1));
 		}
 		time = new TimeTextField("", 6);
 		time.setToolTipText("Enter Time Here. Press c or g here to add cones or gates, shift+c/g to remove them");
 		cones = new IntTextField("0", 2);
 		gates = new IntTextField("0", 2);
 
 		cones.addFocusListener(new SelectAllFocusListener());
 		gates.addFocusListener(new SelectAllFocusListener());
 
 		// set ` key as tab for mistypes in time/penalty fields
 		Set<AWTKeyStroke> s = new HashSet<AWTKeyStroke>(
 			time.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
 		s.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_BACK_QUOTE, 0));
 
 		for (JComponent c : new JComponent[] { reaction, sixty, time, cones, gates })
 			c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, s);
 		for (int ii = 0; ii < Run.SEGMENTS; ii++)
 			segVal[ii].setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, s);
 		
 		registerKeyboardAction(
 			this,
 			"Enter Time",
 			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
 			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
 		);
 		
 		status = new JComboBox<String>(new String[] { "OK", "DNF", "DNS", "RL", "NS", "DSQ" });
 		enter = new JButton("Enter Time");
 		enter.addActionListener(this);
 		enter.setDefaultCapable(true);
 		del = new JButton("Delete From List");
 		del.setFont(new Font(null, Font.PLAIN, 11));
 		del.setMargin(new Insets(0,0,0,0));
 		del.addActionListener(this);
 
 
 		error = new JTextArea("");
 		error.setEditable(false);
 		error.setLineWrap(true);
 		error.setWrapStyleWord(true);
 		error.setBackground((Color)UIManager.get("Label.background"));
 		error.setForeground(Color.RED);
 		error.setFont((Font)UIManager.get("Label.font"));
 
 
 		JScrollPane scroll = new JScrollPane(timeList);
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		
 		setLayout(new MigLayout("ins 0 0 0 4, hidemode 3, fillx", "[al right, 50!][fill,grow]", ""));
 		add(connectionStatus, "spanx 2, al center, wrap");
 		add(del, "spanx 2, growx, wrap");
 		add(scroll, "spanx 2, growx, h 50:300:500, wrap, w 120!");
 		add(reactionLabel, "");
 		add(reaction, "wrap");
 		add(sixtyLabel, "");
 		add(sixty, "wrap");
 		for (int ii = 0; ii < Run.SEGMENTS; ii++)
 		{
 			add(segLabel[ii], "");
 			add(segVal[ii], "wrap");
 		}
 		add(new JLabel("Time"), "");
 		add(time, "wrap");
 		add(new JLabel("Cones"), "");
 		add(cones, "wrap");
 		add(new JLabel("Gates"), "");
 		add(gates, "wrap");
 		add(new JLabel("Status"), "");
 		add(status, "wrap");
 		add(enter, "spanx 2, growx, wrap");
 		add(error, "spanx 2, wrap");
 
 		switchTimerMode(Mode.OFF);
 		
 		time.addKeyListener(this);
 		cones.addKeyListener(this);
 		gates.addKeyListener(this);
 		reaction.addKeyListener(this);
 		sixty.addKeyListener(this);
 		enter.addKeyListener(this);
 		
 	}
 	/**
 
 	 * @return the default enter button for use by the application
 	 */
 	public JButton getEnterButton()
 	{
 		return enter;
 	}
 
 	/**
 	 * Get the menu used for this timer
 	 * @return a JMenu for use by the application
 	 */
 	public JMenu getTimerMenu()
 	{
 		JMenu timerMenu = new JMenu("Timer");
 		for (Mode m : Mode.values())
 		{
 			JRadioButtonMenuItem bm = new JRadioButtonMenuItem();
 			bm.setActionCommand(m.name());
 			bm.addActionListener(this);
 			timerMenu.add(bm);
 			modeGroup.add(bm);
 			switch (m)
 			{
 				case OFF: bm.setText("Off"); break;
 				case BASIC_SERIAL: bm.setText("RaceAmerica/JACircuits"); break;
 				case BWTIMER_SERIAL: bm.setText("BWTimer Serial"); break;
 				case BWTIMER_NETWORK: bm.setText("BWTimer Network"); break;
 				case PROTIMER_NETWORK: bm.setText("ProTimer Network"); break;
 			}
 		}
 
 		modeGroup.setSelected(Mode.OFF);
 		return timerMenu;
 	}
 
 	/**
 	 * Change the timer input mode for the this TimeEntry box
 	 * @param newMode the mode to switch to
 	 */
 	private void switchTimerMode(Mode newMode)
 	{
 		try
 		{
 			String newCommPort = "";
 			InetSocketAddress newAddr = null;
 
 			/* First see if they can provide the necessary details */
 			switch (newMode)
 			{
 				case BASIC_SERIAL:
 					if ((newCommPort = SerialDataInterface.selectPort("BasicSerial")) == null)
 						throw new Exception("cancel");
 					break;
 				case BWTIMER_SERIAL:
 					if ((newCommPort = SerialDataInterface.selectPort("BWSerial")) == null)
 						throw new Exception("cancel");
 					break;
 				case BWTIMER_NETWORK:
 					SimpleFinderDialog dialog = new SimpleFinderDialog("BWTimer");
 					dialog.doDialog("Find BW Timers", null);
 					if ((newAddr = dialog.getResult()) == null)
 						throw new Exception("cancel");
 					break;
 				case PROTIMER_NETWORK:					
 					SimpleFinderDialog dialog2 = new SimpleFinderDialog("ProTimer");
 					dialog2.doDialog("Find Pro Timers", null);
 					if ((newAddr = dialog2.getResult()) == null)
 						throw new Exception("cancel");
 					break;
 			}
 
 			/* Turn current stuff off */
 			Messenger.unregisterAll(defaultModel);
 			Messenger.unregisterAll(course2Model);
 			switch (mode)
 			{
 				case BASIC_SERIAL:
 					SerialDataInterface.close(commPort);
 					commPort = null;
 					break;
 					
 				case BWTIMER_SERIAL:
 					SerialDataInterface.close(commPort);
 					commPort = null;
 					break;
 
 				case BWTIMER_NETWORK:
 				case PROTIMER_NETWORK:
 					tclient.stop();
 					tclient = null;
 					break;
 			}
 
 			/* Reset switchable components */
 			for (int ii = 0; ii < Run.SEGMENTS; ii++)
 			{
 				segVal[ii].setVisible(false);
 				segLabel[ii].setVisible(false);
 			}
 
 			/* Now try and setup the new model */
 			switch (newMode)
 			{
 				case OFF:
 					break;
 					
 				case BASIC_SERIAL:
 					commPort = newCommPort;
 					SerialDataInterface.open(commPort);
 					defaultModel = new SimpleTimeListModel(0);
 					course2Model = defaultModel;
 					break;
 
 				case BWTIMER_SERIAL:
 					commPort = newCommPort;
 					SerialDataInterface.open(commPort);
 					defaultModel = new TimerModel();
 					course2Model = defaultModel;
 					break;
 
 				case BWTIMER_NETWORK:
 					tclient = new TimerClient(newAddr);
 					tclient.start();
 					defaultModel = new SimpleTimeListModel(0);
 					course2Model = defaultModel;
 					break;
 
 				case PROTIMER_NETWORK:
 					tclient = new TimerClient(newAddr);
 					tclient.start();
 					defaultModel = new SimpleTimeListModel(1);
 					course2Model = new SimpleTimeListModel(2);
 					break;
 			}
 
 			mode = newMode;
 			event(MT.COURSE_CHANGED, null);
 		}
 		catch (Exception ioe) // IOError, etc, warn and go to off mode
 		{
 			String msg = ioe.getMessage();
 			if ((msg != null) && !msg.equals("cancel"))
 			{
 				log.log(Level.WARNING, "Timer Select Failed ({0}), turning Off", ioe.getMessage());
 				mode = Mode.OFF;				
 			}
 
 			if (modeGroup != null)
 				modeGroup.setSelected(mode);  // Select off or previous mode if we canceled early.
 		}
 
 		String msg = modeGroup.getSelected();
 		if ((msg == null) || msg.equals("") || msg.equals("Off"))
 		{
 			msg = "Not Connected";
 			connectionStatus.setForeground(Color.RED);
 		}
 		else
 			connectionStatus.setForeground(Color.BLACK);
 		connectionStatus.setText(msg);
 	}
 
 	/**
 	 * Called when there is a request (button/program) to enter the current time/penalties
 	 */
 	private void enterTime()
 	{
 		String sStatus = (String)status.getSelectedItem();
 		double dTime = time.getTime();
 
 		try
 		{
 			/* Beep and exit if status is OK and we don't have a time */
 			if (Double.isNaN(dTime) && sStatus.equals("OK"))
 				throw new IndexOutOfBoundsException("No time or status entered");
 
 			/* Create a run from the text boxes and send */
 			Run val = new Run(reaction.getTime(), sixty.getTime(), dTime, cones.getInt(), gates.getInt(), sStatus);
 			for (int ii = 0; ii < Run.SEGMENTS; ii++)
 				val.setSegment(ii+1, segVal[ii].getTime());
 
 			Messenger.sendEventNow(MT.TIME_ENTERED, val);
 
 			/* If everything progressed okay remove any times from the serial port
 				if they were selected and used, ... */
 			Object o = timeList.getSelectedValue();
 			if (o instanceof Run)
 			{
 				Run r = (Run)o;
 				if ((r != null) && (r.getRaw() == dTime))
 				{
 					activeModel.remove(timeList.getSelectedIndex());
 				}
 			}
 			/* ... select the next value if one exists */
 			selectNext(0);
 		}
 		catch (Exception e)
 		{
 			String msg = e.getMessage();
 			error.setText((msg != null)?msg : "Unknown error, see log trace");
 			log.log(Level.INFO, "Time entry failed: " + e.getMessage(), e);
             Toolkit.getDefaultToolkit().beep();
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		String cmd = e.getActionCommand();
 		if (cmd.equals("Enter Time"))
 		{
 			enterTime();
 		}
 		else if (cmd.equals("Delete From List"))
 		{
 			int index = timeList.getSelectedIndex();
 			if (index >= 0)
 			{
 				activeModel.remove(index);
 				selectNext(index);
 			}
 		}
 
 		else
 		{
 			try {
 				switchTimerMode(Mode.valueOf(cmd));
 			} catch (IllegalArgumentException iae) {
 				log.log(Level.INFO, "Unknown command: {0}", cmd);
 			}
 		}
 	}
 
 	/**
 	 * A new time came in via the connected timer
 	 * @param e only use the source of the list event data
 	 */
 	@Override
     public void intervalAdded(ListDataEvent e)
 	{
 		// Select first item if its brand new and we aren't editing another time
 		TimeStorage s = (TimeStorage)e.getSource();
 		if ((s.getFinishedCount() == 1) && (time.getText().equals("")))
 		{
 			Component compFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
 			while (compFocusOwner != null)
 			{
 				if ((compFocusOwner instanceof TimeEntry) || (compFocusOwner instanceof RunsTable))
 				{
 					selectNext(0);  // only select and pull focus if users isn't 'focused' doing something else
 					break;
 				}
 				compFocusOwner = compFocusOwner.getParent();
 			}
 		}
 	}
 
 	@Override
     public void intervalRemoved(ListDataEvent e) {}
 	@Override
     public void contentsChanged(ListDataEvent e) {}
 
 	
 	/**
 	 * Reset the selection in the index list, taking into account list size
 	 * @param index 
 	 */
 	protected void selectNext(int index)
 	{
 		int size = activeModel.getFinishedCount();
 		if (size > 0)
 		{
 			timeList.clearSelection();
 			timeList.setSelectedIndex(0);
 
 			if (index >= size)
 				timeList.setSelectedIndex(size-1);
 			else
 				timeList.setSelectedIndex(index);
 		}
 		else
 		{
 			clearValues();
 		}
 	}
 
 	
 	/**
 	 * Clear the values in all of the available entry boxes
 	 */
 	protected void clearValues()
 	{
 		reaction.setTime(0);
 		sixty.setTime(0);
 		for (int ii = 0; ii < Run.SEGMENTS; ii++)
 			segVal[ii].setTime(0);
 		time.setText("");
 		cones.setInt(0);
 		gates.setInt(0);
 		status.setSelectedIndex(0);
 		error.setText("");
 	}
 
 
 	/**
 	 * Set all the values in all of the available entry boxes
 	 * @param r the Run to take the values from
 	 */
 	protected void setValues(Run r)
 	{
 		reaction.setTime(r.getReaction());
 		sixty.setTime(r.getSixty());
 		for (int ii = 0; ii < Run.SEGMENTS; ii++)
 		{
 			double d = r.getSegment(ii+1);
 			if (d > 0)
 			{
 				segVal[ii].setVisible(true);
 				segLabel[ii].setVisible(true);
 			}
 			segVal[ii].setTime(d);
 		}
 		time.setTime(r.getRaw());
 		cones.setInt(r.getCones());
 		gates.setInt(r.getGates());
 		status.setSelectedItem(r.getStatus());
 		error.setText("");
 	}
 
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) 
 	{
 		if (!e.getValueIsAdjusting() && (e.getSource() == timeList))
 		{
 			Object o = timeList.getSelectedValue();
 			if (o instanceof Run)
 			{
 				setValues((Run)o);
 				cones.requestFocusInWindow();
 			}
			else if (o != null)  // null is passed in in some instances, do not clear values
 			{
 				clearValues();
 			}
 		}
 	}
 
 	@Override
 	public void event(MT type, Object o)
 	{
 		switch (type)
 		{
 			case OBJECT_DCLICKED:
 				if (o instanceof Run)
 				{
 					timeList.clearSelection();
 					setValues((Run)o);
 					time.requestFocusInWindow();
 					time.selectAll();
 				}
 				else if (o == null)
 				{
 					timeList.clearSelection();
 					time.requestFocusInWindow();
 					clearValues();
 				}
 				break;
 				
 			case EVENT_CHANGED:
 				if (Database.d.getCurrentEvent().isPro())
 				{
 					reaction.setVisible(true);
 					reactionLabel.setVisible(true);
 					sixty.setVisible(true);
 					sixtyLabel.setVisible(true);
 				}
 				else
 				{
 					reaction.setVisible(false);
 					reactionLabel.setVisible(false);
 					sixty.setVisible(false);
 					sixtyLabel.setVisible(false);
 				}
 				break;
 
 			case COURSE_CHANGED:
 				if (activeModel != null)
 					activeModel.removeListDataListener(this);
 				if (Database.d.getCurrentCourse() == 2)
 					activeModel = course2Model;
 				else
 					activeModel = defaultModel;
 				activeModel.addListDataListener(this);
 				timeList.setModel(activeModel);
 				break;
 
 			case TIMER_SERVICE_CONNECTION:
 				Object[] a = (Object[])o;
 				if ((a[0] == tclient) && ((Boolean)a[1]))
 				{
 					connectionStatus.setForeground(Color.RED);
 					connectionStatus.setText("Not Connected");
 				}
 				break;
 				
 			case TIME_ENTER_REQUEST:
 				enterTime();
 				break;
 		}
 	}
 	
 	@Override
 	public void keyTyped(KeyEvent e)
 	{
 		Object o = e.getSource();
 		if(o instanceof JTextField || o == enter)
 		{
 			int increment = ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) ? -1 : 1;
 			
 			switch(e.getKeyChar())
 			{
 				case 'c':
 				case 'C':
 					cones.setText(Integer.toString(cones.getInt() + increment));
 					e.consume();
 					break;
 				case 'g':
 				case 'G':
 					gates.setText(Integer.toString(gates.getInt() + increment));
 					e.consume();
 					break;
 			}
 		}
 	}
 	@Override
 	public void keyPressed(KeyEvent e) { /* do nothing */ }
 	@Override
 	public void keyReleased(KeyEvent e) { /* do nothing */ }
 }
 
 
 class ModeButtonGroup extends ButtonGroup
 {
 	public void setSelected(TimeEntry.Mode m)
 	{
 		for (AbstractButton b : buttons)
 		{
 			if (b.getActionCommand().equals(m.name()))
 			{
 				setSelected(b.getModel(), true);
 				break;
 			}
 		}
 	}
 
 	public String getSelected()
 	{
 		ButtonModel m = getSelection();
 		for (AbstractButton b : buttons)
 		{
 			if (b.getModel() == m)
 			{
 				return b.getText();
 			}
 		}
 		return null;
 	}
 }
 
 
 class RunListRenderer extends DefaultListCellRenderer
 {
 	protected Font font;
 
 	public RunListRenderer()
 	{
 		font = new Font("sansserif", Font.PLAIN, 22);
 	}
 
 	@Override
 	public Component getListCellRendererComponent(JList<?> l, Object o, int i, boolean is, boolean f) 
 	{
 		super.getListCellRendererComponent(l, o, i, is, f);
 
 		if (o instanceof Run)
 		{
 			Run r = (Run)o;
 			setText(NF.format(r.getRaw()));
 		}
 		else if (o instanceof Double)
 		{
 			setText(NF.format((Double)o));
 		}
 
 		setFont(font);
 		return this;
 	}
 }
 
 
