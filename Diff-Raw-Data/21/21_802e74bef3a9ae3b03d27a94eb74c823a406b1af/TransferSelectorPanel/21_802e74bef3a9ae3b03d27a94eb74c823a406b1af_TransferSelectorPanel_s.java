 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl.transfers.viz;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Collection;
 
 import javax.swing.*;
 
 import ditl.*;
 import ditl.transfers.TransferTrace;
 import ditl.viz.Scene;
 
 
 
 @SuppressWarnings("serial")
 public class TransferSelectorPanel extends JPanel implements ActionListener, ItemListener {
 	
 	protected Store _store;
 	protected JComboBox transferChooser;
 	protected TransferRunner runner;
 	protected JCheckBox enabledBox;
 	protected Scene scene;
 	protected TransferTrace cur_transfers = null;
 	
 	public TransferSelectorPanel(TransferRunner transferRunner, Scene sc){
 		runner = transferRunner;
 		scene = sc;
 		setBorder(BorderFactory.createTitledBorder("Transfers"));
 		
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridy=0; c.gridx = 0;
 		c.anchor = GridBagConstraints.EAST;
 		JLabel enabledLabel = new JLabel("Enabled");
 		gridbag.setConstraints(enabledLabel, c);
 		add(enabledLabel);
 		
 		c.gridx = 1;
 		enabledBox = new JCheckBox();
 		enabledBox.setSelected(false);
 		gridbag.setConstraints(enabledBox, c);
 		add(enabledBox);
 		
 		c.gridy = 1; c.gridx=0;
 		c.gridwidth = 2;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		transferChooser = new JComboBox();
 		gridbag.setConstraints(transferChooser, c);
 		add(transferChooser);
 		
 		setLayout(gridbag);
 		setVisible(false);
 	}
 	
 	public void setStore(Store store){
 		_store = store;
 	}
 	
 	public void load(Collection<Trace<?>> traces) {
 		transferChooser.removeActionListener(this);
 		transferChooser.removeAllItems();
 		enabledBox.removeItemListener(this);
 		if ( ! traces.isEmpty() ){
 			for ( Trace<?> trace : traces ){
 				transferChooser.addItem(trace.name());
 			}
 			enabledBox.setSelected(true);
 			transferChooser.addActionListener(this);
 			transferChooser.setSelectedIndex(0);
 			enabledBox.addItemListener(this);
 			setVisible(true);
 		} else {
 			setVisible(false);
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
         updateLinkTrace();
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent arg0) {
 		transferChooser.setEnabled(enabledBox.isSelected());
 		updateLinkTrace();
 	}
 	
 	private void updateLinkTrace() {
 		String name = "null";
 		try {
 			cur_transfers = null;
 			if ( enabledBox.isSelected() ){
 				name = (String)transferChooser.getSelectedItem();
 				cur_transfers = (TransferTrace)_store.getTrace(name);
 			}
 			runner.setTransferTrace(cur_transfers);
         } catch (Exception ioe){
        	JOptionPane.showMessageDialog(this, "Failed to load links file '"+name+"'", "Warning", JOptionPane.ERROR_MESSAGE);
         }
         scene.repaint();
 	}
 }
