 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.data.webservice.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.data.webservice.CyWebServiceEvent;
 import cytoscape.data.webservice.CyWebServiceException;
 import cytoscape.data.webservice.DatabaseSearchResult;
 import cytoscape.data.webservice.NetworkImportWebServiceClient;
 import cytoscape.data.webservice.WebServiceClient;
 import cytoscape.data.webservice.WebServiceClientManager;
 import cytoscape.data.webservice.CyWebServiceEvent.WSEventType;
 import cytoscape.data.webservice.CyWebServiceEvent.WSResponseType;
 import cytoscape.data.webservice.ui.WebServiceClientGUI.IconSize;
 import cytoscape.layout.Tunable;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.util.ModuleProperties;
 import cytoscape.util.swing.AboutDialog;
 import cytoscape.visual.VisualStyle;
 
 
 /**
  *
  * @author  kono
  */
 public class UnifiedNetworkImportDialog extends JDialog implements PropertyChangeListener {
 	// This is a singleton.
 	private static final UnifiedNetworkImportDialog dialog;
 
 	// Selected web service client ID
 	private String selectedClientID = null;
 
 	// Task to run in the separate thread.
 	private WSNetworkImportTask task;
 
 	// Key is display name, value is actual service name.
 	private Map<String, String> clientNames;
 
 	// Client-Dependent GUI panels
 	private Map<String, Container> serviceUIPanels = new HashMap<String, Container>();
 	
 	//Default icon for about dialog
 	private static final Icon DEF_ICON = new javax.swing.ImageIcon(Cytoscape.class.getResource("images/ximian/stock_internet-32.png"));
 
     private int numDataSources = 0;
     private int numClients = 0;
     
     private boolean cancelFlag = false;
 
     static {
 		dialog = new UnifiedNetworkImportDialog(Cytoscape.getDesktop(), false);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 */
 	public static void showDialog() {
 		dialog.setLocationRelativeTo(Cytoscape.getDesktop());
 		dialog.setVisible(true);
 	}
 
 	/** Creates new form NetworkImportDialog */
 	public UnifiedNetworkImportDialog(Frame parent, boolean modal) {
 		super(parent, modal);
 
 		// Register as listener.
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(this);
 
         initGUI();
 	}
 
     /**
      * Resets the GUI w/ all new info.
      */
     public void resetGUI() {
         this.getContentPane().removeAll();
         initGUI();
     }
 
     private void initGUI() {
         clientNames = new HashMap<String, String>();
 
         List<WebServiceClient> clients = WebServiceClientManager.getAllClients();
 		for (WebServiceClient client : clients) {
 			if (client instanceof NetworkImportWebServiceClient) {
                 numClients++;
             }
 		}
 
         initComponents();
         setDatasource();
 
         //  If we have no data sources, show the install panel
         getContentPane().setLayout(new BorderLayout());
         if (numClients <= 1) {
             this.getContentPane().add(installPanel, BorderLayout.SOUTH);
         }
         if (numClients > 0) {
             this.getContentPane().add(queryPanel, BorderLayout.CENTER);
         }
 
         this.pack();
         setProperty(clientNames.get(datasourceComboBox.getSelectedItem()));
         selectedClientID = clientNames.get(datasourceComboBox.getSelectedItem());
 
         // Initialize GUI panel.
         datasourceComboBoxActionPerformed(null);
     }
 
     /** This method is called from within the constructor to
 	 * initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is
 	 * always regenerated by the Form Editor.
 	 */                   
 	private void initComponents() {
 		mainTabbedPane = new javax.swing.JTabbedPane();
 		searchTermScrollPane = new javax.swing.JScrollPane();
 		searchTermTextPane = new javax.swing.JTextPane();
 		propertyPanel = new javax.swing.JPanel();
 
 		searchTermTextPane.setFont(new java.awt.Font("SansSerif", 0, 12));
 		searchTermTextPane.setText("Please enter search terms...");
 		searchTermScrollPane.setViewportView(searchTermTextPane);
 
 		mainTabbedPane.addTab("Query", searchTermScrollPane);
 
 		org.jdesktop.layout.GroupLayout propertyPanelLayout = new org.jdesktop.layout.GroupLayout(propertyPanel);
 		propertyPanel.setLayout(propertyPanelLayout);
 		propertyPanelLayout.setHorizontalGroup(propertyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                          .add(0, 408, Short.MAX_VALUE));
 		propertyPanelLayout.setVerticalGroup(propertyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                        .add(0, 303, Short.MAX_VALUE));
 
 		propertyScrollPane = new JScrollPane();
 		propertyScrollPane.setViewportView(propertyPanel);
 		mainTabbedPane.addTab("Search Property", propertyScrollPane);
 
 		titlePanel = new javax.swing.JPanel();
 		titleIconLabel = new javax.swing.JLabel();
 		datasourcePanel = new javax.swing.JPanel();
 		datasourceLabel = new javax.swing.JLabel();
 		datasourceComboBox = new javax.swing.JComboBox();
 		aboutButton = new javax.swing.JButton();
 		buttonPanel = new javax.swing.JPanel();
 		searchButton = new javax.swing.JButton();
 		cancelButton = new javax.swing.JButton();
 		clearButton = new javax.swing.JButton();
 		dataQueryPanel = new javax.swing.JPanel();
 
 		setTitle("Import Network from Database");
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
 		titlePanel.setBackground(new java.awt.Color(0, 0, 0));
 
 		titleIconLabel.setIcon(new javax.swing.ImageIcon(Cytoscape.class.getResource("images/networkImportIcon.png"))); // NOI18N
 
 		org.jdesktop.layout.GroupLayout titlePanelLayout = new org.jdesktop.layout.GroupLayout(titlePanel);
 		titlePanel.setLayout(titlePanelLayout);
 		titlePanelLayout.setHorizontalGroup(titlePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                    .add(titleIconLabel,
 		                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                         461,
 		                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
 		titlePanelLayout.setVerticalGroup(titlePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                  .add(titleIconLabel));
 
 		datasourceLabel.setFont(new java.awt.Font("SansSerif", 0, 12));
 		datasourceLabel.setText("Data Source");
 
 		datasourceComboBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					datasourceComboBoxActionPerformed(evt);
 				}
 			});
 
 		aboutButton.setText("About");
 		aboutButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		aboutButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					aboutButtonActionPerformed(evt);
 				}
 			});
 
 		org.jdesktop.layout.GroupLayout datasourcePanelLayout = new org.jdesktop.layout.GroupLayout(datasourcePanel);
 		datasourcePanel.setLayout(datasourcePanelLayout);
 		datasourcePanelLayout.setHorizontalGroup(datasourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                              .add(datasourcePanelLayout.createSequentialGroup()
 		                                                                                        .addContainerGap()
 		                                                                                        .add(datasourceLabel)
 		                                                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                        .add(datasourceComboBox,
 		                                                                                             0,
 		                                                                                             301,
 		                                                                                             Short.MAX_VALUE)
 		                                                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                        .add(aboutButton)
 		                                                                                        .addContainerGap()));
 		datasourcePanelLayout.setVerticalGroup(datasourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                            .add(datasourcePanelLayout.createSequentialGroup()
 		                                                                                      .addContainerGap()
 		                                                                                      .add(datasourcePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                                                .add(datasourceLabel)
 		                                                                                                                .add(aboutButton)
 		                                                                                                                .add(datasourceComboBox,
 		                                                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 		                                                                                      .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                       Short.MAX_VALUE)));
 
 		buttonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
 		searchButton.setText("Search");
 		searchButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					searchButtonActionPerformed(evt);
 				}
 			});
 
 		cancelButton.setText("Cancel");
 		cancelButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					cancelButtonActionPerformed(evt);
 				}
 			});
 
 		clearButton.setText("Clear");
 		clearButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					clearButtonActionPerformed(evt);
 				}
 			});
 
 		org.jdesktop.layout.GroupLayout buttonPanelLayout = new org.jdesktop.layout.GroupLayout(buttonPanel);
 		buttonPanel.setLayout(buttonPanelLayout);
 		buttonPanelLayout.setHorizontalGroup(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                           buttonPanelLayout.createSequentialGroup()
 		                                                                            .addContainerGap()
 		                                                                            .add(clearButton)
 		                                                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
 		                                                                                             225,
 		                                                                                             Short.MAX_VALUE)
 		                                                                            .add(cancelButton)
 		                                                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                            .add(searchButton)
 		                                                                            .addContainerGap()));
 		buttonPanelLayout.setVerticalGroup(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                         buttonPanelLayout.createSequentialGroup()
 		                                                                          .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                           Short.MAX_VALUE)
 		                                                                          .add(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                                .add(searchButton)
 		                                                                                                .add(cancelButton)
 		                                                                                                .add(clearButton))
 		                                                                          .addContainerGap()));
 
 		org.jdesktop.layout.GroupLayout dataQueryPanelLayout = new org.jdesktop.layout.GroupLayout(dataQueryPanel);
 		dataQueryPanel.setLayout(dataQueryPanelLayout);
 		dataQueryPanelLayout.setHorizontalGroup(dataQueryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                            .add(0, 461, Short.MAX_VALUE));
 		dataQueryPanelLayout.setVerticalGroup(dataQueryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                          .add(0, 247, Short.MAX_VALUE));
 
 
         queryPanel = new JPanel();
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(queryPanel);
 		queryPanel.setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                .add(titlePanel,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     Short.MAX_VALUE)
 		                                .add(datasourcePanel,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     Short.MAX_VALUE)
 		                                .add(buttonPanel,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     Short.MAX_VALUE)
 		                                .add(dataQueryPanel,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     Short.MAX_VALUE));
 		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                              .add(layout.createSequentialGroup()
 		                                         .add(titlePanel,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                         .add(datasourcePanel,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                         .add(dataQueryPanel,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              Short.MAX_VALUE)
 		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                         .add(buttonPanel,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 
 		dataQueryPanel.setLayout(new BorderLayout());
         createInstallPanel();
 	} // </editor-fold>
 
     private void createInstallPanel() {
         installPanel = new JPanel();
         installPanel.setLayout(new BorderLayout());
         JLabel titleIconLabel2 = new JLabel();
         titleIconLabel2.setIcon(new ImageIcon
                 (Cytoscape.class.getResource("images/networkImportIcon.png")));
         JPanel titlePanel2 = new JPanel();
         titlePanel2.add(titleIconLabel2);
         titlePanel2.setBackground(new Color(0, 0, 0));
         titlePanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
         if (numClients ==0) {
             installPanel.add(titlePanel2, BorderLayout.NORTH);
         }
 
         JPanel internalPanel = new JPanel();
         internalPanel.setBorder(new EmptyBorder(10,10,10,10));
         internalPanel.setLayout(new BoxLayout(internalPanel, BoxLayout.PAGE_AXIS));
         JTextArea area = new JTextArea (1, 40);
         area.setBorder(new EmptyBorder(0,0,0,0));
         if (numClients == 0) {
             area.setText("There are no network import web service clients installed.");
         } else {
             area.setText("To install additional web service clients, click the install button below.");
         }
         area.setEditable(false);
         area.setOpaque(false);
         area.setAlignmentX(Component.LEFT_ALIGNMENT);
         internalPanel.add(area);
         JButton installButton = new JButton ("Install Web Services Pack");
         installButton.setAlignmentX(Component.LEFT_ALIGNMENT);
         internalPanel.add(Box.createVerticalStrut(15));
         internalPanel.add(installButton);
         installPanel.add(internalPanel, BorderLayout.CENTER);
         createInstallButtonListener(installButton);
     }
 
     private void createInstallButtonListener(JButton installButton) {
         installButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                     cytoscape.data.webservice.util.WebServiceThemeInstall wst
                             = new cytoscape.data.webservice.util.WebServiceThemeInstall
                             (UnifiedNetworkImportDialog.this);
             	boolean displayError = false;
             	try {
             		cytoscape.plugin.DownloadableInfo InstalledTheme = wst.installTheme();
             		if (InstalledTheme == null)
             			displayError = true;
             	} catch (java.io.IOException ioe) {
             		displayError = true;
             		ioe.printStackTrace();
             	} catch (org.jdom.JDOMException jde) {
             		displayError = true;
             		jde.printStackTrace();
             	} finally {
             		if (displayError)
             			JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                                 "Failed to install the WebServiceThemePack",
                                 "Install Error", JOptionPane.ERROR_MESSAGE);
             	}
                 setDatasource();
             }
         });
     }
 
     private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		selectedClientID = clientNames.get(datasourceComboBox.getSelectedItem());
 
 		final CyWebServiceEvent<String> event = buildEvent();
 		System.out.println("Start importing network: " + evt.getActionCommand());
 
 		task = new WSNetworkImportTask(datasourceComboBox.getSelectedItem().toString(), event);
 
 		// Configure JTask Dialog Pop-Up Box
 		final JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(true);
 		jTaskConfig.displayCancelButton(true);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(false);
 
 		// Execute Task in New Thread; pops open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 
 		System.out.println("Network Import from WS Success!");
 		dispose();
 	}
 
 	private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		WebServiceClient wsc = WebServiceClientManager.getClient(selectedClientID);
 		final String clientName = wsc.getDisplayName();
 		final String description = wsc.getDescription();
 		Icon icon = null;
 		if(wsc instanceof WebServiceClientGUI) {
 			icon = ((WebServiceClientGUI)wsc).getIcon(IconSize.FULL);
 		}
 		
 		if(icon == null) {
 			icon = DEF_ICON;
 		}
 		AboutDialog.showDialog(clientName, icon, description);
 	}
 
 	private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		
 	}
 
 	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		dispose();
 	}
 
 	private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		searchTermTextPane.setText("");
 	}
 
 	private void datasourceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		
 		searchTermTextPane.setText("");
 		setProperty(clientNames.get(datasourceComboBox.getSelectedItem()));
 		selectedClientID = clientNames.get(datasourceComboBox.getSelectedItem());
 
 		// Update Panel
 		dataQueryPanel.removeAll();
 
 		final Container gui = serviceUIPanels.get(selectedClientID);
 		if (gui != null) {
 				// This service has custom panel.
 				dataQueryPanel.add(gui, BorderLayout.CENTER);
 				// Hide button panel.
 				buttonPanel.setVisible(false);
 		} else {
 				// Otherwise, use the default panel.
 				System.out.println("No custom GUI.  Use default panel.");
 				dataQueryPanel.add(mainTabbedPane, BorderLayout.CENTER);
 				buttonPanel.setVisible(true);
 		}
 			
 		pack();
 		repaint();
 	}
 
 	private void setProperty(String clientID) {
 		WebServiceClient client = WebServiceClientManager.getClient(clientID);
 
 		if (client == null) {
 			return;
 		}
 
 		ModuleProperties props = client.getProps();
 		List<Tunable> tunables = props.getTunables();
 		propertyPanel = new JPanel(new GridLayout(0, 1));
 
 		for (Tunable tu : tunables) {
 			JPanel p = tu.getPanel();
 			p.setBackground(Color.white);
 
 			if (p != null)
 				propertyPanel.add(p);
 		}
 
 		propertyScrollPane.setViewportView(propertyPanel);
 		pack();
 		repaint();
 	}
 
 	private void setDatasource() {
 		List<WebServiceClient> clients = WebServiceClientManager.getAllClients();
 		for (WebServiceClient client : clients) {
 			if (client instanceof NetworkImportWebServiceClient) {
 				this.datasourceComboBox.addItem(client.getDisplayName());
 				this.clientNames.put(client.getDisplayName(), client.getClientID());
 
 				if (client instanceof WebServiceClientGUI
 				    && (((WebServiceClientGUI) client).getGUI() != null)) {
                     serviceUIPanels.put(client.getClientID(),
 					                     ((WebServiceClientGUI) client).getGUI());
                 }
                 numDataSources++;
             }
 		}
 	}
 
 	private CyWebServiceEvent<String> buildEvent() {
 		final String clientID = clientNames.get(datasourceComboBox.getSelectedItem());
 
 		// Update props here.
 		WebServiceClientManager.getClient(clientID).getProps().updateValues();
 
 		return new CyWebServiceEvent<String>(clientID, WSEventType.SEARCH_DATABASE,
 		                                     searchTermTextPane.getText(),
 		                                     WSEventType.IMPORT_NETWORK);
 	}
 
 	// Variables declaration - do not modify                     
 	private javax.swing.JButton cancelButton;
 	private javax.swing.JComboBox datasourceComboBox;
 	private javax.swing.JLabel datasourceLabel;
 	private javax.swing.JPanel mainPanel;
 	private javax.swing.JTabbedPane mainTabbedPane;
 	private javax.swing.JPanel propertyPanel;
 	private JScrollPane propertyScrollPane;
 	private javax.swing.JButton resetButton;
 	private javax.swing.JButton searchButton;
 	private javax.swing.JScrollPane searchTermScrollPane;
 	private javax.swing.JTextPane searchTermTextPane;
 	private javax.swing.JLabel titleLabel;
 	private javax.swing.JButton aboutButton;
 	private javax.swing.JPanel buttonPanel;
     private JPanel queryPanel;
     private JPanel installPanel;
 
     //    private javax.swing.JButton cancelButton;
 	private javax.swing.JButton clearButton;
 	private javax.swing.JPanel dataQueryPanel;
 
 	//    private javax.swing.JComboBox datasourceComboBox;
 	//    private javax.swing.JLabel datasourceLabel;
 	private javax.swing.JPanel datasourcePanel;
 
 	//    private javax.swing.JButton searchButton;
 	private javax.swing.JLabel titleIconLabel;
 	private javax.swing.JPanel titlePanel;
 
 	// End of variables declaration        
 	class WSNetworkImportTask implements Task {
 		private String serviceName;
 		private CyWebServiceEvent<String> evt;
 		private TaskMonitor taskMonitor;
 
 		public WSNetworkImportTask(final String serviceName, final CyWebServiceEvent<String> evt) {
 			this.evt = evt;
 			this.serviceName = serviceName;
 		}
 
 		public String getTitle() {
 			return "Loading network from web service...";
 		}
 
 		public void halt() {
 			
 			cancelFlag = true;
			Thread.currentThread().interrupt();		
 			taskMonitor.setPercentCompleted(100);
 
 			// Kill the import task.
 			CyWebServiceEvent<String> cancelEvent = new CyWebServiceEvent<String>(serviceName, WSEventType.CANCEL,
                      null,
                      null);
 			try {
 				WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(cancelEvent);
 			} catch (CyWebServiceException e) {
 				// TODO Auto-generated catch block
 				taskMonitor.setException(e, "Cancel Failed.");
 			}
 		}
 
 		public void run() {
 			cancelFlag = false;
 			taskMonitor.setStatus("Loading interactions from " + serviceName);
 			taskMonitor.setPercentCompleted(-1);
 
 			// this even will load the file
 			try {
 				WebServiceClientManager.getCyWebServiceEventSupport().fireCyWebServiceEvent(evt);
 			} catch (Exception e) {
 				taskMonitor.setException(e, "Failed to load network from web service.");
 
 				return;
 			}
 
 			taskMonitor.setPercentCompleted(100);
 			taskMonitor.setStatus("Network successfully loaded.");
 		}
 
 		public void setTaskMonitor(TaskMonitor arg0) throws IllegalThreadStateException {
 			this.taskMonitor = arg0;
 		}
 
 		protected TaskMonitor getTaskMonitor() {
 			return taskMonitor;
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param evt DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent evt) {
 		
 		if(cancelFlag) return;
 		
 		Object resultObject = evt.getNewValue();
 
 		if (evt.getPropertyName().equals(WSResponseType.SEARCH_FINISHED.toString())
 		    && (resultObject != null) && resultObject instanceof DatabaseSearchResult) {
 			DatabaseSearchResult result = (DatabaseSearchResult) resultObject;
 
 			if (result.getNextMove().equals(WSEventType.IMPORT_NETWORK)) {
 				System.out.println("Got search result from: " + evt.getSource() + ", Num result = "
 				                   + result.getResultSize() + ", Source name = "
 				                   + evt.getOldValue());
 
 				String[] message = {
 				                       result.getResultSize() + " records found in "
 				                       + selectedClientID,
 				                       "Do you want to create new network from the search result?"
 				                   };
 				int value = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), message,
 				                                          "Import network",
 				                                          JOptionPane.YES_NO_OPTION);
 
 				if (value == JOptionPane.YES_OPTION) {
 					CyWebServiceEvent<Object> evt2 = new CyWebServiceEvent<Object>(evt.getOldValue()
 					                                                                  .toString(),
 					                                                               WSEventType.IMPORT_NETWORK,
 					                                                               result.getResult());
 
 					try {
 						WebServiceClientManager.getCyWebServiceEventSupport()
 						                       .fireCyWebServiceEvent(evt2);
 					} catch (CyWebServiceException e) {
 						// TODO Auto-generated catch block
 						if (task.getTaskMonitor() != null) {
 							task.getTaskMonitor().setException(e, "Database search failed.");
 						}
 					}
 				}
 			}
 		} else if (evt.getPropertyName().equals(WSResponseType.DATA_IMPORT_FINISHED.toString())) {
 			
 			// If network is empty, just ignore it.
 			if(evt.getNewValue() == null)
 				return;
 			
 			String[] message = { "Network loaded.", "Please enter name for new network:" };
 			String value = JOptionPane.showInputDialog(Cytoscape.getDesktop(), message,
 			                                           "Name new network",
 			                                           JOptionPane.QUESTION_MESSAGE);
 			if (value == null || value.length() == 0 )
 				value = selectedClientID + " Network";
 
 			
 				final CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();
 				Cytoscape.getCurrentNetwork().setTitle(value);
 				Cytoscape.getDesktop().getNetworkPanel().updateTitle(cyNetwork);
 
 				VisualStyle style = ((NetworkImportWebServiceClient) WebServiceClientManager.getClient(selectedClientID))
 				                    .getDefaultVisualStyle();
 				if(style == null) {
 					style = Cytoscape.getVisualMappingManager().getVisualStyle();
 				}
 
 
 
 				if (Cytoscape.getVisualMappingManager().getCalculatorCatalog()
                 .getVisualStyle(style.getName()) == null)
 					Cytoscape.getVisualMappingManager().getCalculatorCatalog().addVisualStyle(style);
 
 				Cytoscape.getVisualMappingManager().setVisualStyle(style);
 
 		}
 	}
 }
