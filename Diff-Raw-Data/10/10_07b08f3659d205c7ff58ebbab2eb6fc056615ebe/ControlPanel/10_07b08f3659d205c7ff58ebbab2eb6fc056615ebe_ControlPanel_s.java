 
 package gui.panels;
 
 import gui.panels.subcontrolpanels.ConfigSelectPanel;
 import gui.panels.subcontrolpanels.GlassInfoPanel;
 import gui.panels.subcontrolpanels.GlassSelectPanel;
 import gui.panels.subcontrolpanels.LogoPanel;
 import gui.panels.subcontrolpanels.NonNormPanel;
 import gui.panels.subcontrolpanels.StatePanel;
 import gui.panels.subcontrolpanels.TitlePanel;
 import gui.panels.subcontrolpanels.TracePanel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 
 import transducer.TEvent;
 import transducer.Transducer;
 import transducer.TChannel;
 import transducer.TReceiver;
 
 /**
  * The ControlPanel class contains the buttons and panels that are responsible
  * for controlling the factory, printing agent traces, and running non-normatives
  * 
  * It is the GUI implementation of the Factory Control System (FCS)
  */
 @SuppressWarnings("serial")
 public class ControlPanel extends JPanel implements TReceiver
 {
 	/**
 	 * The parent panel for communication with the display
 	 */
 	FactoryPanel parent;
 
 	/**
 	 * Allows the control panel to communicate with the back end and give commands
 	 */
 	Transducer transducer;
 
 	/**
 	 * The panel containing the title
 	 */
 	TitlePanel titlePanel;
 
 	/**
 	 * The panel that controls whether the factory pauses
 	 */
 	StatePanel statePanel;
 
 	/**
 	 * The panel containing the buttons to control the configuration of glass
 	 */
 	GlassSelectPanel glassSelectPanel;
 
 	/*
 	 * The panel containing the buttons to production of the config
 	 */
 	ConfigSelectPanel configSelectPanel;
 	
 	/**
 	 * The panel displaying information on current glass production
 	 */
 	GlassInfoPanel glassInfoPanel;
 
 	/**
 	 * The panel handling non-normative events
 	 */
	NonNormPanel nonNormPanel;
 
 	/**
 	 * Panel holding logo
 	 */
 	LogoPanel logoPanel;
 
 	/**
 	 * A panel for printing backend messages.
 	 */
 	TracePanel tracePanel;
 
 	public final static Dimension size = new Dimension(400, 880);
 
 	/**
 	 * Creates a ControlPanel with no connections. Used only for testing
 	 * purposes
 	 */
 	public ControlPanel()
 	{
 		// manage layout
 		// this.setLayout(new GridLayout(5, 1));
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		this.setAlignmentY(JPanel.CENTER_ALIGNMENT);
 		this.setMaximumSize(size);
 		this.setPreferredSize(size);
 
 		this.setBackground(Color.black);
 		this.setForeground(Color.black);
 
 		// construct subpanels
 		titlePanel = new TitlePanel(this);
 		statePanel = new StatePanel(this);
 		configSelectPanel = new ConfigSelectPanel(this);
 		glassSelectPanel = new GlassSelectPanel(this);
 		glassInfoPanel = new GlassInfoPanel(this);
		nonNormPanel = new NonNormPanel(this);
 		tracePanel = new TracePanel(this, "Begin");
 		logoPanel = new LogoPanel(this);
 
 		// make tabbed panels
 
 		UIManager.put("TabbedPane.selected", Color.gray);
 
 		JTabbedPane selectTabbedPanel = new JTabbedPane(JTabbedPane.TOP);
 		selectTabbedPanel.add("Glass Select", glassSelectPanel);
 		selectTabbedPanel.add("Produce Config", configSelectPanel);
 		selectTabbedPanel.add("Non Norms", nonNormPanel);
 		selectTabbedPanel.setBackground(Color.black);
 		selectTabbedPanel.setForeground(Color.white);
 		selectTabbedPanel.setBorder(BorderFactory.createEmptyBorder());
 
 		JTabbedPane infoTabbedPanel = new JTabbedPane(JTabbedPane.TOP);
 		infoTabbedPanel.add("Glass Info", glassInfoPanel);
 		infoTabbedPanel.add("Agent Traces", tracePanel);
 		infoTabbedPanel.setBackground(Color.black);
 		infoTabbedPanel.setForeground(Color.white);
 		infoTabbedPanel.setBorder(BorderFactory.createEmptyBorder());
 
 		JPanel colorLinesPanel1 = new JPanel();
 		colorLinesPanel1.setPreferredSize(new Dimension(350, 10));
 		colorLinesPanel1.setBackground(Color.black);
 		JPanel colorLinesPanel2 = new JPanel();
 		colorLinesPanel2.setPreferredSize(new Dimension(350, 10));
 		colorLinesPanel2.setBackground(Color.black);
 		JPanel colorLines = new JPanel();
 		colorLines.setPreferredSize(new Dimension(350, 10));
 		colorLines.setBackground(Color.black);
 
 		ImageIcon colorLine = new ImageIcon("imageicons/doubleColoredLines.png");
 		ImageIcon cl = new ImageIcon("imageicons/singleColoredLine.png");
 		JLabel clLabel1 = new JLabel(cl);
 		JLabel clLabel2 = new JLabel(cl);
 		JLabel clDoubleLabel = new JLabel(colorLine);
 		colorLinesPanel1.add(clLabel1);
 		colorLinesPanel2.add(clLabel2);
 		colorLines.add(clDoubleLabel);
 
 		// add subpanels to control panel
 		this.add(titlePanel);
 		this.add(colorLines);
 		this.add(statePanel);
 		this.add(selectTabbedPanel);
 		this.add(colorLinesPanel1);
 		this.add(infoTabbedPanel);
 		this.add(colorLinesPanel2);
 		this.add(logoPanel);
 
 		System.out.println("Control Panel created.");
 	}
 
 	/**
 	 * Creates a ControlPanel with corresponding subpanels.
 	 * @param fPanel
 	 *        the parent FactoryPanel
 	 * @param cCell
 	 *        the ControlCell to link to this ControlPanel
 	 */
 	public ControlPanel(FactoryPanel fPanel, Transducer fTransducer)
 	{
 		this();
 
 		transducer = fTransducer;
 		transducer.register(this,TChannel.CONTROL_PANEL);
 
 		parent = fPanel;
 
 	}
 
 	/**
 	 * Sets the parent
 	 */
 	public void setParent(FactoryPanel fp)
 	{
 		parent = fp;
 	}
 
 	/**
 	 * Returns the parent FactoryPanel
 	 * @return the parent FactoryPanel
 	 */
 	public FactoryPanel getGuiParent()
 	{
 		return parent;
 	}
 
 	/**
 	 * Listens to events fired on the transducer, especially from Agents
 	 */
 	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args)
 	{
 		if(event == TEvent.STOP){
 			parent.getGuiParent().getTimer().stop();
 			System.out.println("you fired me! Control_Panel_STOP");
 		}
 		if(event == TEvent.START){
 			parent.getGuiParent().getTimer().start();
 			System.out.println("you fired me! Control_Panel_START");
 		}
 	}
 
 	/**
 	 * Sets the transducer
 	 * @param newTransducer
 	 *        the new transducer to link
 	 */
 	public void setTransducer(Transducer newTransducer)
 	{
 		// TODO set the transducer, then register with all the necessary channels
 	}
 
 	/**
 	 * Returns the transducer
 	 * @return the transducer
 	 */
 	public Transducer getTransducer()
 	{
 		return transducer;
 	}
 
 	/**
 	 * Returns the State Panel
 	 * @return the State Panel
 	 */
 	public StatePanel getStatePanel()
 	{
 		return statePanel;
 	}
 
 	/**
 	 * Returns the glass info panel, for ease of printing
 	 * @return the glass info panel
 	 */
 	public GlassInfoPanel getGlassInfoPanel()
 	{
 		return glassInfoPanel;
 	}
 
 	/**
 	 * Returns the glass select panel
 	 * @return the glass select panel
 	 */
 	public GlassSelectPanel getGlassSelectPanel()
 	{
 		return glassSelectPanel;
 	}
 
 	/**
 	 * Returns the config select panel
 	 * @return the config select panel
 	 */
 	public ConfigSelectPanel getConfigSelectPanel()
 	{
 		return configSelectPanel;
 	}
 	
 	/**
 	 * Returns the non-norm panel
 	 * @return the NonNormPanel
 	 */
	public NonNormPanel getNonNormPanel()
 	{
 		return nonNormPanel;
 	}
 
 	/**
 	 * Returns an instance of trace panel
 	 * @return trace panel
 	 */
 	public TracePanel getTracePanel()
 	{
 		return tracePanel;
 	}
 
 	/**
 	 * Returns the name of the panel
 	 */
 	public String toString()
 	{
 		return "Control Panel";
 	}
 
 	//added by Minh
 	public void setBinAgent(){
 	//glassSelectPanel.setBinAgent(parent.getBinAgent());
 		//alex configSelectPanel will send the message to bin
 		configSelectPanel.setBinAgent(parent.getBinAgent())	;
 	System.out.println("set BinAgent");
 	}
 }
