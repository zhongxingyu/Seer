 /*******************************************************************************
  * Copyright (C) 2007-2008 The University of Manchester   
  * Copyright (C) 2008 The University of Chicago
  * @author Wei Tan
  * 
  *  Modifications to the initial code base are copyright of their
  *  respective authors, or their employers as appropriate.
  * 
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2.1 of
  *  the License, or (at your option) any later version.
  *    
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *    
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  ******************************************************************************/
 package net.sf.taverna.t2.workbench.cagrid;
 
 //import gov.nih.nci.cagrid.cadsr.client.CaDSRServiceClient;
 //import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.workflow.factory.client.TavernaWorkflowServiceClient;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 //import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.ByteArrayInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.StringReader;
 //import java.util.Date;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Random;
 
 import java.io.File;
 
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.commons.io.FileUtils;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 
 
 //import javax.swing.Action;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 //import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.log4j.Logger;
 import org.cagrid.cds.CDSUtil;
 import org.cagrid.gaards.cds.delegated.stubs.types.DelegatedCredentialReference;
 import org.cagrid.transfer.context.stubs.types.TransferServiceContextReference;
 import org.globus.gsi.GlobusCredential;
 import org.globus.wsrf.encoding.ObjectDeserializer;
 import org.globus.wsrf.encoding.ObjectSerializer;
 import org.jdom.Element;
 import org.jdom.output.DOMOutputter;
 import org.jdom.output.XMLOutputter;
 
 import org.springframework.context.ApplicationContext;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 
 import workflowmanagementfactoryservice.WorkflowOutputType;
 import workflowmanagementfactoryservice.WorkflowPortType;
 import workflowmanagementfactoryservice.WorkflowStatusType;
 
 import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
 
 import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
 import net.sf.taverna.t2.reference.ReferenceService;
 import net.sf.taverna.t2.reference.ReferenceServiceException;
 import net.sf.taverna.t2.reference.T2Reference;
 import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
 import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
 import net.sf.taverna.t2.workflowmodel.Dataflow;
 import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
 import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
 ///import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
 import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
 import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
 import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;
 import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
 import net.sf.taverna.t2.reference.impl.WriteQueueAspect;
 import net.sf.taverna.t2.security.credentialmanager.CMUtil;
 
 public class CaGridComponent extends JPanel implements UIComponentSPI, ActionListener {
 
 	private static final long serialVersionUID = 1L;
 	
 	private static CaGridComponent singletonInstance;
 	
       private static Logger logger = Logger.getLogger(CaGridComponent.class);
       
 	private ReferenceService referenceService;
 	
 	private String referenceContext;
       
       public JComboBox services;
            
       private int row = -1;
       
       private JButton runButton;
       
       private JButton refreshButton;
         
       private JButton caGridTransferDownloadButton;
       
       private JButton removeCaGridRunsButton;
      
       private JButton testButton;
      
       private JList runList;
      
       
       private DefaultListModel runListModel;
       
       //result panel is divided into two parts
       //left part is a list of run
       //right part is a tabbed pane showing results of each run
       
       private JSplitPane resultPanel;
       private JPanel runListPanel;
       //private JPanel outputPanel;
       private JScrollPane outputPanel;
       private JTextArea resultText;
 	
 	private CaGridComponent() {
 		  super(new GridBagLayout());
           addHeader();
           addcagridService();
           addRunButton();
           addRefreshButton();
           addServiceURLButton();
           checkButtons();
           addResultPanel();       
         //force reference service to be constructed now rather than at first workflow run
   		//getReferenceService();
   		
 		
 	}
 	   private void addResultPanel() {
 		   GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = ++row;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0.01;
            c.weighty = 0.01;
            c.anchor = GridBagConstraints.SOUTHEAST;
            c.gridwidth = GridBagConstraints.REMAINDER;
 		    resultPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 			resultPanel.setDividerLocation(200);
 			resultPanel.setBorder(null);
 			runListModel = new DefaultListModel();
 			runList = new JList(runListModel);
 			runList.setBorder(new EmptyBorder(5,5,5,5));
 			runList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
 			runListPanel = new JPanel(new BorderLayout());
 			runListPanel.setBorder(LineBorder.createGrayLineBorder());
 			
 			JLabel worklflowRunsLabel = new JLabel("Workflow Runs");
 			worklflowRunsLabel.setBorder(new EmptyBorder(5,5,5,5));			
 			removeCaGridRunsButton = new JButton("Remove"); // button to remove previous workflow runs
 			removeCaGridRunsButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
 			removeCaGridRunsButton.setEnabled(false);
 			removeCaGridRunsButton.setToolTipText("Remove caGrid run(s)");
 			removeCaGridRunsButton.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent e) {
 					int[] selected = runList.getSelectedIndices();
 					for (int i = selected.length - 1; i >=0; i--){
 						CaGridRun cr = (CaGridRun) runListModel.get(selected[i]);
 						
 						//delete the EPR file
 						File file = new File(new File(System.getProperty("user.home")),cr.workflowid+".epr");
 						file.delete();
 						System.out.println(cr.workflowid+".epr deleted");
 						runListModel.remove(selected[i]);
 					}
 					// Set the first item as selected - if there is one
 					if (runListModel.size() > 0){
 						runList.setSelectedIndex(0);
 					}
 					else{
 						resultText.setText("");
 						resultText.revalidate();
 						
 					}
 				}
 			});
 			runListPanel.add(worklflowRunsLabel, BorderLayout.NORTH);
 			runListPanel.add(removeCaGridRunsButton, BorderLayout.BEFORE_FIRST_LINE);
 			
 			JScrollPane scrollPane = new JScrollPane(runList);
 			scrollPane.setBorder(null);
 			runListPanel.add(scrollPane, BorderLayout.CENTER);
 			// loadWorkflowRunsFromStoredEPRFiles(): add CaGridRun to runList for each EPR
 			// add two buttons: remove and refresh status
 			runList.addListSelectionListener(new ListSelectionListener() {
 				public void valueChanged(ListSelectionEvent e) {
 					if (!e.getValueIsAdjusting()) {
 						Object selection = runList.getSelectedValue();
 						if (selection instanceof CaGridRun) {
 							removeCaGridRunsButton.setEnabled(true);
 							CaGridRun dataflowRun = (CaGridRun) selection;
 							// update status and refresh outputPanel
 							String resultDisplayString = updateResultDisplayString(dataflowRun);
 							resultText.setText(resultDisplayString);
 							resultText.setLineWrap(true);
 							resultText.setEditable(false);
 							outputPanel.revalidate();			
 							revalidate();								
 						}
 						else{
 							removeCaGridRunsButton.setEnabled(false);
 							revalidate();
 							
 						}
 					}
 				}
 			});
 
 			resultPanel.setTopComponent(runListPanel);
 			
 			
 			//each output should be a (xml) string
 			outputPanel = new JScrollPane();
 			
 			resultText = new JTextArea();
 			outputPanel = new JScrollPane(resultText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 			//outputPanel = new JPanel(new BorderLayout());
 			outputPanel.setBorder(LineBorder.createGrayLineBorder());
 			outputPanel.setBackground(Color.WHITE);
 			//outputPanel.add(new JLabel("Workflow Execution Outputs shows here.", JLabel.CENTER), null);
 			resultPanel.setBottomComponent(outputPanel);
 			add(resultPanel,c);
 			
 		
 			
 			//add runComponent to the GUI
 			ArrayList<CaGridRun> loadedRunList = loadWorkflowRunsFromStoredEPRFiles(null,(String)services.getSelectedItem());
 			if(loadedRunList!=null){
 				for(int m = 0; m < loadedRunList.size(); m++){  
 					CaGridRun   cr   =   (CaGridRun)loadedRunList.get(m); 
 					runListModel.add(0, cr);
 				}
 				System.out.println(loadedRunList.size()+" EPR loaded.");
 				runList.setSelectedIndex(0);						
 			}
 			
 	}
 	protected void addHeader() {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = ++row;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            add(new JLabel("Execute workflow as a caGrid Service"), c);
    }
    
    protected void addcagridService() {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = ++row;
            c.anchor = GridBagConstraints.LINE_END;
            c.ipadx = 5;
            c.ipady = 5;
            add(new JLabel("CaGrid Service URL :"), c);
            
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.LINE_START;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = GridBagConstraints.RELATIVE;
            
            services = new JComboBox();
            services.addItem("https://bridled.ci.uchicago.edu:5000/wsrf/services/cagrid/TavernaWorkflowService");
            services.addItem("http://test.cagrid.org//wsrf/services/cagrid/TavernaWorkflowService");
            services.setSelectedIndex(0);
            //set the list of available caGrid workflow services here
            //services.addActionListener(new ServiceSelectionListener());
            services.setEditable(true);
            add(services, c);
            c.weightx = 0;
    
            //Action connectService = new ConnectServiceAction();
            testButton = new JButton("Test Service", WorkbenchIcons.configureIcon);
            testButton.setActionCommand("test");
            testButton.addActionListener(this);
            add(testButton,c);
            
            //Action addService = new NewServiceAction();
            //add(new JButton("addService"), c);
            
            //Action editService = new EditServiceAction();
            //editButton = new JButton("editService");
            //add(editButton, c);
            
            //Action removeService = new RemoveServiceAction();
            //removeButton = new JButton("removeService");
            //add(removeButton, c);
    }
    
    protected void addRunButton() {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = ++row;
            //runButton = new JButton("Run Workflow", WorkbenchIcons.runIcon);
            runButton = new JButton(new RunAsCaGridServiceAction());
            runButton.setEnabled(true);
            add(runButton, c);
    }
    
    private void checkButtons() {
           
            //removeButton.setEnabled(context != null);
            //editButton.setEnabled(context != null);
            
           // refreshButton.invalidate();
           // removeButton.invalidate();
           // editButton.invalidate();
    }
    
    private void addRefreshButton() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        refreshButton = new JButton("Refresh Selected Execution",WorkbenchIcons.refreshIcon);
        refreshButton.setEnabled(true);
        refreshButton.setActionCommand("refresh");
        refreshButton.addActionListener(this);
        add(refreshButton, c);
        
 }
    
    private void addServiceURLButton() {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = row;
            c.anchor = GridBagConstraints.WEST;
            caGridTransferDownloadButton = new JButton("Download Result File", WorkbenchIcons.updateIcon);
            caGridTransferDownloadButton.setEnabled(true);   
            caGridTransferDownloadButton.setActionCommand("download");
            caGridTransferDownloadButton.addActionListener(this);
            add(caGridTransferDownloadButton, c);
            
           
    }
    
    
  //TODO: should be executed in a new thread
    public void runWorkflow(WorkflowInstanceFacade facade, Map<String, T2Reference> inputs) {
 	   //invoke caGrid workflow execution service
 	   String url = (String) services.getSelectedItem();
 		CaGridRun runComponent = new CaGridRun(url,facade.getDataflow().getLocalName());
 		System.out.println("caGridRun initiated with url:"+url);
 		
 		//print out the information of the workflow
 		System.out.println("Workflow is running.");
 		//traverse inputports, get all strings and combine to a String[]
 		Map<String, String> inputMap = new HashMap <String, String>();
 		for (int i=0;i<facade.getDataflow().getInputPorts().size();i++){
 			//TODO: sequence will mess up!
 			DataflowInputPort ip = facade.getDataflow().getInputPorts().get(i);
 			T2Reference inputRef = (T2Reference) inputs.get(ip.getName());
 			//System.out.println(inputRef.toString());
 			//TODO what if the input is NOT given
 			//get a string from a T2Reference
 			String inputString = "";
 			try{
 			inputString = (String) facade.getContext().getReferenceService().renderIdentifier(inputRef,
 					String.class, null);
 			}
 			catch(ReferenceServiceException ex){
 				  System.err.println("Reference ServiceException: " + ex.getMessage());
 				  System.out.println("A possible reason is some input value are NOT given -- we use empty string instead");				
 			}
 			inputMap.put(ip.getName(),inputString);
 			
 		}
 		//traverse the inputMap
 		String inputDisplayString = "The input of this workflow is as follows:\n";
 		for(Iterator it = inputMap.entrySet().iterator(); it.hasNext();) {
 		    Map.Entry entry = (Map.Entry) it.next();
 		    Object key = entry.getKey();
 		    Object value = entry.getValue();
 		    inputDisplayString  = inputDisplayString + (String) key + "---: " +(String) value + "\n";
 		}
 		System.out.println("Input of the workflow is: " + inputDisplayString);
         
         Dataflow dataflow = facade.getDataflow();
 		XMLSerializer serialiser = new XMLSerializerImpl();
 		String[] outputs = null;
 		String resultDisplayString = "";
 		try {
 			Element workflowDef = serialiser.serializeDataflow(dataflow);
 			XMLOutputter outputter = new XMLOutputter();
 			
 			String workflowDefString = outputter.outputString(workflowDef);
 			//org.w3c.dom.Element 
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 			docBuilderFactory.setNamespaceAware(true);
 		    DocumentBuilder docBuilder;
 			docBuilder = docBuilderFactory.newDocumentBuilder();
 			Document wfDoc = docBuilder.parse( new InputSource(new StringReader(workflowDefString)));
 			
 			//write this string into a file to be consumed by Dina's service
 			//move the file into user directory
 			File file = new File(System.getProperty("user.home") + System.getProperty("file.separator")  +dataflow.getLocalName());
 		    FileUtils.writeStringToFile(file, workflowDefString);	
 		    System.out.println("File name: " + file.getAbsolutePath());
 		    //System.out.println("----------------Workflow Definition----------------------");
 			//System.out.println(workflowDefString);
 			//System.out.println("----------------End of Workflow Definition---------------");
 			//String url = "http://128.135.125.17:51000/wsrf/services/cagrid/TavernaWorkflowService";
 			//String url =  (String) services.getSelectedItem();
 	      //add the additional CDS+Transfer configuration dialog
 	      WFProperties wfp = T2Util.parseWorkflow(wfDoc.getDocumentElement());
 	      runComponent.wfp = wfp;
 	      CDSAndTransferConfDialog cDialog = new CDSAndTransferConfDialog(wfp);
 	      cDialog.pack();
 	      cDialog.setLocationRelativeTo(null);
 	      cDialog.setVisible(true);
 	      GlobusCredential proxy = null;
 	      if(wfp.needSecurity||(wfp.needTransfer!=wfp.TRANSFER_NONE)){
 	    	//get EPR from CDS
 		    //get users Globus credential in a certain caGrid, like NCI_Prod or CVRG
 			//System.out.println("Get user's Globus Credential in "+ config.getCaGridName());
 			File secConfigDirectory = CMUtil.getSecurityConfigurationDirectory();
 			String path = secConfigDirectory.getAbsolutePath()+"/cagrid/trusted-certificates";
 			System.setProperty("X509_CERT_DIR",path);					
 			proxy = CDSUtil.getGlobusCredential(cDialog.getCaGridName());
 			runComponent.proxy = proxy;
 			//TODO save it to ${tempdir}/x509up_u_${user.name} to be picked up by Globus?
 			gov.nih.nci.cagrid.common.security.ProxyUtil.saveProxyAsDefault(proxy);
 			
 			if(wfp.needSecurity){
 				System.out.println("Delegate Credential\n" +
 						"caGrid:"+cDialog.getCaGridName()+"\n"+
 						"party:"+cDialog.getParty()+"\n"+
 						"delegationLifeTime:"+cDialog.getDelegationLifetime()+"\n"+
 						"delegationPathLength:"+cDialog.getDelegationPathLength()+"\n"+
 						"issuedCredentialLifeTime:"+cDialog.getIssuedCredentialLifetime()+"\n"+
 						"issuedCredentialPathLength:"+cDialog.getIssuedCredentialPathLength()+"\n"
 				);
 				DelegatedCredentialReference epr = CDSUtil.delegateCredential2(cDialog.getCaGridName(),proxy, 
 						cDialog.getParty(),cDialog.getDelegationLifetime(),cDialog.getDelegationPathLength(),
 						cDialog.getIssuedCredentialLifetime(),cDialog.getIssuedCredentialPathLength());					
 				System.out.println(epr);
 				
 				runComponent.cdsEPR = epr;
 		      }
 	      }
 	  
 	      
 	      
 	      	
 			String workflowName = String.valueOf(runComponent.workflowid);
 			System.out.println("\n1. Running createWorkflow ..");
 			Calendar terminationTime = Calendar.getInstance();
 			terminationTime.add(Calendar.MINUTE, 60);
 			//WMSOutputType wMSOutputElement =  client.createWorkflow(input);
 			System.out.println("workflow "+workflowName+ " at "+file.getAbsolutePath());
 			EndpointReferenceType resourceEPR = TavernaWorkflowServiceClient.setupWorkflow(url, file.getAbsolutePath(), workflowName,terminationTime);
 			//EndpointReferenceType resourceEPR = TavernaWorkflowServiceClient.setupWorkflow(url, "D:/03 T Workbench/Taverna Workbench 2.1.b2/caArray_SVM-090703.t2flow", workflowName);
 			runComponent.workflowEPR = resourceEPR;
 			// 2. Start Workflow Operations Invoked.
 			//
 			if(wfp.needSecurity){
 				
 				
 				TavernaWorkflowServiceClient.setDelegatedCredential(resourceEPR, runComponent.cdsEPR, runComponent.proxy);
 
 			}
 			//TODO upload the input file; how do we know uploading is completed?
 		      if(wfp.needTransfer==wfp.TRANSFER_UPLOAD_ONLY||wfp.needTransfer==wfp.TRANSFER_BOTH){
 		    	 
 		    	  TransferServiceContextReference ref1 = 
 		    		  TavernaWorkflowServiceClient.putInputDataHelper(resourceEPR, cDialog.getFileToUpload(),proxy);
 		    	  //Thread.sleep(5000);
 		    	  
 		      }
 			WorkflowPortType [] inputArgs = null;
 			
 			//String[] inputArgs = null;
 			if(inputMap.size()>0){
 				System.out.println("number of input ports: " + inputMap.size());
 				inputArgs = new WorkflowPortType[inputMap.size()];		
 				int i = 0;
 				for(Iterator it = inputMap.entrySet().iterator(); it.hasNext();) {
 				    Map.Entry entry = (Map.Entry) it.next();
 				    Object key = entry.getKey();
 				    Object value = entry.getValue();
 				    inputArgs[i++] =  new WorkflowPortType((String)key,(String) value);
 				}
 			
 			
 			for(int k=0;k<inputArgs.length;k++){
 				System.out.println("\ninput sent to execution service ..\n");
 				System.out.println("No. "+k+"\t" + inputArgs[k].getPort()+"\t"+inputArgs[k].getValue());
 			}
 			}
 			System.out.println("\n2. Now starting the workflow ..");
 
 			//This method runs the workflow with the resource represented by the EPR.
 			// If there is no inputFile for the workflow, give "null"
 			System.out.println("Created a resource with EPR ..");
 			WorkflowStatusType workflowStatusElement =  TavernaWorkflowServiceClient.startWorkflow(inputArgs, resourceEPR);
 			//System.out.println("NULL input");
 			//WorkflowStatusType workflowStatusElement =  TavernaWorkflowServiceClient.startWorkflow(null, resourceEPR);
 			System.out.println("workflow created");			
 			//System.out.println("Writing EPR to file ..");
 			TavernaWorkflowServiceClient.writeEprToFile(resourceEPR, workflowName);		
 			//add runComponent to the GUI
 			runListModel.add(0, runComponent);
 			runList.setSelectedIndex(0);	
 		} catch (SerializationException e) {
 			
 			e.printStackTrace();
 		} catch (IOException e) {
 			
 			e.printStackTrace();
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		resultDisplayString = "workflow submitted";	
 		//show the outputMap in resultPanel
 		
 				
 		//JTextArea resultText = new JTextArea(resultDisplayString);
 		resultText.setText(resultDisplayString);
 		resultText.setLineWrap(true);
 		resultText.setEditable(false);		
 		resultText.revalidate();
 		//outputPanel.removeAll();
 		outputPanel.revalidate();
 		//outputPanel.add(resultText);	
 		//outputPanel.revalidate();
 		revalidate();
 		
 	}
 	public static CaGridComponent getInstance() {
 		if (singletonInstance == null) {
 			singletonInstance = new CaGridComponent();
 		}
 		return singletonInstance;
 	}
 	
 	 public void actionPerformed(ActionEvent e) {
 	        if("test".equals(e.getActionCommand())){
 	        	//TODO test whether the caGrid execution service is online
 	        	System.out.println("testing caGrid services......successful!");
 	        	      	
 	        }
 	        if(e.getActionCommand().equals("refresh")){
 	        	Object selection = runList.getSelectedValue();
 				if (selection instanceof CaGridRun) {
 					
 					CaGridRun dataflowRun = (CaGridRun) selection;
 					// update status and refresh outputPanel
 					String resultDisplayString = updateResultDisplayString(dataflowRun);
 					resultText.setText(resultDisplayString);
 					resultText.setLineWrap(true);
 					resultText.setEditable(false);
 					outputPanel.revalidate();			
 					revalidate();								
 				}
 	        	
 	        }
 	      //download the output file
 	        if(e.getActionCommand().equals("download")){
 	        	Object selection = runList.getSelectedValue();
 				if (selection instanceof CaGridRun) {
 					CaGridRun dataflowRun = (CaGridRun) selection;
 					//if there is any output file
 					if(dataflowRun.wfp.needTransfer==dataflowRun.wfp.TRANSFER_DOWNLOAD_ONLY||dataflowRun.wfp.needTransfer==dataflowRun.wfp.TRANSFER_BOTH){
 				    	// download only when workflow is finished
 						if(dataflowRun.workflowStatusElement.equals(WorkflowStatusType.Done)){
 				    		File outFile;
 							try {
 								outFile = TavernaWorkflowServiceClient
 								.getOutputDataHelper(dataflowRun.workflowEPR, dataflowRun.proxy,System.getProperty("user.home"));
 								JOptionPane.showMessageDialog(null,
 									    "The workflow output file is downloaed to: "+outFile.getAbsolutePath(), 
 									    "Message",JOptionPane.PLAIN_MESSAGE);
 								System.out.println("caTransfer Output: " + outFile.getAbsolutePath());
 							} catch (MalformedURIException ex) {
 								
 								ex.printStackTrace();
 							} catch (RemoteException ex) {
 								
 								ex.printStackTrace();
 							} catch (IOException ex) {
 								
 								ex.printStackTrace();
 							} catch (Exception ex) {
 								
 								ex.printStackTrace();
 							}
 				    		
 				    	}
 				    	else if(dataflowRun.workflowStatusElement.equals(WorkflowStatusType.Failed)){
 				    		JOptionPane.showMessageDialog(null,
 								    "The workflow has failed.", 
 								    "Message",JOptionPane.PLAIN_MESSAGE);
 				    		
 				    	}
 				    	else {
 				    		JOptionPane.showMessageDialog(null,
 								    "The workflow is running. Please check back later", 
 								    "Message",JOptionPane.PLAIN_MESSAGE);
 				    		
 				    	}				    	  
 				      }
 					//no output file through caGrid transfer
 					else{
 						JOptionPane.showMessageDialog(null,
 							    "This workflow does not have any fileOutputs.", 
 							    "Message",JOptionPane.PLAIN_MESSAGE);
 
 						
 					}
 				}
 				
 	        }
 	    }
 	 //return the ReferenceService -- used to manipulate data
 	 /*
 		public ReferenceService getReferenceService() {
 			String context = ReferenceConfiguration.getInstance().getProperty(
 					ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT);
 			if (!context.equals(referenceContext)) {
 				referenceContext = context;
 				ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
 						context);
 				referenceService = (ReferenceService) appContext
 						.getBean("t2reference.service.referenceService");
 			}
 			return referenceService;
 
 		}
 		*/
 	 public ReferenceService getReferenceService() {
 		 String context = DataManagementConfiguration.getInstance()
 			.getDatabaseContext();
 	if (!context.equals(referenceContext)) {
 		referenceContext = context;
 		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
 				context);
 		referenceService = (ReferenceService) appContext
 				.getBean("t2reference.service.referenceService");
 		try {
 			WriteQueueAspect cache = (WriteQueueAspect) appContext
 					.getBean("t2reference.cache.cacheAspect");
 			//TODO what ?
 			//ReferenceServiceShutdownHook.setReferenceServiceCache(cache);
 		} catch (NoSuchBeanDefinitionException e) {
 			// ReferenceServiceShutdown.setReferenceServiceCache(null);
 		} catch (ClassCastException e) {
 			// ReferenceServiceShutdown.setReferenceServiceCache(null);
 		}
 	}
 	return referenceService;
 
 		}
 
 	
 	public ImageIcon getIcon() {
 		
 		return null;
 	}
 
 	public void onDisplay() {
 		
 		
 	}
 
 	public void onDispose() {
 		
 		
 	}
 	public String updateResultDisplayString(CaGridRun dataflowRun){
 		// update status and refresh outputPanel	
 		String resultDisplayString = "";
 		WorkflowStatusType oldStatusType = dataflowRun.workflowStatusElement;
 		dataflowRun.updateStatus();
 		System.out.println("Workflow Status Updated.");
 		if(dataflowRun.workflowStatusElement.equals(WorkflowStatusType.Done)){
 			// only update if necessary
 			if(!oldStatusType.equals(WorkflowStatusType.Done)){
 				
 				System.out.println("\n Getting back the output string..");
 				WorkflowOutputType workflowOutput = null;
 				try {
 					workflowOutput = TavernaWorkflowServiceClient.getOutput(dataflowRun.workflowEPR);
 				} catch (MalformedURIException e1) {
 					
 					e1.printStackTrace();
 				} catch (RemoteException e1) {
 				
 					e1.printStackTrace();
 				}
 				
 				WorkflowPortType[] outputs = workflowOutput.getOutput();
 					
 				
 				//get a list of xml strings,add to output panel
 				Map<String, String> outputMap = new HashMap <String, String>();
 				for (int j=0;j<outputs.length;j++){
 					System.out.println("output no."+ (j+1));
 					outputMap.put(outputs[j].getPort(),outputs[j].getValue());
 						
 				}
 			
 				dataflowRun.outputMap = outputMap;
 			}
 			if(dataflowRun.outputMap!=null){
 				for(Iterator it = dataflowRun.outputMap.entrySet().iterator(); it.hasNext();) {
 			    Map.Entry entry = (Map.Entry) it.next();
 			    Object key = entry.getKey();
 			    Object value = entry.getValue();
 			    resultDisplayString  = resultDisplayString + (String) key + ":--" + "\n\r" + (String) value + "\n\r";
 			}
 			}
 			else{
 				resultDisplayString = "Workflow succeeds to execute with no output.";
 			}
 		}
 		else if(dataflowRun.workflowStatusElement.equals(WorkflowStatusType.Failed)){
 			resultDisplayString = "Workflow failed to execute.";
 			
 		}
 		else if(dataflowRun.workflowStatusElement.equals(WorkflowStatusType.Active)){
 			resultDisplayString = "Workflow is still running,please check back later.";
 		}
 		return resultDisplayString;
 	}
 	
 	
 	ArrayList<CaGridRun> loadWorkflowRunsFromStoredEPRFiles(File directory, String url) {	
 		File dir = new File(System.getProperty("user.home"));
 		System.out.println("Reading EPR from dir "+ System.getProperty("user.home"));
 		 ArrayList<CaGridRun> caGridRunList = new ArrayList<CaGridRun>();
 		EndpointReferenceType workflowEPR = new EndpointReferenceType();
 		try {
 			ExtensionFilter filter = new ExtensionFilter(".epr");
 			String[] list = dir.list(filter);
 		    File file;
 		    if (list.length == 0) return null;	   
 		    for (int i = 0; i < list.length; i++) {
 		    file = new File(dir, list[i]);
 		      System.out.print(file.getAbsolutePath() + "  loaded : ");
 		      TavernaWorkflowServiceClient client = new TavernaWorkflowServiceClient(url);
 		      workflowEPR = TavernaWorkflowServiceClient.readEprFromFile(file.getAbsolutePath());
 		      String s = file.getName();
 		      s = s.substring(0, s.length()-4);
 		      System.out.println(s);
 		      Date d = new Date(Long.parseLong(s));
 		      	      
 		      CaGridRun runComponent = new CaGridRun(url,"");
 		      runComponent.date = d;
		      runComponent.workflowid = Long.parseLong(s);
 		      runComponent.workflowEPR = workflowEPR;
 		      caGridRunList.add(runComponent);		      
 		    }			   
 		} catch (Exception e1) {
 			
 			e1.printStackTrace();
 		}
 		 return caGridRunList;
 		
 	}
 }
 
 class ExtensionFilter implements FilenameFilter {
 	  private String extension;
 	  public ExtensionFilter( String extension ) {
 	    this.extension = extension;             
 	  }	  
 	  public boolean accept(File dir, String name) {
 	    return (name.endsWith(extension));
 	  }
 	}
 
