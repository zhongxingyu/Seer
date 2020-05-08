 import java.applet.*;
 import java.awt.*;
 import java.awt.event.*;
 import gort.applet.*;
 
 /********************************************************************************
 ** The applet derived class used as the entry point for the applet.
 */
 
 public class JLogo extends WebApp
 	implements ActionListener
 {
 	/********************************************************************************
 	** Template method called by the VM to initialise the applet.
 	*/
 
 	public void init()
 	{
 		// Initialise the applets panel.
 		setLayout(new BorderLayout());
 		setBackground(SystemColor.control);
 
 		// Layout the left side top inner panel.
 		m_pnlLeftTop.add(BorderLayout.NORTH,  m_pnlGfxCmds);
 		m_pnlLeftTop.add(BorderLayout.SOUTH,  m_pnlFlowCmds);
 
 		// Layout the left side panel.
 		m_pnlLeft.add(BorderLayout.NORTH,  m_pnlLeftTop);
 		m_pnlLeft.add(BorderLayout.CENTER, m_pnlVars);
 
 		// Layout the right side panel.
 		m_pnlRight.add(BorderLayout.CENTER, m_oDisplay);
 		m_pnlRight.add(BorderLayout.SOUTH,  m_pnlBottom);
 
 		// Layout the bottom panel.
		m_pnlBottom.add(m_pnlProgram);
		m_pnlBottom.add(m_pnlTurtle);
		m_pnlBottom.add(m_pnlDisplay);
 		
 		// Layout the applet panel.
 		add(BorderLayout.NORTH,  m_oTitleBar);
 		add(BorderLayout.WEST,   m_pnlLeft);
 		add(BorderLayout.CENTER, m_pnlRight);
 
 		// Add event handlers.
 		m_oProgram.addActionListener(this);
 		m_oDisplay.addComponentListener(new ResizeListener());
 	}
 
 	/********************************************************************************
 	** Template method called by the VM to start the applet running.
 	*/
 
 	public void start()
 	{
 	}
 
 	/********************************************************************************
 	** Template method called by the VM to stop the applet running.
 	*/
 
 	public void stop()
 	{
 	}
 
 	/********************************************************************************
 	** Template method called by the VM to terminate the applet.
 	*/
 
 	public void destroy()
 	{
 	}
 
 	/********************************************************************************
 	** Action event handler.
 	*/
 
 	public void actionPerformed(ActionEvent eEvent)
 	{
 		if (eEvent.getSource() == m_oProgram)
 		{
 			String strEvent = eEvent.getActionCommand();
 
 			if (strEvent.equals(Program.EDITED))
 				onProgramEdited();
 
 			if (strEvent.equals(Program.STARTED))
 				onProgramStarted();
 
 			if (strEvent.equals(Program.STOPPED))
 				onProgramStopped();
 
 			if (strEvent.equals(Program.CLEARED))
 				onProgramCleared();
 		}
 	}
 
 	/********************************************************************************
 	** Program event handlers.
 	*/
 
 	public void onProgramStarted()
 	{
 		setEnabled(false);
 		m_oTitleBar.setStatus("Running");
 	}
 
 	public void onProgramStopped()
 	{
 		setEnabled(true);
 		m_oTitleBar.setStatus("");
 	}
 
 	public void onProgramCleared()
 	{
 		m_oEditCtx.clear();
 	}
 
 	public void onProgramEdited()
 	{
 		m_oEditCtx.clear();
 
 		m_oProgram.execute(this, m_oEditCtx);
 	}
 
 	/********************************************************************************
 	** Adapter to handle component events.
 	*/
 
 	public class ResizeListener extends ComponentAdapter
 	{
 		public void componentResized(ComponentEvent eEvent)
 		{
 			m_oTurtle.reset(true);
 		}
 	}
 
 	/********************************************************************************
 	** Constants
 	*/
 
 	/********************************************************************************
 	** Members.
 	*/
 
 	// Data members.
 	private Program		m_oProgram = new Program();
 	private Display		m_oDisplay = new Display();
 	private Turtle		m_oTurtle  = new Turtle(m_oDisplay);
 	private ExecContext m_oEditCtx = new ExecContext(m_oTurtle);
 
 	// GUI panels.
 	private TitleBar			m_oTitleBar   = new TitleBar();
 	private GfxCmdsPanel		m_pnlGfxCmds  = new GfxCmdsPanel(m_oTurtle, m_oProgram, m_oEditCtx);
 	private FlowCmdsPanel		m_pnlFlowCmds = new FlowCmdsPanel(m_oProgram, m_oEditCtx);
 	private VarsPanel			m_pnlVars     = new VarsPanel(m_oTurtle, m_oProgram, m_oEditCtx);
 	private ProgramPanel		m_pnlProgram  = new ProgramPanel(m_oTurtle, m_oDisplay, m_oProgram);
 	private TurtleStatusPanel	m_pnlTurtle   = new TurtleStatusPanel(m_oTurtle);
 	private DisplayStatusPanel	m_pnlDisplay  = new DisplayStatusPanel(m_oDisplay);
 
 	// Layout panels.
 	private Panel		m_pnlLeftTop = new Panel(new BorderLayout());
 	private Panel		m_pnlLeft    = new Panel(new BorderLayout());
 	private Panel		m_pnlRight   = new Panel(new BorderLayout());
	private Panel		m_pnlBottom  = new Panel(new FlowLayout(FlowLayout.CENTER));
 }
