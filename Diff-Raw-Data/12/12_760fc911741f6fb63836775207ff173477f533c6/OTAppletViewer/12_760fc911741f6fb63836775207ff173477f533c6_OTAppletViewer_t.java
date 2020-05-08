 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.otrunk.applet;
 
 import java.applet.Applet;
 import java.applet.AppletContext;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Enumeration;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.view.OTViewContainerPanel;
 import org.concord.otrunk.view.OTViewerHelper;
import org.concord.otrunk.xml.XMLDatabase;
 
 public class OTAppletViewer extends JApplet
 {
     private static final long serialVersionUID = 1L;
 
 	protected OTViewerHelper viewerHelper;
 
 	protected boolean masterLoaded = false;
 	protected OTAppletViewer master;
 	protected Action stateAction;
 	protected JButton authorSaveButton;
 	
 	private OTViewContainerPanel otContainer;
 
 	public OTAppletViewer()
 	{
 		super();
 		viewerHelper = new OTViewerHelper();
 	}
 
 	public String getAppletName()
 	{
 		return getParameter("name");
 	}
 
 	@Override
     public void init() 
 	{
 		super.init();
 
 		System.out.println("" + getAppletName() + " started init");
 
 		try{
 			// getCodeBase throws a security exception inside of MW
 			System.out.println("" + getAppletName() + " codebase: " + getCodeBase());		
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}	
 	
 	@Override
     public void start() 
 	{
 		super.start();
 
 		System.out.println("applet.start called on " + getAppletName());
 
 		if(isMaster()){
 			loadState();
 		}
 
 		if(isMasterLoaded()){
 			setupView();
 		}
 		
 		if(isMaster()){			
 			// The browser needs time to finish loading before we can find all the applets on the paqe
 			// so we sleep for a bit before looking for them.
 			// if there was some way to know when they had fully loaded. that should be used instead.			
 			Thread delayer = new Thread(){
 				@Override
                 public void run()
 				{
 					try {
 	                    Thread.sleep(1000);
                     } catch (InterruptedException e) {
 	                    // TODO Auto-generated catch block
 	                    e.printStackTrace();
                     }
 					
                     AppletContext appletContext = getAppletContext();
                     if(appletContext == null){
                     	// this should never be true but in MW there is no applet context
                     	return;
                     }
                     
 					// Go through all the applets on the page that are OTAppletViewerS and
 					// check if we are set to be their master.  If so then notify them
 					// that we have finished loading.
 					Enumeration<Applet> applets = appletContext.getApplets();
 					while(applets.hasMoreElements()){
 						Applet a = applets.nextElement();
 						System.out.println("" + getAppletName() + " found: " + a.getParameter("name"));
 						if(a instanceof OTAppletViewer &&
 								!((OTAppletViewer)a).isMaster() &&
 								((OTAppletViewer)a).getMaster() == OTAppletViewer.this){
 							System.out.println("" + getAppletName() + " calling finishedLoading on " + a.getParameter("name"));
 							((OTAppletViewer)a).masterFinishedLoading(OTAppletViewer.this);
 						}
 					}					
 				}
 			};
 			
 			delayer.start();
 		}
 	}	
 
 	/**
 	 * There is also the stop method, but I believe that might be called if 
 	 */
 	@Override
     public void destroy() 
 	{
 		System.out.println("applet.destroy called on " + getAppletName());
 		
 		if(otContainer != null){
 			otContainer.setCurrentObject(null);
 		}
 
 		super.destroy();
 	}
 
 	@Override
     public void stop()
 	{
 		System.out.println("applet.stop called on " + getAppletName());
 		
 		super.stop();		
 	}
 	
 	protected URL getDatabaseURL()
 	{
 		String urlString = getParameter("url");
 
 		if (urlString != null && !urlString.equals("")){
 			try {
 				System.out.println(getAppletName() + " url " + urlString);
 				return new URL(getDocumentBase(), urlString);
 			} catch(MalformedURLException e){
 				e.printStackTrace();
 			}			
 		}
 		
 		String resourceString = getParameter("resource");
 
 		if (resourceString != null && !resourceString.equals("")){
 			System.out.println(getAppletName() + " resource " + resourceString);
 			return getClass().getResource(resourceString);			
 		}
 		
 		throw new RuntimeException("No url specified to load otml");
 		
 	}
 	
 	protected OTDatabase openOTDatabase() throws Exception
 	{
 		try {
 			URL url = getDatabaseURL();
 			
 			
 			return viewerHelper.loadOTDatabaseXML(url);
 		}catch (Exception ex) {
 			ex.printStackTrace();
 			throw ex;
 		}				
 	}
 	
 	protected void loadState()
 	{
 		try {
 
 			//Open xmlDB
 			OTDatabase otDB = openOTDatabase();
 			
 			viewerHelper.loadOTrunk(otDB, this);
 
 			masterLoaded = true;
 			master = this;
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}		
 	}
 
 	public void setupView()
 	{
 		System.out.println("" + getAppletName() + " start setupView");
 	
 		// get the otml url
 		try {
 			// look up view container with the frame.
 			otContainer = getViewerHelper().createViewContainerPanel(); 
 	
 			getContentPane().setLayout(new BorderLayout());
 	
 			getContentPane().removeAll();
 			
 			getContentPane().add(otContainer, BorderLayout.CENTER);
 	
 			// call setCurrentObject on that view container with a null
 			// frame
 			OTObject appletObject = getRootOTObject();
 
 			otContainer.setCurrentObject(appletObject);
 			
 			///////////////////////////////
 			String saveUrlString = getParameter("author_state_save_url");
 			if (saveUrlString == null){
 				//Don't save
 			}
 			else{
 				stateAction = new StateHandlerAction();
 				
 				//Save author content button
 				authorSaveButton = new JButton("Save");
 				authorSaveButton.setActionCommand("save_author");
 				authorSaveButton.addActionListener(stateAction);
 				
 				JPanel buttonPanel = new JPanel();
 				buttonPanel.add(authorSaveButton);
 				
 				getContentPane().add(buttonPanel, BorderLayout.SOUTH);
 			}
 			///////////////////////////////
 			
 			System.out.println("" + getAppletName() + " finished setupView");
 			//repaint();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 
 	public OTObject getRootOTObject()
 		throws Exception
 	{
 		// call setCurrentObject on that view container with a null
 		// frame
 		OTObject root = getViewerHelper().getRootObject();
 	
 		String refid = getParameter("refid");
 		OTObject appletObject = root;
 		if(refid != null && refid.length() > 0){
 			appletObject = getOTObject(refid);
 		}
 	
 		return appletObject;
 	}
 	
 	public OTObject getOTObject(String otid) throws Exception {
 		OTID id = getID(otid);
 		return getOTrunk().getOTObject(id);
 	}
 
 	/**
 	 * The applet is considered a master applet if it doesn't have master parameter
 	 * pointing to another applet's name.
 	 * 
 	 * @return
 	 */
 	public boolean isMaster()
 	{
 		// If the master field is set then we have already found our master
 		// so we don't need to check our parameters
 		if(master != null){
 			return master == this;
 		}
 		
 		
 		String masterName = getMasterName();
 		if(masterName != null){
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public String getMasterName()
 	{
 		String masterString = getParameter("master");		
 		if(masterString == null || masterString.equals("")){
 			return null;
 		}
 		
 		return masterString;
 	}
 	
 	public OTAppletViewer getMaster()
 	{
 		if(isMaster()){
 			return this;
 		}
 
 		if(master != null) {
 			return master;
 		}
 
 		String masterName = getMasterName();
 		if(masterName == null){
 			// we don't have a pointer to our master, and we are not a master ourselves
 			// this should never happen.
 			throw new RuntimeException("Non-master applet doesn't have a set master property");
 		}
 		
 		Applet masterApplet = getAppletContext().getApplet(masterName);
 		if(masterApplet instanceof OTAppletViewer &&
 				((OTAppletViewer)masterApplet).isMaster()){
 			master = (OTAppletViewer)masterApplet;
 			return master;
 		}
 
 		// We did not find our master using that simple look up approach
 		// try manually going through all the applets
 		Enumeration<Applet> applets = getAppletContext().getApplets();
 		while(applets.hasMoreElements()){
 			Applet a = applets.nextElement();
 			System.out.println("" + getAppletName() + " found: " + a.getParameter("name"));
 			if(a instanceof OTAppletViewer){
 				OTAppletViewer sibbling = (OTAppletViewer)a;
 				if(sibbling.isMaster() &&
 					sibbling.getAppletName() != null &&
 					sibbling.getAppletName().equals(getMasterName())){
 					master = (OTAppletViewer)a;
 					return master;					
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public boolean isMasterLoaded()
 	{
 		if(isMaster()) {
 			return masterLoaded;
 		}
 	
 		if(getMaster() != null){
 			return getMaster().isMasterLoaded();
 		}
 		
 		return false;
 	}
 
 	public void masterFinishedLoading(OTAppletViewer master)
 	{
 		this.master = master;
 		//we might not be in the correct thread
 		SwingUtilities.invokeLater(new Runnable(){
 			public void run() {
 				setupView();
 			}
 		});
 	}
 
 	public OTViewerHelper getViewerHelper()
 	{
 		if(isMaster()) {
 			return viewerHelper;
 		}
 		
 		// try to get the viewerHelper from the master applet
 		return getMaster().getViewerHelper();
 		
 	}
 	
 	public OTrunk getOTrunk()
 	{
 		if(isMaster()) {
 			return viewerHelper.getOtrunk();
 		}
 	
 		return getMaster().getOTrunk();
 	}
 	
 	public OTViewContainerPanel getOTContainer()
 	{
 		return otContainer;
 	}
 
 	public OTID getID(String id)
 	{
 		if(isMaster()){
			if (id.startsWith("${")) {
				// local id
				id = id.substring(2, id.length()-1);
				// FIXME it's not guaranteed that the db in viewerHelper is the db that contains this local id
				return ((XMLDatabase)viewerHelper.getOtDB()).getOTIDFromLocalID(id);
			} else {
				// assume its a uuid or path id
				return getOTrunk().getOTID(id);
			}
 		}
 
 		OTAppletViewer localMaster = getMaster();
 		if(localMaster == null){
 			return null;
 		}
 		
 		return localMaster.getID(id);
 	}
 
 	public void saveAuthorState()
 	{
 		String saveUrlString = getParameter("author_state_save_url");
 		if (saveUrlString == null){
 			//Don't save
 			System.err.println("No author url specified for saving");
 			return;
 		}
 				
 		try{
 			System.out.println("opening "+saveUrlString);
 			URL saveUrl = new URL(getDocumentBase(), saveUrlString);
 			OTDatabase otDB = viewerHelper.getOtDB();
 
 			String method = getParameter("author_state_save_method");
 			if(method == null || method.length() == 0) {
 				method = "PUT";
 			}
 
 			viewerHelper.saveOTDatabaseXML(otDB, saveUrl, method);			
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	class StateHandlerAction extends AbstractAction
 	{
 		/**
 		 * This is not intended to be serialized, but this removes the warnings
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e)
 		{
 			if (e.getActionCommand().equals("save_author")){
 				saveAuthorState();
 			}
 			
 		}
 		
 	}
 }
