 /*
 Mjdj MIDI Morph - an extensible MIDI processor and translator.
 Copyright (C) 2010 Confusionists, LLC (www.confusionists.com)
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 
 See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. 
 
 You may contact the author at mjdj_midi_morph [at] confusionists.com
 */
 package com.confusionists.mjdj.morphs.nullConnection;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.swing.*;
 
 import net.miginfocom.swing.MigLayout;
 
 public @SuppressWarnings("serial")
 class UiRow extends JPanel {
 	JComboBox leftBox;
 	JComboBox rightBox;
 	JLabel label;
 	Ui ui;
 	JButton lessButton = new JButton("-");
 	JButton moreButton = new JButton("+");
 	
 	public UiRow(Ui ui, List<String> inDevices, List<String> outDevices) {
 		this.ui = ui;
 		MigLayout mig = new MigLayout();		
 		mig.setColumnConstraints("[][grow, center][][center][center]");
 		this.setLayout(mig);
 		
 		leftBox = new JComboBox(inDevices.toArray());
 		rightBox = new JComboBox(outDevices.toArray());
 		label = new JLabel("send to");
 		label.setAlignmentX(SwingConstants.CENTER);
 		
 		this.add(leftBox);
 		this.add(label);
 		this.add(rightBox);
 		
 		lessButton.setMaximumSize(new Dimension(20, 20));
 		lessButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				UiRow.this.ui.removeRow(UiRow.this);
 			}
 		});
 		
 		moreButton.setMaximumSize(new Dimension(20, 20));
 		this.add(lessButton);
 		this.add(moreButton);
 		moreButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				UiRow.this.ui.addRowAfter(UiRow.this);
 			}
 		});
 		
 	}
 	
 	public void setStatus(List<UiRow> list) {
 		int position = list.indexOf(this);
 		if (position == 0) lessButton.setVisible(false);
 	}
 	
 	
 	public String getLeftName() {
 		return (String)leftBox.getSelectedItem();
 	}
 	
 	public String getRightName() {
 		return (String)rightBox.getSelectedItem();
 	}
 	
 	public Hashtable<String, String> getSerializable() {
 		Hashtable<String, String> retVal = new Hashtable<String, String>();
 		retVal.put("in", getLeftName());
 		retVal.put("out", getRightName());
 		return retVal;
 	}
 	
	private void disableBoxAndAddItemIfDoesNotContain(JComboBox box, String lookingFor) {
 		boolean foundIn = false;
 		// not sure how to do contains
 		for (int i=0; i<box.getItemCount(); i++) {
 			String item = (String)box.getItemAt(i);
 			if (item.equals(lookingFor)) {
 				foundIn = true;
 				break;
 			}
 		}
 		
 		if (!foundIn) {
			box.setEnabled(false);
 			box.addItem(lookingFor);
 		}
 		
		
 	}
 	
 	public void setSerializable(Hashtable<String, String> serialiable) {
 		String lookingForIn = serialiable.get("in");
 		String lookingForOut = serialiable.get("out");

		disableBoxAndAddItemIfDoesNotContain(leftBox, lookingForIn);
		disableBoxAndAddItemIfDoesNotContain(rightBox, lookingForOut);
 		
 		
 		
 		leftBox.setSelectedItem(lookingForIn);
 		rightBox.setSelectedItem(lookingForOut);
 	}
 	
 	
 	@Override
 	public boolean isEnabled() {
 			return (leftBox.isEnabled() && rightBox.isEnabled());
 	}
 }
