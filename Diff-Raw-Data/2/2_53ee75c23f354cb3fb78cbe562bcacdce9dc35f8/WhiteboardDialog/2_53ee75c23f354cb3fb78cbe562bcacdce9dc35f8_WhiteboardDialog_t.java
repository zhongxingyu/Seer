 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Queue;
 import java.util.Vector;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JViewport;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.Timer;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 
 import core.abstraction.Controller;
 import core.im.IM;
 import core.whiteboard.WhiteboardPanel;
 
 public class WhiteboardDialog extends MessageDialog implements ListDataListener{
 
 	//string to hold the conversation from the old messagedialog
 		String prevConvo;
 	//panel that contains the whiteboard
 		JPanel whiteboardPanel;
 		private JDialog previousConvoPanel;
 		
 		//default no-args constructor, probably not necessary
 		public WhiteboardDialog(){
 			super();
 			defaultSize = new Dimension(1200,800);
 		}
 		
 		//constructor if you go straight to a WhiteboardDialog (there is no previous MessageDialog)
 		public WhiteboardDialog(String dialogName){
 			super(dialogName);
 		}
 		//constructor for case where you open a whiteboardDialog from an existing messageDialog
 		public WhiteboardDialog(String dialogName, String message, JPanel oldMessagePanel, JTextArea oldInput, JTextPane oldConvo, Vector<Controller> oldListeners){
 			userName = dialogName;
 			prevConvo = message;
 			convoWindow = oldConvo;
 			inputArea = oldInput;
 			messagePanel = oldMessagePanel;
 			conversation = new JDialog(null, dialogName, Dialog.ModalityType.MODELESS);
 			setupGUI();
 			conversation.pack();
 			conversation.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			conversation.setVisible(true);
 			listeners = new Vector<Controller>();
 			for(Controller c : oldListeners){
 				listeners.add(c);
 			}
 		}
 		
 		public static void main(String[] args) {
 			// TODO Auto-generated method stub
 			MessageDialog test = new MessageDialog();
 		}
 
 		private void setupGUI(){
 			//BoxLayout bl = new BoxLayout(conversation.getContentPane(), BoxLayout.Y_AXIS);
 			BoxLayout bl2 = new BoxLayout(messagePanel, BoxLayout.Y_AXIS);
 		//	conversation.setLayout(bl); 
 			whiteboardPanel = new WhiteboardPanel();
 			//setpreferredsize?
 			
 			conversation.addWindowListener(new WindowListener() {
 				
 				@Override
 				public void windowOpened(WindowEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void windowIconified(WindowEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void windowDeiconified(WindowEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void windowDeactivated(WindowEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void windowClosing(WindowEvent e) {
 					// TODO Auto-generated method stub
					startLogs();
 				}
 				
 				@Override
 				public void windowClosed(WindowEvent e) {
 					// TODO Auto-generated method stub
 					for(int i = 0; i < listeners.size(); i++)
 					{
 						listeners.get(i).removeDialog(userName);
 					}
 					
 				}
 				
 				@Override
 				public void windowActivated(WindowEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 			});
 			//TODO: for adding a menu later
 			JMenuBar menuBar = new JMenuBar();
 			menuBar.add(new JMenu("One"));
 			menuBar.add(new JMenu("Two"));
 			menuBar.add(new JMenu("Three"));
 			conversation.setJMenuBar(menuBar);
 			
 			JPanel buttonPanel = new JPanel();
 			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 			
 			JButton sendMsgButton = new JButton("Send Message");
 			sendMsgButton.addActionListener(new ActionListener(){
 			
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					//fix this!
 					sendMessage(inputArea);				
 				}
 			});
 		
 			JButton openWbButton = new JButton("Whiteboard <");
 			openWbButton.addActionListener(new ActionListener(){
 			
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					//go back to message dialog only!
 					MessageDialog md = new MessageDialog(userName, convoWindow.getText(), listeners);
 					//MessageDialog md = new MessageDialog(previousConvoPanel, convoWindow.getText(), listeners);
 					//So for some reason dispose causes this to fail.
 					conversation.setVisible(false);
 				}
 			});
 			
 			buttonPanel.add(sendMsgButton);
 			buttonPanel.add(openWbButton);
 			
 			conversation.add(messagePanel, BorderLayout.WEST);
 			conversation.add(whiteboardPanel, BorderLayout.EAST);
 			conversation.add(buttonPanel, BorderLayout.SOUTH);
 		}
 		public WhiteboardPanel getPanel()
 		{
 			return (WhiteboardPanel) whiteboardPanel;
 		}
 		public void setPreviousPanel(JDialog panel)
 		{
 			previousConvoPanel = panel; 
 		}
 		protected void sendMessage(JTextArea inputField){
 			try
 			{
 				String s = inputField.getText();
 				super.sendMessage(inputField);
 				WhiteboardPanel wp = (WhiteboardPanel) whiteboardPanel;
 				Queue<String>queue = wp.getQueue();
 				for(int i = 0; i < listeners.size(); i++)
 					listeners.get(i).sendQueue(userName, queue);
 				
 				
 			}
 			catch(NullPointerException e)
 			{
 				
 			}
 		}
 		public void applyQueue(Queue<String> q)
 		{
 			super.applyQueue(q);
 			//then apply stuff
 			WhiteboardPanel wp = (WhiteboardPanel) whiteboardPanel;
 			wp.applyQueue(commandQueue);
 		}
 		public void setCommandQueue(Queue<String>q)
 		{
 			super.setCommandQueue(q);
 			applyQueue(q);
 		}
 }
