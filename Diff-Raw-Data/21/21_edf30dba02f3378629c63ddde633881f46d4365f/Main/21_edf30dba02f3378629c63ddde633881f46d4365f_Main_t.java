 package frontend.swing;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 
 public class Main {
 	
 	
 	/**
 	 * Sets up the design of the components to the look we want to achieve
 	 */
 	public static void setUpDesign() {
 		UIManager.put("SplitPane.dividerSize", CurrentConstants.SPLITPANE_WIDTH);
 		UIManager.put("SplitPane.background", CurrentConstants.SPLITPANE_BG);
 		UIManager.put("TabbedPane.font", CurrentConstants.TAB_FONT);  		
 		UIManager.put("TabbedPane.focus", CurrentConstants.TAB_FOCUS); //removes the ugly border around the header text when in focus
 		UIManager.put("TabbedPane.selected", CurrentConstants.TAB_SELECTED);  
 		UIManager.put("TabbedPane.selectHighlight", CurrentConstants.TAB_SELECTED_HIGHLIGHT); 
 		UIManager.put("TabbedPane.selectedForeground", CurrentConstants.TAB_SELECTED_FG);  
 		UIManager.put("TabbedPane.contentBorderInsets", CurrentConstants.TAB_CONTENT_BORDER_INSETS); //gets rid of the ugly light blue line in the content pane
 		UIManager.put("TabbedPane.contentAreaColor", CurrentConstants.TAB_CONTENT_AREA); //gets rid of the ugly light blue line in the content pane
 		UIManager.put("TabbedPane.darkShadow", CurrentConstants.TAB_DARK_SHADOW); 
 		UIManager.put("TabbedPane.foreground", CurrentConstants.TAB_FG);  
 		UIManager.put("TabbedPane.background", CurrentConstants.TAB_BG);  
 		UIManager.put("TabbedPane.light", CurrentConstants.TAB_LIGHT);  
 		UIManager.put("TabbedPane.tabsOverlapBorder", CurrentConstants.TAB_TABS_OVERLAP_BORDER);
 		
 		UIManager.put("TabbedPane.tabInsets", CurrentConstants.TAB_INSETS);  //sets padding within the tab header
 		UIManager.put("TabbedPane.tabAreaInsets", CurrentConstants.TAB_AREA_INSETS);  //sets the margin of the block of tab headers
 	
	
 	}
 	
 	private static boolean _splash = false;
 	
 	public static void main(String[] args) {
 		
 		setUpDesign();
 		final InitFrameThread initFrameThread = new InitFrameThread();
 		if(_splash){
 			SwingUtilities.invokeLater(initFrameThread);
 			
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
 		}
 
 		SwingUtilities.invokeLater(new Runnable() { //as per best practice for concurrency in swing - see http://docs.oracle.com/javase/tutorial/uiswing/concurrency/
 			@Override
 			public void run() {
 				final AppFrame appFrame = new AppFrame();
 				appFrame.setVisible(true);
 				
 				if(_splash){
 					initFrameThread.kill();
 				}
 			}
 		});
 		
 		
 	
 	}
 	
 	
 
 }
