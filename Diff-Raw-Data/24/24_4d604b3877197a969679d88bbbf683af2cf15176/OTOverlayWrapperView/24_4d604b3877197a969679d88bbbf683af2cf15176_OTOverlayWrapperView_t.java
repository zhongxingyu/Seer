 package org.concord.otrunk.overlay;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.otrunk.OTObjectServiceImpl;
 import org.concord.otrunk.OTrunkUtil;
 import org.concord.otrunk.datamodel.OTTransientMapID;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.view.AbstractOTJComponentContainerView;
 import org.concord.otrunk.view.OTGroupListManager;
 
 public class OTOverlayWrapperView extends AbstractOTJComponentContainerView
 {
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 	private OTOverlay overlay;
 	private OTObject wrappedObject;
 	private JComponent subview;
 	private OTOverlayWrapper wrapper;
 	private OTGroupListManager groupListManager;
 	private OTUserOverlayManager overlayManager;
 	private JButton submitButton;
 	private GridBagConstraints noStretchConstraints;
 	private GridBagConstraints stretchConstraints;
 	private OTObject resultsObject;
 	private JPanel mainPanel;
 	private JLabel submittedLabel;
 	private static DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance();
 	private JFrame resultsFrame;
 	
 	public JComponent getComponent(OTObject otObject)
 	{
 		// FIXME This should be changed so that the user data is stored in the user session layer,
 		// and only when the overlay data is saved remotely should it get copied into the overlay
 		wrapper = (OTOverlayWrapper) otObject;
 		overlay = wrapper.getOverlay();
 		groupListManager = wrapper.getOTObjectService().getOTrunkService(OTGroupListManager.class);
 		overlayManager = wrapper.getOTObjectService().getOTrunkService(OTUserOverlayManager.class);
 		wrappedObject = wrapper.getWrappedObject();
 		
 		/* String msg = "\nwrapped object is: " + wrappedObject;
 		msg += "\noverlay object is: " + overlay;
 		msg += "\ngroup list manager is: " + groupListManager;
 		msg += "\noverlay manager is: " + overlayManager;
 		logger.info(msg);
 		*/
 		if (overlay == null && groupListManager != null && overlayManager != null) {
 			// logger.info("Using group list manager/overlay manager");
 			OTUserObject currentGroupMember = groupListManager.getCurrentGroupMember().getUserObject();
 			overlay = overlayManager.getOverlay(currentGroupMember);
 		}
 
 		subview = createSubViewComponent(wrappedObject);
 		
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new GridBagLayout());
 		
 		noStretchConstraints = new GridBagConstraints();
 		noStretchConstraints.anchor = GridBagConstraints.NORTHWEST;
 		noStretchConstraints.weightx = 0;
 		noStretchConstraints.weighty = 0;
 		noStretchConstraints.fill = GridBagConstraints.NONE;
 		noStretchConstraints.gridwidth = GridBagConstraints.REMAINDER;
 		noStretchConstraints.ipady = 5;
 		
 		stretchConstraints = new GridBagConstraints();
 		stretchConstraints.anchor = GridBagConstraints.NORTHWEST;
 		stretchConstraints.weightx = 1;
 		stretchConstraints.weighty = 0;
 		stretchConstraints.fill = GridBagConstraints.HORIZONTAL;
 		stretchConstraints.gridwidth = GridBagConstraints.REMAINDER;
 		stretchConstraints.ipady = 5;
 		
 		mainPanel.add(subview, stretchConstraints);
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		
 		if (wrapper.getShowButton()) {
 			submitButton = new JButton(wrapper.getButtonText());
 			buttonPanel.add(submitButton);
 			
 			submitButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e)
                 {
 	                saveData();
                 }
 			});
 		}
 		
 		resultsObject = wrapper.getResultsObject();
 		if (resultsObject != null) {
     		JButton resultsButton = new JButton("Results");
     		buttonPanel.add(resultsButton);
     		
     		resultsButton.addActionListener(new ActionListener() {
     			public void actionPerformed(ActionEvent e)
                 {
                     popUpResults();
                 }
     		});
 		}
 		
 		submittedLabel = new JLabel("");
 		buttonPanel.add(submittedLabel);
 		
 		mainPanel.add(buttonPanel, stretchConstraints);
 		
		// FIXME This is a hack because certain wrapped objects aren't resizing themselves well
		// (OTQuestions when the OTChoice is embedding the view for the choice, for example)
		// hopefully a chain of 10 should be enough to get around all the repainting that happens when this loads...
		invokeLater(10);
		
 		return mainPanel;
 	}
 	
	private void invokeLater(final int depth) {
		EventQueue.invokeLater(new Runnable() {
            public void run()
            {
            	if (depth != 0) {
            		logger.finer("Event queue ran, but at wrong depth: " + depth);
            		invokeLater(depth-1);
            	} else {
                	logger.finer("Event queue ran at the right depth.");
    				mainPanel.setSize(new Dimension(mainPanel.getWidth()+1, mainPanel.getHeight()+1));
    				mainPanel.validate();
            	}
            }
        });
	}
	
 	private void popUpResults() {
 		final JComponent resultsSubview = createSubViewComponent(resultsObject);
 		closeResultsFrame();
 		resultsFrame = new JFrame("Results");
 		resultsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		resultsFrame.getContentPane().add(resultsSubview);
 		resultsFrame.pack();
 		resultsFrame.setSize(new Dimension(600,400));
 		resultsFrame.setVisible(true);
 	}
 	
 	private void closeResultsFrame() {
 		if (resultsFrame != null) {
 			resultsFrame.setVisible(false);
 			resultsFrame.dispose();
 			resultsFrame = null;
 		}
 	}
 	
 	@Override
     public void viewClosed() {
 		// logger.info("Closing the wrapped object's views");
 		closeResultsFrame();
 		removeAllSubViews();
 		if (wrapper.getAutoSubmit()) {
 			saveData();
 		}
 		super.viewClosed();
 	}
 	
 	private void saveData() {
 		// save everything
 		if (overlayManager != null && overlay != null) {
 			// logger.info("had an overlay");
     		try {
     			OTID id = wrappedObject.getGlobalId();
     			if (id instanceof OTTransientMapID) {
     				id = ((OTTransientMapID) id).getMappedId();
     			}
     	        OTObject newWrappedObject = overlayManager.getOTObject(overlay, id);
     	        if (! OTrunkUtil.compareObjects(newWrappedObject, wrappedObject, true)) {
     	        	mainPanel.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
     	        	// only save if there are changes
         	        ((OTObjectServiceImpl) wrappedObject.getOTObjectService()).copyInto(wrappedObject, newWrappedObject, -1, true);
         	        
         	        overlayManager.remoteSave(overlay);
         	        submittedLabel.setText("Submitted: " + dateFormatter.format(new Date()));
         	        submittedLabel.revalidate();
     	        }
             } catch (Exception e) {
     	        logger.log(Level.SEVERE, "Couldn't get the object from the overlay!", e);
     	        logger.log(Level.FINER, "stack trace:", e);
             }
             try {
     	        overlayManager.remoteSave(overlay);
             } catch (Exception e) {
             	logger.log(Level.SEVERE, "Couldn't save the user's overlay!", e);
             }
             mainPanel.getRootPane().setCursor(Cursor.getDefaultCursor());
 		}
 	}
 
 }
