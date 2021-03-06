 package org.cytoscape.browser.internal;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JToggleButton;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingUtilities;
 
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.application.events.SetCurrentNetworkEvent;
 import org.cytoscape.application.events.SetCurrentNetworkListener;
 import org.cytoscape.equations.EquationCompiler;
 import org.cytoscape.event.CyEventHelper;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNetworkTableManager;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyTableManager;
 import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
 import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
 import org.cytoscape.model.events.NetworkAddedEvent;
 import org.cytoscape.model.events.NetworkAddedListener;
 import org.cytoscape.service.util.CyServiceRegistrar;
 import org.cytoscape.task.TableTaskFactory;
 import org.cytoscape.task.destroy.DeleteTableTaskFactory;
 import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
 import org.cytoscape.util.swing.OpenBrowser;
 import org.cytoscape.work.swing.DialogTaskManager;
 import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
 import org.cytoscape.model.events.TableAboutToBeDeletedListener;
 
 
 public class DefaultTableBrowser extends AbstractTableBrowser implements SetCurrentNetworkListener,
 		NetworkAddedListener, NetworkAboutToBeDestroyedListener, TableAboutToBeDeletedListener {
 
 	private static final long serialVersionUID = 627394119637512735L;
 
 	private final JToggleButton selectionModeButton;
 	
 	private final JComboBox networkChooser;
 	private final Class<? extends CyIdentifiable> objType;
 
 	private boolean rowSelectionMode;
 	private boolean ignoreSetCurrentNetwork = true;
 	
 
 	public DefaultTableBrowser(String tabTitle, Class<? extends CyIdentifiable> objType, CyTableManager tableManager,
 			CyNetworkTableManager networkTableManager, CyServiceRegistrar serviceRegistrar, EquationCompiler compiler,
 			OpenBrowser openBrowser, CyNetworkManager networkManager, DeleteTableTaskFactory deleteTableTaskFactoryService,
 			DialogTaskManager guiTaskManagerServiceRef, PopupMenuHelper popupMenuHelper,
 			CyApplicationManager applicationManager, final CyEventHelper eventHelper){//, final MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryService) {
 		super(tabTitle, tableManager, networkTableManager, serviceRegistrar, compiler, openBrowser, networkManager,
 				deleteTableTaskFactoryService, guiTaskManagerServiceRef, popupMenuHelper, applicationManager, eventHelper);
 
 		this.objType = objType;
 
 		networkChooser = new JComboBox();
 		networkChooser.setRenderer(new NetworkChooserCustomRenderer());
 		networkChooser.addActionListener(this);
 		networkChooser.setMaximumSize(SELECTOR_SIZE);
 		networkChooser.setMinimumSize(SELECTOR_SIZE);
 		networkChooser.setPreferredSize(SELECTOR_SIZE);
 		networkChooser.setSize(SELECTOR_SIZE);
 		networkChooser.setEnabled(false);
 		
 		selectionModeButton = new JToggleButton();
 		selectionModeButton.addActionListener(this);
 		selectionModeButton.setBorder(null);
 		selectionModeButton.setSelected(rowSelectionMode);
 		selectionModeButton.setMargin(new Insets(0, 0, 0, 0));
 		selectionModeButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/table_selection_mode.png")));
 		selectionModeButton.setToolTipText("Change Selection Mode");
 
 		selectionModeButton.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					changeSelectionMode();
 				}
 			});
 		
 		this.attributeBrowserToolBar = new AttributeBrowserToolBar(serviceRegistrar, compiler,
 				deleteTableTaskFactoryService, guiTaskManagerServiceRef, networkChooser, selectionModeButton, objType, applicationManager);//, mapGlobalTableTaskFactoryService);
 
 		add(attributeBrowserToolBar, BorderLayout.NORTH);
 	}
 	
 	private void changeSelectionMode() {
 		rowSelectionMode = selectionModeButton.isSelected();
 		getCurrentBrowserTableModel().setShowAll(rowSelectionMode);
 		getCurrentBrowserTableModel().updateShowAll();
 	}
 	
 	@Override
 	public void actionPerformed(final ActionEvent e) {
 		if (ignoreSetCurrentNetwork)
 			return;
 			
 		final CyNetwork currentNetwork = this.applicationManager.getCurrentNetwork();
 		final CyNetwork network = (CyNetwork) networkChooser.getSelectedItem();
 		
 		if (network != null && !network.equals(currentNetwork) && networkManager.networkExists(network.getSUID())) {
 			applicationManager.setCurrentNetwork(network);
 		}
 	}
 
 	@Override
 	public void handleEvent(final SetCurrentNetworkEvent e) {
 		final CyNetwork currentNetwork = e.getNetwork();
 		
 		if (currentNetwork != null) {
 			if (objType == CyNode.class) {
 				currentTable = currentNetwork.getDefaultNodeTable();
 			} else if (objType == CyEdge.class) {
 				currentTable = currentNetwork.getDefaultEdgeTable();
 			} else {
 				currentTable = currentNetwork.getDefaultNetworkTable();
 			}
 		}
 		
 		getCurrentBrowserTableModel().setShowAll(rowSelectionMode);
 		showSelectedTable();
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				final CyNetwork selectedNetwork = (CyNetwork) networkChooser.getSelectedItem();
				if(selectedNetwork!= null)
					if ((currentNetwork == null && selectedNetwork != null) || !currentNetwork.equals(selectedNetwork)) {
						ignoreSetCurrentNetwork = true;
						networkChooser.setSelectedItem(currentNetwork);
						ignoreSetCurrentNetwork = false;
					}
 			}
 		});
 	}
 
 	@Override
 	public void handleEvent(NetworkAddedEvent e) {
 		final CyNetwork network = e.getNetwork();
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				ignoreSetCurrentNetwork = true;
 				
 				try {
 					networkChooser.addItem(network);
 					
 					if (networkChooser.isEnabled() == false)
 						networkChooser.setEnabled(true);
 				} finally {
 					ignoreSetCurrentNetwork = false;
 				}
 			}
 		});
 	}
 
 	@Override
 	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
 		final CyNetwork network = e.getNetwork();
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				ignoreSetCurrentNetwork = true;
 				
 				try {
 					networkChooser.removeItem(network);
 					
 					if (networkChooser.getItemCount() == 0)
 						networkChooser.setEnabled(false);
 				} finally {
 					ignoreSetCurrentNetwork = false;
 				}
 			}
 		});
 	}
 	
 	
 	@Override
 	public void handleEvent(final TableAboutToBeDeletedEvent e) {
 		final CyTable cyTable = e.getTable();
 		DeleteTable(cyTable);
 	}
 	
 	
 	private static final class NetworkChooserCustomRenderer extends JLabel implements ListCellRenderer {
 
 		private static final long serialVersionUID = 7103666112352192698L;
 
 		@Override
 		public Component getListCellRendererComponent(JList list, Object item, int index, boolean isSelected,
 				boolean hasFocus) {
 			
 			if(item instanceof CyNetwork == false) {
 				this.setText("No Network");
 				return this;
 			}
 			
 			final CyNetwork network = (CyNetwork) item;
 			if(isSelected || hasFocus) {
 				this.setBackground(list.getSelectionBackground());				
 				this.setForeground(list.getSelectionForeground());
 			} else {
 				this.setBackground(list.getBackground());				
 				this.setForeground(list.getForeground());
 			}
 			
 			setOpaque(true);
 			this.setText(network.getRow(network).get(CyNetwork.NAME, String.class));
 			return this;
 		}
 	}
 }
