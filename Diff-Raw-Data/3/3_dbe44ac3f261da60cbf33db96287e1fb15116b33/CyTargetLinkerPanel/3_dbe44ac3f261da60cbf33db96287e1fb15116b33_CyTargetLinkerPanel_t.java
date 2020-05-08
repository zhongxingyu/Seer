 // CyTargetLinker,
 // a Cytoscape plugin to extend biological networks with regulatory interaction
 //
 // Copyright 2011-2013 Department of Bioinformatics - BiGCaT, Maastricht University
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //       http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 package org.cytargetlinker.app.internal.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collections;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.cytargetlinker.app.internal.ExtensionManager;
 import org.cytargetlinker.app.internal.Plugin;
 import org.cytargetlinker.app.internal.Utils;
 import org.cytargetlinker.app.internal.data.DataSource;
 import org.cytargetlinker.app.internal.tasks.ShowHideTaskFactory;
 import org.cytoscape.application.swing.CytoPanelComponent;
 import org.cytoscape.application.swing.CytoPanelName;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.util.swing.CyColorChooser;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.work.TaskIterator;
 
 import com.jgoodies.forms.builder.PanelBuilder;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * 
  * @author martina kutmon
  * side panel in the control panel of Cytoscape (WEST)
  * adds a dropdown box to select which extended network should be visualized
  * shows the number of edges added, possibility to change the color
  * possibility to show/hide a datasource
  * possibility to set an overlap threshold
  *
  */
 public class CyTargetLinkerPanel extends JPanel implements CytoPanelComponent {
 
 	private JPanel contentPanel;
 	private JPanel mainPanel;
 	private Plugin plugin;
 	private JComboBox cbNetworks;
 	private JSpinner thresholdSpinner;
 	
 	private NetworkName currentNetwork;
 	
 	public CyTargetLinkerPanel(Plugin plugin) {
 		super();
 		this.plugin = plugin;
 		plugin.setPanel(this);
 	}
 	
 	@Override
 	public Component getComponent() {
 		mainPanel = new JPanel();
 		mainPanel.setBackground(Color.white);
 		mainPanel.setLayout(new BorderLayout());
 		
 		JPanel top = new JPanel();
 		top.setBackground(Color.WHITE);
 		top.setLayout(new GridLayout(3, 2));
 		top.add(new JLabel());top.add(new JLabel());
 		top.add(new JLabel("Select extended network:"));
 		cbNetworks = new JComboBox(getNetworks());
 		contentPanel = new JPanel();
 		updateContentPanel(null);
 		mainPanel.add(contentPanel, BorderLayout.CENTER);
 		cbNetworks.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				NetworkName name = (NetworkName) cbNetworks.getSelectedItem();
 				
 				if(name != null) {
 					plugin.getCyApplicationManager().setCurrentNetworkView(Utils.getNetworkView(name.getNetwork(), plugin));
 				}
 				updateContentPanel(name);
 			}
 		});
 		top.add(cbNetworks);
 		top.add(new JLabel());top.add(new JLabel());
 		mainPanel.add(top, BorderLayout.NORTH);
 
 		return mainPanel;
 	}
 	
 	public void updateView() {
 		if(currentNetwork != null) {
 			updateContentPanel(currentNetwork);
 		} else {
 			update();
 		}
 	}
 	
 	private void updateContentPanel(NetworkName name) {
 		if(name == null) {
 			contentPanel.removeAll();
 			contentPanel.setBackground(Color.WHITE);
 			contentPanel.setLayout(new BorderLayout());
 			contentPanel.add(new JLabel("<html>Start network extension in<br>\"Apps -> CyTargetLinker -> Extend network\".</html>"), BorderLayout.NORTH);
 			contentPanel.repaint();
 			contentPanel.revalidate();
 		} else {
 			ExtensionManager mgr = plugin.getManagers().get(name.getNetwork());
 			currentNetwork = name;
 			contentPanel.removeAll();
 			contentPanel.setLayout(new BorderLayout());
 			contentPanel.add(getDataColorPanel(mgr), BorderLayout.CENTER);
 			
 			contentPanel.repaint();
 			contentPanel.revalidate();
 		}
 	}
 	
 	private Component getDataColorPanel(ExtensionManager mgr) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 		panel.setBackground(Color.WHITE);
 		
 		panel.add(fillPanel(mgr), BorderLayout.NORTH);
 		
 		JScrollPane pane = new JScrollPane(panel);
 		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		
 		return pane;
 	}
 
 	private Component fillPanel(final ExtensionManager mgr) {
 		CellConstraints cc = new CellConstraints();
 		String rowLayout = "5dlu, pref, 5dlu, pref, 15dlu";
 
 		for(int i = 0; i < mgr.getDatasources().size(); i++) {
 			rowLayout = rowLayout + ",p,5dlu";
 		}
 
 		rowLayout = rowLayout + ", 15dlu, p, 5dlu";
 		
 		FormLayout layout = new FormLayout("pref,10dlu, pref, 10dlu, pref, 10dlu, pref", rowLayout);
 		PanelBuilder builder = new PanelBuilder(layout);
 		builder.setDefaultDialogBorder();
 		builder.setBackground(Color.WHITE);
 		SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
 		thresholdSpinner = new JSpinner(model);
 		thresholdSpinner.setValue(mgr.getThreshold());
 		thresholdSpinner.addChangeListener(new ChangeListener() {
 			
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				Integer i = (Integer) thresholdSpinner.getValue();
 				mgr.setThreshold(i);
 				ShowHideTaskFactory factory = new ShowHideTaskFactory(plugin, mgr);
 				TaskIterator it = factory.createTaskIterator();
 				plugin.getDialogTaskManager().execute(it);
 			}
 		});
 		
 		builder.addLabel("Overlap threshold:", cc.xy(1, 2));
 		builder.add(thresholdSpinner, cc.xy(3, 2));
 		
 		builder.addSeparator("", cc.xyw(1, 5, 7));
 		
 		int rowCount = 6;
 		
 		builder.addLabel("RIN", cc.xy(1, rowCount));
 		builder.addLabel("#", cc.xy(3,rowCount));
 	    builder.addLabel("Color", cc.xy(5, rowCount)); 
 	    builder.addLabel("Show/Hide", cc.xy(7, rowCount));
 	    builder.addSeparator("", cc.xyw(1,rowCount+1,7));
 	    
 	    rowCount = rowCount+2;
 	    Collections.sort(mgr.getDatasources());
 		for(int i = 0; i < mgr.getDatasources().size(); i++) {
 			final DataSource ds = mgr.getDatasources().get(i);
 		    builder.addLabel(ds.getName(), cc.xy(1, rowCount));
 		    
 		    int visible = ds.getEdges().size() - ds.getHiddenEdges().size();
 		    JLabel field = new JLabel(visible + "/" + ds.getEdges().size());
             builder.add(field, cc.xy(3, rowCount));
              
             final JButton button = new JButton();
             button.setBackground(ds.getColor());
            button.setOpaque(true);
             button.addActionListener(new ActionListener() {
             	@Override
 				public void actionPerformed(ActionEvent arg0) {
             		Color c = CyColorChooser.showDialog(plugin.getCySwingApplication().getJFrame(), "Select color for " + ds.getName(), ds.getColor());
             		ds.setColor(c);
 					button.setBackground(c);
					button.setOpaque(true);
 					
 					CyNetworkView view = Utils.getNetworkView(mgr.getNetwork(), plugin);
 					Utils.updateVisualStyle(plugin, view, mgr.getNetwork());
 					ShowHideTaskFactory factory = new ShowHideTaskFactory(plugin, mgr);
 					TaskIterator it = factory.createTaskIterator();
 					plugin.getDialogTaskManager().execute(it);
 				}
 			});
              
 	        builder.add(button, cc.xy(5, rowCount));
 	        	
 	        String[] show = { "Show", "Hide" };
             JComboBox box = new JComboBox(show);
             if(ds.isShow()) {
             	box.setSelectedIndex(0);
             } else {
             	box.setSelectedIndex(1);
             }
             box.addActionListener(new ActionListener() {
             	@Override
 				public void actionPerformed(ActionEvent event) {
 					JComboBox source = (JComboBox) event.getSource();
 						
 					if (source.getSelectedItem().equals("Show")) {
 						ds.setShow(true);
 						ShowHideTaskFactory factory = new ShowHideTaskFactory(plugin, mgr);
 	        			TaskIterator it = factory.createTaskIterator();
 	        			plugin.getDialogTaskManager().execute(it);
 					} else {
 						ds.setShow(false);
 						ShowHideTaskFactory factory = new ShowHideTaskFactory(plugin, mgr);
 						TaskIterator it = factory.createTaskIterator();
 						plugin.getDialogTaskManager().execute(it);
 					}
 				}
 			});
              
             builder.add(box, cc.xy(7, rowCount));
 	        	
 	        rowCount = rowCount+2;
 		}
 		
 		return builder.getPanel();
 	}
 
 	public void update() {
 		cbNetworks.removeAllItems();
 	    cbNetworks.setModel(new DefaultComboBoxModel(getNetworks().toArray()));
 	    cbNetworks.setSelectedIndex(0);
 
 	    cbNetworks.repaint();
 		cbNetworks.revalidate();
 	}
 
 	private Vector<NetworkName> getNetworks() {
 		Vector<NetworkName> list = new Vector<NetworkName>();
 		for(CyNetwork network : plugin.getManagers().keySet()) {
 			String name = network.getRow(network).get(CyNetwork.NAME, String.class);
 			NetworkName nn = new NetworkName(name, network);
 			list.add(nn);
 		}
 		return list;
 	}
 
 	@Override
 	public CytoPanelName getCytoPanelName() {
 		return CytoPanelName.WEST;
 	}
 
 	@Override
 	public Icon getIcon() {
 		return null;
 	}
 
 	@Override
 	public String getTitle() {
 		return "CyTargetLinker";
 	}
 
 	public NetworkName getCurrentNetwork() {
 		return currentNetwork;
 	}
 
 	public void setCurrentNetwork(NetworkName currentNetwork) {
 		this.currentNetwork = currentNetwork;
 	}
 	
 }
