 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.client.gui.PopUpFrames;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.ojim.client.gui.GUIClient;
 import org.ojim.language.Localizer;
 import org.ojim.language.Localizer.TextKey;
 
 public class JoinGameFrame extends JFrame {
 
 	private JLabel ip;
 	private JLabel buttonLabel;
 	private JTextField ipField = new JTextField();;
 	private JTextField portField = new JTextField();;
 	private JPanel panel;
 	private JButton joinButton;
 	private Localizer language;
 	private GUIClient gui;
 	private ActionListener joinButtonListener;
 
 	private Window windowStatus;
 
 	enum Window {
 		directConnection, serverList
 	}
 
 	public JoinGameFrame(Localizer language, final GUIClient gui) {
 		this.language = language;
 		setMinimumSize(new Dimension(400, 100));
 
 		ip = new JLabel(this.language.getText(TextKey.IP));
 		joinButton = new JButton();
 		buttonLabel = new JLabel(this.language.getText(TextKey.JOIN));
 		joinButton.add(buttonLabel);
 
 		ipField.setColumns(20);
 		portField.setColumns(5);
 		panel = new JPanel();
 
 		this.gui = gui;
 
 		joinButtonListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("ACTION PERFORMED");
 				if (ipField.getText().equals("")) {
 					System.out.println("Keine IP");
 				} else {
 
 					try {
 						gui.startIpConnection(ipField.getText(), Integer
 								.parseInt(portField.getText()));
 						setVisible(false);
 					} catch (NumberFormatException e1) {
 						System.out.println("Kein Port");
 					}
 				}
 
 			}
 		};
 
 		joinButton.addActionListener(joinButtonListener);
 
 		panel.add(ip);
 		panel.add(ipField);
 		panel.add(portField);
 		panel.add(joinButton);
 		this.add(panel);
 
 		this.pack();
 	}
 
 	public void showDirectConnection() {
 
 		ip.setText(this.language.getText(TextKey.IP));
 		buttonLabel.setText(this.language.getText(TextKey.JOIN));
 
 		showJoin();
 
 	}
 
 	public void showServerList() {
 		// ip = new JLabel(this.language.getText("serverListText"));
 
 		// panel.add(ip);
 	}
 
 	public void showJoin() {
 
		ip.setText(this.language.getText(TextKey.IP));
		buttonLabel.setText(this.language.getText(TextKey.JOIN));
 		setVisible(true);
 
 	}
 
 	public void setLanguage(Localizer language) {
 		this.language = language;
 		if (isVisible()) {
 			showJoin();
 		}
 
 	}
 
 }
