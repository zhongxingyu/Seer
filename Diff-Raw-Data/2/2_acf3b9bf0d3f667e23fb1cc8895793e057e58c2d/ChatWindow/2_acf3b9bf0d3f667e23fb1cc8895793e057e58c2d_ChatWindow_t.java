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
 
 package org.ojim.client.gui.RightBar;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.util.LinkedList;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 
 import org.ojim.client.gui.GUIClient;
 import org.ojim.client.gui.PlayerColor;
 import org.ojim.client.gui.OLabel.FontLayout;
 import org.ojim.language.Localizer;
 import org.ojim.language.Localizer.TextKey;
 import org.ojim.logic.state.Player;
 
 public class ChatWindow extends JPanel {
 
 	private static final long serialVersionUID = 5179104588584714072L;
 
 	private Localizer language;
 	private LinkedList<ChatMessage> messages = new LinkedList<ChatMessage>();
 	private JTextPane textPane;
 	private JScrollPane scrollPane;
 	private StyledDocument doc;
 	private JTextField textField;
 	private GUIClient gui;
 	private JButton sendButton = new JButton();
 	private JPanel chatWindowPanel = new JPanel();
 
 	// JScrollBar scrollPane = new JScrollBar(JScrollBar.VERTICAL);
 
 	public ChatWindow(Localizer language, GUIClient guiClient) {
 		super();
 
 		this.gui = guiClient;
 
 		this.setLayout(new GridBagLayout());
 
 		chatWindowPanel.setLayout(new BorderLayout());
 
 		textPane = new JTextPane();
 		doc = textPane.getStyledDocument();
 		this.addStylesToDocument(doc);
 		textPane.setEditable(false);
 		scrollPane = new JScrollPane(textPane);
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		scrollPane.setAutoscrolls(true);
 		// textArea.add(new JScrollPane(textArea));
 		// textPane.append("Zeile 1\nZeile 2\nZeile3\nZeile4");
 
 		// Zeigt die erste Zeile an,
 		// indem dort der Caret positioniert wird
 		// textPane.setCaretPosition(4);
 		// scrollPane.setBlockIncrement(1);
 
 		// AdjustmentListener scrollListener = new AdjustmentListener() {
 
 		// @Override
 		// public void adjustmentValueChanged(AdjustmentEvent e) {
 		// //textArea.setText("    New Value is " + e.getValue() + "      ");
 		//
 		// repaint();
 		//
 		// }
 		// };
 
 		// scrollPane.addAdjustmentListener(scrollListener);
 
 		chatWindowPanel.add(scrollPane, BorderLayout.CENTER);
 		// chatWindowPanel.add(scrollPane, BorderLayout.EAST);
 
 		this.add(chatWindowPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
 				GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(
 						0, 0, 0, 0), 0, 0));
 
 		this.revalidate();
 
 		JPanel textPanel = new JPanel();
 
 		textPanel.setLayout(new GridLayout(1, 0));
 
 		textField = new JTextField("");
 		textField.setLayout(new FontLayout());
 
 		this.add(textPanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0,
 				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
 				new Insets(0, 0, 0, 0), 0, 0));
 
 		sendButton.setLayout(new FontLayout());
 
 		ActionListener sendListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				sendMessage();
 
 			}
 		};
 
 		sendButton.setText(language.getText(TextKey.SEND_MESSAGE));
 		sendButton.addActionListener(sendListener);
 
 		textPanel.add(textField);
 
 		textPanel.add(sendButton);
 	}
 
 	private void sendMessage() {
 		if (this.textField.getText().length() > 0) {
 			gui.sendOutMessage(this.textField.getText());
 			this.textField.setText("");
 		}
 	}
 
 	public void clear() {
 
 	}
 
 	public void setLanguage(Localizer language) {
 		this.language = language;
 		sendButton.setText(language.getText(TextKey.SEND_MESSAGE));
 	}
 
 	public void write(ChatMessage chatMessage) {
 		messages.add(chatMessage);
 		try {
 			if (chatMessage.isPrivate) {
 				doc.insertString(doc.getLength(),
 						language.getText(TextKey.PRIVATE_MESSAGE),
 						doc.getStyle("regular"));
 			}
 
			if (chatMessage.player == null) {
 				doc.insertString(doc.getLength(), "server",
 						doc.getStyle("regular"));
 			} else {
 				doc.insertString(doc.getLength(), chatMessage.player.getName(),
 						doc.getStyle("color" + chatMessage.player.getColor()));
 			}
 			doc.insertString(doc.getLength(), " " + chatMessage.message + "\n",
 					doc.getStyle("regular"));
 		} catch (BadLocationException e) {
 			System.err.println("Could not write ChatText into text pane!");
 		}
 		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
 
 	}
 	
 	private void addStylesToDocument(StyledDocument doc) {
 		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
 		
 		Style regular = doc.addStyle("regular", def);
 		
 		for(int i = 0; i < 8; i++) {
 			Style newStyle = doc.addStyle("color" + i, def);
 			StyleConstants.setForeground(newStyle, PlayerColor.getBackGroundColor(i));
 		}
 	}
 
 }
