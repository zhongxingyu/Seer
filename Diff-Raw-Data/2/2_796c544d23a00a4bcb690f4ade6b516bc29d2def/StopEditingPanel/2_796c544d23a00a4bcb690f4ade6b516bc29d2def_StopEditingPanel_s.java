 /* gvSIG. Geographic Information System of the Valencian Government
  *
  * Copyright (C) 2007-2008 Infrastructures and Transports Department
  * of the Valencian Government (CIT)
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  *
  */
 
 /*
  * AUTHORS (In addition to CIT):
  * 2009 {Iver T.I.}   {Task}
  */
 
 package com.iver.cit.gvsig.gui.cad.panels;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import org.gvsig.gui.beans.swing.JButton;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.andami.plugins.Extension;
 import com.iver.andami.plugins.IExtension;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.andami.ui.mdiManager.WindowInfo;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.ClearSelectionExtension;
 import com.iver.cit.gvsig.ExportTo;
 import com.iver.cit.gvsig.StopEditing;
 import com.iver.cit.gvsig.fmap.MapControl;
 import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
 import com.iver.cit.gvsig.gui.cad.CADToolAdapter;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.iver.utiles.swing.JComboBox;
 
 /**
  * @author <a href="mailto:jpiera@gvsig.org">Jorge Piera</a>
  */
 public class StopEditingPanel extends JPanel implements ActionListener, IWindow, ExportTo.EndExportToCommand{
 	private JPanel buttonsPanel;
 	private JButton acceptButton;
 	private JButton closeButton;
 	private JComboBox formatCombo;
 	private JRadioButton closeRButton;
 	private JRadioButton exportRButton;
 	private JTextArea massageTextArea;
 	private JScrollPane messageScrollPanel;
 	private JPanel optionsPanel;
 	private JPanel topPanel;
 	private ButtonGroup buttonGroup = null;
 	private WindowInfo windowInfo = null;
 	private StopEditing stopEditing = null;
 	private FLyrVect layer = null;
 	private MapControl mapControl = null;
 
 	public StopEditingPanel(StopEditing stopEditing, FLyrVect layer,
 			MapControl mapControl) {
 		super();
 		initComponents();
 		initLabels();
 		initCombos();
 		initListeners();
 		this.stopEditing = stopEditing;
 		this.layer = layer;
 		this.mapControl = mapControl;
 	}
 
 	private void initListeners() {
 		acceptButton.addActionListener(this);
 		acceptButton.setActionCommand("a");
 		closeButton.addActionListener(this);
 		closeButton.setActionCommand("c");
 	}
 
 	private void initLabels() {
 		closeButton.setText(PluginServices.getText(this, "close"));
 		acceptButton.setText(PluginServices.getText(this, "accept"));
 		massageTextArea.setText(PluginServices.getText(this, "stop_editing_message"));
 		exportRButton.setText(PluginServices.getText(this, "export_to"));
 		closeRButton.setText(PluginServices.getText(this, "stop_editing_close"));
 	}
 
 	private void initCombos(){
 		HashMap<String, Class> formats = StopEditing.getSupportedFormats();
 		Iterator<String> it = formats.keySet().iterator();
 		while (it.hasNext()){
 			formatCombo.addItem(it.next());
 		}
 	}
 
 	private void initComponents() {
 		java.awt.GridBagConstraints gridBagConstraints;
 
 		buttonsPanel = new JPanel();
 		closeButton = new JButton();
 		acceptButton = new JButton();
 		topPanel = new JPanel();
 		messageScrollPanel = new JScrollPane();
 		massageTextArea = new JTextArea();
 		optionsPanel = new JPanel();
 		closeRButton = new JRadioButton();
 		exportRButton = new JRadioButton();
 		formatCombo = new JComboBox();
 		buttonGroup = new ButtonGroup();
 
 		setLayout(new java.awt.BorderLayout());
 
 		buttonsPanel.setLayout(new java.awt.GridBagLayout());
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 2);
 		buttonsPanel.add(closeButton, gridBagConstraints);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 	    gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 2);
 		buttonsPanel.add(acceptButton, gridBagConstraints);
 
 		buttonGroup.add(closeRButton);
 		buttonGroup.add(exportRButton);
 		closeRButton.setSelected(true);
 
 		add(buttonsPanel, java.awt.BorderLayout.SOUTH);
 
 		topPanel.setLayout(new java.awt.GridBagLayout());
 
 		messageScrollPanel.setBorder(null);
 
 		massageTextArea.setColumns(20);
 		massageTextArea.setEditable(false);
 		massageTextArea.setLineWrap(true);
 		massageTextArea.setRows(4);
 		massageTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
 		messageScrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 		messageScrollPanel.setViewportView(massageTextArea);
 
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
 		gridBagConstraints.weightx = 1.0;
 		topPanel.add(messageScrollPanel, gridBagConstraints);
 
 		optionsPanel.setLayout(new java.awt.GridBagLayout());
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridwidth = 2;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		optionsPanel.add(closeRButton, gridBagConstraints);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		optionsPanel.add(exportRButton, gridBagConstraints);
 
 		formatCombo.setModel(new javax.swing.DefaultComboBoxModel());
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		optionsPanel.add(formatCombo, gridBagConstraints);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		gridBagConstraints.weightx = 1.0;
 		topPanel.add(optionsPanel, gridBagConstraints);
 
 		add(topPanel, java.awt.BorderLayout.PAGE_START);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("a")){
 			try {
 				acceptButtonActionPerformed();
 			} catch (Exception e1) {
 				NotificationManager.addError(e1);
 			}
 		}else if (e.getActionCommand().equals("c")){
 			closeButtonActionPerformed();
 		}
 	}
 
 	private void closeButtonActionPerformed() {
 		PluginServices.getMDIManager().closeWindow(this);
 	}
 
 	private void acceptButtonActionPerformed() throws Exception {
 		if (closeRButton.isSelected()){
 			stopEditing();
 			closeButtonActionPerformed();
 		}else if (exportRButton.isSelected()){
 			Object obj = formatCombo.getSelectedItem();
 			if (obj != null){
 				Class extensionClass = StopEditing.getSupportedFormats().get(obj);
 				IExtension extension = PluginServices.getExtension(extensionClass);
 					if (extension != null){
 					closeButtonActionPerformed();
 					IExtension clearExtension = PluginServices.getExtension(ClearSelectionExtension.class);
 					clearExtension.execute("DEL_SELECTION");
 
 					ExportTo.addLayerToStopEdition(layer, this);
 
 					mapControl.setTool("pointSelection");
 
 					extension.execute((String)obj);
 				}
 			}
 		}
 	}
 
 	public void stopEditing() throws Exception {
 		stopEditing.cancelEdition(layer);
 		VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
 		.getSource();
 		vea.getCommandRecord().removeCommandListener(mapControl);
 		if (!(layer.getSource().getDriver() instanceof IndexedShpDriver)){
 			VectorialLayerEdited vle=(VectorialLayerEdited)CADExtension.getEditionManager().getLayerEdited(layer);
 			layer.setLegend((IVectorLegend)vle.getLegend());
 		}
 		layer.setEditing(false);
 	}
 
 	public WindowInfo getWindowInfo() {
 		if (windowInfo == null){
 			windowInfo = new WindowInfo(WindowInfo.MODALDIALOG);
 			windowInfo.setWidth(400);
 			windowInfo.setHeight(150);
 		}
 		return windowInfo;
 	}
 
 	public Object getWindowProfile() {
 		return WindowInfo.DIALOG_PROFILE;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.ExportTo.EndExportToCommand#execute()
 	 */
 	public void execute() throws Exception {
 		stopEditing();
 	}
 }
 
