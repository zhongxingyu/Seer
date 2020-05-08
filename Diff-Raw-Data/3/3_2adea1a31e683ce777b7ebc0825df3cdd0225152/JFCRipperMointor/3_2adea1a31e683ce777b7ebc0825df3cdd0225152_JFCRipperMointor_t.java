 /*	
  *  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
  *  be obtained by sending an e-mail to atif@cs.umd.edu
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
  *  documentation files (the "Software"), to deal in the Software without restriction, including without 
  *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *	the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
  *	conditions:
  * 
  *	The above copyright notice and this permission notice shall be included in all copies or substantial 
  *	portions of the Software.
  *
  *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  *	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
  *	EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
  *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
  *	THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 package edu.umd.cs.guitar.ripper;
 
 import java.awt.AWTEvent;
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.AWTEventListener;
 import java.awt.event.WindowEvent;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.accessibility.Accessible;
 import javax.accessibility.AccessibleAction;
 import javax.accessibility.AccessibleContext;
 import javax.accessibility.AccessibleSelection;
 import javax.accessibility.AccessibleText;
 
 import org.apache.log4j.Logger;
 import org.netbeans.jemmy.EventTool;
 import org.netbeans.jemmy.QueueTool;
 
 import edu.umd.cs.guitar.event.GEvent;
 import edu.umd.cs.guitar.event.GThreadEvent;
 import edu.umd.cs.guitar.event.JFCActionHandler;
 import edu.umd.cs.guitar.event.JFCActionEDT;
 import edu.umd.cs.guitar.exception.ApplicationConnectException;
 import edu.umd.cs.guitar.model.GApplication;
 import edu.umd.cs.guitar.model.GComponent;
 import edu.umd.cs.guitar.model.GUITARConstants;
 import edu.umd.cs.guitar.model.GWindow;
 import edu.umd.cs.guitar.model.JFCApplication;
 import edu.umd.cs.guitar.model.JFCConstants;
 import edu.umd.cs.guitar.model.JFCXComponent;
 import edu.umd.cs.guitar.model.JFCXWindow;
 import edu.umd.cs.guitar.util.GUITARLog;
 
 /**
  * 
  * Monitor for the ripper to handle Java Swing specific features
  * 
  * @see GRipperMonitor
  * 
  * @author <a href="mailto:baonn@cs.umd.edu"> Bao Nguyen </a>
  */
 public class JFCRipperMointor extends GRipperMonitor {
 
 	// --------------------------
 	// Configuartion Parameters
 	// --------------------------
 
 	/**
      * 
      */
 	private static final int INITIAL_DELAY = 1000;
 
 	// Logger logger;
 	JFCRipperConfiguration configuration;
 
 	List<String> sIgnoreWindowList = new ArrayList<String>();
 
 	/**
 	 * Constructor
 	 * 
 	 * @param logger
 	 *            logger to keep track of output
 	 * @param sMainClass
 	 *            full name of the main class
 	 * 
 	 */
 	@Deprecated
 	public JFCRipperMointor(String sMainClass, Logger logger) {
 		super();
 		// this.logger = logger;
 		JFCRipperConfiguration.MAIN_CLASS = sMainClass;
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * <p>
 	 * 
 	 * @param configuration
 	 *            ripper configuration
 	 */
 	public JFCRipperMointor(JFCRipperConfiguration configuration) {
 		super();
 		// this.logger = logger;
 		this.configuration = configuration;
 	}
 
 	/**
 	 * @param main_class
 	 */
 	@Deprecated
 	public JFCRipperMointor(String main_class) {
 		super();
 		JFCRipperConfiguration.MAIN_CLASS = main_class;
 		// this.logger = Logger.getLogger(this.getClass().getSimpleName());
 	}
 
 	List<String> sRootWindows = new ArrayList<String>();
 
 	/**
 	 * Temporary list of windows opened during the expand event is being
 	 * performed. Those windows are in a native form to prevent data loss.
 	 * 
 	 */
 	volatile LinkedList<Window> tempOpenedWinStack = new LinkedList<Window>();
 
 	volatile LinkedList<Window> tempClosedWinStack = new LinkedList<Window>();
 
 	// volatile LinkedList<GWindow> tempGWinStack = new LinkedList<GWindow>();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.ripper.RipperMonitor#cleanUp()
 	 */
 	@Override
 	public void cleanUp() {
 		// Debugger.pause("Clean up pause....");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.umd.cs.guitar.ripper.RipperMonitor#closeWindow(edu.umd.cs.guitar.
 	 * model.GXWindow)
 	 */
 	@Override
 	public void closeWindow(GWindow gWindow) {
 
 		JFCXWindow jWindow = (JFCXWindow) gWindow;
 		Window window = jWindow.getWindow();
 
 		// TODO: A bug might happen here, will fix later
 		// window.setVisible(false);
 		window.dispose();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.umd.cs.guitar.ripper.RipperMonitor#expand(edu.umd.cs.guitar.model
 	 * .GXComponent)
 	 */
 	@Override
 	public void expandGUI(GComponent component) {
 
 		// JFCXComponent jComponent = (JFCXComponent) component;
 		// Accessible aComponent = jComponent.getAComponent();
 		// GComponent gComponent = new JFCXComponent(aComponent);
 
 		if (component == null)
 			return;
 
 		GUITARLog.log.info("Expanding *" + component.getTitle() + "*...");
 
 //		GThreadEvent action = new JFCActionHandler();
 		 GEvent action = new JFCActionEDT();
 
 		action.perform(component);
 		GUITARLog.log.info("Waiting  " + configuration.DELAY
 				+ "ms for a new window to open");
 		
 		//new QueueTool().waitEmpty(configuration.DELAY);
 		new EventTool().waitNoEvent(configuration.DELAY);
 
 //		 try {
 //			 Thread.sleep(configuration.DELAY);
 //		 } catch (InterruptedException e) {
 //		 }
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.ripper.RipperMonitor#getOpenedWindowCache()
 	 */
 	@Override
 	public LinkedList<GWindow> getOpenedWindowCache() {
 
 		LinkedList<GWindow> retWindows = new LinkedList<GWindow>();
 
 		for (Window window : tempOpenedWinStack) {
 			GWindow gWindow = new JFCXWindow(window);
 			if (gWindow.isValid())
 				retWindows.addLast(gWindow);
 		}
 		return retWindows;
 	}
 
 	@Override
 	public LinkedList<GWindow> getClosedWindowCache() {
 
 		LinkedList<GWindow> retWindows = new LinkedList<GWindow>();
 
 		for (Window window : tempClosedWinStack) {
 			GWindow gWindow = new JFCXWindow(window);
 			if (gWindow.isValid())
 				retWindows.addLast(gWindow);
 		}
 		return retWindows;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.ripper.RipperMonitor#getRootWindows()
 	 */
 	@Override
 	public List<GWindow> getRootWindows() {
 
 		List<GWindow> retWindowList = new ArrayList<GWindow>();
 
 		retWindowList.clear();
 
 		Frame[] lFrames = Frame.getFrames();
 
 		for (Frame frame : lFrames) {
 
 			if (!isValidRootWindow(frame))
 				continue;
 
 			AccessibleContext xContext = frame.getAccessibleContext();
 			String sWindowName = xContext.getAccessibleName();
 
 			if (sRootWindows.size() == 0
 					|| (sRootWindows.contains(sWindowName))) {
 
 				GWindow gWindow = new JFCXWindow(frame);
 				retWindowList.add(gWindow);
 				// frame.requestFocus();
 			}
 		}
 
 		// / Debugs:
 		GUITARLog.log.debug("Root window size: " + retWindowList.size());
 		for (GWindow window : retWindowList) {
 			GUITARLog.log.debug("Window title: " + window.getTitle());
 		}
 
 		// Debugger.pause("Press ENTER to continue");
 		// //
 
 		try {
 			Thread.sleep(50);
 		} catch (InterruptedException e) {
 			GUITARLog.log.error(e);
 		}
 		return retWindowList;
 	}
 
 	/**
 	 * Check if a root window is worth ripping
 	 * 
 	 * <p>
 	 * 
 	 * @param window
 	 *            the window to consider
 	 * @return true/false
 	 */
 	private boolean isValidRootWindow(Frame window) {
 
 		// Check if window is valid
 		// if (!window.isValid())
 		// return false;
 
 		// Check if window is visible
 		if (!window.isVisible())
 			return false;
 
 		// Check if window is on screen
 		// double nHeight = window.getSize().getHeight();
 		// double nWidth = window.getSize().getWidth();
 		// if (nHeight <= 0 || nWidth <= 0)
 		// return false;
 
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.umd.cs.guitar.ripper.RipperMonitor#isExpandable(edu.umd.cs.guitar
 	 * .model.GXComponent, edu.umd.cs.guitar.model.GXWindow)
 	 */
 	@Override
 	boolean isExpandable(GComponent gComponent, GWindow window) {
 
 		JFCXComponent jComponent = (JFCXComponent) gComponent;
 //		Accessible aComponent = jComponent.getAComponent();
 //
 //		if (aComponent == null)
 //			return false;
 		
 		Component component = jComponent.getComponent();
 		AccessibleContext aContext = component .getAccessibleContext();
 		
 
 		String ID = gComponent.getTitle();
 		if (ID == null)
 			return false;
 
 		if ("".equals(ID))
 			return false;
 
 		if (!gComponent.isEnable()) {
 			GUITARLog.log.debug("Component is disabled");
 			return false;
 		}
 
 		if (!isClickable(component)) {
 			return false;
 		}
 
 		if (gComponent.getTypeVal().equals(GUITARConstants.TERMINAL))
 			return false;
 
 		// // Check for more details
 		// AccessibleContext aContext = component.getAccessibleContext();
 
 		if (aContext == null)
 			return false;
 
 		AccessibleText aText = aContext.getAccessibleText();
 
 		if (aText != null)
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Check if a component is click-able.
 	 * 
 	 * @param component
 	 * @return true/false
 	 */
 	private boolean isClickable(Component component) {
 
 		AccessibleContext aContext = component.getAccessibleContext();
 
 		if (aContext == null)
 			return false;
 
 		AccessibleAction action = aContext.getAccessibleAction();
 
 		if (action == null)
 			return false;
 
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.umd.cs.guitar.ripper.RipperMonitor#isIgnoredWindow(edu.umd.cs.guitar
 	 * .model.GXWindow)
 	 */
 	@Override
 	public boolean isIgnoredWindow(GWindow window) {
 		String sWindow = window.getTitle();
 		// TODO: Ignore template
 		return (this.sIgnoreWindowList.contains(sWindow));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.ripper.RipperMonitor#isNewWindowOpened()
 	 */
 	@Override
 	public boolean isNewWindowOpened() {
 		return (tempOpenedWinStack.size() > 0);
 		// return (tempGWinStack.size() > 0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.ripper.RipperMonitor#resetWindowCache()
 	 */
 	@Override
 	public void resetWindowCache() {
 		this.tempOpenedWinStack.clear();
 		this.tempClosedWinStack.clear();
 	}
 
 	public class WindowOpenListener implements AWTEventListener {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * java.awt.event.AWTEventListener#eventDispatched(java.awt.AWTEvent)
 		 */
 		@Override
 		public void eventDispatched(AWTEvent event) {
 
 			switch (event.getID()) {
 			case WindowEvent.WINDOW_OPENED:
 				processWindowOpened((WindowEvent) event);
 				break;
 			case WindowEvent.WINDOW_ACTIVATED:
 			case WindowEvent.WINDOW_DEACTIVATED:
 			case WindowEvent.WINDOW_CLOSING:
 				processWindowClosed((WindowEvent) event);
 				break;
 
 			default:
 				break;
 			}
 
 		}
 
 		/**
 		 * @param event
 		 */
 		private void processWindowClosed(WindowEvent wEvent) {
 			Window window = wEvent.getWindow();
 			tempClosedWinStack.add(window);
 		}
 
 		/**
 		 * @param wEvent
 		 */
 		private void processWindowOpened(WindowEvent wEvent) {
 			Window window = wEvent.getWindow();
 			tempOpenedWinStack.add(window);
 		}
 	}
 
 	Toolkit toolkit;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.ripper.RipperMonitor#setUp()
 	 */
 	@Override
 	public void setUp() {
 		// Set up parameters
 		sIgnoreWindowList = JFCConstants.sIgnoredWins;
 
 		// Start the application
 		GApplication application;
 		try {
 			String[] URLs;
 			if (JFCRipperConfiguration.URL_LIST != null)
 				URLs = JFCRipperConfiguration.URL_LIST
 						.split(GUITARConstants.CMD_ARGUMENT_SEPARATOR);
 			else
 				URLs = new String[0];
 
 			application = new JFCApplication(JFCRipperConfiguration.MAIN_CLASS,
 					URLs);
 
 			String[] args;
 			if (JFCRipperConfiguration.ARGUMENT_LIST != null)
 				args = JFCRipperConfiguration.ARGUMENT_LIST
 						.split(GUITARConstants.CMD_ARGUMENT_SEPARATOR);
 			else
 				args = new String[0];
 
 			application.connect(args);
 
 			// Delay
 			try {
 				GUITARLog.log
 						.info("Initial waiting: "
 								+ JFCRipperConfiguration.INITIAL_WAITING_TIME
 								+ "ms...");
 				Thread.sleep(JFCRipperConfiguration.INITIAL_WAITING_TIME);
 			} catch (InterruptedException e) {
 				GUITARLog.log.error(e);
 			}
 
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			GUITARLog.log.error(e);
 		} catch (ApplicationConnectException e) {
 			// TODO Auto-generated catch block
 			GUITARLog.log.error(e);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			GUITARLog.log.error(e);
 		}
 
 		// -----------------------------
 		// Assign listener
 		toolkit = java.awt.Toolkit.getDefaultToolkit();
 
 		WindowOpenListener listener = new WindowOpenListener();
 		toolkit.addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
 
 	}
 
 	/**
 	 * 
 	 * Add a root window to be ripped
 	 * 
 	 * <p>
 	 * 
 	 * @param sWindowName
 	 *            the window name
 	 */
 	public void addRootWindow(String sWindowName) {
 		this.sRootWindows.add(sWindowName);
 	}
 
 	boolean flagWindowClosed;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.ripper.GRipperMonitor#isWindowClose()
 	 */
 	@Override
 	public boolean isWindowClosed() {
 		return (tempClosedWinStack.size() > 0);
 	}
 
 }
