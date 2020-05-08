 package edu.umd.cs.guitar.replayer;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.netbeans.jemmy.QueueTool;
 
 import edu.umd.cs.guitar.event.GEvent;
 import edu.umd.cs.guitar.exception.ApplicationConnectException;
 import edu.umd.cs.guitar.model.GUITARConstants;
 import edu.umd.cs.guitar.model.GWindow;
 import edu.umd.cs.guitar.model.IphApplication;
 import edu.umd.cs.guitar.model.IphConstants;
 import edu.umd.cs.guitar.model.data.AttributesType;
 import edu.umd.cs.guitar.model.data.ComponentType;
 import edu.umd.cs.guitar.model.data.PropertyType;
 import edu.umd.cs.guitar.util.GUITARLog;
 
 public class IphReplayerMonitor extends GReplayerMonitor{
 
 	IphReplayerConfiguration configuration;
 
 	List<String> sIgnoreWindowList = new ArrayList<String>();
 	List<String> sRootWindows = new ArrayList<String>();
 	
 	//application;
 	
 	/**
 	 * Delay for widget searching loop
 	 */
 	private static final int DELAY_STEP = 50;
 	
 	/**
 	 * Constructor
 	 * 
 	 * <p>
 	 * 
 	 * @param configuration
 	 *            replayer configuration
 	 */
 	public IphReplayerMonitor(IphReplayerConfiguration configuration) {
 		super();
 		// this.logger = logger;
 		this.configuration = configuration;
 	}
 	
 	
 	@Override
 	public void connectToApplication() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setUp() {
 		// Set up parameters
 		sIgnoreWindowList = IphConstants.sIgnoredWins;
 
 		// Start the application		
 		try {
 			this.application = new IphApplication();
 			
 			// Parsing arguments
 			String[] args;
 			if (IphReplayerConfiguration.ARGUMENT_LIST != null)
 				args = IphReplayerConfiguration.ARGUMENT_LIST
 						.split(GUITARConstants.CMD_ARGUMENT_SEPARATOR);
 			else
 				args = new String[0];
 			
 			GUITARLog.log.debug("Requesting server host:" + this.configuration.SERVER_HOST);
 			GUITARLog.log.debug("Requesting server port:" + this.configuration.PORT);
 			
 			// set up the client
 			//new CommClient();
 			application.connect(args);
 
 			// Delay
 			try {
 				GUITARLog.log
 						.info("Initial waiting: "
 								+ IphReplayerConfiguration.INITIAL_WAITING_TIME
 								+ "ms...");
 				Thread.sleep(IphReplayerConfiguration.INITIAL_WAITING_TIME);
 			} catch (InterruptedException e) {
 				GUITARLog.log.error(e);
 			}
 
 		}  catch (ApplicationConnectException e) {
 			GUITARLog.log.error(e);
 		}
 	}
 
 	@Override
 	public void cleanUp() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public GWindow getWindow(String sWindowTitle) {
 		System.out.println("Trying to find GWindow: " +sWindowTitle);
 		List<GWindow> retWindowList = new ArrayList<GWindow>();
 
 		retWindowList.clear();
 		if (application != null) {
 			if (application instanceof IphApplication) {
 				for (GWindow window : ((IphApplication) application).getAllWindow()) {
 					System.out.println("Checking window : " + window.getTitle() + 
 							" (isRoot : " + window.isRoot() + " | isValid : " + window.isValid() + ")");
 					if (window.isRoot() && window.isValid()) {
 						retWindowList.add(window);
 					}
 				}
 				/*if (iphApplication.allWindows == null) {
 					iphApplication.allWindows = iphApplication.getAllWindow();
 					application = iphApplication;
 				}
 				for (GWindow window : iphApplication.allWindows) {
 					System.out.println("Checking window : " + window.getTitle() + 
 							" (isRoot : " + window.isRoot() + " | isValid : " + window.isValid() + ")");
 					if (window.isRoot() && window.isValid()) {
 						retWindowList.add(window);
 					}
 				}*/
 			}
 		}
 		
 		// / Debugs:
 		GUITARLog.log.debug("Root window size: " + retWindowList.size());
 		for (GWindow window : retWindowList) {
 			GUITARLog.log.debug("Window title: " + window.getTitle());
 			if (sWindowTitle.equals(window.getTitle()))
 				return window;
 		}
 
 		try {
 			Thread.sleep(50);
 		} catch (InterruptedException e) {
 			GUITARLog.log.error(e);
 		}
 		return null;
 	}
 
 	@Override
 	public GEvent getAction(String sActionName) {
 		GEvent retAction = null;
 		try {
 			Class<?> c = Class.forName(sActionName);
 			Object action = c.newInstance();
 
 			retAction = (GEvent) action;
 
 		} catch (Exception e) {
 			GUITARLog.log.error("Error in getting action", e);
 		}
 
 		return retAction;
 	}
 
 	@Override
 	public Object getArguments(String action) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<PropertyType> selectIDProperties(ComponentType comp) {
 		if (comp == null)
 			return new ArrayList<PropertyType>();
 
 		List<PropertyType> retIDProperties = new ArrayList<PropertyType>();
 
 		AttributesType attributes = comp.getAttributes();
 		List<PropertyType> lProperties = attributes.getProperty();
 		for (PropertyType p : lProperties) {
 			if (IphConstants.ID_PROPERTIES.contains(p.getName()))
 				retIDProperties.add(p);
 		}
 		return retIDProperties;
 	}
 
 	@Override
 	public void delay(int delay) {
 		// new QueueTool().waitEmpty(delay);
 	}
 }
