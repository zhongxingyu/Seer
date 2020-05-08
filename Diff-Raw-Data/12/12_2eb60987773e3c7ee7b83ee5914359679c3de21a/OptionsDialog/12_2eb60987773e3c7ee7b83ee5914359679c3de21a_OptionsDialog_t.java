 /*
  * This file is part of DeskBin.
  *
  * Copyright (c) 2012, alta189 <http://github.com/alta189/DeskBin/>
  * DeskBin is licensed under the GNU Lesser General Public License.
  *
  * DeskBin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * DeskBin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.deskbin.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 import com.alta189.deskbin.util.UIUtil;
 
 public class OptionsDialog extends JDialog {
 	private JTabbedPane tabs;
 	private List<OptionsPanel> optionsPanels = new ArrayList<OptionsPanel>();
 
 	public OptionsDialog(String title, Map<String, OptionsPanel> optionsPanels) {
 		setTitle(title);
 		setResizable(false);
 		buildUserInterface(optionsPanels);
 		pack();
 		setSize(400, 500);
 	}
 
 	private <T extends OptionsPanel> T wrap(T panel) {
 		optionsPanels.add(panel);
 		return panel;
 	}
 
 	public void buildUserInterface(Map<String, OptionsPanel> optionsPanels) {
 		final OptionsDialog self = this;
 
 		JPanel container = new JPanel();
 		container.setBorder(BorderFactory.createEmptyBorder(8, 8, 5, 8));
 		container.setLayout(new BorderLayout(3, 3));
 
 		tabs = new JTabbedPane();
 		for (String title : optionsPanels.keySet()) {
 			tabs.add(title, wrap(optionsPanels.get(title)));
 		}
 		container.add(tabs, BorderLayout.CENTER);
 
 		JPanel buttonsPanel = new JPanel();
 		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
 		JButton okButton = new JButton("OK");
 		JButton cancelButton = new JButton("Cancel");
 		UIUtil.equalWidth(okButton, cancelButton);
 		buttonsPanel.add(okButton);
 		buttonsPanel.add(cancelButton);
 		container.add(buttonsPanel, BorderLayout.SOUTH);
 
 		okButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				self.save();
 				self.dispose();
 			}
 		});
 
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				self.dispose();
 			}
 		});
 
 		self.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 
 		add(container, BorderLayout.CENTER);
 	}

 	public void save() {
 		for (OptionsPanel panel : optionsPanels) {
 			panel.save();
 		}
 	}
 }
