 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.console.swingui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.grinder.console.ConsoleException;
 import net.grinder.console.model.Model;
 import net.grinder.console.model.ModelListener;
 import net.grinder.console.model.SampleListener;
 import net.grinder.plugininterface.Test;
 import net.grinder.statistics.Statistics;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class ConsoleUI implements ModelListener
 {
     private static ResourceBundle s_resources = null;
 
     private final static Font s_tpsFont =
 	new Font("helvetica", Font.ITALIC | Font.BOLD, 40);
 
     private final static NumberFormat s_twoDPFormat =
 	new DecimalFormat("0.00");
 
     private final HashMap m_actionTable;
     private final Model m_model;
     private final JLabel m_collectSampleLabel;
     private final JLabel m_ignoreSampleLabel;
     private final JLabel m_intervalLabel;
     private final JLabel m_stateLabel;
     private SummaryFrame m_summaryFrame = null;
     private Image m_logoImage = null;
 
     public ConsoleUI(Model model, ActionListener startProcessesHandler,
 		     ActionListener stopProcessesHandler)
 	throws ConsoleException
     {
 	getResources();
 
 	final Action[] actions = {
 	    new StartProcessesGrinderAction(startProcessesHandler),
 	    new StopProcessesGrinderAction(stopProcessesHandler),
 	    new StartAction(),
 	    new StopAction(),
 	    new SummaryAction(),
 	    new ExitAction(),
 	};
 
 	m_actionTable = new HashMap();
 
 	for (int i=0; i<actions.length; i++) {
 	    m_actionTable.put(actions[i].getValue(Action.NAME), actions[i]);
 	}
 
 	m_model = model;
 
 	// Create a scrolled pane of test graphs.
         final JPanel testPanel = new JPanel();
 	testPanel.setLayout(new GridLayout(0, 2, 20, 0));
 
 	final Iterator testIterator = m_model.getTests().iterator();
 
 	while (testIterator.hasNext())
 	{
 	    final Test test = (Test)testIterator.next();
 
 	    final Integer testNumber = test.getTestNumber();
 	    final String description = test.getDescription();
 
 	    String label = "Test " + testNumber;
 
 	    if (description != null) {
 		label = label + " (" + description + ")";
 	    }
 
 	    final LabelledGraph testGraph = new LabelledGraph(label);
 
 	    m_model.addSampleListener(
 		testNumber,
 		new SampleListener() {
 			public void update(double tps, double averageTPS,
 					   double peakTPS, Statistics total) {
 			    testGraph.add(tps, averageTPS, peakTPS, total);
 			}
 		    }
 		);
 
 	    testPanel.add(testGraph);
 	}
 
 	// Make space for vertical scroll bar.
 	testPanel.setBorder(new EmptyBorder(0, 0, 0, 40));
 
         final JScrollPane scrollPane =
 	    new JScrollPane(testPanel,
 			    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 			    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
 	final LabelledGraph totalGraph = new LabelledGraph("Total",
 							   Color.darkGray);
 
	final JLabel tpsLabel = new JLabel();
 	tpsLabel.setForeground(Color.black);
 	tpsLabel.setFont(s_tpsFont);
 
 	m_model.addTotalSampleListener(
 	    new SampleListener() {
 		    public void update(double tps, double average,
 				       double peak, Statistics total) {
 			tpsLabel.setText(s_twoDPFormat.format(tps) + " TPS");
 			totalGraph.add(tps, average, peak, total);
 		    }
 		}
 	    );
 
 	final JSlider intervalSlider =
 	    new JSlider(100, 10000, m_model.getSampleInterval());
 	intervalSlider.setMajorTickSpacing(1000);
 	intervalSlider.setMinorTickSpacing(100);
 	intervalSlider.setPaintTicks(true);
 	intervalSlider.setSnapToTicks(true);
 
 	intervalSlider.addChangeListener(
 	    new ChangeListener() {
 		    public void stateChanged(ChangeEvent e) {
 			m_model.setSampleInterval(intervalSlider.getValue());
 		    }
 		}
 	    );
 
 	m_intervalLabel = new JLabel();
 
 	final JSlider ignoreSampleSlider =
 	    new JSlider(0, 9, m_model.getIgnoreSampleCount());
 	ignoreSampleSlider.setMajorTickSpacing(1);
 	ignoreSampleSlider.setSnapToTicks(true);
 	ignoreSampleSlider.setPaintTicks(true);
 
 	m_ignoreSampleLabel = new JLabel();
 
 	ignoreSampleSlider.addChangeListener(
 	    new ChangeListener() {
 		    public void stateChanged(ChangeEvent e) {
 			m_model.setIgnoreSampleCount(
 			    ignoreSampleSlider.getValue());
 		    }
 		}
 	    );
 
 	final JSlider collectSampleSlider =
 	    new JSlider(0, 50, m_model.getCollectSampleCount());
 	collectSampleSlider.setMajorTickSpacing(1);
 	collectSampleSlider.setSnapToTicks(true);
 	collectSampleSlider.setPaintTicks(true);
 
 	m_collectSampleLabel = new JLabel();
 
 	collectSampleSlider.addChangeListener(
 	    new ChangeListener() {
 		    public void stateChanged(ChangeEvent e) {
 			m_model.setCollectSampleCount(
 			    collectSampleSlider.getValue());
 		    }
 		}
 	    );
 
 	m_stateLabel = new JLabel();
 
 	final JPanel controlPanel = new JPanel();
 	controlPanel.setLayout(new GridLayout(0, 1));
 	controlPanel.add(m_intervalLabel);
 	controlPanel.add(intervalSlider);
 	controlPanel.add(m_ignoreSampleLabel);
 	controlPanel.add(ignoreSampleSlider);
 	controlPanel.add(m_collectSampleLabel);
 	controlPanel.add(collectSampleSlider);
 	controlPanel.add(m_stateLabel);
 	controlPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
 
 	final JPanel controlAndTotalPanel = new JPanel();
 	controlAndTotalPanel.setLayout(
 	    new BoxLayout(controlAndTotalPanel, BoxLayout.Y_AXIS));

 	controlAndTotalPanel.add(controlPanel);
 	controlAndTotalPanel.add(Box.createRigidArea(new Dimension(0, 50)));
 	controlAndTotalPanel.add(tpsLabel);
 	controlAndTotalPanel.add(Box.createRigidArea(new Dimension(0, 30)));
 	controlAndTotalPanel.add(totalGraph);
 
	// Really wanted this left alligned, but doesn't really work
	// with a box layout.
	tpsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

 	final JPanel hackToFixLayout = new JPanel();
 	hackToFixLayout.add(controlAndTotalPanel);
 
 	final URL logoURL = getResource("logo.image");
 
 	final JPanel contentPanel = new JPanel();
 
 	contentPanel.setLayout(new BorderLayout());
 	contentPanel.add(hackToFixLayout, BorderLayout.WEST);
 	contentPanel.add(scrollPane, BorderLayout.CENTER);
 
 	if (logoURL != null) {
 	    final ImageIcon imageIcon = new ImageIcon(logoURL);
 	    final JLabel logo = new JLabel(imageIcon, SwingConstants.LEADING);
 	    contentPanel.add(logo, BorderLayout.EAST);
 
 	    m_logoImage = imageIcon.getImage();
 	}
 
 	// Create a panel to hold the tool bar and the test pane.
         final JPanel toolBarPanel = new JPanel();
 	toolBarPanel.setLayout(new BorderLayout());
 	toolBarPanel.add(createToolBar(), BorderLayout.NORTH);
 	toolBarPanel.add(contentPanel, BorderLayout.CENTER);
 
 	// Create the frame, containing the a menu and the top level pane.
 	final JFrame frame = new JFrame(getResourceString("title"));
 
         frame.addWindowListener(new WindowCloseAdapter());
 	final Container topLevelPane= frame.getContentPane();
 	topLevelPane.add(createMenuBar(), BorderLayout.NORTH);
         topLevelPane.add(toolBarPanel, BorderLayout.CENTER);
 
 	if (m_logoImage != null) {
 	    frame.setIconImage(m_logoImage);
 	}
 
 	m_model.addModelListener(this);
 	update();
 
         frame.pack();
 
 	// Arbitary sizing that looks good for Phil.
 	final int maxHeight = 600;
 	final Dimension d = frame.getSize();
 
 	if (d.height > maxHeight) {
 	    d.height = maxHeight;
 	    frame.setSize(d);
 	}
 
         frame.show();
     }
 
     private JMenuBar createMenuBar()
     {
 	final JMenuBar menuBar = new JMenuBar();
 
 	final Iterator menuBarIterator =
 	    tokenise(getResourceString("menubar"));
 	
 	while (menuBarIterator.hasNext()) {
 	    final String menuKey = (String)menuBarIterator.next();
 	    final JMenu menu =
 		new JMenu(getResourceString(menuKey + ".menu.label"));
 
 	    final Iterator menuIterator =
 		tokenise(getResourceString(menuKey + ".menu"));
 
 	    while (menuIterator.hasNext()) {
 		final String menuItemKey = (String)menuIterator.next();
 
 		if ("-".equals(menuItemKey)) {
 		    menu.addSeparator();
 		}
 		else {
 		    final JMenuItem menuItem =
 			new JMenuItem(
 			    getResourceString(menuItemKey + ".label"));
 
 		    final URL imageURL = getResource(menuItemKey + ".image",
 						     false);
 
 		    if (imageURL != null) {
 			menuItem.setIcon(new ImageIcon(imageURL));
 		    }
 
 		    setAction(menuItem, menuItemKey);
 		    menu.add(menuItem);
 		}
 	    }
 
 	    menuBar.add(menu);
 	}
 
 	return menuBar;
     }
 
     private JToolBar createToolBar() 
     {
 	final JToolBar toolBar = new JToolBar();
 	
 	final Iterator toolBarIterator =
 	    tokenise(getResourceString("toolbar"));
 	
 	while (toolBarIterator.hasNext()) {
 	    final String toolKey = (String)toolBarIterator.next();
 
 	    if ("-".equals(toolKey)) {
 		toolBar.addSeparator();
 	    }
 	    else {
 		final URL url = getResource(toolKey + ".image");
 
 		if (url != null) {
 		    final JButton button = new JButton(new ImageIcon(url));
 		
 		    setAction(button, toolKey);
 
 		    final String tipString =
 			getResourceString(toolKey + ".tip", false);
 
 		    if (tipString != null) {
 			button.setToolTipText(tipString);
 		    }
 
 		    toolBar.add(button);
 		}
 	    }
 	}
 
 	return toolBar;
     }
 
     private void setAction(AbstractButton  button, String resourceKey)
     {
 	final String actionString = getResourceString(resourceKey + ".action");
 	final Action action = (Action)m_actionTable.get(actionString);
 
 	if (action != null) {
 	    action.addPropertyChangeListener(
 		new ActionChangedListener(button));
 
 	    button.addActionListener(action);
 	    button.setActionCommand(actionString);
 	    button.setEnabled(action.isEnabled());
 	}
 	else {
 	    System.err.println("Action '" + actionString + "' not found");
 	    button.setEnabled(false);
 	}
     }
 
     public void update()
     {
 	// Ignoring synchronisation issues for now.
 	final int state = m_model.getState();
 	final long sampleInterval = m_model.getSampleInterval();
 	final long sampleCount = m_model.getSampleCount();
 	final int ignoreCount = m_model.getIgnoreSampleCount();
 	final int collectCount = m_model.getCollectSampleCount();
 	final boolean receivedSample = m_model.getRecievedSample();
 
 	if (state == Model.STATE_WAITING_FOR_TRIGGER) {
 	    if (receivedSample) {
 		m_stateLabel.setText(
 		    "Waiting for samples (ignoring: " + sampleCount + ")");
 	    }
 	    else {
 		m_stateLabel.setText("Waiting for samples");
 	    }
 	}
 	else if (state == Model.STATE_STOPPED) {
 	    if (receivedSample) {
 		m_stateLabel.setText(
 		    "Collection stopped, ignoring samples");
 	    }
 	    else {
 		m_stateLabel.setText("Collection stopped");
 	    }
 	}
 	else if (state == Model.STATE_CAPTURING) {
 	    m_stateLabel.setText("Collecting samples: " + sampleCount);
 	}
 	else {
 	    m_stateLabel.setText("UNKNOWN STATE");
 	}
 
 	m_intervalLabel.setText("Sample interval: " + sampleInterval + " ms");
 
 	if (ignoreCount == 0) {
 	    m_ignoreSampleLabel.setText("Start on first sample");
 	}
 	else if (ignoreCount == 1) {
 	    m_ignoreSampleLabel.setText("Ignore first sample");
 	}
 	else {
 	    m_ignoreSampleLabel.setText("Ignore first " + ignoreCount +
 					" samples");
 	}
 
 	if (collectCount == 0) {
 	    m_collectSampleLabel.setText("Collect samples forever");
 	}
 	else if (collectCount == 1) {
 	    m_collectSampleLabel.setText("Stop after 1 sample");
 	}
 	else {
 	    m_collectSampleLabel.setText("Stop after " + collectCount +
 					 " samples");
 	}
     }
 
     private class ActionChangedListener implements PropertyChangeListener
     {
         final AbstractButton m_button;
         
         ActionChangedListener(AbstractButton button)
 	{
             super();
             m_button = button;
         }
 
         public void propertyChange(PropertyChangeEvent e) 
 	{
             final String propertyName = e.getPropertyName();
 
             if (e.getPropertyName().equals(Action.NAME))
 	    {
                 final String text = (String)e.getNewValue();
                 m_button.setText(text);
             }
 	    else if (propertyName.equals("enabled"))
 	    {
                 final Boolean enabledState = (Boolean)e.getNewValue();
                 m_button.setEnabled(enabledState.booleanValue());
             }
         }
     }
 
     private static final class WindowCloseAdapter extends WindowAdapter
     {
 	public void windowClosing(WindowEvent e)
 	{
 	    System.exit(0);
 	}
     }
 
     private class ExitAction extends AbstractAction
     {
 	ExitAction()
 	{
 	    super("exit");
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    System.exit(0);
 	}
     }
 
     private class StartAction extends AbstractAction
     {
 	StartAction()
 	{
 	    super("start");
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    m_model.start();
 	}
     }
 
     private class StopAction extends AbstractAction
     {
 	StopAction()
 	{
 	    super("stop");
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    m_model.stop();
 	}
     }
 
     private class StartProcessesGrinderAction extends AbstractAction
     {
 	private final ActionListener m_delegateAction;
 
 	StartProcessesGrinderAction(ActionListener delegateAction)
 	{
 	    super("start-processes");
 	    m_delegateAction = delegateAction;
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    m_delegateAction.actionPerformed(e);
 	}
     }
 
     private class StopProcessesGrinderAction extends AbstractAction
     {
 	private final ActionListener m_delegateAction;
 
 	StopProcessesGrinderAction(ActionListener delegateAction)
 	{
 	    super("stop-processes");
 	    m_delegateAction = delegateAction;
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    m_delegateAction.actionPerformed(e);
 	}
     }
 
     private class SummaryAction extends AbstractAction
     {
 	SummaryAction()
 	{
 	    super("summary");
 	}
 
         public void actionPerformed(ActionEvent e)
 	{
 	    if (m_summaryFrame == null) {
 		synchronized(ConsoleUI.this) {
 		    if (m_summaryFrame == null) {
 			m_summaryFrame =
 			    new SummaryFrame(m_model,
 					     getResourceString(
 						 "summaryTitle"));
 
 			if (m_logoImage != null) {
 			    m_summaryFrame.setIconImage(m_logoImage);
 			}
 		    }
 		}
 	    }
 
 	    m_summaryFrame.displaySummary();
 	}
     }
 
     static void getResources()
 	throws ConsoleException
     {
 	try {
 	    s_resources = ResourceBundle.getBundle(
 		"net.grinder.console.swingui.resources.Console");
 	}
 	catch (MissingResourceException e) {
 	    throw new ConsoleException("Resource bundle not found");
 	}
     }
 
     private static Iterator tokenise(String string)
     {
 	final LinkedList list = new LinkedList();
 
 	final StringTokenizer t = new StringTokenizer(string);
 
 	while (t.hasMoreTokens()) {
 	    list.add(t.nextToken());
 	}
 
 	return list.iterator();
     }
 
     private String getResourceString(String key)
     {
 	return getResourceString(key, true);
     }
 
     private String getResourceString(String key, boolean warnIfMissing)
     {
 	try {
 	    return s_resources.getString(key);
 	}
 	catch (MissingResourceException e) {
 	    if (warnIfMissing) {
 		System.err.println(
 		    "Warning - resource " + key + " not specified");
 	    }
 	    
 	    return "";
 	}
     }
 
     private URL getResource(String key)
     {
 	return getResource(key, true);
     }
 
     private URL getResource(String key, boolean warnIfMissing)
     {
 	final String name = getResourceString(key, true);
 
 	if (name.length() == 0) {
 	    return null;
 	}
 	
 	final URL url = this.getClass().getResource("resources/" + name);
 
 	if (warnIfMissing && url == null) {
 	    System.err.println("Warning - could not load resource " + name);
 	}
 
 	return url;
     }
 }
