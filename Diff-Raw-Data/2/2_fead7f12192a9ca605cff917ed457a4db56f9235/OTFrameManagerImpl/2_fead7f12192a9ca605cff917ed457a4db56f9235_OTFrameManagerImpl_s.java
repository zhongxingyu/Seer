 /**
  * 
  */
 package org.concord.otrunk.view;
 
 import java.awt.BorderLayout;
 import java.util.Hashtable;
 
 import javax.swing.JFrame;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.OTFrame;
 import org.concord.framework.otrunk.view.OTFrameManager;
 import org.concord.framework.otrunk.view.OTViewContext;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewFactory;
 
 public class OTFrameManagerImpl implements OTFrameManager 
 {
 	Hashtable frameContainers = new Hashtable();
 	OTViewFactory viewFactory;
 	private JFrame jFrame;
 	private int oldX = 0;
 	private int oldY = 0;
 	
 	public class FrameContainer
 	{
 		OTViewContainerPanel container;
 		JFrame frame;
 	}
 	
 	public void setViewFactory(OTViewFactory viewFactory)
 	{
 		this.viewFactory = viewFactory;
 	}
 	
 	public void putObjectInFrame(OTObject otObject, 
 			OTFrame otFrame) 
 	{
 		putObjectInFrame(otObject, null, otFrame, null);
 	}
 	
 	public void putObjectInFrame(OTObject otObject, 
 		OTFrame otFrame, int positionX, int positionY) 
 	{
 		putObjectInFrame(otObject, null, otFrame, null, positionX, positionY);
 	}
 	
 	public void putObjectInFrame(OTObject otObject, 
 			org.concord.framework.otrunk.view.OTViewEntry viewEntry,
 			OTFrame otFrame) 
 	{
 		putObjectInFrame(otObject, viewEntry, otFrame, null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.view.OTFrameManager#putObjectInFrame(org.concord.framework.otrunk.OTObject, org.concord.framework.otrunk.view.OTViewEntry, org.concord.framework.otrunk.view.OTFrame, java.lang.String)
 	 */
 	public void putObjectInFrame(OTObject otObject, OTViewEntry viewEntry, OTFrame otFrame, String viewMode) 
 	{
 		putObjectInFrame(otObject, viewEntry, otFrame, viewMode, 30, 30);
 	}
 	
 	public void putObjectInFrame(OTObject otObject, OTViewEntry viewEntry, OTFrame otFrame, String viewMode, 
 		int positionX, int positionY) 
 	{
 		// look up view container with the frame.
 		FrameContainer frameContainer = 
 			(FrameContainer)frameContainers.get(otFrame.getGlobalId());
 		
 		if(frameContainer == null || oldX != positionX || oldY != positionY) {
 			jFrame = new JFrame(otFrame.getTitle());
 	
 			frameContainer = new FrameContainer();
 			frameContainer.frame = jFrame;
 			OTViewContainerPanel otContainer = new OTViewContainerPanel(this);
 			otContainer.setTopLevelContainer(true);
 			otContainer.setUseScrollPane(otFrame.getUseScrollPane());
 			
 			frameContainer.container = otContainer;	
 			OTViewContext factoryContext = viewFactory.getViewContext();
 			frameContainer.container.setOTViewFactory(factoryContext.createChildViewFactory());
 
 			jFrame.getContentPane().setLayout(new BorderLayout());
 	
 			jFrame.getContentPane().add(otContainer, BorderLayout.CENTER);
 			
 			jFrame.setBounds(positionX, positionY, otFrame.getWidth(), otFrame.getHeight());
 			
 			oldX = positionX;
 			oldY = positionY;
 			
 			if (otFrame.getBorderlessPopup()){
 				jFrame.setUndecorated(true);
 			}
 			if (otFrame.isResourceSet("alwaysOnTop")){
 				jFrame.setAlwaysOnTop(otFrame.getAlwaysOnTop());
 			}
 			
 			frameContainers.put(otFrame.getGlobalId(), frameContainer);
 		}
 		
 		// call setCurrentObject on that view container with a null
 		// frame
 		frameContainer.container.setViewMode(viewMode);
 		frameContainer.container.setCurrentObject(otObject, viewEntry);
 		frameContainer.frame.setVisible(true);
 	}
 	
	public void distroyFrame(){
 		jFrame.dispose();
 	}
 }
